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
package org.olat.presentation.portfolio.structel.run;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commentandrate.UserCommentsCount;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.EPPage;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.commentandrate.CommentAndRatingService;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.security.EPSecurityCallback;
import org.olat.presentation.commentandrate.UserCommentsAndRatingsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.portfolio.structel.EPStructureEvent;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * presents a static TOC with links to elements
 * <P>
 * Initial Date: 25.10.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTOCReadOnlyController extends BasicController {

    private static final String CONST_FOR_VC_STYLE_STRUCT = "struct"; // used to style in velocity
    private static final String CONST_FOR_VC_STYLE_PAGE = "page"; // used to style in velocity
    private static final String LINK_CMD_OPEN_ARTEFACT = "oArtefact";
    private static final String LINK_CMD_OPEN_STRUCT = "oStruct";
    private static final String LINK_CMD_OPEN_COMMENTS = "oComments";
    private final VelocityContainer vC;
    private final EPFrontendManager ePFMgr;
    private final List<UserCommentsCount> commentCounts;
    private final CommentAndRatingService commentAndRatingService;
    private UserCommentsAndRatingsController commentsAndRatingCtr;
    private final PortfolioStructure map;
    private final EPSecurityCallback secCallback;
    private Link artOnOffLink;

    public EPTOCReadOnlyController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructure map, final EPSecurityCallback secCallback) {
        super(ureq, wControl);
        vC = createVelocityContainer("toc");
        this.map = map;
        this.secCallback = secCallback;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        final boolean withArtefacts = false;

        commentAndRatingService = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
        commentAndRatingService.init(getIdentity(), map.getOlatResource(), null, false, ureq.getUserSession().getRoles().isGuestOnly());
        commentCounts = commentAndRatingService.getUserCommentsManager().countCommentsWithSubPath();

        init(ureq, withArtefacts);

        putInitialPanel(vC);
    }

    private void init(final UserRequest ureq, final boolean withArtefacts) {
        // have a toggle to show with/without artefacts
        artOnOffLink = LinkFactory.createButtonSmall("artOnOffLink", vC, this);
        artOnOffLink.setCustomDisplayText(translate("artOnOffLink." + !withArtefacts));
        artOnOffLink.setUserObject(withArtefacts);

        // do recursively
        final int level = 0;
        final List<TOCElement> tocList = new ArrayList<TOCElement>();
        buildTOCModel(map, tocList, level, withArtefacts);

        vC.contextPut("tocList", tocList);

        if (secCallback.canCommentAndRate()) {
            removeAsListenerAndDispose(commentsAndRatingCtr);
            commentsAndRatingCtr = commentAndRatingService.createUserCommentsAndRatingControllerExpandable(ureq, getWindowControl());
            listenTo(commentsAndRatingCtr);
            vC.put("commentCtrl", commentsAndRatingCtr.getInitialComponent());
        }
    }

    /**
     * builds the tocList recursively containing artefacts, pages and struct-Elements
     * 
     * @param pStruct
     * @param tocList
     *            list with TOCElement's to use in velocity
     * @param level
     * @param withArtefacts
     *            set false, to skip artefacts
     */
    private void buildTOCModel(final PortfolioStructure pStruct, final List<TOCElement> tocList, int level, final boolean withArtefacts) {
        level++;

        if (withArtefacts) {
            final List<AbstractArtefact> artList = ePFMgr.getArtefacts(pStruct);
            if (artList != null && artList.size() != 0) {
                for (final AbstractArtefact artefact : artList) {
                    final String key = String.valueOf(artefact.getKey());
                    final String title = StringHelper.escapeHtml(artefact.getTitle());

                    final Link iconLink = LinkFactory.createCustomLink("arte_" + key, LINK_CMD_OPEN_ARTEFACT, "", Link.NONTRANSLATED, vC, this);
                    iconLink.setCustomEnabledLinkCSS("b_small_icon b_open_icon");
                    iconLink.setUserObject(pStruct);

                    final Link titleLink = LinkFactory.createCustomLink("arte_t_" + key, LINK_CMD_OPEN_ARTEFACT, title, Link.NONTRANSLATED, vC, this);
                    titleLink.setUserObject(pStruct);

                    final TOCElement actualTOCEl = new TOCElement(level, "artefact", titleLink, iconLink, null, null);
                    tocList.add(actualTOCEl);
                }
            }
        }

        final List<PortfolioStructure> childs = ePFMgr.loadStructureChildren(pStruct);
        if (childs != null && childs.size() != 0) {
            for (final PortfolioStructure portfolioStructure : childs) {
                String type = "";
                if (portfolioStructure instanceof EPPage) {
                    type = CONST_FOR_VC_STYLE_PAGE;
                } else {
                    // a structure element
                    type = CONST_FOR_VC_STYLE_STRUCT;
                }

                final String key = String.valueOf(portfolioStructure.getKey());
                final String title = StringHelper.escapeHtml(portfolioStructure.getTitle());

                final Link iconLink = LinkFactory.createCustomLink("portstruct" + key, LINK_CMD_OPEN_STRUCT, "", Link.NONTRANSLATED, vC, this);
                iconLink.setCustomEnabledLinkCSS("b_small_icon b_open_icon");
                iconLink.setUserObject(portfolioStructure);

                final Link titleLink = LinkFactory.createCustomLink("portstruct_t_" + key, LINK_CMD_OPEN_STRUCT, title, Link.NONTRANSLATED, vC, this);
                titleLink.setUserObject(portfolioStructure);

                Link commentLink = null;
                if (portfolioStructure instanceof EPPage && secCallback.canCommentAndRate()) {
                    final UserCommentsCount comments = getUserCommentsCount(portfolioStructure);
                    final String count = comments == null ? "0" : comments.getCount().toString();
                    final String label = translate("commentLink", new String[] { count });
                    commentLink = LinkFactory.createCustomLink("commentLink" + key, LINK_CMD_OPEN_COMMENTS, label, Link.NONTRANSLATED, vC, this);
                    commentLink.setCustomEnabledLinkCSS("b_comments");
                    commentLink.setUserObject(portfolioStructure);
                }

                // prefetch children to keep reference on them
                final List<TOCElement> tocChildList = new ArrayList<TOCElement>();
                buildTOCModel(portfolioStructure, tocChildList, level, withArtefacts);
                final TOCElement actualTOCEl = new TOCElement(level, type, titleLink, iconLink, commentLink, tocChildList);
                tocList.add(actualTOCEl);

                if (tocChildList.size() != 0) {
                    tocList.addAll(tocChildList);
                }
            }
        }
    }

    protected UserCommentsCount getUserCommentsCount(final PortfolioStructure portfolioStructure) {
        if (commentCounts == null || commentCounts.isEmpty()) {
            return null;
        }

        final String keyStr = portfolioStructure.getKey().toString();
        for (final UserCommentsCount commentCount : commentCounts) {
            if (keyStr.equals(commentCount.getSubPath())) {
                return commentCount;
            }
        }
        return null;
    }

    /**
	 */
    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == artOnOffLink) {
            artOnOffLink.setCustomDisplayText(translate("artOnOffLink." + !(Boolean) artOnOffLink.getUserObject()));
            init(ureq, !(Boolean) artOnOffLink.getUserObject());
        } else if (source instanceof Link) {
            // could be a TOC-Link
            final Link link = (Link) source;
            final String cmd = link.getCommand();
            final PortfolioStructure parentStruct = (PortfolioStructure) link.getUserObject();
            if (cmd.equals(LINK_CMD_OPEN_STRUCT)) {
                fireEvent(ureq, new EPStructureEvent(EPStructureEvent.SELECT, parentStruct));
            } else if (cmd.equals(LINK_CMD_OPEN_ARTEFACT)) {
                // open the parent structure
                fireEvent(ureq, new EPStructureEvent(EPStructureEvent.SELECT, parentStruct));
            } else if (cmd.equals(LINK_CMD_OPEN_COMMENTS)) {
                fireEvent(ureq, new EPStructureEvent(EPStructureEvent.SELECT_WITH_COMMENTS, parentStruct));
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
