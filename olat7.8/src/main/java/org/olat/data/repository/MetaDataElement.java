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

package org.olat.data.repository;

import java.util.Date;

import org.olat.data.commons.database.PersistentObject;

/**
 * Initial Date: Jul 2, 2004
 * 
 * @author mike Comment:
 */
public class MetaDataElement extends PersistentObject {
    private String name; // mandatory
    private String value;

    /**
     * Constructor for a repository meta data element
     * 
     * @param name
     *            The metadata element name
     * @param value
     *            The metadata element value
     */
    public MetaDataElement(final String name, final String value) {
        super();
        this.creationDate = new Date();
        this.name = name;
        this.value = value;
    }

    private MetaDataElement() {
        super();
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
