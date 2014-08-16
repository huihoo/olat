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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 29.11.2011 <br>
 * 
 * @author lavinia
 */
@Repository
public class PublisherDao {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private GenericDao<Publisher> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(Publisher.class);
    }

    public Publisher findPublisher(Long contextId, ContextType contextType, Long sourceId, String sourceType) {
        HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put(Publisher.CONTEXT_TYPE_PARAM, contextType);
        restrictionMap.put(Publisher.CONTEXT_ID_PARAM, contextId);
        restrictionMap.put(Publisher.SOURCE_TYPE_PARAM, sourceType);
        restrictionMap.put(Publisher.SOURCE_ID_PARAM, sourceId);
        List<Publisher> publishers = genericDao.findByCriteria(restrictionMap);
        if (publishers.isEmpty()) {
            return null;
        } else if (publishers.size() == 1) {
            return publishers.get(0);
        } else {
            throw new AssertException("Multiple publishers found for contextId: " + contextId + " and sourceId: " + sourceId);
        }
    }

    // TODO: What should be return when no publisher exist ? null, an object, NullPublisher ?
    public Publisher findPublisher(String sourceType, Long sourceId) {
        Criteria criteria = genericDao.createCriteria();
        criteria.add(Restrictions.eq(Publisher.SOURCE_TYPE_PARAM, sourceType));
        criteria.add(Restrictions.eq(Publisher.SOURCE_ID_PARAM, sourceId));
        List<Publisher> publishers = criteria.list();
        if (publishers.isEmpty()) {
            return null;
        } else if (publishers.size() == 1) {
            return publishers.get(0);
        } else {
            throw new AssertException("No unique publisher for type=" + sourceType + " and resourceableId=" + sourceId);
        }
    }

    /**
     * just for testing
     */
    List<Publisher> findAll() {
        return genericDao.findAll();
    }

    public Publisher savePublisher(Publisher publisher) {
        return genericDao.save(publisher);
    }

    /**
     * This should delete the publisher together with its subscriptions and notificationEvents, the subscriptions are deleted explicitly.
     */
    public void deletePublisher(Publisher publisher) {
        log.info("deletePublisher - publisher.getContextId(): " + publisher.getContextId());
        // remove first subscription from the other parent
        Set<Subscription> subscriptions = publisher.getSubscriptions();
        for (Iterator<Subscription> iterator = subscriptions.iterator(); iterator.hasNext();) {
            Subscription subscription = iterator.next();
            subscription.getSubscriber().removeSubscription(subscription);
        }
        genericDao.delete(publisher);
    }

    public Publisher createAndSavePublisher(Long contextId, ContextType contextType, Long sourceId, String sourceType, Long subcontextId) {
        Publisher publisher = new Publisher();
        publisher.setContextId(contextId);
        publisher.setContextType(contextType);
        publisher.setSourceId(sourceId);
        publisher.setSourceType(sourceType);
        publisher.setSubcontextId(subcontextId);

        log.info("createAndSavePublisher with contextId: " + contextId + " , sourceId: " + sourceId + " and subcontextId: " + subcontextId);
        return savePublisher(publisher);
    }

    public void removePublishers(Set<Long> contextIds) {
        for (Iterator<Long> idIterator = contextIds.iterator(); idIterator.hasNext();) {
            List<Publisher> publishers = findPublishers(idIterator.next());
            for (Iterator<Publisher> publisherIterator = publishers.iterator(); publisherIterator.hasNext();) {
                this.deletePublisher(publisherIterator.next());
            }
        }
    }

    private List<Publisher> findPublishers(Long contextId) {
        HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
        restrictionMap.put(Publisher.CONTEXT_ID_PARAM, contextId);
        return genericDao.findByCriteria(restrictionMap);
    }

}
