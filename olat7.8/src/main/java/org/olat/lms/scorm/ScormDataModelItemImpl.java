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

package org.olat.lms.scorm;

/**
 * Initial Date: 08.06.2005 <br>
 * 
 * @author guido
 */
public class ScormDataModelItemImpl implements ScormDataModelItem {

    private final String userId;
    private final String scoId;
    private String key;
    private String value;

    /**
     * @param userId
     * @param scoId
     * @param key
     * @param value
     */
    public ScormDataModelItemImpl(final String userId, final String scoId, final String key, final String value) {
        this.userId = userId;
        this.scoId = scoId;
        this.key = key;
        this.value = value;
    }

    /**
     * @param userId
     * @param scoId
     */
    public ScormDataModelItemImpl(final String userId, final String scoId) {
        this.userId = userId;
        this.scoId = scoId;
    }

    /**
     * @return Returns the key.
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            The key to set.
     */
    @Override
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @return Returns the scoId.
     */
    @Override
    public String getScoId() {
        return scoId;
    }

    /**
     * @return Returns the userId.
     */
    @Override
    public String getUserId() {
        return userId;
    }

    /**
     * @return Returns the value.
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    @Override
    public void setValue(final String value) {
        this.value = value;
    }
}
