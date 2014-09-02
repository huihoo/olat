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

package org.olat.presentation.search;

import static org.olat.presentation.search.ResultsController.RESULT_PER_PAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.mediaresource.RedirectMediaResource;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.ServiceNotAvailableException;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.document.ResultDocument;
import org.olat.lms.search.searcher.QueryException;
import org.olat.lms.search.searcher.SearchClient;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalWindowController;
import org.olat.presentation.search.SearchServiceUIFactory.DisplayOption;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller with a simple input for the full text search. The display option select how the input is shown: only a button, button with text, input field and button.
 * <P>
 * Initial Date: 3 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class SearchInputController extends FormBasicController implements SearchController {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String FUZZY_SEARCH = "~0.7";
    private static final String CMD_DID_YOU_MEAN_LINK = "didYouMeanLink-";
    private static final String SEARCH_STORE_KEY = "search-store-key";
    private static final String SEARCH_CACHE_KEY = "search-cache-key";

    private String parentContext;
    private String documentType;
    private String resourceUrl;
    private boolean resourceContextEnable = true;

    private final DisplayOption displayOption;

    protected FormLink searchButton;
    protected TextElement searchInput;
    private ResultsSearchController resultCtlr;
    private Controller searchDialogBox;

    protected List<FormLink> didYouMeanLinks;

    private Map<String, Properties> prefs;
    private SearchLRUCache searchCache;
    private SearchClient searchClient;

    public SearchInputController(final UserRequest ureq, final WindowControl wControl, final String resourceUrl, final DisplayOption displayOption) {
        super(ureq, wControl, LAYOUT_HORIZONTAL);
        this.resourceUrl = resourceUrl;
        this.displayOption = displayOption;
        setSearchStore(ureq);
        initForm(ureq);
        loadPersistedSearch();
        loadContext();
    }

    public SearchInputController(final UserRequest ureq, final WindowControl wControl, final String resourceUrl, final String customPage) {
        super(ureq, wControl, customPage);
        this.displayOption = DisplayOption.STANDARD_TEXT;
        this.resourceUrl = resourceUrl;
        setSearchStore(ureq);
        initForm(ureq);
        loadPersistedSearch();
        loadContext();
    }

    public SearchInputController(final UserRequest ureq, final WindowControl wControl, final String resourceUrl, final DisplayOption displayOption, final Form mainForm) {
        super(ureq, wControl, LAYOUT_HORIZONTAL, null, mainForm);
        this.displayOption = displayOption;
        this.resourceUrl = resourceUrl;
        setSearchStore(ureq);
        initForm(ureq);
        loadPersistedSearch();
        loadContext();
    }

    @Override
    public String getParentContext() {
        return parentContext;
    }

    @Override
    public void setParentContext(final String parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    public String getDocumentType() {
        return documentType;
    }

    @Override
    public void setDocumentType(final String documentType) {
        this.documentType = documentType;
    }

    @Override
    public String getResourceUrl() {
        return resourceUrl;
    }

    @Override
    public void setResourceUrl(final String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    @Override
    public boolean isResourceContextEnable() {
        return resourceContextEnable;
    }

    @Override
    public void setResourceContextEnable(final boolean resourceContextEnable) {
        this.resourceContextEnable = resourceContextEnable;
    }

    @Override
    public String getSearchString() {
        return searchInput.getValue();
    }

    @Override
    public void setSearchString(final String searchString) {
        if (StringHelper.containsNonWhitespace(searchString)) {
            if (searchInput != null) {
                searchInput.setValue(searchString);
            }
        }
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        searchClient = CoreSpringFactory.getBean(SearchClient.class);

        if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.STANDARD_TEXT)) {
            searchInput = uifactory.addTextElement("search_input", "search.title", 255, "", formLayout);
            searchInput.setLabel(null, null);
        }

        if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.BUTTON)) {
            searchButton = uifactory.addFormLink("search", "", "", formLayout, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS);
            searchButton.setCustomEnabledLinkCSS("o_fulltext_search_button b_small_icon");
        } else if (displayOption.equals(DisplayOption.BUTTON_WITH_LABEL)) {
            searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON_SMALL);
        } else if (displayOption.equals(DisplayOption.STANDARD_TEXT)) {
            final String searchLabel = getTranslator().translate("search");
            searchButton = uifactory.addFormLink("search", searchLabel, "", formLayout, Link.NONTRANSLATED + Link.BUTTON_SMALL);
        }
        searchButton.setEnabled(true);
    }

    private void loadContext() {
        if (resourceUrl != null) {
            final ContextTokens context = getContextTokens(resourceUrl);
            setContext(context);
        }
    }

    protected void setContext(final ContextTokens context) {
        if (!context.isEmpty()) {
            final String scope = context.getValueAt(context.getSize() - 1);
            final String tooltip = getTranslator().translate("form.search.label.tooltip", new String[] { scope });
            ((Link) searchButton.getComponent()).setTooltip(tooltip, false);
        }
    }

    private void setSearchStore(final UserRequest ureq) {
        prefs = (Map<String, Properties>) ureq.getUserSession().getEntry(SEARCH_STORE_KEY);
        if (prefs == null) {
            prefs = new HashMap<String, Properties>();
            ureq.getUserSession().putEntry(SEARCH_STORE_KEY, prefs);
        }

        searchCache = (SearchLRUCache) ureq.getUserSession().getEntry(SEARCH_CACHE_KEY);
        if (searchCache == null) {
            searchCache = new SearchLRUCache();
            ureq.getUserSession().putEntry(SEARCH_CACHE_KEY, searchCache);
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        doSearch(ureq);
    }

    @Override
    protected void formNOK(final UserRequest ureq) {
        doSearch(ureq);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == searchButton) {
            doSearch(ureq);
        } else if (didYouMeanLinks != null && didYouMeanLinks.contains(source)) {
            final String didYouMeanWord = (String) source.getUserObject();
            searchInput.setValue(didYouMeanWord);
            doSearch(ureq, didYouMeanWord, null, parentContext, documentType, resourceUrl, 0, RESULT_PER_PAGE, false);
        }
    }

    protected void doSearch(final UserRequest ureq) {
        if (resultCtlr != null) {
            removeAsListenerAndDispose(resultCtlr);
            resultCtlr = null;
        }

        String oldSearchString = null;
        final Properties props = getPersistedSearch();
        if (props != null) {
            oldSearchString = props.getProperty("s");
        }

        persistSearch(ureq);

        if (DisplayOption.BUTTON.equals(displayOption) || DisplayOption.BUTTON_WITH_LABEL.equals(displayOption)) {
            // no search, only popup
            createResultsSearchController(ureq);
            popupResultsSearchController(ureq);
            if (resultCtlr.getPersistedSearch() != null && !resultCtlr.getPersistedSearch().isEmpty()) {
                resultCtlr.doSearch(ureq);
            }
        } else {
            final String searchString = getSearchString();
            if (StringHelper.containsNonWhitespace(searchString)) {
                if (oldSearchString != null && !oldSearchString.equals(searchString)) {
                    resetSearch();
                }

                createResultsSearchController(ureq);
                resultCtlr.setSearchString(searchString);
                popupResultsSearchController(ureq);
                resultCtlr.doSearch(ureq);
            }
        }
    }

    protected Properties getPersistedSearch() {
        if (getResourceUrl() != null) {
            final String uri = getResourceUrl();
            Properties props = prefs.get(uri);
            if (props == null) {
                props = new Properties();
                prefs.put(uri, props);
            }
            return props;
        }
        // not possible but i don't want to trigger a red screen for this if i'm wrong
        return new Properties();
    }

    protected void resetSearch() {
        if (getResourceUrl() != null) {
            final String uri = getResourceUrl();
            final Properties props = prefs.get(uri);
            if (props != null) {
                prefs.remove(uri);
            }
        }
    }

    protected void persistSearch(final UserRequest ureq) {
        if (getResourceUrl() != null) {
            final String uri = getResourceUrl();
            Properties props = prefs.get(uri);
            if (props == null) {
                props = new Properties();
            }
            getSearchProperties(props);

            if (props.isEmpty()) {
                prefs.remove(uri);
            } else {
                prefs.put(uri, props);
            }
        }
    }

    protected void loadPersistedSearch() {
        if (getResourceUrl() != null) {
            final String uri = getResourceUrl();
            final Properties props = prefs.get(uri);
            if (props != null) {
                setSearchProperties(props);
            }
        }
    }

    private void createResultsSearchController(final UserRequest ureq) {
        resultCtlr = new ResultsSearchController(ureq, getWindowControl(), getResourceUrl());
        resultCtlr.setDocumentType(getDocumentType());
        resultCtlr.setParentContext(getParentContext());
        resultCtlr.setResourceContextEnable(isResourceContextEnable());
        listenTo(resultCtlr);
    }

    protected void getSearchProperties(final Properties props) {
        if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.STANDARD_TEXT)) {
            final String searchString = getSearchString();
            props.setProperty("s", searchString == null ? "" : searchString);
        }
    }

    protected void setSearchProperties(final Properties props) {
        if (displayOption.equals(DisplayOption.STANDARD) || displayOption.equals(DisplayOption.STANDARD_TEXT)) {
            final String searchString = props.getProperty("s");
            if (StringHelper.containsNonWhitespace(searchString)) {
                setSearchString(searchString);
            } else {
                setSearchString("");
            }
        }
    }

    private void popupResultsSearchController(final UserRequest ureq) {
        final String title = translate("search.title");
        final boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
        if (ajaxOn) {
            searchDialogBox = new CloseableModalWindowController(ureq, getWindowControl(), title, resultCtlr.getInitialComponent(), "ofulltextsearch");
            ((CloseableModalWindowController) searchDialogBox).activate();
            resultCtlr.listenTo(searchDialogBox);
        } else {
            searchDialogBox = new CloseableModalController(getWindowControl(), title, resultCtlr.getInitialComponent());
            ((CloseableModalController) searchDialogBox).activate();
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == resultCtlr) {
            if (event instanceof SearchEvent) {
                final SearchEvent goEvent = (SearchEvent) event;
                final ResultDocument doc = goEvent.getDocument();
                gotoSearchResult(ureq, doc);
            } else if (event == Event.DONE_EVENT) {
                setSearchString(resultCtlr.getSearchString());
            }
        } else if (CloseableModalWindowController.CLOSE_WINDOW_EVENT.equals(event)) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    public void closeSearchDialogBox() {
        if (searchDialogBox instanceof CloseableModalController) {
            ((CloseableModalController) searchDialogBox).deactivate();
        } else if (searchDialogBox instanceof CloseableModalWindowController) {
            ((CloseableModalWindowController) searchDialogBox).deactivate();
        }
        searchDialogBox = null;
    }

    /**
     * @param ureq
     * @param command
     */
    @Override
    public void gotoSearchResult(final UserRequest ureq, final ResultDocument document) {
        try {
            // attach the launcher data
            closeSearchDialogBox();
            final String url = document.getResourceUrl();
            if (!StringHelper.containsNonWhitespace(url)) {
                // no url, no document
                getWindowControl().setWarning(getTranslator().translate("error.resource.could.not.found"));
            } else if (url != null && url.startsWith("[ContextHelpModule:")) {
                // do something special for ContextHelp
                final int pathIndex = url.indexOf("path=");
                final String uri = url.substring(pathIndex + 5, url.length() - 1);
                final RedirectMediaResource rsrc = new RedirectMediaResource(uri);
                ureq.getDispatchResult().setResultingMediaResource(rsrc);
            } else {
                final BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
                final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
                NewControllerFactory.getInstance().launch(ureq, bwControl);
            }
        } catch (final Exception ex) {
            log.debug("Document not found");
            getWindowControl().setWarning(getTranslator().translate("error.resource.could.not.found"));
        }
    }

    protected SearchResults doSearch(final UserRequest ureq, final String searchString, final List<String> condSearchStrings, final String parentCtxt,
            final String docType, final String rsrcUrl, final int firstResult, final int maxReturns, final boolean doSpellCheck) {

        String query = null;
        List<String> condQueries = null;
        try {
            if (doSpellCheck) {
                // remove first old "did you mean words"
                hideDidYouMeanWords();
            }

            query = getQueryString(searchString, false);
            condQueries = getCondQueryStrings(condSearchStrings, parentCtxt, docType, rsrcUrl);
            SearchResults searchResults = searchCache.get(getQueryCacheKey(firstResult, query, condQueries));
            if (searchResults == null || true) {
                searchResults = searchClient.doSearch(query, condQueries, ureq.getIdentity(), ureq.getUserSession().getRoles(), firstResult, maxReturns, true);
                searchCache.put(getQueryCacheKey(firstResult, query, condQueries), searchResults);
            }
            if ((firstResult == 0 && searchResults.getList().isEmpty()) && !query.endsWith(FUZZY_SEARCH)) {
                // result-list was empty => first try to find word via spell-checker
                if (doSpellCheck) {
                    final Set<String> didYouMeansWords = searchClient.spellCheck(searchString);
                    if (didYouMeansWords != null && !didYouMeansWords.isEmpty()) {
                        setDidYouMeanWords(didYouMeansWords);
                    } else {
                        searchResults = doFuzzySearch(ureq, searchString, null, parentCtxt, docType, rsrcUrl, firstResult, maxReturns);
                    }
                } else {
                    searchResults = doFuzzySearch(ureq, searchString, null, parentCtxt, docType, rsrcUrl, firstResult, maxReturns);
                }
            }

            if (firstResult == 0 && searchResults.getList().isEmpty()) {
                showInfo("found.no.result.try.fuzzy.search");
            }
            return searchResults;
        } catch (final QueryException e) {
            getWindowControl().setWarning(translate("invalid.search.query"));
            // getWindowControl().setWarning(translate("invalid.search.query.with.wildcard"));
        } catch (final ServiceNotAvailableException e) {
            getWindowControl().setWarning(translate("search.service.not.available"));
        } catch (final Exception e) {
            log.error("Unexpected exception while searching", e);
            getWindowControl().setWarning(translate("search.service.unexpected.error"));
        }
        return SearchResults.EMPTY_SEARCH_RESULTS;
    }

    protected SearchResults doFuzzySearch(final UserRequest ureq, final String searchString, final List<String> condSearchStrings, final String parentCtxt,
            final String docType, final String rsrcUrl, final int firstResult, final int maxReturns) throws QueryException, ServiceNotAvailableException {
        hideDidYouMeanWords();
        final String query = getQueryString(searchString, true);
        final List<String> condQueries = getCondQueryStrings(condSearchStrings, parentCtxt, docType, rsrcUrl);
        SearchResults searchResults = searchCache.get(getQueryCacheKey(firstResult, query, condQueries));
        if (searchResults == null) {
            searchResults = searchClient.doSearch(query, condQueries, ureq.getIdentity(), ureq.getUserSession().getRoles(), firstResult, maxReturns, true);
            searchCache.put(getQueryCacheKey(firstResult, query, condQueries), searchResults);
        }
        return searchResults;
    }

    private Object getQueryCacheKey(final int firstResult, final String query, final List<String> condQueries) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append(firstResult).append(']').append(query).append(' ');
        for (final String condQuery : condQueries) {
            sb.append(condQuery).append(' ');
        }
        return sb.toString();
    }

    public Set<String> getDidYouMeanWords() {
        if (didYouMeanLinks != null && !didYouMeanLinks.isEmpty()) {
            final Set<String> didYouMeanWords = new HashSet<String>();
            for (final FormLink link : didYouMeanLinks) {
                final String word = (String) link.getUserObject();
                didYouMeanWords.add(word);
            }
            return didYouMeanWords;
        }
        return Collections.emptySet();
    }

    /**
     * Unregister existing did-you-mean-links from content and add new links.
     * 
     * @param didYouMeansWords
     *            List of 'did you mean' words
     */
    public void setDidYouMeanWords(final Set<String> didYouMeansWords) {
        // unregister existing did-you-mean links
        hideDidYouMeanWords();

        didYouMeanLinks = new ArrayList<FormLink>(didYouMeansWords.size());
        int wordNumber = 0;
        for (final String word : didYouMeansWords) {
            final FormLink l = uifactory.addFormLink(CMD_DID_YOU_MEAN_LINK + wordNumber++, word, null, flc, Link.NONTRANSLATED);
            l.setUserObject(word);
            didYouMeanLinks.add(l);
        }
        flc.contextPut("didYouMeanLinks", didYouMeanLinks);
        flc.contextPut("hasDidYouMean", Boolean.TRUE);
    }

    protected void hideDidYouMeanWords() {
        // unregister existing did-you-mean links
        if (didYouMeanLinks != null) {
            for (int i = 0; i < didYouMeanLinks.size(); i++) {
                flc.remove(CMD_DID_YOU_MEAN_LINK + i);
            }
            didYouMeanLinks = null;
        }
        flc.contextPut("didYouMeanLinks", didYouMeanLinks);
        flc.contextPut("hasDidYouMean", Boolean.FALSE);
    }

    private String getQueryString(final String searchString, final boolean fuzzy) {
        final StringBuilder query = new StringBuilder(searchString);
        if (fuzzy) {
            query.append(FUZZY_SEARCH);
        }
        return query.toString();
    }

    private List<String> getCondQueryStrings(final List<String> condSearchStrings, final String parentCtxt, final String docType, final String rsrcUrl) {
        final List<String> queries = new ArrayList<String>();
        if (condSearchStrings != null && !condSearchStrings.isEmpty()) {
            queries.addAll(condSearchStrings);
        }

        if (StringHelper.containsNonWhitespace(parentCtxt)) {
            appendAnd(queries, AbstractOlatDocument.PARENT_CONTEXT_TYPE_FIELD_NAME, ":\"", parentCtxt, "\"");
        }
        if (StringHelper.containsNonWhitespace(docType)) {
            appendAnd(queries, "(", AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, ":(", docType, "))");
        }
        if (StringHelper.containsNonWhitespace(rsrcUrl)) {
            appendAnd(queries, AbstractOlatDocument.RESOURCEURL_FIELD_NAME, ":", escapeResourceUrl(rsrcUrl), "*");
        }
        return queries;
    }

    private void appendAnd(final List<String> queries, final String... strings) {
        final StringBuilder query = new StringBuilder();
        for (final String string : strings) {
            query.append(string);
        }

        if (query.length() > 0) {
            queries.add(query.toString());
        }
    }

    /**
     * Remove the ROOT keyword, duplicate entry in the business path and escape the keywords used by lucene.
     * 
     * @param url
     * @return
     */
    protected String escapeResourceUrl(final String url) {
        final List<String> tokens = getResourceUrlTokenized(url);
        final StringBuilder sb = new StringBuilder();
        for (final String token : tokens) {
            sb.append("\\[").append(token.replace(":", "\\:")).append("\\]");
        }
        return sb.toString();
    }

    protected List<String> getResourceUrlTokenized(String url) {
        if (url.startsWith("ROOT")) {
            url = url.substring(4, url.length());
        }
        final List<String> tokens = new ArrayList<String>();
        for (final StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens();) {
            final String token = tokenizer.nextToken();
            if (!tokens.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    protected ContextTokens getContextTokens(final String resourceURL) {
        final SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory) CoreSpringFactory.getBean(SearchServiceUIFactory.class);
        final List<String> tokens = getResourceUrlTokenized(resourceURL);
        final String[] keys = new String[tokens.size() + 1];
        final String[] values = new String[tokens.size() + 1];
        keys[0] = "";
        values[0] = translate("search.context.all");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            final String token = tokens.get(i);
            keys[i + 1] = sb.append('[').append(token).append(']').toString();
            values[i + 1] = searchUIFactory.getBusinessPathLabel(token, tokens, getLocale());
        }
        return new ContextTokens(keys, values);
    }

    @Override
    public FormItem getFormItem() {
        return flc;
    }

    public class ContextTokens {
        private final String[] keys;
        private final String[] values;

        public ContextTokens(final String[] keys, final String[] values) {
            this.keys = keys == null ? new String[0] : keys;
            this.values = values == null ? new String[0] : values;
        }

        public String[] getKeys() {
            return keys;
        }

        public String[] getValues() {
            return values;
        }

        public boolean isEmpty() {
            return values.length == 0;
        }

        public int getSize() {
            return values.length;
        }

        public String getKeyAt(final int index) {
            if (keys != null && index < keys.length && index >= 0) {
                return keys[index];
            }
            return "";
        }

        public String getValueAt(final int index) {
            if (values != null && index < values.length && index >= 0) {
                return values[index];
            }
            return "";
        }
    }
}
