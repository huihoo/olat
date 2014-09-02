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

package org.olat.lms.search.indexer;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.commons.fileresource.SuffixFilter;
import org.olat.lms.search.SearchModule;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.ServiceNotAvailableException;
import org.olat.lms.search.searcher.QueryException;
import org.olat.lms.search.searcher.SearchResultsImpl;
import org.olat.lms.search.spell.SearchSpellChecker;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Controls the existing index, copies newly generated index from temporary directory to search directory.
 * 
 * @author Christian Guretzki
 */
public class Index {

    private static final Logger log = LoggerHelper.getLogger();

    private final SearchModule searchModule;

    private final MainIndexer mainIndexer;

    private final long maxIndexTime;

    private final String[] fields;

    private final OlatFullIndexer fullIndexer;

    private final Analyzer analyzer;

    private Searcher searcher;
    private final Object createIndexSearcherLock = new Object();

    private SpellChecker spellChecker;
    private final Object createSpellCheckSearcherLock = new Object();

    /** Counts number of search queries since last restart. */
    private long queryCount = 0;

    public Index(final SearchModule searchModule, final MainIndexer mainIndexer, final long maxIndexTime, final String[] fields) {
        this.searchModule = searchModule;
        this.mainIndexer = mainIndexer;
        this.maxIndexTime = maxIndexTime;
        this.fields = fields;

        fullIndexer = new OlatFullIndexer(this, mainIndexer, searchModule);
        analyzer = new StandardAnalyzer(Version.LUCENE_30);

        createIndexSearcher(false);
        if (searchModule.isSpellCheckEnabled()) {
            createSpellCheckSearcher(false);
        }
    }

    /**
     * @see org.olat.lms.search.SearchService#doSearch(String, List, Identity, Roles, int, int, boolean)
     */
    public SearchResults doSearch(final String queryString, final List<String> condQueries, final Identity identity, final Roles roles, final int firstResult,
            final int maxResults, final boolean doHighlighting) throws ServiceNotAvailableException, QueryException {

        synchronized (createIndexSearcherLock) {// o_clusterOK by:fj if service is only configured on one vm, which is recommended way
            if (searcher == null) {
                log.warn("Index does not exist, can't search for queryString: " + queryString);
                throw new ServiceNotAvailableException("Index does not exist");
            }
        }

        try {
            log.info("queryString=" + queryString);

            final BooleanQuery query = new BooleanQuery();
            if (StringHelper.containsNonWhitespace(queryString)) {
                final QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_30, fields, analyzer);
                queryParser.setLowercaseExpandedTerms(false);// some add. fields are not tokenized and not lowered case
                final Query multiFieldQuery = queryParser.parse(queryString.toLowerCase());
                query.add(multiFieldQuery, Occur.MUST);
            }

            if (condQueries != null && !condQueries.isEmpty()) {
                for (final String condQueryString : condQueries) {
                    final QueryParser condQueryParser = new QueryParser(Version.LUCENE_30, condQueryString, analyzer);
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
        } catch (final ParseException pex) {
            throw new QueryException("can not parse query=" + queryString);
        } catch (final Exception ex) {
            log.warn("Exception in search", ex);
            throw new ServiceNotAvailableException(ex.getMessage());
        }
    }

    /**
     * @see org.olat.lms.search.SearchService#spellCheck(String)
     */
    public Set<String> spellCheck(final String query) {
        synchronized (createSpellCheckSearcherLock) {// o_clusterOK by:fj if service is only configured on one vm, which is recommended way
            if (spellChecker == null) {
                return null;
            }
        }

        try {
            final String[] words = spellChecker.suggestSimilar(query, 5);
            // Remove duplicate
            final Set<String> filteredList = new TreeSet<String>();
            for (final String word : words) {
                filteredList.add(word);
            }
            return filteredList;
        } catch (final IOException e) {
            log.warn("Can not spell check", e);
            return null;
        }
    }

    /**
     * @see org.olat.lms.search.SearchService#startIndexing(boolean)
     */
    public void startFullIndex(boolean testMode) {
        fullIndexer.startIndexing(testMode);
    }

    /**
     * @see org.olat.lms.search.SearchService#stopIndexing()
     */
    public void stopFullIndex() {
        fullIndexer.stopIndexing();
    }

    /**
     * @see org.olat.lms.search.SearchService#getQueryCount()
     */
    public long getQueryCount() {
        return queryCount;
    }

    /**
     * @return Return current status of full-indexer.
     */
    public FullIndexerStatus getFullIndexStatus() {
        return fullIndexer.getStatus();
    }

    /**
     * @return Creation date of current used search index.
     */
    public Date getCreationDate() {
        try {
            final File indexDir = new File(searchModule.getSearchIndexPath());
            final Directory directory = FSDirectory.open(indexDir);
            return new Date(IndexReader.getCurrentVersion(directory));
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * @return <code>true</code> if Lucene index exists.
     */
    public boolean existIndex() {
        try {
            final File indexDir = new File(searchModule.getSearchIndexPath());
            final Directory indexLuceneDirectory = FSDirectory.open(indexDir);
            return IndexReader.indexExists(indexLuceneDirectory);
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * Replaces existing index data with new index data and creates new spell check index
     */
    public void activateNewIndex() {
        if (searchModule.isSpellCheckEnabled()) {
            SearchSpellChecker.createSpellIndex(searchModule);
            createSpellCheckSearcher(true);
        }
        createIndexSearcher(true);
    }

    public void closeIndexSearcher() {
        try {
            if (searcher != null) {
                searcher.close();
                searcher = null;
            }
        } catch (final IOException e) {
            log.error("Error closing searcher", e);
        }
    }

    public void closeSpellCheckSearcher() {
        try {
            if (spellChecker != null) {
                spellChecker.close();
                spellChecker = null;
            }
        } catch (final IOException e) {
            log.error("Error closing spell checker", e);
        }
    }

    private void createIndexSearcher(boolean indexNewlyBuilt) {
        try {
            log.info("Create searcher on new index ...");
            Directory searchIndexDirectory = null;
            synchronized (createIndexSearcherLock) {
                closeIndexSearcher();
                if (indexNewlyBuilt) {
                    replaceIndexFiles();
                }
                searchIndexDirectory = FSDirectory.open(new File(searchModule.getSearchIndexPath()));
                if (!IndexReader.indexExists(searchIndexDirectory)) {
                    log.error("SpellChecker index does not exist [" + searchModule.getSearchIndexPath() + "]");
                    return;
                }
                searcher = new IndexSearcher(searchIndexDirectory);
            }

            final long indexTime = IndexReader.getCurrentVersion(searchIndexDirectory);
            if ((System.currentTimeMillis() - indexTime) > maxIndexTime) {
                log.error("Search index is too old [indexDate=" + new Date(indexTime) + "].");
            }

            if (indexNewlyBuilt) {
                log.info("Cleanup old index files ...");
                cleanupIndexFiles();
            }
        } catch (IOException ex) {
            log.error("Searcher couldn't be created.", ex);
        }
    }

    private void replaceIndexFiles() {
        final File searchIndexDir = new File(searchModule.getSearchIndexPath());
        final File tempSearchIndexDir = new File(searchModule.getTempSearchIndexPath());
        if (log.isDebugEnabled()) {
            log.debug("Copy new generated Index from '" + tempSearchIndexDir.getAbsolutePath() + "/main" + "' to '" + searchIndexDir.getAbsolutePath() + "'");
        }

        if (!searchIndexDir.exists()) {
            searchIndexDir.mkdirs();
        }

        // rename existing index dir (keep it until new index has been copied successfully)
        final File searchIndexDirOld = new File(searchIndexDir.getAbsolutePath() + "-old");
        FileUtils.moveDirToDir(searchIndexDir, searchIndexDirOld, "rename search index dir");

        // move new search index files from temp dir to index dir
        FileUtils.copyDirContentsToDir(new File(tempSearchIndexDir, "main"), searchIndexDir, true, "search indexer move tmp index");

        log.info("Newly generated Index ready to use.");
    }

    private void cleanupIndexFiles() {
        final File indexDir = new File(searchModule.getIndexPath());
        final File searchIndexDir = new File(searchModule.getSearchIndexPath());
        final File tempSearchIndexDir = new File(searchModule.getTempSearchIndexPath());

        // move statistic files from temp dir to index dir
        final File[] statisticFiles = tempSearchIndexDir.listFiles(new SuffixFilter(TopLevelIndexer.STATISTIC_FILE_SUFFIX));
        for (File statisticFile : statisticFiles) {
            if (statisticFile.isFile()) {
                FileUtils.moveFileToDir(statisticFile, indexDir);
            }
        }

        // delete old index dir and temp index dir
        final File searchIndexDirOld = new File(searchIndexDir.getAbsolutePath() + "-old");
        FileUtils.deleteDirsAndFiles(searchIndexDirOld, true, true);
        FileUtils.deleteDirsAndFiles(tempSearchIndexDir, true, true);

        if (searchModule.isTextBufferCleanupEnabled()) {
            // avoid too much stale data in text buffer dir by deleting parts of it after each indexer run
            final Calendar date = Calendar.getInstance();
            final String subfolderPath = String.format("%1$02d", date.get(Calendar.DAY_OF_YEAR) % 100);
            final File textBufferCleanupDir = new File(searchModule.getTextBufferPath(), subfolderPath);
            log.info("Delete text buffer dir: " + textBufferCleanupDir.getAbsolutePath());
            textBufferCleanupDir.delete();
        }
    }

    private void createSpellCheckSearcher(boolean indexNewlyBuilt) {
        try {
            log.info("Create spell checker on new index ...");
            synchronized (createSpellCheckSearcherLock) {// o_clusterOK by:pb if service is only configured on one vm, which is recommended way
                closeSpellCheckSearcher();
                if (indexNewlyBuilt) {
                    replaceSpellCheckFiles();
                }

                final File spellDictionaryFile = new File(searchModule.getSpellCheckerIndexPath());
                final Directory spellIndexDirectory = FSDirectory.open(spellDictionaryFile);
                if (!IndexReader.indexExists(spellIndexDirectory)) {
                    log.error("SpellChecker index does not exist [" + spellDictionaryFile.getAbsolutePath() + "]");
                    return;
                }
                spellChecker = new SpellChecker(spellIndexDirectory);
                spellChecker.setAccuracy(0.7f);
            }

            if (indexNewlyBuilt) {
                log.info("Cleanup old spell checker index files ...");
                cleanupSpellCheckFiles();
            }
        } catch (IOException ex) {
            log.error("SpellChecker couldn't be created.", ex);
        }

    }

    private void replaceSpellCheckFiles() {
        final File spellCheckerIndexDir = new File(searchModule.getSpellCheckerIndexPath());
        final File tempSpellCheckerIndexDir = new File(searchModule.getTempSpellCheckerIndexPath());
        final File spellCheckerIndexDirOld = new File(spellCheckerIndexDir.getAbsolutePath() + "-old");

        if (log.isDebugEnabled()) {
            log.debug("Move newly generated spell check index from '" + tempSpellCheckerIndexDir + "' to '" + spellCheckerIndexDir + "'.");
        }
        FileUtils.moveDirToDir(spellCheckerIndexDir, spellCheckerIndexDirOld, "rename spell check index dir");
        FileUtils.copyDirContentsToDir(tempSpellCheckerIndexDir, spellCheckerIndexDir, true, "spell checker indexer move tmp index");

        log.info("Newly generated spell check index ready to use.");
    }

    private void cleanupSpellCheckFiles() {
        final File spellCheckerIndexDir = new File(searchModule.getSpellCheckerIndexPath());
        final File spellCheckerIndexDirOld = new File(spellCheckerIndexDir.getAbsolutePath() + "-old");
        FileUtils.deleteDirsAndFiles(spellCheckerIndexDirOld, true, true);
    }

}
