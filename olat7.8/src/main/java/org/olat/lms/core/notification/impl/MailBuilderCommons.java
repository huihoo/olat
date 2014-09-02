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
package org.olat.lms.core.notification.impl;

import java.util.Locale;

import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This should contain all the commons for building mails.
 * 
 * Initial Date: 28.09.2012 <br>
 * 
 * @author lavinia
 */
@Component
public class MailBuilderCommons {

    @Value("${notification.mail.from.address}")
    protected String mailFromAddress;

    @Value("${olat.web.url}")
    protected String olatWebUrl;

    @Autowired
    protected UriBuilder uriBuilder;

    public UriBuilder getUriBuilder() {
        return uriBuilder;
    }

    public Translator getEmailTranslator(Class<?> theClass, Locale locale) {
        return PackageUtil.createPackageTranslator(theClass, locale);
    }

    /**
     * Returns the String representation of the olat.web.url property.
     */
    public String getOlatWebUrl() {
        return olatWebUrl;
    }

    /**
     * Returns the HTML Href of the OLAT URL.
     */
    public String getOlatUrlAsHtmlHref() {
        return Formatter.getHtmlHref(olatWebUrl, "OLAT");
    }

    /**
     * This is the default email from which all notifications/confirmations are sent. (e.g. noreply@olat.uzh.ch)
     */
    public String getSystemMailFromAddress() {
        return mailFromAddress;
    }

}
