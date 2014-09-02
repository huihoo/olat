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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.SimpleDublinCoreMetadataFieldsProvider;
import org.olat.lms.search.document.OlatDocument;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public abstract class FileDocument extends OlatDocument {

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and Lucene has problems with '_'
    public final static String TYPE = "type.file";

    private static final Logger log = LoggerHelper.getLogger();

    private static final boolean textBufferEnabled = SearchServiceFactory.getService().getSearchModuleConfig().isTextBufferEnabled();

    private static final String textBufferPath = SearchServiceFactory.getService().getSearchModuleConfig().getTextBufferPath();

    private File bufferFile;

    private long fileLastModified;

    protected FileDocument() {
        super();
    }

    protected void init(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException, DocumentException {
        // Load metadata for this file
        MetaInfo meta = null;
        if (leaf instanceof OlatRelPathImpl) {
            FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
            meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) leaf);
        }

        // Set all know attributes
        this.setResourceUrl(leafResourceContext.getResourceUrl());
        fileLastModified = leaf.getLastModified();
        this.setLastChange(new Date(fileLastModified));
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
        if (textBufferEnabled && documentUsesTextBuffer()) {
            bufferFile = getTextBufferFile(leafResourceContext);
            if (isBufferFileOutdated(leaf)) {
                if (log.isDebugEnabled()) {
                    log.debug("Read '" + leaf.getPath() + "' from filesystem.");
                }
                final String bufferText = readContent(leaf);
                writeTextToBuffer(bufferText);
                this.setContent(bufferText);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Read '" + leaf.getPath() + "' from text buffer.");
                }
                this.setContent(readTextFromBuffer());
            }
        } else {
            this.setContent(readContent(leaf));
        }

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
        addMetadata(SimpleDublinCoreMetadataFieldsProvider.DC_FORMAT, mimeType);
    }

    abstract protected boolean documentUsesTextBuffer();

    abstract protected String readContent(VFSLeaf leaf) throws IOException, DocumentException;

    /**
     * Create path for a text buffer file (e.g. '04/1601914104/anuale_print.pdf.tmp')
     */
    private static File getTextBufferFile(final SearchResourceContext leafResourceContext) {
        final String resourceHashCode = Integer.toString(Math.abs(leafResourceContext.getResourceUrl().hashCode()));
        // to avoid breaking some file system limitations we scatter text buffer files over several dirs (defined by last 2 digits of hash)
        final String splitDirName = resourceHashCode.substring(resourceHashCode.length() - 2);
        final String pdfTextTmpFilePath = textBufferPath + File.separator + splitDirName + File.separator + resourceHashCode + leafResourceContext.getFilePath() + ".tmp";
        return new File(pdfTextTmpFilePath);
    }

    private String readTextFromBuffer() {
        return FileUtils.load(bufferFile, "utf-8");
    }

    private void writeTextToBuffer(final String text) {
        bufferFile.getParentFile().mkdirs();
        FileUtils.save(bufferFile, text, "utf-8");
    }

    private boolean isBufferFileOutdated(final VFSLeaf leaf) {
        // returns 0 if file does not exist
        long bufferLastModified = bufferFile.lastModified();
        if (bufferLastModified == 0 || fileLastModified > bufferLastModified) {
            return true;
        }
        return false;
    }

}
