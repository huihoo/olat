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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.ims.cp;

import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.ims.cp.CPManager;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Implementation of the repository add controller for IMS ContentPackages
 * <P>
 * Initial Date: 11.09.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CreateNewCPController extends BasicController implements IAddController, ControllerEventListener {

    private final OLATResource newCPResource;

    public CreateNewCPController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // do prepare course now
        newCPResource = OLATResourceManager.getInstance().createOLATResourceInstance("FileResource.IMSCP");

        if (addCallback != null) {
            addCallback.setResourceable(newCPResource);
            addCallback.setDisplayName(translate("FileResource.IMSCP"));
            addCallback.setResourceName("-");
            addCallback.finished(ureq);
        }
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to listen to...
    }

    @Override
    public Component getTransactionComponent() {
        return getInitialComponent();
    }

    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        getBaseSecurityEBL().createCourseAdminPolicy(re);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    @Override
    public void transactionAborted() {
        // Don't do nothing!
    }

    @Override
    public boolean transactionFinishBeforeCreate() {
        final CPManager cpMmg = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        final String initialPageTitle = translate("cptreecontroller.newpage.title");
        cpMmg.createNewCP(newCPResource, initialPageTitle);
        return true;
    }

}
