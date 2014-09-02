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
 * Initial Date: 07.12.2012 <br>
 * 
 * @author aabouc
 */
@Repository
public class OrgDao implements CampusDao<Org> {

    @Autowired
    private GenericDao<Org> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Org.class);
    }

    @Override
    public void saveOrUpdate(List<Org> orgs) {
        genericDao.saveOrUpdate(orgs);
    }

    public List<Long> getIdsOfAllEnabledOrgs() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryEntityIds(Org.GET_IDS_OF_ALL_ENABLED_ORGS, parameters);
    }

    public List<Long> getAllNotUpdatedOrgs(Date date) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("lastImportDate", date);
        return genericDao.getNamedQueryEntityIds(Org.GET_ALL_NOT_UPDATED_ORGS, parameters);
    }

    public int deleteByOrgIds(List<Long> orgIds) {
        Query query = genericDao.getNamedQuery(Org.DELETE_BY_ORG_IDS);
        query.setParameterList("orgIds", orgIds);
        return query.executeUpdate();
    }

}
