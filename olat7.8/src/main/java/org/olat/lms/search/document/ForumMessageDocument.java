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
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.forum.Message;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class ForumMessageDocument extends OlatDocument {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    public final static String TYPE = "type.forum.message";

    private static final Logger log = LoggerHelper.getLogger();

    public static Document createDocument(final SearchResourceContext searchResourceContext, final Message message) {
        final ForumMessageDocument forumMessageDocument = new ForumMessageDocument();

        forumMessageDocument.setTitle(message.getTitle());
        final String msgContent = FilterFactory.getHtmlTagAndDescapingFilter().filter(message.getBody());
        forumMessageDocument.setContent(msgContent);
        forumMessageDocument.setAuthor(message.getCreator().getName());
        forumMessageDocument.setCreatedDate(message.getCreationDate());
        forumMessageDocument.setLastChange(message.getLastModified());
        forumMessageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        if ((searchResourceContext.getDocumentType() != null) && !searchResourceContext.getDocumentType().equals("")) {
            // Document is already set => take this value
            forumMessageDocument.setDocumentType(searchResourceContext.getDocumentType());
        } else {
            forumMessageDocument.setDocumentType(TYPE);
        }
        forumMessageDocument.setCssIcon("o_fo_icon");
        forumMessageDocument.setParentContextType(searchResourceContext.getParentContextType());
        forumMessageDocument.setParentContextName(searchResourceContext.getParentContextName());

        // TODO: chg: What is with message attributes ?
        // ?? Identity modifier = message.getModifier();
        // ?? message.getParent();
        // ?? message.getThreadtop();

        if (log.isDebugEnabled()) {
            log.debug(forumMessageDocument.toString());
        }
        return forumMessageDocument.getLuceneDocument();
    }
}
