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
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.xmlbeans.XmlException;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

/**
 * Description:<br>
 * Parse the PowerPoint XML document (.pptx) with Apache POI
 * <P>
 * Initial Date: 14 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class PowerPointOOXMLDocument extends FileDocument {
    private static final Logger log = LoggerHelper.getLogger();

    public final static String FILE_TYPE = "type.file.ppt";

    public PowerPointOOXMLDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf) throws IOException, DocumentException,
            DocumentAccessException {
        final PowerPointOOXMLDocument powerPointDocument = new PowerPointOOXMLDocument();
        powerPointDocument.init(leafResourceContext, leaf);
        powerPointDocument.setFileType(FILE_TYPE);
        powerPointDocument.setCssIcon("b_filetype_ppt");
        if (log.isDebugEnabled()) {
            log.debug(powerPointDocument.toString());
        }
        return powerPointDocument.getLuceneDocument();
    }

    @Override
    public String readContent(final VFSLeaf leaf) throws IOException, DocumentException {
        BufferedInputStream bis = null;
        final StringBuilder buffy = new StringBuilder();
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            final POIXMLTextExtractor extractor = (POIXMLTextExtractor) ExtractorFactory.createExtractor(bis);
            final POIXMLDocument document = extractor.getDocument();

            if (document instanceof XSLFSlideShow) {
                final XSLFSlideShow slideShow = (XSLFSlideShow) document;
                final XMLSlideShow xmlSlideShow = new XMLSlideShow(slideShow);
                extractContent(buffy, xmlSlideShow);
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

    private void extractContent(final StringBuilder buffy, final XMLSlideShow xmlSlideShow) throws IOException, XmlException {
        final XSLFSlide[] slides = xmlSlideShow.getSlides();
        for (final XSLFSlide slide : slides) {
            final CTSlide rawSlide = slide._getCTSlide();
            final CTSlideIdListEntry slideId = slide._getCTSlideId();

            final CTNotesSlide notes = xmlSlideShow._getXSLFSlideShow().getNotes(slideId);
            final CTCommentList comments = xmlSlideShow._getXSLFSlideShow().getSlideComments(slideId);

            extractShapeContent(buffy, rawSlide.getCSld().getSpTree());

            if (comments != null) {
                for (final CTComment comment : comments.getCmArray()) {
                    buffy.append(comment.getText()).append(' ');
                }
            }

            if (notes != null) {
                extractShapeContent(buffy, notes.getCSld().getSpTree());
            }
        }
    }

    private void extractShapeContent(final StringBuilder buffy, final CTGroupShape gs) {
        final CTShape[] shapes = gs.getSpArray();
        for (final CTShape shape : shapes) {
            final CTTextBody textBody = shape.getTxBody();
            if (textBody != null) {
                final CTTextParagraph[] paras = textBody.getPArray();
                for (final CTTextParagraph textParagraph : paras) {
                    final CTRegularTextRun[] textRuns = textParagraph.getRArray();
                    for (final CTRegularTextRun textRun : textRuns) {
                        buffy.append(textRun.getT()).append(' ');
                    }
                }
            }
        }
    }
}
