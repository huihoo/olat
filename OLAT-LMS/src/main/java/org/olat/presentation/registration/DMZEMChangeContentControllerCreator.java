/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.presentation.registration;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappController;
import org.olat.presentation.security.authentication.DmzBFWCParts;

/**
 * Description:<br>
 * TODO: bja Class Description for DMZEMChangeContentControllerCreator
 * <P>
 * Initial Date: 21.11.2008 <br>
 * 
 * @author bja
 */
public class DMZEMChangeContentControllerCreator implements ControllerCreator {

    private ControllerCreator controllerCreator;

    @Override
    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
        final DmzBFWCParts dmzSitesAndNav = new DmzBFWCParts();
        dmzSitesAndNav.showTopNav(false);
        dmzSitesAndNav.setContentControllerCreator(controllerCreator);
        return new BaseFullWebappController(lureq, lwControl, dmzSitesAndNav);
    }

    public void setControllerCreator(ControllerCreator controllerCreator) {
        this.controllerCreator = controllerCreator;
    }

}
