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

package org.olat.presentation.course.nodes.tu;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.TUCourseNode;
import org.olat.lms.course.run.preview.PreviewConfigHelper;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.presentation.tunneling.IframeTunnelController;
import org.olat.system.event.Event;

/**
 * Description:<BR/>
 * The tunneling edit controller is used to edit a course building block of typ tu
 * <P/>
 * Initial Date: Oct 12, 2004
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class TUEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public static final String PANE_TAB_TUCONFIG = "pane.tab.tuconfig";
    public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    private static final String[] paneKeys = { PANE_TAB_TUCONFIG, PANE_TAB_ACCESSIBILITY };

    private ModuleConfiguration config;
    private final VelocityContainer myContent;

    private final TUConfigForm tuConfigForm;
    private final TUCourseNode courseNode;
    private final ConditionEditController accessibilityCondContr;
    private TabbedPane myTabbedPane;
    private LayoutMain3ColsPreviewController previewLayoutCtr;
    private final Link previewButton;
    private final ICourse course; // used only for preview of the current node

    /**
     * Constructor for tunneling editor controller
     * 
     * @param config
     *            The node module configuration
     * @param ureq
     *            The user request
     * @param wControl
     *            The window controller
     * @param tuCourseNode
     *            The current single page course node
     * @param course
     */
    public TUEditController(final ModuleConfiguration config, final UserRequest ureq, final WindowControl wControl, final TUCourseNode tuCourseNode,
            final ICourse course, final UserCourseEnvironment euce) {
        super(ureq, wControl);

        this.config = config;
        this.courseNode = tuCourseNode;
        this.course = course;

        myContent = this.createVelocityContainer("edit");
        previewButton = LinkFactory.createButtonSmall("command.preview", myContent, this);

        tuConfigForm = new TUConfigForm(ureq, wControl, config, false);
        listenTo(tuConfigForm);
        myContent.put("tuConfigForm", tuConfigForm.getInitialComponent());

        final CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final CourseEditorTreeModel editorModel = course.getEditorTreeModel();
        // Accessibility precondition
        final Condition accessCondition = courseNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, accessCondition, "accessabilityConditionForm",
                AssessmentHelper.getAssessableNodes(editorModel, tuCourseNode), euce);
        this.listenTo(accessibilityCondContr);

        // Enable preview button only if node configuration is valid
        if (!(tuCourseNode.isConfigValid().isError())) {
            myContent.contextPut("showPreviewButton", Boolean.TRUE);
        } else {
            myContent.contextPut("showPreviewButton", Boolean.FALSE);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == previewButton) { // those must be links
            Controller tunnelRunCtr;
            if (config.getBooleanSafe(TUConfigForm.CONFIG_IFRAME)) {
                tunnelRunCtr = new IframeTunnelController(ureq, getWindowControl(), config);
            } else {
                tunnelRunCtr = new TURunController(getWindowControl(), config, ureq, courseNode, PreviewConfigHelper.getPreviewCourseEnvironment(true, true, course));
            }
            if (previewLayoutCtr != null) {
                previewLayoutCtr.dispose();
            }
            // preview layout: only center column (col3) used
            previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, null, tunnelRunCtr.getInitialComponent(), null);
            previewLayoutCtr.addDisposableChildController(tunnelRunCtr); // cascade dispose
            previewLayoutCtr.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                courseNode.setPreConditionAccess(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == tuConfigForm) {
            if (event == Event.CANCELLED_EVENT) {
                // do nothing
            } else if (event == Event.DONE_EVENT) {
                config = tuConfigForm.getUpdatedConfig();
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                // form valid -> node config valid -> show preview button
                myContent.contextPut("showPreviewButton", Boolean.TRUE);
            }
        }
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
        tabbedPane.addTab(translate(PANE_TAB_TUCONFIG), myContent);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
        if (previewLayoutCtr != null) {
            previewLayoutCtr.dispose();
            previewLayoutCtr = null;
        }
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
