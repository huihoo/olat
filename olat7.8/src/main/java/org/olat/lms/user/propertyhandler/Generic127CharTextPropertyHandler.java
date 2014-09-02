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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.user.propertyhandler;

import java.util.Locale;
import java.util.Map;

import org.olat.data.user.User;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.formelements.FormElement;
import org.olat.presentation.framework.core.formelements.TextElement;

/**
 * <h3>Description:</h3> This generic text property provides a userfield that has a maximum of 127 characters length. It can contain any string, the only validity check
 * that is performed is the not empty check if the property is declared as mandatory.
 * <p>
 * Create subclass of this class if you allow only a certain range of entered data. See EmailProperty as an example.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class Generic127CharTextPropertyHandler extends AbstractUserPropertyHandler {

    /**
     * spring
     */
    protected Generic127CharTextPropertyHandler() {
        //
    }

    /**
	 */
    @Override
    public void updateUserFromFormElement(final User user, final FormElement ui) {
        final String internalValue = getStringValue(ui);
        setInternalValue(user, internalValue);
    }

    /**
	 */
    @Override
    public void updateUserFromFormItem(final User user, final FormItem formItem) {
        final String internalValue = getStringValue(formItem);
        setInternalValue(user, internalValue);
    }

    /**
	 */
    @Override
    public String getStringValue(final FormElement ui) {
        return ((TextElement) ui).getValue();
    }

    /**
	 */
    @Override
    public String getStringValue(final FormItem formItem) {
        return ((org.olat.presentation.framework.core.components.form.flexible.elements.TextElement) formItem).getValue();
    }

    /**
	 */
    @Override
    public String getStringValue(final String displayValue, final Locale locale) {
        // default implementation is to use same as displayValue
        return displayValue;
    }

    /**
	 */
    @Override
    public FormElement getFormElement(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser) {
        // default is no element
        TextElement ui = null;
        ui = new TextElement(i18nFormElementLabelKey(), getInternalValue(user), 127);
        if (getUserService().isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
            ui.setReadOnly(true);
        }
        if (getUserService().isMandatoryUserProperty(usageIdentifyer, this)) {
            ui.setMandatory(true);
        }
        return ui;
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItemContainer)
     */
    @Override
    public FormItem addFormItem(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser,
            final FormItemContainer formItemContainer) {
        org.olat.presentation.framework.core.components.form.flexible.elements.TextElement tElem = null;
        tElem = FormUIFactory.getInstance().addTextElement(getName(), i18nFormElementLabelKey(), 127, getInternalValue(user), formItemContainer);
        tElem.setItemValidatorProvider(this);
        tElem.setLabel(i18nFormElementLabelKey(), null);
        if (getUserService().isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
            tElem.setEnabled(false);
        }
        if (getUserService().isMandatoryUserProperty(usageIdentifyer, this)) {
            tElem.setMandatory(true);
        }
        return tElem;
    }

    /**
	 */
    @Override
    public void updateFormElementFromUser(final FormElement ui, final User user) {
        ((TextElement) ui).setValue(getInternalValue(user));
    }

    /**
	 */
    @Override
    public boolean isValid(final FormElement ui, final Map formContext) {
        final TextElement uiText = (TextElement) ui;
        if (ui.isMandatory()) {
            return uiText.notEmpty(i18nFormElementLabelKey() + ".error.empty");
        }
        return true;
    }

    /**
	 */
    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {
        final org.olat.presentation.framework.core.components.form.flexible.elements.TextElement textElemItem = (org.olat.presentation.framework.core.components.form.flexible.elements.TextElement) formItem;
        if (textElemItem.isMandatory()) {
            return !textElemItem.isEmpty("new.form.mandatory");
        }
        return true;
    }

    /**
	 */
    @Override
    public boolean isValidValue(final String value, final ValidationError validationError, final Locale locale) {
        if (value != null && value.length() > 127) {
            validationError.setErrorKey("general.error.max.127");
            return false;
        }
        return true;
    }

}
