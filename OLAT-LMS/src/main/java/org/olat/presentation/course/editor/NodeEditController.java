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

package org.olat.presentation.course.editor;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.group.context.BGContextEvent;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * is the controller for
 * 
 * @author Felix Jost
 */
public class NodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    private static final String PANE_TAB_VISIBILITY = "pane.tab.visibility";
    private static final String PANE_TAB_GENERAL = "pane.tab.general";

    /** Configuration key: use spash-scree start page when accessing a course node. Values: true, false **/
    public static final String CONFIG_STARTPAGE = "startpage";
    /** Configuration key: integrate component menu into course menu. Values: true, false **/
    public static final String CONFIG_COMPONENT_MENU = "menuon";
    /** Configuration key: how to integrate the course node content into the course **/
    public static final String CONFIG_INTEGRATION = "integration";
    /** To enforce the encoding of the content of the node **/
    public final static String CONFIG_CONTENT_ENCODING = "encodingContent";
    /** Try to discovery automatically the encoding of the node content **/
    public final static String CONFIG_CONTENT_ENCODING_AUTO = "auto";
    /** To enforce the encoding of the embedded javascript of the node **/
    public final static String CONFIG_JS_ENCODING = "encodingJS";
    /** Take the same encoding as the content **/
    public final static String CONFIG_JS_ENCODING_AUTO = "auto";

    private final CourseNode courseNode;
    private final VelocityContainer descriptionVc, visibilityVc;

    private final NodeConfigFormController nodeConfigController;

    private final ConditionEditController visibilityCondContr;
    private final NoAccessExplEditController noAccessContr;
    private TabbedPane myTabbedPane;
    private final TabbableController childTabsCntrllr;

    /** Event that signals that the node configuration has been changed * */
    public static final Event NODECONFIG_CHANGED_EVENT = new Event("nodeconfigchanged");
    private static final String[] paneKeys = { PANE_TAB_VISIBILITY, PANE_TAB_GENERAL };

    /**
     * @param ureq
     * @param editorModel
     * @param course
     * @param luNode
     * @param groupMgr
     */
    public NodeEditController(final UserRequest ureq, final WindowControl wControl, final CourseEditorTreeModel editorModel, final ICourse course,
            final CourseNode luNode, final CourseGroupManager groupMgr, final UserCourseEnvironment euce, final TabbableController childTabsController) {
        super(ureq, wControl);
        this.courseNode = luNode;

        addLoggingResourceable(LoggingResourceable.wrap(course));
        addLoggingResourceable(LoggingResourceable.wrap(courseNode));

        /*
         * the direct child tabs.
         */
        this.childTabsCntrllr = childTabsController;
        this.listenTo(childTabsCntrllr);

        // description and metadata component
        descriptionVc = this.createVelocityContainer("nodeedit");
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, true);

        final StringBuilder extLink = new StringBuilder();
        extLink.append(Settings.getServerContextPathURI()).append("/url/RepositoryEntry/").append(re.getKey()).append("/CourseNode/").append(luNode.getIdent());
        final StringBuilder intLink = new StringBuilder();
        intLink.append("javascript:parent.gotonode(").append(luNode.getIdent()).append(")");

        descriptionVc.contextPut("extLink", extLink.toString());
        descriptionVc.contextPut("intLink", intLink.toString());
        descriptionVc.contextPut("nodeId", luNode.getIdent());

        this.putInitialPanel(descriptionVc);

        nodeConfigController = new NodeConfigFormController(ureq, wControl, luNode, false);
        listenTo(nodeConfigController);
        descriptionVc.put("nodeConfigForm", nodeConfigController.getInitialComponent());

        // Visibility and no-access explanation component
        visibilityVc = this.createVelocityContainer("visibilityedit");

        // Visibility precondition
        final Condition visibCondition = luNode.getPreConditionVisibility();
        visibilityCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, visibCondition, "visibilityConditionForm",
                AssessmentHelper.getAssessableNodes(editorModel, luNode), euce);
        // set this useractivity logger for the visibility condition controller
        this.listenTo(visibilityCondContr);
        visibilityVc.put("visibilityCondition", visibilityCondContr.getInitialComponent());

        // No-Access-Explanation
        final String noAccessExplanation = luNode.getNoAccessExplanation();
        noAccessContr = new NoAccessExplEditController(ureq, getWindowControl(), noAccessExplanation);
        this.listenTo(noAccessContr);
        visibilityVc.put("noAccessExplanationComp", noAccessContr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // Don't do anything.
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {

        if (source == visibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = visibilityCondContr.getCondition();
                courseNode.setPreConditionVisibility(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == noAccessContr) {
            if (event == Event.CHANGED_EVENT) {
                final String noAccessExplanation = noAccessContr.getNoAccessExplanation();
                courseNode.setNoAccessExplanation(noAccessExplanation);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == childTabsCntrllr) {
            if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
                // fire child controller request further
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == nodeConfigController) {
            if (event == Event.DONE_EVENT) {
                courseNode.setShortTitle(nodeConfigController.getMenuTitle());
                courseNode.setLongTitle(nodeConfigController.getDisplayTitle());
                courseNode.setLearningObjectives(nodeConfigController.getLearningObjectives());
                courseNode.setDisplayOption(nodeConfigController.getDisplayOption());
            }
            fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
        }

        if (event.getCommand().equals(BGContextEvent.RESOURCE_ADDED)) {
            System.out.println("nec:resource added");
        }
        // do logging
        ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_EDITED, getClass());
    }

    /**
     * Package private. Used by EditorMainController.
     * 
     * @return CourseNode
     */
    CourseNode getCourseNode() {
        return courseNode;
    }

    /**
     * Returns the component that is used to configurate the nodes description and metadata
     * 
     * @return The description and metadata edit component
     */
    public Component getDescriptionEditComponent() {
        return descriptionVc;
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

    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_GENERAL), descriptionVc);
        tabbedPane.addTab(translate(PANE_TAB_VISIBILITY), visibilityVc);
        if (childTabsCntrllr != null) {
            childTabsCntrllr.addTabs(tabbedPane);
        }
    }

    /**
	 */
    @Override
    protected ActivateableTabbableDefaultController[] getChildren() {
        if (childTabsCntrllr != null && childTabsCntrllr instanceof ActivateableTabbableDefaultController) {
            return new ActivateableTabbableDefaultController[] { (ActivateableTabbableDefaultController) childTabsCntrllr };
        } else {
            return new ActivateableTabbableDefaultController[] {};
        }
    }

}
