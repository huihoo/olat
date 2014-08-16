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

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.fileresource.ScormCPFileResource;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.imports.CourseExportEBL;
import org.olat.lms.course.imports.ImportReferencesEBL;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.scorm.ScormEditController;
import org.olat.presentation.course.nodes.scorm.ScormRunController;
import org.olat.presentation.course.repository.ImportReferencesController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.scorm.assessment.ScormResultDetailsController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class ScormCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode, UsedByXstream {
    private static final String TYPE = "scorm";
    private static final int CURRENT_CONFIG_VERSION = 3;

    /**
     * Constructor for a course building block of the type IMS CP learning content
     */
    public ScormCourseNode() {
        super(TYPE);
        // init default values
        updateModuleConfigDefaults(true);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        final ScormEditController childTabCntrllr = new ScormEditController(this, ureq, wControl, course, euce);
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
        final ScormRunController cprunC = new ScormRunController(getModuleConfiguration(), ureq, userCourseEnv, wControl, this, false);
        final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, cprunC, this, "o_scorm_icon");
        // no inline-in-olat-menu integration possible: no display configuration option
        return new NodeRunConstructionResult(ctrl);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        updateModuleConfigDefaults(false);
        final ScormRunController cprunC = new ScormRunController(getModuleConfiguration(), ureq, userCourseEnv, wControl, this, true);
        return new NodeRunConstructionResult(cprunC).getRunController();
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

        StatusDescription sd = StatusDescription.NOERROR;
        if (!isModuleConfigValid()) {
            final String shortKey = "error.noreference.short";
            final String longKey = "error.noreference.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(ScormEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(ScormEditController.PANE_TAB_CPCONFIG);
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
        final String translatorStr = PackageUtil.getPackageName(ScormEditController.class);
        final List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return true;
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
            config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
            config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
            config.setBooleanEntry(ScormEditController.CONFIG_SHOWNAVBUTTONS, Boolean.TRUE.booleanValue());
            config.set(ScormEditController.CONFIG_HEIGHT, ScormEditController.CONFIG_HEIGHT_AUTO);
            config.setConfigurationVersion(1);
        } else {
            int version = config.getConfigurationVersion();
            if (version < CURRENT_CONFIG_VERSION) {
                // Loaded config is older than current config version => migrate
                if (version == 1) {
                    version = 2;
                    // remove old config from previous versions
                    config.remove(NodeEditController.CONFIG_INTEGRATION);
                    // add new parameter 'shownavbuttons' and 'height'
                    config.setBooleanEntry(ScormEditController.CONFIG_SHOWNAVBUTTONS, Boolean.TRUE.booleanValue());
                    config.set(ScormEditController.CONFIG_HEIGHT, ScormEditController.CONFIG_HEIGHT_AUTO);
                }

                if (version == 2) {
                    version = 3;
                    config.set(NodeEditController.CONFIG_CONTENT_ENCODING, NodeEditController.CONFIG_CONTENT_ENCODING_AUTO);
                    config.set(NodeEditController.CONFIG_JS_ENCODING, NodeEditController.CONFIG_JS_ENCODING_AUTO);
                }
                // version is now set to current version
                config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
            }
        }
    }

    @Override
    public void exportNode(final File exportDirectory, final ICourse course) {
        getCourseExportEBL().exportNode(exportDirectory, this);
    }

    private CourseExportEBL getCourseExportEBL() {
        return CoreSpringFactory.getBean(CourseExportEBL.class);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl) {
        final File importSubdir = new File(importDirectory, getIdent());
        final RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importSubdir);
        if (!rie.anyExportedPropertiesAvailable()) {
            return null;
        }

        // do import referenced repository entries
        if (unattendedImport) {
            final Identity admin = getBaseSecurity().findIdentityByName("administrator");
            getImportReferencesEBL().doImport(rie, this, true, admin);
            return null;
        } else {
            return new ImportReferencesController(ureq, wControl, this, getResourceType(), rie);
        }
    }

    private ImportReferencesEBL getImportReferencesEBL() {
        return CoreSpringFactory.getBean(ImportReferencesEBL.class);
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private String getResourceType() {
        return ScormCPFileResource.TYPE_NAME;
    }

    /**
	 */
    @Override
    public ScoreEvaluation getUserScoreEvaluation(final UserCourseEnvironment userCourseEnvironment) {
        // read score from properties save score, passed and attempts information
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final Boolean passed = am.getNodePassed(this, mySelf);
        final Float score = am.getNodeScore(this, mySelf);
        final ScoreEvaluation se = new ScoreEvaluation(score, passed);
        return se;
    }

    /**
	 */
    @Override
    public Float getCutValueConfiguration() {
        final ModuleConfiguration config = this.getModuleConfiguration();
        final int cutValue = config.getIntegerSafe(ScormEditController.CONFIG_CUTVALUE, 0);
        return new Float(new Integer(cutValue).floatValue());
    }

    /**
	 */
    @Override
    public Float getMaxScoreConfiguration() {
        // According to SCORM Standard, SCORE is between 0 and 100.
        return new Float(100);
    }

    /**
	 */
    @Override
    public Float getMinScoreConfiguration() {
        // According to SCORM Standard, SCORE is between 0 and 100.
        return new Float(0);
    }

    /**
	 */
    @Override
    public boolean hasCommentConfigured() {
        return false;
    }

    /**
	 */
    @Override
    public boolean hasPassedConfigured() {
        return getModuleConfiguration().getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE, false);
    }

    /**
	 */
    @Override
    public boolean hasScoreConfigured() {
        return getModuleConfiguration().getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE, false);
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
        return getModuleConfiguration().getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE, false);
    }

    /**
	 */
    @Override
    public void updateUserCoachComment(final String coachComment, final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        if (coachComment != null) {
            am.saveNodeCoachComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity(), coachComment);
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
        if (userComment != null) {
            final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
            final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
            am.saveNodeComment(this, coachingIdentity, mySelf, userComment);
        }
    }

    /**
	 */
    @Override
    public String getUserCoachComment(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final String coachCommentValue = am.getNodeCoachComment(this, mySelf);
        return coachCommentValue;
    }

    /**
	 */
    @Override
    public String getUserUserComment(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final String userCommentValue = am.getNodeComment(this, mySelf);
        return userCommentValue;
    }

    /**
	 */
    @Override
    public String getUserLog(final UserCourseEnvironment userCourseEnvironment) {
        final UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final String logValue = am.getUserNodeLog(this, mySelf);
        return logValue;
    }

    /**
	 */
    @Override
    public Integer getUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final Integer userAttemptsValue = am.getNodeAttempts(this, mySelf);
        return userAttemptsValue;

    }

    /**
	 */
    @Override
    public boolean hasAttemptsConfigured() {
        return getModuleConfiguration().getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE, false);
    }

    /**
     * org.olat.data.basesecurity.Identity)
     */
    @Override
    public void updateUserAttempts(final Integer userAttempts, final UserCourseEnvironment userCourseEnvironment, final Identity coachingIdentity) {
        if (userAttempts != null) {
            final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
            final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
            am.saveNodeAttempts(this, coachingIdentity, mySelf, userAttempts);
        }
    }

    /**
	 */
    @Override
    public void incrementUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        am.incrementNodeAttempts(this, mySelf, userCourseEnvironment);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public Controller getDetailsEditController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnvironment) {
        return new ScormResultDetailsController(ureq, wControl, this, userCourseEnvironment);
    }

    /**
	 */
    @Override
    public String getDetailsListView(final UserCourseEnvironment userCourseEnvironment) {
        return null;
    }

    /**
	 */
    @Override
    public String getDetailsListViewHeaderKey() {
        return null;
    }

    /**
	 */
    @Override
    public boolean hasDetails() {
        return getModuleConfiguration().getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE, false);
    }

    // //////////////////////////// fix it

    /**
	 */
    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        // checking for data is too complex - we would have to work through all users
        // of the system since data is stored under users name instead of the repo entry
        // with many users on the system it could take quite some time and the user
        // is waiting in a workflow
        // FIXME gs
        // see comment on cleanupOnDelete
        return null;
    }

    /**
	 */
    @Override
    public void cleanupOnDelete(final ICourse course) {
        final CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
        // 1) Delete all properties: score, passed, log, comment, coach_comment,
        // attempts
        pm.deleteNodeProperties(this, null);
        // 2) Delete all user files for this scorm node
        // FIXME gs
        // it is problematic that the data is stored using username/courseid-scormid/
        // much better would be /courseid-scormid/username/
        // I would consider refatoring this and setting up an upgrade task that moves the
        // folders accordingly
    }

    /**
     * Override default implementation
     * 
     */
    @Override
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
        return super.archiveNodeData(locale, course, exportDirectory, charset);
        // copy all user directories containing the xml files into the export dir

        // FIXME gs
        // we would have to work through all users
        // of the system since data is stored under users name instead of the repo entry
        // with many users on the system it could take quite some time and the user
        // is waiting in a workflow
        // see comment on cleanupOnDelete
    }

}
