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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.user.notification;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.lms.notifications.SubscriptionInfo;
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
 * This is an implementation of the NotificationsHandler for newly created users.
 * <P>
 * Initial Date: 18 august 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Component
public class NewUsersNotificationHandler implements NotificationsHandler {
    private static final Logger log = LoggerHelper.getLogger();

    private List<Identity> identities;
    @Autowired
    NotificationService notificationService;

    /**
     * [spring]
     */
    private NewUsersNotificationHandler() {
        //
    }

    @Override
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        final Publisher p = subscriber.getPublisher();
        final Date latestNews = p.getLatestNewsDate();

        SubscriptionInfo si;
        Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
        // there could be news for me, investigate deeper
        try {
            if (notificationService.isPublisherValid(p) && compareDate.before(latestNews)) {
                identities = UsersSubscriptionManager.getInstance().getNewIdentityCreated(compareDate);
                if (identities.isEmpty()) {
                    si = notificationService.getNoSubscriptionInfo();
                } else {
                    translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
                    si = new SubscriptionInfo(new TitleItem(getItemTitle(translator), CSSHelper.CSS_CLASS_GROUP), null);
                    SubscriptionListItem subListItem;
                    for (final Identity newUser : identities) {
                        final String desc = translator.translate("notifications.entry", new String[] { NotificationHelper.getFormatedName(newUser) });
                        final String businessPath = "[Identity:" + newUser.getKey() + "]";
                        final String urlToSend = NotificationHelper.getURLFromBusinessPathString(p, businessPath);
                        final Date modDate = newUser.getCreationDate();
                        subListItem = new SubscriptionListItem(desc, urlToSend, modDate, CSSHelper.CSS_CLASS_USER);
                        si.addSubscriptionListItem(subListItem);
                    }
                }
            } else {
                si = notificationService.getNoSubscriptionInfo();
            }
        } catch (final Exception e) {
            log.error("Error creating new identity's notifications for subscriber: " + subscriber.getKey(), e);
            si = notificationService.getNoSubscriptionInfo();
        }
        return si;
    }

    private String getItemTitle(final Translator translator) {
        final String numOfNewUsers = Integer.toString(identities.size());
        if (identities.size() > 1) {
            return translator.translate("notifications.title", new String[] { numOfNewUsers });
        }
        return translator.translate("notifications.titleOne");
    }

    @Override
    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        final Translator translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
        return translator.translate("notifications.table.title");
    }

    @Override
    public String getType() {
        return "User";
    }
}
