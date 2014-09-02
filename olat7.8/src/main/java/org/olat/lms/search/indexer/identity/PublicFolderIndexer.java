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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * <h3>Description:</h3>
 * <p>
 * The identity indexer indexes the users public folder
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class PublicFolderIndexer extends SubLevelIndexer<Identity> {
    private static final Logger log = LoggerHelper.getLogger();

    public static final String TYPE = "type.identity.publicfolder";
    public static final OLATResourceable BUSINESS_CONTROL_TYPE = OresHelper.createOLATResourceableTypeWithoutCheck(FolderRunController.class.getSimpleName());

    @Override
    public String getSupportedTypeName() {
        return Identity.class.getSimpleName();
    }

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final Identity identity, final OlatFullIndexer indexWriter)
            throws FolderIndexerTimeoutException {

        // get public folder for user
        final OlatRootFolderImpl rootContainer = new OlatRootFolderImpl(FolderConfig.getUserHome(identity.getName()) + "/public", null);
        if (!rootContainer.getBasefile().exists()) {
            return;
        }
        // build new resource context
        final SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
        searchResourceContext.setParentContextName(identity.getName());
        searchResourceContext.setBusinessControlFor(BUSINESS_CONTROL_TYPE);
        searchResourceContext.setDocumentType(TYPE);
        // now index the folder
        FolderIndexer.indexVFSContainer(searchResourceContext, rootContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);

        if (log.isDebugEnabled()) {
            log.debug("PublicFolder finished for user::" + identity);
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
