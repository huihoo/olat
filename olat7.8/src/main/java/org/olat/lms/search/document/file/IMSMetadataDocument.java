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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.dom4j.Element;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.ims.resources.IMSLoader;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.OlatDocument;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * For indexing the metadatas of scorm packaging
 * <P>
 * Initial Date: 11 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class IMSMetadataDocument extends OlatDocument {
    private static Set<String> stopWords = new HashSet<String>();
    private static final Logger log = LoggerHelper.getLogger();

    static {
        stopWords.add("LOMv1.0");
        stopWords.add("yes");
        stopWords.add("NA");
    }

    public static Document createDocument(final SearchResourceContext searchResourceContext, final VFSLeaf fManifest) {
        final IMSMetadataDocument document = new IMSMetadataDocument();
        document.setResourceUrl(searchResourceContext.getResourceUrl());
        if (log.isDebugEnabled()) {
            log.debug("MM: URL=" + document.getResourceUrl());
        }
        document.setLastChange(new Date(fManifest.getLastModified()));
        document.setDocumentType(searchResourceContext.getDocumentType());
        if (StringHelper.containsNonWhitespace(searchResourceContext.getTitle())) {
            document.setTitle(searchResourceContext.getTitle());
        } else {
            document.setTitle(fManifest.getName());
        }
        document.setParentContextType(searchResourceContext.getParentContextType());
        document.setParentContextName(searchResourceContext.getParentContextName());

        final Element rootElement = IMSLoader.loadIMSDocument(fManifest).getRootElement();

        final StringBuilder sb = new StringBuilder();
        collectLangString(sb, rootElement);
        collectTitleNames(sb, rootElement);
        document.setContent(sb.toString());
        return document.getLuceneDocument();
    }

    private static void collectLangString(final StringBuilder sb, final Element element) {
        if ("langstring".equals(element.getName())) {
            final String content = element.getText();
            if (!stopWords.contains(content)) {
                sb.append(content).append(' ');
            }
        }
        @SuppressWarnings("rawtypes")
        final List children = element.elements();
        for (int i = 0; i < children.size(); i++) {
            final Element child = (Element) children.get(i);
            collectLangString(sb, child);
        }
    }

    private static void collectTitleNames(final StringBuilder sb, final Element element) {
        if ("title".equals(element.getName())) {
            final String title = element.getText();
            sb.append(title).append(' ');
        }
        @SuppressWarnings("rawtypes")
        final List children = element.elements();
        for (int i = 0; i < children.size(); i++) {
            final Element child = (Element) children.get(i);
            collectTitleNames(sb, child);
        }
    }
}
