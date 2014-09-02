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
package org.olat.data.commons.filter.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.olat.data.commons.filter.FilterFactory;

/**
 * Initial Date: 16.12.2013 <br>
 * 
 * @author lavinia
 */
@RunWith(JUnit4.class)
public class HtmlFilterTest {

    @Test
    public void filterHtml() {
        String inputText = "bla <p>&lt;script&gt;alert('XSS attempt');&lt;/script&gt;</p> blu";
        String filteredText = FilterFactory.unescapeAndFilterHtml(inputText);
        System.out.println("filteredText:");
        System.out.println(filteredText);
        assertTrue(filteredText.indexOf("script") == -1);
        assertTrue(filteredText.indexOf("attempt") != -1);
    }

    @Test
    public void filterHtml_nullInput() {
        String inputText = null;
        String filteredText = FilterFactory.unescapeAndFilterHtml(inputText);
        assertTrue(filteredText == null);
    }

}
