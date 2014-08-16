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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.user.UserDataDeletable;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.GenericEventListener;

/**
 * TODO: Class Description for NotificationService
 * 
 * <P>
 * Initial Date: 10.05.2011 <br>
 * 
 * @author lavinia
 */
@Deprecated
public interface NotificationService extends UserDataDeletable {

    /**
     * Return all the subscription items for a defined user and restricted to a single type of publisher
     * 
     * @param identity
     * @param publisherType
     * @return
     */
    public List<SubscriptionInfo> getSubscriptionInfos(Identity identity, String publisherType);

    /**
     * Notify all users by email with their notifications
     */
    public void notifyAllSubscribersByEmail(String title);

    /**
     * Send an email to a single user with the given set of subscription items
     * 
     * @param curIdent
     * @param items
     * @param translator
     * @param subscribersToUpdate
     * @return
     */
    public boolean sendMailToUserAndUpdateSubscriber(Identity curIdent, List<SubscriptionItem> items, String title, List<Subscriber> subscribersToUpdate);

    /**
     * @param key
     * @return the subscriber with this key or null if not found
     */
    public Subscriber getSubscriber(Long key);

    /**
     * @param subsContext
     * @return the publisher belonging to the given context or null
     */
    public Publisher getPublisher(SubscriptionContext subsContext);

    public List<Publisher> getAllPublisher();

    /**
     * deletes all publishers of the given olatresourceable. e.g. ores = businessgroup 123 -> deletes possible publishers: of Folder(toolfolder), of Forum(toolforum)
     * 
     * @param ores
     */
    public void deletePublishersOf(OLATResourceable ores);

    /**
     * @param identity
     * @param publisher
     * @return a Subscriber object belonging to the identity and listening to the given publisher
     */
    public Subscriber getSubscriber(Identity identity, Publisher publisher);

    /**
     * Return all subscribers of a publisher
     * 
     * @param publisher
     * @return
     */
    public List<Subscriber> getSubscribers(Publisher publisher);

    /**
     * Return identities of all subscribers of the publisher
     * 
     * @param publisher
     * @return
     */
    public List<Identity> getSubscriberIdentities(Publisher publisher);

    /**
     * sets the latest visited date of the subscription to 'now' .assumes the identity is already subscribed to the publisher
     * 
     * @param identity
     * @param subsContext
     */
    public void markSubscriberRead(Identity identity, SubscriptionContext subsContext);

    /**
     * call this method to indicate that there is news for the given subscriptionContext
     * 
     * @param subscriptionContext
     * @param ignoreNewsFor
     */
    public void markPublisherNews(SubscriptionContext subscriptionContext, Identity ignoreNewsFor);

    /**
     * get interval of identity.
     * 
     * @param ident
     * @return interval string or defaultinterval if not valid or not set
     */
    public String getUserIntervalOrDefault(Identity ident);

    /**
     * calculate a Date from the past with given interval (now - interval)
     * 
     * @param interval
     * @return
     */
    public Date getCompareDateFromInterval(String interval);

    /**
     * @param identity
     * @param subscriptionContext
     * @return true if this user is subscribed
     */
    public boolean isSubscribed(Identity identity, SubscriptionContext subscriptionContext);

    /**
     * marks the publisher as deleted. It cannot delete the publisher, since most often there are subscribers listening to the publisher. Instead, the subscriptioncontext
     * of the publisher is set to null,null,null (so a new publisher with the name ores is possible) and the state set to PUB_STATE_DELETED <br>
     * only the resName, resId, and subIdentifier of the subscriptioncontext are used in this method
     * 
     * @param scontext
     *            the subscriptioncontext
     */
    public void delete(SubscriptionContext scontext);

    /**
     * delete the publisher and all subscribers to this publisher.
     * 
     * @param publisher
     */
    public void deactivate(Publisher publisher);

    /**
     * @param pub
     * @return true if the publisher is valid (that is: has not been marked as deleted)
     */
    public boolean isPublisherValid(Publisher pub);

    /**
     * no match if: a) not the same publisher b) a deleted publisher
     * 
     * @param p
     * @param subscriptionContext
     * @return true when the subscriptionContext refers to the publisher p
     */
    public boolean matches(Publisher p, SubscriptionContext subscriptionContext);

    /**
     * @param subscriber
     * @param locale
     * @param mimeType
     *            text/html or text/plain
     * @return the item or null if there is currently no news for this subscription
     */
    public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent);

    /**
     * @param subscriber
     * @param locale
     * @param mimeType
     * @param lowerDateBoundary
     *            The date from which the news should be collected
     * @return
     */
    public SubscriptionItem createSubscriptionItem(Subscriber subscriber, Locale locale, String mimeTypeTitle, String mimeTypeContent, Date lowerDateBoundary);

    public SubscriptionInfo getNoSubscriptionInfo();

    /**
     * Delete all subscribers for certain identity.
     * 
     * @param identity
     */
    public void deleteUserData(Identity identity, String newDeletedUserName);

    /**
     * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers belonging to this person)
     * 
     * @param identity
     * @return List of Subscriber Objects which belong to the identity
     */
    public List<Subscriber> getSubscribers(Identity identity);

    /**
     * @param identity
     * @return a list of all subscribers which belong to the identity and which publishers are valid
     */
    public List<Subscriber> getValidSubscribers(Identity identity);

    /**
     * @param publisher
     * @return
     */
    public List<Subscriber> getValidSubscribersOf(Publisher publisher);

    /**
     * @param identity
     * @param subscriptionContext
     * @param publisherData
     */
    public void subscribe(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData);

    public void unsubscribe(Subscriber s);

    /**
     * @param identity
     * @param subscriptionContext
     */
    public void unsubscribe(Identity identity, SubscriptionContext subscriptionContext);

    /**
     * @return the handler for the type
     */
    public NotificationsHandler getNotificationsHandler(Publisher publisher);

    /**
     * @return the notification intervals
     */
    public List<String> getEnabledNotificationIntervals();

    /**
     * @return the default notification interval
     */
    public String getDefaultNotificationInterval();

    public void registerAsListener(GenericEventListener gel, Identity ident);

    public void deregisterAsListener(GenericEventListener gel);

    public Translator getNotificationsTranslator(Identity identity);

}
