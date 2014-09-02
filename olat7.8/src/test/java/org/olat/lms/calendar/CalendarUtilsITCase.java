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

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class CalendarUtilsITCase extends OlatTestCase {

    private static final Runtime RUNTIME = Runtime.getRuntime();
    private OlatCalendar olatCalendar;
    private static final int numEvents = 10000;
    private static final int maxEventDuratio = 1000 * 60 * 60 * 24 * 14; // maximum of 14 days duration
    private static final int oneYearSec = 60 * 60 * 24 * 365;
    private static final int goBackNumYears = 1;
    private static long calendarStart = new Date().getTime() - (((long) goBackNumYears * oneYearSec) * 1000);
    private static Identity test;
    @Autowired
    private CalendarService calendarService;

    @Before
    public void setUp() throws Exception {
        test = JunitTestHelper.createAndPersistIdentityAsUser("test");
        olatCalendar = new OlatCalendar("test", CalendarDao.TYPE_USER);
    }

    @After
    public void tearDown() throws Exception {
        calendarService.deletePersonalCalendar(test);
    }

    @Test
    public void testListEventsForPeriod() {
        System.out.println("*** Starting test with the following configuration:");
        System.out.println("*** Number of events: " + numEvents);
        System.out.println("*** Maximum event duration (ms): " + maxEventDuratio);
        System.out.println("*** Generate events in between " + new Date(calendarStart) + " and "
                + new Date(calendarStart + (1000 * ((long) goBackNumYears * oneYearSec))));

        createTestEvents(numEvents, olatCalendar);
        System.out.println("*** Load calendar...");
        long start = System.currentTimeMillis();
        calendarService.getPersonalCalendar(test);
        long stop = System.currentTimeMillis();
        System.out.println("Duration load: " + (stop - start) + " ms.");

        System.out.println("*** Find events within period...");
        start = System.currentTimeMillis();
        final List events = CalendarUtils.listEventsForPeriod(olatCalendar, new Date(calendarStart), new Date(calendarStart
                + (1000 * ((long) (goBackNumYears * oneYearSec)))));
        stop = System.currentTimeMillis();
        System.out.println("Duration find: " + (stop - start) + " ms.");
        System.out.println("Found " + events.size() + " events out of " + olatCalendar.getAllCalendarEntries().size() + " total events.");
        assertEquals(olatCalendar.getAllCalendarEntries().size(), events.size());

        System.out.println("*** Save calendar...");
        start = System.currentTimeMillis();
        calendarService.persistCalendar(olatCalendar);
        stop = System.currentTimeMillis();
        System.out.println("Duration save: " + (stop - start) + " ms.");

    }

    /**
     * Creates a number of events in certain calendar.
     * 
     * @param numEvents
     * @param cal
     */
    private void createTestEvents(final int numberOfEvents, final OlatCalendar cal) {
        final Random rand = new Random();
        final long startUsed = RUNTIME.totalMemory() - RUNTIME.freeMemory();
        for (int i = 0; i < numberOfEvents; i++) {
            final long begin = calendarStart + (1000 * ((long) rand.nextInt(goBackNumYears * oneYearSec)));
            final CalendarEntry event = new CalendarEntry("id" + i, "test" + i, new Date(begin), rand.nextInt(maxEventDuratio));
            cal.addEvent(event);
        }
        final long stopUsed = RUNTIME.totalMemory() - RUNTIME.freeMemory();
        System.out.println("*** SETUP: Kalendar structure uses approx. " + (stopUsed - startUsed) / 1024 + " kb memory.");
        calendarService.persistCalendar(olatCalendar);
    }

}
