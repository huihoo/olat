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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.control.generic.portal;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Factory to create instances of portlets defined in the WEB-INF/olat_portals.xml
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 */
public class PortletFactory {
    private static final Logger log = LoggerHelper.getLogger();

    private Map<String, Portlet> enabledPortlets;
    @Autowired
    private Map<String, Portlet> portletMap;
    private Object lockObject = new Object();

    /**
     * Singleton
     */
    private PortletFactory() {
        // singleton
    }

    public Map<String, Portlet> getPortlets() {
        if (enabledPortlets == null) {
            synchronized (lockObject) {
                if (enabledPortlets == null) { // check again in synchronized-block, only one may create list
                    initPortlets();
                }
            }
        }
        return enabledPortlets;
    }

    private void initPortlets() {
        enabledPortlets = new HashMap<String, Portlet>();
        for (Portlet portlet : portletMap.values()) {
            log.debug("initPortlets portlet=" + portlet);
            if (portlet.isEnabled()) {
                enabledPortlets.put(portlet.getName(), portlet);
                log.debug("portlet is enabled => add to list portlet=" + portlet);
            }
        }
    }

    /**
     * Factory method to create a portled wrapped in a portlet container.
     * 
     * @param defaultConfiguration
     *            The default configuration map
     * @param wControl
     * @param ureq
     * @return The portlet container that contains the portlet
     */
    public static PortletContainer getPortletContainerFor(Portlet portlet, WindowControl wControl, UserRequest ureq) {
        return new PortletContainer(wControl, ureq, portlet.createInstance(wControl, ureq, portlet.getConfiguration()));
    }

    /**
     * @param beanName
     *            : The bean name to check for
     * @return true if such a bean does exist in the config, false         otherwhise
     */
    public boolean containsPortlet(String beanName) {
        return getPortlets().containsKey(beanName);
    }
}
