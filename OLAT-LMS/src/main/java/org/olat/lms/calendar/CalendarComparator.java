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

import java.util.Comparator;

import org.olat.data.calendar.CalendarDao;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;

public class CalendarComparator implements Comparator {

    private static final CalendarComparator INSTANCE = new CalendarComparator();

    public static final CalendarComparator getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(final Object arg0, final Object arg1) {
        final CalendarRenderWrapper calendar0 = (CalendarRenderWrapper) arg0;
        final CalendarRenderWrapper calendar1 = (CalendarRenderWrapper) arg1;
        // if of the same type, order by display name
        if (calendar0.getCalendar().getType() == calendar1.getCalendar().getType()) {
            return calendar0.getCalendarConfig().getDisplayName().compareTo(calendar1.getCalendarConfig().getDisplayName());
        }
        // if of different type, order by type
        if (calendar0.getCalendar().getType() == CalendarDao.TYPE_USER) {
            return -1; // TYPE_USER is displayed first
        }
        if (calendar0.getCalendar().getType() == CalendarDao.TYPE_GROUP) {
            return +1; // TYPE GROUP is displayed last
        }
        if (calendar1.getCalendar().getType() == CalendarDao.TYPE_USER) {
            return +1;
        } else {
            return -1;
        }
    }

}
