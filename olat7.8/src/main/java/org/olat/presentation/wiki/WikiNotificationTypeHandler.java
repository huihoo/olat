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
package org.olat.presentation.wiki;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.impl.AbstractPublisherTypeHandler;
import org.olat.lms.core.notification.service.ContextInfo;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.wiki.WikiPage;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 21.03.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class WikiNotificationTypeHandler extends AbstractPublisherTypeHandler {

    public final static String WIKI_SOURCE_TYPE = "WIKI";

    private final static String WIKI_PUBLISHER_DATA_TYPE = WikiPage.class.getSimpleName();

    public WikiNotificationTypeHandler() {
        super(WIKI_SOURCE_TYPE, WIKI_PUBLISHER_DATA_TYPE);
    }

    // TODO after Review: for CG: it needs a test
    @Override
    public String getSourceEntryPath(String sourceEntryId) {
        return "/path%3D" + sourceEntryId + "/0";
    }

    public PublishEventTO createPublishEventTO(SubscriptionContext subsContext, Long resourceableId, Identity identity, WikiPage page, EventType eventType) {
        if (EventType.NO_PUBLISH.equals(eventType) || subsContext == null) {
            return PublishEventTO.getNoPublishInstance();
        }
        ContextInfo contextInfo = notificationSubscriptionContextFactory.createContextInfoFrom(subsContext);
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(contextInfo.getContextType(), contextInfo.contextId(), subsContext.getContextTitle(),
                contextInfo.subContextId(), WikiNotificationTypeHandler.WIKI_SOURCE_TYPE, resourceableId, subsContext.getSourceTitle(), page.getPageName(), identity,
                eventType);
        publishEventTO.setSourceEntryId(page.getPageName());
        return publishEventTO;
    }

}
