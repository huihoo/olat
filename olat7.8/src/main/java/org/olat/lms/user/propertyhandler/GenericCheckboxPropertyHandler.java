/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.lms.user.propertyhandler;

import java.util.Locale;
import java.util.Map;

import org.olat.data.user.User;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.formelements.CheckBoxElement;
import org.olat.presentation.framework.core.formelements.FormElement;

/**
 * Description: Checkbox property handler.<br>
 * <P>
 * Initial Date: 06.08.2008 <br>
 * 
 * @author bja
 */
public class GenericCheckboxPropertyHandler extends AbstractUserPropertyHandler {

    /**
     * spring
     */
    protected GenericCheckboxPropertyHandler() {
        //
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItemContainer)
     */
    @Override
    public FormItem addFormItem(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser,
            final FormItemContainer formItemContainer) {
        SelectionElement sElem = null;
        sElem = FormUIFactory.getInstance().addCheckboxesVertical(getName(), i18nFormElementLabelKey(), formItemContainer, new String[] { getName() },
                new String[] { "" }, null, 1);

        if (getUserService().isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
            sElem.setEnabled(false);
        }
        if (getUserService().isMandatoryUserProperty(usageIdentifyer, this)) {
            sElem.setMandatory(true);
        }
        return sElem;
    }

    /**
	 */
    @Override
    public FormElement getFormElement(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser) {
        CheckBoxElement ui = null;

        final String value = getInternalValue(user);
        final boolean isEnabled = value != null && value.equals("true") ? Boolean.TRUE : Boolean.FALSE;
        ui = new CheckBoxElement(i18nFormElementLabelKey(), isEnabled);
        if (getUserService().isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
            ui.setReadOnly(true);
        }
        if (getUserService().isMandatoryUserProperty(usageIdentifyer, this)) {
            ui.setMandatory(true);
        }
        return ui;
    }

    /**
	 */
    @Override
    public String getStringValue(final FormElement ui) {
        String value = "";
        if (((CheckBoxElement) ui).isChecked()) {
            value = "true";
        } else {
            value = "false";
        }

        return value;
    }

    /**
	 */
    @Override
    public String getStringValue(final FormItem formItem) {
        String value = "";
        if (((SelectionElement) formItem).isSelected(0)) {
            value = "true";
        } else {
            value = "false";
        }

        return value;
    }

    /**
	 */
    @Override
    public String getStringValue(final String displayValue, final Locale locale) {
        return displayValue;
    }

    /**
	 */
    @Override
    public boolean isValid(final FormElement ui, final Map formContext) {
        return true;
    }

    /**
	 */
    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {
        return true;
    }

    /**
	 */
    @Override
    public boolean isValidValue(final String value, final ValidationError validationError, final Locale locale) {
        return true;
    }

    /**
	 */
    @Override
    public void updateFormElementFromUser(final FormElement ui, final User user) {
        ((CheckBoxElement) ui).setName(getInternalValue(user));
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

}
