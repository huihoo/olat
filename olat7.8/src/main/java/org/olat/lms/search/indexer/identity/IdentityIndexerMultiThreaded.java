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
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.search.indexer.TopLevelIndexerMultiThreaded;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description:</h3>
 * <p>
 * The identity indexer indexes public information about a user such as the profile or the users public folder
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 * @author oliver.buehler@agility-informatik.ch
 */
public class IdentityIndexerMultiThreaded extends TopLevelIndexerMultiThreaded {

    public static final String TYPE = "type.identity";

    private static final Logger log = LoggerHelper.getLogger();

    protected final BaseSecurity securityManager;

    private List<SubLevelIndexer<Identity>> subIndexerList;

    private List<Long> identityList;

    /**
     * [spring managed]
     */
    private IdentityIndexerMultiThreaded(final BaseSecurity securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public String getSupportedTypeName() {
        return Identity.class.getSimpleName();
    }

    @Override
    protected void doIndexing(final OlatFullIndexer indexWriter) {
        super.doIndexing(indexWriter);
        subIndexerList = getSubIndexerList();
        identityList = loadIdentityList();

        status = IndexerStatus.RUNNING;

        for (Long identityId : identityList) {
            threadPool.executeIndexerTask(new IdentityIndexerTask(indexWriter, identityId));
        }

        status = threadPool.waitForCompletion(indexWriter.getTimeoutSeconds());
    }

    @Override
    protected int getNumberOfItemsToBeIndexed() {
        return identityList.size();
    }

    @Override
    protected String getThreadPoolName() {
        return "IdentityIndexer";
    }

    /**
     * org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Roles)
     */
    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        return true;
    }

    private List<Long> loadIdentityList() {
        try {
            final BaseSecurity secMgr = CoreSpringFactory.getBean(BaseSecurity.class);
            final List<Long> identityIdList = secMgr.getActiveIdentityIds();
            if (log.isDebugEnabled()) {
                log.debug("Found " + identityList.size() + " active identities to index");
            }
            return identityIdList;
        } finally {
            // committing here to make sure identities get reloaded
            // from the database and not only from the session cache
            DBFactory.getInstance(false).commitAndCloseSession();
        }

    }

    private void indexIdentity(Long identityId, OlatFullIndexer indexWriter) throws IOException, DocumentException, FolderIndexerTimeoutException {
        try {
            // reload the identity here before indexing it to make sure it has not been deleted in the meantime
            final Identity identity = securityManager.loadIdentityByKey(identityId);
            if (identity == null || (identity.getStatus() >= Identity.STATUS_VISIBLE_LIMIT)) {
                log.info("doIndex: identity was deleted while we were indexing. The deleted identity was: " + identity);
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Indexing identity::" + identity.getName());
            }
            // Create a search context for this identity. The search context will open the users visiting card in a new tab
            final SearchResourceContext searchResourceContext = new SearchResourceContext();
            searchResourceContext.setBusinessControlFor(OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey()));
            searchResourceContext.setParentContextType(TYPE);

            // delegate indexing work to all configured indexers
            for (final SubLevelIndexer<Identity> subIndexer : subIndexerList) {
                subIndexer.doIndex(searchResourceContext, identity, indexWriter);
            }
        } finally {
            DBFactory.getInstance(false).commitAndCloseSession();
        }
    }

    private final class IdentityIndexerTask implements Runnable {

        private final OlatFullIndexer indexWriter;

        private final Long identityId;

        private IdentityIndexerTask(final OlatFullIndexer indexWriter, final Long identityId) {
            this.indexWriter = indexWriter;
            this.identityId = identityId;
        }

        @Override
        public void run() {
            try {
                indexingItemStarted(identityId);
                indexIdentity(identityId, indexWriter);
                indexingItemFinished(identityId);
            } catch (FolderIndexerTimeoutException ex) {
                indexingItemTimedOut(identityId);
                log.warn("Timeout indexing Identity: " + identityId);
            } catch (Exception ex) {
                indexingItemFailed(identityId, ex);
                log.warn("Error indexing Identity: " + identityId, ex);
            }
        }
    }

}
