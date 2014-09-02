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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.dao.GenericDao;
import org.olat.system.commons.Retryable;
import org.olat.system.commons.date.DateFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 30.11.2011 <br>
 * 
 * @author lavinia
 */
@Repository
public class NotificationEventDao {

    @Autowired
    private GenericDao<NotificationEvent> genericDao;
    @Autowired
    NotificationEventCreator notificationEventCreator;

    @PostConstruct
    void initType() {
        genericDao.setType(NotificationEvent.class);
    }

    public List<NotificationEvent> createAndSaveNotificationEvents(Publisher publisher, Map<String, String> attributes) {
        List<NotificationEvent> list = new ArrayList<NotificationEvent>();
        if (publisher == null) {
            return list;
        }

        // TODO: replace with getValidSubscriptions()
        Set<Subscription> subscriptionSet = publisher.getSubscriptions();
        for (Subscription subscription : subscriptionSet) {
            boolean isSubscriptionValid = Subscription.Status.VALID.equals(subscription.getStatus());
            if (isSubscriptionValid && isEventValid(subscription, attributes)) {
                NotificationEvent persistedEvent = notificationEventCreator.createAndSaveNotificationEvent(attributes, subscription);
                list.add(persistedEvent);
            }
        }

        return list;
    }

    /**
     * a valid event should have different creator and subscriber.
     */
    private boolean isEventValid(Subscription subscription, Map<String, String> attributes) {
        String subscriberUsername = subscription.getSubscriber().getIdentity().getName();
        String creatorUsername = attributes.get(NotificationEvent.Attribute.CREATOR_USERNAME.name());
        return !subscriberUsername.equals(creatorUsername);
    }

    public List<NotificationEvent> findNotificationEvents(NotificationEvent.Status eventStatus) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("status", eventStatus);
        return genericDao.findByCriteria(restrictionMap);
    }

    public List<NotificationEvent> findAllNotificationEvents() {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        List<NotificationEvent> events = genericDao.findByCriteria(restrictionMap);
        return events;
    }

    void updateEvent(NotificationEvent notificationEvent) {
        genericDao.update(notificationEvent);
    }

    @Retryable
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEvents(List<NotificationEvent> events, NotificationEvent.Status status) {
        for (NotificationEvent event : events) {
            event = genericDao.findById(event.getId());
            event.setStatus(status);
            event.getSubscription().setLastNotifiedDate(new Date());
            updateEvent(event);
        }
    }

    @Retryable
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateEventsByIds(Set<Long> ids, NotificationEvent.Status status) {
        Query query = genericDao.getNamedQuery(NotificationEvent.UPDATE_EVENT_STATUS_BY_IDS);
        query.setParameterList(NotificationEvent.IDS_PARAM, ids);
        query.setParameter(NotificationEvent.EVENT_STATUS_PARAM, status);
        return query.executeUpdate();
    }

    public List<NotificationEvent> getEventsForIdentity(Identity identity, DateFilter dateFilter) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(NotificationEvent.IDENTITY_ID_PARAM, identity);
        parameters.put(NotificationEvent.DATE_FROM_PARAM, dateFilter.getFromDate());
        parameters.put(NotificationEvent.DATE_TO_PARAM, dateFilter.getToDate());
        return genericDao.getNamedQueryListResult(NotificationEvent.GET_EVENTS_FOR_IDENTITY, parameters);
    }

    /**
     * It won't find all events but the last relevant events, that is if there are many events for <br/>
     * the same publisher and SOURCE_ENTRY_ID, it only gets the latest (the other are obsolete). <br/>
     * Use case sample: there could be several events for the same forum message, only the last one is considered.<br/>
     * 
     * It sorts the events: <br/>
     * 1. after the creation date <br/>
     * 2. after the id (in case several events with the same creation date)
     */
    public List<NotificationEvent> findNotificationEventsBySubscriber(Long subscriberId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(NotificationEvent.SUBSCRIBER_ID_PARAM, subscriberId);
        return genericDao.getNamedQueryListResult(NotificationEvent.GET_EVENTS_BY_SUBSCRIBER, parameters);
    }

    public List<Long> getEventsForUpdate(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(NotificationEvent.IDS_PARAM, ids);
            List<NotificationEvent> events = genericDao.getNamedQueryListResult(NotificationEvent.GET_EVENTS_FOR_UPDATE, parameters);
            List<Long> eventIds = new ArrayList<Long>();
            for (NotificationEvent event : events) {
                eventIds.add(event.getId());
            }
            return eventIds;
        }
        return new ArrayList<Long>();
    }

    public void deleteOldEvents(Date notificationNewsDate) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(NotificationEvent.NOTIFICATION_NEWS_DATE_PARAM, notificationNewsDate);
        List<NotificationEvent> oldEvents = genericDao.getNamedQueryListResult(NotificationEvent.GET_OLD_EVENTS, parameters);
        for (NotificationEvent event : oldEvents) {
            event.getSubscription().removeNotificationEvent(event);
            genericDao.delete(event);
        }
    }

}
