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
package org.olat.lms.core.notification.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.core.notification.NotificationTypeHandler;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.presentation.wiki.WikiNotificationTypeHandler;

/**
 * Initial Date: 20.03.2012 <br>
 * 
 * @author cg
 */
public class UriBuilderTest {

    private UriBuilder uriBuilderTestObject;
    private String testContextPathUri = "/test";

    Long contextId = 1234L;
    Publisher publisher;
    Long subContextId = 4567L;
    String sourceEntryId = "9876";

    @Before
    public void setup() {
        publisher = mock(Publisher.class);
        when(publisher.getContextType()).thenReturn(ContextType.COURSE);
        when(publisher.getContextId()).thenReturn(contextId);

        uriBuilderTestObject = new UriBuilder();
        uriBuilderTestObject.setServerContextPathURI(testContextPathUri);
        Set<NotificationTypeHandler> typeHandler = new HashSet<NotificationTypeHandler>();
        typeHandler.add(new ForumNotificationTypeHandler());
        typeHandler.add(new WikiNotificationTypeHandler());
        NotificationTypeHandlerSelector typeHandlerSelector = new NotificationTypeHandlerSelector();
        typeHandlerSelector.notificationTypeHandler = typeHandler;
        uriBuilderTestObject.typeHandlerSelector = typeHandlerSelector;
    }

    @Test
    public void getURIToContext_ContextType_COURSE() {
        String uriToContext = uriBuilderTestObject.getURIToContext(publisher);
        assertEquals("Wrong URI to context", testContextPathUri + "/url/RepositoryEntry/" + contextId, uriToContext);
    }

    @Test
    public void getURIToContext_ContextType_UNKOWN() {
        Publisher publisherUnkown = mock(Publisher.class);
        when(publisherUnkown.getContextType()).thenReturn(ContextType.UNKNOWN);
        String uriToContext = uriBuilderTestObject.getURIToContext(publisherUnkown);
        assertEquals("Wrong URI with unkown type", testContextPathUri + "/url/UNKOWN/0", uriToContext);
    }

    @Test
    public void getURIToEventSource_Forum() {
        when(publisher.getSourceType()).thenReturn(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        checkUriToContext();
    }

    @Test
    public void getURIToEventSource_Wiki() {
        when(publisher.getSourceType()).thenReturn(WikiNotificationTypeHandler.WIKI_SOURCE_TYPE);
        checkUriToContext();
    }

    private void checkUriToContext() {
        when(publisher.getSubcontextId()).thenReturn(subContextId);
        String uriToEventSource = uriBuilderTestObject.getURIToEventSource(publisher);
        assertEquals("Wrong URI with type " + publisher.getSourceType(), testContextPathUri + "/url/RepositoryEntry/" + contextId + "/CourseNode/" + subContextId,
                uriToEventSource);
    }

    @Test
    public void getURIToSourceEntry_Forum() {
        when(publisher.getSourceType()).thenReturn(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        when(publisher.getSubcontextId()).thenReturn(subContextId);
        String uriToSourceEntry = uriBuilderTestObject.getURIToSourceEntry(publisher, sourceEntryId);
        assertEquals("Wrong URI with type " + publisher.getSourceType(), testContextPathUri + "/url/RepositoryEntry/" + contextId + "/CourseNode/" + subContextId
                + "/Message/" + sourceEntryId, uriToSourceEntry);
    }

    @Test
    public void getURIToSourceEntry_Wiki() {
        when(publisher.getSourceType()).thenReturn(WikiNotificationTypeHandler.WIKI_SOURCE_TYPE);
        when(publisher.getSubcontextId()).thenReturn(subContextId);
        String uriToSourceEntry = uriBuilderTestObject.getURIToSourceEntry(publisher, sourceEntryId);
        assertEquals("Wrong URI with type " + publisher.getSourceType(), testContextPathUri + "/url/RepositoryEntry/" + contextId + "/CourseNode/" + subContextId
                + "/path%3D" + sourceEntryId + "/0", uriToSourceEntry);
    }

    @Test
    public void xxx() {
        System.out.println("=============================");
    }

}
