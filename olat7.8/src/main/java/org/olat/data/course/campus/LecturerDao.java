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
public class LecturerDao implements CampusDao<Lecturer> {

    @Autowired
    private GenericDao<Lecturer> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Lecturer.class);
    }

    @Override
    public void saveOrUpdate(List<Lecturer> lecturers) {
        genericDao.saveOrUpdate(lecturers);
    }

    public Lecturer getLecturerById(Long id) {
        return genericDao.findById(id);
    }

    public Lecturer getLecturerByEmail(String email) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("email", email);
        List<Lecturer> lecturers = genericDao.findByCriteria(restrictionMap);
        if (lecturers != null && !lecturers.isEmpty()) {
            return lecturers.get(0);
        }
        return null;
    }

    public List<Lecturer> getAllLecturers() {
        // return genericDao.findAll();
        return getAllPilotLecturers();
    }

    public List<Lecturer> getAllPilotLecturers() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryListResult(Lecturer.GET_ALL_PILOT_LECTURERS, parameters);
    }

    public List<Long> getAllNotUpdatedLecturers(Date date) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastImportDate", date);
        return genericDao.getNamedQueryEntityIds(Lecturer.GET_ALL_NOT_UPDATED_LECTURERS, parameters);
    }

    public void delete(Lecturer lecturer) {
        genericDao.delete(lecturer);
    }

    public void deleteByLecturerIds(List<Long> lecturerIds) {
        Query query = genericDao.getNamedQuery(Lecturer.DELETE_BY_LECTURER_IDS);
        query.setParameterList("lecturerIds", lecturerIds);
        query.executeUpdate();
    }

}
