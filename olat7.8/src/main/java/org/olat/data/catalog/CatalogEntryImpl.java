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

package org.olat.data.catalog;

import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.PersistentObject;
import org.olat.data.repository.RepositoryEntry;
import org.olat.system.exception.AssertException;

/**
 * Description: <br>
 * Implementation of CatalogEntry
 * 
 * @author Felix Jost
 */
public class CatalogEntryImpl extends PersistentObject implements CatalogEntry {
    private String name;
    private String description;
    private String externalURL;
    private RepositoryEntry repositoryEntry;
    private CatalogEntry parent;

    private SecurityGroup ownerGroup;
    private int type;

    // only for Unit testing public
    public CatalogEntryImpl() {
        // for hibernate
    }

    /**
	 */
    @Override
    public String getDescription() {
        return description;
    }

    /**
	 */
    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
	 */
    @Override
    public String getName() {
        return name;
    }

    /**
	 */
    @Override
    public void setName(final String name) {
        if (name.length() > RepositoryEntry.MAX_DISPLAYNAME_LENGTH) {
            throw new AssertException("CatalogEntry: Name is limited to 140 characters.");
        }
        this.name = name;
    }

    /**
	 */
    @Override
    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    /**
	 */
    @Override
    public void setRepositoryEntry(final RepositoryEntry repositoryEntry) {
        this.repositoryEntry = repositoryEntry;
    }

    /**
	 */
    @Override
    public SecurityGroup getOwnerGroup() {
        return ownerGroup;
    }

    /**
	 */
    @Override
    public void setOwnerGroup(final SecurityGroup ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    /**
	 */
    @Override
    public int getType() {
        return type;
    }

    /**
	 */
    @Override
    public void setType(final int type) {
        this.type = type;
    }

    /**
	 */
    @Override
    public String getExternalURL() {
        return externalURL;
    }

    /**
	 */
    @Override
    public void setExternalURL(final String externalURL) {
        this.externalURL = externalURL;
    }

    /**
	 */
    @Override
    public CatalogEntry getParent() {
        return parent;
    }

    /**
	 */
    @Override
    public void setParent(final CatalogEntry parent) {
        this.parent = parent;
    }

    /**
	 */
    @Override
    public String toString() {
        return "cat:" + getName() + "=" + super.toString();
    }

    /**
	 */
    @Override
    public String getResourceableTypeName() {
        return this.getClass().getName();
    }

    /**
	 */
    @Override
    public Long getResourceableId() {
        final Long key = getKey();
        if (key == null) {
            throw new AssertException("no key yet!");
        }
        return key;
    }
}
