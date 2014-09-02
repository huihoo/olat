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
package org.olat.lms.admin.sysinfo;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * Initial Date: 11.09.2012 <br>
 * 
 * @author lavinia
 */
public class LogFileParserTest {

    private LogFileParser logFileParser;
    private String logFileName = "olat.sample.log";
    private String logfilepath = "";
    private final int linecount = 30;

    @Before
    public void setup() {
        File testData = new File(this.getClass().getResource(logFileName).getFile());
        try {
            logfilepath = testData.getCanonicalPath();
            logFileParser = new LogFileParser(logfilepath, linecount);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void getError_Exists() {
        Collection<String> errormsg = logFileParser.getError("N1-E1", logfilepath, true);
        assertTrue("Expected to find at least an error in the olat.log", errormsg.size() > 0);
        assertTrue("expected: " + linecount, errormsg.size() == linecount);
        // Iterator<String> iterator = errormsg.iterator();
        // while (iterator.hasNext()) {
        // debug
        // System.out.println(iterator.next());
        // }

    }

    @Test
    public void getError_NotExists() {
        Collection<String> errormsg = logFileParser.getError("N1-E999", logfilepath, true);
        assertTrue("Expected to find at least an error in the olat.log", errormsg.size() == 0);
    }
}
