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

package org.olat.lms.calendar;

import java.io.File;
import java.io.IOException;

import net.fortuna.ical4j.model.Calendar;

import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.ICourse;
import org.olat.lms.preferences.Preferences;

public interface CalendarService {

    /**
     * Get the individual calendar configuration for a specific calendar for a specific identity. If no individual calendar config exists, null is returned.
     * 
     * @param calendar
     * @param ureq
     * @return
     */
    public CalendarConfig findCalendarConfigForIdentity(OlatCalendar calendar, Preferences userGuiPreferences);

    /**
     * Save the calendar configuration for a specific calendar for a specific identity.
     * 
     * @param calendarConfig
     * @param calendar
     * @param ureq
     */
    public void saveCalendarConfigForIdentity(CalendarConfig calendarConfig, OlatCalendar calendar, Preferences userGuiPreferences);

    public boolean addEntryTo(OlatCalendar cal, CalendarEntry calendarEntry);

    public boolean updateEntryFrom(OlatCalendar cal, CalendarEntry calendarEntry);

    public boolean removeEntryFrom(OlatCalendar cal, CalendarEntry calendarEntry);

    /**
     * Get an identity's personal calendar. If the calendar does not exist yet, a new calendar will be created. The calendar will be configured with defaults for calendar
     * config.
     * 
     * @param identity
     * @return
     */
    public OlatCalendar getPersonalCalendar(Identity identity);

    /**
     * Get an identity's personal calendar. If the calendar does not exist yet, a new calendar will be created. The calendar will be configured with defaults for calendar
     * config.
     * 
     * @param identity
     * @return
     */
    public OlatCalendar getImportedCalendar(Identity identity, String calendarName);

    /**
     * Get a group's calendar. If the calendar does not yet exist, a new calendar will be created. The calendar will be configured with defaults for calendar config.
     * 
     * @param businessGroup
     * @return
     */
    public OlatCalendar getGroupCalendar(BusinessGroup businessGroup);

    /**
     * Get calendar for course. If the calendar does not yet exist, a new calendar will be created. The calendar will be configured with defaults for calendar config.
     * 
     * @param course
     * @return
     */
    public OlatCalendar getCourseCalendar(ICourse course);

    /**
     * Delete the personal calendar of an identity.
     * 
     * @param identity
     */
    public void deletePersonalCalendar(Identity identity);

    /**
     * Delete the calendar of the given business group.
     * 
     * @param businessGroup
     */
    public void deleteGroupCalendar(BusinessGroup businessGroup);

    /**
     * Delete the calendar of the given course.
     * 
     * @param course
     */
    public void deleteCourseCalendar(ICourse course);

    /**
     * Check if a calendar already exists for the given id.
     * 
     * @param calendarID
     * @param type
     * @return
     */
    public boolean calendarExists(String calendarType, String calendarID);

    /**
     * Read the calendar file (.ics) from the olatdata section.
     * 
     * @param type
     * @param calendarID
     * @return
     */
    public abstract Calendar readCalendar(String type, String calendarID);

    /**
     * get the calendar file name from type and id
     * 
     * @param type
     * @param calendarID
     * @return
     */
    public abstract File getCalendarFile(String type, String calendarID);

    /**
     * Get a calendar by type and id.
     * 
     * @param type
     * @param calendarID
     * @return
     */
    public abstract OlatCalendar getCalendar(String type, String calendarID);

    /**
     * Delete a calendar.
     * 
     * @param calendarType
     * @param calendarID
     * @return
     */
    public abstract boolean deleteCalendar(String calendarType, String calendarID);

    /**
     * Build a Calendar object from String object.
     * 
     * @param calendarContent
     * @return
     */
    public abstract OlatCalendar buildCalendarFrom(String calendarContent, String calType, String calId);

    /**
     * Save a calendar.
     * 
     * @param calendar
     */
    public abstract boolean persistCalendar(OlatCalendar calendar);

    public void writeCalendarToFile(String calendarId, String calendarContent) throws IOException;

}
