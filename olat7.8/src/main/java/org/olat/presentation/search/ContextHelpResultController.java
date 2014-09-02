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

import org.olat.lms.search.document.ResultDocument;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * Show context help documents. Choose if the link to open the document go to a new window or stay in the same.
 * <P>
 * Initial Date: 11 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ContextHelpResultController extends FormBasicController implements ResultController {

    private final ResultDocument document;
    private boolean highlight;

    public ContextHelpResultController(final UserRequest ureq, final WindowControl wControl, final Form mainForm, final ResultDocument document) {
        super(ureq, wControl, LAYOUT_CUSTOM, "contextHelpResult", mainForm);
        this.document = document;
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        if (formLayout instanceof FormLayoutContainer) {
            final FormLayoutContainer formLayoutCont = (FormLayoutContainer) formLayout;
            formLayoutCont.contextPut("result", document);
            formLayoutCont.contextPut("id", this.hashCode());
            formLayoutCont.contextPut("formatter", Formatter.getInstance(getLocale()));
        }
        final String target = openLinkInNewWindow(ureq) ? "_blank" : "_self";
        flc.contextPut("target", target);
    }

    @Override
    protected void doDispose() {
        //
    }

    private boolean openLinkInNewWindow(final UserRequest ureq) {
        final String context = ureq.getHttpReq().getContextPath();
        final String request = ureq.getHttpReq().getRequestURI();
        if (StringHelper.containsNonWhitespace(context) && StringHelper.containsNonWhitespace(request)) {
            final boolean helpDispatcher = request.startsWith(context + "/help/");
            if (helpDispatcher) {
                return false;
            }
        }
        return true;
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
    public FormItem getInitialFormItem() {
        return flc;
    }
}
