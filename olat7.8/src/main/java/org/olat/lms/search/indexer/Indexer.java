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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 04.04.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public abstract class Indexer {

    private static final Logger log = LoggerHelper.getLogger();

    protected enum IndexerStatus {
        INITIAL, IGNORED, RUNNING, COMPLETED, INTERRUPTED, TIMEOUT;
    }

    private List<SubLevelIndexer<Object>> subIndexerList;

    private Map<String, SubLevelIndexer<Object>> subIndexersBySupportedType = new HashMap<String, SubLevelIndexer<Object>>();

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setIndexerList(final List<SubLevelIndexer<Object>> subIndexerList) {
        if (subIndexerList == null) {
            throw new AssertException("null value for subIndexerList not allowed.");
        }

        this.subIndexerList = subIndexerList;
        for (final SubLevelIndexer<Object> indexer : subIndexerList) {
            subIndexersBySupportedType.put(indexer.getSupportedTypeName(), indexer);
            log.debug("Adding SubIndexer from configuration. TypeName=" + indexer.getSupportedTypeName());
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> List<SubLevelIndexer<T>> getSubIndexerList() {
        final List<SubLevelIndexer<T>> castedSubIndexerList = new ArrayList<SubLevelIndexer<T>>();
        if (subIndexerList != null) {
            for (SubLevelIndexer<Object> indexer : subIndexerList) {
                castedSubIndexerList.add((SubLevelIndexer<T>) indexer);
            }
        }
        return castedSubIndexerList;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> SubLevelIndexer<T> getSubIndexer(final String supportedType) {
        return (SubLevelIndexer<T>) subIndexersBySupportedType.get(supportedType);
    }

    /**
     * @return The indexer supports this type name. E.g. ORES_TOOLFOLDER.getResourceableTypeName()
     */
    public abstract String getSupportedTypeName();

    /**
     * Check access for certain business-control (resourceUrl) and user with roles.
     * 
     * @param contextEntry
     * @param businessControl
     * @param identity
     * @param roles
     * @return
     */
    public abstract boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles);

}
