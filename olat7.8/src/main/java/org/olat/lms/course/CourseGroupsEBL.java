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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.course.access.CourseAccessEvaluator;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.webfeed.Path;
import org.olat.presentation.framework.dispatcher.webfeed.FeedMediaDispatcher;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.mail.ContactList;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for CourseGroupEBL
 * 
 * <P>
 * Initial Date: 13.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class CourseGroupsEBL {

    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    BaseSecurityEBL baseSecurityEBL;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    CourseAccessEvaluator courseAccessEvaluatorImpl;

    /**
     * @param cgm
     * @param assessmentNotificationsHandler
     *            TODO
     * @param identity
     * @param course
     * @param hasFullAccess
     * @return
     */
    public List<Identity> getCoachedUsers(final Identity identity, final ICourse course) {
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        final List<Identity> coachedUsers = new ArrayList<Identity>();

        // initialize list of users, only when user has not full access
        final List<BusinessGroup> coachedGroups = cgm.getOwnedLearningGroupsFromAllContexts(identity, course);
        for (final Iterator<BusinessGroup> iter = coachedGroups.iterator(); iter.hasNext();) {
            final BusinessGroup group = iter.next();
            coachedUsers.addAll(baseSecurity.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup()));
        }

        return coachedUsers;
    }

    /**
     * @param cgm
     * @param identity
     * @param course
     * @return
     */
    public boolean hasAllRights(final Identity identity, final ICourse course) {
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        return (cgm.isIdentityCourseAdministrator(identity, course) ? true : cgm.hasRight(identity, CourseRights.RIGHT_ASSESSMENT, course));
    }

    /**
     * @param grpMan
     * @param identity
     * @param course
     * @return
     */
    public boolean isIdentityCourseAdminOrCoachOrHasRight(final Identity identity, final ICourse course) {
        final CourseGroupManager grpMan = course.getCourseEnvironment().getCourseGroupManager();
        return grpMan.isIdentityCourseAdministrator(identity, course) || grpMan.isIdentityCourseCoach(identity, course)
                || grpMan.hasRight(identity, CourseRights.RIGHT_ASSESSMENT, course);
    }

    /**
     * @param assessmentNotificationsHandler
     *            TODO
     * @param ident
     * @param course
     * @return
     */
    public boolean isSuperUser(final Identity ident, final ICourse course) {
        final boolean isInstitutionalResourceManager = baseSecurityEBL.isInstitutionalResourceManager(ident);
        boolean isIdentityCourseAdminOrCoachOrHasRight = isIdentityCourseAdminOrCoachOrHasRight(ident, course);
        return isInstitutionalResourceManager || isIdentityCourseAdminOrCoachOrHasRight;
    }

    /**
     * 
     * @param groupNames
     * @return
     */
    public ContactList[] getGroupsContactLists(final OLATResourceable oLATResourceable, final CourseGroupManager courseGroupManager, final List<String> groupNames) {
        final List<ContactList> groupsCL = new ArrayList<ContactList>();
        /*
         * for each group name in all the course's group contexts get the participants groups. From the resulting groups take all participants and add the identities to
         * the ContactList named like the group.
         */
        final Iterator<String> iterator = groupNames.iterator();
        while (iterator.hasNext()) {
            // fetch all participants and owners by getting all participants and
            // owners of all groups
            final String groupName = iterator.next();
            final List<BusinessGroup> mygroups = courseGroupManager.getLearningGroupsFromAllContexts(groupName, oLATResourceable);
            // create a ContactList with the name of the group
            final ContactList tmp = new ContactList(groupName);
            for (int i = 0; i < mygroups.size(); i++) {
                final BusinessGroup bg = mygroups.get(i);
                final List<Identity> ids = baseSecurity.getIdentitiesOfSecurityGroup(bg.getPartipiciantGroup());
                ids.addAll(baseSecurity.getIdentitiesOfSecurityGroup(bg.getOwnerGroup()));
                // add all identities to the ContactList
                tmp.addAllIdentites(ids);
            }
            // add the ContactList
            groupsCL.add(tmp);
        }
        // remove duplicates and convert List -> to Array.
        final Set<ContactList> groupsCLWithouthDups = new HashSet<ContactList>(groupsCL);
        ContactList[] retVal = new ContactList[groupsCLWithouthDups.size()];
        retVal = groupsCLWithouthDups.toArray(retVal);
        return retVal;
    }

    /**
     * @param courseEnvironment
     * @param oLATResourceable
     *            TODO
     * @param courseGroupManager
     *            TODO
     * @param areaNames
     * @return
     */
    public ContactList[] getAreasGroupsContactLists(final OLATResourceable oLATResourceable, final CourseGroupManager courseGroupManager, final List<String> areaNames) {
        final List<ContactList> groupsCL = new ArrayList<ContactList>();
        /*
         * for each area name in all the course's group contexts get the participants groups. From the resulting groups take all participants and add the identities to
         * the ContactList named like the group.
         */
        final Iterator<String> iterator = areaNames.iterator();
        while (iterator.hasNext()) {
            // fetch all participants and owners by getting all participants and
            // owners of all groups
            final String areaName = iterator.next();
            final List<BusinessGroup> mygroups = courseGroupManager.getLearningGroupsInAreaFromAllContexts(areaName, oLATResourceable);
            // create a ContactList with the name of the group
            final ContactList tmp = new ContactList(areaName);
            for (int i = 0; i < mygroups.size(); i++) {
                final BusinessGroup bg = mygroups.get(i);
                final List<Identity> ids = baseSecurity.getIdentitiesOfSecurityGroup(bg.getPartipiciantGroup());
                ids.addAll(baseSecurity.getIdentitiesOfSecurityGroup(bg.getOwnerGroup()));
                // add all identities to the ContactList
                tmp.addAllIdentites(ids);
            }
            // add the ContactList
            groupsCL.add(tmp);
        }
        // remove duplicates and convert List -> to Array.
        final Set<ContactList> groupsCLWithouthDups = new HashSet<ContactList>(groupsCL);
        ContactList[] retVal = new ContactList[groupsCLWithouthDups.size()];
        retVal = groupsCLWithouthDups.toArray(retVal);
        return retVal;
    }

    /**
     * Get the identity from the key.
     * 
     * @param idKey
     * @return the Identity
     */
    public Identity getIdentity(final Long idKey) {
        return baseSecurity.loadIdentityByKey(idKey);
    }

    /**
     * The global access verification method.
     * 
     * @param feed
     * @param path
     * @return true if the path may be dispatched.
     */
    public boolean hasAccess(final OLATResourceable feed, final Path path) {
        boolean hasAccess = false;
        final Identity identity = getIdentity(path.getIdentityKey());

        if (path.isCourseType()) {
            // A course node is being requested
            final OLATResourceable oresCourse = OLATResourceManager.getInstance().findResourceable(path.getCourseId(), CourseModule.getCourseTypeName());
            final ICourse course = CourseFactory.loadCourse(oresCourse);
            // Check access
            hasAccess = hasAccessCourseNode(identity, path.getToken(), course, path.getNodeId());
        } else {
            // A learning resource is being requested
            hasAccess = hasAccessFeed(identity, path.getToken(), feed);
        }
        return hasAccess;
    }

    /**
     * Verifies the access of an identity to a course node.
     * 
     * @param identity
     * @param token
     * @param course
     * @param node
     * @return True if the identity has access to the node in the given course. False otherwise.
     */
    private boolean hasAccessCourseNode(final Identity identity, final String token, final ICourse course, final String courseNodeId) {
        boolean hasAccess = false;
        if (allowsGuestAccess(course)) {
            hasAccess = true;
        } else {
            if (courseAccessEvaluatorImpl.isCourseNodeVisibleForIdentity(identity, course, courseNodeId, true) && baseSecurityEBL.hasValidAuthentication(identity, token)) {
                hasAccess = true;
            }
        }
        return hasAccess;
    }

    /**
     * Verifiy if the identity has access to the feed.
     * 
     * @param identity
     * @param token
     * @param feed
     * @return true if the identity has access.
     */
    private boolean hasAccessFeed(final Identity identity, final String token, final OLATResourceable feed) {
        boolean hasAccess = false;
        final RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntry(feed, false);
        if (repoEntry == null) {
            // fixed bug http://bugs.olat.org/jira/browse/OLAT-6525:
            // Both a blog course node and a "Learning journal"/"Lerntagebuch" (technically 'LiveBlog') is referenced by a BlogFileResource.
            // A "Learning journal" has no repo entry but a correponding e-portfolio artefact (handled by LiveBlogArtefactHandler).
            // This is rather a quick hack but since the "Learning journal" simply reuses the blog functionality without
            // adapting concepts (e.g. the file resources should rather be stored under bcroot/portfolio than bcroot/repository)
            // it's IMHO acceptable until the concept has been reworked.
            if (feed.getResourceableTypeName().equals(BlogFileResource.TYPE_NAME) && identity != null) {
                final EPFrontendManager ePFrontendManager = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
                if (ePFrontendManager.hasAccessToLiveBlogFeedMedia(feed, identity)) {
                    hasAccess = validAuthentication(identity, token);
                } else {
                    hasAccess = false;
                }
            }
        } else {
            if (allowsGuestAccess(feed)) {
                hasAccess = true;
            } else if (identity != null) {
                final Roles roles = baseSecurity.getRoles(identity);
                final boolean isAllowedToLaunch = repositoryService.isAllowedToLaunch(identity, roles, repoEntry);
                if (isAllowedToLaunch && validAuthentication(identity, token)) {
                    hasAccess = true;
                }
            }
        }
        return hasAccess;
    }

    /**
     * Authenticates the identity by token
     * 
     * @param identity
     * @param token
     * @return True if authentication is valid
     */
    private boolean validAuthentication(final Identity identity, final String token) {
        boolean valid = false;
        final Authentication authentication = baseSecurity.findAuthenticationByAuthusername(identity.getKey().toString(), FeedMediaDispatcher.TOKEN_PROVIDER);
        if (authentication != null && authentication.getCredential().equals(token)) {
            valid = true;
        }
        return valid;
    }

    /**
     * @param feed
     * @param res
     *            TODO
     * @return true if the feed allows guest access.
     * @return
     */
    private boolean allowsGuestAccess(final OLATResourceable res) {
        boolean guestsAllowed = false;
        final RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntry(res, false);
        if (repoEntry.getAccess() == RepositoryEntry.ACC_USERS_GUESTS) {
            guestsAllowed = true;
        }
        return guestsAllowed;
    }

    /**
     * 
     * @param course
     * @return list of identities
     */
    public List<Identity> getSelectedIdentities(final ICourse course) {
        final List<Identity> identities = new ArrayList<Identity>();
        identities.addAll(getLearningGroupsIdentities(course));
        identities.addAll(getSecurityGroupIdentities(course));
        return identities;
    }

    private List<Identity> getSecurityGroupIdentities(final ICourse course) {
        final List<Identity> identities = new ArrayList<Identity>();
        final RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(course, true);
        final SecurityGroup sg = repositoryEntry.getOwnerGroup();
        final BaseSecurity securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        final List<Object[]> owners = securityManager.getIdentitiesAndDateOfSecurityGroup(sg);
        for (final Object[] owner : owners) {
            identities.add((Identity) owner[0]);
        }
        return identities;
    }

    private List<Identity> getLearningGroupsIdentities(final ICourse course) {
        final List<Identity> identities = new ArrayList<Identity>();
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        final List<BusinessGroup> learningGroups = cgm.getAllLearningGroupsFromAllContexts(course);
        for (final BusinessGroup bg : learningGroups) {
            final List<Identity> participants = cgm.getParticipantsFromLearningGroup(bg.getName(), course);
            identities.addAll(participants);
            final List<Identity> coaches = cgm.getCoachesFromLearningGroup(bg.getName(), course);
            identities.addAll(coaches);
        }
        return identities;
    }

    /**
     * do unsubscribe all group members from this course
     * 
     * @param identity
     * @param repositoryEntry
     */
    public void removeAllMemberFromCourseGroups(final Identity identity, RepositoryEntry repositoryEntry) {
        final ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
        if (course != null) {
            SecurityGroup secGroupOwner = null;
            SecurityGroup secGroupPartipiciant = null;
            SecurityGroup secGroupWaiting = null;
            // LearningGroups
            List allGroups = course.getCourseEnvironment().getCourseGroupManager().getAllLearningGroupsFromAllContexts(course);
            final BGConfigFlags flagsLearning = BGConfigFlags.createLearningGroupDefaultFlags();
            for (final Object bGroup : allGroups) {
                secGroupOwner = ((BusinessGroup) bGroup).getOwnerGroup();
                secGroupPartipiciant = ((BusinessGroup) bGroup).getPartipiciantGroup();
                businessGroupService
                        .removeOwnersAndFireEvent(identity, baseSecurity.getIdentitiesOfSecurityGroup(secGroupOwner), ((BusinessGroup) bGroup), flagsLearning);
                businessGroupService.removeParticipantsAndFireEvent(identity, baseSecurity.getIdentitiesOfSecurityGroup(secGroupPartipiciant), ((BusinessGroup) bGroup),
                        flagsLearning);
                if (((BusinessGroup) bGroup).getWaitingListEnabled()) {
                    secGroupWaiting = ((BusinessGroup) bGroup).getWaitingGroup();
                    businessGroupService.removeFromWaitingListAndFireEvent(identity, baseSecurity.getIdentitiesOfSecurityGroup(secGroupWaiting),
                            ((BusinessGroup) bGroup), flagsLearning);
                }
            }
            // RightGroups
            allGroups.clear();
            allGroups = course.getCourseEnvironment().getCourseGroupManager().getAllRightGroupsFromAllContexts(course);
            final BGConfigFlags flagsRightgroup = BGConfigFlags.createRightGroupDefaultFlags();
            for (final Object bGroup : allGroups) {
                secGroupPartipiciant = ((BusinessGroup) bGroup).getPartipiciantGroup();
                businessGroupService.removeParticipantsAndFireEvent(identity, baseSecurity.getIdentitiesOfSecurityGroup(secGroupPartipiciant), ((BusinessGroup) bGroup),
                        flagsRightgroup);
            }
        }
    }

}
