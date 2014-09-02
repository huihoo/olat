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
package org.olat.data.notification;

import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.olat.data.commons.dao.GenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 03.05.2012 <br>
 * 
 * @author lavinia
 */
@Repository
public class NotificationEventCreator {

    @Autowired
    private GenericDao<NotificationEvent> genericDao;

    @PostConstruct
    void initType() {
        genericDao.setType(NotificationEvent.class);
    }

    /**
     * Creates a new event with status WAITING and stores it, event attributes are stored as well.
     */
    NotificationEvent createAndSaveNotificationEvent(Map<String, String> attributes, Subscription subscription) {
        NotificationEvent event = genericDao.create();
        event.setSubscription(subscription);
        event.setCreationDate(new Date());
        event.setStatus(NotificationEvent.Status.WAITING);

        NotificationEvent persistedEvent = genericDao.save(event);

        for (String attributeName : attributes.keySet()) {
            persistedEvent.addAttribute(attributeName, attributes.get(attributeName));
        }
        persistedEvent = genericDao.update(persistedEvent);
        return persistedEvent;
    }

}
