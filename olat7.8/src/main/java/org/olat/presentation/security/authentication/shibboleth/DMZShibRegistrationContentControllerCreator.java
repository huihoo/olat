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
package org.olat.presentation.security.authentication.shibboleth;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappController;
import org.olat.presentation.security.authentication.DmzBFWCParts;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DMZContentControllerCreator
 * <P>
 * Initial Date: 29.01.2008 <br>
 * 
 * @author patrickb
 */
public class DMZShibRegistrationContentControllerCreator implements ControllerCreator {

    private ControllerCreator controllerCreator;

    /**
	 */
    @Override
    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
        final DmzBFWCParts dmzSitesAndNav = new DmzBFWCParts();
        dmzSitesAndNav.setContentControllerCreator(controllerCreator);
        return new BaseFullWebappController(lureq, lwControl, dmzSitesAndNav);
    }

    public void setControllerCreator(ControllerCreator controllerCreator) {
        this.controllerCreator = controllerCreator;
    }

}
