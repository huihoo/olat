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
package org.olat.presentation.portfolio.artefacts.collect;

import java.util.Date;
import java.util.List;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepRunnerCallback;
import org.olat.presentation.framework.core.control.generic.wizard.StepsMainRunController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Entry point to the collection wizzard.
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ArtefactWizzardStepsController extends BasicController {

    private Controller collectStepsCtrl;
    EPFrontendManager ePFMgr;
    private PortfolioAbstractHandler portfolioModule;
    private VelocityContainer collectLinkVC;
    private Link addLink;
    AbstractArtefact artefact;
    private OLATResourceable ores;
    private String businessPath;
    private VFSContainer tmpFolder = null;

    public ArtefactWizzardStepsController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        setManagersAndModule();
        final EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler("Forum");
        final AbstractArtefact newArtefact = handler.createArtefact();
        this.artefact = newArtefact;

        initCollectionStepWizzard(ureq);
        final Panel emptyItself = new Panel("emptyItself");
        putInitialPanel(emptyItself);
    }

    /**
     * to be used to manipulate with the wizzard on an already existing artefact.
     * 
     * @param ureq
     * @param wControl
     * @param artefact
     */
    public ArtefactWizzardStepsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final VFSContainer tmpFolder) {
        super(ureq, wControl);
        setManagersAndModule();
        this.artefact = artefact;
        this.tmpFolder = tmpFolder;

        initCollectionStepWizzard(ureq);
        final Panel emptyItself = new Panel("emptyItself");
        putInitialPanel(emptyItself);
    }

    /**
     * !! you should not use this constructor directly !! intention would be to use the EPUIFactory instead. like this the collect-links are hidden, if ePortfolio is
     * disabled! the use of the EPUIFactory is not yet possible in all places in OLAT (sometimes a businesspath is missing).
     * 
     * @param ureq
     * @param wControl
     * @param ores
     * @param subPath
     * @param businessPath
     */
    public ArtefactWizzardStepsController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final String businessPath) {
        super(ureq, wControl);
        setManagersAndModule();
        this.ores = ores;
        this.businessPath = businessPath;
        initCollectLinkVelocity();
    }

    /**
     * !! you should not use this constructor directly !! intention would be to use the EPUIFactory instead. like this the collect-links are hidden, if ePortfolio is
     * disabled! the use of the EPUIFactory is not yet possible in all places in OLAT (sometimes a businesspath is missing).
     * 
     * @param ureq
     * @param wControl
     * @param artefact
     */
    public ArtefactWizzardStepsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        super(ureq, wControl);
        setManagersAndModule();
        this.artefact = artefact;
        this.businessPath = artefact.getBusinessPath();
        initCollectLinkVelocity();
    }

    /**
     * @param ureq
     * @param artefact
     */
    private void initCollectLinkVelocity() {
        collectLinkVC = createVelocityContainer("collectLink");

        addLink = LinkFactory.createCustomLink("add.to.eportfolio", "add.to.eportfolio", "", Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, collectLinkVC, this);
        addLink.setCustomEnabledLinkCSS("b_eportfolio_add");
        addLink.setTooltip(translate("add.to.eportfolio"), false);

        // check for an already existing artefact with same businessPath, change collect-item
        final List<AbstractArtefact> existingArtefacts = ePFMgr.loadArtefactsByBusinessPath(businessPath, getIdentity());
        if (existingArtefacts != null) {
            final int amount = existingArtefacts.size();
            addLink.setCustomEnabledLinkCSS("b_eportfolio_add_again");
            addLink.setTooltip(translate("add.to.eportfolio.again", String.valueOf(amount)), false);
        }
        putInitialPanel(collectLinkVC);
    }

    /**
     * @param ores
     * @param businessPath
     */
    private void prepareNewArtefact() {
        final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(ores.getResourceableTypeName());
        final AbstractArtefact artefact1 = artHandler.createArtefact();
        artefact1.setAuthor(getIdentity());
        artefact1.setCollectionDate(new Date());
        artefact1.setBusinessPath(businessPath);
        artHandler.prefillArtefactAccordingToSource(artefact1, ores);
        this.artefact = artefact1;
    }

    private void setManagersAndModule() {
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);
    }

    private void initCollectionStepWizzard(final UserRequest ureq) {
        if (artefact == null && ores != null) {
            prepareNewArtefact();
        }
        final Step start = new EPCollectStep00(ureq, artefact);
        final StepRunnerCallback finish = new EPArtefactWizzardStepCallback(tmpFolder);
        collectStepsCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("collect.wizzard.title"));
        listenTo(collectStepsCtrl);
        getWindowControl().pushAsModalDialog(collectStepsCtrl.getInitialComponent());
    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == addLink) {
            // someone triggered the 'add to my portfolio' workflow by its link
            artefact = null; // always collect a new artefact
            initCollectionStepWizzard(ureq);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == collectStepsCtrl) {
            if (event == Event.CHANGED_EVENT) {
                ePFMgr.updateArtefact(artefact);
                showInfo("collect.success", artefact.getTitle());
            } else {
                // set back artefact-values
                // artefact = ePFMgr.loadArtefact(artefact.getKey());
            }
            // cancel / done event means no data change but close wizzard and fwd
            // event
            getWindowControl().pop();
            fireEvent(ureq, event);
        }
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
