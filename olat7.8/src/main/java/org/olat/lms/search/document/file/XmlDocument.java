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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class XmlDocument extends FileDocument {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String FILE_TYPE = "type.file.html";

    public static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    public XmlDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final XmlDocument xmlDocument = new XmlDocument();
        xmlDocument.init(leafResourceContext, leaf, mimeType);
        xmlDocument.setFileType(FILE_TYPE);
        xmlDocument.setCssIcon("b_filetype_xml");
        if (log.isDebugEnabled()) {
            log.debug(xmlDocument.toString());
        }
        return xmlDocument.getLuceneDocument();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return true;
    }

    @Override
    protected String readContent(final VFSLeaf leaf) throws DocumentException {
        final StringBuilder output = new StringBuilder();

        try {
            final XMLStreamReader xmlStreamReader = XML_INPUT_FACTORY.createXMLStreamReader(leaf.getInputStream());
            while (xmlStreamReader.hasNext()) {
                xmlStreamReader.next();
                int eventType = xmlStreamReader.getEventType();
                if (eventType == XMLEvent.CHARACTERS || eventType == XMLEvent.CDATA) {
                    output.append(xmlStreamReader.getText().trim());
                    output.append(" ");
                }
            }
        } catch (XMLStreamException ex) {
            throw new DocumentException("Error parsing XML document '" + leaf + "'", ex);
        }

        return output.toString();
    }
}
