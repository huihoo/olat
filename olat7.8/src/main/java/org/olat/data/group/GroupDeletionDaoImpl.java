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

package org.olat.data.group;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Manager for group deletion. Handle deletion-email and db-access for group-deletion lists.
 * 
 * @author Christian Guretzki
 */
@Repository
public class GroupDeletionDaoImpl extends BasicManager implements GroupDeletionDao {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private DB database;

    /**
     * [used by spring]
     * 
     * @param deletionModule
     */
    private GroupDeletionDaoImpl() {
        super();
    }

    @Override
    public List<BusinessGroup> getDeletableGroups(final int lastLoginDuration) {
        final Calendar lastUsageLimit = Calendar.getInstance();
        lastUsageLimit.add(Calendar.MONTH, -lastLoginDuration);
        log.debug("lastLoginLimit=" + lastUsageLimit);
        // 1. get all businness-groups with lastusage > x
        String query = "select gr from org.olat.data.group.BusinessGroupImpl as gr " + " where (gr.lastUsage = null or gr.lastUsage < :lastUsage)"
                + " and gr.type = :type ";
        DBQuery dbq = database.createQuery(query);
        dbq.setDate("lastUsage", lastUsageLimit.getTime());
        dbq.setString("type", BusinessGroup.TYPE_BUDDYGROUP);
        final List groups = dbq.list();
        // 2. get all businness-groups in deletion-process (email send)
        query = "select gr from org.olat.data.group.BusinessGroupImpl as gr" + " , org.olat.data.lifecycle.LifeCycleEntry as le" + " where gr.key = le.persistentRef "
                + " and le.persistentTypeName ='" + BusinessGroupImpl.class.getSimpleName() + "'" + " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' ";
        dbq = database.createQuery(query);
        final List groupsInProcess = dbq.list();
        // 3. Remove all groups in deletion-process from all unused-groups
        groups.removeAll(groupsInProcess);
        return groups;
    }

    @Override
    public List<BusinessGroup> getGroupsInDeletionProcess(final int deleteEmailDuration) {
        final Calendar deleteEmailLimit = Calendar.getInstance();
        deleteEmailLimit.add(Calendar.DAY_OF_MONTH, -(deleteEmailDuration - 1));
        log.debug("deleteEmailLimit=" + deleteEmailLimit);
        final String queryStr = "select gr from org.olat.data.group.BusinessGroupImpl as gr" + " , org.olat.data.lifecycle.LifeCycleEntry as le"
                + " where gr.key = le.persistentRef " + " and le.persistentTypeName ='" + BusinessGroupImpl.class.getSimpleName() + "'" + " and le.action ='"
                + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp >= :deleteEmailDate " + " and gr.type = :type ";
        final DBQuery dbq = database.createQuery(queryStr);
        dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
        dbq.setString("type", BusinessGroup.TYPE_BUDDYGROUP);
        return dbq.list();
    }

    @Override
    public List<BusinessGroup> getGroupsReadyToDelete(final int deleteEmailDuration) {
        final Calendar deleteEmailLimit = Calendar.getInstance();
        deleteEmailLimit.add(Calendar.DAY_OF_MONTH, -(deleteEmailDuration - 1));
        log.debug("deleteEmailLimit=" + deleteEmailLimit);
        final String queryStr = "select gr from org.olat.data.group.BusinessGroupImpl as gr" + " , org.olat.data.lifecycle.LifeCycleEntry as le"
                + " where gr.key = le.persistentRef " + " and le.persistentTypeName ='" + BusinessGroupImpl.class.getSimpleName() + "'" + " and le.action ='"
                + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp < :deleteEmailDate " + " and gr.type = :type ";
        final DBQuery dbq = database.createQuery(queryStr);
        dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
        dbq.setString("type", BusinessGroup.TYPE_BUDDYGROUP);
        return dbq.list();
    }

}
