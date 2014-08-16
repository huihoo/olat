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

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> This is a simple controller that displays no header at all. This is needed to override the default header from the brasato configuration that
 * displays a logo.
 * <p>
 * Use the ControllerCreator to configure this header in the spring configuration
 * <p>
 * Initial Date: 24.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class EmptyHeaderController extends BasicController {

    /**
     * Constructor for an empty header creator that displays a configured logo
     * 
     * @param ureq
     * @param wControl
     */
    public EmptyHeaderController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        // use an empty panel as content
        putInitialPanel(null);
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // TODO Auto-generated method stub

    }
}
