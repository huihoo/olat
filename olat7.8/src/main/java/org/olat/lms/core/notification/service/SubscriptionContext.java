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

package org.olat.lms.core.notification.service;

import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Contains the coordinates of a subscription for notification. This could become a publisher, if someone subscribes. <br>
 * A publisher is a subscriptionContext with at least one subscription. <br>
 * SubscriptionContext used to belong to the presentation package.
 * <P>
 * Initial Date: 25.10.2004 <br>
 * 
 * @author Felix Jost
 */
public class SubscriptionContext {
    private final String resName;
    private final Long resId;
    private final String subidentifier;

    // new fields added to accommodate the new notification service
    private Long contextId; // this is the course id - RepositoryEntryId
    private String contextTitle; // this is the course title
    private String sourceTitle; // this is courseNodeTitle

    /**
     * Create a new subscription context
     * 
     * @param resName
     *            not null, unique identifier for this context use something like: OresHelper.calculateTypeName(DropboxController.class);
     * @param resId
     *            not null, resource id like OLATResourcable.getResourceId()
     * @param subidentifier
     *            not null, when context is from course use CourseNode.getIdent()
     */
    public SubscriptionContext(String resName, Long resId, String subidentifier) {
        if (resName == null || resId == null || subidentifier == null)
            throw new AssertException("resName, resId, subident cannot be null");
        this.resName = resName;
        this.resId = resId;
        this.subidentifier = subidentifier;
    }

    /**
     * Create a new subscription context. Calls the other constructor by calculating the unique name and the resource id out of the OLATResourcable
     * 
     * @param ores
     *            ,
     * @param subidentifier
     *            not null, when context is from course use CourseNode.getIdent()
     */
    public SubscriptionContext(OLATResourceable ores, String subidentifier) {
        this(ores.getResourceableTypeName(), ores.getResourceableId(), subidentifier);
    }

    /**
     * @return resId
     */
    public Long getResId() {
        return resId;
    }

    /**
     * @return resName
     */
    public String getResName() {
        return resName;
    }

    /**
     * @return subidentifier
     */
    public String getSubidentifier() {
        return subidentifier;
    }

    /**
	 */
    @Override
    public String toString() {
        return getResName() + "," + getResId() + "," + getSubidentifier() + "," + super.toString();
    }

    // ************ methods to accommodate the new NotificationService ***************************************
    public String getContextTitle() {
        return contextTitle;
    }

    public void setContextTitle(String contextTitle) {
        this.contextTitle = contextTitle;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

}
