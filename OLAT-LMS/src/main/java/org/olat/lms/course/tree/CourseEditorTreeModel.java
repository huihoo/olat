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

package org.olat.lms.course.tree;

import org.apache.log4j.Logger;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.util.ObjectCloner;
import org.olat.lms.course.Structure;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class CourseEditorTreeModel extends GenericTreeModel {
    private long latestPublishTimestamp = -1;
    private long highestNodeId; // start at Long.MAX_VALUE - 1000000; if set to
                                // zero -> meaning we read from an old
                                // xml-structure which set it to zero, since it
                                // did not exist
    transient private final static int CURRENTVERSION = 2;
    private int version;
    private static final Logger log = LoggerHelper.getLogger();

    /**
	 * 
	 */
    public CourseEditorTreeModel() {
        highestNodeId = Long.MAX_VALUE - 1000000;
        this.version = CURRENTVERSION;
    }

    /**
     * @param nodeId
     * @return the course node
     */
    public CourseNode getCourseNode(final String nodeId) {
        final CourseEditorTreeNode ctn = (CourseEditorTreeNode) getNodeById(nodeId);
        final CourseNode cn = ctn.getCourseNode();
        return cn;
    }

    /**
     * @param newNode
     * @param parentNode
     * @param pos
     */
    public void insertCourseNodeAt(final CourseNode newNode, final CourseNode parentNode, final int pos) {
        // update editor tree model
        final CourseEditorTreeNode ctnParent = (CourseEditorTreeNode) getNodeById(parentNode.getIdent());
        if (ctnParent == null) {
            throw new AssertException("Corrupt CourseEditorTreeModel.");
        }
        final CourseEditorTreeNode newCetn = new CourseEditorTreeNode(newNode);
        newCetn.setNewnode(true);
        newCetn.setDirty(true);
        ctnParent.insert(newCetn, pos);
        log.debug("insertCourseNodeAt - nodeId: " + newNode.getIdent());
    }

    /**
     * append new course
     * 
     * @param newNode
     * @param parentNode
     */
    public void addCourseNode(final CourseNode newNode, final CourseNode parentNode) {
        // update editor tree model
        final CourseEditorTreeNode ctnParent = (CourseEditorTreeNode) getNodeById(parentNode.getIdent());
        if (ctnParent == null) {
            throw new AssertException("Corrupt CourseEditorTreeModel.");
        }
        final CourseEditorTreeNode newCetn = new CourseEditorTreeNode(newNode);
        newCetn.setNewnode(true);
        newCetn.setDirty(true);
        ctnParent.addChild(newCetn);
        log.debug("addCourseNode - nodeId: " + newNode.getIdent());
    }

    /**
     * marks an couse node and all it's children as deleted
     * 
     * @param courseNode
     */
    public void markDeleted(final CourseNode courseNode) {
        final CourseEditorTreeNode cetNode = (CourseEditorTreeNode) getNodeById(courseNode.getIdent());
        if (cetNode == null) {
            throw new AssertException("Corrupt CourseEditorTreeModel.");
        }
        markDeleted(cetNode);
    }

    /**
     * marks an couse node and all it's children as undeleted
     * 
     * @param courseNode
     */
    public void markUnDeleted(final CourseNode courseNode) {
        final CourseEditorTreeNode cetNode = (CourseEditorTreeNode) getNodeById(courseNode.getIdent());
        if (cetNode == null) {
            throw new AssertException("Corrupt CourseEditorTreeModel.");
        }
        markUnDeleted(cetNode);
    }

    private void markDeleted(final CourseEditorTreeNode cetNode) {
        cetNode.setDeleted(true);
        cetNode.setDirty(true);
        if (cetNode.getChildCount() > 0) {
            for (int i = 0; i < cetNode.getChildCount(); i++) {
                markDeleted((CourseEditorTreeNode) cetNode.getChildAt(i));
            }
        }
    }

    public void markUnDeleted(final CourseEditorTreeNode cetNode) {
        cetNode.setDeleted(false);
        cetNode.setDirty(true);
        if (cetNode.getChildCount() > 0) {
            for (int i = 0; i < cetNode.getChildCount(); i++) {
                markUnDeleted((CourseEditorTreeNode) cetNode.getChildAt(i));
            }
        }
    }

    /**
     * @deprecated REVIEW:pb: no longer used? it is not referenced from java, and also not found in velocity *.html
     * @param courseNode
     */
    @Deprecated
    public void removeCourseNode(final CourseNode courseNode) {
        final CourseEditorTreeNode cetNode = (CourseEditorTreeNode) getNodeById(courseNode.getIdent());
        if (cetNode == null) {
            throw new AssertException("Corrupt CourseEditorTreeModel.");
        }
        cetNode.removeFromParent();
    }

    /**
     * @param nodeId
     * @return null if not found, or the <code>CourseEditorTreeNode</code> with the given nodeId
     */
    public CourseEditorTreeNode getCourseEditorNodeById(final String nodeId) {
        return (CourseEditorTreeNode) getNodeById(nodeId);
    }

    public CourseEditorTreeNode getCourseEditorNodeContaining(final CourseNode cn) {
        final String nodeId = cn.getIdent();
        return getCourseEditorNodeById(nodeId);
    }

    /**
     * @param courseNode
     */
    public void nodeConfigChanged(final INode node) {
        final CourseEditorTreeNode changedNode = (CourseEditorTreeNode) getNodeById(node.getIdent());
        if (changedNode == null) {
            throw new AssertException("Corrupt course editor tree model.");
        }
        changedNode.setDirty(true);
    }

    public long getLatestPublishTimestamp() {
        return latestPublishTimestamp;
    }

    /**
     * @param latestPublishTimestamp
     *            The latestPublishTimestamp to set.
     */
    public void setLatestPublishTimestamp(final long latestPublishTimestamp) {
        this.latestPublishTimestamp = latestPublishTimestamp;
    }

    /**
     * FIXME: use this method for node generation
     * 
     * @return the highest used node id so far
     */
    public long getHighestNodeId() {
        return highestNodeId;
    }

    /**
     * increments the highestnodeid: for the next new node in the editor. does not persist.
     */
    public void incHighestNodeId() {
        highestNodeId++;
    }

    /**
     * @return a deep clone of the current (run) structure of this editortreemodel
     */
    public Structure createStructureForPreview() {
        final CourseEditorTreeNode cetn = (CourseEditorTreeNode) getRootNode();
        final CourseNode clone = buildUp(cetn);
        final Structure structure = new Structure();
        structure.setRootNode(clone);
        return structure;
    }

    private CourseNode buildUp(final CourseEditorTreeNode cetn) {
        final CourseNode attachedNode = cetn.getCourseNode();
        // clone current
        final CourseNode cloneCn = (CourseNode) ObjectCloner.deepCopy(attachedNode);
        // visit all children
        final int chdCnt = cetn.getChildCount();
        for (int i = 0; i < chdCnt; i++) {
            final CourseEditorTreeNode child = cetn.getCourseEditorTreeNodeChildAt(i);
            // only add if not deleted and configuration is valid
            if (!child.isDeleted() && !(child.getCourseNode().isConfigValid().isError())) {
                final CourseNode res = buildUp(child);
                cloneCn.addChild(res);
            }
        }
        return cloneCn;

    }

    /**
     * @return Returns the version.
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version
     *            The version to set.
     */
    public void setVersion(final int version) {
        this.version = version;
    }

    public boolean isVersionUpToDate() {
        if (Integer.valueOf(version) == null || version < CURRENTVERSION) {
            return false;
        }
        return true;
    }
}
