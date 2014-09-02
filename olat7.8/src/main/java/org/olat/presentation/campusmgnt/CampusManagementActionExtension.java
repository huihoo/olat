package org.olat.presentation.campusmgnt;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.extensions.action.GenericActionExtension;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

public class CampusManagementActionExtension extends GenericActionExtension {

    @Override
    public Controller createController(final UserRequest ureq, final WindowControl wControl, final Object arg) {
        if (arg instanceof OLATResourceable) {
            return new CampusManagementController(ureq, wControl, (OLATResourceable) arg);
        } else {
            throw new AssertException("CampusManagementActionExtension needs a OLATResourceable as the argument parameter: arg = " + arg);
        }

    }

}
