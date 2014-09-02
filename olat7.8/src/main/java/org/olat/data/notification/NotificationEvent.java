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
package org.olat.data.notification;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * Initial Date: 25.11.2011 <br>
 * 
 * A publisher generates a NotificationEvent if the source changes.
 * 
 * @author lavinia
 */
@Entity
@NamedQueries({
        @NamedQuery(name = NotificationEvent.UPDATE_EVENT_STATUS_BY_IDS, query = "update NotificationEvent n set n.status = :" + NotificationEvent.EVENT_STATUS_PARAM
                + " where id in (:" + NotificationEvent.IDS_PARAM + ")"),
        @NamedQuery(name = NotificationEvent.GET_OLD_EVENTS, query = "from NotificationEvent where creationDate < :" + NotificationEvent.NOTIFICATION_NEWS_DATE_PARAM) })
@NamedNativeQueries({
        @NamedNativeQuery(name = NotificationEvent.GET_EVENTS_FOR_UPDATE, query = "select e2.* from sy_event e1,sy_attribute a1,sy_event e2,sy_attribute a2 where"
                + " e1.id in (:" + NotificationEvent.IDS_PARAM + ") and e1.subscription_id = e2.subscription_id and e1.id = a1.event_id and e2.id = a2.event_id"
                + " and a1.attribute_key = '" + NotificationEvent.ATTRIBUTE_SOURCE_ENTRY_ID + "' and a2.attribute_key = '" + NotificationEvent.ATTRIBUTE_SOURCE_ENTRY_ID
                + "' and a1.attribute_value = a2.attribute_value", resultClass = NotificationEvent.class),
        @NamedNativeQuery(name = NotificationEvent.GET_EVENTS_BY_SUBSCRIBER, query = "select * from sy_event,(select max(id) as id from sy_event e1,sy_attribute a1,"
                + "(select max(e2.creation_date) creation_date, e2.subscription_id,a2.attribute_key,a2.attribute_value"
                + " from sy_subscription s,sy_event e2,sy_attribute a2 where s.id = e2.subscription_id and s.subscriber_id = :" + NotificationEvent.SUBSCRIBER_ID_PARAM
                + " and e2.status = '" + NotificationEvent.STATUS_WAITING + "' and s.status = '" + Subscription.STATUS_VALID
                + "' and e2.id = a2.event_id and a2.attribute_key = '" + NotificationEvent.ATTRIBUTE_SOURCE_ENTRY_ID
                + "' group by e2.subscription_id,a2.attribute_key,a2.attribute_value) e_temp1"
                + " where e1.id = a1.event_id and e1.creation_date = e_temp1.creation_date and e1.subscription_id = e_temp1.subscription_id"
                + " and a1.attribute_key = e_temp1.attribute_key and a1.attribute_value = e_temp1.attribute_value"
                + " group by e1.subscription_id, e1.creation_date, a1.attribute_key,a1.attribute_value) e_temp2 where sy_event.id = e_temp2.id", resultClass = NotificationEvent.class),
        @NamedNativeQuery(name = NotificationEvent.GET_EVENTS_FOR_IDENTITY, query = "select * from sy_event,(select max(id) as id from sy_event e1,sy_attribute a1,"
                + "(select max(e2.creation_date) creation_date, e2.subscription_id,a2.attribute_key,a2.attribute_value"
                + " from sy_subscriber sub,sy_subscription s,sy_event e2,sy_attribute a2 where s.id = e2.subscription_id and s.subscriber_id = sub.id and sub.identity_id = :"
                + NotificationEvent.IDENTITY_ID_PARAM + " and s.status = '" + Subscription.STATUS_VALID + "' and e2.id = a2.event_id and e2.creation_date between :"
                + NotificationEvent.DATE_FROM_PARAM + " and :" + NotificationEvent.DATE_TO_PARAM + " and a2.attribute_key = '"
                + NotificationEvent.ATTRIBUTE_SOURCE_ENTRY_ID + "' group by e2.subscription_id,a2.attribute_key,a2.attribute_value) e_temp1"
                + " where e1.id = a1.event_id and e1.creation_date = e_temp1.creation_date and e1.subscription_id = e_temp1.subscription_id"
                + " and a1.attribute_key = e_temp1.attribute_key and a1.attribute_value = e_temp1.attribute_value"
                + " group by e1.subscription_id, e1.creation_date, a1.attribute_key,a1.attribute_value) e_temp2 where sy_event.id = e_temp2.id", resultClass = NotificationEvent.class) })
@Table(name = "sy_event")
public class NotificationEvent {

    /**
     * 
     * An event could be in one of these states: <br/>
     * - WAITING - new event waiting to be sent <br/>
     * - DELIVERED - delivered event <br/>
     * - FAILED - event could not be delivered <br/>
     */
    public static enum Status {
        WAITING, DELIVERED, FAILED;
    }

    /**
     * The accepted attribute keys. (e.g. Attribute.EVENT_TYPE.name())
     */
    public static enum Attribute {
        SOURCE_ENTRY_ID, EVENT_TYPE, CREATOR_USERNAME, CREATOR_FIRST_LAST_NAME, CONTEXT_TITLE, SOURCE_TITLE, SOURCE_ENTRY_TITLE;
    }

    public static final String SUBSCRIPTION_ID_PARAM = "subscription";
    public static final String SUBSCRIBER_ID_PARAM = "subscriber";
    public static final String MAP_KEY_PARAM = "mapKey";
    public static final String MAP_VALUE_PARAM = "mapValue";
    public static final String ATTRIBUTE_SOURCE_ENTRY_ID = "SOURCE_ENTRY_ID"; // MUST BE SAME LIKE : Attribute.SOURCE_ENTRY_ID.name
    public static final String STATUS_WAITING = "WAITING";
    public static final String GET_EVENTS_FOR_IDENTITY = "getEventsForIdentity";
    public static final String GET_EVENTS_BY_SUBSCRIBER = "getEventsBySubscriber";
    public static final String UPDATE_EVENT_STATUS_BY_IDS = "updateEventStatusByIds";
    public static final String IDENTITY_ID_PARAM = "identity";
    public static final String DATE_FROM_PARAM = "dateFrom";
    public static final String DATE_TO_PARAM = "dateTo";
    public static final String GET_EVENTS_FOR_UPDATE = "getEventsForUpdate";
    public static final String IDS_PARAM = "ids";
    public static final String EVENT_STATUS_PARAM = "status";
    public static final String GET_OLD_EVENTS = "getOldEvents";
    public static final String NOTIFICATION_NEWS_DATE_PARAM = "notificationNewsDateParam";

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Version
    private Long version;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    @Index(name = "sy_event_i02")
    private Date creationDate;

    @CollectionOfElements
    @JoinTable(name = "sy_attribute", joinColumns = @JoinColumn(name = "event_id"))
    @org.hibernate.annotations.MapKey(columns = { @Column(name = "attribute_key") })
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<String, String>();

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.WAITING;

    public Subscription getSubscription() {
        return subscription;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Accepted attribute names: <br>
     * <code>Attribute.EVENT_TYPE.name()</code> <br>
     * <code>Attribute.CREATOR_USERNAME.name()</code> <br>
     * <code>Attribute.CREATOR_FIRST_LAST_NAME.name()</code> <br>
     * <code>Attribute.SOURCE_ENTRY_ID.name()</code> <br>
     * <code>Attribute.CONTEXT_TITLE.name()</code> <br>
     * <code>Attribute.SOURCE_TITLE.name()</code> <br>
     * <code>Attribute.SOURCE_ENTRY_TITLE.name()</code>
     * 
     */
    public void addAttribute(String attributeName, String attributeValue) {
        attributes.put(attributeName, attributeValue);
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
        subscription.addNotificationEvent(this);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NotificationEvent))
            return false;
        NotificationEvent theOther = (NotificationEvent) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.appendSuper(this.getSubscription().equals(theOther.getSubscription()));
        builder.append(this.creationDate, theOther.getCreationDate());
        builder.append(this.status, theOther.status);
        builder.append(this.getAttributes(), theOther.getAttributes());

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(29, 57);
        if (getSubscription() != null) {
            builder.appendSuper(getSubscription().hashCode());
        } else if (getId() != null) {
            builder.append(getId());
        }
        builder.append(creationDate);
        builder.append(status.name().hashCode());
        return builder.toHashCode();
    }

}
