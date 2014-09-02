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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.ta.Dropbox_EBL;
import org.olat.lms.course.nodes.ta.Returnbox_EBL;
import org.olat.lms.course.nodes.ta.StatusManager;
import org.olat.lms.course.nodes.ta.Task_EBL;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.HtmlStaticPageComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.iframe.IFrameDisplayController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 02.09.2004
 * 
 * @author Mike Stock
 */

public abstract class AbstractDropboxScoringViewBaseController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    protected CourseNode node;
    protected UserCourseEnvironment userCourseEnv;
    private VelocityContainer myContent;
    private Link taskLaunchButton;
    private Link cancelTaskButton;
    private FolderRunController dropboxFolderRunController, returnboxFolderRunController;
    private String assignedTask;
    private StatusForm statusForm;
    private CloseableModalController cmc;
    private IFrameDisplayController iFrameCtr;
    private DialogBoxController dialogBoxController;

    protected Returnbox_EBL returnboxEbl;
    protected Dropbox_EBL dropboxEbl;
    protected Task_EBL taskEbl;

    /**
     * @param ureq
     * @param wControl
     * @param node
     * @param userCourseEnv
     * @param doInit
     *            When true call init-method in constructor.
     */
    protected AbstractDropboxScoringViewBaseController(final UserRequest ureq, final WindowControl wControl, final CourseNode node,
            final UserCourseEnvironment userCourseEnv) {
        super(ureq, wControl);
        returnboxEbl = CoreSpringFactory.getBean(Returnbox_EBL.class);
        dropboxEbl = CoreSpringFactory.getBean(Dropbox_EBL.class);
        taskEbl = CoreSpringFactory.getBean(Task_EBL.class);
        this.node = node;
        this.userCourseEnv = userCourseEnv;
    }

    protected void init(final UserRequest ureq) {
        myContent = createVelocityContainer("dropboxscoring");
        taskLaunchButton = LinkFactory.createButton("task.launch", myContent, this);
        cancelTaskButton = LinkFactory.createButton("task.cancel", myContent, this);
        putInitialPanel(myContent);

        final ModuleConfiguration modConfig = node.getModuleConfiguration();
        final Boolean bValue = (Boolean) modConfig.get(TACourseNode.CONF_TASK_ENABLED);
        myContent.contextPut("hasTask", (bValue != null) ? bValue : new Boolean(false));
        final Boolean hasDropbox = (Boolean) modConfig.get(TACourseNode.CONF_DROPBOX_ENABLED); // configured value
        final Boolean hasDropboxValue = (hasDropbox != null) ? hasDropbox : new Boolean(true);
        myContent.contextPut("hasDropbox", hasDropboxValue);
        final Boolean hasReturnbox = (Boolean) modConfig.get(TACourseNode.CONF_RETURNBOX_ENABLED);
        myContent.contextPut("hasReturnbox", (hasReturnbox != null) ? hasReturnbox : hasDropboxValue);

        final Identity assesseeIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
        VFSContainer namedDropbox = dropboxEbl.createNamedDropboxFolder(getDropboxFilePath(assesseeIdentity), getDropboxRootFolderName(assesseeIdentity.getName()),
                getDropboxVfsSecurityCallback());
        dropboxFolderRunController = new FolderRunController(namedDropbox, false, ureq, getWindowControl());
        listenTo(dropboxFolderRunController);
        myContent.put("dropbox", dropboxFolderRunController.getInitialComponent());

        VFSContainer namedReturnbox = returnboxEbl.createNamedReturnboxFolder(getReturnboxFilePath(assesseeIdentity),
                getReturnboxRootFolderName(assesseeIdentity.getName()), getReturnboxVfsSecurityCallback(getReturnboxFilePath(assesseeIdentity)));
        returnboxFolderRunController = new FolderRunController(namedReturnbox, false, ureq, getWindowControl());
        listenTo(returnboxFolderRunController);
        myContent.put("returnbox", returnboxFolderRunController.getInitialComponent());

        // insert Status Pull-Down Menu depending on user role == author
        final boolean isAuthor = ureq.getUserSession().getRoles().isAuthor();
        final boolean isTutor = userCourseEnv.getCourseEnvironment().getCourseGroupManager()
                .isIdentityCourseCoach(ureq.getIdentity(), userCourseEnv.getCourseEnvironment().getCourseOLATResourceable());
        if (((AssessableCourseNode) node).hasStatusConfigured() && (isAuthor || isTutor)) {
            myContent.contextPut("hasStatusPullDown", Boolean.TRUE);
            statusForm = new StatusForm(ureq, getWindowControl());
            listenTo(statusForm);

            // get identity not from request (this would be an author)
            StatusManager.getInstance().loadStatusFormData(statusForm, node, userCourseEnv);
            myContent.put("statusForm", statusForm.getInitialComponent());
        }

        assignedTask = taskEbl.getAssignedTask(userCourseEnv.getIdentityEnvironment().getIdentity(), userCourseEnv.getCourseEnvironment(), node);
        if (assignedTask != null) {
            myContent.contextPut("assignedtask", assignedTask);
            if (!(assignedTask.toLowerCase().endsWith(".html") || assignedTask.toLowerCase().endsWith(".htm") || assignedTask.toLowerCase().endsWith(".txt"))) {
                taskLaunchButton.setTarget("_blank");
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == taskLaunchButton) {
            if (assignedTask.toLowerCase().endsWith(".html") || assignedTask.toLowerCase().endsWith(".htm") || assignedTask.toLowerCase().endsWith(".txt")) {

                if (getWindowControl().getWindowBackOffice().getWindowManager().isForScreenReader()) {
                    // render content for screenreaders always inline
                    final HtmlStaticPageComponent cpc = new HtmlStaticPageComponent("cpc", new LocalFolderImpl(taskEbl.getTaskFolder(
                            userCourseEnv.getCourseEnvironment(), node)));
                    cpc.setCurrentURI(assignedTask);

                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), translate("close"), cpc);
                    listenTo(cmc);

                    cmc.activate();
                } else {

                    // render content for other users always in iframe
                    removeAsListenerAndDispose(iFrameCtr);
                    iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), new LocalFolderImpl(taskEbl.getTaskFolder(userCourseEnv.getCourseEnvironment(),
                            node)));
                    listenTo(iFrameCtr);

                    iFrameCtr.setCurrentURI(assignedTask);

                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), translate("close"), iFrameCtr.getInitialComponent());
                    listenTo(cmc);

                    cmc.activate();
                }

            } else {
                VFSItem item = taskEbl.getTaskFile(assignedTask, userCourseEnv.getCourseEnvironment(), node);
                if (item != null && item instanceof VFSLeaf) {
                    final VFSLeaf leaf = (VFSLeaf) item;
                    ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(leaf));
                } else {
                    this.showWarning("task.warn.no.task.file");
                }

            }
        } else if (source == cancelTaskButton) {
            // confirm cancel task assignment
            dialogBoxController = this.activateYesNoDialog(ureq, "", translate("task.cancel.reassign"), dialogBoxController);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == dropboxFolderRunController) {
            if (event instanceof FolderEvent) {
                final FolderEvent folderEvent = (FolderEvent) event;
                if (folderEvent.getCommand().equals(FolderEvent.DELETE_EVENT)) {
                    final UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
                    // log entry for this file
                    final Identity coach = ureq.getIdentity();
                    final Identity student = userCourseEnv.getIdentityEnvironment().getIdentity();
                    am.appendToUserNodeLog(node, coach, student, "FILE DELETED: " + folderEvent.getFilename());
                }
            }
        } else if (source == returnboxFolderRunController) {
            if (event instanceof FolderEvent) {
                final FolderEvent folderEvent = (FolderEvent) event;
                if (folderEvent.getCommand().equals(FolderEvent.UPLOAD_EVENT) || folderEvent.getCommand().equals(FolderEvent.NEW_FILE_EVENT)) {
                    logAndSendStudentConfirmationAtReturn(ureq, folderEvent);
                }
            }
        } else if (source == statusForm) {
            if (event == Event.DONE_EVENT) {
                // get identity not from request (this would be an author)
                StatusManager.getInstance().saveStatusFormData(statusForm, node, userCourseEnv);
            }
        } else if (source == dialogBoxController) {
            if (DialogBoxUIFactory.isYesEvent(event) && assignedTask != null) {
                // cancel task assignment, and show "no task assigned to user"
                taskEbl.removeAssignedTask(userCourseEnv, node, userCourseEnv.getIdentityEnvironment().getIdentity(), assignedTask);
                assignedTask = null;
                // update UI
                myContent.contextPut("assignedtask", null);
            }
        }
    }

    protected void logAndSendStudentConfirmationAtReturn(final UserRequest ureq, final FolderEvent folderEvent) {
        final UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
        // log entry for this file
        final Identity coach = ureq.getIdentity();
        final Identity student = userCourseEnv.getIdentityEnvironment().getIdentity();

        am.appendToUserNodeLog(node, coach, student, "FILE UPLOADED: " + folderEvent.getFilename());
        sendStudentConfirmation(folderEvent, am, coach, student);
    }

    protected void sendStudentConfirmation(final FolderEvent folderEvent, final UserNodeAuditManager am, final Identity coach, final Identity student) {
        final String toMail = student.getAttributes().getEmail();

        boolean isMailSucessfullySent = sendStudentConfirmation(student, coach, folderEvent.getFilename());
        if (!isMailSucessfullySent) {
            am.appendToUserNodeLog(node, coach, student, "Confirmation mail send an returnbox failed for :" + toMail + ";");
            log.warn("Could not send email 'returnbox confirmation' to " + student + "with email=" + toMail);
        } else {
            log.info("Sent email 'returnbox confirmation' to " + student + "with email=" + toMail);
        }
    }

    protected abstract boolean sendStudentConfirmation(Identity student, Identity tutor, String fileName);

    protected ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    abstract protected VFSSecurityCallback getDropboxVfsSecurityCallback();

    abstract protected VFSSecurityCallback getReturnboxVfsSecurityCallback(String returnboxRelPath);

    abstract protected String getDropboxRootFolderName(final String assesseeName);

    abstract protected String getReturnboxRootFolderName(final String assesseeName);

    abstract protected String getDropboxFilePath(final Identity assesseeIdentity);

    abstract protected String getReturnboxFilePath(final Identity assesseeIdentity);

}
