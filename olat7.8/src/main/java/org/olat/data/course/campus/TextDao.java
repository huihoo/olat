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
public class TextDao implements CampusDao<Text> {

    @Autowired
    private GenericDao<Text> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Text.class);
    }

    @Override
    public void saveOrUpdate(List<Text> texts) {
        genericDao.save(texts);
    }

    public List<Text> getTextsByCourseId(Long courseId) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("courseId", courseId);
        return genericDao.findByCriteria(restrictionMap);
    }

    public String getContentsByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndType(courseId, Text.CONTENTS);
        return buildText(texts);
    }

    public String getMaterialsByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndType(courseId, Text.MATERIALS);
        return buildText(texts);
    }

    public String getInfosByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndType(courseId, Text.INFOS);
        return buildText(texts);
    }

    private List<Text> getTextsByCourseIdAndType(Long courseId, String type) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("courseId", courseId);
        parameters.put("type", type);
        return genericDao.getNamedQueryListResult(Text.GET_TEXTS, parameters);
    }

    private String buildText(List<Text> texts) {
        StringBuffer content = new StringBuffer();
        for (Text text : texts) {
            content.append(text.getLine());
            content.append(Text.BREAK_TAG);
        }
        return content.toString();
    }

    public int deleteAllTexts() {
        return genericDao.getNamedQuery(Text.DELETE_ALL_TEXTS).executeUpdate();
    }

    public void deleteTextsByCourseId(Long courseId) {
        Query query = genericDao.getNamedQuery(Text.DELETE_TEXTS_BY_COURSE_ID);
        query.setParameter("courseId", courseId);
        query.executeUpdate();
    }

    public void deleteTextsByCourseIds(List<Long> courseIds) {
        Query query = genericDao.getNamedQuery(Text.DELETE_TEXTS_BY_COURSE_IDS);
        query.setParameterList("courseIds", courseIds);
        query.executeUpdate();
    }

}
