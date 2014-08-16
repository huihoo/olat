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
 * Initial Date: 11.01.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class MailBodyInfo {

    private final String firstPart;
    private final String secondPart;
    private final String thirdPart;
    private final String urlLink;
    private final String subscriptionLink;

    public MailBodyInfo(Translator translator, String olatWebUrl) {
        this.firstPart = translator.translate("mail.body.info.firstpart");
        this.secondPart = translator.translate("mail.body.info.secondpart");
        this.thirdPart = translator.translate("mail.body.info.thirdpart");
        this.urlLink = olatWebUrl;
        this.subscriptionLink = "test";
    }

    public String getFirstPart() {
        return firstPart;
    }

    public String getSecondPart() {
        return secondPart;
    }

    public String getThirdPart() {
        return thirdPart;
    }

    public String getUrlLink() {
        // Long id = NotificationController.notificationSettingsController.getInitialComponent().getDispatchID();
        // return id.toString();
        return urlLink;
    }

    public String getSubscriptionLink() {
        return subscriptionLink;
    }

}
