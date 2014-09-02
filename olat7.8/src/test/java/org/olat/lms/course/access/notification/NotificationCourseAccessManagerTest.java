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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.Structure;
import org.olat.lms.course.access.CourseAccessEvaluator;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.repository.RepositoryEntryStatus;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.components.tree.TreeNode;

/**
 * Initial Date: 25.05.2012 <br>
 * 
 * @author lavinia
 */
public class NotificationCourseAccessManagerTest {

    NotificationCourseAccessManager notificationCourseAccessManager;

    NotificationLearnService notificationLearnServiceMock;
    RepositoryService repositoryServiceMock;
    CourseAccessEvaluator courseAccessEvaluatorMock;

    RepositoryEntry privateCourseMock1; // unaccessible course
    Long resourceableId_privateCourse1 = new Long(7);

    RepositoryEntry privateCourseMock2; // unaccessible course
    Long resourceableId_privateCourse2 = new Long(8);

    RepositoryEntry closedCourseMock; // unaccessible course
    Long resourceableId_closedCourse = new Long(9);

    RepositoryEntry okCourseMock; // accessible course
    Long resourceableId_OKCourse = new Long(6);

    ICourse accessibleCourseMock;
    CourseNode courseNodeMock;

    Long contextId_privateCourse1 = new Long(1);
    Long contextId_privateCourse2 = new Long(11);
    // int statusCode = 2;
    Long contextId_deletedCourse = new Long(2);
    Long contextId_closedCourse = new Long(3);
    Long contextId_ok = new Long(4);
    Long subcontextId_ok = new Long(5);

    List<NotificationSubscriptionContext> contextsOfSubscriber1;
    List<NotificationSubscriptionContext> contextsOfSubscriber2;

    Long subscriberId1 = new Long(50);
    Long subscriberId2 = new Long(60);
    List<Long> subscriberIds;

    Identity identityMock1;

    @Before
    public void setUp() throws Exception {

        subscriberIds = new ArrayList<Long>();
        subscriberIds.add(subscriberId1);
        subscriberIds.add(subscriberId2);

        // mock NotificationSubscriptionContext list for identityMock1
        identityMock1 = mock(Identity.class);
        when(identityMock1.toString()).thenReturn("identityMock1");
        contextsOfSubscriber1 = new ArrayList<NotificationSubscriptionContext>();
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_privateCourse1, identityMock1);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_ok, identityMock1);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_deletedCourse, identityMock1);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber1, contextId_closedCourse, identityMock1);

        // mock NotificationSubscriptionContext list for identityMock2
        Identity identityMock2 = mock(Identity.class);
        contextsOfSubscriber2 = new ArrayList<NotificationSubscriptionContext>();
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber2, contextId_privateCourse1, identityMock2);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber2, contextId_privateCourse2, identityMock2);
        createMockNotificationSubscriptionContextAndAddToContextList(contextsOfSubscriber2, contextId_ok, identityMock2);

        // mock the NotificationLearnService
        notificationLearnServiceMock = mock(NotificationLearnService.class);
        when(notificationLearnServiceMock.getNotificationSubscriptionContexts(subscriberId1)).thenReturn(contextsOfSubscriber1);
        when(notificationLearnServiceMock.getNotificationSubscriptionContexts(subscriberId2)).thenReturn(contextsOfSubscriber2);

        // mock the RepositoryEntry for a private course
        privateCourseMock1 = mock(RepositoryEntry.class);
        when(privateCourseMock1.getAccess()).thenReturn(RepositoryEntry.ACC_OWNERS);
        when(privateCourseMock1.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN);
        when(privateCourseMock1.getResourceableId()).thenReturn(resourceableId_privateCourse1);

        // mock the RepositoryEntry for a private course
        privateCourseMock2 = mock(RepositoryEntry.class);
        when(privateCourseMock2.getAccess()).thenReturn(RepositoryEntry.ACC_OWNERS);
        when(privateCourseMock2.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN);
        when(privateCourseMock2.getResourceableId()).thenReturn(resourceableId_privateCourse2);

        // mock the RepositoryEntry for a closed course
        closedCourseMock = mock(RepositoryEntry.class);
        when(closedCourseMock.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED);
        when(closedCourseMock.getResourceableId()).thenReturn(resourceableId_closedCourse);

        // mock the RepositoryEntry for a public course
        okCourseMock = mock(RepositoryEntry.class);
        when(okCourseMock.getAccess()).thenReturn(RepositoryEntry.ACC_USERS);
        when(okCourseMock.getStatusCode()).thenReturn(RepositoryEntryStatus.REPOSITORY_STATUS_OPEN);
        when(okCourseMock.getResourceableId()).thenReturn(resourceableId_OKCourse);
        OLATResource oLATResourceableMock = mock(OLATResource.class);
        when(oLATResourceableMock.getResourceableId()).thenReturn(resourceableId_OKCourse);
        when(okCourseMock.getOlatResource()).thenReturn(oLATResourceableMock);
        when(okCourseMock.toString()).thenReturn("okCourseMock");

        // mock the repositoryService
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

        // create NotificationCourseAccessManager and populate instance variables with mock objects
        notificationCourseAccessManager = new NotificationCourseAccessManager();
        notificationCourseAccessManager.notificationLearnServiceImpl = notificationLearnServiceMock;
        notificationCourseAccessManager.repositoryService = repositoryServiceMock;
        notificationCourseAccessManager.dBImpl = mock(DB.class);

        // fill the cache of NotificationCourseAccessManager
        notificationCourseAccessManager.repositoryEntryToCourseCache = new HashMap<Long, ICourse>();
        ICourse inaccessibleCourseMock = mock(ICourse.class);
        when(inaccessibleCourseMock.toString()).thenReturn("inaccessibleCourseMock");
        notificationCourseAccessManager.repositoryEntryToCourseCache.put(privateCourseMock1.getResourceableId(), inaccessibleCourseMock);
        notificationCourseAccessManager.repositoryEntryToCourseCache.put(privateCourseMock2.getResourceableId(), inaccessibleCourseMock);
        notificationCourseAccessManager.repositoryEntryToCourseCache.put(closedCourseMock.getResourceableId(), inaccessibleCourseMock);

        accessibleCourseMock = mock(ICourse.class);
        when(accessibleCourseMock.toString()).thenReturn("accessibleCourseMock");
        CourseEditorTreeModel courseEditorTreeModel = mock(CourseEditorTreeModel.class);
        TreeNode treeNodeMock = mock(TreeNode.class);
        when(courseEditorTreeModel.getNodeById(String.valueOf(subcontextId_ok))).thenReturn(treeNodeMock);
        when(accessibleCourseMock.getEditorTreeModel()).thenReturn(courseEditorTreeModel);
        courseNodeMock = mock(CourseNode.class);
        Structure structureMock = mock(Structure.class);
        when(accessibleCourseMock.getRunStructure()).thenReturn(structureMock);
        when(structureMock.getNode(String.valueOf(subcontextId_ok))).thenReturn(courseNodeMock);
        notificationCourseAccessManager.repositoryEntryToCourseCache.put(okCourseMock.getResourceableId(), accessibleCourseMock);

        // fixture for CourseAccessEvaluator
        courseAccessEvaluatorMock = mock(CourseAccessEvaluator.class);
        when(courseAccessEvaluatorMock.isCourseAccessibleForIdentity(identityMock1, okCourseMock, accessibleCourseMock, false)).thenReturn(true);
        when(courseAccessEvaluatorMock.isCourseAccessibleForIdentity(identityMock1, privateCourseMock1, inaccessibleCourseMock, false)).thenReturn(false);
        when(courseAccessEvaluatorMock.isCourseAccessibleForIdentity(identityMock1, privateCourseMock2, inaccessibleCourseMock, false)).thenReturn(false);
        when(courseAccessEvaluatorMock.isCourseAccessibleForIdentity(identityMock1, closedCourseMock, inaccessibleCourseMock, false)).thenReturn(false);
        when(courseAccessEvaluatorMock.isCourseNodeAccessibleForIdentity(identityMock1, accessibleCourseMock, courseNodeMock, false)).thenReturn(false);
        notificationCourseAccessManager.courseAccessEvaluatorImpl = courseAccessEvaluatorMock;
    }

    private void createMockNotificationSubscriptionContextAndAddToContextList(List<NotificationSubscriptionContext> contexts, Long contextId, Identity identity) {
        NotificationSubscriptionContext mockContext1 = mock(NotificationSubscriptionContext.class);
        when(mockContext1.getContextId()).thenReturn(contextId);
        when(mockContext1.getSubcontextId()).thenReturn(subcontextId_ok);
        when(mockContext1.getIdentity()).thenReturn(identity);
        when(mockContext1.toString()).thenReturn(String.valueOf(contextId));
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

    @Test
    public void getCoursesAccesibleForIdentity() {
        assertTrue(contextsOfSubscriber1.size() == 4);
        Map<Long, RepositoryEntry> contextIdToRepositoryEntryMap = notificationCourseAccessManager.getCoursesAccesibleForIdentity(contextsOfSubscriber1);
        assertEquals("only one course is accessible for this identity", 1, contextIdToRepositoryEntryMap.size());
    }

    @Test
    public void getUnaccesibleSubscriptionsForSubscriber() {
        List<NotificationSubscriptionContext> contextList = notificationCourseAccessManager.getUnaccesibleSubscriptionsForSubscriber(subscriberId1);
        assertEquals("expected 4 unaccessible subscriptions for this identity", 4, contextList.size());
    }

    @Test
    public void getCourseFromCacheOrLoad_getExistingFromCache() {
        // we know we put this on cache in the setup, so we expect to get it from there
        ICourse publicCourse = notificationCourseAccessManager.getCourseFromCacheOrLoad(okCourseMock);
        assertNotNull(publicCourse);
    }

    @Test
    public void getCourseFromCacheOrLoad_getNotExisting() {
        // this is not found in cache and cannot be loaded, so it is null
        RepositoryEntry notExistingRepositoryEntry = mock(RepositoryEntry.class);
        when(notExistingRepositoryEntry.getResourceableId()).thenReturn(new Long(100));
        ICourse publicCourse = notificationCourseAccessManager.getCourseFromCacheOrLoad(notExistingRepositoryEntry);
        assertNull(publicCourse);
    }

}
