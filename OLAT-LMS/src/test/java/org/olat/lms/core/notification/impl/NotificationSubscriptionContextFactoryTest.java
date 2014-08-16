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

import org.junit.Before;
import org.junit.Test;

/**
 * Initial Date: 19.03.2012 <br>
 * 
 * @author guretzki
 */
public class NotificationSubscriptionContextFactoryTest {

    NotificationSubscriptionContextFactory notificationSubscriptionContextFactoryTestObject;

    @Before
    public void setup() {
        notificationSubscriptionContextFactoryTestObject = new NotificationSubscriptionContextFactory();
    }

    @Test
    public void getSubContextIdFrom() {
        String subidentifier = "12345";
        Long subContextId = notificationSubscriptionContextFactoryTestObject.getSubContextIdFrom(subidentifier);
        assertEquals("Could not extract subContextId from '" + subidentifier + "'", Long.valueOf(subidentifier), subContextId);
    }

    @Test
    public void getSubContextIdFrom_WithDelimiter() {
        String subContextIdString = "12345";
        String subidentifier = subContextIdString + ":456";
        Long subContextId = notificationSubscriptionContextFactoryTestObject.getSubContextIdFrom(subidentifier);
        assertEquals("Could not extract subContextId from '" + subidentifier + "'", Long.valueOf(subContextIdString), subContextId);
    }

    @Test
    public void getSourceIdFrom() {
        String sourceId = "82817051271953";
        String data = "/course/85196901216965/foldernodes/" + sourceId;
        Long extractedSourceId = notificationSubscriptionContextFactoryTestObject.getSourceIdFrom(data);
        assertEquals("Could not extract sourceId from '" + data + "'", Long.valueOf(sourceId), extractedSourceId);
    }

}
