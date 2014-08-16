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
package org.olat.data.course.nodes.projectbroker;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.commons.database.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * TODO: Class Description for ProjectBrokerDao
 * 
 * <P>
 * Initial Date: 13.07.2011 <br>
 * 
 * @author lavinia
 */
@Repository
public class ProjectBrokerDaoImpl implements ProjectBrokerDao {

    protected ProjectBrokerDaoImpl() {
    }

    @Autowired
    DB db;

    @Override
    public ProjectBroker createAndSaveProjectBroker() {
        final ProjectBroker projectBroker = new ProjectBrokerImpl();
        db.saveObject(projectBroker);
        return projectBroker;
    }

    @Override
    public List<Project> findProjects(final Long projectBrokerId) {
        final List projectList = db.find("select project from org.olat.data.course.nodes.projectbroker.ProjectImpl as project" + " where project.projectBroker.key = ?",
                projectBrokerId, Hibernate.LONG);
        return projectList;
    }

    @Override
    public List<Project> findProjectsForGoup(final Long groupKey) {
        final List<Project> projectList = db.find("select project from org.olat.data.course.nodes.projectbroker.ProjectImpl as project"
                + " where project.projectGroup.key = ?", groupKey, Hibernate.LONG);
        return projectList;
    }

    @Override
    public List<Project> findProjects(final Long projectBrokerId, String title) {
        return db.find("select project from org.olat.data.course.nodes.projectbroker.ProjectImpl as project" + " where project.projectBroker = ? and project.title = ?",
                new Object[] { projectBrokerId, title }, new Type[] { Hibernate.LONG, Hibernate.STRING });
    }

    @Override
    public Project findProject(final Long projectKey) {
        return (ProjectImpl) db.findObject(ProjectImpl.class, projectKey);
    }

    @Override
    public ProjectBroker loadProjectBroker(final Long projectBrokerId) {
        return (ProjectBroker) db.loadObject(ProjectBrokerImpl.class, projectBrokerId);
    }

    @Override
    public Project loadProject(final Project project) {
        return (Project) db.loadObject(project, true);
    }

    @Override
    public void saveProjectBroker(final ProjectBrokerImpl projectBroker) {
        db.saveObject(projectBroker);
    }

    @Override
    public void saveProject(final Project project) {
        db.saveObject(project);
    }

    @Override
    public void deleteProject(final Project project) {
        db.deleteObject(project);
    }

    @Override
    public void deleteProjectBroker(final ProjectBroker projectBroker) {
        final ProjectBroker reloadedProjectBroker = (ProjectBroker) db.loadObject(projectBroker, true);
        db.deleteObject(reloadedProjectBroker);
    }

    @Override
    public void updateProject(final Project reloadedProject) {
        db.updateObject(reloadedProject);
    }

}
