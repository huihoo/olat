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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.calendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<BR>
 * Constants and helper methods for the OLAT iCal feeds
 * <P>
 * Initial Date: July 22, 2008
 * 
 * @author Udit Sajjanhar
 */
@Service("ImportCalendarManager")
public class ImportCalendarManager extends BasicManager {
    private static final Logger log = LoggerHelper.getLogger();

    public String PROP_CATEGORY = "Imported-Calendar";

    @Autowired
    CalendarService calendarService;

    /**
     * Save the imported calendar 1. make an entry in the database 2. save the calendar file
     * 
     * @param calendarName
     * @param ureq
     * @param importUrl
     *            can be null when import from file
     * @return
     */
    public void persistCalendar(final String calendarName, final UserRequest ureq, final String importUrl) {
        // move the temporary file to the permanent file
        final String tempCalendarID = getTempCalendarIDForUpload(ureq.getIdentity().getName());
        final String importedCalendarID = getImportedCalendarID(ureq, calendarName);
        final String importedCalendarType = getImportedCalendarType();

        final File oldCalendarFile = calendarService.getCalendarFile(importedCalendarType, tempCalendarID);
        oldCalendarFile.renameTo(calendarService.getCalendarFile(importedCalendarType, importedCalendarID));

        // make the entry in the database
        final long timestamp = System.currentTimeMillis();
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.createUserPropertyInstance(ureq.getIdentity(), PROP_CATEGORY, sanitize(calendarName), null, timestamp, importUrl, null);
        pm.saveProperty(p);
    }

    /**
     * Delete an imported calendar 1. remove the entry from the database 2. delete the calendar file
     * 
     * @param calendarID
     * @param ureq
     * @return
     */
    public void deleteCalendar(final String calendarID, final UserRequest ureq) {
        final String calendarName = getImportedCalendarNameFromID(ureq.getIdentity(), calendarID);
        // remove the entry from the database
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findUserProperty(ureq.getIdentity(), PROP_CATEGORY, calendarName);
        pm.deleteProperty(p);

        // delete the calendar file
        final String importedCalendarID = getImportedCalendarID(ureq, calendarName);
        final String importedCalendarType = getImportedCalendarType();
        calendarService.deleteCalendar(importedCalendarType, importedCalendarID);
    }

    /**
     * Get imported calendars for a user.
     * 
     * @param ureq
     * @return
     */
    public List getImportedCalendarsForIdentity(final UserRequest ureq) {
        // initialize the calendars list
        final List calendars = new ArrayList();
        final CalendarService calendarService = CoreSpringFactory.getBean(CalendarService.class);

        // read all the entries from the database
        final PropertyManager pm = PropertyManager.getInstance();
        final List properties = pm.listProperties(ureq.getIdentity(), null, null, PROP_CATEGORY, null);

        // return the list of calendar objects
        final Iterator propertyIter = properties.iterator();
        while (propertyIter.hasNext()) {
            final PropertyImpl calendarProperty = (PropertyImpl) propertyIter.next();
            final String calendarName = calendarProperty.getName();
            final OlatCalendar calendar = calendarService.getImportedCalendar(ureq.getIdentity(), calendarName);
            CalendarRenderWrapper calendarWrapper = CalendarRenderWrapper.wrapImportedCalendar(calendar, calendarName);
            calendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_ONLY);
            calendarWrapper.setImported(true);
            final CalendarConfig importedCalendarConfig = calendarService.findCalendarConfigForIdentity(calendarWrapper.getCalendar(), ureq.getUserSession()
                    .getGuiPreferences());
            if (importedCalendarConfig != null) {
                calendarWrapper.getCalendarConfig().setCss(importedCalendarConfig.getCss());
                calendarWrapper.getCalendarConfig().setVis(importedCalendarConfig.isVis());
            }
            calendars.add(calendarWrapper);
        }
        Collections.sort(calendars, CalendarComparator.getInstance());
        return calendars;
    }

    /**
     * Reload imported calendars from URL and persist calendars.
     * 
     * @param ureq
     */
    public void reloadUrlImportedCalendars(final UserRequest ureq) {
        // read all the entries from the database
        final List properties = PropertyManager.getInstance().listProperties(ureq.getIdentity(), null, null, PROP_CATEGORY, null);
        // return the list of calendar objects
        final Iterator propertyIter = properties.iterator();
        while (propertyIter.hasNext()) {
            final PropertyImpl calendarProperty = (PropertyImpl) propertyIter.next();
            final String calendarName = calendarProperty.getName();
            final String calendarUrl = calendarProperty.getStringValue();
            final long timestampLastupdate = calendarProperty.getLongValue();
            final long timestamp = System.currentTimeMillis();
            // reload only if string-property with importUrl exist and last update is older than 1 hour
            if ((calendarProperty.getStringValue() != null) && (timestamp - timestampLastupdate > 3600000)) {
                reloadCalendarFromUrl(calendarUrl, getImportedCalendarType(), getImportedCalendarID(ureq, calendarName));
                log.info("Calendar reload started from url=" + calendarUrl);
                calendarProperty.setLongValue(timestamp);
                PropertyManager.getInstance().updateProperty(calendarProperty);
                log.info("Calendar reloaded from url=" + calendarUrl);
            }
        }
    }

    /**
     * Reload calendar from url and store calendar file locally.
     * 
     * @param importUrl
     * @param calType
     * @param calId
     */
    private void reloadCalendarFromUrl(final String importUrl, final String calType, final String calId) {
        try {
            final String calendarContent = getContentFromUrl(importUrl);
            final OlatCalendar olatCalendar = calendarService.buildCalendarFrom(calendarContent, calType, calId);
            calendarService.persistCalendar(olatCalendar);
        } catch (final IOException e) {
            log.error("Could not reload calendar from url=" + importUrl, e);
        }

    }

    /**
     * Get a temporary calendarID for upload
     * 
     * @param ureq
     * @return
     */
    public String getTempCalendarIDForUpload(final String identityName) {
        return identityName + "_import_tmp";
    }

    /**
     * Get ID of a imported calendar
     * 
     * @param ureq
     * @param calendarName
     * @return
     */
    public String getImportedCalendarID(final UserRequest ureq, final String calendarName) {
        return getImportedCalendarID(ureq.getIdentity(), calendarName);
    }

    /**
     * Get ID of a imported calendar
     * 
     * @param identity
     * @param calendarName
     * @return
     */
    public String getImportedCalendarID(final Identity identity, final String calendarName) {
        return identity.getName() + "_" + sanitize(calendarName);
    }

    private String getImportedCalendarNameFromID(final Identity identity, final String calendarID) {
        final int idLength = calendarID.length();
        return calendarID.substring(identity.getName().length() + 1, idLength);
    }

    private String getImportedCalendarType() {
        return CalendarDao.TYPE_USER;
    }

    private String sanitize(String name) {
        // delete the preceding and trailing whitespaces
        name = name.trim();

        // replace every other character other than alphabets and numbers by underscore
        final Pattern specialChars = Pattern.compile("([^a-zA-z0-9])");
        return specialChars.matcher(name).replaceAll("_").toLowerCase();
    }

    public String getContentFromUrl(final String url) throws IOException {
        final InputStream in = (new URL(url)).openStream();
        final BufferedReader dis = new BufferedReader(new InputStreamReader(in));
        final StringBuffer fBuf = new StringBuffer();
        String line;
        while ((line = dis.readLine()) != null) {
            fBuf.append(line + "\n");
        }
        in.close();
        return fBuf.toString();
    }

}
