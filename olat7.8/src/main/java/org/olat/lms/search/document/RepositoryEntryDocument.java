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

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.repository.GlossaryRepositoryIndexer;
import org.olat.lms.search.indexer.repository.WikiRepositoryIndexer;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class RepositoryEntryDocument extends OlatDocument {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    public final static String TYPE = "type.repository.entry.";

    private static final Logger log = LoggerHelper.getLogger();

    public RepositoryEntryDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext searchResourceContext, final RepositoryEntry repositoryEntry) {
        final RepositoryEntryDocument repositoryEntryDocument = new RepositoryEntryDocument();

        // Set all known attributes
        repositoryEntryDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        repositoryEntryDocument.setLastChange(repositoryEntry.getLastModified());
        repositoryEntryDocument.setCreatedDate(repositoryEntry.getCreationDate());
        repositoryEntryDocument.setDocumentType(TYPE + repositoryEntry.getOlatResource().getResourceableTypeName());
        repositoryEntryDocument.setCssIcon(getIconCss(repositoryEntryDocument.getDocumentType()));
        repositoryEntryDocument.setTitle(repositoryEntry.getDisplayname());
        // description is rich text
        String desc = FilterFactory.unescapeAndFilterHtml(repositoryEntry.getDescription());
        repositoryEntryDocument.setDescription(desc);

        repositoryEntryDocument.setAuthor(repositoryEntry.getInitialAuthor());

        if (log.isDebugEnabled()) {
            log.debug(repositoryEntryDocument.toString());
        }
        return repositoryEntryDocument.getLuceneDocument();
    }

    private static String getIconCss(final String docType) {
        String icon;
        if (docType.equals("type.repository.entry.CourseModule")) {
            icon = "o_course_icon";
        } else if (docType.toLowerCase().equals(WikiRepositoryIndexer.TYPE)) {
            icon = "o_wiki_icon";
        } else if (docType.equals("type.repository.entry.imscp") || docType.equals("type.repository.entry.FileResource.IMSCP")) {
            icon = "o_cp_icon";
        } else if (docType.equals("type.repository.entry.sharedfolder") || docType.equals("type.repository.entry.FileResource.SHAREDFOLDER")) {
            icon = "o_FileResource-SHAREDFOLDER_icon";
        } else if (docType.toLowerCase().equals(GlossaryRepositoryIndexer.TYPE)) {
            icon = "o_FileResource-GLOSSARY_icon";
        } else if (docType.equals("type.repository.entry.FileResource.SURVEY")) {
            icon = "o_iqsurv_icon";
        } else if (docType.equals("type.repository.entry.FileResource.SCORMCP")) {
            icon = "o_FileResource-SCORMCP_icon";
        } else if (docType.equals("type.repository.entry.FileResource.XLS")) {
            icon = "b_filetype_xls";
        } else if (docType.equals("type.repository.entry.FileResource.DOC")) {
            icon = "b_filetype_doc";
        } else if (docType.equals("type.repository.entry.FileResource.FILE")) {
            icon = "b_filetype_file";
        } else if (docType.equals("type.repository.entry.FileResource.PDF")) {
            icon = "b_filetype_pdf";
        } else if (docType.equals("type.repository.entry.FileResource.PPT")) {
            icon = "b_filetype_ppt";
        } else if (docType.equals("type.repository.entry.FileResource.PODCAST")) {
            icon = "o_podcast_icon";
        } else if (docType.equals("type.repository.entry.FileResource.BLOG")) {
            icon = "o_blog_icon";
        } else if (docType.equals("type.repository.entry.FileResource.TEST")) {
            icon = "o_iqtest_icon";
        } else if (docType.equals("type.repository.entry.FileResource.SURVEY")) {
            icon = "o_iqsurv_icon";
        } else if (docType.equals("type.repository.entry.EPStructuredMapTemplate") || docType.equals("type.repository.entry.ep")) {
            icon = "o_ep_icon";
        } else {
            icon = "o_sp_icon";
            System.out.println("*********** Unkown doc type: " + docType);
        }
        return icon;
    }
}
