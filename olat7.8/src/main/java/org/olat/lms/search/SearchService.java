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

package org.olat.lms.search;

import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.search.searcher.QueryException;

/**
 * Interface to search service.
 * 
 * @author Christian Guretzki
 */
public interface SearchService {

    /**
     * Initializes service.
     */
    public void init();

    /**
     * Shuts down search service.
     */
    public void stop();

    /**
     * Do search a certain query. The results will be filtered for the identity and roles.
     * 
     * @param queryString
     *            Search query-string.
     * @param identity
     *            Filter results for this identity (user).
     * @param roles
     *            Filter results for this roles (role of user).
     * @return SearchResults object for this query
     */
    public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, int firstResult, int maxReturns, boolean doHighlighting)
            throws ServiceNotAvailableException, QueryException;

    /**
     * Check a query for similar words.
     * 
     * @param query
     * @return List of similar words which exist in index or <code>null</code> if
     */
    public Set<String> spellCheck(String query) throws ServiceNotAvailableException;

    /**
     * Start a new full index.
     * 
     * @param testMode
     *            if set to <code>true</code> existing index won't be replaced after new index has been generated
     */
    public void startIndexing(boolean testMode);

    /**
     * Stop current full-indexing.
     */
    public void stopIndexing();

    /**
     * Return current state of search service, Includes full-indexing, index and search.
     * 
     * @return
     */
    public SearchServiceStatus getStatus();

    /**
     * Add a document to existing index.
     * 
     * @param document
     *            New document.
     */
    public void addToIndex(Document document);

    /**
     * Delete a document in existing index.
     * 
     * @param document
     *            Delete this document.
     */
    public void deleteFromIndex(Document document);

    /**
     * access the module configuration
     * 
     * @return
     */
    public SearchModule getSearchModuleConfig();

    /**
     * @return true when the search service is enabled
     */
    public boolean isEnabled();

    public long getQueryCount();

}
