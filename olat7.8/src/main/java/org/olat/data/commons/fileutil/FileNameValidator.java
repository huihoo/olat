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
package org.olat.data.commons.fileutil;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * Initial Date: 26.10.2011 <br>
 * 
 * @author guretzki
 */
public class FileNameValidator {
    // windows: invalid characters for filenames: \ / : * ? " < > |
    // linux: invalid characters for file/folder names: /, but you have to escape certain chars, like ";$%&*"
    // OLAT reserved char: ":"
    private static char[] FILE_NAME_FORBIDDEN_CHARS = { '/', '\n', '\r', '\t', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
    // private static char[] FILE_NAME_ACCEPTED_CHARS = { 'ä', 'Ä', 'ü', 'Ü', 'ö', 'Ö', ' '};
    private static char[] FILE_NAME_ACCEPTED_CHARS = { '\u0228', '\u0196', '\u0252', '\u0220', '\u0246', '\u0214', ' ' };

    /**
     * Simple check for filename validity. It compares each character if it is accepted, forbidden or in a certain (Latin-1) range.
     * <p>
     * Characters < 33 --> control characters and space Characters > 255 --> above ASCII http://www.danshort.com/ASCIImap/ TODO: control chars from 127 - 157 should also
     * not be accepted TODO: how about non ascii chars in filenames, they should also work! See: OLAT-5704
     * 
     * @param filename
     * @return true if filename valid
     */
    public static boolean validate(String filename) {
        if (filename == null) {
            return false;
        }
        Arrays.sort(FILE_NAME_FORBIDDEN_CHARS);
        Arrays.sort(FILE_NAME_ACCEPTED_CHARS);

        for (int i = 0; i < filename.length(); i++) {
            char character = filename.charAt(i);
            if (Arrays.binarySearch(FILE_NAME_ACCEPTED_CHARS, character) >= 0) {
                continue;
            } else if (character < 33 || character > 255 || Arrays.binarySearch(FILE_NAME_FORBIDDEN_CHARS, character) >= 0) {
                return false;
            }
        }
        // Remove the last three positions with ...
        // And check if there are any unwanted path denominators in the name
        if (StringUtils.remove(filename, "...").indexOf("..") > -1) {
            return false;
        }
        return true;
    }

}
