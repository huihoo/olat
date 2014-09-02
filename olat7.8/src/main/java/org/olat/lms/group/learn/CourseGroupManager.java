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

package org.olat.lms.group.learn;

import java.io.File;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<BR/>
 * The course group manager provides methods to access groups, areas and contexts. For many functionality the BusinessGroupManager, BGAreaDao or the BGContextManager must
 * be used.
 * <P/>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public interface CourseGroupManager {

    /** default course group contexts prefix used for learning and right groups * */
    static final String DEFAULT_CONTEXT_PREFIX = "default::";
    /** default course group context name for learning groups */
    static final String DEFAULT_NAME_LC_PREFIX = DEFAULT_CONTEXT_PREFIX + "learninggroups::";
    /** default course group context name for right groups */
    static final String DEFAULT_NAME_RC_PREFIX = DEFAULT_CONTEXT_PREFIX + "rightgroups::";

    /**
     * Checks users course rights in any of the available right group context of this course
     * 
     * @param identity
     * @param courseRight
     * @return true if user has course right, false otherwise
     */
    public boolean hasRight(Identity identity, String courseRight, OLATResourceable ores);

    /**
     * Checks users course rights in the specified right group context of this course
     * 
     * @param identity
     * @param courseRight
     * @param groupContextName
     * @return true if user has course right, false otherwhise
     */
    public boolean hasRight(Identity identity, String courseRight, String groupContextName, OLATResourceable ores);

    /**
     * Checks if an identity is in a learning group with the given name in any contexts of this course, either as owner or as participant
     * 
     * @param identity
     * @param groupName
     * @return true if user is in learning group, false otherwhise
     */
    public boolean isIdentityInLearningGroup(Identity identity, String groupName, OLATResourceable ores);

    /**
     * Checks if an identity is in a learning group with the given name in the the given group context of this course, either as owner or as participant
     * 
     * @param identity
     * @param groupName
     * @param groupContextName
     * @return true if user is in learning group, false otherwhise
     */
    public boolean isIdentityInLearningGroup(Identity identity, String groupName, String groupContextName, OLATResourceable ores);

    /**
     * Checks whether a set of learning groups with an identical name are full or not.
     * 
     * @param groupName
     *            the name of groups
     * @return true means all learning groups are full
     */
    public boolean isLearningGroupFull(String groupName, OLATResourceable ores);

    /**
     * Checks if an identity is in a right group with the given name in any contexts of this course
     * 
     * @param identity
     * @param groupName
     * @return true if user is in right group, false otherwhise
     */
    public boolean isIdentityInRightGroup(Identity identity, String groupName, OLATResourceable ores);

    /**
     * Checks if an identity is in a right group with the given name in the the given group context of this course
     * 
     * @param identity
     * @param groupName
     * @param groupContextName
     * @return true if user is in right group, false otherwhise
     */
    public boolean isIdentityInRightGroup(Identity identity, String groupName, String groupContextName, OLATResourceable ores);

    /**
     * Checks if an identity is in any learning areas with the given name in any of the courses group contexts
     * 
     * @param identity
     * @param areaName
     * @return true if user is in such an area, false otherwhise
     */
    public boolean isIdentityInLearningArea(Identity identity, String areaName, OLATResourceable ores);

    /**
     * Checks if an identity is in a learning areas with the given name in the given group context
     * 
     * @param identity
     * @param areaName
     * @param groupContextName
     * @return true if user is in such an area, false otherwhise
     */
    public boolean isIdentityInLearningArea(Identity identity, String areaName, String groupContextName, OLATResourceable ores);

    /**
     * Checks if an identity is in the given group context
     * 
     * @param identity
     * @param groupContextName
     * @return true if user is in this context, false otherwhise
     */
    public boolean isIdentityInGroupContext(Identity identity, String groupContextName, OLATResourceable ores);

    /**
     * Checks if user is coach in any of the courses learning groups
     * 
     * @param identity
     * @return true if user is coach
     */
    public boolean isIdentityCourseCoach(Identity identity, OLATResourceable ores);

    /**
     * Checks if user is course administrator (is owner of repository entry)
     * 
     * @param identity
     * @return boolean
     */
    public boolean isIdentityCourseAdministrator(Identity identity, OLATResourceable ores);

    /**
     * Checks if user is participant in any right group of this course
     * 
     * @param identity
     * @return boolean
     */
    public boolean isIdentityParticipantInAnyRightGroup(Identity identity, OLATResourceable ores);

    /**
     * Checks if user is participant in any learning group of this course
     * 
     * @param identity
     * @return boolean
     */
    public boolean isIdentityParticipantInAnyLearningGroup(Identity identity, OLATResourceable ores);

    /**
     * @return The list of learning group contexts of this course
     */
    public List getLearningGroupContexts(OLATResourceable ores);

    /**
     * @return The list of right group contexts of this course
     */
    public List getRightGroupContexts(OLATResourceable ores);

    /**
     * @return A list of all learning group from all learning group contexts of this course
     */
    public List getAllLearningGroupsFromAllContexts(OLATResourceable ores);

    /**
     * @param groupName
     * @return A list of all learning groups with the given group name from all contexts of this course
     */
    public List getLearningGroupsFromAllContexts(String groupName, OLATResourceable ores);

    /**
     * @param groupName
     * @return
     */
    public List getRightGroupsFromAllContexts(String groupName, OLATResourceable ores);

    /**
     * @param areaName
     * @return A list of all learning groups from all contexts from this course that are in the given group area
     */
    public List getLearningGroupsInAreaFromAllContexts(String areaName, OLATResourceable ores);

    /**
     * @param groupName
     * @return A list of all learning areas where the given group takes part. All course group contexts are considered in this search.
     */
    public List getLearningAreasOfGroupFromAllContexts(String groupName, OLATResourceable ores);

    /**
     * @param identity
     * @param groupName
     * @return A list of all learning groups with the given name where this identity is participant
     */
    public List getParticipatingLearningGroupsFromAllContexts(Identity identity, String groupName, OLATResourceable ores);

    /**
     * @param identity
     * @param araName
     * @return A list of all learning groups within the given group area where this identity is participant
     */
    public List getParticipatingLearningGroupsInAreaFromAllContexts(Identity identity, String araName, OLATResourceable ores);

    /**
     * @param identity
     * @return A list of all learning groups where this identity is owner
     */
    public List getOwnedLearningGroupsFromAllContexts(Identity identity, OLATResourceable ores);

    /**
     * @param identity
     * @return A list of all learning groups where this identity is participant
     */
    public List getParticipatingLearningGroupsFromAllContexts(Identity identity, OLATResourceable ores);

    /**
     * @param identity
     * @return A list of right groups where this identity is participating
     */
    public List getParticipatingRightGroupsFromAllContexts(Identity identity, OLATResourceable ores);

    /**
     * @return A list of all right groups from this course
     */
    public List getAllRightGroupsFromAllContexts(OLATResourceable ores);

    /**
     * @return A list of all group areas from this course
     */
    public List getAllAreasFromAllContexts(OLATResourceable ores);

    /**
     * Deletes the course group management. This will unlink all group contexts from this course. When the unlinked contexts are not used in any other resources then the
     * context itself will be deleted as well. This will delete all areas, groups, group memberships, group folders, forums etc. Use with care!
     */
    public void deleteCourseGroupmanagement(OLATResourceable ores);

    /**
     * Initializes the course groupmanagement. This will create the initial default learning and right group contexts.
     * 
     * @param courseTitle
     */
    public void createCourseGroupmanagement(String courseTitle, OLATResourceable ores);

    /**
     * Initializes the course groupmanagement as a copy of another course. This will copy the initial default learning and right group contexts and add the references to
     * all other group contexts.
     * 
     * @param originalCourseGroupManager
     * @param courseTitle
     */
    public void createCourseGroupmanagementAsCopy(OLATResourceable originalCourseOres, String courseTitle, OLATResourceable copyCourseOres);

    /**
     * Method to count group memberships. Only participants will be counted, no the owners.
     * 
     * @param groups
     *            A list of groups
     * @return A list of Integers that show the number of members in a group for each of the group from the groups list.
     */
    public List getNumberOfMembersFromGroups(List groups);

    /**
     * @return A list with all group names used in all learning group contexts. If a group red is in more than one contexts, red will be only once in the list
     */
    public List getUniqueLearningGroupNamesFromAllContexts(OLATResourceable ores);

    /**
     * @return A list with all area names used in all learning group contexts. If an area red is in more than one contexts, red will be only once in the list
     */
    public List getUniqueAreaNamesFromAllContexts(OLATResourceable ores);

    /**
     * Export all groups which are course internal to a file for later import.
     * 
     * @param fExportDirectory
     */
    public void exportCourseLeaningGroups(File fExportDirectory, OLATResourceable ores);

    /**
     * Import course internal groups from previous export.
     * 
     * @param fImportDirectory
     */
    public void importCourseLearningGroups(File fImportDirectory, OLATResourceable ores);

    /**
     * Export all groups which are course internal to a file for later import.
     * 
     * @param fExportDirectory
     */
    public void exportCourseRightGroups(File fExportDirectory, OLATResourceable ores);

    /**
     * Import course internal groups from previous export.
     * 
     * @param fImportDirectory
     */
    public void importCourseRightGroups(File fImportDirectory, OLATResourceable ores);

    /**
     * List with identities being coaches in learning groups of this course. If the specified name is null, all learning groups are considered.
     * 
     * @param groupName
     * @return a list with all coaches of this course
     */
    public List<Identity> getCoachesFromLearningGroup(String groupName, OLATResourceable ores);

    /**
     * List with identities being coaches in the areas of this course. If the specified name is null, all areas are considered.
     * 
     * @param areaName
     * @return a list with all coaches of this course
     */
    public List getCoachesFromArea(String areaName, OLATResourceable ores);

    /**
     * List with identities being participants in the learning groups of this course. If the specified name is null, all learning groups are considered.
     * 
     * @param groupName
     * @return a list with all participants of this course
     */
    public List<Identity> getParticipantsFromLearningGroup(String groupName, OLATResourceable ores);

    /**
     * List with identities being participants in the areas of this course. If the specified name is null, all areas are considered.
     * 
     * @param areaName
     * @return a list with participants of this course
     */
    public List getParticipantsFromArea(String areaName, OLATResourceable ores);

    /**
     * @param identity
     * @return A list of all waiting-list groups where this identity is in
     */
    public List getWaitingListGroupsFromAllContexts(Identity identity, OLATResourceable ores);

    /**
     * @param exportDirectory
     * @param courseResource
     */
    void archiveCourseGroups(File exportDirectory, OLATResourceable courseResource);

    List<Identity> getCourseTutors(OLATResourceable courseResource);

    List<Identity> getCourseOwnersAndTutors(OLATResourceable courseResource);

    List<Identity> getCourseOwners(OLATResourceable course);

}
