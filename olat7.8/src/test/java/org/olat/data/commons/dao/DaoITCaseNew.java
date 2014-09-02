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
package org.olat.data.commons.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: 23.11.2011 <br>
 * 
 * @author guretzki
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/commons/dao/_spring/daoDatabaseContextTest.xml" })
public class DaoITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_NAME = "Test Name";
    @Autowired
    private ExampleDaoImpl exampleDao;

    @Test
    public void create() {
        ExampleEntity exampleEntity = exampleDao.create();
        assertNotNull("'Create' must return an instance", exampleEntity);
    }

    @Test
    public void save() {
        exampleDao.save(exampleDao.create());
        // hint: Entity will be automatically cleanup with rollback in AbstractTransactionalJUnit4SpringContextTests
    }

    @Test(expected = IllegalArgumentException.class)
    public void save_Null() {
        exampleDao.save(null);
    }

    @Test
    public void save_AlreadyExist() {
        ExampleEntity exampleEntity = exampleDao.create();
        exampleDao.save(exampleEntity);
        exampleDao.save(exampleEntity);
    }

    @Test
    public void findById() {
        ExampleEntity exampleEntity = createTestEntityWith();
        exampleDao.save(exampleEntity);
        ExampleEntity resultEntity = exampleDao.findById(exampleEntity.getId());
        assertResult(TEST_NAME, TEST_DESCRIPTION, resultEntity);
    }

    @Test
    public void findById_WhenNotExist() {
        ExampleEntity exampleEntity = createTestEntityWith();
        exampleDao.save(exampleEntity);
        Long unknownId = new Long(1123456789);
        ExampleEntity resultEntity = exampleDao.findById(unknownId);
        assertNull("Return null when entity does not exist", resultEntity);
    }

    @Test
    public void update() {
        ExampleEntity exampleEntity = createTestEntityWith();
        exampleDao.save(exampleEntity);
        Long id1 = exampleEntity.getId();

        String changedName = exampleEntity.getName() + " changed";
        String changedDescription = exampleEntity.getDescription() + " changed";
        exampleEntity.setName(changedName);
        exampleEntity.setDescription(changedDescription);
        exampleDao.update(exampleEntity);

        ExampleEntity resultEntity = exampleDao.findById(exampleEntity.getId());
        assertResult(changedName, changedDescription, resultEntity);
        assertEquals("ResultEntity has wrong id", id1, resultEntity.getId());
    }

    @Test
    public void update_WhenNotExist() {
        String entityName = "UPDATE_" + TEST_NAME;
        ExampleEntity exampleEntity = createEntityWith(entityName, "UPDATE_" + TEST_DESCRIPTION);
        ExampleEntity updatedEntity = exampleDao.update(exampleEntity);
        assertNotNull(updatedEntity);
        assertEquals(entityName, updatedEntity.getName());
        assertNotNull(updatedEntity.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_Null() {
        exampleDao.update(null);
    }

    @Test
    public void delete() {
        ExampleEntity exampleEntity = createTestEntityWith();
        exampleDao.save(exampleEntity);
        exampleDao.delete(exampleEntity);
    }

    @Test
    public void delete_WhenNotExist() {
        ExampleEntity exampleEntity = createTestEntityWith();
        exampleDao.delete(exampleEntity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void delete_Null() {
        exampleDao.delete(null);
    }

    private ExampleEntity createTestEntityWith() {
        return createEntityWith(TEST_NAME, TEST_DESCRIPTION);
    }

    private ExampleEntity createEntityWith(String name, String description) {
        ExampleEntity exampleEntity = new ExampleEntity(name, description);
        exampleEntity.setName(name);
        exampleEntity.setDescription(description);
        return exampleEntity;
    }

    private void assertResult(String name, String description, ExampleEntity resultEntity) {
        assertNotNull("ResultEntity could not be null", resultEntity);
        assertEquals("ResultEntity has wrogn name", name, resultEntity.getName());
        assertEquals("ResultEntity has wrong description", description, resultEntity.getDescription());
    }

    @Test(expected = DataRuntimeException.class)
    public void testDataRuntimeException() {
        throw new DataRuntimeException(this.getClass(), "Test DataRuntimeException", null);
    }

    @Test(expected = EntityDoesNotExistException.class)
    public void testEntityDoesNotExistException() {
        throw new EntityDoesNotExistException(this.getClass(), "Test EntityDoesNotExistException", null);
    }

}
