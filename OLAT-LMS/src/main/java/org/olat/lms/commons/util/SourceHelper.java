/**
 * This software is based on OLAT, www.olat.org
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
 * Copyright (c) JLS goodsolutions GmbH, Zurich, Switzerland. http://www.goodsolutions.ch <br>
 * All rights reserved.
 * <p>
 */
package org.olat.lms.commons.util;

import java.io.File;

import org.apache.log4j.Logger;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.system.commons.Settings;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Helper class to get source-path.
 * 
 * @author Christian Guretzki
 */
public class SourceHelper {

    private static final Logger log = LoggerHelper.getLogger();

    private static String fullPathToSrc;

    /**
     * needed only for development; with debug mode enabled. The returned path does never end with a slash
     * 
     * @return the absolute path to the application webapp source directory, e.g. /opt/olat3/webapp/WEB-INF/src" (no trailing slash)
     */

    protected SourceHelper() {
    }

    public static String getSourcePath() {
        // String srcPath = getContextRoot() + "/" + relPathToSrc;
        File fil = new File(fullPathToSrc);
        if (fil.exists()) {
            log.info("Path to source set to: " + fullPathToSrc);
        } else {
            if (Settings.isDebuging() || I18nModule.isTransToolEnabled()) {
                log.error("Path to source wrong, debugging may not work as expected: " + fullPathToSrc, new Exception("getSourcePath"));
            } else {
                log.info("Path to source not valid: " + fullPathToSrc);
            }
        }

        return fullPathToSrc;
    }

    /**
     * [spring]
     * 
     * @param fullPathToSrc
     */
    public void setFullPathToSrc(String fullPathToSrc) {
        SourceHelper.fullPathToSrc = fullPathToSrc;
    }

}
