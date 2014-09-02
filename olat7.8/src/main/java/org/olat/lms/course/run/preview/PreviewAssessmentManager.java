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

package org.olat.lms.course.run.preview;

import java.util.HashMap;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final public class PreviewAssessmentManager extends BasicManager implements AssessmentManager {
    private final HashMap nodeScores = new HashMap();
    private final HashMap nodePassed = new HashMap();
    private final HashMap nodeAttempts = new HashMap();
    private final HashMap nodeAssessmentID = new HashMap();

    /**
     * java.lang.Float)
     */
    private void saveNodeScore(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Float score) {
        nodeScores.put(courseNode.getIdent(), score);
    }

    /**
     * java.lang.Integer)
     */
    @Override
    public void saveNodeAttempts(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Integer attempts) {
        nodeAttempts.put(courseNode.getIdent(), attempts);
    }

    /**
     * java.lang.String)
     */
    @Override
    public void saveNodeComment(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final String comment) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
	 */
    @Override
    public void saveNodeCoachComment(final CourseNode courseNode, final Identity assessedIdentity, final String comment) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
     * java.lang.Boolean)
     */
    private void saveNodePassed(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final Boolean passed) {
        nodePassed.put(courseNode.getIdent(), passed);
    }

    /**
	 */
    @Override
    public void incrementNodeAttempts(final CourseNode courseNode, final Identity identity, final UserCourseEnvironment userCourseEnvironment) {
        Integer attempts = (Integer) nodeAttempts.get(courseNode.getIdent());
        if (attempts == null) {
            attempts = new Integer(0);
        }
        int iAttempts = attempts.intValue();
        iAttempts++;
        nodeAttempts.put(courseNode.getIdent(), new Integer(iAttempts));
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public void incrementNodeAttemptsInBackground(final CourseNode courseNode, final Identity identity, final UserCourseEnvironment userCourseEnvironment) {
        incrementNodeAttempts(courseNode, identity, userCourseEnvironment);
    }

    /**
	 */
    @Override
    public Float getNodeScore(final CourseNode courseNode, final Identity identity) {
        return (Float) nodeScores.get(courseNode.getIdent());
    }

    /**
	 */
    @Override
    public String getNodeComment(final CourseNode courseNode, final Identity identity) {
        return "This is a preview"; // default comment for preview
    }

    /**
	 */
    @Override
    public String getNodeCoachComment(final CourseNode courseNode, final Identity identity) {
        return "This is a preview"; // default comment for preview
    }

    /**
	 */
    @Override
    public Boolean getNodePassed(final CourseNode courseNode, final Identity identity) {
        return (Boolean) nodePassed.get(courseNode.getIdent());
    }

    /**
	 */
    @Override
    public Integer getNodeAttempts(final CourseNode courseNode, final Identity identity) {
        final Integer attempts = (Integer) nodeAttempts.get(courseNode.getIdent());
        return (attempts == null ? new Integer(0) : attempts);
    }

    /**
	 */
    @Override
    public void registerForAssessmentChangeEvents(final GenericEventListener gel, final Identity identity) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
	 */
    @Override
    public void deregisterFromAssessmentChangeEvents(final GenericEventListener gel) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
	 */
    @Override
    public void preloadCache() {
        throw new AssertException("Not implemented for preview.");
    }

    /**
	 */
    @Override
    public void preloadCache(final Identity identity) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
	 */
    private void saveAssessmentID(final CourseNode courseNode, final Identity assessedIdentity, final Long assessmentID) {
        nodeAssessmentID.put(courseNode.getIdent(), assessmentID);
    }

    /**
     * @param courseNode
     * @param identity
     * @return
     */
    @Override
    public Long getAssessmentID(final CourseNode courseNode, final Identity identity) {
        return (Long) nodeAssessmentID.get(courseNode.getIdent());
    }

    /**
     * org.olat.lms.course.run.scoring.ScoreEvaluation)
     */
    @Override
    public void saveScoreEvaluation(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final ScoreEvaluation scoreEvaluation,
            final UserCourseEnvironment userCourseEnvironment, final boolean incrementUserAttempts) {

        saveNodeScore(courseNode, identity, assessedIdentity, scoreEvaluation.getScore());
        saveNodePassed(courseNode, identity, assessedIdentity, scoreEvaluation.getPassed());
        saveAssessmentID(courseNode, assessedIdentity, scoreEvaluation.getAssessmentID());
        if (incrementUserAttempts) {
            incrementNodeAttempts(courseNode, identity, userCourseEnvironment);
        }
    }

    @Override
    public OLATResourceable createOLATResourceableForLocking(final Identity assessedIdentity) {
        throw new AssertException("Not implemented for preview.");
    }

}
