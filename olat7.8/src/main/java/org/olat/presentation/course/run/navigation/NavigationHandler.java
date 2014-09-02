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

package org.olat.presentation.course.run.navigation;

import org.apache.log4j.Logger;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.TreeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description: <br>
 * TODO: Felix Jost Class Description for NavigationHandler Initial Date: 19.01.2005 <br>
 * 
 * @author Felix Jost
 */
public class NavigationHandler {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String LOG_NODE_ACCESS = "NODE_ACCESS";
    private static final String LOG_NODE_NO_ACCESS = "NODE_NO_ACCESS";

    private final UserCourseEnvironment userCourseEnv;
    private final boolean previewMode;

    // remember so subsequent click to a subtreemodel's node has a handler
    private ControllerEventListener subtreemodelListener = null;

    /**
     * @param userCourseEnv
     * @param previewMode
     */
    public NavigationHandler(final UserCourseEnvironment userCourseEnv, final boolean previewMode) {
        this.userCourseEnv = userCourseEnv;
        this.previewMode = previewMode;
    }

    /**
     * to be called upon entering a course. <br>
     * 
     * @param ureq
     * @param wControl
     * @return NodeClickedRef
     * @param calledCourseNode
     *            the coursenode to jump to; if null, the root coursenode is selected
     * @param listeningController
     */
    public NodeClickedRef evaluateJumpToCourseNode(final UserRequest ureq, final WindowControl wControl, final CourseNode calledCourseNode,
            final ControllerEventListener listeningController, final String nodecmd) {
        CourseNode cn;
        if (calledCourseNode == null) {
            // indicate to jump to root course node
            cn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
        } else {
            cn = calledCourseNode;
        }
        return doEvaluateJumpTo(ureq, wControl, cn, listeningController, nodecmd);
    }

    /**
     * to be called when the users clickes on a node when in the course
     * 
     * @param ureq
     * @param wControl
     * @param treeModel
     * @param treeEvent
     * @param listeningController
     * @param nodecmd
     *            null or a subcmd which activates a node-specific view (e.g. opens a certain uri in a contentpackaging- buildingblock)
     * @return the NodeClickedRef
     * @return currentNodeController the current node controller that will be dispose before creating the new one
     */
    public NodeClickedRef evaluateJumpToTreeNode(final UserRequest ureq, final WindowControl wControl, final TreeModel treeModel, final TreeEvent treeEvent,
            final ControllerEventListener listeningController, final String nodecmd, final Controller currentNodeController) {
        NodeClickedRef ncr;
        final String treeNodeId = treeEvent.getNodeId();
        final TreeNode selTN = treeModel.getNodeById(treeNodeId);
        if (selTN == null) {
            throw new AssertException("no treenode found:" + treeNodeId);
        }

        // check if appropriate for subtreemodelhandler
        final Object userObject = selTN.getUserObject();
        if (!(userObject instanceof NodeEvaluation)) {
            // yes, appropriate
            if (subtreemodelListener == null) {
                throw new AssertException("no handler for subtreemodelcall!");
            }
            if (log.isDebugEnabled()) {
                log.debug("delegating to handler: treeNodeId = " + treeNodeId);
            }
            // null as controller source since we are not a controller
            subtreemodelListener.dispatchEvent(ureq, null, treeEvent);
            // no node construction result indicates handled
            ncr = new NodeClickedRef(null, true, null, null, null);
        } else {
            // normal dispatching to a coursenode.
            // get the courseNode that was called
            final NodeEvaluation prevEval = (NodeEvaluation) selTN.getUserObject();
            if (!prevEval.isVisible()) {
                throw new AssertException("clicked on a node which is not visible: treenode=" + selTN.getIdent() + ", " + selTN.getTitle());
            }
            final CourseNode calledCourseNode = prevEval.getCourseNode();
            ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(calledCourseNode));
            // dispose old node controller before creating the NodeClickedRef which creates
            // the new node controller. It is important that the old node controller is
            // disposed before the new one to not get conflicts with cacheable mappers that
            // might be used in both controllers with the same ID (e.g. the course folder)
            if (currentNodeController != null) {
                currentNodeController.dispose();
            }
            ncr = doEvaluateJumpTo(ureq, wControl, calledCourseNode, listeningController, nodecmd);
        }
        return ncr;

    }

    private NodeClickedRef doEvaluateJumpTo(final UserRequest ureq, final WindowControl wControl, final CourseNode courseNode,
            final ControllerEventListener listeningController, final String nodecmd) {
        NodeClickedRef nclr;
        if (log.isDebugEnabled()) {
            log.debug("evaluateJumpTo courseNode = " + courseNode.getIdent() + ", " + courseNode.getShortName());
        }

        // build the new treemodel by evaluating the preconditions
        final TreeEvaluation treeEval = new TreeEvaluation();
        final GenericTreeModel treeModel = new GenericTreeModel();
        final CourseNode rootCn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
        final NodeEvaluation rootNodeEval = rootCn.eval(userCourseEnv.getConditionInterpreter(), treeEval, true);
        final TreeNode treeRoot = rootNodeEval.getTreeNode();
        treeModel.setRootNode(treeRoot);

        // find the treenode that corresponds to the node (!= selectedTreeNode since
        // we built the TreeModel anew in the meantime
        final TreeNode newCalledTreeNode = treeEval.getCorrespondingTreeNode(courseNode);
        if (newCalledTreeNode == null) {
            // the clicked node is not visible anymore!
            // if the new calculated model does not contain the selected node anymore
            // (because of visibility changes of at least one of the ancestors
            // -> issue an user infomative msg
            // nclr: the new treemodel, not visible, no selected nodeid, no
            // calledcoursenode, no nodeconstructionresult
            nclr = new NodeClickedRef(treeModel, false, null, null, null);
        } else {
            // calculate the NodeClickedRef
            // 1. get the correct (new) nodeevaluation
            final NodeEvaluation nodeEval = (NodeEvaluation) newCalledTreeNode.getUserObject();
            if (nodeEval.getCourseNode() != courseNode) {
                throw new AssertException("error in structure");
            }
            if (!nodeEval.isVisible()) {
                throw new AssertException("node eval not visible!!");
            }
            // 2. start with the current NodeEvaluation, evaluate overall accessiblity
            // per node bottom-up to see if all ancestors still grant access to the
            // desired node
            final boolean mayAccessWholeTreeUp = mayAccessWholeTreeUp(nodeEval);
            final String newSelectedNodeId = newCalledTreeNode.getIdent();
            if (!mayAccessWholeTreeUp) {
                // we cannot access the node anymore (since e.g. a time constraint
                // changed), so give a (per-node-configured) explanation why and what
                // the access conditions would be (a free form text, should be
                // nontechnical).
                // NOTE: we do not take into account what node caused the non-access by
                // being !isAtLeastOneAccessible, but always state the
                // NoAccessExplanation of the Node originally called by the user
                final String explan = courseNode.getNoAccessExplanation();
                final String sExplan = (explan == null ? "" : Formatter.formatLatexFormulas(explan));
                final Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, null, sExplan);
                // write log information
                ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_NAVIGATION_NODE_NO_ACCESS, getClass(), LoggingResourceable.wrap(courseNode));
                final NodeRunConstructionResult ncr = new NodeRunConstructionResult(controller, null, null, null);
                // nclr: the new treemodel, visible, selected nodeid, calledcoursenode,
                // nodeconstructionresult
                nclr = new NodeClickedRef(treeModel, true, newSelectedNodeId, courseNode, ncr);
            } else { // access ok
                // access the node, display its result in the right pane
                NodeRunConstructionResult ncr;

                // calculate the new businesscontext for the coursenode being called.
                // type: class of node; key = node.getIdent;

                final Class<CourseNode> oresC = CourseNode.class; // don't use the concrete instance since for the course: to jump to a coursenode with a given id is all
                                                                  // there
                // is to know
                final Long oresK = new Long(Long.parseLong(courseNode.getIdent()));
                final OLATResourceable ores = OresHelper.createOLATResourceableInstance(oresC, oresK);

                // REVIEW:pb:this is responsible for building up the jumpable businesspath/REST URL kind of
                final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ores);
                final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);

                if (previewMode) {
                    ncr = new NodeRunConstructionResult(courseNode.createPreviewController(ureq, bwControl, userCourseEnv, nodeEval));
                } else {
                    ncr = courseNode.createNodeRunConstructionResult(ureq, bwControl, userCourseEnv, nodeEval, nodecmd);

                    // remember as instance variable for next click
                    subtreemodelListener = ncr.getSubTreeListener();
                    if (subtreemodelListener != null) {
                        addSubTreeModel(newCalledTreeNode, ncr.getSubTreeModel());
                    }
                }

                // nclr: the new treemodel, visible, selected nodeid, calledcoursenode,
                // nodeconstructionresult
                nclr = new NodeClickedRef(treeModel, true, newSelectedNodeId, courseNode, ncr);
                // attach listener; we know we have a runcontroller here
                if (listeningController != null) {
                    nclr.getRunController().addControllerListener(listeningController);
                }
                // write log information
                ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_NAVIGATION_NODE_ACCESS, getClass(), LoggingResourceable.wrap(courseNode));
            }
        }
        return nclr;
    }

    private void addSubTreeModel(final TreeNode parent, final TreeModel modelToAppend) {
        // ignore root and directly add children.
        // need to clone children so that are not detached from their original
        // parent (which is the cp treemodel)
        // parent.addChild(modelToAppend.getRootNode());
        final TreeNode root = modelToAppend.getRootNode();
        final int chdCnt = root.getChildCount();

        // full cloning of ETH webclass energie takes about 4/100 of a second
        for (int i = chdCnt; i > 0; i--) {
            final TreeNode chd = (TreeNode) root.getChildAt(i - 1);
            final TreeNode chdc = (TreeNode) XStreamHelper.xstreamClone(chd);
            // always insert before already existing course building block children
            parent.insert(chdc, 0);
        }
    }

    /**
     * @param ne
     * @return
     */
    public static boolean mayAccessWholeTreeUp(final NodeEvaluation ne) {
        NodeEvaluation curNodeEval = ne;
        boolean mayAccess;
        do {
            mayAccess = curNodeEval.isAtLeastOneAccessible();
            curNodeEval = (NodeEvaluation) curNodeEval.getParent();
        } while (curNodeEval != null && mayAccess);
        // top reached or may not access node
        return mayAccess;
    }

}
