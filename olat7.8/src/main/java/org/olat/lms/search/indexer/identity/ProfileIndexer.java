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
package org.olat.lms.search.indexer.identity;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.IdentityDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * <h3>Description:</h3>
 * <p>
 * The identity indexer indexes the users profile
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class ProfileIndexer extends SubLevelIndexer<Identity> {
    private static final Logger log = LoggerHelper.getLogger();

    /**
	 */
    @Override
    public String getSupportedTypeName() {
        return Identity.class.getSimpleName();
    }

    /**
	 */

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final Identity identity, final OlatFullIndexer indexWriter) throws IOException {

        try {
            // no need to change the resource context, the profile is activated in the user homepage anyway
            final Document document = IdentityDocument.createDocument(parentResourceContext, identity);
            indexWriter.addDocument(document);
        } catch (final Exception ex) {
            log.warn("Exception while indexing profile for identity::" + identity + ". Skipping this user, try next one.", ex);
        }
        if (log.isDebugEnabled()) {
            log.debug("ProfileIndexer finished for user::" + identity);
        }

    }

    /**
     * org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Roles)
     */
    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        return true;
    }
}
