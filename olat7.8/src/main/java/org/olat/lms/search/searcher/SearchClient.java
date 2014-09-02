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
package org.olat.lms.search.searcher;

import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.SearchService;
import org.olat.lms.search.ServiceNotAvailableException;

/**
 * 
 * Interface defining the client (GUI) access to search service.
 * 
 * Initial Date: 27.03.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public interface SearchClient {

    /**
     * @see SearchService#doSearch(String, List, Identity, Roles, int, int, boolean)
     */
    public SearchResults doSearch(final String queryString, final List<String> condQueries, final Identity identity, final Roles roles, final int firstResult,
            final int maxResults, final boolean doHighlighting) throws ServiceNotAvailableException, QueryException;

    /**
     * @see SearchService#spellCheck(String)
     */
    public Set<String> spellCheck(final String query) throws ServiceNotAvailableException;

}
