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
import org.olat.data.commons.database.exception.DBRuntimeException;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.fileresource.TestFileResource;
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
import org.olat.lms.ims.qti.exporter.QTIExportFormatter;
import org.olat.lms.ims.qti.exporter.QTIExportFormatterCSVType1;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.qti.QTIResultService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.iq.IQEditController;
import org.olat.presentation.course.nodes.iq.IQRunController;
import org.olat.presentation.course.nodes.iq.IQUIFactory;
import org.olat.presentation.course.repository.ImportReferencesController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.KnownIssueException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Feb 9, 2004
 * 
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQTESTCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode, UsedByXstream {
    public static final String TYPE = "iqtest";

    /**
     * owners&tutors get notified (per email) at assignment delivery (student uploads file in drop-box)
     */
    public static final String CONFIRMATION_REQUESTED = "confirmation_for_tutors";

    private static final int CURRENT_CONFIG_VERSION = 2;

    public IQTESTCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    private QTIResultService getQtiResultService() {
        return CoreSpringFactory.getBean(QTIResultService.class);
    }

    private QtiEBL getQtiEBL() {
        return CoreSpringFactory.getBean(QtiEBL.class);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        final TabbableController childTabCntrllr = IQUIFactory.createIQTestEditController(ureq, wControl, course, this, course.getCourseEnvironment()
                .getCourseGroupManager(), euce);
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
        final Controller controller = IQUIFactory.createIQTestRunController(ureq, wControl, userCourseEnv, ne, this);
        final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_iqtest_icon");
        return new NodeRunConstructionResult(ctrl);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        return IQUIFactory.createIQTestPreviewController(ureq, wControl, userCourseEnv, ne, this);
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

        boolean isValid = getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY) != null;
        if (isValid) {
            /*
             * COnfiugre an IQxxx BB with a repo entry, do not publish this BB, mark IQxxx as deleted, remove repo entry, undelete BB IQxxx and bang you enter this if.
             */
            final Object repoEntry = getReferencedRepositoryEntry();
            if (repoEntry == null) {
                isValid = false;
                removeRepositoryReference();
                // FIXME:ms: may be show a refined error message, that the former referenced repo entry is meanwhile deleted.
            }
        }
        StatusDescription sd = StatusDescription.NOERROR;
        if (!isValid) {
            // FIXME: refine statusdescriptions
            final String shortKey = "error.test.undefined.short";
            final String longKey = "error.test.undefined.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(IQEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(IQEditController.PANE_TAB_IQCONFIG_TEST);
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
        final String translatorStr = PackageUtil.getPackageName(IQEditController.class);
        final List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
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
        final Long assessmentID = am.getAssessmentID(this, mySelf);
        final ScoreEvaluation se = new ScoreEvaluation(score, passed, assessmentID);
        return se;
    }

    /**
	 */
    @Override
    public Float getCutValueConfiguration() {
        final ModuleConfiguration config = this.getModuleConfiguration();
        return (Float) config.get(IQEditController.CONFIG_KEY_CUTVALUE);
    }

    /**
	 */
    @Override
    public Float getMaxScoreConfiguration() {
        final ModuleConfiguration config = this.getModuleConfiguration();
        return (Float) config.get(IQEditController.CONFIG_KEY_MAXSCORE);
    }

    /**
	 */
    @Override
    public Float getMinScoreConfiguration() {
        final ModuleConfiguration config = this.getModuleConfiguration();
        return (Float) config.get(IQEditController.CONFIG_KEY_MINSCORE);
    }

    /**
	 */
    @Override
    public boolean hasCommentConfigured() {
        // coach should be able to add comments here, visible to users
        return true;
    }

    /**
	 */
    @Override
    public boolean hasPassedConfigured() {
        return true;
    }

    /**
	 */
    @Override
    public boolean hasScoreConfigured() {
        return true;
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
        // test scoring fields can be edited manually
        return true;
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
        try {
            am.saveScoreEvaluation(this, coachingIdentity, mySelf, scoreEvaluation, userCourseEnvironment, incrementAttempts);
        } catch (final DBRuntimeException ex) {
            throw new KnownIssueException("DBRuntimeException - Row was updated or deleted...", 3570, ex);
        }
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
    public boolean needsReferenceToARepositoryEntry() {
        return true;
    }

    /**
	 */
    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        // Check if there are qtiresults for this test
        final String repositorySoftKey = (String) getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        QTIResultService qtiResultService = getQtiResultService();
        if (qtiResultService.hasResultSets(course.getResourceableId(), this.getIdent(), repKey)) {
            final Translator trans = PackageUtil.createPackageTranslator(IQRunController.class, locale);
            return trans.translate("info.nodedelete");
        }
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
        // 2) Delete all qtiresults for this node
        final String repositorySoftKey = (String) getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        final Long olatRes = course.getResourceableId();
        final String olatResDet = this.getIdent();
        QTIResultService qtiResultService = getQtiResultService();
        qtiResultService.deleteAllResults(olatRes, olatResDet, repKey);
    }

    /**
     * Override default implementation
     * 
     */
    @Override
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
        super.archiveNodeData(locale, course, exportDirectory, charset);

        final QTIExportFormatter formatter = new QTIExportFormatterCSVType1(locale, null, "\t", "\"", "\\", "\r\n", false, false);
        return getQtiEBL()
                .archiveIQTestCourseNode(formatter, getModuleConfiguration(), course.getResourceableId(), getShortTitle(), getIdent(), exportDirectory, charset);

    }

    @Override
    public void exportNode(final File exportDirectory, final ICourse course) {
        getCourseExportEBL().exportNode(exportDirectory, this);
    }

    private CourseExportEBL getCourseExportEBL() {
        return CoreSpringFactory.getBean(CourseExportEBL.class);
    }

    /**
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
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private String getResourceType() {
        return TestFileResource.TYPE_NAME;
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
        return true;
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
        return IQUIFactory.createIQTestDetailsEditController(userCourseEnvironment.getCourseEnvironment().getCourseResourceableId(), this.getIdent(),
                userCourseEnvironment.getIdentityEnvironment().getIdentity(), this.getReferencedRepositoryEntry(), AssessmentInstance.QMD_ENTRY_TYPE_ASSESS, ureq,
                wControl);
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
            // add default module configuration
            config.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
            config.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
            config.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
            config.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT);
            config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, new Boolean(true));
        } else {
            int version = config.getConfigurationVersion();
            if (version < CURRENT_CONFIG_VERSION) {
                // Loaded config is older than current config version => migrate
                if (version == 1) {
                    // migrate V1 => V2, new parameter 'enableScoreInfo'
                    version = 2;
                    config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, new Boolean(true));
                }
                config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
            }
        }
    }

}
