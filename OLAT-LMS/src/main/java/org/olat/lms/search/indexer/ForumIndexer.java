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

package org.olat.lms.search.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.lms.forum.ForumService;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.ForumMessageDocument;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Common forum indexer. Index all Messages of a forum.
 * 
 * @author Christian Guretzki
 */
public abstract class ForumIndexer extends AbstractIndexer {

    public ForumIndexer() {
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    public void doIndexAllMessages(final SearchResourceContext parentResourceContext, final Forum forum, final OlatFullIndexer indexWriter) throws IOException,
            InterruptedException {
        // loop over all messages of a forum
        final List<Message> messages = getForumService().getMessagesByForum(forum);
        for (final Message message : messages) {
            final SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
            searchResourceContext.setBusinessControlFor(message);
            searchResourceContext.setDocumentContext(parentResourceContext.getDocumentContext() + " " + message.getKey());
            final Document document = ForumMessageDocument.createDocument(searchResourceContext, message);
            indexWriter.addDocument(document);
        }
    }

}
