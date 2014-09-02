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
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedLoggingAction;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.FeedSecurityCallback;
import org.olat.lms.webfeed.FeedViewHelper;
import org.olat.lms.webfeed.Item;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.notification.ContextualSubscriptionController;
import org.olat.presentation.webfeed.blog.BlogNotificationTypeHandler;
import org.olat.presentation.webfeed.podcast.PodcastNotificationTypeHandler;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;

/**
 * This is the main feed layout controller. It handles everything from adding episodes to changing title and description.
 * <P>
 * Initial Date: Feb 5, 2009 <br>
 * 
 * @author gwassmann
 */
// ClosableModalController is deprecated. No alternative implemented.
@SuppressWarnings("deprecation")
public class FeedMainController extends BasicController implements Activateable, GenericEventListener {

    private static final FeedManager feedManager = FeedManager.getInstance();
    private Feed feed;
    private Link editFeedButton;
    private CloseableModalController cmc;
    private FormBasicController feedFormCtr;
    private VelocityContainer vcMain, vcInfo, vcRightCol;
    private ItemsController itemsCtr;
    private LockResult lock;
    private final FeedViewHelper helper;
    private DisplayFeedUrlController displayUrlCtr;
    private final FeedUIFactory uiFactory;
    private final FeedSecurityCallback callback;
    // needed for comparison
    private String oldFeedUrl;
    private final SubscriptionContext subscriptionContext;
    private ContextualSubscriptionController contextualSubscriptionController;

    /**
     * Constructor for learning resource (not course nodes)
     * 
     * @param ores
     * @param ureq
     * @param wControl
     * @param previewMode
     *            Indicates that the content will only be displayed in preview and no editing functionality is enabled.
     */
    public FeedMainController(final OLATResourceable ores, final UserRequest ureq, final WindowControl wControl, final FeedUIFactory uiFactory,
            final FeedSecurityCallback callback) {
        this(ores, ureq, wControl, null, null, uiFactory, callback, null);
    }

    /**
     * Constructor for course node
     * 
     * @param ores
     * @param ureq
     * @param wControl
     * @param previewMode
     *            Indicates that the content will only be displayed in preview and no editing functionality is enabled.
     */
    public FeedMainController(final OLATResourceable ores, final UserRequest ureq, final WindowControl wControl, final Long courseId, final String nodeId,
            final FeedUIFactory uiFactory, final FeedSecurityCallback callback, FeedItemDisplayConfig displayConfig) {
        super(ureq, wControl);
        this.uiFactory = uiFactory;
        this.callback = callback;
        this.subscriptionContext = callback.getSubscriptionContext();

        setTranslator(uiFactory.getTranslator());
        feed = feedManager.getFeed(ores);
        helper = feedManager.createFeedViewHelper(feed, getIdentity(), uiFactory.getTranslator(), courseId, nodeId, callback);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), feed);
        display(ureq, wControl, displayConfig);

        if (subscriptionContext != null) {
            final PublisherData data = new PublisherData(OresHelper.calculateTypeName(uiFactory.getClass()), ores.getResourceableId().toString());
            contextualSubscriptionController = new ContextualSubscriptionController(ureq, getWindowControl(), subscriptionContext, data);

            listenTo(contextualSubscriptionController);
            vcMain.put("subscription", contextualSubscriptionController.getInitialComponent());
        }

        final BusinessControl bc = getWindowControl().getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        if (ce != null) {
            String contextType = getContextTypeFromContextEntry(ce);
            String contextId = getContextIdFromContextEntry(ce);
            if (BlogNotificationTypeHandler.BLOG_SOURCE_TYPE.equals(contextType)) {
                for (Item item : feed.getItems()) {
                    if (CodeHelper.getForeverUniqueIDFromGlobalForeverUniqueID(item.getGuid()).equals(contextId)) {
                        itemsCtr.activate(ureq, item);
                        break;
                    }
                }
            } else if (PodcastNotificationTypeHandler.PODCAST_SOURCE_TYPE.equals(contextType)) {
                // TODO: implement direct jump to episode
            }
        }

        // do logging
        ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_READ, getClass(), LoggingResourceable.wrap(feed));
    }

    private String getContextTypeFromContextEntry(ContextEntry contextEntry) {
        return contextEntry.toString().substring(1, contextEntry.toString().indexOf(":"));
    }

    private String getContextIdFromContextEntry(ContextEntry contextEntry) {
        return contextEntry.toString().substring(contextEntry.toString().indexOf(":") + 1, contextEntry.toString().length() - 1);
    }

    /**
     * Sets up the velocity container for displaying the view
     * 
     * @param ores
     * @param ureq
     * @param wControl
     * @param previewMode
     * @param isCourseNode
     */
    private void display(final UserRequest ureq, final WindowControl wControl, FeedItemDisplayConfig displayConfig) {
        vcMain = createVelocityContainer("feed_main");

        vcInfo = uiFactory.createInfoVelocityContainer(this);
        vcInfo.contextPut("feed", feed);
        vcInfo.contextPut("helper", helper);

        vcRightCol = uiFactory.createRightColumnVelocityContainer(this);
        vcMain.put("rightColumn", vcRightCol);

        // The current user has edit rights if he/she is an administrator or an
        // owner of the resource.
        if (callback.mayEditMetadata()) {
            editFeedButton = LinkFactory.createButtonSmall("feed.edit", vcMain, this);
        }
        vcMain.contextPut("callback", callback);

        displayUrlCtr = new DisplayFeedUrlController(ureq, wControl, feed, helper, uiFactory.getTranslator());
        listenTo(displayUrlCtr);
        vcInfo.put("feedUrlComponent", displayUrlCtr.getInitialComponent());

        vcMain.put("info", vcInfo);

        itemsCtr = new ItemsController(ureq, wControl, feed, helper, uiFactory, callback, vcRightCol, displayConfig, subscriptionContext);
        listenTo(itemsCtr);
        vcMain.put("items", itemsCtr.getInitialComponent());

        this.putInitialPanel(vcMain);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        feedManager.releaseLock(lock);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, feed);
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == editFeedButton) {
            lock = feedManager.acquireLock(feed, ureq.getIdentity());
            if (lock.isSuccess()) {
                if (feed.isExternal()) {
                    oldFeedUrl = feed.getExternalFeedUrl();
                    feedFormCtr = new ExternalFeedFormController(ureq, getWindowControl(), feed, uiFactory.getTranslator());
                } else {
                    // Default for podcasts is that they are edited within OLAT
                    feedFormCtr = new FeedFormController(ureq, getWindowControl(), feed, uiFactory);
                }
                activateModalDialog(feedFormCtr);
            } else {
                showInfo("feed.is.being.edited.by", lock.getOwner().getName());
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == cmc) {
            if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
                removeAsListenerAndDispose(cmc);
                cmc = null;
                removeAsListenerAndDispose(feedFormCtr);
                feedFormCtr = null;
                // If the user cancels the first time after deciding to subscribe to
                // an external feed, undo his decision
                if (feed.isExternal()) {
                    if (oldFeedUrl == null || "".equals(oldFeedUrl)) {
                        feed = feedManager.updateFeedMode(null, feed);
                        itemsCtr.makeInternalAndExternalButtons();
                    }
                }
                // release lock
                feedManager.releaseLock(lock);
            }
        } else if (source == feedFormCtr) {
            if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
                // Dispose the cmc and the feedFormCtr.
                cmc.deactivate();
                removeAsListenerAndDispose(cmc);
                cmc = null;

                if (event.equals(Event.CHANGED_EVENT)) {
                    vcInfo.setDirty(true);
                    // For external podcasts, set the feed to undefined if the feed url
                    // has been set empty.
                    if (feed.isExternal()) {
                        final String newFeed = feed.getExternalFeedUrl();
                        displayUrlCtr.setUrl(newFeed);
                        if (newFeed == null) {
                            feed.setExternal(null);
                            itemsCtr.makeInternalAndExternalButtons();
                            // No more episodes to display
                            itemsCtr.resetItems(ureq, feed);
                        } else if (!newFeed.equals(oldFeedUrl)) {
                            // Set the episodes dirty since the feed url changed.
                            itemsCtr.resetItems(ureq, feed);
                        }
                        // Set the URIs correctly
                        helper.setURIs();
                    } else {
                        if (feedFormCtr instanceof FeedFormController) {
                            final FeedFormController internalFormCtr = (FeedFormController) feedFormCtr;
                            if (internalFormCtr.imageDeleted()) {
                                feedManager.deleteImage(feed);
                            } else {
                                // set the image
                                FileElement image = null;
                                image = internalFormCtr.getFile();
                                feedManager.setImage(image, feed);
                            }
                        } else {
                            // it's an external feed form, nothing to do in this case
                        }
                    }
                    // Eventually update the feed
                    feed = feedManager.updateFeedMetadata(feed);
                    // Dispose the feedFormCtr
                    removeAsListenerAndDispose(feedFormCtr);
                    feedFormCtr = null;
                    // do logging
                    ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(), LoggingResourceable.wrap(feed));
                } else if (event.equals(Event.CANCELLED_EVENT)) {
                    // If the user cancels the first time after deciding to subscribe to
                    // an external feed, undo his decision
                    if (feed.isExternal()) {
                        if (oldFeedUrl == null || "".equals(oldFeedUrl)) {
                            feed = feedManager.updateFeedMode(null, feed);
                            itemsCtr.makeInternalAndExternalButtons();
                        }
                    }
                }
                // release the lock
                feedManager.releaseLock(lock);
            }
        } else if (source == itemsCtr && event.equals(ItemsController.HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT)) {
            oldFeedUrl = feed.getExternalFeedUrl();
            feedFormCtr = new ExternalFeedFormController(ureq, getWindowControl(), feed, uiFactory.getTranslator());
            activateModalDialog(feedFormCtr);
        } else if (source == itemsCtr && event.equals(ItemsController.FEED_INFO_IS_DIRTY_EVENT)) {
            vcInfo.setDirty(true);
        }
    }

    /**
     * @param controller
     *            The <code>FormBasicController</code> to be displayed in the modal dialog.
     */
    private void activateModalDialog(final FormBasicController controller) {
        listenTo(controller);
        cmc = new CloseableModalController(getWindowControl(), translate("close"), controller.getInitialComponent());
        listenTo(cmc);
        cmc.activate();
    }

    /**
	 */
    @Override
    public void activate(final UserRequest ureq, final String itemId) {
        final int index = feed.getItemIds().indexOf(itemId);
        if (index >= 0) {
            final Item item = feed.getItems().get(index);
            itemsCtr.activate(ureq, item);
        }
    }

    /**
	 */
    @Override
    public void event(final Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
            // make sure it is our course (actually not needed till now, since we
            // registered only to one event, but good style.
            if (ojde.targetEquals(feed, true)) {
                dispose();
            }
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
