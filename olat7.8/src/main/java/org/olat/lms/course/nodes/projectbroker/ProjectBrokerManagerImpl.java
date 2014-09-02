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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.Project.EventType;
import org.olat.data.course.nodes.projectbroker.ProjectBroker;
import org.olat.data.course.nodes.projectbroker.ProjectBrokerDao;
import org.olat.data.course.nodes.projectbroker.ProjectEvent;
import org.olat.data.course.nodes.projectbroker.ProjectImpl;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.ta.Dropbox_EBL;
import org.olat.lms.course.nodes.ta.Returnbox_EBL;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.DeletableGroupData;
import org.olat.lms.group.DeletableReference;
import org.olat.presentation.course.nodes.projectbroker.ProjectBrokerNodeConfiguration;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableDeletedEvent;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guretzki
 */
@Component
public class ProjectBrokerManagerImpl extends BasicManager implements ProjectBrokerManager, DeletableGroupData, Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String ATTACHEMENT_DIR_NAME = "projectbroker_attach";
    private CacheWrapper projectCache;
    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private BaseSecurity baseSecurtiy;
    @Autowired
    ProjectBrokerDao projectBrokerDao;
    @Autowired
    ProjectBrokerNodeConfiguration projectBrokerNodeConfiguration;

    protected ProjectBrokerManagerImpl() {
        log.debug("ProjectBrokerManagerImpl created");
    }

    /**
     * @param projectbroker_id
     * @return List of projects for certain project-broker
     */
    @Override
    public List<Project> getProjectListBy(final Long projectBrokerId) {
        log.debug("getProjectListBy for projectBroker=" + projectBrokerId);
        long rstart = 0;
        if (log.isDebugEnabled()) {
            rstart = System.currentTimeMillis();
        }
        final OLATResourceable projectBrokerOres = OresHelper.createOLATResourceableInstance(this.getClass(), projectBrokerId);
        final List<Project> projectList = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectBrokerOres, new SyncerCallback<List<Project>>() {
            @Override
            public List<Project> execute() {
                final ProjectBroker projectBroker = getOrLoadProjectBoker(projectBrokerId);
                return projectBroker.getProjects();
            }

        });

        if (log.isDebugEnabled()) {
            final long rstop = System.currentTimeMillis();
            log.debug("time to fetch project with projectbroker_id " + projectBrokerId + " :" + (rstop - rstart), null);
        }
        return projectList;
    }

    @Override
    public ProjectBroker createAndSaveProjectBroker() {
        return projectBrokerDao.createAndSaveProjectBroker();
    }

    @Override
    public Project createAndSaveProjectFor(final String title, final String description, final Long projectBrokerId, final BusinessGroup projectGroup) {
        final OLATResourceable projectBrokerOres = OresHelper.createOLATResourceableInstance(this.getClass(), projectBrokerId);
        final Project project = new ProjectImpl(title, description, projectGroup, getProjectBroker(projectBrokerId));
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectBrokerOres, new SyncerExecutor() {
            @Override
            public void execute() {
                projectBrokerDao.saveProject(project);
                final ProjectBroker projectBroker = getOrLoadProjectBoker(projectBrokerId);
                projectBroker.getProjects().add(project);
                projectCache.update(projectBrokerId.toString(), projectBroker);
            }
        });
        return project;
    }

    @Override
    public int getSelectedPlaces(final Project project) {
        return baseSecurtiy.countIdentitiesOfSecurityGroup(project.getProjectParticipantGroup())
                + baseSecurtiy.countIdentitiesOfSecurityGroup(project.getCandidateGroup());
    }

    @Override
    public void updateProject(final Project project) {
        final Long projectBrokerId = project.getProjectBroker().getKey();
        final OLATResourceable projectBrokerOres = OresHelper.createOLATResourceableInstance(this.getClass(), projectBrokerId);
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectBrokerOres, new SyncerExecutor() {
            @Override
            public void execute() {
                updateProjectAndInvalidateCache(project);
            }
        });
    }

    @Override
    public boolean existsProject(final Long projectKey) {
        return projectBrokerDao.findProject(projectKey) != null;
    }

    @Override
    public boolean enrollProjectParticipant(final Identity identity, final Project project, final ProjectBrokerModuleConfiguration moduleConfig,
            final int nbrSelectedProjects, final boolean isParticipantInAnyProject) {
        final OLATResourceable projectOres = OresHelper.createOLATResourceableInstance(Project.class, project.getKey());
        log.debug("enrollProjectParticipant: start identity=" + identity + "  project=" + project);
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
        final Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectOres, new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                if (existsProject(project.getKey())) {
                    // For cluster-safe : reload project object here another node might have changed this in the meantime
                    final Project reloadedProject = projectBrokerDao.loadProject(project);
                    log.debug("enrollProjectParticipant: project.getMaxMembers()=" + reloadedProject.getMaxMembers());
                    log.debug("enrollProjectParticipant: project.getSelectedPlaces()=" + reloadedProject.getSelectedPlaces());
                    if (ProjectBrokerManagerFactory.getProjectBrokerManager().canBeProjectSelectedBy(identity, reloadedProject, moduleConfig, nbrSelectedProjects,
                            isParticipantInAnyProject)) {
                        if (moduleConfig.isAcceptSelectionManually()) {
                            baseSecurtiy.addIdentityToSecurityGroup(identity, reloadedProject.getCandidateGroup());
                            log.info("Audit:ProjectBroker: Add as candidate identity=" + identity + " to project=" + reloadedProject);
                            if (log.isDebugEnabled()) {
                                log.debug("ProjectBroker: Add as candidate reloadedProject=" + reloadedProject + "  CandidateGroup="
                                        + reloadedProject.getCandidateGroup());
                            }
                        } else {
                            baseSecurtiy.addIdentityToSecurityGroup(identity, reloadedProject.getProjectParticipantGroup());
                            log.info("Audit:ProjectBroker: Add as participant identity=" + identity + " to project=" + reloadedProject);
                            if (log.isDebugEnabled()) {
                                log.debug("ProjectBroker: Add as participant reloadedProject=" + reloadedProject + "  ParticipantGroup="
                                        + reloadedProject.getProjectParticipantGroup());
                            }
                            if ((reloadedProject.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED)
                                    && (reloadedProject.getSelectedPlaces() >= reloadedProject.getMaxMembers())) {
                                reloadedProject.setState(Project.STATE_ASSIGNED);
                                updateProjectAndInvalidateCache(reloadedProject);
                            }
                        }
                        return Boolean.TRUE;
                    } else {
                        log.debug("ProjectBroker: project-group was full for identity=" + identity + " , project=" + reloadedProject);
                        return Boolean.FALSE;
                    }
                } else {
                    // project no longer exist
                    return Boolean.FALSE;
                }
            }
        });// end of doInSync
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
        return result.booleanValue();
    }

    @Override
    public boolean cancelProjectEnrollmentOf(final Identity identity, final Project project, final ProjectBrokerModuleConfiguration moduleConfig) {
        final OLATResourceable projectOres = OresHelper.createOLATResourceableInstance(Project.class, project.getKey());
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "beforeDoInSync");
        final Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectOres, new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                if (existsProject(project.getKey())) {
                    // For cluster-safe : reload project object here another node might have changed this in the meantime
                    final Project reloadedProject = projectBrokerDao.loadProject(project);
                    // User can only cancel enrollment, when state is 'NOT_ASSIGNED'
                    if (canBeCancelEnrollmentBy(identity, project, moduleConfig)) {
                        baseSecurtiy.removeIdentityFromSecurityGroup(identity, reloadedProject.getProjectParticipantGroup());
                        baseSecurtiy.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
                        log.info("Audit:ProjectBroker: Remove (as participant or waitinglist) identity=" + identity + " from project=" + project);
                        if (log.isDebugEnabled()) {
                            log.debug("ProjectBroker: Remove as participant reloadedProject=" + reloadedProject + "  ParticipantGroup="
                                    + reloadedProject.getProjectParticipantGroup() + "  CandidateGroup=" + reloadedProject.getCandidateGroup());
                        }
                        if ((reloadedProject.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) && (reloadedProject.getSelectedPlaces() < reloadedProject.getMaxMembers())) {
                            reloadedProject.setState(Project.STATE_NOT_ASSIGNED);
                            updateProjectAndInvalidateCache(reloadedProject);
                        }
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } else {
                    // project no longer exist
                    return Boolean.FALSE;
                }
            }
        });// end of doInSync
        Codepoint.codepoint(ProjectBrokerManagerImpl.class, "afterDoInSync");
        return result.booleanValue();
    }

    /**
     * Delete a project and delete project-groups related to this project. This method is cluster-save.
     * 
     */
    @Override
    public void deleteProject(final Project project, final boolean deleteGroup, final CourseEnvironment courseEnv, final CourseNode cNode) {
        log.debug("start deleteProject project=" + project);
        final Long projectBrokerId = project.getProjectBroker().getKey();
        final OLATResourceable projectBrokerOres = OresHelper.createOLATResourceableInstance(this.getClass(), projectBrokerId);
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectBrokerOres, new SyncerExecutor() {
            @Override
            public void execute() {
                final Project reloadedProject = projectBrokerDao.loadProject(project);
                // delete first candidate-group, project-group will be deleted after deleting project
                final SecurityGroup candidateGroup = reloadedProject.getCandidateGroup();
                if ((courseEnv != null) && (cNode != null)) {
                    deleteAllAttachmentFilesOfProject(reloadedProject, courseEnv, cNode);
                    deleteAllDropboxFilesOfProject(reloadedProject, courseEnv, cNode);
                    deleteAllReturnboxFilesOfProject(reloadedProject, courseEnv, cNode);
                }
                projectBrokerDao.deleteProject(reloadedProject);
                log.info("deleteSecurityGroup(project.getCandidateGroup())=" + candidateGroup.getKey());
                baseSecurtiy.deleteSecurityGroup(candidateGroup);
                // invalide with removing from cache
                projectCache.remove(projectBrokerId.toString());
            }
        });
        if (deleteGroup) {
            log.debug("start deleteProjectGroupFor project=" + project);
            ProjectBrokerManagerFactory.getProjectGroupManager().deleteProjectGroupFor(project);
        }
        log.debug("DONE deleteProjectGroupFor project=" + project);
    }

    @Override
    public int getNbrSelectedProjects(final Identity identity, final List<Project> projectList) {
        int selectedCounter = 0;
        for (final Iterator iterator = projectList.iterator(); iterator.hasNext();) {
            final Project project = (Project) iterator.next();
            if (baseSecurtiy.isIdentityInSecurityGroup(identity, project.getProjectParticipantGroup())
                    || baseSecurtiy.isIdentityInSecurityGroup(identity, project.getCandidateGroup())) {
                selectedCounter++;
            }
        }
        return selectedCounter;
    }

    /**
     * return true, when the project can be selected by the user.
     * 
     */
    @Override
    public boolean canBeProjectSelectedBy(final Identity identity, final Project project, final ProjectBrokerModuleConfiguration moduleConfig,
            final int nbrSelectedProjects, final boolean isParticipantInAnyProject) {
        log.debug("canBeSelectedBy: identity=" + identity + "  project=" + project);
        // 1. check if already enrolled
        if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectParticipant(identity, project)
                || ProjectBrokerManagerFactory.getProjectGroupManager().isProjectCandidate(identity, project)) {
            log.debug("canBeSelectedBy: return false because identity is already enrolled");
            return false;
        }
        // 2. check number of max project members
        final int projectMembers = baseSecurtiy.countIdentitiesOfSecurityGroup(project.getProjectParticipantGroup())
                + baseSecurtiy.countIdentitiesOfSecurityGroup(project.getCandidateGroup());
        if ((project.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) && (projectMembers >= project.getMaxMembers())) {
            log.debug("canBeSelectedBy: return false because projectMembers >= getMaxMembers()");
            return false;
        }
        // 3. number of selected topic per user
        final int nbrOfParticipantsPerTopicValue = moduleConfig.getNbrParticipantsPerTopic();
        if ((nbrOfParticipantsPerTopicValue != ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) && (nbrSelectedProjects >= nbrOfParticipantsPerTopicValue)) {
            log.debug("canBeSelectedBy: return false because number of selected topic per user is " + nbrOfParticipantsPerTopicValue);
            return false;
        }
        // 4. accept is done manually
        if (moduleConfig.isAcceptSelectionManually()) {
            // 4.1 and project-state is assigned
            if (project.getState().equals(Project.STATE_ASSIGNED)) {
                log.debug("canBeSelectedBy: return false because accept is done manually and project-state is assigned, project.getState()=" + project.getState());
                return false;
            }
            // 4.2. and user is already assigned in another project
            if (moduleConfig.isAcceptSelectionManually() && moduleConfig.isAutoSignOut() && isParticipantInAnyProject) {
                log.debug("canBeSelectedBy: return false because accept is done manually and user is already participant in another project");
                return false;
            }
        }
        // 5. date for enrollment ok
        if (!isEnrollmentDateOk(project, moduleConfig)) {
            log.debug("canBeSelectedBy: return false because enrollment date not valid =" + project.getProjectEvent(EventType.ENROLLMENT_EVENT));
            return false;
        }
        log.debug("canBeSelectedBy: return true");
        return true;
    }

    @Override
    public boolean canBeCancelEnrollmentBy(final Identity identity, final Project project, final ProjectBrokerModuleConfiguration moduleConfig) {
        // 6. date for enrollemnt ok
        if (!isEnrollmentDateOk(project, moduleConfig)) {
            return false;
        }
        if (moduleConfig.isAcceptSelectionManually()) {
            // could only cancel enrollment, when projectleader did not accept yet
            return ProjectBrokerManagerFactory.getProjectGroupManager().isProjectCandidate(identity, project) && !project.getState().equals(Project.STATE_ASSIGNED);
        } else {
            // could always cancel enrollment
            return ProjectBrokerManagerFactory.getProjectGroupManager().isProjectParticipant(identity, project);
        }
    }

    @Override
    public void signOutFromAllCandidateLists(final List<Identity> chosenIdentities, final Long projectBrokerId) {
        final OLATResourceable projectBrokerOres = OresHelper.createOLATResourceableInstance(this.getClass(), projectBrokerId);
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectBrokerOres, new SyncerExecutor() {
            @Override
            public void execute() {
                final ProjectBroker projectBroker = getOrLoadProjectBoker(projectBrokerId);
                for (final Project project : projectBroker.getProjects()) {
                    // loop over all identities
                    for (final Identity identity : chosenIdentities) {
                        baseSecurtiy.removeIdentityFromSecurityGroup(identity, project.getCandidateGroup());
                        log.info("Audit:ProjectBroker: AutoSignOut: identity=" + identity + " from project=" + project);
                    }
                }
            }
        });
    }

    @Override
    public String getStateFor(final Project project, final Identity identity, final ProjectBrokerModuleConfiguration moduleConfig) {
        if (moduleConfig.isAcceptSelectionManually()) {
            // Accept manually : unterscheiden Betreuer | Teilnehmer
            if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(identity, project)) {
                // State Betreuer : Teilnehmer prüfen | Teilnemher akzeptiert
                if (project.getState().equals(Project.STATE_ASSIGNED)) {
                    return Project.STATE_ASSIGNED_ACCOUNT_MANAGER;
                } else {
                    if (baseSecurtiy.countIdentitiesOfSecurityGroup(project.getCandidateGroup()) > 0) {
                        return Project.STATE_NOT_ASSIGNED_ACCOUNT_MANAGER;
                    } else {
                        return Project.STATE_NOT_ASSIGNED_ACCOUNT_MANAGER_NO_CANDIDATE;
                    }
                }
            } else {
                // State Teilnehmer : prov. eingeschrieben | definitiv eingeschrieben | belegt | frei
                if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectParticipant(identity, project)) {
                    return Project.STATE_FINAL_ENROLLED;
                } else if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectCandidate(identity, project)) {
                    return Project.STATE_PROV_ENROLLED;
                } else {
                    if (((project.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) && (project.getSelectedPlaces() >= project.getMaxMembers()))
                            || project.getState().equals(Project.STATE_ASSIGNED)) {
                        return Project.STATE_COMPLETE;
                    } else {
                        return Project.STATE_NOT_ASSIGNED;
                    }
                }
            }
        } else {
            // Accept automatically => State : frei | belegt | eingeschrieben
            if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectParticipant(identity, project)) {
                return Project.STATE_ENROLLED;
            } else {
                if ((project.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) && (project.getSelectedPlaces() >= project.getMaxMembers())) {
                    return Project.STATE_COMPLETE;
                } else {
                    return Project.STATE_NOT_ASSIGNED;
                }
            }
        }
    }

    @Override
    public void deleteProjectBroker(final Long projectBrokerId, final CourseEnvironment courseEnvironment, final CourseNode courseNode) {
        log.debug("Start deleting projectBrokerId=" + projectBrokerId);
        final ProjectBroker projectBroker = getOrLoadProjectBoker(projectBrokerId);
        // delete all projects of a project-broker
        final List<Project> deleteProjectList = new ArrayList<Project>();
        deleteProjectList.addAll(projectBroker.getProjects());
        for (final Iterator iterator = deleteProjectList.iterator(); iterator.hasNext();) {
            final Project project = (Project) iterator.next();
            deleteProject(project, true, courseEnvironment, courseNode);
            log.info("Audit:ProjectBroker: Deleted project=" + project);
        }
        log.debug("All projects are deleted for ProjectBroker=" + projectBroker);
        ProjectBrokerManagerFactory.getProjectGroupManager().deleteAccountManagerGroup(courseEnvironment.getCoursePropertyManager(), courseNode);
        projectBrokerDao.deleteProjectBroker(projectBroker);
        // invalide with removing from cache
        projectCache.remove(projectBrokerId.toString());
        log.info("Audit:ProjectBroker: Deleted ProjectBroker=" + projectBroker);
    }

    @Override
    public void saveAttachedFile(final Project project, final String fileName, final VFSLeaf uploadedItem, final CourseEnvironment courseEnv, final CourseNode cNode) {
        log.debug("saveAttachedFile file-name=" + uploadedItem.getName());
        final OlatRootFolderImpl uploadVFSContainer = new OlatRootFolderImpl(getAttamchmentRelativeRootPath(project, courseEnv, cNode), null);
        log.debug("saveAttachedFile uploadVFSContainer.relPath=" + uploadVFSContainer.getRelPath());
        // only one attachment, delete other file
        for (final Iterator<VFSItem> iterator = uploadVFSContainer.getItems().iterator(); iterator.hasNext();) {
            final VFSItem item = iterator.next();
            // Project.getAttachmentFileName is the previous file-name, will not be deleted; student could have open detail-project page with previous attachemnt-link
            if (!item.getName().equals(project.getAttachmentFileName())) {
                item.delete();
            }
        }
        VFSLeaf newFile = (VFSLeaf) uploadVFSContainer.resolve(fileName);
        if (newFile == null) {
            newFile = uploadVFSContainer.createChildLeaf(fileName);
        }
        final BufferedInputStream in = new BufferedInputStream(uploadedItem.getInputStream());
        final BufferedOutputStream out = new BufferedOutputStream(newFile.getOutputStream(false));
        boolean success = false;
        if (in != null) {
            success = FileUtils.copy(in, out);
        }
        FileUtils.closeSafely(in);
        FileUtils.closeSafely(out);
        log.debug("saveAttachedFile success=" + success);
    }

    @Override
    public boolean isCustomFieldValueValid(final String value, final String valueList) {
        final StringTokenizer tok = new StringTokenizer(valueList, ProjectBrokerManager.CUSTOMFIELD_LIST_DELIMITER);
        if (tok.hasMoreTokens()) {
            // It is a list of values => check if value is one of them
            while (tok.hasMoreTokens()) {
                if (tok.nextToken().equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        } else {
            // no value-list => value can be any value
            return true;
        }
    }

    @Override
    public String getAttamchmentRelativeRootPath(final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
        return getAttachmentBasePathRelToFolderRoot(courseEnv, cNode) + File.separator + project.getKey();
    }

    @Override
    public String getAttachmentBasePathRelToFolderRoot(final CourseEnvironment courseEnvironment, final CourseNode courseNode) {
        return courseEnvironment.getCourseBaseContainer().getRelPath() + File.separator + ATTACHEMENT_DIR_NAME + File.separator + courseNode.getIdent();
    }

    private void deleteAllAttachmentFilesOfProject(final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
        final VFSContainer attachmentDir = new OlatRootFolderImpl(getAttamchmentRelativeRootPath(project, courseEnv, cNode), null);
        attachmentDir.delete();
        log.debug("deleteAllAttachmentFilesOfProject path=" + attachmentDir);
    }

    private void deleteAllDropboxFilesOfProject(final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
        final VFSContainer dropboxDir = new OlatRootFolderImpl(getDropboxBasePathForProject(project, courseEnv, cNode), null);
        dropboxDir.delete();
        log.debug("deleteAllDropboxFilesOfProject path=" + dropboxDir);
    }

    private void deleteAllReturnboxFilesOfProject(final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
        final VFSContainer returnboxDir = new OlatRootFolderImpl(getReturnboxBasePathForProject(project, courseEnv, cNode), null);
        returnboxDir.delete();
        log.debug("deleteAllReturnboxFilesOfProject path=" + returnboxDir);
    }

    // /////////////////
    // Private Methods
    // /////////////////
    /**
     * Get ProjectBroker from cache, or load and put into cache.
     */
    private ProjectBroker getOrLoadProjectBoker(final Long projectBrokerId) {
        // 1. check if alreday a projectBroker is in the cache
        ProjectBroker projectBroker = (ProjectBroker) projectCache.get(projectBrokerId.toString());
        if (projectBroker == null) {
            log.debug("find no projectBroker in the cache => create a new one projectBrokerId=" + projectBrokerId);
            final List projectList = projectBrokerDao.findProjects(projectBrokerId);
            projectBroker = getProjectBroker(projectBrokerId);
            projectBroker.setProjects(projectList);
            projectCache.put(projectBrokerId.toString(), projectBroker);
        }
        return projectBroker;
    }

    @Override
    public ProjectBroker getProjectBroker(final Long projectBrokerId) {
        return projectBrokerDao.loadProjectBroker(projectBrokerId);
    }

    private boolean isEnrollmentDateOk(final Project project, final ProjectBrokerModuleConfiguration moduleConfig) {
        if (moduleConfig.isProjectEventEnabled(EventType.ENROLLMENT_EVENT)) {
            final ProjectEvent enrollmentEvent = project.getProjectEvent(EventType.ENROLLMENT_EVENT);
            final Date now = new Date();
            if (enrollmentEvent.getStartDate() != null) {
                if (now.before(enrollmentEvent.getStartDate())) {
                    return false;
                }
            }
            if (enrollmentEvent.getEndDate() != null) {
                if (now.after(enrollmentEvent.getEndDate())) {
                    return false;
                }
            }
            if ((enrollmentEvent.getStartDate() == null) && (enrollmentEvent.getEndDate() == null)) {
                // no enrollment date define => access ok
                return true;
            }
        }
        return true;
    }

    /**
     * return true, when identity is participant in any project of project-list.
     * 
     * @param identity
     * @param projectList
     * @return
     */
    @Override
    public boolean isParticipantInAnyProject(final Identity identity, final List<Project> projectList) {
        for (final Iterator iterator = projectList.iterator(); iterator.hasNext();) {
            final Project project = (Project) iterator.next();
            if (baseSecurtiy.isIdentityInSecurityGroup(identity, project.getProjectParticipantGroup())) {
                return true;
            }
        }
        return false;
    }

    // ////////////////////////////////////////
    // implements interface DeletableGroupData
    // ////////////////////////////////////////
    @Override
    public boolean deleteGroupDataFor(final BusinessGroup group) {
        log.debug("deleteAllProjectGroupEntiresFor started.. group=" + group);
        final List<Project> projectList = getProjectsWith(group);
        if (projectList.isEmpty()) {
            return false;
        }
        for (final Project project : projectList) {
            this.deleteProject(project, false, null, null); // no course-env, no course-node
            final ProjectBroker projectBroker = project.getProjectBroker();
            final OLATResourceableDeletedEvent delEv = new OLATResourceableDeletedEvent(projectBroker);
            CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, projectBroker);
            log.debug("deleteProjectWith: group=" + group + " , project=" + project);
        }
        return true;
    }

    @Override
    public DeletableReference checkIfReferenced(final BusinessGroup group, final Locale locale) {
        final StringBuilder buf = new StringBuilder();
        final List<Project> projectList = getProjectsWith(group);
        if (projectList.isEmpty()) {
            return DeletableReference.createNoDeletableReference();
        }
        buf.append(projectBrokerNodeConfiguration.getLinkText(locale));
        buf.append(":");

        for (final Project project : projectList) {
            buf.append(project.getTitle());
        }
        return DeletableReference.createDeletableReference(buf.toString());
    }

    @SuppressWarnings("unchecked")
    private List<Project> getProjectsWith(final BusinessGroup group) {
        return projectBrokerDao.findProjectsForGoup(group.getKey());
    }

    @Override
    public void setProjectState(final Project project, final String state) {
        final Long projectBrokerId = project.getProjectBroker().getKey();
        final OLATResourceable projectBrokerOres = OresHelper.createOLATResourceableInstance(this.getClass(), projectBrokerId);
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(projectBrokerOres, new SyncerExecutor() {
            @Override
            public void execute() {
                // For cluster-safe : reload project object here another node might have changed this in the meantime
                final Project reloadedProject = projectBrokerDao.loadProject(project);
                reloadedProject.setState(state);
                updateProjectAndInvalidateCache(reloadedProject);
            }
        });
    }

    @Override
    public Long getProjectBrokerId(final CoursePropertyManager cpm, final CourseNode courseNode) {
        final PropertyImpl projectBrokerKeyProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_PROJECTBROKER_KEY);
        // Check if forum-property exist
        if (projectBrokerKeyProperty != null) {
            final Long projectBrokerId = projectBrokerKeyProperty.getLongValue();
            return projectBrokerId;
        }
        return null;
    }

    @Override
    public void saveProjectBrokerId(final Long projectBrokerId, final CoursePropertyManager cpm, final CourseNode courseNode) {
        final PropertyImpl projectBrokerKeyProperty = cpm.createCourseNodePropertyInstance(courseNode, null, null, ProjectBrokerCourseNode.CONF_PROJECTBROKER_KEY, null,
                projectBrokerId, null, null);
        cpm.saveProperty(projectBrokerKeyProperty);
    }

    @Override
    public boolean existProjectName(final Long projectBrokerId, final String newProjectTitle) {
        final List<Project> projectList = projectBrokerDao.findProjects(projectBrokerId, newProjectTitle);
        log.debug("existProjectName projectList.size=" + projectList.size());
        return !projectList.isEmpty();
    }

    @Override
    public List<Project> getProjectsOf(final Identity identity, final Long projectBrokerId) {
        final List<Project> myProjects = new ArrayList<Project>();
        final List<Project> allProjects = getProjectListBy(projectBrokerId);
        // TODO: for better performance should be done with sql query instead of a loop
        for (final Iterator iterator = allProjects.iterator(); iterator.hasNext();) {
            final Project project = (Project) iterator.next();
            if (baseSecurtiy.isIdentityInSecurityGroup(identity, project.getProjectParticipantGroup())) {
                myProjects.add(project);
            }
        }
        return myProjects;
    }

    @Override
    public Project getProject(final Long resourceableId) {
        return projectBrokerDao.findProject(resourceableId);
    }

    @Override
    public List<Project> getCoachedProjectsOf(final Identity identity, final Long projectBrokerId) {
        final List<Project> myProjects = new ArrayList<Project>();
        final List<Project> allProjects = getProjectListBy(projectBrokerId);
        // TODO: for better performance should be done with sql query instead of a loop
        for (final Iterator iterator = allProjects.iterator(); iterator.hasNext();) {
            final Project project = (Project) iterator.next();
            if (baseSecurtiy.isIdentityInSecurityGroup(identity, project.getProjectLeaderGroup())) {
                myProjects.add(project);
            }
        }
        return myProjects;
    }

    private void updateProjectAndInvalidateCache(final Project project) {
        // avoid hibernate exception : object with same identifier already exist in session.
        // reload object from db, because project is a detached object but could be already in hibernate session
        final Project reloadedProject = projectBrokerDao.loadProject(project);
        // set all value on reloadedProject with values from updated project
        reloadedProject.setTitle(project.getTitle());
        reloadedProject.setState(project.getState());
        for (final Project.EventType eventType : Project.EventType.values()) {
            reloadedProject.setProjectEvent(project.getProjectEvent(eventType));
        }
        reloadedProject.setMaxMembers(project.getMaxMembers());
        reloadedProject.setMailNotificationEnabled(project.isMailNotificationEnabled());
        reloadedProject.setDescription(project.getDescription());
        for (int index = 0; index < project.getCustomFieldSize(); index++) {
            reloadedProject.setCustomFieldValue(index, project.getCustomFieldValue(index));
        }
        reloadedProject.setAttachedFileName(project.getAttachmentFileName());
        projectBrokerDao.updateProject(reloadedProject);
        // invalide with removing from cache
        projectCache.remove(project.getProjectBroker().getKey().toString());
    }

    /**
     * @see org.olat.system.commons.configuration.Initializable#init()
     */
    @Override
    @PostConstruct
    public void init() {
        // cache name should not be too long e.g. 'projectbroker' is too long, use 'pb' instead.
        projectCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(ProjectBrokerManagerImpl.class, "pb");
        businessGroupService.registerDeletableGroupDataListener(this);

    }

    /**
     * Return dropbox base-path. e.g. course/<COURSE_ID>/dropbox/<NODE_id> To have the path for certain user you must call method 'getRelativeDropBoxFilePath'
     * 
     * @param project
     * @param courseEnv
     * @param cNode
     * @return
     */
    @Override
    public String getDropboxBasePathForProject(final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
        return getDropboxRootFolder(courseEnv, cNode) + File.separator + project.getKey();
    }

    @Override
    public String getDropboxPathForProjectAndIdentity(Project project, Identity identity, CourseEnvironment courseEnvironment, CourseNode node) {
        return getDropboxBasePathForProject(project, courseEnvironment, node) + File.separator + identity.getName();
    }

    @Override
    public String getDropboxRootFolder(CourseEnvironment courseEnvironment, CourseNode node) {
        return courseEnvironment.getCourseBaseContainer().getRelPath() + File.separator + Dropbox_EBL.DROPBOX_DIR_NAME + File.separator + node.getIdent();
    }

    /**
     * Return returnbox base-path. e.g. course/<COURSE_ID>/returnbox/<NODE_id> To have the path for certain user you must call method 'getReturnboxPathFor'
     * 
     * @param project
     * @param courseEnv
     * @param cNode
     * @return Returnbox path relative to folder root.
     */
    public String getReturnboxBasePathForProject(final Project project, final CourseEnvironment courseEnv, final CourseNode node) {
        return getReturnboxRootFolder(courseEnv, node) + File.separator + project.getKey();
    }

    @Override
    public String getReturnboxPathForProjectAndIdentity(Project project, CourseEnvironment courseEnv, CourseNode node, Identity identity) {
        return getReturnboxBasePathForProject(project, courseEnv, node) + File.separator + identity.getName();
    }

    @Override
    public String getReturnboxRootFolder(CourseEnvironment courseEnvironment, CourseNode node) {
        return courseEnvironment.getCourseBaseContainer().getRelPath() + File.separator + Returnbox_EBL.RETURNBOX_DIR_NAME + File.separator + node.getIdent();
    }

    public boolean isDropboxAccessible(final Project project, final ProjectBrokerModuleConfiguration moduleConfig) {
        if (moduleConfig.isProjectEventEnabled(EventType.HANDOUT_EVENT)) {
            final ProjectEvent handoutEvent = project.getProjectEvent(EventType.HANDOUT_EVENT);
            final Date now = new Date();
            if (handoutEvent.getStartDate() != null) {
                if (now.before(handoutEvent.getStartDate())) {
                    return false;
                }
            }
            if (handoutEvent.getEndDate() != null) {
                if (now.after(handoutEvent.getEndDate())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<Project> findProjectsForGoup(Long groupId) {
        return projectBrokerDao.findProjectsForGoup(groupId);
    }

    @Override
    public List<Identity> getTopicTutors(Project project, UserCourseEnvironment userCourseEnv) {
        final List<Identity> identities = new ArrayList<Identity>();
        Set<Identity> identitiesSet = new HashSet<Identity>();
        OLATResourceable course = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();
        identitiesSet.addAll(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseOwners(course));
        identitiesSet.addAll(project.getProjectLeaders());
        identities.addAll(identitiesSet);
        return identities;
    }

}
