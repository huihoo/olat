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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.folder;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.filebrowser.FileInfo;
import org.olat.data.filebrowser.FolderManager;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.group.BusinessGroup;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.lms.notifications.NotificationsUpgradeHelper;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.presentation.notifications.SubscriptionListItem;
import org.olat.presentation.notifications.TitleItem;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description: <br>
 * create SubscriptionInfo for a folder.
 * <P>
 * Initial Date: 25.10.2004 <br>
 * 
 * @author Felix Jost
 */
@Component
public class FolderNotificationsHandler implements NotificationsHandler {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    FileMetadataInfoService metaInfoService;

    /**
	 * 
	 */
    private FolderNotificationsHandler() {
        //
    }

    /**
	 */
    @Override
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        final Publisher p = subscriber.getPublisher();
        final Date latestNews = p.getLatestNewsDate();

        final String businessPath = p.getBusinessPath() + "[path=";

        SubscriptionInfo si;
        // there could be news for me, investigate deeper
        try {
            if (notificationService.isPublisherValid(p) && compareDate.before(latestNews)) {
                final String folderRoot = p.getData();
                final List<FileInfo> fInfos = FolderManager.getFileInfos(folderRoot, compareDate);
                final Translator translator = PackageUtil.createPackageTranslator(FolderNotificationsHandler.class, locale);

                si = new SubscriptionInfo(getTitleItem(p, translator), null);
                SubscriptionListItem subListItem;
                for (final Iterator<FileInfo> it_infos = fInfos.iterator(); it_infos.hasNext();) {
                    final FileInfo fi = it_infos.next();
                    String title = fi.getRelPath();
                    final MetaInfo metaInfo = fi.getMetaInfo();
                    String iconCssClass = null;
                    if (metaInfo != null) {
                        if (metaInfo.getTitle() != null) {
                            title += " (" + metaInfo.getTitle() + ")";
                        }
                        iconCssClass = metaInfoService.getIconCssClass(metaInfo);
                    }
                    final Identity ident = fi.getAuthor();
                    final Date modDate = fi.getLastModified();

                    final String desc = translator.translate("notifications.entry", new String[] { title, NotificationHelper.getFormatedName(ident) });
                    String urlToSend = null;
                    if (p.getBusinessPath() != null) {
                        urlToSend = NotificationHelper.getURLFromBusinessPathString(p, businessPath + fi.getRelPath() + "]");
                    }
                    subListItem = new SubscriptionListItem(desc, urlToSend, modDate, iconCssClass);
                    si.addSubscriptionListItem(subListItem);
                }
            } else {
                si = notificationService.getNoSubscriptionInfo();
            }
        } catch (final Exception e) {
            log.error("Error creating folder's notifications for subscriber: " + subscriber.getKey(), e);
            checkPublisher(subscriber.getPublisher());
            si = notificationService.getNoSubscriptionInfo();
        }
        return si;
    }

    private void checkPublisher(final Publisher p) {
        try {
            if ("BusinessGroup".equals(p.getResName())) {
                final BusinessGroup bg = businessGroupService.loadBusinessGroup(p.getResId(), false);
                if (bg == null) {
                    log.info("deactivating publisher with key; " + p.getKey(), null);
                    notificationService.deactivate(p);
                }
            } else if ("CourseModule".equals(p.getResName())) {
                if (!NotificationsUpgradeHelper.isCourseRepositoryEntryFound(p)) {
                    log.info("deactivating publisher with key; " + p.getKey(), null);
                    notificationService.deactivate(p);
                }
            }
        } catch (final Exception e) {
            log.error("", e);
        }
    }

    private TitleItem getTitleItem(final Publisher p, final Translator translator) {
        String title;
        try {
            final String resName = p.getResName();
            if ("BusinessGroup".equals(resName)) {
                final BusinessGroup bg = businessGroupService.loadBusinessGroup(p.getResId(), false);
                title = translator.translate("notifications.header.group", new String[] { bg.getName() });
            } else if ("CourseModule".equals(resName)) {
                final String displayName = RepositoryServiceImpl.getInstance().lookupDisplayNameByOLATResourceableId(p.getResId());
                title = translator.translate("notifications.header.course", new String[] { displayName });
            } else {
                title = translator.translate("notifications.header");
            }
        } catch (final Exception e) {
            log.error("", e);
            checkPublisher(p);
            title = translator.translate("notifications.header");
        }
        return new TitleItem(title, CSSHelper.CSS_CLASS_FILETYPE_FOLDER);
    }

    @Override
    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        final Translator translator = PackageUtil.createPackageTranslator(FolderNotificationsHandler.class, locale);
        final TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
        return title.getInfoContent("text/plain");
    }

    @Override
    public String getType() {
        return "FolderModule";
    }
}
