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

package org.olat.lms.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectBrokerDao;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.group.edit.BusinessGroupModifiedEvent;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guretzki
 */
@Component
public class ProjectGroupManagerImpl extends BasicManager implements ProjectGroupManager {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    ProjectBrokerDao projectBrokerDao;

    /**
	 *  
	 */
    private ProjectGroupManagerImpl() {
        //
    }

    // ////////////////////
    // ACCOUNT MANAGEMENT
    // ////////////////////
    @Override
    public BusinessGroup getAccountManagerGroupFor(final CoursePropertyManager cpm, final CourseNode courseNode, final ICourse course, final String groupName,
            final String groupDescription, final Identity identity) {
        Long groupKey = null;
        BusinessGroup accountManagerGroup = null;
        final PropertyImpl accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
        // Check if account-manager-group-key-property already exist
        if (accountManagerGroupProperty != null) {
            groupKey = accountManagerGroupProperty.getLongValue();
            log.debug("accountManagerGroupProperty=" + accountManagerGroupProperty + "  groupKey=" + groupKey);
        }
        log.debug("groupKey=" + groupKey);
        if (groupKey != null) {
            accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey, false);
            log.debug("load businessgroup=" + accountManagerGroup);
            if (accountManagerGroup != null) {
                return accountManagerGroup;
            } else {
                if (accountManagerGroupProperty != null) {
                    cpm.deleteProperty(accountManagerGroupProperty);
                }
                groupKey = null;
                log.warn("ProjectBroker: Account-manager does no longer exist, create a new one", null);
            }
        } else {
            log.debug("No group for project-broker exist => create a new one");
            final BGContext context = createGroupContext(course);
            log.info("groupName=" + groupName);
            accountManagerGroup = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, groupName, groupDescription, null, null,
                    false, false, context);
            int i = 2;
            while (accountManagerGroup == null) {
                // group with this name exist already, try another name
                accountManagerGroup = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, groupName + " _" + i,
                        groupDescription, null, null, false, false, context);
                i++;
            }
            log.debug("createAndPersistBusinessGroup businessgroup=" + accountManagerGroup);

            saveAccountManagerGroupKey(accountManagerGroup.getKey(), cpm, courseNode);
            log.debug("created account-manager default businessgroup=" + accountManagerGroup);
        }
        return accountManagerGroup;
    }

    public void saveAccountManagerGroupKey(final Long accountManagerGroupKey, final CoursePropertyManager cpm, final CourseNode courseNode) {
        final PropertyImpl accountManagerGroupKeyProperty = cpm.createCourseNodePropertyInstance(courseNode, null, null,
                ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY, null, accountManagerGroupKey, null, null);
        cpm.saveProperty(accountManagerGroupKeyProperty);
        log.debug("saveAccountManagerGroupKey accountManagerGroupKey=" + accountManagerGroupKey);
    }

    @Override
    public boolean isAccountManager(final Identity identity, final CoursePropertyManager cpm, final CourseNode courseNode) {
        final PropertyImpl accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
        if (accountManagerGroupProperty != null) {
            final Long groupKey = accountManagerGroupProperty.getLongValue();
            final BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey, false);
            if (accountManagerGroup != null) {
                return isAccountManager(identity, accountManagerGroup);
            }
        }
        return false;
    }

    @Override
    public void deleteAccountManagerGroup(final CoursePropertyManager cpm, final CourseNode courseNode) {
        log.debug("deleteAccountManagerGroup start...");
        final PropertyImpl accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
        if (accountManagerGroupProperty != null) {
            final Long groupKey = accountManagerGroupProperty.getLongValue();
            if (groupKey != null) {
                final BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey, false);
                if (accountManagerGroup != null) {
                    businessGroupService.deleteBusinessGroup(accountManagerGroup);
                    log.info("Audit:ProjectBroker: Deleted accountManagerGroup=" + accountManagerGroup);
                } else {
                    log.debug("deleteAccountManagerGroup: accountManagerGroup=" + accountManagerGroup + " has already been deleted");
                }
            }
            cpm.deleteProperty(accountManagerGroupProperty);
            log.debug("deleteAccountManagerGroup: deleted accountManagerGroupProperty=" + accountManagerGroupProperty);
        } else {
            log.debug("deleteAccountManagerGroup: found no accountManagerGroup-key");
        }
    }

    @Override
    public void updateAccountManagerGroupName(final String groupName, final String groupDescription, final BusinessGroup accountManagerGroup) {
        if (businessGroupService == null) {
            throw new AssertException("Object 'businessGroupService' is null (see I-130412-0106)");
        }
        if (accountManagerGroup == null) {
            throw new AssertException("Object 'accountManagerGroup' is null (see I-130412-0106)");
        }
        final BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(accountManagerGroup.getKey(), true);
        reloadedBusinessGroup.setName(groupName);
        reloadedBusinessGroup.setDescription(groupDescription);
        businessGroupService.updateBusinessGroup(reloadedBusinessGroup);
    }

    // //////////////////////////
    // PROJECT GROUP MANAGEMENT
    // //////////////////////////
    @Override
    public BusinessGroup createProjectGroupFor(final Long projectBrokerId, final Identity identity, final String groupName, final String groupDescription,
            final Long courseId) {
        final List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);

        final BGContext context = createGroupContext(CourseFactory.loadCourse(courseId));
        log.debug("createProjectGroupFor groupName=" + groupName);
        BusinessGroup projectGroup = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, groupName, groupDescription, null,
                null, false, false, context);
        // projectGroup could be null when a group with name already exists
        int counter = 2;
        while (projectGroup == null) {
            // name alreday exist try another one
            final String newGroupName = groupName + " _" + counter;
            projectGroup = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, identity, newGroupName, groupDescription, null, null,
                    false, false, context);
            counter++;
        }
        log.debug("Created a new projectGroup=" + projectGroup);
        return projectGroup;
    }

    @Override
    public void deleteProjectGroupFor(final Project project) {
        businessGroupService.deleteBusinessGroup(project.getProjectGroup());
    }

    /**
     * Change group-name and description. Check if new group-name does not already exist in the course-group-context. If the goup-name already exist, it will be
     * automatically try another one with suffix e.g. ' _2'
     * 
     * java.lang.String)
     */
    @Override
    public void changeProjectGroupName(final BusinessGroup projectGroup, final String initialGroupName, final String groupDescription) {
        final BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(projectGroup.getKey(), true);
        log.debug("initialGroupName=" + initialGroupName);
        String groupName = initialGroupName;
        Set names = new HashSet();
        names.add(groupName);
        int counter = 2;
        while (businessGroupService.checkIfOneOrMoreNameExistsInContext(names, reloadedBusinessGroup.getGroupContext())) {
            // a group with name already exist => look for an other one, append a number
            groupName = initialGroupName + " _" + counter++;
            log.debug("try groupName=" + groupName);
            names = new HashSet();
            names.add(groupName);

        }
        log.debug("groupName=" + groupName);
        reloadedBusinessGroup.setName(groupName);
        reloadedBusinessGroup.setDescription(groupDescription);
        businessGroupService.updateBusinessGroup(reloadedBusinessGroup);
    }

    @Override
    public List<Identity> addCandidates(final List<Identity> identitiesToAdd, final Project project) {
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
        final List<Identity> addedIdentities = CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(project.getProjectGroup(), new SyncerCallback<List<Identity>>() {
                    @Override
                    public List<Identity> execute() {
                        final List<Identity> addedIdentities = new ArrayList<Identity>();
                        for (final Identity identity : identitiesToAdd) {
                            if (baseSecurity.isIdentityInSecurityGroup(identity, project.getCandidateGroup())) {
                                // this should never happen since adding identity to own group should be avoided by generic group handling
                                log.warn("Audit:ProjectBroker: Didn't add user as candidate since already in group, identity=" + identity);
                                continue;
                            }
                            if (baseSecurity.isIdentityInSecurityGroup(identity, project.getProjectParticipantGroup())) {
                                log.warn("Audit:ProjectBroker: Didn't add user as candidate since already participant, identity=" + identity);
                                continue;
                            }

                            baseSecurity.addIdentityToSecurityGroup(identity, project.getCandidateGroup());
                            addedIdentities.add(identity);
                            log.info("Audit:ProjectBroker: Add user as candidate, identity=" + identity);
                        }

                        return addedIdentities;
                    }
                });// end of doInSync
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
        return addedIdentities;
    }

    @Override
    public void removeCandidates(final List<Identity> addIdentities, final Project project) {
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
        final Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                final Project reloadedProject = projectBrokerDao.loadProject(project);
                for (final Identity identity : addIdentities) {
                    baseSecurity.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
                    log.info("Audit:ProjectBroker: Remove user as candidate, identity=" + identity);
                    // fireEvents ?
                }
                return Boolean.TRUE;
            }
        });// end of doInSync
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
    }

    @Override
    public List<Identity> acceptCandidates(final List<Identity> identities, final Project project, final Identity actionIdentity, final boolean autoSignOut,
            final boolean isAcceptSelectionManually) {
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
        final Project reloadedProject = projectBrokerDao.loadProject(project);
        final List<Identity> acceptedCandidates = CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(project.getProjectGroup(), new SyncerCallback<List<Identity>>() {
                    @Override
                    public List<Identity> execute() {
                        final List<Identity> acceptedCandidates = new ArrayList<Identity>();
                        final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
                        for (final Identity identity : identities) {
                            if (baseSecurity.isIdentityInSecurityGroup(identity, reloadedProject.getProjectParticipantGroup())) {
                                log.warn("Audit:ProjectBroker: Didn't accept candidate since already in participant group, identity=" + identity + " project="
                                        + reloadedProject);
                                continue;
                            }

                            baseSecurity.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
                            businessGroupService.addParticipantAndFireEvent(actionIdentity, identity, reloadedProject.getProjectGroup(), flags, false);
                            acceptedCandidates.add(identity);
                            log.info("Audit:ProjectBroker: Accept candidate, identity=" + identity + " project=" + reloadedProject);
                        }
                        return acceptedCandidates;
                    }
                });// end of doInSync
        if (autoSignOut && !acceptedCandidates.isEmpty()) {
            ProjectBrokerManagerFactory.getProjectBrokerManager().signOutFromAllCandidateLists(identities, reloadedProject.getProjectBroker().getKey());
        }
        if (isAcceptSelectionManually && (reloadedProject.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED)
                && reloadedProject.getSelectedPlaces() >= reloadedProject.getMaxMembers()) {
            ProjectBrokerManagerFactory.getProjectBrokerManager().setProjectState(reloadedProject, Project.STATE_ASSIGNED);
            log.info("ProjectBroker: Accept candidate, change project-state=" + Project.STATE_ASSIGNED);
        }
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");

        return acceptedCandidates;
    }

    @Override
    public void sendGroupChangeEvent(final Project project, final Long courseResourceableId, final Identity identity) {
        final ICourse course = CourseFactory.loadCourse(courseResourceableId);
        final RepositoryEntry ores = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, true);
        final MultiUserEvent modifiedEvent = new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, project.getProjectGroup(), identity);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, ores);
    }

    @Override
    public boolean isProjectManager(final Identity identity, final Project project) {
        return baseSecurity.isIdentityInSecurityGroup(identity, project.getProjectLeaderGroup());
    }

    @Override
    public boolean isProjectManagerOrAdministrator(final UserRequest ureq, final CourseEnvironment courseEnv, final Project project) {
        return ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(ureq.getIdentity(), project)
                || courseEnv.getCourseGroupManager().isIdentityCourseAdministrator(ureq.getIdentity(), courseEnv.getCourseOLATResourceable())
                || ureq.getUserSession().getRoles().isOLATAdmin();
    }

    @Override
    public boolean isProjectParticipant(final Identity identity, final Project project) {
        return baseSecurity.isIdentityInSecurityGroup(identity, project.getProjectParticipantGroup());
    }

    @Override
    public boolean isProjectCandidate(final Identity identity, final Project project) {
        return baseSecurity.isIdentityInSecurityGroup(identity, project.getCandidateGroup());
    }

    @Override
    public void setProjectGroupMaxMembers(final BusinessGroup projectGroup, final int maxMembers) {
        final BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(projectGroup.getKey(), true);
        log.debug("ProjectGroup.name=" + reloadedBusinessGroup.getName() + " setMaxParticipants=" + maxMembers);
        reloadedBusinessGroup.setMaxParticipants(maxMembers);
        businessGroupService.updateBusinessGroup(reloadedBusinessGroup);
    }

    // /////////////////
    // PRIVATE METHODS
    // /////////////////
    private BGContext createGroupContext(final ICourse course) {
        final List<BGContext> groupContexts = course.getCourseEnvironment().getCourseGroupManager().getLearningGroupContexts(course);
        log.debug("createGroupContext groupContexts.size=" + groupContexts.size());
        for (final Iterator<BGContext> iterator = groupContexts.iterator(); iterator.hasNext();) {
            final BGContext iterContext = iterator.next();
            log.debug("createGroupContext loop iterContext=" + iterContext);
            if (iterContext.isDefaultContext()) {
                log.debug("createGroupContext default groupContexts=" + iterContext);
                return iterContext;
            }
        }
        // found no default context
        final String defaultContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + course.getCourseTitle();
        if (groupContexts.size() == 0) {
            log.debug("no group context exists, create a new default defaultContextName=" + defaultContextName);
        } else {
            log.debug("Found no default group context, create a new default defaultContextName=" + defaultContextName);
        }
        // no context exists => create a new default context
        final OLATResource courseResource = OLATResourceManager.getInstance().findOrPersistResourceable(course);
        final BGContext context = businessGroupService.createAndAddBGContextToResource(defaultContextName, courseResource, BusinessGroup.TYPE_LEARNINGROUP, null, true);
        return context;
    }

    private boolean isAccountManager(final Identity identity, final BusinessGroup businessGroup) {
        if ((businessGroup == null) || (businessGroup.getPartipiciantGroup() == null)) {
            return false;
        }
        return baseSecurity.isIdentityInSecurityGroup(identity, businessGroup.getPartipiciantGroup())
                || baseSecurity.isIdentityInSecurityGroup(identity, businessGroup.getOwnerGroup());
    }

    @Override
    public void acceptAllCandidates(final Long projectBrokerId, final Identity actionIdentity, final boolean autoSignOut, final boolean isAcceptSelectionManually) {
        // loop over all project
        final List<Project> projectList = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
        for (final Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
            final Project project = iterator.next();
            final List<Identity> candidates = baseSecurity.getIdentitiesOfSecurityGroup(project.getCandidateGroup());
            if (!candidates.isEmpty()) {
                log.info("Audit:ProjectBroker: Accept ALL candidates, project=" + project);
                acceptCandidates(candidates, project, actionIdentity, autoSignOut, isAcceptSelectionManually);
            }
        }

    }

    @Override
    public boolean hasProjectBrokerAnyCandidates(final Long projectBrokerId) {
        final List<Project> projectList = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
        for (final Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
            final Project project = iterator.next();
            final List<Identity> candidates = baseSecurity.getIdentitiesOfSecurityGroup(project.getCandidateGroup());
            if (!candidates.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCandidateListEmpty(final SecurityGroup candidateGroup) {
        final List<Identity> candidates = baseSecurity.getIdentitiesOfSecurityGroup(candidateGroup);
        return candidates.isEmpty();
    }

}
