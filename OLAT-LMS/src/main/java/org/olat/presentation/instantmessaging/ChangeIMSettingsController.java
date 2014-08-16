/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.instantmessaging;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.instantmessaging.ClientManager;
import org.olat.lms.instantmessaging.ImPreferences;
import org.olat.lms.instantmessaging.ImPrefsManager;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: August 08, 2005
 * 
 * @author Alexander Schneider
 */

public class ChangeIMSettingsController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer myContent;
    private Identity changeableIdentity;
    private final OnlineListForm onlineListForm;
    private final RosterForm rosterForm;
    private final ImPrefsManager ipm;

    /**
     * Constructor for the change instant messaging controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The current window controller
     * @param changeableIdentity
     */
    public ChangeIMSettingsController(final UserRequest ureq, final WindowControl wControl, final Identity changeableIdentity) {
        super(ureq, wControl);

        this.changeableIdentity = changeableIdentity;

        myContent = createVelocityContainer("imsettings");

        ipm = ImPrefsManager.getInstance();
        final ImPreferences imPrefs = ipm.loadOrCreatePropertiesFor(changeableIdentity);

        onlineListForm = new OnlineListForm(ureq, wControl, imPrefs);
        listenTo(onlineListForm);
        myContent.put("onlinelistform", onlineListForm.getInitialComponent());

        rosterForm = new RosterForm(ureq, wControl, imPrefs);
        listenTo(rosterForm);
        myContent.put("rosterform", rosterForm.getInitialComponent());

        myContent.contextPut("chatusername", InstantMessagingModule.getAdapter().getUserJid(changeableIdentity.getName()));
        final Authentication auth = getBaseSecurity().findAuthentication(changeableIdentity, ClientManager.PROVIDER_INSTANT_MESSAGING);
        if (auth == null) {
            // somehow this is a messed up user. happens sometimes with the default users when IM server is not running at first startup
            log.error("Could not find authentication for identity::" + changeableIdentity.getName() + " and provider::" + ClientManager.PROVIDER_INSTANT_MESSAGING
                    + "; Please fix this users Instant Messaging password manually", null);
        } else {
            myContent.contextPut("password", auth.getCredential());
        }

        putInitialPanel(myContent);
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == onlineListForm) {
            if (event == Event.DONE_EVENT) {
                changeableIdentity = getBaseSecurity().loadIdentityByKey(changeableIdentity.getKey());
                final ImPreferences imPrefs = ipm.loadOrCreatePropertiesFor(changeableIdentity);
                onlineListForm.updateImPreferencesFromFormData(imPrefs);
                ipm.updatePropertiesFor(changeableIdentity, imPrefs);
                getUserService().updateUserFromIdentity(changeableIdentity);

                fireEvent(ureq, Event.DONE_EVENT);
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (source == rosterForm) {
            if (event == Event.DONE_EVENT) {
                final ImPreferences imPrefs = ipm.loadOrCreatePropertiesFor(changeableIdentity);
                rosterForm.updateImPreferencesFromFormData(imPrefs);
                ipm.updatePropertiesFor(changeableIdentity, imPrefs);
                fireEvent(ureq, Event.DONE_EVENT);
            } else if (event == Event.CANCELLED_EVENT) {
                // Form is cancelled
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
