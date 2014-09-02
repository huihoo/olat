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

import org.olat.lms.wiki.WikiPage;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Form for editing a textfield with wiki content inside
 * <P>
 * Initial Date: May 7, 2006 <br>
 * 
 * @author guido
 */
public class WikiEditArticleForm extends FormBasicController {

    private TextElement wikiContent;
    private TextElement updateComment;
    private final WikiPage page;

    public WikiEditArticleForm(final UserRequest ureq, final WindowControl wControl, final WikiPage page) {
        super(ureq, wControl);
        this.page = page;
        initForm(ureq);
    }

    protected String getWikiContent() {
        return WikiXSSProcessor.getFilteredWikiContent(wikiContent.getValue());
    }

    protected String getUpdateComment() {
        return updateComment.getValue();
    }

    protected void resetUpdateComment() {
        updateComment.setValue("");
    }

    protected void setPage(final WikiPage page) {
        wikiContent.setValue(page.getContent());
        wikiContent.getRootForm().setDirtyMarking(false);
    }

    protected void setDirty(final boolean dirt) {
        wikiContent.getRootForm().setDirtyMarking(dirt);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        fireEvent(ureq, new Event(source.getName()));
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        wikiContent = uifactory.addTextAreaElement("wikiContentElement", 20, 110, page.getContent(), formLayout);
        updateComment = uifactory.addTextElement("wikiUpdateComment", null, 40, "", formLayout);
        updateComment.setExampleKey("update.comment", null);
        // Button layout
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("save", buttonLayout);
        uifactory.addFormLink("save.and.close", buttonLayout, Link.BUTTON);
        uifactory.addFormLink("preview", buttonLayout, Link.BUTTON);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }

}
