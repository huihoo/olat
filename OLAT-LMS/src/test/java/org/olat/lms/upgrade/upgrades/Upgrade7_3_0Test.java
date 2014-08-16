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
package org.olat.lms.upgrade.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

/**
 * 
 * <P>
 * Initial Date: 26.07.2011 <br>
 * 
 * @author guido
 */
public class Upgrade7_3_0Test {

    File xmlFile;

    // String xmlFile =
    // "<DbPrefs> <prefstore> <entry> <string>InfoSubscription::subs</string> <list> <string>[RepositoryEntry:917504][CourseNode:83947548516378]</string> </list> </entry> <entry> <string>LayoutMain3ColsController::sysadminmain</string> <org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsConfig> <col1WidthEM>21</col1WidthEM> <col2WidthEM>12</col2WidthEM> </org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsConfig> </entry> <entry> <string>InfoSubscription::notdesired</string> <list> <string>[RepositoryEntry:917504][CourseNode:83947548516378]</string> <string>[RepositoryEntry:917504][CourseNode:83947548516379]</string> </list> </entry> <entry> <string>CourseCalendarSubscription::subs</string> <list> <string>83947548516370</string> <string>83947548516371</string> </list> </entry> <entry> <string>CourseCalendarSubscription::notdesired</string> <list> <string>83947548516370</string> <string>83947548516371</string> </list> </entry> </prefstore> <version>1</version> <isInitialized>true</isInitialized> </DbPrefs>";

    /**
     * @throws IOException
     * 
     */
    public Upgrade7_3_0Test() throws IOException {
        URL url = this.getClass().getResource("v2guiprefs.xml");
        xmlFile = new File(url.getFile());
    }

    @Test
    public void testXMLParsingForInfoQuery() {
        GUIPreferencesParser parser = new GUIPreferencesParser();
        List<String> list = parser.parseDataForInputQuery(parser.createDocument(xmlFile), parser.queryInfo);
        assertTrue(list.size() > 0);
        assertEquals("[RepositoryEntry:3168829444][CourseNode:83017497416290]", list.get(0));
    }

    @Test
    public void testXMLParsingForInfoQueryNotDesired() {
        GUIPreferencesParser parser = new GUIPreferencesParser();
        List<String> list = parser.parseDataForInputQuery(parser.createDocument(xmlFile), parser.queryInfoNot);
        assertTrue(list.size() == 0);
    }

    @Test
    public void testXMLParsingForCalendarQuery() {
        GUIPreferencesParser parser = new GUIPreferencesParser();
        List<String> list = parser.parseDataForInputQuery(parser.createDocument(xmlFile), parser.queryCal);
        assertTrue(list.size() > 0);
        assertEquals("83165536553146", list.get(0));
    }

    @Test
    public void testXMLParsingForCalendarQueryNot() {
        GUIPreferencesParser parser = new GUIPreferencesParser();
        List<String> list = parser.parseDataForInputQuery(parser.createDocument(xmlFile), parser.queryCalNot);
        assertTrue(list.size() > 0);
        assertEquals("76989122833532", list.get(0));
        assertEquals("81136132742388", list.get(1));
    }

}
