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
package org.olat.lms.ims.cp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.ims.cp.objects.CPItem;
import org.olat.lms.ims.cp.objects.CPOrganization;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.OlatTestCase;

/**
 * The test class for the CPManager and its implementation.
 * <P>
 * Initial Date: Jun 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class CPManagerITCase extends OlatTestCase {

    private static final String ITEM_ID = "this_is_a_great_inital_item_identifier";
    private static final String PAGE_TITLE = "fancy page";
    private static final Logger log = LoggerHelper.getLogger();

    private CPManager mgr = null;
    private ContentPackage cp;

    @Before
    public void setUp() {
        // create some users with user manager
        mgr = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        try {
            log.info("setUp start ------------------------");

            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(this.getClass(), Long.valueOf((long) (Math.random() * 100000)));
            cp = mgr.createNewCP(ores, PAGE_TITLE);
            assertNotNull("crated cp is null, check filesystem where the temp cp's are created.", cp);

        } catch (final Exception e) {
            log.error("Exception in setUp(): " + e);
        }
    }

    @After
    public void tearDown() {
        cp.getRootDir().delete();
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
        }
    }

    @Test
    public void testLoad() {
        final ContentPackage relodedCP = mgr.load(cp.getRootDir(), cp.getResourcable());
        assertNotNull(relodedCP);
        final CPOrganization orga = relodedCP.getFirstOrganizationInManifest();
        assertNotNull(orga);
        final CPItem item = orga.getFirstItem();
        assertEquals(PAGE_TITLE, item.getTitle());
    }

    @Test
    public void testCreateNewCP() {
        // Tested through setup. Foundation for the other tests.
    }

    @Test
    public void testIsSingleUsedResource() {
        // mgr.isSingleUsedResource(res, cp);
    }

    @Test
    public void testAddBlankPage() {
        final String pageTitle = "the blank page";
        final String ident = mgr.addBlankPage(cp, pageTitle);
        assertNotNull(ident);

    }

    @Test
    public void testUpdatePage() {
        // TODO:GW impl
    }

    @Test
    public void testAddElement() {
        // TODO:GW impl
    }

    public void testAddElementAfter() {
        final CPItem newItem = new CPItem();
        mgr.addElementAfter(cp, newItem, ITEM_ID);
        assertTrue("The new item wasn't inserted at the second position.", newItem.getPosition() == 1);
    }

    @Test
    public void testRemoveElement() {
        // TODO:GW impl
    }

    @Test
    public void testMoveElement() {
        // TODO:GW impl
    }

    public void testCopyElement() {
        // TODO:GW impl
    }

    @Test
    public void testGetDocument() {
        // TODO:GW impl
    }

    @Test
    public void testGetItemTitle() {
        final String title = mgr.getItemTitle(cp, ITEM_ID);
        assertNotNull(title);
        assertEquals(PAGE_TITLE, title);
    }

    @Test
    public void testGetTreeDataModel() {
        // TODO:GW impl
    }

    @Test
    public void testGetFirstOrganizationInManifest() {
        // TODO:GW impl
    }

    public void testGetFirstPageToDisplay() {
        // this method basically just returns the first element in the cp
    }

    @Test
    public void testGetPageByItemId() {
        final String href = mgr.getPageByItemId(cp, ITEM_ID);
        final VFSItem file = cp.getRootDir().resolve(href);
        assertNotNull("The file path doesn't lead to a file.", file);
    }

    @Test
    public void testWriteToFile() {
        mgr.writeToFile(cp); // Throws exception on failure
    }

    @Test
    public void testWriteToZip() {
        // Substract 1s = 1000ms from now to make sure the time is before
        // execution
        final long before = System.currentTimeMillis() - 1000;
        final VFSLeaf zip = mgr.writeToZip(cp);
        assertNotNull("The zip file wasn't created properly", zip);
        assertTrue("The last modified date of the zip file wasn't updated", zip.getLastModified() > before);
    }

    @Test
    public void testGetElementByIdentifier() {
        // TODO:GW impl
    }

}
