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
package org.olat.lms.core.notification;

import org.olat.data.notification.Publisher;

/**
 * This is the abstraction for the existing publisher types.
 * 
 * Initial Date: 21.03.2012 <br>
 * 
 * @author guretzki
 */
public interface PublisherTypeHandler {

    String UNKNOWN = "UNKOWN";

    /**
     * @return the source type.
     */
    public String getSourceType();

    /**
     * @return the business path to the source. (e.g. if the source is a forum <br>
     *         node in a course, than the business path contains the course id and node id in course).
     */
    public String getBusinessPathToSource(Publisher publisher);

    /**
     * @return the identifier of the source entry (e.g. the id of the message that changed in a forum)
     */
    public String getSourceEntryPath(String sourceEntryId);

    /**
     * @return the PublisherData. This is very similar with <code>getSourceType()</code>.
     */
    public String getPublisherDataType();
}
