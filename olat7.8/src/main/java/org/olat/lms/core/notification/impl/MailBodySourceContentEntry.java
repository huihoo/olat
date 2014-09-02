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

import org.olat.presentation.framework.core.translator.Translator;

/**
 * For the notification email.
 * 
 * Initial Date: 10.01.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class MailBodySourceContentEntry {

    private final String firstPart;
    private final String secondPart;
    private final String entryTitle;
    private final String urlLink;

    public MailBodySourceContentEntry(String entryTitle, String contentLink, String[] firstPartArgs, String[] secondPartArgs, Translator translator) {
        firstPart = translator.translate("mail.body.source.entry.firstpart", firstPartArgs);
        secondPart = translator.translate("mail.body.source.entry.secondpart", secondPartArgs);
        this.entryTitle = entryTitle;
        this.urlLink = contentLink;
    }

    public String getFirstPart() {
        return firstPart;
    }

    public String getSecondPart() {
        return secondPart;
    }

    public String getEntryTitle() {
        return entryTitle;
    }

    public String getUrlLink() {
        return urlLink;
    }

}
