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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.repository.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemSuffixFilter;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiContainer;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.lms.wiki.WikiSecurityCallback;
import org.olat.lms.wiki.WikiSecurityCallbackImpl;
import org.olat.lms.wiki.WikiToZipUtils;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.repository.AddFileResourceController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.presentation.repository.WizardCloseResourceController;
import org.olat.presentation.wiki.WikiCreateController;
import org.olat.presentation.wiki.WikiUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Handles the type wiki in the repository
 * <P>
 * Initial Date: May 4, 2006 <br>
 * 
 * @author guido
 */
@Component
public class WikiRepositoryHandler implements RepositoryHandler {

    private static final Logger log = LoggerHelper.getLogger();

    private static final boolean LAUNCHEABLE = true;
    private static final boolean DOWNLOADEABLE = true;
    private static final boolean EDITABLE = false;
    private static final boolean WIZARD_SUPPORT = false;
    private List<String> supportedTypes;

    /**
     * Comment for <code>PROCESS_CREATENEW</code>
     */
    public static final String PROCESS_CREATENEW = "cn";
    public static final String PROCESS_UPLOAD = "pu";

    @Autowired
    ReferenceService referenceService;

    protected WikiRepositoryHandler() {
        supportedTypes = new ArrayList<String>(1);
        supportedTypes.add(WikiResource.TYPE_NAME);
    }

    @Override
    public List<String> getSupportedTypes() {
        return supportedTypes;
    }

    /**
	 */
    @Override
    public boolean supportsDownload(final RepositoryEntry repoEntry) {
        return DOWNLOADEABLE;
    }

    /**
	 */
    @Override
    public boolean supportsLaunch(final RepositoryEntry repoEntry) {
        return LAUNCHEABLE;
    }

    /**
	 */
    @Override
    public boolean supportsEdit(final RepositoryEntry repoEntry) {
        return EDITABLE;
    }

    /**
	 */
    @Override
    public boolean supportsWizard(final RepositoryEntry repoEntry) {
        return WIZARD_SUPPORT;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createWizardController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public MainLayoutController createLaunchController(final OLATResourceable res, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        Controller controller = null;

        // check role
        final boolean isOLatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        final boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
        boolean isResourceOwner = false;
        if (isOLatAdmin) {
            isResourceOwner = true;
        } else {
            final RepositoryService repoMgr = RepositoryServiceImpl.getInstance();
            isResourceOwner = repoMgr.isOwnerOfRepositoryEntry(ureq.getIdentity(), repoMgr.lookupRepositoryEntry(res, true));
        }

        final BusinessControl bc = wControl.getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        // final SubscriptionContext subsContext = new SubscriptionContext(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
        SubscriptionContext noNotificationSubscriptionContext = null;
        final WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isOLatAdmin, isGuestOnly, false, isResourceOwner, noNotificationSubscriptionContext);

        if (ce != null) { // jump to a certain context
            final OLATResourceable ores = ce.getOLATResourceable();
            final String typeName = ores.getResourceableTypeName();
            final String page = typeName.substring("page=".length());
            controller = WikiUIFactory.getInstance().createWikiMainControllerDisposeOnOres(ureq, wControl, res, callback, page);
        } else {
            controller = WikiUIFactory.getInstance().createWikiMainControllerDisposeOnOres(ureq, wControl, res, callback, null);
        }
        // use on column layout
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, controller.getInitialComponent(), null);
        layoutCtr.addDisposableChildController(controller); // dispose content on layout dispose
        return layoutCtr;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        // edit is always part of a wiki
        return null;
    }

    /**
	 */
    @Override
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        final VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(res);
        final VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
        return new VFSMediaResource(wikiZip);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean cleanupOnDelete(final OLATResourceable res) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
        FileResourceManager.getInstance().deleteFileResource(res);
        return true;
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean readyToDelete(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        final String referencesSummary = referenceService.getReferencesToSummary(res, ureq.getLocale());
        if (referencesSummary != null) {
            final Translator translator = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, ureq.getLocale());
            wControl.setError(translator.translate("details.delete.error.references", new String[] { referencesSummary }));
            return false;
        }
        return true;
    }

    /**
	 */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        final FileResourceManager frm = FileResourceManager.getInstance();
        final VFSContainer wikiContainer = WikiManager.getInstance().getWikiContainer(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
        if (wikiContainer == null) {
            // if the wiki container is null, let the WikiManager to create one
            WikiManager.getInstance().getOrLoadWiki(res);
        }
        final OLATResourceable copy = frm.createCopy(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
        final VFSContainer rootContainer = frm.getFileResourceRootImpl(copy);
        // create folders
        final VFSContainer newMediaCont = rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME);
        rootContainer.createChildContainer(WikiManager.VERSION_FOLDER_NAME);
        // copy media files to folders
        final VFSContainer origRootContainer = frm.getFileResourceRootImpl(res);
        final VFSContainer origMediaCont = (VFSContainer) origRootContainer.resolve(WikiContainer.MEDIA_FOLDER_NAME);
        final List<VFSItem> mediaFiles = origMediaCont.getItems();
        for (final Iterator<VFSItem> iter = mediaFiles.iterator(); iter.hasNext();) {
            final VFSLeaf element = (VFSLeaf) iter.next();
            newMediaCont.copyFrom(element);
        }

        // reset properties files to default values
        final VFSContainer wikiCont = (VFSContainer) rootContainer.resolve(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
        final List<VFSItem> leafs = wikiCont.getItems(new VFSItemSuffixFilter(new String[] { WikiManager.WIKI_PROPERTIES_SUFFIX }));
        for (final Iterator<VFSItem> iter = leafs.iterator(); iter.hasNext();) {
            final VFSLeaf leaf = (VFSLeaf) iter.next();
            final WikiPage page = Wiki.assignPropertiesToPage(leaf);
            // reset the copied pages to a the default values
            page.resetCopiedPage();
            WikiManager.getInstance().updateWikiPageProperties(copy, page);
        }

        return copy;
    }

    /**
     * org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public IAddController createAddController(final RepositoryAddCallback callback, final Object userObject, final UserRequest ureq, final WindowControl wControl) {
        if (userObject == null || userObject.equals(WikiRepositoryHandler.PROCESS_UPLOAD)) {
            return new AddFileResourceController(callback, supportedTypes, new String[] { "zip" }, ureq, wControl);
        } else {
            return new WikiCreateController(callback, ureq, wControl);
        }
    }

    @Override
    public Controller createDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return FileResourceManager.getInstance().getDetailsForm(ureq, wControl, res);
    }

    @Override
    public String archive(final Identity archiveOnBehalfOf, final String archivFilePath, final RepositoryEntry repoEntry) {
        final VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(repoEntry.getOlatResource());
        final VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
        final String exportFileName = "del_wiki_" + repoEntry.getOlatResource().getResourceableId() + ".zip";
        final String fullFilePath = archivFilePath + File.separator + exportFileName;

        final File fExportZIP = new File(fullFilePath);
        final InputStream fis = wikiZip.getInputStream();

        try {
            FileUtils.bcopy(wikiZip.getInputStream(), fExportZIP, "archive wiki");
        } catch (final FileNotFoundException e) {
            log.warn("Can not archive wiki repoEntry=" + repoEntry);
        } catch (final IOException ioe) {
            log.warn("Can not archive wiki repoEntry=" + repoEntry);
        } finally {
            FileUtils.closeSafely(fis);
        }
        return exportFileName;
    }

    /**
	 */
    @Override
    public LockResult acquireLock(final OLATResourceable ores, final Identity identity) {
        // nothing to do
        return null;
    }

    /**
	 */
    @Override
    public void releaseLock(final LockResult lockResult) {
        // nothing to do since nothing locked
    }

    /**
	 */
    @Override
    public boolean isLocked(final OLATResourceable ores) {
        return false;
    }

    @Override
    public WizardCloseResourceController createCloseResourceController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry repositoryEntry) {
        throw new AssertException("not implemented");
    }

}
