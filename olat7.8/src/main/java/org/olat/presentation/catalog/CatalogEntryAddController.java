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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com,
 * <p>
 */
package org.olat.presentation.catalog;

import java.util.List;

import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.catalog.CatalogService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This subworkflow creates a selection tree to move a level from within the catalog to another level
 * <P>
 * Events fired by this controller:
 * <UL>
 * <LI>Event.DONE_EVENT</LI>
 * <LI>Event.CANCELLED_EVENT</LI>
 * </UL>
 * <P>
 * Initial Date: 04.06.2008 <br>
 * 
 * @author Florian Gnägi, frentix GmbH
 */
public class CatalogEntryAddController extends BasicController {
    private SelectionTree selectionTree;
    private VelocityContainer mainVC;
    private final RepositoryEntry toBeAddedEntry;
    private CatalogService catalogService;

    /**
     * Constructor
     * 
     * @param wControl
     * @param ureq
     * @param toBeAddedEntry
     */
    public CatalogEntryAddController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry toBeAddedEntry) {
        super(ureq, wControl);
        this.toBeAddedEntry = toBeAddedEntry;
        this.catalogService = CoreSpringFactory.getBean(CatalogService.class);
        final List<CatalogEntry> catEntryList = catalogService.getAllCatalogNodes();

        mainVC = createVelocityContainer("catMove");
        selectionTree = new SelectionTree("catSelection", getTranslator());
        selectionTree.addListener(this);
        selectionTree.setMultiselect(false);
        selectionTree.setFormButtonKey("cat.move.submit");
        selectionTree.setShowCancelButton(true);
        selectionTree.setTreeModel(new CatalogTreeModel(catEntryList, null, null));
        mainVC.put("tree", selectionTree);

        putInitialPanel(mainVC);

    }

    @Override
    protected void doDispose() {
        this.mainVC = null;
        this.selectionTree = null;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == selectionTree) {
            final TreeEvent te = (TreeEvent) event;
            if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
                final GenericTreeNode node = (GenericTreeNode) selectionTree.getSelectedNode();
                if (catalogService.hasParentAllreadyCatalogEntryAsChild(node.getIdent(), toBeAddedEntry)) {
                    showError("catalog.tree.add.already.exists", toBeAddedEntry.getDisplayname());
                } else {
                    catalogService.createCatalogEntryLeaf(toBeAddedEntry, node.getIdent());
                    fireEvent(ureq, Event.DONE_EVENT);
                }
            } else if (te.getCommand().equals(TreeEvent.COMMAND_CANCELLED)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }

    }

}
