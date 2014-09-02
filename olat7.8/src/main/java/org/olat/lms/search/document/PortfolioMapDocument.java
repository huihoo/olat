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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.EPAbstractMap;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.user.User;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.user.UserService;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Deliver the lucene document made from a portfolio
 * <P>
 * Initial Date: 12 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioMapDocument extends OlatDocument {

    private static final Logger log = LoggerHelper.getLogger();

    private static BaseSecurity securityManager;
    private static EPFrontendManager ePFMgr;
    private static PortfolioAbstractHandler portfolioModule;

    public PortfolioMapDocument() {
        super();
        securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        portfolioModule = (PortfolioAbstractHandler) CoreSpringFactory.getBean("portfolioModule");
    }

    public static Document createDocument(final SearchResourceContext searchResourceContext, final PortfolioStructure map) {
        final PortfolioMapDocument document = new PortfolioMapDocument();
        if (map instanceof EPAbstractMap) {
            final EPAbstractMap abstractMap = (EPAbstractMap) map;
            if (abstractMap.getOwnerGroup() != null) {
                final List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(abstractMap.getOwnerGroup());
                final StringBuilder authors = new StringBuilder();
                for (final Identity identity : identities) {
                    if (authors.length() > 0) {
                        authors.append(", ");
                    }
                    final User user = identity.getUser();
                    authors.append(getUserService().getFirstAndLastname(user));
                }
                document.setAuthor(authors.toString());
            }
            document.setCreatedDate(abstractMap.getCreationDate());
        }

        final Filter filter = FilterFactory.getHtmlTagsFilter();

        document.setTitle(map.getTitle());
        // description is rich text
        document.setDescription(FilterFactory.unescapeAndFilterHtml(map.getDescription()));
        final StringBuilder sb = new StringBuilder();
        getContent(map, searchResourceContext, sb, filter);
        document.setContent(sb.toString());
        document.setResourceUrl(searchResourceContext.getResourceUrl());
        document.setDocumentType(searchResourceContext.getDocumentType());
        document.setCssIcon("o_ep_icon");
        document.setParentContextType(searchResourceContext.getParentContextType());
        document.setParentContextName(searchResourceContext.getParentContextName());

        if (log.isDebugEnabled()) {
            log.debug(document.toString());
        }
        return document.getLuceneDocument();
    }

    private static String getContent(final PortfolioStructure map, final SearchResourceContext resourceContext, final StringBuilder sb, final Filter filter) {
        sb.append(' ').append(map.getTitle());
        if (StringHelper.containsNonWhitespace(map.getDescription())) {
            sb.append(' ').append(filter.filter(map.getDescription()));
        }
        for (final PortfolioStructure child : ePFMgr.loadStructureChildren(map)) {
            getContent(child, resourceContext, sb, filter);
        }
        for (final AbstractArtefact artefact : ePFMgr.getArtefacts(map)) {
            final String reflexion = artefact.getReflexion();
            if (StringHelper.containsNonWhitespace(reflexion)) {
                sb.append(' ').append(filter.filter(reflexion));
            }

            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(AbstractArtefact.class.getSimpleName(), artefact.getKey());

            final SearchResourceContext artefactResourceContext = new SearchResourceContext(resourceContext);
            artefactResourceContext.setBusinessControlFor(ores);

            String artefactContent = ePFMgr.getArtefactFullTextContent(artefact);
            if (StringHelper.containsNonWhitespace(artefactContent)) {
                String unescapedContent = FilterFactory.unescapeAndFilterHtml(artefactContent);
                // System.out.println("PorfolioMapDocument - unescapedContent : " + unescapedContent);
                sb.append(' ').append(unescapedContent);
            }
        }
        return sb.toString();
    }

    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
