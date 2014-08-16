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
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Temporary class. EBL stands for Extracted Business Logic <br/>
 * Contains business methods extracted from controllers. <br/>
 * Do they belong to the NotificationService?
 * 
 * <P>
 * Initial Date: 26.08.2011 <br>
 * 
 * @author lavinia
 */
public class NotificationServiceEBL {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * BUSINESS LOGIC: get only the subscribers for valid publishers. Should this logic be moved into the service?
     * 
     * @param identity
     * @return
     */
    public List<Subscriber> getSubscribers(Identity identity) {
        // Load subscriptions from DB. Don't use the ureq.getIdentity() but the
        // subscriberIdentity instead to make this controller also be usable in the
        // admin environment (admins might change notifications for a user)
        final NotificationService notificationService = getNotificationService();
        final List<Subscriber> subs = notificationService.getSubscribers(identity);
        for (final Iterator<Subscriber> subIt = subs.iterator(); subIt.hasNext();) {
            final Subscriber sub = subIt.next();
            if (!notificationService.isPublisherValid(sub.getPublisher())) {
                subIt.remove();
            }
        }
        return subs;
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    public List<Subscriber> getSubscribersWithNews(Identity identity, Locale locale, Date newsStartDate) {
        final NotificationService notificationService = getNotificationService();
        List<Subscriber> subscriberList = notificationService.getValidSubscribers(identity);
        // calc subscriptioninfo for all subscriptions and, if only those with news are to be shown, remove the other ones
        for (final Iterator<Subscriber> it_subs = subscriberList.iterator(); it_subs.hasNext();) {
            final Subscriber subscriber = it_subs.next();
            final Publisher pub = subscriber.getPublisher();
            try {
                final NotificationsHandler notifHandler = notificationService.getNotificationsHandler(pub);
                if (notifHandler == null) {
                    it_subs.remove();
                } else {
                    final SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, newsStartDate);
                    if (!subsInfo.hasNews()) {
                        it_subs.remove();
                    }
                }
            } catch (final Exception e) {
                log.error("Cannot load publisher:" + pub, e);
            }
        }
        return subscriberList;
    }

    public Map<Subscriber, SubscriptionInfo> getSubscriptionMap(Identity identity, Locale locale, Date compareDate) {
        return NotificationHelper.getSubscriptionMap(identity, locale, true, compareDate);
    }

    public boolean sendMailToUserAndUpdateSubscriber(Identity subscriberIdentity, Locale locale, Date compareDate, String title) {
        Map<Subscriber, SubscriptionInfo> subsInfoMap = NotificationHelper.getSubscriptionMap(subscriberIdentity, locale, true, compareDate);
        // send email to user with the currently visible date
        final NotificationService notificationService = getNotificationService();
        final List<SubscriptionItem> infoList = new ArrayList<SubscriptionItem>();
        final List<Subscriber> subsList = new ArrayList<Subscriber>();
        for (final Subscriber subscriber : subsInfoMap.keySet()) {
            subsList.add(subscriber);
            final SubscriptionItem item = notificationService.createSubscriptionItem(subscriber, locale, SubscriptionInfo.MIME_PLAIN, SubscriptionInfo.MIME_PLAIN,
                    compareDate);
            if (item != null) {
                infoList.add(item);
            }
        }
        return notificationService.sendMailToUserAndUpdateSubscriber(subscriberIdentity, infoList, title, subsList);
    }

    public Date getNewsStartDate(Identity identity) {
        return getNotificationService().getCompareDateFromInterval(getNotificationService().getUserIntervalOrDefault(identity));
    }

}
