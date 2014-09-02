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

package org.olat.presentation.course.calendar;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.lms.properties.PropertyParameterObject;
import org.olat.presentation.calendar.CalendarSubscription;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.system.spring.CoreSpringFactory;

public class CourseCalendarSubscription implements CalendarSubscription {

    private String calendarId;
    private Identity identity;

    public CourseCalendarSubscription(final String calendarId, final Identity identity) {
        this.calendarId = calendarId;
        this.identity = identity;
    }

    /**
	 */
    @Override
    public boolean isSubscribed() {
        return getSubscribedCourseCalendarIDs().contains(calendarId);
    }

    /**
	 */
    @Override
    public Controller triggerSubscribeAction() {
        final List<String> courseSubscriptions = getSubscribedCourseCalendarIDs();
        final List<String> courseUnSubscriptions = getUnsubscribedCourseCalendarIDs();
        // check if already subscribed
        if (courseSubscriptions.contains(calendarId)) {
            // do an unsubscribe of the actual calendar
            if (courseSubscriptions.remove(calendarId)) {
                courseUnSubscriptions.add(calendarId);
                persistAllSubscribptionInfos(courseSubscriptions, courseUnSubscriptions, identity);
            }
        } else {
            // subscribe to the actual calendar
            courseSubscriptions.add(calendarId);
            courseUnSubscriptions.remove(calendarId);
            persistAllSubscribptionInfos(courseSubscriptions, courseUnSubscriptions, identity);
        }
        return null;
    }

    @Override
    public void subscribe(final boolean force) {
        // check if already subscribed
        if (!isSubscribed()) {
            // subscribe to the actual calendar
            final List<String> courseSubscriptions = getSubscribedCourseCalendarIDs();
            final List<String> courseUnSubscriptions = getUnsubscribedCourseCalendarIDs();
            if (!courseUnSubscriptions.contains(calendarId) || force) {
                courseSubscriptions.add(calendarId);
                courseUnSubscriptions.remove(calendarId);
                persistAllSubscribptionInfos(courseSubscriptions, courseUnSubscriptions, identity);
            }
        }
    }

    @Override
    public void unsubscribe() {
        // unsubscribe to the actual calendar
        final List<String> courseSubscriptions = getSubscribedCourseCalendarIDs();
        final List<String> courseUnSubscriptions = getUnsubscribedCourseCalendarIDs();
        courseSubscriptions.remove(calendarId);
        courseUnSubscriptions.add(calendarId);
        persistAllSubscribptionInfos(courseSubscriptions, courseUnSubscriptions, identity);
    }

    public List<String> getSubscribedCourseCalendarIDs() {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().identity(identity).name(PropertyManagerEBL.KEY_SUBSCRIPTION).build();
        return getPropertyManagerEBL().getCourseCalendarSubscriptionProperty(propertyParameterObject);
    }

    public List<String> getUnsubscribedCourseCalendarIDs() {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().identity(identity).name(PropertyManagerEBL.KEY_UN_SUBSCRIPTION).build();
        return getPropertyManagerEBL().getCourseCalendarSubscriptionProperty(propertyParameterObject);
    }

    public void persistSubscribedCalendarIDs(final List<String> subscribedCalendarIDs, final Identity identity) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().identity(identity).name(PropertyManagerEBL.KEY_SUBSCRIPTION).build();
        getPropertyManagerEBL().persistCourseCalendarSubscriptions(subscribedCalendarIDs, propertyParameterObject);
    }

    private void persistAllSubscribptionInfos(final List<String> subscribedCalendarIDs, final List<String> unSubscribedCalendarIDs, final Identity identity) {
        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().identity(identity).name(PropertyManagerEBL.KEY_SUBSCRIPTION).build();
        getPropertyManagerEBL().persistCourseCalendarSubscriptions(subscribedCalendarIDs, propertyParameterObject);
        propertyParameterObject = new PropertyParameterObject.Builder().identity(identity).name(PropertyManagerEBL.KEY_UN_SUBSCRIPTION).build();
        getPropertyManagerEBL().persistCourseCalendarSubscriptions(unSubscribedCalendarIDs, propertyParameterObject);
    }

    private PropertyManagerEBL getPropertyManagerEBL() {
        return CoreSpringFactory.getBean(PropertyManagerEBL.class);
    }

}
