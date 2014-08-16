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

package org.olat.lms.search.indexer.repository;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.Indexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index a repository entry of type glossary.
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class GlossaryRepositoryIndexer implements Indexer {

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttypes and
    // lucene has problems with '_'
    public static String TYPE = "type.repository.entry.glossary";
    private static final Logger log = LoggerHelper.getLogger();

    public static String ORES_TYPE_GLOSSARY = GlossaryResource.TYPE_NAME;

    public GlossaryRepositoryIndexer() {
        // Repository types

    }

    /**
	 * 
	 */
    @Override
    public String getSupportedTypeName() {
        return ORES_TYPE_GLOSSARY;
    }

    /**
	 */
    @Override
    public void doIndex(final SearchResourceContext resourceContext, final Object parentObject, final OlatFullIndexer indexWriter) throws IOException,
            InterruptedException {
        final RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
        log.debug("Analyse Glosary RepositoryEntry...");
        try {
            resourceContext.setDocumentType(TYPE);
            final Document document = GlossaryManager.getInstance().getIndexerDocument(repositoryEntry, resourceContext);
            if (document != null) {
                indexWriter.addDocument(document);
            }
        } catch (final NullPointerException nex) {
            log.warn("NullPointerException in GlossaryRepositoryIndexer.doIndex.", nex);
        }
    }

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setIndexerList(final List indexerList) {
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        return true;
    }

}
