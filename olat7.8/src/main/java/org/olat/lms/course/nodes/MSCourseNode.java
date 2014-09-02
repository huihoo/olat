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

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.properties.PersistingCoursePropertyManager;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.ms.MSCourseNodeEditController;
import org.olat.presentation.course.nodes.ms.MSCourseNodeRunController;
import org.olat.presentation.course.nodes.ms.MSEditFormController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Initial Date: Jun 16, 2004
 * 
 * @author gnaegi
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class MSCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode, UsedByXstream {

    public static final String TYPE = "ms";
    /** configuration: score can be set */
    public static final String CONFIG_KEY_HAS_SCORE_FIELD = "hasScoreField";
    /** configuration: score min value */
    public static final String CONFIG_KEY_SCORE_MIN = "scoreMin";
    /** configuration: score max value */
    public static final String CONFIG_KEY_SCORE_MAX = "scoreMax";
    /** configuration: passed can be set */
    public static final String CONFIG_KEY_HAS_PASSED_FIELD = "hasPassedField";
    /** configuration: passed set to when score higher than cut value */
    public static final String CONFIG_KEY_PASSED_CUT_VALUE = "passedCutValue";
    /** configuration: comment can be set */
    public static final String CONFIG_KEY_HAS_COMMENT_FIELD = "hasCommentField";
    /** configuration: infotext for user */
    public static final String CONFIG_KEY_INFOTEXT_USER = "infoTextUser";
    /** configuration: infotext for coach */
    public static final String CONFIG_KEY_INFOTEXT_COACH = "nfoTextCoach";

    /**
     * Constructor for a course building block of type manual score
     */
    public MSCourseNode() {
        super(TYPE);
        MSCourseNode.initDefaultConfig(getModuleConfiguration());
    }

    /**
     * Adds to the given module configuration the default configuration for the manual scoring
     * 
     * @param moduleConfiguration
     */
    public static void initDefaultConfig(final ModuleConfiguration moduleConfiguration) {
        moduleConfiguration.set(CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE);
        moduleConfiguration.set(CONFIG_KEY_SCORE_MIN, new Float(0));
        moduleConfiguration.set(CONFIG_KEY_SCORE_MAX, new Float(0));
        moduleConfiguration.set(CONFIG_KEY_HAS_PASSED_FIELD, Boolean.TRUE);
        // no preset for passed cut value -> manual setting of passed
        moduleConfiguration.set(CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.TRUE);
        moduleConfiguration.set(CONFIG_KEY_INFOTEXT_USER, "");
        moduleConfiguration.set(CONFIG_KEY_INFOTEXT_COACH, "");
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        final MSCourseNodeEditController childTabCntrllr = new MSCourseNodeEditController(ureq, wControl, this, course, euce);
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
        Controller controller;
        // Do not allow guests to have manual scoring
        final Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
            final String title = trans.translate("guestnoaccess.title");
            final String message = trans.translate("guestnoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            controller = new MSCourseNodeRunController(ureq, wControl, userCourseEnv, this, true);
        }

        final Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_ms_icon");
        return new NodeRunConstructionResult(wrappedCtrl);
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
	 */
    @Override
    public StatusDescription isConfigValid() {
        /*
         * first check the one click cache
         */
        if (oneClickStatusCache != null) {
            return oneClickStatusCache[0];
        }

        final boolean isValid = MSEditFormController.isConfigValid(getModuleConfiguration());
        StatusDescription sd = StatusDescription.NOERROR;
        if (!isValid) {
            // FIXME: refine statusdescriptions by moving the statusdescription
            // generation to the MSEditForm
            final String shortKey = "error.missingconfig.short";
            final String longKey = "error.missingconfig.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(MSEditFormController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(MSCourseNodeEditController.PANE_TAB_CONFIGURATION);
        }
        return sd;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(MSEditFormController.class);
        final List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public ScoreEvaluation getUserScoreEvaluation(final UserCourseEnvironment userCourseEnvironment) {
        // read score from properties
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        Boolean passed = null;
        Float score = null;
        // only db lookup if configured, else return null
        if (hasPassedConfigured()) {
            passed = am.getNodePassed(this, mySelf);
        }
        if (hasScoreConfigured()) {
            score = am.getNodeScore(this, mySelf);
        }

        final ScoreEvaluation se = new ScoreEvaluation(score, passed);
        return se;
    }

    /**
	 */
    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        final CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
        final List list = cpm.listCourseNodeProperties(this, null, null, null);
        if (list.size() == 0) {
            return null; // no properties created yet
        }
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_MS_, locale);
        return trans.translate("warn.nodedelete");
    }

    /**
	 */
    @Override
    public void cleanupOnDelete(final ICourse course) {
        final CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
        // Delete all properties: score, passed, log, comment, coach_comment
        pm.deleteNodeProperties(this, null);
    }

    /**
	 */
    @Override
    public boolean hasCommentConfigured() {
        final ModuleConfiguration config = getModuleConfiguration();
        final Boolean comment = (Boolean) config.get(CONFIG_KEY_HAS_COMMENT_FIELD);
        if (comment == null) {
            return false;
        }
        return comment.booleanValue();
    }

    /**
	 */
    @Override
    public boolean hasPassedConfigured() {
        final ModuleConfiguration config = getModuleConfiguration();
        final Boolean passed = (Boolean) config.get(CONFIG_KEY_HAS_PASSED_FIELD);
        if (passed == null) {
            return false;
        }
        return passed.booleanValue();
    }

    /**
	 */
    @Override
    public boolean hasScoreConfigured() {
        final ModuleConfiguration config = getModuleConfiguration();
        final Boolean score = (Boolean) config.get(CONFIG_KEY_HAS_SCORE_FIELD);
        if (score == null) {
            return false;
        }
        return score.booleanValue();
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
    public Float getMaxScoreConfiguration() {
        if (!hasScoreConfigured()) {
            throw new OLATRuntimeException(MSCourseNode.class, "getMaxScore not defined when hasScore set to false", null);
        }
        final ModuleConfiguration config = getModuleConfiguration();
        final Float max = (Float) config.get(CONFIG_KEY_SCORE_MAX);
        return max;
    }

    /**
	 */
    @Override
    public Float getMinScoreConfiguration() {
        if (!hasScoreConfigured()) {
            throw new OLATRuntimeException(MSCourseNode.class, "getMinScore not defined when hasScore set to false", null);
        }
        final ModuleConfiguration config = getModuleConfiguration();
        final Float min = (Float) config.get(CONFIG_KEY_SCORE_MIN);
        return min;
    }

    /**
	 */
    @Override
    public Float getCutValueConfiguration() {
        if (!hasPassedConfigured()) {
            throw new OLATRuntimeException(MSCourseNode.class, "getCutValue not defined when hasPassed set to false", null);
        }
        final ModuleConfiguration config = getModuleConfiguration();
        final Float cut = (Float) config.get(CONFIG_KEY_PASSED_CUT_VALUE);
        return cut;
    }

    /**
	 */
    @Override
    public String getUserCoachComment(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final String coachCommentValue = am.getNodeCoachComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
        return coachCommentValue;
    }

    /**
	 */
    @Override
    public String getUserUserComment(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final String userCommentValue = am.getNodeComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
        return userCommentValue;
    }

    /**
	 */
    @Override
    public String getUserLog(final UserCourseEnvironment userCourseEnvironment) {
        final UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
        final String logValue = am.getUserNodeLog(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
        return logValue;
    }

    /**
	 */
    @Override
    public boolean isEditableConfigured() {
        // manual scoring fields can be edited manually
        return true;
    }

    /**
	 */
    @Override
    public void updateUserCoachComment(final String coachComment, final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        if (coachComment != null) {
            am.saveNodeCoachComment(this, mySelf, coachComment);
        }
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.data.basesecurity.Identity)
     */
    @Override
    public void updateUserScoreEvaluation(final ScoreEvaluation scoreEvaluation, final UserCourseEnvironment userCourseEnvironment, final Identity coachingIdentity,
            final boolean incrementAttempts) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        am.saveScoreEvaluation(this, coachingIdentity, mySelf, new ScoreEvaluation(scoreEvaluation.getScore(), scoreEvaluation.getPassed()), userCourseEnvironment,
                incrementAttempts);
    }

    /**
     * org.olat.data.basesecurity.Identity)
     */
    @Override
    public void updateUserUserComment(final String userComment, final UserCourseEnvironment userCourseEnvironment, final Identity coachingIdentity) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        if (userComment != null) {
            am.saveNodeComment(this, coachingIdentity, mySelf, userComment);
        }
    }

    /**
	 */
    @Override
    public Integer getUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(MSCourseNode.class, "No attempts available in MS nodes", null);

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
        throw new OLATRuntimeException(MSCourseNode.class, "Attempts variable can't be updated in MS nodes", null);
    }

    /**
	 */
    @Override
    public void incrementUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(MSCourseNode.class, "Attempts variable can't be updated in MS nodes", null);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public Controller getDetailsEditController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(MSCourseNode.class, "Details controler not available in MS nodes", null);
    }

    /**
	 */
    @Override
    public String getDetailsListView(final UserCourseEnvironment userCourseEnvironment) {
        throw new OLATRuntimeException(MSCourseNode.class, "Details not available in MS nodes", null);
    }

    /**
	 */
    @Override
    public String getDetailsListViewHeaderKey() {
        throw new OLATRuntimeException(MSCourseNode.class, "Details not available in MS nodes", null);
    }

    /**
	 */
    @Override
    public boolean hasDetails() {
        return false;
    }

}
