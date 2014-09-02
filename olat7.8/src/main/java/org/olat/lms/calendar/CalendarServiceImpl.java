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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.fortuna.ical4j.model.Calendar;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.ICourse;
import org.olat.lms.preferences.Preferences;
import org.olat.lms.user.UserDataDeletable;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalendarServiceImpl implements CalendarService, UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    CalendarDao calendarDao;
    @Autowired
    UserDeletionManager userDeletionManager;
    @Autowired
    CoordinatorManager coordinatorManager;
    @Autowired
    ImportCalendarManager importCalendarManager;

    /**
     * [spring]
     */
    private CalendarServiceImpl() {
    }

    /**
     * [testing]
     */
    CalendarServiceImpl(CalendarDao calendarDao, CoordinatorManager coordinatorManager) {
        this.calendarDao = calendarDao;
        this.coordinatorManager = coordinatorManager;
    }

    /**
	 */
    @Override
    public CalendarConfig findCalendarConfigForIdentity(final OlatCalendar calendar, final Preferences userGuiPreferences) {
        return (CalendarConfig) userGuiPreferences.get(CalendarConfig.class, calendar.getCalendarID());
    }

    /**
     * org.olat.presentation.framework.UserRequest)
     */
    @Override
    public void saveCalendarConfigForIdentity(final CalendarConfig config, final OlatCalendar calendar, final Preferences userGuiPreferences) {
        userGuiPreferences.putAndSave(CalendarConfig.class, calendar.getCalendarID(), config);
    }

    /**
     * @see org.olat.lms.calendar.CalendarService#addEntryTo(org.olat.data.calendar.OlatCalendar, org.olat.data.calendar.CalendarEntry)
     */
    @Override
    public boolean addEntryTo(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        Boolean persistSuccessful = calendarDao.addEventTo(cal, calendarEntry);
        informAboutCalendarChange(cal);
        return persistSuccessful.booleanValue();
    }

    public boolean updateEntryFrom(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        final boolean successfullyPersist = calendarDao.updateEventFrom(cal, calendarEntry);
        informAboutCalendarChange(cal);
        return successfullyPersist;
    }

    public boolean removeEntryFrom(final OlatCalendar cal, final CalendarEntry calendarEntry) {
        boolean removeSuccessful = calendarDao.removeEventFrom(cal, calendarEntry);
        informAboutCalendarChange(cal);
        return removeSuccessful;
    }

    public void informAboutCalendarChange(final OlatCalendar cal) {
        // TODO: 1.6.2011/cg Service-Refcatoring to-do :
        // OresHelper.lookupType(CalendarManager.class) should be in method like getOlatResourcableForEventChannel,
        // all 'OresHelper.lookupType(CalendarManager.class)' must be replaced (Controller)
        coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new CalendarModifiedEvent(cal), OresHelper.lookupType(CalendarDao.class));
    }

    @Override
    public OlatCalendar getPersonalCalendar(final Identity identity) {
        return calendarDao.getCalendar(CalendarDao.TYPE_USER, identity.getName());
    }

    @Override
    public OlatCalendar getImportedCalendar(final Identity identity, final String calendarName) {
        return calendarDao.getCalendar(CalendarDao.TYPE_USER, importCalendarManager.getImportedCalendarID(identity, calendarName));
    }

    @Override
    public OlatCalendar getGroupCalendar(final BusinessGroup businessGroup) {
        return calendarDao.getCalendar(CalendarDao.TYPE_GROUP, getCalendarIdFor(businessGroup));
    }

    @Override
    public OlatCalendar getCourseCalendar(final ICourse course) {
        return calendarDao.getCalendar(CalendarDao.TYPE_COURSE, getCalendarIdFor(course));
    }

    @Override
    public void deletePersonalCalendar(final Identity identity) {
        calendarDao.deleteCalendar(CalendarDao.TYPE_USER, identity.getName());
    }

    @Override
    public void deleteGroupCalendar(final BusinessGroup businessGroup) {
        calendarDao.deleteCalendar(CalendarDao.TYPE_GROUP, getCalendarIdFor(businessGroup));
    }

    @Override
    public void deleteCourseCalendar(final ICourse course) {
        calendarDao.deleteCalendar(CalendarDao.TYPE_COURSE, getCalendarIdFor(course));
    }

    // ///////////////////////////////////////
    // implements interface UserDataDeletable
    // ///////////////////////////////////////
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        deletePersonalCalendar(identity);
        log.debug("Personal calendar deleted for identity=" + identity);
    }

    private String getCalendarIdFor(OLATResourceable olatResourceable) {
        return olatResourceable.getResourceableId().toString();
    }

    @Override
    public boolean calendarExists(String calendarType, String calendarID) {
        return calendarDao.calendarExists(calendarType, calendarID);
    }

    @Override
    public Calendar readCalendar(String type, String calendarID) {
        return calendarDao.readCalendar(type, calendarID);
    }

    @Override
    public File getCalendarFile(String type, String calendarID) {
        return calendarDao.getCalendarFile(type, calendarID);
    }

    @Override
    public OlatCalendar getCalendar(String type, String calendarID) {
        return calendarDao.getCalendar(type, calendarID);
    }

    @Override
    public boolean deleteCalendar(String calendarType, String calendarID) {
        return calendarDao.deleteCalendar(calendarType, calendarID);
    }

    @Override
    public OlatCalendar buildCalendarFrom(String calendarContent, String calType, String calId) {
        return calendarDao.buildKalendarFrom(calendarContent, calType, calId);
    }

    @Override
    public boolean persistCalendar(OlatCalendar calendar) {
        return calendarDao.persistCalendar(calendar);
    }

    @Override
    public void writeCalendarToFile(String calendarId, String calendarContent) throws IOException {
        final File tmpFile = getCalendarFile(CalendarDao.TYPE_USER, calendarId);
        final BufferedWriter output = new BufferedWriter(new FileWriter(tmpFile));
        output.write(calendarContent);
        output.close();

    }

}
