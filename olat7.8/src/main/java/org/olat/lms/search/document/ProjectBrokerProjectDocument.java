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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class ProjectBrokerProjectDocument extends OlatDocument {

    private static final Logger log = LoggerHelper.getLogger();

    public static Document createDocument(final SearchResourceContext searchResourceContext, final Project project) {
        final ProjectBrokerProjectDocument projectDocument = new ProjectBrokerProjectDocument();

        projectDocument.setTitle(project.getTitle());
        final String projectDescription = FilterFactory.getHtmlTagsFilter().filter(project.getDescription());
        projectDocument.setContent(projectDescription);
        final StringBuilder projectLeaderString = new StringBuilder();
        for (final Iterator<Identity> iterator = project.getProjectLeaders().iterator(); iterator.hasNext();) {
            projectLeaderString.append(iterator.next().getName());
            projectLeaderString.append(" ");
        }
        projectDocument.setAuthor(projectLeaderString.toString());
        projectDocument.setCreatedDate(project.getCreationDate());
        projectDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        projectDocument.setDocumentType(searchResourceContext.getDocumentType());
        projectDocument.setCssIcon("o_projectbroker_icon");
        projectDocument.setParentContextType(searchResourceContext.getParentContextType());
        projectDocument.setParentContextName(searchResourceContext.getParentContextName());

        if (log.isDebugEnabled()) {
            log.debug(projectDocument.toString());
        }
        return projectDocument.getLuceneDocument();
    }
}
