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
package org.olat.presentation.wiki;

import org.olat.lms.wiki.WikiInputValidation;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Provides a search text input field
 * <P>
 * Initial Date: 07.02.2008 <br>
 * 
 * @author guido
 */
public class WikiArticleSearchForm extends FormBasicController {

    private TextElement searchQuery;

    public WikiArticleSearchForm(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        initForm(this.flc, this, ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {/**/
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        searchQuery.showError(false);
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @SuppressWarnings("unused")
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        searchQuery = uifactory.addTextElement("search", null, 250, null, formLayout);
        searchQuery.setDisplaySize(40);
        searchQuery.setExampleKey("navigation.create.article.example", null);

        // it doesn't seem to work
        // String regExp = "[a-zA-Z0-9]*";
        // searchQuery.setRegexMatchCheck(regExp, "navigation.create.article.validation.error");

        final FormSubmit submit = new FormSubmit("subm", "navigation.create.article");
        formLayout.add(submit);
    }

    /**
     * Cannot filters XSS out because it escapes the umlauts as well.
     */
    public String getQuery() {
        String query = searchQuery.getValue();
        searchQuery.setValue(null);
        // if (query != null) {
        // query = FilterFactory.filterXSS(query);
        // }
        return query;
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean isValidPageName = WikiInputValidation.validatePageName(searchQuery.getValue());

        if (isValidPageName) {
            return true;
        }

        searchQuery.setErrorKey("navigation.create.article.validation.error", null);
        searchQuery.showError(true);
        return false;

    }

}
