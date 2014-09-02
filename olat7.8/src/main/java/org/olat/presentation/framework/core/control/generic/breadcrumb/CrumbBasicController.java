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

package org.olat.presentation.framework.core.control.generic.breadcrumb;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;

/**
 * <h3>Description:</h3> This abstract class serves as a base for controllers that can be used in a bread crumb path navigation controller. It provides methods to
 * activate and deactivate child controllers.
 * <p>
 * Initial Date: 09.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public abstract class CrumbBasicController extends BasicController implements CrumbController {
    private BreadCrumbController breadCrumbCtr;
    private CrumbController childCrumbCtr;

    /**
     * Constructor inherited from basic controller
     * 
     * @param ureq
     * @param control
     */
    protected CrumbBasicController(UserRequest ureq, WindowControl control) {
        super(ureq, control);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public abstract String getCrumbLinkText();

    /*
     * (non-Javadoc)
     */
    @Override
    public abstract String getCrumbLinkHooverText();

    /*
     * (non-Javadoc)
     */
    @Override
    public CrumbController getChildCrumbController() {
        return childCrumbCtr;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void setBreadCrumbController(BreadCrumbController breadCrumbCtr) {
        this.breadCrumbCtr = breadCrumbCtr;
    }

    /*
     * (non-Javadoc)
     * org.olat.presentation.framework.control.generic.breadcrumb.CrumbController#activateAndListenToChildCrumbController(org.olat.presentation.framework.control
     * .generic.breadcrumb.CrumbController)
     */
    @Override
    public void activateAndListenToChildCrumbController(CrumbController childCrumbController) {
        // remove old one
        deactivateAndDisposeChildCrumbController();
        childCrumbCtr = childCrumbController;
        listenTo(childCrumbCtr); // auto cleanup
        childCrumbCtr.setBreadCrumbController(this.breadCrumbCtr);
        breadCrumbCtr.putToBreadCrumbStack(childCrumbCtr);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void deactivateAndDisposeChildCrumbController() {
        if (childCrumbCtr != null) {
            childCrumbCtr.deactivateAndDisposeChildCrumbController();
            removeAsListenerAndDispose(childCrumbCtr);
            childCrumbCtr = null;
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void removeFromBreadCrumbPathAndDispose() {
        this.breadCrumbCtr.removeFromBreadCrumb(this);
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void resetCrumbTexts() {
        this.breadCrumbCtr.resetCrumbTexts();
    }

}
