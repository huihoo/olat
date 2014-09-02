/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.filebrowser.commands;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.Subscribed;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.folder.FolderNotificationService;
import org.olat.lms.folder.FolderNotificationTypeHandler;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.progressbar.ProgressBar;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * File Upload command class
 * <P>
 * Initial Date: 09.06.2006 <br>
 * 
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CmdUpload extends BasicController implements FolderCommand {

    private static final Logger log = LoggerHelper.getLogger();

    public static final Event FOLDERCOMMAND_CANCELED = new Event("fc_canceled");

    private int status = FolderCommandStatus.STATUS_SUCCESS;

    private VelocityContainer mainVC;
    private VFSContainer currentContainer, inheritingContainer;
    private VFSSecurityCallback secCallback;

    private FolderComponent folderComponent;
    private ProgressBar ubar;
    private String uploadFileName;
    private VFSLeaf vfsNewFile;
    private long quotaKB;
    private int uploadLimitKB;
    private boolean overwritten = false;
    private FileUploadController fileUploadCtr;
    private boolean cancelResetsForm;
    private boolean showMetadata = false;
    private boolean showCancel = true; // default is to show cancel button
    private FolderNotificationService folderNotificationService;
    private FolderNotificationTypeHandler folderNotificationTypeHandler;

    public CmdUpload(UserRequest ureq, WindowControl wControl, boolean showMetadata, boolean showCancel) {
        this(ureq, wControl, showMetadata);
        this.showCancel = showCancel;
    }

    protected CmdUpload(UserRequest ureq, WindowControl wControl, boolean showMetadata) {
        super(ureq, wControl, PackageUtil.createPackageTranslator(FileUploadController.class, ureq.getLocale()));
        folderNotificationService = CoreSpringFactory.getBean(FolderNotificationService.class);
        folderNotificationTypeHandler = CoreSpringFactory.getBean(FolderNotificationTypeHandler.class);
        this.showMetadata = showMetadata;
    }

    @Override
    public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl windowControl, Translator trans) {
        return execute(fc, ureq, windowControl, false);
    }

    public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl windowControl, boolean cancelResetsForm) {
        this.folderComponent = fc;
        this.cancelResetsForm = cancelResetsForm;

        currentContainer = folderComponent.getCurrentContainer();
        if (currentContainer.canWrite() != VFSConstants.YES)
            throw new AssertException("Cannot write to selected folder.");
        // mainVC is the main view

        mainVC = createVelocityContainer("upload");
        // Add progress bar
        ubar = new ProgressBar("ubar");
        ubar.setWidth(200);
        ubar.setUnitLabel("MB");
        mainVC.put(ubar.getComponentName(), ubar);

        // Calculate quota and limits
        long actualUsage = 0;
        quotaKB = Quota.UNLIMITED;
        uploadLimitKB = Quota.UNLIMITED;

        inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
        if (inheritingContainer != null) {
            secCallback = inheritingContainer.getLocalSecurityCallback();
            actualUsage = VFSManager.getUsageKB(inheritingContainer);
            ubar.setActual(actualUsage / 1024);
            if (inheritingContainer.getLocalSecurityCallback().getQuota() != null) {
                quotaKB = secCallback.getQuota().getQuotaKB().longValue();
                uploadLimitKB = (int) secCallback.getQuota().getUlLimitKB().longValue();
            }
        }
        // set wether we have a quota on this folder
        if (quotaKB == Quota.UNLIMITED)
            ubar.setIsNoMax(true);
        else
            ubar.setMax(quotaKB / 1024);
        // set default ulLimit if none is defined...
        if (uploadLimitKB == Quota.UNLIMITED)
            uploadLimitKB = (int) QuotaManager.getInstance().getDefaultQuotaDependingOnRole(ureq.getIdentity()).getUlLimitKB().longValue();

        // Add file upload form
        int remainingQuotaKB = (int) quotaKB - (int) actualUsage;
        if (quotaKB == Quota.UNLIMITED)
            remainingQuotaKB = (int) quotaKB;
        else if (quotaKB - actualUsage < 0)
            remainingQuotaKB = 0;
        else
            remainingQuotaKB = (int) quotaKB - (int) actualUsage;
        removeAsListenerAndDispose(fileUploadCtr);

        // if folder full show error msg
        if (remainingQuotaKB == 0) {
            log.warn("Quota exceeded on folder: " + inheritingContainer + " [quota=" + quotaKB + "KB, used=" + actualUsage + "KB].");
            String supportAddr = WebappHelper.getMailConfig("mailSupport");
            String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
            mainVC.contextPut("overQuota", msg);
            getWindowControl().setError(msg);
            putInitialPanel(mainVC);
            return null;
        }

        fileUploadCtr = new FileUploadController(getWindowControl(), currentContainer, ureq, uploadLimitKB, remainingQuotaKB, null, true, showMetadata, true, showCancel);
        listenTo(fileUploadCtr);
        mainVC.put("fileUploadCtr", fileUploadCtr.getInitialComponent());
        mainVC.contextPut("showFieldset", Boolean.TRUE);

        putInitialPanel(mainVC);
        return this;
    }

    public void refreshActualFolderUsage() {
        long actualUsage = 0;
        quotaKB = Quota.UNLIMITED;
        uploadLimitKB = Quota.UNLIMITED;

        inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
        if (inheritingContainer != null) {
            secCallback = inheritingContainer.getLocalSecurityCallback();
            actualUsage = VFSManager.getUsageKB(inheritingContainer);
            quotaKB = secCallback.getQuota().getQuotaKB().longValue();
            uploadLimitKB = (int) secCallback.getQuota().getUlLimitKB().longValue();
            ubar.setActual(actualUsage / 1024);
            if (fileUploadCtr != null)
                fileUploadCtr.setMaxUploadSizeKB(uploadLimitKB);
        }
    }

    /**
     * Call this to remove the fieldset
     */
    public void hideFieldset() {
        if (mainVC == null) {
            throw new AssertException("Programming error - execute must be called before calling hideFieldset()");
        }
        mainVC.contextPut("showFieldset", Boolean.FALSE);
        if (fileUploadCtr != null) {
            fileUploadCtr.hideTitleAndFieldset();
        }
    }

    /**
     * @return
     */
    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void event(UserRequest ureq, Component source, Event event) {
        // no events to catch
    }

    @Override
    public void event(UserRequest ureq, Controller source, Event event) {
        if (source == fileUploadCtr) {
            // catch upload event
            if (event instanceof FolderEvent && event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
                FolderEvent folderEvent = (FolderEvent) event;
                // Get file from temp folder location
                uploadFileName = folderEvent.getFilename();
                vfsNewFile = (VFSLeaf) currentContainer.resolve(uploadFileName);
                overwritten = fileUploadCtr.isExistingFileOverwritten();
                if (vfsNewFile != null) {
                    fireEvent(ureq, event); // forward the UPLOAD_EVENT
                    notifyFinished(ureq, folderComponent.getCurrentContainerPath() + "/", vfsNewFile);
                } else {
                    showError("file.element.error.general");
                }
            } else if (event.equals(Event.CANCELLED_EVENT)) {
                if (cancelResetsForm) {
                    fileUploadCtr.reset();
                } else {
                    status = FolderCommandStatus.STATUS_CANCELED;
                    fireEvent(ureq, FOLDERCOMMAND_FINISHED);
                }
            }
        }
    }

    private void notifyFinished(UserRequest ureq, String uploadRelPath, VFSLeaf vfsNewFile) {
        VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
        if (secCallback instanceof Subscribed) {
            SubscriptionContext subsContext = ((Subscribed) secCallback).getSubscriptionContext();
            if (subsContext != null) {
                PublishEventTO publishEventTO = folderNotificationTypeHandler.createPublishEventTO(subsContext, folderComponent.courseNodeId(), ureq.getIdentity(),
                        uploadRelPath, vfsNewFile.getName(), EventType.NEW);
                folderNotificationService.publishEvent(publishEventTO);
            }
        }
        // Notify everybody
        fireEvent(ureq, FOLDERCOMMAND_FINISHED);
    }

    /**
     * Get the filename of the uploaded file or NULL if nothing uploaded
     * 
     * @return
     */
    public String getFileName() {
        return this.uploadFileName;
    }

    public Boolean fileWasOverwritten() {
        return this.overwritten;
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    public boolean runsModal() {
        return false;
    }

}
