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

package org.olat.presentation.course.nodes.scorm;

import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.scorm.ScormAPICallback;
import org.olat.lms.scorm.ScormCPManifestTreeModel;
import org.olat.lms.scorm.ScormConstants;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.nodes.ObjectivesHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.scorm.ScormAPIandDisplayController;
import org.olat.system.commons.CodeHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description: <BR/>
 * Run controller for content packaging course nodes
 * <P/>
 * 
 * @author Felix Jost
 */
public class ScormRunController extends BasicController implements ScormAPICallback {

    private static final Logger log = LoggerHelper.getLogger();

    private final ModuleConfiguration config;
    private Panel main;
    private VelocityContainer startPage;

    // private Translator translator;
    private ScormAPIandDisplayController scormDispC;
    private final ScormCourseNode scormNode;

    // for external menu representation
    private ScormCPManifestTreeModel treeModel;
    private ControllerEventListener treeNodeClickListener;
    private final UserCourseEnvironment userCourseEnv;
    private ChooseScormRunModeForm chooseScormRunMode;
    private final boolean isPreview;

    private final Identity identity;
    private boolean isAssessable;

    /**
     * Use this constructor to launch a CP via Repository reference key set in the ModuleConfiguration. On the into page a title and the learning objectives can be
     * placed.
     * 
     * @param config
     * @param ureq
     * @param userCourseEnv
     * @param wControl
     * @param cpNode
     */
    public ScormRunController(final ModuleConfiguration config, final UserRequest ureq, final UserCourseEnvironment userCourseEnv, final WindowControl wControl,
            final ScormCourseNode scormNode, final boolean isPreview) {
        super(ureq, wControl);
        this.isPreview = isPreview;
        this.userCourseEnv = userCourseEnv;
        this.config = config;
        this.scormNode = scormNode;
        this.identity = ureq.getIdentity();

        addLoggingResourceable(LoggingResourceable.wrap(scormNode));
        init(ureq);
    }

    private void init(final UserRequest ureq) {

        startPage = createVelocityContainer("run");
        // show browse mode option only if not assessable, hide it if in "real test mode"
        isAssessable = config.getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE);

        chooseScormRunMode = new ChooseScormRunModeForm(ureq, getWindowControl(), !isAssessable);
        listenTo(chooseScormRunMode);
        startPage.put("chooseScormRunMode", chooseScormRunMode.getInitialComponent());

        main = new Panel("scormrunmain");
        // scorm always has a start page
        doStartPage(ureq);

        putInitialPanel(main);
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
        if (source == scormDispC) { // just pass on the event.
            doStartPage(ureq);
            fireEvent(ureq, event);
        } else if (source == null) { // external source
            if (event instanceof TreeEvent) {
                scormDispC.switchToPage((TreeEvent) event);
            }
        } else if (source == chooseScormRunMode) {
            doLaunch(ureq);
        }
    }

    private void doStartPage(final UserRequest ureq) {

        // push title and learning objectives, only visible on intro page
        startPage.contextPut("menuTitle", scormNode.getShortTitle());
        startPage.contextPut("displayTitle", scormNode.getLongTitle());

        // Adding learning objectives
        final String learningObj = scormNode.getLearningObjectives();
        if (learningObj != null) {
            final Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
            startPage.put("learningObjectives", learningObjectives);
            startPage.contextPut("hasObjectives", Boolean.TRUE);
        } else {
            startPage.contextPut("hasObjectives", Boolean.FALSE);
        }

        if (isAssessable) {
            final ScoreEvaluation scoreEval = scormNode.getUserScoreEvaluation(userCourseEnv);
            final Float score = scoreEval.getScore();
            startPage.contextPut("score", score != null ? AssessmentHelper.getRoundedScore(score) : "0");
            startPage.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
            startPage.contextPut("passed", scoreEval.getPassed());
            startPage.contextPut("comment", scormNode.getUserUserComment(userCourseEnv));
            startPage.contextPut("attempts", scormNode.getUserAttempts(userCourseEnv));
        }
        startPage.contextPut("isassessable", Boolean.valueOf(isAssessable));
        main.setContent(startPage);
    }

    private void doLaunch(final UserRequest ureq) {
        final RepositoryEntry re = scormNode.getReferencedRepositoryEntry();
        if (re == null) {
            throw new AssertException("configurationkey 'CONFIG_KEY_REPOSITORY_SOFTKEY' of BB CP was missing");
        }
        addLoggingResourceable(LoggingResourceable.wrapScormRepositoryEntry(re));

        String courseId;
        final boolean showMenu = config.getBooleanSafe(ScormEditController.CONFIG_SHOWMENU, true);

        if (isPreview) {
            courseId = new Long(CodeHelper.getRAMUniqueID()).toString();
            scormDispC = new ScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, re.getOlatResource(), null, courseId,
                    ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, true, true);
        } else {
            courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().toString();
            if (isAssessable) {
                scormDispC = new ScormAPIandDisplayController(ureq, getWindowControl(), showMenu, this, re.getOlatResource(), null,
                        courseId + "-" + scormNode.getIdent(), ScormConstants.SCORM_MODE_NORMAL, ScormConstants.SCORM_MODE_CREDIT, false, true);
                scormNode.incrementUserAttempts(userCourseEnv);
            } else if (chooseScormRunMode.getSelectedElement().equals(ScormConstants.SCORM_MODE_NORMAL)) {
                scormDispC = new ScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, re.getOlatResource(), null,
                        courseId + "-" + scormNode.getIdent(), ScormConstants.SCORM_MODE_NORMAL, ScormConstants.SCORM_MODE_CREDIT, false, true);
            } else {
                scormDispC = new ScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, re.getOlatResource(), null, courseId,
                        ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, false, true);
            }
        }
        // configure some display options
        final boolean showNavButtons = config.getBooleanSafe(ScormEditController.CONFIG_SHOWNAVBUTTONS, true);
        scormDispC.showNavButtons(showNavButtons);
        final String height = (String) config.get(ScormEditController.CONFIG_HEIGHT);
        if (!height.equals(ScormEditController.CONFIG_HEIGHT_AUTO)) {
            scormDispC.setHeightPX(Integer.parseInt(height));
        }
        final String contentEncoding = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
        if (!contentEncoding.equals(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO)) {
            scormDispC.setContentEncoding(contentEncoding);
        }
        final String jsEncoding = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
        if (!jsEncoding.equals(NodeEditController.CONFIG_JS_ENCODING_AUTO)) {
            scormDispC.setJSEncoding(jsEncoding);
        }

        // the scormDispC activates itself
    }

    @Override
    public void lmsCommit(final String olatSahsId, final Properties scoScores) {
        // only write score info when node is configured to do so
        if (isAssessable) {
            // do a sum-of-scores over all sco scores
            float score = 0f;
            for (final Iterator it_score = scoScores.values().iterator(); it_score.hasNext();) {
                final String aScore = (String) it_score.next();
                final float ascore = Float.parseFloat(aScore);
                score += ascore;
            }
            final float cutval = scormNode.getCutValueConfiguration().floatValue();
            final boolean passed = (score >= cutval);
            final ScoreEvaluation sceval = new ScoreEvaluation(new Float(score), Boolean.valueOf(passed));
            final boolean incrementAttempts = false;
            scormNode.updateUserScoreEvaluation(sceval, userCourseEnv, identity, incrementAttempts);
            userCourseEnv.getScoreAccounting().scoreInfoChanged(scormNode, sceval);

            if (log.isDebugEnabled()) {
                final String msg = "for scorm node:" + scormNode.getIdent() + " (" + scormNode.getShortTitle() + ") a lmsCommit for scoId " + olatSahsId
                        + " occured, total sum = " + score + ", cutvalue =" + cutval + ", passed: " + passed + ", all scores now = " + scoScores.toString();
                log.debug(msg);
            }
        }
    }

    /**
     * @return true if there is a treemodel and an event listener ready to be used in outside this controller
     */
    public boolean isExternalMenuConfigured() {
        return (config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU).booleanValue());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    /**
     * @return the treemodel of the enclosed ScormDisplayController, or null, if no tree should be displayed (configured by author, see
     *         DisplayConfigurationForm.CONFIG_COMPONENT_MENU)
     */
    public ScormCPManifestTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * @return the listener to listen to clicks to the nodes of the treemodel obtained calling getTreeModel()
     */
    public ControllerEventListener getTreeNodeClickListener() {
        return treeNodeClickListener;
    }

}
