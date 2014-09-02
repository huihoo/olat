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
package org.olat.presentation.group;

import java.util.List;

import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.TreeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.course.nodes.projectbroker.ProjectController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Nov 15, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class BusinessGroupProjectControllerFactoryImpl implements BusinessGroupProjectControllerFactory {

    private final Project project;
    private final RepositoryEntry repositoryEntry;
    private final UserRequest userRequest;
    private final CourseNode projectBrokerCourseNode;
    private final NodeEvaluation nodeEvaluation;
    private final UserCourseEnvironment userCourseEnvironment;
    private final ICourse course;
    private final WindowControl windowControl;

    public BusinessGroupProjectControllerFactoryImpl(UserRequest userRequest, WindowControl windowControl, Project project) {
        this.userRequest = userRequest;
        this.windowControl = windowControl;
        this.project = project;
        this.repositoryEntry = getBusinessGroupService().getCourseRepositoryEntryForBusinessGroup(project.getProjectGroup());
        this.course = CourseFactory.loadCourse(repositoryEntry.getOlatResource().getResourceableId());
        this.projectBrokerCourseNode = getProjectBrokerCourseNode(course);
        this.userCourseEnvironment = getUserCourseEnvironment();
        this.nodeEvaluation = getNodeEvaluation();
    }

    private UserCourseEnvironment getUserCourseEnvironment() {
        IdentityEnvironment identityEnvironment = new IdentityEnvironment();
        identityEnvironment.setIdentity(userRequest.getIdentity());
        return new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
    }

    private NodeEvaluation getNodeEvaluation() {
        TreeEvaluation treeEval = new TreeEvaluation();
        CourseNode node = course.getRunStructure().getNode(projectBrokerCourseNode.getIdent());
        NodeEvaluation nodeEvaluation = node.eval(userCourseEnvironment.getConditionInterpreter(), treeEval, true);
        return nodeEvaluation;
    }

    private CourseNode getProjectBrokerCourseNode(ICourse course) {
        final CourseNode projectBrokerCourseNode = getProjectBrokerCourseNode(course, course.getRunStructure().getRootNode());
        if (projectBrokerCourseNode != null) {
            return projectBrokerCourseNode;
        }
        throw new AssertException("project broker course node not found");
    }

    private CourseNode getProjectBrokerCourseNode(final ICourse course, final CourseNode node) {
        if (isCorrectProjectBrokerNode(course, node)) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            final CourseNode projectBrokerCourseNode = getProjectBrokerCourseNode(course, (CourseNode) node.getChildAt(i));
            if (projectBrokerCourseNode != null) {
                return projectBrokerCourseNode;
            }
        }
        return null;
    }

    private boolean isCorrectProjectBrokerNode(ICourse course, CourseNode node) {
        if (node instanceof ProjectBrokerCourseNode) {
            List<Project> projects = getProjects(course, (ProjectBrokerCourseNode) node);
            for (Project project : projects) {
                if (project.getKey().equals(this.project.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Project> getProjects(ICourse course, ProjectBrokerCourseNode projectBrokerCourseNode) {
        Long projectBrokerId = getProjectBrokerId(course, projectBrokerCourseNode);
        List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
        return projects;
    }

    private Long getProjectBrokerId(ICourse course, ProjectBrokerCourseNode projectBrokerCourseNode) {
        CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        Long projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, projectBrokerCourseNode);
        return projectBrokerId;
    }

    private BusinessGroupService getBusinessGroupService() {
        return CoreSpringFactory.getBean(BusinessGroupService.class);
    }

    @Override
    public ProjectController getProjectController() {
        ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration = new ProjectBrokerModuleConfiguration(projectBrokerCourseNode.getModuleConfiguration());
        return new ProjectController(userRequest, windowControl, userCourseEnvironment, nodeEvaluation, project, false, projectBrokerModuleConfiguration, false);
    }
}
