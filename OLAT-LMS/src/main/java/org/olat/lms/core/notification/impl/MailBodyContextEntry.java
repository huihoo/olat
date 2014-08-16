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
 * Initial Date: 10.01.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class MailBodyContextEntry {

    private final String header;
    private final String urlLink;
    private final String contextTitle;
    private final List<MailBodySourceEntry> sourceEntries;
    private final String headerPrefix;

    public MailBodyContextEntry(String contextTitle, String urlLink, List<MailBodySourceEntry> sourceEntries, Translator translator) {
        this.contextTitle = contextTitle;
        header = translator.translate("mail.body.context.header");
        headerPrefix = translator.translate("mail.body.context.header.prefix");
        this.urlLink = urlLink;
        this.sourceEntries = sourceEntries;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public List<MailBodySourceEntry> getSourceEntries() {
        return sourceEntries;
    }

    public String getHeader() {
        return header;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

}
