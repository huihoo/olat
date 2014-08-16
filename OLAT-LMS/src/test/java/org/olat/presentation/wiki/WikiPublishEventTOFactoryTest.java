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
package org.olat.presentation.wiki;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher;
import org.olat.lms.core.notification.impl.NotificationSubscriptionContextFactory;
import org.olat.lms.core.notification.service.ContextInfo;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.lms.wiki.WikiPage;

/**
 * Initial Date: 15.03.2012 <br>
 * 
 * @author guretzki
 */
public class WikiPublishEventTOFactoryTest {

    SubscriptionContext subsContext;
    Long wikiResourceableId = 1L;
    // RepositoryService repositoryServiceMock;
    private Identity testIdentity;
    private WikiPage wikiPage;
    String sourceEntryId = "WikiPage";
    Long resId = 2L;
    Long contextId = 3L;
    String subidentifier = "123";

    private WikiNotificationTypeHandler wikiNotificationTypeHandler;

    @Before
    public void setup() {
        String resName = "CourseModule";
        subsContext = new SubscriptionContext(resName, resId, subidentifier);
        // repositoryServiceMock = mock(RepositoryServiceImpl.class);
        // when(repositoryServiceMock.getRepositoryEntryIdFromResourceable(anyLong(), anyString())).thenReturn(contextId);
        testIdentity = mock(Identity.class);

        wikiPage = mock(WikiPage.class);
        when(wikiPage.getPageName()).thenReturn(sourceEntryId);

        NotificationSubscriptionContextFactory notificationSubscriptionContextFactoryMock = mock(NotificationSubscriptionContextFactory.class);
        when(notificationSubscriptionContextFactoryMock.createContextInfoFrom(subsContext)).thenReturn(
                new ContextInfo(Publisher.ContextType.COURSE, contextId, new Long(subidentifier)));

        wikiNotificationTypeHandler = new WikiNotificationTypeHandler();
        // wikiNotificationTypeHandler.repositoryService = repositoryServiceMock;
        wikiNotificationTypeHandler.notificationSubscriptionContextFactory = notificationSubscriptionContextFactoryMock;
    }

    @Test
    public void createPublishEventTO_EventTypeIsNO_PUBLISH() {
        PublishEventTO publishEventTo = wikiNotificationTypeHandler.createPublishEventTO(subsContext, wikiResourceableId, testIdentity, wikiPage, EventType.NO_PUBLISH);
        assertEquals("Wrong EventType", EventType.NO_PUBLISH, publishEventTo.getEvenType());
    }

    @Test
    public void createPublishEventTO_EventTypeIsNEW() {
        PublishEventTO publishEventTo = wikiNotificationTypeHandler.createPublishEventTO(subsContext, wikiResourceableId, testIdentity, wikiPage, EventType.NEW);
        assertEquals("Wrong EventType", EventType.NEW, publishEventTo.getEvenType());
        assertEquals("Wrong SourceType", WikiNotificationTypeHandler.WIKI_SOURCE_TYPE, publishEventTo.getSourceType());
        assertEquals("Wrong SourceEntryId", sourceEntryId, publishEventTo.getSourceEntryId());
        assertEquals("Wrong ContextType", Publisher.ContextType.COURSE, publishEventTo.getContextType());
        assertEquals("Wrong ContextId", contextId, publishEventTo.getContextId());
        assertEquals("Wrong SubContextId", new Long(subidentifier), publishEventTo.getSubcontextId());
    }

    @Test
    public void createPublishEventTO_ContextTypeUnkown() {
        wikiNotificationTypeHandler.notificationSubscriptionContextFactory = new NotificationSubscriptionContextFactory();
        SubscriptionContext subsContextUnkown = new SubscriptionContext("something", resId, subidentifier);
        PublishEventTO publishEventTo = wikiNotificationTypeHandler.createPublishEventTO(subsContextUnkown, wikiResourceableId, testIdentity, wikiPage, EventType.NEW);
        assertEquals("Wrong ContextType", Publisher.ContextType.UNKNOWN, publishEventTo.getContextType());
    }

}
