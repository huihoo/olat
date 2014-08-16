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

package org.olat.lms.search.indexer.identity;

import java.io.IOException;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioAbstractHandler;
import org.olat.lms.portfolio.artefacthandler.EPArtefactHandler;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.OlatDocument;
import org.olat.lms.search.indexer.AbstractIndexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description:<br>
 * Index artefacts
 * <P>
 * Initial Date: 12 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioArtefactIndexer extends AbstractIndexer {

    public static final String TYPE = "type.identity." + AbstractArtefact.class.getSimpleName();
    public static final String ORES_TYPE = AbstractArtefact.class.getSimpleName();

    private static final int MAX_RESULTS = 500;

    private PortfolioAbstractHandler portfolioModule;
    private EPFrontendManager frontendManager;

    /**
     * [used by Spring]
     * 
     * @param frontendManager
     */
    public void setFrontendManager(final EPFrontendManager frontendManager) {
        this.frontendManager = frontendManager;
    }

    /**
     * [used by Spring]
     * 
     * @param portfolioModule
     */
    public void setPortfolioModule(final PortfolioAbstractHandler portfolioModule) {
        this.portfolioModule = portfolioModule;
    }

    @Override
    public String getSupportedTypeName() {
        return ORES_TYPE;
    }

    @Override
    public void doIndex(final SearchResourceContext searchResourceContext, final Object object, final OlatFullIndexer indexerWriter) throws IOException,
            InterruptedException {
		if(!portfolioModule.isEnabled()) return;
        final Identity identity = (Identity) object;
        final SearchResourceContext resourceContext = new SearchResourceContext();
        resourceContext.setDocumentType(TYPE);
		resourceContext.setParentContextType(null);

        int currentPosition = 0;
        List<AbstractArtefact> artefacts;
        do {
            artefacts = frontendManager.getArtefacts(identity, currentPosition, MAX_RESULTS);
            for (final AbstractArtefact artefact : artefacts) {
                final OLATResourceable ores = OresHelper.createOLATResourceableInstance(AbstractArtefact.class.getSimpleName(), artefact.getKey());
                final EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
                resourceContext.setBusinessControlFor(ores);
                final OlatDocument doc = handler.getIndexerDocument(resourceContext, artefact, frontendManager);
                final Identity author = artefact.getAuthor();
                if (author != null && author.getUser() != null) {
                    doc.setReservedTo(author.getKey().toString());
                }
                if (doc != null) {
                    indexerWriter.addDocument(doc.getLuceneDocument());
                }
            }
            currentPosition += artefacts.size();
        } while (artefacts.size() == MAX_RESULTS);

        super.doIndex(searchResourceContext, object, indexerWriter);
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // reserved, check made by indexer with the RESERVEDTO field
        return true;
    }
}
