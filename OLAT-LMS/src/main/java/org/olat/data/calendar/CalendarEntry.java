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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

public class CalendarEntry implements Cloneable, Comparable {
    private static final Logger log = LoggerHelper.getLogger();

    public static final int CLASS_PRIVATE = 0;
    public static final int CLASS_X_FREEBUSY = 1;
    public static final int CLASS_PUBLIC = 2;

    public static final String DAILY = Recur.DAILY;
    public static final String WEEKLY = Recur.WEEKLY;
    public static final String MONTHLY = Recur.MONTHLY;
    public static final String YEARLY = Recur.YEARLY;
    public static final String WORKDAILY = "WORKDAILY";
    public static final String BIWEEKLY = "BIWEEKLY";

    public static final String UNTIL = "UNTIL";
    public static final String COUNT = "COUNT";

    private String id;
    transient private OlatCalendar calendar;
    private String subject;
    private String description;
    private Date begin, end;
    private boolean isAllDayEvent;
    private String location;
    private List calendarEntryLinks;
    private long created, lastModified;
    private String createdBy;
    private int classification;

    private String comment;
    private Integer numParticipants;
    private String[] participants;
    private String sourceNodeId;

    private String recurrenceRule;
    private String recurrenceExc;

    private CalendarEntry() {
        // save no-args constructor for XStream
    }

    /**
     * Create a new calendar event with the given subject and given start and end times as UNIX timestamps.
     * 
     * @param subject
     * @param begin
     * @param end
     */
    public CalendarEntry(final String id, final String subject, final Date begin, final Date end) {
        this.id = id;
        this.subject = subject;
        this.begin = begin;
        this.end = end;
        this.isAllDayEvent = false;
        this.calendarEntryLinks = new ArrayList();
    }

    /**
     * Create a new calendar entry with the given subject, starting at <begin> and with a duration of <duration> milliseconds.
     * 
     * @param subject
     * @param begin
     * @param duration
     */
    public CalendarEntry(final String id, final String subject, final Date begin, final int duration) {
        this.id = id;
        this.subject = subject;
        this.begin = begin;
        this.end = new Date(begin.getTime() + duration);
        this.isAllDayEvent = false;
        this.calendarEntryLinks = new ArrayList();
    }

    /**
     * Create a new calendar entry with the given start, a duration and a recurrence
     * 
     * @param id
     * @param subject
     * @param begin
     * @param duration
     * @param recurrenceRule
     */
    public CalendarEntry(final String id, final String subject, final Date begin, final int duration, final String recurrenceRule) {
        this(id, subject, begin, duration);
        this.recurrenceRule = recurrenceRule;
    }

    /**
     * Create a new calendar entry with the given start and end
     * 
     * @param id
     * @param subject
     * @param begin
     * @param end
     * @param recurrenceRule
     */
    public CalendarEntry(final String id, final String subject, final Date begin, final Date end, final String recurrenceRule) {
        this(id, subject, begin, end);
        this.recurrenceRule = recurrenceRule;
    }

    protected void setKalendar(final OlatCalendar calendar) {
        this.calendar = calendar;
    }

    public String getID() {
        return id;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(final Date begin) {
        this.begin = begin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(final Date end) {
        this.end = end;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public int getClassification() {
        return classification;
    }

    public void setClassification(final int classification) {
        this.classification = classification;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(final long created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public OlatCalendar getCalendar() {
        return calendar;
    }

    public boolean isAllDayEvent() {
        return isAllDayEvent;
    }

    public void setAllDayEvent(final boolean isAllDayEvent) {
        this.isAllDayEvent = isAllDayEvent;
    }

    public boolean isToday() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(begin);
        final int startDay = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTime(end);
        final int endDay = cal.get(Calendar.DAY_OF_YEAR);
        final int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return (todayDay - startDay == 0) && ((todayDay - endDay == 0));
    }

    /**
     * @return
     */
    public boolean isWithinOneDay() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(begin);
        final int startDay = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTime(end);
        final int endDay = cal.get(Calendar.DAY_OF_YEAR);
        return (endDay - startDay == 0);
    }

    /**
     * @return Returns the uRI.
     */
    public List getCalendarEntryLinks() {
        return calendarEntryLinks;
    }

    /**
     * @param uri
     *            The uRI to set.
     */
    public void setCalendarEntryLinks(final List calendarEntryLinks) {
        this.calendarEntryLinks = calendarEntryLinks;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Integer getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(final int numParticipants) {
        this.numParticipants = numParticipants;
    }

    public String[] getParticipants() {
        return participants;
    }

    public void setParticipants(final String[] participants) {
        this.participants = participants;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(final String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(final String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    @Override
    public CalendarEntry clone() {
        Object c = null;
        try {
            c = super.clone();
        } catch (final CloneNotSupportedException e) {
            return null;
        }
        return (CalendarEntry) c;
    }

    public void setRecurrenceExc(final String recurrenceExc) {
        this.recurrenceExc = recurrenceExc;
    }

    public String getRecurrenceExc() {
        return recurrenceExc;
    }

    public void addRecurrenceExc(final Date excDate) {
        final List<Date> excDates = getRecurrenceExcludeDates(recurrenceExc);
        excDates.add(excDate);
        final String excRule = getRecurrenceExcludeRule(excDates);
        setRecurrenceExc(excRule);
    }

    @Override
    public int compareTo(final Object o1) {
        if (!(o1 instanceof CalendarEntry)) {
            return -1;
        }
        final CalendarEntry event1 = (CalendarEntry) o1;
        return this.getBegin().compareTo(event1.getBegin());
    }

    /**
     * @param rule
     * @return date of recurrence end
     */
    public Date getRecurrenceEndDate() {
        final TimeZone tz = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());

        if (recurrenceRule != null) {
            try {
                final Recur recur = new Recur(recurrenceRule);
                final Date dUntil = recur.getUntil();
                final DateTime dtUntil = dUntil == null ? null : new DateTime(dUntil.getTime());
                if (dtUntil != null) {
                    dtUntil.setTimeZone(tz);
                    return dtUntil;
                }
            } catch (final ParseException e) {
                log.error("cannot restore recurrence rule", e);
            }
        }

        return null;
    }

    /**
     * Get the recurring event
     * 
     * @param today
     * @param kEvent
     * @return affected <code>KalendarEvent</code> or <code>null</code> if not recurring in period
     */
    public CalendarEntry getRecurringInPeriod(final Date periodStart, final Date periodEnd) {
        final boolean isRecurring = isRecurringInPeriod(periodStart, periodEnd);
        CalendarEntry recurEvent = null;

        if (isRecurring) {
            final java.util.Calendar periodStartCal = java.util.Calendar.getInstance();
            final java.util.Calendar eventBeginCal = java.util.Calendar.getInstance();

            periodStartCal.setTime(periodStart);
            eventBeginCal.setTime(getBegin());

            final Long duration = getEnd().getTime() - getBegin().getTime();

            final java.util.Calendar beginCal = java.util.Calendar.getInstance();
            beginCal.setTime(getBegin());
            beginCal.set(java.util.Calendar.YEAR, periodStartCal.get(java.util.Calendar.YEAR));
            beginCal.set(java.util.Calendar.MONTH, periodStartCal.get(java.util.Calendar.MONTH));
            beginCal.set(java.util.Calendar.DAY_OF_MONTH, periodStartCal.get(java.util.Calendar.DAY_OF_MONTH));

            recurEvent = clone();
            recurEvent.setBegin(beginCal.getTime());
            recurEvent.setEnd(new Date(beginCal.getTime().getTime() + duration));
        }

        return recurEvent;
    }

    /**
     * Check if the event recurs within the given period
     * 
     * @param periodStart
     * @param periodEnd
     * @param kEvent
     * @return <code>true</code> if event recurs in the given period, otherwise <code>false</code>
     */
    private boolean isRecurringInPeriod(final Date periodStart, final Date periodEnd) {
        final DateList recurDates = getRecurringsInPeriod(periodStart, periodEnd);
        return (recurDates != null && !recurDates.isEmpty());
    }

    private DateList getRecurringsInPeriod(final Date periodStart, final Date periodEnd) {
        DateList recurDates = null;
        final String recurrenceRule = getRecurrenceRule();
        if (recurrenceRule != null && !recurrenceRule.equals("")) {
            try {
                final Recur recur = new Recur(recurrenceRule);
                final net.fortuna.ical4j.model.Date periodStartDate = new net.fortuna.ical4j.model.Date(periodStart);
                final net.fortuna.ical4j.model.Date periodEndDate = new net.fortuna.ical4j.model.Date(periodEnd);
                final net.fortuna.ical4j.model.Date eventStartDate = new net.fortuna.ical4j.model.Date(getBegin());
                recurDates = recur.getDates(eventStartDate, periodStartDate, periodEndDate, Value.DATE);
            } catch (final ParseException e) {
                log.error("cannot restore recurrence rule: " + recurrenceRule, e);
            }

            final String recurrenceExc = getRecurrenceExc();
            if (recurrenceExc != null && !recurrenceExc.equals("")) {
                try {
                    final ExDate exdate = new ExDate();
                    // expected date+time format:
                    // 20100730T100000
                    // unexpected all-day format:
                    // 20100730
                    // see OLAT-5645
                    if (recurrenceExc.length() > 8) {
                        exdate.setValue(recurrenceExc);
                    } else {
                        exdate.getParameters().replace(Value.DATE);
                        exdate.setValue(recurrenceExc);
                    }
                    for (final Object date : exdate.getDates()) {
                        if (recurDates.contains(date)) {
                            recurDates.remove(date);
                        }
                    }
                } catch (final ParseException e) {
                    log.error("cannot restore excluded dates for this recurrence: " + recurrenceExc, e);
                }
            }
        }

        return recurDates;
    }

    /**
     * Get all recurrings of an event within the given period
     * 
     * @param periodStart
     * @param periodEnd
     * @param kEvent
     * @return list with <code>KalendarRecurEvent</code>
     */
    public List<CalendarRecurEntry> getRecurringDatesInPeriod(final Date periodStart, final Date periodEnd) {
        final List<CalendarRecurEntry> lstDates = new ArrayList<CalendarRecurEntry>();
        final DateList recurDates = getRecurringsInPeriod(periodStart, periodEnd);
        if (recurDates == null) {
            return lstDates;
        }

        for (final Object obj : recurDates) {
            final net.fortuna.ical4j.model.Date date = (net.fortuna.ical4j.model.Date) obj;

            CalendarRecurEntry recurEvent;

            final java.util.Calendar eventStartCal = java.util.Calendar.getInstance();
            eventStartCal.clear();
            eventStartCal.setTime(getBegin());

            final java.util.Calendar eventEndCal = java.util.Calendar.getInstance();
            eventEndCal.clear();
            eventEndCal.setTime(getEnd());

            final java.util.Calendar recurStartCal = java.util.Calendar.getInstance();
            recurStartCal.clear();
            recurStartCal.setTimeInMillis(date.getTime());

            final long duration = getEnd().getTime() - getBegin().getTime();

            final java.util.Calendar beginCal = java.util.Calendar.getInstance();
            beginCal.clear();
            beginCal.set(recurStartCal.get(java.util.Calendar.YEAR), recurStartCal.get(java.util.Calendar.MONTH), recurStartCal.get(java.util.Calendar.DATE),
                    eventStartCal.get(java.util.Calendar.HOUR_OF_DAY), eventStartCal.get(java.util.Calendar.MINUTE), eventStartCal.get(java.util.Calendar.SECOND));

            final java.util.Calendar endCal = java.util.Calendar.getInstance();
            endCal.clear();
            endCal.setTimeInMillis(beginCal.getTimeInMillis() + duration);
            if (getBegin().compareTo(beginCal.getTime()) == 0) {
                continue; // prevent doubled events
            }
            final Date recurrenceEnd = getRecurrenceEndDate();
            if (isAllDayEvent() && recurrenceEnd != null && recurStartCal.getTime().after(recurrenceEnd)) {
                continue; // workaround for ical4j-bug in all day events
            }
            recurEvent = new CalendarRecurEntry(getID(), getSubject(), new Date(beginCal.getTimeInMillis()), new Date(endCal.getTimeInMillis()));
            recurEvent.setSourceCalendarEntry(this);
            lstDates.add(recurEvent);
        }
        return lstDates;
    }

    /**
     * Create list with excluded dates based on the exclusion rule.
     * 
     * @param recurrenceExc
     * @return list with excluded dates
     */
    private List<Date> getRecurrenceExcludeDates(final String recurrenceExc) {
        final List<Date> recurExcDates = new ArrayList<Date>();
        if (recurrenceExc != null && !recurrenceExc.equals("")) {
            try {
                final net.fortuna.ical4j.model.ParameterList pl = new net.fortuna.ical4j.model.ParameterList();
                final ExDate exdate = new ExDate(pl, recurrenceExc);
                final DateList dl = exdate.getDates();
                for (final Object date : dl) {
                    final Date excDate = (Date) date;
                    recurExcDates.add(excDate);
                }
            } catch (final ParseException e) {
                log.error("cannot restore recurrence exceptions", e);
            }
        }

        return recurExcDates;
    }

    /**
     * Create exclusion rule based on list with dates.
     * 
     * @param dates
     * @return string with exclude rule
     */
    private static String getRecurrenceExcludeRule(final List<Date> dates) {
        if (dates != null && dates.size() > 0) {
            final DateList dl = new DateList();
            for (final Date date : dates) {
                final net.fortuna.ical4j.model.Date dd = new net.fortuna.ical4j.model.Date(date);
                dl.add(dd);
            }
            final ExDate exdate = new ExDate(dl);
            return exdate.getValue();
        }

        return null;
    }

}
