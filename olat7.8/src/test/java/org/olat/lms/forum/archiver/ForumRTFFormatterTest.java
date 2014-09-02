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
package org.olat.lms.forum.archiver;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Initial Date: 22.08.2012 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class ForumRTFFormatterTest {

    public static final ForumRTFFormatter rtfFormatter = new ForumRTFFormatter(null, false);

    @Test
    public void testConvertHTMLMarkupToRTF() throws Exception {
        // test correct escaping of reserved RTF chars
        Assert.assertTrue(convert("Zeile mit \\ in der Mitte").contains("\\\\"));
        Assert.assertTrue(convert("Zeile mit { in der Mitte").contains("\\{"));
        Assert.assertTrue(convert("Zeile mit } in der Mitte").contains("\\}"));

        // test correct detection of HTML <ol> and <ul> lists
        Assert.assertTrue(convert("<ol> <li>eins</li> <li>zwei</li> </ol>").contains("listid1"));
        Assert.assertFalse(convert("<ol> <li>eins</li> <li>zwei</li> </ol>").contains("listid2"));

        Assert.assertTrue(convert("<ul> <li>eins</li> <li>zwei</li> </ul>").contains("listid1"));
        Assert.assertFalse(convert("<ul> <li>eins</li> <li>zwei</li> </ul>").contains("listid2"));

        Assert.assertTrue(convert("<ol> <li>one number</li> <li>two number</li> </ol><ul> <li>one bullet</li> <li>two bullet</li> </ul>").contains("listid1"));
        Assert.assertTrue(convert("<ol> <li>one number</li> <li>two number</li> </ol><ul> <li>one bullet</li> <li>two bullet</li> </ul>").contains("listid2"));
        Assert.assertFalse(convert("<ol> <li>one number</li> <li>two number</li> </ol><ul> <li>one bullet</li> <li>two bullet</li> </ul>").contains("listid3"));

        Assert.assertTrue(convert("<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol>").contains(
                "listid1"));
        Assert.assertTrue(convert("<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol>").contains(
                "listid2"));
        Assert.assertTrue(convert("<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol>").contains(
                "listid3"));
        Assert.assertFalse(convert("<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol>").contains(
                "listid4"));

        Assert.assertTrue(convert(
                "<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul>")
                .contains("listid1"));
        Assert.assertTrue(convert(
                "<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul>")
                .contains("listid2"));
        Assert.assertTrue(convert(
                "<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul>")
                .contains("listid3"));
        Assert.assertTrue(convert(
                "<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul>")
                .contains("listid4"));
        Assert.assertFalse(convert(
                "<ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul><ol> <li>eins</li> <li>zwei</li> </ol><ul> <li>eins</li> <li>zwei</li> </ul>")
                .contains("listid5"));

    }

    @Test
    public void testListsWithDollarSign() throws Exception {
        Assert.assertTrue(convert("<ol> <li>$</li> </ol>").contains("$"));
    }

    private String convert(String input) throws Exception {
        final Method convertMethod = ForumRTFFormatter.class.getDeclaredMethod("convertHTMLMarkupToRTF", String.class);
        convertMethod.setAccessible(true);
        final String result = (String) convertMethod.invoke(rtfFormatter, input);
        return result;
    }

}
