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

package org.olat.lms.search.document;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.search.SearchResourceContext;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class GroupDocument extends OlatDocument {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    public final static String TYPE = "type.group";

    private static final Logger log = LoggerHelper.getLogger();

    public static Document createDocument(final SearchResourceContext searchResourceContext, final BusinessGroup businessGroup) {
        final GroupDocument groupDocument = new GroupDocument();

        // Set all know attributes
        groupDocument.setResourceUrl(searchResourceContext.getResourceUrl());
        groupDocument.setLastChange(businessGroup.getLastModified());
        groupDocument.setDocumentType(TYPE);
        groupDocument.setCssIcon(CSSHelper.CSS_CLASS_GROUP);
        groupDocument.setTitle(businessGroup.getName());
        // description is rich text
        groupDocument.setDescription(FilterFactory.unescapeAndFilterHtml(businessGroup.getDescription()));

        if (log.isDebugEnabled()) {
            log.debug(groupDocument.toString());
        }
        return groupDocument.getLuceneDocument();
    }
}
