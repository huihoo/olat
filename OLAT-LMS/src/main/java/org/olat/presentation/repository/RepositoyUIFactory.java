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
package org.olat.presentation.repository;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * TODO: patrickb Class Description for RepositoyUIFactory
 * <P>
 * Initial Date: 03.12.2007 <br>
 * 
 * @author patrickb
 */
public class RepositoyUIFactory {

    /**
     * Create a launch controller used to launch the given repo entry.
     * 
     * @param re
     * @param initialViewIdentifier
     *            if null the default view will be started, otherwise a controllerfactory type dependant view will be activated (subscription subtype)
     * @param ureq
     * @param wControl
     * @return null if no entry was found, a no access message controller if not allowed to launch or the launch controller if successful.
     */
    public static MainLayoutController createLaunchController(final RepositoryEntry re, final String initialViewIdentifier, final UserRequest ureq,
            final WindowControl wControl) {
        if (re == null) {
            return null;
        }
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        if (!rm.isAllowedToLaunch(ureq.getIdentity(), ureq.getUserSession().getRoles(), re)) {
            final Translator trans = PackageUtil.createPackageTranslator(RepositoyUIFactory.class, ureq.getLocale());
            final String text = trans.translate("launch.noaccess");
            final Controller c = MessageUIFactory.createInfoMessage(ureq, wControl, null, text);

            // use on column layout
            final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, c.getInitialComponent(), null);
            layoutCtr.addDisposableChildController(c); // dispose content on layout dispose
            return layoutCtr;
        }
        rm.incrementLaunchCounter(re);
        final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);

        WindowControl bwControl;
        final OLATResourceable businessOres = re;
        final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
        // OLAT-5944: check if the current context entry is not already the repository entry to avoid duplicate in the business path
        if (ce.equals(wControl.getBusinessControl().getCurrentContextEntry())) {
            bwControl = wControl;
        } else {
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
        }

        final MainLayoutController ctrl = handler.createLaunchController(re.getOlatResource(), initialViewIdentifier, ureq, bwControl);
        if (ctrl == null) {
            throw new AssertException("could not create controller for repositoryEntry " + re);
        }
        if (ctrl instanceof MainLayoutController) {
            return ctrl;
        } else {
            // add layout wrapper
            final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, ctrl.getInitialComponent(), null);
            layoutCtr.addDisposableChildController(ctrl); // dispose content on layout dispose
            return layoutCtr;
        }
    }

}
