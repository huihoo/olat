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
package org.olat.presentation.portfolio.structel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPLoggingAction;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.presentation.portfolio.EPArtefactPoolRunController;
import org.olat.presentation.portfolio.artefacts.collect.EPCollectStepForm03;
import org.olat.presentation.portfolio.artefacts.collect.EPReflexionChangeEvent;
import org.olat.presentation.portfolio.artefacts.run.EPArtefactChoosenEvent;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller to select an Element which has to be added to given PortfolioStructure. All possible elements are disabled per default:
 * <UL>
 * <LI>use setShowLink to enable elements which can be added on this level of structure</LI>
 * </UL>
 * <P>
 * Initial Date: 20.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPAddElementsController extends BasicController {

    private final PortfolioStructure portfolioStructure;
    private final VelocityContainer addLinkVC;
    private final Link addStructLink;
    private final EPFrontendManager ePFMgr;
    public static final String ADD_ARTEFACT = "Artefact";
    public static final String ADD_PAGE = "page";
    public static final String ADD_STRUCTUREELEMENT = "struct";
    public static final String ADD_PORTFOLIOSTRUCTURE = "map";
    private static final Set<String> typeSet = new HashSet<String>() {
        {
            add(ADD_ARTEFACT);
            add(ADD_PAGE);
            add(ADD_STRUCTUREELEMENT);
            add(ADD_PORTFOLIOSTRUCTURE);
        }
    };
    private final Map<String, Boolean> typeMap = new HashMap<String, Boolean>();
    private CloseableModalWindowWrapperController artefactBox;
    private Controller artefactPoolCtrl;
    private Controller artReflexionCtrl;
    private CloseableModalWindowWrapperController reflexionBox;
    private final Link linkArtefactLink;
    private String activeType;

    public EPAddElementsController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructure portStruct) {
        super(ureq, wControl);
        this.portfolioStructure = portStruct;
        ePFMgr = CoreSpringFactory.getBean(EPFrontendManager.class);
        addLinkVC = createVelocityContainer("addLink");
        addStructLink = LinkFactory.createCustomLink("popupLink", "add", "&nbsp;&nbsp;&nbsp;", Link.NONTRANSLATED, addLinkVC, this);
        addStructLink.setVisible(false);

        linkArtefactLink = LinkFactory.createCustomLink("linkArtefact", "link", "&nbsp;&nbsp;&nbsp;", Link.NONTRANSLATED, addLinkVC, this);
        linkArtefactLink.setTooltip(translate("linkArtefact.tooltip"), false);
        linkArtefactLink.setCustomEnabledLinkCSS("b_eportfolio_add_link b_eportfolio_link");

        for (final String key : typeSet) {
            typeMap.put(key, Boolean.FALSE);
        }

        putInitialPanel(addLinkVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, @SuppressWarnings("unused") final Event event) {
        if (source == linkArtefactLink) {
            popUpAddArtefactBox(ureq);
        } else if (source == addStructLink) {
            if (ADD_PAGE.equals(activeType)) {
                final String title = translate("new.page.title");
                final String description = translate("new.page.desc");
                final PortfolioStructure newPage = ePFMgr.createAndPersistPortfolioPage(portfolioStructure, title, description);
                fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, newPage));
            } else if (ADD_STRUCTUREELEMENT.equals(activeType)) {
                final String title = translate("new.structure.title");
                final String description = translate("new.structure.desc");
                final PortfolioStructure newStruct = ePFMgr.createAndPersistPortfolioStructureElement(portfolioStructure, title, description);
                fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, newStruct));
            } else if (ADD_PORTFOLIOSTRUCTURE.equals(activeType)) {
                // show tree-with maps to choose from
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == artefactPoolCtrl && event instanceof EPArtefactChoosenEvent) {
            // finally an artefact was choosen
            final EPArtefactChoosenEvent artCEv = (EPArtefactChoosenEvent) event;
            artefactBox.deactivate();
            final AbstractArtefact choosenArtefact = artCEv.getArtefact();
            // check for a yet existing link to this artefact
            if (ePFMgr.isArtefactInStructure(choosenArtefact, portfolioStructure)) {
                showWarning("artefact.already.in.structure");
            } else {
                final boolean successfullLink = ePFMgr.addArtefactToStructure(getIdentity(), choosenArtefact, portfolioStructure);
                if (successfullLink) {
                    showInfo("artefact.choosen", new String[] { choosenArtefact.getTitle(), portfolioStructure.getTitle() });
                    ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(choosenArtefact));
                    ThreadLocalUserActivityLogger.log(EPLoggingAction.EPORTFOLIO_ARTEFACT_SELECTED, getClass());
                } else {
                    showError("restrictions.not.conform");
                }
                // TODO: epf: RH: improvement let user choose in settings, if reflexion
                // should be presented right after adding
                // popUpAdaptReflexionBox(ureq, choosenArtefact);
                fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, portfolioStructure));
            }

        } else if (source == artReflexionCtrl && event instanceof EPReflexionChangeEvent) {
            final EPReflexionChangeEvent refEv = (EPReflexionChangeEvent) event;
            ePFMgr.setReflexionForArtefactToStructureLink(refEv.getRefArtefact(), portfolioStructure, refEv.getReflexion());
            reflexionBox.deactivate();
            fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, portfolioStructure));
        }
    }

    public void setShowLink(final String... types) {
        int addAmount = 0;
        for (final String type : types) {
            if (typeSet.contains(type)) {
                typeMap.put(type, Boolean.TRUE);
                if (!type.equals(ADD_ARTEFACT)) {
                    prepareAddLink(type);
                    activeType = type;
                    addAmount++;
                }
            }
        }

        if (addAmount > 1) {
            throw new AssertException(
                    "its not possible anymore to have more than one structure element type to be added. if needed, implement links of this controller in callout again.");
        }
        linkArtefactLink.setVisible(typeMap.get(ADD_ARTEFACT));
    }

    private void prepareAddLink(final String type) {
        addStructLink.setVisible(true);
        addStructLink.setTooltip(translate("add." + type), false);
        addStructLink.setCustomEnabledLinkCSS("b_eportfolio_add_link b_ep_" + type + "_icon");
    }

    private void popUpAddArtefactBox(final UserRequest ureq) {
        if (artefactPoolCtrl == null) {
            artefactPoolCtrl = new EPArtefactPoolRunController(ureq, getWindowControl(), true);
            listenTo(artefactPoolCtrl);
        }
        final String title = translate("choose.artefact.title");
        artefactBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, artefactPoolCtrl.getInitialComponent(), "addArtefact"
                + portfolioStructure.getKey());
        listenTo(artefactBox);
        artefactBox.activate();
    }

    // TODO: epf: RH: improvement to let user see reflexion right after adding
    private void popUpAdaptReflexionBox(final UserRequest ureq, final AbstractArtefact artefact) {
        if (artReflexionCtrl == null) {
            artReflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact);
            listenTo(artReflexionCtrl);
        }
        final String title = translate("change.reflexion");
        reflexionBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, artReflexionCtrl.getInitialComponent(), "reflexionFor"
                + artefact.getKey());
        reflexionBox.setInitialWindowSize(600, 400);
        listenTo(reflexionBox);
        reflexionBox.activate();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
