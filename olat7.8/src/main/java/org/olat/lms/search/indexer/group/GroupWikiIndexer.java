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

package org.olat.lms.search.indexer.group;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.WikiPageDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.group.run.BusinessGroupMainRunController;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index all group folders.
 * 
 * @author Christian Guretzki
 */
public class GroupWikiIndexer extends SubLevelIndexer<BusinessGroup> {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private static final String TYPE = "type.group.wiki";

    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final BusinessGroup businessGroup, final OlatFullIndexer indexWriter) throws IOException {

        // Index Group Wiki
        if (log.isDebugEnabled()) {
            log.debug("Analyse Wiki for Group=" + businessGroup);
        }
        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
        if (collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI)) {
            try {
                final Wiki wiki = WikiManager.getInstance().getOrLoadWiki(businessGroup);
                // loop over all wiki pages
                final List<WikiPage> wikiPageList = wiki.getAllPagesWithContent();
                for (final WikiPage wikiPage : wikiPageList) {
                    final SearchResourceContext wikiResourceContext = new SearchResourceContext(parentResourceContext);
                    wikiResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLWIKI);
                    wikiResourceContext.setDocumentType(TYPE);
                    wikiResourceContext.setDocumentContext(businessGroup.getKey() + " ");
                    wikiResourceContext.setFilePath(wikiPage.getPageName());

                    final Document document = WikiPageDocument.createDocument(wikiResourceContext, wikiPage);
                    indexWriter.addDocument(document);
                }
            } catch (final NullPointerException nex) {
                log.warn("NullPointerException in GroupWikiIndexer.doIndex.", nex);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Group=" + businessGroup + " has no Wiki.");
            }
        }
    }

    @Override
    public boolean checkAccess(final ContextEntry groupContextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // check if tool is enabled
        return CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(groupContextEntry.getOLATResourceable()).isToolEnabled(CollaborationTools.TOOL_WIKI);
    }

    @Override
    public String getSupportedTypeName() {
        return BusinessGroupMainRunController.ORES_TOOLWIKI.getResourceableTypeName();
    }

}
