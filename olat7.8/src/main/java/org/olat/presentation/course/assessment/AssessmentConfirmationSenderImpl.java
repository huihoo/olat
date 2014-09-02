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
package org.olat.presentation.course.assessment;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessedIdentityWrapper;
import org.olat.lms.course.nodes.AssessableCourseNode;

/**
 * Initial Date: Nov 7, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class AssessmentConfirmationSenderImpl extends AssessmentConfirmationSender {

    private final AssessedIdentityWrapper assessedIdentityWrapper;
    private final Float oldScore;
    private final Boolean oldPassed;
    private final String oldUserComment;

    public AssessmentConfirmationSenderImpl(Identity originator, AssessableCourseNode courseNode, ICourse course, AssessedIdentityWrapper assessedIdentityWrapper) {
        super(originator, courseNode, course);
        this.assessedIdentityWrapper = assessedIdentityWrapper;
        oldScore = courseNode.hasScoreConfigured() ? courseNode.getUserScoreEvaluation(assessedIdentityWrapper.getUserCourseEnvironment()).getScore() : null;
        oldPassed = courseNode.hasPassedConfigured() ? courseNode.getUserScoreEvaluation(assessedIdentityWrapper.getUserCourseEnvironment()).getPassed() : null;
        oldUserComment = courseNode.hasCommentConfigured() ? courseNode.getUserUserComment(assessedIdentityWrapper.getUserCourseEnvironment()) : null;
    }

    @Override
    public void sendAssessmentConfirmation() {
        if (shouldBeConfirmation()) {
            List<Identity> recipients = new ArrayList<Identity>();
            recipients.add(assessedIdentityWrapper.getIdentity());
            sendConfirmation(recipients);
        }
    }

    private boolean shouldBeConfirmation() {
        return isScoreChanged() || isPassedChanged() || isUserCommentChanged();
    }

    private boolean isUserCommentChanged() {
        if (!courseNode.hasCommentConfigured()) {
            return false;
        }
        String userComment = courseNode.getUserUserComment(assessedIdentityWrapper.getUserCourseEnvironment());
        if (oldUserComment == null && userComment == null) {
            return false;
        }
        return (oldUserComment != null && userComment == null) || (oldUserComment == null && userComment != null) || !(oldUserComment.equals(userComment));
    }

    private boolean isPassedChanged() {
        if (!courseNode.hasPassedConfigured()) {
            return false;
        }
        Boolean passed = courseNode.getUserScoreEvaluation(assessedIdentityWrapper.getUserCourseEnvironment()).getPassed();
        if (oldPassed == null && passed == null) {
            return false;
        }
        return (oldPassed != null && passed == null) || (oldPassed == null && passed != null) || !(oldPassed.booleanValue() == passed.booleanValue());
    }

    private boolean isScoreChanged() {
        if (!courseNode.hasScoreConfigured()) {
            return false;
        }
        Float score = courseNode.getUserScoreEvaluation(assessedIdentityWrapper.getUserCourseEnvironment()).getScore();
        if (oldScore == null && score == null) {
            return false;
        }
        return (oldScore != null && score == null) || (oldScore == null && score != null) || !(oldScore.floatValue() == score.floatValue());
    }

}
