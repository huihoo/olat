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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.framework.layout.fullWebApp;

import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.dispatcher.ClassPathStaticDispatcher;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> This is a simple controller that displays the brasato web app framework default footer.
 * <p>
 * Initial Date: 10.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DefaultFooterController extends BasicController {
    private VelocityContainer footerVC;

    /**
     * Constructor: usedd with AutoCreator
     */
    public DefaultFooterController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        // Initialize velocity container
        footerVC = createVelocityContainer("defaultfooter");

        String ressourceMapperUri = ClassPathStaticDispatcher.getInstance().getMapperBasePath(this.getClass());
        footerVC.contextPut("ressourceMapperUri", ressourceMapperUri);
        footerVC.contextPut("olatversion", Settings.getFullVersionInfo() + " " + Settings.getNodeInfo());

        // Push information about AJAX mode
        boolean ajaxOn = false;
        if (ureq.getUserSession().isAuthenticated()) {
            ajaxOn = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
        } else {
            // on construction time only global and browserdependent ajax on
            // settings can be used
            // to show ajax gif :-)
            ajaxOn = Settings.isAjaxGloballyOn();
        }
        footerVC.contextPut("ajaxOn", ajaxOn ? Boolean.TRUE : Boolean.FALSE);

        // Push information about logged in users
        footerVC.contextPut("userSessionsCnt", UserSession.getUserSessionsCnt());

        // Push information about user
        if (ureq.getUserSession().isAuthenticated()) {
            footerVC.contextPut("loggedIn", Boolean.TRUE);
            footerVC.contextPut("username", ureq.getIdentity().getName());
        } else {
            footerVC.contextPut("loggedIn", Boolean.FALSE);
        }

        putInitialPanel(footerVC);
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // no events to catch
    }
}
