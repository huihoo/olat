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

import org.apache.log4j.Logger;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.system.commons.Settings;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 20.03.2012 <br>
 * 
 * @author cg
 */
@Component
public class UriBuilder {

    @Autowired
    NotificationTypeHandlerSelector typeHandlerSelector;

    private static final Logger log = LoggerHelper.getLogger();

    String serverContextPathURI;

    private final String NOTIFICATION_SETTINGS_TAB_ID = "1";
    private final String NOTIFICATION_NEWS_TAB_ID = "0";

    public UriBuilder() {
    }

    public String getURIToContext(Publisher publisher) {
        return getURI(getBusinessPathToContext(publisher));
    }

    public String getURIToEventSource(Publisher publisher) {
        return getURI(getBusinessPathToSource(publisher));
    }

    public String getURIToSourceEntry(Publisher publisher, String sourceEntryId) {
        return getURIToEventSource(publisher) + typeHandlerSelector.getTypeHandler(publisher.getSourceType()).getSourceEntryPath(sourceEntryId);
    }

    /**
     * The implementation uses the legacy jump-in-links semantics.
     * 
     * @param publisher
     */
    private String getBusinessPathToContext(Publisher publisher) {
        // TODO: OLAT-1011: Current we have only one ContextType, extend this concept like SourceType when we will have more ContextTypes
        String businessPath = "";
        if (ContextType.COURSE.equals(publisher.getContextType())) {
            businessPath = "[RepositoryEntry:" + publisher.getContextId() + "]";
        } else {
            // TODO
            log.error("Could not build BusinessPathToContext, unkown contextType=" + publisher.getContextType());
            businessPath = "[UNKOWN]";
        }
        return businessPath;
    }

    private String getBusinessPathToSource(Publisher publisher) {
        return typeHandlerSelector.getTypeHandler(publisher.getSourceType()).getBusinessPathToSource(publisher);
    }

    private String getURI(String businessPath) {
        // TODO: implement this
        BusinessControlFactory bCF = BusinessControlFactory.getInstance();
        List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
        String busPath = NotificationHelper.getBusPathStringAsURIFromCEList(ceList);
        return getServerContextPathURI() + "/url/" + busPath;
    }

    /**
     * return the one set (e.g. for tests) if not null, or a default one.
     */
    private String getServerContextPathURI() {
        if (serverContextPathURI != null) {
            return serverContextPathURI;
        }
        return Settings.getServerContextPathURI();
    }

    public void setServerContextPathURI(String serverContextPathURI) {
        this.serverContextPathURI = serverContextPathURI;
    }

    public String getNotificationTabContext() {
        return "adminnotifications";
    }

    // due to existing OLAT implementation this must be simple so built to get correct link to notification settings
    public String getUriToNotificationSettings() {
        return getServerContextPathURI() + "/url/" + getNotificationTabContext() + "/" + NOTIFICATION_SETTINGS_TAB_ID + "/" + getNotificationTabContext() + "."
                + getNotificationTabContext() + "/" + NOTIFICATION_SETTINGS_TAB_ID + "/";
    }

    // due to existing OLAT implementation this must be simple so built to get correct link to notification news
    public String getUriToNotificationNews() {
        return getServerContextPathURI() + "/url/" + getNotificationTabContext() + "/" + NOTIFICATION_NEWS_TAB_ID + "/" + getNotificationTabContext() + "."
                + getNotificationTabContext() + "/" + NOTIFICATION_NEWS_TAB_ID + "/";
    }

}
