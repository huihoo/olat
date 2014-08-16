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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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

    @Override
    public void execute() {
        List<Long> subscriberIds = notificationLearnServiceImpl.getAllSubscriberKeys();

        removeInvalidSubscribers();
        removeInvalidContexts(subscriberIds);
        invalidateUnaccessibleSubscriptionsForSubscribers(subscriberIds);
        removeOldEvents();

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
        log.debug("invalidateUnaccessibleSubscriptionsForSubscribers finished");
    }

    /**
     * catches the possible RuntimeException triggered by invalidateUnaccesibleSubscriptions - for one subscriber.
     */
    private void invalidateUnaccessibleSubscriptions(List<NotificationSubscriptionContext> notAccessibleNotificationSubscriptionContexts) {
        try {
            if (!notAccessibleNotificationSubscriptionContexts.isEmpty()) {
                log.info("invalidateUnaccessibleSubscriptions: " + notAccessibleNotificationSubscriptionContexts.size());
                notificationLearnServiceImpl.invalidateUnaccesibleSubscriptions(notAccessibleNotificationSubscriptionContexts);
            }
        } catch (RuntimeException e) {
            log.error("invalidateUnaccesibleSubscriptions failed: ", e);
        }
    }

    private List<NotificationSubscriptionContext> getUnaccesibleSubscriptionsForSubscriber(Long subscriberId) {
        List<NotificationSubscriptionContext> notificationCourseAccessTOs = notificationLearnServiceImpl.getNotificationSubscriptionContexts(subscriberId);
        List<NotificationSubscriptionContext> notAccessiblenotificationCourseAccessTOs = new ArrayList<NotificationSubscriptionContext>();
        for (NotificationSubscriptionContext notificationCourseAccessTO : notificationCourseAccessTOs) {
            if (!isSubscriptionAccessible(notificationCourseAccessTO)) {
                notAccessiblenotificationCourseAccessTOs.add(notificationCourseAccessTO);
            }
        }
        return notAccessiblenotificationCourseAccessTOs;
    }

    /**
     * A subscription gets unaccessible if the course node is deleted or if the visibility/access rules are restricted for this identity.
     */
    private boolean isSubscriptionAccessible(NotificationSubscriptionContext notificationSubscriptionContext) {
        RepositoryEntry courseRepositoryEntry = repositoryService.lookupRepositoryEntry(notificationSubscriptionContext.getContextId());
        commitDBImplTransaction();

        if (courseRepositoryEntry != null) {
            ICourse course = loadCourseFromRepositoryEntry(courseRepositoryEntry);
            boolean isCourseNodeStillAvailable = isCourseNodeStillAvailable(course, String.valueOf(notificationSubscriptionContext.getSubcontextId()));
            if (!isCourseNodeStillAvailable) {
                return false; // e.g. node was deleted
            }
            CourseNode courseNode = getCourseNode(course, String.valueOf(notificationSubscriptionContext.getSubcontextId()));
            return courseAccessEvaluatorImpl.isCourseAccesibleForIdentity(notificationSubscriptionContext.getIdentity(), courseRepositoryEntry, course)
                    && courseAccessEvaluatorImpl.isCourseNodeAccesibleForIdentity(notificationSubscriptionContext.getIdentity(), course, courseNode);
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
        DBFactory.getInstance(false).commit();
    }

    private ICourse loadCourseFromRepositoryEntry(RepositoryEntry courseRepositoryEntry) {
        OLATResourceable course = courseRepositoryEntry.getOlatResource();
        return CourseFactory.loadCourse(course);
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
