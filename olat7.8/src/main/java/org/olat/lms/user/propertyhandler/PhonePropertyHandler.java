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
import java.util.regex.Pattern;

import org.olat.data.user.User;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.formelements.FormElement;
import org.olat.presentation.framework.core.formelements.TextElement;
import org.olat.system.commons.StringHelper;

/**
 * <h3>Description:</h3> The phne property provides a user property that contains a valid phone number.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class PhonePropertyHandler extends Generic127CharTextPropertyHandler {

    // Regexp to define valid phone numbers
    private static final Pattern VALID_PHONE_PATTERN_IP = Pattern.compile("[0-9/\\-+' ]+");

    /**
     * spring
     */
    private PhonePropertyHandler() {
        // TODO Auto-generated constructor stub
    }

    /**
	 */
    @Override
    public FormElement getFormElement(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser) {
        final TextElement ui = (TextElement) super.getFormElement(locale, user, usageIdentifyer, isAdministrativeUser);
        ui.setExample("+41 12 345 67 89");
        return ui;
    }

    /*
     * (non-Javadoc) org.olat.presentation.framework.components.form.flexible.FormItemContainer)
     */
    @Override
    public FormItem addFormItem(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser,
            final FormItemContainer formItemContainer) {
        final org.olat.presentation.framework.core.components.form.flexible.elements.TextElement textElement = (org.olat.presentation.framework.core.components.form.flexible.elements.TextElement) super
                .addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
        textElement.setExampleKey("form.example.phone", null);
        return textElement;
    }

    /**
	 */
    @Override
    public String getUserPropertyAsHTML(final User user, final Locale locale) {
        String phonenr = getUserProperty(user, locale);
        if (StringHelper.containsNonWhitespace(phonenr)) {
            phonenr = StringHelper.escapeHtml(phonenr);
            final StringBuffer sb = new StringBuffer();
            sb.append("<a href=\"callto:");
            sb.append(phonenr);
            sb.append("\" class=\"b_link_call\">");
            sb.append(phonenr);
            sb.append("</a>");
            return sb.toString();
        }
        return null;
    }

    /**
	 */
    @Override
    public boolean isValid(final FormElement ui, final Map formContext) {
        // check parent rules first: check if mandatory and empty
        if (!super.isValid(ui, formContext)) {
            return false;
        }

        final TextElement uiPhone = (TextElement) ui;
        final String value = uiPhone.getValue();
        if (StringHelper.containsNonWhitespace(value)) {
            // check phone address syntax
            if (!VALID_PHONE_PATTERN_IP.matcher(value).matches()) {
                ui.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
                return false;
            }
        }
        // everthing ok
        return true;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {
        // check parent rules first: check if mandatory and empty
        if (!super.isValid(formItem, formContext)) {
            return false;
        }

        final org.olat.presentation.framework.core.components.form.flexible.elements.TextElement textElement = (org.olat.presentation.framework.core.components.form.flexible.elements.TextElement) formItem;
        final String value = textElement.getValue();

        if (StringHelper.containsNonWhitespace(value)) {
            // check phone address syntax
            if (!VALID_PHONE_PATTERN_IP.matcher(value).matches()) {
                formItem.setErrorKey(i18nFormElementLabelKey() + ".error.valid", null);
                return false;
            }
        }
        // everthing ok
        return true;
    }

    /*
     * (non-Javadoc) java.util.Locale)
     */
    @Override
    public boolean isValidValue(final String value, final ValidationError validationError, final Locale locale) {
        if (!super.isValidValue(value, validationError, locale)) {
            return false;
        }

        if (StringHelper.containsNonWhitespace(value)) {
            // check phone address syntax
            if (!VALID_PHONE_PATTERN_IP.matcher(value).matches()) {
                validationError.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
                return false;
            }
        }
        return true;
    }

}
