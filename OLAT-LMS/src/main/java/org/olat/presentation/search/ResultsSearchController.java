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

import java.util.List;
import java.util.Properties;

import org.olat.lms.search.SearchResults;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * With toggle simple search &lt;-&gt; extended search
 * <P>
 * Initial Date: 3 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ResultsSearchController extends SearchInputController implements Activateable {

    private boolean extendedSearch;
    private FormLink simpleSearchLink;
    private FormLink extendedSearchLink;
    private SingleSelection contextSelection;

    private ResultsController resultCtlr;
    private AdvancedSearchInputController advancedSearchController;

    public ResultsSearchController(final UserRequest ureq, final WindowControl wControl, final String resourceUrl) {
        super(ureq, wControl, resourceUrl, "searchInput");
    }

    @Override
    public void setResourceContextEnable(final boolean resourceContextEnable) {
        if (contextSelection.isVisible() != resourceContextEnable) {
            contextSelection.setVisible(resourceContextEnable);
        }
        advancedSearchController.setResourceContextEnable(resourceContextEnable);
        super.setResourceContextEnable(resourceContextEnable);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        extendedSearchLink = uifactory.addFormLink("switch.advanced.search", formLayout);
        simpleSearchLink = uifactory.addFormLink("switch.simple.search", formLayout);

        final FormLayoutContainer searchLayout = FormLayoutContainer.createHorizontalFormLayout("search_form", getTranslator());
        formLayout.add(searchLayout);
        super.initForm(searchLayout, listener, ureq);

        final FormLayoutContainer extSearchLayout = FormLayoutContainer.createVerticalFormLayout("ext_search_form", getTranslator());
        formLayout.add(extSearchLayout);
        advancedSearchController = new AdvancedSearchInputController(ureq, getWindowControl(), mainForm);
        listenTo(advancedSearchController);
        extSearchLayout.add("adv_search", advancedSearchController.getFormItem());

        contextSelection = uifactory.addRadiosHorizontal("context", "form.search.label.context", formLayout, new String[0], new String[0]);

        resultCtlr = new ResultsController(ureq, getWindowControl(), mainForm);
        listenTo(resultCtlr);
        formLayout.add("resultList", resultCtlr.getFormItem());
    }

    @Override
    protected void setContext(final ContextTokens context) {
        super.setContext(context);
        contextSelection.setKeysAndValues(context.getKeys(), context.getValues(), null);
        if (!context.isEmpty()) {
            String selectedContext = context.getKeyAt(context.getSize() - 1);
            final Properties props = getPersistedSearch();
            if (props != null && props.containsKey("ctxt")) {
                selectedContext = props.getProperty("ctxt");
            }
            contextSelection.select(selectedContext, true);
        }
        advancedSearchController.setContextKeysAndValues(context.getKeys(), context.getValues());

        final String extended = getPersistedSearch().getProperty("ext", "false");
        if ("true".equals(extended)) {
            extendedSearch = true;
            advancedSearchController.setSearchString(getSearchString());
            advancedSearchController.load();
            flc.contextPut("advancedSearchFlag", extendedSearch);
        }
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
            advancedSearchController.setSearchString(didYouMeanWord);

            String key = null;
            List<String> condQueries = null;
            if (extendedSearch) {
                key = advancedSearchController.getContext();
                condQueries = advancedSearchController.getQueryStrings();
            } else if (contextSelection.isOneSelected()) {
                key = contextSelection.getSelectedKey();
            }

            hideDidYouMeanWords();
            final SearchResults results = doSearch(ureq, didYouMeanWord, condQueries, getParentContext(), getDocumentType(), key, 0, RESULT_PER_PAGE, false);
            resultCtlr.setSearchResults(ureq, results);
            persistSearch(ureq);
        } else if (source == extendedSearchLink) {
            extendedSearch = true;
            advancedSearchController.setSearchString(getSearchString());
            advancedSearchController.load();
            flc.contextPut("advancedSearchFlag", extendedSearch);
        } else if (source == simpleSearchLink) {
            extendedSearch = false;
            advancedSearchController.unload();
            setSearchString(advancedSearchController.getSearchString());
            flc.contextPut("advancedSearchFlag", extendedSearch);
        } else if (source == advancedSearchController.getSearchButton()) {
            doSearch(ureq);
        }
    }

    @Override
    protected void doSearch(final UserRequest ureq) {
        doSearch(ureq, 0);
    }

    private void doSearch(final UserRequest ureq, final int firstResult) {
        SearchResults results;
        if (extendedSearch) {
            final String query = advancedSearchController.getSearchString();
            final List<String> condQueries = advancedSearchController.getQueryStrings();
            final String key = advancedSearchController.getContext();
            if (advancedSearchController.isDocumentTypesSelected()) {
                // if document types are selected, these queries overwrite the conditional query for document type
                // set in this controller
                results = doSearch(ureq, query, condQueries, getParentContext(), null, key, firstResult, RESULT_PER_PAGE, true);
            } else {
                results = doSearch(ureq, query, condQueries, getParentContext(), getDocumentType(), key, firstResult, RESULT_PER_PAGE, true);
            }
        } else {
            final String searchString = getSearchString();
            if (StringHelper.containsNonWhitespace(searchString)) {
                String key = null;
                if (contextSelection.isOneSelected()) {
                    key = contextSelection.getSelectedKey();
                }
                results = doSearch(ureq, searchString, null, getParentContext(), getDocumentType(), key, firstResult, RESULT_PER_PAGE, true);
            } else {
                results = SearchResults.EMPTY_SEARCH_RESULTS;
            }
        }

        if (firstResult == 0) {
            resultCtlr.setSearchResults(ureq, results);
        } else {
            resultCtlr.nextSearchResults(ureq, results);
        }

        persistSearch(ureq);
    }

    @Override
    protected void getSearchProperties(final Properties props) {
        super.getSearchProperties(props);
        if (contextSelection.isOneSelected() && contextSelection.getSelectedKey() != null) {
            props.put("ctxt", contextSelection.getSelectedKey());
        } else {
            props.remove("ctxt");
        }
        props.put("ext", extendedSearch ? "true" : "false");
        advancedSearchController.getSearchProperties(props);

        final int currentPage = resultCtlr.getCurrentPage();
        if (currentPage >= 0) {
            props.put("c_page", Integer.toString(currentPage));
        } else {
            props.remove("c_page");
        }
    }

    @Override
    protected void setSearchProperties(final Properties props) {
        super.setSearchProperties(props);
        // set context after
        advancedSearchController.setSearchProperties(props);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == resultCtlr) {
            if (SearchEvent.NEW_SEARCH_EVENT.equals(event.getCommand())) {
                final SearchEvent e = (SearchEvent) event;
                doSearch(ureq, e.getFirstResult());
            } else {
                fireEvent(ureq, event);
            }
        } else {
            super.event(ureq, source, event);
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    /**
     * string
     */
    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        if (StringHelper.containsNonWhitespace(viewIdentifier)) {
            searchInput.setValue(viewIdentifier);
            doSearch(ureq);
        }
    }
}
