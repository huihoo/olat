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
package org.olat.lms.core.notification.impl.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.NotificationEventTO;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.springframework.mail.MailSendException;

/**
 * Initial Date: 28.03.2012 <br>
 * 
 * @author aabouc
 */
public class ChannelChainTest {
    private ChannelChain channelChainTestObject;

    private MailChannel mailChannelMock;
    private DummyChannel dummyChannelMock;

    private Subscriber subscriber;
    private List<NotificationEventTO> eventTOs = new ArrayList<NotificationEventTO>();

    @Before
    public void setup() throws Exception {
        channelChainTestObject = new ChannelChain();

        // Mock for MailChannel
        mailChannelMock = mock(MailChannel.class);
        when(mailChannelMock.getChannelName()).thenReturn(Subscriber.Channel.EMAIL);
        channelChainTestObject.setMailChannel(mailChannelMock);

        // Mock for DummyChannel
        dummyChannelMock = mock(DummyChannel.class);
        when(dummyChannelMock.getChannelName()).thenReturn(null);
        channelChainTestObject.setDummyChannel(dummyChannelMock);

        // Test Subscriber
        subscriber = new Subscriber();
        subscriber.addChannel(Subscriber.Channel.EMAIL);
        subscriber.addChannel(null);

        // add one event
        eventTOs.add(mock(NotificationEventTO.class));

        channelChainTestObject.init();
    }

    @Test
    public void send_UserIsConfiguredForNoChannel() {
        subscriber.getChannels().clear();
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 0, statistics.getChannel2StatusMap().size());
    }

    @Test
    public void send_UserIsConfiguredForMailChannelOnly_Success() throws Exception {
        subscriber.getChannels().remove(null);
        doNothing().when(mailChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 1, statistics.getChannel2StatusMap().size());
        assertTrue(statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
    }

    @Test
    public void send_UserIsConfiguredForMailChannelOnly_Failed() throws Exception {
        subscriber.getChannels().remove(null);
        doThrow(new MailSendException("")).when(mailChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 1, statistics.getChannel2StatusMap().size());
        assertFalse(statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
    }

    @Test
    public void send_UserIsConfiguredForMailChannelAndDummyChannel_SucessForBoth() throws Exception {
        doNothing().when(mailChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        doNothing().when(dummyChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 2, statistics.getChannel2StatusMap().size());
        assertTrue(statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
        assertTrue(statistics.getChannel2StatusMap().get(null));
    }

    @Test
    public void send_UserIsConfiguredForMailChannelAndDummyChannel_FailedForBoth() throws Exception {
        doThrow(new MailSendException("")).when(mailChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        doThrow(new MailSendException("")).when(dummyChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 2, statistics.getChannel2StatusMap().size());
        assertFalse(statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
        assertFalse(statistics.getChannel2StatusMap().get(null));
    }

    @Test
    public void send_UserIsConfiguredForMailChannelAndDummyChannel_SucessForMail_FaildForDummy() throws Exception {
        doNothing().when(mailChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        doThrow(new MailSendException("")).when(dummyChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 2, statistics.getChannel2StatusMap().size());
        assertTrue(statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
        assertFalse(statistics.getChannel2StatusMap().get(null));
    }

    @Test
    public void send_UserIsConfiguredForMailChannelAndDummyChannel_SucessForDummy_FaildForMail() throws Exception {
        doNothing().when(dummyChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        doThrow(new MailSendException("")).when(mailChannelMock).send(any(Subscriber.class), anyListOf(NotificationEventTO.class));
        NotifyStatistics statistics = channelChainTestObject.send(subscriber, eventTOs);
        assertEquals("Wrong number of the size", 2, statistics.getChannel2StatusMap().size());
        assertFalse(statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
        assertTrue(statistics.getChannel2StatusMap().get(null));
    }

}
