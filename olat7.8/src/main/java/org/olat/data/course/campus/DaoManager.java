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
package org.olat.data.course.campus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.system.commons.collections.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class serves as a facade to a set of data access objects.
 * 
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Repository
public class DaoManager {
    @Autowired
    private CourseDao courseDao;
    @Autowired
    private StudentDao studentDao;
    @Autowired
    private LecturerDao lecturerDao;
    @Autowired
    private SapOlatUserDao sapOlatUserDao;
    @Autowired
    private DelegationDao delegationDao;
    @Autowired
    private StudentCourseDao studentCourseDao;
    @Autowired
    private LecturerCourseDao lecturerCourseDao;
    @Autowired
    private TextDao textDao;
    @Autowired
    private EventDao eventDao;
    @Autowired
    private OrgDao orgDao;
    @Autowired
    protected ImportStatisticDao statisticDao;
    @Autowired
    DataConverter dataConverter;

    private static final int SUBLIST_MAX_SIZE = 4000;
    private static final int MUST_COMPLETED_IMPORTED_FILES = 8;

    public void saveCourses(List<Course> courses) {
        courseDao.saveOrUpdate(courses);
    }

    public void saveOrgs(List<Org> orgs) {
        orgDao.saveOrUpdate(orgs);
    }

    public void saveStudents(List<Student> students) {
        studentDao.saveOrUpdate(students);
    }

    public void saveLecturers(List<Lecturer> lecturers) {
        lecturerDao.saveOrUpdate(lecturers);
    }

    public void saveStudentCourses(List<StudentCourse> studentCourses) {
        studentCourseDao.saveOrUpdate(studentCourses);
    }

    public void saveLecturerCourses(List<LecturerCourse> lecturercourses) {
        lecturerCourseDao.saveOrUpdate(lecturercourses);
    }

    public void saveTexts(List<Text> texts) {
        textDao.saveOrUpdate(texts);
    }

    public void saveEvents(List<Event> events) {
        eventDao.saveOrUpdate(events);
    }

    public void saveSapOlatUsers(List<SapOlatUser> sapOlatUsers) {
        sapOlatUserDao.saveOrUpdate(sapOlatUsers);
    }

    public void saveDelegation(Identity delegator, Identity delegatee) {
        Delegation delegation = new Delegation();
        delegation.setDelegator(delegator.getName());
        delegation.setDelegatee(delegatee.getName());
        delegation.setModifiedDate(new Date());
        delegationDao.saveOrUpdate(delegation);
    }

    public boolean existDelegation(Identity delegator, Identity delegatee) {
        return delegationDao.existDelegation(delegator.getName(), delegatee.getName());
    }

    public boolean existResourceableId(Long resourceableId) {
        return courseDao.existResourceableId(resourceableId);
    }

    public void delete(Course course) {
        courseDao.delete(course);
    }

    public void deleteByCourseId(Long courseId) {
        courseDao.deleteByCourseId(courseId);
    }

    public void deleteCoursesAndBookingsByCourseIds(List<Long> courseIds) {
        List<List<Long>> listSplit = ListUtil.split(courseIds, SUBLIST_MAX_SIZE);
        for (List<Long> subList : listSplit) {
            if (!subList.isEmpty()) {
                studentCourseDao.deleteByCourseIds(subList);
                lecturerCourseDao.deleteByCourseIds(subList);
                eventDao.deleteEventsByCourseIds(subList);
                textDao.deleteTextsByCourseIds(subList);
                courseDao.deleteByCourseIds(subList);
            }
        }
    }

    public List<Long> getAllCoursesToBeDeleted(Date date) {
        return courseDao.getAllNotUpdatedCourses(date);
    }

    public Course getCourseById(Long id) {
        return courseDao.getCourseById(id);
    }

    public int deleteAllTexts() {
        return textDao.deleteAllTexts();
    }

    public int deleteAllEvents() {
        return eventDao.deleteAllEvents();
    }

    public void delete(StudentCourse studentCourse) {
        studentCourseDao.delete(studentCourse);
    }

    public void deleteStudentCourseByCoruseId(Long courseId) {
        studentCourseDao.deleteByCourseId(courseId);
    }

    public void deleteStudentsAndBookingsByStudentIds(List<Long> studentIds) {
        List<List<Long>> listSplit = ListUtil.split(studentIds, SUBLIST_MAX_SIZE);
        for (List<Long> subList : listSplit) {
            if (!subList.isEmpty()) {
                studentCourseDao.deleteByStudentIds(subList);
                sapOlatUserDao.deleteMappingBySapStudentIds(subList);
                studentDao.deleteByStudentIds(subList);
            }
        }
    }

    public void delete(Student student) {
        studentDao.delete(student);
    }

    public void delete(LecturerCourse lecturerCourse) {
        lecturerCourseDao.delete(lecturerCourse);
    }

    public void deleteLecturerCourseByCoruseId(Long courseId) {
        lecturerCourseDao.deleteByCourseId(courseId);
    }

    public int deleteAllNotUpdatedLCBooking(Date date) {
        return lecturerCourseDao.deleteAllNotUpdatedLCBooking(date);
    }

    public int deleteAllNotUpdatedSCBooking(Date date) {
        return studentCourseDao.deleteAllNotUpdatedSCBooking(date);
    }

    public void deleteLecturersAndBookingsByLecturerIds(List<Long> lecturerIds) {
        List<List<Long>> listSplit = ListUtil.split(lecturerIds, SUBLIST_MAX_SIZE);
        for (List<Long> subList : listSplit) {
            if (!subList.isEmpty()) {
                lecturerCourseDao.deleteByLecturerIds(subList);
                sapOlatUserDao.deleteMappingBySapLecturerIds(subList);
                lecturerDao.deleteByLecturerIds(subList);
            }
        }
    }

    public void delete(Lecturer lecturer) {
        lecturerDao.delete(lecturer);
    }

    public Student getStudentById(Long id) {
        return studentDao.getStudentById(id);
    }

    public List<Student> getAllStudents() {
        return studentDao.getAllStudents();
    }

    public List<Student> getAllPilotStudents() {
        return studentDao.getAllPilotStudents();
    }

    public Student getStudentByRegistrationNrId(String registrationNr) {
        return studentDao.getStudentByRegistrationNr(registrationNr);
    }

    public Student getStudentByEmail(String email) {
        return studentDao.getStudentByEmail(email);
    }

    public List<Long> getAllStudentsToBeDeleted(Date date) {
        return studentDao.getAllNotUpdatedStudents(date);
    }

    public void deleteEventsByCourseId(Long courseId) {
        eventDao.deleteEventsByCourseId(courseId);
    }

    public void deleteTextsByCourseId(Long courseId) {
        textDao.deleteTextsByCourseId(courseId);
    }

    public Lecturer getLecturerById(Long id) {
        return lecturerDao.getLecturerById(id);
    }

    public List<Lecturer> getAllLecturers() {
        return lecturerDao.getAllLecturers();
    }

    public List<Lecturer> getAllPilotLecturers() {
        return lecturerDao.getAllPilotLecturers();
    }

    public Lecturer getLecturerByEmail(String email) {
        return lecturerDao.getLecturerByEmail(email);
    }

    public List<Long> getAllLecturersToBeDeleted(Date date) {
        return lecturerDao.getAllNotUpdatedLecturers(date);
    }

    public List<Long> getAllOrgsToBeDeleted(Date date) {
        return orgDao.getAllNotUpdatedOrgs(date);
    }

    public int deleteByOrgIds(List<Long> orgIds) {
        return orgDao.deleteByOrgIds(orgIds);
    }

    public SapOlatUser getStudentSapOlatUserByOlatUserName(String olatUserName) {
        return sapOlatUserDao.getSapOlatUserByOlatUserNameAndSapUserType(olatUserName, SapOlatUser.SapUserType.STUDENT);
    }

    public List<SapOlatUser> getLecturerSapOlatUsersByOlatUserName(String olatUserName) {
        return sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType(olatUserName, SapOlatUser.SapUserType.LECTURER);
    }

    public List<SapOlatUser> getSapOlatUserListByOlatUserName(String olatUserName) {
        return sapOlatUserDao.getSapOlatUserListByOlatUserName(olatUserName);
    }

    public SapOlatUser getSapOlatUserBySapUserId(Long sapUserId) {
        return sapOlatUserDao.getSapOlatUserBySapUserId(sapUserId);
    }

    public List<Text> getTextByCourseId(Long id) {
        return textDao.getTextsByCourseId(id);
    }

    public String getTextContentsByCourseId(Long id) {
        return textDao.getContentsByCourseId(id);
    }

    public String getTextInfosByCourseId(Long id) {
        return textDao.getInfosByCourseId(id);
    }

    public String getTextMaterialsByCourseId(Long id) {
        return textDao.getMaterialsByCourseId(id);
    }

    public List<Course> getPilotCoursesByStudentId(Long id) {
        return courseDao.getPilotCoursesByStudentId(id);
    }

    public List<Course> getCreatedCoursesByStudentId(Long id) {
        return courseDao.getCreatedCoursesByStudentId(id);
    }

    public List<Course> getNotCreatedCoursesByStudentId(Long id) {
        return courseDao.getNotCreatedCoursesByStudentId(id);
    }

    public List<Course> getPilotCoursesByLecturerId(Long id) {
        return courseDao.getPilotCoursesByLecturerId(id);
    }

    public List<Course> getCreatedCoursesByLecturerIds(List<Long> ids) {
        return courseDao.getCreatedCoursesByLecturerIds(ids);
    }

    public List<Course> getNotCreatedCoursesByLecturerIds(List<Long> ids) {
        return courseDao.getNotCreatedCoursesByLecturerIds(ids);
    }

    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType) {
        Set<Course> coursesWithoutResourceableId = new HashSet<Course>();
        coursesWithoutResourceableId.addAll(getCourses(identity.getName(), userType, false));// false --- > notCreatedCourses
        // THE CASE OF LECTURER, ADD THE APPRORIATE DELEGATEES
        if (userType.equals(SapOlatUser.SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationByDelegatee(identity.getName());
            for (Delegation delegation : delegations) {
                coursesWithoutResourceableId.addAll(getCourses(delegation.getDelegator(), userType, false));// false --- > notCreatedCourses
            }
        }
        return coursesWithoutResourceableId;
    }

    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType) {
        Set<Course> coursesWithResourceableId = new HashSet<Course>();
        coursesWithResourceableId.addAll(getCourses(identity.getName(), userType, true));// true --- > CreatedCourses
        // THE CASE OF LECTURER, ADD THE APPRORIATE DELEGATEES
        if (userType.equals(SapOlatUser.SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationByDelegatee(identity.getName());
            for (Delegation delegation : delegations) {
                coursesWithResourceableId.addAll(getCourses(delegation.getDelegator(), userType, true));// true --- > CreatedCourses
            }
        }
        return coursesWithResourceableId;
    }

    public List<Course> getCourses(String olatUserName, SapOlatUser.SapUserType userType, boolean created) {
        if (userType.equals(SapOlatUser.SapUserType.LECTURER)) {
            List<SapOlatUser> sapOlatUsers = getLecturerSapOlatUsersByOlatUserName(olatUserName);
            if (sapOlatUsers.isEmpty()) {
                return Collections.<Course> emptyList();
            } else {
                List<Long> sapUserIds = new ArrayList<Long>();
                for (SapOlatUser sapOlatUser : sapOlatUsers) {
                    sapUserIds.add(sapOlatUser.getSapUserId());
                }
                return (created) ? getCreatedCoursesByLecturerIds(sapUserIds) : getNotCreatedCoursesByLecturerIds(sapUserIds);
            }
        } else {
            SapOlatUser sapOlatUser = getStudentSapOlatUserByOlatUserName(olatUserName);
            if (sapOlatUser == null) {
                return Collections.<Course> emptyList();
            } else {
                return (created) ? getCreatedCoursesByStudentId(sapOlatUser.getSapUserId()) : getNotCreatedCoursesByStudentId(sapOlatUser.getSapUserId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCampusCourseResoureableId(Long courseId, Long resourceableId) {
        courseDao.saveResourceableId(courseId, resourceableId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCampusCourseResoureableIdAndDisableSynchronization(Long courseId, Long resourceableId) {
        courseDao.saveResourceableId(courseId, resourceableId);
        courseDao.disableSynchronization(courseId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteResourceableId(Long resourceableId) {
        courseDao.deleteResourceableId(resourceableId);
    }

    public List<Long> getAllCreatedSapCourcesIds() {
        return courseDao.getIdsOfAllCreatedCourses();
    }

    public List<Long> getAllCreatedSapCourcesResourceableIds() {
        return courseDao.getResourceableIdsOfAllCreatedCourses();
    }

    public List<Long> getAllNotCreatedSapCourcesIds() {
        return courseDao.getIdsOfAllNotCreatedCourses();
    }

    public List<Long> getIdsOfAllEnabledOrgs() {
        return orgDao.getIdsOfAllEnabledOrgs();
    }

    public List<Course> getAllCreatedSapCources() {
        return courseDao.getAllCreatedCourses();
    }

    public CampusCourseImportTO getSapCampusCourse(long courseId) {
        Course course = getCourseById(courseId);
        if (course == null) {
            return null;
        }
        return new CampusCourseImportTO(course.getTitle(), course.getSemester(), dataConverter.convertLecturersToIdentities(course.getCourseLecturerSet()),
                dataConverter.convertDelegateesToIdentities(course.getCourseLecturerSet()), dataConverter.convertStudentsToIdentities(course.getCourseStudentSet()),
                textDao.getContentsByCourseId(course.getId()), course.getResourceableId(), course.getId(), course.getLanguage());
    }

    public CampusCourseImportTO getSapCampusCourse(Course course) {
        if (course == null) {
            return null;
        }
        return new CampusCourseImportTO(course.getTitle(), course.getSemester(), dataConverter.convertLecturersToIdentities(course.getCourseLecturerSet()),
                dataConverter.convertDelegateesToIdentities(course.getCourseLecturerSet()), dataConverter.convertStudentsToIdentities(course.getCourseStudentSet()),
                textDao.getContentsByCourseId(course.getId()), course.getResourceableId(), course.getId(), course.getLanguage());
    }

    public List getDelegatees(Identity delegator) {
        return dataConverter.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        delegationDao.deleteByDelegatorAndDelegatee(delegator.getName(), delegatee.getName());
    }

    public boolean chekImportedData() {
        return (statisticDao.getLastCompletedImportedStatistic().size() == MUST_COMPLETED_IMPORTED_FILES);
    }

    public void deleteOldLecturerMapping() {
        sapOlatUserDao.deleteOldLecturerMapping();
    }

    public void deleteOldStudentMapping() {
        sapOlatUserDao.deleteOldStudentMapping();
    }

}
