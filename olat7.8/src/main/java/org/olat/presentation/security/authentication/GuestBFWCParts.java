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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.navigation.SiteDefinition;
import org.olat.presentation.framework.core.control.navigation.SiteDefinitions;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.extensions.ExtManager;
import org.olat.presentation.framework.extensions.Extension;
import org.olat.presentation.framework.extensions.sitescreator.SitesCreator;
import org.olat.presentation.framework.layout.OlatGuestFooterController;
import org.olat.presentation.framework.layout.OlatGuestTopNavController;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappControllerParts;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: patrickb Class Description for GuestBFWCParts
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author patrickb
 */
public class GuestBFWCParts implements BaseFullWebappControllerParts {

    /**
	 */
    @Override
    public Controller createFooterController(final UserRequest ureq, final WindowControl wControl) {
        Controller footerCtr = null;
        // ----------- footer, optional (e.g. for copyright, powered by) ------------------
        if (CoreSpringFactory.containsBean("fullWebApp.GuestFooterControllerCreator")) {
            final ControllerCreator footerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.GuestFooterControllerCreator");
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
        if (CoreSpringFactory.containsBean("fullWebApp.GuestHeaderControllerCreator")) {
            final ControllerCreator headerControllerCreator = (ControllerCreator) CoreSpringFactory.getBean("fullWebApp.GuestHeaderControllerCreator");
            headerCtr = headerControllerCreator.createController(ureq, wControl);
        }
        return headerCtr;
    }

    /**
	 */
    @Override
    public Controller createTopNavController(final UserRequest ureq, final WindowControl wControl) {
        return new OlatGuestTopNavController(ureq, wControl);
    }

    @Override
    public Controller getContentController(final UserRequest ureq, final WindowControl wControl) {
        return null;
    }

    /**
	 */
    @Override
    public List<SiteInstance> getSiteInstances(final UserRequest ureq, final WindowControl wControl) {
        final List<SiteInstance> sites = new ArrayList<SiteInstance>();
        final SiteDefinitions sitedefs = (SiteDefinitions) CoreSpringFactory.getBean("olatsites");
        final List<SiteDefinition> sitedeflist = sitedefs.getSiteDefList();

        for (final Iterator<SiteDefinition> it_sites = sitedeflist.iterator(); it_sites.hasNext();) {
            final SiteDefinition sitedef = it_sites.next();
            final SiteInstance site = sitedef.createSite(ureq, wControl);
            if (site != null) {
                // site == null means that site is not visible to the current user
                sites.add(site);
            }
        }

        // let all extensions add sitedefinitions
        final ExtManager extm = CoreSpringFactory.getBean(ExtManager.class);
        final Class extensionPointSites = DTabs.class;
        final int cnt = extm.getExtensionCnt();
        for (int i = 0; i < cnt; i++) {
            final Extension anExt = extm.getExtension(i);
            // check for sites
            final SitesCreator sc = (SitesCreator) anExt.getExtensionFor(extensionPointSites.getName());
            if (sc != null) {
                final List extsitedefs = sc.createSiteDefinitions();
                for (final Iterator it_extsites = extsitedefs.iterator(); it_extsites.hasNext();) {
                    final SiteDefinition sdef = (SiteDefinition) it_extsites.next();
                    final SiteInstance si = sdef.createSite(ureq, wControl);
                    sites.add(si);
                    extm.inform(extensionPointSites, anExt, "added sitedefinition " + sdef.getClass().getName());
                }
            }
        }

        return sites;
    }

}
