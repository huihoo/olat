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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.lms.user.propertyhandler;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.olat.connectors.httpclient.HttpClientFactory;
import org.olat.data.user.User;
import org.olat.presentation.framework.core.components.form.ValidationError;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Implements a user property handler for XING screen names.
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class XingPropertyHandler extends Generic127CharTextPropertyHandler {

    private static final Logger log = LoggerHelper.getLogger();
    public static final int XING_NAME_MAX_LENGTH = 320;
    public static final String XING_NAME_VALIDATION_URL = "http://www.xing.com/profile/";

    /**
     * java.util.Locale)
     */

    protected XingPropertyHandler() {
    }

    @Override
    public boolean isValidValue(final String value, final ValidationError validationError, final Locale locale) {
        if (!super.isValidValue(value, validationError, locale)) {
            return false;
        }
        if (StringHelper.containsNonWhitespace(value)) {
            return value.length() <= XING_NAME_MAX_LENGTH;
        }
        return true;
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItemContainer)
     */
    @Override
    public FormItem addFormItem(final Locale locale, final User user, final String usageIdentifyer, final boolean isAdministrativeUser,
            final FormItemContainer formItemContainer) {
        final TextElement textElement = (TextElement) super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
        textElement.setMaxLength(XING_NAME_MAX_LENGTH);

        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            textElement.setExampleKey("form.example.xingname", null);
        }
        return textElement;
    }

    /**
	 */
    @Override
    public String getUserPropertyAsHTML(final User user, final Locale locale) {
        final String xingname = getUserProperty(user, locale);
        if (StringHelper.containsNonWhitespace(xingname)) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<a href=\"" + XING_NAME_VALIDATION_URL + xingname + "\" target=\"_blank\">" + xingname + "</a>");
            return stringBuffer.toString();
        } else {
            return null;
        }
    }

    /**
	 */
    @SuppressWarnings({ "unused", "unchecked" })
    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {
        boolean result;
        final TextElement textElement = (TextElement) formItem;

        if (StringHelper.containsNonWhitespace(textElement.getValue())) {
            final HttpClient httpClient = HttpClientFactory.getHttpClientInstance();
            final HttpClientParams httpClientParams = httpClient.getParams();
            httpClientParams.setConnectionManagerTimeout(2500);
            httpClient.setParams(httpClientParams);
            try {
                // Could throw IllegalArgumentException if argument is not a valid url
                // (e.g. contains whitespaces)
                final HttpMethod httpMethod = new GetMethod(XING_NAME_VALIDATION_URL + textElement.getValue());
                // Don't allow redirects since otherwise, we won't be able to get the correct status
                httpMethod.setFollowRedirects(false);
                // Get the user profile page
                httpClient.executeMethod(httpMethod);
                final int httpStatusCode = httpMethod.getStatusCode();
                // Looking at the HTTP status code tells us whether a user with the given Xing name exists.
                if (httpStatusCode == HttpStatus.SC_OK) {
                    // If the user exists, we get a 200...
                    result = true;
                } else if (httpStatusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
                    // ... and if he doesn't exist, we get a 301.
                    textElement.setErrorKey("form.name.xing.error", null);
                    result = false;
                } else {
                    // In case of any exception, assume that the given MSN name is valid (The opposite would block easily upon network problems), and inform the user
                    // about this.
                    textElement.setExampleKey("form.example.xingname.notvalidated", null);
                    log.warn("Xing name validation: Expected HTTP status 200 or 301, but got " + httpStatusCode);
                    result = true;
                }
            } catch (final IllegalArgumentException e) {
                // The xing name is not url compatible (e.g. contains whitespaces)
                textElement.setErrorKey("form.xingname.notvalid", null);
                result = false;
            } catch (final Exception e) {
                // In case of any exception, assume that the given MSN name is valid (The opposite would block easily upon network problems), and inform the user about
                // this.
                textElement.setExampleKey("form.example.xingname.notvalidated", null);
                log.warn("Xing name validation: Exception: " + e.getMessage());
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }
}
