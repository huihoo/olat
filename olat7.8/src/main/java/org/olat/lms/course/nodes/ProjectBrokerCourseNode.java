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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.course.nodes.projectbroker.ProjectBroker;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionExpression;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerExportGenerator;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManager;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.ta.Returnbox_EBL;
import org.olat.lms.course.nodes.ta.Task_EBL;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.properties.PersistingCoursePropertyManager;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.ms.MSEditFormController;
import org.olat.presentation.course.nodes.projectbroker.ProjectBrokerControllerFactory;
import org.olat.presentation.course.nodes.projectbroker.ProjectBrokerCourseEditorController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Christian Guretzki
 */

public class ProjectBrokerCourseNode extends GenericCourseNode implements AssessableCourseNode, UsedByXstream {

    private static final Logger log = LoggerHelper.getLogger();
    private transient static final String TYPE = "projectbroker";

    // NLS support:
    private transient static final String NLS_GUESTNOACCESS_TITLE = "guestnoaccess.title";
    private transient static final String NLS_GUESTNOACCESS_MESSAGE = "guestnoaccess.message";
    private transient static final String NLS_ERROR_MISSINGSCORECONFIG_SHORT = "error.missingscoreconfig.short";
    private transient static final String NLS_WARN_NODEDELETE = "warn.nodedelete";

    // MUST BE NON TRANSIENT
    private static final int CURRENT_CONFIG_VERSION = 2;

    /** CONF_DROPBOX_ENABLED configuration parameter key. */
    public transient static final String CONF_DROPBOX_ENABLED = "dropbox_enabled";
    /**
     * owners&tutors get notified (per email) at assignment delivery (student uploads file in drop-box)
     */
    public static final String CONF_DROPBOX_CONFIRMATION_REQUESTED = "dropbox_email";
    /** CONF_SCORING_ENABLED configuration parameter key. */
    public transient static final String CONF_SCORING_ENABLED = "scoring_enabled";

    /** ACCESS_SCORING configuration parameter key. */
    public transient static final String ACCESS_SCORING = "scoring";
    /** ACCESS_DROPBOX configuration parameter key. */
    public transient static final String ACCESS_DROPBOX = "dropbox";
    public transient static final String ACCESS_RETURNBOX = "returnbox";
    public transient static final String ACCESS_PROJECTBROKER = "projectbroker";

    /** CONF_TASK_PREVIEW configuration parameter key used for task-form. */
    public transient static final String CONF_TASK_PREVIEW = "task_preview";

    public transient static final String CONF_RETURNBOX_ENABLED = "returnbox_enabled";

    public transient static final String CONF_ACCOUNTMANAGER_GROUP_KEY = "config_accountmanager_group_id";

    public transient static final String CONF_PROJECTBROKER_KEY = "conf_projectbroker_id";

    // MUST BE NON TRANSIENT
    private Condition conditionDrop, conditionScoring, conditionReturnbox;
    private Condition conditionProjectBroker;

    private transient CourseGroupManager groupMgr;

    /**
     * Default constructor.
     */
    public ProjectBrokerCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        final ProjectBrokerCourseEditorController childTabCntrllr = ProjectBrokerControllerFactory.createCourseEditController(ureq, wControl, course, euce, this);
        final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        groupMgr = course.getCourseEnvironment().getCourseGroupManager();
        final NodeEditController editController = new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, groupMgr, euce, childTabCntrllr);
        editController.addControllerListener(childTabCntrllr);
        return editController;
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final String nodecmd) {
        updateModuleConfigDefaults(false);
        Controller controller;
        // Do not allow guests to access tasks
        final Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
            final String title = trans.translate(NLS_GUESTNOACCESS_TITLE);
            final String message = trans.translate(NLS_GUESTNOACCESS_MESSAGE);
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            // Add message id to business path if nodemcd is available
            if (nodecmd != null) {
                try {
                    final Long projectId = Long.valueOf(nodecmd);
                    final BusinessControlFactory bcf = BusinessControlFactory.getInstance();
                    final BusinessControl businessControl = bcf.createFromString("[Project:" + projectId + "]");
                    wControl = bcf.createBusinessWindowControl(businessControl, wControl);
                } catch (final NumberFormatException e) {
                    // ups, nodecmd is not a message, what the heck is it then?
                    log.warn("Could not create message ID from given nodemcd::" + nodecmd, e);
                }
            }
            controller = ProjectBrokerControllerFactory.createRunController(ureq, wControl, userCourseEnv, ne);
        }
        final Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_projectbroker_icon");
        return new NodeRunConstructionResult(wrapperCtrl);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        return ProjectBrokerControllerFactory.createPreviewController(ureq, wControl, userCourseEnv, ne);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        if (ne.isAtLeastOneAccessible()) {
            final Controller peekViewController = ProjectBrokerControllerFactory.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
            return peekViewController;
        } else {
            // use standard peekview
            return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
        }
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

        boolean isValid = true;
        final Boolean hasScoring = (Boolean) getModuleConfiguration().get(CONF_SCORING_ENABLED);
        if (hasScoring.booleanValue()) {
            if (!MSEditFormController.isConfigValid(getModuleConfiguration())) {
                isValid = false;
            }
        }
        StatusDescription sd = StatusDescription.NOERROR;
        if (!isValid) {
            // FIXME: refine statusdescriptions by moving the statusdescription
            final String shortKey = NLS_ERROR_MISSINGSCORECONFIG_SHORT;
            final String longKey = NLS_ERROR_MISSINGSCORECONFIG_SHORT;
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(MSEditFormController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            // TODO:cg 28.01.2010 no assessment-tool in V1.0
            // sd.setActivateableViewIdentifier(ProjectBrokerCourseEditorController.PANE_TAB_CONF_SCORING);
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
        final String translatorStr = PackageUtil.getPackageName(ProjectBrokerCourseEditorController.class);
        // check if group-manager is already initialized
        if (groupMgr == null) {
            groupMgr = cev.getCourseGroupManager();
        }
        final List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    @Override
    protected void calcAccessAndVisibility(final ConditionInterpreter ci, final NodeEvaluation nodeEval) {
        if (ci == null) {
            throw new OLATRuntimeException("no condition interpreter <" + getIdent() + " " + getShortName() + ">", new IllegalArgumentException());
        }
        if (nodeEval == null) {
            throw new OLATRuntimeException("node Evaluationt is null!! for <" + getIdent() + " " + getShortName() + ">", new IllegalArgumentException());
        }
        // evaluate the preconditions
        final boolean projectBrokerAccess = (getConditionProjectBroker().getConditionExpression() == null ? true : ci.evaluateCondition(conditionProjectBroker));
        nodeEval.putAccessStatus(ACCESS_PROJECTBROKER, projectBrokerAccess);
        // add a dummy access-status to open course node in general otherwise the hole project-broker could be closed
        // TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
        // boolean scoring = (getConditionScoring().getConditionExpression() == null ? true : ci.evaluateCondition(conditionScoring));
        // nodeEval.putAccessStatus(ACCESS_SCORING, scoring);

        final boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionVisibility()));
        nodeEval.setVisible(visible);
    }

    /**
	 */
    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_PROJECTBROKER_, locale);
        final CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
        final List list = cpm.listCourseNodeProperties(this, null, null, null);
        if (list.size() != 0) {
            return trans.translate(NLS_WARN_NODEDELETE); // properties exist
        }
        ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        final File fDropboxFolder = new File(FolderConfig.getCanonicalRoot() + projectBrokerManager.getDropboxRootFolder(course.getCourseEnvironment(), this));
        if (fDropboxFolder.exists() && fDropboxFolder.list().length > 0) {
            return trans.translate(NLS_WARN_NODEDELETE); // Dropbox folder contains files
        }
        Returnbox_EBL returnboxEbl = CoreSpringFactory.getBean(Returnbox_EBL.class);
        final File fReturnboxFolder = new File(FolderConfig.getCanonicalRoot() + returnboxEbl.getReturnboxRootFolder(course.getCourseEnvironment(), this));
        if (fReturnboxFolder.exists() && fReturnboxFolder.list().length > 0) {
            return trans.translate(NLS_WARN_NODEDELETE); // Returnbox folder contains files
        }

        return null; // no data yet.
    }

    /**
	 */
    @Override
    public void cleanupOnDelete(final ICourse course) {
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final Long projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, this);
        ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        final File fDropBox = new File(FolderConfig.getCanonicalRoot() + projectBrokerManager.getDropboxRootFolder(course.getCourseEnvironment(), this));
        if (fDropBox.exists()) {
            FileUtils.deleteDirsAndFiles(fDropBox, true, true);
        }
        Returnbox_EBL returnboxEbl = CoreSpringFactory.getBean(Returnbox_EBL.class);
        final File fReturnBox = new File(FolderConfig.getCanonicalRoot() + returnboxEbl.getReturnboxRootFolder(course.getCourseEnvironment(), this));
        if (fReturnBox.exists()) {
            FileUtils.deleteDirsAndFiles(fReturnBox, true, true);
        }
        final File attachmentDir = new File(FolderConfig.getCanonicalRoot()
                + ProjectBrokerManagerFactory.getProjectBrokerManager().getAttachmentBasePathRelToFolderRoot(course.getCourseEnvironment(), this));
        if (attachmentDir.exists()) {
            FileUtils.deleteDirsAndFiles(attachmentDir, true, true);
        }
        // Delete project-broker, projects and project-groups
        if (projectBrokerId != null) {
            ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProjectBroker(projectBrokerId, course.getCourseEnvironment(), this);
        }
        // Delete all properties...
        cpm.deleteNodeProperties(this, null);
    }

    /**
     * @return dropbox condition
     */
    public Condition getConditionDrop() {
        if (conditionDrop == null) {
            conditionDrop = new Condition();
        }
        conditionDrop.setConditionId("drop");
        return conditionDrop;
    }

    /**
     * @return scoring condition
     */
    public Condition getConditionScoring() {
        if (conditionScoring == null) {
            conditionScoring = new Condition();
        }
        conditionScoring.setConditionId("scoring");
        return conditionScoring;
    }

    /**
     * @return Returnbox condition
     */
    public Condition getConditionReturnbox() {
        if (conditionReturnbox == null) {
            conditionReturnbox = new Condition();
        }
        conditionReturnbox.setConditionId("returnbox");
        return conditionReturnbox;
    }

    /**
     * @param conditionDrop
     */
    public void setConditionDrop(Condition conditionDrop) {
        if (conditionDrop == null) {
            conditionDrop = getConditionDrop();
        }
        conditionDrop.setConditionId("drop");
        this.conditionDrop = conditionDrop;
    }

    /**
     * @param conditionScoring
     */
    public void setConditionScoring(Condition conditionScoring) {
        if (conditionScoring == null) {
            conditionScoring = getConditionScoring();
        }
        conditionScoring.setConditionId("scoring");
        this.conditionScoring = conditionScoring;
    }

    /**
     * @param condition
     */
    public void setConditionReturnbox(Condition condition) {
        if (condition == null) {
            condition = getConditionReturnbox();
        }
        condition.setConditionId("returnbox");
        this.conditionReturnbox = condition;
    }

    public Condition getConditionProjectBroker() {
        if (conditionProjectBroker == null) {
            conditionProjectBroker = new Condition();
        }
        conditionProjectBroker.setConditionId("projectbroker");
        return conditionProjectBroker;
    }

    public void setConditionProjectBroker(Condition condition) {
        if (condition == null) {
            condition = getConditionProjectBroker();
        }
        condition.setConditionId("projectbroker");
        this.conditionProjectBroker = condition;
    }

    // //////////// assessable interface implementation

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
    public boolean hasCommentConfigured() {
        return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
        // ModuleConfiguration config = getModuleConfiguration();
        // Boolean comment = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
        // if (comment == null) return false;
        // return comment.booleanValue();
    }

    /**
	 */
    @Override
    public boolean hasPassedConfigured() {
        return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
        // ModuleConfiguration config = getModuleConfiguration();
        // Boolean passed = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
        // if (passed == null) return false;
        // return passed.booleanValue();
    }

    /**
	 */
    @Override
    public boolean hasScoreConfigured() {
        return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
        // ModuleConfiguration config = getModuleConfiguration();
        // Boolean score = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
        // if (score == null) return false;
        // return score.booleanValue();
    }

    /**
	 */
    @Override
    public boolean hasStatusConfigured() {
        return false; // Project broker Course node has no status-field
    }

    /**
	 */
    @Override
    public Float getMaxScoreConfiguration() {
        if (!hasScoreConfigured()) {
            throw new OLATRuntimeException(ProjectBrokerCourseNode.class, "getMaxScore not defined when hasScore set to false", null);
        }
        final ModuleConfiguration config = getModuleConfiguration();
        final Float max = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
        return max;
    }

    /**
	 */
    @Override
    public Float getMinScoreConfiguration() {
        if (!hasScoreConfigured()) {
            throw new OLATRuntimeException(ProjectBrokerCourseNode.class, "getMinScore not defined when hasScore set to false", null);
        }
        final ModuleConfiguration config = getModuleConfiguration();
        final Float min = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
        return min;
    }

    /**
	 */
    @Override
    public Float getCutValueConfiguration() {
        if (!hasPassedConfigured()) {
            throw new OLATRuntimeException(ProjectBrokerCourseNode.class, "getCutValue not defined when hasPassed set to false", null);
        }
        final ModuleConfiguration config = getModuleConfiguration();
        final Float cut = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
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
        // always true when assessable
        return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
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
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final Integer userAttemptsValue = am.getNodeAttempts(this, mySelf);
        return userAttemptsValue;

    }

    /**
	 */
    @Override
    public boolean hasAttemptsConfigured() {
        return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
        // return true;
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
        // prepare file component
        throw new AssertException("ProjectBroker does not support AssessmentTool");
    }

    /**
	 */
    @Override
    public String getDetailsListView(final UserCourseEnvironment userCourseEnvironment) {
        final Identity identity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        final CoursePropertyManager propMgr = userCourseEnvironment.getCourseEnvironment().getCoursePropertyManager();
        final List samples = propMgr.findCourseNodeProperties(this, identity, null, Task_EBL.PROP_ASSIGNED);
        if (samples.size() == 0) {
            return null; // no sample assigned yet
        }
        return ((PropertyImpl) samples.get(0)).getStringValue();
    }

    /**
	 */
    @Override
    public String getDetailsListViewHeaderKey() {
        return "table.header.details.ta";
    }

    /**
	 */
    @Override
    public boolean hasDetails() {
        Boolean hasDropbox = (Boolean) getModuleConfiguration().get(CONF_DROPBOX_ENABLED);
        if (hasDropbox == null) {
            hasDropbox = Boolean.FALSE;
        }
        return hasDropbox.booleanValue();
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl) {
        final ProjectBroker projectBroker = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        ProjectBrokerManagerFactory.getProjectBrokerManager().saveProjectBrokerId(projectBroker.getKey(), cpm, this);
        return null;
    }

    /**
     * archives the dropbox of this task course node to the user's personal folder under private/archive/[coursename]/dropboxes/[nodeIdent].zip
     * 
     * @param locale
     * @param course
     * @param fArchiveDirectory
     * @param charset
     */
    @Override
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File fArchiveDirectory, final String charset) {
        boolean dataFound = false;
        ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        final String dropboxPath = FolderConfig.getCanonicalRoot() + projectBrokerManager.getDropboxRootFolder(course.getCourseEnvironment(), this);
        final File dropboxDir = new File(dropboxPath);
        Returnbox_EBL returnboxEbl = CoreSpringFactory.getBean(Returnbox_EBL.class);
        final String returnboxPath = FolderConfig.getCanonicalRoot() + returnboxEbl.getReturnboxRootFolder(course.getCourseEnvironment(), this);
        final File returnboxDir = new File(returnboxPath);

        if (dropboxDir.exists() || returnboxDir.exists()) {
            // Create Temp Dir for zipping
            final String tmpDirPath = FolderConfig.getCanonicalTmpDir() + course.getCourseEnvironment().getCourseBaseContainer().getRelPath();
            final File tmpDir = new File(tmpDirPath);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }

            // prepare writing course results overview table
            // TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
            //
            // List users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment());
            // List nodes = ScoreAccountingHelper.loadAssessableNodes(course.getCourseEnvironment());
            // String s = ScoreAccountingHelper.createCourseResultsOverviewTable(users, nodes, course, locale);
            //
            // String courseTitle = course.getCourseTitle();
            // String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle + "-score", "xls");
            //
            // // write course results overview table to filesystem
            // ExportUtil.writeContentToFile(fileName, s, tmpDir, charset);

            final String projectBrokerTableExport = ProjectBrokerExportGenerator.createCourseResultsOverviewTable(this, course, locale);
            final String tableExportFileName = ExportUtil.createFileNameWithTimeStamp(this.getShortTitle() + "-projectbroker_overview", "xls");
            ExportUtil.writeContentToFile(tableExportFileName, projectBrokerTableExport, tmpDir, charset);

            // prepare zipping the node directory and the course results overview table
            final Set fileList = new HashSet();
            // move xls file to tmp dir
            // TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
            // fileList.add(fileName);
            fileList.add(tableExportFileName);

            // copy dropboxes to tmp dir
            if (dropboxDir.exists()) {
                // OLAT-6426 archive only dropboxes of users that handed in at least one file -> prevent empty folders in archive
                boolean validDropboxesfound = false;
                File[] themaFolderArray = dropboxDir.listFiles();
                for (File themaFolder : themaFolderArray) {
                    File[] userFolderArray = themaFolder.listFiles();
                    if (userFolderArray == null)
                        continue;
                    for (File userFolder : userFolderArray) {
                        if (FileUtils.isDirectoryAndNotEmpty(userFolder)) {
                            validDropboxesfound = true;
                            File source = new File(dropboxDir + "/" + themaFolder.getName() + "/" + userFolder.getName());
                            File target = new File(tmpDirPath + "/dropboxes/" + themaFolder.getName() + "/" + userFolder.getName());
                            FileUtils.copyDirContentsToDir(source, target, false, "archive projectbroker dropboxes ");
                        }
                    }
                }

                if (validDropboxesfound) {
                    // dropboxes exists, so there is something to archive
                    dataFound |= true;
                }
            }

            // copy returnboxes to tmp dir
            if (returnboxDir.exists()) {
                FileUtils.copyDirContentsToDir(returnboxDir, new File(tmpDirPath + "/returnboxes"), false, "archive projectbroker returnboxes");
                fileList.add("returnboxes");
                // returnboxes exists, so there is something to archive
                dataFound |= true;
            }

            if (dataFound) {
                final String zipName = ExportUtil.createFileNameWithTimeStamp(this.getIdent(), "zip");

                final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss_SSS");
                final String exportDirName = "projectbroker_" + this.getShortName() + "_" + formatter.format(new Date(System.currentTimeMillis()));
                final File fDropBoxArchiveDir = new File(fArchiveDirectory, exportDirName);
                if (!fDropBoxArchiveDir.exists()) {
                    fDropBoxArchiveDir.mkdir();
                }
                final File archiveDir = new File(fDropBoxArchiveDir, zipName);

                // zip
                dataFound &= ZipUtil.zip(fileList, tmpDir, archiveDir, true);
                // Delete all temp files
                FileUtils.deleteDirsAndFiles(tmpDir, true, true);
            }
        }
        return dataFound;
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
        //
        final String conditionProjectBroker = getConditionProjectBroker().getConditionExpression();
        if (conditionProjectBroker != null && !conditionProjectBroker.equals("")) {
            // an active condition is defined
            final ConditionExpression ce = new ConditionExpression(getConditionProjectBroker().getConditionId());
            ce.setExpressionString(getConditionProjectBroker().getConditionExpression());
            retVal.add(ce);
        }
        //
        return retVal;
    }

    /**
     * Init config parameter with default values for a new course node.
     */
    @Override
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        final ModuleConfiguration config = getModuleConfiguration();
        if (isNewNode) {
            // use defaults for new course building blocks
            // dropbox defaults
            config.set(CONF_DROPBOX_ENABLED, Boolean.TRUE);
            config.set(CONF_DROPBOX_CONFIRMATION_REQUESTED, Boolean.FALSE);

            // scoring defaults
            config.set(CONF_SCORING_ENABLED, Boolean.FALSE);
            // returnbox defaults
            config.set(CONF_RETURNBOX_ENABLED, Boolean.TRUE);
            // New config parameter version 2
            config.setBooleanEntry(CONF_TASK_PREVIEW, false);
            MSCourseNode.initDefaultConfig(config);
            config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
        } else {
            int version = config.getConfigurationVersion();
            if (version < CURRENT_CONFIG_VERSION) {
                // Loaded config is older than current config version => migrate
                if (version == 1) {
                    // migrate V1 => V2 (remove all condition
                    this.setConditionDrop(null);
                    this.setConditionReturnbox(null);
                    version = 2;
                }
                config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
            }
        }
    }

    /**
	 */
    @Override
    public CourseNode createInstanceForCopy(final boolean isNewTitle) {
        final CourseNode copyInstance = super.createInstanceForCopy(isNewTitle);
        return copyInstance;
    }

}
