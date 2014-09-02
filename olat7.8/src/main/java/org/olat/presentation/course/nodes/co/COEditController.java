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

package org.olat.presentation.course.nodes.co;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.COCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.group.learn.GroupAndAreaSelectController;
import org.olat.system.event.Event;

/**
 * Description:<BR/>
 * Edit controller for the contact form course building block Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 */
public class COEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public static final String PANE_TAB_COCONFIG = "pane.tab.coconfig";
    private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
    private static final String[] paneKeys = { PANE_TAB_COCONFIG, PANE_TAB_ACCESSIBILITY };

    /** config key: to email addresses to be extracted from specified groups */
    public static final String CONFIG_KEY_EMAILTOGROUPS = "emailToGroups";
    /** config key: to email addresses to be extracted from specified learn areas */
    public static final String CONFIG_KEY_EMAILTOAREAS = "emailToAreas";
    /** config key: email goes to partipiciants */
    public static final String CONFIG_KEY_EMAILTOPARTICIPANTS = "emailtToPartips";
    /** config key: email goes to coaches */
    public static final String CONFIG_KEY_EMAILTOCOACHES = "emailToCoaches";
    /** config key: to email address */
    public static final String CONFIG_KEY_EMAILTOADRESSES = "emailToAdresses";
    /** config key: default subject text */
    public static final String CONFIG_KEY_MSUBJECT_DEFAULT = "mSubjectDefault";
    /** config key: default body text */
    public static final String CONFIG_KEY_MBODY_DEFAULT = "mBodyDefault";

    private static final String JSELEMENTID = "bel_";

    private final ModuleConfiguration moduleConfiguration;
    private final VelocityContainer myContent;
    private final Panel main;
    private final COConfigForm configForm;
    private final COCourseNode courseNode;
    private final ConditionEditController accessibilityCondContr;
    private final ICourse course;
    private GroupAndAreaSelectController selectGroupsCtr;
    private GroupAndAreaSelectController selectAreasCtr;
    private TabbedPane myTabbedPane;

    /**
     * Constructor for a contact form edit controller
     * 
     * @param config
     * @param ureq
     * @param coCourseNode
     * @param course
     */
    public COEditController(final ModuleConfiguration config, final UserRequest ureq, final WindowControl wControl, final COCourseNode coCourseNode,
            final ICourse course, final UserCourseEnvironment euce) {
        super(ureq, wControl);
        this.moduleConfiguration = config;
        resolveModuleConfigurationIssues(moduleConfiguration);
        this.courseNode = coCourseNode;
        this.course = course;

        main = new Panel("coeditpanel");

        myContent = this.createVelocityContainer("edit");

        configForm = new COConfigForm(ureq, wControl, config, euce);
        configForm.addControllerListener(this);

        myContent.put("configForm", configForm.getInitialComponent());

        // not needed: setInitialComponent(myContent);
        // Accessibility precondition
        final Condition accessCondition = courseNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), course.getCourseEnvironment().getCourseGroupManager(), accessCondition,
                "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), coCourseNode), euce);
        this.listenTo(accessibilityCondContr);

        main.setContent(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
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
        } else if (source == configForm) { // those must be links
            if (event == Event.CANCELLED_EVENT) {
                return;
            } else if (event == Event.DONE_EVENT) {
                moduleConfiguration.set(CONFIG_KEY_EMAILTOGROUPS, configForm.getEmailGroups());
                moduleConfiguration.set(CONFIG_KEY_EMAILTOAREAS, configForm.getEmailAreas());
                moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, configForm.sendToCoaches());
                moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, configForm.sendToPartips());
                moduleConfiguration.set(CONFIG_KEY_EMAILTOADRESSES, configForm.getEmailList());
                moduleConfiguration.set(CONFIG_KEY_MSUBJECT_DEFAULT, configForm.getMSubject());
                moduleConfiguration.set(CONFIG_KEY_MBODY_DEFAULT, configForm.getMBody());

                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
                return;
            } else if (event.getCommand().equals("popupchoosegroups")) {
                // open a controller in a new window which only results in sending back
                // javascript
                // get preselected groups
                final String groups = (String) moduleConfiguration.get(CONFIG_KEY_EMAILTOGROUPS);
                // get group select controller
                final ControllerCreator ctrlCreator = new ControllerCreator() {
                    @Override
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        selectGroupsCtr = new GroupAndAreaSelectController(course, lureq, lwControl, course.getCourseEnvironment().getCourseGroupManager(),
                                GroupAndAreaSelectController.TYPE_GROUP, groups, JSELEMENTID + "popupchoosegroups" + configForm.hashCode());
                        // use a one-column main layout
                        // disposed in dispose method of COEditController!
                        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, selectGroupsCtr.getInitialComponent(),
                                "null");
                        return layoutCtr;
                    }
                };
                // wrap the content controller into a full header layout
                final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                // open in new browser window
                final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                pbw.open(ureq);
                //
            } else if (event.getCommand().equals("popupchooseareas")) {
                // open a controller in a new window which only results in sending back
                // javascript
                // get preselected areas
                final String areas = (String) moduleConfiguration.get(CONFIG_KEY_EMAILTOAREAS);
                // get area select controller
                final ControllerCreator ctrlCreator = new ControllerCreator() {
                    @Override
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        selectAreasCtr = new GroupAndAreaSelectController(course, lureq, lwControl, course.getCourseEnvironment().getCourseGroupManager(),
                                GroupAndAreaSelectController.TYPE_AREA, areas, JSELEMENTID + "popupchooseareas" + configForm.hashCode());
                        // use a one-column main layout
                        // disposed in dispose method of COEditController!
                        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, selectAreasCtr.getInitialComponent(),
                                null);
                        return layoutCtr;
                    }
                };
                // wrap the content controller into a full header layout
                final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                // open in new browser window
                final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                pbw.open(ureq);
                //
            }
        }
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;

        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
        tabbedPane.addTab(translate(PANE_TAB_COCONFIG), myContent);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
        if (selectGroupsCtr != null) {
            selectGroupsCtr.dispose();
        }
        if (selectAreasCtr != null) {
            selectAreasCtr.dispose();
        }
    }

    /**
     * resolving version issues of the module configuration, adds new default values for new keys
     * 
     * @param moduleConfiguration2
     */
    private void resolveModuleConfigurationIssues(final ModuleConfiguration moduleConfiguration2) {
        final int version = moduleConfiguration2.getConfigurationVersion();
        /*
         * if no version was set before -> version is 1
         */
        if (version == 1) {
            // new keys and defaults are
            moduleConfiguration.set(CONFIG_KEY_EMAILTOAREAS, "");
            moduleConfiguration.set(CONFIG_KEY_EMAILTOGROUPS, "");
            moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOCOACHES, false);
            moduleConfiguration.setBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS, false);
            //
            moduleConfiguration2.setConfigurationVersion(2);
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
