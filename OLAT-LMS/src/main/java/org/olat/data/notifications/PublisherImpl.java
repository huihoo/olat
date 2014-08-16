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

package org.olat.data.notifications;

import java.util.Date;

import org.olat.data.commons.database.PersistentObject;

/**
 * Description: <br>
 * TODO: Felix Jost Class Description for PublisherImpl
 * <P>
 * Initial Date: 21.10.2004 <br>
 * 
 * @author Felix Jost
 */
public class PublisherImpl extends PersistentObject implements Publisher {
    private String type; // e.g. Forum
    private String resName; // e.g. CourseModule
    private Long resId; // e.g. 2343284327
    private String subidentifier; // e.g. 69680861018558 (for a node in
    // a course)
    private String data; // any additional data (depending on type)
    private int state; // 0 = ok
    private Date latestNewsDate;
    private String businessPath;

    /**
     * for hibernate only
     */
    protected PublisherImpl() {
        //
    }

    /**
     * @param resName
     * @param resId
     * @param subidentifier
     * @param type
     * @param data
     * @param state
     */
    PublisherImpl(final String resName, final Long resId, final String subidentifier, final String type, final String data, final String businessPath,
            final Date latestNewsDate, final int state) {
        this.resId = resId;
        this.resName = resName;
        this.subidentifier = subidentifier;
        this.type = type;
        this.data = data;
        this.state = state;
        this.businessPath = businessPath;
        this.latestNewsDate = latestNewsDate;
    }

    /**
     * @return resId
     */
    @Override
    public Long getResId() {
        return resId;
    }

    /**
     * @param resId
     */
    @Override
    public void setResId(final Long resId) {
        this.resId = resId;
    }

    /**
     * @return resName
     */
    @Override
    public String getResName() {
        return resName;
    }

    /**
     * @param resName
     */
    @Override
    public void setResName(final String resName) {
        this.resName = resName;
    }

    /**
     * @return the subidentifier
     */
    @Override
    public String getSubidentifier() {
        return subidentifier;
    }

    /**
     * @param subidentifier
     *            (max len 255)
     */
    @Override
    public void setSubidentifier(final String subidentifier) {
        this.subidentifier = subidentifier;
    }

    /**
     * @return the type
     */
    @Override
    public String getType() {
        return type;
    }

    // FIXME:fj:make methods private if no setter used (used only by hibernate)
    /**
     * @param type
     */
    @Override
    public void setType(final String type) {
        this.type = type;
    }

    /**
	 */
    @Override
    public String getData() {
        return data;
    }

    /**
     * @param data
     */
    @Override
    public void setData(final String data) {
        this.data = data;
    }

    /**
     * @return Returns the state.
     */
    @Override
    public int getState() {
        return state;
    }

    /**
     * @param state
     *            The state to set.
     */
    @Override
    public void setState(final int state) {
        this.state = state;
    }

    /**
     * @return Returns the latestNewsDate.
     */
    @Override
    public Date getLatestNewsDate() {
        return latestNewsDate;
    }

    /**
     * @param latestNewsDate
     *            The latestNewsDate to set.
     */
    @Override
    public void setLatestNewsDate(final Date latestNewsDate) {
        this.latestNewsDate = latestNewsDate;
    }

    @Override
    public String getBusinessPath() {
        return businessPath;
    }

    @Override
    public void setBusinessPath(final String businessPath) {
        this.businessPath = businessPath;
    }
}
