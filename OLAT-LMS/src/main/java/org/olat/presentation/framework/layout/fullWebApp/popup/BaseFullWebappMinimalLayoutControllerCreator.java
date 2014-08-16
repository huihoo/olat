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

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappControllerParts;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The minimal layout controller creator uses the spring configuration to decide what header, top nav and footer component should be uses in the minmal layout (usually
 * used for popup windows)
 * <P>
 * Initial Date: 15.02.2008 <br>
 * 
 * @author patrickb
 */
class BaseFullWebappMinimalLayoutControllerCreator implements BaseFullWebappPopupLayout {
    private ControllerCreator contentControllerCreator;

    BaseFullWebappMinimalLayoutControllerCreator(ControllerCreator contentControllerCreator) {
        this.contentControllerCreator = contentControllerCreator;
    }

    /**
	 */
    @Override
    public BaseFullWebappControllerParts getFullWebappParts() {
        return new BaseFullWebappControllerParts() {

            @Override
            public List<SiteInstance> getSiteInstances(UserRequest ureq, WindowControl control) {
                // no static sites
                return null;
            }

            @Override
            public Controller getContentController(UserRequest ureq, WindowControl wControl) {
                // the content for the Pop-up Window
                return contentControllerCreator.createController(ureq, wControl);
            }

            @Override
            public Controller createTopNavController(UserRequest ureq, WindowControl wControl) {
                Controller topnavCtr = null;
                // ----------- topnav, optional (e.g. for imprint, logout) ------------------
                if (CoreSpringFactory.containsBean("fullWebApp.MinimalTopNavControllerCreator")) {
                    ControllerCreator topnavControllerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.MinimalTopNavControllerCreator");
                    topnavCtr = topnavControllerCreator.createController(ureq, wControl);
                }
                return topnavCtr;
            }

            @Override
            public Controller createHeaderController(UserRequest ureq, WindowControl control) {
                Controller headerCtr = null;
                // ----------- header, optional (e.g. for logo, advertising ) ------------------
                if (CoreSpringFactory.containsBean("fullWebApp.MinimalHeaderControllerCreator")) {
                    ControllerCreator headerControllerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.MinimalHeaderControllerCreator");
                    headerCtr = headerControllerCreator.createController(ureq, control);
                }
                return headerCtr;
            }

            @Override
            public Controller createFooterController(UserRequest ureq, WindowControl control) {
                Controller footerCtr = null;
                // ----------- footer, optional (e.g. for copyright, powerd by) ------------------
                if (CoreSpringFactory.containsBean("fullWebApp.MinimalFooterControllerCreator")) {
                    ControllerCreator footerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.MinimalFooterControllerCreator");
                    footerCtr = footerCreator.createController(ureq, control);
                }
                return footerCtr;
            }
        };
    }

    /**
	 */
    @Override
    public Controller createController(UserRequest lureq, WindowControl lwControl) {
        // content without topnav etc.
        return contentControllerCreator.createController(lureq, lwControl);
    }

}
