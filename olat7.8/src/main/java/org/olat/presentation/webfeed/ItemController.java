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
package org.olat.presentation.webfeed;

import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commentandrate.CommentAndRatingService;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedLoggingAction;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.lms.webfeed.FeedViewHelper;
import org.olat.lms.webfeed.Item;
import org.olat.presentation.commentandrate.UserCommentsAndRatingsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.date.DateComponentFactory;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.dispatcher.ClassPathStaticDispatcher;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * This Controller is responsible for displaying a singel blog item for reading (and maybe commenting later).
 * <P>
 * Initial Date: Sep 30, 2009 <br>
 * 
 * @author gwassmann
 */
public class ItemController extends BasicController implements Activateable {
    public static final String ACTIVATION_KEY_COMMENTS = "comments";
    private final Link backLink;
    private UserCommentsAndRatingsController commentsCtr;

    /**
     * @param ureq
     * @param wControl
     * @param displayConfig
     */
    public ItemController(final UserRequest ureq, final WindowControl wControl, final Item item, final Feed feed, final FeedViewHelper helper,
            final FeedUIFactory uiFactory, final FeedSecurityCallback callback, final Link editButton, final Link deleteButton, final Controller artefactLink,
            FeedItemDisplayConfig displayConfig) {
        super(ureq, wControl);
        setTranslator(uiFactory.getTranslator());
        final VelocityContainer vcItem = uiFactory.createItemVelocityContainer(this);
        vcItem.contextPut("item", item);
        vcItem.contextPut("feed", feed);
        vcItem.contextPut("helper", helper);
        vcItem.contextPut("callback", callback);
        if (feed.isInternal()) {
            if (editButton != null) {
                vcItem.put("editButton", editButton);
            }
            if (deleteButton != null) {
                vcItem.put("deleteButton", deleteButton);
            }
            if (artefactLink != null) {
                vcItem.put("artefactLink", artefactLink.getInitialComponent());
            }
        }
        backLink = LinkFactory.createLinkBack(vcItem, this);
        // Add static path for resource delivery and js player for media
        final String baseStaticPath = ClassPathStaticDispatcher.getInstance().getMapperBasePath(FeedUIFactory.class);
        vcItem.contextPut("baseStaticPath", baseStaticPath);
        // Add date component
        if (item.getDate() != null) {
            DateComponentFactory.createDateComponentWithYear("dateComp", item.getDate(), vcItem);
        }
        // Add rating and commenting controller - only when configured
        final CommentAndRatingService commentAndRatingService = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
        if (commentAndRatingService != null && displayConfig.isShowCRInDetails()) {
            commentAndRatingService.init(getIdentity(), feed, item.getGuid(), callback.mayEditMetadata(), ureq.getUserSession().getRoles().isGuestOnly());
            commentsCtr = commentAndRatingService.createUserCommentsAndRatingControllerExpandable(ureq, getWindowControl());
            listenTo(commentsCtr);
            vcItem.put("commentsAndRating", commentsCtr.getInitialComponent());
        }
        //
        this.putInitialPanel(vcItem);
        // do logging
        ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_READ, getClass(), LoggingResourceable.wrap(item));
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == backLink) {
            fireEvent(ureq, Event.BACK_EVENT);
        }
    }

    /**
	 */
    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        if (ACTIVATION_KEY_COMMENTS.equals(viewIdentifier)) {
            // show comments
            if (commentsCtr != null) {
                commentsCtr.expandComments(ureq);
            }
        }
    }

}
