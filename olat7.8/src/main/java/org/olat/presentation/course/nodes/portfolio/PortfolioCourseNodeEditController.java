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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.course.nodes.portfolio;

import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.PortfolioCourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.nodes.ms.MSEditFormController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: srosse Class Description for PortfolioCourseNodeEditController
 * <P>
 * Initial Date: 6 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
    private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
    public static final String PANE_TAB_CONFIG = "pane.tab.portfolio_config";
    public static final String PANE_TAB_SCORING = "pane.tab.portfolio_scoring";
    static final String[] paneKeys = { PANE_TAB_CONFIG, PANE_TAB_SCORING };

    private final VelocityContainer configContent;
    private final PortfolioConfigForm configForm;
    private final PortfolioTextForm textForm;
    private final Component scoringContent;
    private final MSEditFormController scoringController;

    private TabbedPane myTabbedPane;

    private final boolean hasLogEntries;
    private final ModuleConfiguration config;
    private final ConditionEditController accessibilityCondContr;
    private final PortfolioCourseNode courseNode;

    public PortfolioCourseNodeEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final PortfolioCourseNode node,
            final ModuleConfiguration config, final UserCourseEnvironment euce) {
        super(ureq, wControl);
        this.config = config;
        this.courseNode = node;

        configForm = new PortfolioConfigForm(ureq, wControl, course, node);
        listenTo(configForm);
        scoringController = new MSEditFormController(ureq, wControl, config);
        scoringContent = scoringController.getInitialComponent();
        listenTo(scoringController);
        textForm = new PortfolioTextForm(ureq, wControl, course, node);
        listenTo(textForm);

        configContent = createVelocityContainer("edit");
        configContent.put("configForm", configForm.getInitialComponent());
        configContent.put("textForm", textForm.getInitialComponent());

        // Accessibility precondition
        final CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final CourseEditorTreeModel editorModel = course.getEditorTreeModel();
        final Condition accessCondition = node.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, accessCondition, "accessabilityConditionForm",
                AssessmentHelper.getAssessableNodes(editorModel, node), euce);
        listenTo(accessibilityCondContr);

        // if there is already user data available, make for read only
        final UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
        hasLogEntries = am.hasUserNodeLogs(node);
        configContent.contextPut("hasLogEntries", new Boolean(hasLogEntries));
        if (hasLogEntries) {
            scoringController.setDisplayOnly(true);
        }
        // Initialstate
        configContent.contextPut("isOverwriting", new Boolean(false));
    }

    @Override
    protected void doDispose() {
        //
    }

    /**
     * @param moduleConfiguration
     * @return boolean
     */
    public static boolean isModuleConfigValid(final ModuleConfiguration moduleConfiguration) {
        return (moduleConfiguration.get(PortfolioCourseNodeConfiguration.MAP_KEY) != null);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                courseNode.setPreConditionAccess(cond);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == configForm) {
            if (event == Event.DONE_EVENT) {
                configForm.getUpdatedConfig();
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == textForm) {
            if (event == Event.DONE_EVENT) {
                textForm.getUpdatedConfig();
                configForm.setDirtyFromOtherForm(false);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            } else if (event == Event.CHANGED_EVENT) {
                // disable modification in other forms!
                configForm.setDirtyFromOtherForm(true);
            }
        } else if (source == scoringController) {
            if (event == Event.CANCELLED_EVENT) {
                if (hasLogEntries) {
                    scoringController.setDisplayOnly(true);
                }
                configContent.contextPut("isOverwriting", new Boolean(false));
                return;
            } else if (event == Event.DONE_EVENT) {
                scoringController.updateModuleConfiguration(config);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        }
    }

    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate(PANE_TAB_ACCESSIBILITY)));
        tabbedPane.addTab(translate(PANE_TAB_CONFIG), configContent);
        tabbedPane.addTab(translate(PANE_TAB_SCORING), scoringContent);
    }

    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    @Override
    public TabbedPane getTabbedPane() {
        return myTabbedPane;
    }

    public static void removeReference(final ModuleConfiguration moduleConfig) {
        moduleConfig.remove(PortfolioCourseNodeConfiguration.MAP_KEY);
        moduleConfig.remove(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY);
    }

    public static void setReference(final RepositoryEntry repoEntry, final PortfolioStructure map, final ModuleConfiguration moduleConfig) {
        moduleConfig.set(PortfolioCourseNodeConfiguration.MAP_KEY, map.getKey());
        if (repoEntry != null && repoEntry.getSoftkey() != null) {
            moduleConfig.set(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY, repoEntry.getSoftkey());
        }
    }
}
