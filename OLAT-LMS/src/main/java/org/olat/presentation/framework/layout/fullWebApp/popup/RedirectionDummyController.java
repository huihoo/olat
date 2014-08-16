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
package org.olat.presentation.framework.layout.fullWebApp.popup;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * A dummy controller to create dummy content with a redirect to an URL, used for within a pop up window.
 * <P>
 * Initial Date: 15.05.2009 <br>
 * 
 * @author patrickb
 */
class RedirectionDummyController extends BasicController {

    protected RedirectionDummyController(UserRequest ureq, WindowControl control, String url) {
        super(ureq, control);
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertException("invalid URL " + url);
        }
        VelocityContainer vc = createVelocityContainer("redirect");
        vc.contextPut("url", url);
        putInitialPanel(vc);

    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        //
    }

}
