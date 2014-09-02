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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class ExcelDocument extends FileDocument {
    private static final Logger log = LoggerHelper.getLogger();

    public final static String FILE_TYPE = "type.file.excel";

    public ExcelDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final ExcelDocument excelDocument = new ExcelDocument();
        excelDocument.init(leafResourceContext, leaf, mimeType);
        excelDocument.setFileType(FILE_TYPE);
        excelDocument.setCssIcon("b_filetype_xls");
        if (log.isDebugEnabled()) {
            log.debug(excelDocument.toString());
        }
        return excelDocument.getLuceneDocument();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return true;
    }

    @Override
    protected String readContent(final VFSLeaf leaf) throws IOException, DocumentException {
        BufferedInputStream bis = null;
        int cellNullCounter = 0;
        int rowNullCounter = 0;
        int sheetNullCounter = 0;

        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            final StringBuilder content = new StringBuilder(bis.available());
            final POIFSFileSystem fs = new POIFSFileSystem(bis);
            final HSSFWorkbook workbook = new HSSFWorkbook(fs);

            for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
                final HSSFSheet sheet = workbook.getSheetAt(sheetNumber);
                if (sheet != null) {
                    for (int rowNumber = sheet.getFirstRowNum(); rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                        final HSSFRow row = sheet.getRow(rowNumber);
                        if (row != null) {
                            for (int cellNumber = row.getFirstCellNum(); cellNumber <= row.getLastCellNum(); cellNumber++) {
                                final HSSFCell cell = row.getCell(cellNumber);
                                if (cell != null) {
                                    // if (cell.getCellStyle().equals(HSSFCell.CELL_TYPE_NUMERIC))
                                    if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                        content.append(cell.getStringCellValue()).append(' ');
                                    }
                                } else {
                                    // throw new DocumentException();
                                    cellNullCounter++;
                                }
                            }
                        } else {
                            rowNullCounter++;
                        }
                    }
                } else {
                    sheetNullCounter++;
                }
            }
            if (log.isDebugEnabled()) {
                if ((cellNullCounter > 0) || (rowNullCounter > 0) || (sheetNullCounter > 0)) {
                    log.debug("Read Excel content cell=null #:" + cellNullCounter + ", row=null #:" + rowNullCounter + ", sheet=null #:" + sheetNullCounter);
                }
            }
            return content.toString();
        } catch (final Exception ex) {
            throw new DocumentException("Can not read XLS Content. File=" + leaf.getName());
        } finally {
            if (bis != null) {
                bis.close();
            }

        }
    }

}
