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

import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;

public interface NotificationsDao {

    public Publisher createAndPersistPublisher(final String resName, final Long resId, final String subidentifier, final String type, final String data,
            String businessPath);

    public Subscriber createAndPersistSubscriber(final Publisher persistedPublisher, final Identity listener);

    public List<Subscriber> getSubscriberList(final Identity identity, final String publisherType);

    public List<Subscriber> getAllValidSubscribers();

    public Subscriber getSubscriber(Long key);

    public Publisher getPublisher(final String resName, final Long resId, final String subidentifier);

    public List<Publisher> getPublishers(final String resName, final Long resId);

    public List<Publisher> getAllPublisher();

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

    public void updatePublisher(Publisher publisher);

    public void updateSubscriber(final Subscriber subscriber);

    /**
     * sets the latest visited date of the subscription to 'now' .assumes the identity is already subscribed to the publisher
     * 
     * @param identity
     * @param subsContext
     */
    public void markSubscriberRead(Identity identity, Publisher publisher);

    public Set<Long> markPublisherNews(String resName, Long resId, String subidentifier, Identity ignoreNewsFor);

    public boolean isSubscribed(Identity identity, String resName, Long resId, String subidentifier);

    /**
     * deletes publisher and subscribers
     * 
     * @param publisher
     */
    public void delete(Publisher publisher);

    public void deletePublisher(final Publisher publisher);

    /**
     * delete the publisher and all subscribers to this publisher.
     * 
     * @param publisher
     */
    public void deactivate(Publisher publisher);

    public boolean isPublisherValid(Publisher pub);

    public void deleteSubscriber(final Subscriber subscriber);

    public List<Subscriber> getSubscribers(Identity identity);

    public List<Subscriber> getValidSubscribers(Identity identity);

    public List<Subscriber> getValidSubscribersOf(Publisher publisher);

}
