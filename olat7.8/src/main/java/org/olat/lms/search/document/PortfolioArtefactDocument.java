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

import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.user.User;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.user.UserService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 17.06.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class PortfolioArtefactDocument extends OlatDocument {

    public static Document createDocument(final SearchResourceContext searchResourceContext, final AbstractArtefact artefact, final EPFrontendManager ePFManager,
            final String cssIcon) {
        final OlatDocument document = new PortfolioArtefactDocument();

        final Identity author = artefact.getAuthor();
        if (author != null && author.getUser() != null) {
            final User user = author.getUser();
            document.setAuthor(getUserService().getFirstAndLastname(user));
            document.setReservedTo(author.getKey().toString());
        }

        final Filter filter = FilterFactory.getHtmlTagsFilter();

        document.setCreatedDate(artefact.getCreationDate());
        document.setTitle(filter.filter(artefact.getTitle()));
        // description is rich text
        document.setDescription(FilterFactory.unescapeAndFilterHtml(artefact.getDescription()));
        document.setResourceUrl(searchResourceContext.getResourceUrl());
        document.setDocumentType(searchResourceContext.getDocumentType());
        document.setCssIcon(cssIcon);
        document.setParentContextType(searchResourceContext.getParentContextType());
        document.setParentContextName(searchResourceContext.getParentContextName());

        final StringBuilder sb = new StringBuilder();
        if (artefact.getReflexion() != null) {
            sb.append(artefact.getReflexion()).append(' ');
        }

        final String content = ePFManager.getArtefactFullTextContent(artefact);
        if (content != null) {
            sb.append(FilterFactory.unescapeAndFilterHtml(content));
        }
        document.setContent(sb.toString());
        return document.getLuceneDocument();
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
