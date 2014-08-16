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

package org.olat.presentation.course.nodes.basiclti;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.BasicLTICourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
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
import org.olat.system.event.Event;

/**
 * Description:<BR/>
 * This edit controller is used to edit a course building block of type basic lti
 * <P/>
 * Initial Date: march 2010
 * 
 * @author guido
 * @author Charles Severance
 */
public class LTIEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public static final String PANE_TAB_LTCONFIG = "pane.tab.ltconfig";
    public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    private static final String[] paneKeys = { PANE_TAB_LTCONFIG, PANE_TAB_ACCESSIBILITY };

    private ModuleConfiguration config;
    private final CourseEnvironment editCourseEnv;
    private final VelocityContainer myContent;

    private final LTIConfigForm ltConfigForm;
    private final BasicLTICourseNode courseNode;
    private final ConditionEditController accessibilityCondContr;
    private TabbedPane myTabbedPane;
    private LayoutMain3ColsPreviewController previewLayoutCtr;
    private final Link previewButton;
    private Controller tunnelRunCtr;

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
    public LTIEditController(final ModuleConfiguration config, final UserRequest ureq, final WindowControl wControl, final BasicLTICourseNode ltCourseNode,
            final ICourse course, final UserCourseEnvironment euce) {
        super(ureq, wControl);

        this.config = config;
        this.courseNode = ltCourseNode;
        this.editCourseEnv = course.getCourseEnvironment();

        myContent = this.createVelocityContainer("edit");
        previewButton = LinkFactory.createButtonSmall("command.preview", myContent, this);

        ltConfigForm = new LTIConfigForm(ureq, wControl, config);
        listenTo(ltConfigForm);

        myContent.put("ltConfigForm", ltConfigForm.getInitialComponent());

        final CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final CourseEditorTreeModel editorModel = course.getEditorTreeModel();
        // Accessibility precondition
        final Condition accessCondition = courseNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, accessCondition, "accessabilityConditionForm",
                AssessmentHelper.getAssessableNodes(editorModel, ltCourseNode), euce);
        this.listenTo(accessibilityCondContr);

        // Enable preview button only if node configuration is valid
        if (!(ltCourseNode.isConfigValid().isError())) {
            myContent.contextPut("showPreviewButton", Boolean.TRUE);
        } else {
            myContent.contextPut("showPreviewButton", Boolean.FALSE);
        }

    }

    /**
     * config.set
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == previewButton) { // those must be links

            removeAsListenerAndDispose(tunnelRunCtr);
            tunnelRunCtr = new LTIRunController(getWindowControl(), config, ureq, courseNode, editCourseEnv);
            listenTo(tunnelRunCtr);

            // preview layout: only center column (col3) used
            removeAsListenerAndDispose(previewLayoutCtr);
            previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, null, tunnelRunCtr.getInitialComponent(), null);
            previewLayoutCtr.addDisposableChildController(tunnelRunCtr); // cascade dispose
            listenTo(previewLayoutCtr);

            previewLayoutCtr.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                courseNode.setPreConditionAccess(cond);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == ltConfigForm) {
            if (event == Event.CANCELLED_EVENT) {
                // do nothing
            } else if (event == Event.DONE_EVENT) {
                config = ltConfigForm.getUpdatedConfig();
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
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
        tabbedPane.addTab(translate(PANE_TAB_LTCONFIG), myContent);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
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
