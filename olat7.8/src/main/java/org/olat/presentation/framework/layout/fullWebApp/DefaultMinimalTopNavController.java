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
package org.olat.presentation.framework.layout.fullWebApp;

import org.olat.lms.commons.mediaresource.RedirectMediaResource;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.dispatcher.StaticMediaDispatcher;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * The default minimal top navigation offers a close and print link
 * <P>
 * Initial Date: 24.09.2009 <br>
 * 
 * @author Florian Gnägi
 */
public class DefaultMinimalTopNavController extends BasicController {
    private VelocityContainer topNavVC;
    private Link closeLink;

    /**
     * Default constructor
     * 
     * @param ureq
     * @param wControl
     */
    public DefaultMinimalTopNavController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        topNavVC = createVelocityContainer("defaulttopnavminimal");
        closeLink = LinkFactory.createLink("header.topnav.close", topNavVC, this);
        putInitialPanel(topNavVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == closeLink) {
            // close window (a html page which calls Window.close onLoad
            ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(StaticMediaDispatcher.createStaticURIFor("closewindow.html")));
            // release all resources and close window
            WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
            Window w = wbo.getWindow();
            Windows.getWindows(ureq).deregisterWindow(w);
            wbo.dispose();
        }
    }

}
