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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class PowerPointDocument extends FileDocument {

    public final static String FILE_TYPE = "type.file.ppt";

    private static final Logger log = LoggerHelper.getLogger();

    private static final long serialVersionUID = 4569822467064358575L;

    public PowerPointDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final PowerPointDocument powerPointDocument = new PowerPointDocument();
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
        OutputStream oStream = null;
        if (log.isDebugEnabled()) {
            log.debug("read PPT Content of leaf=" + leaf.getName());
        }
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            oStream = new ByteArrayOutputStream();
            extractText(bis, oStream);
            final String content = oStream.toString();
            return removeUnvisibleChars(content);
        } catch (final Exception e) {
            throw new DocumentException("Can not read PPT Content. File=" + leaf.getName());
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (oStream != null) {
                oStream.close();
            }
        }
    }

    /**
     * Remove unvisible chars form input string.
     * 
     * @param inputString
     * @return Return filtered string
     */
    private static String removeUnvisibleChars(final String inputString) {
        final Pattern p = Pattern.compile("[^a-zA-Z0-9\n\r!&#<>{}]");
        final Matcher m = p.matcher(inputString);
        final String output = m.replaceAll(" ");
        return output;
    }

    private static void extractText(final InputStream inStream, final OutputStream stream) throws IOException {
        final POIFSReader r = new POIFSReader();
        /* Register a listener for *all* documents. */
        r.registerListener(new MyPOIFSReaderListener(stream));
        r.read(inStream);
    }

    private static class MyPOIFSReaderListener implements POIFSReaderListener {

        private final OutputStream oStream;

        public MyPOIFSReaderListener(final OutputStream oStream) {
            this.oStream = oStream;
        }

        @Override
        public void processPOIFSReaderEvent(final POIFSReaderEvent event) {
            int errorCounter = 0;

            try {
                DocumentInputStream dis = null;
                dis = event.getStream();

                final byte btoWrite[] = new byte[dis.available()];
                dis.read(btoWrite, 0, dis.available());
                for (int i = 0; i < btoWrite.length - 20; i++) {
                    final long type = LittleEndian.getUShort(btoWrite, i + 2);
                    final long size = LittleEndian.getUInt(btoWrite, i + 4);
                    if (type == 4008) {
                        try {
                            oStream.write(btoWrite, i + 4 + 1, (int) size + 3);
                        } catch (final IndexOutOfBoundsException ex) {
                            errorCounter++;
                        }
                    }
                }
            } catch (final Exception ex) {
                // FIXME:chg: Remove general Exception later, for now make it run
                log.warn("Can not read PPT content.", ex);
            }
            if (errorCounter > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not parse ppt properly. There were " + errorCounter + " IndexOutOfBoundsException");
                }
            }
        }
    }

}
