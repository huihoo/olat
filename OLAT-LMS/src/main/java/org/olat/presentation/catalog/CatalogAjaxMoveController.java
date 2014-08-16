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
package org.olat.presentation.catalog;

import java.util.List;

import org.olat.data.catalog.CatalogEntry;
import org.olat.lms.catalog.CatalogService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The ajax catalog move controller implements a dynamic tree that offers catalog navigating and selection of a catalog element. The to be moved entry is then moved to
 * the selected catalog category
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * <li>Event.FAILED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogAjaxMoveController extends BasicController {

    private TreeController treeCtr;
    private AjaxTreeModel treeModel;
    private VelocityContainer contentVC;
    private CatalogEntry toBeMovedEntry;
    private List<CatalogEntry> ownedEntries;
    private Link cancelLink;
    private final Link selectLink;
    private CatalogEntry selectedParent;
    final private CatalogService catalogService;

    /**
     * Constructor for the ajax move catalog entry controller
     * 
     * @param ureq
     * @param wControl
     * @param toBeMovedEntry
     */
    public CatalogAjaxMoveController(final UserRequest ureq, final WindowControl wControl, final CatalogEntry toBeMovedEntry) {
        super(ureq, wControl);
        this.toBeMovedEntry = toBeMovedEntry;
        this.catalogService = CoreSpringFactory.getBean(CatalogService.class);
        // Main view is a velocity container
        contentVC = createVelocityContainer("catalogentryajaxmove");
        contentVC.contextPut("entryname", toBeMovedEntry.getName());
        // build current path for gui
        CatalogEntry tempEntry = toBeMovedEntry;
        String path = "";
        while (tempEntry != null) {
            path = "/" + tempEntry.getName() + path;
            tempEntry = tempEntry.getParent();
        }
        contentVC.contextPut("path", path.toString());

        // Fetch all entries that can be accessed by this user. This is kept as a
        // local copy for performance reasons. This is only used when a node is
        // moved, when moving leafs we don't check for ownership of the category
        if (toBeMovedEntry.getType() == CatalogEntry.TYPE_NODE) {
            ownedEntries = getOwnedEntries(ureq);
        }

        final CatalogEntry rootce = (CatalogEntry) catalogService.getRootCatalogEntries().get(0);
        // Build tree model
        treeModel = new CatalogAjaxTreeModel(rootce, toBeMovedEntry, ownedEntries, false, false);

        // Create the ajax tree controller, add it to your main view
        treeCtr = new TreeController(ureq, getWindowControl(), rootce.getName(), treeModel, null);
        listenTo(treeCtr);
        contentVC.put("treeCtr", treeCtr.getInitialComponent());

        cancelLink = LinkFactory.createButton("cancel", contentVC, this);
        // select link is disabled until an item is selected
        selectLink = LinkFactory.createButton("select", contentVC, this);
        selectLink.setEnabled(false);

        putInitialPanel(contentVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        contentVC = null;
        treeModel = null;
        // Controllers auto disposed by basic controller
        treeCtr = null;
        ownedEntries = null;
        toBeMovedEntry = null;
        cancelLink = null;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == treeCtr) {
            if (event instanceof TreeNodeClickedEvent) {
                final TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                // try to update the catalog
                final String nodeId = clickedEvent.getNodeId();
                final Long newParentId = Long.parseLong(nodeId);
                final CatalogEntry newParent = catalogService.loadCatalogEntry(newParentId);
                boolean hasAccess;
                if (toBeMovedEntry.getType() == CatalogEntry.TYPE_LEAF) {
                    // Leafs can be attached anywhere in the catalog, no need to check for
                    // category ownership
                    hasAccess = true;
                } else {
                    // Check if the user owns this category or one of the preceding
                    // categories.
                    hasAccess = catalogService.isEntryWithinCategory(newParent, ownedEntries);
                }
                if (hasAccess) {
                    // don't move entry right away, user must select submit button first
                    selectedParent = newParent;
                    // enable link, set dirty button class and trigger redrawing
                    selectLink.setEnabled(true);
                    selectLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
                    selectLink.setDirty(true);
                } else {
                    showWarning("catalog.tree.move.noaccess");
                }
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == cancelLink) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else if (source == selectLink) {
            if (selectedParent != null) {
                final boolean success = catalogService.moveCatalogEntry(toBeMovedEntry, selectedParent);
                if (success) {
                    fireEvent(ureq, Event.DONE_EVENT);
                } else {
                    fireEvent(ureq, Event.FAILED_EVENT);
                }
            }
        }
    }

    /**
     * Internal helper method to get list of catalog entries where current user is in the owner group
     * 
     * @param ureq
     * @return List of repo entries
     */
    private List<CatalogEntry> getOwnedEntries(final UserRequest ureq) {
        if (ureq.getUserSession().getRoles().isOLATAdmin()) {
            return catalogService.getRootCatalogEntries();
        } else {
            return catalogService.getCatalogEntriesOwnedBy(ureq.getIdentity());
        }
    }

}
