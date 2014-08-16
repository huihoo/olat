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
package org.olat.lms.commons.filemetadata;

/**
 * TODO: Class Description for FileFilterEBL
 * 
 * <P>
 * Initial Date: 02.09.2011 <br>
 * 
 * @author lavinia
 */
public class HtmlXmlFileFilterEBL {

    public static final String[] INITIAL_ALLOWED_FILE_SUFFIXES = new String[] { "html", "htm", "xml", "xhtml" };

    /**
     * Accepts zip files as well.
     * 
     * @param fileName
     * @return
     */
    public boolean acceptFileName(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".zip")) {
            return true;
        }
        for (int i = 0; i < INITIAL_ALLOWED_FILE_SUFFIXES.length; i++) {
            if (fileName.endsWith("." + INITIAL_ALLOWED_FILE_SUFFIXES[i])) {
                return true;
            }
        }
        return false;
    }

}
