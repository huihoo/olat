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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 29.06.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class DataConverter {

    @Autowired
    SapOlatUserDao sapOlatUserDao;

    @Autowired
    DelegationDao delegationDao;

    // @Autowired
    BaseSecurity baseSecurity;

    DB dBImpl;

    public BaseSecurity getBaseSecurity() {
        return baseSecurity;
    }

    public void setBaseSecurity(BaseSecurity baseSecurity) {
        this.baseSecurity = baseSecurity;
    }

    public List<Identity> convertStudentsToIdentities(Set<CourseStudent> courseStudents) {
        List<Identity> identitiesOfparticipant = new ArrayList<Identity>();
        SapOlatUser sapOlatUser = null;
        for (CourseStudent courseStudent : courseStudents) {
            sapOlatUser = sapOlatUserDao.getSapOlatUserBySapUserId(courseStudent.getStudent().getId());
            if (sapOlatUser == null) {
                continue;
            }
            Identity identity = findIdentity(sapOlatUser.getOlatUserName());
            if (identity != null) {
                identitiesOfparticipant.add(identity);
            }
        }
        return identitiesOfparticipant;
    }

    public List<Identity> convertLecturersToIdentities(Set<CourseLecturer> courselecturers) {
        List<Identity> identitiesOfLecturers = new ArrayList<Identity>();
        SapOlatUser sapOlatUser = null;
        for (CourseLecturer courseLecturer : courselecturers) {
            sapOlatUser = sapOlatUserDao.getSapOlatUserBySapUserId(courseLecturer.getLecturer().getPersonalNr());
            if (sapOlatUser == null) {
                continue;
            }
            Identity identity = findIdentity(sapOlatUser.getOlatUserName());
            if (identity != null) {
                identitiesOfLecturers.add(identity);
            }

            List<Delegation> delegations = delegationDao.getDelegationByDelegator(sapOlatUser.getOlatUserName());
            for (Delegation delegation : delegations) {
                Identity delegatee = findIdentity(delegation.getDelegatee());
                if (delegatee != null) {
                    identitiesOfLecturers.add(delegatee);
                }
            }
        }
        return identitiesOfLecturers;
    }

    public List<Identity> convertDelegateesToIdentities(Set<CourseLecturer> courselecturers) {
        List<Identity> identitiesOfLecturers = new ArrayList<Identity>();
        SapOlatUser sapOlatUser = null;
        for (CourseLecturer courseLecturer : courselecturers) {
            sapOlatUser = sapOlatUserDao.getSapOlatUserBySapUserId(courseLecturer.getLecturer().getPersonalNr());
            if (sapOlatUser == null) {
                continue;
            }
            List<Delegation> delegations = delegationDao.getDelegationByDelegator(sapOlatUser.getOlatUserName());
            for (Delegation delegation : delegations) {
                Identity delegatee = findIdentity(delegation.getDelegatee());
                if (delegatee != null) {
                    identitiesOfLecturers.add(delegatee);
                }
            }
        }
        return identitiesOfLecturers;
    }

    public List getDelegatees(Identity delegator) {
        List<Object[]> identitiesOfDelegatees = new ArrayList<Object[]>();
        List<Delegation> delegations = delegationDao.getDelegationByDelegator(delegator.getName());
        for (Delegation delegation : delegations) {
            Identity identity = findIdentity(delegation.getDelegatee());

            if (identity != null) {
                identitiesOfDelegatees.add(new Object[] { identity, delegation.getModifiedDate() });
            }
        }
        return identitiesOfDelegatees;
    }

    private Identity findIdentity(String olatUserName) {
        Identity identity = baseSecurity.findIdentityByName(olatUserName);
        commitDBImplTransaction();
        if (identity != null && identity.getStatus().equals(Identity.STATUS_DELETED)) {
            return null;
        }
        return identity;
    }

    @SuppressWarnings("deprecation")
    private void commitDBImplTransaction() {
        if (dBImpl == null) {
            dBImpl = DBFactory.getInstance(false);
        }
        dBImpl.intermediateCommit();
    }
}
