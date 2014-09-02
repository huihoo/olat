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

package org.olat.lms.registration;

import org.apache.log4j.Logger;
import org.olat.presentation.registration.UserNameCreationInterceptor;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: May 4, 2004
 * 
 * @author gnaegi <a href="http://www.frentix.com">frentix GmbH</a>
 * @author guido
 *         <p>
 *         Comment:
 *         <p>
 *         The registration module deals with system registration and the disclaimer that has to be accepted by users when entering the system the first time.
 */
public class RegistrationModule extends AbstractOLATModule {

    private static final Logger log = LoggerHelper.getLogger();
    public static final String REGISTRATION = "REGISTRATION";
    // registration config
    private static final String CONFIG_SELFREGISTRATION = "enableSelfregistration";

    private static boolean selfRegistrationEnabled;

    // disclaimer config
    private static final String CONFIG_DISCLAIMER = "disclaimerEnabled";
    private static final String CONFIG_ADDITIONAL_CHECKBOX = "disclaimerAdditionalCheckbox";
    private static final String CONFIG_ADDITIONAL_LINK = "disclaimerAdditionaLinkText";
    private static boolean disclaimerEnabled;
    private static boolean additionalCheckbox;
    private static boolean additionaLinkText;

    private static UserNameCreationInterceptor usernamePresetBean;

    /**
     * [used by spring]
     */
    private RegistrationModule() {
        //
    }

    public static UserNameCreationInterceptor getUsernamePresetBean() {
        return RegistrationModule.usernamePresetBean;
    }

    public void setUsernamePresetBean(final UserNameCreationInterceptor usernamePresetBean) {
        RegistrationModule.usernamePresetBean = usernamePresetBean;
    }

    /**
     * @return true if self registration is turned on, false otherwhise
     */
    public static boolean isSelfRegistrationEnabled() {
        return selfRegistrationEnabled;
    }

    /**
     * @return true to force acceptance of disclaimer on first login; true to skip disclaimer
     */
    public static boolean isDisclaimerEnabled() {
        return disclaimerEnabled;
    }

    /**
     * @return true to add a second checkbox to the disclaimer
     */
    public static boolean isDisclaimerAdditionalCheckbox() {
        return additionalCheckbox;
    }

    /**
     * @return true to add a link to the disclaimer
     */
    public static boolean isDisclaimerAdditionaLinkText() {
        return additionaLinkText;
    }

    @Override
    public void initialize() {
        // Nothing to initialize
    }

    @Override
    protected void initDefaultProperties() {

        selfRegistrationEnabled = getBooleanConfigParameter(CONFIG_SELFREGISTRATION, false);
        if (selfRegistrationEnabled) {
            log.info("Selfregistration is turned ON");
        } else {
            log.info("Selfregistration is turned OFF");
        }

        // disclaimer configuration
        disclaimerEnabled = getBooleanConfigParameter(CONFIG_DISCLAIMER, false);
        if (disclaimerEnabled) {
            log.info("Disclaimer is turned ON");
        } else {
            log.info("Disclaimer is turned OFF");
        }
        // optional disclaimer elements
        additionalCheckbox = getBooleanConfigParameter(CONFIG_ADDITIONAL_CHECKBOX, false);
        additionaLinkText = getBooleanConfigParameter(CONFIG_ADDITIONAL_LINK, false);
    }

    @Override
    protected void initFromChangedProperties() {
        // Nothing to init
    }

}
