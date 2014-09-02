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

package org.olat.lms.course.nodes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionExpression;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.scoring.ScoreCalculator;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseInternalLinkTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.sp.SPEditController;
import org.olat.presentation.course.nodes.sp.SPPeekviewController;
import org.olat.presentation.course.nodes.st.STCourseNodeEditController;
import org.olat.presentation.course.nodes.st.STCourseNodeRunController;
import org.olat.presentation.course.nodes.st.STPeekViewController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.common.htmlpageview.SinglePageController;
import org.olat.presentation.framework.common.linkchooser.CustomLinkTreeModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.clone.CloneController;
import org.olat.presentation.framework.core.control.generic.clone.CloneLayoutControllerCreatorCallback;
import org.olat.presentation.framework.core.control.generic.clone.CloneableController;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * The structure node (ST) is used to build structures in the course hierarchy. In addition it is also used to calculate score and passed values, to syndicate these
 * values e.g. from children nodes. Example: a lesson with two tests is passed when both tests are passed. This would be designed as an ST node with two IMSTEST nodes as
 * children and a scoring rule on the ST node that syndicates the testresults. In the assessment tool the ST node results can be seen but not changed since these are
 * calculated values and not saved values from properties.
 * <P>
 * Initial Date: Feb 9, 2004<br>
 * 
 * @author Mike Stock
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class STCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode, UsedByXstream {

    private static final String TYPE = "st";
    private static final String ICON_CSS_CLASS = "o_st_icon";
    private static final Logger log = LoggerHelper.getLogger();
    private ScoreCalculator scoreCalculator;

    transient private Condition scoreExpression;

    transient private Condition passedExpression;

    /**
     * Constructor for a course building block of the type structure
     */
    public STCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        // only the precondition "access" can be configured till now
        final STCourseNodeEditController childTabCntrllr = new STCourseNodeEditController(ureq, wControl, this, course.getCourseFolderContainer(), course
                .getCourseEnvironment().getCourseGroupManager(), course.getEditorTreeModel(), euce);
        final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(), euce,
                childTabCntrllr);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final String nodecmd) {
        updateModuleConfigDefaults(false);
        Controller cont;

        final String displayType = getModuleConfiguration().getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE);
        /* STATIC_METHOD_REFACTORING removed call of public static function STCourseNodeEditController.getFileName */
        final String relPath = (String) getModuleConfiguration().get(STCourseNodeEditController.CONFIG_KEY_FILE);

        if (relPath != null && displayType.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
            // we want a user chosen overview, so display the chosen file from the
            // material folder, otherwise display the normal overview
            // reuse the Run controller from the "Single Page" building block, since
            // we need to do exactly the same task
            final Boolean allowRelativeLinks = getModuleConfiguration().getBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);
            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
            final SinglePageController spCtr = new SinglePageController(ureq, wControl, userCourseEnv.getCourseEnvironment().getCourseFolderContainer(), relPath, null,
                    allowRelativeLinks.booleanValue(), ores);
            // check if user is allowed to edit the page in the run view
            final CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
            final boolean hasEditRights = (cgm.isIdentityCourseAdministrator(ureq.getIdentity(), ores) || cgm.hasRight(ureq.getIdentity(),
                    CourseRights.RIGHT_COURSEEDITOR, ores));
            if (hasEditRights) {
                spCtr.allowPageEditing();
                // set the link tree model to internal for the HTML editor
                final CustomLinkTreeModel linkTreeModel = new CourseInternalLinkTreeModel(userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode());
                spCtr.setInternalLinkTreeModel(linkTreeModel);
            }
            spCtr.addLoggingResourceable(LoggingResourceable.wrap(this));
            // create clone wrapper layout, allow popping into second window
            final CloneLayoutControllerCreatorCallback clccc = new CloneLayoutControllerCreatorCallback() {
                @Override
                public ControllerCreator createLayoutControllerCreator(final UserRequest ureq, final ControllerCreator contentControllerCreator) {
                    return BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, new ControllerCreator() {
                        @Override
                        @SuppressWarnings("synthetic-access")
                        public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                            // wrapp in column layout, popup window needs a layout controller
                            final Controller ctr = contentControllerCreator.createController(lureq, lwControl);
                            final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, ctr.getInitialComponent(), null);
                            layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), userCourseEnv.getCourseEnvironment()));

                            final Controller wrappedCtrl = TitledWrapperHelper.getWrapper(lureq, lwControl, ctr, STCourseNode.this, ICON_CSS_CLASS);
                            layoutCtr.addDisposableChildController(wrappedCtrl);
                            return layoutCtr;
                        }
                    });
                }
            };
            final Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, spCtr, this, ICON_CSS_CLASS);
            if (wrappedCtrl instanceof CloneableController) {
                cont = new CloneController(ureq, wControl, (CloneableController) wrappedCtrl, clccc);
            } else {
                throw new AssertException("Need to be a cloneable");
            }
        } else {
            // evaluate the score accounting for this node. this uses the score accountings local
            // cache hash map to reduce unnecessary calculations
            final ScoreEvaluation se = userCourseEnv.getScoreAccounting().evalCourseNode(this);
            cont = TitledWrapperHelper.getWrapper(ureq, wControl, new STCourseNodeRunController(ureq, wControl, userCourseEnv, this, se, ne), this, ICON_CSS_CLASS);
        }

        // access the current calculated score, if there is one, so that it can be
        // displayed in the ST-Runcontroller
        return new NodeRunConstructionResult(cont);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, ne, null).getRunController();
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        if (ne.isAtLeastOneAccessible()) {
            final ModuleConfiguration config = getModuleConfiguration();
            if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE))) {
                final String file = config.getStringValue(SPEditController.CONFIG_KEY_FILE);
                if (file != null) {
                    // use single page preview if a file is configured
                    final OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, userCourseEnv.getCourseEnvironment()
                            .getCourseResourceableId());

                    return new SPPeekviewController(ureq, wControl, userCourseEnv, config, ores);
                }
            }
            // a peekview controller that displays the listing of the next ST level
            return new STPeekViewController(ureq, wControl, ne);
        } else {
            // use standard peekview without content
            return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
        }
    }

    /**
     * the structure node does not have a score itself, but calculates the score/passed info by evaluating the configured expression in the the (condition)interpreter.
     * 
     */
    @Override
    public ScoreEvaluation getUserScoreEvaluation(final UserCourseEnvironment userCourseEnv) {
        Float score = null;
        Boolean passed = null;

        if (scoreCalculator == null) {
            // this is a not-computable course node at the moment (no scoring/passing rules defined)
            return null;
        }
        final String scoreExpressionStr = scoreCalculator.getScoreExpression();
        final String passedExpressionStr = scoreCalculator.getPassedExpression();

        final ConditionInterpreter ci = userCourseEnv.getConditionInterpreter();
        userCourseEnv.getScoreAccounting().setEvaluatingCourseNode(this);
        if (scoreExpressionStr != null) {
            score = new Float(ci.evaluateCalculation(scoreExpressionStr));
        }
        if (passedExpressionStr != null) {
            passed = new Boolean(ci.evaluateCondition(passedExpressionStr));
        }
        final ScoreEvaluation se = new ScoreEvaluation(score, passed);
        return se;
    }

    /**
	 */
    @Override
    public StatusDescription isConfigValid() {
        /*
         * first check the one click cache
         */
        if (oneClickStatusCache != null) {
            return oneClickStatusCache[0];
        }

        return StatusDescription.NOERROR;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(STCourseNodeEditController.class);
        final List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public RepositoryEntry getReferencedRepositoryEntry() {
        return null;
    }

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return false;
    }

    /**
     * @return Returns the scoreCalculator.
     */
    public ScoreCalculator getScoreCalculator() {
        if (scoreCalculator == null) {
            scoreCalculator = new ScoreCalculator(null, null);
        }
        passedExpression = new Condition();
        passedExpression.setConditionId("passed");
        if (scoreCalculator.getPassedExpression() != null) {
            passedExpression.setConditionExpression(scoreCalculator.getPassedExpression());
            passedExpression.setExpertMode(true);
        }
        scoreExpression = new Condition();
        scoreExpression.setConditionId("score");
        if (scoreCalculator.getScoreExpression() != null) {
            scoreExpression.setConditionExpression(scoreCalculator.getScoreExpression());
            scoreExpression.setExpertMode(true);
        }
        return scoreCalculator;
    }

    /**
     * @param scoreCalculator
     *            The scoreCalculator to set.
     */
    public void setScoreCalculator(final ScoreCalculator scoreCalculatorP) {
        scoreCalculator = scoreCalculatorP;
        if (scoreCalculatorP == null) {
            scoreCalculator = getScoreCalculator();
        }
        String passed, score;
        passed = scoreCalculator.getPassedExpression();
        score = scoreCalculator.getScoreExpression();
        scoreExpression.setExpertMode(true);
        scoreExpression.setConditionExpression(score);
        scoreExpression.setConditionId("score");
        passedExpression.setExpertMode(true);
        passedExpression.setConditionExpression(passed);
        passedExpression.setConditionId("passed");
    }

    /**
	 */
    @Override
    public Float getCutValueConfiguration() {
        throw new OLATRuntimeException(STCourseNode.class, "Cut value never defined for ST nodes", null);
    }

    /**
	 */
    @Override
    public Float getMaxScoreConfiguration() {
        throw new OLATRuntimeException(STCourseNode.class, "Max score never defined for ST nodes", null);
    }

    /**
	 */
    @Override
    public Float getMinScoreConfiguration() {
        throw new OLATRuntimeException(STCourseNode.class, "Min score never defined for ST nodes", null);
    }

    /**
	 */
    @Override
    public String getUserCoachComment(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "No coach comments available in ST nodes", null);
    }

    /**
	 */
    @Override
    public String getUserLog(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "No user logs available in ST nodes", null);
    }

    /**
	 */
    @Override
    public String getUserUserComment(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "No comments available in ST nodes", null);
    }

    /**
	 */
    @Override
    public boolean hasCommentConfigured() {
        // never has comments
        return false;
    }

    /**
	 */
    @Override
    public boolean hasPassedConfigured() {
        if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getPassedExpression())) {
            return true;
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean hasScoreConfigured() {
        if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getScoreExpression())) {
            return true;
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean hasStatusConfigured() {
        return false;
    }

    /**
	 */
    @Override
    public boolean isEditableConfigured() {
        // ST nodes never editable, data generated on the fly
        return false;
    }

    /**
	 */
    @Override
    public void updateUserCoachComment(final String coachComment, final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "Coach comment variable can't be updated in ST nodes", null);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.data.basesecurity.Identity)
     */
    @Override
    public void updateUserScoreEvaluation(final ScoreEvaluation scoreEvaluation, final UserCourseEnvironment userCourseEnvironment, final Identity coachingIdentity,
            final boolean incrementAttempts) {
        throw new OLATRuntimeException(STCourseNode.class, "Score variable can't be updated in ST nodes", null);
    }

    /**
     * org.olat.data.basesecurity.Identity)
     */
    @Override
    public void updateUserUserComment(final String userComment, final UserCourseEnvironment userCourseEnvironment, final Identity coachingIdentity) {
        throw new OLATRuntimeException(STCourseNode.class, "Comment variable can't be updated in ST nodes", null);
    }

    /**
	 */
    @Override
    public Integer getUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "No attempts available in ST nodes", null);

    }

    /**
	 */
    @Override
    public boolean hasAttemptsConfigured() {
        return false;
    }

    /**
     * org.olat.data.basesecurity.Identity)
     */
    @Override
    public void updateUserAttempts(final Integer userAttempts, final UserCourseEnvironment userCourseEnvironment, final Identity coachingIdentity) {
        throw new OLATRuntimeException(STCourseNode.class, "Attempts variable can't be updated in ST nodes", null);
    }

    /**
	 */
    @Override
    public void incrementUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "Attempts variable can't be updated in ST nodes", null);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public Controller getDetailsEditController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "Details controler not available in ST nodes", null);
    }

    /**
	 */
    @Override
    public String getDetailsListView(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(STCourseNode.class, "Details not available in ST nodes", null);
    }

    /**
	 */
    @Override
    public String getDetailsListViewHeaderKey() {
        throw new OLATRuntimeException(STCourseNode.class, "Details not available in ST nodes", null);
    }

    /**
	 */
    @Override
    public boolean hasDetails() {
        return false;
    }

    /**
     * Update the module configuration to have all mandatory configuration flags set to usefull default values
     * 
     * @param isNewNode
     *            true: an initial configuration is set; false: upgrading from previous node configuration version, set default to maintain previous behaviour
     */
    @Override
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        final ModuleConfiguration config = getModuleConfiguration();
        if (isNewNode) {
            // use defaults for new course building blocks
            config.setBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, Boolean.FALSE.booleanValue());
            // set the default display to peekview in two columns
            config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);
            config.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 2);
            config.setConfigurationVersion(3);
        } else {
            // update to version 2
            if (config.getConfigurationVersion() < 2) {
                // use values accoring to previous functionality
                config.setBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, Boolean.FALSE.booleanValue());
                // previous version of score st node didn't have easy mode on score
                // calculator, se to expert mode
                if (getScoreCalculator() != null) {
                    getScoreCalculator().setExpertMode(true);
                }
                config.setConfigurationVersion(2);
            }
            // update to version 3
            if (config.getConfigurationVersion() < 3) {
                final String fileName = (String) config.get(STCourseNodeEditController.CONFIG_KEY_FILE);
                if (fileName != null) {
                    // set to custom file display config
                    config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE);
                } else {
                    // set the default display to plain vanilla TOC view in one column
                    config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
                    config.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
                }
                config.setConfigurationVersion(3);
            }
        }
    }

    /**
	 */
    @Override
    public List getConditionExpressions() {
        ArrayList retVal;
        final List parentsConditions = super.getConditionExpressions();
        if (parentsConditions.size() > 0) {
            retVal = new ArrayList(parentsConditions);
        } else {
            retVal = new ArrayList();
        }
        // init passedExpression and scoreExpression
        getScoreCalculator();
        //
        passedExpression.setExpertMode(true);
        String coS = passedExpression.getConditionExpression();
        if (coS != null && !coS.equals("")) {
            // an active condition is defined
            final ConditionExpression ce = new ConditionExpression(passedExpression.getConditionId());
            ce.setExpressionString(passedExpression.getConditionExpression());
            retVal.add(ce);
        }
        scoreExpression.setExpertMode(true);
        coS = scoreExpression.getConditionExpression();
        if (coS != null && !coS.equals("")) {
            // an active condition is defined
            final ConditionExpression ce = new ConditionExpression(scoreExpression.getConditionId());
            ce.setExpressionString(scoreExpression.getConditionExpression());
            retVal.add(ce);
        }
        //
        return retVal;
    }

    /**
	 */
    @Override
    public String getDisplayOption() {
        // if nothing other defined, view content only, when a structure node
        // contains an html-file.

        final ModuleConfiguration config = getModuleConfiguration();
        final String thisConf = super.getDisplayOption(false);
        if (thisConf == null && config.get(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE).equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
            if (log.isDebugEnabled()) {
                log.debug("no displayOption set, use default (content)" + thisConf);
            }
            return CourseNode.DISPLAY_OPTS_CONTENT;
        }
        if (log.isDebugEnabled()) {
            log.debug("there is a config set, use it: " + thisConf);
        }
        return super.getDisplayOption();
    }

}
