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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.NotificationsDao;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.notifications.event.EventFactory;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.notifications.NotificationSubscriptionController;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.security.OLATPrincipal;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Class Description for NotificationServiceImpl
 * 
 * <P>
 * Initial Date: 10.05.2011 <br>
 * 
 * @author lavinia
 */
@Deprecated
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerHelper.getLogger();

    static final Class<NotificationSubscriptionController> CLASS_SERVING_FOR_PACKAGETRANSLATOR_CREATION = NotificationSubscriptionController.class;

    static final String KEY_RSS_TITLE = "rss.title";

    @Autowired
    private NotificationsDao notificationDao;

    @Autowired
    I18nManager i18nManager;

    static final String LATEST_EMAIL_USER_PROP = "noti_latest_email";

    private Map<String, NotificationsHandler> notificationHandlers;
    private Map<String, Object> notificationHandlersMap;

    private final SubscriptionInfo NOSUBSINFO = new NoSubscriptionInfo();

    private final Object lockObject = new Object();

    private static final Map<String, Integer> INTERVAL_DEF_MAP = buildIntervalMap();

    private List<String> notificationIntervals;

    private String defaultNotificationInterval;

    private final OLATResourceable oresMyself = OresHelper.lookupType(NotificationServiceImpl.class);

    @Autowired
    private NotificationServiceStaticDependenciesWrapper staticDelegator;

    @SuppressWarnings("unused")
    private NotificationServiceImpl() {
        // spring only
    }

    /**
     * testing
     */
    NotificationServiceImpl(final NotificationsDao notificationsManager, Map<String, Object> notificationHandlersMap,
            NotificationServiceStaticDependenciesWrapper staticDelegator) {
        this.notificationDao = notificationsManager;
        this.notificationHandlersMap = notificationHandlersMap;
        this.staticDelegator = staticDelegator;
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getSubscriptionInfos(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    @Override
    public List<SubscriptionInfo> getSubscriptionInfos(Identity identity, String publisherType) {
        final List<Subscriber> subscribers = notificationDao.getSubscriberList(identity, publisherType);
        if (subscribers.isEmpty()) {
            return Collections.emptyList();
        }

        final List<SubscriptionInfo> sis = getSubscriptionInfos(identity, subscribers);
        return sis;
    }

    private List<SubscriptionInfo> getSubscriptionInfos(final Identity identity, List<Subscriber> subscribers) {
        if (subscribers.isEmpty()) {
            return Collections.emptyList();
        }

        final Locale locale = new Locale(identity.getUser().getPreferences().getLanguage());
        final Date compareDate = getDefaultCompareDate();
        final List<SubscriptionInfo> sis = new ArrayList<SubscriptionInfo>();
        for (final Subscriber subscriber : subscribers) {
            final Publisher pub = subscriber.getPublisher();
            final NotificationsHandler notifHandler = getNotificationsHandler(pub);
            // do not create subscription item when deleted
            if (isPublisherValid(pub)) {
                final SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, compareDate);
                if (subsInfo.hasNews()) {
                    sis.add(subsInfo);
                }
            }
        }
        return sis;
    }

    /**
     * if no compareDate is selected, cannot be calculated by user-interval, or no latestEmail is available => use this to get a Date 30d in the past. maybe the latest
     * user-login could also be used.
     * 
     * @return Date
     */
    protected Date getDefaultCompareDate() {
        final Calendar calNow = Calendar.getInstance();
        calNow.add(Calendar.DAY_OF_MONTH, -30);
        final Date compareDate = calNow.getTime();
        return compareDate;
    }

    public void notifyAllSubscribersByEmail(String title) {
        log.info("Audit:starting notification cronjob for email sending", null);
        final List<Subscriber> subs = notificationDao.getAllValidSubscribers();
        // ordered by identity.name!

        List<SubscriptionItem> items = new ArrayList<SubscriptionItem>();
        List<Subscriber> subsToUpdate = null;
        final StringBuilder mailLog = new StringBuilder();
        final StringBuilder mailErrorLog = new StringBuilder();

        boolean veto = false;
        Subscriber latestSub = null;
        Identity ident = null;
        Translator translator = null;
        Locale locale = null;

        final Date defaultCompareDate = getDefaultCompareDate();
        long start = System.currentTimeMillis();

        // loop all subscriptions, as its ordered by identity, they get collected for each identity
        for (final Subscriber sub : subs) {
            try {
                ident = sub.getIdentity();
                User identUser = ident.getUser();
                Preferences identUserPreferences = identUser.getPreferences();
                String language = identUserPreferences.getLanguage();
                locale = staticDelegator.getLocaleFor(language);
                if (latestSub == null || (!ident.equalsByPersistableKey(latestSub.getIdentity()))) {
                    // first time or next identity => prepare for a new user and send old data.

                    // send a mail
                    notifySubscribersByEmail(latestSub, items, subsToUpdate, title, start, veto, mailLog, mailErrorLog);

                    // prepare for new user
                    start = System.currentTimeMillis();
                    translator = getNotificationsTranslator(ident);
                    items = new ArrayList<SubscriptionItem>();
                    subsToUpdate = new ArrayList<Subscriber>();
                    latestSub = sub;
                    veto = false;

                    final PropertyManager pm = staticDelegator.getPropertyManager();
                    PropertyImpl p = pm.findProperty(ident, null, null, null, LATEST_EMAIL_USER_PROP);
                    if (p != null) {
                        final Date latestEmail = new Date(p.getLongValue());
                        final String userInterval = getUserIntervalOrDefault(ident);
                        final Date compareDate = getCompareDateFromInterval(userInterval);
                        if (latestEmail.after(compareDate)) {
                            veto = true;
                        }
                    }
                }

                if (veto) {
                    continue;
                }
                // only send notifications to active users
                if (ident.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) >= 0) {
                    continue;
                }
                // this user doesn't want notifications
                final String userInterval = getUserIntervalOrDefault(ident);
                if ("never".equals(userInterval)) {
                    continue;
                }

                // find out the info that happened since the date the last email was sent. Only those infos need to be emailed.
                // mail is only sent if users interval is over.
                final Date compareDate = getCompareDateFromInterval(userInterval);
                Date latestEmail = sub.getLatestEmailed();

                SubscriptionItem subsitem = null;
                if (latestEmail == null || compareDate.after(latestEmail)) {
                    // no notif. ever sent until now
                    if (latestEmail == null) {
                        latestEmail = defaultCompareDate;
                    } else if (latestEmail.before(defaultCompareDate)) {
                        // no notification older than a month
                        latestEmail = defaultCompareDate;
                    }
                    subsitem = createSubscriptionItem(sub, locale, SubscriptionInfo.MIME_PLAIN, SubscriptionInfo.MIME_PLAIN, latestEmail);
                } else if (latestEmail != null && latestEmail.after(compareDate)) {
                    // already send an email within the user's settings interval
                    veto = true;
                }
                if (subsitem != null) {
                    items.add(subsitem);
                    subsToUpdate.add(sub);
                }
            } catch (final Error er) {
                log.error("Error in NotificationsManagerImpl.notifyAllSubscribersByEmail, ", er);
                throw er;
            } catch (final RuntimeException re) {
                log.error("RuntimeException in NotificationsManagerImpl.notifyAllSubscribersByEmail,", re);
                throw re;
            } catch (final Throwable th) {
                log.error("Throwable in NotificationsManagerImpl.notifyAllSubscribersByEmail,", th);
            }
        } // for

        // done, purge last entry
        notifySubscribersByEmail(latestSub, items, subsToUpdate, title, start, veto, mailLog, mailErrorLog);

        // purge logs
        if (mailErrorLog.length() > 0) {
            log.info("Audit:error sending email to the following identities: " + mailErrorLog.toString(), null);
        }
        log.info("Audit:sent email to the following identities: " + mailLog.toString(), null);
    }

    private void notifySubscribersByEmail(final Subscriber latestSub, final List<SubscriptionItem> items, final List<Subscriber> subsToUpdate, final String title,
            final long start, final boolean veto, final StringBuilder mailLog, final StringBuilder mailErrorLog) {
        if (veto) {
            if (latestSub != null) {
                mailLog.append(latestSub.getIdentity().getName()).append(" already received email within prefs interval, ");
            }
        } else if (items.size() > 0) {
            final Identity curIdent = latestSub.getIdentity();
            final boolean sentOk = sendMailToUserAndUpdateSubscriber(curIdent, items, title, subsToUpdate);
            if (sentOk) {
                final PropertyManager pm = staticDelegator.getPropertyManager();
                PropertyImpl p = pm.findProperty(curIdent, null, null, null, LATEST_EMAIL_USER_PROP);
                if (p == null) {
                    p = pm.createUserPropertyInstance(curIdent, null, LATEST_EMAIL_USER_PROP, null, null, null, null);
                    p.setLongValue(new Date().getTime());
                    pm.saveProperty(p);
                } else {
                    p.setLongValue(new Date().getTime());
                    pm.updateProperty(p);
                }

                mailLog.append(curIdent.getName()).append(' ').append(items.size()).append(' ').append((System.currentTimeMillis() - start)).append("ms, ");
            } else {
                mailErrorLog.append(curIdent.getName()).append(", ");
            }
        }
        // collecting the SubscriptionItem can potentially make a lot of DB calls
        staticDelegator.intermediateCommit();
    }

    @Override
    public boolean sendMailToUserAndUpdateSubscriber(final Identity curIdent, final List<SubscriptionItem> items, String title, final List<Subscriber> subscribersToUpdate) {
        final String formattedNameOfIdentity = staticDelegator.getFormatedName(curIdent);
        final boolean sentOk = sendEmail(curIdent, formattedNameOfIdentity, items);
        // save latest email sent date for the subscription just emailed
        // do this only if the mail was successfully sent
        if (sentOk) {
            for (final Iterator<Subscriber> it_subs = subscribersToUpdate.iterator(); it_subs.hasNext();) {
                final Subscriber subscriber = it_subs.next();
                subscriber.setLatestEmailed(new Date());
                notificationDao.updateSubscriber(subscriber);
            }
        }
        return sentOk;
    }

    private boolean sendEmail(final Identity to, final String title, final List<SubscriptionItem> subItems) {
        final StringBuilder plaintext = new StringBuilder();
        for (final Iterator<SubscriptionItem> it_subs = subItems.iterator(); it_subs.hasNext();) {
            final SubscriptionItem subitem = it_subs.next();
            plaintext.append(subitem.getTitle());
            if (StringHelper.containsNonWhitespace(subitem.getLink())) {
                plaintext.append("\n");
                plaintext.append(subitem.getLink());
            }
            plaintext.append("\n");
            if (StringHelper.containsNonWhitespace(subitem.getDescription())) {
                plaintext.append(subitem.getDescription());
            }
            plaintext.append("\n\n");
        }
        final String mailText = plaintext.toString();
        final MailTemplate mailTempl = new MailTemplate(title, mailText, staticDelegator.getMailFooter(to, null), null) {

            @Override
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal recipient) {
                // nothing to do
            }
        };

        final MailerResult result = staticDelegator.sendMail(to, null, null, mailTempl, null);
        if (result.getReturnCode() > 0) {
            log.warn("Could not send email to identity " + to.getName() + ". (returncode=" + result.getReturnCode() + ", to=" + to + ")", null);
            return false;
        }
        return true;
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getSubscriber(java.lang.Long)
     */
    @Override
    public Subscriber getSubscriber(Long key) {
        return notificationDao.getSubscriber(key);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getPublisher(org.olat.lms.notifications.SubscriptionContext)
     */
    @Override
    public Publisher getPublisher(SubscriptionContext subsContext) {
        if (subsContext == null) {
            return null;
        }
        return notificationDao.getPublisher(subsContext.getResName(), subsContext.getResId(), subsContext.getSubidentifier());
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getAllPublisher()
     */
    @Override
    public List<Publisher> getAllPublisher() {
        return notificationDao.getAllPublisher();
    }

    /**
     * deletes all publishers of the given olatresourceable. e.g. ores = businessgroup 123 -> deletes possible publishers: of Folder(toolfolder), of Forum(toolforum)
     * 
     * @param ores
     */
    public void deletePublishersOf(final OLATResourceable ores) {
        final String type = ores.getResourceableTypeName();
        final Long id = ores.getResourceableId();
        if (type == null || id == null) {
            throw new AssertException("type/id cannot be null! type:" + type + " / id:" + id);
        }
        final List<Publisher> pubs = notificationDao.getPublishers(type, id);
        for (final Iterator<Publisher> it_pub = pubs.iterator(); it_pub.hasNext();) {
            final Publisher pub = it_pub.next();

            // grab all subscribers to the publisher and delete them
            final List<Subscriber> subs = getValidSubscribersOf(pub);
            for (final Iterator<Subscriber> iterator = subs.iterator(); iterator.hasNext();) {
                final Subscriber sub = iterator.next();
                unsubscribe(sub);
            }
            notificationDao.deletePublisher(pub);
        }
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getSubscriber(org.olat.data.basesecurity.Identity, org.olat.data.notifications.Publisher)
     */
    @Override
    public Subscriber getSubscriber(Identity identity, Publisher publisher) {
        return notificationDao.getSubscriber(identity, publisher);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getSubscribers(org.olat.data.notifications.Publisher)
     */
    @Override
    public List<Subscriber> getSubscribers(Publisher publisher) {
        return notificationDao.getSubscribers(publisher);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getSubscriberIdentities(org.olat.data.notifications.Publisher)
     */
    @Override
    public List<Identity> getSubscriberIdentities(Publisher publisher) {
        return notificationDao.getSubscriberIdentities(publisher);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#markSubscriberRead(org.olat.data.basesecurity.Identity, org.olat.lms.notifications.SubscriptionContext)
     */
    @Override
    public void markSubscriberRead(Identity identity, SubscriptionContext subsContext) {
        final Publisher publisher = notificationDao.getPublisher(subsContext.getResName(), subsContext.getResId(), subsContext.getSubidentifier());
        if (publisher == null) {
            throw new AssertException("cannot markRead for identity " + identity.getName()
                    + ", since the publisher for the given subscriptionContext does not exist: subscontext = " + subsContext);
        }
        notificationDao.markSubscriberRead(identity, publisher);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#markPublisherNews(org.olat.lms.notifications.SubscriptionContext, org.olat.data.basesecurity.Identity)
     */
    @Override
    public void markPublisherNews(SubscriptionContext subscriptionContext, Identity ignoreNewsFor) {
        if (subscriptionContext == null) {
            return;
        } // TODO: 8.6.2011/cg : null-check exists already in 7.2, without check => Redscreen in home-calender,add event to personal-calendar

        Set<Long> subsKeys = notificationDao.markPublisherNews(subscriptionContext.getResName(), subscriptionContext.getResId(), subscriptionContext.getSubidentifier(),
                ignoreNewsFor);
        // fire the event
        final MultiUserEvent mue = EventFactory.createAffectedEvent(subsKeys);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, oresMyself);
    }

    /**
     * interval string or defaultinterval if not valid or not set
     */
    public String getUserIntervalOrDefault(final Identity ident) {
        String userInterval = getUserNotificationInterval(ident);
        if (!StringHelper.containsNonWhitespace(userInterval)) {
            userInterval = getDefaultNotificationInterval();
        }
        final List<String> avIntvls = getEnabledNotificationIntervals();
        if (!avIntvls.contains(userInterval)) {
            log.warn("User " + ident.getName() + " has an invalid notification-interval (not found in config): " + userInterval, null);
            userInterval = getDefaultNotificationInterval();
        }
        return userInterval;
    }

    private String getUserNotificationInterval(Identity ident) {
        // Always return a valid notification interval
        String storedNotificationInterval = ident.getUser().getPreferences().getNotificationInterval();
        if (storedNotificationInterval == null || storedNotificationInterval.isEmpty() || !this.getEnabledNotificationIntervals().contains(storedNotificationInterval)) {
            return getDefaultNotificationInterval();
        }
        return storedNotificationInterval;
    }

    /**
     * calculate a Date from the past with given interval (now - interval)
     */
    public Date getCompareDateFromInterval(final String interval) {
        final Calendar calNow = Calendar.getInstance();
        // get hours to subtract from now
        final Integer diffHours = INTERVAL_DEF_MAP.get(interval);
        calNow.add(Calendar.HOUR_OF_DAY, -diffHours);
        final Date compareDate = calNow.getTime();
        return compareDate;
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#isSubscribed(org.olat.data.basesecurity.Identity, org.olat.lms.notifications.SubscriptionContext)
     */
    @Override
    public boolean isSubscribed(Identity identity, SubscriptionContext subscriptionContext) {
        return notificationDao.isSubscribed(identity, subscriptionContext.getResName(), subscriptionContext.getResId(), subscriptionContext.getSubidentifier());
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#delete(org.olat.lms.notifications.SubscriptionContext)
     */
    @Override
    public void delete(SubscriptionContext scontext) {
        final Publisher publisher = notificationDao.getPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier());
        notificationDao.delete(publisher);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#deactivate(org.olat.data.notifications.Publisher)
     */
    @Override
    public void deactivate(Publisher publisher) {
        notificationDao.deactivate(publisher);

    }

    /**
     * @see org.olat.lms.notifications.NotificationService#isPublisherValid(org.olat.data.notifications.Publisher)
     */
    @Override
    public boolean isPublisherValid(Publisher pub) {
        return notificationDao.isPublisherValid(pub);
    }

    /**
     * no match if: a) not the same publisher b) a deleted publisher
     * 
     * @param p
     * @param subscriptionContext
     * @return true when the subscriptionContext refers to the publisher p
     */
    public boolean matches(final Publisher p, final SubscriptionContext subscriptionContext) {
        // if the publisher has been deleted in the meantime, return no match
        if (!isPublisherValid(p)) {
            return false;
        }
        final boolean ok = (p.getResName().equals(subscriptionContext.getResName()) && p.getResId().equals(subscriptionContext.getResId()) && p.getSubidentifier()
                .equals(subscriptionContext.getSubidentifier()));
        return ok;
    }

    /**
     * @param subscriber
     * @param locale
     * @param mimeType
     *            text/html or text/plain
     * @return the item or null if there is currently no news for this subscription
     */
    public SubscriptionItem createSubscriptionItem(final Subscriber subscriber, final Locale locale, final String mimeTypeTitle, final String mimeTypeContent) {
        // calculate the item based on subscriber.getLastestReadDate()
        // used for rss-feed, no longer than 1 month
        final Date compareDate = getDefaultCompareDate();
        return createSubscriptionItem(subscriber, locale, mimeTypeTitle, mimeTypeContent, compareDate);
    }

    /**
     * @param subscriber
     * @param locale
     * @param mimeType
     * @param latestEmailed
     *            needs to be given! SubscriptionInfo is collected from then until latestNews of publisher
     * @return null if the publisher is not valid anymore (deleted), or if there are no news
     */
    public SubscriptionItem createSubscriptionItem(final Subscriber subscriber, final Locale locale, final String mimeTypeTitle, final String mimeTypeContent,
            final Date latestEmailed) {
        if (latestEmailed == null) {
            throw new AssertException("compareDate may not be null, use a date from history");
        }

        try {
            SubscriptionItem si = null;
            final Publisher pub = subscriber.getPublisher();
            final NotificationsHandler notifHandler = getNotificationsHandler(pub);
            // do not create subscription item when deleted
            if (isPublisherValid(pub)) {
                if (log.isDebugEnabled()) {
                    log.debug("NotifHandler: " + notifHandler.getClass().getName() + " compareDate: " + latestEmailed.toString() + " now: " + new Date().toString(), null);
                }
                final SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, latestEmailed);
                if (subsInfo.hasNews()) {
                    final String title = getFormatedTitle(subsInfo, subscriber, locale, mimeTypeTitle);

                    String itemLink = null;
                    if (subsInfo.getCustomUrl() != null) {
                        itemLink = subsInfo.getCustomUrl();
                    }
                    if (itemLink == null && pub.getBusinessPath() != null) {
                        itemLink = NotificationHelper.getURLFromBusinessPathString(pub, pub.getBusinessPath());
                    }

                    final String description = subsInfo.getSpecificInfo(mimeTypeContent, locale);
                    si = new SubscriptionItem(title, itemLink, description);
                }
            }
            return si;
        } catch (final Exception e) {
            log.error("Cannot generate a subscription item.", e);
            return null;
        }
    }

    /**
     * format the type-title and title-details
     * 
     * @param subscriber
     * @param locale
     * @param mimeType
     * @return
     */
    protected String getFormatedTitle(final SubscriptionInfo subsInfo, final Subscriber subscriber, final Locale locale, final String mimeType) {
        final Publisher pub = subscriber.getPublisher();
        final String innerType = pub.getType();
        final String typeName = ControllerFactory.translateResourceableTypeName(innerType, locale);
        final StringBuilder titleSb = new StringBuilder();
        titleSb.append(typeName);

        final String title = subsInfo.getTitle(mimeType);
        if (StringHelper.containsNonWhitespace(title)) {
            titleSb.append(": ").append(title);
        } else {
            final NotificationsHandler notifHandler = getNotificationsHandler(pub);
            final String titleInfo = notifHandler.createTitleInfo(subscriber, locale);
            if (StringHelper.containsNonWhitespace(titleInfo)) {
                titleSb.append(": ").append(titleInfo);
            }
        }

        return titleSb.toString();
    }

    /**
	 */
    public SubscriptionInfo getNoSubscriptionInfo() {
        return NOSUBSINFO;
    }

    /**
     * Delete all subscribers for certain identity.
     * 
     * @param identity
     */
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        final List<Subscriber> subscribers = getSubscribers(identity);
        for (final Iterator<Subscriber> iter = subscribers.iterator(); iter.hasNext();) {
            notificationDao.deleteSubscriber(iter.next());
        }
        log.debug("All notification-subscribers deleted for identity=" + identity, null);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getSubscribers(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List<Subscriber> getSubscribers(Identity identity) {
        return notificationDao.getSubscribers(identity);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getValidSubscribers(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List<Subscriber> getValidSubscribers(Identity identity) {
        return notificationDao.getValidSubscribers(identity);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getValidSubscribersOf(org.olat.data.notifications.Publisher)
     */
    @Override
    public List<Subscriber> getValidSubscribersOf(Publisher publisher) {

        return notificationDao.getValidSubscribersOf(publisher);
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#subscribe(org.olat.data.basesecurity.Identity, org.olat.lms.notifications.SubscriptionContext,
     *      org.olat.lms.notifications.PublisherData)
     */
    @Override
    public void subscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData) {
        // no need to sync, since an identity only has one gui thread / one mouse
        final Publisher publisher = findOrCreatePublisher(subscriptionContext, publisherData);
        final Subscriber s = getSubscriber(identity, publisher);
        if (s == null) {
            // no subscriber -> create.
            // s.latestReadDate >= p.latestNewsDate == no news for subscriber when no
            // news after subscription time
            notificationDao.createAndPersistSubscriber(publisher, identity);
        }
    }

    /**
     * @param scontext
     * @param pdata
     * @return the publisher
     */
    private Publisher findOrCreatePublisher(final SubscriptionContext scontext, final PublisherData pdata) {
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(scontext.getResName() + "_" + scontext.getSubidentifier(), scontext.getResId());
        // o_clusterOK by:cg
        final Publisher publisher = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Publisher>() {
            public Publisher execute() {
                Publisher p = notificationDao.getPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier());
                // if not found, create it
                if (p == null) {
                    p = notificationDao.createAndPersistPublisher(scontext.getResName(), scontext.getResId(), scontext.getSubidentifier(), pdata.getType(),
                            pdata.getData(), pdata.getBusinessPath());
                }
                if (p.getData() == null || !p.getData().startsWith("[")) {
                    // update silently the publisher
                    if (pdata.getData() != null) {
                        // updatePublisher(p, pdata.getData());
                    }
                }
                return p;
            }
        });
        return publisher;
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#unsubscribe(org.olat.data.notifications.Subscriber)
     */
    @Override
    public void unsubscribe(Subscriber s) {
        final Subscriber foundSub = getSubscriber(s.getKey());
        if (foundSub != null) {
            notificationDao.deleteSubscriber(foundSub);
        } else {
            log.warn("could not unsubscribe " + s.getIdentity().getName() + " from publisher:" + s.getPublisher().getResName() + "," + s.getPublisher().getResId() + ","
                    + s.getPublisher().getSubidentifier(), null);
        }
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#unsubscribe(org.olat.data.basesecurity.Identity, org.olat.lms.notifications.SubscriptionContext)
     */
    @Override
    public void unsubscribe(Identity identity, SubscriptionContext subscriptionContext) {
        // no need to sync, since an identity only has one gui thread / one mouse
        final Publisher publisher = notificationDao
                .getPublisher(subscriptionContext.getResName(), subscriptionContext.getResId(), subscriptionContext.getSubidentifier());
        // if no publisher yet.
        // TODO: check race condition: can p be null at all?
        if (publisher == null) {
            return;
        }
        final Subscriber s = getSubscriber(identity, publisher);
        if (s != null) {
            notificationDao.deleteSubscriber(s);
        } else {
            log.warn(
                    "could not unsubscribe " + identity.getName() + " from publisher:" + publisher.getResName() + "," + publisher.getResId() + ","
                            + publisher.getSubidentifier(), null);
        }
    }

    /**
     * @return the handler for the type
     */
    public NotificationsHandler getNotificationsHandler(final Publisher publisher) {
        final String type = publisher.getType();
        if (notificationHandlers == null) {
            synchronized (lockObject) {
                if (notificationHandlers == null) { // check again in synchronized-block, only one may create list
                    notificationHandlers = new HashMap<String, NotificationsHandler>();
                    if (notificationHandlersMap == null) {
                        notificationHandlersMap = getNotificationHandlersMap();
                    }
                    final Collection<Object> notificationsHandlerValues = notificationHandlersMap.values();
                    for (final Object object : notificationsHandlerValues) {
                        final NotificationsHandler notificationsHandler = (NotificationsHandler) object;
                        log.debug("initNotificationUpgrades notificationsHandler=" + notificationsHandler);
                        notificationHandlers.put(notificationsHandler.getType(), notificationsHandler);
                    }
                }
            }
        }
        return notificationHandlers.get(type);
    }

    private Map<String, Object> getNotificationHandlersMap() {
        return staticDelegator.getNotificationHandlers();
    }

    /**
     * Spring setter method
     * 
     * @param notificationIntervals
     */
    public void setNotificationIntervals(final Map<String, Boolean> intervals) {
        notificationIntervals = new ArrayList<String>();
        for (final String key : intervals.keySet()) {
            if (intervals.get(key)) {
                if (key.length() <= 16) {
                    notificationIntervals.add(key);
                } else {
                    log.error("Interval notification cannot be more than 16 characters wide: " + key, null);
                }
            }
        }
    }

    /**
     * @see org.olat.lms.notifications.NotificationService#getEnabledNotificationIntervals()
     */
    @Override
    public List<String> getEnabledNotificationIntervals() {
        return notificationIntervals;
    }

    /**
     * Spring setter method
     * 
     * @param defaultNotificationInterval
     */
    public void setDefaultNotificationInterval(final String defaultNotificationInterval) {
        this.defaultNotificationInterval = defaultNotificationInterval;
    }

    /**
	 */
    public String getDefaultNotificationInterval() {
        return defaultNotificationInterval;
    }

    /**
     * Needs to correspond to notification-settings. all available configs should be contained in the map below!
     * 
     * @return
     */
    private static final Map<String, Integer> buildIntervalMap() {
        final Map<String, Integer> intervalDefMap = new HashMap<String, Integer>();
        intervalDefMap.put("never", 0);
        intervalDefMap.put("monthly", 720);
        intervalDefMap.put("weekly", 168);
        intervalDefMap.put("daily", 24);
        intervalDefMap.put("half-daily", 12);
        intervalDefMap.put("four-hourly", 4);
        intervalDefMap.put("two-hourly", 2);
        return intervalDefMap;
    }

    /**
	 */
    public void registerAsListener(final GenericEventListener gel, final Identity ident) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(gel, ident, oresMyself);
    }

    /**
	 */
    public void deregisterAsListener(final GenericEventListener gel) {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(gel, oresMyself);
    }

    @Override
    public Translator getNotificationsTranslator(Identity identity) {
        String language = identity.getUser().getPreferences().getLanguage();
        final Locale locale = staticDelegator.getLocaleFor(language);
        return PackageUtil.createPackageTranslator(CLASS_SERVING_FOR_PACKAGETRANSLATOR_CREATION, locale);
    }

}
