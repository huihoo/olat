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
package org.olat.presentation.portfolio.artefacts.run;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.portfolio.artefacts.collect.EPCollectStepForm04;
import org.olat.presentation.portfolio.structel.EPStructureChangeEvent;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: Displays the options-Link for an artefact. handles displaying of the callout and its links (remove artefact from map, reflexion, move artefact within map)<br>
 * <P>
 * Initial Date: 14.07.2011 <br>
 * 
 * @author Sergio Trentini, sergio.trentini@frentix.com, http://www.frentix.com
 */
public class EPArtefactViewOptionsLinkController extends BasicController {

    private final AbstractArtefact artefact;
    private PortfolioStructure struct;
    private final EPSecurityCallback secCallback;
    private final VelocityContainer vC;
    private final EPFrontendManager ePFMgr;

    // controllers
    private EPCollectStepForm04 moveTreeCtrl;
    private CloseableModalWindowWrapperController moveTreeBox;
    private Controller reflexionCtrl;
    private CloseableCalloutWindowController artefactOptionCalloutCtrl;

    // the link that triggers the callout
    private Link optionLink;

    // the links within the callout
    private Link unlinkLink;
    private Link moveLink;
    private Link reflexionLink;

    public EPArtefactViewOptionsLinkController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact,
            final EPSecurityCallback secCallback, final PortfolioStructure struct) {
        super(ureq, wControl);
        this.artefact = artefact;
        this.struct = struct;
        this.secCallback = secCallback;

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        vC = createVelocityContainer("optionsLink");

        optionLink = LinkFactory.createCustomLink("option.link", "option", "&nbsp;&nbsp;", Link.NONTRANSLATED, vC, this);
        optionLink.setCustomEnabledLinkCSS("b_ep_options");
        optionLink.setTooltip(translate("option.link"), false);

        putInitialPanel(vC);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == optionLink) {
            popUpArtefactOptionsBox(ureq);
        } else if (source == unlinkLink) {
            closeArtefactOptionsCallout();
            struct = ePFMgr.removeArtefactFromStructure(artefact, struct);
            fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.REMOVED, struct)); // refresh ui
        } else if (source == moveLink) {
            closeArtefactOptionsCallout();
            showMoveTree(ureq);
        } else if (source == reflexionLink) {
            closeArtefactOptionsCallout();
            reflexionCtrl = EPUIFactory.getReflexionPopup(ureq, getWindowControl(), secCallback, artefact, struct);
            listenTo(reflexionCtrl);
        }
    }

    /**
     * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
     */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == moveTreeCtrl && event.getCommand().equals(EPStructureChangeEvent.CHANGED)) {
            EPStructureChangeEvent epsEv = (EPStructureChangeEvent) event;
            PortfolioStructure newStruct = epsEv.getPortfolioStructure();
            showInfo("artefact.moved", newStruct.getTitle());
            moveTreeBox.deactivate();
        }
        fireEvent(ureq, event);
    }

    /**
     * opens a modalWindow that displays the "move-tree"
     * 
     * @param ureq
     */
    private void showMoveTree(UserRequest ureq) {
        moveTreeCtrl = new EPCollectStepForm04(ureq, getWindowControl(), artefact, struct);
        listenTo(moveTreeCtrl);
        String title = translate("artefact.move.title");
        moveTreeBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, moveTreeCtrl.getInitialComponent(), "moveTreeBox");
        listenTo(moveTreeBox);
        moveTreeBox.setInitialWindowSize(450, 300);
        moveTreeBox.activate();
    }

    /**
     * closes the callout
     */
    private void closeArtefactOptionsCallout() {
        if (artefactOptionCalloutCtrl != null) {
            artefactOptionCalloutCtrl.deactivate();
            removeAsListenerAndDispose(artefactOptionCalloutCtrl);
            artefactOptionCalloutCtrl = null;
        }
    }

    /**
     * opens the callout
     * 
     * @param ureq
     */
    private void popUpArtefactOptionsBox(UserRequest ureq) {
        VelocityContainer artOptVC = createVelocityContainer("artefactOptions");
        if (secCallback.canRemoveArtefactFromStruct()) {
            unlinkLink = LinkFactory.createCustomLink("unlink.link", "remove", "remove.from.map", Link.LINK, artOptVC, this);
        }
        if (secCallback.canAddArtefact() && secCallback.canRemoveArtefactFromStruct() && secCallback.isOwner()) { // isOwner: don't show move in group maps!
            moveLink = LinkFactory.createCustomLink("move.link", "move", "artefact.options.move", Link.LINK, artOptVC, this);
        }
        reflexionLink = LinkFactory.createCustomLink("reflexion.link", "reflexion", "table.header.reflexion", Link.LINK, artOptVC, this);
        String title = translate("option.link");
        removeAsListenerAndDispose(artefactOptionCalloutCtrl);
        artefactOptionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), artOptVC, optionLink, title, true, null);
        listenTo(artefactOptionCalloutCtrl);
        artefactOptionCalloutCtrl.activate();
    }

    @Override
    protected void doDispose() {
        closeArtefactOptionsCallout();
    }

}
