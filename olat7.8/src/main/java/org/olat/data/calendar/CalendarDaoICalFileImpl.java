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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Contact;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CalendarDaoICalFileImpl extends BasicManager implements CalendarDao, Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private final File fStorageBase;
    // o_clusterOK by:cg
    private CacheWrapper calendarCache;

    private static final Clazz ICAL_CLASS_PRIVATE = new Clazz("PRIVATE");
    private static final Clazz ICAL_CLASS_PUBLIC = new Clazz("PUBLIC");
    private static final Clazz ICAL_CLASS_X_FREEBUSY = new Clazz("X-FREEBUSY");

    private static final String ICAL_X_OLAT_LINK = "X-OLAT-LINK";

    private static final String ICAL_X_OLAT_COMMENT = "X-OLAT-COMMENT";
    private static final String ICAL_X_OLAT_NUMPARTICIPANTS = "X-OLAT-NUMPARTICIPANTS";
    private static final String ICAL_X_OLAT_PARTICIPANTS = "X-OLAT-PARTICIPANTS";
    private static final String ICAL_X_OLAT_SOURCENODEID = "X-OLAT-SOURCENODEID";

    /** rule for recurring events */
    private static final String ICAL_RRULE = "RRULE";
    /** property to exclude events from recurrence */
    private static final String ICAL_EXDATE = "EXDATE";

    private final TimeZone tz;

    @Autowired
    CoordinatorManager coordinatorManager;

    /**
     * [spring]
     */
    private CalendarDaoICalFileImpl() {
        this.fStorageBase = new File(WebappHelper.getUserDataRoot() + "/calendars");
        if (!fStorageBase.exists()) {
            if (!fStorageBase.mkdirs()) {
                throw new OLATRuntimeException("Error creating calendar base directory at: " + fStorageBase.getAbsolutePath(), null);
            }
        }
        createCalendarFileDirectories();
        // set parser to relax (needed for allday events
        // see http://sourceforge.net/forum/forum.php?thread_id=1253735&forum_id=368291
        System.setProperty("ical4j.unfolding.relaxed", "true");
        // initialize timezone
        tz = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(java.util.Calendar.getInstance().getTimeZone().getID());
        // INSTANCE = this;
    }

    /**
     * Check if a calendar already exists for the given id.
     * 
     * @param calendarID
     * @param type
     * @return
     */
    @Override
    public boolean calendarExists(final String calendarType, final String calendarID) {
        return getCalendarFile(calendarType, calendarID).exists();
    }

    /**
	 */
    @Override
    public OlatCalendar createCalendar(final String type, final String calendarID) {
        return new OlatCalendar(calendarID, type);
    }

    @Override
    public OlatCalendar getCalendar(final String type, final String calendarID) {
        // o_clusterOK by:cg
        final OLATResourceable calOres = OresHelper.createOLATResourceableType(getKeyFor(type, calendarID));
        final String callType = type;
        final String callCalendarID = calendarID;
        final OlatCalendar cal = coordinatorManager.getCoordinator().getSyncer().doInSync(calOres, new SyncerCallback<OlatCalendar>() {
            @Override
            public OlatCalendar execute() {
                return getCalendarFromCache(callType, callCalendarID);
            }
        });
        return cal;
    }

    protected OlatCalendar getCalendarFromCache(final String callType, final String callCalendarID) {
        final OLATResourceable calOres = OresHelper.createOLATResourceableType(getKeyFor(callType, callCalendarID));
        coordinatorManager.getCoordinator().getSyncer().assertAlreadyDoInSyncFor(calOres);

        final String key = getKeyFor(callType, callCalendarID);
        OlatCalendar cal = (OlatCalendar) calendarCache.get(key);
        if (cal == null) {
            cal = loadOrCreateCalendar(callType, callCalendarID);
            calendarCache.put(key, cal);
        }
        return cal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OLATResourceable getOresHelperFor(final OlatCalendar cal) {
        return OresHelper.createOLATResourceableType(getKeyFor(cal.getType(), cal.getCalendarID()));
    }

    private String getKeyFor(final String type, final String calendarID) {
        return type + "_" + calendarID;
    }

    /**
     * Internal load calendar file from filesystem.
     */
    // o_clusterOK by:cg This must not be synchronized because the caller already synchronized
    private OlatCalendar loadCalendarFromFile(final String type, final String calendarID) {
        final Calendar calendar = readCalendar(type, calendarID);
        final OlatCalendar olatCalendar = createKalendar(type, calendarID, calendar);
        return olatCalendar;
    }

    private OlatCalendar createKalendar(final String type, final String calendarID, final Calendar calendar) {
        final OlatCalendar olatCalendar = new OlatCalendar(calendarID, type);
        for (final Iterator iter = calendar.getComponents().iterator(); iter.hasNext();) {
            final Component comp = (Component) iter.next();
            if (comp instanceof VEvent) {
                final VEvent vevent = (VEvent) comp;
                final CalendarEntry calEntry = getCalendarEntry(vevent);
                olatCalendar.addEvent(calEntry);
            } else if (comp instanceof VTimeZone) {
                log.info("createKalendar: VTimeZone Component is not supported and will not be added to calender");
                log.debug("createKalendar: VTimeZone=" + comp);
            } else {
                log.warn("createKalendar: unknown Component=" + comp);
            }
        }
        return olatCalendar;
    }

    /**
     * Internal read calendar file from filesystem
     */
    @Override
    public Calendar readCalendar(final String type, final String calendarID) {
        log.debug("readCalendar from file, type=" + type + "  calendarID=" + calendarID);
        final File calendarFile = getCalendarFile(type, calendarID);

        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(calendarFile));
        } catch (final FileNotFoundException fne) {
            throw new OLATRuntimeException("Not found: " + calendarFile, fne);
        }

        final CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = null;
        try {
            calendar = builder.build(in);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Error parsing calendar file.", e);
        } finally {
            if (in != null) {
                FileUtils.closeSafely(in);
            }
        }
        return calendar;
    }

    @Override
    public OlatCalendar buildKalendarFrom(final String calendarContent, final String calType, final String calId) {
        OlatCalendar olatCalendar = null;
        final BufferedReader reader = new BufferedReader(new StringReader(calendarContent));
        final CalendarBuilder builder = new CalendarBuilder();
        try {
            final Calendar calendar = builder.build(reader);
            olatCalendar = createKalendar(calType, calId, calendar);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Error parsing calendar file.", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    throw new OLATRuntimeException("Could not close reader after build calendar file.", e);
                }
            }
        }
        return olatCalendar;
    }

    /**
     * Save a calendar. This method is not thread-safe. Must be called from a synchronized block. Be sure to have the newest calendar (reload calendar in synchronized
     * block before safe it).
     * 
     * @param calendar
     */
    // o_clusterOK by:cg only called by Junit-test
    @Override
    public boolean persistCalendar(final OlatCalendar olatCalendar) {
        final Calendar calendar = buildCalendar(olatCalendar);
        final boolean success = writeCalendarFile(calendar, olatCalendar.getType(), olatCalendar.getCalendarID());
        calendarCache.update(getKeyFor(olatCalendar.getType(), olatCalendar.getCalendarID()), olatCalendar);
        return success;
    }

    private boolean writeCalendarFile(final Calendar calendar, final String calType, final String calId) {
        final File fCalendarFile = getCalendarFile(calType, calId);
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(fCalendarFile, false));
            final CalendarOutputter calOut = new CalendarOutputter(false);
            calOut.output(calendar, os);
        } catch (final Exception e) {
            return false;
        } finally {
            FileUtils.closeSafely(os);
        }
        return true;
    }

    /**
     * Delete calendar by type and id.
     */
    @Override
    public boolean deleteCalendar(final String type, final String calendarID) {
        calendarCache.remove(getKeyFor(type, calendarID));
        final File fCalendarFile = getCalendarFile(type, calendarID);
        return fCalendarFile.delete();
    }

    @Override
    public File getCalendarICalFile(final String type, final String calendarID) {
        final File fCalendarICalFile = getCalendarFile(type, calendarID);
        if (fCalendarICalFile.exists()) {
            return fCalendarICalFile;
        } else {
            return null;
        }
    }

    private Calendar buildCalendar(final OlatCalendar olatCalendar) {
        final Calendar calendar = new Calendar();
        // add standard propeties
        calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        for (final Iterator<CalendarEntry> iter = olatCalendar.getAllCalendarEntries().iterator(); iter.hasNext();) {
            final CalendarEntry kEvent = iter.next();
            final VEvent vEvent = getVEvent(kEvent);
            calendar.getComponents().add(vEvent);
        }
        return calendar;
    }

    private VEvent getVEvent(final CalendarEntry calendarEntry) {
        VEvent vEvent = new VEvent();
        if (!calendarEntry.isAllDayEvent()) {
            // regular VEvent
            final DateTime dtBegin = new DateTime(calendarEntry.getBegin());
            dtBegin.setTimeZone(tz);
            final DateTime dtEnd = new DateTime(calendarEntry.getEnd());
            dtEnd.setTimeZone(tz);
            vEvent = new VEvent(dtBegin, dtEnd, calendarEntry.getSubject());
        } else {
            // AllDay VEvent
            final net.fortuna.ical4j.model.Date dtBegin = new net.fortuna.ical4j.model.Date(calendarEntry.getBegin());
            // adjust end date: ICal end dates for all day events are on the next day
            final Date adjustedEndDate = new Date(calendarEntry.getEnd().getTime() + (1000 * 60 * 60 * 24));
            final net.fortuna.ical4j.model.Date dtEnd = new net.fortuna.ical4j.model.Date(adjustedEndDate);
            vEvent = new VEvent(dtBegin, dtEnd, calendarEntry.getSubject());
            vEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);
            vEvent.getProperties().getProperty(Property.DTEND).getParameters().add(Value.DATE);
        }

        if (calendarEntry.getCreated() > 0) {
            final Created created = new Created(new DateTime(calendarEntry.getCreated()));
            vEvent.getProperties().add(created);
        }

        if ((calendarEntry.getCreatedBy() != null) && !calendarEntry.getCreatedBy().trim().isEmpty()) {
            final Contact contact = new Contact();
            contact.setValue(calendarEntry.getCreatedBy());
            vEvent.getProperties().add(contact);
        }

        if (calendarEntry.getLastModified() > 0) {
            final LastModified lastMod = new LastModified(new DateTime(calendarEntry.getLastModified()));
            vEvent.getProperties().add(lastMod);
        }

        // Uid
        final PropertyList vEventProperties = vEvent.getProperties();
        vEventProperties.add(new Uid(calendarEntry.getID()));

        // clazz
        switch (calendarEntry.getClassification()) {
        case CalendarEntry.CLASS_PRIVATE:
            vEventProperties.add(ICAL_CLASS_PRIVATE);
            break;
        case CalendarEntry.CLASS_PUBLIC:
            vEventProperties.add(ICAL_CLASS_PUBLIC);
            break;
        case CalendarEntry.CLASS_X_FREEBUSY:
            vEventProperties.add(ICAL_CLASS_X_FREEBUSY);
            break;
        default:
            vEventProperties.add(ICAL_CLASS_PRIVATE);
            break;
        }

        // location
        if (calendarEntry.getLocation() != null) {
            vEventProperties.add(new Location(calendarEntry.getLocation()));
        }

        // event links
        final List calendarEventLinks = calendarEntry.getCalendarEntryLinks();
        if ((calendarEventLinks != null) && !calendarEventLinks.isEmpty()) {
            for (final Iterator iter = calendarEventLinks.iterator(); iter.hasNext();) {
                final CalendarEntryLink link = (CalendarEntryLink) iter.next();
                final StringBuilder linkEncoded = new StringBuilder(200);
                linkEncoded.append(link.getProvider());
                linkEncoded.append("§");
                linkEncoded.append(link.getId());
                linkEncoded.append("§");
                linkEncoded.append(link.getDisplayName());
                linkEncoded.append("§");
                linkEncoded.append(link.getURI());
                linkEncoded.append("§");
                linkEncoded.append(link.getIconCssClass());
                final XProperty linkProperty = new XProperty(ICAL_X_OLAT_LINK, linkEncoded.toString());
                vEventProperties.add(linkProperty);
            }
        }

        if (calendarEntry.getComment() != null) {
            vEventProperties.add(new XProperty(ICAL_X_OLAT_COMMENT, calendarEntry.getComment()));
        }
        if (calendarEntry.getNumParticipants() != null) {
            vEventProperties.add(new XProperty(ICAL_X_OLAT_NUMPARTICIPANTS, Integer.toString(calendarEntry.getNumParticipants())));
        }
        if (calendarEntry.getParticipants() != null) {
            final StringBuffer strBuf = new StringBuffer();
            final String[] participants = calendarEntry.getParticipants();
            for (final String participant : participants) {
                strBuf.append(participant);
                strBuf.append("§");
            }
            vEventProperties.add(new XProperty(ICAL_X_OLAT_PARTICIPANTS, strBuf.toString()));
        }
        if (calendarEntry.getSourceNodeId() != null) {
            vEventProperties.add(new XProperty(ICAL_X_OLAT_SOURCENODEID, calendarEntry.getSourceNodeId()));
        }

        // recurrence
        final String recurrence = calendarEntry.getRecurrenceRule();
        if (recurrence != null && !recurrence.equals("")) {
            try {
                final Recur recur = new Recur(recurrence);
                final RRule rrule = new RRule(recur);
                vEventProperties.add(rrule);
            } catch (final ParseException e) {
                log.error("cannot create recurrence rule: " + recurrence.toString(), e);
            }
        }
        // recurrence exclusions
        final String recurrenceExc = calendarEntry.getRecurrenceExc();
        if (recurrenceExc != null && !recurrenceExc.equals("")) {
            final ExDate exdate = new ExDate();
            try {
                exdate.setValue(recurrenceExc);
                vEventProperties.add(exdate);
            } catch (final ParseException e) {
                log.warn("Could not set recurrence exclusions recurrenceExc=" + recurrenceExc, e);
            }
        }

        return vEvent;
    }

    /**
     * Build a KalendarEvent out of a source VEvent.
     * 
     * @param vEvent
     * @return
     */
    private CalendarEntry getCalendarEntry(final VEvent vEvent) {
        // subject
        final String subject = vEvent.getSummary().getValue();
        // start
        final Date start = vEvent.getStartDate().getDate();
        final Duration dur = vEvent.getDuration();
        // end
        Date end = null;
        if (dur != null) {
            end = dur.getDuration().getTime(vEvent.getStartDate().getDate());
        } else {
            end = vEvent.getEndDate().getDate();
        }

        // check all day event first
        boolean isAllDay = false;
        final Parameter dateParameter = vEvent.getProperties().getProperty(Property.DTSTART).getParameters().getParameter(Value.DATE.getName());
        if (dateParameter != null) {
            isAllDay = true;
        }

        if (isAllDay) {
            // adjust end date: ICal sets end dates to the next day
            end = new Date(end.getTime() - (1000 * 60 * 60 * 24));
        }

        final CalendarEntry calEntry = new CalendarEntry(vEvent.getUid().getValue(), subject, start, end);
        calEntry.setAllDayEvent(isAllDay);

        // classification
        final Clazz classification = vEvent.getClassification();
        if (classification != null) {
            final String sClass = classification.getValue();
            int iClassification = CalendarEntry.CLASS_PRIVATE;
            if (sClass.equals(ICAL_CLASS_PRIVATE.getValue())) {
                iClassification = CalendarEntry.CLASS_PRIVATE;
            } else if (sClass.equals(ICAL_CLASS_X_FREEBUSY.getValue())) {
                iClassification = CalendarEntry.CLASS_X_FREEBUSY;
            } else if (sClass.equals(ICAL_CLASS_PUBLIC.getValue())) {
                iClassification = CalendarEntry.CLASS_PUBLIC;
            }
            calEntry.setClassification(iClassification);
        }
        // created/last modified
        final Created created = vEvent.getCreated();
        if (created != null) {
            calEntry.setCreated(created.getDate().getTime());
        }
        // created/last modified
        final Contact contact = (Contact) vEvent.getProperty(Property.CONTACT);
        if (contact != null) {
            calEntry.setCreatedBy(contact.getValue());
        }

        final LastModified lastModified = vEvent.getLastModified();
        if (lastModified != null) {
            calEntry.setLastModified(lastModified.getDate().getTime());
        }

        // location
        final Location location = vEvent.getLocation();
        if (location != null) {
            calEntry.setLocation(location.getValue());
        }

        // links if any
        final List linkProperties = vEvent.getProperties(ICAL_X_OLAT_LINK);
        final List calendarEntryLinks = new ArrayList();
        for (final Iterator iter = linkProperties.iterator(); iter.hasNext();) {
            final XProperty linkProperty = (XProperty) iter.next();
            if (linkProperty != null) {
                final String encodedLink = linkProperty.getValue();
                final StringTokenizer st = new StringTokenizer(encodedLink, "§", false);
                if (st.countTokens() == 4) {
                    final String provider = st.nextToken();
                    final String id = st.nextToken();
                    final String displayName = st.nextToken();
                    final String uri = st.nextToken();
                    String iconCss = "";
                    // migration: iconCss has been added later, check if available first
                    if (st.hasMoreElements()) {
                        iconCss = st.nextToken();
                    }
                    final CalendarEntryLink entryLink = new CalendarEntryLink(provider, id, displayName, uri, iconCss);
                    calendarEntryLinks.add(entryLink);
                }
            }
        }
        calEntry.setCalendarEntryLinks(calendarEntryLinks);

        final Property comment = vEvent.getProperty(ICAL_X_OLAT_COMMENT);
        if (comment != null) {
            calEntry.setComment(comment.getValue());
        }

        final Property numParticipants = vEvent.getProperty(ICAL_X_OLAT_NUMPARTICIPANTS);
        if (numParticipants != null) {
            calEntry.setNumParticipants(Integer.parseInt(numParticipants.getValue()));
        }

        final Property participants = vEvent.getProperty(ICAL_X_OLAT_PARTICIPANTS);
        if (participants != null) {
            final StringTokenizer strTok = new StringTokenizer(participants.getValue(), "§", false);
            final String[] parts = new String[strTok.countTokens()];
            for (int i = 0; strTok.hasMoreTokens(); i++) {
                parts[i] = strTok.nextToken();
            }
            calEntry.setParticipants(parts);
        }

        final Property sourceNodId = vEvent.getProperty(ICAL_X_OLAT_SOURCENODEID);
        if (sourceNodId != null) {
            calEntry.setSourceNodeId(sourceNodId.getValue());
        }

        // recurrence
        if (vEvent.getProperty(ICAL_RRULE) != null) {
            calEntry.setRecurrenceRule(vEvent.getProperty(ICAL_RRULE).getValue());
        }

        // recurrence exclusions
        if (vEvent.getProperty(ICAL_EXDATE) != null) {
            calEntry.setRecurrenceExc(vEvent.getProperty(ICAL_EXDATE).getValue());
        }

        return calEntry;
    }

    @Override
    public File getCalendarFile(final String type, final String calendarID) {
        return new File(fStorageBase, "/" + type + "/" + calendarID + ".ics");
    }

    private void createCalendarFileDirectories() {
        File fDirectory = new File(fStorageBase, "/" + TYPE_USER);
        fDirectory.mkdirs();
        fDirectory = new File(fStorageBase, "/" + TYPE_GROUP);
        fDirectory.mkdirs();
        fDirectory = new File(fStorageBase, "/" + TYPE_COURSE);
        fDirectory.mkdirs();
    }

    /**
	 */
    @Override
    public boolean addEventTo(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        final OLATResourceable calOres = getOresHelperFor(cal);
        final Boolean persistSuccessful = coordinatorManager.getCoordinator().getSyncer().doInSync(calOres, new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                final OlatCalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
                loadedCal.addEvent(calendarEntry);
                final boolean successfullyPersist = persistCalendar(loadedCal);
                return new Boolean(successfullyPersist);
            }
        });
        return persistSuccessful.booleanValue();
    }

    /**
	 */
    @Override
    public boolean removeEventFrom(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        final OLATResourceable calOres = getOresHelperFor(cal);
        final Boolean removeSuccessful = coordinatorManager.getCoordinator().getSyncer().doInSync(calOres, new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                final OlatCalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
                loadedCal.removeEvent(calendarEntry);
                final boolean successfullyPersist = persistCalendar(loadedCal);
                return new Boolean(successfullyPersist);
            }
        });
        return removeSuccessful.booleanValue();
    }

    /**
	 */
    @Override
    public boolean updateEventFrom(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        final OLATResourceable calOres = getOresHelperFor(cal);
        final Boolean updatedSuccessful = coordinatorManager.getCoordinator().getSyncer().doInSync(calOres, new SyncerCallback<Boolean>() {
            @Override
            public Boolean execute() {
                return updateEventAlreadyInSync(cal, calendarEntry);
            }
        });
        return updatedSuccessful.booleanValue();
    }

    /**
	 */
    @Override
    public boolean updateEventAlreadyInSync(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        final OLATResourceable calOres = getOresHelperFor(cal);
        coordinatorManager.getCoordinator().getSyncer().assertAlreadyDoInSyncFor(calOres);
        final OlatCalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
        loadedCal.removeEvent(calendarEntry); // remove old event
        loadedCal.addEvent(calendarEntry); // add changed event
        final boolean successfullyPersist = persistCalendar(loadedCal);
        return successfullyPersist;
    }

    /**
     * Load a calendar when a calendar exists or create a new one. This method is not thread-safe. Must be called from synchronized block!
     * 
     * @param callType
     * @param callCalendarID
     * @return
     */
    protected OlatCalendar loadOrCreateCalendar(final String callType, final String callCalendarID) {
        if (!calendarExists(callType, callCalendarID)) {
            return createCalendar(callType, callCalendarID);
        } else {
            return loadCalendarFromFile(callType, callCalendarID);
        }
    }

    /**
	 */
    @Override
    @PostConstruct
    public void init() {
        calendarCache = coordinatorManager.getCoordinator().getCacher().getOrCreateCache(CalendarDao.class, "calendar");
    }

}
