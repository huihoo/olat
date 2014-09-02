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

package org.olat.lms.course.assessment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.util.BulkAction;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * TODO: schneider Class Description for BulkActionSetNodePassed
 * <P>
 * Initial Date: 23.01.2006 <br>
 * 
 * @author Alexander Schneider
 */
public class BulkActionSetNodePassed extends BulkAction {
    private final Translator translator;
    private AssessableCourseNode courseNode;
    private final List allowedIdKeys;
    private final Identity coachIdentity;
    private boolean hasPassed;
    Float cut;
    private final OLATResourceable ores;

    public BulkActionSetNodePassed(final OLATResourceable ores, final List allowedIdKeys, final Identity coachIdentity, final Translator translator) {
        this.translator = translator;
        this.allowedIdKeys = allowedIdKeys;
        this.coachIdentity = coachIdentity;
        this.ores = ores;
    }

    @Override
    public List doAction(final List identitiesAndTheirsNodePassed) {
        if (this.ores == null || this.courseNode == null || this.coachIdentity == null) {
            throw new AssertException("use constructor with course, assessable coursnode and coachidentity");
        }
        final List feedbacks = new ArrayList(identitiesAndTheirsNodePassed.size());
        final ICourse course = CourseFactory.loadCourse(ores);
        for (final Iterator iter = identitiesAndTheirsNodePassed.iterator(); iter.hasNext();) {
            final Object[] identityAndItsNodePassed = (Object[]) iter.next();
            if (identityAndItsNodePassed[0] != null) {

                if (allowedIdKeys.contains(((Identity) identityAndItsNodePassed[0]).getKey())) {
                    final IdentityEnvironment ienv = new IdentityEnvironment();
                    ienv.setIdentity((Identity) identityAndItsNodePassed[0]);
                    final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
                    final String nodePassed = (String) identityAndItsNodePassed[1];
                    if ((nodePassed != null && nodePassed.equalsIgnoreCase("y")) || (nodePassed != null && nodePassed.equalsIgnoreCase("n"))) {
                        if (hasPassed && cut == null) { // Configuration of manual assessment --> Display passed/not passed: yes, Type of display: Manual by tutor
                            final ScoreEvaluation seOld = courseNode.getUserScoreEvaluation(uce);
                            final Float score = seOld.getScore();
                            Boolean passed = Boolean.TRUE;
                            if (nodePassed.equalsIgnoreCase("n")) {
                                passed = Boolean.FALSE;
                            }
                            final ScoreEvaluation seNew = new ScoreEvaluation(score, passed);

                            // Update score,passed properties in db, and the user's efficiency statement
                            final boolean incrementAttempts = false;
                            courseNode.updateUserScoreEvaluation(seNew, uce, coachIdentity, incrementAttempts);

                            // Refresh score view
                            uce.getScoreAccounting().scoreInfoChanged(this.courseNode, seNew);

                            final Object[] feedback = new Object[] { Boolean.TRUE, identityAndItsNodePassed[0], translator.translate("bulk.action.ok") };
                            feedbacks.add(feedback);
                        } else { // Configuration of manual assessment --> Display passed/not passed: no
                            final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsNodePassed[0],
                                    translator.translate("bulk.action.wrong.config.toSetPassed") };
                            feedbacks.add(feedback);
                        }
                    } else { // nodePassed == null
                        final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsNodePassed[0], translator.translate("bulk.action.no.value") };
                        feedbacks.add(feedback);
                    }
                } else { // identity exists, but current user has no rights to assess identityAndItsScore[0]
                    final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsNodePassed[0], translator.translate("bulk.action.not.allowed") };
                    feedbacks.add(feedback);
                }
            } else { // identity == null
                final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsNodePassed[0], translator.translate("bulk.action.no.such.user") };
                feedbacks.add(feedback);
            }
        }
        return feedbacks;
    }

    public void setCourseNode(final AssessableCourseNode courseNode) {
        this.courseNode = courseNode;
        this.hasPassed = courseNode.hasPassedConfigured();
        if (hasPassed) {
            this.cut = courseNode.getCutValueConfiguration();
        }
    }
}
