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
import org.olat.data.notification.Publisher.ContextType;

/**
 * This is a transfer object describing the publish event including the creator identity. <br/>
 * See <code>Publisher<code/> entity for detailed information. <br/>
 * 
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
public class PublishEventTO {

    private final Identity creator;
    private PublisherTO publisher;

    private final String contextTitle; // e.g. course title
    private final String sourceTitle; // e.g. forum title
    private final String sourceEntryTitle; // e.g. message title
    private String sourceEntryId; // e.g. message id

    private EventType eventType; // e.g. NEW, CHANGED, DELETED, NO_PUBLISH

    /**
     * Stored in the sy_attribute DB table as values for Attribute.EVENT_TYPE.
     */
    public enum EventType {
        NEW, CHANGED, DELETED, NO_PUBLISH;
    }

    private PublishEventTO(EventType type) {
        this(null, null, null, null, type);
    }

    public static PublishEventTO getValidInstance(ContextType contextType, Long contextId, String contextTitle, Long subcontextId, String sourceType, Long sourceId,
            String sourceTitle, String sourceEntryTitle, Identity creator, EventType eventType) {

        PublishEventTO event = new PublishEventTO(contextTitle, sourceTitle, sourceEntryTitle, creator, eventType);
        if (!eventType.equals(EventType.NO_PUBLISH)) {
            event.publisher = PublisherTO.getValidInstance(contextType, contextId, subcontextId, sourceType, sourceId);
        }
        return event;
    }

    private String getFirstLastName(Identity identity) {
        return identity.getAttributes().getFirstName() + " " + identity.getAttributes().getLastName();
    }

    public static PublishEventTO getNoPublishInstance() {
        return new PublishEventTO(EventType.NO_PUBLISH);
    }

    private PublishEventTO(String contextTitle, String sourceTitle, String sourceEntryTitle, Identity creator, EventType eventType) {
        this.contextTitle = contextTitle;
        this.sourceTitle = sourceTitle;
        this.sourceEntryTitle = sourceEntryTitle;
        this.creator = creator;
        this.eventType = eventType;
    }

    private PublishEventTO(String contextTitle, String sourceTitle, String sourceEntryTitle, String sourceEntryId, Identity creator, EventType eventType) {
        this(contextTitle, sourceTitle, sourceEntryId, creator, eventType);
        this.sourceEntryId = sourceEntryId;
    }

    public String getSourceType() {
        return publisher.getSourceType();
    }

    public Long getSourceId() {
        return publisher.getSourceId();
    }

    /**
     * needed to check that creator and subscriber are not the same.
     */
    public Identity getCreator() {
        return creator;
    }

    /**
     * needed for the notification message.
     */
    public String getCreatorsFirstLastName() {
        return getFirstLastName(creator);
    }

    public EventType getEvenType() {
        return eventType;
    }

    public ContextType getContextType() {
        return publisher.getContextType();
    }

    public Long getContextId() {
        return publisher.getContextId();
    }

    public Long getSubcontextId() {
        return publisher.getSubcontextId();
    }

    public String getSourceEntryId() {
        return sourceEntryId;
    }

    public void setSourceEntryId(String sourceEntryId) {
        this.sourceEntryId = sourceEntryId;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getSourceEntryTitle() {
        return sourceEntryTitle;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("contextTitle", getContextTitle());
        builder.append("sourceTitle", getSourceTitle());
        builder.append("sourceEntryTitle", getSourceEntryTitle());
        builder.append("sourceEntryId", getSourceEntryId());
        builder.append("contextId", getContextId());
        builder.append("contextType", getContextType());
        builder.append("sourceId", getSourceId());
        builder.append("sourceType", getSourceType());
        return builder.toString();
    }

    // TODO: TO-classes should not have a setter => Refactoring when old notification are removed
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
