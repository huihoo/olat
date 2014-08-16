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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.data.commons.database;

import java.util.Date;

/**
 * Needs the primary key called 'key' in hibernate and 'creationDate'
 * 
 * @author Andreas Ch. Kapp
 * @deprecated
 */
@SuppressWarnings("unused")
public abstract class PersistentObject implements CreateInfo, Persistable {

    private Long key = null;
    private int version;
    protected Date creationDate;

    /**
	 */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * For Hibernate only
     * 
     * @param version
     */
    private void setVersion(int version) {
        this.version = version;
    }

    /**
     * For Hibernate only
     * 
     * @param date
     */
    private void setCreationDate(Date date) {
        creationDate = date;
    }

    /**
     * @return Long
     */
    @Override
    public Long getKey() {
        return key;
    }

    /**
     * for hibernate only
     * 
     * @param key
     */
    private void setKey(Long key) {
        this.key = key;
    }

    /**
	 */
    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        if (this.getKey().compareTo(persistable.getKey()) == 0)
            return true;
        else
            return false;
    }

    /**
	 */
    @Override
    public String toString() {
        return "key:" + key + "=" + super.toString();
    }

}
