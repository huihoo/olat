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
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.poi.extractor.ExtractorFactory;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Parse the Excel XML document (.xslx) with Apache POI
 * <P>
 * Initial Date: 14 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ExcelOOXMLDocument extends FileDocument {

    public static final String FILE_TYPE = "type.file.excel";

    private static final long serialVersionUID = 3484347919291654719L;

    private static final Logger log = LoggerHelper.getLogger();

    public ExcelOOXMLDocument() {
        super();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return true;
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final ExcelOOXMLDocument excelDocument = new ExcelOOXMLDocument();
        excelDocument.init(leafResourceContext, leaf, mimeType);
        excelDocument.setFileType(FILE_TYPE);
        excelDocument.setCssIcon("b_filetype_xls");
        if (log.isDebugEnabled()) {
            log.debug(excelDocument.toString());
        }
        return excelDocument.getLuceneDocument();
    }

    @Override
    protected String readContent(final VFSLeaf leaf) throws IOException, DocumentException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            // event based text extraction in POI 3.9 doesn't consider header/footer
            // (see org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor
            // and comments in constructor of FileDocumentFactory)
            // Previous versions of this class had implemented support for header/footer extraction
            // based on the complete memory model which caused performance problems for large files.
            return ExtractorFactory.createExtractor(bis).getText();
        } catch (final Exception e) {
            throw new DocumentException(e.getMessage());
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }
}
