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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;

/**
 * User profile for notification. <br/>
 * There is exactly 0 or 1 subscriber for each user.
 * 
 * Initial Date: 25.11.2011 <br>
 * 
 * @author lavinia
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Subscriber.IS_SUBSCRIBED, query = "select count(*) as count from Subscription s inner join s.subscriber as sub inner join s.publisher as p where sub.identity = :"
                + Subscriber.IDENTITY_PARAM
                + " and p.contextId = :"
                + Publisher.CONTEXT_ID_PARAM
                + " and p.contextType = :"
                + Publisher.CONTEXT_TYPE_PARAM
                + " and p.sourceId = :"
                + Publisher.SOURCE_ID_PARAM
                + " and p.sourceType = :"
                + Publisher.SOURCE_TYPE_PARAM
                + " and s.status = :"
                + Subscription.SUBSCRIPTION_STATUS_PARAM),
        @NamedQuery(name = Subscriber.GET_SUBSCRIBER_IDS_BY_EVENT_STATUS, query = "select s.id from Subscriber s " + "where exists (select ne.id "
                + "from NotificationEvent ne where ne.subscription.subscriber.id = s.id " + "and ne.status = :status " + "and ne.subscription.status = '"
                + Subscription.STATUS_VALID + "')"),

        @NamedQuery(name = Subscriber.GET_ALL_SUBSCRIBER_IDS, query = "select id from Subscriber"),
        @NamedQuery(name = Subscriber.GET_INVALID_SUBSCRIBERS, query = "from Subscriber sub where sub.identity.status in (:" + Subscriber.IDENTITY_STATUS_PARAM + ")") })
@Table(name = "sy_subscriber", uniqueConstraints = @UniqueConstraint(columnNames = { "identity_id" }))
public class Subscriber {

    public static final String GET_ALL_SUBSCRIBER_IDS = "getAllSubscriberIds";

    // notification channel
    public enum Channel {
        EMAIL;

    }

    // how often chooses the user to be notified
    public enum NotificationInterval {
        IMMEDIATELY, HOURLY, DAILY, NEVER;
    }

    // user could subscribe everything in a context (subscribes an entire course, that is all publishers of a course), or just a selection of publishers.
    public enum SubscriptionOption {
        ALL, SELECTION;
    }

    public static final String IS_SUBSCRIBED = "isSubscribed";
    public static final String GET_SUBSCRIBER_IDS_BY_EVENT_STATUS = "getSubscribersByEventStatus";
    public static final String IDENTITY_PARAM = "identity";
    public static final String SUBSCRIBER_ID_PARAM = "subscriberId";
    public static final String GET_INVALID_SUBSCRIBERS = "getInvalidSubscribers";
    public static final String IDENTITY_STATUS_PARAM = "identityStatusParam";

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id = new Long(-1);

    @Version
    private Long version;

    @OneToOne
    @JoinColumn(name = "identity_id", nullable = false)
    private IdentityImpl identity;

    @CollectionOfElements
    @JoinTable(name = "sy_channel", joinColumns = @JoinColumn(name = "subscriber_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "value")
    private Set<Channel> channels = new HashSet<Channel>();

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_interval")
    private NotificationInterval interval;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_option")
    private SubscriptionOption option;

    @OneToMany(mappedBy = "subscriber")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<Subscription> subscriptions = new HashSet<Subscription>();

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = (IdentityImpl) identity;
    }

    public Set<Channel> getChannels() {
        return channels;
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public NotificationInterval getInterval() {
        return interval;
    }

    public void setInterval(NotificationInterval interval) {
        this.interval = interval;
    }

    public SubscriptionOption getOption() {
        return option;
    }

    public void setOption(SubscriptionOption option) {
        this.option = option;
    }

    public Long getId() {
        return id;
    }

    /**
     * An Identity has a single subscriber.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Subscriber))
            return false;
        Subscriber theOther = (Subscriber) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getIdentity().getName(), theOther.getIdentity().getName());
        // TODO: review this!

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(63, 111);
        builder.append(getIdentity().getName());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("identity-name", getIdentity().getName());
        return builder.toString();
    }

}
