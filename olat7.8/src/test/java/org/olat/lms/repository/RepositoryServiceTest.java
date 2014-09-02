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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.repository.RepositoryDao;
import org.olat.data.repository.RepositoryEntry;

/**
 * RepositoryServiceTest
 * 
 * <P>
 * Initial Date: 06.07.2011 <br>
 * 
 * @author guido
 */
public class RepositoryServiceTest {

    private RepositoryServiceImpl repositoryService;
    private RepositoryDao repositoryDao;
    private RepositoryEntry entry;

    @Before
    public void setup() {
        repositoryService = new RepositoryServiceImpl();
        repositoryDao = mock(RepositoryDao.class);
        repositoryService.repositoryDao = repositoryDao;
        entry = mock(RepositoryEntry.class);

    }

    @Test
    public void createRepositoryEntryInstanceWithNullAuthorTest() {
        try {
            repositoryService.createRepositoryEntryInstance(null);
            fail("null value should raise exception");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void createRepositoryEntryInstanceWithAuthorStringTest() {
        String author = "blabliblue";
        when(repositoryDao.createRepositoryEntryInstance(author)).thenReturn(entry);
        RepositoryEntry re = repositoryService.createRepositoryEntryInstance(author);
        assertNotNull(re);
    }

    @Test
    public void createRepositoryEntryStatusTest() {
        RepositoryEntryStatus reStatus = repositoryService.createRepositoryEntryStatus(2);
        assertTrue(reStatus.isClosed());

        reStatus = repositoryService.createRepositoryEntryStatus(-2);
        assertTrue(reStatus.isClosed());

        reStatus = repositoryService.createRepositoryEntryStatus(22);
        assertTrue(reStatus.isClosed());

    }

}
