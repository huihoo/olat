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
package org.olat.presentation.webfeed.podcast;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.impl.AbstractPublisherTypeHandler;
import org.olat.lms.core.notification.service.ContextInfo;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.webfeed.Item;
import org.olat.system.commons.CodeHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 30.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class PodcastNotificationTypeHandler extends AbstractPublisherTypeHandler {

    public final static String PODCAST_SOURCE_TYPE = "PODCAST";

    private final static String PODCAST_PUBLISHER_DATA_TYPE = PodcastUIFactory.class.getSimpleName();

    public PodcastNotificationTypeHandler() {
        super(PODCAST_SOURCE_TYPE, PODCAST_PUBLISHER_DATA_TYPE);
    }

    @Override
    public String getSourceEntryPath(String sourceEntryId) {
        return "/Podcast/" + sourceEntryId;
    }

    public PublishEventTO createPublishEventTO(SubscriptionContext subsContext, Long podcastResourceableId, Identity identity, Item currentItem, EventType eventType) {
        if (EventType.NO_PUBLISH.equals(eventType) || subsContext == null) {
            return PublishEventTO.getNoPublishInstance();
        }
        ContextInfo contextInfo = notificationSubscriptionContextFactory.createContextInfoFrom(subsContext);
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(contextInfo.getContextType(), contextInfo.contextId(), subsContext.getContextTitle(),
                contextInfo.subContextId(), PODCAST_SOURCE_TYPE, podcastResourceableId, subsContext.getSourceTitle(), currentItem.getTitle(), identity, eventType);
        String podcastEntryId = CodeHelper.getForeverUniqueIDFromGlobalForeverUniqueID(currentItem.getGuid());
        publishEventTO.setSourceEntryId(podcastEntryId);
        return publishEventTO;
    }

}
