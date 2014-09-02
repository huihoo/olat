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

import java.util.List;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.restriction.CollectRestriction;
import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.portfolio.structure.StructureStatusEnum;
import org.olat.lms.commentandrate.CommentAndRatingService;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.commentandrate.UserCommentsAndRatingsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.portfolio.artefacts.run.EPMultiArtefactsController;
import org.olat.presentation.portfolio.structel.edit.EPCollectRestrictionResultController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * View the content of a page ( structure / artefacts)
 * <P>
 * Initial Date: 23.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPPageViewController extends BasicController {

    private EPPage page;
    private final VelocityContainer vC;
    private final EPSecurityCallback secCallback;
    private EPCollectRestrictionResultController resultCtrl;
    private final EPFrontendManager ePFMgr;
    private final CommentAndRatingService commentAndRatingService;
    private UserCommentsAndRatingsController commentsAndRatingCtr;

    public EPPageViewController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructure map, final EPPage page, final boolean withComments,
            final EPSecurityCallback secCallback) {
        super(ureq, wControl);
        vC = createVelocityContainer("pageView");
        this.page = page;
        this.secCallback = secCallback;

        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        commentAndRatingService = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
        commentAndRatingService.init(getIdentity(), map.getOlatResource(), page.getKey().toString(), false, ureq.getUserSession().getRoles().isGuestOnly());

        init(ureq);

        if (withComments && commentsAndRatingCtr != null) {
            commentsAndRatingCtr.expandComments(ureq);
        }

        putInitialPanel(vC);
    }

    public EPPage getPage() {
        return page;
    }

    protected void init(final UserRequest ureq) {
        vC.contextPut("page", page);
        final boolean parentMapClosed = StructureStatusEnum.CLOSED.equals(((PortfolioStructureMap) ePFMgr.loadStructureParent(page)).getStatus());

        vC.remove(vC.getComponent("checkResults"));
        if (secCallback.isRestrictionsEnabled()) {
            removeAsListenerAndDispose(resultCtrl);
            final List<CollectRestriction> restrictions = page.getCollectRestrictions();
            if (!restrictions.isEmpty()) {
                final boolean check = ePFMgr.checkCollectRestriction(page);
                resultCtrl = new EPCollectRestrictionResultController(ureq, getWindowControl(), page);
                resultCtrl.setMessage(ureq, restrictions, check);
                vC.put("checkResults", resultCtrl.getInitialComponent());
                listenTo(resultCtrl);
            }
        }

        vC.remove(vC.getComponent("artefacts"));
        final List<AbstractArtefact> artefacts = ePFMgr.getArtefacts(page);
        if (artefacts.size() != 0) {
            final EPMultiArtefactsController artefactCtrl = EPUIFactory.getConfigDependentArtefactsControllerForStructure(ureq, getWindowControl(), artefacts, page,
                    secCallback);
            vC.put("artefacts", artefactCtrl.getInitialComponent());
            listenTo(artefactCtrl);
        }

        vC.remove(vC.getComponent("structElements"));
        final List<PortfolioStructure> structElements = ePFMgr.loadStructureChildren(page);
        if (structElements.size() != 0) {
            final EPStructureElementsController structElCtrl = new EPStructureElementsController(ureq, getWindowControl(), structElements, secCallback, parentMapClosed);
            vC.put("structElements", structElCtrl.getInitialComponent());
            listenTo(structElCtrl);
        }

        vC.remove(vC.getComponent("addButton"));
        if (!parentMapClosed && (secCallback.canAddArtefact() || secCallback.canAddStructure())) {
            final EPAddElementsController addButton = new EPAddElementsController(ureq, getWindowControl(), page);
            if (secCallback.canAddArtefact()) {
                addButton.setShowLink(EPAddElementsController.ADD_ARTEFACT);
            }
            if (secCallback.canAddStructure()) {
                addButton.setShowLink(EPAddElementsController.ADD_STRUCTUREELEMENT);
            }
            vC.put("addButton", addButton.getInitialComponent());
            listenTo(addButton);
        }

        vC.remove(vC.getComponent("commentCtrl"));
        if (secCallback.canCommentAndRate()) {
            removeAsListenerAndDispose(commentsAndRatingCtr);
            commentsAndRatingCtr = commentAndRatingService.createUserCommentsAndRatingControllerExpandable(ureq, getWindowControl());
            // commentsAndRatingCtr.expandComments(ureq);
            listenTo(commentsAndRatingCtr);
            vC.put("commentCtrl", commentsAndRatingCtr.getInitialComponent());
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (event instanceof EPStructureChangeEvent) {
            this.page = (EPPage) ePFMgr.loadPortfolioStructureByKey(page.getKey());
            init(ureq);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

}
