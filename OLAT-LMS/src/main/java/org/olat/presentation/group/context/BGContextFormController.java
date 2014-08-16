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
 * Copyright (c) 2009 frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.group.context;

import org.olat.data.group.context.BGContext;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.group.learn.DefaultContextTranslationHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Provides a FlexiForm-based dialog for entering group administration info.
 * 
 * @author twuersch
 */
public class BGContextFormController extends FormBasicController {

    /** Input element for the group name. */
    private TextElement name;

    /** Key for the group type. */
    private final String typeKey;

    /** Checkbox "only visible within course". */
    private MultipleSelectionElement defaultContext;

    /** Keys for the checkbox. */
    private final String[] defaultContextKeys = new String[] { "form.defaultContext" };

    /** Values for the checkbox. */
    private final String[] defaultContextValues = new String[] { "" };

    /** Input element for the description of this group. */
    private RichTextElement description;

    /** Decides whether the "only visible within course" checkbox is shown. */
    private final boolean showIsDefaultOption;

    /**
     * Creates this controller.
     * 
     * @param ureq
     *            The user request.
     * @param wControl
     *            The window control.
     * @param groupType
     *            This group's type.
     * @param showIsDefaultOption
     *            Decides whether the "only visible within course" checkbox is shown.
     */
    public BGContextFormController(final UserRequest ureq, final WindowControl wControl, final String groupType, final boolean showIsDefaultOption) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
        this.showIsDefaultOption = showIsDefaultOption;
        this.typeKey = groupType;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Nothing to dispose

    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void formNOK(final UserRequest ureq) {
        fireEvent(ureq, Event.FAILED_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, @SuppressWarnings("unused") final Controller listener, final UserRequest ureq) {
        // add the type of business group as a static text
        uifactory.addStaticTextElement("form.type", typeKey, formLayout);

        // add visibility checkbox
        if (showIsDefaultOption) {
            defaultContext = uifactory.addCheckboxesHorizontal("form.defaultContext", formLayout, defaultContextKeys, defaultContextValues, null);
        }

        // add business group name input element
        name = uifactory.addTextElement("form.name", "form.name", 255, "", formLayout);
        name.setMandatory(true);

        // add business group description input element
        description = uifactory.addRichTextElementForStringDataMinimalistic("form.description", "form.description", "", 10, -1, false, formLayout, ureq.getUserSession(),
                getWindowControl());

        // Create submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("finish", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());

    }

    /**
     * Set the name and description of the context in this form
     * 
     * @param context
     */
    public void setValues(final BGContext context) {
        final String contextName = DefaultContextTranslationHelper.translateIfDefaultContextName(context, getTranslator());
        if (defaultContext != null) {
            defaultContext.select("form.defaultContext", context.isDefaultContext());
        }
        name.setValue(contextName);
        description.setValue(context.getDescription());
    }

    /**
     * @return boolean true if this is now a default context
     */
    public boolean isDefaultContext() {
        if (defaultContext == null) {
            throw new AssertException("default context switch only editable when form is started with isAdmin=true");
        } else {
            return (defaultContext.getSelectedKeys().size() != 0);
        }
    }

    /**
     * @return the context description as string
     */
    public String getDescription() {
        return description.getValue();
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(@SuppressWarnings("unused") final UserRequest ureq) {
        if (!StringHelper.containsNonWhitespace(name.getValue())) {
            // name is mandatory
            name.setErrorKey("form.error.emptyName", new String[] {});
            return false;
        } else if (!name.getValue().matches(BGContext.VALID_GROUPCONTEXTNAME_REGEXP)) {
            // name must match a given format
            name.setErrorKey("form.error.illegalName", new String[] {});
            return false;
        }
        // stip whitespace
        name.clearError();
        if (description.getValue().length() > 4000) {
            // description has maximum length
            description.setErrorKey("input.toolong", new String[] {});
            return false;
        }
        // ok, passed all checks
        return true;
    }

    /**
     * @return String context name
     */
    public String getName() {
        return name.getValue().trim();
    }

    /**
     * @return String the group context type
     */
    public String getType() {
        return typeKey;
    }
}
