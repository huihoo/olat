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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.OptimisticLock;

/**
 * There is exactly one subscription per subscriber and publisher.
 * 
 * Initial Date: 25.11.2011 <br>
 * 
 * @author lavinia
 */
@Entity
@Table(name = "sy_subscription", uniqueConstraints = @UniqueConstraint(columnNames = { "subscriber_id", "publisher_id" }))
@NamedQueries({
        @NamedQuery(name = Subscription.GET_SUBSCRIPTIONS_FOR_IDENTITY, query = "from Subscription s where s.subscriber.identity = :" + Subscriber.IDENTITY_PARAM
                + " and s.status = :" + Subscription.SUBSCRIPTION_STATUS_PARAM),
        @NamedQuery(name = Subscription.GET_SUBSCRIPTIONS_FOR_SUBSCRIBER_ID, query = "from Subscription s where s.subscriber.id = :" + Subscriber.SUBSCRIBER_ID_PARAM
                + " and s.status = :" + Subscription.SUBSCRIPTION_STATUS_PARAM),
        @NamedQuery(name = Subscription.UPDATE_LASTNOTIFIEDDATE_BY_IDS, query = "update Subscription set lastNotifiedDate = :date where id in(:ids)") })
public class Subscription {

    /**
     * VALID - initial status after user does subscription <br/>
     * INVALID - status when user unsubscribes or visiblity rules are changed or context container (for example course) is deleted
     * 
     */
    public static enum Status {
        VALID, INVALID;
    }

    public static final String GET_SUBSCRIPTIONS_FOR_IDENTITY = "getSubscriptionsForIdentity";
    public static final String GET_SUBSCRIPTIONS_FOR_SUBSCRIBER_ID = "getSubscriptionsForSubscriber";
    public static final String SUBSCRIPTION_STATUS_PARAM = "status";
    public static final String UPDATE_LASTNOTIFIEDDATE_BY_IDS = "updateLastNotifiedDateByIds";
    public static final String STATUS_VALID = "VALID";

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id = new Long(-1);

    @Version
    private Long version;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_notified_date")
    private Date lastNotifiedDate;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    // do not update the version when elements are added/deleted.
    // changed mapping from Set to List because PeristedSet implementation from Hibernate has known bug (remove, contains elements from collection does not work right)
    @OptimisticLock(excluded = true)
    @OneToMany(mappedBy = "subscription", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<NotificationEvent> notificationEvents = new ArrayList<NotificationEvent>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.VALID;

    public List<NotificationEvent> getNotificationEvents() {
        return notificationEvents;
    }

    public void addNotificationEvent(NotificationEvent notificationEvent) {
        this.notificationEvents.add(notificationEvent);
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Date getLastNotifiedDate() {
        return lastNotifiedDate;
    }

    public void setLastNotifiedDate(Date lastNotifiedDate) {
        this.lastNotifiedDate = lastNotifiedDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Subscription))
            return false;
        Subscription theOther = (Subscription) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.appendSuper(getPublisher().equals(obj));
        builder.appendSuper(getSubscriber().equals(obj));

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.appendSuper(publisher.hashCode());
        builder.appendSuper(subscriber.hashCode());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("lastNotifiedDate", getLastNotifiedDate());
        builder.append("#events", notificationEvents.size());
        return builder.toString();
    }

    public void removeNotificationEvent(NotificationEvent notificationEvent) {
        notificationEvents.remove(notificationEvent);
    }

}
