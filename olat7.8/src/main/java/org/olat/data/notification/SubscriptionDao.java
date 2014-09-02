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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.dao.GenericDao;
import org.olat.system.commons.Retryable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 29.11.2011 <br>
 * 
 * @author cg
 */
@Repository
public class SubscriptionDao {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    GenericDao<Subscription> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Subscription.class);
    }

    public Subscription createOrReuseSubscription(Subscriber subscriber, Publisher publisher) {
        Subscription subscription = getSubscription(subscriber, publisher);
        if (subscription == null) {
            log.info("createAndSaveSubscription started with subscriber=" + subscriber + " publisher=" + publisher);
            subscription = new Subscription();
            subscription.setSubscriber(subscriber);
            subscription.setPublisher(publisher);
            // we have to add Subscription after setting subscriber and publisher
            subscriber.addSubscription(subscription);
            publisher.addSubscription(subscription);

            subscription.setCreationDate(new Date());
            subscription = genericDao.save(subscription);
        }
        /** reuse existing invalid subscription */
        else {
            subscription.setStatus(Subscription.Status.VALID);
            subscription = genericDao.update(subscription);
        }
        return subscription;
    }

    public Subscription getSubscription(Subscriber subscriber, Publisher publisher) {
        Map<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put("publisher", publisher);
        restrictionMap.put("subscriber", subscriber);
        List<Subscription> subscriptions = genericDao.findByCriteria(restrictionMap);
        return getSubscriptionFrom(subscriptions, subscriber, publisher);
    }

    Subscription getSubscriptionFrom(List<Subscription> subscriptions, Subscriber subscriber, Publisher publisher) {
        if (subscriptions.isEmpty()) {
            return null;
        } else if (subscriptions.size() == 1) {
            return subscriptions.get(0);
        } else {
            throw new AssertException("No unique subsciption for subscriber=" + subscriber + " and publisher=" + publisher);
        }
    }

    public void deleteSubscription(Subscription subscription) {
        subscription.getPublisher().removeSubscription(subscription);
        genericDao.delete(subscription);
    }

    public Subscription updateSubscription(Subscription subscription) {
        return genericDao.update(subscription);
    }

    @Retryable
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateLastNotifiedDateByIds(Set<Long> ids, Date date) {
        Query query = genericDao.getNamedQuery(Subscription.UPDATE_LASTNOTIFIEDDATE_BY_IDS);
        query.setParameterList("ids", ids);
        query.setTimestamp("date", date);
        return query.executeUpdate();
    }

    public List<Subscription> getSubscriptionsForIdentity(Identity identity) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Subscriber.IDENTITY_PARAM, identity);
        parameters.put(Subscription.SUBSCRIPTION_STATUS_PARAM, Subscription.Status.VALID);
        return genericDao.getNamedQueryListResult(Subscription.GET_SUBSCRIPTIONS_FOR_IDENTITY, parameters);
    }

    public List<Subscription> getSubscriptionsForSubscriberId(Long subscriberId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Subscriber.SUBSCRIBER_ID_PARAM, subscriberId);
        parameters.put(Subscription.SUBSCRIPTION_STATUS_PARAM, Subscription.Status.VALID);
        return genericDao.getNamedQueryListResult(Subscription.GET_SUBSCRIPTIONS_FOR_SUBSCRIBER_ID, parameters);
    }

}
