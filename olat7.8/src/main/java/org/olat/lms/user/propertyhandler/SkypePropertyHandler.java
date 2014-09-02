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

import org.olat.data.user.User;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.formelements.FormElement;
import org.olat.presentation.framework.core.formelements.TextElement;
import org.olat.system.commons.StringHelper;

/**
 * <h3>Description:</h3> The skype field provides a user property that contains a valid Skype ID.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class SkypePropertyHandler extends Generic127CharTextPropertyHandler {

    /**
	 */

    protected SkypePropertyHandler() {
    }

    @Override
    public String getUserPropertyAsHTML(final User user, final Locale locale) {
        String skypeid = getUserProperty(user, locale);
        if (StringHelper.containsNonWhitespace(skypeid)) {
            skypeid = StringHelper.escapeHtml(skypeid);
            final StringBuffer sb = new StringBuffer();
            sb.append("<script type=\"text/javascript\" src=\"http://download.skype.com/share/skypebuttons/js/skypeCheck.js\"></script>");
            sb.append("<img src=\"http://mystatus.skype.com/smallicon/");
            sb.append(skypeid);
            sb.append("\" style=\"border: none; position:relative; top:1px; margin-right:2px;\" width=\"10\" height=\"10\" alt=\"My status\" />");
            sb.append("<a href=\"skype:");
            sb.append(skypeid);
            sb.append("?call\">");
            sb.append(skypeid);
            sb.append("</a>");
            return sb.toString();
        }
        return null;
    }

    /**
	 */
    @Override
    public FormElement getFormElement(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser) {
        final TextElement ui = (TextElement) super.getFormElement(locale, user, usageIdentifyer, isAdministrativeUser);
        // skype names are max 32 chars long
        ui.setMaxLength(32);
        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            ui.setExample("myskypename");
        }
        return ui;
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
            // skype names are max 32 chars long
            if (value.length() > 32) {
                validationError.setErrorKey("general.error.max.32");
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc) org.olat.presentation.framework.components.form.flexible.FormItemContainer)
     */
    @Override
    public FormItem addFormItem(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser,
            final FormItemContainer formItemContainer) {
        final org.olat.presentation.framework.core.components.form.flexible.elements.TextElement textElement = (org.olat.presentation.framework.core.components.form.flexible.elements.TextElement) super
                .addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
        textElement.setMaxLength(32);
        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            textElement.setExampleKey("form.example.skypename", null);
        }
        return textElement;
    }

}
