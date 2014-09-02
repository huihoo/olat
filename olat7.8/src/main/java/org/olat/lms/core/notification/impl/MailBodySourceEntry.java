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

import java.util.List;

import org.olat.presentation.framework.core.translator.Translator;

/**
 * For the notification email.
 * 
 * Initial Date: 10.01.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class MailBodySourceEntry {

    private final String header;
    private final String urlLink;
    private final String sourceTitle;
    private final List<MailBodySourceContentEntry> sourceContentEntries;
    private final String headerPrefix;

    public MailBodySourceEntry(String sourceTitle, String sourceEntryType, String urlLink, List<MailBodySourceContentEntry> sourceContentEntries, Translator translator) {
        this.sourceTitle = sourceTitle;
        header = translator.translate("mail.body.source.header." + sourceEntryType);
        headerPrefix = translator.translate("mail.body.source.header.prefix." + sourceEntryType);
        this.sourceContentEntries = sourceContentEntries;
        this.urlLink = urlLink;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getHeader() {
        return header;
    }

    public List<MailBodySourceContentEntry> getSourceContentEntries() {
        return sourceContentEntries;
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

}
