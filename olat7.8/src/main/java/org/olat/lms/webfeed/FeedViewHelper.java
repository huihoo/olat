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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.course.CourseModule;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.webfeed.blog.portfolio.LiveBlogArtefactHandler;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * The object provides helper methods for feed views. This is required since the feed urls are user dependent.
 * <P>
 * Initial Date: Mar 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedViewHelper {

    private static final Logger log = LoggerHelper.getLogger();

    // display 5 items per default
    private int itemsPerPage = 5;
    private final Feed feed;
    private final Identity identity;
    private Translator translator;
    private Locale locale;
    private String baseUri, feedUrl;

    private final String nodeId;
    private final Long courseId;
    private static final String MEDIA_DIR = Path.MEDIA_DIR;
    // Per default show the first page
    private int page = 0;
    private List<Item> cachedItems;
    private FeedSecurityCallback callback;
    private RepositoryService repositoryService;
    private final static String MIMY_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * Use this constructor for localized content (like e.g. date formats)
     * 
     * @param feed
     * @param identityKey
     * @param locale
     */
    protected FeedViewHelper(final Feed feed, final Identity identity, final Translator translator, final Long courseId, final String nodeId,
            final FeedSecurityCallback callback, RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        this.feed = feed;
        this.identity = identity;
        this.translator = translator;
        if (translator != null) {
            this.locale = translator.getLocale();
        } else {
            this.locale = I18nModule.getDefaultLocale();
        }
        this.courseId = courseId;
        this.nodeId = nodeId;
        this.callback = callback;
        this.cachedItems = feed.getFilteredItems(callback, identity);
        this.setURIs();
    }

    /**
     * Use this constructor if no internationalization properties are required
     * 
     * @param feed
     * @param identityKey
     */
    protected FeedViewHelper(final Feed feed, final Identity identity, final Long courseId, final String nodeId, Translator translator,
            RepositoryService repositoryService) {
        this(feed, identity, translator, courseId, nodeId, null, repositoryService);
    }

    /**
     * Set the base uri of an internal feed. <br>
     * E.g http://my.olat.org/olat/feed/ident/[IDKEY]/token/[TOKEN]/id/[ORESID]
     */
    public void setURIs() {
        // Set feed base URI for internal feeds
        if (feed.isInternal()) {
            baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, courseId, nodeId);
            feedUrl = baseUri + "/" + FeedManager.RSS_FEED_NAME;
        } else if (feed.isExternal()) {
            // The base uri is needed for dispatching the picture
            baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, courseId, nodeId);
            feedUrl = feed.getExternalFeedUrl();
        } else {
            // feed is undefined
            // The base uri is needed for dispatching the picture
            baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, courseId, nodeId);
            feedUrl = null;
            feed.setExternalImageURL(null);
        }
    }

    /**
     * @return The iTunes subscription url
     */
    public String getITunesUrl() {
        String iTunesfeed = null;
        if (StringHelper.containsNonWhitespace(feedUrl)) {
            try {
                final URL url = new URL(feedUrl);
                iTunesfeed = "itpc://" + url.getHost() + url.getPath();
            } catch (final MalformedURLException e) {
                log.warn("Malformed podcast URL: " + feedUrl, e);
            }
        }
        return iTunesfeed;
    }

    /**
     * @return The Yahoo! subscription url
     */
    public String getYahooUrl() {
        return "http://add.my.yahoo.com/rss?url=" + feedUrl;
    }

    /**
     * @return The Google subscription url
     */
    public String getGoogleUrl() {
        return "http://fusion.google.com/add?feedurl=" + feedUrl;
    }

    /**
     * @return The feed url
     */
    public String getFeedUrl() {
        return feedUrl;
    }

    /**
     * @param item
     * @return The media url of the item
     */
    public String getMediaUrl(final Item item) {
        String file = null;
        final Enclosure enclosure = item.getEnclosure();
        if (enclosure != null) {
            if (feed.isExternal()) {
                file = item.getEnclosure().getExternalUrl();
            } else if (feed.isInternal()) {
                file = this.baseUri + "/" + item.getGuid() + "/" + MEDIA_DIR + "/" + enclosure.getFileName();
            }
        }
        return file;
    }

    /**
     * @return The feed image url
     */
    public String getImageUrl() {
        String imageUrl = null;
        if (feed.getImageName() != null) {
            imageUrl = baseUri + "/" + MEDIA_DIR + "/" + feed.getImageName();
        } else if (feed.getExternalImageURL() != null) {
            // If there's no custom image and the feed contains an image, use it!
            imageUrl = feed.getExternalImageURL();
        }
        return imageUrl;
    }

    /**
     * @param enclosure
     * @return The media type (audio or video)
     */
    public String getMediaType(final Enclosure enclosure) {
        String mediaType = null;
        if (enclosure != null) {
            // type is like 'video/mpeg' or 'audio/mpeg'
            String type = enclosure.getType();
            /*
             * Issue 6857 - in some cases (depends on browser, operating system etc.) uploaded file (video or audio) for podcast gets not correct MIME type (e.g.
             * application/octet-stream for *.flv file). MIME type is also used to set correct size of player for video and audio files for podcast.This has been solved
             * here to set correct MIME type based on file extension. Another possibilities are to determine correct MIME type also on content of file - for example
             * external library http://tika.apache.org/ etc.
             */
            if (MIMY_TYPE_APPLICATION_OCTET_STREAM.equals(type)) {
                type = WebappHelper.getMimeType(enclosure.getFileName());
            }
            if (type != null) {
                type = type.split("/")[0];
                if ("audio".equals(type) || "video".equals(type)) {
                    mediaType = type;
                }
            }
        }
        return mediaType;
    }

    /**
     * @param item
     * @return The formatted last modified date string of the item
     */
    public String getLastModified(final Item item) {
        String lastModified = null;
        final Date date = item.getLastModified();
        if (date != null) {
            lastModified = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
        }
        return lastModified;
    }

    /**
     * @param item
     * @return The formatted last modified date string of the item
     */
    private String getPublishDate(final Item item) {
        String publishDate = null;
        final Date date = item.getPublishDate();
        if (date != null) {
            publishDate = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
        }
        return publishDate;
    }

    /**
     * @param item
     * @return Information about publication date and author
     */
    private String getPublishInfo(final Item item) {
        String info = null;
        final String date = getPublishDate(item);
        final String author = item.getAuthor();
        if (author != null) {
            if (date != null) {
                info = translator.translate("feed.published.by.on", new String[] { author, date });
            } else {
                info = translator.translate("feed.published.by", new String[] { author });
            }
        } else {
            if (date != null) {
                info = translator.translate("feed.published.on", new String[] { date });
            } else {
                // no publication info available
            }
        }
        return info;
    }

    /**
     * @param item
     * @return Information about the item. Is it draft, scheduled or published?
     */
    public String getInfo(final Item item) {
        String info = null;
        if (item.isDraft()) {
            info = translator.translate("feed.item.draft");
        } else if (item.isScheduled()) {
            info = translator.translate("feed.item.scheduled.for", new String[] { getPublishDate(item) });
        } else if (item.isPublished()) {
            info = getPublishInfo(item);
        }
        return info;
    }

    public boolean isModified(final Item item) {
        return item.getModifierKey() > 0 && StringHelper.containsNonWhitespace(item.getModifier());
    }

    /**
     * @param item
     * @return Information about the item. Is it draft, scheduled or published?
     */
    public String getModifierInfo(final Item item) {
        if (isModified(item)) {
            final String date = getLastModified(item);
            final String modifier = item.getModifier();
            return translator.translate("feed.modified.by.on", new String[] { modifier, date });
        }
        return null;
    }

    /**
     * @return The formatted last modified date string of the feed
     */
    public String getLastModified() {
        String lastModified = null;
        final Date date = feed.getLastModified();
        if (date != null) {
            lastModified = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
        }
        return lastModified;
    }

    /**
     * @return The jump in link
     */
    public String getJumpInLink() {
        StringBuffer jumpInLink = new StringBuffer();

        if (courseId != null && nodeId != null) {
            final OLATResourceable oresCourse = OLATResourceManager.getInstance().findResourceable(courseId, CourseModule.getCourseTypeName());
            final RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(oresCourse, false);
            jumpInLink.append(Settings.getServerContextPathURI()).append("/url/RepositoryEntry/").append(repositoryEntry.getKey()).append("/CourseNode/").append(nodeId);
            log.debug("Feed: Jumpin URL Case1 jumpInLink=" + jumpInLink);
        } else {
            final RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(feed, false);
            if (repositoryEntry != null) {
                jumpInLink.append(Settings.getServerContextPathURI()).append("/url/RepositoryEntry/").append(repositoryEntry.getKey());
                log.debug("Feed: Jumpin URL Case2 jumpInLink=" + jumpInLink);
            } else {
                // its a liveblog-feed
                final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
                String feedBP = LiveBlogArtefactHandler.LIVEBLOG + feed.getResourceableId() + "]";
                final List<ContextEntry> ceList = bCF.createCEListFromString(feedBP);
                jumpInLink.append(bCF.getAsURIString(ceList, true));
                log.debug("Feed: Jumpin URL Case3 jumpInLink=" + jumpInLink);
            }
        }
        return jumpInLink.toString();
    }

    /**
     * @param item
     * @return The item description with media file paths that are dispatchable by the FeedMediaDispatcher
     */
    public String getItemDescriptionForBrowser(final Item item) {
        String itemDescription = item.getDescription();
        if (itemDescription != null) {
            if (feed.isExternal()) {
                // Apply xss filter for security reasons. Only necessary for external
                // feeds (e.g. to not let them execute JS code in our OLAT environment)
                final Filter xssFilter = FilterFactory.getXSSFilter(itemDescription.length() + 1);
                itemDescription = xssFilter.filter(itemDescription);
            } else {
                // Add relative media base to media elements to display internal media
                // files
                final String basePath = baseUri + "/" + item.getGuid();
                final Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
                itemDescription = mediaUrlFilter.filter(itemDescription);
            }
        }
        return itemDescription;
    }

    /**
     * @param item
     * @return The item content with media file paths that are dispatchable by the FeedMediaDispatcher
     */
    public String getItemContentForBrowser(final Item item) {
        String itemContent = item.getContent();
        if (itemContent != null) {
            if (feed.isExternal()) {
                // Apply xss filter for security reasons. Only necessary for external
                // feeds (e.g. to not let them execute JS code in our OLAT environment)
                final Filter xssFilter = FilterFactory.getXSSFilter(itemContent.length() + 1);
                itemContent = xssFilter.filter(itemContent);
            } else {
                // Add relative media base to media elements to display internal media
                // files
                final String basePath = baseUri + "/" + item.getGuid();
                final Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
                itemContent = mediaUrlFilter.filter(itemContent);
            }
        }
        return itemContent;
    }

    /**
     * @return The feed description with dispatchable media file paths
     */
    public String getFeedDescriptionForBrowser() {
        final Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUri);
        return mediaUrlFilter.filter(feed.getDescription());
    }

    /* Used for paging */

    public void setItemsPerPage(final int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Show older items, meaning go to the next page.
     */
    public void olderItems() {
        if (hasOlderItems()) {
            page++;
        }
    }

    /**
     * @return True there are newer items to display
     */
    public boolean hasOlderItems() {
        return cachedItems.size() > itemsPerPage * (page + 1);
    }

    /**
     * Show newer items, meaning go to the previous page.
     */
    public void newerItems() {
        page--;
        if (page < 0) {
            page = 0;
        }
    }

    /**
     * Go to the startpage
     */
    public void startpage() {
        page = 0;
    }

    /**
     * @return True if there are newer items to display
     */
    public boolean hasNewerItems() {
        return page > 0;
    }

    /**
     * @param callback
     * @return The items count of all displayed (accessible) items
     */
    public int itemsCount(final FeedSecurityCallback callback) {
        if (cachedItems == null) {
            cachedItems = feed.getFilteredItems(callback, identity);
        }
        return cachedItems.size();
    }

    /**
     * @return The items to be displayed on the current page
     */
    public List<Item> getItems(final FeedSecurityCallback callback) {
        final List<Item> itemsOnPage = new ArrayList<Item>(itemsPerPage);
        if (cachedItems == null) {
            cachedItems = feed.getFilteredItems(callback, identity);
        }
        final int start = page * itemsPerPage;
        final int end = Math.min(cachedItems.size(), start + itemsPerPage);
        for (int i = start; i < end; i++) {
            itemsOnPage.add(cachedItems.get(i));
        }
        return itemsOnPage;
    }

    /**
     * @param selectedItems
     */
    public void setSelectedItems(final List<Item> selectedItems) {
        this.cachedItems = selectedItems;
        // go to the first page
        page = 0;
    }

    /**
     * Removes the item from the current selection of items
     * 
     * @param item
     *            The item to remove
     */
    public void removeItem(final Item item) {
        cachedItems.remove(item);
    }

    /**
     * Adds the item to the current selection of items.
     * 
     * @param item
     *            The item to add
     */
    public void addItem(final Item item) {
        if (!cachedItems.contains(item)) {
            cachedItems.add(item);
        }
        Collections.sort(cachedItems, new ItemPublishDateComparator());
    }

    /**
     * Update the given item in the current selection of items. The code will replace the item with the same GUID in the current selection of items.
     * 
     * @param item
     *            The item to update
     */
    public void updateItem(final Item item) {
        if (cachedItems.contains(item)) {
            // Remove old version first. Not necessarily the same on object level
            // since item overrides the equal method
            cachedItems.remove(item);
        }
        addItem(item);
    }

    /**
     * Resets the item selection to all accessible items of the feed
     * 
     * @param callback
     */
    public void resetItems(final FeedSecurityCallback callback) {
        cachedItems = feed.getFilteredItems(callback, identity);
    }

    /**
     * Check if the current user is the author of this feed item
     * 
     * @param item
     * @return
     */
    public boolean isAuthor(final Item item) {
        if (item != null) {
            if (item.getAuthorKey() == identity.getKey().longValue()) {
                return true;
            }
        }
        return false;
    }

}
