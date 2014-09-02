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
package org.olat.lms.core.notification.service;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;

/**
 * This is a business class containing all subscription info: identity, publisher info.<br>
 * It is a facade for the domain classes.
 * 
 * 
 * Initial Date: 29.11.2011 <br>
 * 
 * @author cg
 */
public class NotificationSubscriptionContext {

    private Identity identity;
    private PublisherTO publisher;

    public NotificationSubscriptionContext(Identity identity, String sourceType, Long sourceId, Publisher.ContextType contextType, Long contextId, Long subcontextId) {

        this.identity = identity;
        this.publisher = PublisherTO.getValidInstance(contextType, contextId, subcontextId, sourceType, sourceId);

    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity subscriber) {
        this.identity = subscriber;
    }

    public String getSourceType() {
        return publisher.getSourceType();
    }

    public Long getContextId() {
        return publisher.getContextId();
    }

    public Long getSourceId() {
        return publisher.getSourceId();
    }

    public ContextType getContextType() {
        return publisher.getContextType();
    }

    public Long getSubcontextId() {
        return publisher.getSubcontextId();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identity", getIdentity());
        builder.append("publisher", publisher.toString());

        return builder.toString();
    }

}
