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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.infomessage.notification;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.infomessage.InfoMessage;
import org.olat.data.infomessage.InfoMessageDao;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.notifications.SubscriptionListItem;
import org.olat.presentation.notifications.TitleItem;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * <P>
 * Initial Date: 27 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Component
public class InfoMessageNotificationHandler implements NotificationsHandler {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CSS_CLASS_ICON = "o_infomsg_icon";
    @Autowired
    private InfoMessageDao infoMessageManager;
    @Autowired
    private NotificationService notificationService;

    /**
     * [spring]
     */
    private InfoMessageNotificationHandler() {
        //
    }

    @Override
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        SubscriptionInfo si = null;
        final Publisher p = subscriber.getPublisher();
        final Date latestNews = p.getLatestNewsDate();

        // do not try to create a subscription info if state is deleted - results in
        // exceptions, course
        // can't be loaded when already deleted
        if (notificationService.isPublisherValid(p) && compareDate.before(latestNews)) {

            try {
                final Long resId = subscriber.getPublisher().getResId();
                final String resName = subscriber.getPublisher().getResName();
                final String resSubPath = subscriber.getPublisher().getSubidentifier();
                final String businessPath = subscriber.getPublisher().getBusinessPath();
                final String title = RepositoryServiceImpl.getInstance().lookupDisplayNameByOLATResourceableId(resId);
                si = new SubscriptionInfo(new TitleItem(title, CSS_CLASS_ICON), null);

                final OLATResourceable ores = new OLATResourceable() {
                    @Override
                    public String getResourceableTypeName() {
                        return resName;
                    }

                    @Override
                    public Long getResourceableId() {
                        return resId;
                    }
                };

                final List<InfoMessage> infos = infoMessageManager.loadInfoMessageByResource(ores, resSubPath, businessPath, compareDate, null, 0, 0);
                for (final InfoMessage info : infos) {
                    final String desc = info.getTitle();
                    final String tooltip = info.getMessage();
                    final String infoBusinessPath = info.getBusinessPath() + "[InfoMessage:" + info.getKey() + "]";
                    final String urlToSend = NotificationHelper.getURLFromBusinessPathString(p, infoBusinessPath);
                    final Date dateInfo = info.getModificationDate() == null ? info.getCreationDate() : info.getModificationDate();
                    final SubscriptionListItem subListItem = new SubscriptionListItem(desc, tooltip, urlToSend, dateInfo, CSS_CLASS_ICON);
                    si.addSubscriptionListItem(subListItem);
                }
            } catch (final Exception e) {
                log.error("Unexpected exception", e);
                si = notificationService.getNoSubscriptionInfo();
            }
        } else {
            si = notificationService.getNoSubscriptionInfo();
        }
        return si;
    }

    @Override
    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
        return translator.translate("notification.title");
    }

    @Override
    public String getType() {
        return "InfoMessage";
    }
}
