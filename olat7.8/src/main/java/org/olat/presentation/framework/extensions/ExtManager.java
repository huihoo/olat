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

package org.olat.presentation.framework.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Initial Date: 02.08.2005 <br>
 * 
 * @author Felix
 * @author guido
 */
@Component
public class ExtManager implements Initializable {
    private static final Logger log = LoggerHelper.getLogger();

    private long timeOfExtensionStartup;
    private List<Extension> extensions;
    @Autowired
    private Map<String, Extension> extensionMap;

    /**
     * [used by spring]
     */
    protected ExtManager() {
        // for spring framework and..
        timeOfExtensionStartup = System.currentTimeMillis();
    }

    /**
     * @return the number of extensions
     */
    public int getExtensionCnt() {
        return (getExtensions() == null ? 0 : extensions.size());
    }

    /**
     * @param i
     * @return the extension at pos i
     */
    public Extension getExtension(int i) {
        return getExtensions().get(i);
    }

    /**
     * [used by spring]
     * 
     * @return list
     */
    public List<Extension> getExtensions() {
        return extensions;
    }

    /**
     * @return the time when the extmanager was initialized
     */
    public long getTimeOfExtensionStartup() {
        return timeOfExtensionStartup;
    }

    /**
     * @param extensionPoint
     * @param anExt
     * @param addInfo
     *            additional info to log
     */
    public void inform(Class extensionPoint, Extension anExt, String addInfo) {
        // log.info(this.getClass());
    }

    @Override
    @PostConstruct
    public void init() {
        extensions = new ArrayList<Extension>();
        Map<Integer, Extension> sortedMap = new TreeMap<Integer, Extension>();
        Collection<Extension> extensionValues = extensionMap.values();
        // first build ordered list
        for (Object object : extensionValues) {
            Extension extension = (Extension) object;
            log.debug("initExtentions extention=" + extension);
            int key = extension.getOrder();
            while (sortedMap.containsKey(key)) {
                // a key with this value already exist => add 1000 because offset must be outside of other values.
                key += 1000;
            }
            if (key != extension.getOrder()) {
                log.warn("Extension-Configuration Problem: Dublicate order-value (" + extension.getOrder() + ") for extension=" + extension.getClass()
                        + ", append extension at the end");
            }
            sortedMap.put(key, extension);
            log.debug("extension is enabled => add to list of extentions = " + extension);
        }
        for (Object key : sortedMap.keySet()) {
            extensions.add(sortedMap.get(key));
        }
    }

}
