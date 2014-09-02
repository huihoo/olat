package org.olat.data.course.campus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ImportStatisticDao {

    @Autowired
    private GenericDao<ImportStatistic> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(ImportStatistic.class);
    }

    public void save(ImportStatistic statistic) {
        genericDao.save(statistic);
    }

    public List<ImportStatistic> getLastCompletedImportedStatistic() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryListResult(ImportStatistic.GET_LAST_COMPLETED_IMPORT_STATISTIC, parameters);
    }
}
