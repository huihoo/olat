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

package org.olat.lms.group;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.BusinessGroupDao;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseModule;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.right.BGRightManager;
import org.olat.lms.group.right.BGRightManagerImpl;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<BR/>
 * Implementation of the CourseGroupManager that persists its data on the database
 * <P/>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
@Component("persistingCourseGroupManager")
public class PersistingCourseGroupManager extends BasicManager implements CourseGroupManager {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String LEARNINGGROUPEXPORT_XML = "learninggroupexport.xml";
    private static final String RIGHTGROUPEXPORT_XML = "rightgroupexport.xml";
    private static final String LEARNINGGROUPARCHIVE_XLS = "learninggroup_archiv.xls";
    private static final String RIGHTGROUPARCHIVE_XLS = "rightgroup_archiv.xls";
    @Autowired
    BGContextDao contextManager;
    @Autowired
    BGAreaDao areaManager;
    @Autowired
    BusinessGroupDao businessGroupManager;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    OLATResourceManager resourceManager;
    @Autowired
    GroupImporterExporter groupImportExport;
    @Autowired
    BusinessGroupContextService bgContextService;
    @Autowired
    RepositoryService repositoryService;

    /**
	 * 
	 */
    protected PersistingCourseGroupManager() {
        //
    }

    /**
	 */
    @Override
    public boolean hasRight(final Identity identity, final String courseRight, OLATResourceable courseResource) {
        return hasRight(identity, courseRight, null, courseResource);
    }

    /**
	 */
    @Override
    public boolean hasRight(final Identity identity, final String courseRight, final String groupContextName, OLATResourceable courseResource) {
        final BGRightManager rightManager = BGRightManagerImpl.getInstance();
        final Iterator iter = getRightGroupContexts(courseResource).iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            if (groupContextName == null || context.getName().equals(groupContextName)) {
                final boolean hasRight = rightManager.hasBGRight(courseRight, identity, context);
                if (hasRight) {
                    return true; // finished
                }
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningGroup(final Identity identity, final String groupName, OLATResourceable courseResource) {
        return isIdentityInGroup(identity, groupName, null, courseResource, BusinessGroup.TYPE_LEARNINGROUP);
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningGroup(final Identity identity, final String groupName, final String groupContextName, OLATResourceable courseResource) {
        return isIdentityInGroup(identity, groupName, groupContextName, courseResource, BusinessGroup.TYPE_LEARNINGROUP);
    }

    /**
	 */
    @Override
    public boolean isLearningGroupFull(final String groupName, OLATResourceable courseResource) {

        final List<BusinessGroup> groups = getLearningGroupsFromAllContexts(groupName, courseResource);

        if (groups == null) {
            log.warn("no groups available");
            return false;
        } else {
            boolean isLearningGroupFull = false;
            for (final BusinessGroup businessGroup : groups) {
                // if group null
                if (businessGroup == null) {
                    log.warn("group is null");
                    return false;
                }
                // has group participants
                final List<Identity> members = baseSecurity.getIdentitiesOfSecurityGroup(businessGroup.getPartipiciantGroup());
                if (members == null) {
                    log.warn("group members are null");
                    return false;
                }
                // has group no maximum of participants
                if (businessGroup.getMaxParticipants() == null) {
                    log.warn("group.getMaxParticipants() is null");
                    return false;
                }
                // is the set of members greater equals than the maximum of participants
                if (members.size() >= businessGroup.getMaxParticipants().intValue()) {
                    isLearningGroupFull = true;
                } else {
                    return false;
                }
            }
            return isLearningGroupFull;
        }
    }

    /**
	 */
    @Override
    public boolean isIdentityInRightGroup(final Identity identity, final String groupName, OLATResourceable courseResource) {
        return isIdentityInGroup(identity, groupName, null, courseResource, BusinessGroup.TYPE_RIGHTGROUP);
    }

    /**
	 */
    @Override
    public boolean isIdentityInRightGroup(final Identity identity, final String groupName, final String groupContextName, OLATResourceable courseResource) {
        return isIdentityInGroup(identity, groupName, groupContextName, courseResource, BusinessGroup.TYPE_RIGHTGROUP);
    }

    /**
     * Internal method to check if an identity is in a group
     * 
     * @param identity
     * @param groupName
     *            the group name. must not be null
     * @param groupContextName
     *            context name to restrict to a certain context or null if in any context
     * @param contextList
     *            list of contexts that should be searched
     * @return true if in group, false otherwhise
     */
    private boolean isIdentityInGroup(final Identity identity, final String groupName, final String groupContextName, OLATResourceable courseResource, String groupType) {
        List<BGContext> contexts = Collections.emptyList();

        if (BusinessGroup.TYPE_LEARNINGROUP.equals(groupType)) {
            contexts = getLearningGroupContexts(courseResource);
        } else if (BusinessGroup.TYPE_RIGHTGROUP.equals(groupType)) {
            contexts = getRightGroupContexts(courseResource);
        }

        for (BGContext context : contexts) {
            if (groupContextName == null || context.getName().equals(groupContextName)) {
                final boolean inGroup = businessGroupManager.isIdentityInBusinessGroup(identity, groupName, context);
                if (inGroup) {
                    return true; // finished
                }
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningArea(final Identity identity, final String areaName, OLATResourceable courseResource) {
        return isIdentityInLearningArea(identity, areaName, null, courseResource);
    }

    /**
	 */
    @Override
    public boolean isIdentityInLearningArea(final Identity identity, final String areaName, final String groupContextName, OLATResourceable courseResource) {
        final Iterator iter = getLearningGroupContexts(courseResource).iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            if (groupContextName == null || context.getName().equals(groupContextName)) {
                final boolean inArea = areaManager.isIdentityInBGArea(identity, areaName, context);
                if (inArea) {
                    return true; // finished
                }
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean isIdentityInGroupContext(final Identity identity, final String groupContextName, OLATResourceable courseResource) {
        Iterator iter = getLearningGroupContexts(courseResource).iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            if (groupContextName == null || context.getName().equals(groupContextName)) {
                final boolean inContext = contextManager.isIdentityInBGContext(identity, context, true, true);
                if (inContext) {
                    return true; // finished
                }
            }
        }
        iter = getRightGroupContexts(courseResource).iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            if (groupContextName == null || context.getName().equals(groupContextName)) {
                final boolean inContext = contextManager.isIdentityInBGContext(identity, context, true, true);
                if (inContext) {
                    return true; // finished
                }
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public List getLearningGroupContexts(OLATResourceable ores) {
        OLATResource courseResource = resourceManager.findResourceable(ores);
        return contextManager.findBGContextsForResource(courseResource, BusinessGroup.TYPE_LEARNINGROUP, true, true);
    }

    /**
	 */
    @Override
    public List getRightGroupContexts(OLATResourceable ores) {
        OLATResource courseResource = resourceManager.findResourceable(ores);
        return contextManager.findBGContextsForResource(courseResource, BusinessGroup.TYPE_RIGHTGROUP, true, true);
    }

    /**
	 */
    @Override
    public List getRightGroupsFromAllContexts(final String groupName, OLATResourceable courseResource) {
        final List groups = new ArrayList();
        final Iterator iterator = getRightGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final BusinessGroup group = contextManager.findGroupOfBGContext(groupName, bgContext);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
	 */
    @Override
    public List<BusinessGroup> getAllLearningGroupsFromAllContexts(OLATResourceable courseResource) {
        final List<BusinessGroup> allGroups = new ArrayList<BusinessGroup>();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List<BusinessGroup> contextGroups = contextManager.getGroupsOfBGContext(bgContext);
            allGroups.addAll(contextGroups);
        }
        return allGroups;
    }

    /**
	 */
    @Override
    public List<BusinessGroup> getLearningGroupsFromAllContexts(final String groupName, OLATResourceable courseResource) {
        final List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final BusinessGroup group = contextManager.findGroupOfBGContext(groupName, bgContext);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
	 */
    @Override
    public List getAllAreasFromAllContexts(OLATResourceable courseResource) {
        final List allAreas = new ArrayList();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List contextAreas = areaManager.findBGAreasOfBGContext(bgContext);
            allAreas.addAll(contextAreas);
        }
        return allAreas;

    }

    /**
	 */
    @Override
    public List getLearningGroupsInAreaFromAllContexts(final String areaName, OLATResourceable courseResource) {
        final List groups = new ArrayList();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final BGArea area = areaManager.findBGArea(areaName, bgContext);
            if (area != null) {
                final List areaGroups = areaManager.findBusinessGroupsOfArea(area);
                groups.addAll(areaGroups);
            }
        }
        return groups;
    }

    /**
	 */
    @Override
    public List getLearningAreasOfGroupFromAllContexts(final String groupName, OLATResourceable courseResource) {
        final List areas = new ArrayList();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final BusinessGroup group = contextManager.findGroupOfBGContext(groupName, bgContext);
            if (group != null) {
                final List groupAreas = areaManager.findBGAreasOfBusinessGroup(group);
                areas.addAll(groupAreas);
            }
        }
        return areas;
    }

    /**
	 */
    @Override
    public List getParticipatingLearningGroupsFromAllContexts(final Identity identity, final String groupName, OLATResourceable courseResource) {
        final List groups = new ArrayList();
        final Iterator iter = getLearningGroupContexts(courseResource).iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            final BusinessGroup group = contextManager.findGroupAttendedBy(identity, groupName, context);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
	 */
    @Override
    public List getParticipatingLearningGroupsInAreaFromAllContexts(final Identity identity, final String areaName, OLATResourceable courseResource) {
        final List groups = new ArrayList();
        final Iterator iter = getLearningGroupContexts(courseResource).iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            final List contextGroups = areaManager.findBusinessGroupsOfAreaAttendedBy(identity, areaName, context);
            groups.addAll(contextGroups);
        }
        return groups;
    }

    /**
	 */
    @Override
    public List getAllRightGroupsFromAllContexts(OLATResourceable courseResource) {
        final List allGroups = new ArrayList();
        final Iterator iterator = getRightGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List contextGroups = contextManager.getGroupsOfBGContext(bgContext);
            allGroups.addAll(contextGroups);
        }
        return allGroups;
    }

    /**
	 */
    @Override
    public List getOwnedLearningGroupsFromAllContexts(final Identity identity, OLATResourceable courseResource) {
        final List allGroups = new ArrayList();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List contextGroups = businessGroupManager.findBusinessGroupsOwnedBy(bgContext.getGroupType(), identity, bgContext);
            allGroups.addAll(contextGroups);
        }
        return allGroups;
    }

    /**
	 */
    @Override
    public List getParticipatingLearningGroupsFromAllContexts(final Identity identity, OLATResourceable courseResource) {
        final List allGroups = new ArrayList();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List contextGroups = businessGroupManager.findBusinessGroupsAttendedBy(bgContext.getGroupType(), identity, bgContext);
            allGroups.addAll(contextGroups);
        }
        return allGroups;
    }

    /**
	 */
    @Override
    public List getParticipatingRightGroupsFromAllContexts(final Identity identity, OLATResourceable courseResource) {
        final List allGroups = new ArrayList();
        final Iterator iterator = getRightGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List contextGroups = businessGroupManager.findBusinessGroupsAttendedBy(bgContext.getGroupType(), identity, bgContext);
            allGroups.addAll(contextGroups);
        }
        return allGroups;
    }

    /**
	 */
    @Override
    public boolean isIdentityCourseCoach(final Identity identity, OLATResourceable courseResource) {
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final boolean isCoach = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_COACH, bgContext);
            if (isCoach) {
                return true;
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean isIdentityCourseAdministrator(final Identity identity, OLATResourceable ores) {
        // not really a group management method, for your convenience we have a
        // shortcut here...
        return baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ADMIN, ores);
    }

    /**
	 */
    @Override
    public boolean isIdentityParticipantInAnyLearningGroup(final Identity identity, OLATResourceable courseResource) {
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            if (contextManager.isIdentityInBGContext(identity, bgContext, false, true)) {
                return true;
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public boolean isIdentityParticipantInAnyRightGroup(final Identity identity, OLATResourceable courseResource) {
        final Iterator iterator = getRightGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            if (contextManager.isIdentityInBGContext(identity, bgContext, false, true)) {
                return true;
            }
        }
        return false;
    }

    /**
	 */
    @Override
    public void createCourseGroupmanagement(final String courseTitle, OLATResourceable ores) {
        OLATResource courseResource = resourceManager.findResourceable(ores);
        // 1. context for learning groups
        if (this.getLearningGroupContexts(courseResource).size() == 0) {
            final String learningGroupContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + courseTitle;
            businessGroupService.createAndAddBGContextToResource(learningGroupContextName, courseResource, BusinessGroup.TYPE_LEARNINGROUP, null, true);
            // no need to add it to list of contexts, already done by createAndAddBGContextToResource

        }
        // 2. context for right groups
        if (this.getRightGroupContexts(courseResource).size() == 0) {
            final String rightGroupContextName = CourseGroupManager.DEFAULT_NAME_RC_PREFIX + courseTitle;
            businessGroupService.createAndAddBGContextToResource(rightGroupContextName, courseResource, BusinessGroup.TYPE_RIGHTGROUP, null, true);
            // no need to add it to list of contexts, already done by createAndAddBGContextToResource
        }
    }

    /**
	 */
    @Override
    public void createCourseGroupmanagementAsCopy(final OLATResourceable originalCourseOres, final String courseTitle, final OLATResourceable copyCourseOres) {

        OLATResource origCourseResource = resourceManager.findResourceable(originalCourseOres);
        OLATResource copyCourseResource = resourceManager.findResourceable(copyCourseOres);
        // 1. do copy learning group contexts
        final List origLgC = getLearningGroupContexts(origCourseResource);
        Iterator iter = origLgC.iterator();
        while (iter.hasNext()) {
            final BGContext origContext = (BGContext) iter.next();
            if (origContext.isDefaultContext()) {
                // we found default context, copy this one
                final String learningGroupContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + courseTitle;
                businessGroupService.copyAndAddBGContextToResource(learningGroupContextName, copyCourseResource, origContext);
                // no need to add it to list of contexts, already done by copyAndAddBGContextToResource
            } else {
                // not a course default context but an associated context - copy only
                // reference
                businessGroupService.addBGContextToResource(origContext, copyCourseResource);
                // no need to add it to list of contexts, already done by addBGContextToResource
            }
        }
        // 2. do copy right group contexts
        final List origRgC = getRightGroupContexts(origCourseResource);
        iter = origRgC.iterator();
        while (iter.hasNext()) {
            final BGContext origContext = (BGContext) iter.next();
            if (origContext.isDefaultContext()) {
                // we found default context, copy this one
                final String rightGroupContextName = CourseGroupManager.DEFAULT_NAME_RC_PREFIX + courseTitle;
                businessGroupService.copyAndAddBGContextToResource(rightGroupContextName, copyCourseResource, origContext);
                // no need to add it to list of contexts, already done by copyAndAddBGContextToResource
            } else {
                // not a course default context but an associated context - copy only
                // reference
                businessGroupService.addBGContextToResource(origContext, copyCourseResource);
                // no need to add it to list of contexts, already done by addBGContextToResource
            }
        }
    }

    /**
	 */
    @Override
    public void deleteCourseGroupmanagement(OLATResourceable ores) {
        OLATResource courseResource = resourceManager.findResourceable(ores);
        final List allContexts = contextManager.findBGContextsForResource(courseResource, true, true);
        final Iterator iter = allContexts.iterator();
        while (iter.hasNext()) {
            final BGContext context = (BGContext) iter.next();
            if (context.isDefaultContext()) {
                bgContextService.deleteCompleteBGContext(context);
            } else {
                // not a default context, only unlink from this course
                contextManager.removeBGContextFromResource(context, courseResource);
            }
        }
        log.info("Audit:Deleting course groupmanagement for " + courseResource.toString());
    }

    /**
     * @param groups
     *            List of business groups
     * @return list of Integers that contain the number of participants for each group
     */
    @Override
    public List getNumberOfMembersFromGroups(final List groups) {
        final List members = new ArrayList();
        final Iterator iterator = groups.iterator();
        while (iterator.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iterator.next();
            final int numbMembers = baseSecurity.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
            members.add(new Integer(numbMembers));
        }
        return members;
    }

    /**
	 */
    @Override
    public List getUniqueAreaNamesFromAllContexts(OLATResourceable courseResource) {
        final List areas = getAllAreasFromAllContexts(courseResource);
        final List areaNames = new ArrayList();

        final Iterator iter = areas.iterator();
        while (iter.hasNext()) {
            final BGArea area = (BGArea) iter.next();
            final String areaName = area.getName();
            if (!areaNames.contains(areaName)) {
                areaNames.add(areaName.trim());
            }
        }

        Collections.sort(areaNames);

        return areaNames;
    }

    /**
	 */
    @Override
    public List getUniqueLearningGroupNamesFromAllContexts(OLATResourceable courseResource) {
        final List groups = getAllLearningGroupsFromAllContexts(courseResource);
        final List groupNames = new ArrayList();

        final Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            final String groupName = group.getName();
            if (!groupNames.contains(groupName)) {
                groupNames.add(groupName.trim());
            }
        }

        Collections.sort(groupNames);

        return groupNames;
    }

    /**
	 */
    @Override
    public void exportCourseLeaningGroups(final File fExportDirectory, OLATResourceable courseResource) {
        final BGContext context = findDefaultLearningContext(courseResource);
        final File fExportFile = new File(fExportDirectory, LEARNINGGROUPEXPORT_XML);
        groupImportExport.exportGroups(context, fExportFile);
    }

    /**
	 */
    @Override
    public void importCourseLearningGroups(final File fImportDirectory, OLATResourceable courseResource) {
        final File fGroupExportXML = new File(fImportDirectory, LEARNINGGROUPEXPORT_XML);
        final BGContext context = findDefaultLearningContext(courseResource);
        if (context == null) {
            throw new AssertException("Unable to find default context for imported course. Should have been created before calling importCourseLearningGroups()");
        }
        groupImportExport.importGroups(context, fGroupExportXML);
    }

    /**
	 */
    @Override
    public void exportCourseRightGroups(final File fExportDirectory, OLATResourceable courseResource) {
        final BGContext context = findDefaultRightsContext(courseResource);
        final File fExportFile = new File(fExportDirectory, RIGHTGROUPEXPORT_XML);
        groupImportExport.exportGroups(context, fExportFile);
    }

    /**
	 */
    @Override
    public void importCourseRightGroups(final File fImportDirectory, OLATResourceable courseResource) {
        final File fGroupExportXML = new File(fImportDirectory, RIGHTGROUPEXPORT_XML);
        final BGContext context = findDefaultRightsContext(courseResource);
        if (context == null) {
            throw new AssertException("Unable to find default context for imported course. Should have been created before calling importCourseLearningGroups()");
        }
        groupImportExport.importGroups(context, fGroupExportXML);
    }

    private BGContext findDefaultLearningContext(OLATResourceable courseResource) {
        final List contexts = getLearningGroupContexts(courseResource);
        BGContext context = null;
        for (final Iterator iter = contexts.iterator(); iter.hasNext();) {
            context = (BGContext) iter.next();
            if (context.isDefaultContext()) {
                break;
            }
        }
        return context;
    }

    private BGContext findDefaultRightsContext(OLATResourceable courseResource) {
        final List contexts = getRightGroupContexts(courseResource);
        BGContext context = null;
        for (final Iterator iter = contexts.iterator(); iter.hasNext();) {
            context = (BGContext) iter.next();
            if (context.isDefaultContext()) {
                break;
            }
        }
        return context;
    }

    /**
     * Returns a list with all coaches, without duplicates.
     */
    @Override
    public List<Identity> getCoachesFromLearningGroup(final String groupName, OLATResourceable courseResource) {
        final List coaches = new ArrayList();
        List bgs = getLearningGroupsFromAllContextsWithFiltering(groupName, courseResource);
        for (int i = 0; i < bgs.size(); i++) {
            // iterates over all business group in the course, fetching the identities
            // of the business groups OWNER
            final BusinessGroup elm = (BusinessGroup) bgs.get(i);
            SecurityGroup ownerGroup = elm.getOwnerGroup();
            coaches.addAll(baseSecurity.getIdentitiesOfSecurityGroup(ownerGroup));
        }
        final Set<Identity> coachesSet = new HashSet<Identity>(coaches);
        return new ArrayList<Identity>(coachesSet);
    }

    /**
     * @param groupName
     * @param courseResource
     * @return
     */
    private List getLearningGroupsFromAllContextsWithFiltering(final String groupName, OLATResourceable courseResource) {
        List bgs = null;
        if (groupName != null) {
            // filtered by name
            bgs = getLearningGroupsFromAllContexts(groupName, courseResource);
        } else {
            // no filter
            bgs = getAllLearningGroupsFromAllContexts(courseResource);
        }
        return bgs;
    }

    /**
	 */
    @Override
    public List<Identity> getParticipantsFromLearningGroup(final String groupName, OLATResourceable courseResource) {
        final List participants = new ArrayList();
        List bgs = getLearningGroupsFromAllContextsWithFiltering(groupName, courseResource);
        for (int i = 0; i < bgs.size(); i++) {
            // iterates over all business group in the course, fetching the identities
            // of the business groups PARTICIPANTS
            final BusinessGroup elm = (BusinessGroup) bgs.get(i);
            participants.addAll(baseSecurity.getIdentitiesOfSecurityGroup(elm.getPartipiciantGroup()));
        }
        final Set<Identity> participantSet = new HashSet<Identity>(participants);
        return new ArrayList<Identity>(participantSet);
    }

    /**
	 */
    @Override
    public List getCoachesFromArea(final String areaName, OLATResourceable courseResource) {
        final List retVal = new ArrayList();
        List bgs = null;
        if (areaName != null) {
            bgs = getLearningGroupsInAreaFromAllContexts(areaName, courseResource);
        } else {
            bgs = getAllLearningGroupsFromAllContexts(courseResource);
        }
        for (int i = 0; i < bgs.size(); i++) {
            // iterates over all business group in the course's area, fetching the
            // OWNER identities
            final BusinessGroup elm = (BusinessGroup) bgs.get(i);
            retVal.addAll(baseSecurity.getIdentitiesOfSecurityGroup(elm.getOwnerGroup()));
        }
        return retVal;
    }

    /**
	 */
    @Override
    public List getParticipantsFromArea(final String areaName, OLATResourceable courseResource) {
        final List retVal = new ArrayList();
        List bgs = null;
        if (areaName != null) {
            bgs = getLearningGroupsInAreaFromAllContexts(areaName, courseResource);
        } else {
            bgs = getAllLearningGroupsFromAllContexts(courseResource);
        }
        for (int i = 0; i < bgs.size(); i++) {
            // iterates over all business group in the course's area, fetching the
            // PARTIPICIANT identities
            final BusinessGroup elm = (BusinessGroup) bgs.get(i);
            retVal.addAll(baseSecurity.getIdentitiesOfSecurityGroup(elm.getPartipiciantGroup()));
        }
        return retVal;
    }

    /**
	 */
    @Override
    public List getWaitingListGroupsFromAllContexts(final Identity identity, OLATResourceable courseResource) {
        final List allGroups = new ArrayList();
        final Iterator iterator = getLearningGroupContexts(courseResource).iterator();
        while (iterator.hasNext()) {
            final BGContext bgContext = (BGContext) iterator.next();
            final List contextGroups = businessGroupManager.findBusinessGroupsWithWaitingListAttendedBy(bgContext.getGroupType(), identity, bgContext);
            allGroups.addAll(contextGroups);
        }
        return allGroups;
    }

    /**
     * Archive all learning-group-contexts and right-group-contexts.
     * 
     * @param exportDirectory
     */
    @Override
    public void archiveCourseGroups(final File exportDirectory, OLATResourceable courseResource) {
        archiveAllContextFor(getLearningGroupContexts(courseResource), LEARNINGGROUPARCHIVE_XLS, exportDirectory);
        archiveAllContextFor(getRightGroupContexts(courseResource), RIGHTGROUPARCHIVE_XLS, exportDirectory);
    }

    /**
     * Archive a list of context. Archive the default context in a xls file with prefix 'default_' e.g. default_learninggroupexport.xml. Archive all other context in xls
     * files with prefix 'context_<CONTEXTCOUNTER>_' e.g. context_2_learninggroupexport.xml
     * 
     * @param contextList
     *            List of BGContext
     * @param fileName
     *            E.g. learninggroupexport.xml
     * @param exportDirectory
     *            Archive files will be created in this dir.
     */
    private void archiveAllContextFor(final List contextList, final String fileName, final File exportDirectory) {
        int contextCounter = 1;
        for (final Iterator iter = contextList.iterator(); iter.hasNext();) {
            final BGContext context = (BGContext) iter.next();
            if (context.isDefaultContext()) {
                businessGroupService.archiveGroups(context, new File(exportDirectory, "default_" + fileName));
            } else {
                businessGroupService.archiveGroups(context, new File(exportDirectory, "context_" + contextCounter + "_" + fileName));
                contextCounter++;
            }
        }

    }

    @Override
    public List<Identity> getCourseTutors(OLATResourceable course) {
        final List<Identity> identities = new ArrayList<Identity>();
        final Set<Identity> identitiesSet = new HashSet<Identity>();
        final List<BusinessGroup> learningGroups = getAllLearningGroupsFromAllContexts(course);
        for (final BusinessGroup bg : learningGroups) {
            final List<Identity> coaches = getCoachesFromLearningGroup(bg.getName(), course);
            identitiesSet.addAll(coaches);
        }
        identities.addAll(identitiesSet);
        return identities;
    }

    @Override
    public List<Identity> getCourseOwnersAndTutors(OLATResourceable course) {
        final List<Identity> tutors = new ArrayList<Identity>();
        Set<Identity> identitiesSet = new HashSet<Identity>();
        identitiesSet.addAll(getCourseTutors(course));
        identitiesSet.addAll(getCourseOwners(course));
        tutors.addAll(identitiesSet);
        return tutors;
    }

    @Override
    public List<Identity> getCourseOwners(OLATResourceable course) {
        final List<Identity> identities = new ArrayList<Identity>();
        final RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(
                OresHelper.createOLATResourceableInstance(CourseModule.class, course.getResourceableId()), false);
        Set<Identity> identitiesSet = new HashSet<Identity>();
        identitiesSet.addAll(baseSecurity.getIdentitiesOfSecurityGroup(repositoryEntry.getOwnerGroup()));
        identities.addAll(identitiesSet);
        return identities;
    }

}
