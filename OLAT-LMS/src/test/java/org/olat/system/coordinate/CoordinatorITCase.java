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

package org.olat.system.coordinate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;

/**
 * 
 */
public class CoordinatorITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Test with 2 threads T1 & T2. T1 T2 doInSync T1-1 sleep 5sec sleep 10sec ... ... ... ... doInSync T2-1 ... sleep 10sec ... ... doInSync T1-2 ... finished ...
     * doInSync T2-2 finished
     */
    @Test
    public void testDoInSyncWithSyncerExecutor() {

        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testDoInSync", new Long("123"));
        // thread 1
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
                        @Override
                        public void execute() {
                            System.out.println("Thread-1: execute doInSync 1");
                        }
                    });// end syncerCallback

                    // sleep
                    sleep(10000);

                    // do again do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
                        @Override
                        public void execute() {
                            System.out.println("Thread-1: execute doInSync 2");
                        }
                    });// end syncerCallback
                    System.out.println("Thread-1: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // sleep
                    sleep(5000);

                    // do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
                        @Override
                        public void execute() {
                            System.out.println("Thread-2: execute doInSync 1");
                        }
                    });// end syncerCallback

                    // sleep
                    sleep(10000);

                    // do again do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
                        @Override
                        public void execute() {
                            System.out.println("Thread-2: execute doInSync 2");
                        }
                    });// end syncerCallback
                    System.out.println("Thread-2: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                }
            }
        }).start();

        // sleep until t1 and t2 should have terminated/excepted
        int loopCount = 0;
        while ((statusList.size() < 2) && (exceptionHolder.size() < 1) && (loopCount < 90)) {
            sleep(1000);
            loopCount++;
        }
        assertTrue("Threads did not finish in 90sec", loopCount < 90);
        // if not -> they are in deadlock and the db did not detect it
        for (final Exception exception : exceptionHolder) {
            System.out.println("exception: " + exception.getMessage());
            exception.printStackTrace();
        }
        if (exceptionHolder.size() > 0) {
            assertTrue("It throws an exception in test => see sysout exception[0]=" + exceptionHolder.get(0).getMessage(), exceptionHolder.size() == 0);
        }
    }

    /**
     * Test with 2 threads T1 & T2. T1 T2 doInSync T1-1 sleep 5sec sleep 10sec ... ... ... ... doInSync T2-1 ... sleep 10sec ... ... doInSync T1-2 ... finished ...
     * doInSync T2-2 finished
     */
    @Test
    public void testDoInSyncWithSyncerCallback() {

        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testDoInSync", new Long("123"));
        // thread 1
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                        @Override
                        public Boolean execute() {
                            System.out.println("Thread-1: execute doInSync 1");
                            return Boolean.TRUE;
                        }
                    });// end syncerCallback

                    // sleep
                    sleep(10000);

                    // do again do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                        @Override
                        public Boolean execute() {
                            System.out.println("Thread-1: execute doInSync 2");
                            return Boolean.TRUE;
                        }
                    });// end syncerCallback
                    System.out.println("Thread-1: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // sleep
                    sleep(5000);

                    // do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                        @Override
                        public Boolean execute() {
                            System.out.println("Thread-2: execute doInSync 1");
                            return Boolean.TRUE;
                        }
                    });// end syncerCallback

                    // sleep
                    sleep(10000);

                    // do again do something in sync
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                        @Override
                        public Boolean execute() {
                            System.out.println("Thread-2: execute doInSync 2");
                            return Boolean.TRUE;
                        }
                    });// end syncerCallback
                    System.out.println("Thread-2: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                }
            }
        }).start();

        // sleep until t1 and t2 should have terminated/excepted
        int loopCount = 0;
        while ((statusList.size() < 2) && (exceptionHolder.size() < 1) && (loopCount < 90)) {
            sleep(1000);
            loopCount++;
        }
        assertTrue("Threads did not finish in 90sec", loopCount < 90);
        // if not -> they are in deadlock and the db did not detect it
        for (final Exception exception : exceptionHolder) {
            System.out.println("exception: " + exception.getMessage());
            exception.printStackTrace();
        }
        if (exceptionHolder.size() > 0) {
            assertTrue("It throws an exception in test => see sysout exception[0]=" + exceptionHolder.get(0).getMessage(), exceptionHolder.size() == 0);
        }
    }

    @Test
    public void testNestedAssertExceptionInDoInSync() {
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testNestedAssertExceptionInDoInSync", new Long("123"));

        try {
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                @Override
                public Boolean execute() {
                    System.out.println("testNestedAssertExceptionInDoInSync: execute doInSync 1");

                    // Do agin in sync => nested => no allowed!
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                        @Override
                        public Boolean execute() {
                            System.out.println("testNestedAssertExceptionInDoInSync: execute doInSync 2");
                            fail("No NestedAssertException thrown");
                            return Boolean.TRUE;
                        }
                    });// end syncerCallback

                    return Boolean.TRUE;
                }
            });// end syncerCallback
        } catch (final AssertException aex) {
            System.out.println("testNestedAssertExceptionInDoInSync: Ok, got a AssertException=" + aex);
        }
    }

    @Test
    public void testSyncerAssertAlreadyDoInSyncFor() {
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testSyncerAssertAlreadyDoInSyncFor", new Long("123"));

        // 1. check assertAlreadyDoInSyncFor WITHOUT sync-block => AssertException must be thrown
        try {
            CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(ores);
            fail("Did not throw AssertException");
        } catch (final AssertException ex) {
            System.out.println("testSyncerAssertAlreadyDoInSyncFor: This exception is ok, exception=" + ex.getMessage());
        }

        // 2.check assertAlreadyDoInSyncFor WITH sync-block => No AssertException should occour
        try {
            System.out.println("testSyncerAssertAlreadyDoInSyncFor: before doInSync");
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>() {
                @Override
                public Boolean execute() {
                    System.out.println("testSyncerAssertAlreadyDoInSyncFor: execute before assertAlreadyDoInSyncFor");
                    CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(ores);
                    System.out.println("testSyncerAssertAlreadyDoInSyncFor: execute done");
                    return Boolean.TRUE;
                }
            });// end syncerCallback
        } catch (final AssertException aex) {
            fail("testSyncerAssertAlreadyDoInSyncFor: got a AssertException=" + aex);
        }

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
    public void setUp() throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
    }

}
