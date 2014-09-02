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
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.FeedItemDocument;
import org.olat.lms.search.document.FeedNodeDocument;
import org.olat.lms.search.document.OlatDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.course.CourseNodeIndexer;
import org.olat.lms.webfeed.Feed;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.webfeed.Item;
import org.olat.lms.webfeed.Path;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Indexer for feed course nodes
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedCourseNodeIndexer extends CourseNodeIndexer {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * org.olat.lms.search.indexer.OlatFullIndexer)
     */
    @Override
    public void doIndex(final SearchResourceContext searchResourceContext, final ICourse course, final CourseNode node, final OlatFullIndexer indexer) throws IOException {
        final RepositoryEntry repositoryEntry = node.getReferencedRepositoryEntry();

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

            // Set the document type, e.g. type.repository.entry.FileResource.BLOG
            final SearchResourceContext nodeSearchContext = new SearchResourceContext(searchResourceContext);
            nodeSearchContext.setBusinessControlFor(node);
            nodeSearchContext.setDocumentType(getDocumentTypeName());

            // Create the olatDocument for the feed course node itself
            final Feed feed = FeedManager.getInstance().getFeed(repositoryEntry.getOlatResource());
            final OlatDocument feedNodeDoc = new FeedNodeDocument(feed, nodeSearchContext);
            indexer.addDocument(feedNodeDoc.getLuceneDocument());

            // Make sure images are displayed properly
            final String mapperBaseURL = Path.getFeedBaseUri(feed, null, course.getResourceableId(), node.getIdent());
            final Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperBaseURL);

            // Only index items. Feed itself is indexed by RepositoryEntryIndexer.
            for (final Item item : feed.getPublishedItems()) {
                final OlatDocument itemDoc = new FeedItemDocument(item, nodeSearchContext, mediaUrlFilter);
                indexer.addDocument(itemDoc.getLuceneDocument());
            }
        } catch (final NullPointerException e) {
            log.error("Error indexing feed:" + repoEntryName, e);
        }
    }

    /**
     * org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Roles)
     */
    @Override
    public boolean checkAccess(ContextEntry courseNodeContextEntry, BusinessControl businessControl, Identity identity, Roles roles, boolean isCourseOwner) {
        return true;
    }

}
