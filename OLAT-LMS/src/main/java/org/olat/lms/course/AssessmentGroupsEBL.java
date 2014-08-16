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
package org.olat.lms.course;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.assessment.IAssessmentCallback;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * TODO: Class Description for AssessmentGroupsEBL
 * 
 * <P>
 * Initial Date: 06.09.2011 <br>
 * 
 * @author lavinia
 */
public class AssessmentGroupsEBL {

    // List of groups to which the user has access rights in this course
    private final List<BusinessGroup> coachedGroups;

    public List<BusinessGroup> getCoachedGroups() {
        return coachedGroups;
    }

    /**
	 * 
	 */
    public AssessmentGroupsEBL(final Identity identity, final OLATResourceable ores, IAssessmentCallback callback) {
        // Initialize all groups that the user is allowed to coach
        coachedGroups = getAllowedGroupsFromGroupmanagement(identity, ores, callback);
    }

    /**
     * @param assessmentMainController
     *            TODO
     * @return List of all course participants
     */
    public List<Identity> getAllIdentitisFromGroupmanagement() {
        final List<Identity> allUsersList = new ArrayList<Identity>();
        final BaseSecurity secMgr = getBaseSecurity();
        final Iterator<BusinessGroup> iter = coachedGroups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = iter.next();
            final SecurityGroup secGroup = group.getPartipiciantGroup();
            final List<Identity> identities = secMgr.getIdentitiesOfSecurityGroup(secGroup);
            for (final Iterator<Identity> identitiyIter = identities.iterator(); identitiyIter.hasNext();) {
                final Identity identity = identitiyIter.next();
                if (!PersistenceHelper.listContainsObjectByKey(allUsersList, identity)) {
                    // only add if not already in list
                    allUsersList.add(identity);
                }
            }
        }
        return allUsersList;
    }

    BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * @param selectedGroup
     * @return List of participant identities from this group
     */
    public List<Identity> getGroupIdentitiesFromGroupmanagement(final BusinessGroup selectedGroup) {
        final SecurityGroup selectedSecurityGroup = selectedGroup.getPartipiciantGroup();
        return getBaseSecurity().getIdentitiesOfSecurityGroup(selectedSecurityGroup);
    }

    /**
     * @param identity
     * @return List of all course groups if identity is course admin, else groups that are coached by this identity
     */
    private List<BusinessGroup> getAllowedGroupsFromGroupmanagement(final Identity identity, final OLATResourceable ores, IAssessmentCallback callback) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CourseGroupManager gm = course.getCourseEnvironment().getCourseGroupManager();
        if (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments()) {
            return gm.getAllLearningGroupsFromAllContexts(course);
        } else if (callback.mayAssessCoachedUsers()) {
            return gm.getOwnedLearningGroupsFromAllContexts(identity, course);
        } else {
            throw new OLATSecurityException("No rights to assess or even view any groups");
        }
    }

}
