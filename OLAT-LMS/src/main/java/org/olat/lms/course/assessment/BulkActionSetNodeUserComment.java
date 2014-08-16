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
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * TODO: schneider Class Description for BulkActionSetNodeUserComment
 * <P>
 * Initial Date: 23.01.2006 <br>
 * 
 * @author Alexander Schneider
 */
public class BulkActionSetNodeUserComment extends BulkAction {
    private final Translator translator;
    private AssessableCourseNode courseNode;
    private final List allowedIdKeys;
    private final Identity coachIdentity;

    private boolean hasUserComment;
    Float min, max, cut;
    private final OLATResourceable ores;

    public BulkActionSetNodeUserComment(final OLATResourceable ores, final List allowedIdKeys, final Identity coachIdentity, final Translator translator) {
        this.ores = ores;
        this.translator = translator;
        this.allowedIdKeys = allowedIdKeys;
        this.coachIdentity = coachIdentity;
    }

    @Override
    public List doAction(final List identitiesAndTheirsUserComments) {
        if (this.ores == null || this.courseNode == null || this.coachIdentity == null) {
            throw new AssertException("use constructor with course, assessable coursnode and coachidentity");
        }
        final List feedbacks = new ArrayList(identitiesAndTheirsUserComments.size());
        final ICourse course = CourseFactory.loadCourse(ores);
        for (final Iterator iter = identitiesAndTheirsUserComments.iterator(); iter.hasNext();) {
            final Object[] identityAndItsUserComment = (Object[]) iter.next();
            if (identityAndItsUserComment[0] != null) {

                if (allowedIdKeys.contains(((Identity) identityAndItsUserComment[0]).getKey())) {
                    final IdentityEnvironment ienv = new IdentityEnvironment();
                    ienv.setIdentity((Identity) identityAndItsUserComment[0]);
                    final UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
                    final String userComment = (String) identityAndItsUserComment[1];
                    if (userComment != null && !userComment.equals("")) {
                        if (hasUserComment) {
                            // Update userComment in db
                            courseNode.updateUserUserComment(userComment, uce, coachIdentity);
                            // LD: why do we have to update the efficiency statement?
                            // EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
                            // esm.updateUserEfficiencyStatement(uce);

                            final Object[] feedback = new Object[] { Boolean.TRUE, identityAndItsUserComment[0], translator.translate("bulk.action.ok") };
                            feedbacks.add(feedback);
                        } else { // Configuration of manual assessment --> Score granted: No
                            final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsUserComment[0],
                                    translator.translate("bulk.action.wrong.config.toSetUserComment") };
                            feedbacks.add(feedback);
                        }
                    } else { // userComment == null
                        final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsUserComment[0], translator.translate("bulk.action.no.value") };
                        feedbacks.add(feedback);
                    }
                } else { // identity exists, but current user has no rights to assess identityAndItsUserComment[0]
                    final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsUserComment[0], translator.translate("bulk.action.not.allowed") };
                    feedbacks.add(feedback);
                }
            } else { // identity == null
                final Object[] feedback = new Object[] { Boolean.FALSE, identityAndItsUserComment[0], translator.translate("bulk.action.no.such.user") };
                feedbacks.add(feedback);
            }
        }
        return feedbacks;
    }

    public void setCourseNode(final AssessableCourseNode courseNode) {
        this.hasUserComment = courseNode.hasCommentConfigured();
        this.courseNode = courseNode;
    }

}
