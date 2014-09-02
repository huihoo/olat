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
package org.olat.lms.commons;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.calendar.ImportCalendarManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.learn.campus.service.CampusCourseLearnService;
import org.olat.lms.learn.hello.service.HelloWorldLearnService;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.user.UserService;

/**
 * 
 * <P>
 * Initial Date: 28.06.2011 <br>
 * 
 * @author guido
 */
public enum LearnServices {

    businessGroupService(BusinessGroupService.class), userService(UserService.class), baseSecurity(BaseSecurity.class), calendarService(CalendarService.class), importCalendarManager(
            ImportCalendarManager.class), repositoryService(RepositoryServiceImpl.class), helloWorldService(HelloWorldLearnService.class), notificationLearnService(
            NotificationLearnService.class), campusCourseLearnService(CampusCourseLearnService.class);
    // TODO: integrate import-part to calendar-service

    private Class serviceInterface;

    private LearnServices(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public <T> Class<T> getService() {
        return this.serviceInterface;
    }

}
