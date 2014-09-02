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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.lms.search.indexer.repository;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.forum.Forum;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.forum.ForumService;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.WikiPageDocument;
import org.olat.lms.search.indexer.ForumIndexerHelper;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 18.07.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class WikiIndexerHelper {

    private static final Logger log = LoggerHelper.getLogger();

    private WikiIndexerHelper() {
        super();
    }

    public static void indexWiki(final SearchResourceContext resourceContext, final RepositoryEntry repositoryEntry, final OlatFullIndexer indexWriter, final String type)
            throws IOException {
        String repoEntryName = "*name not available*";
        repoEntryName = repositoryEntry.getDisplayname();
        final Wiki wiki = WikiManager.getInstance().getOrLoadWiki(repositoryEntry.getOlatResource());
        // loop over all wiki pages
        final List<WikiPage> wikiPageList = wiki.getAllPagesWithContent();
        for (final WikiPage wikiPage : wikiPageList) {
            try {
                resourceContext.setDocumentType(type);
                resourceContext.setDocumentContext(Long.toString(repositoryEntry.getKey()));
                resourceContext.setParentContextType(type);
                resourceContext.setParentContextName(wikiPage.getPageName());
                resourceContext.setFilePath(wikiPage.getPageName());

                final Document document = WikiPageDocument.createDocument(resourceContext, wikiPage);
                indexWriter.addDocument(document);

                if (wikiPage.getForumKey() != 0) {
                    final Forum wikiForum = getForumService().loadForum(wikiPage.getForumKey());
                    ForumIndexerHelper.doIndexAllMessages(resourceContext, wikiForum, indexWriter, false);
                }
            } catch (final IOException e) {
                log.error("Error indexing wiki page:" + repoEntryName + " " + (wikiPage == null ? "null" : wikiPage.getPageName()), e);
                throw e;
            }
        }
    }

    private static ForumService getForumService() {
        return CoreSpringFactory.getBean(ForumService.class);
    }

}
