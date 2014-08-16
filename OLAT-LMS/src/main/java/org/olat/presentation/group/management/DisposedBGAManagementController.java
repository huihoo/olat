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
package org.olat.presentation.group.management;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DisposedBGAManagementController
 * <P>
 * Initial Date: 27.04.2008 <br>
 * 
 * @author patrickb
 */
class DisposedBGAManagementController extends BasicController {

    private final Link closeLink;
    private final BGManagementController managementController;

    protected DisposedBGAManagementController(final UserRequest ureq, final WindowControl control, final BGManagementController managementController) {
        super(ureq, control);
        final VelocityContainer initialContent = createVelocityContainer("disposedbgmanagement");
        closeLink = LinkFactory.createButton("bgmanagement.disposed.command.close", initialContent, this);
        putInitialPanel(initialContent);
        this.managementController = managementController;
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
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == closeLink) {
            // lock is already released in doDispose() of managementController
            // the way to remove controller correctly is to send the DoneEvent, but
            // source of DoneEvent must be the disposed BGManagementController Instance
            //
            // this way of coupling is only 'allowed' for Controller and its
            // DisposedController !!
            managementController.fireDoneEvent(ureq);
            dispose();
        }

    }

}
