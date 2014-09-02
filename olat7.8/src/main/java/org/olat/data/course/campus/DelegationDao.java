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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.olat.data.commons.dao.GenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 03.05.2013 <br>
 * 
 * @author aabouc
 */
@Repository
public class DelegationDao {

    @Autowired
    private GenericDao<Delegation> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Delegation.class);
    }

    public void saveOrUpdate(Delegation delegation) {
        genericDao.saveOrUpdate(delegation);
    }

    public void deleteByDelegatorAndDelegatee(String delegator, String delegatee) {
        Query query = genericDao.getNamedQuery(Delegation.DELETE_BY_DELEGATOR_AND_DELEGATEE);
        query.setParameter("delegator", delegator);
        query.setParameter("delegatee", delegatee);
        query.executeUpdate();
    }

    public List<Delegation> getDelegationByDelegator(String delegator) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("delegator", delegator);
        List<Delegation> delegations = genericDao.findByCriteria(restrictionMap);
        return delegations;
    }

    public List<Delegation> getDelegationByDelegatee(String delegatee) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("delegatee", delegatee);
        List<Delegation> delegations = genericDao.findByCriteria(restrictionMap);
        return delegations;
    }

    public boolean existDelegation(String delegator, String delegatee) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("delegator", delegator);
        restrictionMap.put("delegatee", delegatee);
        List<Delegation> delegations = genericDao.findByCriteria(restrictionMap);

        if (!delegations.isEmpty()) {
            return true;
        }
        return false;
    }

}
