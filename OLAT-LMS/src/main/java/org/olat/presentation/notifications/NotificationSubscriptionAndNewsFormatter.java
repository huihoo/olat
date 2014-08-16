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
package org.olat.presentation.notifications;

import java.util.Date;
import java.util.Map;

import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.notifications.SubscriptionItem;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This class provides helper methods to render notification news details
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class NotificationSubscriptionAndNewsFormatter {
    private final Date compareDate;
    private final Translator translator;
    private final Map<Subscriber, SubscriptionInfo> subsInfoMap;

    NotificationSubscriptionAndNewsFormatter(final Date compareDate, final Translator translator, final Map<Subscriber, SubscriptionInfo> subsInfoMap) {
        this.compareDate = compareDate;
        this.translator = translator;
        this.subsInfoMap = subsInfoMap;
    }

    public String getType(final Subscriber sub) {
        final Publisher pub = sub.getPublisher();
        final String innerType = pub.getType();
        final String typeName = ControllerFactory.translateResourceableTypeName(innerType, translator.getLocale());
        return typeName;
    }

    public String getContainerType(final Subscriber sub) {
        final Publisher pub = sub.getPublisher();
        final String containerType = pub.getResName();
        final String containerTypeTrans = ControllerFactory.translateResourceableTypeName(containerType, translator.getLocale());
        return containerTypeTrans;
    }

    public boolean hasNews(final Subscriber sub) {
        return subsInfoMap.containsKey(sub);
    }

    public String getNewsAsHTML(final Subscriber sub) {
        return getNews(sub, SubscriptionInfo.MIME_HTML);
    }

    public String getNewsAsTxt(final Subscriber sub) {
        return getNews(sub, SubscriptionInfo.MIME_PLAIN);
    }

    private String getNews(final Subscriber sub, final String mimeType) {
        final SubscriptionInfo subsInfo = subsInfoMap.get(sub);
        if (subsInfo == null || !subsInfo.hasNews()) {
            return translator.translate("news.no.news");
        }
        return subsInfo.getSpecificInfo(mimeType, translator.getLocale());
    }

    public String getTitleAsHTML(final Subscriber sub) {
        return getTitle(sub, SubscriptionInfo.MIME_HTML);
    }

    public String getTitleAsTxt(final Subscriber sub) {
        return getTitle(sub, SubscriptionInfo.MIME_PLAIN);
    }

    private String getTitle(final Subscriber sub, final String mimeType) {
        final SubscriptionInfo subsInfo = subsInfoMap.get(sub);
        if (subsInfo == null) {
            return "";
        }
        return subsInfo.getTitle(mimeType);
    }

    public String getCustomUrl(final Subscriber sub) {
        final SubscriptionInfo subsInfo = subsInfoMap.get(sub);
        return subsInfo.getCustomUrl();
    }

    public SubscriptionItem getSubscriptionItem(final Subscriber sub) {
        final NotificationService notiMgr = getNotificationService();
        final SubscriptionItem subscrItem = notiMgr.createSubscriptionItem(sub, translator.getLocale(), SubscriptionInfo.MIME_HTML, SubscriptionInfo.MIME_HTML,
                compareDate);
        return subscrItem;
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

}
