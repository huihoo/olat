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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.ims.cp;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.ims.cp.CPManager;
import org.olat.lms.ims.cp.CPPage;
import org.olat.lms.ims.cp.ContentPackage;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeModifiedEvent;
import org.olat.presentation.framework.core.control.generic.layout.MainLayout3ColumnsController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * The content packaging main edit controller.
 */
public class CPEditMainController extends MainLayoutBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private LayoutMain3ColsController columnLayoutCtr;
    private CPContentController contentCtr;
    private CPTreeController treeCtr;
    private final ContentPackage cp;
    private final LockResult lock;

    public CPEditMainController(final UserRequest ureq, final WindowControl wControl, final VFSContainer cpContainer, final OLATResourceable ores) {
        super(ureq, wControl);

        // acquire lock for resource
        lock = getLockingService().acquireLock(ores, ureq.getIdentity(), null);

        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);

        this.cp = cpMgm.load(cpContainer, ores);

        final String errorString = cp.getLastError();
        if (errorString == null) {
            if (lock.isSuccess()) {
                initDefaultView(ureq, wControl);
            } else {
                showInfo("contentcontroller.no.lock");
                displayCP(ureq, wControl, cpContainer);
            }
        } else {
            initErrorView(ureq, wControl, errorString);
            this.showError("maincontroller.loaderror", errorString);
        }
        log.info("Audit:cp editor started. oresId: " + ores.getResourceableId(), null);
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * Displays the cp without being able to modify it.
     * 
     * @param ureq
     * @param wControl
     * @param root
     */
    private void displayCP(final UserRequest ureq, final WindowControl wControl, final VFSContainer root) {
        final MainLayout3ColumnsController cpCtr = CPUIFactory.getInstance().createMainLayoutController(ureq, wControl, root, true);
        this.putInitialPanel(cpCtr.getInitialComponent());
    }

    /**
     * initializes default controllers
     * 
     * @param ureq
     * @param wControl
     * @param cp
     */
    private void initDefaultView(final UserRequest ureq, final WindowControl wControl) {
        treeCtr = new CPTreeController(ureq, wControl, cp);
        listenTo(treeCtr);

        contentCtr = new CPContentController(ureq, wControl, cp);
        listenTo(contentCtr);
        contentCtr.init(ureq);

        // Make tree controller aware of contentCtr in order to display pages after
        // import.
        treeCtr.setContentController(contentCtr);

        columnLayoutCtr = new LayoutMain3ColsController(ureq, wControl, treeCtr.getInitialComponent(), null, contentCtr.getInitialComponent(), "cptestmain");
        columnLayoutCtr.addCssClassToMain("b_menu_toolbar");
        listenTo(columnLayoutCtr); // auto dispose

        this.putInitialPanel(columnLayoutCtr.getInitialComponent());

        if (!cp.isOLATContentPackage()) {
            showWarning("maincontroller.cp.created.with.third.party.editor");
        }
    }

    /**
     * initializes a special view, where the user is informed about errors. (while loading cp)
     * 
     * @param ureq
     * @param wControl
     * @param cp
     */
    private void initErrorView(final UserRequest ureq, final WindowControl wControl, final String errorString) {
        final Panel p = new Panel("errorPanel");
        columnLayoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, p, "cptestmain");
        this.putInitialPanel(columnLayoutCtr.getInitialComponent());
    }

    @Override
    protected void doDispose() {
        final Long oresId = cp.getResourcable().getResourceableId();
        log.info("Audit:cp editor closing. oresId: " + oresId, null);
        if (lock.isSuccess() && contentCtr != null) {
            // Save CP to zip
            ((CPManager) CoreSpringFactory.getBean(CPManager.class)).writeToZip(cp);
        }
        // In any case, release the lock
        getLockingService().releaseLock(lock);
        log.info("Audit:finished editing cp. ores-id: " + oresId, null);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == treeCtr) {
            // event from TreeController
            if (event instanceof TreeNodeClickedEvent) {
                final TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                final String nodeId = clickedEvent.getNodeId();
                contentCtr.displayPage(ureq, nodeId);

            } else if (event instanceof TreeNodeModifiedEvent) {
                final TreeNodeModifiedEvent nodeEvent = (TreeNodeModifiedEvent) event;
                final String nodeId = nodeEvent.getNodeId();
                final String newItemTitle = nodeEvent.getModifiedValue();
                treeCtr.updateNode(nodeId, newItemTitle);

            } else if (event.getCommand().equals("New Page")) {
                final String newIdentifier = treeCtr.addNewHTMLPage();
                contentCtr.displayPage(ureq, newIdentifier);

            } else if (event instanceof NewCPPageEvent) {
                contentCtr.displayPage(ureq, ((NewCPPageEvent) event).getCPPage().getIdentifier());
            }
        } else if (source == contentCtr) {
            // event from contentController
            if (event instanceof NewCPPageEvent) {
                final NewCPPageEvent ncpEvent = (NewCPPageEvent) event;
                final CPPage page = ncpEvent.getCPPage();
                if (event.getCommand().equals("New Page Saved")) {
                    final String newNodeID = treeCtr.addPage(page);
                    contentCtr.newPageAdded(newNodeID);
                    treeCtr.updatePage(page);

                } else if (event.getCommand().equals("Page Saved")) {
                    treeCtr.updatePage(page);
                    // Title could have changed -> dirty view
                    treeCtr.getInitialComponent().setDirty(true);
                }

            } else if (event.getCommand().equals("Page loaded")) {
                final CPPage page = contentCtr.getCurrentPage();
                if (page != null) {
                    treeCtr.selectTreeNodeByCPPage(page);
                }
            }
        }
    }
}
