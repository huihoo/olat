package org.olat.data.course.campus;

import java.util.List;

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
public class ExportDao implements CampusDao<Export> {
    @Autowired
    private GenericDao<Export> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Export.class);
    }

    @Override
    public void saveOrUpdate(List<Export> exports) {
        genericDao.save(exports);
    }

}
