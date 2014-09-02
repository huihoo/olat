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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.admin.quota.QuotaConstants;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.ta.Solution_EBL;
import org.olat.lms.course.nodes.ta.Task_EBL;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.properties.PersistingCoursePropertyManager;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.nodes.ms.MSCourseNodeEditController;
import org.olat.presentation.course.nodes.ms.MSEditFormController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 30.08.2004
 * 
 * @author Mike Stock Comment: </pre>
 */

public class TACourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String PANE_TAB_CONF_SCORING = "pane.tab.conf.scoring";

    public static final String PANE_TAB_CONF_DROPBOX = "pane.tab.conf.dropbox";

    public static final String PANE_TAB_CONF_TASK = "pane.tab.conf.task";

    public static final String PANE_TAB_CONF_MODULES = "pane.tab.conf.modules";

    public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    public static final String PANE_TAB_SOLUTION = "pane.tab.solution";

    private static final String[] paneKeys = { PANE_TAB_SOLUTION, PANE_TAB_CONF_SCORING, PANE_TAB_CONF_DROPBOX, PANE_TAB_CONF_TASK, PANE_TAB_CONF_MODULES,
            PANE_TAB_ACCESSIBILITY };

    private final ICourse course;
    private final TACourseNode node;
    private final ModuleConfiguration config;
    private final Long courseRepositoryEntryId;

    private final VelocityContainer accessabilityVC, solutionVC;
    private final VelocityContainer editModules, editTask, editDropbox, editScoring;
    private TabbedPane myTabbedPane;
    private int taskTabPosition, dropboxTabPosition, scoringTabPosition, solutionTabPosition;
    private final ModulesForm modulesForm;
    private final TaskFormController taskController;
    private final ConfirmationSettingForm dropboxForm;
    private final MSEditFormController scoringController;
    private FolderRunController frc;
    private final ConditionEditController taskConditionC, dropConditionC, returnboxConditionC, scoringConditionC, solutionConditionC;
    private final boolean hasLogEntries;
    private DialogBoxController dialogBoxController;

    private final Link btfButton;
    private final Link editScoringConfigButton;
    private final Link vfButton;

    private List<Identity> identitiesToBeNotified;

    /**
     * @param ureq
     * @param wControl
     * @param course
     * @param node
     * @param groupMgr
     */
    public TACourseNodeEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final TACourseNode node,
            final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        super(ureq, wControl);

        this.node = node;
        // o_clusterOk by guido: save to hold reference to course inside editor
        this.course = course;
        this.config = node.getModuleConfiguration();
        final Translator newTranslator = new PackageTranslator(PackageUtil.getPackageName(TACourseNodeEditController.class), ureq.getLocale(), new PackageTranslator(
                PackageUtil.getPackageName(MSCourseNodeEditController.class), ureq.getLocale()));
        setTranslator(newTranslator);
        courseRepositoryEntryId = euce.getCourseEditorEnv().getRepositoryEntryId();

        accessabilityVC = this.createVelocityContainer("edit");
        // Task precondition
        taskConditionC = new ConditionEditController(ureq, getWindowControl(), groupMgr, node.getConditionTask(), "taskConditionForm",
                AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node), euce);
        this.listenTo(taskConditionC);
        if (((Boolean) config.get(TACourseNode.CONF_TASK_ENABLED)).booleanValue()) {
            accessabilityVC.put("taskCondition", taskConditionC.getInitialComponent());
        }

        // DropBox precondition
        dropConditionC = new ConditionEditController(ureq, getWindowControl(), groupMgr, node.getConditionDrop(), "dropConditionForm",
                AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node), euce);
        this.listenTo(dropConditionC);
        final Boolean hasDropboxValue = ((Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLED) != null) ? (Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLED)
                : false;
        if (hasDropboxValue) {
            accessabilityVC.put("dropCondition", dropConditionC.getInitialComponent());
        }

        // returnbox precondition - use dropbox condition if none defined for new Boolean(task.isSelected(0)));boolean returnBoxEnabled = (returnBoxConf !=null) ?
        // ((Boolean) returneturnbox
        final Condition dropboxCondition = node.getConditionDrop();
        final Condition returnboxCondition = node.getConditionReturnbox();
        if (dropboxCondition != null && returnboxCondition != null && returnboxCondition.getConditionExpression() == null) {
            // old courses: use ConditionExpression from dropbox if none defined for returnbox
            final Condition dropboxConditionCopy = dropboxCondition.cloneCondition();
            dropboxConditionCopy.setConditionId(TACourseNode.ACCESS_RETURNBOX);
            node.setConditionReturnbox(dropboxConditionCopy);
        }
        returnboxConditionC = new ConditionEditController(ureq, getWindowControl(), groupMgr, returnboxCondition, "returnboxConditionForm",
                AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node), euce);
        this.listenTo(returnboxConditionC);
        final Object returnBoxConf = config.get(TACourseNode.CONF_RETURNBOX_ENABLED);
        // use the dropbox config if none specified for the return box
        final boolean returnBoxEnabled = (returnBoxConf != null) ? ((Boolean) returnBoxConf).booleanValue() : hasDropboxValue;
        if (returnBoxEnabled) {
            accessabilityVC.put("returnboxCondition", returnboxConditionC.getInitialComponent());
        }

        // Scoring precondition
        scoringConditionC = new ConditionEditController(ureq, getWindowControl(), groupMgr, node.getConditionScoring(), "scoringConditionForm",
                AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node), euce);
        this.listenTo(scoringConditionC);
        if (((Boolean) config.get(TACourseNode.CONF_SCORING_ENABLED)).booleanValue()) {
            accessabilityVC.put("scoringCondition", scoringConditionC.getInitialComponent());
        }

        // SolutionFolder precondition
        solutionConditionC = new ConditionEditController(ureq, getWindowControl(), groupMgr, node.getConditionSolution(), "solutionConditionForm",
                AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node), euce);
        this.listenTo(solutionConditionC);
        if (((Boolean) config.get(TACourseNode.CONF_SOLUTION_ENABLED)).booleanValue()) {
            accessabilityVC.put("solutionCondition", solutionConditionC.getInitialComponent());
        }

        // Modules config
        editModules = this.createVelocityContainer("editModules");
        modulesForm = new ModulesForm(ureq, wControl, config);
        listenTo(modulesForm);
        editModules.put("modulesform", modulesForm.getInitialComponent());

        // Task config
        editTask = this.createVelocityContainer("editTask");
        btfButton = LinkFactory.createButton("taskfolder", editTask, this);

        taskController = new TaskFormController(ureq, wControl, config);
        listenTo(taskController);
        final String taskFolderPath = (String) node.getModuleConfiguration().get(TACourseNode.CONF_TASK_FOLDER_REL_PATH);
        if (taskFolderPath == null) {
            editTask.contextPut("taskfolder", translate("taskfolder.empty"));
        } else {
            editTask.contextPut("taskfolder", taskFolderPath);
        }
        editTask.put("taskform", taskController.getInitialComponent());

        // DropBox config
        editDropbox = this.createVelocityContainer("editDropbox");
        dropboxForm = new ConfirmationSettingForm(ureq, wControl, isConfirmationEnabled(), "fieldset.dropbox.title", "form.dropbox.enablemail");
        listenTo(dropboxForm);
        editDropbox.put("dropboxform", dropboxForm.getInitialComponent());

        // Scoring config
        editScoring = this.createVelocityContainer("editScoring");
        editScoringConfigButton = LinkFactory.createButtonSmall("scoring.config.enable.button", editScoring, this);

        scoringController = new MSEditFormController(ureq, wControl, config);
        listenTo(scoringController);
        editScoring.put("scoringController", scoringController.getInitialComponent());

        // if there is already user data available, make for read only
        final UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
        hasLogEntries = am.hasUserNodeLogs(node);
        editScoring.contextPut("hasLogEntries", new Boolean(hasLogEntries));
        if (hasLogEntries) {
            scoringController.setDisplayOnly(true);
        }
        // Initialstate
        editScoring.contextPut("isOverwriting", new Boolean(false));

        // Solution-Tab
        solutionVC = this.createVelocityContainer("editSolutionFolder");
        vfButton = LinkFactory.createButton("link.solutionFolder", solutionVC, this);

    }

    private boolean isConfirmationEnabled() {
        Object isConfirmationEnabled = node.getModuleConfiguration().get(TACourseNode.CONF_DROPBOX_CONFIRMATION_REQUESTED);
        if (isConfirmationEnabled != null) {
            return (Boolean) isConfirmationEnabled;
        }
        return false;
    }

    private VFSSecurityCallback getTaskFolderSecCallback(final String relPath) {
        // check if any tasks assigned yet
        final CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
        final List assignedProps = cpm.listCourseNodeProperties(node, null, null, Task_EBL.PROP_ASSIGNED);
        // return new TaskFolderCallback(relPath, (assignedProps.size() > 0));
        return new TaskFolderCallback(relPath, false); // do not look task folder
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (log.isDebugEnabled()) {
            log.debug("event source=" + source + " " + event.toString());
        }
        if (source == btfButton) {
            // TODO: ORID-1007 Move Business-Logic to Task_Ebl
            // check if there are already assigned tasks
            final CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
            final List assignedProps = cpm.listCourseNodeProperties(node, null, null, Task_EBL.PROP_ASSIGNED);
            if (assignedProps.size() == 0) {
                // no task assigned
                final String relPath = TACourseNode.getTaskFolderPathRelToFolderRoot(course, node);
                final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(relPath, null);
                final OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(translate("taskfolder"), rootFolder);
                namedFolder.setLocalSecurityCallback(getTaskFolderSecCallback(relPath));
                frc = new FolderRunController(namedFolder, false, ureq, getWindowControl());
                // listenTo(frc);
                frc.addControllerListener(this);
                final CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("folder.close"), frc.getInitialComponent());
                cmc.activate();
            } else {
                // already assigned task => open dialog with warn
                final String[] args = new String[] { new Integer(assignedProps.size()).toString() };
                dialogBoxController = this.activateOkCancelDialog(ureq, "", getTranslator().translate("taskfolder.overwriting.confirm", args), dialogBoxController);
                final List cs = new ArrayList();
                cs.add(dialogBoxController);
            }
        } else if (source == vfButton) {
            if (log.isDebugEnabled()) {
                log.debug("Event for sampleVC");
            }
            // switch to new dialog
            Solution_EBL solutionEbl = CoreSpringFactory.getBean(Solution_EBL.class);
            final OlatNamedContainerImpl namedContainer = solutionEbl.getNodeFolderContainer(course.getCourseEnvironment(), node);
            final CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"), new FolderRunController(namedContainer, false,
                    ureq, getWindowControl()).getInitialComponent());
            cmc.activate();

            if (log.isDebugEnabled()) {
                log.debug("Switch to sample folder dialog : DONE");
            }
            return;
        } else if (source == editScoringConfigButton) {
            scoringController.setDisplayOnly(false);
            editScoring.contextPut("isOverwriting", new Boolean(true));
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == modulesForm) {
            final boolean onoff = event.getCommand().endsWith("true");
            if (event.getCommand().startsWith("task")) {
                config.set(TACourseNode.CONF_TASK_ENABLED, new Boolean(onoff));
                myTabbedPane.setEnabled(taskTabPosition, onoff);
                if (onoff) {
                    accessabilityVC.put("taskCondition", taskConditionC.getInitialComponent());
                } else {
                    accessabilityVC.remove(taskConditionC.getInitialComponent());
                    taskConditionC.getCondition().clearEasyConfig();
                    taskConditionC.getCondition().setConditionExpression(taskConditionC.getCondition().getConditionFromEasyModeConfiguration());
                    node.setConditionTask(taskConditionC.getCondition());
                }
            } else if (event.getCommand().startsWith("dropbox")) {
                config.set(TACourseNode.CONF_DROPBOX_ENABLED, new Boolean(onoff));
                myTabbedPane.setEnabled(dropboxTabPosition, onoff);

                if (onoff) {
                    accessabilityVC.put("dropCondition", dropConditionC.getInitialComponent());
                } else {
                    accessabilityVC.remove(dropConditionC.getInitialComponent());
                    // OLAT-6725: clearcondition if component deselected
                    dropConditionC.getCondition().clearEasyConfig();
                    dropConditionC.getCondition().setConditionExpression(dropConditionC.getCondition().getConditionFromEasyModeConfiguration());
                    node.setConditionDrop(dropConditionC.getCondition());
                }
            } else if (event.getCommand().startsWith("returnbox")) {
                config.set(TACourseNode.CONF_RETURNBOX_ENABLED, new Boolean(onoff));

                if (onoff) {
                    accessabilityVC.put("returnboxCondition", returnboxConditionC.getInitialComponent());
                } else {
                    accessabilityVC.remove(returnboxConditionC.getInitialComponent());
                    returnboxConditionC.getCondition().clearEasyConfig();
                    returnboxConditionC.getCondition().setConditionExpression(returnboxConditionC.getCondition().getConditionFromEasyModeConfiguration());
                    node.setConditionReturnbox(returnboxConditionC.getCondition());
                }
            } else if (event.getCommand().startsWith("scoring")) {
                config.set(TACourseNode.CONF_SCORING_ENABLED, new Boolean(onoff));
                myTabbedPane.setEnabled(scoringTabPosition, onoff);
                if (onoff) {
                    accessabilityVC.put("scoringCondition", scoringConditionC.getInitialComponent());
                } else {
                    accessabilityVC.remove(scoringConditionC.getInitialComponent());
                    scoringConditionC.getCondition().clearEasyConfig();
                    scoringConditionC.getCondition().setConditionExpression(scoringConditionC.getCondition().getConditionFromEasyModeConfiguration());
                    node.setConditionScoring(scoringConditionC.getCondition());
                }
            } else if (event.getCommand().startsWith("solution")) {
                config.set(TACourseNode.CONF_SOLUTION_ENABLED, new Boolean(onoff));
                myTabbedPane.setEnabled(solutionTabPosition, onoff);
                if (onoff) {
                    accessabilityVC.put("solutionCondition", solutionConditionC.getInitialComponent());
                } else {
                    accessabilityVC.remove(solutionConditionC.getInitialComponent());
                    solutionConditionC.getCondition().clearEasyConfig();
                    solutionConditionC.getCondition().setConditionExpression(solutionConditionC.getCondition().getConditionFromEasyModeConfiguration());
                    node.setConditionSolution(solutionConditionC.getCondition());
                }
            }

            fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            return;

        } else if (source == taskConditionC) {
            if (event == Event.CHANGED_EVENT) {
                node.setConditionTask(taskConditionC.getCondition());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == dropConditionC) {
            if (event == Event.CHANGED_EVENT) {
                node.setConditionDrop(dropConditionC.getCondition());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == returnboxConditionC) {
            if (event == Event.CHANGED_EVENT) {
                node.setConditionReturnbox(returnboxConditionC.getCondition());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == scoringConditionC) {
            if (event == Event.CHANGED_EVENT) {
                node.setConditionScoring(scoringConditionC.getCondition());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == solutionConditionC) {
            if (event == Event.CHANGED_EVENT) {
                node.setConditionSolution(solutionConditionC.getCondition());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == dialogBoxController) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                // ok: open task folder
                final String relPath = TACourseNode.getTaskFolderPathRelToFolderRoot(course, node);
                final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(relPath, null);
                final OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(translate("taskfolder"), rootFolder);
                namedFolder.setLocalSecurityCallback(getTaskFolderSecCallback(relPath));
                frc = new FolderRunController(namedFolder, false, urequest, getWindowControl());
                listenTo(frc);
                final CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("folder.close"), frc.getInitialComponent());
                cmc.activate();
                fireEvent(urequest, Event.CHANGED_EVENT);
            }
        } else if (source == taskController) {
            if (event == Event.CANCELLED_EVENT) {
                return;
            } else if (event == Event.DONE_EVENT) {
                config.set(TACourseNode.CONF_TASK_TYPE, taskController.getTaskType());
                config.set(TACourseNode.CONF_TASK_TEXT, taskController.getOptionalText());
                config.set(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT, new Boolean(taskController.getIsSamplingWithReplacement()));
                config.setBooleanEntry(TACourseNode.CONF_TASK_PREVIEW, taskController.isTaskPreviewMode());
                config.setBooleanEntry(TACourseNode.CONF_TASK_DESELECT, taskController.isTaskDeselectMode());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                return;
            }
        } else if (source == scoringController) {
            if (event == Event.CANCELLED_EVENT) {
                if (hasLogEntries) {
                    scoringController.setDisplayOnly(true);
                }
                editScoring.contextPut("isOverwriting", new Boolean(false));
                return;
            } else if (event == Event.DONE_EVENT) {
                scoringController.updateModuleConfiguration(config);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == dropboxForm) {
            if (event == Event.CANCELLED_EVENT) {
                return;
            } else if (event == Event.DONE_EVENT) {
                config.set(TACourseNode.CONF_DROPBOX_CONFIRMATION_REQUESTED, new Boolean(dropboxForm.mailEnabled()));
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                return;
            }
        } else if (source == frc && (event instanceof FolderEvent) && event.getCommand().equals(FolderEvent.DELETE_EVENT)) {
            final String deletedTaskFile = getFileListAsComaSeparated(((FolderEvent) event).getFilename());
            // cancel task assignment
            identitiesToBeNotified = removeAssignedTask(course, deletedTaskFile);
            if (identitiesToBeNotified.size() > 0) {
                // sent email to all identities that used to have the deleted task assigned
                sendNotificationEmail(identitiesToBeNotified, urequest.getIdentity(), course, deletedTaskFile);
            }
        } else if (source == frc) {
            log.debug("ignore other FolderRunController events in TACourseNodeEditController from source=" + source);
        } else {
            log.warn("Can not handle event in TACourseNodeEditController source=" + source + " " + event.toString());
        }
    }

    /**
     * Strips the html tags from the input string.
     * 
     * @param fileListHtml
     * @return
     */
    private String getFileListAsComaSeparated(final String fileListHtml) {
        // strip html
        String filesString = "";
        final String[] tokens = fileListHtml.split("<[^<>]+>");
        for (final String token : tokens) {
            if (!token.equals("")) {
                if (filesString.length() > 3) {
                    filesString += ", ";
                }
                filesString += token;
            }
        }
        if (filesString.indexOf("/") != -1) {
            filesString = filesString.replaceAll("/", "");
        }
        return filesString;
    }

    /**
     * sends the confirmation mail, and shows error message to user if email sending failed.
     */
    private void sendNotificationEmail(final List<Identity> recipients, Identity originatorIdentity, final ICourse course, final String task) {
        getConfirmationLearnService().sendTaskDeleteConfirmation(recipients, originatorIdentity, course.getCourseTitle(), courseRepositoryEntryId,
                new Long(node.getIdent()), node.getShortTitle(), task);
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    /**
     * Cancel the task assignment for this task and all Identities.
     * 
     * @param course
     * @param task
     * @return Returns the Identities list that have had this task assigned.
     */
    private List<Identity> removeAssignedTask(final ICourse course, final String task) {
        // identities to be notified
        final List<Identity> identityList = new ArrayList<Identity>();
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final List properties = cpm.listCourseNodeProperties(node, null, null, Task_EBL.PROP_ASSIGNED);
        if (properties != null && properties.size() > 0) {
            for (final Object propetyObj : properties) {
                final PropertyImpl propety = (PropertyImpl) propetyObj;
                identityList.add(propety.getIdentity());
                cpm.deleteProperty(propety);
            }
        }
        return identityList;
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane theTabbedPane) {
        this.myTabbedPane = theTabbedPane;
        myTabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessabilityVC);
        myTabbedPane.addTab(translate(PANE_TAB_CONF_MODULES), editModules);
        taskTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_TASK), editTask);
        dropboxTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_DROPBOX), editDropbox);
        scoringTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_SCORING), editScoring);
        solutionTabPosition = myTabbedPane.addTab(translate(PANE_TAB_SOLUTION), solutionVC);

        Boolean bool = (Boolean) config.get(TACourseNode.CONF_TASK_ENABLED);
        myTabbedPane.setEnabled(taskTabPosition, (bool != null) ? bool.booleanValue() : true);
        bool = (Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLED);
        myTabbedPane.setEnabled(dropboxTabPosition, (bool != null) ? bool.booleanValue() : true);
        bool = (Boolean) config.get(TACourseNode.CONF_SCORING_ENABLED);
        myTabbedPane.setEnabled(scoringTabPosition, (bool != null) ? bool.booleanValue() : true);

        bool = (Boolean) config.get(TACourseNode.CONF_SOLUTION_ENABLED);
        myTabbedPane.setEnabled(solutionTabPosition, (bool != null) ? bool.booleanValue() : true);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
    }

    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    @Override
    public TabbedPane getTabbedPane() {
        return myTabbedPane;
    }

}

class TaskFolderCallback implements VFSSecurityCallback {

    private final boolean folderLocked;
    private Quota folderQuota = null;

    /**
     * @param folderLocked
     */
    public TaskFolderCallback(final String relPath, final boolean folderLocked) {
        this.folderLocked = folderLocked;
        initTaskFolderQuota(relPath);
    }

    private void initTaskFolderQuota(final String relPath) {
        final QuotaManager qm = QuotaManager.getInstance();
        folderQuota = qm.getCustomQuota(relPath);
        if (folderQuota == null) {
            final Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
            folderQuota = QuotaManager.getInstance().createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
        }
    }

    /**
	 */
    @Override
    public boolean canRead() {
        return true;
    }

    /**
	 */
    @Override
    public boolean canWrite() {
        return !folderLocked;
    }

    /**
	 */
    @Override
    public boolean canDelete() {
        return !folderLocked;
    }

    /**
	 */
    @Override
    public boolean canList() {
        return true;
    }

    /**
	 */
    @Override
    public boolean canCopy() {
        return true;
    }

    /**
	 */
    @Override
    public boolean canDeleteRevisionsPermanently() {
        return false;
    }

    /**
	 */
    @Override
    public Quota getQuota() {
        return folderQuota;
    }

    /**
	 */
    @Override
    public void setQuota(final Quota quota) {
        folderQuota = quota;
    }

}
