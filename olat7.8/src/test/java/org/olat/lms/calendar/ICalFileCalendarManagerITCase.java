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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.commons.database.DBFactory;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.testutils.codepoints.client.BreakpointStateException;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 */
public class ICalFileCalendarManagerITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private static String CODEPOINT_SERVER_ID = "ICalFileCalendarManagerITCase";
    private static Identity test = null;
    @Autowired
    private CalendarService calendarService;

    @Test
    public void testAddChangeRemoveEvent() {
        final String TEST_EVENT_ID = "id-testAddEvent";
        OlatCalendar cal = calendarService.getPersonalCalendar(test);
        // 1. Test Add Event
        final CalendarEntry testEvent = new CalendarEntry(TEST_EVENT_ID, "testEvent", new Date(), 1);
        calendarService.addEntryTo(cal, testEvent);
        // set manager null to force reload of calendar from file-system
        cal = calendarService.getPersonalCalendar(test);
        final CalendarEntry reloadedEvent = cal.getCalendarEntry(TEST_EVENT_ID);
        assertNotNull("Could not found added event", reloadedEvent);
        assertEquals("Added event has wrong subject", testEvent.getSubject(), reloadedEvent.getSubject());
        // 2. Test Change event
        reloadedEvent.setSubject("testEvent changed");
        calendarService.updateEntryFrom(cal, reloadedEvent);
        // set manager null to force reload of calendar from file-system
        cal = calendarService.getPersonalCalendar(test);
        final CalendarEntry updatedEvent = cal.getCalendarEntry(TEST_EVENT_ID);
        assertNotNull("Could not found updated event", updatedEvent);
        assertEquals("Added event has wrong subject", reloadedEvent.getSubject(), updatedEvent.getSubject());
        // 3. Test Remove event
        calendarService.removeEntryFrom(cal, updatedEvent);
        cal = calendarService.getPersonalCalendar(test);
        final CalendarEntry removedEvent = cal.getCalendarEntry(TEST_EVENT_ID);
        assertNull("Found removed event", removedEvent);
    }

    /**
     * Test concurrent add event with two threads and code-point to control concurrency.
     */
    @Test
    public void testConcurrentAddEvent() {
        final String TEST_EVENT_ID_1 = "id-testConcurrentAddEvent-1";
        final String TEST_EVENT_SUBJECT_1 = "testEvent1";
        final String TEST_EVENT_ID_2 = "id-testConcurrentAddEvent-2";
        final String TEST_EVENT_SUBJECT_2 = "testEvent2";

        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        // enable breakpoint

        CodepointClient codepointClient = null;
        CodepointRef codepointRef = null;
        try {
            codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
            codepointRef = codepointClient
                    .getCodepoint("org.olat.data.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.data.calendar.CalendarDaoICalFileImpl.addEventTo");
            codepointRef.enableBreakpoint();
            System.out.println();
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Could not initialzed CodepointClient");
        }

        // thread 1
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 1. load calendar
                    OlatCalendar cal = calendarService.getPersonalCalendar(test);

                    // 2. add Event1 => breakpoint hit
                    System.out.println("testConcurrentAddEvent thread1 addEvent1");
                    calendarService.addEntryTo(cal, new CalendarEntry(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, new Date(), 1));
                    System.out.println("testConcurrentAddEvent thread1 addEvent1 DONE");
                    // 3. check event1 exist
                    cal = calendarService.getPersonalCalendar(test);
                    CalendarEntry event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    // 4. sleep 2sec

                    // 5. check event1 still exist (event2 added in meantime)
                    cal = calendarService.getPersonalCalendar(test);
                    event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    statusList.add(Boolean.TRUE);
                    System.out.println("testConcurrentAddEvent thread1 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 1. load calendar
                    OlatCalendar cal = calendarService.getPersonalCalendar(test);
                    // 2. sleep 1sec
                    sleep(1000);
                    // 3. add Event2 (breakpoint of thread1 blocks)
                    System.out.println("testConcurrentAddEvent thread2 addEvent2");
                    calendarService.addEntryTo(cal, new CalendarEntry(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
                    System.out.println("testConcurrentAddEvent thread1 addEvent2 DONE");
                    // 4. check event2 exist
                    cal = calendarService.getPersonalCalendar(test);
                    final CalendarEntry event2 = cal.getCalendarEntry(TEST_EVENT_ID_2);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
                    assertEquals("Wrong calendar-event subject", event2.getSubject(), TEST_EVENT_SUBJECT_2);
                    // 5. check event1 exist
                    cal = calendarService.getPersonalCalendar(test);
                    final CalendarEntry event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    statusList.add(Boolean.TRUE);
                    System.out.println("testConcurrentAddEvent thread2 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        sleep(2000);
        try {
            // to see all registered code-points: comment-in next 2 lines
            // List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
            // System.out.println("codepointList=" + codepointList);
            System.out.println("testConcurrentAddEvent start waiting for breakpoint reached");
            final TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
            assertTrue("Did not reach breakpoint", threads.length > 0);
            System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
            codepointRef.disableBreakpoint(true);
            System.out.println("testConcurrentAddEvent breakpoint reached => continue");
        } catch (final BreakpointStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Codepoints: BreakpointStateException=" + e.getMessage());
        } catch (final CommunicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Codepoints: CommunicationException=" + e.getMessage());
        }

        // sleep until t1 and t2 should have terminated/excepted
        int loopCount = 0;
        while ((statusList.size() < 2) && (exceptionHolder.size() < 1) && (loopCount < 5)) {
            sleep(1000);
            loopCount++;
        }
        assertTrue("Threads did not finish in 5sec", loopCount < 5);
        // if not -> they are in deadlock and the db did not detect it
        for (final Exception exception : exceptionHolder) {
            System.out.println("exception: " + exception.getMessage());
            exception.printStackTrace();
        }
        if (exceptionHolder.size() > 0) {
            assertTrue("It throws an exception in test => see sysout exception[0]=" + exceptionHolder.get(0).getMessage(), exceptionHolder.size() == 0);
        }
        codepointClient.close();
        System.out.println("testConcurrentAddEvent finish successful");
    }

    /**
     * Test concurrent add/update event with two threads and code-point to control concurrency.
     */
    @Test
    public void testConcurrentAddUpdateEvent() {
        final String TEST_EVENT_ID_1 = "id-testConcurrentAddUpdateEvent-1";
        final String TEST_EVENT_SUBJECT_1 = "testEvent1";
        final String TEST_EVENT_ID_2 = "id-testConcurrentAddUpdateEvent-2";
        final String TEST_EVENT_SUBJECT_2 = "testEvent2";
        final String TEST_EVENT_SUBJECT_2_UPDATED = "testUpdatedEvent2";

        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        // Generate event for update
        OlatCalendar cal = calendarService.getPersonalCalendar(test);
        calendarService.addEntryTo(cal, new CalendarEntry(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
        cal = calendarService.getPersonalCalendar(test);
        final CalendarEntry event2 = cal.getCalendarEntry(TEST_EVENT_ID_2);
        assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
        assertEquals("Wrong calendar-event subject", event2.getSubject(), TEST_EVENT_SUBJECT_2);
        System.out.println("testConcurrentAddUpdateEvent thread2 addEvent2 DONE");

        // enable breakpoint
        CodepointClient codepointClient = null;
        CodepointRef codepointRef = null;
        try {
            codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
            codepointRef = codepointClient
                    .getCodepoint("org.olat.data.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.data.calendar.CalendarDaoICalFileImpl.addEventTo");
            codepointRef.enableBreakpoint();
            System.out.println();
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Could not initialzed CodepointClient");
        }

        // thread 1
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 1. load calendar
                    OlatCalendar cal = calendarService.getPersonalCalendar(test);

                    // 2. add Event1 => breakpoint hit
                    System.out.println("testConcurrentAddUpdateEvent thread1 addEvent1");
                    calendarService.addEntryTo(cal, new CalendarEntry(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, new Date(), 1));
                    System.out.println("testConcurrentAddUpdateEvent thread1 addEvent1 DONE");
                    // 3. check event1 exist
                    cal = calendarService.getPersonalCalendar(test);
                    CalendarEntry event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    // 4. sleep 2sec

                    // 5. check event1 still exist (event2 added in meantime)
                    cal = calendarService.getPersonalCalendar(test);
                    event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    statusList.add(Boolean.TRUE);
                    System.out.println("testConcurrentAddUpdateEvent thread1 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            public void run() {
                try {
                    OlatCalendar cal = calendarService.getPersonalCalendar(test);
                    // 2. sleep 1sec
                    sleep(1000);
                    // 3. add Event2 (breakpoint of thread1 blocks)
                    System.out.println("testConcurrentAddUpdateEvent thread2 updateEvent2");
                    calendarService.updateEntryFrom(cal, new CalendarEntry(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
                    System.out.println("testConcurrentAddUpdateEvent thread1 updateEvent2 DONE");
                    // 4. check event2 exist
                    cal = calendarService.getPersonalCalendar(test);
                    final CalendarEntry updatedEvent = cal.getCalendarEntry(TEST_EVENT_ID_2);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, updatedEvent);
                    assertEquals("Wrong calendar-event subject", updatedEvent.getSubject(), TEST_EVENT_SUBJECT_2_UPDATED);
                    // 5. check event1 exist
                    cal = calendarService.getPersonalCalendar(test);
                    final CalendarEntry event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    // Delete Event
                    final boolean removed = calendarService.removeEntryFrom(cal, new CalendarEntry(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
                    assertTrue(removed);
                    statusList.add(Boolean.TRUE);
                    System.out.println("testConcurrentAddUpdateEvent thread2 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        sleep(2000);
        try {
            // to see all registered code-points: comment-in next 2 lines
            // List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
            // System.out.println("codepointList=" + codepointList);
            System.out.println("testConcurrentAddUpdateEvent start waiting for breakpoint reached");
            final TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
            assertTrue("Did not reach breakpoint", threads.length > 0);
            System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
            codepointRef.disableBreakpoint(true);
            System.out.println("testConcurrentAddUpdateEvent breakpoint reached => continue");
        } catch (final BreakpointStateException e) {
            e.printStackTrace();
            fail("Codepoints: BreakpointStateException=" + e.getMessage());
        } catch (final CommunicationException e) {
            e.printStackTrace();
            fail("Codepoints: CommunicationException=" + e.getMessage());
        }

        // sleep until t1 and t2 should have terminated/excepted
        int loopCount = 0;
        while ((statusList.size() < 2) && (exceptionHolder.size() < 1) && (loopCount < 5)) {
            sleep(1000);
            loopCount++;
        }
        assertTrue("Threads did not finish in 5sec", loopCount < 5);
        // if not -> they are in deadlock and the db did not detect it
        for (final Exception exception : exceptionHolder) {
            System.out.println("exception: " + exception.getMessage());
            exception.printStackTrace();
        }
        if (exceptionHolder.size() > 0) {
            assertTrue("It throws an exception in test => see sysout exception[0]=" + exceptionHolder.get(0).getMessage(), exceptionHolder.size() == 0);
        }
        codepointClient.close();
        System.out.println("testConcurrentAddUpdateEvent finish successful");
    }

    /**
     * Test concurrent add/delete event with two threads and code-point to control concurrency.
     */
    @Test
    public void testConcurrentAddRemoveEvent() {
        final String TEST_EVENT_ID_1 = "id-testConcurrentAddRemoveEvent-1";
        final String TEST_EVENT_SUBJECT_1 = "testEvent1";
        final String TEST_EVENT_ID_2 = "id-testConcurrentAddRemoveEvent-2";
        final String TEST_EVENT_SUBJECT_2 = "testEvent2";

        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        // Generate event for update
        OlatCalendar cal = calendarService.getPersonalCalendar(test);
        calendarService.addEntryTo(cal, new CalendarEntry(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
        cal = calendarService.getPersonalCalendar(test);
        final CalendarEntry event2 = cal.getCalendarEntry(TEST_EVENT_ID_2);
        assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
        assertEquals("Wrong calendar-event subject", event2.getSubject(), TEST_EVENT_SUBJECT_2);
        System.out.println("testConcurrentAddRemoveEvent thread2 addEvent2 DONE");

        // enable breakpoint
        CodepointClient codepointClient = null;
        CodepointRef codepointRef = null;
        try {
            codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
            codepointRef = codepointClient
                    .getCodepoint("org.olat.data.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.data.calendar.CalendarDaoICalFileImpl.addEventTo");
            codepointRef.enableBreakpoint();
            System.out.println();
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Could not initialzed CoepointClient");
        }

        // thread 1
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 1. load calendar
                    OlatCalendar cal = calendarService.getPersonalCalendar(test);

                    // 2. add Event1 => breakpoint hit
                    System.out.println("testConcurrentAddRemoveEvent thread1 addEvent1");
                    calendarService.addEntryTo(cal, new CalendarEntry(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, new Date(), 1));
                    System.out.println("testConcurrentAddRemoveEvent thread1 addEvent1 DONE");
                    // 3. check event1 exist
                    cal = calendarService.getPersonalCalendar(test);
                    CalendarEntry event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    // 4. sleep 2sec

                    // 5. check event1 still exist (event2 added in meantime)
                    cal = calendarService.getPersonalCalendar(test);
                    event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    statusList.add(Boolean.TRUE);
                    System.out.println("testConcurrentAddRemoveEvent thread1 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            public void run() {
                try {
                    OlatCalendar cal = calendarService.getPersonalCalendar(test);
                    // 2. sleep 1sec
                    sleep(1000);
                    // 3. add Event2 (breakpoint of thread1 blocks)
                    System.out.println("testConcurrentAddRemoveEvent thread2 removeEvent2");
                    final boolean removed = calendarService.removeEntryFrom(cal, new CalendarEntry(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
                    assertTrue(removed);
                    System.out.println("testConcurrentAddRemoveEvent thread1 removeEvent2 DONE");
                    // 4. check event2 exist
                    cal = calendarService.getPersonalCalendar(test);
                    final CalendarEntry updatedEvent = cal.getCalendarEntry(TEST_EVENT_ID_2);
                    assertNull("Still found deleted event with id=" + TEST_EVENT_ID_2, updatedEvent);
                    // 5. check event1 exist
                    cal = calendarService.getPersonalCalendar(test);
                    final CalendarEntry event1 = cal.getCalendarEntry(TEST_EVENT_ID_1);
                    assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
                    assertEquals("Wrong calendar-event subject", event1.getSubject(), TEST_EVENT_SUBJECT_1);
                    statusList.add(Boolean.TRUE);
                    System.out.println("testConcurrentAddRemoveEvent thread2 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        sleep(2000);
        try {
            // to see all registered code-points: comment-in next 2 lines
            // List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
            // System.out.println("codepointList=" + codepointList);
            System.out.println("testConcurrentAddRemoveEvent start waiting for breakpoint reached");
            final TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
            assertTrue("Did not reach breakpoint", threads.length > 0);
            System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
            codepointRef.disableBreakpoint(true);
            System.out.println("testConcurrentAddRemoveEvent breakpoint reached => continue");
        } catch (final BreakpointStateException e) {
            e.printStackTrace();
            fail("Codepoints: BreakpointStateException=" + e.getMessage());
        } catch (final CommunicationException e) {
            e.printStackTrace();
            fail("Codepoints: CommunicationException=" + e.getMessage());
        }

        // sleep until t1 and t2 should have terminated/excepted
        int loopCount = 0;
        while ((statusList.size() < 2) && (exceptionHolder.size() < 1) && (loopCount < 5)) {
            sleep(1000);
            loopCount++;
        }
        assertTrue("Threads did not finish in 5sec", loopCount < 5);
        // if not -> they are in deadlock and the db did not detect it
        for (final Exception exception : exceptionHolder) {
            System.out.println("exception: " + exception.getMessage());
            exception.printStackTrace();
        }
        if (exceptionHolder.size() > 0) {
            assertTrue("It throws an exception in test => see sysout exception[0]=" + exceptionHolder.get(0).getMessage(), exceptionHolder.size() == 0);
        }
        codepointClient.close();
        System.out.println("testConcurrentAddRemoveEvent finish successful");
    }

    /**
     * @param milis
     *            the duration in miliseconds to sleep
     */
    private void sleep(final int milis) {
        try {
            Thread.sleep(milis);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        test = JunitTestHelper.createAndPersistIdentityAsUser("test");
        // Setup for code-points
        JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
        DBFactory.getInstance().closeSession();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        try {
            JMSCodePointServerJunitHelper.stopServer();
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

}
