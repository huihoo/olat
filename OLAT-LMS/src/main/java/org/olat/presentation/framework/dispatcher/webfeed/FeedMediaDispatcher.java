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
package org.olat.presentation.framework.dispatcher.webfeed;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.lms.course.CourseGroupsEBL;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.FeedMediaEBL;
import org.olat.lms.webfeed.InvalidPathException;
import org.olat.lms.webfeed.Path;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.Dispatcher;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Dispatch any media files belonging to a podcast which an identity is authorized to access. The media file can belong to a learning resource or a course node.
 * <p>
 * Examples: see Path constructor
 * <p>
 * Initial Date: Mar 10, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedMediaDispatcher implements Dispatcher {

    @Autowired
    private CourseGroupsEBL courseGroupsEBL;
    @Autowired
    private FeedMediaEBL feedMediaEBL;
    @Autowired
    private FeedManager feedManager;
    public static final String TOKEN_PROVIDER = "feed";

    public static Hashtable<String, String> resourceTypes;
    static {
        // Mapping: uri prefix --> resource type
        resourceTypes = new Hashtable<String, String>();
        resourceTypes.put(Path.PODCAST_URI_PREFIX, PodcastFileResource.TYPE_NAME);
        resourceTypes.put(Path.BLOG_URI_PREFIX, BlogFileResource.TYPE_NAME);
    }

    private static final Logger log = LoggerHelper.getLogger();

    protected FeedMediaDispatcher() {
    }

    /**
	 */
    @Override
    public void execute(final HttpServletRequest request, final HttpServletResponse response, String uriPrefix) {
        final String requestedPath = getPath(request, uriPrefix);

        Path path = null;
        try {
            // Assume the URL was correct.
            // At first, look up path in cache. Even before extracting any parameters

            path = new Path(requestedPath);
            // See brasatoconfigpart.xml. The uriPrefix is like '/olat/podcast/' or
            // '/company/blog/'. Get the podcast or blog string.
            // remove the last slash if it exists
            final int lastIndex = uriPrefix.length() - 1;
            if (uriPrefix.lastIndexOf("/") == lastIndex) {
                uriPrefix = uriPrefix.substring(0, lastIndex);
            }
            final int lastSlashPos = uriPrefix.lastIndexOf("/");
            final String feedUriPrefix = uriPrefix.substring(lastSlashPos + 1);
            // String feedUriPrefix = uriPrefix.replaceAll("olat|/", "");
            OLATResourceable feed = null;

            if (path.isCachedAndAccessible()) {
                // Serve request
                path.compile();
                feed = OLATResourceManager.getInstance().findResourceable(path.getFeedId(), resourceTypes.get(feedUriPrefix));
                deliverFile(request, response, feed, path);
            } else {
                path.compile();
                feed = OLATResourceManager.getInstance().findResourceable(path.getFeedId(), resourceTypes.get(feedUriPrefix));
                if (courseGroupsEBL.hasAccess(feed, path)) {
                    // Only cache when accessible
                    path.cache(feed, true);
                    deliverFile(request, response, feed, path);
                } else {
                    // Deny access
                    log.info("Access was denied. Path::" + path);
                    DispatcherAction.sendForbidden(request.getRequestURI(), response);
                }
            }
        } catch (final InvalidPathException e) {
            log.warn("The requested path is invalid. path::" + path, e);
            DispatcherAction.sendBadRequest(request.getRequestURI(), response);
        } catch (final Throwable t) {
            log.warn("Nothing was delivered. Path::" + path, t);
            DispatcherAction.sendNotFound(request.getRequestURI(), response);
        }
    }

    /**
     * Dispatch and deliver the requested file given in the path.
     * 
     * @param request
     * @param response
     * @param feed
     * @param path
     */
    private void deliverFile(final HttpServletRequest request, final HttpServletResponse response, final OLATResourceable feed, final Path path) {

        // Create the resource to be delivered
        MediaResource resource = null;

        if (path.isFeedType()) {
            // Only create feed if modified. Send not modified response else.

            Date lastResponse = getLastResponseTime(request);
            Date lastModified = getFeedLastModified(feed);
            if (isNotModified(lastResponse, lastModified)) {
                setrResponseNotModified(response, lastModified);
                return;
            } else {
                final Identity identity = courseGroupsEBL.getIdentity(path.getIdentityKey());
                Translator translator = new PackageTranslator(FeedMediaDispatcher.class.getCanonicalName(), I18nModule.getDefaultLocale());
                resource = feedMediaEBL.createFeedFile(feed, identity, path, translator);
            }
        } else if (path.isItemType()) {
            resource = feedMediaEBL.createItemMediaFile(feed, path);
        } else if (path.isIconType()) {
            resource = feedMediaEBL.createFeedMediaFile(feed, path);
        }
        // Eventually deliver the requested resource
        ServletUtil.serveResource(request, response, resource);
    }

    /**
     * @param response
     * @param lastModified
     */
    private void setrResponseNotModified(final HttpServletResponse response, Date lastModified) {
        // Send not modified response
        response.setDateHeader("last-modified", lastModified.getTime());
        try {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);

        } catch (final IOException e) {
            // Send not modified failed
            log.error("Send not modified failed", e);

        }
    }

    /**
     * @param lastResponse
     * @param lastModified
     * @return
     */
    private boolean isNotModified(Date lastResponse, Date lastModified) {
        return lastResponse != null && lastResponse.before(lastModified);
    }

    /**
     * @param feed
     * @return
     */
    private Date getFeedLastModified(final OLATResourceable feed) {
        final Feed feedLight = feedManager.getFeed(feed);
        Date lastModified = null;
        if (feedLight != null) {
            lastModified = feedLight.getLastModified();
        }
        return lastModified;
    }

    /**
     * @param request
     * @return
     */
    private Date getLastResponseTime(final HttpServletRequest request) {
        final long lastResponseMs = request.getDateHeader("If-Modified-Since");
        Date lastResponse = null;
        if (lastResponseMs != -1) {
            lastResponse = new Date(lastResponseMs);
        }
        return lastResponse;
    }

    /**
     * Remove some prefixes from the request path.
     * 
     * @param request
     * @param prefix
     * @return The path of the request
     */
    private String getPath(final HttpServletRequest request, String prefix) {
        String path = request.getPathInfo();
        // remove servlet context path (/olat) from uri prefix (/olat/podcast)
        prefix = prefix.substring(WebappHelper.getServletContextPath().length());
        // remove prefix (/podcast) from path
        path = path.substring(prefix.length());
        return path;
    }

    /**
     * Redirect to Path.getFeedBaseUri()
     * 
     * @param feed
     * @param identityKey
     * @return The feed base uri for the given user (identity)
     */
    public static String getFeedBaseUri(final Feed feed, final Identity identity, final Long courseId, final String nodeId) {
        return Path.getFeedBaseUri(feed, identity, courseId, nodeId);
    }
}
