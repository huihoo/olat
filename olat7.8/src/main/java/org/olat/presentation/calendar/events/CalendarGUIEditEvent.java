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

package org.olat.presentation.calendar.events;

import org.olat.data.calendar.CalendarEntry;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.system.event.Event;

public class CalendarGUIEditEvent extends Event {

    public static final String CMD_EDIT = "ecalevent";
    private final CalendarEntry event;
    private final CalendarRenderWrapper calendarWrapper;

    public CalendarGUIEditEvent(final CalendarEntry event, final CalendarRenderWrapper calendarWrapper) {
        super(CMD_EDIT);
        this.event = event;
        this.calendarWrapper = calendarWrapper;
    }

    public CalendarEntry getKalendarEvent() {
        return event;
    }

    public CalendarRenderWrapper getKalendarRenderWrapper() {
        return calendarWrapper;
    }
}
