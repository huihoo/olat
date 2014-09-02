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
package org.olat.lms.search.indexer.group;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.GroupDocument;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.search.indexer.TopLevelIndexerMultiThreaded;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 10.04.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class GroupIndexerMultiThreaded extends TopLevelIndexerMultiThreaded {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private BusinessGroupService businessGroupService;

    private List<SubLevelIndexer<BusinessGroup>> subIndexerList;

    private List<Long> groupList;

    @Override
    public String getSupportedTypeName() {
        return OresHelper.calculateTypeName(BusinessGroup.class);
    }

    @Override
    protected void doIndexing(final OlatFullIndexer indexWriter) {
        super.doIndexing(indexWriter);
        subIndexerList = getSubIndexerList();
        groupList = loadBusinessGroupList();

        status = IndexerStatus.RUNNING;

        for (Long businessGroupId : groupList) {
            threadPool.executeIndexerTask(new GroupIndexerTask(indexWriter, businessGroupId));
        }

        status = threadPool.waitForCompletion(indexWriter.getTimeoutSeconds());
    }

    @Override
    protected int getNumberOfItemsToBeIndexed() {
        return groupList.size();
    }

    @Override
    protected String getThreadPoolName() {
        return "GroupIndexer";
    }

    @Override
    public boolean checkAccess(final ContextEntry groupContextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        final Long bgKey = groupContextEntry.getOLATResourceable().getResourceableId();
        final List<BusinessGroup> ownerGroups = businessGroupService.findBusinessGroupsOwnedBy(null, identity, null);
        final List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroupsAttendedBy(null, identity, null);

        boolean inGroup = false;
        for (final BusinessGroup ownerGroup : ownerGroups) {
            if (ownerGroup.getKey().equals(bgKey)) {
                inGroup = true;
                break;
            }
        }
        if (!inGroup) {
            for (final BusinessGroup attendedGroup : attendedGroups) {
                if (attendedGroup.getKey().equals(bgKey)) {
                    inGroup = true;
                    break;
                }
            }
        }

        if (inGroup) {
            final ContextEntry groupToolContextEntry = businessControl.popLauncherContextEntry();
            if (groupToolContextEntry == null) {
                // it's the group itself
                return true;
            }

            final String toolResoursableTypeName = groupToolContextEntry.getOLATResourceable().getResourceableTypeName();
            final SubLevelIndexer<BusinessGroup> toolSubIndexer = getSubIndexer(toolResoursableTypeName);
            return toolSubIndexer.checkAccess(groupContextEntry, businessControl, identity, roles);
        } else {
            return false;
        }
    }

    private List<Long> loadBusinessGroupList() {
        try {
            final List<Long> groupIdList = businessGroupService.getAllBusinessGroupIds();
            if (log.isDebugEnabled()) {
                log.debug("GroupIndexer groupList.size=" + groupList.size());
            }
            return groupIdList;
        } finally {
            // committing here to make sure business groups get reloaded
            // from the database and not only from the session cache
            DBFactory.getInstance(false).commitAndCloseSession();
        }
    }

    private void indexBusinessGroup(Long key, OlatFullIndexer indexWriter) throws IOException, DocumentException, FolderIndexerTimeoutException {
        try {
            // reload the businessGroup here before indexing it to make sure it has not been deleted in the meantime
            final BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(key, false);
            if (businessGroup == null) {
                log.info("doIndex: businessGroup was deleted while we were indexing. The deleted businessGroup was: " + businessGroup);
                return;
            }

            // index group data
            if (log.isDebugEnabled()) {
                log.debug("Index BusinessGroup=" + businessGroup);
            }
            final SearchResourceContext searchResourceContext = new SearchResourceContext();
            searchResourceContext.setBusinessControlFor(businessGroup);
            final Document document = GroupDocument.createDocument(searchResourceContext, businessGroup);
            indexWriter.addDocument(document);

            // index group sub elements
            for (final SubLevelIndexer<BusinessGroup> subIndexer : subIndexerList) {
                subIndexer.doIndex(searchResourceContext, businessGroup, indexWriter);
            }
        } finally {
            DBFactory.getInstance(false).commitAndCloseSession();
        }
    }

    private final class GroupIndexerTask implements Runnable {

        private final OlatFullIndexer indexWriter;

        private final Long groupId;

        private GroupIndexerTask(final OlatFullIndexer indexWriter, final Long groupId) {
            this.indexWriter = indexWriter;
            this.groupId = groupId;
        }

        @Override
        public void run() {
            try {
                indexingItemStarted(groupId);
                indexBusinessGroup(groupId, indexWriter);
                indexingItemFinished(groupId);
            } catch (FolderIndexerTimeoutException ex) {
                indexingItemTimedOut(groupId);
                log.warn("Timeout indexing BusinessGroup: " + groupId);
            } catch (Exception ex) {
                indexingItemFailed(groupId, ex);
                log.warn("Error indexing BusinessGroup: " + groupId, ex);
            }
        }
    }

}
