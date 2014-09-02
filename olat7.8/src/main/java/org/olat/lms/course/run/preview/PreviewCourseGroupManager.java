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

import java.io.File;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final public class PreviewCourseGroupManager extends BasicManager implements CourseGroupManager {

    private final List groups;
    private final List areas;
    private final boolean isCoach, isCourseAdmin;

    /**
     * @param groups
     * @param areas
     * @param isCoach
     * @param isCourseAdmin
     */
    public PreviewCourseGroupManager(final List groups, final List areas, final boolean isCoach, final boolean isCourseAdmin) {
        this.groups = groups;
        this.areas = areas;
        this.isCourseAdmin = isCourseAdmin;
        this.isCoach = isCoach;
    }

    /**
	 */
    @Override
    public boolean hasRight(final Identity identity, final String courseRight, OLATResourceable ores) {
        if (courseRight.equals(CourseRights.RIGHT_COURSEEDITOR)) {
            return false;
        }
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public boolean hasRight(final Identity identity, final String courseRight, final String groupContextName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningGroup(final Identity identity, final String groupName, OLATResourceable ores) {
        return groups.contains(groupName);
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningGroup(final Identity identity, final String groupName, final String groupContextName, OLATResourceable ores) {
        return groups.contains(groupName);
    }

    /**
	 */
    @Override
    public boolean isLearningGroupFull(final String groupName, OLATResourceable ores) {
        return groups.contains(groupName);
    }

    /**
	 */
    @Override
    public boolean isIdentityInRightGroup(final Identity identity, final String groupName, OLATResourceable ores) {
        return groups.contains(groupName);
    }

    /**
	 */
    @Override
    public boolean isIdentityInRightGroup(final Identity identity, final String groupName, final String groupContextName, OLATResourceable ores) {
        return groups.contains(groupName);
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningArea(final Identity identity, final String areaName, OLATResourceable ores) {
        return areas.contains(areaName);
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningArea(final Identity identity, final String areaName, final String groupContextName, OLATResourceable ores) {
        return areas.contains(areaName);
    }

    /**
	 */
    @Override
    public boolean isIdentityInGroupContext(final Identity identity, final String groupContextName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public boolean isIdentityCourseCoach(final Identity identity, OLATResourceable ores) {
        return isCoach;
    }

    /**
	 */
    @Override
    public boolean isIdentityCourseAdministrator(final Identity identity, OLATResourceable ores) {
        return isCourseAdmin;
    }

    /**
	 */
    @Override
    public boolean isIdentityParticipantInAnyRightGroup(final Identity identity, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public boolean isIdentityParticipantInAnyLearningGroup(final Identity identity, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getLearningGroupContexts(OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getRightGroupContexts(OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getAllLearningGroupsFromAllContexts(OLATResourceable ores) {
        return groups;
    }

    /**
	 */
    @Override
    public List getLearningGroupsFromAllContexts(final String groupName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getLearningGroupsInAreaFromAllContexts(final String areaName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getParticipatingLearningGroupsFromAllContexts(final Identity identity, final String groupName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getParticipatingLearningGroupsInAreaFromAllContexts(final Identity identity, final String araName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getOwnedLearningGroupsFromAllContexts(final Identity identity, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getParticipatingLearningGroupsFromAllContexts(final Identity identity, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getParticipatingRightGroupsFromAllContexts(final Identity identity, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getAllRightGroupsFromAllContexts(OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getAllAreasFromAllContexts(OLATResourceable ores) {
        return areas;
    }

    /**
	 */
    @Override
    public void deleteCourseGroupmanagement(OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public void createCourseGroupmanagement(final String courseTitle, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public void createCourseGroupmanagementAsCopy(final OLATResourceable originalCourseOres, final String courseTitle, final OLATResourceable copyCourseOres) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getNumberOfMembersFromGroups(final List groupList) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getUniqueLearningGroupNamesFromAllContexts(OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getUniqueAreaNamesFromAllContexts(OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public List getLearningAreasOfGroupFromAllContexts(final String groupName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public List<Identity> getCoachesFromLearningGroup(final String groupName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public List getCoachesFromArea(final String areaName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public List getParticipantsFromLearningGroup(final String groupName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public List getParticipantsFromArea(final String areaName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public List getRightGroupsFromAllContexts(final String groupName, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public void exportCourseRightGroups(final File fExportDirectory, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public void importCourseRightGroups(final File fImportDirectory, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public void exportCourseLeaningGroups(final File fExportDirectory, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public void importCourseLearningGroups(final File fImportDirectory, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public List getWaitingListGroupsFromAllContexts(final Identity identity, OLATResourceable ores) {
        throw new AssertException("unsupported");
    }

    @Override
    public void archiveCourseGroups(File exportDirectory, OLATResourceable courseResource) {
        throw new AssertException("unsupported");

    }

    @Override
    public List<Identity> getCourseTutors(OLATResourceable course) {
        throw new AssertException("unsupported");
    }

    @Override
    public List<Identity> getCourseOwnersAndTutors(OLATResourceable course) {
        throw new AssertException("unsupported");
    }

    @Override
    public List<Identity> getCourseOwners(OLATResourceable course) {
        throw new AssertException("unsupported");
    }

}
