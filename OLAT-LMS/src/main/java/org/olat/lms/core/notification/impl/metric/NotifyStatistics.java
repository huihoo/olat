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
package org.olat.lms.core.notification.impl.metric;

import java.util.HashMap;
import java.util.Map;

import org.olat.data.notification.Subscriber.Channel;

/**
 * Initial Date: 21.12.2011 <br>
 * 
 * @author Branislav Balaz
 */
public class NotifyStatistics implements NotificationServiceContext {

    private Map<Channel, Boolean> channel2StatusMap = new HashMap<Channel, Boolean>();

    private int failedCounter;
    private int deliveredCounter;

    public void setFailedCounter(int failedCounter) {
        this.failedCounter = failedCounter;
    }

    public void setDeliveredCounter(int deliveredCounter) {
        this.deliveredCounter = deliveredCounter;
    }

    public Map<Channel, Boolean> getChannel2StatusMap() {
        return channel2StatusMap;
    }

    public int getFailedCounter() {
        return failedCounter;
    }

    public int getDeliveredCounter() {
        return deliveredCounter;
    }

    public int getTotalCounter() {
        return getFailedCounter() + getDeliveredCounter();
    }

    public void addChannelResponse(Channel channel, Boolean response) {
        if (response != null) {
            channel2StatusMap.put(channel, response);
        }
    }

    public void add(NotifyStatistics currentNotifyStatistics) {
        if (currentNotifyStatistics == null)
            return;
        for (Map.Entry<Channel, Boolean> entry : currentNotifyStatistics.channel2StatusMap.entrySet()) {
            if (entry.getValue()) {
                deliveredCounter++;
            } else {
                failedCounter++;
            }
        }
    }
}
