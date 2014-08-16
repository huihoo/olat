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

package org.olat.presentation.course.nodes.st;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.StringResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ObjectivesHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.OlatCmdEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * The course structure run controller provides an automatically generated view of the children of this course node. There are two modi to display the childs:
 * <ul>
 * <li>simple TOC: a list of titles and descriptions</li>
 * <li>peek view: for each child the peek view controller is displayed. This gives each child the possibility to render a preview or simplified view of the content. This
 * view is visually more appealing than the simple TOC view.</li>
 * </ul>
 * 
 * @author Felix Jost, Florian Gnägi frentix GmbH
 */
public class STCourseNodeRunController extends BasicController {
    private final VelocityContainer myContent;

    /**
     * @param ureq
     * @param userCourseEnv
     * @param stCourseNode
     * @param se
     * @param ne
     */
    public STCourseNodeRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final STCourseNode stCourseNode,
            final ScoreEvaluation se, final NodeEvaluation ne) {
        super(ureq, wControl);
        addLoggingResourceable(LoggingResourceable.wrap(stCourseNode));

        myContent = createVelocityContainer("run");

        // read display configuration
        final ModuleConfiguration config = stCourseNode.getModuleConfiguration();
        // configure number of display rows
        final int rows = config.getIntegerSafe(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
        myContent.contextPut("layoutType", (rows == 1 ? "o_course_run_toc_one_column" : "o_course_run_toc_two_columns"));
        // the display type: toc or peekview
        final String displayType = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);

        // Build list of child nodes and peek views if necessary
        final List<CourseNode> children = new ArrayList<CourseNode>();

        // Build up a overview of all visible children (direct children only, no
        // grandchildren)
        final String peekviewChildNodesConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, null);
        final List<String> peekviewChildNodes = (peekviewChildNodesConfig == null ? new ArrayList<String>() : Arrays.asList(peekviewChildNodesConfig.split(",")));
        final int chdCnt = ne.getChildCount();
        for (int i = 0; i < chdCnt; i++) {
            final NodeEvaluation neChd = ne.getNodeEvaluationChildAt(i);
            if (neChd.isVisible()) {
                // Build and add child generic or specific peek view
                final CourseNode child = neChd.getCourseNode();
                Controller childViewController = null;
                Controller childPeekViewController = null;
                if (displayType.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW)) {
                    if (peekviewChildNodes.size() == 0) {
                        // Special case: no child nodes configured. This is the case when
                        // the node has been configured before it had any children. We just
                        // use the first children as they appear in the list
                        if (i < STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
                            childPeekViewController = child.createPeekViewRunController(ureq, wControl, userCourseEnv, neChd);
                        } else {
                            // Stop, we already reached the max count
                            break;
                        }
                    } else {
                        // Only add configured children
                        if (peekviewChildNodes.contains(child.getIdent())) {
                            childPeekViewController = child.createPeekViewRunController(ureq, wControl, userCourseEnv, neChd);
                        } else {
                            // Skip this child - not configured
                            continue;
                        }
                    }
                }
                // Add child to list
                children.add(child);
                childViewController = new PeekViewWrapperController(ureq, wControl, child, childPeekViewController);
                listenTo(childViewController); // auto-dispose controller
                myContent.put("childView_" + child.getIdent(), childViewController.getInitialComponent());
            }
        }

        myContent.contextPut("children", children);
        myContent.contextPut("nodeFactory", CourseNodeFactory.getInstance());

        // push title and learning objectives, only visible on intro page
        myContent.contextPut("menuTitle", stCourseNode.getShortTitle());
        myContent.contextPut("displayTitle", stCourseNode.getLongTitle());
        myContent.contextPut("hasScore", new Boolean(stCourseNode.hasScoreConfigured()));
        myContent.contextPut("hasPassed", new Boolean(stCourseNode.hasPassedConfigured()));

        if (se != null) {
            final Float score = se.getScore();
            final Boolean passed = se.getPassed();
            if (score != null) {
                myContent.contextPut("scoreScore", AssessmentHelper.getRoundedScore(score));
            }
            if (passed != null) {
                myContent.contextPut("scorePassed", passed);
                myContent.contextPut("hasPassedValue", Boolean.TRUE);
            } else {
                myContent.contextPut("hasPassedValue", Boolean.FALSE);
            }
        }

        // Adding learning objectives
        final String learningObj = stCourseNode.getLearningObjectives();
        if (learningObj != null) {
            final Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
            myContent.put("learningObjectives", learningObjectives);
            myContent.contextPut("hasObjectives", learningObj); // dummy value, just
                                                                // an exists operator
        }

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to dispatch
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event instanceof OlatCmdEvent) {
            final OlatCmdEvent gotoNodeEvent = (OlatCmdEvent) event;
            final String subcommand = gotoNodeEvent.getSubcommand();
            // subcommand consists of node id and path
            final int slashpos = subcommand.indexOf("/");
            String nodeId = subcommand;
            String path = "";
            if (slashpos != -1) {
                nodeId = subcommand.substring(0, slashpos);
                path = subcommand.substring(slashpos);
            }
            ThreadLocalUserActivityLogger.log(CourseLoggingAction.ST_GOTO_NODE, getClass(),
                    LoggingResourceable.wrapNonOlatResource(StringResourceableType.nodeId, nodeId, path));
            // forward to my listeners
            fireEvent(ureq, event);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do yet
    }

}
