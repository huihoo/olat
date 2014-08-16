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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class PdfDocument extends FileDocument {
    private static final Logger log = LoggerHelper.getLogger();

    public final static String FILE_TYPE = "type.file.pdf";

    private final boolean pdfTextBuffering;

    private final String pdfTextBufferPath;

    private String filePath;

    public PdfDocument() {
        super();
        pdfTextBuffering = SearchServiceFactory.getService().getSearchModuleConfig().getPdfTextBuffering();
        pdfTextBufferPath = SearchServiceFactory.getService().getSearchModuleConfig().getPdfTextBufferPath();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf) throws IOException, DocumentException,
            DocumentAccessException {
        final PdfDocument textDocument = new PdfDocument();
        textDocument.setFilePath(getPdfTextTmpFilePath(leafResourceContext));
        textDocument.init(leafResourceContext, leaf);
        textDocument.setFileType(FILE_TYPE);
        textDocument.setCssIcon("b_filetype_pdf");
        if (log.isDebugEnabled()) {
            log.debug(textDocument.toString());
        }
        return textDocument.getLuceneDocument();
    }

    private void setFilePath(final String filePath2) {
        this.filePath = filePath2;
    }

    /**
     * Create a file-path for certain SearchResourceContext. E.g. '04\1601914104anuale_print.pdf'
     */
    private static String getPdfTextTmpFilePath(final SearchResourceContext leafResourceContext) {
        final int hashCode = Math.abs(leafResourceContext.getResourceUrl().hashCode());
        final String hashCodeAsString = Integer.toString(hashCode);
        final String splitDirName = hashCodeAsString.substring(hashCodeAsString.length() - 2);
        final String pdfTextTmpFilePath = splitDirName + File.separator + hashCodeAsString + leafResourceContext.getFilePath();
        if (log.isDebugEnabled()) {
            log.debug("PdfTextTmpFilePath=" + pdfTextTmpFilePath);
        }
        return pdfTextTmpFilePath;
    }

    @Override
    protected String readContent(final VFSLeaf leaf) throws DocumentException, DocumentAccessException {
        try {
            long startTime = 0;
            if (log.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
            }
            String pdfText = null;
            final String fullPdfTextTmpFilePath = pdfTextBufferPath + File.separator + getFilePath() + ".tmp";
            final File pdfTextFile = new File(fullPdfTextTmpFilePath);
            if (pdfTextBuffering && !isNewPdfFile(leaf, pdfTextFile)) {
                // text file with extracted text exist => read pdf text from there
                pdfText = getPdfTextFromBuffer(pdfTextFile);
            } else {
                // no text file with extracted text exist => extract text from pdf
                pdfText = extractTextFromPdf(leaf);
                if (pdfTextBuffering) {
                    // store extracted pdf-text in
                    storePdfTextInBuffer(pdfText, fullPdfTextTmpFilePath, pdfTextFile);
                }
                if (log.isDebugEnabled()) {
                    log.debug("readContent from pdf done.");
                }
            }
            if (log.isDebugEnabled()) {
                final long time = System.currentTimeMillis() - startTime;
                log.debug("readContent time=" + time);
            }
            return pdfText;
        } catch (final DocumentAccessException ex) {
            // pass exception
            throw new DocumentAccessException(ex.getMessage());
        } catch (final Exception ex) {
            throw new DocumentException("Can not read PDF content. File=" + leaf.getName() + ";" + ex.getMessage());
        }
    }

    private void storePdfTextInBuffer(final String pdfText, final String fullPdfTextTmpFilePath, final File pdfTextFile) throws IOException {
        final int lastSlash = fullPdfTextTmpFilePath.lastIndexOf('/');
        final String dirPath = fullPdfTextTmpFilePath.substring(0, lastSlash);
        final File dirFile = new File(dirPath);
        dirFile.mkdirs();
        FileUtils.save(new FileOutputStream(pdfTextFile), pdfText, "utf-8");
    }

    private String extractTextFromPdf(final VFSLeaf leaf) throws IOException, DocumentAccessException {
        if (log.isDebugEnabled()) {
            log.debug("readContent from pdf starts...");
        }
        PDDocument document = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            document = PDDocument.load(bis);
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
            if (bis != null) {
                bis.close();
            }
        }

    }

    private String getPdfTextFromBuffer(final File pdfTextFile) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("readContent from text file start...");
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(pdfTextFile));
            final String pdfText = FileUtils.load(bis, "utf-8");
            if (log.isDebugEnabled()) {
                log.debug("readContent from text file done.");
            }
            return pdfText;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    private String getFilePath() {
        return filePath;
    }

    private boolean isNewPdfFile(final VFSLeaf leaf, final File pdfTextFile) {
        if (pdfTextFile == null) {
            return true;
        }
        if (!pdfTextFile.exists()) {
            return true;
        }
        if (leaf.getLastModified() > pdfTextFile.lastModified()) {
            // pdf file is newer => delete it
            pdfTextFile.delete();
            return true;
        }
        return false;
    }

}
