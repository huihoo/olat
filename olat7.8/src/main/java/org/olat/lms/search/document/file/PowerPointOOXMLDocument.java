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
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

import com.mchange.util.AssertException;

/**
 * Description:<br>
 * Parse the PowerPoint XML document (.pptx) with Apache POI
 * <P>
 * Initial Date: 14 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class PowerPointOOXMLDocument extends FileDocument {

    public static final String FILE_TYPE = "type.file.ppt";

    private static final long serialVersionUID = 341646697073395045L;

    private static final Logger log = LoggerHelper.getLogger();

    public PowerPointOOXMLDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final PowerPointOOXMLDocument powerPointDocument = new PowerPointOOXMLDocument();
        powerPointDocument.init(leafResourceContext, leaf, mimeType);
        powerPointDocument.setFileType(FILE_TYPE);
        powerPointDocument.setCssIcon("b_filetype_ppt");
        if (log.isDebugEnabled()) {
            log.debug(powerPointDocument.toString());
        }
        return powerPointDocument.getLuceneDocument();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return true;
    }

    @Override
    public String readContent(final VFSLeaf leaf) throws IOException, DocumentException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            final POITextExtractor extractor = ExtractorFactory.createExtractor(bis);
            if (extractor instanceof XSLFPowerPointExtractor) {
                // retrieve slide content and notes
                return ((XSLFPowerPointExtractor) extractor).getText(true, true);
            }
            throw new AssertException("Expected XSLFPowerPointExtractor as text extractor.");
        } catch (final Exception e) {
            throw new DocumentException(e.getMessage());
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

}
