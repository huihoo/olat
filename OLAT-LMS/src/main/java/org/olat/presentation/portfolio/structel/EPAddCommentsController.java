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

import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.commentandrate.CommentAndRatingService;
import org.olat.presentation.commentandrate.UserCommentsAndRatingsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Button which popup the comments and ratings controller
 * <P>
 * Initial Date: 16 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPAddCommentsController extends BasicController {

    private final PortfolioStructure element;
    private final CommentAndRatingService commentAndRatingService;

    private final Link commentLink;
    private final VelocityContainer vc;
    private UserCommentsAndRatingsController commentsAndRatingCtr;
    private CloseableModalWindowWrapperController commentsBox;

    public EPAddCommentsController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructure element) {
        super(ureq, wControl);

        this.element = element;

        String subPath = null;
        PortfolioStructure root = element;
        if (element.getRoot() != null) {
            root = element.getRoot();
            subPath = element.getKey().toString();
        }
        commentAndRatingService = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
        commentAndRatingService.init(getIdentity(), root.getOlatResource(), subPath, false, ureq.getUserSession().getRoles().isGuestOnly());

        vc = createVelocityContainer("commentLink");

        commentLink = LinkFactory.createLink("commentLink", vc, this);
        commentLink.setCustomEnabledLinkCSS("b_eportfolio_comment_link b_comments");
        final Long numberOfComments = commentAndRatingService.getUserCommentsManager().countComments();
        commentLink.setCustomDisplayText(translate("commentLink", new String[] { numberOfComments.toString() }));

        putInitialPanel(vc);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (commentLink == source) {
            popUpCommentBox(ureq);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (commentsBox == source) {
            final Long numberOfComments = commentAndRatingService.getUserCommentsManager().countComments();
            commentLink.setCustomDisplayText(translate("commentLink", new String[] { numberOfComments.toString() }));
        }
    }

    private void popUpCommentBox(final UserRequest ureq) {
        if (commentsAndRatingCtr == null) {
            commentsAndRatingCtr = commentAndRatingService.createUserCommentsAndRatingControllerExpandable(ureq, getWindowControl());
            commentsAndRatingCtr.addUserObject(element);
            commentsAndRatingCtr.expandComments(ureq);
            listenTo(commentsAndRatingCtr);
        }
        final String title = translate("commentLink", new String[] { element.getTitle() });
        commentsBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, commentsAndRatingCtr.getInitialComponent(), "addComment"
                + element.getKey());
        listenTo(commentsBox);
        commentsBox.activate();
    }
}
