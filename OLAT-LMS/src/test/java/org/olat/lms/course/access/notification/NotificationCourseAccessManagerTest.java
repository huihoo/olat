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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.repository.RepositoryEntryStatus;
import org.olat.lms.repository.RepositoryService;

/**
 * Initial Date: 25.05.2012 <br>
 * 
 * @author lavinia
 */
public class NotificationCourseAccessManagerTest {

    NotificationCourseAccessManager notificationCourseAccessManager;

    NotificationLearnService notificationLearnServiceMock;
    RepositoryService repositoryServiceMock;

    RepositoryEntry privateCourseMock1; // unaccessible course
    RepositoryEntry privateCourseMock2; // unaccessible course
    RepositoryEntry closedCourseMock; // unaccessible course
    RepositoryEntry okCourseMock; // accessible course

    Long contextId_privateCourse1 = new Long(1);
    Long contextId_privateCourse2 = new Long(11);
    // int statusCode = 2;
    Long contextId_deletedCourse = new Long(2);
    Long contextId_closedCourse = new Long(3);
    Long contextId_ok = new Long(4);
    List<NotificationSubscriptionContext> contextsOfSubscriber1;
    List<NotificationSubscriptionContext> contextsOfSubscriber2;

    Long subscriberId1 = new Long(5);
    Long subscriberId2 = new Long(6);
    List<Long> subscriberIds;

    @Before
    public void setUp() throws Exception {

        subscriberIds = new ArrayList<Long>();
        subscriberIds.add(subscriberId1);
        subscriberIds.add(subscriberId2);

        contextsOfSubscriber1 = new ArrayList<NotificationSubscriptionContext>();
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_privateCourse1);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_ok);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_deletedCourse);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_closedCourse);

        contextsOfSubscriber2 = new ArrayList<NotificationSubscriptionContext>();
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber2, contextId_privateCourse1);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber2, contextId_privateCourse2);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber2, contextId_ok);

        notificationLearnServiceMock = mock(NotificationLearnService.class);
        when(notificationLearnServiceMock.getNotificationSubscriptionContexts(subscriberId1)).thenReturn(contextsOfSubscriber1);
        when(notificationLearnServiceMock.getNotificationSubscriptionContexts(subscriberId2)).thenReturn(contextsOfSubscriber2);

        privateCourseMock1 = mock(RepositoryEntry.class);
        when(privateCourseMock1.getAccess()).thenReturn(RepositoryEntry.ACC_OWNERS);
        when(privateCourseMock1.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN);

        privateCourseMock2 = mock(RepositoryEntry.class);
        when(privateCourseMock2.getAccess()).thenReturn(RepositoryEntry.ACC_OWNERS);
        when(privateCourseMock2.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN);

        closedCourseMock = mock(RepositoryEntry.class);
        when(closedCourseMock.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED);

        okCourseMock = mock(RepositoryEntry.class);
        when(okCourseMock.getAccess()).thenReturn(RepositoryEntry.ACC_USERS);
        when(okCourseMock.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN);

        repositoryServiceMock = mock(RepositoryService.class);
        when(repositoryServiceMock.lookupRepositoryEntry(contextId_privateCourse1)).thenReturn(privateCourseMock1);
        when(repositoryServiceMock.lookupRepositoryEntry(contextId_privateCourse2)).thenReturn(privateCourseMock2);

        when(repositoryServiceMock.lookupRepositoryEntry(contextId_deletedCourse)).thenReturn(null);
        when(repositoryServiceMock.lookupRepositoryEntry(contextId_closedCourse)).thenReturn(closedCourseMock);
        when(repositoryServiceMock.lookupRepositoryEntry(contextId_ok)).thenReturn(okCourseMock);

        when(repositoryServiceMock.createRepositoryEntryStatus(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN)).thenReturn(
                new RepositoryEntryStatus(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN));
        when(repositoryServiceMock.createRepositoryEntryStatus(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED)).thenReturn(
                new RepositoryEntryStatus(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED));

        notificationCourseAccessManager = new NotificationCourseAccessManager();
        notificationCourseAccessManager.notificationLearnServiceImpl = notificationLearnServiceMock;
        notificationCourseAccessManager.repositoryService = repositoryServiceMock;
    }

    private void createMockNotificationSubscriptionContextAndAddToContextList(List<NotificationSubscriptionContext> contexts, Long contextId) {
        NotificationSubscriptionContext mockContext1 = mock(NotificationSubscriptionContext.class);
        when(mockContext1.getContextId()).thenReturn(contextId);
        contexts.add(mockContext1);
    }

    @Test
    public void removeInvalidContexts_4_InvalidContextsFound() {
        Set<Long> invalidContextsIds = notificationCourseAccessManager.removeInvalidContexts(subscriberIds);

        assertEquals(4, invalidContextsIds.size());
        assertTrue(invalidContextsIds.contains(contextId_privateCourse1));
        assertFalse(invalidContextsIds.contains(contextId_ok));
    }

    @Test
    public void removeInvalidContexts_5_InvalidContextsFound() {
        // we restrict the access to the mockRepositoryEntry_okCourse
        when(okCourseMock.getAccess()).thenReturn(RepositoryEntry.ACC_OWNERS_AUTHORS);

        Set<Long> invalidContextsIds = notificationCourseAccessManager.removeInvalidContexts(subscriberIds);

        assertEquals(5, invalidContextsIds.size());
        assertTrue(invalidContextsIds.contains(contextId_privateCourse1));
        assertTrue(invalidContextsIds.contains(contextId_ok));
    }

    @Test
    public void removeInvalidContexts_2_InvalidContextsFound() {
        subscriberIds.remove(subscriberId1);
        Set<Long> invalidContextsIds = notificationCourseAccessManager.removeInvalidContexts(subscriberIds);
        assertEquals(2, invalidContextsIds.size());
        assertTrue(invalidContextsIds.contains(contextId_privateCourse1));
        assertFalse(invalidContextsIds.contains(contextId_ok));
    }

}
