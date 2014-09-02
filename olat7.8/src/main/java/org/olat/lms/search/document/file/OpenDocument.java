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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.search.document.file;

import java.io.IOException;
import java.io.InputStream;

import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipInputStream;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Description:<br>
 * A simple parser for Open Document files. It take the content and use a SAXParser to collect the text within.
 * <P>
 * Initial Date: 14 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class OpenDocument extends FileDocument {
    private static final Logger log = LoggerHelper.getLogger();

    public final static String TEXT_FILE_TYPE = "type.file.odt";
    public final static String SPEADSHEET_FILE_TYPE = "type.file.ods";
    public final static String PRESENTATION_FILE_TYPE = "type.file.odp";
    public final static String FORMULA_FILE_TYPE = "type.file.odf";
    public final static String GRAPHIC_FILE_TYPE = "type.file.odg";

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final OpenDocument openDocument = new OpenDocument();
        openDocument.init(leafResourceContext, leaf, mimeType);
        if (leaf.getName().toLowerCase().endsWith(".odt")) {
            openDocument.setFileType(TEXT_FILE_TYPE);
            openDocument.setCssIcon("b_filetype_odt");
        } else if (leaf.getName().toLowerCase().endsWith(".ods")) {
            openDocument.setFileType(SPEADSHEET_FILE_TYPE);
            openDocument.setCssIcon("b_filetype_ods");
        } else if (leaf.getName().toLowerCase().endsWith(".odp")) {
            openDocument.setFileType(PRESENTATION_FILE_TYPE);
            openDocument.setCssIcon("b_filetype_odp");
        } else if (leaf.getName().toLowerCase().endsWith(".odg")) {
            openDocument.setFileType(GRAPHIC_FILE_TYPE);
            openDocument.setCssIcon("b_filetype_odg");
        } else if (leaf.getName().toLowerCase().endsWith(".odf")) {
            openDocument.setFileType(FORMULA_FILE_TYPE);
            openDocument.setCssIcon("b_filetype_odf");
        }
        if (log.isDebugEnabled()) {
            log.debug(openDocument.toString());
        }
        return openDocument.getLuceneDocument();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return true;
    }

    @Override
    public String readContent(final VFSLeaf leaf) throws DocumentException {
        final OpenDocumentHandler dh = new OpenDocumentHandler();

        InputStream stream = null;
        ZipInputStream zip = null;
        try {
            stream = leaf.getInputStream();
            zip = new ZipInputStream(stream);
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if (entry.getName().endsWith("content.xml")) {
                    parse(new ShieldInputStream(zip), dh);
                    break;// we parsed only content
                }
                entry = zip.getNextEntry();
            }
        } catch (final DocumentException e) {
            throw e;
        } catch (final Exception e) {
            log.error("", e);
            throw new DocumentException(e.getMessage());
        } finally {
            FileUtils.closeSafely(zip);
            FileUtils.closeSafely(stream);
        }
        return dh.getContent();
    }

    private void parse(final InputStream stream, final DefaultHandler handler) throws DocumentException {
        try {
            final XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(handler);
            parser.setEntityResolver(handler);
            try {
                parser.setFeature("http://xml.org/sax/features/validation", false);
            } catch (final Exception e) {
                e.printStackTrace();
                // cannot desactivate validation
            }
            parser.parse(new InputSource(stream));
        } catch (final Exception e) {
            throw new DocumentException("XML parser configuration error", e);
        }
    }

    private class OpenDocumentHandler extends DefaultHandler {

        private final StringBuilder sb = new StringBuilder();

        public OpenDocumentHandler() {
            //
        }

        public String getContent() {
            return sb.toString();
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) {
            if ("//OpenOffice.org//DTD Modified W3C MathML 1.01//EN".equals(publicId) || systemId.endsWith("math.dtd")) {
                final InputStream dtdStream = OpenDocument.this.getClass().getResourceAsStream("_resources/math.dtd");
                return new InputSource(dtdStream);
            }
            return null;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                sb.append(' ');
            }
            sb.append(ch, start, length);
        }
    }

    private class ShieldInputStream extends InputStream {
        private final ZipInputStream delegate;

        public ShieldInputStream(final ZipInputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() {
            // do nothing
        }

        @Override
        public synchronized void mark(final int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public int read(final byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public long skip(final long n) throws IOException {
            return delegate.skip(n);
        }
    }
}
