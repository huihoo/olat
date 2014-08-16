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

package org.olat.presentation.wiki.versioning.diff;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.presentation.wiki.versioning.ChangeInfo;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 19.9.2006 <br>
 * 
 * @author Christian Guretzki
 */
public class CookbookDiffTest {
    private static final Logger log = LoggerHelper.getLogger();

    private CookbookDifferenceService differenceService;

    @Before
    public void setup() throws Exception {
        differenceService = new CookbookDifferenceService();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddText() {
        final String text1 = "Line1\nLine2\nDies ist ein Text.";
        final String text2 = text1 + "Text2";
        final List diffList = differenceService.diff(text1, text2);
        final int i = 1;
        for (final Iterator iter = diffList.iterator(); iter.hasNext();) {
            final ChangeInfo changeInfo = (ChangeInfo) iter.next();
            assertEquals("Type must be CHANGE", changeInfo.getType(), ChangeInfo.CHANGE);
            assertEquals("Wrong line content.", changeInfo.getLines()[0], "Dies ist ein Text.");
            assertEquals("Wrong line content.", changeInfo.getLines()[1], "Dies ist ein Text.Text2");
        }
    }

    @Test
    public void testMove() {
        final String text1 = "Line1\nLine2\nDies ist ein Text.\nbla bla\nText2 Text2.1 Text2.2";
        final String text2 = "Line1\nLine2\nDies ist ein Text.\nText2 Text2.1 Text2.2\nbla bla";
        final List diffList = differenceService.diff(text1, text2);
        final int i = 1;
        for (final Iterator iter = diffList.iterator(); iter.hasNext();) {
            final ChangeInfo changeInfo = (ChangeInfo) iter.next();
            assertEquals("Type must be MOVE", changeInfo.getType(), ChangeInfo.MOVE);
            assertEquals("Wrong line content.", changeInfo.getLines()[0], "Text2 Text2.1 Text2.2");
        }
    }

}
