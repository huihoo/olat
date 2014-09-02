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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * show minimal set of artefact details in small preview controllers. if an artefact handler provides a special preview, use this instead the generic artefact-view used
 * inside maps.
 * <P>
 * Initial Date: 17.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultipleArtefactSmallReadOnlyPreviewController extends BasicController implements EPMultiArtefactsController {

    private List<AbstractArtefact> artefacts;
    private final PortfolioAbstractHandler portfolioModule;
    private ArrayList<Controller> artefactCtrls;
    private ArrayList<Controller> optionLinkCtrls;
    private final VelocityContainer vC;
    private final PortfolioStructure struct;
    private final EPSecurityCallback secCallback;

    public EPMultipleArtefactSmallReadOnlyPreviewController(final UserRequest ureq, final WindowControl wControl, final List<AbstractArtefact> artefacts,
            final PortfolioStructure struct, final EPSecurityCallback secCallback) {
        super(ureq, wControl);
        this.artefacts = artefacts;
        this.struct = struct;
        this.secCallback = secCallback;
        vC = createVelocityContainer("smallMultiArtefactPreview");
        portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean(PortfolioAbstractHandler.class);

        init(ureq);
        putInitialPanel(vC);
    }

    private void init(final UserRequest ureq) {
        if (artefactCtrls != null) {
            disposeArtefactControllers();
        }
        artefactCtrls = new ArrayList<Controller>();
        if (optionLinkCtrls != null) {
            disposeOptionLinkControllers();
        }
        optionLinkCtrls = new ArrayList<Controller>();
        final List<List<Panel>> artefactCtrlCompLines = new ArrayList<List<Panel>>();
        List<Panel> artefactCtrlCompLine = new ArrayList<Panel>();
        int i = 1;
        for (final AbstractArtefact artefact : artefacts) {
            final EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
            Controller artCtrl;
            // check for special art-display:
            final boolean special = artHandler.isProvidingSpecialMapViewController();
            if (special) {
                artCtrl = artHandler.getSpecialMapViewController(ureq, getWindowControl(), artefact);
            } else {
                artCtrl = new EPArtefactViewReadOnlyController(ureq, getWindowControl(), artefact, secCallback, struct);
            }
            if (artCtrl != null) {
                artefactCtrls.add(artCtrl);
                final Component artefactCtrlComponent = artCtrl.getInitialComponent();
                listenTo(artCtrl);

                final String artifactName = "artCtrl" + i;
                final Panel namedPanel = new Panel(artifactName);
                namedPanel.setContent(artefactCtrlComponent);

                if (special) {
                    // usually we put 3 standard artifacts in one line. Special artifacts (e.g. Blog) take an own line.
                    if (!artefactCtrlCompLine.isEmpty()) {
                        artefactCtrlCompLines.add(artefactCtrlCompLine);
                        artefactCtrlCompLine = new ArrayList<Panel>();
                    }
                    artefactCtrlCompLines.add(Collections.singletonList(namedPanel));

                    // need a flag 'special' for the velocity template
                    vC.put("special" + artifactName, artefactCtrlComponent);
                } else {
                    if (artefactCtrlCompLine.size() == 3) {
                        artefactCtrlCompLines.add(artefactCtrlCompLine);
                        artefactCtrlCompLine = new ArrayList<Panel>();
                    }
                    artefactCtrlCompLine.add(namedPanel);

                    vC.put(artifactName, namedPanel);
                }

                // add the optionsLink to the artefact
                EPArtefactViewOptionsLinkController optionsLinkCtrl = new EPArtefactViewOptionsLinkController(ureq, getWindowControl(), artefact, secCallback, struct);
                vC.put("optionsLink" + i, optionsLinkCtrl.getInitialComponent());
                listenTo(optionsLinkCtrl);
                optionLinkCtrls.add(optionsLinkCtrl);

                i++;
            }
        }

        // add last line even if not completely filled
        if (!artefactCtrlCompLine.isEmpty()) {
            artefactCtrlCompLines.add(artefactCtrlCompLine);
        }

        vC.contextPut("artefactCtrlCompLines", artefactCtrlCompLines);
    }

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
    public void setNewArtefactsList(final UserRequest ureq, final List<AbstractArtefact> artefacts) {
        this.artefacts = artefacts;
        init(ureq);
    }

    /**
	 */
    private void disposeOptionLinkControllers() {
        if (optionLinkCtrls != null) {
            for (Controller optionCtrl : optionLinkCtrls) {
                removeAsListenerAndDispose(optionCtrl);
                optionCtrl = null;
            }
            optionLinkCtrls = null;
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to handle yet
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        fireEvent(ureq, event); // pass to others
    }

    /**
	 */
    @Override
    protected void doDispose() {
        disposeArtefactControllers();
        disposeOptionLinkControllers();
    }

}
