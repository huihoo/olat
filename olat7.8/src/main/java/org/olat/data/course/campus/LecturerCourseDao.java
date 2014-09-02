package org.olat.data.course.campus;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.olat.data.commons.dao.GenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Repository
public class LecturerCourseDao implements CampusDao<LecturerCourse> {

    @Autowired
    private GenericDao<LecturerCourse> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(LecturerCourse.class);
    }

    @Override
    public void saveOrUpdate(List<LecturerCourse> lecturerCourses) {
        genericDao.saveOrUpdate(lecturerCourses);
    }

    public void delete(LecturerCourse lecturerCourse) {
        genericDao.delete(lecturerCourse);
    }

    public void deleteByCourseId(Long courseId) {
        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_LECTURER_BY_COURSE_ID);
        query.setParameter("courseId", courseId);
        query.executeUpdate();
    }

    public void deleteByCourseIds(List<Long> courseIds) {
        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_LECTURERS_BY_COURSE_IDS);
        query.setParameterList("courseIds", courseIds);
        query.executeUpdate();
    }

    public void deleteByLecturerId(Long lecturerId) {
        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_LECTURER_BY_LECTURER_ID);
        query.setParameter("lecturerId", lecturerId);
        query.executeUpdate();
    }

    public void deleteByLecturerIds(List<Long> lecturerIds) {
        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_LECTURERS_BY_LECTURER_IDS);
        query.setParameterList("lecturerIds", lecturerIds);
        query.executeUpdate();
    }

    public int deleteAllNotUpdatedLCBooking(Date date) {
        Query query = genericDao.getNamedQuery(LecturerCourse.DELETE_ALL_NOT_UPDATED_LC_BOOKING);
        query.setParameter("lastImportDate", date);
        return query.executeUpdate();
    }

}
