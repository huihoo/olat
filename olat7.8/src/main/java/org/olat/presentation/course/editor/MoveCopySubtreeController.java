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

package org.olat.presentation.course.editor;

import org.olat.lms.activitylogging.CourseLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.tree.TreePosition;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.commons.tree.Visitor;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.presentation.course.tree.InsertTreeModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: guido Class Description for MoveCopySubtreeController
 */
public class MoveCopySubtreeController extends BasicController {

    private static final String LOG_COURSENODE_COPIED = "COURSENODE_COPIED";
    private static final String LOG_COURSENODE_MOVED = "COURSENODE_MOVED";

    private final CourseEditorTreeNode moveCopyFrom;
    private final boolean copy;

    private final SelectionTree insertTree;
    private final InsertTreeModel insertModel;
    private String copyNodeId = null;
    private final OLATResourceable ores;

    public MoveCopySubtreeController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final CourseEditorTreeNode moveCopyFrom,
            final boolean copy) {
        super(ureq, wControl);
        this.ores = ores;
        this.moveCopyFrom = moveCopyFrom;
        this.copy = copy;

        final ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
        addLoggingResourceable(LoggingResourceable.wrap(course));
        addLoggingResourceable(LoggingResourceable.wrap(moveCopyFrom.getCourseNode()));

        insertTree = new SelectionTree("copy_node_selection", getTranslator());
        insertTree.setFormButtonKey("insertAtSelectedTreepos");
        insertTree.addListener(this);
        insertModel = new InsertTreeModel(course.getEditorTreeModel());
        insertTree.setTreeModel(insertModel);

        final VelocityContainer mainVC = createVelocityContainer("moveCopyNode");

        if (insertModel.totalNodeCount() > CourseModule.getCourseNodeLimit()) {
            final String msg = getTranslator().translate("warning.containsXXXormore.nodes",
                    new String[] { String.valueOf(insertModel.totalNodeCount()), String.valueOf(CourseModule.getCourseNodeLimit() + 1) });
            final Controller tmp = MessageUIFactory.createWarnMessage(ureq, wControl, null, msg);
            listenTo(tmp);
            mainVC.put("nodelimitexceededwarning", tmp.getInitialComponent());
        }

        mainVC.put("selection", insertTree);

        this.putInitialPanel(mainVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
        copyNodeId = null; // initialize copyNodeId with null because a new event happens and old value is invalid.
        if (source == insertTree) {
            final TreeEvent te = (TreeEvent) event;
            if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
                // user chose a position to insert a new node
                final String nodeId = te.getNodeId();
                final TreePosition tp = insertModel.getTreePosition(nodeId);
                final CourseNode selectedNode = insertModel.getCourseNode(tp.getParentTreeNode());
                final CourseEditorTreeNode insertParent = course.getEditorTreeModel().getCourseEditorNodeById(selectedNode.getIdent());

                // check if insert position is within the to-be-copied tree
                if (checkIfIsChild(insertParent, moveCopyFrom)) {
                    this.showError("movecopynode.error.overlap");
                    fireEvent(ureq, Event.CANCELLED_EVENT);
                    return;
                }

                int insertPos = tp.getChildpos();
                if (copy) { // do a copy
                    // copy subtree and save model
                    recursiveCopy(moveCopyFrom, insertParent, insertPos, true, CourseFactory.getCourseEditSession(ores.getResourceableId()));
                    CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());

                    ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_COPIED, getClass());
                    fireEvent(ureq, Event.DONE_EVENT);
                } else { // move only
                    if (insertParent.getIdent().equals(moveCopyFrom.getParent().getIdent())) {
                        // same parent, adjust insertPos
                        if (insertPos > moveCopyFrom.getPosition()) {
                            insertPos--;
                        }
                    }
                    insertParent.insert(moveCopyFrom, insertPos);

                    moveCopyFrom.setDirty(true);
                    // mark subtree as dirty
                    final TreeVisitor tv = new TreeVisitor(new Visitor() {
                        @Override
                        public void visit(final INode node) {
                            final CourseEditorTreeNode cetn = (CourseEditorTreeNode) node;
                            cetn.setDirty(true);
                        }
                    }, moveCopyFrom, true);
                    tv.visitAll();
                    CourseFactory.saveCourseEditorTreeModel(course.getResourceableId()); // TODO: pb: Review : Add by chg to FIX OLAT-1662
                    this.showInfo("movecopynode.info.condmoved");

                    ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_MOVED, getClass());
                    fireEvent(ureq, Event.DONE_EVENT);
                }
            } else {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    private void recursiveCopy(final CourseEditorTreeNode copyFrom2, final CourseEditorTreeNode insertParent, final int pos, final boolean firstIteration,
            final ICourse course) {
        // create copy of course node
        final CourseNode copyOfNode = copyFrom2.getCourseNode().createInstanceForCopy(firstIteration);
        copyNodeId = copyOfNode.getIdent();
        // Insert at desired position
        course.getEditorTreeModel().insertCourseNodeAt(copyOfNode, insertParent.getCourseNode(), pos);
        final CourseEditorTreeNode insertedEditorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(copyOfNode.getIdent());
        for (int i = 0; i < copyFrom2.getChildCount(); i++) {
            recursiveCopy(course.getEditorTreeModel().getCourseEditorNodeById(copyFrom2.getChildAt(i).getIdent()), insertedEditorTreeNode, i, false, course);
        }
    }

    /**
     * Check if prospectChild is a child of sourceTree.
     * 
     * @param prospectChild
     * @param sourceTree
     * @return
     */
    private boolean checkIfIsChild(final CourseEditorTreeNode prospectChild, final CourseEditorTreeNode sourceTree) {
        // FIXME:ms:b would it be simpler to check the parents?
        // INode par;
        // for (par = prospectChild.getParent(); par != null && par != sourceTree;
        // par = par.getParent());
        // return (par == sourceTree);
        final ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
        if (sourceTree.getIdent().equals(prospectChild.getIdent())) {
            return true;
        }
        for (int i = 0; i < sourceTree.getChildCount(); i++) {
            final INode child = sourceTree.getChildAt(i);
            if (checkIfIsChild(prospectChild, course.getEditorTreeModel().getCourseEditorNodeById(child.getIdent()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    /**
     * Returns node-id of a new copied node.
     * 
     * @return Returns null when no copy-workflow happens.
     */
    public String getCopyNodeId() {
        return copyNodeId;
    }

}
