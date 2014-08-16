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

package org.olat.data.tagging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the TaggingManager. This is more an integration test as a unit test. This goal is to check if the DB queries are OK. It's not in olatcore because it needs
 * Identity and the database.
 * <P>
 * Initial Date: 19 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TaggingManagerITCase extends OlatTestCase {

    private static boolean isInitialized = false;

    private final static String identityTest1Name = "identityTagTest1";
    private final static String identityTest2Name = "identityTagTest2";
    private final static String identityTest3Name = "identityTagTest3";

    private static Identity ident1, ident2, ident3;

    @Autowired
    private DB dbInstance;

    @Autowired
    private TaggingDao taggingManager;

    @Before
    public void setUp() throws Exception {
        if (isInitialized == false) {
            ident1 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest1Name);
            ident2 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest2Name);
            ident3 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest3Name);
            dbInstance.commitAndCloseSession();
            isInitialized = true;
        }
    }

    @Test
    public void testManager() {
        assertNotNull(taggingManager);
    }

    @Test
    public void testSetTag() {
        final TestOLATResource ores = new TestOLATResource(45l, "Artefact");
        final Tag tag = taggingManager.createAndPersistTag(ident1, "Tag1", ores, "SubPath", "BusinessPath");
        dbInstance.commitAndCloseSession();

        assertNotNull(tag);
        assertEquals("Tag1", tag.getTag());
        assertNotNull(tag.getOLATResourceable());
        assertEquals(new Long(45l), tag.getOLATResourceable().getResourceableId());
        assertEquals("Artefact", tag.getOLATResourceable().getResourceableTypeName());
        assertEquals("SubPath", tag.getResSubPath());
        assertEquals("BusinessPath", tag.getBusinessPath());
    }

    @Test
    public void testRetrieveDBTags() {
        final String uuid = UUID.randomUUID().toString();
        final TestOLATResource ores = new TestOLATResource(46l, uuid);
        final Tag tag1 = taggingManager.createAndPersistTag(ident1, "Tag1", ores, null, null);
        final Tag tag2 = taggingManager.createAndPersistTag(ident1, "Tag2", ores, null, null);
        final Tag tag3 = taggingManager.createAndPersistTag(ident2, "Tag2", ores, null, null);
        final Tag tag4 = taggingManager.createAndPersistTag(ident3, "Tag2", ores, null, null);
        dbInstance.commitAndCloseSession();

        assertNotNull(tag1);
        assertNotNull(tag2);
        assertNotNull(tag3);
        assertNotNull(tag4);

        final List<Tag> tags = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(tags);
        assertEquals(4, tags.size());
        assertTrue(tags.contains(tag1));
        assertTrue(tags.contains(tag2));
        assertTrue(tags.contains(tag3));
        assertTrue(tags.contains(tag4));
    }

    @Test
    public void testRetrieveTags() {
        final String uuid = UUID.randomUUID().toString();
        final TestOLATResource ores = new TestOLATResource(47l, uuid);
        final Tag tag1 = taggingManager.createAndPersistTag(ident1, "TagGroup1", ores, null, null);
        final Tag tag2 = taggingManager.createAndPersistTag(ident1, "TagGroup2", ores, null, null);
        final Tag tag3 = taggingManager.createAndPersistTag(ident2, "TagGroup2", ores, null, null);
        final Tag tag4 = taggingManager.createAndPersistTag(ident3, "TagGroup2", ores, null, null);
        dbInstance.commitAndCloseSession();

        assertNotNull(tag1);
        assertNotNull(tag2);
        assertNotNull(tag3);
        assertNotNull(tag4);

        final List<String> tags = taggingManager.getTagsAsString(ident1, ores, null, null);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("TagGroup1"));
        assertTrue(tags.contains("TagGroup2"));
    }

    @Test
    public void testUpdateTag() {
        final String uuid = UUID.randomUUID().toString();
        final TestOLATResource ores = new TestOLATResource(48l, uuid);
        final Tag tag1 = taggingManager.createAndPersistTag(ident1, "TagUpdate1", ores, null, null);
        final Tag tag2 = taggingManager.createAndPersistTag(ident1, "TagUpdate2", ores, null, null);
        dbInstance.commitAndCloseSession();

        assertNotNull(tag1);
        assertNotNull(tag2);

        final List<Tag> tags = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(tags);
        assertEquals(2, tags.size());

        ((TagImpl) tags.get(0)).setTag("TagUpdated1");
        taggingManager.updateTag(tags.get(0));
        ((TagImpl) tags.get(1)).setTag("TagUpdated2");
        taggingManager.updateTag(tags.get(1));

        dbInstance.commitAndCloseSession();

        final List<Tag> updatedTags = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(updatedTags);
        assertEquals(2, updatedTags.size());
        assertTrue(updatedTags.get(0).getTag().equals("TagUpdated1") || updatedTags.get(1).getTag().equals("TagUpdated1"));
        assertTrue(updatedTags.get(0).getTag().equals("TagUpdated2") || updatedTags.get(1).getTag().equals("TagUpdated2"));
    }

    @Test
    public void testDeleteTag() {
        final String uuid = UUID.randomUUID().toString();
        final TestOLATResource ores = new TestOLATResource(49l, uuid);
        final Tag tag1 = taggingManager.createAndPersistTag(ident1, "TagDelete1", ores, null, null);
        final Tag tag2 = taggingManager.createAndPersistTag(ident1, "TagDelete2", ores, null, null);
        dbInstance.commitAndCloseSession();

        assertNotNull(tag1);
        assertNotNull(tag2);

        final List<Tag> tagsToDelete = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(tagsToDelete);
        assertEquals(2, tagsToDelete.size());

        taggingManager.deleteTag(tagsToDelete.get(0));
        taggingManager.deleteTag(tagsToDelete.get(1));
        dbInstance.commitAndCloseSession();

        final List<Tag> deletedTags = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(deletedTags);
        assertEquals(0, deletedTags.size());
    }

    @Test
    public void testDeleteAllTag() {
        final String uuid = UUID.randomUUID().toString();
        final TestOLATResource ores = new TestOLATResource(49l, uuid);
        final Tag tag1 = taggingManager.createAndPersistTag(ident1, "TagDelete1", ores, "subPath1", null);
        final Tag tag2 = taggingManager.createAndPersistTag(ident1, "TagDelete2", ores, "subPath2", null);
        final Tag tag3 = taggingManager.createAndPersistTag(ident1, "TagDelete3", ores, "subPath3", "businessPath45");
        final Tag tag4 = taggingManager.createAndPersistTag(ident1, "TagDelete3", ores, "subPath3", null);
        final Tag tag5 = taggingManager.createAndPersistTag(ident1, "TagDelete4", ores, "subPath1", null);

        final TestOLATResource oresMark = new TestOLATResource(50l, uuid);
        final Tag tag6 = taggingManager.createAndPersistTag(ident1, "TagDelete1", oresMark, "subPath1", null);
        dbInstance.commitAndCloseSession();

        assertNotNull(tag1);
        assertNotNull(tag2);
        assertNotNull(tag3);
        assertNotNull(tag4);
        assertNotNull(tag5);
        assertNotNull(tag6);

        taggingManager.deleteTags(ores, "subPath1", null);
        dbInstance.commitAndCloseSession();

        final List<Tag> deletedTags = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(deletedTags);
        assertEquals(3, deletedTags.size());

        // remove an other one
        taggingManager.deleteTags(ores, "subPath3", "businessPath45");
        dbInstance.commitAndCloseSession();

        final List<Tag> deletedTags2 = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(deletedTags2);
        assertEquals(2, deletedTags2.size());

        // remove all tags of the resource
        taggingManager.deleteTags(ores, null, null);
        dbInstance.commitAndCloseSession();

        final List<Tag> deletedTags3 = taggingManager.loadTagsForResource(ores, null, null);
        assertNotNull(deletedTags3);
        assertEquals(0, deletedTags3.size());

        // check that not all tags are deleted
        final List<Tag> tagMark = taggingManager.loadTagsForResource(oresMark, null, null);
        assertNotNull(tagMark);
        assertEquals(1, tagMark.size());
    }

    private class TestOLATResource implements OLATResourceable {

        private final Long id;
        private final String name;

        public TestOLATResource(final Long id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getResourceableTypeName() {
            return name;
        }

        @Override
        public Long getResourceableId() {
            return id;
        }
    }
}
