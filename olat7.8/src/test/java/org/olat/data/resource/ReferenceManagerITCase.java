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

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.reference.Reference;
import org.olat.data.reference.ReferenceDao;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;

/**
 * 
 */
public class ReferenceManagerITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();
    private static Long RESOURCABLE_ID_1 = new Long(123);
    private static Long RESOURCABLE_ID_2 = new Long(456);
    private static Long RESOURCABLE_ID_3 = new Long(457);

    // Already tested in BusinessGroupTest :
    // - getGroupsWithPermissionOnOlatResourceable
    // - getIdentitiesWithPermissionOnOlatResourceable
    /**
	 * 
	 */
    @Test
    public void testAddReference() {
        final OLATResource oressource = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type1", RESOURCABLE_ID_1));
        final OLATResource orestarget = OLATResourceManager.getInstance().findOrPersistResourceable(
                OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_2));
        final String udata = "üserdätä";

        // add a reference
        ReferenceDao.getInstance().addReference(oressource, orestarget, udata);
        DBFactory.getInstance().closeSession();

        final OLATResource orestarget2 = OLATResourceManager.getInstance().findOrPersistResourceable(
                OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_2));
        final List refs = ReferenceDao.getInstance().getReferencesTo(orestarget2);
        for (final Iterator it_refs = refs.iterator(); it_refs.hasNext();) {
            final Reference ref = (Reference) it_refs.next();
            System.out.println("ref:" + ref);
        }
        assertTrue("only one reference may exist", refs.size() == 1);
    }

    @Test
    public void testReferencesToAndFrom() {
        // same resouceable id on purpose
        final OLATResource s1 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("s1rrtype1", RESOURCABLE_ID_1));
        final OLATResource s2 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("s2rrtype1", RESOURCABLE_ID_1));
        final OLATResource s3 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("s31rrtype1", RESOURCABLE_ID_1));
        final OLATResource t1 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("t1rrtype1", RESOURCABLE_ID_1));
        final OLATResource t2 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("t2rrtype1", RESOURCABLE_ID_1));

        // add references
        ReferenceDao.getInstance().addReference(s1, t1, "r11");
        ReferenceDao.getInstance().addReference(s2, t1, "r21");
        ReferenceDao.getInstance().addReference(s2, t2, "r22");
        ReferenceDao.getInstance().addReference(s3, t2, "r32");

        DBFactory.getInstance().closeSession();

        // find the refs again

        final List s1R = ReferenceDao.getInstance().getReferences(s1);
        assertTrue("s1 only has one reference", s1R.size() == 1);
        final Reference ref = (Reference) s1R.get(0);
        assertTrue("source and s1 the same", OresHelper.equals(ref.getSource(), s1));
        assertTrue("target and t1 the same", OresHelper.equals(ref.getTarget(), t1));

        // two refs from s2
        final List s2refs = ReferenceDao.getInstance().getReferences(s2);
        assertTrue("s2 holds two refs (to t1 and t2)", s2refs.size() == 2);

        // two refs to t2
        final List t2refs = ReferenceDao.getInstance().getReferencesTo(t2);
        assertTrue("t2 holds two source refs (to s2 and s3)", t2refs.size() == 2);

    }

    @Test
    public void testAddAndDeleteReference() {
        final OLATResource oressource = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type1", RESOURCABLE_ID_1));
        final OLATResource orestarget = OLATResourceManager.getInstance().findOrPersistResourceable(
                OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_3));
        final String udata = "üserdätä";

        // add a reference
        ReferenceDao.getInstance().addReference(oressource, orestarget, udata);
        DBFactory.getInstance().closeSession();

        final OLATResource orestarget2 = OLATResourceManager.getInstance().findOrPersistResourceable(
                OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_3));
        final List refs = ReferenceDao.getInstance().getReferencesTo(orestarget2);
        assertTrue("only one reference may exist", refs.size() == 1);
        for (final Iterator it_refs = refs.iterator(); it_refs.hasNext();) {
            final Reference ref = (Reference) it_refs.next();
            ReferenceDao.getInstance().delete(ref);
        }

        DBFactory.getInstance().closeSession();

        // now make sure the reference was deleted
        final OLATResource orestarget3 = OLATResourceManager.getInstance().findOrPersistResourceable(
                OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_3));
        final List norefs = ReferenceDao.getInstance().getReferencesTo(orestarget3);
        assertTrue("reference should now be deleted", norefs.size() == 0);

    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        try {
            // DB.getInstance().delete("select * from o_bookmark");
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }
}
