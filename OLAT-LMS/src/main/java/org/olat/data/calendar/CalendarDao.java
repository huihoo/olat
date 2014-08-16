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

package org.olat.data.calendar;

import java.io.File;

import net.fortuna.ical4j.model.Calendar;

import org.olat.system.commons.resource.OLATResourceable;

public interface CalendarDao {

    public static final String TYPE_USER = "user";
    public static final String TYPE_GROUP = "group";
    public static final String TYPE_COURSE = "course";

    public static final int MAX_SUBJECT_DISPLAY_LENGTH = 30;

    public static final String CALENDAR_MANAGER = "CalendarManager";

    /**
     * Create a new calendar with the given id.
     * 
     * @param calendarID
     * @param type
     * @return
     */
    public abstract OlatCalendar createCalendar(String calendarType, String calendarID);

    /**
     * Check if a calendar already exists for the given id.
     * 
     * @param calendarID
     * @param type
     * @return
     */
    public abstract boolean calendarExists(String calendarType, String calendarID);

    /**
     * Save a calendar.
     * 
     * @param calendar
     */
    public abstract boolean persistCalendar(OlatCalendar calendar);

    /**
     * Delete a calendar.
     * 
     * @param calendarType
     * @param calendarID
     * @return
     */
    public abstract boolean deleteCalendar(String calendarType, String calendarID);

    /**
     * Get a calendar as iCalendar file.
     * 
     * @param calendarType
     * @param calendarID
     * @return
     */
    public abstract File getCalendarICalFile(String calendarType, String calendarID);

    /**
     * get the calendar file name from type and id
     * 
     * @param type
     * @param calendarID
     * @return
     */
    public abstract File getCalendarFile(String type, String calendarID);

    /**
     * Read the calendar file (.ics) from the olatdata section.
     * 
     * @param type
     * @param calendarID
     * @return
     */
    public abstract Calendar readCalendar(String type, String calendarID);

    /**
     * Add an event to given calendar and save calendar.
     * 
     * @param cal
     * @param calendarEntry
     * @return true if success
     */
    public abstract boolean addEventTo(OlatCalendar cal, CalendarEntry calendarEntry);

    /**
     * Remove an event from given calendar and save calendar.
     * 
     * @param cal
     * @param calendarEntry
     * @return true if success
     */
    public abstract boolean removeEventFrom(OlatCalendar cal, CalendarEntry calendarEntry);

    /**
     * Update an event of given calendar and save calendar.
     * 
     * @param cal
     * @param calendarEntry
     * @return true if success
     */
    public abstract boolean updateEventFrom(OlatCalendar cal, CalendarEntry calendarEntry);

    /**
     * Update an event of given calendar and save calendar. Use this method if the Kalendar is already in a doInSync.
     * 
     * @param cal
     * @param calendarEntry
     * @return true if success
     */
    public abstract boolean updateEventAlreadyInSync(final OlatCalendar cal, final CalendarEntry calendarEntry);

    /**
     * Get a calendar by type and id.
     * 
     * @param type
     * @param calendarID
     * @return
     */
    public abstract OlatCalendar getCalendar(String type, String calendarID);

    /**
     * Build a Calendar object from String object.
     * 
     * @param calendarContent
     * @return
     */
    public abstract OlatCalendar buildKalendarFrom(String calendarContent, String calType, String calId);

    /**
     * Create Ores Helper object.
     * 
     * @param cal
     * @return OLATResourceable for given Kalendar
     */
    public abstract OLATResourceable getOresHelperFor(OlatCalendar cal);
}
