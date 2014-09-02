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
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.PDFTextStripper;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class PdfDocument extends FileDocument {

    public final static String FILE_TYPE = "type.file.pdf";

    private static final Logger log = LoggerHelper.getLogger();

    private static final long serialVersionUID = -1891302496477591652L;

    private PdfDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final PdfDocument textDocument = new PdfDocument();
        textDocument.init(leafResourceContext, leaf, mimeType);
        textDocument.setFileType(FILE_TYPE);
        textDocument.setCssIcon("b_filetype_pdf");

        if (log.isDebugEnabled()) {
            log.debug(textDocument.toString());
        }
        return textDocument.getLuceneDocument();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return true;
    }

    @Override
    protected String readContent(final VFSLeaf leaf) throws DocumentException {
        try {
            long startTime = 0;
            if (log.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
            }
            final String pdfText = extractText(leaf);

            if (log.isDebugEnabled()) {
                final long time = System.currentTimeMillis() - startTime;
                log.debug("readContent time=" + time);
            }
            return pdfText;
        } catch (final Exception ex) {
            throw new DocumentException("Can not read PDF content. File=" + leaf.getName() + ";" + ex.getMessage());
        }
    }

    private String extractText(final VFSLeaf leaf) throws IOException, DocumentAccessException {
        if (log.isDebugEnabled()) {
            log.debug("readContent from pdf starts...");
        }
        PDDocument document = null;
        InputStream is = null;
        try {
            is = leaf.getInputStream();
            document = PDDocument.load(is);
            if (document.isEncrypted()) {
                try {
                    document.decrypt("");
                } catch (final Exception e) {
                    throw new DocumentAccessException("PDF is encrypted. Can not read content file=" + leaf.getName());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("readContent PDDocument loaded");
            }
            final PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } finally {
            if (document != null) {
                document.close();
            }
            if (is != null) {
                is.close();
            }
            // needed to prevent potential OutOfMemoryError
            // https://issues.apache.org/jira/browse/PDFBOX-1009
            PDFont.clearResources();
        }
    }

}
