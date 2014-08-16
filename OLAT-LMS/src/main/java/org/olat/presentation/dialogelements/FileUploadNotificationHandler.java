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

package org.olat.presentation.dialogelements;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.dialogelements.DialogElement;
import org.olat.lms.dialogelements.DialogElementsPropertyManager;
import org.olat.lms.dialogelements.DialogPropertyElements;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsEBL;
import org.olat.lms.notifications.NotificationsHandler;
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
 * Description:<br>
 * Notification handler for course node dialog. Subscribers get informed about new uploaded file in the dialog table.
 * <P>
 * Initial Date: 23.11.2005 <br>
 * 
 * @author guido
 */
@Component
public class FileUploadNotificationHandler implements NotificationsHandler {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String CSSS_CLASS_UPLOAD_ICON = "o_dialog_icon";
    @Autowired
    NotificationService notificationService;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    NotificationsEBL notificationsEBL;

    private FileUploadNotificationHandler() {
        //
    }

    /**
	 */
    @Override
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        final Publisher p = subscriber.getPublisher();
        final Date latestNews = p.getLatestNewsDate();

        SubscriptionInfo si;
        // there could be news for me, investigate deeper
        try {
            if (notificationService.isPublisherValid(p) && compareDate.before(latestNews)) {
                final String displayname = RepositoryServiceImpl.getInstance().lookupDisplayNameByOLATResourceableId(p.getResId());
                if (displayname == null) {
                    if (!notificationsEBL.checkPublisher(subscriber.getPublisher())) {
                        return notificationService.getNoSubscriptionInfo();
                    }
                }
                final DialogElementsPropertyManager mgr = DialogElementsPropertyManager.getInstance();
                final DialogPropertyElements elements = mgr.findDialogElements(p.getResId(), p.getSubidentifier());
                final List<DialogElement> dialogElements = elements.getDialogPropertyElements();
                final Translator translator = PackageUtil.createPackageTranslator(FileUploadNotificationHandler.class, locale);
                si = new SubscriptionInfo(new TitleItem(translator.translate("notifications.header", new String[] { displayname }), CSSS_CLASS_UPLOAD_ICON), null);

                SubscriptionListItem subListItem;
                for (final DialogElement element : dialogElements) {
                    // do only show entries newer then the ones already seen
                    if (element.getDate().after(compareDate)) {
                        final String filename = element.getFilename();
                        final String creator = element.getAuthor();
                        final Identity ident = baseSecurity.findIdentityByName(creator);
                        final Date modDate = element.getDate();

                        final String desc = translator.translate("notifications.entry", new String[] { filename, NotificationHelper.getFormatedName(ident) });
                        final String urlToSend = NotificationHelper.getURLFromBusinessPathString(p, p.getBusinessPath());
                        final String cssClass = CSSHelper.createFiletypeIconCssClassFor(filename);

                        subListItem = new SubscriptionListItem(desc, urlToSend, modDate, cssClass);
                        si.addSubscriptionListItem(subListItem);
                    }
                }
            } else {
                si = notificationService.getNoSubscriptionInfo();
            }
        } catch (final Exception e) {
            log.error("Error creating file upload's notifications for subscriber: " + subscriber.getKey(), e);
            si = notificationService.getNoSubscriptionInfo();
        }
        return si;
    }

    @Override
    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        try {
            final Translator translator = PackageUtil.createPackageTranslator(FileUploadNotificationHandler.class, locale);
            final String displayname = RepositoryServiceImpl.getInstance().lookupDisplayNameByOLATResourceableId(subscriber.getPublisher().getResId());
            if (displayname == null) {
                notificationsEBL.checkPublisher(subscriber.getPublisher());
            }
            return translator.translate("notifications.header", new String[] { displayname });
        } catch (final Exception e) {
            log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
            notificationsEBL.checkPublisher(subscriber.getPublisher());
            return "-";
        }
    }

    @Override
    public String getType() {
        return "DialogElement";
    }
}
