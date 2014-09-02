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

package org.olat.data.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.exception.DBRuntimeException;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.OlatTestCase;
import org.olat.testutils.codepoints.client.BreakpointStateException;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A <b>OLATResourceManagerITCase </b> is used for SecurityResourceManager testing.
 * 
 * @author Andreas Ch. Kapp
 */

public class OLATResourceManagerITCase extends OlatTestCase implements OLATResourceable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CODEPOINT_SERVER_ID = "OLATResourceManagerITCase";

    @Autowired
    OLATResourceManager resourceManager;
    @Autowired
    DB db;

    /**
     * Test creation/insert/update and deletion of a resource
     */
    @Test
    public void testCreateInsertUpdateDeleteResource() {
        // create
        final OLATResource res = resourceManager.createOLATResourceInstance(this);
        resourceManager.saveOLATResource(res);
        assertNotNull(res);
        assertTrue(res.getResourceableId().equals(this.getResourceableId()));
        // Insert
        resourceManager.saveOLATResource(res);
        resourceManager.deleteOLATResource(res);
    }

    /**
     * Test find/persist of a resource
     */
    @Test
    public void testFindOrPersistResourceable() {
        // create by finding
        OLATResource ores = resourceManager.findOrPersistResourceable(this);
        assertNotNull(ores);
        // only find
        ores = resourceManager.findOrPersistResourceable(this);
        assertNotNull(ores);
    }

    /**
     * Test type-only resource
     */
    @Test
    public void testInsertTypeOnly() {
        final OLATResourceable oresable = new OLATResourceable() {

            @Override
            public String getResourceableTypeName() {
                return "typeonly";
            }

            @Override
            public Long getResourceableId() {
                return null;
            }
        };
        OLATResource ores = resourceManager.findOrPersistResourceable(oresable);
        assertNotNull(ores);
        assertNull(ores.getResourceableId());
        // only find
        ores = resourceManager.findOrPersistResourceable(this);
        assertNotNull(ores);
    }

    /**
     * Test deletion of a resource
     */
    @Test
    public void testDeleteResourceable() {
        // delete on not persisted resourceable
        resourceManager.deleteOLATResourceable(this);
        // delete persisted resourceable
        final OLATResource ores = resourceManager.findOrPersistResourceable(this);
        assertNotNull(ores);
        resourceManager.deleteOLATResourceable(this);
    }

    /**
     * Test find/persist of a resource
     */
    @Test
    public void testConcurrentFindOrPersistResourceable() {
        OLATResource ores = resourceManager.findOrPersistResourceable(this);
        assertNotNull(ores);
        resourceManager.deleteOLATResource(ores);
        db.closeSession();
        // now we are sure OLATResource is deleted

        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<OLATResource> statusList = Collections.synchronizedList(new ArrayList<OLATResource>(1));

        // enable breakpoint

        CodepointClient codepointClient = null;
        CodepointRef codepointRef = null;
        try {
            codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
            codepointRef = codepointClient
                    .getCodepoint("org.olat.data.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.data.resource.OLATResourceManager.findOrPersistResourceable");
            codepointRef.enableBreakpoint();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Could not initialzed CodepointClient");
        }

        // thread 1
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final OLATResource ores = resourceManager.findOrPersistResourceable(new TestResourceable());
                    assertNotNull(ores);
                    statusList.add(ores);
                    System.out.println("testConcurrentFindOrPersistResourceable thread1 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    final OLATResource ores = resourceManager.findOrPersistResourceable(new TestResourceable());
                    assertNotNull(ores);
                    statusList.add(ores);
                    System.out.println("testConcurrentFindOrPersistResourceable thread2 finished");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        sleep(2000);
        // check thread 2 should not finished
        assertEquals("Thread already finished => synchronization did not work", 0, statusList.size());
        try {
            // to see all registered code-points: comment-in next 2 lines
            // List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
            // System.out.println("codepointList=" + codepointList);
            System.out.println("testConcurrentFindOrPersistResourceable start waiting for breakpoint reached");
            final TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
            assertTrue("Did not reach breakpoint", threads.length > 0);
            System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
            codepointRef.disableBreakpoint(true);
            System.out.println("testConcurrentFindOrPersistResourceable breakpoint reached => continue");
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
        assertEquals("Missing created OresResource in statusList", 2, statusList.size());
        assertEquals("Created OresResource has not same key", statusList.get(0).getKey(), statusList.get(1).getKey());
        codepointClient.close();
        System.out.println("testConcurrentFindOrPersistResourceable finish successful");

    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        try {
            // Setup for code-points
            JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
        } catch (final Exception e) {
            log.error("Error while generating database tables or opening hibernate session", e);
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        JMSCodePointServerJunitHelper.stopServer();
        db.closeSession();
    }

    @Override
    public Long getResourceableId() {
        return new Long(1234567890L);
    }

    @Override
    public String getResourceableTypeName() {
        return this.getClass().getName();
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

    /**
     * Test resource for null values
     */
    @Test
    public void testNULLVALUE() {
        final NullTester ntester = new NullTester();
        // Uncomment for testing:
        OLATResource or = null;
        try {
            or = resourceManager.createOLATResourceInstance(ntester);
        } catch (final RuntimeException re) {
            assertNull(or);
        }
        try {
            db.closeSession();
        } catch (final DBRuntimeException e) {
            // ignore
        }
    }

    /**
     * Resource with null value
     */
    class NullTester implements OLATResourceable {

        @Override
        public Long getResourceableId() {
            return new Long(0);
        }

        @Override
        public String getResourceableTypeName() {
            return this.getClass().getName();
        }
    }

    // /////////////////////////////
    // Inner class TestResourceable
    // /////////////////////////////
    class TestResourceable implements OLATResourceable {

        @Override
        public Long getResourceableId() {
            return new Long(123123999);
        }

        @Override
        public String getResourceableTypeName() {
            // TODO Auto-generated method stub
            return "TestResourceable";
        }

    }

}
