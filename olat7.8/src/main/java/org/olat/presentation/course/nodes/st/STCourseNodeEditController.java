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

import java.util.Iterator;
import java.util.List;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.run.scoring.ScoreCalculator;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseInternalLinkTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.commons.filechooser.FileChooseCreateEditController;
import org.olat.presentation.commons.filechooser.LinkChooseCreateEditController;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Description:<BR/>
 * Edit controller for a course node of type structure
 * <P/>
 * Initial Date: Oct 12, 2004
 * 
 * @author gnaegi
 */
public class STCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    private static final String PANE_TAB_ST_SCORECALCULATION = "pane.tab.st_scorecalculation";
    private static final String PANE_TAB_ST_CONFIG = "pane.tab.st_config";
    private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

    /** configuration key for the filename */
    public static final String CONFIG_KEY_FILE = "file";
    /**
     * configuration key: should relative links like ../otherfolder/my.css be allowed? *
     */
    public static final String CONFIG_KEY_ALLOW_RELATIVE_LINKS = "allowRelativeLinks";
    // key to store information on what to display in the run
    public static final String CONFIG_KEY_DISPLAY_TYPE = "display";
    // display a custom file
    public static final String CONFIG_VALUE_DISPLAY_FILE = "file";
    // display a simple table on content
    public static final String CONFIG_VALUE_DISPLAY_TOC = "toc";
    // display a detailed peek view
    public static final String CONFIG_VALUE_DISPLAY_PEEKVIEW = "peekview";
    // key to display the enabled child node peek views
    public static final String CONFIG_KEY_PEEKVIEW_CHILD_NODES = "peekviewChildNodes";
    // key to store the number of columns
    public static final String CONFIG_KEY_COLUMNS = "columns";

    private static final String[] paneKeys = { PANE_TAB_ST_SCORECALCULATION, PANE_TAB_ST_CONFIG, PANE_TAB_ACCESSIBILITY };

    private final STCourseNode stNode;
    private EditScoreCalculationExpertForm scoreExpertForm;
    private EditScoreCalculationEasyForm scoreEasyForm;
    private final List<CourseNode> assessableChildren;
    private final STCourseNodeDisplayConfigFormController nodeDisplayConfigFormController;

    private final VelocityContainer score, configvc;
    private final Link activateEasyModeButton;
    private final Link activateExpertModeButton;

    private final VFSContainer courseFolderContainer;
    private String chosenFile;
    private Boolean allowRelativeLinks;

    private Panel fccePanel;
    private FileChooseCreateEditController fccecontr;
    private final ConditionEditController accessibilityCondContr;

    private boolean editorEnabled = false;
    private final UserCourseEnvironment euce;
    private TabbedPane myTabbedPane;
    private final CourseEditorTreeModel editorModel;

    /**
     * @param ureq
     * @param wControl
     * @param stNode
     * @param courseFolderPath
     * @param groupMgr
     * @param editorModel
     */
    public STCourseNodeEditController(final UserRequest ureq, final WindowControl wControl, final STCourseNode stNode, final VFSContainer courseFolderContainer,
            final CourseGroupManager groupMgr, final CourseEditorTreeModel editorModel, final UserCourseEnvironment euce) {
        super(ureq, wControl);

        this.stNode = stNode;
        this.courseFolderContainer = courseFolderContainer;
        this.euce = euce;
        this.editorModel = editorModel;

        final Translator fallback = new PackageTranslator(PackageUtil.getPackageName(Condition.class), ureq.getLocale());
        final Translator newTranslator = new PackageTranslator(PackageUtil.getPackageName(STCourseNodeEditController.class), ureq.getLocale(), fallback);
        setTranslator(newTranslator);

        score = this.createVelocityContainer("scoreedit");
        activateEasyModeButton = LinkFactory.createButtonSmall("cmd.activate.easyMode", score, this);
        activateExpertModeButton = LinkFactory.createButtonSmall("cmd.activate.expertMode", score, this);

        configvc = this.createVelocityContainer("config");

        // Load configured value for file if available and enable editor when in
        // file display move, even when no file is selected (this will display the
        // file selector button)
        chosenFile = (String) stNode.getModuleConfiguration().get(CONFIG_KEY_FILE);
        editorEnabled = (CONFIG_VALUE_DISPLAY_FILE.equals(stNode.getModuleConfiguration().getStringValue(CONFIG_KEY_DISPLAY_TYPE)));

        allowRelativeLinks = stNode.getModuleConfiguration().getBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS);

        nodeDisplayConfigFormController = new STCourseNodeDisplayConfigFormController(ureq, wControl, stNode.getModuleConfiguration(),
                editorModel.getCourseEditorNodeById(stNode.getIdent()));
        listenTo(nodeDisplayConfigFormController);
        configvc.put("nodeDisplayConfigFormController", nodeDisplayConfigFormController.getInitialComponent());

        if (editorEnabled) {
            configvc.contextPut("editorEnabled", Boolean.valueOf(editorEnabled));
            addStartEditorToView(ureq);
        }

        // Find assessable children nodes
        assessableChildren = AssessmentHelper.getAssessableNodes(editorModel, stNode);

        // Accessibility precondition
        final Condition accessCondition = stNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, accessCondition, "accessabilityConditionForm", assessableChildren, euce);
        this.listenTo(accessibilityCondContr);

        ScoreCalculator scoreCalc = stNode.getScoreCalculator();
        if (scoreCalc != null) {
            if (scoreCalc.isExpertMode() && scoreCalc.getPassedExpression() == null && scoreCalc.getScoreExpression() == null) {
                scoreCalc = null;
            } else if (!scoreCalc.isExpertMode() && scoreCalc.getPassedExpressionFromEasyModeConfiguration() == null
                    && scoreCalc.getScoreExpressionFromEasyModeConfiguration() == null) {
                scoreCalc = null;
            }
        }

        if (assessableChildren.size() == 0 && scoreCalc == null) {
            // show only the no assessable children message, if no previous score
            // config exists.
            score.contextPut("noAssessableChildren", Boolean.TRUE);
        } else {
            score.contextPut("noAssessableChildren", Boolean.FALSE);
        }

        // Init score calculator form
        if (scoreCalc != null && scoreCalc.isExpertMode()) {
            initScoreExpertForm(ureq);
        } else {
            initScoreEasyForm(ureq);
        }
    }

    /**
     * Initialize an easy mode score calculator form and push it to the score velocity container
     */
    private void initScoreEasyForm(final UserRequest ureq) {
        removeAsListenerAndDispose(scoreEasyForm);
        scoreEasyForm = new EditScoreCalculationEasyForm(ureq, getWindowControl(), stNode.getScoreCalculator(), assessableChildren);
        listenTo(scoreEasyForm);
        score.put("scoreForm", scoreEasyForm.getInitialComponent());
        score.contextPut("isExpertMode", Boolean.FALSE);
    }

    /**
     * Initialize an expert mode score calculator form and push it to the score velocity container
     */
    private void initScoreExpertForm(final UserRequest ureq) {
        removeAsListenerAndDispose(scoreExpertForm);
        scoreExpertForm = new EditScoreCalculationExpertForm(ureq, getWindowControl(), stNode.getScoreCalculator(), euce, assessableChildren);
        listenTo(scoreExpertForm);
        scoreExpertForm.setScoreCalculator(stNode.getScoreCalculator());
        score.put("scoreForm", scoreExpertForm.getInitialComponent());
        score.contextPut("isExpertMode", Boolean.TRUE);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == activateEasyModeButton) {
            initScoreEasyForm(ureq);
        } else if (source == activateExpertModeButton) {
            initScoreExpertForm(ureq);
        }
    }

    /**
     * @param nodeDescriptions
     * @return the warning message if any, null otherwise
     */

    private String getWarningMessage(final List<String> nodeDescriptions) {
        if (nodeDescriptions.size() > 0) {
            String invalidNodeTitles = "";
            final Iterator<String> titleIterator = nodeDescriptions.iterator();
            while (titleIterator.hasNext()) {
                if (!invalidNodeTitles.equals("")) {
                    invalidNodeTitles += "; ";
                }
                invalidNodeTitles += titleIterator.next();
            }
            return translate("scform.error.configuration") + ": " + invalidNodeTitles;
        }
        return null;
    }

    /**
	 */

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                stNode.setPreConditionAccess(cond);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (event == FileChooseCreateEditController.FILE_CHANGED_EVENT) {
            chosenFile = fccecontr.getChosenFile();
            if (chosenFile != null) {
                stNode.getModuleConfiguration().set(CONFIG_KEY_FILE, chosenFile);
            } else {
                stNode.getModuleConfiguration().remove(CONFIG_KEY_FILE);
            }
            fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
        } else if (event == FileChooseCreateEditController.ALLOW_RELATIVE_LINKS_CHANGED_EVENT) {
            allowRelativeLinks = fccecontr.getAllowRelativeLinks();
            stNode.getModuleConfiguration().setBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS, allowRelativeLinks.booleanValue());
            fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);

        } else if (source == nodeDisplayConfigFormController) {
            if (event == Event.DONE_EVENT) {
                // update the module configuration
                final ModuleConfiguration moduleConfig = stNode.getModuleConfiguration();
                nodeDisplayConfigFormController.updateModuleConfiguration(moduleConfig);
                allowRelativeLinks = moduleConfig.getBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS);
                // update some class vars
                if (CONFIG_VALUE_DISPLAY_FILE.equals(moduleConfig.getStringValue(CONFIG_KEY_DISPLAY_TYPE))) {
                    editorEnabled = true;
                    configvc.contextPut("editorEnabled", Boolean.valueOf(editorEnabled));
                    stNode.getModuleConfiguration().set(CONFIG_KEY_FILE, chosenFile);
                    addStartEditorToView(ureq);
                } else { // user generated overview
                    editorEnabled = false;
                    configvc.contextPut("editorEnabled", Boolean.valueOf(editorEnabled));
                    fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
                    // Let other config values from old config setup remain in config,
                    // maybe used when user switches back to other config (OLAT-5610)
                }
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }

        } else if (source == scoreEasyForm) {

            if (event == Event.DONE_EVENT) {
                // show warning if the score might be wrong because of the invalid nodes used for calculation
                final List<String> testElemWithNoResource = scoreEasyForm.getInvalidNodeDescriptions();
                final String msg = getWarningMessage(testElemWithNoResource);
                if (msg != null) {
                    showWarning(msg);
                }

                final ScoreCalculator sc = scoreEasyForm.getScoreCalulator();
                /*
                 * OLAT-1144 bug fix if Calculation Score -> NO and Calculate passing score -> NO we get a ScoreCalculator == NULL !
                 */
                if (sc != null) {
                    sc.setPassedExpression(sc.getPassedExpressionFromEasyModeConfiguration());
                    sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
                }
                // ..setScoreCalculator(sc) can handle NULL values!
                stNode.setScoreCalculator(sc);
                initScoreEasyForm(ureq); // reload form, remove deleted nodes
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            } else if (event == Event.CANCELLED_EVENT) { // reload form
                initScoreEasyForm(ureq);
            }
        } else if (source == scoreExpertForm) {
            if (event == Event.DONE_EVENT) {
                // show warning if the score might be wrong because of the invalid nodes used for calculation
                final List<String> testElemWithNoResource = scoreExpertForm.getInvalidNodeDescriptions();
                final String msg = getWarningMessage(testElemWithNoResource);
                if (msg != null) {
                    getWindowControl().setWarning(msg);
                }

                final ScoreCalculator sc = scoreExpertForm.getScoreCalulator();
                /*
                 * OLAT-1144 bug fix if a ScoreCalculator == NULL !
                 */
                if (sc != null) {
                    sc.clearEasyMode();
                }
                // ..setScoreCalculator(sc) can handle NULL values!
                stNode.setScoreCalculator(sc);
                fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
            } else if (event == Event.CANCELLED_EVENT) { // reload form
                initScoreExpertForm(ureq);
            }
        }
    }

    private void addStartEditorToView(final UserRequest ureq) {
        this.fccecontr = new LinkChooseCreateEditController(ureq, getWindowControl(), chosenFile, allowRelativeLinks, courseFolderContainer,
                new CourseInternalLinkTreeModel(editorModel));
        this.listenTo(fccecontr);

        fccePanel = new Panel("filechoosecreateedit");
        final Component fcContent = fccecontr.getInitialComponent();
        fccePanel.setContent(fcContent);
        configvc.put(fccePanel.getComponentName(), fccePanel);
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
        tabbedPane.addTab(translate(PANE_TAB_ST_CONFIG), configvc);
        tabbedPane.addTab(translate(PANE_TAB_ST_SCORECALCULATION), score);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
    }

    /**
     * @param mc
     *            The module confguration
     * @return The configured file name
     */
    public static String getFileName(final ModuleConfiguration mc) {
        return (String) mc.get(CONFIG_KEY_FILE);
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
