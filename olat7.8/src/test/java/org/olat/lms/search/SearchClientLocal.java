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

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.search.searcher.QueryException;
import org.olat.lms.search.searcher.SearchClient;

/**
 * Search client implementation calling SearchService on local node. For testing purpose.
 * 
 * Initial Date: 27.03.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class SearchClientLocal implements SearchClient {

    @Override
    public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, int firstResult, int maxResults, boolean doHighlighting)
            throws ServiceNotAvailableException, QueryException {
        return SearchServiceFactory.getService().doSearch(queryString, condQueries, identity, roles, firstResult, maxResults, doHighlighting);
    }

    @Override
    public Set<String> spellCheck(String query) throws ServiceNotAvailableException {
        return SearchServiceFactory.getService().spellCheck(query);
    }
}
