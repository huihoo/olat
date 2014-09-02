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

import java.util.List;

import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.NotificationEventTO;

/**
 * This is the Base channel interface defining the unique method for sending the given events to the given subscriber
 * 
 * Initial Date: 16.03.2012 <br>
 * 
 * @author aabouc
 */
public interface Channel {

    /**
     * Send the given events to the given subscriber. This method will throw a channel specific exception if the events cannot be sent.
     * 
     * @param subscriber
     *            the Subscriber to send to
     * @param events
     *            the List of the NotificationEventTO to be sent
     */
    void send(Subscriber subscriber, List<NotificationEventTO> events) throws Exception;

    /**
     * Return the name of this channel
     */
    Subscriber.Channel getChannelName();
}
