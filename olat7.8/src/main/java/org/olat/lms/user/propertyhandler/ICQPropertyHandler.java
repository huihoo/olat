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
import org.apache.commons.httpclient.NameValuePair;
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
 * Implements a user property handler for ICQ screen names.
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class ICQPropertyHandler extends Generic127CharTextPropertyHandler {

    public static final int ICQ_NAME_MAX_LENGTH = 16;
    public static final String ICQ_INDICATOR_URL = "http://status.icq.com/online.gif";
    public static final String ICQ_NAME_VALIDATION_URL = "http://www.icq.com/people/about_me.php";
    public static final String ICQ_NAME_URL_PARAMETER = "uin";
    private static final Logger log = LoggerHelper.getLogger();

    /**
	 */

    protected ICQPropertyHandler() {
    }

    @Override
    public String getUserPropertyAsHTML(final User user, final Locale locale) {
        String icqname = getUserProperty(user, locale);
        if (StringHelper.containsNonWhitespace(icqname)) {
            icqname = StringHelper.escapeHtml(icqname);
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<a href=\"" + ICQ_NAME_VALIDATION_URL + "?" + ICQ_NAME_URL_PARAMETER + "=" + icqname + "\" target=\"_blank\">" + icqname + "</a>");
            stringBuffer.append("<img src=\"" + ICQ_INDICATOR_URL + "?icq=" + icqname + "&img=5\" style=\"width:10px; height:10px; margin-left:2px;\">");
            return stringBuffer.toString();
        } else {
            return null;
        }
    }

    /**
     * java.util.Locale)
     */
    @Override
    public boolean isValidValue(final String value, final ValidationError validationError, final Locale locale) {
        if (!super.isValidValue(value, validationError, locale)) {
            return false;
        }
        if (StringHelper.containsNonWhitespace(value)) {
            return value.length() <= ICQ_NAME_MAX_LENGTH;
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
        textElement.setMaxLength(ICQ_NAME_MAX_LENGTH);

        if (!getUserService().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
            textElement.setExampleKey("form.example.icqname", null);
        }
        return textElement;
    }

    /**
	 */
    @SuppressWarnings({ "unchecked", "unused" })
    @Override
    public boolean isValid(final FormItem formItem, final Map formContext) {
        boolean result;
        final TextElement textElement = (TextElement) formItem;

        if (StringHelper.containsNonWhitespace(textElement.getValue())) {

            // Use an HttpClient to fetch a profile information page from ICQ.
            final HttpClient httpClient = HttpClientFactory.getHttpClientInstance();
            final HttpClientParams httpClientParams = httpClient.getParams();
            httpClientParams.setConnectionManagerTimeout(2500);
            httpClient.setParams(httpClientParams);
            final HttpMethod httpMethod = new GetMethod(ICQ_NAME_VALIDATION_URL);
            final NameValuePair uinParam = new NameValuePair(ICQ_NAME_URL_PARAMETER, textElement.getValue());
            httpMethod.setQueryString(new NameValuePair[] { uinParam });
            // Don't allow redirects since otherwise, we won't be able to get the HTTP 302 further down.
            httpMethod.setFollowRedirects(false);
            try {
                // Get the user profile page
                httpClient.executeMethod(httpMethod);
                final int httpStatusCode = httpMethod.getStatusCode();
                // Looking at the HTTP status code tells us whether a user with the given ICQ name exists.
                if (httpStatusCode == HttpStatus.SC_OK) {
                    // ICQ tells us that a user name is valid if it sends an HTTP 200...
                    result = true;
                } else if (httpStatusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                    // ...and if it's invalid, it sends an HTTP 302.
                    textElement.setErrorKey("form.name.icq.error", null);
                    result = false;
                } else {
                    // For HTTP status codes other than 200 and 302 we will silently assume that the given ICQ name is valid, but inform the user about this.
                    textElement.setExampleKey("form.example.icqname.notvalidated", null);
                    log.warn("ICQ name validation: Expected HTTP status 200 or 301, but got " + httpStatusCode);
                    result = true;
                }
            } catch (final Exception e) {
                // In case of any exception, assume that the given ICQ name is valid (The opposite would block easily upon network problems), and inform the user about
                // this.
                textElement.setExampleKey("form.example.icqname.notvalidated", null);
                log.warn("ICQ name validation: Exception: " + e.getMessage());
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }
}
