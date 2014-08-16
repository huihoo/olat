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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAOs are simple enough to use the public interface as implicit interface, so no need for a ***DaoImpl, it is enough to have a ***Dao.
 * 
 * Initial Date: 29.11.2011 <br>
 * 
 * @author lavinia
 */
@Repository
public class SubscriberDao {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private GenericDao<Subscriber> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Subscriber.class);
    }

    /**
     * Defaults: <br/>
     * - interval: Subscriber.NotificationInterval.DAILY <br/>
     * - option: Subscriber.SubscriptionOption.SELECTION <br/>
     * - channel: Subscriber.Channel.EMAIL
     */
    public Subscriber createAndSaveSubscriber(Identity identity) {
        Subscriber subscriberEntity = genericDao.create();
        subscriberEntity.setIdentity(identity);
        // default settings
        subscriberEntity.setInterval(Subscriber.NotificationInterval.DAILY);
        subscriberEntity.setOption(Subscriber.SubscriptionOption.SELECTION);
        subscriberEntity.addChannel(Subscriber.Channel.EMAIL);
        log.info("createAndSaveSubscriber subscriberEntity=" + subscriberEntity);
        return genericDao.save(subscriberEntity);
    }

    public Subscriber updateSubscriber(Subscriber subscriber) {
        // TODO: implement
        // find subscriber, change attributes (channels, option, interval)
        // update subscriber
        return genericDao.update(subscriber);
    }

    public Subscriber findSubscriber(Identity identity) {

        HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("identity", identity);
        List<Subscriber> subscribers = genericDao.findByCriteria(restrictionMap);

        log.info("subscribers.size=" + subscribers.size());
        if (subscribers.isEmpty()) {
            return null;
        } else if (subscribers.size() == 1) {
            return subscribers.get(0);
        } else {
            // return subscribers.get(0);
            throw new AssertException("No unique subscriber for identity=" + identity.getName());
        }
    }

    /**
     * just for testing
     */
    List<Subscriber> findAll() {
        return genericDao.findAll();
    }

    /**
     * Deletes Subscriber of this Identity with all descendants.
     */
    public void deleteSubscriber(Identity identity) {
        Subscriber subscriber = findSubscriber(identity);
        deleteSubscriber(subscriber);
    }

    void deleteSubscriber(Subscriber subscriber) {
        if (subscriber == null) {
            return;
        }
        // remove first subscription from the other parent, else you get a "ObjectDeletedException: deleted object would be re-saved by cascade"
        Set<Subscription> suscriptions = subscriber.getSubscriptions();
        for (Iterator<Subscription> iterator = suscriptions.iterator(); iterator.hasNext();) {
            Subscription subscription = iterator.next();
            subscription.getPublisher().removeSubscription(subscription);
        }
        genericDao.delete(subscriber);
    }

    public boolean isSubscribed(Identity identity, Long contextId, ContextType contextType, Long sourceId, String sourceType) {
        Query query = genericDao.getNamedQuery(Subscriber.IS_SUBSCRIBED);
        query.setParameter(Subscriber.IDENTITY_PARAM, identity);
        query.setParameter(Publisher.CONTEXT_ID_PARAM, contextId);
        query.setParameter(Publisher.CONTEXT_TYPE_PARAM, contextType);
        query.setParameter(Publisher.SOURCE_ID_PARAM, sourceId);
        query.setParameter(Publisher.SOURCE_TYPE_PARAM, sourceType);
        query.setParameter(Subscription.SUBSCRIPTION_STATUS_PARAM, Subscription.Status.VALID);
        return ((Long) query.uniqueResult()).longValue() > 0 ? true : false;
    }

    public List<Long> getSubscriberIDsByEventStatus(NotificationEvent.Status eventStatus) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("status", eventStatus);
        /** TODO: REVIEW PUBLISH PERFORMANCE: bb/11.03.2012 **/
        return genericDao.getNamedQueryEntityIds(Subscriber.GET_SUBSCRIBER_IDS_BY_EVENT_STATUS, parameters);
    }

    public List<Long> getAllSubscriberIds() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        return genericDao.getNamedQueryEntityIds(Subscriber.GET_ALL_SUBSCRIBER_IDS, parameters);
    }

    public Subscriber findSubscriber(Long id) {
        return genericDao.findById(id);
    }

    /**
     * Only the subscribers corresponding to identities with status STATUS_DELETED are considered invalid.
     */
    public void deleteInvalidSubscribers() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        List<Integer> invalidStatusList = new ArrayList<Integer>();
        invalidStatusList.add(Identity.STATUS_DELETED);
        // invalidStatusList.add(Identity.STATUS_LOGIN_DENIED);
        parameters.put(Subscriber.IDENTITY_STATUS_PARAM, invalidStatusList);
        List<Subscriber> invalidSubscribers = genericDao.getNamedQueryListResult(Subscriber.GET_INVALID_SUBSCRIBERS, parameters);
        for (Subscriber subscriber : invalidSubscribers) {
            genericDao.delete(subscriber);
        }
    }

}
