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
 * Description:<br>
 * Run controller of the institution portlet.
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
package org.olat.presentation.portal.shiblogin;

import org.olat.lms.security.authentication.shibboleth.SwitchShibbolethAuthenticationConfigurator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * uses the EmbeddedWAYF provided by SWITCH
 */
public class ShibLoginPortletRunController extends BasicController {

    private final VelocityContainer portletVC;

    protected ShibLoginPortletRunController(final UserRequest ureq, final WindowControl wControl, final SwitchShibbolethAuthenticationConfigurator config) {
        super(ureq, wControl);

        this.portletVC = createVelocityContainer("portlet");

        portletVC.contextPut("wayfSPEntityID", config.getWayfSPEntityID());
        portletVC.contextPut("wayfSPHandlerURL", config.getWayfSPHandlerURL());
        portletVC.contextPut("wayfSPSamlDSURL", config.getWayfSPSamlDSURL());
        portletVC.contextPut("wayfReturnUrl", config.getWayfReturnUrl());
        portletVC.contextPut("additionalIDPs", config.getAdditionalIdentityProviders());

        // TODO: this will throw AssertException in BasicController
        setInitialComponent(this.portletVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
