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

package org.olat.presentation.course.nodes.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.forum.Forum;
import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.nodes.DialogCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.dialogelements.DialogElement;
import org.olat.lms.dialogelements.DialogElementsPropertyManager;
import org.olat.lms.dialogelements.DialogPropertyElements;
import org.olat.lms.forum.ForumService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.dialogelements.DialogElementsTableModel;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * controller for the tabbed pane inside the course editor for the course node 'dialog elements'
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    private static final String PANE_TAB_DIALOGCONFIG = "pane.tab.dialogconfig";
    private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    private static final String[] paneKeys = { PANE_TAB_DIALOGCONFIG, PANE_TAB_ACCESSIBILITY };

    private final VelocityContainer content, accessContent;
    private final DialogCourseNode courseNode;
    private final ConditionEditController readerCondContr, posterCondContr, moderatorCondContr;
    private TabbedPane myTabbedPane;
    private final BCCourseNode bcNode = new BCCourseNode();
    private final ICourse course;
    private DialogConfigForm configForumLaunch;
    private TableController tableCtr;
    private final PackageTranslator resourceTrans;
    private FileUploadController fileUplCtr;
    private DialogElement recentElement;
    private final TableGuiConfiguration tableConf;
    private final Link uploadButton;

    public DialogCourseNodeEditController(final UserRequest ureq, final WindowControl wControl, final DialogCourseNode node, final ICourse course,
            final UserCourseEnvironment userCourseEnv) {
        super(ureq, wControl);
        // o_clusterOk by guido: save to hold reference to course inside editor
        this.course = course;
        this.courseNode = node;

        this.resourceTrans = new PackageTranslator(PackageUtil.getPackageName(DialogElementsTableModel.class), ureq.getLocale(), getTranslator());
        // set name of the folder we use
        bcNode.setShortTitle(translate("dialog.folder.name"));

        // dialog specific config tab
        content = this.createVelocityContainer("edit");
        uploadButton = LinkFactory.createButton("dialog.upload.file", content, this);

        // configure table
        tableConf = new TableGuiConfiguration();
        tableConf.setResultsPerPage(10);
        showOverviewTable(ureq);

        initConfigForm(ureq);

        // accessability config tab
        accessContent = this.createVelocityContainer("edit_access");

        final CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final CourseEditorTreeModel editorModel = course.getEditorTreeModel();
        // Reader precondition
        final Condition readerCondition = courseNode.getPreConditionReader();
        // TODO:gs:a getAssessableNodes ist der dialog node assessable oder nicht?
        readerCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, readerCondition, "readerConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, courseNode), userCourseEnv);
        this.listenTo(readerCondContr);
        accessContent.put("readerCondition", readerCondContr.getInitialComponent());

        // Poster precondition
        final Condition posterCondition = courseNode.getPreConditionPoster();
        posterCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, posterCondition, "posterConditionForm", AssessmentHelper.getAssessableNodes(
                editorModel, courseNode), userCourseEnv);
        this.listenTo(posterCondContr);
        accessContent.put("posterCondition", posterCondContr.getInitialComponent());

        // Moderator precondition
        final Condition moderatorCondition = courseNode.getPreConditionModerator();
        moderatorCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, moderatorCondition, "moderatorConditionForm",
                AssessmentHelper.getAssessableNodes(editorModel, courseNode), userCourseEnv);
        // FIXME:gs: why is firing needed here?
        fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
        this.listenTo(moderatorCondContr);
        accessContent.put("moderatorCondition", moderatorCondContr.getInitialComponent());
    }

    private void initConfigForm(final UserRequest ureq) {

        removeAsListenerAndDispose(configForumLaunch);
        configForumLaunch = new DialogConfigForm(ureq, getWindowControl(), courseNode.getModuleConfiguration());
        listenTo(configForumLaunch);

        content.put("showForumAsPopupConfigForm", configForumLaunch.getInitialComponent());
    }

    /**
	 */
    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    /**
	 */
    @Override
    public TabbedPane getTabbedPane() {
        return myTabbedPane;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == uploadButton) {
            ForumService forumService = getForumService();
            final Forum forum = forumService.addAForum();
            final VFSContainer forumContainer = forumService.getForumContainer(forum.getKey());

            removeAsListenerAndDispose(fileUplCtr);
            fileUplCtr = new FileUploadController(getWindowControl(), forumContainer, ureq, (int) FolderConfig.getLimitULKB(), Quota.UNLIMITED, null, false);
            listenTo(fileUplCtr);

            recentElement = new DialogElement();
            recentElement.setForumKey(forum.getKey());
            recentElement.setAuthor(ureq.getIdentity().getName());
            content.contextPut("overview", Boolean.FALSE);
            content.put("upload", fileUplCtr.getInitialComponent());
        }
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean(ForumService.class);

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == configForumLaunch) {
            if (event == Event.CHANGED_EVENT) {
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == readerCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = readerCondContr.getCondition();
                courseNode.setPreConditionReader(cond);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == posterCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = posterCondContr.getCondition();
                courseNode.setPreConditionPoster(cond);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == moderatorCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = moderatorCondContr.getCondition();
                courseNode.setPreConditionModerator(cond);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == tableCtr) {
            // process table events
        } else if (source == fileUplCtr) {
            // event.
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                // reset recent element
                recentElement = null;
                showOverviewTable(ureq);
            } else if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {

                // new dialog element
                final DialogElement element = new DialogElement();
                element.setAuthor(recentElement.getAuthor());
                element.setDate(new Date());
                final String filename = ((FolderEvent) event).getFilename();
                element.setFilename(filename);
                element.setForumKey(recentElement.getForumKey());
                element.setFileSize(getForumService().getForumContainerSize(recentElement.getForumKey()));

                // save property
                DialogElementsPropertyManager.getInstance().addDialogElement(course.getCourseEnvironment().getCoursePropertyManager(), courseNode, element);

                // do logging
                ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(), LoggingResourceable.wrapUploadFile(filename));
            }
        }
    }

    /**
     * update table with latest elements
     * 
     * @param ureq
     */
    private void showOverviewTable(final UserRequest ureq) {

        removeAsListenerAndDispose(tableCtr);
        tableCtr = new TableController(tableConf, ureq, getWindowControl(), resourceTrans);
        listenTo(tableCtr);

        final DialogPropertyElements elements = DialogElementsPropertyManager.getInstance().findDialogElements(
                this.course.getCourseEnvironment().getCoursePropertyManager(), courseNode);
        List list = new ArrayList();
        final DialogElementsTableModel tableModel = new DialogElementsTableModel(getTranslator(), null, null);
        if (elements != null) {
            list = elements.getDialogPropertyElements();
        }
        tableModel.setEntries(list);
        tableModel.addColumnDescriptors(tableCtr);
        tableCtr.setTableDataModel(tableModel);
        tableCtr.modelChanged();
        tableCtr.setSortColumn(1, true);
        content.contextPut("overview", Boolean.TRUE);
        content.put("dialogElementsTable", tableCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessContent);
        tabbedPane.addTab(translate(PANE_TAB_DIALOGCONFIG), content);
    }

}
