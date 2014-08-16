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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.filebrowser.FileInfo;
import org.olat.data.filebrowser.FolderManager;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for NotificationsEBL
 * 
 * <P>
 * Initial Date: 16.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class NotificationsEBL {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    FileMetadataInfoService fileMetadataInfoService;
    @Autowired
    NotificationService notificationService;

    /**
     * Extract username form file-path and return the firstname and lastname. *
     * 
     * @param filePath
     *            E.g. '/username/abgabe.txt'
     * @return 'firstname lastname'
     */
    private String getUserNameFromFilePath(final String filePath) {
        // remove first '/'
        try {
            final String path = filePath.substring(1);
            if (path.indexOf("/") != -1) {
                final String userName = path.substring(0, path.indexOf("/"));
                final Identity identity = baseSecurity.findIdentityByName(userName);
                final String fullName = NotificationHelper.getFormatedName(identity);
                return fullName;
            } else {
                return "";
            }
        } catch (final Exception e) {
            log.warn("Can not extract user from path=" + filePath, null);
            return "";
        }
    }

    public List<SubscriptionParameter> createSubscriptionTransferObjectForTask(final Subscriber subscriber, final Date compareDate) {
        List<SubscriptionParameter> subscriptionInfos = new ArrayList<SubscriptionParameter>();
        final Publisher p = subscriber.getPublisher();
        final String folderRoot = p.getData();
        final List<FileInfo> fInfos = FolderManager.getFileInfos(folderRoot, compareDate);
        for (final Iterator<FileInfo> it_infos = fInfos.iterator(); it_infos.hasNext();) {
            final FileInfo fi = it_infos.next();
            final MetaInfo metaInfo = fi.getMetaInfo();
            final String filePath = fi.getRelPath();

            final String fullUserName = getUserNameFromFilePath(filePath);

            final Date modDate = fi.getLastModified();

            final String urlToSend = NotificationHelper.getURLFromBusinessPathString(p, p.getBusinessPath());

            String iconCssClass = null;
            if (metaInfo != null) {
                iconCssClass = fileMetadataInfoService.getIconCssClass(metaInfo);
            }
            subscriptionInfos.add(new SubscriptionParameter(urlToSend, modDate, iconCssClass, filePath, fullUserName));
        }
        return subscriptionInfos;
    }

    /**
     * Check course Repository entry and deactivates publisher if NOK.
     * 
     * @param p
     * @return
     */
    public boolean checkPublisher(final Publisher p) {
        try {
            if ("CourseModule".equals(p.getResName()) && !NotificationsUpgradeHelper.isCourseRepositoryEntryFound(p)) {
                log.info("deactivating publisher with key; " + p.getKey(), null);
                notificationService.deactivate(p);
                return false;

            }
        } catch (final Exception e) {
            log.error("Could not check Publisher", e);
        }
        return true;
    }

}
