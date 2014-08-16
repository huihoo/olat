/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.orgrmform
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
 * Copyright (c) since 2004 at frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.lms.notifications;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.data.user.User;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Helper for some tasks with notifications
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */

public class NotificationHelper {

    private static final Logger log = LoggerHelper.getLogger();

    public static Map<Subscriber, SubscriptionInfo> getSubscriptionMap(Identity identity, Locale locale, boolean showWithNewsOnly, Date compareDate) {
        NotificationService man = getNotificationService();
        List<Subscriber> subs = man.getSubscribers(identity);
        Map<Subscriber, SubscriptionInfo> subToSubInfo = new HashMap<Subscriber, SubscriptionInfo>();

        // calc subscriptioninfo for all subscriptions and, if only those with news
        // are to be shown, remove the other ones
        for (Iterator<Subscriber> it_subs = subs.iterator(); it_subs.hasNext();) {
            Subscriber subscriber = it_subs.next();
            Publisher pub = subscriber.getPublisher();
            SubscriptionInfo subsInfo;
            if (man.isPublisherValid(pub)) {
                NotificationsHandler notifHandler = man.getNotificationsHandler(pub);
                if (notifHandler != null) {
                    subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, compareDate);
                } else {
                    // OLAT-5647
                    log.error("getSubscriptionMap: No notificationhandler for valid publisher: " + pub + ", resname: " + pub.getResName() + ", businesspath: "
                            + pub.getBusinessPath() + ", subscriber: " + subscriber);
                    subsInfo = man.getNoSubscriptionInfo();
                }
            } else {
                subsInfo = man.getNoSubscriptionInfo();
            }
            if (subsInfo.hasNews() || !showWithNewsOnly) {
                subToSubInfo.put(subscriber, subsInfo);
            }
        }
        return subToSubInfo;
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean("notificationService");

    }

    public static String getURLFromBusinessPathString(Publisher p, String bPathString) {
        if (!StringHelper.containsNonWhitespace(bPathString)) {
            log.error("Publisher without businesspath: " + p.getKey() + " resName:" + p.getResName() + " subidentifier:" + p.getSubidentifier() + " data:" + p.getData());
            return null;// TODO remove after the upgrade
        }

        try {
            BusinessControlFactory bCF = BusinessControlFactory.getInstance();
            List<ContextEntry> ceList = bCF.createCEListFromString(bPathString);
            String busPath = getBusPathStringAsURIFromCEList(ceList);

            return Settings.getServerContextPathURI() + "/url/" + busPath;
        } catch (Exception e) {
            log.error("Error with publisher: " + p.getKey() + " resName:" + p.getResName() + " subidentifier:" + p.getSubidentifier() + " data:" + p.getData()
                    + " businessPath:" + p.getBusinessPath(), e);
            return null;
        }
    }

    public static String getBusPathStringAsURIFromCEList(List<ContextEntry> ceList) {
        if (ceList == null || ceList.isEmpty())
            return "";

        StringBuilder retVal = new StringBuilder();
        // see code in JumpInManager, cannot be used, as it needs BusinessControl-Elements, not the path
        for (ContextEntry contextEntry : ceList) {
            String ceStr = contextEntry != null ? contextEntry.toString() : "NULL_ENTRY";
            if (ceStr.startsWith("[path")) {
                // the %2F make a problem on browsers.
                // make the change only for path which is generally used
                // TODO: find a better method or a better separator as |
                ceStr = ceStr.replace("%2F", "~~");
            }
            ceStr = ceStr.replace(':', '/');
            ceStr = ceStr.replaceFirst("\\]", "/");
            ceStr = ceStr.replaceFirst("\\[", "");
            retVal.append(ceStr);
        }
        return retVal.substring(0, retVal.length() - 1);
    }

    /**
     * returns "firstname lastname" or a translated "user unknown" for a given identity
     * 
     * @param ident
     * @return
     */
    public static String getFormatedName(Identity ident) {
        Translator trans;
        User user = null;
        if (ident == null) {
            trans = PackageUtil.createPackageTranslator(NotificationHelper.class, I18nManager.getInstance().getLocaleOrDefault(null));
        } else {
            trans = PackageUtil.createPackageTranslator(NotificationHelper.class,
                    I18nManager.getInstance().getLocaleOrDefault(ident.getUser().getPreferences().getLanguage()));
            user = ident.getUser();
        }
        if (user == null)
            return trans.translate("user.unknown");
        return getUserService().getFirstAndLastname(user);
    }

    /**
     * @param mimeType
     * @param titleSb
     */
    public static void appendLineBreak(String mimeType, StringBuilder titleSb) {
        if (mimeType.equals(SubscriptionInfo.MIME_HTML)) {
            titleSb.append("<br/>");
        } else {
            titleSb.append("\n");
        }
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
