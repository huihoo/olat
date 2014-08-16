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
package org.olat.lms.commons;

import java.util.Date;

import org.olat.system.commons.StringHelper;

/**
 * TODO: Class Description for ModuleConfigurationEBL
 * 
 * <P>
 * Initial Date: 02.09.2011 <br>
 * 
 * @author lavinia
 */
public class ModuleConfigurationEBL {

    private static final String CONFIG_START_DATE = "startDate";
    private static final String CONFIG_AUTO_DATE = "autoDate";
    private static final String CONFIG_AUTO_SUBSCRIBE = "autoSubscribe";

    public static Date getStartDate(final ModuleConfiguration config) {
        final String timeStr = config.getStringValue(CONFIG_START_DATE);
        if (StringHelper.containsNonWhitespace(timeStr)) {
            try {
                final Long time = Long.parseLong(timeStr);
                return new Date(time);
            } catch (final Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean getAutoDate(final ModuleConfiguration config) {
        final String autoStr = config.getStringValue(CONFIG_AUTO_DATE);
        if (StringHelper.containsNonWhitespace(autoStr)) {
            return new Boolean(autoStr);
        }
        return Boolean.FALSE;
    }

    public static void setStartDate(final ModuleConfiguration config, final Date startDate) {
        if (startDate == null) {
            config.setStringValue(CONFIG_START_DATE, "");
        } else {
            final String timeStr = String.valueOf(startDate.getTime());
            config.setStringValue(CONFIG_START_DATE, timeStr);
        }
    }

    public static void setAutoDate(final ModuleConfiguration config, final boolean autoDate) {
        config.setStringValue(CONFIG_AUTO_DATE, Boolean.toString(autoDate));
    }

    public static boolean getAutoSubscribe(final ModuleConfiguration config) {
        final String autoStr = config.getStringValue(CONFIG_AUTO_SUBSCRIBE);
        if (StringHelper.containsNonWhitespace(autoStr)) {
            return new Boolean(autoStr);
        }
        return Boolean.FALSE;
    }

    public static void setAutoSubscribe(final ModuleConfiguration config, final boolean subscribe) {
        config.setStringValue(CONFIG_AUTO_SUBSCRIBE, Boolean.toString(subscribe));
    }

}
