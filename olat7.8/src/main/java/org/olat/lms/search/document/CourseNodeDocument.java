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

package org.olat.lms.search.document;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class CourseNodeDocument extends OlatDocument {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    public final static String TYPE = "type.course.node";

    private static final Logger log = LoggerHelper.getLogger();

    public static Document createDocument(final SearchResourceContext searchResourceContext, final CourseNode courseNode) {
        final CourseNodeDocument courseNodeDocument = new CourseNodeDocument();

        // Set all know attributes
        courseNodeDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        if (searchResourceContext.getDocumentType() != null && !searchResourceContext.getDocumentType().equals("")) {
            courseNodeDocument.setDocumentType(searchResourceContext.getDocumentType());
        } else {
            courseNodeDocument.setDocumentType(TYPE);
        }
        courseNodeDocument.setCssIcon("o_course_icon");
        courseNodeDocument.setTitle(courseNode.getShortTitle());
        courseNodeDocument.setDescription(courseNode.getLongTitle());
        // unescape HTML entities from rich text field input
        courseNodeDocument.setContent(StringHelper.unescapeHtml(courseNode.getLearningObjectives()));
        // Get dates from parent object via context because course node has no dates
        courseNodeDocument.setCreatedDate(searchResourceContext.getCreatedDate());
        courseNodeDocument.setLastChange(searchResourceContext.getLastModified());
        courseNodeDocument.setParentContextType(searchResourceContext.getParentContextType());
        courseNodeDocument.setParentContextName(searchResourceContext.getParentContextName());
        // unused course-node attributtes
        // courseNode.getShortName();
        // courseNode.getType();

        if (log.isDebugEnabled()) {
            log.debug(courseNodeDocument.toString());
        }
        return courseNodeDocument.getLuceneDocument();
    }

}
