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

import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
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
 * The ajax catalog add controller implements a dynamic tree that offers catalog navigating and selection of a catalog element. The to be added reporitory entry is then
 * added to the selected catalog category
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogAjaxAddController extends BasicController {

    protected TreeController treeCtr;
    private AjaxTreeModel treeModel;
    private VelocityContainer contentVC;
    protected RepositoryEntry toBeAddedEntry;
    protected Link cancelLink, selectLink;
    final private CatalogService catalogService;
    private String parentNodeIdent;

    public CatalogAjaxAddController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry toBeAddedEntry) {
        super(ureq, wControl);
        this.toBeAddedEntry = toBeAddedEntry;
        this.catalogService = CoreSpringFactory.getBean(CatalogService.class);
        // Main view is a velocity container
        contentVC = createVelocityContainer("catalogentryajaxadd");
        contentVC.contextPut("entryname", toBeAddedEntry.getDisplayname());

        final CatalogEntry rootce = (CatalogEntry) catalogService.getRootCatalogEntries().get(0);
        // Build tree model
        treeModel = new CatalogAjaxTreeModel(rootce, null, null, false, false);

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
        toBeAddedEntry = null;
        cancelLink = null;
        selectLink = null;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == treeCtr) {
            if (event instanceof TreeNodeClickedEvent) {
                final TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                // build new entry for this catalog level
                parentNodeIdent = clickedEvent.getNodeId();
                if (catalogService.hasParentAllreadyCatalogEntryAsChild(parentNodeIdent, toBeAddedEntry)) {
                    showError("catalog.tree.add.already.exists", toBeAddedEntry.getDisplayname());
                } else {
                    // enable link, set dirty button class and trigger redrawing
                    selectLink.setEnabled(true);
                    selectLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
                    selectLink.setDirty(true);
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
            if (parentNodeIdent != null) {
                catalogService.createCatalogEntryLeaf(toBeAddedEntry, parentNodeIdent);
                fireEvent(ureq, Event.DONE_EVENT);
            }
        }
    }

}
