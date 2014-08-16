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

package org.olat.lms.search.indexer.repository.course;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.WikiPageDocument;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.CourseIndexer;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Indexer for Wiki course-node.
 * 
 * @author Christian Guretzki
 */
public class WikiCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {
    private static final Logger log = LoggerHelper.getLogger();

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to search for certain documenttype and lucene have problems with '_'
    public final static String TYPE = "type.course.node.wiki";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.WikiCourseNode";

    private final CourseIndexer courseNodeIndexer;

    public WikiCourseNodeIndexer() {
        courseNodeIndexer = new CourseIndexer();
    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter) {
        if (log.isDebugEnabled()) {
            log.debug("Index wiki...");
        }
        String repoEntryName = "*name not available*";
        try {
            final RepositoryEntry repositoryEntry = courseNode.getReferencedRepositoryEntry();
            repoEntryName = repositoryEntry.getDisplayname();
            final Wiki wiki = WikiManager.getInstance().getOrLoadWiki(courseNode.getReferencedRepositoryEntry().getOlatResource());
            // loop over all wiki pages
            final List<WikiPage> wikiPageList = wiki.getAllPagesWithContent();
            for (final WikiPage wikiPage : wikiPageList) {
                try {
                    final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
                    courseNodeResourceContext.setBusinessControlFor(courseNode);
                    courseNodeResourceContext.setDocumentType(TYPE);
                    courseNodeResourceContext.setDocumentContext(course.getResourceableId() + " " + courseNode.getIdent());
                    courseNodeResourceContext.setFilePath(wikiPage.getPageName());

                    final Document document = WikiPageDocument.createDocument(courseNodeResourceContext, wikiPage);
                    indexWriter.addDocument(document);
                } catch (final Exception e) {
                    log.error("Error indexing wiki page:" + (wikiPage == null ? "null" : wikiPage.getPageName()), e);
                }
            }
        } catch (final Exception e) {
            log.error("Error indexing wiki:" + repoEntryName, e);
        }

        // go further, index my child nodes
        try {
            courseNodeIndexer.doIndexCourse(repositoryResourceContext, course, courseNode, indexWriter);
        } catch (final Exception e) {
            log.error("Error indexing child of courseNode=" + courseNode, e);
        }
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        return true;
    }

}
