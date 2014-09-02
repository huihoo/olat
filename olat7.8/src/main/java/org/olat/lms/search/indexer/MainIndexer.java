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

package org.olat.lms.search.indexer;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.indexer.Indexer.IndexerStatus;
import org.olat.lms.search.indexer.TopLevelIndexer.TopLevelIndexerStatus;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Entry class for controlling the indexer run. This class manages all defined top level indexers.
 * 
 * @author Christian Guretzki
 * @author oliver.buehler@agility-informatik.ch
 */
public class MainIndexer {

    private static final Logger log = LoggerHelper.getLogger();

    private List<TopLevelIndexer> topLevelIndexerList;

    private Map<String, TopLevelIndexer> topLevelIndexerByType;

    private IndexerWorkerPool topLevelIndexerPool;

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setIndexerList(final List<TopLevelIndexer> indexerList) {
        if (indexerList == null) {
            throw new AssertException("null value for indexer list not allowed.");
        }

        topLevelIndexerList = indexerList;
        topLevelIndexerByType = new HashMap<String, TopLevelIndexer>(topLevelIndexerList.size());
        for (TopLevelIndexer topLevelIndexer : indexerList) {
            topLevelIndexerByType.put(topLevelIndexer.getSupportedTypeName(), topLevelIndexer);
        }
    }

    IndexerStatus startIndexing(final OlatFullIndexer indexWriter) {
        for (TopLevelIndexer indexer : topLevelIndexerList) {
            indexer.resetStatus();
        }

        topLevelIndexerPool = new IndexerWorkerPool(topLevelIndexerList.size(), indexWriter.getSearchModuleConfig().getIndexerPrio(), "TopLevelIndexer");

        for (final TopLevelIndexer indexer : topLevelIndexerList) {
            topLevelIndexerPool.executeIndexerTask(new Runnable() {

                @Override
                public void run() {
                    indexer.startIndexing(indexWriter);
                }

            });
        }

        return topLevelIndexerPool.waitForCompletion(indexWriter.getTimeoutSeconds());
    }

    void stopIndexing() {
        for (TopLevelIndexer indexer : topLevelIndexerList) {
            indexer.stopIndexing();
        }

        topLevelIndexerPool.terminate();
    }

    public boolean checkAccess(final BusinessControl businessControl, final Identity identity, final Roles roles) {
        if (log.isDebugEnabled()) {
            log.debug("checkAccess for businessControl=" + businessControl + "  identity=" + identity + "  roles=" + roles);
        }
        final ContextEntry contextEntry = businessControl.popLauncherContextEntry();
        if (contextEntry != null) {
            final OLATResourceable ores = contextEntry.getOLATResourceable();
            final String type = ores.getResourceableTypeName();
            final TopLevelIndexer indexer = topLevelIndexerByType.get(type);
            if (indexer == null) {
                log.error("could not find an indexer for type=" + type + " businessControl=" + businessControl + " identity=" + identity);
                return false;
            }
            return indexer.checkAccess(contextEntry, businessControl, identity, roles);
        } else {
            log.error("No context defined for businessControl=" + businessControl + " identity=" + identity);
            return false;
        }
    }

    Map<String, TopLevelIndexerStatus> getStatus() {
        Map<String, TopLevelIndexerStatus> status = new LinkedHashMap<String, TopLevelIndexerStatus>(topLevelIndexerList.size());
        for (TopLevelIndexer indexer : topLevelIndexerList) {
            status.put(indexer.getClass().getSimpleName(), indexer.getStatus());
        }
        return status;
    }

    void reportItemIndexing(File destDir) {
        for (TopLevelIndexer indexer : topLevelIndexerList) {
            indexer.createStatisticsFile(destDir);
        }
    }

}
