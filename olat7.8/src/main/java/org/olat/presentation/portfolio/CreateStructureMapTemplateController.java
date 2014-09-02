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
package org.olat.presentation.portfolio;

import org.olat.data.portfolio.structure.PortfolioStructureDao;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.portfolio.structel.EPCreateMapController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This is the controller which create a new template in the repository via the repository
 * <P>
 * Initial Date: 12 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CreateStructureMapTemplateController extends BasicController implements IAddController {
    private OLATResource templateOres;
    private final PortfolioStructureDao eSTMgr;
    private boolean isNewTemplateAndGetsAPage = false;

    /**
     * Constructor
     * 
     * @param addCallback
     * @param ureq
     * @param wControl
     */
    public CreateStructureMapTemplateController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        eSTMgr = (PortfolioStructureDao) CoreSpringFactory.getBean(PortfolioStructureDao.class);
        if (addCallback != null) {
            // create a new template
            isNewTemplateAndGetsAPage = true;
            templateOres = eSTMgr.createPortfolioMapTemplateResource();
            addCallback.setDisplayName(translate(templateOres.getResourceableTypeName()));
            addCallback.setResourceable(templateOres);
            addCallback.setResourceName(translate("EPStructuredMapTemplate"));
            addCallback.finished(ureq);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Nothing to dispose
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // Nothing to catch
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        return getInitialComponent();
    }

    /**
	 */
    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        final PortfolioStructureMap mapTemp = eSTMgr.createAndPersistPortfolioMapTemplateFromEntry(getIdentity(), re);
        // add a page, as each map should have at least one per default!
        final Translator pt = PackageUtil.createPackageTranslator(EPCreateMapController.class, getLocale(), getTranslator());
        final String title = pt.translate("new.page.title");
        final String description = pt.translate("new.page.desc");
        if (isNewTemplateAndGetsAPage) { // no additional page when this is a copy.
            final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
            ePFMgr.createAndPersistPortfolioPage(mapTemp, title, description);
        }
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        // nothing persisted
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        return true;
    }
}
