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
package org.olat.presentation.admin.layout;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 28.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class LayoutAdminEBL {

    private static final Logger log = LoggerHelper.getLogger();

    public String[] getThemes() {
        final String staticAbsPath = WebappHelper.getContextRoot() + "/static/themes";
        final File themesDir = new File(staticAbsPath);
        if (!themesDir.exists()) {
            log.warn("Themes dir not found: " + staticAbsPath);
        }
        final File[] themes = themesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                if (!new File(dir, name).isDirectory()) {
                    return false;
                }
                if (name.equalsIgnoreCase("CVS")) {
                    return false;
                } else if (name.equalsIgnoreCase(".DS_Store")) {
                    return false;
                } else {
                    return true;
                }
            }
        });

        final String[] themesStr = new String[themes.length];
        for (int i = 0; i < themes.length; i++) {
            final File theme = themes[i];
            themesStr[i] = theme.getName();
        }
        return themesStr;
    }

}
