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
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class PowerPointDocument extends FileDocument {
    private static final Logger log = LoggerHelper.getLogger();

    public final static String FILE_TYPE = "type.file.ppt";

    public PowerPointDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf) throws IOException, DocumentException,
            DocumentAccessException {
        final PowerPointDocument powerPointDocument = new PowerPointDocument();
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
        OutputStream oStream = null;
        if (log.isDebugEnabled()) {
            log.debug("read PPT Content of leaf=" + leaf.getName());
        }
        try {
            bis = new BufferedInputStream(leaf.getInputStream());
            oStream = new ByteArrayOutputStream();
            PPT2Text.extractText(bis, oStream);
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
    private String removeUnvisibleChars(final String inputString) {
        final Pattern p = Pattern.compile("[^a-zA-Z0-9\n\r!&#<>{}]");
        final Matcher m = p.matcher(inputString);
        final String output = m.replaceAll(" ");
        return output;
    }

}
