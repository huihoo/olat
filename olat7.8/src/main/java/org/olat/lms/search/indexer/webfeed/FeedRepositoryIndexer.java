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
package org.olat.lms.search.indexer.webfeed;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.FeedItemDocument;
import org.olat.lms.search.document.OlatDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.Item;
import org.olat.lms.webfeed.Path;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * The feed repository entry indexer
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedRepositoryIndexer extends SubLevelIndexer<RepositoryEntry> {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Roles)
     */
    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // check for accessing RepoEntry is done in RepositoryIndexerMultiThreaded
        return true;
    }

    /**
     * org.olat.lms.search.indexer.OlatFullIndexer)
     */
    @Override
    public void doIndex(final SearchResourceContext searchResourceContext, final RepositoryEntry repositoryEntry, final OlatFullIndexer indexer) throws IOException {
        // we won't index external feeds
        final VFSContainer feedContainer = FeedManager.getInstance().getFeedContainer(repositoryEntry.getOlatResource());
        final Feed feedShallow = FeedManager.getInstance().readFeedFile(feedContainer);
        if (feedShallow.isExternal()) {
            log.info("Omit indexing external feed: " + repositoryEntry.getDisplayname());
            return;
        }

        // used for log messages
        String repoEntryName = "*name not available*";
        try {
            repoEntryName = repositoryEntry.getDisplayname();
            if (log.isDebugEnabled()) {
                log.info("Indexing: " + repoEntryName);
            }
            final Feed feed = FeedManager.getInstance().getFeed(repositoryEntry.getOlatResource());

            // Set the document type, e.g. type.repository.entry.FileResource.BLOG
            searchResourceContext.setDocumentType(getDocumentType());
            searchResourceContext.setParentContextType(getDocumentType());
            searchResourceContext.setParentContextName(repoEntryName);

            // Make sure images are displayed properly
            // TODO:GW It's only working for public resources, because base url is
            // personal. -> fix
            final String mapperBaseURL = Path.getFeedBaseUri(feed, null, null, null);
            final Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperBaseURL);

            // Only index items. Feed itself is indexed by RepositoryEntryIndexer.
            log.debug("PublishedItems size=" + feed.getPublishedItems().size());
            for (final Item item : feed.getPublishedItems()) {
                final OlatDocument itemDoc = new FeedItemDocument(item, searchResourceContext, mediaUrlFilter);
                indexer.addDocument(itemDoc.getLuceneDocument());
            }
        } catch (final NullPointerException e) {
            log.error("Error indexing feed:" + repoEntryName, e);
        }

    }

    /**
	 */
    @Override
    public abstract String getSupportedTypeName();

    /**
     * @return The I18n key representing the document type
     */
    protected abstract String getDocumentType();
}
