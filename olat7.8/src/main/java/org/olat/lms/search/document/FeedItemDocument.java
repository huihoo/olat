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
package org.olat.lms.search.document;

import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.webfeed.Item;

/**
 * OlatDocument holding feed item information for search indexing.
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedItemDocument extends OlatDocument {

    public FeedItemDocument(final Item item, final SearchResourceContext searchResourceContext, final Filter mediaUrlFilter) {
        super();
        setTitle(item.getTitle());
        setAuthor(item.getAuthor());
        // description is rich text, no need to index the HTML tags as well
        setDescription(mediaUrlFilter.filter(FilterFactory.unescapeAndFilterHtml(item.getDescription())));
        setContent(mediaUrlFilter.filter(item.getContent()));
        setLastChange(item.getLastModified());
        setResourceUrl(searchResourceContext.getResourceUrl());
        setDocumentType(searchResourceContext.getDocumentType());
        setParentContextType(searchResourceContext.getParentContextType());
        setParentContextName(searchResourceContext.getParentContextName());
        if (getDocumentType().equals("type.repository.entry.FileResource.PODCAST") || getDocumentType().equals("type.course.node.podcast")) {
            setCssIcon("o_podcast_icon");
        } else if ((getDocumentType().equals("type.repository.entry.FileResource.BLOG")) || getDocumentType().equals("type.course.node.blog")) {
            setCssIcon("o_blog_icon");
        }
    }
}
