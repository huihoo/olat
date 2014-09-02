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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.system.commons.configuration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

/**
 * 
 * broadcasts and receives whenever someone sets/changes one of the system wide olat properties
 * 
 * <P>
 * Initial Date: 30.05.2011 <br>
 * 
 * @author guido
 */
@Component("propertiesTopicListener")
@Qualifier("main")
public class ManagedPropertiesMessageCoordinator implements MessageListener {

    @Autowired
    private JmsTemplate template;
    @Autowired
    private SystemPropertiesLoader propertiesLoader;

    /**
     * [spring]
     */
    protected ManagedPropertiesMessageCoordinator() {

    }

    /**
     * Set a string property
     * 
     * @param propertyName
     *            The key
     * @param value
     *            The Value
     */
    public void setProperty(String propertyName, String value) {
        sendMessage(propertyName, value);
    }

    /**
     * Save the properties configuration to disk and notify other nodes about change. This is only done when there are dirty changes, otherwhile the method call does
     * nothing.
     */
    private void sendMessage(final String propName, final String propValue) {
        if (propName == null) {
            // noting to save and propagate
            return;
        }

        template.send(new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(propName);
                message.setStringProperty(propName, propValue);

                System.out.printf("********************  Sending message with key=%s value=%s\n object:%d", propName, propValue, this.hashCode());

                return message;
            }
        });

    }

    /**
	 */
    @Override
    public void onMessage(Message message) {

        try {
            if (message instanceof TextMessage) {
                TextMessage tm = (TextMessage) message;
                // TODO use MapMessage and allow update of more then one property at one
                String propertyName = tm.getText();
                String value = tm.getStringProperty(propertyName);
                System.out.printf("*****************  Processed message (listener 1) 'key=%s' 'value=%s'. hashCode={%d}\n", propertyName, value, this.hashCode());

                propertiesLoader.setProperty(propertyName, value);
            }
        } catch (JMSException e) {
            Log.error("Error while processing jms message ", e);
        }
    }

}
