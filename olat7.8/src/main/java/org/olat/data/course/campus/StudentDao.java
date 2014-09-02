package org.olat.data.course.campus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class StudentDao implements CampusDao<Student> {

    @Autowired
    private GenericDao<Student> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Student.class);
    }

    @Override
    public void saveOrUpdate(List<Student> students) {
        genericDao.saveOrUpdate(students);
    }

    public Student getStudentById(Long id) {
        return genericDao.findById(id);
    }

    public Student getStudentByEmail(String email) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("email", email);
        List<Student> students = genericDao.findByCriteria(restrictionMap);
        if (students != null) {
            return students.get(0);
        }
        return null;
    }

    public Student getStudentByRegistrationNr(String registrationNr) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("registrationNr", registrationNr);
        List<Student> students = genericDao.findByCriteria(restrictionMap);
        if (students != null && !students.isEmpty()) {
            return students.get(0);
        }
        return null;
    }

    public List<Student> getAllStudents() {
        // return genericDao.findAll();
        return getAllPilotStudents();
    }

    public List<Student> getAllPilotStudents() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryListResult(Student.GET_ALL_PILOT_STUDENTS, parameters);
    }

    public List<Long> getAllNotUpdatedStudents(Date date) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastImportDate", date);
        return genericDao.getNamedQueryEntityIds(Student.GET_ALL_NOT_UPDATED_STUDENTS, parameters);
    }

    public void delete(Student student) {
        genericDao.delete(student);
    }

    public void deleteByStudentIds(List<Long> studentIds) {
        Query query = genericDao.getNamedQuery(Student.DELETE_BY_STUDENT_IDS);
        query.setParameterList("studentIds", studentIds);
        query.executeUpdate();
    }

}
