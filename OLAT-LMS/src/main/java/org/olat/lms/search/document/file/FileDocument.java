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

package org.olat.lms.search.document.file;

import java.io.IOException;
import java.util.Date;

import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SimpleDublinCoreMetadataFieldsProvider;
import org.olat.lms.search.document.OlatDocument;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public abstract class FileDocument extends OlatDocument {

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_'
    public final static String TYPE = "type.file";

    public FileDocument() {
        super();
    }

    protected void init(final SearchResourceContext leafResourceContext, final VFSLeaf leaf) throws IOException, DocumentException, DocumentAccessException {
        // Load metadata for this file
        MetaInfo meta = null;
        if (leaf instanceof OlatRelPathImpl) {
            FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
            meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) leaf);
        }

        // Set all know attributes
        this.setResourceUrl(leafResourceContext.getResourceUrl());
        this.setLastChange(new Date(leaf.getLastModified()));
        // Check if there are documents attributes set in resource context
        if (leafResourceContext.getDocumentType() != null && !leafResourceContext.getDocumentType().equals("")) {
            // document-type in context is set => get from there
            this.setDocumentType(leafResourceContext.getDocumentType());
        } else {
            this.setDocumentType(TYPE);
        }
        String metaTitle = (meta == null ? null : meta.getTitle());
        if (!StringHelper.containsNonWhitespace(metaTitle)) {
            metaTitle = null;
        }
        if (leafResourceContext.getTitle() != null && !leafResourceContext.getTitle().equals("")) {
            // Title in context is set => get from there and add filename
            this.setTitle(leafResourceContext.getTitle() + " , " + (metaTitle == null ? "" : (metaTitle + " ( ")) + leaf.getName() + (metaTitle == null ? "" : " )"));
        } else {
            this.setTitle((metaTitle == null ? "" : (metaTitle + " ( ")) + leaf.getName() + (metaTitle == null ? "" : " )"));
        }
        final String metaDesc = (meta == null ? null : meta.getComment());
        if (leafResourceContext.getDescription() != null && !leafResourceContext.getDescription().equals("")) {
            // Title in context is set => get from there
            this.setDescription(leafResourceContext.getDescription() + (metaDesc == null ? "" : " " + metaDesc));
        } else {
            // no description this.setDescription();
            if (metaDesc != null) {
                this.setDescription(metaDesc);
            }
        }
        this.setParentContextType(leafResourceContext.getParentContextType());
        this.setParentContextName(leafResourceContext.getParentContextName());
        // Add the content itself
        this.setContent(readContent(leaf));

        // Add other metadata from meta info
        if (meta != null) {
            addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_DESCRIPTION, meta.getComment());
            addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_LANGUAGE, meta.getLanguage());
            // Date is 2009 200902 or 20090228
            final String[] pubDateArray = meta.getPublicationDate();
            if (pubDateArray != null) {
                String pubDate = null;
                if (pubDateArray.length == 1) {
                    pubDate = meta.getPublicationDate()[0];
                }
                if (pubDateArray.length == 2) {
                    pubDate = meta.getPublicationDate()[0] + meta.getPublicationDate()[1];
                }
                if (pubDateArray.length == 3) {
                    pubDate = meta.getPublicationDate()[0] + meta.getPublicationDate()[1] + meta.getPublicationDate()[2];
                }
                addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_DATE, pubDate);
            }
            addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_PUBLISHER, meta.getPublisher());
            addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_SOURCE, meta.getSource());
            addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_SOURCE, meta.getUrl());
            // use creator and author as olat author
            setAuthor((meta.getCreator() == null ? meta.getAuthor() : meta.getAuthor() + " " + meta.getCreator()));
            addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_CREATOR, meta.getCreator());
        }
        // Add file type
        final String mimeType = WebappHelper.getMimeType(leaf.getName());
        addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_FORMAT, mimeType);

    }

    abstract protected String readContent(VFSLeaf leaf) throws IOException, DocumentException, DocumentAccessException;

}
