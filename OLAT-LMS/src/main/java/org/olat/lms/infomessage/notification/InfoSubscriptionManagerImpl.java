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

import java.util.Collections;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.infomessage.InfoMessage;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.PublisherData;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Implementation of the subscriptions manager for the messages.
 * <P>
 * Initial Date: 27 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class InfoSubscriptionManagerImpl extends InfoSubscriptionManager {

    @Autowired
    NotificationService notificationsManager;
    @Autowired
    PropertyManager propertyManager;

    private final String PUBLISHER_TYPE = OresHelper.calculateTypeName(InfoMessage.class);

    /**
     * [used by Spring]
     */
    private InfoSubscriptionManagerImpl() {
        //
    }

    @Override
    public SubscriptionContext getInfoSubscriptionContext(final OLATResourceable resource, final String subPath) {
        final String resName = resource.getResourceableTypeName();
        final Long resId = resource.getResourceableId();
        return new SubscriptionContext(resName, resId, subPath);
    }

    @Override
    public PublisherData getInfoPublisherData(final OLATResourceable resource, final String businessPath) {
        final String resId = resource.getResourceableId() == null ? "0" : resource.getResourceableId().toString();
        final PublisherData publisherData = new PublisherData(PUBLISHER_TYPE, resId, businessPath);
        return publisherData;
    }

    @Override
    public InfoSubscription getInfoSubscription(Identity ident) {
        return new InfoSubscription(propertyManager, ident);
    }

    @Override
    public Subscriber getInfoSubscriber(final Identity identity, final OLATResourceable resource, final String subPath) {
        final SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
        final Publisher publisher = notificationsManager.getPublisher(context);
        if (publisher == null) {
            return null;
        }
        return notificationsManager.getSubscriber(identity, publisher);
    }

    @Override
    public List<Identity> getInfoSubscribers(final OLATResourceable resource, final String subPath) {
        final SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
        final Publisher publisher = notificationsManager.getPublisher(context);
        if (publisher == null) {
            return Collections.emptyList();
        }

        return notificationsManager.getSubscriberIdentities(publisher);
    }

    @Override
    public void subscribe(final OLATResourceable resource, final String resSubPath, final String businessPath, final Identity identity) {
        final PublisherData data = getInfoPublisherData(resource, businessPath);
        final SubscriptionContext context = getInfoSubscriptionContext(resource, resSubPath);
        notificationsManager.subscribe(identity, context, data);
    }

    @Override
    public void unsubscribe(final OLATResourceable resource, final String subPath, final Identity identity) {
        final SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
        notificationsManager.unsubscribe(identity, context);
    }

    @Override
    public void markPublisherNews(final OLATResourceable resource, final String subPath) {
        final SubscriptionContext context = getInfoSubscriptionContext(resource, subPath);
        notificationsManager.markPublisherNews(context, null);
    }
}
