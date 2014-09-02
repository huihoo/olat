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

import java.util.Date;

import org.apache.lucene.document.Document;
import org.olat.data.commons.filter.Filter;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.glossary.GlossaryItemManager;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.search.SearchResourceContext;

/**
 * Initial Date: 17.06.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class GlossaryDocument extends OlatDocument {

    public static Document createDocument(final SearchResourceContext searchResourceContext, final RepositoryEntry repositoryEntry) {
        final GlossaryItemManager gIMgr = GlossaryItemManager.getInstance();
        final VFSContainer glossaryFolder = GlossaryManager.getInstance().getGlossaryRootFolder(repositoryEntry.getOlatResource());
        final VFSLeaf glossaryFile = gIMgr.getGlossaryFile(glossaryFolder);
        if (glossaryFile == null) {
            return null;
        }
        String glossaryContent = gIMgr.getGlossaryContent(glossaryFolder);
        // strip all html tags
        final Filter htmlTagsFilter = FilterFactory.getHtmlTagsFilter();
        glossaryContent = htmlTagsFilter.filter(glossaryContent);

        // create standard olat index document with this data
        final OlatDocument glossaryDocument = new OlatDocument();
        if (repositoryEntry.getInitialAuthor() != null) {
            glossaryDocument.setAuthor(repositoryEntry.getInitialAuthor());
        }
        if (repositoryEntry.getDisplayname() != null) {
            glossaryDocument.setTitle(repositoryEntry.getDisplayname());
        }
        if (repositoryEntry.getDescription() != null) {
            // unescape HTML entities from rich text field input
            glossaryDocument.setDescription(FilterFactory.unescapeAndFilterHtml((repositoryEntry.getDescription())));
        }
        glossaryDocument.setContent(glossaryContent);
        glossaryDocument.setCreatedDate(repositoryEntry.getCreationDate());
        glossaryDocument.setLastChange(new Date(glossaryFile.getLastModified()));
        glossaryDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        glossaryDocument.setDocumentType(searchResourceContext.getDocumentType());
        glossaryDocument.setCssIcon("o_FileResource-GLOSSARY_icon");
        return glossaryDocument.getLuceneDocument();
    }

}
