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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.document.ResultDocument;
import org.olat.lms.search.indexer.MainIndexer;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Data object to pass search results back from search service.
 * 
 * @author Christian Guretzki
 */
public class SearchResultsImpl implements SearchResults {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String HIGHLIGHT_PRE_TAG = "<span class=\"o_search_result_highlight\">";
    private static final String HIGHLIGHT_POST_TAG = "</span>";
    private static final String HIGHLIGHT_SEPARATOR = "...<br />";

    /* Define in module config */
    private int maxHits;
    private int totalHits;
    private int totalDocs;
    private long queryTime;
    private int numberOfIndexDocuments;
    /* List of ResultDocument. */
    private final List<ResultDocument> resultList;
    private transient MainIndexer mainIndexer;

    /**
     * Constructure for certain search-results. Does not include any search-call to search-service. Search call must be made before to create a Hits object.
     * 
     * @param hits
     *            Search hits return from search.
     * @param query
     *            Search query-string.
     * @param analyzer
     *            Search analyser, must be the same like at creation of index.
     * @param identity
     *            Filter results for this identity (user).
     * @param roles
     *            Filter results for this roles (role of user).
     * @param doHighlighting
     *            Flag to enable highlighting search
     * @throws IOException
     */
    public SearchResultsImpl(final MainIndexer mainIndexer, final Searcher searcher, final TopDocs docs, final Query query, final Analyzer analyzer,
            final Identity identity, final Roles roles, final int firstResult, final int maxReturns, final boolean doHighlighting) throws IOException {
        this.mainIndexer = mainIndexer;
        resultList = initResultList(identity, roles, query, analyzer, searcher, docs, firstResult, maxReturns, doHighlighting);
    }

    /**
     * @return Length of result-list.
     */
    @Override
    public String getLength() {
        return Integer.toString(resultList.size());
    }

    /**
     * @return List of ResultDocument.
     */
    @Override
    public List<ResultDocument> getList() {
        return resultList;
    }

    /**
     * Set query response time in milliseconds.
     * 
     * @param queryTime
     *            Query response time in milliseconds.
     */
    public void setQueryTime(final long queryTime) {
        this.queryTime = queryTime;
    }

    /**
     * @return Query response time in milliseconds.
     */
    public String getQueryTime() {
        return Long.toString(queryTime);
    }

    /**
     * Set number of search-index-elements.
     * 
     * @param numberOfIndexDocuments
     *            Number of search-index-elements.
     */
    public void setNumberOfIndexDocuments(final int numberOfIndexDocuments) {
        this.numberOfIndexDocuments = numberOfIndexDocuments;
    }

    /**
     * @return Number of search-index-elements.
     */
    @Override
    public String getNumberOfIndexDocuments() {
        return Integer.toString(numberOfIndexDocuments);
    }

    /**
     * @return Number of maximal possible results.
     */
    @Override
    public int getTotalHits() {
        return totalHits;
    }

    @Override
    public int getTotalDocs() {
        return totalDocs;
    }

    public String getMaxHits() {
        return Integer.toString(maxHits);
    }

    public boolean hasTooManyResults() {
        return totalHits > maxHits;
    }

    private List<ResultDocument> initResultList(final Identity identity, final Roles roles, final Query query, final Analyzer analyzer, final Searcher searcher,
            final TopDocs docs, final int firstResult, final int maxReturns, final boolean doHighlight) throws IOException {
        final FieldSelector selector = new FieldSelector() {
            @Override
            public FieldSelectorResult accept(final String fieldName) {
                return (doHighlight || !AbstractOlatDocument.CONTENT_FIELD_NAME.equals(fieldName)) ? FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
            }
        };

        maxHits = SearchServiceFactory.getService().getSearchModuleConfig().getMaxHits();
        totalHits = docs.totalHits;
        totalDocs = (docs.scoreDocs == null ? 0 : docs.scoreDocs.length);
        final int numOfDocs = Math.min(maxHits, docs.totalHits);
        final List<ResultDocument> res = new ArrayList<ResultDocument>(maxReturns + 1);
        for (int i = firstResult; i < numOfDocs && res.size() < maxReturns; i++) {
            final Document doc = searcher.doc(docs.scoreDocs[i].doc, selector);
            final String reservedTo = doc.get(AbstractOlatDocument.RESERVED_TO);
            if (StringHelper.containsNonWhitespace(reservedTo) && !"public".equals(reservedTo) && !reservedTo.contains(identity.getKey().toString())) {
                continue;// admin cannot see private documents
            }

            final ResultDocument rDoc = createResultDocument(doc, i, query, analyzer, doHighlight, identity, roles);
            if (rDoc != null) {
                res.add(rDoc);
            }

            if (!roles.isOLATAdmin() && i % 10 == 0) {
                // Do commit after certain number of documents because the transaction should not be too big
                DBFactory.getInstance().intermediateCommit();
            }
        }
        return res;
    }

    /**
     * Create a result document. Return null if the identity has not enough privileges to see the document.
     * 
     * @param doc
     * @param query
     * @param analyzer
     * @param doHighlight
     * @param identity
     * @param roles
     * @return
     * @throws IOException
     */
    private ResultDocument createResultDocument(final Document doc, final int pos, final Query query, final Analyzer analyzer, final boolean doHighlight,
            final Identity identity, final Roles roles) throws IOException {
        boolean hasAccess = false;
        if (roles.isOLATAdmin()) {
            hasAccess = true;
        } else {
            String resourceUrl = doc.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
            if (resourceUrl == null) {
                resourceUrl = "";
            }

            final BusinessControl businessControl = BusinessControlFactory.getInstance().createFromString(resourceUrl);
            hasAccess = mainIndexer.checkAccess(businessControl, identity, roles);
        }

        ResultDocument resultDoc;
        if (hasAccess) {
            resultDoc = new ResultDocument(doc, pos);
            if (doHighlight) {
                doHighlight(query, analyzer, doc, resultDoc);
            }
        } else {
            resultDoc = null;
        }
        return resultDoc;
    }

    /**
     * Highlight (bold,color) query words in result-document. Set HighlightResult for content or description.
     * 
     * @param query
     * @param analyzer
     * @param doc
     * @param resultDocument
     * @throws IOException
     */
    private void doHighlight(final Query query, final Analyzer analyzer, final Document doc, final ResultDocument resultDocument) throws IOException {
        final Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(HIGHLIGHT_PRE_TAG, HIGHLIGHT_POST_TAG), new QueryScorer(query));
        // Get 3 best fragments of content and seperate with a "..."
        try {
            // highlight content
            final String content = doc.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
            TokenStream tokenStream = analyzer.tokenStream(AbstractOlatDocument.CONTENT_FIELD_NAME, new StringReader(content));
            String highlightResult = highlighter.getBestFragments(tokenStream, content, 3, HIGHLIGHT_SEPARATOR);

            // if no highlightResult is in content => look in description
            if (highlightResult.length() == 0) {
                final String description = doc.get(AbstractOlatDocument.DESCRIPTION_FIELD_NAME);
                tokenStream = analyzer.tokenStream(AbstractOlatDocument.DESCRIPTION_FIELD_NAME, new StringReader(description));
                highlightResult = highlighter.getBestFragments(tokenStream, description, 3, HIGHLIGHT_SEPARATOR);
                resultDocument.setHighlightingDescription(true);
            }
            resultDocument.setHighlightResult(highlightResult);

            // highlight title
            final String title = doc.get(AbstractOlatDocument.TITLE_FIELD_NAME);
            tokenStream = analyzer.tokenStream(AbstractOlatDocument.TITLE_FIELD_NAME, new StringReader(title));
            final String highlightTitle = highlighter.getBestFragments(tokenStream, title, 3, " ");
            resultDocument.setHighlightTitle(highlightTitle);
        } catch (final InvalidTokenOffsetsException e) {
            log.warn("", e);
        }
    }
}
