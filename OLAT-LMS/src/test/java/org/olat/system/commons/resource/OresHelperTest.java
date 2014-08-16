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
package org.olat.system.commons.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.system.exception.AssertException;

/**
 * TODO: Class Description for OresHelperTest
 * 
 * <P>
 * Initial Date: 07.06.2011 <br>
 * 
 * @author lavinia
 */
public class OresHelperTest {

    private final Class classInstance = CourseModule.class;
    private final String CLASS_NAME_NO_PACKAGE = "CourseModule";
    private final Long DEFAULT_KEY = new Long(0);
    private final String SUBTYPE = "xyz";
    private final String CLASS_NAME_NO_PACKAGE_WITH_SUBTYPE = CLASS_NAME_NO_PACKAGE + ":" + SUBTYPE;
    private final String RESOURCEABLE_STRING_REPRESENTATION = CLASS_NAME_NO_PACKAGE + "::" + DEFAULT_KEY;

    private OLATResourceable oLATResourceable;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        oLATResourceable = new OLATResourceable() {
            @Override
            public String getResourceableTypeName() {
                return CLASS_NAME_NO_PACKAGE;
            }

            @Override
            public Long getResourceableId() {
                return DEFAULT_KEY;
            }
        };
    }

    @Test
    public void calculateTypeName_WithClassArg() {
        String typeName = OresHelper.calculateTypeName(classInstance);
        assertEquals(typeName, CLASS_NAME_NO_PACKAGE);
    }

    @Test
    public void calculateTypeName_WithClassAndSubtypeArgs() {
        String typeName = OresHelper.calculateTypeName(classInstance, SUBTYPE);
        // System.out.println(typeName);
        assertEquals(typeName, CLASS_NAME_NO_PACKAGE_WITH_SUBTYPE);
    }

    @Test(expected = AssertException.class)
    public void calculateTypeName_WithTypeAndSubtypeArgs_wrongType() {
        OresHelper.calculateTypeName(CLASS_NAME_NO_PACKAGE, SUBTYPE);
    }

    @Test
    public void calculateTypeName_WithTypeAndSubtypeArgs() {
        String typeName = OresHelper.calculateTypeName(classInstance.getName(), SUBTYPE);
        // System.out.println(typeName);
        assertEquals(typeName, CLASS_NAME_NO_PACKAGE_WITH_SUBTYPE);
    }

    @Test
    public void lookupType() {
        OLATResourceable resourceable = OresHelper.lookupType(classInstance);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE);
        assertNull(resourceable.getResourceableId());
    }

    @Test
    public void lookupType_WithSubtype() {
        OLATResourceable resourceable = OresHelper.lookupType(classInstance, SUBTYPE);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE_WITH_SUBTYPE);
        assertNull(resourceable.getResourceableId());
    }

    @Test
    public void createOLATResourceableTypeWithoutCheck() {
        OLATResourceable resourceable = OresHelper.createOLATResourceableTypeWithoutCheck(CLASS_NAME_NO_PACKAGE);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE);
        assertNotNull(resourceable.getResourceableId());
        assertEquals(resourceable.getResourceableId(), DEFAULT_KEY);
    }

    @Test
    public void createOLATResourceableType_WithTypeAndSubtypeArg() {
        OLATResourceable resourceable = OresHelper.createOLATResourceableType(CLASS_NAME_NO_PACKAGE, SUBTYPE);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE_WITH_SUBTYPE);
        assertNotNull(resourceable.getResourceableId());
        assertEquals(resourceable.getResourceableId(), DEFAULT_KEY);
    }

    @Test
    public void createOLATResourceableType_WithTypeArg() {
        OLATResourceable resourceable = OresHelper.createOLATResourceableType(CLASS_NAME_NO_PACKAGE);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE);
        assertNotNull(resourceable.getResourceableId());
        assertEquals(resourceable.getResourceableId(), DEFAULT_KEY);
    }

    @Test(expected = AssertException.class)
    public void createOLATResourceableInstance_ForClassAndNullKey() {
        OresHelper.createOLATResourceableInstance(classInstance, null);
    }

    @Test
    public void createOLATResourceableInstance_ForClass() {
        OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(classInstance, DEFAULT_KEY);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE);
        assertNotNull(resourceable.getResourceableId());
        assertEquals(resourceable.getResourceableId(), DEFAULT_KEY);
    }

    @Test(expected = AssertException.class)
    public void createOLATResourceableInstance_ForStringAndNullKey() {
        OresHelper.createOLATResourceableInstance(CLASS_NAME_NO_PACKAGE, null);
    }

    @Test
    public void createOLATResourceableInstance_ForString() {
        OLATResourceable resourceable = OresHelper.createOLATResourceableInstance(CLASS_NAME_NO_PACKAGE, DEFAULT_KEY);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), CLASS_NAME_NO_PACKAGE);
        assertNotNull(resourceable.getResourceableId());
        assertEquals(resourceable.getResourceableId(), DEFAULT_KEY);
    }

    @Test
    public void createStringRepresenting() {
        String oresStringRespresentation = OresHelper.createStringRepresenting(oLATResourceable);
        // System.out.println(oresStringRespresentation);
        assertEquals(RESOURCEABLE_STRING_REPRESENTATION, oresStringRespresentation);
    }

    @Test
    public void isOfType() {
        assertTrue(OresHelper.isOfType(oLATResourceable, classInstance));
        assertFalse(OresHelper.isOfType(oLATResourceable, ICourse.class));
    }

    @Test
    public void isLookupTypeEquivalentWithCreateOLATResourceableType() {
        OLATResourceable oLATResourceable_1 = OresHelper.lookupType(classInstance, SUBTYPE);

        String typeName = OresHelper.calculateTypeName(classInstance, SUBTYPE);
        OLATResourceable oLATResourceable_2 = OresHelper.createOLATResourceableType(typeName);
        assertFalse(OresHelper.equals(oLATResourceable_1, oLATResourceable_2));
        assertTrue(oLATResourceable_1.getResourceableTypeName().equals(oLATResourceable_2.getResourceableTypeName()));
    }
}
