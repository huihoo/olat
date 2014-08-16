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
package org.olat.presentation.group.run;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DisposedBusinessGroup
 * <P>
 * Initial Date: 28.04.2008 <br>
 * 
 * @author patrickb
 */
class DisposedBusinessGroup extends BasicController {

    protected DisposedBusinessGroup(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        final VelocityContainer initialContent = createVelocityContainer("disposedgroup");
        putInitialPanel(initialContent);
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
        // TODO Auto-generated method stub

    }

}
