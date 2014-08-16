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

package org.olat.data.lifecycle;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.commons.database.Persistable;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author Christian Guretzki
 */
public class LifeCycleManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    private final Long persistentRef;
    private final String persistentTypeName;

    /**
	 * 
	 */
    private LifeCycleManager(final Persistable persistentObject) {
        this.persistentRef = persistentObject.getKey();
        this.persistentTypeName = getShortTypeName(persistentObject.getClass().getSimpleName());
    }

    /**
     * Get an instance of a narrowed life-cycle manager for this olat persistentObject
     * 
     * @param resourceable
     *            The resource
     * @return The narrowed life-cycle manager
     */
    public static LifeCycleManager createInstanceFor(final Persistable persistentObject) {
        if (persistentObject == null) {
            throw new AssertException("resourceable cannot be null");
        }

        return new LifeCycleManager(persistentObject);
    }

    /**
     * Create a LifeCycleEntry
     * 
     * @return The created LifeCycleEntry
     */
    public void markTimestampFor(final String action) {
        this.markTimestampFor(action, null);
    }

    /**
     * Create a LifeCycleEntry
     * 
     * @return The created LifeCycleEntry
     */
    public void markTimestampFor(final String action, final String userValue) {
        markTimestampFor(new Date(), action, userValue);
    }

    public void markTimestampFor(final Date eventDate, final String action) {
        markTimestampFor(eventDate, action, null);
    }

    public void markTimestampFor(final Date eventDate, final String action, final String userValue) {
        final LifeCycleEntry entry = lookupLifeCycleEntry(action, userValue);
        if (entry == null) {
            createAndSaveLifeCycleEntry(eventDate, action, userValue);
        } else {
            entry.setLcTimestamp(eventDate);
            updateLifeCycleEntry(entry);
        }
    }

    public LifeCycleEntry lookupLifeCycleEntry(final String action) {
        return lookupLifeCycleEntry(action, null);
    }

    public LifeCycleEntry lookupLifeCycleEntry(final String action, final String userValue) {
        final StringBuilder query = new StringBuilder("from org.olat.data.lifecycle.LifeCycleEntry as e ");
        query.append("where e.action=:action and e.persistentTypeName=:persistentTypeName and e.persistentRef=:persistentRef");
        if (userValue == null) {
            query.append(" and e.userValue=null");
        } else {
            query.append(" and e.userValue=:userValue");
        }
        final DBQuery dbq = DBFactory.getInstance().createQuery(query.toString());
        dbq.setString("action", action);
        dbq.setString("persistentTypeName", persistentTypeName);
        dbq.setLong("persistentRef", persistentRef);
        if (userValue != null) {
            dbq.setString("userValue", userValue);
        }
        final List lifeCycleEntries = dbq.list();

        if (lifeCycleEntries.size() == 0) {
            return null;
        } else if (lifeCycleEntries.size() > 1) {
            log.warn("Found more than one lifeCycleObject with same parametert :" + lifeCycleEntries);
        }
        return (LifeCycleEntry) lifeCycleEntries.get(0);
    }

    /**
     * Delete a LifeCycleEntry from the database
     * 
     * @param p
     */
    public void deleteTimestampFor(final String action) {
        this.deleteTimestampFor(action, null);
    }

    /**
     * Delete a LifeCycleEntry from the database
     * 
     * @param p
     */
    public void deleteTimestampFor(final String action, final String userValue) {
        final LifeCycleEntry entry = lookupLifeCycleEntry(action, userValue);
        if (entry != null) {
            DBFactory.getInstance().deleteObject(entry);
        }
    }

    /**
     * Deletes all LifeCycleEntry of this resourceable
     */
    public void deleteAllEntriesForPersistentObject() {
        // TODO:
        log.error("NOT IMPLEMENTED YET !!!!!!!!!!!");
    }

    /**
     * Get a normalized type-name because type-name could be too long.
     * 
     * @param standardTypeName
     * @return
     */
    public static String getShortTypeName(final String standardTypeName) {
        if (standardTypeName.length() > LifeCycleEntry.PERSISTENTTYPENAME_MAXLENGTH) {
            // encode into an md5 hash with fixed length of 32 characters otherwise the sting may get too long for locks or db fields
            return Encoder.encrypt(standardTypeName);
        } else {
            return standardTypeName;
        }
    }

    // /////////////////
    // Private Methods
    // /////////////////

    private LifeCycleEntry createAndSaveLifeCycleEntry(final Date date, final String action, final String userValue) {
        final LifeCycleEntry entry = new LifeCycleEntry(date, persistentTypeName, persistentRef);
        entry.setAction(action);
        entry.setUserValue(userValue);
        DBFactory.getInstance().saveObject(entry);
        return entry;
    }

    private void updateLifeCycleEntry(final LifeCycleEntry entry) {
        DBFactory.getInstance().updateObject(entry);
    }

}
