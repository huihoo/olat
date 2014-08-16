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
package org.olat.presentation.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * generic settings related to the gui framework. see also coreconfig.xml for comments
 * <P>
 * Initial Date: 04.01.2007 <br>
 * 
 * @author Felix Jost
 */
public class AjaxSettings {

    private static final Logger log = LoggerHelper.getLogger();

    private static List<Pattern> ajaxBlacklistPatterns = new ArrayList<Pattern>();

    /**
     * [used by spring]
     */
    AjaxSettings() {
    }

    /**
     * Checks against a list of browser defined in brasatoconfig.xml whether the browser is on the AJAX blacklist.
     * <p>
     * Note that this configuration changed in OLAT 7.1. In previous releases OLAT used a whitelist mechanism which is now converted into a blacklist.
     * 
     * @param ureq
     * @return true: user agent is blacklistet for AJAX mode; false: user agent can use AJAX mode
     */
    public static boolean isBrowserAjaxBlacklisted(UserRequest ureq) {
        String uag = ureq.getHttpReq().getHeader("user-agent");
        if (uag == null)
            return false;
        for (Pattern agentPattern : ajaxBlacklistPatterns) {
            if (agentPattern.matcher(uag).matches()) {
                // This browser is on the web 1.0 mode list, not AJAX certified
                return true;
            }
        }
        // Passed all patterns, is certified
        return false;
    }

    /**
     * Set the list of regular expressions that represent user agents that are not allowed to use the ajax mode.
     * <p>
     * Note that this method is not thread save. The intention is to set this list only once at system startup by spring. After that values can only be red. [spring]
     * 
     * @param userAgents
     */
    public void setAjaxBlacklistedUserAgents(List<String> userAgents) {
        // Use CopyOnWriteArrayList instead of the ArrayList if you make a GUI
        // that allows changing of the list values
        for (String regexp : userAgents) {
            try {
                Pattern pattern = Pattern.compile(regexp);
                ajaxBlacklistPatterns.add(pattern);
            } catch (PatternSyntaxException e) {
                log.error("Ignoring invalid ajax blacklist user agent::" + regexp + " Please fix your brasatoconfig.xml", e);
            }
        }
    }

}
