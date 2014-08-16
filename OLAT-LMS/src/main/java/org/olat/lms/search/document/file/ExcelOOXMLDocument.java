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
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    private static final Logger log = LoggerHelper.getLogger();

    public final static String FILE_TYPE = "type.file.excel";

    public ExcelOOXMLDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf) throws IOException, DocumentException,
            DocumentAccessException {
        final ExcelOOXMLDocument excelDocument = new ExcelOOXMLDocument();
        excelDocument.init(leafResourceContext, leaf);
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
        final StringBuilder buffy = new StringBuilder();
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            final POIXMLTextExtractor extractor = (POIXMLTextExtractor) ExtractorFactory.createExtractor(bis);
            final POIXMLDocument document = extractor.getDocument();

            if (document instanceof XSSFWorkbook) {
                final XSSFWorkbook xDocument = (XSSFWorkbook) document;
                extractContent(buffy, xDocument);
            }

            return buffy.toString();
        } catch (final Exception e) {
            throw new DocumentException(e.getMessage());
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    private void extractContent(final StringBuilder buffy, final XSSFWorkbook document) {
        for (int i = 0; i < document.getNumberOfSheets(); i++) {
            final XSSFSheet sheet = document.getSheetAt(i);
            buffy.append(document.getSheetName(i)).append(' ');

            // Header(s), if present
            extractHeaderFooter(buffy, sheet.getFirstHeader());
            extractHeaderFooter(buffy, sheet.getOddHeader());
            extractHeaderFooter(buffy, sheet.getEvenHeader());

            // Rows and cells
            for (final Object rawR : sheet) {
                final Row row = (Row) rawR;
                for (final Iterator<Cell> ri = row.cellIterator(); ri.hasNext();) {
                    final Cell cell = ri.next();

                    if (cell.getCellType() == Cell.CELL_TYPE_FORMULA || cell.getCellType() == Cell.CELL_TYPE_STRING) {
                        buffy.append(cell.getRichStringCellValue().getString()).append(' ');
                    } else {
                        final XSSFCell xc = (XSSFCell) cell;
                        final String rawValue = xc.getRawValue();
                        if (rawValue != null) {
                            buffy.append(rawValue).append(' ');
                        }

                    }

                    // Output the comment in the same cell as the content
                    final Comment comment = cell.getCellComment();
                    if (comment != null) {
                        buffy.append(comment.getString().getString()).append(' ');
                    }
                }
            }

            // Finally footer(s), if present
            extractHeaderFooter(buffy, sheet.getFirstFooter());
            extractHeaderFooter(buffy, sheet.getOddFooter());
            extractHeaderFooter(buffy, sheet.getEvenFooter());
        }
    }

    private void extractHeaderFooter(final StringBuilder buffy, final HeaderFooter hf) {
        final String content = ExcelExtractor._extractHeaderFooter(hf);
        if (content.length() > 0) {
            buffy.append(content).append(' ');
        }
    }
}
