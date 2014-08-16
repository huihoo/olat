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

package org.olat.presentation.user;

import org.apache.log4j.Logger;
import org.olat.connectors.webdav.WebDAVManager;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.registration.RegistrationModule;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.authentication.ldap.LDAPLoginManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.instantmessaging.ChangeIMSettingsController;
import org.olat.presentation.registration.DisclaimerController;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 29, 2003
 * 
 * @author Sabina Jeger
 */
public class PersonalSettingsController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final TabbedPane userConfig;

    private final Controller ucsc;
    private Controller pwdc;
    private final Controller hpec;
    private Controller cimsc;
    private Controller pwdav;

    /**
     * @param ureq
     * @param wControl
     */
    public PersonalSettingsController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        if (!getBaseSecurityEBL().isChangePersonalSettingsPermitted(ureq.getIdentity())) {
            throw new OLATSecurityException("Insufficient permissions to access PersonalSettingsController");
        }

        userConfig = new TabbedPane("userConfig", ureq.getLocale());
        hpec = new ProfileAndHomePageEditController(ureq, getWindowControl(), ureq.getIdentity(), false);
        listenTo(hpec);
        userConfig.addTab(translate("tab.profile"), hpec.getInitialComponent());

        ucsc = new ChangePrefsController(ureq, getWindowControl(), ureq.getIdentity());
        listenTo(ucsc);
        userConfig.addTab(translate("tab.prefs"), ucsc.getInitialComponent());
        LDAPLoginManager ldapLoginManager = CoreSpringFactory.getBean(LDAPLoginManager.class);
        if (ldapLoginManager.canChangePassword(getIdentity())) {
            pwdc = new ChangePasswordController(ureq, getWindowControl());
            listenTo(pwdc);
            userConfig.addTab(translate("tab.pwd"), pwdc.getInitialComponent());
        }

        if (WebDAVManager.getInstance().isEnabled()) {
            pwdav = new WebDAVPasswordController(ureq, getWindowControl());
            userConfig.addTab(translate("tab.pwdav"), pwdav.getInitialComponent());
        }

        if (InstantMessagingModule.isEnabled()) {
            cimsc = new ChangeIMSettingsController(ureq, getWindowControl(), ureq.getIdentity());
            listenTo(cimsc);
            userConfig.addTab(translate("tab.im"), cimsc.getInitialComponent());
        }

        // Show read only display of disclaimer so user sees what he accepted if disclaimer enabled
        if (RegistrationModule.isDisclaimerEnabled()) {
            final Controller disclaimerCtr = new DisclaimerController(ureq, getWindowControl(), true);
            listenTo(disclaimerCtr);
            userConfig.addTab(translate("tab.disclaimer"), disclaimerCtr.getInitialComponent());
        }

        putInitialPanel(userConfig);

        log.debug("PersonalSettingsController constructed, set velocity page to index.html");
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do here.
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // nothing to be done
    }

    @Override
    protected void doDispose() {
        //
    }
}
