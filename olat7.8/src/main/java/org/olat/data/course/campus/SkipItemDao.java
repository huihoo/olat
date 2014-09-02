package org.olat.data.course.campus;

import javax.annotation.PostConstruct;

import org.olat.data.commons.dao.GenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Repository
public class SkipItemDao {

    @Autowired
    private GenericDao<SkipItem> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(SkipItem.class);
    }

    public void save(SkipItem skipItem) {
        genericDao.save(skipItem);
    }
}
