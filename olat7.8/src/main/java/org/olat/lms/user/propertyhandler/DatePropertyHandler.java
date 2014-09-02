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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.formelements.DateElement;
import org.olat.presentation.framework.core.formelements.FormElement;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * <h3>Description:</h3> The DatePropertyHandler offers the functionality of a date. It can be used to store something like a birth date.
 * <p>
 * Initial Date: 26.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DatePropertyHandler extends AbstractUserPropertyHandler {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Format internal values as yyyyMMdd string e.g. "19751210". So it is possible to use the less, greater and equal operators.
     */
    private final DateFormat INTERNAL_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);

    /**
     * @Override
     */

    protected DatePropertyHandler() {
    }

    @Override
    public String getUserProperty(final User user, Locale locale) {
        final Date date = decode(getInternalValue(user));
        if (date == null) {
            return null;
        }
        if (locale == null) {
            locale = I18nModule.getDefaultLocale();
        }
        return Formatter.getInstance(locale).formatDate(date);
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
        final Date date = ((DateElement) ui).getDate();
        return encode(date);
    }

    /**
	 */
    @Override
    public String getStringValue(final FormItem formItem) {
        final Date date = ((DateChooser) formItem).getDate();
        return encode(date);
    }

    /**
	 */
    @Override
    public String getStringValue(final String displayValue, final Locale locale) {
        if (StringHelper.containsNonWhitespace(displayValue)) {
            final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            df.setLenient(false);
            try {
                final Date date = df.parse(displayValue.trim());
                return encode(date);
            } catch (final ParseException e) {
                // catch but do nothing, return null in the end
            }
        }
        return null;
    }

    /**
	 */
    @Override
    public FormElement getFormElement(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser) {
        // default is no element
        DateElement ui = null;
        ui = new DateElement(i18nFormElementLabelKey(), locale);
        updateFormElementFromUser(ui, user);
        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            ui.setExample(Formatter.getInstance(locale).formatDate(new Date()));
        } else {
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
        DateChooser dateElem = null;
        dateElem = FormUIFactory.getInstance().addDateChooser(getName(), i18nFormElementLabelKey(), getInternalValue(user), formItemContainer);
        dateElem.setItemValidatorProvider(this);
        if (getUserService().isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
            dateElem.setEnabled(false);
        }
        if (getUserService().isMandatoryUserProperty(usageIdentifyer, this)) {
            dateElem.setMandatory(true);
        }

        dateElem.setExampleKey("form.example.free", new String[] { Formatter.getInstance(locale).formatDate(new Date()) });
        return dateElem;
    }

    /**
	 */
    @Override
    public void updateFormElementFromUser(final FormElement ui, final User user) {
        final Date date = decode(getInternalValue(user));
        ((DateElement) ui).setDate(date);
    }

    /**
	 */
    @Override
    public boolean isValid(final FormElement ui, final Map formContext) {

        final DateElement uiDate = (DateElement) ui;

        if (uiDate.getValue().length() == 0) {
            if (!ui.isMandatory()) {
                return true;
            }
            ui.setErrorKey(i18nFormElementLabelKey() + ".error.empty");
            return false;
        }

        return uiDate.validDate(i18nFormElementLabelKey() + ".error");
    }

    /**
	 */
    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {

        final DateChooser dateElem = (DateChooser) formItem;

        if (dateElem.isEmpty()) {
            return !dateElem.isMandatory() || !dateElem.isEmpty("new.form.mandatory");
        }
        final List<ValidationStatus> validation = new ArrayList<ValidationStatus>();
        dateElem.validate(validation);
        if (validation.size() == 0) {
            return true;
        } else {
            // errorkey should be set by dateElem.validate formItem.setErrorKey(i18nFormElementLabelKey()+ ".error", null);
            return false;
        }
    }

    /**
     * Helper to encode the date as a String
     * 
     * @param date
     * @return
     */
    private String encode(final Date date) {
        if (date == null) {
            return null;
        }
        return INTERNAL_DATE_FORMATTER.format(date);
    }

    /**
     * Helper to decode a String value to a date
     * 
     * @param value
     * @return
     */
    private Date decode(final String value) {
        if (!StringHelper.containsNonWhitespace(value)) {
            return null;
        }
        try {
            return INTERNAL_DATE_FORMATTER.parse(value.trim());
        } catch (final ParseException e) {
            log.warn("Could not parse BirthDayField from database", e);
            return null;
        }
    }

    /**
	 */
    @Override
    public boolean isValidValue(final String value, final ValidationError validationError, final Locale locale) {
        if (StringHelper.containsNonWhitespace(value)) {
            final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            df.setLenient(false);
            try {
                df.parse(value.trim());
            } catch (final ParseException e) {
                validationError.setErrorKey(i18nFormElementLabelKey() + ".error");
                return false;
            }
            return true;
        }
        // null values are ok
        return true;
    }

}
