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

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.indexer.FullIndexerStatus;
import org.olat.lms.search.indexer.Index;
import org.olat.lms.search.indexer.MainIndexer;
import org.olat.lms.search.searcher.JmsSearchProvider;
import org.olat.lms.search.searcher.QueryException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author Christian Guretzki
 */
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerHelper.getLogger();

    private final SearchModule searchModuleConfig;
    private final MainIndexer mainIndexer;

    private Index index;

    // this is not implemented yet
    // private IndexUpdater indexUpdater;

    private long maxIndexTime;

    private String[] fields = { AbstractOlatDocument.TITLE_FIELD_NAME, AbstractOlatDocument.DESCRIPTION_FIELD_NAME, AbstractOlatDocument.CONTENT_FIELD_NAME,
            AbstractOlatDocument.AUTHOR_FIELD_NAME, AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, AbstractOlatDocument.FILETYPE_FIELD_NAME };

    /**
     * [used by spring]
     */
    private SearchServiceImpl(final SearchModule searchModule, final MainIndexer mainIndexer, final JmsSearchProvider searchProvider) {
        log.info("Start SearchServiceImpl constructor...");
        this.searchModuleConfig = searchModule;
        this.mainIndexer = mainIndexer;
        searchProvider.setSearchService(this);
    }

    /**
     * [used by spring]
     */
    public void setMaxIndexTime(final long maxIndexTime) {
        this.maxIndexTime = maxIndexTime;
    }

    /**
     * [used by spring] Spring setter to inject the available metadata
     * 
     * @param metadataFields
     */
    public void setMetadataFields(final SearchMetadataFieldsProvider metadataFields) {
        if (metadataFields != null) {
            // add metadata fields to normal fields
            final String[] metaFields = ArrayHelper.toArray(metadataFields.getAdvancedSearchableFields());
            final String[] newFields = new String[this.fields.length + metaFields.length];
            System.arraycopy(this.fields, 0, newFields, 0, this.fields.length);
            System.arraycopy(metaFields, 0, newFields, this.fields.length, metaFields.length);
            this.fields = newFields;
        }
    }

    @Override
    public void init() {
        log.info("init searchModuleConfig=" + searchModuleConfig);
        log.info("Running with searchIndexPath=" + searchModuleConfig.getSearchIndexPath());
        log.info("             tempSearchIndexPath=" + searchModuleConfig.getTempSearchIndexPath());

        // TODO check initialization
        index = new Index(searchModuleConfig, mainIndexer, maxIndexTime, fields);
        // this is not implemented yet
        // indexUpdater = new IndexUpdater(searchModuleConfig.getSearchIndexPath(), searchModuleConfig.getUpdateInterval());

        log.info("init DONE");
    }

    @Override
    public void stop() {
        final SearchServiceStatus status = getStatus();
        final String statusStr = status.getStatus();
        if (statusStr.equals(FullIndexerStatus.STATUS_RUNNING)) {
            stopIndexing();
        }
        index.closeIndexSearcher();
        index.closeSpellCheckSearcher();
    }

    @Override
    public SearchResults doSearch(final String queryString, final List<String> condQueries, final Identity identity, final Roles roles, final int firstResult,
            final int maxResults, final boolean doHighlighting) throws ServiceNotAvailableException, QueryException {
        return index.doSearch(queryString, condQueries, identity, roles, firstResult, maxResults, doHighlighting);
    }

    @Override
    public Set<String> spellCheck(final String query) {
        return index.spellCheck(query);
    }

    @Override
    public void startIndexing(boolean testMode) {
        index.startFullIndex(testMode);
        log.info("startIndexing...");
    }

    @Override
    public void stopIndexing() {
        index.stopFullIndex();
        log.info("stopIndexing.");
    }

    @Override
    public void addToIndex(final Document document) {
        // this is not implemented yet
        // log.info("addToIndex document=" + document);
        // indexUpdater.addToIndex(document);
        throw new UnsupportedOperationException("addToIndex has not been implemented yet.");
    }

    @Override
    public void deleteFromIndex(final Document document) {
        // this is not implemented yet
        // log.info("deleteFromIndex document=" + document);
        // indexUpdater.deleteFromIndex(document);
        throw new UnsupportedOperationException("deleteFromIndex has not been implemented yet.");
    }

    @Override
    public long getQueryCount() {
        return index.getQueryCount();
    }

    @Override
    public SearchServiceStatus getStatus() {
        return new SearchServiceStatusImpl(index, this);
    }

    @Override
    public SearchModule getSearchModuleConfig() {
        return searchModuleConfig;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
