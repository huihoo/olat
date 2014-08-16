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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.NotificationEventTO;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ChannelChain is based on the design pattern 'Chain Of Responsibilities'
 * 
 * This pattern helps to avoid coupling the sender (e.g. NotifyDelegate) of a request to its receiver (e.g. MailChannel) by giving more than one channel a chance to
 * handle the request.
 * 
 * The unique 'send' method has to be called in order to could make the chain of Channels.
 * 
 * Initial Date: 16.03.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class ChannelChain {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private Channel mailChannel;
    @Autowired
    private Channel dummyChannel;

    private List<Channel> channels = new ArrayList<Channel>();

    @PostConstruct
    public void init() {
        channels.add(mailChannel);
        channels.add(dummyChannel);
    }

    public List<Channel> getChannels() {
        channels.remove(dummyChannel);
        return channels;
    }

    public Channel getMailChannel() {
        return mailChannel;
    }

    public void setMailChannel(MailChannel mailChannel) {
        this.mailChannel = mailChannel;
    }

    public Channel getDummyChannel() {
        return dummyChannel;
    }

    public void setDummyChannel(DummyChannel dummyChannel) {
        this.dummyChannel = dummyChannel;
    }

    /**
     * Send the request with the given subscriber and events along the chain until a channel or more channels handle it
     * 
     * @param subscriber
     *            the Subscriber to send to
     * @param events
     *            the List of the NotificationEventTO to be sent
     */
    public NotifyStatistics send(Subscriber subscriber, List<NotificationEventTO> events) {
        NotifyStatistics statistics = new NotifyStatistics();

        if (events == null || events.isEmpty()) {
            return statistics;
        }
        // TODO (REVIEW independent notifySubscriber): iterate over subscriber's channels instead of all channels
        for (Channel channel : channels) {
            boolean delivered = false;

            if (!subscriber.getChannels().contains(channel.getChannelName()))
                continue;
            try {
                channel.send(subscriber, events);
                delivered = true;
            } catch (Exception e) {
                log.error("Could not send via the channel [" + channel.getChannelName() + "] for the subscriber [" + subscriber.getId() + "] because of: " + e);
            } finally {
                statistics.addChannelResponse(channel.getChannelName(), delivered);
            }
        }
        return statistics;
    }
}
