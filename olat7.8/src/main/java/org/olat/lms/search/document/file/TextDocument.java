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
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class TextDocument extends FileDocument {

    private static final Logger log = LoggerHelper.getLogger();

    public final static String FILE_TYPE = "type.file.text";

    public TextDocument() {
        super();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return false;
    }

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final TextDocument textDocument = new TextDocument();
        textDocument.init(leafResourceContext, leaf, mimeType);
        textDocument.setFileType(FILE_TYPE);
        textDocument.setCssIcon("b_filetype_txt");
        if (log.isDebugEnabled()) {
            log.debug(textDocument.toString());
        }
        return textDocument.getLuceneDocument();
    }

    @Override
    protected String readContent(final VFSLeaf leaf) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(leaf.getInputStream());
        final String content = FileUtils.load(bis, "utf-8");
        bis.close();
        return content;
    }
}
