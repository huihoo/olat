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

import java.util.ArrayList;
import java.util.List;

import org.olat.lms.search.SearchResults;
import org.olat.lms.search.document.ResultDocument;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller which show the list of results, with paging.
 * <P>
 * Events:
 * <ul>
 * <li>SearchEvent</li>
 * </ul>
 * Initial Date: 3 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ResultsController extends FormBasicController {
    private FormLink previousLink, nextLink;
    private FormLink highlightLink, dishighlightLink;

    private int currentPage;
    public static final int RESULT_PER_PAGE = 10;
    private boolean highlight = true;
    private SearchResults searchResults;

    private final List<ResultDocument> documents = new ArrayList<ResultDocument>();
    private final List<ResultController> resultsCtrl = new ArrayList<ResultController>();

    public ResultsController(final UserRequest ureq, final WindowControl wControl, final Form mainForm) {
        super(ureq, wControl, LAYOUT_CUSTOM, "results", mainForm);

        initForm(ureq);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        previousLink = uifactory.addFormLink("previous.page", formLayout);
        previousLink.setCustomEnabledLinkCSS("b_large");
        previousLink.setCustomDisabledLinkCSS("b_large");
        nextLink = uifactory.addFormLink("next.page", formLayout);
        nextLink.setCustomEnabledLinkCSS("b_large");
        nextLink.setCustomDisabledLinkCSS("b_large");
        highlightLink = uifactory.addFormLink("highlight.page", "enable.highlighting", "enable.highlighting", formLayout, Link.LINK);
        dishighlightLink = uifactory.addFormLink("dishighlight.page", "disable.highlighting", "disable.highlighting", formLayout, Link.LINK);
        flc.contextPut("highlight", true);
        reset();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public SearchResults getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(final UserRequest ureq, final SearchResults results) {
        reset();
        this.searchResults = results;
        documents.addAll(searchResults.getList());
        setSearchResults(ureq, 0);
    }

    private void setSearchResults(final UserRequest ureq, final int page) {
        currentPage = page;
        updateUI(ureq);
    }

    public void nextSearchResults(final UserRequest ureq, final SearchResults results) {
        searchResults = results;

        // the last result set can be empty
        if (!searchResults.getList().isEmpty()) {
            currentPage++;

            final int pos = currentPage * RESULT_PER_PAGE;
            for (int i = 0; (i < RESULT_PER_PAGE) && (i < searchResults.getList().size()); i++) {
                final ResultDocument document = searchResults.getList().get(i);
                if (documents.size() > pos + i) {
                    documents.set(pos + i, document);
                } else {
                    documents.add(document);
                }
            }
        }
        updateUI(ureq);
    }

    private void updateUI(final UserRequest ureq) {
        removeResultsController();

        final int start = currentPage * RESULT_PER_PAGE;

        final SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory) CoreSpringFactory.getBean(SearchServiceUIFactory.class);
        int count = 0;
        for (int i = start; (count < RESULT_PER_PAGE) && (i < documents.size()); i++) {
            final ResultDocument document = documents.get(i);
            final ResultController ctrl = searchUIFactory.createController(ureq, getWindowControl(), mainForm, document);
            ctrl.setHighlight(highlight);
            listenTo(ctrl);
            flc.add("result_" + (++count), ctrl.getInitialFormItem());
            resultsCtrl.add(ctrl);
        }

        flc.contextPut("numOfPages", getMaxPage() + 1);
        flc.contextPut("numOfResults", getNumOfResults());
        flc.contextPut("results", resultsCtrl);
        flc.contextPut("hasResult", searchResults != null);
        flc.contextPut("emptyResult", documents.isEmpty());
        flc.contextPut("searchResults", searchResults);
        flc.contextPut("currentPage", currentPage + 1);

        previousLink.setEnabled(currentPage != 0);
        nextLink.setEnabled(currentPage != getMaxPage());

        final String[] args = { Integer.toString(getStartResult()), Integer.toString(getEndResult()), Integer.toString(getNumOfResults()) };
        flc.contextPut("resultTitle", getTranslator().translate("search.result.title", args));
    }

    public void reload(final UserRequest ureq) {
        updateUI(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == highlightLink) {
            highlight = true;
            flc.contextPut("highlight", highlight);
            reload(ureq);
        } else if (source == dishighlightLink) {
            highlight = false;
            flc.contextPut("highlight", highlight);
            reload(ureq);
        } else if (source == previousLink) {
            setSearchResults(ureq, Math.max(0, --currentPage));
        } else if (source == nextLink) {
            if (documents.size() <= (currentPage + 1) * RESULT_PER_PAGE) {
                final SearchEvent e = new SearchEvent(getLastLucenePosition() + 1, RESULT_PER_PAGE);
                fireEvent(ureq, e);
            } else {
                setSearchResults(ureq, Math.min(getMaxPage(), ++currentPage));
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event instanceof SearchEvent) {
            if (resultsCtrl.contains(source)) {
                fireEvent(ureq, event);
            }
        }
    }

    private void reset() {
        flc.contextPut("numOfResults", 0);
        flc.contextPut("hasResult", Boolean.FALSE);
        flc.contextPut("emptyResult", Boolean.TRUE);

        documents.clear();
        removeResultsController();
    }

    private void removeResultsController() {
        if (resultsCtrl != null && !resultsCtrl.isEmpty()) {
            for (int i = 0; i < resultsCtrl.size(); i++) {
                flc.remove("result_" + (i + 1));
                removeAsListenerAndDispose(resultsCtrl.get(i));
            }
            resultsCtrl.clear();
            flc.contextPut("results", resultsCtrl);
        }
    }

    public int getStartResult() {
        return currentPage * RESULT_PER_PAGE + 1;
    }

    public int getEndResult() {
        if ((currentPage * RESULT_PER_PAGE + RESULT_PER_PAGE) > documents.size()) {
            return documents.size();
        } else {
            return getStartResult() + RESULT_PER_PAGE - 1;
        }
    }

    /**
     * @return Number of pages for current result-list.
     */
    public int getMaxPage() {
        final int numOfResults = getNumOfResults();
        int maxPage = numOfResults / RESULT_PER_PAGE;
        if ((numOfResults) % RESULT_PER_PAGE == 0) {
            maxPage--;
        }
        return maxPage;
    }

    public int getNumOfResults() {
        if (searchResults.getList().size() < RESULT_PER_PAGE) {
            // last result set, all documents are loaded
            return documents.size();
        }
        return searchResults.getTotalDocs() - getLastLucenePosition() + documents.size() - 1;
    }

    private int getLastLucenePosition() {
        if (documents.isEmpty()) {
            return 0;
        }
        return documents.get(documents.size() - 1).getLucenePosition();
    }

    public FormItem getFormItem() {
        return this.flc;
    }
}
