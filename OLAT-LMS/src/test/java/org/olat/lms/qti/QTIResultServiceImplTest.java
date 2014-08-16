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
package org.olat.lms.qti;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.qti.QTIResultDao;
import org.olat.data.qti.QTIResultSet;

/**
 * Tests for QTI-result-service.
 * 
 * @author Christian Guretzki
 */
public class QTIResultServiceImplTest {

    private QTIResultDao qtiResultDaoMock;
    private QTIResultServiceImpl qtiResultServiceImpl;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        qtiResultDaoMock = mock(QTIResultDao.class);
        qtiResultServiceImpl = new QTIResultServiceImpl(qtiResultDaoMock);
    }

    /**
     * Test method 'deleteUserData', check if all qti-results will be deleted. Input : empty list Output: empty list
     */
    @Test
    public void testDeleteUserData() {
        Identity identityMock = mock(Identity.class);
        String newDeletedUserName = "deletedUserName";
        List qtiResultSets = new ArrayList();
        QTIResultSet qtiResultSet1 = mock(QTIResultSet.class);
        qtiResultSets.add(qtiResultSet1);
        QTIResultSet qtiResultSet2 = mock(QTIResultSet.class);
        qtiResultSets.add(qtiResultSet2);
        when(qtiResultDaoMock.findQtiResultSets(identityMock)).thenReturn(qtiResultSets);
        qtiResultServiceImpl.deleteUserData(identityMock, newDeletedUserName);

        verify(qtiResultDaoMock).deleteResultSet(qtiResultSet1);
        verify(qtiResultDaoMock).deleteResultSet(qtiResultSet2);
    }

}
