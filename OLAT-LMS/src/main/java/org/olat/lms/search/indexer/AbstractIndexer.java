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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.search.SearchResourceContext;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Common abstract indexer. Used as base class for indexers.
 * 
 * @author Christian Guretzki
 */
public abstract class AbstractIndexer implements Indexer {

    private static final Logger log = LoggerHelper.getLogger();
    protected Map<String, Indexer> childIndexers = new HashMap<String, Indexer>();

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setIndexerList(final List<Indexer> indexerList) {
        if (indexerList == null) {
            throw new AssertException("null value for indexerList not allowed.");
        }

        try {
            for (final Indexer indexer : indexerList) {
                childIndexers.put(indexer.getSupportedTypeName(), indexer);
                log.debug("Adding indexer from configuraton. TypeName=" + indexer.getSupportedTypeName());
            }
        } catch (final ClassCastException cce) {
            throw new StartupException("Configured indexer is not of type Indexer", cce);
        }
    }

    /**
     * Iterate over all child indexer define in indexer-list.
     * 
     * org.olat.lms.search.indexer.OlatFullIndexer)
     */
    @Override
    public void doIndex(final SearchResourceContext searchResourceContext, final Object object, final OlatFullIndexer indexerWriter) throws IOException,
            InterruptedException {
        for (final Indexer indexer : childIndexers.values()) {
            if (log.isDebugEnabled()) {
                log.debug("Start doIndex for indexer.typeName=" + indexer.getSupportedTypeName());
            }
            try {
                indexer.doIndex(searchResourceContext, object, indexerWriter);
            } catch (final InterruptedException iex) {
                throw iex;
            } catch (final Throwable ex) {
                // FIXME:chg: Workaround to fix indexing-abort
                log.warn("Exception in diIndex indexer.typeName=" + indexer.getSupportedTypeName(), ex);
            }
        }
    }

    /**
     * @param businessControl
     * @param identity
     * @param roles
     * @return
     */
    public boolean checkAccess(final BusinessControl businessControl, final Identity identity, final Roles roles) {
        if (log.isDebugEnabled()) {
            log.debug("checkAccess for businessControl=" + businessControl + "  identity=" + identity + "  roles=" + roles);
        }
        final ContextEntry contextEntry = businessControl.popLauncherContextEntry();
        if (contextEntry != null) {
            // there is an other context-entry => go further
            final OLATResourceable ores = contextEntry.getOLATResourceable();
            final String type = ores.getResourceableTypeName();
            final Indexer indexer = this.childIndexers.get(type);
            if (indexer == null) {
                // loop in child-indexers to check access for businesspath not stacked as on index-run
                for (Entry<String, Indexer> entSet : childIndexers.entrySet()) {
                    AbstractIndexer childIndexer = entSet.getValue() instanceof AbstractIndexer ? (AbstractIndexer) entSet.getValue() : null;
                    Indexer foundSubChildIndexer = childIndexer == null ? null : childIndexer.getChildIndexers().get(type);
                    if (foundSubChildIndexer != null) {
                        if (log.isDebugEnabled())
                            log.debug("took a childindexer for ores= " + ores
                                    + " not directly linked (means businesspath is not the same stack as indexer -> childindexer). type= " + type
                                    + " . indexer parent-type not on businesspath=" + childIndexer.getSupportedTypeName());
                        return foundSubChildIndexer.checkAccess(contextEntry, businessControl, identity, roles);
                    }
                }
                log.error("could not find an indexer for type=" + type + " businessControl=" + businessControl + " identity=" + identity);
                return false;
            }
            return indexer.checkAccess(contextEntry, businessControl, identity, roles);
        } else {
            // rearch the end context entry list
            return true;
        }
    }

    public Map<String, Indexer> getChildIndexers() {
        return this.childIndexers;
    }

}
