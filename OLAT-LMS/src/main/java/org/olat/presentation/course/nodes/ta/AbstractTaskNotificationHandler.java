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

package org.olat.presentation.course.nodes.ta;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsEBL;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.lms.notifications.PublisherData;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.notifications.SubscriptionParameter;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.notifications.ContextualSubscriptionController;
import org.olat.presentation.notifications.SubscriptionListItem;
import org.olat.presentation.notifications.TitleItem;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic abstract notification-handler for all task-notification-handler.
 * 
 * @author guretzki
 */
public abstract class AbstractTaskNotificationHandler implements NotificationsHandler {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private NotificationsEBL notificationsEBL;
    @Autowired
    private RepositoryService repositoryService;

    public AbstractTaskNotificationHandler() {
        super();
    }

    /**
	 */
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        final Publisher p = subscriber.getPublisher();
        final Date latestNews = p.getLatestNewsDate();
        SubscriptionInfo si;

        // there could be news for me, investigate deeper
        try {
            if (getNotificationService().isPublisherValid(p) && compareDate.before(latestNews)) {
                final String folderRoot = p.getData();
                if (log.isDebugEnabled()) {
                    log.debug("folderRoot=" + folderRoot);
                }

                final String displayName = repositoryService.lookupDisplayNameByOLATResourceableId(p.getResId());
                if (displayName == null) {
                    if (!notificationsEBL.checkPublisher(p)) {
                        return getNotificationService().getNoSubscriptionInfo();
                    }
                }

                final Translator translator = PackageUtil.createPackageTranslator(AbstractTaskNotificationHandler.class, locale);
                si = new SubscriptionInfo(new TitleItem(translator.translate(getNotificationHeaderKey(), new String[] { displayName }), getCssClassIcon()), null);

                List<SubscriptionParameter> transferObjects = notificationsEBL.createSubscriptionTransferObjectForTask(subscriber, compareDate);
                for (SubscriptionParameter transferObject : transferObjects) {
                    final String desc = translator.translate(getNotificationEntryKey(), new String[] { transferObject.getFilePath(), transferObject.getFullUserName() });
                    SubscriptionListItem subListItem = new SubscriptionListItem(desc, transferObject.getUrlToSend(), transferObject.getModDate(),
                            transferObject.getIconCssClass());
                    si.addSubscriptionListItem(subListItem);
                }

            } else {
                si = getNotificationService().getNoSubscriptionInfo();
            }
        } catch (final Exception e) {
            log.error("Error creating task notifications for subscriber: " + subscriber.getKey(), e);
            notificationsEBL.checkPublisher(p);
            si = getNotificationService().getNoSubscriptionInfo();
        }
        return si;
    }

    private NotificationService getNotificationService() {
        return CoreSpringFactory.getBean(NotificationService.class);
    }

    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        try {
            final Translator translator = PackageUtil.createPackageTranslator(AbstractTaskNotificationHandler.class, locale);
            final Long resId = subscriber.getPublisher().getResId();
            final String displayName = repositoryService.lookupDisplayNameByOLATResourceableId(resId);
            return translator.translate(getNotificationHeaderKey(), new String[] { displayName });
        } catch (final Exception e) {
            log.error("Error while creating task notifications for subscriber: " + subscriber.getKey(), e);
            notificationsEBL.checkPublisher(subscriber.getPublisher());
            return "-";
        }
    }

    public static ContextualSubscriptionController createContextualSubscriptionController(final UserRequest ureq, final WindowControl wControl, final String folderPath,
            final SubscriptionContext subsContext, final Class callerClass) {
        final String businessPath = wControl.getBusinessControl().getAsString();
        final PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(callerClass), folderPath, businessPath);
        final ContextualSubscriptionController contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, wControl, subsContext, pdata);
        return contextualSubscriptionCtr;
    }

    // Abstract methods
    // //////////////////
    abstract protected String getCssClassIcon();

    abstract protected String getNotificationHeaderKey();

    abstract protected String getNotificationEntryKey();

}
