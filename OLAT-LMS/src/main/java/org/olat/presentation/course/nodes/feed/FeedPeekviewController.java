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
package org.olat.presentation.course.nodes.feed;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.resource.OLATResource;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.lms.webfeed.FeedViewHelper;
import org.olat.lms.webfeed.Item;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.OlatCmdEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.webfeed.FeedUIFactory;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> The feed peekview controller displays the configurable amount of the most recent feed items.
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>OlatCmdEvent to notify that a jump to the course node is desired</li>
 * </ul>
 * <p>
 * Initial Date: 29.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class FeedPeekviewController extends BasicController implements Controller {
    // the current course node id
    private final String nodeId;

    /**
     * Constructor for the feed peekview controller
     * 
     * @param olatResource
     *            The feed olat resource
     * @param ureq
     *            the User request
     * @param wControl
     *            The window control
     * @param callback
     *            the feed security callback
     * @param courseId
     *            The course ID in which the feed is used
     * @param nodeId
     *            The current course node ID
     * @param feedUIFactory
     *            The feed UI factory
     * @param itemsToDisplay
     *            number of items to be displayed, must be > 0
     * @param wrapperCssClass
     *            An optional wrapper CSS class that is added to the wrapper DIV to style icons etc
     */
    public FeedPeekviewController(final OLATResource olatResource, final UserRequest ureq, final WindowControl wControl, final FeedSecurityCallback callback,
            final Long courseId, final String nodeId, final FeedUIFactory feedUIFactory, final int itemsToDisplay, final String wrapperCssClass) {
        super(ureq, wControl);
        this.nodeId = nodeId;
        final FeedManager feedManager = FeedManager.getInstance();
        final Feed feed = feedManager.getFeed(olatResource);

        final VelocityContainer peekviewVC = createVelocityContainer("peekview");
        peekviewVC.contextPut("wrapperCssClass", wrapperCssClass != null ? wrapperCssClass : "");
        // add gui helper
        final FeedViewHelper helper = feedManager.createFeedViewHelper(feed, getIdentity(), getTranslator(), courseId, nodeId, callback);
        peekviewVC.contextPut("helper", helper);
        // add items, only as many as configured
        final List<Item> allItems = feed.getFilteredItems(callback, getIdentity());
        final List<Item> items = new ArrayList<Item>();
        for (int i = 0; i < allItems.size(); i++) {
            if (items.size() == itemsToDisplay) {
                break;
            }
            // add item itself if published
            final Item item = allItems.get(i);
            if (item.isPublished()) {
                items.add(item);
                // add link to item
                // Add link to jump to course node
                final Link nodeLink = LinkFactory.createLink("nodeLink_" + item.getGuid(), peekviewVC, this);
                nodeLink.setCustomDisplayText(item.getTitle());
                nodeLink.setCustomEnabledLinkCSS("b_with_small_icon_left o_feed_item_icon o_gotoNode");
                nodeLink.setUserObject(item.getGuid());
            }
        }
        peekviewVC.contextPut("items", items);
        // Add link to show all items (go to node)
        final Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", peekviewVC, this);
        allItemsLink.setCustomEnabledLinkCSS("b_float_right");
        // Add Formatter for proper date formatting
        peekviewVC.contextPut("formatter", Formatter.getInstance(getLocale()));
        //
        this.putInitialPanel(peekviewVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source instanceof Link) {
            final Link nodeLink = (Link) source;
            final String itemId = (String) nodeLink.getUserObject();
            if (itemId == null) {
                fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
            } else {
                fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId + "/" + itemId));
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
