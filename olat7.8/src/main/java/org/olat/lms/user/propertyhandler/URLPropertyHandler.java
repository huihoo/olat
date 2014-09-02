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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.user.User;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.formelements.FormElement;
import org.olat.presentation.framework.core.formelements.TextElement;
import org.olat.system.commons.StringHelper;

/**
 * <h3>Description:</h3> The url field provides a user property that contains a valid URL.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class URLPropertyHandler extends Generic127CharTextPropertyHandler {

    /**
	 */

    protected URLPropertyHandler() {
    }

    @Override
    public String getUserPropertyAsHTML(final User user, final Locale locale) {
        String href = getUserProperty(user, locale);
        if (StringHelper.containsNonWhitespace(href)) {
            href = StringHelper.escapeHtml(href);
            final StringBuffer sb = new StringBuffer();
            sb.append("<a href=\"");
            sb.append(href);
            sb.append("\" class=\"b_link_extern\" target=\"_blank\">");
            sb.append(href);
            sb.append("</a>");
            return FilterFactory.filterXSS(sb.toString());
        }
        return null;
    }

    /**
	 */
    @Override
    public FormElement getFormElement(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser) {
        final TextElement ui = (TextElement) super.getFormElement(locale, user, usageIdentifyer, isAdministrativeUser);
        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            ui.setExample("http://www.olat.org");
        }
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
        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            textElement.setExampleKey("form.example.url", null);
        }
        return textElement;
    }

    /**
	 */
    @Override
    public boolean isValid(final FormElement ui, final Map formContext) {
        // check parent rules first: check if mandatory and empty
        if (!super.isValid(ui, formContext)) {
            return false;
        }
        final TextElement uiURL = (TextElement) ui;
        final String value = uiURL.getValue();
        if (StringHelper.containsNonWhitespace(value)) {
            // check url address syntax
            try {
                new URL(value);
            } catch (final MalformedURLException e) {
                uiURL.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
                return false;
            }
        }
        // everthing ok
        return true;
    }

    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {
        // check parent rules first: check if mandatory and empty
        if (!super.isValid(formItem, formContext)) {
            return false;
        }
        final org.olat.presentation.framework.core.components.form.flexible.elements.TextElement uiEl = (org.olat.presentation.framework.core.components.form.flexible.elements.TextElement) formItem;
        final String value = uiEl.getValue();
        final ValidationError validationError = new ValidationError();
        final boolean valid = isValidValue(value, validationError, formItem.getTranslator().getLocale());
        if (!valid) {
            uiEl.setErrorKey(validationError.getErrorKey(), new String[] {});
        }
        return valid;
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
            // check url address syntax
            try {
                new URL(value);
            } catch (final MalformedURLException e) {
                validationError.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
                return false;
            }
        }
        // everthing ok
        return true;
    }

}
