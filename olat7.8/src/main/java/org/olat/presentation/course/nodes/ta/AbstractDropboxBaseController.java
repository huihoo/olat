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

package org.olat.presentation.course.nodes.ta;

import java.util.Date;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ta.Dropbox_EBL;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.lms.user.UserService;
import org.olat.presentation.commons.filechooser.FileChooserController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.render.velocity.VelocityHelper;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 02.09.2004
 * 
 * @author Mike Stock
 */

public abstract class AbstractDropboxBaseController extends BasicController {

    protected ModuleConfiguration config;
    protected CourseNode node;
    protected UserCourseEnvironment userCourseEnv;
    private VelocityContainer myContent;
    private FileChooserController fileChooserController;

    private Link ulButton;
    private CloseableModalController cmc;
    protected Dropbox_EBL dropboxEbl;

    protected AbstractDropboxBaseController(final UserRequest ureq, final WindowControl wControl, ModuleConfiguration config, CourseNode node,
            UserCourseEnvironment userCourseEnv) {
        super(ureq, wControl);
        this.config = config;
        this.node = node;
        this.userCourseEnv = userCourseEnv;
        setBasePackage(DropboxController.class); // 11.07.2011/cg : base-package must be set because projectbroker re-use this class
        dropboxEbl = CoreSpringFactory.getBean(Dropbox_EBL.class);
    }

    protected void init(final UserRequest ureq, final WindowControl wControl, final boolean previewMode, final boolean hasNotification) {
        myContent = createVelocityContainer("dropbox");

        ulButton = LinkFactory.createButton("dropbox.upload", myContent, this);

        if (!previewMode) {
            final int numFiles = dropboxEbl.getNumberOfFilesInDropbbox(getDropboxFolderForIdentity(ureq.getIdentity()));
            if (numFiles > 0) {
                myContent.contextPut("numfiles", new String[] { Integer.toString(numFiles) });
            }

        } else {
            myContent.contextPut("numfiles", "0");
        }
        myContent.contextPut("previewMode", previewMode ? Boolean.TRUE : Boolean.FALSE);

        // notification
        if (!hasNotification || previewMode) {
            myContent.contextPut("hasNotification", Boolean.FALSE);
        }

        putInitialPanel(myContent);
    }

    abstract protected String getDropboxFolderForIdentity(final Identity identity);

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == ulButton) {

            removeAsListenerAndDispose(fileChooserController);
            fileChooserController = new FileChooserController(ureq, getWindowControl(), dropboxEbl.getUploadLimit(ureq.getIdentity(),
                    getDropboxFolderForIdentity(ureq.getIdentity())), true);
            listenTo(fileChooserController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), fileChooserController.getInitialComponent(), true, "Upload");
            listenTo(cmc);

            cmc.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == fileChooserController) {
            cmc.deactivate();
            if (event.equals(Event.DONE_EVENT)) {
                VFSLeaf fIn;
                if (fileChooserController.isFileFromFolder()) {
                    fIn = fileChooserController.getFileSelection();
                } else {
                    fIn = fileChooserController.getUploadedVFSFile();
                }
                boolean success = dropboxEbl.saveUploadedFileInDropbox(fIn, getDropboxFolderForIdentity(ureq.getIdentity()), ureq.getIdentity());

                if (success) {
                    incrementUserAttempts();
                    logFileUploadForUser(fIn.getName(), ureq.getIdentity());

                    final int numFiles = dropboxEbl.getNumberOfFilesInDropbbox(getDropboxFolderForIdentity(ureq.getIdentity()));
                    myContent.contextPut("numfiles", new String[] { Integer.toString(numFiles) });

                    // send email if necessary
                    if (isEmailToStudentWanted()) {
                        sendStudentConfirmation(ureq.getIdentity(), fIn.getName());
                    }

                    if (isEmailToTutorWanted()) {
                        List<Identity> tutors = getTutors();
                        sendTutorConfirmation(tutors, ureq.getIdentity(), fIn.getName());
                    }
                    // assemble confirmation
                    final String confirmation = getConfirmation(ureq, fIn.getName());
                    getWindowControl().setInfo(confirmation.replace("\n", "&#10;").replace("\r", "&#10;").replace("\u2028", "&#10;"));

                    return;
                } else {
                    showInfo("dropbox.upload.failed");
                }
            }
        }
    }

    abstract protected boolean sendStudentConfirmation(Identity identity, String fileName);

    abstract protected boolean sendTutorConfirmation(List<Identity> tutors, Identity identity, String fileName);

    abstract protected List<Identity> getTutors();

    private String getConfirmation(final UserRequest ureq, final String filename) {
        // grab standard text
        final String confirmation = getDefaultConfirmationText();

        final Context c = new VelocityContext();
        final Identity identity = ureq.getIdentity();
        c.put("login", identity.getName());
        c.put("first", getUserService().getUserProperty(identity.getUser(), UserConstants.FIRSTNAME, getLocale()));
        c.put("last", getUserService().getUserProperty(identity.getUser(), UserConstants.LASTNAME, getLocale()));
        c.put("email", getUserService().getUserProperty(identity.getUser(), UserConstants.EMAIL, getLocale()));
        c.put("filename", filename);
        final Date now = new Date();
        final Formatter f = Formatter.getInstance(ureq.getLocale());
        c.put("date", f.formatDate(now));
        c.put("time", f.formatTime(now));

        VelocityHelper vh = CoreSpringFactory.getBean(VelocityHelper.class);
        return vh.evaluateVTL(confirmation, c);
    }

    private void logFileUploadForUser(final String filename, final Identity identity) {
        // log entry for this file
        final UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
        am.appendToUserNodeLog(node, identity, identity, "FILE UPLOADED: " + filename);
    }

    private void incrementUserAttempts() {
        // update attempts counter for this user: one file - one attempts
        final AssessableCourseNode acn = (AssessableCourseNode) node;
        acn.incrementUserAttempts(userCourseEnv);
    }

    protected String getDefaultConfirmationText() {
        final String confirmation = translate("conf.stdtext");
        return confirmation;
    }

    /**
     * Business decision: confirmation email for Task and ProjectBroker nodes always wanted.
     */
    protected boolean isEmailToStudentWanted() {
        return true;
    }

    protected boolean isEmailToTutorWanted() {
        // default is false
        return false;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // DialogBoxController gets disposed by BasicController
        if (fileChooserController != null) {
            fileChooserController.dispose();
            fileChooserController = null;
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    protected ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

}
