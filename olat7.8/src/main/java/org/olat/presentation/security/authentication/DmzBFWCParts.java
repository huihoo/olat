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
package org.olat.presentation.security.authentication;

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.layout.OlatGuestFooterController;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappControllerParts;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DmzBFWCParts
 * <P>
 * Initial Date: 29.01.2008 <br>
 * 
 * @author patrickb
 */
public class DmzBFWCParts implements BaseFullWebappControllerParts {

    private ControllerCreator contentControllerCreator;
    private boolean showTopNav = true; // default

    /**
	 */
    @Override
    public Controller createFooterController(final UserRequest ureq, final WindowControl wControl) {
        Controller footerCtr = null;
        // ----------- footer, optional (e.g. for copyright, powered by) ------------------
        if (CoreSpringFactory.containsBean("fullWebApp.DMZFooterControllerCreator")) {
            final ControllerCreator footerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.DMZFooterControllerCreator");
            footerCtr = footerCreator.createController(ureq, wControl);
        } else {
            footerCtr = new OlatGuestFooterController(ureq, wControl);
        }
        return footerCtr;
    }

    /**
	 */
    @Override
    public Controller createHeaderController(final UserRequest ureq, final WindowControl wControl) {
        Controller headerCtr = null;
        // ----------- header, optional (e.g. for logo, advertising ) ------------------
        if (CoreSpringFactory.containsBean("fullWebApp.DMZHeaderControllerCreator")) {
            final ControllerCreator headerControllerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.DMZHeaderControllerCreator");
            headerCtr = headerControllerCreator.createController(ureq, wControl);
        }
        return headerCtr;
    }

    /**
	 */
    @Override
    public Controller createTopNavController(final UserRequest ureq, final WindowControl wControl) {
        if (showTopNav) {
            Controller topNavCtr = null;
            if (CoreSpringFactory.containsBean("fullWebApp.DMZTopNavControllerCreator")) {
                final ControllerCreator headerControllerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.DMZTopNavControllerCreator");
                topNavCtr = headerControllerCreator.createController(ureq, wControl);
            }
            return topNavCtr;
        } else {
            return null;
        }
    }

    public void setContentControllerCreator(final ControllerCreator contentControllerCreator) {
        this.contentControllerCreator = contentControllerCreator;
    }

    @Override
    public Controller getContentController(final UserRequest ureq, final WindowControl wControl) {
        return contentControllerCreator.createController(ureq, wControl);
    }

    /**
	 */
    @Override
    public List<SiteInstance> getSiteInstances(final UserRequest ureq, final WindowControl wControl) {
        return null;
    }

    /**
     * Enable or disable the dmz top navigation. This is usefull to remove the lang-chooser which causes troubles in the registratoin workflow.
     * 
     * @param showTopNav
     */
    public void showTopNav(final boolean showTopNavController) {
        this.showTopNav = showTopNavController;
    }

}
