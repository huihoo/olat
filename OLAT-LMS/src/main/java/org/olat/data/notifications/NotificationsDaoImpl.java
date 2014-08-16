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

package org.olat.data.notifications;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description: <br>
 * see NotificationsManager Initial Date: 21.10.2004 <br>
 * 
 * @author Felix Jost
 */
@Repository("notificationsDao")
public class NotificationsDaoImpl extends BasicManager implements NotificationsDao {
    private static final Logger log = LoggerHelper.getLogger();

    private static final int PUB_STATE_OK = 0;
    private static final int PUB_STATE_NOT_OK = 1;

    @Autowired
    private DB db;
    @Autowired
    private CoordinatorManager coordinatorManager;

    /**
     * [used by spring]
     * 
     * 
     */
    private NotificationsDaoImpl() {
        // private since singleton

    }

    /**
     * @param resName
     * @param resId
     * @param subidentifier
     * @param type
     * @param data
     * @return a persisted publisher with ores/subidentifier as the composite primary key
     */
    public Publisher createAndPersistPublisher(final String resName, final Long resId, final String subidentifier, final String type, final String data,
            String businessPath) {
        if (resName == null || resId == null || subidentifier == null) {
            throw new AssertException("resName, resId, and subidentifier must not be null");
        }

        if (businessPath != null && businessPath.length() > 230) {
            log.error("Businesspath too long for publisher: " + resName + " with business path: " + businessPath);
            businessPath = businessPath.substring(0, 230);
        }
        final PublisherImpl pi = new PublisherImpl(resName, resId, subidentifier, type, data, businessPath, new Date(), PUB_STATE_OK);
        db.saveObject(pi);
        return pi;
    }

    /**
     * @param persistedPublisher
     * @param listener
     * @param subscriptionContext
     *            the context of the object we subscribe to
     * @return a subscriber with a db key
     */
    public Subscriber createAndPersistSubscriber(final Publisher persistedPublisher, final Identity listener) {
        final SubscriberImpl si = new SubscriberImpl(persistedPublisher, listener);
        si.setLastModified(new Date());
        si.setLatestEmailed(new Date());
        db.saveObject(si);
        return si;
    }

    /**
     * subscribers for ONE person (e.g. subscribed to 5 forums -> 5 subscribers belonging to this person)
     * 
     * @param identity
     * @return List of Subscriber Objects which belong to the identity
     */
    @SuppressWarnings("unchecked")
    public List<Subscriber> getSubscribers(final Identity identity) {

        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher where sub.identity = :anIdentity";
        final DBQuery query = db.createQuery(q);
        query.setEntity("anIdentity", identity);
        final List<Subscriber> res = query.list();
        return res;
    }

    /**
     * @param identity
     * @return a list of all subscribers which belong to the identity and which publishers are valid
     */
    @SuppressWarnings("unchecked")
    public List<Subscriber> getValidSubscribers(final Identity identity) {
        // pub.getState() == PUB_STATE_OK;

        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher as pub"
                + " where sub.identity = :anIdentity" + " and pub.state = :aState";
        final DBQuery query = db.createQuery(q);
        query.setEntity("anIdentity", identity);
        query.setLong("aState", PUB_STATE_OK);
        final List<Subscriber> res = query.list();
        return res;
    }

    /**
	 */
    @SuppressWarnings("unchecked")
    public List<Subscriber> getValidSubscribersOf(final Publisher publisher) {

        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub inner join fetch sub.identity" + " where sub.publisher = :publisher"
                + " and sub.publisher.state = " + PUB_STATE_OK;
        final DBQuery query = db.createQuery(q);
        query.setEntity("publisher", publisher);
        final List<Subscriber> res = query.list();
        return res;
    }

    /**
     * @return a list of subscribers ordered by the name of the identity of the subscription
     */
    public List<Subscriber> getAllValidSubscribers() {

        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher as pub" + " where pub.state = :aState"
                + " order by sub.identity.name";
        final DBQuery query = db.createQuery(q);
        query.setLong("aState", PUB_STATE_OK);
        final List<Subscriber> res = query.list();
        return res;
    }

    public List<Subscriber> getSubscriberList(final Identity identity, final String publisherType) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select sub from ").append(SubscriberImpl.class.getName()).append(" sub").append(" inner join fetch sub.publisher as pub")
                .append(" where sub.identity=:identity and pub.type=:type and pub.state=:aState");

        final DBQuery query = db.createQuery(sb.toString());
        query.setLong("aState", PUB_STATE_OK);
        query.setString("type", publisherType);
        query.setEntity("identity", identity);

        @SuppressWarnings("unchecked")
        final List<Subscriber> subscribers = query.list();
        return subscribers;
    }

    /**
     * @param key
     * @return the subscriber with this key or null if not found
     */
    @SuppressWarnings("unchecked")
    public Subscriber getSubscriber(final Long key) {

        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub" + " inner join fetch sub.publisher " + " where sub.key = :aKey";
        final DBQuery query = db.createQuery(q);
        query.setLong("aKey", key.longValue());
        final List<Subscriber> res = query.list();
        final int cnt = res.size();
        if (cnt == 0) {
            return null;
        }
        if (cnt > 1) {
            throw new AssertException("more than one subscriber for key " + key);
        }
        return res.get(0);
    }

    public List<Publisher> getAllPublisher() {

        final String q = "select pub from org.olat.data.notifications.PublisherImpl pub";
        final DBQuery query = db.createQuery(q);
        return query.list();
    }

    /**
     * return the publisher for the given composite primary key ores + subidentifier.
     */
    @SuppressWarnings("unchecked")
    public Publisher getPublisher(final String resName, final Long resId, final String subidentifier) {

        final String q = "select pub from org.olat.data.notifications.PublisherImpl pub" + " where pub.resName = :resName" + " and pub.resId = :resId"
                + " and pub.subidentifier = :subidentifier";
        final DBQuery query = db.createQuery(q);
        query.setString("resName", resName);
        query.setLong("resId", resId.longValue());
        query.setString("subidentifier", subidentifier);
        final List<Publisher> res = query.list();
        if (res.size() == 0) {
            return null;
        }
        if (res.size() != 1) {
            throw new AssertException("only one subscriber per person and publisher!!");
        }
        final Publisher p = res.get(0);
        return p;
    }

    /**
     * @param resName
     * @param resId
     * @return a list of publishers belonging to the resource
     */
    @SuppressWarnings("unchecked")
    public List<Publisher> getPublishers(final String resName, final Long resId) {

        final String q = "select pub from org.olat.data.notifications.PublisherImpl pub" + " where pub.resName = :resName" + " and pub.resId = :resId";
        final DBQuery query = db.createQuery(q);
        query.setString("resName", resName);
        query.setLong("resId", resId.longValue());
        final List<Publisher> res = query.list();
        return res;
    }

    /**
     * @param identity
     * @param publisher
     * @return a Subscriber object belonging to the identity and listening to the given publisher
     */
    @SuppressWarnings("unchecked")
    public Subscriber getSubscriber(final Identity identity, final Publisher publisher) {
        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub where sub.publisher = :publisher" + " and sub.identity = :identity";
        final DBQuery query = db.createQuery(q);
        query.setEntity("publisher", publisher);
        query.setEntity("identity", identity);
        final List<Subscriber> res = query.list();
        if (res.size() == 0) {
            return null;
        }
        if (res.size() != 1) {
            throw new AssertException("only one subscriber per person and publisher!!");
        }
        final Subscriber s = res.get(0);
        return s;
    }

    /**
	 */
    @Override
    public List<Subscriber> getSubscribers(final Publisher publisher) {
        final String q = "select sub from org.olat.data.notifications.SubscriberImpl sub where sub.publisher = :publisher";
        final DBQuery query = db.createQuery(q);
        query.setEntity("publisher", publisher);
        final List<Subscriber> res = query.list();
        return res;
    }

    /**
	 */
    @Override
    public List<Identity> getSubscriberIdentities(final Publisher publisher) {
        final String q = "select sub.identity from org.olat.data.notifications.SubscriberImpl sub where sub.publisher = :publisher";
        final DBQuery query = db.createQuery(q);
        query.setEntity("publisher", publisher);
        final List<Identity> res = query.list();
        return res;
    }

    /**
     * @param subscriber
     */
    public void deleteSubscriber(final Subscriber subscriber) {
        db.deleteObject(subscriber);
    }

    /**
     * @param subscriber
     */
    public void updateSubscriber(final Subscriber subscriber) {
        subscriber.setLastModified(new Date());
        db.updateObject(subscriber);
    }

    /**
     * @param publisher
     */
    public void updatePublisher(final Publisher publisher) {
        db.updateObject(publisher);
    }

    /**
     * @param publisher
     */
    public void deletePublisher(final Publisher publisher) {
        db.deleteObject(publisher);
    }

    /**
     * sets the latest visited date of the subscription to 'now' .assumes the identity is already subscribed to the publisher
     * 
     * @param identity
     * @param subsContext
     */
    public void markSubscriberRead(final Identity identity, final Publisher publisher) {
        final Subscriber sub = getSubscriber(identity, publisher);
        if (sub == null) {
            throw new AssertException("cannot markRead, since identity " + identity.getName() + " is not subscribed to subscontext " + publisher);
        }
        updateSubscriber(sub);
    }

    /**
     * call this method to indicate that there is news for the given subscriptionContext
     * 
     * @param subscriptionContext
     * @param ignoreNewsFor
     */
    public Set<Long> markPublisherNews(final String resName, final Long resId, final String subidentifier, final Identity ignoreNewsFor) {
        // to make sure: ignore if no subscriptionContext
        if (resName == null && resId == null && subidentifier == null) {
            return new HashSet<Long>();
        }
        final Date now = new Date();

        // two threads with both having a publisher they want to update
        // o_clusterOK by:cg
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName + "_" + subidentifier, resId);
        final Publisher publisher = coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Publisher>() {
            public Publisher execute() {
                final Publisher p = getPublisher(resName, resId, subidentifier);
                // if no publisher yet, ignore
                // TODO: check if that can be null
                if (p == null) {
                    return null;
                }

                // force a reload from db loadObject(..., true) by evicting it from
                // hibernates session
                // cache to catch up on a different thread having commited the update of
                // this object

                // not needed, since getPublisher()... always loads from db???
                // p = (Publisher) DB.getInstance().loadObject(p, true);

                p.setLatestNewsDate(now);
                updatePublisher(p);
                return p;
            }
        });
        if (publisher == null) {// TODO: check if that can be null
            return new HashSet<Long>();
        }

        // no need to sync, since there is only one gui thread at a time from one
        // user
        if (ignoreNewsFor != null) {
            final Subscriber sub = getSubscriber(ignoreNewsFor, publisher);
            if (sub != null) { // mark as read if subscribed
                updateSubscriber(sub);
            }
        }

        // channel-notify all interested listeners (e.g. the pnotificationsportletruncontroller)
        // 1. find all subscribers which can be affected
        final List<Subscriber> subscribers = getValidSubscribersOf(publisher);

        final Set<Long> subsKeys = new HashSet<Long>();
        // 2. collect all keys of the affected subscribers
        for (final Iterator<Subscriber> it_subs = subscribers.iterator(); it_subs.hasNext();) {
            final Subscriber su = it_subs.next();
            subsKeys.add(su.getKey());
        }
        return subsKeys;

    }

    /**
     * @param identity
     * @param subscriptionContext
     * @return true if this user is subscribed
     */
    @SuppressWarnings("unchecked")
    public boolean isSubscribed(final Identity identity, final String resName, final Long resId, final String subidentifier) {

        final String q = "select count(sub) from org.olat.data.notifications.SubscriberImpl sub inner join sub.publisher as pub "
                + " where sub.identity = :anIdentity and pub.resName = :resName and pub.resId = :resId" + " and pub.subidentifier = :subidentifier group by sub";
        final DBQuery query = db.createQuery(q);
        query.setEntity("anIdentity", identity);
        query.setString("resName", resName);
        query.setLong("resId", resId);
        query.setString("subidentifier", subidentifier);
        final List res = query.list();
        // must return one result or null
        if (res.isEmpty()) {
            return false;
        }
        final long cnt = ((Long) res.get(0)).longValue();
        if (cnt == 0) {
            return false;
        } else if (cnt == 1) {
            return true;
        } else {
            throw new AssertException("more than once subscribed!" + identity + ", " + resName + "," + resId + "," + subidentifier);
        }
    }

    /**
     * delete publisher and subscribers
     * 
     * @param scontext
     *            the subscriptioncontext
     */
    public void delete(final Publisher publisher) {
        // if none found, no one has subscribed yet and therefore no publisher has
        // been generated lazily.
        // -> nothing to do
        if (publisher == null) {
            return;
        }
        // first delete all subscribers
        final List<Subscriber> subscribers = getValidSubscribersOf(publisher);
        for (final Object susbscriberObj : subscribers) {
            deleteSubscriber((Subscriber) susbscriberObj);
        }
        // else:
        deletePublisher(publisher);
    }

    /**
     * delete publisher and subscribers
     * 
     * @param publisher
     *            the publisher to delete
     */
    public void deactivate(final Publisher publisher) {
        publisher.setState(PUB_STATE_NOT_OK);
        updatePublisher(publisher);
    }

    /**
     * @param pub
     * @return true if the publisher is valid (that is: has not been marked as deleted)
     */
    public boolean isPublisherValid(final Publisher pub) {
        return pub.getState() == PUB_STATE_OK;
    }

}
