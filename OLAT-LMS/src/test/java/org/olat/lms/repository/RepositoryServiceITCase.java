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

package org.olat.lms.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.coordinate.ClusterSyncer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseModule;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.Syncer;
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
import org.olat.testutils.codepoints.server.impl.JMSCodepointServer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Mar 26, 2004
 * 
 * @author gnaegi Comment:
 */
public class RepositoryServiceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private static boolean isInitialized = false;
    private static String CODEPOINT_SERVER_ID = "RepositoryManagerITCase";
    private JMSCodepointServer codepointServer_;
    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() {
        try {
            // Setup for code-points
            JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);

            RepositoryServiceITCase.isInitialized = true;
        } catch (final Exception e) {
            log.error("Error while setting up activeMq or Codepointserver", e);
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() {
        try {
            JMSCodePointServerJunitHelper.stopServer();
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed", e);
        }
    }

    /**
     * Test creation of a repository entry.
     */
    @Test
    public void testRawRepositoryEntryCreate() {
        try {
            final DB db = DBFactory.getInstance();
            final OLATResourceManager rm = OLATResourceManager.getInstance();
            // create course and persist as OLATResourceImpl
            final OLATResourceable resourceable = new OLATResourceable() {
                public String getResourceableTypeName() {
                    return "Course";
                }

                public Long getResourceableId() {
                    return new Long(456);
                }
            };
            final OLATResource r = rm.createOLATResourceInstance(resourceable);
            db.saveObject(r);

            // now make a repository entry for this course
            final RepositoryEntry d = repositoryService.createRepositoryEntryInstance("test");
            d.setOlatResource(r);
            d.setResourcename("Lernen mit OLAT");
            d.setInitialAuthor("Florian Gnägi");
            d.setDisplayname("JunitTest_RepositoryEntry");
            // TODO: chg: Remove for 5.3 MetaDataElement because it does not work with mySql 5.0
            // and it is not used !
            // lastmodified of MetaDataElement is null and is not set by hibernate because
            // it is a child object od RepositoryEntry !
            // It could be solved with defintion of MetaDataElement.lastmodified IS NULL = false
            // MetaDataElement v1, v2, v3;
            // v1 = new MetaDataElement("version", "1.0 alpha");
            // d.getMetaDataElements().add(v1);
            // v2 = new MetaDataElement("duration", "2h");
            // d.getMetaDataElements().add(v2);
			// v3 = new MetaDataElement("path", "/UZH/ID/MELS/");
            // d.getMetaDataElements().add(v3);
            db.saveObject(d);
        } catch (final Exception ex) {
            fail("No Exception allowed. ex=" + ex.getMessage());
        }

    }

    /**
	 */
    @Test
    public void testQueryReferencableResourcesLimitType() {
        DB db = DBFactory.getInstance();

        final Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id1");
        final Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id2");

        // generate 5000 repo entries
        final int numbRes = 5000;
        final long startCreate = System.currentTimeMillis();
        for (int i = 1; i < numbRes; i++) {
            // create course and persist as OLATResourceImpl
            final RepositoryEntry re = createCourseRepositoryEntry(i);
            if ((i % 2 > 0)) {
                re.setCanReference(true);
            } else {
                re.setCanReference(false);
            }
            // create security group
            final SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
            // member of this group may modify member's membership
            securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, ownerGroup);
            // members of this group are always authors also
            securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
            if ((i % 2 > 0)) {
                securityManager.addIdentityToSecurityGroup(id1, ownerGroup);
            } else {
                securityManager.addIdentityToSecurityGroup(id2, ownerGroup);
            }
            re.setOwnerGroup(ownerGroup);
            // save the repository entry
            repositoryService.saveRepositoryEntry(re);
            // Create course admin policy for owner group of repository entry
            // -> All owners of repository entries are course admins
            securityManager.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());

            // flush database and hibernate session cache after 10 records to improve performance
            // without this optimization, the first entries will be fast but then the adding new
            // entries will slow down due to the fact that hibernate needs to adjust the size of
            // the session cache permanently. flushing or transactions won't help since the problem
            // is in the session cache.
            if (i % 10 == 0) {
                db.closeSession();
                db = DBFactory.getInstance();
            }
        }
        final long endCreate = System.currentTimeMillis();
        log.debug("created " + numbRes + " repo entries in " + (endCreate - startCreate) + "ms");

        final List typelist = new ArrayList();
        typelist.add(CourseModule.getCourseTypeName());
        // finally the search query
        final long startSearchReferencable = System.currentTimeMillis();
		final List results = repositoryService.queryReferencableResourcesLimitType(id1, new Roles(false, false, false, true, false, false, false), typelist, null, null, null);
        final long endSearchReferencable = System.currentTimeMillis();
        log.debug("found " + results.size() + " repo entries " + (endSearchReferencable - startSearchReferencable) + "ms");

        // only half of the items should be found
        assertTrue(results.size() == (int) (numbRes / 2));

        // inserting must take longer than searching, otherwhise most certainly we have a problem somewhere in the query
        assertTrue((endCreate - startCreate) > (endSearchReferencable - startSearchReferencable));

    }

    private RepositoryEntry createCourseRepositoryEntry(final int i) {
        final DB db = DBFactory.getInstance();
        final OLATResourceable resourceable = new OLATResourceable() {
            public String getResourceableTypeName() {
                return CourseModule.getCourseTypeName();
            }

            public Long getResourceableId() {
                return new Long(i);
            }
        };
        final OLATResource r = OLATResourceManager.getInstance().createOLATResourceInstance(resourceable);
        db.saveObject(r);

        // now make a repository entry for this course
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().createRepositoryEntryInstance("Florian Gnägi", "Lernen mit OLAT " + i,
                "yo man description bla bla + i");
        re.setDisplayname("JunitTest_RepositoryEntry_" + i);
        re.setOlatResource(r);
        re.setAccess(RepositoryEntry.ACC_OWNERS_AUTHORS);
        return re;
    }

    @Test
    public void testCountByTypeLimitAccess() {

        int count = repositoryService.countByTypeLimitAccess("unkown", RepositoryEntry.ACC_OWNERS_AUTHORS);
        assertEquals("Unkown type must return 0 elements", 0, count);
        final int countValueBefore = repositoryService.countByTypeLimitAccess(CourseModule.getCourseTypeName(), RepositoryEntry.ACC_OWNERS_AUTHORS);
        // add 1 entry
        final RepositoryEntry re = createCourseRepositoryEntry(999999);
        // create security group
        final SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
        re.setOwnerGroup(ownerGroup);
        repositoryService.saveRepositoryEntry(re);
        count = repositoryService.countByTypeLimitAccess(CourseModule.getCourseTypeName(), RepositoryEntry.ACC_OWNERS_AUTHORS);
        // check count must be one more element
        assertEquals("Add one course repository-entry, but countByTypeLimitAccess does NOT return one more element", countValueBefore + 1, count);
    }

    /**
	 * 
	 */
    @Test
    public void testIncrementLaunchCounter() {
        final Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
        assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
        final RepositoryEntry repositoryEntry = createRepository("T1_perf2", new Long(927888101));
        final Long keyRepo = repositoryEntry.getKey();
        final OLATResourceable resourceable = repositoryEntry.getOlatResource();
        DBFactory.getInstance().closeSession();
        assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter());
        final int mainLoop = 10;
        final int loop = 50; // 10 * 50 = 500
        for (int m = 0; m < mainLoop; m++) {
            final long startTime = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                // 1. load RepositoryEntry
                final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                RepositoryServiceImpl.getInstance().incrementLaunchCounter(repositoryEntryT1);
                DBFactory.getInstance().closeSession();
            }
            final long endTime = System.currentTimeMillis();
            System.out.println("testIncrementLaunchCounter time=" + (endTime - startTime) + " for " + loop + " incrementLaunchCounter calls");
            sleep(2000);
        }
        sleep(20000);
        final RepositoryEntry repositoryEntry2 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        assertEquals("Wrong value of incrementLaunch counter", mainLoop * loop, repositoryEntry2.getLaunchCounter());
        System.out.println("testIncrementLaunchCounter finished");
    }

    /**
	 * 
	 */
    @Test
    public void testIncrementDownloadCounter() {
        final Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
        assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
        final RepositoryEntry repositoryEntry = createRepository("T1_perf2", new Long(927888102));
        final Long keyRepo = repositoryEntry.getKey();
        final OLATResourceable resourceable = repositoryEntry.getOlatResource();
        DBFactory.getInstance().closeSession();
        assertEquals("Download counter was not 0", 0, repositoryEntry.getDownloadCounter());
        final int mainLoop = 10;
        final int loop = 50; // 10 * 50 = 500
        for (int m = 0; m < mainLoop; m++) {
            final long startTime = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                // 1. load RepositoryEntry
                final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                RepositoryServiceImpl.getInstance().incrementDownloadCounter(repositoryEntryT1);
                DBFactory.getInstance().closeSession();
            }
            final long endTime = System.currentTimeMillis();
            System.out.println("testIncrementDownloadCounter time=" + (endTime - startTime) + " for " + loop + " incrementDownloadCounter calls");
            sleep(2000);
        }
        sleep(20000);
        final RepositoryEntry repositoryEntry2 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        assertEquals("Wrong value of incrementLaunch counter", mainLoop * loop, repositoryEntry2.getDownloadCounter());
        System.out.println("testIncrementDownloadCounter finished");
    }

    /**
     * Test synchronization between same RepositoryEntry and setLastUsageNowFor, incrementLaunchCounter and incrementDownloadCounter. This test starts 4 threads : 2 to
     * call setLastUsageNowFor, 1 to call incrementLaunchCounter 1 to call incrementDownloadCounter Breakpoint is set for 'setLastUsageNowFor', all other calls must wait.
     */
    @Test
    public void testSetLastUsageNowFor() {
        Date lastSetLastUsageDate = null;
        final Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
        assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
        final int loop = 500;
        final RepositoryEntry repositoryEntry = createRepository("T1_perf2", new Long(927888103));
        final Long keyRepo = repositoryEntry.getKey();
        final OLATResourceable resourceable = repositoryEntry.getOlatResource();
        DBFactory.getInstance().closeSession();
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            // 1. load RepositoryEntry
            final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
            RepositoryServiceImpl.getInstance().setLastUsageNowFor(repositoryEntryT1);
            lastSetLastUsageDate = Calendar.getInstance().getTime();
            DBFactory.getInstance().closeSession();
        }
        final long endTime = System.currentTimeMillis();
        sleep(20000);
        final RepositoryEntry repositoryEntry2 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        assertTrue("Wrong date-value of lastUsage, could not be before ", repositoryEntry2.getLastUsage().after(lastSetLastUsageDate));
        System.out.println("testSetLastUsageNowFor time=" + (endTime - startTime) + " for " + loop + " testSetLastUsageNowFor calls");
        System.out.println("testSetLastUsageNowFor finished");
    }

    @Test
    public void testConcurrentIncrementLaunchCounter() {
        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        final Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
        assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
        final int loop = 100;
        final int numberOfThreads = 3;
        final RepositoryEntry repositoryEntry = createRepository("T1_perf2", new Long(927888104));
        final Long keyRepo = repositoryEntry.getKey();
        final OLATResourceable resourceable = repositoryEntry.getOlatResource();
        DBFactory.getInstance().closeSession();
        assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter());
        final long startTime = System.currentTimeMillis();
        // start thread 1 : incrementLaunchCounter / setAccess
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 1; i <= loop; i++) {
                        // 1. load RepositoryEntry
                        final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                        RepositoryServiceImpl.getInstance().incrementLaunchCounter(repositoryEntryT1);
                        if (i % 20 == 0) {
                            final int ACCESS_VALUE = 4;
                            System.out.println("RepositoryManagerITCase: call setAccess i=" + i);
                            RepositoryServiceImpl.getInstance().setAccess(repositoryEntryT1, ACCESS_VALUE);
                            DBFactory.getInstance().closeSession();
                            final RepositoryEntry repositoryEntryT1Reloaded = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                            assertEquals("Wrong access value", ACCESS_VALUE, repositoryEntryT1Reloaded.getAccess());
                        } else if (i % 10 == 0) {
                            final int ACCESS_VALUE = 1;
                            System.out.println("RepositoryManagerITCase: call setAccess i=" + i);
                            RepositoryServiceImpl.getInstance().setAccess(repositoryEntryT1, ACCESS_VALUE);
                            DBFactory.getInstance().closeSession();
                            final RepositoryEntry repositoryEntryT1Reloaded = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                            assertEquals("Wrong access value", ACCESS_VALUE, repositoryEntryT1Reloaded.getAccess());
                        }
                        DBFactory.getInstance().closeSession();
                    }
                    System.out.println("Thread-1: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                } finally {
                    try {
                        DBFactory.getInstance().closeSession();
                    } catch (final Exception e) {
                        // ignore
                    }
                }
            }
        }).start();
        // start thread 2 : incrementLaunchCounter / setDescriptionAndName
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 1; i <= loop; i++) {
                        // 1. load RepositoryEntry
                        final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                        RepositoryServiceImpl.getInstance().incrementLaunchCounter(repositoryEntryT1);
                        if (i % 25 == 0) {
                            final String displayName = "DisplayName" + i;
                            final String description = "Description" + i;
                            System.out.println("RepositoryManagerITCase: call setDescriptionAndName");
                            RepositoryServiceImpl.getInstance().setDescriptionAndName(repositoryEntryT1, displayName, description);
                            DBFactory.getInstance().closeSession();
                            final RepositoryEntry repositoryEntryT1Reloaded = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                            assertEquals("Wrong displayName value", displayName, repositoryEntryT1Reloaded.getDisplayname());
                            assertEquals("Wrong description value", description, repositoryEntryT1Reloaded.getDescription());
                        }
                        DBFactory.getInstance().closeSession();
                    }
                    System.out.println("Thread-1: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                } finally {
                    try {
                        DBFactory.getInstance().closeSession();
                    } catch (final Exception e) {
                        // ignore
                    }
                }
            }
        }).start();
        // start thread 3
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 1; i <= loop; i++) {
                        // 1. load RepositoryEntry
                        final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                        RepositoryServiceImpl.getInstance().incrementLaunchCounter(repositoryEntryT1);
                        if (i % 30 == 0) {
                            System.out.println("RepositoryManagerITCase: call setProperties i=" + i);
                            RepositoryServiceImpl.getInstance().setProperties(repositoryEntryT1, true, false, true, false);
                            DBFactory.getInstance().closeSession();
                            final RepositoryEntry repositoryEntryT1Reloaded = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                            assertEquals("Wrong canCopy value", true, repositoryEntryT1Reloaded.getCanCopy());
                            assertEquals("Wrong getCanReference value", false, repositoryEntryT1Reloaded.getCanReference());
                            assertEquals("Wrong getCanLaunch value", true, repositoryEntryT1Reloaded.getCanLaunch());
                            assertEquals("Wrong getCanDownload value", false, repositoryEntryT1Reloaded.getCanDownload());
                        } else if (i % 15 == 0) {
                            System.out.println("RepositoryManagerITCase: call setProperties i=" + i);
                            RepositoryServiceImpl.getInstance().setProperties(repositoryEntryT1, false, true, false, true);
                            DBFactory.getInstance().closeSession();
                            final RepositoryEntry repositoryEntryT1Reloaded = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                            assertEquals("Wrong canCopy value", false, repositoryEntryT1Reloaded.getCanCopy());
                            assertEquals("Wrong getCanReference value", true, repositoryEntryT1Reloaded.getCanReference());
                            assertEquals("Wrong getCanLaunch value", false, repositoryEntryT1Reloaded.getCanLaunch());
                            assertEquals("Wrong getCanDownload value", true, repositoryEntryT1Reloaded.getCanDownload());
                        }
                        DBFactory.getInstance().closeSession();
                    }
                    System.out.println("Thread-1: finished");
                    statusList.add(Boolean.TRUE);
                } catch (final Exception e) {
                    exceptionHolder.add(e);
                } finally {
                    try {
                        DBFactory.getInstance().closeSession();
                    } catch (final Exception e) {
                        // ignore
                    }
                }
            }
        }).start();

        final long endTime = System.currentTimeMillis();
        sleep(20000);
        final RepositoryEntry repositoryEntry2 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        assertEquals("Worng value of incrementLaunch counter", loop * numberOfThreads, repositoryEntry2.getLaunchCounter());
        System.out.println("testConcurrentIncrementLaunchCounter time=" + (endTime - startTime) + " for " + loop + " incrementLaunchCounter calls");
        System.out.println("testConcurrentIncrementLaunchCounter finished");
    }

    /**
     * Compare async increment-call with sync 'setDscription' call.
     */
    @Test
    public void testIncrementLaunchCounterSetDescription() {
        System.out.println("testIncrementLaunchCounterSetDescription: START...");
        final RepositoryEntry repositoryEntry = createRepository("testIncrementLaunchCounterSetDescription", new Long(927888105));
        DBFactory.getInstance().closeSession();
        final Long keyRepo = repositoryEntry.getKey();
        final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        System.out.println("RepositoryManagerITCase: call incrementLaunchCounter");
        final long t1 = System.nanoTime();
        RepositoryServiceImpl.getInstance().incrementLaunchCounter(repositoryEntryT1);
        final long t2 = System.nanoTime();
        System.out.println("RepositoryManagerITCase: call incrementLaunchCounter DONE");
        final String displayName = "DisplayName_testIncrementLaunchCounterSetDescription";
        final String description = "Description_testIncrementLaunchCounterSetDescription";
        System.out.println("RepositoryManagerITCase: call setDescriptionAndName");
        final long t3 = System.nanoTime();
        RepositoryServiceImpl.getInstance().setDescriptionAndName(repositoryEntryT1, displayName, description);
        final long t4 = System.nanoTime();
        System.out.println("RepositoryManagerITCase: call setDescriptionAndName DONE");
        System.out.println("RepositoryManagerITCase: increments take=" + (t2 - t1) + " setDescription take=" + (t4 - t3));
        DBFactory.getInstance().closeSession();
        final RepositoryEntry repositoryEntryT1Reloaded = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        assertEquals("Wrong displayName value", displayName, repositoryEntryT1Reloaded.getDisplayname());
        assertEquals("Wrong description value", description, repositoryEntryT1Reloaded.getDescription());
        System.out.println("testIncrementLaunchCounterSetDescription: FINISHED");
    }

    /**
     * Modify a repository-entry from three thread to check if the exception does not pop up. Thread 1 : call incrementDownloadCounter after 100ms Thread 2 : call
     * incrementDownloadCounter after 300ms Thread 3 : update access-value on repository-entry directly after 200ms Codepoint-breakpoint at
     * IncrementDownloadCounterBackgroundTask in executeTask before update
     */
    @Test
    public void testConcurrentIncrementLaunchCounterWithCodePoints() {
        final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
        final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

        final RepositoryEntry repositoryEntry = createRepository("IncCodePoint", new Long(927888106));
        final Long keyRepo = repositoryEntry.getKey();
        final OLATResourceable resourceable = repositoryEntry.getOlatResource();
        final int access = 4;
        DBFactory.getInstance().closeSession();
        assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter());

        // enable breakpoint
        CodepointClient codepointClient = null;
        CodepointRef codepointRef = null;
        try {
            codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
            codepointRef = codepointClient.getCodepoint("org.olat.lms.repository.async.tasks.IncrementDownloadCounterBackgroundTask.executeTask-before-update");
            codepointRef.enableBreakpoint();
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Could not initialzed CodepointClient");
        }
        // thread 1
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.currentThread().sleep(100);
                    final RepositoryEntry repositoryEntryT1 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                    RepositoryServiceImpl.getInstance().incrementDownloadCounter(repositoryEntryT1);
                    System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread1 incremented download-counter");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        // thread 2
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.currentThread().sleep(300);
                    final RepositoryEntry repositoryEntryT2 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                    RepositoryServiceImpl.getInstance().incrementDownloadCounter(repositoryEntryT2);
                    System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread2 incremented download-counter");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        // thread 3
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.currentThread().sleep(200);
                    final RepositoryEntry repositoryEntryT3 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
                    // change repository directly and not via RepositoryManager.setAccess(...) for testing
                    repositoryEntryT3.setAccess(access);
                    RepositoryServiceImpl.getInstance().updateRepositoryEntry(repositoryEntryT3);
                    DBFactory.getInstance().closeSession();
                    System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread3 setAccess DONE");
                } catch (final Exception ex) {
                    exceptionHolder.add(ex);// no exception should happen
                }
            }
        }).start();

        try {
            System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: main thread sleep 500msec");
            Thread.currentThread().sleep(500);
        } catch (final InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            // to see all registered code-points: comment-in next 2 lines
            // List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
            // System.out.println("codepointList=" + codepointList);
            System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints start waiting for breakpoint reached");
            final TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
            assertTrue("Did not reach breakpoint", threads.length > 0);
            System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
            codepointRef.disableBreakpoint(true);
            System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints breakpoint reached => continue");
        } catch (final BreakpointStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Codepoints: BreakpointStateException=" + e.getMessage());
        } catch (final CommunicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Codepoints: CommunicationException=" + e.getMessage());
        }
        sleep(100);
        final RepositoryEntry repositoryEntry2 = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(keyRepo);
        assertEquals("Wrong value of incrementLaunch counter", 2, repositoryEntry2.getDownloadCounter());
        assertEquals("Wrong access value", access, repositoryEntry2.getAccess());

        codepointClient.close();
        System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints finish successful");
    }

    private RepositoryEntry createRepository(final String name, final Long resourceableId) {
        final OLATResourceManager rm = OLATResourceManager.getInstance();
        // create course and persist as OLATResourceImpl
        final OLATResourceable resourceable = new OLATResourceable() {
            public String getResourceableTypeName() {
                return CourseModule.ORES_TYPE_COURSE;
            }

            public Long getResourceableId() {
                return resourceableId;
            }
        };
        final OLATResource r = rm.createOLATResourceInstance(resourceable);
        DBFactory.getInstance().saveObject(r);

        // now make a repository entry for this course
        final RepositoryEntry d = repositoryService.createRepositoryEntryInstance("test");
        d.setOlatResource(r);
        d.setResourcename(name);
        d.setInitialAuthor("Christian Guretzki");
        d.setDisplayname("JunitTest_RepositoryEntry");
        DBFactory.getInstance().saveObject(d);
        return d;
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

}
