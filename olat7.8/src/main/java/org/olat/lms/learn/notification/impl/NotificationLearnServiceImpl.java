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
package org.olat.lms.learn.notification.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.Subscription;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.core.notification.impl.NotificationSubscriptionContextFactory;
import org.olat.lms.core.notification.impl.UriBuilder;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.learn.LearnBaseService;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.learn.notification.service.SubscriptionTO;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.commons.Retryable;
import org.olat.system.commons.date.DateFilter;
import org.olat.system.commons.service.ServiceContext;
import org.olat.system.commons.service.ServiceMetric;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
@Service
// TODO: REVIEW DECLARATIVE RETRY bb/26.03.2012 : could be only Required used - imagine situation when caller need to call 2 service methods in one transaction and
// service at the
// moment does not support that
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class NotificationLearnServiceImpl extends LearnBaseService<ServiceMetric<ServiceContext>, ServiceContext> implements NotificationLearnService {
    private static final Logger log = LoggerHelper.getLogger();

    private final long numberOfMilisecondsPerDay = 86000000;

    @Autowired
    protected NotificationService notificationService;
    @Autowired
    NotificationSubscriptionContextFactory notificationSubscriptionContextFactory;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    UriBuilder uriBuilder;

    @Value("${notification.news.days}")
    private String numberOfNewsDays;

    @Autowired
    HibernateTransactionManager transactionManager;

    @PostConstruct
    public void checkTransactionManager() {
        if (transactionManager == null) {
            throw new AssertException("transactionManager is null", null);
        }

        // checkIfSubscribedToADummyPublisher();
    }

    /*
     * private void checkIfSubscribedToADummyPublisher() { Identity identity = baseSecurity.findIdentityByName("administrator"); final Long CONTEXT_ID = Long.valueOf(1);
     * final Long SOURCE_ID = Long.valueOf(2); final Long SUBCONTEXT_ID = Long.valueOf(3); String forumSourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
     * ContextType courseContextType = Publisher.ContextType.COURSE; NotificationSubscriptionContext notificationSubscriptionContext = new
     * NotificationSubscriptionContext(identity, forumSourceType, SOURCE_ID, courseContextType, CONTEXT_ID, SUBCONTEXT_ID);
     * 
     * boolean isSubscribed = this.isSubscribed(notificationSubscriptionContext); if (isSubscribed) {
     * log.info("@PostConstruct check was susscessful - another node could have already successfully subscribed"); } else { Subscription subscription =
     * this.subscribe(notificationSubscriptionContext); if (subscription == null || !CONTEXT_ID.equals(subscription.getPublisher().getContextId())) {
     * log.info("NotificationLearnServiceImpl - transaction check failed"); } } }
     */

    /**
     * just for testing
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    protected void setMetrics(List<ServiceMetric<ServiceContext>> metrics) {
        // we are not interested to set any metrics for this learn service, not yet
    }

    @Override
    public boolean isSubscribed(NotificationSubscriptionContext subscriptionContext) {
        log.info("call isSubscribed with subscriptionContext=" + subscriptionContext);
        return notificationService.isSubscribed(subscriptionContext);
    }

    /**
     * @Retryable since it could throw a ConstraintViolationException at create Publisher, if subscribe occurs concurrently.
     */
    @Override
    @Retryable
    public Subscription subscribe(NotificationSubscriptionContext subscriptionContext) {
        log.info("call subscribe with subscriptionContext=" + subscriptionContext);
        return notificationService.subscribe(subscriptionContext);
    }

    @Override
    @Retryable
    public void unSubscribe(NotificationSubscriptionContext subscriptionContext) {
        log.info("call unsubscribe with subscriptionContext=" + subscriptionContext);
        notificationService.unsubscribe(subscriptionContext);
    }

    public NotificationSubscriptionContext createNotificationSubscriptionContext(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData) {
        return notificationSubscriptionContextFactory.createNotificationSubscriptionContext(identity, subscriptionContext, publisherData);
    }

    @Override
    public List<UserNotificationEventTO> getNews(Identity identity, DateFilter dateFilter) {
        return notificationService.getNews(identity, dateFilter);
    }

    @Override
    public List<NotificationSubscriptionContext> getNotificationSubscriptionContexts(Long subscriberId) {
        return notificationService.getNotificationSubscriptionContexts(subscriberId);
    }

    @Override
    public List<Long> getAllSubscriberKeys() {
        return notificationService.getAllSubscriberKeys();
    }

    @Override
    @Retryable
    public void invalidateUnaccesibleSubscriptions(List<NotificationSubscriptionContext> notificationSubscriptionContexts) {
        notificationService.invalidateUnaccesibleSubscriptions(notificationSubscriptionContexts);
    }

    @Override
    public List<SubscriptionTO> getSubscriptions(Identity identity) {
        List<Subscription> subscriptions = notificationService.getSubscriptions(identity);
        return createSubscriptionTO(subscriptions);
    }

    private List<SubscriptionTO> createSubscriptionTO(List<Subscription> subscriptions) {
        List<SubscriptionTO> SubscriptionTOs = new ArrayList<SubscriptionTO>();
        Map<Long, RepositoryEntry> contextIdToRepositoryEntryMap = new HashMap<Long, RepositoryEntry>();
        Map<Long, ICourse> contextIdToCourseMap = new HashMap<Long, ICourse>();
        for (Iterator<Subscription> iterator = subscriptions.iterator(); iterator.hasNext();) {
            Subscription subscription = iterator.next();
            RepositoryEntry repositoryEntry = getRepositoryEntry(subscription.getPublisher().getContextId(), contextIdToRepositoryEntryMap);
            if (repositoryEntry != null) {
                // the repositoryEntry could be null indeed, if the course was deleted in the meantime
                String sourceType = subscription.getPublisher().getSourceType();
                ICourse course = getCourse(repositoryEntry, contextIdToCourseMap);
                CourseNode courseNode = getCourseNodeFrom(course, subscription.getPublisher().getSubcontextId());
                if (courseNode != null) {
                    // source could be null, if the course node was deleted in the meantime
                    String courseNodeTitle = getCourseNodeTitleFrom(course, subscription.getPublisher().getSubcontextId());
                    String courseTitle = getCourseTitleFrom(subscription.getPublisher().getContextId(), contextIdToRepositoryEntryMap);
                    OLATResource courseOlatResourcable = getRepositoryEntry(subscription.getPublisher().getContextId(), contextIdToRepositoryEntryMap).getOlatResource();
                    String courseNodeId = subscription.getPublisher().getSubcontextId().toString();
                    String contextUrl = uriBuilder.getURIToContext(subscription.getPublisher());
                    String publisherSourceUrl = uriBuilder.getURIToEventSource(subscription.getPublisher());
                    SubscriptionTOs.add(new SubscriptionTO(sourceType, courseNodeTitle, publisherSourceUrl, courseTitle, contextUrl, subscription));
                }
            }
        }
        return SubscriptionTOs;
    }

    private String getCourseNodeTitleFrom(ICourse course, Long subContextId) {
        return getCourseNodeFrom(course, subContextId).getShortTitle();
    }

    private CourseNode getCourseNodeFrom(ICourse course, Long subContextId) {
        CourseNode node = course.getRunStructure().getNode(subContextId.toString());
        return node;
    }

    /**
     * Loads course only is not found in the input map.
     */
    private ICourse getCourse(RepositoryEntry repositoryEntry, Map<Long, ICourse> contextIdToCourseMap) {
        ICourse course = contextIdToCourseMap.get(repositoryEntry.getOlatResource().getResourceableId());
        if (course == null) {
            course = CourseFactory.loadCourse(repositoryEntry.getOlatResource().getResourceableId());
            contextIdToCourseMap.put(repositoryEntry.getOlatResource().getResourceableId(), course);
        }
        return course;
    }

    /**
     * Loads repositoryEntry only if it is not found in the input map.
     */
    private RepositoryEntry getRepositoryEntry(Long contextId, Map<Long, RepositoryEntry> contextIdToRepositoryEntryMap) {
        RepositoryEntry repositoryEntry = contextIdToRepositoryEntryMap.get(contextId);
        if (repositoryEntry == null) {
            repositoryEntry = repositoryService.lookupRepositoryEntry(contextId);
            contextIdToRepositoryEntryMap.put(contextId, repositoryEntry);
        }
        return repositoryEntry;
    }

    private String getCourseTitleFrom(Long contextId, Map<Long, RepositoryEntry> contextIdToRepositoryEntryMap) {
        RepositoryEntry repositoryEntry = getRepositoryEntry(contextId, contextIdToRepositoryEntryMap);
        return repositoryEntry.getDisplayname();
    }

    @Override
    @Retryable
    public void unSubscribe(Subscription subscription) {
        notificationService.unsubscribe(subscription);
    }

    @Override
    public boolean isNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval) {
        return notificationService.isNotificationIntervalFor(identity, notificationInterval);
    }

    @Override
    public void setNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval) {
        notificationService.setNotificationIntervalFor(identity, notificationInterval);
    }

    @Override
    public void removePublishers(Set<Long> contextIds) {
        // TODO: does this need a performance test?
        notificationService.removePublishers(contextIds);
    }

    /**
     * The notification.news.days property in olat.properties configures how long an event is still new.
     */
    @Override
    public int getNumberOfNewsDays() {
        return Integer.parseInt(numberOfNewsDays);
    }

    @Override
    public NotifyStatistics notifySubscribers() {
        notificationService.startNotificationJob();

        NotifyStatistics notifyStatistics = new NotifyStatistics();
        List<Long> subscriberIds = notificationService.getSubscribersIds();
        Iterator<Long> subscribersIdsIter = subscriberIds.iterator();
        while (subscribersIdsIter.hasNext()) {
            notifyStatistics.add(notificationService.notifySubscriber(subscribersIdsIter.next()));
        }

        notificationService.finishNotificationJob();
        return notifyStatistics;
    }

    @Override
    public List<Long> getSubscribersIds() {
        return notificationService.getSubscribersIds();
    }

    @Override
    public NotifyStatistics notifySubscriber(Long subscriberId) {
        return notificationService.notifySubscriber(subscriberId);
    }

    @Override
    public void removeOldEvents() {
        notificationService.removeOldEvents(getNotificationNewsDate());

    }

    private Date getNotificationNewsDate() {
        Long numberOfNewsDaysInMiliseconds = Long.parseLong(numberOfNewsDays) * numberOfMilisecondsPerDay;
        return new Date(System.currentTimeMillis() - numberOfNewsDaysInMiliseconds);
    }

    @Override
    public void deleteInvalidSubscribers() {
        notificationService.deleteInvalidSubscribers();
    }

}
