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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.change.ChangeManager;
import org.olat.lms.commons.change.ObjectAccessEvent;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.GroupDocument;
import org.olat.lms.search.indexer.AbstractIndexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Index all business-groups. Includes group-forums and groups-folders.
 * 
 * @author Christian Guretzki
 */
public class GroupIndexer extends AbstractIndexer implements GenericEventListener {

    @Autowired
    private BusinessGroupService businessGroupService;
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * [spring managed]
     */
    private GroupIndexer() {
        // -> OLAT-3367 OLATResourceable ores = OresHelper.lookupType(BusinessGroup.class);
        // -> OLAT-3367 CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, ores);
    }

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final Object parentObject, final OlatFullIndexer indexWriter) throws IOException,
            InterruptedException {
        final long startTime = System.currentTimeMillis();
        final List groupList = businessGroupService.getAllBusinessGroups();
        if (log.isDebugEnabled()) {
            log.debug("GroupIndexer groupList.size=" + groupList.size());
        }

        // committing here to make sure the loadBusinessGroup below does actually
        // reload from the database and not only use the session cache
        // (see org.hibernate.Session.get():
        // If the instance, or a proxy for the instance, is already associated with the session, return that instance or proxy.)
        DBFactory.getInstance().commitAndCloseSession();

        // loop over all groups
        final Iterator iter = groupList.iterator();
        while (iter.hasNext()) {
            BusinessGroup businessGroup = null;
            try {
                businessGroup = (BusinessGroup) iter.next();

                // reload the businessGroup here before indexing it to make sure it has not been deleted in the meantime
                final BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(businessGroup.getKey(), false);
                if (reloadedBusinessGroup == null) {
                    log.info("doIndex: businessGroup was deleted while we were indexing. The deleted businessGroup was: " + businessGroup);
                    continue;
                }
                businessGroup = reloadedBusinessGroup;

                if (log.isDebugEnabled()) {
                    log.debug("Index BusinessGroup=" + businessGroup);
                }
                final SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
                searchResourceContext.setBusinessControlFor(businessGroup);
                final Document document = GroupDocument.createDocument(searchResourceContext, businessGroup);
                indexWriter.addDocument(document);
                // Do index child
                super.doIndex(searchResourceContext, businessGroup, indexWriter);
            } catch (final Exception ex) {
                log.error("Exception indexing group=" + businessGroup, ex);
                DBFactory.getInstance(false).rollbackAndCloseSession();
            } catch (final Error err) {
                log.error("Error indexing group=" + businessGroup, err);
                DBFactory.getInstance(false).rollbackAndCloseSession();
            }
        }
        final long indexTime = System.currentTimeMillis() - startTime;
        if (log.isDebugEnabled()) {
            log.debug("GroupIndexer finished in " + indexTime + " ms");
        }
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        final Long key = contextEntry.getOLATResourceable().getResourceableId();
        final List oGroups = businessGroupService.findBusinessGroupsOwnedBy(null, identity, null);
        final List aGroups = businessGroupService.findBusinessGroupsAttendedBy(null, identity, null);

        boolean inGroup = false; // TODO
        for (final Iterator it_ogroups = oGroups.iterator(); !inGroup && it_ogroups.hasNext();) {
            final BusinessGroup gr = (BusinessGroup) it_ogroups.next();
            final Long grk = gr.getKey();
            if (grk.equals(key)) {
                inGroup = true;
            }
        }
        for (final Iterator it_agroups = aGroups.iterator(); !inGroup && it_agroups.hasNext();) {
            final BusinessGroup gr = (BusinessGroup) it_agroups.next();
            final Long grk = gr.getKey();
            if (grk.equals(key)) {
                inGroup = true;
            }
        }
        if (inGroup) {
            return super.checkAccess(businessControl, identity, roles);
        } else {
            return false;
        }
    }

    @Override
    public String getSupportedTypeName() {
        return OresHelper.calculateTypeName(BusinessGroup.class);
    }

    // Handling Update Event
    @Override
    public void event(final Event event) {
        if (ChangeManager.isChangeEvent(event)) {
            final ObjectAccessEvent oae = (ObjectAccessEvent) event;
            if (log.isDebugEnabled()) {
                log.debug("info: oae = " + oae.toString());
            }
            final int action = oae.getAction();
            final Long id = oae.getOresId();
            final BusinessGroup newBusinessGroup = businessGroupService.loadBusinessGroup(id, true);
            final SearchResourceContext searchResourceContext = new SearchResourceContext(); // businessContextString
            searchResourceContext.setBusinessControlFor(newBusinessGroup);
            final Document document = GroupDocument.createDocument(searchResourceContext, newBusinessGroup);
            if (action == ChangeManager.ACTION_UPDATE) {
                // -> OLAT-3367 SearchServiceImpl.getInstance().addToIndex(document);
            } else if (action == ChangeManager.ACTION_CREATE) {
                // -> OLAT-3367 SearchServiceImpl.getInstance().addToIndex(document);
            } else if (action == ChangeManager.ACTION_DELETE) {
                // -> OLAT-3367 SearchServiceImpl.getInstance().deleteFromIndex(document);
            }
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
