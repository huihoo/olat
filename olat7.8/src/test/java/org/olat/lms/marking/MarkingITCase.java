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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.lms.marking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.marking.Mark;
import org.olat.data.marking.MarkDAO;
import org.olat.data.marking.MarkResourceStat;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * Test for the marking service
 * <P>
 * Initial Date: 8 juin 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class MarkingITCase extends OlatTestCase {

    private Identity ident1, ident2, ident3;
    private Identity[] identities;

    private final String subPath1 = "sub-path-1";
    private final String subPath2 = "sub-path-2";
    private final String subPath3 = "sub-path-3";
    private final String subPath4 = "sub-path-4";
    private final String[] subPaths = { subPath1, subPath2, subPath3, subPath4 };

    private static final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testresource", Long.valueOf(1234l));
    private static MarkDAO marking;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        ident1 = JunitTestHelper.createAndPersistIdentityAsUser("test-1");
        ident2 = JunitTestHelper.createAndPersistIdentityAsUser("test-2");
        ident3 = JunitTestHelper.createAndPersistIdentityAsUser("test-3");
        identities = new Identity[] { ident1, ident2, ident3 };
        marking = applicationContext.getBean(MarkDAO.class);
    }

    @Test
    public void testSetMark() {
        for (final Identity ident : identities) {
            final Mark mark = marking.setMark(ores, ident, subPath1, "");
            assertEquals(ident, mark.getCreator());
            assertEquals(subPath1, mark.getResSubPath());
            assertEquals(ores.getResourceableTypeName(), mark.getOLATResourceable().getResourceableTypeName());
            assertEquals(ores.getResourceableId(), mark.getOLATResourceable().getResourceableId());
            final boolean marked = marking.isMarked(ores, ident, subPath1);
            assertTrue(marked);
        }
    }

    @Test
    public void testRemoveMark() {
        for (final Identity ident : identities) {
            marking.setMark(ores, ident, subPath1, "");
        }
        marking.removeMark(ores, ident1, subPath1);
        final boolean markedAfterRemove = marking.isMarked(ores, ident1, subPath1);
        assertEquals(markedAfterRemove, false);
        final boolean marked = marking.isMarked(ores, ident2, subPath1);
        assertTrue(marked);
    }

    @Test
    public void testRemoveResource() {
        for (final Identity ident : identities) {
            for (final String subPath : subPaths) {
                marking.setMark(ores, ident, subPath, "");
            }
        }

        marking.deleteMark(ores);

        boolean marked = false;
        for (final Identity ident : identities) {
            for (final String subPath : subPaths) {
                marked |= marking.isMarked(ores, ident, subPath);
            }
        }
        assertFalse(marked);
    }

    @Test
    public void testIdentityStats() {
        for (final String subPath : subPaths) {
            if (subPath.equals(subPath3)) {
                continue;
            }
            marking.setMark(ores, ident1, subPath, "");
        }

        final List<String> subPathList = Arrays.asList(subPaths);
        final List<MarkResourceStat> stats = marking.getStats(ores, subPathList, ident1);
        assertEquals(3, stats.size());

        for (final MarkResourceStat stat : stats) {
            assertEquals(1, stat.getCount());
        }
    }

    @Test
    public void testStats() {
        for (final Identity ident : identities) {
            for (final String subPath : subPaths) {
                if (subPath.equals(subPath3)) {
                    continue;
                }
                marking.setMark(ores, ident, subPath, "");
            }
        }

        final List<String> subPathList = Arrays.asList(subPath1, subPath2, subPath3);
        final List<MarkResourceStat> stats = marking.getStats(ores, subPathList, null);
        assertEquals(2, stats.size());

        for (final MarkResourceStat stat : stats) {
            assertEquals(3, stat.getCount());
        }
    }

    /*
     * @Test public void testHeavyLoadStats() { final int numberOf = 30; List<Identity> loadIdentities = new ArrayList<Identity>(); for(int i=0; i<numberOf; i++) {
     * loadIdentities.add(JunitTestHelper.createAndPersistIdentityAsUser("identity-test-" + i)); } DBFactory.getInstance().intermediateCommit(); List<OLATResourceable>
     * loadOres = new ArrayList<OLATResourceable>(); for(int i=0; i<numberOf; i++) { loadOres.add(OresHelper.createOLATResourceableInstance("testresource",
     * Long.valueOf(12300 + i))); } List<String> loadSubPaths = new ArrayList<String>(); for(int i=0; i<numberOf; i++) { loadSubPaths.add("sub-path-" + i); } int count =
     * 0; for(Identity ident:loadIdentities) { for(OLATResourceable o:loadOres) { for(String sPath:loadSubPaths) { service.setMark(o, ident, sPath, ""); if(++count % 20
     * == 0) { DBFactory.getInstance().intermediateCommit(); } } } } DBFactory.getInstance().intermediateCommit(); long start = System.currentTimeMillis();
     * List<MarkResourceStat> stat1s = service.getStats(loadOres.get(7), null, loadIdentities.get(15)); System.out.println(stat1s.size() + " in (ms): " +
     * (System.currentTimeMillis() - start)); DBFactory.getInstance().intermediateCommit(); start = System.currentTimeMillis(); List<MarkResourceStat> stat2s =
     * service.getStats(loadOres.get(18), null, null); System.out.println(stat2s.size() + " in (ms): " + (System.currentTimeMillis() - start));
     * DBFactory.getInstance().intermediateCommit(); }
     */
}
