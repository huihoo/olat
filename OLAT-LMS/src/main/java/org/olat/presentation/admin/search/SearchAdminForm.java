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

package org.olat.presentation.admin.search;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Fulltext search input form.
 * 
 * @author Christian Guretzki
 */
public class SearchAdminForm extends FormBasicController {

    private TextElement indexInterval;
    private FormSubmit submit;

    /**
     * @param name
     *            Name of the form
     */
    public SearchAdminForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
    }

    public long getIndexInterval() {
        return Long.parseLong(indexInterval.getValue());
    }

    public void setIndexInterval(final long v) {
        indexInterval.setValue(Long.toString(v));
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("search.admin.form.title");

        indexInterval = uifactory.addTextElement("indexInterval", "search.admin.label.index.interval", 20, "", formLayout);
        indexInterval.setRegexMatchCheck("\\d+", "error.index.interval.must.be.number");
        // indexInterval.setMinValueCheck(0, "error.index.interval.must.be.number");
        indexInterval.setDisplaySize(4);
        submit = new FormSubmit("submit", "submit");
        formLayout.add(submit);
    }

    @Override
    protected void doDispose() {
        //
    }
}
