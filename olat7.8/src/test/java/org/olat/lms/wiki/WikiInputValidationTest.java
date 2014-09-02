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
package org.olat.lms.wiki;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Initial Date: 15.11.2013 <br>
 * 
 * @author lavinia
 */
public class WikiInputValidationTest {

    @Test
    public void testInputValidation_accepts_umlaut() {
        String pageNameOK = "Artikel für test 1";
        // String pageNameOK = "Artikel fur test 1";
        boolean isValid = WikiInputValidation.validatePageName(pageNameOK);
        assertTrue(isValid);
    }

    @Test
    public void testInputValidation_refuses_lessthen() {
        String pageNameOK = "<script>";
        boolean isValid = WikiInputValidation.validatePageName(pageNameOK);
        assertFalse(isValid);
    }

    @Test
    public void testInputValidation_refuses_singlequotes() {
        String pageNameOK = "alert('xss')";
        boolean isValid = WikiInputValidation.validatePageName(pageNameOK);
        assertFalse(isValid);
    }
}
