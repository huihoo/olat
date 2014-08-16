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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.connectors.rest;

import org.apache.log4j.Logger;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Configuration of the REST API
 * <P>
 * Initial Date: 18 juin 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestModule extends AbstractOLATModule {
    private static final String KEY_REST_ENABLED = "rest.enabled";
    private static final Logger log = LoggerHelper.getLogger();
    private Boolean enabled = false;

    /**
     * [spring]
     */
    private RestModule() {
        //
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        log.info("setEnabled=" + Boolean.toString(enabled));
        if (this.enabled != enabled) {
            setStringProperty(KEY_REST_ENABLED, Boolean.toString(enabled), true);
            log.info("setStringProperty " + KEY_REST_ENABLED + "=" + Boolean.toString(enabled));
            this.enabled = enabled;
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

    @Override
    public void initialize() {
        final String enabledObj = getStringPropertyValue(KEY_REST_ENABLED, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            enabled = "true".equals(enabledObj);
        }
        log.info("REST is enabled: " + Boolean.toString(enabled));
    }

    @Override
    protected void initDefaultProperties() {
        enabled = getBooleanConfigParameter(KEY_REST_ENABLED, true);
    }

    @Override
    protected void initFromChangedProperties() {
        init();
    }

}
