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

package org.olat.presentation.course.nodes.ms;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ObjectivesHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;

/**
 * Initial Date: Jun 16, 2004
 * 
 * @author gnaegi
 */
public class MSCourseNodeRunController extends DefaultController {

    private static final String PACKAGE = PackageUtil.getPackageName(MSCourseNodeRunController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(MSCourseNodeRunController.class);

    private final VelocityContainer myContent;

    /**
     * Constructor for a manual scoring course run controller
     * 
     * @param ureq
     *            The user request
     * @param userCourseEnv
     *            The user course environment
     * @param msCourseNode
     *            The manual scoring course node
     * @param displayNodeInfo
     *            True: the node title and learning objectives will be displayed
     */
    public MSCourseNodeRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final AssessableCourseNode msCourseNode, final boolean displayNodeInfo) {
        super(wControl);
        final PackageTranslator trans = new PackageTranslator(PACKAGE, ureq.getLocale());

        myContent = new VelocityContainer("olatmsrun", VELOCITY_ROOT + "/run.html", trans, this);

        final ModuleConfiguration config = msCourseNode.getModuleConfiguration();
        myContent.contextPut("displayNodeInfo", Boolean.valueOf(displayNodeInfo));
        if (displayNodeInfo) {
            // push title and learning objectives, only visible on intro page
            myContent.contextPut("menuTitle", msCourseNode.getShortTitle());
            myContent.contextPut("displayTitle", msCourseNode.getLongTitle());

            // Adding learning objectives
            final String learningObj = msCourseNode.getLearningObjectives();
            if (learningObj != null) {
                final Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
                myContent.put("learningObjectives", learningObjectives);
                myContent.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator
            }
        }

        // Push variables to velcity page
        exposeConfigToVC(config);
        exposeUserDataToVC(userCourseEnv, msCourseNode);

        setInitialComponent(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    private void exposeConfigToVC(final ModuleConfiguration config) {
        myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
        myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD));
        myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD));
        final String infoTextUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
        myContent.contextPut(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, (infoTextUser == null ? "" : infoTextUser));
        myContent.contextPut(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE));
        myContent.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MIN, config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN));
        myContent.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MAX, config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX));
    }

    private void exposeUserDataToVC(final UserCourseEnvironment userCourseEnv, final AssessableCourseNode courseNode) {
        final ScoreEvaluation scoreEval = courseNode.getUserScoreEvaluation(userCourseEnv);
        myContent.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
        myContent.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
        myContent.contextPut("passed", scoreEval.getPassed());
        myContent.contextPut("comment", courseNode.getUserUserComment(userCourseEnv));
        final UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
        myContent.contextPut("log", am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity()));
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // do nothing here yet
    }
}
