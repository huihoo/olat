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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.ICourse;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.Coordinator;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.security.OLATPrincipal;

/**
 * Tests for Calendar-service.
 * 
 * @author Christian Guretzki
 */
public class CalendarServiceImplTest {

    private CalendarServiceImpl calendarServiceImpl;
    private CalendarDao calendarManagerMock;
    private CoordinatorManager coordinatorManagerMock;
    // common test objects
    private Identity testIdentity;
    private String testIdentityName = "cal_test_name";
    private EventBusMock eventBusMock;
    private OlatCalendar cal;
    private CalendarEntry calendarEntry;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        calendarManagerMock = mock(CalendarDao.class);
        coordinatorManagerMock = mock(CoordinatorManager.class);

        Coordinator coordinatorMock = mock(Coordinator.class);
        eventBusMock = new EventBusMock();
        when(coordinatorMock.getEventBus()).thenReturn(eventBusMock);
        when(coordinatorManagerMock.getCoordinator()).thenReturn(coordinatorMock);
        calendarServiceImpl = new CalendarServiceImpl(calendarManagerMock, coordinatorManagerMock);
        calendarServiceImpl.importCalendarManager = mock(ImportCalendarManager.class);

        testIdentity = mock(Identity.class);
        when(testIdentity.getName()).thenReturn(testIdentityName);

        cal = mock(OlatCalendar.class);
        calendarEntry = mock(CalendarEntry.class);

    }

    // Do not test 'findKalendarConfigForIdentity' and 'saveKalendarConfigForIdentity' because
    // this methods maps only calls to GuiPreferences (like DAO delegate calls).

    /**
     * Test method 'getPersonalCalendar'. Check mapping to course-type 'TYPE_USER'. Input : empty list Output: empty list
     */
    @Test
    public void testGetPersonalCalendar() {
        OlatCalendar olatCalendar = calendarServiceImpl.getPersonalCalendar(testIdentity);
        verify(calendarManagerMock).getCalendar(CalendarDao.TYPE_USER, testIdentity.getName());
    }

    /**
     * Test method 'getImportedCalendar'. Check mapping to course-type 'TYPE_USER'. Input : empty list Output: empty list
     */
    @Test
    public void testGetImportedCalendar() {
        String importedCalendarName = "importedCalendarName";
        when(calendarServiceImpl.importCalendarManager.getImportedCalendarID(testIdentity, importedCalendarName)).thenReturn("testID");
        OlatCalendar olatCalendar = calendarServiceImpl.getImportedCalendar(testIdentity, importedCalendarName);
        verify(calendarManagerMock).getCalendar(eq(CalendarDao.TYPE_USER), anyString());
    }

    /**
     * Test method 'getGroupCalendar'. Check mapping to course-type 'TYPE_GROUP'. Input : Output:
     */
    @Test
    public void testGetGroupCalendar() {
        BusinessGroup businessGroup = mock(BusinessGroup.class);
        OlatCalendar olatCalendar = calendarServiceImpl.getGroupCalendar(businessGroup);
        verify(calendarManagerMock).getCalendar(eq(CalendarDao.TYPE_GROUP), anyString());
    }

    /**
     * Test method 'getCourseCalendar'. Check mapping to course-type 'TYPE_COURSE'. Input : Output:
     */
    @Test
    public void testGetCourseCalendar() {
        ICourse course = mock(ICourse.class);
        OlatCalendar olatCalendar = calendarServiceImpl.getCourseCalendar(course);
        verify(calendarManagerMock).getCalendar(eq(CalendarDao.TYPE_COURSE), anyString());
    }

    /**
     * Test method 'addEventTo'. Check that add-call was done and a modified-event was triggered. Input : Output:
     */
    @Test
    public void testAddEventTo() {
        boolean result = calendarServiceImpl.addEntryTo(cal, calendarEntry);
        verify(calendarManagerMock).addEventTo(cal, calendarEntry);
        assertTrue("Event was not triggered, missing fireEventToListenersOf(...)", eventBusMock.firedEvent);
    }

    /**
     * Test method 'updateEventFrom'. Check that update-call was done and a modified-event was triggered. Input : Output:
     */
    @Test
    public void testUpdateEventTo() {
        boolean result = calendarServiceImpl.updateEntryFrom(cal, calendarEntry);
        verify(calendarManagerMock).updateEventFrom(cal, calendarEntry);
        assertTrue("Event was not triggered, missing fireEventToListenersOf(...)", eventBusMock.firedEvent);
    }

    /**
     * Test method 'removeEventFrom'. Check that remove-call was done and a modified-event was triggered. Input : Output:
     */
    @Test
    public void testRemoveEventFrom() {
        boolean result = calendarServiceImpl.removeEntryFrom(cal, calendarEntry);
        verify(calendarManagerMock).removeEventFrom(cal, calendarEntry);
        assertTrue("Modified-Event was not triggered, missing fireEventToListenersOf(...)", eventBusMock.firedEvent);
    }

}

class EventBusMock implements EventBus {

    protected MultiUserEvent event;
    protected OLATResourceable ores;
    protected boolean firedEvent;

    @Override
    public void registerFor(GenericEventListener gel, OLATPrincipal principal, OLATResourceable ores) {
    }

    @Override
    public void deregisterFor(GenericEventListener gel, OLATResourceable ores) {
    }

    @Override
    public void fireEventToListenersOf(MultiUserEvent event, OLATResourceable ores) {
        this.event = event;
        this.ores = ores;
        firedEvent = true;
    }

    @Override
    public Set getListeningIdentityNamesFor(OLATResourceable ores) {
        return null;
    }

    @Override
    public int getListeningIdentityCntFor(OLATResourceable ores) {
        return 0;
    }

    @Override
    public Map getUnmodifiableInfoCenter() {
        return null;
    }

}
