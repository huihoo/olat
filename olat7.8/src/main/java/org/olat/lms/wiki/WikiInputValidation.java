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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Initial Date: 15.11.2013 <br>
 * 
 * @author lavinia
 */
public class WikiInputValidation {

    // Latin letters, digits, and white space characters
    private static String pageNameRegExp = "(^[a-zA-Z0-9\\s\\xc0-\\xff]*$)";

    public static boolean validatePageName(String inputPageName) {
        Pattern pattern = Pattern.compile(pageNameRegExp);
        Matcher matcher = pattern.matcher(inputPageName);
        return matcher.matches();
    }
}
