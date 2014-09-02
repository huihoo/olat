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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.data.notification.Publisher.ContextType;

/**
 * This is a transfer object describing the publish context including the subContextId and the event source information. <br/>
 * See <code>Publisher<code/> entity for detailed information. <br/>
 * 
 * Initial Date: 06.02.2012 <br>
 * 
 * @author lavinia
 */
public class PublisherTO {

    private final ContextInfo contextInfo;

    private final String sourceType; // e.g. FORUM
    private final Long sourceId; // e.g. resourceId

    /**
     * @param contextType
     * @param contextId
     * @param subcontextId
     * @param sourceType
     * @param sourceId
     */
    private PublisherTO(ContextType contextType, Long contextId, Long subcontextId, String sourceType, Long sourceId) {

        this.contextInfo = new ContextInfo(contextType, contextId, subcontextId);

        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    public static PublisherTO getValidInstance(ContextType contextType, Long contextId, Long subcontextId, String sourceType, Long sourceId) {
        PublisherTO publisher = new PublisherTO(contextType, contextId, subcontextId, sourceType, sourceId);
        publisher.validate();
        return publisher;
    }

    /**
     * Factory method for PublisherTO objects, which don't have sourceId.
     */
    public static PublisherTO createNewPublisherTOInCourse(Long contextId, Long subcontextId) {
        if (contextId == null || subcontextId == null) {
            throw new IllegalArgumentException("no valid argument at call - createNewForumInCoursePublisherTO");
        }
        return new PublisherTO(ContextType.COURSE, contextId, subcontextId, null, Long.valueOf(0));
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getContextId() {
        return contextInfo.contextId();
    }

    public Long getSourceId() {
        return sourceId;
    }

    public ContextType getContextType() {
        return contextInfo.getContextType();
    }

    public Long getSubcontextId() {
        return contextInfo.subContextId();
    }

    private void validate() {
        if (contextInfo.getContextType() == null)
            throw new IllegalArgumentException("Context type is null.");

        // We have only course context
        // if (!Publisher.ContextType.COURSE.equals(publishContext.getContextType()))
        // throw new IllegalArgumentException("Inappropriate context type: " + publishContext.getContextType() + " instead of " + Publisher.ContextType.COURSE);

        if (contextInfo.contextId() == null)
            throw new IllegalArgumentException("Context ID is null.");

        if (sourceType == null)
            throw new IllegalArgumentException("Source type is null.");

        if ("UNKNOWN".equals(getSourceType()))
            throw new IllegalArgumentException("Source type is UNKNOWN.");

        if (sourceId == null)
            throw new IllegalArgumentException("Source ID is null.");

    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);

        builder.append("sourceType", getSourceType());
        builder.append("sourceId", getSourceId());
        builder.append("contextType", getContextType());
        builder.append("contextId", getContextId());
        return builder.toString();
    }

}
