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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.indexer.FullIndexerStatus;
import org.olat.lms.search.indexer.Index;
import org.olat.lms.search.indexer.MainIndexer;
import org.olat.lms.search.searcher.JmsSearchProvider;
import org.olat.lms.search.searcher.SearchResultsImpl;
import org.olat.lms.search.spell.SearchSpellChecker;
import org.olat.lms.search.update.IndexUpdater;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author Christian Guretzki
 */
public class SearchServiceImpl implements SearchService {
    private static final Logger log = LoggerHelper.getLogger();

    private Index indexer;
    private final SearchModule searchModuleConfig;
    private IndexUpdater indexUpdater;
    private final MainIndexer mainIndexer;

    private long maxIndexTime;
    private final Analyzer analyzer;
    private Searcher searcher;
    private SearchSpellChecker searchSpellChecker;
    private String indexPath;
    /** Counts number of search queries since last restart. */
    private long queryCount = 0;
    private final Object createIndexSearcherLock = new Object();
    private Date openIndexDate;

    private String fields[] = { AbstractOlatDocument.TITLE_FIELD_NAME, AbstractOlatDocument.DESCRIPTION_FIELD_NAME, AbstractOlatDocument.CONTENT_FIELD_NAME,
            AbstractOlatDocument.AUTHOR_FIELD_NAME, AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, AbstractOlatDocument.FILETYPE_FIELD_NAME };

    /**
     * [used by spring]
     */
    private SearchServiceImpl(final SearchModule searchModule, final MainIndexer mainIndexer, final JmsSearchProvider searchProvider) {
        log.info("Start SearchServiceImpl constructor...");
        this.searchModuleConfig = searchModule;
        this.mainIndexer = mainIndexer;
        analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
        searchProvider.setSearchService(this);

    }

    @Override
    public void addToIndex(final Document document) {
        if (indexUpdater == null) {
            throw new AssertException("Try to call addToIndex() but indexUpdater is null!");
        }
        log.info("addToIndex document=" + document);
        indexUpdater.addToIndex(document);
    }

    @Override
    public void startIndexing() {
        if (indexer == null) {
            throw new AssertException("Try to call startIndexing() but indexer is null");
        }
        indexer.startFullIndex();
        log.info("startIndexing...");
    }

    @Override
    public void stopIndexing() {
        if (indexer == null) {
            throw new AssertException("Try to call stopIndexing() but indexer is null");
        }
        indexer.stopFullIndex();
        log.info("stopIndexing.");
    }

    @Override
    public void deleteFromIndex(final Document document) {
        if (indexUpdater == null) {
            throw new AssertException("Try to call deleteFromIndex() but indexUpdater is null");
        }
        log.info("deleteFromIndex document=" + document);
        indexUpdater.deleteFromIndex(document);
    }

    @Override
    public void init() {
        log.info("init searchModuleConfig=" + searchModuleConfig);

        log.info("Running with indexPath=" + searchModuleConfig.getFullIndexPath());
        log.info("        tempIndexPath=" + searchModuleConfig.getFullTempIndexPath());
        log.info("        generateAtStartup=" + searchModuleConfig.getGenerateAtStartup());
        log.info("        restartInterval=" + searchModuleConfig.getRestartInterval());
        log.info("        indexInterval=" + searchModuleConfig.getIndexInterval());

        searchSpellChecker = new SearchSpellChecker();
        searchSpellChecker.setIndexPath(searchModuleConfig.getFullIndexPath());
        searchSpellChecker.setSpellDictionaryPath(searchModuleConfig.getSpellCheckDictionaryPath());
        searchSpellChecker.setSpellCheckEnabled(searchModuleConfig.getSpellCheckEnabled());

        indexer = new Index(searchModuleConfig, searchSpellChecker, mainIndexer);
        indexUpdater = new IndexUpdater(searchModuleConfig.getFullIndexPath(), searchModuleConfig.getUpdateInterval());

        indexPath = searchModuleConfig.getFullIndexPath();

        try {
            checkIsIndexUpToDate();
        } catch (final IOException e) {
            log.info("Can not create IndexSearcher at startup");
        }

        if (startingFullIndexingAllowed()) {
            indexer.startFullIndex();
        }
        log.info("init DONE");
    }

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
    @Override
    public SearchResults doSearch(final String queryString, final List<String> condQueries, final Identity identity, final Roles roles, final int firstResult,
            final int maxResults, final boolean doHighlighting) throws ServiceNotAvailableException, ParseException {
        try {
            if (!existIndex()) {
                log.warn("Index does not exist, can't search for queryString: " + queryString);
                throw new ServiceNotAvailableException("Index does not exist");
            }
            synchronized (createIndexSearcherLock) {// o_clusterOK by:fj if service is only configured on one vm, which is recommended way
                if (searcher == null) {
                    try {
                        createIndexSearcher(indexPath);
                        checkIsIndexUpToDate();
                    } catch (final IOException ioEx) {
                        log.warn("Can not create searcher", ioEx);
                        throw new ServiceNotAvailableException("Index is not available");
                    }
                }
                if (hasNewerIndexFile()) {
                    reopenIndexSearcher();
                    checkIsIndexUpToDate();
                }
            }
            log.info("queryString=" + queryString);

            final BooleanQuery query = new BooleanQuery();
            if (StringHelper.containsNonWhitespace(queryString)) {
                final QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, fields, analyzer);
                queryParser.setLowercaseExpandedTerms(false);// some add. fields are not tokenized and not lowered case
                final Query multiFieldQuery = queryParser.parse(queryString.toLowerCase());
                query.add(multiFieldQuery, Occur.MUST);
            }

            if (condQueries != null && !condQueries.isEmpty()) {
                for (final String condQueryString : condQueries) {
                    final QueryParser condQueryParser = new QueryParser(Version.LUCENE_CURRENT, condQueryString, analyzer);
                    condQueryParser.setLowercaseExpandedTerms(false);
                    final Query condQuery = condQueryParser.parse(condQueryString);
                    query.add(condQuery, Occur.MUST);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("query=" + query);
            }
            // TODO: 14.06.2010/cg : fellowig cide fragment can be removed later, do no longer call rewrite(query) because wildcard-search problem (OLAT-5359)
            // Query query = null;
            // try {
            // query = searcher.rewrite(query);
            // log.debug("after 'searcher.rewrite(query)' query=" + query);
            // } catch (Exception ex) {
            // throw new QueryException("Rewrite-Exception query because too many clauses. Query=" + query);
            // }
            final long startTime = System.currentTimeMillis();
            final int n = SearchServiceFactory.getService().getSearchModuleConfig().getMaxHits();
            final TopDocs docs = searcher.search(query, n);
            final long queryTime = System.currentTimeMillis() - startTime;
            if (log.isDebugEnabled()) {
                log.debug("hits.length()=" + docs.totalHits);
            }
            final SearchResultsImpl searchResult = new SearchResultsImpl(mainIndexer, searcher, docs, query, analyzer, identity, roles, firstResult, maxResults,
                    doHighlighting);
            searchResult.setQueryTime(queryTime);
            searchResult.setNumberOfIndexDocuments(searcher.maxDoc());
            queryCount++;
            return searchResult;
        } catch (final ServiceNotAvailableException naex) {
            // pass exception
            throw new ServiceNotAvailableException(naex.getMessage());
        } catch (final ParseException pex) {
            throw new ParseException("can not parse query=" + queryString);
        } catch (final Exception ex) {
            log.warn("Exception in search", ex);
            throw new ServiceNotAvailableException(ex.getMessage());
        }
    }

    /**
     * Delegates impl to the searchSpellChecker.
     * 
     */
    @Override
    public Set<String> spellCheck(final String query) {
        if (searchSpellChecker == null) {
            throw new AssertException("Try to call spellCheck() in Search.java but searchSpellChecker is null");
        }
        return searchSpellChecker.check(query);
    }

    @Override
    public long getQueryCount() {
        return queryCount;
    }

    /**
     * [used by spring]
     */
    public void setMaxIndexTime(final long maxIndexTime) {
        this.maxIndexTime = maxIndexTime;
    }

    @Override
    public SearchServiceStatus getStatus() {
        return new SearchServiceStatusImpl(indexer, this);
    }

    @Override
    public void setIndexInterval(final long indexInterval) {
        if (indexer == null) {
            throw new AssertException("Try to call setIndexInterval() but indexer is null");
        }
        indexer.setIndexInterval(indexInterval);
    }

    @Override
    public long getIndexInterval() {
        if (indexer == null) {
            throw new AssertException("Try to call setIndexInterval() but indexer is null");
        }
        return indexer.getIndexInterval();
    }

    /**
     * @return Resturn search module configuration.
     */
    @Override
    public SearchModule getSearchModuleConfig() {
        return searchModuleConfig;
    }

    @Override
    public void stop() {
        final SearchServiceStatus status = getStatus();
        final String statusStr = status.getStatus();
        if (statusStr.equals(FullIndexerStatus.STATUS_RUNNING)) {
            stopIndexing();
        }
        try {
            if (searcher != null) {
                searcher.close();
                searcher = null;
            }
        } catch (final IOException e) {
            log.error("", e);
        }

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ////////////////
    // Private Methods
    // ////////////////
    private void checkIsIndexUpToDate() throws IOException {
        final long indexTime = getCurrentIndexDate().getTime();
        final long currentTime = System.currentTimeMillis();
        if ((currentTime - indexTime) > maxIndexTime) {
            log.error("Search index is too old indexDate=" + getCurrentIndexDate());
        }
    }

    private void createIndexSearcher(final String path) throws IOException {
        final File indexFile = new File(path);
        final Directory directory = FSDirectory.open(indexFile);
        searcher = new IndexSearcher(directory);
        openIndexDate = getCurrentIndexDate();
    }

    /**
     * @return Creation date of current used search index.
     */
    private Date getCurrentIndexDate() throws IOException {
        final File indexFile = new File(indexPath);
        final Directory directory = FSDirectory.open(indexFile);
        return new Date(IndexReader.getCurrentVersion(directory));
    }

    private void reopenIndexSearcher() {
        if (hasNewerIndexFile()) {
            log.debug("New index file available, reopen it");
            try {
                searcher.close();
                createIndexSearcher(indexPath);
            } catch (final IOException e) {
                log.warn("Could not reopen index-searcher", e);
            }
        }
    }

    private boolean hasNewerIndexFile() {
        try {
            if (getCurrentIndexDate().after(openIndexDate)) {
                return true;
            }
        } catch (final IOException e) { // no index file exist
        }
        return false;
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

    /**
     * Check if index exist.
     * 
     * @return true : Index exists.
     */
    private boolean existIndex() throws IOException {
        try {
            final File indexFile = new File(searchModuleConfig.getFullIndexPath());
            final Directory directory = FSDirectory.open(indexFile);
            return IndexReader.indexExists(directory);
        } catch (final IOException e) {
            throw e;
        }
    }

    /**
     * Check if starting a generating full-index is allowed. Depends config-parameter 'generateAtStartup', day of the week and config-parameter 'restartDayOfWeek',
     * current time and the restart- window (config-parameter 'restartWindowStart', 'restartWindowEnd')
     * 
     * @return TRUE: Starting is allowed
     */
    private boolean startingFullIndexingAllowed() {
        if (searchModuleConfig.getGenerateAtStartup()) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            // check day, Restart only at config day of week, 0-7 8=every day
            final int dayNow = calendar.get(Calendar.DAY_OF_WEEK);
            final int restartDayOfWeek = searchModuleConfig.getRestartDayOfWeek();
            if (restartDayOfWeek == 0 || (dayNow == restartDayOfWeek)) {
                // check time, Restart only in the config time-slot e.g. 01:00 - 03:00
                final int hourNow = calendar.get(Calendar.HOUR_OF_DAY);
                final int restartWindowStart = searchModuleConfig.getRestartWindowStart();
                final int restartWindowEnd = searchModuleConfig.getRestartWindowEnd();
                if ((restartWindowStart <= hourNow) && (hourNow < restartWindowEnd)) {
                    return true;
                }
            }
        }
        return false;
    }

}
