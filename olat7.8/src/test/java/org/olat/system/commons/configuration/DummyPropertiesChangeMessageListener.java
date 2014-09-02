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
package org.olat.system.commons.configuration;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * a second jms topic listener to check if we use a broadcast message
 * 
 * <P>
 * Initial Date: 31.05.2011 <br>
 * 
 * @author guido
 */
@Component("dummyTopicListener")
@Qualifier("test")
public class DummyPropertiesChangeMessageListener extends ManagedPropertiesMessageCoordinator {

    protected int messageCount;
    protected String messageValue;

    /**
	 * 
	 */
    private DummyPropertiesChangeMessageListener() {
        //
    }

    /**
     * @see org.olat.system.commons.configuration.ManagedPropertiesMessageCoordinator#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        super.onMessage(message);
        messageCount++;
    }

}
