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

package org.olat.presentation.calendar.components;

import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.ICalTokenGenerator;
import org.olat.lms.course.ICourse;
import org.olat.presentation.calendar.LinkProvider;

public class CalendarRenderWrapper {

    /**
     * These CSS classes must be defined in the calendar.css file.
     */
    public static final String CALENDAR_COLOR_BLUE = "o_cal_blue";
    public static final String CALENDAR_COLOR_ORANGE = "o_cal_orange";
    public static final String CALENDAR_COLOR_GREEN = "o_cal_green";
    public static final String CALENDAR_COLOR_YELLOW = "o_cal_yellow";
    public static final String CALENDAR_COLOR_RED = "o_cal_red";

    /**
     * These are the access restrictions on this calendar.
     */
    public static final int ACCESS_READ_WRITE = 0;
    public static final int ACCESS_READ_ONLY = 1;

    private OlatCalendar calendar;
    private int access;
    private boolean imported = false;
    private boolean subscribed = false;
    private CalendarConfig calendarConfig;
    private LinkProvider linkProvider;

    /**
     * Configure a calendar for rendering. Set default values for calendar color (BLUE) and access (READ_ONLY).
     * 
     * @param calendar
     * @param calendarColor
     * @param access
     */
    public CalendarRenderWrapper(final OlatCalendar calendar) {
        this(calendar, new CalendarConfig(), ACCESS_READ_ONLY);
    }

    /**
     * Configure a calendar for rendering.
     * 
     * @param calendar
     * @param calendarColor
     * @param access
     */
    public CalendarRenderWrapper(final OlatCalendar calendar, final CalendarConfig config, final int access) {
        this.calendar = calendar;
        this.access = access;
    }

    public void setAccess(final int access) {
        this.access = access;
    }

    public int getAccess() {
        return access;
    }

    public void setImported(final boolean imported) {
        this.imported = imported;
    }

    public boolean isImported() {
        return imported;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    public OlatCalendar getCalendar() {
        return calendar;
    }

    public CalendarConfig getCalendarConfig() {
        return calendarConfig;
    }

    public void setCalendarConfig(final CalendarConfig calendarConfig) {
        this.calendarConfig = calendarConfig;
    }

    /**
     * @return Returns the linkProvider.
     */
    public LinkProvider getLinkProvider() {
        return linkProvider;
    }

    /**
     * @param linkProvider
     *            The linkProvider to set.
     */
    public void setLinkProvider(final LinkProvider linkProvider) {
        this.linkProvider = linkProvider;
    }

    public boolean hasIcalFeed(final Identity identity) {
        return ICalTokenGenerator.existIcalFeedLink(this.getCalendar().getType(), this.getCalendar().getCalendarID(), identity);
    }

    public static CalendarRenderWrapper wrapPersonalCalendar(final OlatCalendar cal, final String username) {
        return wrapCalendar(cal, username, CalendarRenderWrapper.CALENDAR_COLOR_BLUE);
    }

    public static CalendarRenderWrapper wrapImportedCalendar(final OlatCalendar cal, final String calendarName) {
        return wrapCalendar(cal, calendarName, CalendarRenderWrapper.CALENDAR_COLOR_BLUE);
    }

    public static CalendarRenderWrapper wrapGroupCalendar(final OlatCalendar cal, final BusinessGroup businessGroup) {
        return wrapCalendar(cal, businessGroup.getName(), CalendarRenderWrapper.CALENDAR_COLOR_ORANGE);
    }

    public static CalendarRenderWrapper wrapCourseCalendar(final OlatCalendar cal, final ICourse course) {
        return wrapCalendar(cal, course.getCourseTitle(), CalendarRenderWrapper.CALENDAR_COLOR_GREEN);
    }

    private static CalendarRenderWrapper wrapCalendar(final OlatCalendar cal, final String calendarName, final String calendarCssClass) {
        final CalendarRenderWrapper calendarWrapper = new CalendarRenderWrapper(cal);
        final CalendarConfig config = new CalendarConfig(calendarName, calendarCssClass, true);
        calendarWrapper.setCalendarConfig(config);
        return calendarWrapper;
    }
}
