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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.notification.PublisherDao;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.presentation.forum.ForumNotificationTypeHandler;

/**
 * Initial Date: 08.02.2012 <br>
 * 
 * @author guretzki
 */
public class PublishDelegateTest {

    protected PublishDelegate publishDelegateTestObject;

    private Long contextId = 1L;
    private ContextType contextType = ContextType.COURSE;
    private String sourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
    private String contextTitle = "ContextTestTitle";
    private Long subcontextId = 2L;
    private Long sourceId = 3L;
    private String sourceTitle = "SourceTitle";
    private String sourceEntryTitle = "SourceEntryTitle";
    private Long sourceEntryId = 4L;

    private Identity creatorIdentity;

    @Before
    public void setup() {
        publishDelegateTestObject = new PublishDelegate();
        creatorIdentity = mock(Identity.class);
        when(creatorIdentity.getName()).thenReturn("test");
        PublisherDao publisherDao = mock(PublisherDao.class);
        publishDelegateTestObject.publisherDao = publisherDao;
    }

    @Test(expected = IllegalArgumentException.class)
    public void publishEvent() {
        PublishEventTO invalidPublishEventTO = PublishEventTO.getValidInstance(contextType, null, contextTitle, subcontextId, sourceType, sourceId, sourceTitle,
                sourceEntryTitle, creatorIdentity, EventType.NEW);
        invalidPublishEventTO.setSourceEntryId(sourceEntryId.toString());
        publishDelegateTestObject.publishEvent(invalidPublishEventTO);
    }

    @Test
    public void publishEvent_noPublisherExists() {
        when(publishDelegateTestObject.publisherDao.findPublisher(contextId, contextType, sourceId, sourceType)).thenReturn(null);
        PublishEventTO invalidPublishEventTO = PublishEventTO.getValidInstance(ContextType.COURSE, contextId, contextTitle, subcontextId,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId, sourceTitle, sourceEntryTitle, creatorIdentity, EventType.NEW);
        invalidPublishEventTO.setSourceEntryId("4");
        assertEquals("", 0, publishDelegateTestObject.publishEvent(invalidPublishEventTO));
    }

    @Test
    @Ignore
    public void publishEvent_publisherExists() {
        // TODO: NOT YET IMPLEMENTED
    }

    @Test
    @Ignore
    public void createAttributesFrom() {
        // TODO: NOT YET IMPLEMENTED
    }

}
