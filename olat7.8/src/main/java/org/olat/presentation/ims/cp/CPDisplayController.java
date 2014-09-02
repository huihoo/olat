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

package org.olat.presentation.ims.cp;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.course.ICourse;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.HtmlStaticPageComponent;
import org.olat.presentation.framework.core.components.htmlsite.NewInlineUriEvent;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.iframe.IFrameDisplayController;
import org.olat.presentation.framework.core.control.generic.iframe.NewIframeUriEvent;
import org.olat.presentation.search.SearchServiceUIFactory;
import org.olat.presentation.search.SearchServiceUIFactory.DisplayOption;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * shows the actual content package with or without a menu
 * 
 * @author Felix Jost
 */
public class CPDisplayController extends BasicController {

    private static final String FILE_SUFFIX_HTM = "htm";
    private static final String FILE_SUFFIX_XML = "xml";

    private VelocityContainer myContent;
    private MenuTree cpTree;
    private CPManifestTreeModel ctm;
    private VFSContainer rootContainer;
    private String selNodeId;
    private HtmlStaticPageComponent cpComponent;
    private IFrameDisplayController cpContentCtr;
    private Controller searchCtrl;

    /**
     * @param ureq
     * @param cpRoot
     * @param showMenu
     * @param activateFirstPage
     */
    CPDisplayController(final UserRequest ureq, final WindowControl wControl, final VFSContainer rootContainer, final boolean showMenu, final boolean activateFirstPage,
            final String initialUri, final OLATResourceable ores) {
        super(ureq, wControl);
        this.rootContainer = rootContainer;

        // wrapper velocity container for page content
        this.myContent = createVelocityContainer("cpDisplay");
        // the cp component, added to the velocity

        if (!ureq.getUserSession().getRoles().isGuestOnly()) {
            final SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory) CoreSpringFactory.getBean(SearchServiceUIFactory.class);
            searchCtrl = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.BUTTON, null);
            myContent.put("search_input", searchCtrl.getInitialComponent());
        }

        // TODO:gs:a
        // may add an additional config for disabling, enabling IFrame style or not in CP mode
        // but always disable IFrame display when in screenreader mode (no matter whether style gets ugly)
        if (getWindowControl().getWindowBackOffice().getWindowManager().isForScreenReader()) {
            cpComponent = new HtmlStaticPageComponent("", rootContainer);
            cpComponent.addListener(this);
            myContent.put("cpContent", cpComponent);
        } else {
            cpContentCtr = new IFrameDisplayController(ureq, getWindowControl(), rootContainer, null, ores);
            cpContentCtr.setAllowDownload(true);
            listenTo(cpContentCtr);
            myContent.put("cpContent", cpContentCtr.getInitialComponent());
        }

        // even if we do not show the menu, we need to build parse the manifest and
        // find the first node to display at startup
        final VFSItem mani = rootContainer.resolve("imsmanifest.xml");
        if (mani == null || !(mani instanceof VFSLeaf)) {
            throw new OLATRuntimeException("error.manifest.missing", null, this.getClass().getPackage().getName(), "CP " + rootContainer + " has no imsmanifest", null);
        }
        // initialize tree model in any case
        ctm = new CPManifestTreeModel((VFSLeaf) mani);

        if (showMenu) {
            // the menu is only initialized when needed.
            cpTree = new MenuTree("cpDisplayTree");
            cpTree.setTreeModel(ctm);
            cpTree.addListener(this);
        }

        LoggingResourceable nodeInfo = null;
        if (activateFirstPage) {
            // set content to first accessible child or root node if no children
            // available
            TreeNode node = ctm.getRootNode();
            if (node == null) {
                throw new OLATRuntimeException(CPDisplayController.class, "root node of content packaging was null, file:" + rootContainer, null);
            }
            while (node != null && !node.isAccessible()) {
                if (node.getChildCount() > 0) {
                    node = (TreeNode) node.getChildAt(0);
                } else {
                    node = null;
                }
            }
            if (node != null) { // node.isAccessible
                final String nodeUri = (String) node.getUserObject();
                if (cpContentCtr != null) {
                    cpContentCtr.setCurrentURI(nodeUri);
                }
                if (cpComponent != null) {
                    cpComponent.setCurrentURI(nodeUri);
                }
                if (showMenu) {
                    cpTree.setSelectedNodeId(node.getIdent());
                }
                // activate the selected node in the menu (skips the root node that is
                // empty anyway and saves one user click)
                selNodeId = node.getIdent();

                nodeInfo = LoggingResourceable.wrapCpNode(nodeUri);
            }
        } else if (initialUri != null) {
            // set page
            if (cpContentCtr != null) {
                cpContentCtr.setCurrentURI(initialUri);
            }
            if (cpComponent != null) {
                cpComponent.setCurrentURI(initialUri);
            }
            // update menu
            final TreeNode newNode = ctm.lookupTreeNodeByHref(initialUri);
            if (newNode != null) { // user clicked on a link which is listed in the
                                   // toc
                if (cpTree != null) {
                    cpTree.setSelectedNodeId(newNode.getIdent());
                } else {
                    selNodeId = newNode.getIdent();
                }
            }
            nodeInfo = LoggingResourceable.wrapCpNode(initialUri);
        }
        // Note: the ores has a typename of ICourse - see
        // CPCourseNode.createNodeRunConstructorResult
        // which has the following line:
        // OresHelper.createOLATResourceableInstance(ICourse.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
        // therefore we use OresHelper.calculateTypeName(ICourse.class) here
        if (ores != null && nodeInfo != null && !OresHelper.calculateTypeName(ICourse.class).equals(ores.getResourceableTypeName())) {
            addLoggingResourceable(LoggingResourceable.wrap(ores, OlatResourceableType.cp));
            ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass(), nodeInfo);
        }

        putInitialPanel(myContent);
    }

    public void setContentEncoding(final String encoding) {
        if (cpContentCtr != null) {
            cpContentCtr.setContentEncoding(encoding);
        }
    }

    public void setJSEncoding(final String encoding) {
        if (cpContentCtr != null) {
            cpContentCtr.setJSEncoding(encoding);
        }
    }

    /**
     * @return The menu component for this content packaging. The Controller must be initialized properly to use this method
     */
    Component getMenuComponent() {
        return cpTree;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == cpTree) {
            // FIXME:fj: cleanup between MenuTree.COMMAND_TREENODE_CLICKED and
            // TreeEvent.dito...
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                final TreeEvent te = (TreeEvent) event;
                switchToPage(ureq, te);
            }
        } else if (source == cpComponent) {
            if (event instanceof NewInlineUriEvent) {
                final NewInlineUriEvent nue = (NewInlineUriEvent) event;
                // adjust the tree selection to the current choice if found
                selectTreeNode(ureq, nue.getNewUri());
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == cpContentCtr) { // a .html click within the contentpackage
            if (event instanceof NewInlineUriEvent) {
                final NewInlineUriEvent nue = (NewInlineUriEvent) event;
                // adjust the tree selection to the current choice if found
                selectTreeNode(ureq, nue.getNewUri());
            } else if (event instanceof NewIframeUriEvent) {
                final NewIframeUriEvent nue = (NewIframeUriEvent) event;
                selectTreeNode(ureq, nue.getNewUri());
            }// else ignore (e.g. misplaced olatcmd event (inner olat link found in a
             // contentpackaging file)
        }
    }

    /**
     * adjust the cp menu tree with the page selected with a link clicked in the content
     * 
     * @param ureq
     * @param newUri
     */
    public void selectTreeNode(final UserRequest ureq, final String newUri) {
        final TreeNode newNode = ctm.lookupTreeNodeByHref(newUri);
        if (newNode != null) { // user clicked on a link which is listed in the
            // toc
            if (cpTree != null) {
                cpTree.setSelectedNodeId(newNode.getIdent());
            } else {
                // for the case the tree is outside this controller (e.g. in the
                // course), we fire an event with the chosen node)
                fireEvent(ureq, new TreeNodeEvent(newNode));
            }
        }
        ThreadLocalUserActivityLogger.log(CourseLoggingAction.CP_GET_FILE, getClass(), LoggingResourceable.wrapCpNode(newUri));
    }

    /**
     * @param ureq
     * @param te
     */
    public void switchToPage(final UserRequest ureq, final TreeEvent te) {
        // all treeevents receiced here are event clicked only
        // if (!te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) throw
        // new AssertException("error");

        // switch to the new page
        final String nodeId = te.getNodeId();
        final TreeNode tn = ctm.getNodeById(nodeId);
        final String identifierRes = (String) tn.getUserObject();

        // security check
        if (identifierRes.indexOf("../") != -1) {
            throw new AssertException("a non-normalized url encountered in a manifest item:" + identifierRes);
        }

        // Check if path ends with .html, .htm or .xhtml. We do this by searching for "htm"
        // and accept positions of this string at length-3 or length-4
        // Check also for XML resources that use XSLT for rendering
        if (identifierRes.toLowerCase().lastIndexOf(FILE_SUFFIX_HTM) >= (identifierRes.length() - 4) || identifierRes.toLowerCase().endsWith(FILE_SUFFIX_XML)) {
            // display html files inline or in an iframe
            if (cpContentCtr != null) {
                cpContentCtr.setCurrentURI(identifierRes);
            }
            if (cpComponent != null) {
                cpComponent.setCurrentURI(identifierRes);
            }

        } else {
            // Also display pdf and other files in the iframe if it has been
            // initialized. Delegates displaying to the browser (and its plugins).
            if (cpContentCtr != null) {
                cpContentCtr.setCurrentURI(identifierRes);
            } else {
                // if an entry in a manifest points e.g. to a pdf file and the iframe
                // controller has not been initialized display it non-inline
                final VFSItem currentItem = rootContainer.resolve(identifierRes);
                MediaResource mr;
                if (currentItem == null || !(currentItem instanceof VFSLeaf)) {
                    mr = new NotFoundMediaResource(identifierRes);
                } else {
                    mr = new VFSMediaResource((VFSLeaf) currentItem);
                }
                ureq.getDispatchResult().setResultingMediaResource(mr);
                // Prevent 'don't reload' warning
                cpTree.setDirty(false);
            }
        }
        ThreadLocalUserActivityLogger.log(CourseLoggingAction.CP_GET_FILE, getClass(), LoggingResourceable.wrapCpNode(identifierRes));
    }

    /**
	 */
    @Override
    protected void doDispose() {
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());
        cpTree = null;
        ctm = null;
        myContent = null;
        rootContainer = null;
        cpComponent = null;
    }

    /**
     * @return the treemodel. (for read-only usage) Useful if you would like to integrate the menu at some other place
     */
    public CPManifestTreeModel getTreeModel() {
        return ctm;
    }

    /**
     * @param ureq
     * @param te
     * @deprecated @TODO To be deleted - does logging and would have to go via an event() method
     */
    @Deprecated
    public void externalNodeClicked(final UserRequest ureq, final TreeEvent te) {
        switchToPage(ureq, te);
    }

    /**
     * to use with the option "external menu" only
     * 
     * @return
     */
    public String getInitialSelectedNodeId() {
        return selNodeId;
    }

    public String getNodeByUri(final String uri) {
        if (StringHelper.containsNonWhitespace(uri)) {
            final TreeNode node = ctm.lookupTreeNodeByHref(uri);
            if (node != null) {
                return node.getIdent();
            }
        }
        return getInitialSelectedNodeId();
    }
}
