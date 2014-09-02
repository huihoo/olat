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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<BR>
 * Business-logic of enrollemnt
 * <p>
 * Handle enroll and cancel-enrollment
 * <P>
 * Initial Date: Nov 11, 2006
 * 
 * @author Christian Guretzki
 */
@Component
public class EnrollmentManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    private static EnrollmentManager INSTANCE;
    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private BusinessGroupService businessGroupService;

    /**
     * @param moduleConfiguration
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param enNode
     */
    private EnrollmentManager() {
        INSTANCE = this;
    }

    public static EnrollmentManager getInstance() {
        return INSTANCE;
    }

    public EnrollStatus doEnroll(final OLATResourceable ores, final Identity identity, final BusinessGroup group, final ENCourseNode enNode,
            final CoursePropertyManager coursePropertyManager, final List groupNames, final List areaNames, final CourseGroupManager cgm) {
        final EnrollStatus enrollStatus = new EnrollStatus();
        if (log.isDebugEnabled()) {
            log.debug("doEnroll");
        }
        // check if the user is already enrolled (user can be enrooled only in one group)
        if ((getBusinessGroupWhereEnrolled(ores, identity, groupNames, areaNames, cgm) == null)
                && (getBusinessGroupWhereInWaitingList(ores, identity, groupNames, areaNames, cgm) == null)) {
            if (log.isDebugEnabled()) {
                log.debug("Identity is not enrolled identity=" + identity.getName() + "  group=" + group.getName());
                // 1. Check if group has max size defined. If so check if group is full
                // o_clusterREVIEW cg please review it - also where does the group.getMaxParticipants().equals("") come from??
                // and: why can't we just have a group here and a max participants count and an identity to enrol?
                // the group was chosen, so why do we need the groupNames and areaNames here???
            }

            Codepoint.codepoint(EnrollmentManager.class, "beforeDoInSync");
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor() {
                @Override
                public void execute() {
                    log.info("doEnroll start: group=" + OresHelper.createStringRepresenting(group) + identity.getName());
                    Codepoint.codepoint(EnrollmentManager.class, "doInSync1");
                    // 6_1_0-RC15: reload group object here another node might have changed this in the meantime
                    final BusinessGroup reloadedGroup = businessGroupService.loadBusinessGroup(group, true);
                    if (reloadedGroup.getMaxParticipants() != null && !reloadedGroup.getMaxParticipants().equals("")) {
                        final int participantsCounter = securityManager.countIdentitiesOfSecurityGroup(reloadedGroup.getPartipiciantGroup());

                        log.info("doEnroll - participantsCounter: " + participantsCounter + ", maxParticipants: " + reloadedGroup.getMaxParticipants().intValue()
                                + identity.getName());
                        if (participantsCounter >= reloadedGroup.getMaxParticipants().intValue()) {
                            // already full, show error and updated choose page again
                            if (!reloadedGroup.getWaitingListEnabled().booleanValue()) {
                                // No Waiting List => List is full
                                enrollStatus.setErrorMessageKey("error.group.full");
                            } else {
                                final boolean done = addUserToWaitingList(identity, reloadedGroup, enNode, coursePropertyManager);
                                enrollStatus.setIsInWaitingList(done);
                            }
                        } else {
                            final boolean done = addUserToParticipantList(identity, reloadedGroup, enNode, coursePropertyManager);
                            Codepoint.codepoint(EnrollmentManager.class, "doInSync2");
                            enrollStatus.setIsEnrolled(done);
                            log.info("doEnroll - setIsEnrolled:  " + identity.getName());
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("doEnroll beginTransaction");
                        }

                        final boolean done = addUserToParticipantList(identity, reloadedGroup, enNode, coursePropertyManager);
                        enrollStatus.setIsEnrolled(done);
                        if (log.isDebugEnabled()) {
                            log.debug("doEnroll committed");
                        }
                    }
                    log.info("doEnroll end: " + identity.getName());
                }
            });// end of doInSync
            Codepoint.codepoint(EnrollmentManager.class, "afterDoInSync");
        } else {
            enrollStatus.setErrorMessageKey("error.group.already.enrolled");
        }
        if (log.isDebugEnabled()) {
            log.debug("doEnroll finished");
        }
        return enrollStatus;
    }

    public EnrollStatus doCancelEnrollment(final Identity identity, final BusinessGroup enrolledGroup, final ENCourseNode enNode,
            final CoursePropertyManager coursePropertyManager) {
        if (log.isDebugEnabled()) {
            log.debug("doCancelEnrollment");
        }
        // 1. Remove group membership, fire events, do loggin etc.
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();

        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(enrolledGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                // Remove participant. This will also check if a waiting-list with auto-close-ranks is configurated
                // and move the users accordingly
                businessGroupService.removeParticipantAndFireEvent(identity, identity, enrolledGroup, flags, false);
                log.info("doCancelEnrollment in group " + enrolledGroup + identity.getName());
                // 2. Remove enrollmentdate property
                // only remove last time date, not firsttime
                final PropertyImpl lastTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE);
                if (lastTime != null) {
                    coursePropertyManager.deleteProperty(lastTime);
                }
            }
        });

        EnrollStatus enrollStatus = new EnrollStatus();
        return enrollStatus;
    }

    public EnrollStatus doCancelEnrollmentInWaitingList(final Identity identity, final BusinessGroup enrolledWaitingListGroup, final ENCourseNode enNode,
            final CoursePropertyManager coursePropertyManager) {
        // 1. Remove group membership, fire events, do loggin etc.
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(enrolledWaitingListGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                businessGroupService.removeFromWaitingListAndFireEvent(identity, identity, enrolledWaitingListGroup, false);
                // 2. Remove enrollmentdate property
                // only remove last time date, not firsttime
                final PropertyImpl lastTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE);
                if (lastTime != null) {
                    coursePropertyManager.deleteProperty(lastTime);
                }
            }
        });

        EnrollStatus enrollStatus = new EnrollStatus();
        return enrollStatus;
    }

    // Helper Methods
    // ////////////////
    /**
     * @param identity
     * @param groupNames
     * @return BusinessGroup in which the identity is enrolled, null if identity is nowhere enrolled.
     */
    public BusinessGroup getBusinessGroupWhereEnrolled(final OLATResourceable ores, final Identity identity, final List groupNames, final List areaNames,
            final CourseGroupManager cgm) {
        Iterator iterator = groupNames.iterator();
        // 1. check in groups
        while (iterator.hasNext()) {
            final String groupName = (String) iterator.next();
            final List groups = cgm.getParticipatingLearningGroupsFromAllContexts(identity, groupName, ores);
            if (groups.size() > 0) {
                // Usually it is only possible to be in one group. However,
                // theoretically the
                // admin can put the user in a second enrollment group or the user could
                // theoretically be in a second group context. For now, we only look for
                // the first
                // group. All groups found after the first one are disgarded.
                return (BusinessGroup) groups.get(0);
            }
        }
        // 2. check in areas
        iterator = areaNames.iterator();
        while (iterator.hasNext()) {
            final String areaName = (String) iterator.next();
            final List groups = cgm.getParticipatingLearningGroupsInAreaFromAllContexts(identity, areaName, ores);
            if (groups.size() > 0) {
                // Usually it is only possible to be in one group. However,
                // theoretically the
                // admin can put the user in a second enrollment group or the user could
                // theoretically be in a second group context. For now, we only look for
                // the first
                // group. All groups found after the first one are disgarded.
                return (BusinessGroup) groups.get(0);
            }
        }
        return null; //
    }

    /**
     * @param identity
     * @param groupNames
     * @return true if this identity is any waiting-list group in this course that has a name that is in the group names list
     */
    public BusinessGroup getBusinessGroupWhereInWaitingList(final OLATResourceable ores, final Identity identity, final List groupNames, final List areaNames,
            final CourseGroupManager cgm) {
        final List groups = loadGroupsFromNames(ores, groupNames, areaNames, cgm);
        BusinessGroup businessGroup;
        // loop over all business-groups
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            businessGroup = (BusinessGroup) iter.next();
            if (securityManager.isIdentityInSecurityGroup(identity, businessGroup.getWaitingGroup())) {
                return businessGroup;
            }
        }
        return null;
    }

    /**
     * @param groupNames
     * @return a list of business groups from any of the courses group contexts that match the names from the groupNames list. If a groupname is not found it won't be in
     *         the list. So groupNames.size() can very well by different than loadGroupsFromNames().size()
     */
    public List<BusinessGroup> loadGroupsFromNames(final OLATResourceable ores, final List<String> groupNames, final List<String> areaNames, final CourseGroupManager cgm) {
        final List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
        // 1. add groups
        for (final String groupName : groupNames) {
            final List mygroups = cgm.getLearningGroupsFromAllContexts(groupName, ores);
            for (final Iterator it = mygroups.iterator(); it.hasNext();) {
                final BusinessGroup bg = (BusinessGroup) it.next();
                if (!groups.contains(bg)) {
                    groups.add(bg);
                }
            }
        }
        // add groups from areas
        for (final String areaName : areaNames) {
            final List mygroups = cgm.getLearningGroupsInAreaFromAllContexts(areaName, ores);
            for (final Iterator it = mygroups.iterator(); it.hasNext();) {
                final BusinessGroup bg = (BusinessGroup) it.next();
                if (!groups.contains(bg)) {
                    groups.add(bg);
                }
            }
        }
        return groups;
    }

    /**
     * Check if in any business-group a waiting-list is configured.
     * 
     * @param groups
     * @return true : YES, there are waiting-list<br>
     *         false: NO, no waiting-list
     */
    public boolean hasAnyWaitingList(final List groups) {
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup businessGroup = (BusinessGroup) iter.next();
            if (businessGroup.getWaitingListEnabled().booleanValue()) {
                return true;
            }
        }
        return false;
    }

    // /////////////////
    // Private Methods
    // /////////////////
    private boolean addUserToParticipantList(final Identity identity, final BusinessGroup group, final ENCourseNode enNode,
            final CoursePropertyManager coursePropertyManager) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        // 1. Add user to group, fire events, do loggin etc.
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
        businessGroupService.addParticipantAndFireEvent(identity, identity, group, flags, false);
        // 2. Set first enrollment date
        final String nowString = Long.toString(System.currentTimeMillis());
        PropertyImpl firstTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_INITIAL_ENROLLMENT_DATE);
        if (firstTime == null) {
            // create firsttime
            firstTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null, ENCourseNode.PROPERTY_INITIAL_ENROLLMENT_DATE, null, null,
                    nowString, null);
            coursePropertyManager.saveProperty(firstTime);
        }
        // 3. Set enrollmentdate property
        PropertyImpl thisTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE);
        if (thisTime == null) {
            // create firsttime
            thisTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE, null, null,
                    nowString, null);
            coursePropertyManager.saveProperty(thisTime);
        } else {
            thisTime.setStringValue(nowString);
            coursePropertyManager.updateProperty(thisTime);
        }

        return true;
    }

    private boolean addUserToWaitingList(final Identity identity, final BusinessGroup group, final ENCourseNode enNode, final CoursePropertyManager coursePropertyManager) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        // 1. Add user to group, fire events, do loggin etc.
        businessGroupService.addToWaitingListAndFireEvent(identity, identity, group, false);
        // 2. Set first waiting-list date
        final String nowString = Long.toString(System.currentTimeMillis());
        PropertyImpl firstTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_INITIAL_WAITINGLIST_DATE);
        if (firstTime == null) {
            // create firsttime
            firstTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null, ENCourseNode.PROPERTY_INITIAL_WAITINGLIST_DATE, null, null,
                    nowString, null);
            coursePropertyManager.saveProperty(firstTime);
        }
        // 3. Set waiting-list date property
        PropertyImpl thisTime = coursePropertyManager.findCourseNodeProperty(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE);
        if (thisTime == null) {
            // create firsttime
            thisTime = coursePropertyManager.createCourseNodePropertyInstance(enNode, identity, null, ENCourseNode.PROPERTY_RECENT_WAITINGLIST_DATE, null, null,
                    nowString, null);
            coursePropertyManager.saveProperty(thisTime);
        } else {
            thisTime.setStringValue(nowString);
            coursePropertyManager.updateProperty(thisTime);
        }

        return true;
    }

}
