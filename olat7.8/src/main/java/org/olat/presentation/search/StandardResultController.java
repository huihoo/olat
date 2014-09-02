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

import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.search.document.ResultDocument;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * The standard output for a search result.
 * <P>
 * Initial Date: 3 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class StandardResultController extends FormBasicController implements ResultController {

    protected final ResultDocument document;
    protected FormLink docLink, docHighlightLink;
    private boolean highlight;

    public StandardResultController(final UserRequest ureq, final WindowControl wControl, final Form mainForm, final ResultDocument document) {
        super(ureq, wControl, LAYOUT_CUSTOM, "standardResult", mainForm);
        this.document = document;
        initForm(ureq);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        if (formLayout instanceof FormLayoutContainer) {
            final FormLayoutContainer formLayoutCont = (FormLayoutContainer) formLayout;
            formLayoutCont.contextPut("result", document);
            formLayoutCont.contextPut("id", this.hashCode());
            formLayoutCont.contextPut("formatter", Formatter.getInstance(getLocale()));
        }

        String highlightLabel = document.getHighlightTitle();
        // highlightLabel is an html fragment, so it cannot be escaped
        highlightLabel = FilterFactory.filterXSS(highlightLabel);
        docHighlightLink = uifactory.addFormLink("open_doc_highlight", highlightLabel, highlightLabel, formLayout, Link.NONTRANSLATED);
        String icon = document.getCssIcon();
        if (!StringHelper.containsNonWhitespace(icon)) {
            icon = "o_sp_icon";
        }
        final String cssClass = "b_with_small_icon_left " + icon;
        ((Link) docHighlightLink.getComponent()).setCustomEnabledLinkCSS(cssClass);
        ((Link) docHighlightLink.getComponent()).setCustomDisabledLinkCSS(cssClass);

        // we can escape here because is only the title, without highlighting
        final String label = StringHelper.escapeHtml(document.getTitle());
        docLink = uifactory.addFormLink("open_doc", label, label, formLayout, Link.NONTRANSLATED);
        ((Link) docLink.getComponent()).setCustomEnabledLinkCSS(cssClass);
        ((Link) docLink.getComponent()).setCustomDisabledLinkCSS(cssClass);
    }

    @Override
    public boolean isHighlight() {
        return highlight;
    }

    @Override
    public void setHighlight(final boolean highlight) {
        this.highlight = highlight;
        flc.contextPut("highlight", highlight);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == docLink || source == docHighlightLink) {
            if (event != null) {
                fireEvent(ureq, new SearchEvent(document));
            }
        }
    }

    @Override
    public FormItem getInitialFormItem() {
        return flc;
    }
}
