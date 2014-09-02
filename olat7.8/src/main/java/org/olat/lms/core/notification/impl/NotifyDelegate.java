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
package org.olat.lms.core.notification.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.notification.NotificationEvent;
import org.olat.data.notification.NotificationEventDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.impl.channel.ChannelChain;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notifies subscriber and updates events.
 * 
 * Initial Date: 21.03.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class NotifyDelegate {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    ChannelChain channelChain;
    @Autowired
    NotificationConverter converter;
    @Autowired
    SubscriberDao subscriberDao;
    @Autowired
    NotificationEventDao eventDao;
    @Autowired
    SubscriptionDao subscriptionDao;

    /**
     * Sorts events, sends notification to this subscriber and updates events.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotifyStatistics notifySubscriber(Long subscriberId) {
        NotifyStatistics statistics = new NotifyStatistics();

        if (subscriberId == null) {
            return statistics;
        }

        List<NotificationEvent> events = getEvents(subscriberId);
        if (events.isEmpty()) {
            return statistics;
        }

        List<NotificationEventTO> eventTOs = converter.toEventTOList(events);
        if (eventTOs.isEmpty()) {
            return statistics;
        }

        Collections.sort(eventTOs, new NotificationEventTO.NotificationEventTOComparator());
        statistics = send(eventTOs.get(0).getEvent().getSubscription().getSubscriber(), eventTOs);
        updateEvents(eventTOs, statistics);

        return statistics;
    }

    private Iterator<Long> getSubscribersIdIterator() {
        return subscriberDao.getSubscriberIDsByEventStatus(NotificationEvent.Status.WAITING).iterator();
    }

    protected List<Long> getSubscribersIDs() {
        return subscriberDao.getSubscriberIDsByEventStatus(NotificationEvent.Status.WAITING);
    }

    private Subscriber findSubscriber(Long id) {
        return subscriberDao.findSubscriber(id);
    }

    private List<NotificationEvent> getEvents(Long subscriberId) {
        return eventDao.findNotificationEventsBySubscriber(subscriberId);
    }

    private NotifyStatistics send(Subscriber subscriber, List<NotificationEventTO> notificationEventTOList) {
        return channelChain.send(subscriber, notificationEventTOList);
    }

    private void updateEvents(List<NotificationEventTO> eventTOs, NotifyStatistics statistics) {
        if (statistics != null) {
            updateEventsAndSubscriptions(eventTOs, getStatus(statistics));
        }
    }

    public Iterator<Subscriber> getSubscribersIterator() {
        return new SubscribersIterator();
    }

    private NotificationEvent.Status getStatus(NotifyStatistics statistics) {
        // TODO(AA): THE ENTITY NotificationEvent DOESN'S SUPPORT MULTI-CHANNEL, THAT's WHY USING ONLY EMAIL FOR UPDATING THE STATUS
        return (statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL)) ? NotificationEvent.Status.DELIVERED : NotificationEvent.Status.FAILED;
    }

    private void updateEventsAndSubscriptions(List<NotificationEventTO> eventTOs, NotificationEvent.Status status) {
        // PREPARING THE EVENT IDS FOR UPDATING THE Events WITH THE GIVEN STATUS
        Set<Long> eventIds = new HashSet<Long>();
        // PREPARING THE SUBSCRIPTION IDS FOR UPDATING THE Subscriptions WITH THE lastNotifiedDate
        Set<Long> subscriptionIds = new HashSet<Long>();
        for (NotificationEventTO eventTO : eventTOs) {
            eventIds.add(eventTO.getEvent().getId());
            subscriptionIds.add(eventTO.getEvent().getSubscription().getId());
        }
        /** Could be a performance issue, for now is ok. **/
        Set<Long> ids = new HashSet<Long>(eventDao.getEventsForUpdate(new ArrayList<Long>(eventIds)));
        int updatedEvents = eventDao.updateEventsByIds(ids, status);
        int updatedSubscriptions = subscriptionDao.updateLastNotifiedDateByIds(subscriptionIds, new Date());

        log.info("Update result for this subscriber:[" + eventTOs.get(0).getEvent().getSubscription().getSubscriber().getId() + "] [EVENTS: " + updatedEvents
                + ", SUBSCRIPTIONS: " + updatedSubscriptions + "]");

    }

    /**
     * This Iterator serves as a way to implement different strategies how to get the Subscribers in a transparent way to the caller (notifySubscribers). <br/>
     * Current implementation: Get the subscriber's ids first, and the next subscriber just in the next() method.
     */
    /** this is candidate for template class like implements Iterator<T> and therefore useful for any entity **/
    class SubscribersIterator implements Iterator<Subscriber> {

        Iterator<Long> iterator;

        public SubscribersIterator() {
            iterator = getSubscribersIdIterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * could return null, if no subscriber with this id was found (in case that the subscriber was deleted in the meantime)
         */
        @Override
        public Subscriber next() {
            Long subscriberID = iterator.next();
            return findSubscriber(subscriberID);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
