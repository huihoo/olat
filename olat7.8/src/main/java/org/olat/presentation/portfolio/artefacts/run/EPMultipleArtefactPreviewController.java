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
package org.olat.presentation.portfolio.artefacts.run;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: - maybe optimize new creation of artefacts controllers by watching which are yet existing and not dropping/creating them again
 * <P>
 * Initial Date: 08.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class EPMultipleArtefactPreviewController extends BasicController implements EPMultiArtefactsController {

    private final VelocityContainer vC;
    private Link artAttribBtn;
    private List<Controller> artefactCtrls;
    private final EPFrontendManager ePFMgr;
    private EPArtefactAttributeSettingController artAttribCtlr;
    private Map<String, Boolean> artAttribConfig;
    private final boolean artefactChooseMode;
    private static final int artefactsPerPage = 4;
    private List<AbstractArtefact> artefactsFullList;
    private CloseableCalloutWindowController artAttribCalloutCtr;

    public EPMultipleArtefactPreviewController(final UserRequest ureq, final WindowControl wControl, final List<AbstractArtefact> artefacts) {
        this(ureq, wControl, artefacts, false);
    }

    public EPMultipleArtefactPreviewController(final UserRequest ureq, final WindowControl wControl, final List<AbstractArtefact> artefacts,
            final boolean artefactChooseMode) {
        super(ureq, wControl);
        this.artefactChooseMode = artefactChooseMode;
        vC = createVelocityContainer("multiArtefact");
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        if (!artefactChooseMode) {
            artAttribBtn = LinkFactory.createCustomLink("detail.options", "detail.options", "", Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, vC, this);
            artAttribBtn.setTooltip(translate("detail.options"), false);
            artAttribBtn.setTitle(translate("detail.options"));
            artAttribBtn.setCustomEnabledLinkCSS("b_ep_artAttribLink b_small_icon");
        }

        setNewArtefactsList(ureq, artefacts);

        putInitialPanel(vC);
    }

    public EPMultipleArtefactPreviewController(final UserRequest ureq, final WindowControl wControl) {
        this(ureq, wControl, null);
    }

    @Override
    public void setNewArtefactsList(final UserRequest ureq, final List<AbstractArtefact> artefacts) {
        this.artefactsFullList = artefacts;
        if (artefacts != null) {
            preparePaging(ureq, 1);
        }
    }

    private void preparePaging(final UserRequest ureq, final int actualPage) {
        final int nrOfArtefacts = artefactsFullList.size();
        vC.contextPut("artefactAmnt", Integer.toString(nrOfArtefacts));
        if (nrOfArtefacts > artefactsPerPage) {
            final int divRest = (nrOfArtefacts % artefactsPerPage);
            final int nrOfPages = (nrOfArtefacts / artefactsPerPage) + (divRest > 0 ? 1 : 0);
            final ArrayList<Link> pageLinkList = new ArrayList<Link>();
            for (int i = 1; i < nrOfPages + 1; i++) {
                final Link pageLink = LinkFactory.createCustomLink("pageLink" + i, "pageLink" + i, String.valueOf(i), Link.LINK + Link.NONTRANSLATED, vC, this);
                pageLink.setUserObject(i);
                if (actualPage == i) {
                    pageLink.setEnabled(false);
                }
                pageLinkList.add(pageLink);
            }
            final int fromIndex = (actualPage - 1) * artefactsPerPage;
            int toIndex = actualPage * artefactsPerPage;
            if (toIndex > nrOfArtefacts) {
                toIndex = nrOfArtefacts;
            }
            final List<AbstractArtefact> artefactsToShow = artefactsFullList.subList(fromIndex, toIndex);
            vC.contextPut("pageLinkList", pageLinkList);
            initOrUpdateArtefactControllers(ureq, artefactsToShow);
        } else {
            // no paging needed
            vC.contextRemove("pageLinkList");
            initOrUpdateArtefactControllers(ureq, artefactsFullList);
        }
    }

    /**
     * @param ureq
     * @param wControl
     * @param artefacts
     */
    private void initOrUpdateArtefactControllers(final UserRequest ureq, final List<AbstractArtefact> artefacts) {
        vC.contextPut("artefacts", artefacts);
        if (artefactCtrls != null) {
            disposeArtefactControllers();
        }
        artefactCtrls = new ArrayList<Controller>();
        final ArrayList<Component> artefactCtrlComps = new ArrayList<Component>();
        int i = 1;
        getArtefactAttributeDisplayConfig(ureq.getIdentity());
        if (artefacts != null) {
            for (final AbstractArtefact abstractArtefact : artefacts) {
                final Controller artCtrl = new EPArtefactViewController(ureq, getWindowControl(), abstractArtefact, artAttribConfig, artefactChooseMode, false, true);
                artefactCtrls.add(artCtrl);
                final Component artefactCtrlComponent = artCtrl.getInitialComponent();
                listenTo(artCtrl);
                artefactCtrlComps.add(artefactCtrlComponent);
                vC.put("artCtrl" + i, artefactCtrlComponent);
                i++;
            }
        }
        vC.contextPut("artefactCtrlComps", artefactCtrlComps);
    }

    // dispose all artefact controllers
    private void disposeArtefactControllers() {
        if (artefactCtrls != null) {
            for (Controller artefactCtrl : artefactCtrls) {
                removeAsListenerAndDispose(artefactCtrl);
                artefactCtrl = null;
            }
            artefactCtrls = null;
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        disposeArtefactControllers();
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == artAttribBtn) {
            if (artAttribCalloutCtr == null) {
                popupArtAttribBox(ureq);
            } else {
                // close on second click
                closeArtAttribBox();
            }
        } else if (source instanceof Link) {
            final Link link = (Link) source;
            final int pageNum = (Integer) link.getUserObject();
            preparePaging(ureq, pageNum);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == artAttribCtlr) {
            if (event.equals(Event.DONE_EVENT)) {
                closeArtAttribBox();
                // set new display config for each artefact controller
                vC.setDirty(true);
            }
        } else if (source instanceof EPArtefactViewController) {

            if (event.getCommand().equals(EPArtefactDeletedEvent.ARTEFACT_DELETED)) {
                // an artefact has been deleted, so refresh
                final EPArtefactDeletedEvent epDelEv = (EPArtefactDeletedEvent) event;
                // only refresh whats needed, dont load all artefacts!
                artefactsFullList.remove(epDelEv.getArtefact());
                setNewArtefactsList(ureq, artefactsFullList);
                fireEvent(ureq, event); // pass to EPArtefactPoolRunCtrl
            }
        }
        if (event instanceof EPArtefactChoosenEvent) {
            // an artefact was choosen, pass through the event until top
            fireEvent(ureq, event);
        }
        if (source == artAttribCalloutCtr && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
            removeAsListenerAndDispose(artAttribCalloutCtr);
            artAttribCalloutCtr = null;
        }

    }

    private Map<String, Boolean> getArtefactAttributeDisplayConfig(final Identity ident) {
        if (artAttribConfig == null) {
            artAttribConfig = ePFMgr.getArtefactAttributeConfig(ident);
        }
        return artAttribConfig;
    }

    /**
     * @param ureq
     */
    private void popupArtAttribBox(final UserRequest ureq) {
        final String title = translate("display.option.title");
        if (artAttribCtlr == null) {
            artAttribCtlr = new EPArtefactAttributeSettingController(ureq, getWindowControl(), getArtefactAttributeDisplayConfig(ureq.getIdentity()));
            listenTo(artAttribCtlr);
        }
        removeAsListenerAndDispose(artAttribCalloutCtr);
        artAttribCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), artAttribCtlr.getInitialComponent(), artAttribBtn, title, true, null);
        listenTo(artAttribCalloutCtr);
        artAttribCalloutCtr.activate();
    }

    private void closeArtAttribBox() {
        if (artAttribCalloutCtr != null) {
            artAttribCalloutCtr.deactivate();
            removeAsListenerAndDispose(artAttribCalloutCtr);
            artAttribCalloutCtr = null;
        }
    }

}
