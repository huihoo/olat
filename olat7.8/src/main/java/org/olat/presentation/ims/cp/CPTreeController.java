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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.ims.cp.CPManager;
import org.olat.lms.ims.cp.CPPage;
import org.olat.lms.ims.cp.CPTreeDataModel;
import org.olat.lms.ims.cp.ContentPackage;
import org.olat.lms.ims.cp.objects.CPItem;
import org.olat.lms.ims.cp.objects.CPOrganization;
import org.olat.lms.ims.cp.objects.CPResource;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.MoveTreeNodeEvent;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeModifiedEvent;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * The left-hand side of the cp editor shows the document tree with some edit icons on top.
 * <P>
 * Initial Date: May 5, 2009 <br>
 * 
 * @author gwassmann
 */
public class CPTreeController extends BasicController {

    private final TreeController treeCtr;
    private DialogBoxController dialogCtr;
    private CPFileImportController uploadCtr;
    private CPContentController contentCtr;
    private VelocityContainer contentVC;
    private final ContentPackage cp;
    private CloseableModalController cmc;

    private Link importLink;
    private Link newLink;
    private Link copyLink;
    private Link deleteLink;
    private final CPTreeDataModel treeModel;

    private CPPage currentPage;

    protected CPTreeController(final UserRequest ureq, final WindowControl control, final ContentPackage cp) {
        super(ureq, control);
        contentVC = createVelocityContainer("treeView");

        this.cp = cp;

        final String rootTitle = cp.getFirstOrganizationInManifest().getTitle();
        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        treeModel = cpMgm.getTreeDataModel(cp);
        treeCtr = new TreeController(ureq, control, rootTitle, treeModel, null);
        treeCtr.setTreeInlineEditing(true, null, null);

        // do not sort jsTree (structure is given by manifest)
        treeCtr.setTreeSorting(false, false, false);
        listenTo(treeCtr);

        setLinks();
        contentVC.put("cptreecontroller.tree", treeCtr.getInitialComponent());

        putInitialPanel(contentVC);
    }

    private void setLinks() {
        importLink = LinkFactory.createCustomLink("cptreecontroller.importlink", "cptreecontroller.importlink", null, Link.NONTRANSLATED, contentVC, this);
        importLink.setCustomEnabledLinkCSS("o_cpeditor_import");
        importLink.setTooltip(translate("cptreecontroller.importlink_title"), false);
        importLink.setTitle(translate("cptreecontroller.importlink_title"));

        newLink = LinkFactory.createCustomLink("cptreecontroller.newlink", "cptreecontroller.newlink", null, Link.NONTRANSLATED, contentVC, this);
        newLink.setCustomEnabledLinkCSS("o_cpeditor_new");
        newLink.setTooltip(translate("cptreecontroller.newlink_title"), false);
        newLink.setTitle(translate("cptreecontroller.newlink_title"));

        copyLink = LinkFactory.createCustomLink("cptreecontroller.copylink", "cptreecontroller.copylink", null, Link.NONTRANSLATED, contentVC, this);
        copyLink.setTooltip(translate("cptreecontroller.copylink_title"), false);
        copyLink.setTitle(translate("cptreecontroller.copylink_title"));
        copyLink.setCustomEnabledLinkCSS("o_cpeditor_copy");

        deleteLink = LinkFactory.createCustomLink("cptreecontroller.deletelink", "cptreecontroller.deletelink", null, Link.NONTRANSLATED, contentVC, this);
        deleteLink.setTooltip(translate("cptreecontroller.deletelink_title"), false);
        deleteLink.setTitle(translate("cptreecontroller.deletelink_title"));
        deleteLink.setCustomEnabledLinkCSS("o_cpeditor_delete");
    }

    /**
     * page setter
     * 
     * @param page
     */
    protected void setCurrentPage(final CPPage page) {
        currentPage = page;
    }

    /**
     * Make this controller aware of the content controller.
     * 
     * @param page
     */
    protected void setContentController(final CPContentController contentCtr) {
        this.contentCtr = contentCtr;
    }

    /**
     * deletes a page from the manifest
     * 
     * @param nodeID
     */
    private void deletePage(final String identifier, final boolean deleteResource) {
        if (identifier.equals("")) {
            // no page selected
        } else {
            final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
            final String path = treeModel.getPath(identifier);
            treeCtr.removePath(path);
            cpMgm.removeElement(cp, identifier, deleteResource);
            cpMgm.writeToFile(cp);
        }
    }

    /**
     * copies the page with given nodeID
     * 
     * @param nodeID
     */
    private String copyPage(final CPPage page) {
        String newIdentifier = null;
        if (page != null) {
            final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
            newIdentifier = cpMgm.copyElement(cp, page.getIdentifier());
            cpMgm.writeToFile(cp);
        }
        return newIdentifier;
    }

    /**
     * Adds a new page to the CP
     * 
     * @return
     */
    protected String addNewHTMLPage() {
        final String newId = ((CPManager) CoreSpringFactory.getBean(CPManager.class)).addBlankPage(cp, translate("cptreecontroller.newpage.title"),
                currentPage.getIdentifier());
        final CPPage newPage = new CPPage(newId, cp);
        // Create an html file
        final VFSContainer root = cp.getRootDir();
        final VFSLeaf htmlFile = root.createChildLeaf(newId + ".html");
        newPage.setFile(htmlFile);
        updatePage(newPage);
        return newId;
    }

    /**
     * Adds a page to the CP
     * 
     * @return
     */
    protected String addPage(final CPPage page) {
        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        String newNodeID = "";

        if (currentPage.getIdentifier().equals("")) {
            newNodeID = cpMgm.addBlankPage(cp, page.getTitle());
        } else {
            // adds new page as child of currentPage
            newNodeID = cpMgm.addBlankPage(cp, page.getTitle(), currentPage.getIdentifier());
        }
        setCurrentPage(new CPPage(newNodeID, cp));

        cpMgm.writeToFile(cp);
        // treeCtr.getInitialComponent().setDirty(true);
        return newNodeID;
    }

    /**
     * @param page
     */
    protected void updatePage(final CPPage page) {
        setCurrentPage(page);
        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        cpMgm.updatePage(cp, page);
        cpMgm.writeToFile(cp);
        if (page.isOrgaPage()) {
            // TODO:GW Shall the repo entry title be updated when the organization
            // title changes?
            // // If the organization title changed, also update the repo entry
            // // title.
            // RepositoryManager resMgr = RepositoryManager.getInstance();
            // RepositoryEntry cpEntry =
            // resMgr.lookupRepositoryEntry(cp.getResourcable(), false);
            // cpEntry.setDisplayname(page.getTitle());
            treeCtr.setRootNodeTitle(page.getTitle());
        }
        selectTreeNodeByCPPage(page);
    }

    /**
     * Updates a page by nodeId.
     * 
     * @param nodeId
     */
    protected void updateNode(final String nodeId, final String title) {
        final String nodeIdentifier = treeModel.getIdentifierForNodeID(nodeId);
        final CPPage page = new CPPage(nodeIdentifier, cp);
        page.setTitle(title);
        if (page.isOrgaPage()) {
            treeCtr.setRootNodeTitle(title);
        }
        updatePage(page);
    }

    /**
     * Performs the node-move-actions (invokes methods of the manager...)
     * 
     * @param event
     * @return returns true, if move was successfull
     */
    private boolean movePage(final MoveTreeNodeEvent event) {
        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        final String movedNodeId = event.getNodeId();
        cpMgm.moveElement(cp, movedNodeId, event.getNewParentNodeId(), event.getPosition());
        cpMgm.writeToFile(cp);
        selectTreeNodeById(movedNodeId);
        return true;
    }

    /**
     * selects a Tree node in the tree with given id (if found). Returns false, if node is not found, true otherwise info: todo: implement selection of node in js tree
     * 
     * @param id
     * @return
     */
    protected boolean selectTreeNodeById(final String id) {
        currentPage = new CPPage(id, cp);
        return selectTreeNodeByCPPage(currentPage);
    }

    /**
     * Selects the node in the tree with the given page (if found). Returns false, if node is not found, true otherwise info: todo: implement selection of node in js tree
     * 
     * @param page
     * @return
     */
    protected boolean selectTreeNodeByCPPage(final CPPage page) {
        currentPage = page;
        final String path = treeModel.getPath(page.getIdentifier());
        treeCtr.selectPath(path);
        return true;
    }

    /**
     * Builds an html-info string about the current page and its linked resources. <br>
     * The untrusted text is already html escaped.
     * 
     * @return HTML-String
     */
    private String getCurrentPageInfoStringHTML() {
        // test if currentPage links to resource, which is used (linked) somewhere
        // else in the manifest
        final CPManager cpMgm = (CPManager) CoreSpringFactory.getBean(CPManager.class);
        final DefaultElement ele = cpMgm.getElementByIdentifier(cp, currentPage.getIdRef());
        boolean single = false;
        if (ele instanceof CPResource) {
            final CPResource res = (CPResource) ele;
            single = cpMgm.isSingleUsedResource(res, cp);
        }

        final StringBuilder b = new StringBuilder();
        b.append("<br /><ul>");
        b.append("<li><b>" + translate("cptreecontroller.pagetitle") + "</b> " + StringHelper.escapeHtml(currentPage.getTitle()) + "</li>");
        if (single) {
            b.append("<li><b>" + translate("cptreecontroller.file") + "</b> " + StringHelper.escapeHtml(currentPage.getFileName()) + "</li>");
        }
        b.append("</ul>");
        return b.toString();

    }

    /**
     * Event-handling from components
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == importLink) {
            uploadCtr = new CPFileImportController(ureq, getWindowControl(), cp, currentPage);
            activateModalDialog(uploadCtr);

        } else if (source == newLink) {
            fireEvent(ureq, new Event("New Page"));

        } else if (source == copyLink) {
            if (currentPage.isOrgaPage()) {
                showInfo("cptreecontroller.orga.cannot.be.copied");
            } else {
                final String newIdentifier = copyPage(currentPage);
                // this.getInitialComponent().setDirty(true);
                contentCtr.displayPage(ureq, newIdentifier);
            }
        } else if (source == deleteLink) {
            if (currentPage.isOrgaPage()) {
                showInfo("cptreecontroller.orga.cannot.be.deleted");
            } else {
                final List<String> buttonLables = new ArrayList<String>();
                buttonLables.add(translate("cptreecontrolller.delete.items.and.files"));
                buttonLables.add(translate("cptreecontrolller.delete.items.only"));
                buttonLables.add(translate("cancel"));

                // text is already escaped
                String text = translate("cptreecontroller.q_delete_text", getCurrentPageInfoStringHTML());
                dialogCtr = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("cptreecontroller.q_delete_title"), text, buttonLables);
                listenTo(dialogCtr);
                dialogCtr.activate();
            }
        }
    }

    /**
     * Event-handling from controllers
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == cmc) {
            if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
                removeAsListenerAndDispose(cmc);
                cmc = null;
                removeAsListenerAndDispose(uploadCtr);
                uploadCtr = null;
            }
        } else if (source == uploadCtr) {
            if (event instanceof NewCPPageEvent) {
                // TODO:GW Is it necessary to set component dirty?
                // getInitialComponent().setDirty(true);
                fireEvent(ureq, event);
            }
            // Dispose the cmc and the podcastFormCtr.
            cmc.deactivate();
            removeAsListenerAndDispose(cmc);
            cmc = null;
            removeAsListenerAndDispose(uploadCtr);
            uploadCtr = null;
        } else if (source == treeCtr) {
            // event from extJSTree (TreeController)
            if (event instanceof MoveTreeNodeEvent) {
                /** move * */
                final MoveTreeNodeEvent moveEvent = (MoveTreeNodeEvent) event;
                final String selectedNodeID = treeModel.getIdentifierForNodeID(moveEvent.getNodeId());

                currentPage = new CPPage(selectedNodeID, cp);

                final MoveTreeNodeEvent newmoveEvent = new MoveTreeNodeEvent(treeModel.getIdentifierForNodeID(moveEvent.getNodeId()),
                        treeModel.getIdentifierForNodeID(moveEvent.getOldParentNodeId()), treeModel.getIdentifierForNodeID(moveEvent.getNewParentNodeId()),
                        moveEvent.getPosition());

                final boolean success = movePage(newmoveEvent);
                // setResult is important. If sucess is not true, the ajax tree will
                // popup a dialog with error-msg
                moveEvent.setResult(success, "Error", "Error while moving node");

            } else if (event instanceof TreeNodeClickedEvent) {
                /** click * */
                TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                final String selectedNodeID = treeModel.getIdentifierForNodeID(clickedEvent.getNodeId());

                currentPage = new CPPage(selectedNodeID, cp);

                clickedEvent = new TreeNodeClickedEvent(currentPage.getIdentifier());
                fireEvent(ureq, clickedEvent);
            } else if (event instanceof TreeNodeModifiedEvent) {
                /** a node (name) has been modified **/
                fireEvent(ureq, event);
            }
        } else if (source == dialogCtr) {
            // event from dialog (really-delete-dialog)
            if (event != Event.CANCELLED_EVENT) {
                final int position = DialogBoxUIFactory.getButtonPos(event);

                // 0 = delete with resource
                // 1 = delete without resource
                // 2 = cancel
                if (position == 0 || position == 1) {
                    boolean deleteResource = false;
                    if (position == 0) {
                        // Delete element including files
                        deleteResource = true;
                    }
                    final String parentIdentifier = getParentIdentifier();

                    // finally delete the page
                    deletePage(currentPage.getIdentifier(), deleteResource);

                    if (parentIdentifier != null) {
                        contentCtr.displayPage(ureq, parentIdentifier);
                    }
                } else {
                    // Cancel dialog and close window.
                }
            }
        }
    }

    /**
     * Retrieves the parent identifier of the current page
     * 
     * @return The identifier of the current page's parent
     */
    private String getParentIdentifier() {
        final DefaultElement currentElem = ((CPManager) CoreSpringFactory.getBean(CPManager.class)).getElementByIdentifier(cp, currentPage.getIdentifier());

        // Get the parent node to be displayed after deletion.
        String parentIdentifier = null;
        if (currentElem instanceof CPItem) {
            final Element parent = ((CPItem) currentElem).getParentElement();
            if (parent instanceof CPItem) {
                final CPItem parentItem = (CPItem) parent;
                parentIdentifier = parentItem.getIdentifier();
            } else if (parent instanceof CPOrganization) {
                final CPOrganization parentItem = (CPOrganization) parent;
                parentIdentifier = parentItem.getIdentifier();
            }
        }
        return parentIdentifier;
    }

    @Override
    protected void doDispose() {
        contentVC = null;
    }

    /**
     * @param controller
     *            The <code>FormBasicController</code> to be displayed in the modal dialog.
     */
    private void activateModalDialog(final FormBasicController controller) {
        listenTo(controller);
        cmc = new CloseableModalController(getWindowControl(), translate("close"), controller.getInitialComponent());
        listenTo(cmc);
        cmc.activate();
    }
}
