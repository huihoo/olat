package org.olat.lms.search.document.file;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.system.logging.log4j.LoggerHelper;

public class UnkownDocument extends FileDocument {

    private static final Logger log = LoggerHelper.getLogger();

    public final static String UNKOWN_TYPE = "type.file.unkown";

    public static Document createDocument(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final String mimeType) throws IOException,
            DocumentException {
        final UnkownDocument openDocument = new UnkownDocument();
        openDocument.init(leafResourceContext, leaf, mimeType);
        openDocument.setFileType(UNKOWN_TYPE);
        openDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
        if (log.isDebugEnabled()) {
            log.debug(openDocument.toString());
        }
        return openDocument.getLuceneDocument();
    }

    @Override
    protected boolean documentUsesTextBuffer() {
        return false;
    }

    @Override
    protected String readContent(final VFSLeaf leaf) {
        return "";
    }
}
