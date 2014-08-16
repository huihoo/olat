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
package org.olat.lms.notifications;

import java.util.Date;

/**
 * Used in EBL method.
 * 
 * <P>
 * Initial Date: 16.09.2011 <br>
 * 
 * @author lavinia
 */
public class SubscriptionParameter {

    private final String urlToSend;
    private final String iconCssClass;
    private final String filePath;
    private final String fullUserName;
    private final Date modDate;

    public SubscriptionParameter(String urlToSend, Date modDate, String iconCssClass, String filePath, String fullUserName) {

        this.urlToSend = urlToSend;
        this.modDate = modDate;
        this.iconCssClass = iconCssClass;
        this.filePath = filePath;
        this.fullUserName = fullUserName;
    }

    public String getUrlToSend() {
        return urlToSend;
    }

    public String getIconCssClass() {
        return iconCssClass;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFullUserName() {
        return fullUserName;
    }

    public Date getModDate() {
        return modDate;
    }

}
