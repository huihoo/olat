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
package org.olat.lms.search.indexer.repository.course;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.PortfolioCourseNode;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.PortfolioMapDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;

/**
 * Description:<br>
 * Index template of a course node
 * <P>
 * Initial Date: 12 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNodeIndexer extends CourseNodeIndexer {

    private final static String NODE_TYPE = "type.course.node.ep";
    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.PortfolioCourseNode";

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
    public void setPortfolioModule(PortfolioModule portfolioModule) {
        this.portfolioModule = portfolioModule;
    }

    @Override
    public String getDocumentTypeName() {
        return NODE_TYPE;
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public void doIndex(final SearchResourceContext searchResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException {
        if (!portfolioModule.isEnabled())
            return;
        final PortfolioCourseNode portfolioNode = (PortfolioCourseNode) courseNode;
        final RepositoryEntry repoEntry = portfolioNode.getReferencedRepositoryEntry();
        if (repoEntry != null) {
            final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(searchResourceContext);
            courseNodeResourceContext.setBusinessControlFor(courseNode);
            courseNodeResourceContext.setDocumentType(NODE_TYPE);
            courseNodeResourceContext.setDocumentContext(course.getResourceableId() + " " + courseNode.getIdent());
            final OLATResource ores = repoEntry.getOlatResource();
            final PortfolioStructure element = structureManager.loadPortfolioStructure(ores);
            final Document document = PortfolioMapDocument.createDocument(courseNodeResourceContext, element);
            indexWriter.addDocument(document);
        }
    }

    @Override
    public boolean checkAccess(ContextEntry courseNodeContextEntry, BusinessControl businessControl, Identity identity, Roles roles, boolean isCourseOwner) {
        return true;
    }
}
