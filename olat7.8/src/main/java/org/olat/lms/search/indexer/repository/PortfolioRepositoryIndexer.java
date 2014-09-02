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

package org.olat.lms.search.indexer.repository;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.portfolio.structure.EPStructuredMapTemplate;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.PortfolioMapDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Index templates and only templates in the repository
 * <P>
 * Initial Date: 12 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class PortfolioRepositoryIndexer extends SubLevelIndexer {

    private static final Logger log = LoggerHelper.getLogger();

    public final static String TYPE = "type.repository.entry.ep";
    public final static String ORES_TYPE_EP = "EPStructuredMapTemplate";

    private PortfolioStructureDao structureManager;
    private PortfolioModule portfolioModule;

    /**
     * [used by Spring]
     * 
     * @param structureManager
     */
    public void setStructureManager(final PortfolioStructureDao structureManager) {
        this.structureManager = structureManager;
    }

    /**
     * [used by Spring]
     * 
     * @param portfolioModule
     */
    public void setPortfolioModule(final PortfolioModule portfolioModule) {
        this.portfolioModule = portfolioModule;
    }

    @Override
    public String getSupportedTypeName() {
        return ORES_TYPE_EP;
    }

    @Override
    public void doIndex(final SearchResourceContext resourceContext, final Object object, final OlatFullIndexer indexWriter) throws IOException {
        if (!portfolioModule.isEnabled())
            return;
        if (log.isDebugEnabled()) {
            log.debug("Index portfolio templates...");
        }

        final RepositoryEntry repositoryEntry = (RepositoryEntry) object;
        final OLATResource ores = repositoryEntry.getOlatResource();
        final PortfolioStructure element = structureManager.loadPortfolioStructure(ores);
        // only index templates
        if (element instanceof EPStructuredMapTemplate) {
            resourceContext.setDocumentType(TYPE);
            resourceContext.setDocumentContext(Long.toString(repositoryEntry.getKey()));
            resourceContext.setParentContextType(TYPE);
            resourceContext.setParentContextName(repositoryEntry.getDisplayname());
            resourceContext.setFilePath(element.getKey().toString());

            final Document document = PortfolioMapDocument.createDocument(resourceContext, element);
            indexWriter.addDocument(document);
        }
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // check for accessing RepoEntry is done in RepositoryIndexerMultiThreaded
        return true;
    }
}
