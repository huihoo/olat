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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.control.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Sites
 * <P>
 * Initial Date: 12.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class SiteDefinitions {
    private static final Logger log = LoggerHelper.getLogger();

    private List<SiteDefinition> enabledSiteDefList;
    @Autowired
    private Map<String, SiteDefinition> siteDefMap;
    private Object lockObject = new Object();

    /**
	 * 
	 */
    protected SiteDefinitions() {
        // Does NOT call initSiteDefinitionList() here because we are not sure if all SiteDef-beans are loaded !
        // and we won't to define spring depends-on
    }

    private void initSiteDefinitionList() {
        enabledSiteDefList = new ArrayList<SiteDefinition>();
        Map sortedMap = new TreeMap();
        for (SiteDefinition siteDefinition : siteDefMap.values()) {
            if (siteDefinition.isEnabled()) {
                int key = siteDefinition.getOrder();
                while (sortedMap.containsKey(key)) {
                    // a key with this value already exist => add 1000 because offset must be outside of other values.
                    key += 1000;
                }
                if (key != siteDefinition.getOrder()) {
                    log.warn("SiteDefinition-Configuration Problem: Dublicate order-value for siteDefinition=" + siteDefinition + ", append siteDefinition at the end");
                }
                sortedMap.put(key, siteDefinition);
            } else {
                log.debug("Disabled siteDefinition=" + siteDefinition);
            }
        }

        for (Object key : sortedMap.keySet()) {
            enabledSiteDefList.add((SiteDefinition) sortedMap.get(key));
        }
    }

    public List<SiteDefinition> getSiteDefList() {
        if (enabledSiteDefList == null) { // first try non-synchronized for better performance
            synchronized (lockObject) {
                if (enabledSiteDefList == null) { // check again in synchronized-block, only one may create list
                    initSiteDefinitionList();
                }
            }

        }
        return new ArrayList<SiteDefinition>(enabledSiteDefList);
    }
}
