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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olat.data.calendar.CalendarEntry;
import org.olat.lms.calendar.CalendarUtils;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.calendar.events.CalendarGUIAddEvent;
import org.olat.presentation.calendar.events.CalendarGUIEditEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.control.JSAndCSSAdder;
import org.olat.presentation.framework.core.render.ValidationResult;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;

public class WeeklyCalendarComponent extends Component {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String ID_CMD = "cmd";
    public static final String ID_PARAM = "p";
    public static final String ID_PARAM_SEPARATOR = "§";

    public static final String CMD_ADD = "add";
    public static final String CMD_EDIT = "edt";
    public static final String CMD_ADD_ALLDAY = "add_allday";

    private Map calendarRenderWrapperList = new HashMap();
    private int year;
    private int weekOfYear;
    private int displayDays = 7;
    private int viewStartHour = 7;
    private final boolean eventAlwaysVisible;

    /**
     * @param name
     * @param calendarWrappers
     * @param viewStartHour
     * @param translator
     * @param eventAlwaysVisible
     *            When true, the 'isVis()' check is disabled and events will be displayed always.
     */
    public WeeklyCalendarComponent(final String name, final Collection calendarWrappers, final int viewStartHour, final Translator translator,
            final Boolean eventAlwaysVisible) {
        super(name, translator);
        this.viewStartHour = viewStartHour;
        this.eventAlwaysVisible = eventAlwaysVisible;
        setDate(new Date());
        setCalendarRenderWrappers(calendarWrappers);
    }

    /**
     * Set this calendars focus to year/weekOfYear.
     * 
     * @param year
     * @param weekOfYear
     */
    public void setFocus(final int year, final int weekOfYear) {
        this.year = year;
        this.weekOfYear = weekOfYear;
        setDirty(true);
    }

    /**
     * Set how many days from the beginning of a week should be displayd (e.g. 7=sholw week; 5=MO-FI)
     * 
     * @param displayDays
     */
    public void setDisplayDays(final int displayDays) {
        this.displayDays = displayDays;
    }

    /**
	 */
    @Override
    protected void doDispatchRequest(final UserRequest ureq) {
        final String command = ureq.getParameter(ID_CMD);
        if (command == null) {
            return;
        } else if (command.equals(CMD_EDIT)) {
            final String param = ureq.getParameter(ID_PARAM);
            final StringTokenizer st = new StringTokenizer(param, ID_PARAM_SEPARATOR, false);
            if (st.countTokens() != 3) {
                return;
            }
            final String calendarID = st.nextToken();
            final String eventID = st.nextToken();
            final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) calendarRenderWrapperList.get(calendarID);
            final CalendarEntry event = calendarWrapper.getCalendar().getCalendarEntry(eventID);
            CalendarEntry recurEvent = null;
            final Long time = Long.parseLong(st.nextToken());
            final Date dateStart = new Date(time);
            final Calendar cal = CalendarUtils.createCalendarInstance(ureq.getLocale());
            cal.setTime(dateStart);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            final Date dateEnd = cal.getTime();
            recurEvent = event.getRecurringInPeriod(dateStart, dateEnd);
            setDirty(true);
            fireEvent(ureq, new CalendarGUIEditEvent(recurEvent != null ? recurEvent : event, calendarWrapper));
        } else if (command.equals(CMD_ADD) || command.equals(CMD_ADD_ALLDAY)) {
            // this will get us the day of the year
            final String sDate = ureq.getParameter(ID_PARAM);
            Date timeDate = new Date();
            try {
                final DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmm");
                timeDate = dateFormat.parse(sDate);
            } catch (final ParseException pe) {
                // ok, already initialized
            }
            // find first available writeable calendar
            // PRE: component renderer makes sure, at least one writeable calendar exists.
            CalendarRenderWrapper calendarWrapper = null;
            for (final Iterator iter = getCalendarRenderWrappers().iterator(); iter.hasNext();) {
                calendarWrapper = (CalendarRenderWrapper) iter.next();
                if (calendarWrapper.getAccess() == CalendarRenderWrapper.ACCESS_READ_WRITE) {
                    break;
                }
            }
            setDirty(true);
            fireEvent(ureq, new CalendarGUIAddEvent(calendarWrapper.getCalendar().getCalendarID(), timeDate, command.equals(CMD_ADD_ALLDAY)));
        }
    }

    /**
	 */
    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return new WeeklyCalendarComponentRenderer(viewStartHour);
    }

    Collection getCalendarRenderWrappers() {
        return calendarRenderWrapperList.values();
    }

    public CalendarRenderWrapper getCalendarRenderWrapper(final String calendarID) {
        return (CalendarRenderWrapper) calendarRenderWrapperList.get(calendarID);
    }

    public void setCalendarRenderWrappers(final Collection calendarRenderWrappers) {
        this.calendarRenderWrapperList = new HashMap();
        for (final Iterator iter = calendarRenderWrappers.iterator(); iter.hasNext();) {
            final CalendarRenderWrapper calendarRenderWrapper = (CalendarRenderWrapper) iter.next();
            this.calendarRenderWrapperList.put(calendarRenderWrapper.getCalendar().getCalendarID(), calendarRenderWrapper);
        }
    }

    public int getDisplayDays() {
        return displayDays;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public int getYear() {
        return year;
    }

    /**
	 */
    @Override
    public void validate(final UserRequest ureq, final ValidationResult vr) {
        super.validate(ureq, vr);
        final JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
        jsa.addRequiredJsFile(CalendarController.class, "js/calendar.js");
        jsa.addRequiredCSSFile(CalendarController.class, "css/calendar.css", false);
    }

    /**
     * Go back to previous week.
     */
    public void previousWeek() {
        // wrap this call because junit tests must set the calender with different values
        previousWeek(CalendarUtils.createCalendarInstance(getTranslator().getLocale()));
    }

    protected void previousWeek(final Calendar cal) {
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.WEEK_OF_YEAR, getWeekOfYear());
        final int lastWeekOfYear = getWeekOfYear();
        final int lastYear = getYear();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        // year change must correspond with weekOfYear
        // problem java-calendar is daily based,
        // weeks 52,53,1 can include days from old and new year
        if ((lastWeekOfYear == 2) && (cal.get(Calendar.YEAR) != lastYear)) {
            setFocus(lastYear, cal.get(Calendar.WEEK_OF_YEAR));
        } else if ((cal.get(Calendar.WEEK_OF_YEAR) == 53) || ((cal.get(Calendar.WEEK_OF_YEAR) == 52) && (lastWeekOfYear != 53)) && (lastYear == cal.get(Calendar.YEAR))) {
            setFocus(lastYear - 1, cal.get(Calendar.WEEK_OF_YEAR));
        } else {
            setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
        }
    }

    /**
     * Go back to next week.
     */
    public void nextWeek() {
        // wrap this call because junit tests must set the calender with different values
        nextWeek(CalendarUtils.createCalendarInstance(getTranslator().getLocale()));
    }

    protected void nextWeek(final Calendar cal) {
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.WEEK_OF_YEAR, getWeekOfYear());
        final int lastYear = getYear();
        log.info("nextWeek (1): getYear()=" + getYear() + "  getWeekOfYear()=" + getWeekOfYear());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        log.info("nextWeek (2): cal.get(Calendar.WEEK_OF_YEAR)=" + cal.get(Calendar.WEEK_OF_YEAR) + "  cal.get(Calendar.YEAR)=" + cal.get(Calendar.YEAR));

        // year change must correspond with weekOfYear
        // problem java-calendar is daily based,
        // weeks 52,53,1 can include days from old and new year
        if ((cal.get(Calendar.WEEK_OF_YEAR) == 1) && (cal.get(Calendar.YEAR) == lastYear)) {
            log.info("nextWeek (3): case 1 setFocus(lastYear + 1 , cal.get(Calendar.WEEK_OF_YEAR))");
            setFocus(lastYear + 1, cal.get(Calendar.WEEK_OF_YEAR));
        } else if ((cal.get(Calendar.WEEK_OF_YEAR) == 53) || (cal.get(Calendar.WEEK_OF_YEAR) == 52)) {
            log.info("nextWeek (4): case 2 setFocus(lastYear , cal.get(Calendar.WEEK_OF_YEAR))");
            setFocus(lastYear, cal.get(Calendar.WEEK_OF_YEAR));
        } else {
            log.info("nextWeek (5): case 3 setFocus(cal.get(Calendar.YEAR) , cal.get(Calendar.WEEK_OF_YEAR))");
            setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
        }
    }

    /**
     * Set focus of calendar-component to certain date. Calculate correct week-of-year and year for certain date.
     * 
     * @param gotoDate
     */
    public void setDate(final Date gotoDate) {
        final Calendar cal = CalendarUtils.createCalendarInstance(getTranslator().getLocale());
        cal.setTime(gotoDate);
        int weekYear = cal.get(Calendar.YEAR);
        final int week = cal.get(Calendar.WEEK_OF_YEAR);
        if (week == 1) {
            // Week 1 is a special case: the date could be the last days of december, but the week is still counted as week one of the next year. Use the next year in
            // this case to match the week number.
            if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
                weekYear++;
            }
        } else if (week >= 52) {
            // Opposite check: date could be first days of january, but the week is still counted as the last week of the passed year. Use the last year in this case to
            // match the week number.
            if (cal.get(Calendar.MONTH) == Calendar.JANUARY) {
                weekYear--;
            }
        }
        setFocus(weekYear, week);
    }

    /**
     * Returns true when events should be visible always (renderer does not check isVis() )
     * 
     * @return
     */
    public boolean isEventAlwaysVisible() {
        return eventAlwaysVisible;
    }

}
