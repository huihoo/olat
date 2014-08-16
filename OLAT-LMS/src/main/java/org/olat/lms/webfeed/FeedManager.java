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
package org.olat.lms.webfeed;

import java.io.File;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commentandrate.CommentAndRatingService;
import org.olat.lms.commentandrate.CommentAndRatingServiceFactory;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The <code>FeedManager</code> singleton is responsible for dealing with feed resources.
 * <P>
 * Initial Date: Feb 11, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedManager extends BasicManager implements CommentAndRatingServiceFactory {

    @Autowired
    LockingService lockingService;
    @Autowired
    RepositoryService repositoryService;

    protected static FeedManager INSTANCE;

    public static final String ITEMS_DIR = "items";
    protected static final String FEED_FILE_NAME = "feed.xml";
    protected static final String ITEM_FILE_NAME = "item.xml";
    protected static final String MEDIA_DIR = "media";
    public static final String RSS_FEED_NAME = "feed.rss";
    public static final String RESOURCE_NAME = "feed";

    // Definition of different kinds of feeds. By convention, the kind is a single
    // noun designating the feed. (See also getFeedKind().)
    public static final String KIND_PODCAST = "podcast";
    public static final String KIND_BLOG = "blog";

    // public static final String KIND_PHOTOBLOG = "photoblog";
    // public static final String KIND_SCREENCAST = "screencast";

    /**
     * Use this method instead of any constructor to get the singelton object.
     * 
     * @return INSTANCE
     */
    public static final FeedManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates an OLAT podcast resource
     * 
     * @return The resource
     */
    public abstract OLATResourceable createPodcastResource();

    /**
     * Creates an OLAT blog resource
     * 
     * @return The resource
     */
    public abstract OLATResourceable createBlogResource();

    /**
     * Deletes a given feed.
     * 
     * @param feed
     */
    public abstract void delete(OLATResourceable feed);

    /**
     * Copies a given feed resourcable
     * 
     * @param feed
     */
    public abstract OLATResourceable copy(OLATResourceable feed);

    /**
     * Adds the given <code>Item</code> to the <code>Feed</code>.
     * 
     * @param item
     * @param feed
     */
    public abstract void addItem(Item item, FileElement file, Feed feed, PublishEventTO publishEventTO);

    /**
     * Removes the given <code>Item</code> from the <code>Feed</code>. Its content will be deleted.
     * 
     * @param item
     * @param feed
     */
    public abstract void remove(Item item, Feed feed);

    /**
     * @param modifiedItem
     * @param feed
     */
    public abstract void updateItem(Item modifiedItem, FileElement file, Feed feed, PublishEventTO publishEventTO);

    /**
     * Update the feed source mode
     * 
     * @param external
     *            True: set to be an external feed; false: this is an internal feed; null=undefined
     * @param feed
     * @return Feed the updated feed object
     */
    public abstract Feed updateFeedMode(Boolean external, Feed feed);

    /**
     * Update the feed metadata from the given feed object
     * 
     * @param feed
     * @return
     */
    public abstract Feed updateFeedMetadata(Feed feed);

    /**
     * Load all items of the feed (from file system or the external feed)
     * 
     * @param feed
     */
    public abstract List<Item> loadItems(final Feed feed);

    /**
     * Returns the feed with the provided id or null if not found.
     * 
     * @param feed
     *            The feed to be re-read
     * @return The newly read feed (without items)
     */
    public abstract Feed getFeed(OLATResourceable feed);

    /**
     * Returns the media file of the item
     * 
     * @param id
     * @param resourceTypeName
     * @param itemId
     * @param fileName
     * @return The media resource (audio or video file of the feed item)
     */
    public abstract MediaResource createItemMediaFile(OLATResourceable feed, String itemId, String fileName);

    /**
     * Returns the media file of the feed
     * 
     * @param id
     * @param resourceTypeName
     * @param fileName
     * @return The media file of the feed
     */
    public abstract MediaResource createFeedMediaFile(OLATResourceable feed, String fileName);

    /**
     * Returns the base URI of the feed including user identity key and token if necessary.
     * 
     * @param feed
     * @param idKey
     * @return The base URI of the (RSS) feed
     */
    public abstract String getFeedBaseUri(Feed feed, Identity identity, Long courseId, String nodeId);

    /**
     * Creates the RSS feed resource.
     * 
     * @param feedId
     * @param type
     *            The resource type name
     * @param identityKey
     * @return The RSS feed as a MediaResource
     */
    public abstract MediaResource createFeedFile(OLATResourceable feed, Identity identity, Long courseId, String nodeId, final Translator translator);

    /**
     * Creates and returns a zip-file media resource of the given feed resource
     * 
     * @param resource
     * @return A zip-file media resource
     */
    public abstract MediaResource getFeedArchiveMediaResource(OLATResourceable resource);

    /**
     * Create and returns a zip-file as VFSLeaf of the given feed resourue
     * 
     * @param ores
     *            the resource
     * @return The VFSLeaf
     */
    public abstract VFSLeaf getFeedArchive(OLATResourceable ores);

    /**
     * Returns the container of the item which belongs to the feed
     * 
     * @param item
     * @param feed
     * @return The container of the item
     */
    public abstract VFSContainer getItemContainer(Item item, Feed feed);

    /**
     * Returns the media container of the item of feed
     * 
     * @param item
     * @param feed
     * @return The media container of the item
     */
    public abstract VFSContainer getItemMediaContainer(Item item, Feed feed);

    /**
     * Returns the File of the item's enclosure if it exists or null
     * 
     * @param item
     * @param feed
     * @return The enclosure media file
     */
    public abstract File getItemEnclosureFile(Item item, Feed feed);

    /**
     * Returns the container of the feed
     * 
     * @param feed
     * @return The feed container
     */
    public abstract VFSContainer getFeedContainer(OLATResourceable feed);

    /**
     * Validates a feed url.
     * 
     * @param url
     * @return valid url (rss, atom etc.)
     */
    public abstract ValidatedURL validateFeedUrl(String url, String type);

    /**
     * Releases a lock
     * 
     * @param lock
     *            The lock to be released
     */
    public abstract void releaseLock(LockResult lock);

    /**
     * Acquires the lock on the specified feed
     * 
     * @param feed
     *            The feed to be locked
     * @param identity
     *            The person who is locking the resource
     * @return The lock result
     */
    public abstract LockResult acquireLock(OLATResourceable feed, Identity identity);

    /**
     * Acquires the lock of an item
     * 
     * @param feed
     *            The item's feed
     * @param item
     *            The item to be locked
     * @param identity
     *            The person who is locking the resource
     * @return The lock result
     */
    public abstract LockResult acquireLock(OLATResourceable feed, Item item, Identity identity);

    /**
     * @param feed
     * @return True if the feed is locked
     */
    public boolean isLocked(final OLATResourceable feed) {
        return lockingService.isLocked(feed, null);
    }

    /**
     * There are different kinds of web feeds, e.g. podcasts, blogs etc. This method returns the kind of a resourceType. In contrast to the resource type name, the kind
     * is a single noun designating the feed. It might be used to get a comprehensible expression for folder or file names.
     * 
     * @param ores
     * @return The kind of the resource type
     */
    public String getFeedKind(final OLATResourceable ores) {
        String feedKind = null;
        final String typeName = ores.getResourceableTypeName();
        if (PodcastFileResource.TYPE_NAME.equals(typeName)) {
            feedKind = KIND_PODCAST;
        } else if (BlogFileResource.TYPE_NAME.equals(typeName)) {
            feedKind = KIND_BLOG;
        }
        return feedKind;
    }

    /**
     * Set the image of the feed (update handled separately)
     * 
     * @param image
     * @param feed
     */
    public abstract void setImage(FileElement image, Feed feed);

    /**
     * Delete the image of the feed
     * 
     * @param feed
     */
    public abstract void deleteImage(Feed feed);

    /**
     * Prepare the filesystem for a new item, create the item container and all necessary sub container, e.g. the media container
     * 
     * @param feed
     * @param currentItem
     * @return the container for the item
     */
    public abstract VFSContainer createItemContainer(Feed feed, Item currentItem);

    public abstract Feed readFeedFile(VFSContainer root);

    public abstract Item loadItem(VFSItem itemContainer);

    @Override
    public abstract CommentAndRatingService getCommentAndRatingService();

    public abstract Feed getFeedLight(String businessPath);

    /**
     * creates a new item and assigns a global unique id
     * 
     * @return
     */
    public Item createItem() {
        Item item = new Item();
        item.setGuid(CodeHelper.getGlobalForeverUniqueID());
        return item;
    }

    /**
     * @param feed
     * @param identity
     * @param translator
     * @param courseId
     * @param nodeId
     * @param callback
     * @return
     */
    public FeedViewHelper createFeedViewHelper(Feed feed, Identity identity, Translator translator, Long courseId, String nodeId, FeedSecurityCallback callback) {
        return new FeedViewHelper(feed, identity, translator, courseId, nodeId, callback, repositoryService);
    }

}
