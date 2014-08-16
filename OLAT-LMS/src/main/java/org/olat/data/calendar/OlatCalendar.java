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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * OlatCalendar, class name include 'Olat' because class 'Calendar' is already in use form iCal-library.
 * 
 * @author guretzki
 */
public class OlatCalendar implements Serializable {

    private final String calendarID;
    private final String type;
    private final Map<String, CalendarEntry> calendarEntries;

    public OlatCalendar(final String calendarID, final String type) {
        this.calendarID = calendarID;
        this.type = type;
        calendarEntries = new HashMap<String, CalendarEntry>();
    }

    /**
     * Return this calendar's ID.
     * 
     * @return
     */
    public String getCalendarID() {
        return calendarID;
    }

    /**
     * Add a new event.
     * 
     * @param calendarEntry
     */
    public void addEvent(final CalendarEntry calendarEntry) {
        calendarEntry.setKalendar(this);
        calendarEntries.put(calendarEntry.getID(), calendarEntry);
    }

    /**
     * Remove an event from this calendar.
     * 
     * @param calendarEntry
     */
    public void removeEvent(final CalendarEntry calendarEntry) {
        calendarEntries.remove(calendarEntry.getID());
    }

    /**
     * Get a specific event.
     * 
     * @param calendarEntryID
     * @return
     */
    public CalendarEntry getCalendarEntry(final String calendarEntryID) {
        return calendarEntries.get(calendarEntryID);
    }

    /**
     * Return all events associated with this calendar.
     * 
     * @return
     */
    public Collection<CalendarEntry> getAllCalendarEntries() {
        return calendarEntries.values();
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Calendar[type=" + getType() + ", id=" + getCalendarID() + "]";
    }

}
