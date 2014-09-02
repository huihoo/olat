package org.olat.data.course.campus;

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
public class EventDao implements CampusDao<Event> {
    @Autowired
    private GenericDao<Event> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Event.class);
    }

    @Override
    public void saveOrUpdate(List<Event> events) {
        genericDao.save(events);
    }

    public List<Event> getEventsByCourseId(Long id) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("courseId", id);
        return genericDao.findByCriteria(restrictionMap);
    }

    public int deleteAllEvents() {
        return genericDao.getNamedQuery(Event.DELETE_ALL_EVENTS).executeUpdate();
    }

    public void deleteEventsByCourseId(Long courseId) {
        Query query = genericDao.getNamedQuery(Event.DELETE_EVENTS_BY_COURSE_ID);
        query.setParameter("courseId", courseId);
        query.executeUpdate();
    }

    public void deleteEventsByCourseIds(List<Long> courseIds) {
        Query query = genericDao.getNamedQuery(Event.DELETE_EVENTS_BY_COURSE_IDS);
        query.setParameterList("courseIds", courseIds);
        query.executeUpdate();
    }
}
