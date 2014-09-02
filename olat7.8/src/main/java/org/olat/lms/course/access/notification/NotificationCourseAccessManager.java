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
package org.olat.lms.course.access.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.access.CourseAccessEvaluator;
import org.olat.lms.course.access.CourseAccessManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This synchronizes/corrects the data before the notification job is started.
 * <p>
 * 
 * Synchronization subtasks: <br/>
 * - removes the subscribers for the deleted identities <br/>
 * - deletes publishers for a deleted, closed or private course <br/>
 * - deletes publishers for a deleted course node <br/>
 * - invalidates subscription if the visibility/access was restricted for a subscriber <br/>
 * - deletes old events (older than 31 days - the life span of an event is configurable in olat.properties via property notification.news.days)
 * 
 * Initial Date: 21.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class NotificationCourseAccessManager implements CourseAccessManager {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    NotificationLearnService notificationLearnServiceImpl;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    CourseAccessEvaluator courseAccessEvaluatorImpl;

    DB dBImpl;

    Map<Long, ICourse> repositoryEntryToCourseCache; // Course cache during execute() method call, this could be improved!!!

    /**
     * package visibility for testing purposes
     */
    NotificationCourseAccessManager() {
        // initialize cache
        repositoryEntryToCourseCache = new HashMap<Long, ICourse>();
    }

    @Override
    public void execute() {
        List<Long> subscriberIds = notificationLearnServiceImpl.getAllSubscriberKeys();

        removeInvalidSubscribers();
        removeInvalidContexts(subscriberIds);
        invalidateUnaccessibleSubscriptionsForSubscribers(subscriberIds);
        removeOldEvents();

        // cleanup cache
        repositoryEntryToCourseCache.clear();
    }

    private void removeOldEvents() {
        notificationLearnServiceImpl.removeOldEvents();
    }

    /**
     * An invalid subscriber is a subscriber for a deleted identity.
     */
    private void removeInvalidSubscribers() {
        notificationLearnServiceImpl.deleteInvalidSubscribers();
    }

    /**
     * Finds and removes publishers for the invalid contexts. An invalid context is a course which was either deleted, closed or became private.
     */
    Set<Long> removeInvalidContexts(List<Long> subscriberIds) {
        log.debug("removeInvalidContexts started");
        Set<Long> unaccessibleCourseIds = new HashSet<Long>();

        for (Long subscriberId : subscriberIds) {
            List<NotificationSubscriptionContext> notificationCourseAccessTOs = notificationLearnServiceImpl.getNotificationSubscriptionContexts(subscriberId);
            Set<Long> subscribedCourseIds = getSubscribedCourseIds(notificationCourseAccessTOs);
            addUnaccessibleCourseIds(subscribedCourseIds, unaccessibleCourseIds);
            notificationLearnServiceImpl.removePublishers(unaccessibleCourseIds);
        }
        log.debug("removeInvalidContexts finished");
        return unaccessibleCourseIds;
    }

    /**
     * Finds and removes unaccessible subscriptions for this subscriber.
     */
    private void invalidateUnaccessibleSubscriptionsForSubscribers(List<Long> subscriberIds) {
        log.info("invalidateUnaccessibleSubscriptionsForSubscribers started - subscriberIds: " + subscriberIds.size());
        for (Long subscriberId : subscriberIds) {
            List<NotificationSubscriptionContext> notAccessibleNotificationSubscriptionContexts = getUnaccesibleSubscriptionsForSubscriber(subscriberId);
            invalidateUnaccessibleSubscriptions(notAccessibleNotificationSubscriptionContexts);
        }
        log.info("invalidateUnaccessibleSubscriptionsForSubscribers finished");
    }

    /**
     * catches the possible RuntimeException triggered by invalidateUnaccesibleSubscriptions - for one subscriber.
     */
    private void invalidateUnaccessibleSubscriptions(List<NotificationSubscriptionContext> notAccessibleNotificationSubscriptionContexts) {
        try {
            if (!notAccessibleNotificationSubscriptionContexts.isEmpty()) {
                log.info("invalidateUnaccessibleSubscriptions for one subscriber: " + notAccessibleNotificationSubscriptionContexts.size());
                notificationLearnServiceImpl.invalidateUnaccesibleSubscriptions(notAccessibleNotificationSubscriptionContexts);
            }
        } catch (RuntimeException e) {
            log.error("invalidateUnaccesibleSubscriptions failed: ", e);
        }
    }

    /**
     * gets the list with unaccessible subscription for this subscriber, these have to be invalidated.
     */
    List<NotificationSubscriptionContext> getUnaccesibleSubscriptionsForSubscriber(Long subscriberId) {
        List<NotificationSubscriptionContext> notificationCourseAccessTOs = notificationLearnServiceImpl.getNotificationSubscriptionContexts(subscriberId);
        List<NotificationSubscriptionContext> notAccessiblenotificationCourseAccessTOs = new ArrayList<NotificationSubscriptionContext>();
        Map<Long, RepositoryEntry> contextIdIsAccessibleMap = getCoursesAccesibleForIdentity(notificationCourseAccessTOs);

        for (NotificationSubscriptionContext notificationCourseAccessTO : notificationCourseAccessTOs) {
            RepositoryEntry courseRepositoryEntry = contextIdIsAccessibleMap.get(notificationCourseAccessTO.getContextId());
            if (courseRepositoryEntry == null) {
                notAccessiblenotificationCourseAccessTOs.add(notificationCourseAccessTO);
            } else if (!isSubscriptionAccessible(notificationCourseAccessTO)) {
                notAccessiblenotificationCourseAccessTOs.add(notificationCourseAccessTO);
            }
        }
        return notAccessiblenotificationCourseAccessTOs;
    }

    /**
     * Returns a map with accessible repository entries. It checks only once if isCourseAccesibleForIdentity.
     */
    Map<Long, RepositoryEntry> getCoursesAccesibleForIdentity(List<NotificationSubscriptionContext> notificationCourseAccessTOs) {
        Set<Long> contextIdSet = new HashSet<Long>(); // caches all contextIds for a subscriber

        Map<Long, RepositoryEntry> onlyAccessibleCourses = new HashMap<Long, RepositoryEntry>();
        for (NotificationSubscriptionContext notificationCourseAccessTO : notificationCourseAccessTOs) {
            if (!contextIdSet.contains(notificationCourseAccessTO.getContextId())) {
                RepositoryEntry repositoryEntry = isCourseAccesibleForIdentity(notificationCourseAccessTO);
                contextIdSet.add(notificationCourseAccessTO.getContextId());
                if (repositoryEntry != null) {
                    onlyAccessibleCourses.put(notificationCourseAccessTO.getContextId(), repositoryEntry);
                }
            }
        }
        return onlyAccessibleCourses;
    }

    /**
     * Returns the RepositoryEntry if the course is accessible for this identity (see notificationSubscriptionContext), null otherwise.
     * 
     */
    private RepositoryEntry isCourseAccesibleForIdentity(NotificationSubscriptionContext notificationSubscriptionContext) {
        RepositoryEntry courseRepositoryEntry = lookupRepositoryEntryAndCommit(notificationSubscriptionContext.getContextId());

        if (courseRepositoryEntry != null) {
            ICourse course = getCourseFromCacheOrLoad(courseRepositoryEntry);
            if (courseAccessEvaluatorImpl.isCourseAccessibleForIdentity(notificationSubscriptionContext.getIdentity(), courseRepositoryEntry, course, false)) {
                commitDBImplTransaction();
                return courseRepositoryEntry;
            }
        } else {
            commitDBImplTransaction();
            // e.g. the course was deleted
            // TODO: delete publishers for this context?
            return null;
        }
        commitDBImplTransaction();
        return null;
    }

    /**
     * 
     * Gets RepositoryEntry from the local cache (contextIdToRepositoryEntryCache) or loads and puts it into cache. <br/>
     * Always use this method to lookupRepositoryEntry from repositoryService, since it caches the RepositoryEntry.
     */
    RepositoryEntry lookupRepositoryEntryAndCommit(Long contextId) {
        RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(contextId);
        // commitDBImplTransaction();
        return repositoryEntry;
    }

    /**
     * A subscription gets unaccessible if the course node is deleted or if the visibility/access rules are restricted for this identity.
     */
    private boolean isSubscriptionAccessible(NotificationSubscriptionContext notificationSubscriptionContext) {
        RepositoryEntry courseRepositoryEntry = repositoryService.lookupRepositoryEntry(notificationSubscriptionContext.getContextId());

        if (courseRepositoryEntry != null) {
            ICourse course = getCourseFromCacheOrLoad(courseRepositoryEntry);

            boolean isCourseNodeStillAvailable = isCourseNodeStillAvailable(course, String.valueOf(notificationSubscriptionContext.getSubcontextId()));
            if (!isCourseNodeStillAvailable) {
                return false; // e.g. node was deleted
            }
            CourseNode courseNode = getCourseNode(course, String.valueOf(notificationSubscriptionContext.getSubcontextId()));
            return courseAccessEvaluatorImpl.isCourseNodeAccessibleForIdentity(notificationSubscriptionContext.getIdentity(), course, courseNode, false);
        } else {
            // e.g. the course was deleted
            // TODO: delete publishers for this context?
            return false;
        }
    }

    /**
     * This commits a transaction opened by DBImpl.beginTransaction(). <br/>
     * This was introduced as fix for frequent "Overdue resource check-out stack trace" error.
     */
    private void commitDBImplTransaction() {
        if (dBImpl == null) { // this is done to allow mocking dBImpl at testing
            dBImpl = DBFactory.getInstance(false);
        }
        dBImpl.intermediateCommit();
    }

    /**
     * Gets course from the local cache (repositoryEntryToCourseCache) or loads and puts it into cache.
     */
    ICourse getCourseFromCacheOrLoad(RepositoryEntry courseRepositoryEntry) {
        ICourse course = repositoryEntryToCourseCache.get(courseRepositoryEntry.getResourceableId());
        if (course == null) {
            OLATResourceable oLATResourceable = courseRepositoryEntry.getOlatResource();
            if (oLATResourceable != null) {
                course = CourseFactory.loadCourse(oLATResourceable);
                repositoryEntryToCourseCache.put(courseRepositoryEntry.getResourceableId(), course);
            }
        }
        return course;
    }

    private boolean isCourseNodeStillAvailable(ICourse course, String nodeId) {
        log.debug("isCourseNodeStillAvailable: " + nodeId + " " + String.valueOf(course.getEditorTreeModel().getNodeById(nodeId) != null));
        return course.getEditorTreeModel().getNodeById(nodeId) != null;
    }

    private CourseNode getCourseNode(ICourse course, String nodeId) {
        /** TODO: visibility task : bb/22.06.2012 **/
        return course.getRunStructure().getNode(nodeId);
    }

    /**
     * Adds the subscribed context ids to the returned set.
     */
    private Set<Long> getSubscribedCourseIds(List<NotificationSubscriptionContext> contexts) {
        Set<Long> subscribedCourseIds = new HashSet<Long>();
        for (Iterator<NotificationSubscriptionContext> contextIterator = contexts.iterator(); contextIterator.hasNext();) {
            NotificationSubscriptionContext context = contextIterator.next();
            subscribedCourseIds.add(context.getContextId());
        }
        return subscribedCourseIds;
    }

    /**
     * Checks if the subscribedCourseIds contains any deleted/close/private course id, if so adds this to the unaccessibleCourseIds.
     */
    private void addUnaccessibleCourseIds(Set<Long> subscribedCourseIds, Set<Long> unaccessibleCourseIds) {
        // remove the ids which were already checked
        subscribedCourseIds.removeAll(unaccessibleCourseIds);
        for (Iterator<Long> idIterator = subscribedCourseIds.iterator(); idIterator.hasNext();) {
            Long courseId = idIterator.next();
            RepositoryEntry courseRepositoryEntry = repositoryService.lookupRepositoryEntry(courseId);
            commitDBImplTransaction();
            if (isCourseDeleted(courseRepositoryEntry) || isCourseClosed(courseRepositoryEntry) || isCoursePrivate(courseRepositoryEntry)) {
                unaccessibleCourseIds.add(courseId);
            }
        }
    }

    private boolean isCourseDeleted(RepositoryEntry courseRepositoryEntry) {
        if (courseRepositoryEntry == null) {
            return true;
        }
        return false;
    }

    private boolean isCourseClosed(RepositoryEntry courseRepositoryEntry) {
        if (courseRepositoryEntry != null) {
            // TODO: why createRepositoryEntryStatus
            return repositoryService.createRepositoryEntryStatus(courseRepositoryEntry.getStatusCode()).isClosed();
        }
        return false;
    }

    /**
     * A course is private if its access code is less than RepositoryEntry.ACC_USERS.
     */
    private boolean isCoursePrivate(RepositoryEntry courseRepositoryEntry) {
        if (courseRepositoryEntry != null) {
            return courseRepositoryEntry.getAccess() < RepositoryEntry.ACC_USERS;
        }
        return false;
    }
}
