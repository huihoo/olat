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
package org.olat.lms.core.notification.service;

import org.olat.data.notification.Publisher.ContextType;

/**
 * This is a transfer object describing the publish context including the subContextId. <br/>
 * See <code>Publisher<code/> entity for detailed information. <br/>
 * 
 * Initial Date: 14.03.2012 <br>
 * 
 * @author guretzki
 */
public class ContextInfo {
    private final PublishContext publishContext;
    private final Long subContextId; // e.g. courseNode id

    public ContextInfo(ContextType contextType, Long contextId, Long subContextId) {
        this.publishContext = new PublishContext(contextType, contextId);
        this.subContextId = subContextId;
    }

    public ContextType getContextType() {
        return publishContext.getContextType();
    }

    public Long contextId() {
        return publishContext.getContextId();
    }

    public Long subContextId() {
        return subContextId;
    }
}
