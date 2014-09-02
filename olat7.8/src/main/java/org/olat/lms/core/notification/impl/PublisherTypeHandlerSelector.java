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

import java.util.Iterator;
import java.util.Set;

import org.olat.lms.core.notification.PublisherTypeHandler;
import org.olat.lms.core.notification.service.PublisherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This maps the sourceType or publisherData to a PublisherTypeHandler. <br>
 * It contains a set with all <code>PublisherTypeHandler</code>s.
 * 
 * Initial Date: 21.03.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class PublisherTypeHandlerSelector {

    @Autowired
    Set<PublisherTypeHandler> notificationTypeHandler;

    public PublisherTypeHandlerSelector() {
    }

    public PublisherTypeHandler getTypeHandler(String sourceType) {
        for (Iterator<PublisherTypeHandler> iterator = notificationTypeHandler.iterator(); iterator.hasNext();) {
            PublisherTypeHandler notificationTypeHandler = iterator.next();
            if (notificationTypeHandler.getSourceType().equals(sourceType)) {
                return notificationTypeHandler;
            }
        }
        return new UnknownPublisherTypeHandler();
    }

    public PublisherTypeHandler getTypeHandlerFrom(PublisherData publisherData) {
        String inputPublisherDataType = publisherData.getType();
        for (Iterator<PublisherTypeHandler> iterator = notificationTypeHandler.iterator(); iterator.hasNext();) {
            PublisherTypeHandler notificationTypeHandler = iterator.next();
            if (notificationTypeHandler.getPublisherDataType().equals(inputPublisherDataType)) {
                return notificationTypeHandler;
            }
        }
        return new UnknownPublisherTypeHandler();
    }
}
