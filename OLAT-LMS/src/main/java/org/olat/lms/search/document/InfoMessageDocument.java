package org.olat.lms.search.document;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.infomessage.InfoMessage;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.logging.log4j.LoggerHelper;

public class InfoMessageDocument extends OlatDocument {
    private static final Logger log = LoggerHelper.getLogger();

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_'
    public final static String TYPE = "type.info.message";

    public InfoMessageDocument() {
        super();
    }

    public static Document createDocument(final SearchResourceContext searchResourceContext, final InfoMessage message) {
        final InfoMessageDocument messageDocument = new InfoMessageDocument();

        messageDocument.setTitle(message.getTitle());
        messageDocument.setContent(message.getMessage());
        messageDocument.setAuthor(message.getAuthor().getName());
        messageDocument.setCreatedDate(message.getCreationDate());
        messageDocument.setLastChange(message.getCreationDate());
        messageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        if ((searchResourceContext.getDocumentType() != null) && !searchResourceContext.getDocumentType().equals("")) {
            // Document is already set => take this value
            messageDocument.setDocumentType(searchResourceContext.getDocumentType());
        } else {
            messageDocument.setDocumentType(TYPE);
        }
        messageDocument.setCssIcon("o_infomsg_icon");
        messageDocument.setParentContextType(searchResourceContext.getParentContextType());
        messageDocument.setParentContextName(searchResourceContext.getParentContextName());

        if (log.isDebugEnabled()) {
            log.debug(messageDocument.toString());
        }
        return messageDocument.getLuceneDocument();
    }
}
