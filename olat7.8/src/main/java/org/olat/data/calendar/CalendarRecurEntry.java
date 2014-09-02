package org.olat.data.calendar;

import java.util.Date;
import java.util.List;

/**
 * Description:<br>
 * Kalendar Event for recurring events
 * <P>
 * Initial Date: 08.04.2009 <br>
 * 
 * @author skoeber
 */
public class CalendarRecurEntry extends CalendarEntry {

    CalendarEntry sourceCalendarEntry;

    public CalendarRecurEntry(final String id, final String subject, final Date begin, final Date end) {
        super(id, subject, begin, end);
    }

    public CalendarRecurEntry(final String id, final String subject, final Date begin, final int duration) {
        super(id, subject, begin, duration);
    }

    public CalendarRecurEntry(final String id, final String subject, final Date begin, final int duration, final String recurrenceRule) {
        super(id, subject, begin, duration, recurrenceRule);
    }

    public CalendarRecurEntry(final String id, final String subject, final Date begin, final Date end, final String recurrenceRule) {
        super(id, subject, begin, end, recurrenceRule);
    }

    /**
     * @return source event for this recurrence
     */
    public CalendarEntry getSourceCalendarEntry() {
        return sourceCalendarEntry;
    }

    /**
     * @param source
     *            event for this recurrence
     */
    public void setSourceCalendarEntry(final CalendarEntry sourceCalendarEntry) {
        this.sourceCalendarEntry = sourceCalendarEntry;
    }

    @Override
    public OlatCalendar getCalendar() {
        return sourceCalendarEntry.getCalendar();
    }

    @Override
    public int getClassification() {
        return sourceCalendarEntry.getClassification();
    }

    @Override
    public String getComment() {
        return sourceCalendarEntry.getComment();
    }

    @Override
    public long getCreated() {
        return sourceCalendarEntry.getCreated();
    }

    @Override
    public String getCreatedBy() {
        return sourceCalendarEntry.getCreatedBy();
    }

    @Override
    public String getDescription() {
        return sourceCalendarEntry.getDescription();
    }

    @Override
    public String getID() {
        return sourceCalendarEntry.getID();
    }

    @Override
    public List getCalendarEntryLinks() {
        return sourceCalendarEntry.getCalendarEntryLinks();
    }

    @Override
    public long getLastModified() {
        return sourceCalendarEntry.getLastModified();
    }

    @Override
    public String getLocation() {
        return sourceCalendarEntry.getLocation();
    }

    @Override
    public Integer getNumParticipants() {
        return sourceCalendarEntry.getNumParticipants();
    }

    @Override
    public String[] getParticipants() {
        return sourceCalendarEntry.getParticipants();
    }

    @Override
    public String getRecurrenceRule() {
        return sourceCalendarEntry.getRecurrenceRule();
    }

    @Override
    public String getSourceNodeId() {
        return sourceCalendarEntry.getSourceNodeId();
    }

    @Override
    public String getSubject() {
        return sourceCalendarEntry.getSubject();
    }

    @Override
    public boolean isAllDayEvent() {
        return sourceCalendarEntry.isAllDayEvent();
    }
}
