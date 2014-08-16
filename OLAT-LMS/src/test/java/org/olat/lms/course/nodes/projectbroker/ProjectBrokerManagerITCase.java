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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroupImpl;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectBroker;
import org.olat.data.course.nodes.projectbroker.ProjectEvent;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.BusinessGroupImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupService;
import org.olat.presentation.course.nodes.projectbroker.ProjectListTableModel;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * @author Christian Guretzki
 */
public class ProjectBrokerManagerITCase extends OlatTestCase {
    //
    private static final Logger log = LoggerHelper.getLogger();

    /*
     * ::Test Setup::
     */
    private static Identity id1 = null;
    private static Identity id2 = null;
    private BusinessGroupService businessGroupService;
    private static Long resourceableId = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        businessGroupService = applicationContext.getBean(BusinessGroupService.class);
        System.out.println("ProjectBrokerManagerITCase.setUp start...");
        try {
            id1 = JunitTestHelper.createAndPersistIdentityAsUser("id1");
            id2 = JunitTestHelper.createAndPersistIdentityAsUser("id2");

            if (resourceableId == null) {
                final RepositoryEntry repositoryEntry = JunitTestHelper.deployDemoCourse();
                resourceableId = repositoryEntry.getOlatResource().getResourceableId();
                System.out.println("Demo course imported - resourceableId: " + resourceableId);
            }
            DBFactory.getInstance().closeSession();

            System.out.println("ProjectBrokerManagerITCase.setUp finished");
        } catch (final Exception e) {
            System.out.println("ProjectBrokerManagerITCase.setUp Exception=" + e.getMessage());
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
	 * 
	 */
    @Test
    public void testCreateListDeleteProjects() throws Exception {
        System.out.println("testCreateListDeleteProjects: start...");
        // create ProjectBroker A + B
        final ProjectBroker projectBrokerA = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBrokerA = projectBrokerA.getKey();
        final ProjectBroker projectBrokerB = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBrokerB = projectBrokerB.getKey();
        // add project to ProjectBroker A
        createProject("thema A1", id1, idProjectBrokerA, resourceableId);
        createProject("thema A2", id1, idProjectBrokerA, resourceableId);
        // add project to ProjectBroker B
        createProject("thema B1", id1, idProjectBrokerB, resourceableId);
        createProject("thema B2", id1, idProjectBrokerB, resourceableId);

        DBFactory.getInstance().closeSession();
        // get project list and check content
        List<Project> projectListA = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerA);
        List<Project> projectListB = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerB);
        assertEquals("Wrong projectList.size for project-broker A", 2, projectListA.size());
        assertEquals("Wrong projectList.size for project-broker B", 2, projectListB.size());
        assertTrue("Wrong thema in project list A, title must start with 'thema A'", projectListA.get(0).getTitle().startsWith("thema A"));
        assertTrue("Wrong thema in project list A, title must start with 'thema A'", projectListA.get(1).getTitle().startsWith("thema A"));
        assertTrue("Wrong thema in project list B, title must start with 'thema B'", projectListB.get(0).getTitle().startsWith("thema B"));
        assertTrue("Wrong thema in project list B, title must start with 'thema B'", projectListB.get(1).getTitle().startsWith("thema B"));
        if (projectListA.get(0).getTitle().equals("thema A1")) {
            assertTrue("Wrong thema in project list A, title must be 'thema A2'", projectListA.get(1).getTitle().equals("thema A2"));
        } else if (projectListA.get(0).getTitle().equals("thema A2")) {
            assertTrue("Wrong thema in project list A, title must be 'thema A1'", projectListA.get(1).getTitle().equals("thema A1"));
        }
        if (projectListB.get(0).getTitle().equals("thema B1")) {
            assertTrue("Wrong thema in project list B, title must be 'thema B2'", projectListB.get(1).getTitle().equals("thema B2"));
        } else if (projectListB.get(0).getTitle().equals("thema B2")) {
            assertTrue("Wrong thema in project list B, title must be 'thema B1'", projectListB.get(1).getTitle().equals("thema B1"));
        }

        // delete project
        final long candiadteGroupKey = projectListA.get(0).getCandidateGroup().getKey();
        final long projectGroupKey = projectListA.get(0).getProjectGroup().getKey();
        assertNotNull("CandidateGroup does not exist before delete project", DBFactory.getInstance().findObject(SecurityGroupImpl.class, candiadteGroupKey));
        assertNotNull("ProjectGroup does not exist before delete project", DBFactory.getInstance().findObject(BusinessGroupImpl.class, projectGroupKey));
        ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProject(projectListA.get(0), true, null, null);
        assertNull("CandidateGroup still exists after delete project", DBFactory.getInstance().findObject(SecurityGroupImpl.class, candiadteGroupKey));
        assertNull("ProjectGroup still exists after delete project", DBFactory.getInstance().findObject(BusinessGroupImpl.class, projectGroupKey));

        // get project list and check content
        projectListA = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerA);
        projectListB = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerB);
        assertEquals("Wrong projectList.size for project-broker A after delete 'thema A1'", 1, projectListA.size());
        assertEquals("Wrong projectList.size for project-broker B after delete 'thema A1'", 2, projectListB.size());
        // delete project
        ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProject(projectListB.get(1), true, null, null);
        // get project list and check content
        projectListA = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerA);
        projectListB = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerB);
        assertEquals("Wrong projectList.size for project-broker A after delete 'thema B2'", 1, projectListA.size());
        assertEquals("Wrong projectList.size for project-broker B after delete 'thema B2'", 1, projectListB.size());

        // delete project
        ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProject(projectListA.get(0), true, null, null);
        projectListA = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerA);
        projectListB = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerB);
        System.out.println("testCreateListDeleteProjects: projectListA=" + projectListA);
        assertEquals("Wrong projectList.size for project-broker A after delete all thema", 0, projectListA.size());
        assertEquals("Wrong projectList.size for project-broker B after delete all thema", 1, projectListB.size());
        // cleanup
        System.out.println("testCreateListDeleteProjects: done");
    }

    @Test
    public void testPerformanceGetProjectList() throws Exception {
        System.out.println("testPerformanceGetProjectList: start...");
        final int FIRST_ITERATION = 10;
        final int SECOND_ITERATION = 90;
        final int THIRD_ITERATION = 400;
        // create ProjectBroker C
        final ProjectBroker projectBrokerC = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBrokerC = projectBrokerC.getKey();
        DBFactory.getInstance().closeSession();
        for (int i = 0; i < FIRST_ITERATION; i++) {
            createProject("thema C1_" + i, id1, idProjectBrokerC, resourceableId);
        }
        DBFactory.getInstance().closeSession();
        long startTime = System.currentTimeMillis();
        List<Project> projectListC = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerC);
        long endTime = System.currentTimeMillis();
        assertEquals("Wrong projectList.size for project-broker C after first iteration", FIRST_ITERATION, projectListC.size());
        long duration = endTime - startTime;
        System.out.println("getProjectListBy takes " + duration + "ms with " + FIRST_ITERATION + " projects");

        for (int i = 0; i < SECOND_ITERATION; i++) {
            createProject("thema C1_" + i, id1, idProjectBrokerC, resourceableId);
        }
        DBFactory.getInstance().closeSession();
        startTime = System.currentTimeMillis();
        projectListC = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerC);
        endTime = System.currentTimeMillis();
        int numberOfProjects = FIRST_ITERATION + SECOND_ITERATION;
        assertEquals("Wrong projectList.size for project-broker C", numberOfProjects, projectListC.size());
        duration = endTime - startTime;
        System.out.println("getProjectListBy takes " + duration + "ms with " + numberOfProjects + " projects");

        for (int i = 0; i < THIRD_ITERATION; i++) {
            createProject("thema C1_" + i, id1, idProjectBrokerC, resourceableId);
        }
        DBFactory.getInstance().closeSession();
        startTime = System.currentTimeMillis();
        projectListC = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerC);
        endTime = System.currentTimeMillis();
        numberOfProjects = FIRST_ITERATION + SECOND_ITERATION + THIRD_ITERATION;
        assertEquals("Wrong projectList.size for project-broker C", numberOfProjects, projectListC.size());
        duration = endTime - startTime;
        System.out.println("getProjectListBy takes " + duration + "ms with " + numberOfProjects + " projects");
        // cleanup
        System.out.println("testPerformance: done");
    }

    @Test
    public void testPerformanceTableModel() throws Exception {
        final int ITERATION = 300;
        final int START_PAGE_INDEX = 100;
        final int PAGE_SIZE = 20;
        final PackageTranslator translator = new PackageTranslator(this.getClass().getPackage().getName(), Locale.GERMAN);

        final ProjectBroker projectBrokerD = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBrokerD = projectBrokerD.getKey();
        final ProjectBrokerModuleConfiguration moduleConfig = new ProjectBrokerModuleConfiguration(new ModuleConfiguration());

        for (int i = 0; i < ITERATION; i++) {
            createProject("thema D1_" + i, id1, idProjectBrokerD, resourceableId);
        }
        final List projectListD = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(idProjectBrokerD);
        final ProjectListTableModel tableModel = new ProjectListTableModel(projectListD, id1, translator, moduleConfig, 0, 0, 0, false);

        // loop over table like rendering loop
        final long startTime = System.currentTimeMillis();
        for (int row = START_PAGE_INDEX; row < START_PAGE_INDEX + PAGE_SIZE; row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                final Object element = tableModel.getValueAt(row, col);
            }
        }
        final long endTime = System.currentTimeMillis();
        final long duration = endTime - startTime;
        System.out.println("tableModel.getValueAt(row, col) for " + PAGE_SIZE + "elements (of " + ITERATION + ") takes " + duration + "ms with " + ITERATION
                + " projects");
        // cleanup
    }

    @Test
    public void testIsProjectManager() throws Exception {
        final ProjectBroker projectBrokerD = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBrokerD = projectBrokerD.getKey();

        final Project testProjectA = createProject("thema A", id1, idProjectBrokerD, resourceableId);
        final List<Identity> projectManagerList = new ArrayList<Identity>();
        projectManagerList.add(id1);
        final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
        final Project testProjectB = createProject("thema B", id2, idProjectBrokerD, resourceableId);
        // check project leader in ProjectA
        assertTrue("Must be project-leader of project A", ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(id1, testProjectA));
        assertFalse("Can not be project leader of project B", ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(id1, testProjectB));
        assertTrue("Must be project-leader of project A", ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(id2, testProjectB));

        businessGroupService.removeOwnersAndFireEvent(id1, projectManagerList, testProjectA.getProjectGroup(), flags);
        // check no project leader anymore
        assertFalse("Can not be project leader of project A", ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(id1, testProjectA));
        assertFalse("Can not be project leader of project B", ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(id1, testProjectB));
        // cleanup
    }

    @Test
    public void testAcceptManuall() throws Exception {
        //
    }

    @Test
    public void testAcceptAutomaticly() throws Exception {
        //
    }

    @Test
    public void testExistsProject() throws Exception {
        // 1. test project does not exists
        assertFalse("Wrong return value true, project does not exist", ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject(39927492743L));
        // 2. test project exists
        final ProjectBroker projectBrokerD = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBrokerD = projectBrokerD.getKey();
        final Project testProjectA = createProject("thema existsProject-Test", id1, idProjectBrokerD, resourceableId);
        DBFactory.getInstance().closeSession();
        assertTrue("Wrong return value false, project exists", ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject(testProjectA.getKey()));
    }

    @Test
    public void testUpdateProject() throws Exception {
        final ProjectBroker projectBroker = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
        final Long idProjectBroker = projectBroker.getKey();
        final Project testProjectA = createProject("updateTest", id1, idProjectBroker, resourceableId);
        DBFactory.getInstance().closeSession();
        // testProjectA is now a detached-object
        // Update 1
        final String updateTitle = "thema updateProject-Test update1";
        testProjectA.setTitle(updateTitle);
        final String updateDescription = "description update1";
        testProjectA.setDescription(updateDescription);
        final String updateState = "state update1";
        testProjectA.setState(updateState);
        ProjectBrokerManagerFactory.getProjectBrokerManager().updateProject(testProjectA);
        DBFactory.getInstance().closeSession();
        // testProjectA is now a detached-object again
        Project reloadedProject = (Project) DBFactory.getInstance().loadObject(testProjectA, true);
        assertEquals("Wrong updated title 1", updateTitle, reloadedProject.getTitle());
        // Update 2
        final String updateTitle2 = "thema updateProject-Test update2";
        testProjectA.setTitle(updateTitle2);
        final int updateMaxMembers = 3;
        testProjectA.setMaxMembers(updateMaxMembers);
        final String updateAttachmentFileName = "attachmentFile.txt";
        testProjectA.setAttachedFileName(updateAttachmentFileName);
        final boolean updateMailNotification = Boolean.TRUE;
        testProjectA.setMailNotificationEnabled(updateMailNotification);
        final String updateCustomField0 = "CustomField0";
        testProjectA.setCustomFieldValue(0, updateCustomField0);
        final String updateCustomField1 = "CustomField1";
        testProjectA.setCustomFieldValue(1, updateCustomField1);
        ProjectBrokerManagerFactory.getProjectBrokerManager().updateProject(testProjectA);
        DBFactory.getInstance().closeSession();
        // Update 3
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2010, 11, 15, 15, 30, 45);
        final Date startDate = cal.getTime();
        cal.clear();
        cal.set(2010, 11, 20, 15, 30, 45);
        final Date endDate = cal.getTime();
        final ProjectEvent projectEventEnroll = new ProjectEvent(Project.EventType.ENROLLMENT_EVENT, startDate, endDate);
        testProjectA.setProjectEvent(projectEventEnroll);
        final ProjectEvent projectEventHandout = new ProjectEvent(Project.EventType.HANDOUT_EVENT, startDate, endDate);
        testProjectA.setProjectEvent(projectEventHandout);
        DBFactory.getInstance().closeSession();
        reloadedProject = (Project) DBFactory.getInstance().loadObject(testProjectA, true);
        assertEquals("Wrong updated title 2", updateTitle2, reloadedProject.getTitle());
        assertEquals("Wrong description", updateDescription, reloadedProject.getDescription());
        assertEquals("Wrong state", updateState, reloadedProject.getState());
        assertEquals("Wrong maxMembers", updateMaxMembers, reloadedProject.getMaxMembers());
        assertEquals("Wrong AttachmentFileName", updateAttachmentFileName, reloadedProject.getAttachmentFileName());
        assertEquals("Wrong MailNotification", updateMailNotification, reloadedProject.isMailNotificationEnabled());
        assertEquals("Wrong CustomField 0", updateCustomField0, reloadedProject.getCustomFieldValue(0));
        assertEquals("Wrong CustomField 1", updateCustomField1, reloadedProject.getCustomFieldValue(1));
        assertEquals("Wrong customField Size", 2, reloadedProject.getCustomFieldSize());
        assertEquals("Wrong event Type (Handout)", Project.EventType.HANDOUT_EVENT, reloadedProject.getProjectEvent(Project.EventType.HANDOUT_EVENT).getEventType());
        assertEquals("Wrong event start-date (Handout)", startDate.getTime(), reloadedProject.getProjectEvent(Project.EventType.HANDOUT_EVENT).getStartDate().getTime());
        assertEquals("Wrong event end-date (Handout)", endDate.getTime(), reloadedProject.getProjectEvent(Project.EventType.HANDOUT_EVENT).getEndDate().getTime());
        assertEquals("Wrong event Type (Enroll)", Project.EventType.ENROLLMENT_EVENT, reloadedProject.getProjectEvent(Project.EventType.ENROLLMENT_EVENT).getEventType());
        assertEquals("Wrong event start-date (Enroll)", startDate.getTime(), reloadedProject.getProjectEvent(Project.EventType.ENROLLMENT_EVENT).getStartDate().getTime());
        assertEquals("Wrong event end-date (Enroll)", endDate.getTime(), reloadedProject.getProjectEvent(Project.EventType.ENROLLMENT_EVENT).getEndDate().getTime());

    }

    private Project createProject(final String name, final Identity creator, final Long projectBrokerId, final Long courseId) {
        final BusinessGroup projectGroup = ProjectBrokerManagerFactory.getProjectGroupManager().createProjectGroupFor(projectBrokerId, creator, name + "_Group",
                name + "GroupDescription", courseId);
        final Project project = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectFor(name + "title", name + "description1", projectBrokerId,
                projectGroup);
        return project;
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        // TODO: Does not cleanup Demo-course because other Test which use Demo-Course too, will be have failures
        // ICourse course = CourseFactory.loadCourse(resourceableId);
        // CourseFactory.deleteCourse(course);
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

}
