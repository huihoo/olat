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

package org.olat.lms.core.course.campus.impl.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.course.wizard.create.CoursePublishHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
@Component
public class CoursePublisher {

    public void publish(ICourse course, Identity publisherIdentity) {
        Locale locale = new Locale("de");// needed to re-use code in CourseCreationHelper
        CoursePublishHelper.publish(course, locale, publisherIdentity, getAllPublishNodeIds(course.getEditorTreeModel()));
    }

    // Package visible for testing
    List<String> getAllPublishNodeIds(CourseEditorTreeModel editorTreeModel) {
        final List<String> nodeIds = new ArrayList<String>();
        addChildNodeIdRecursive(nodeIds, editorTreeModel.getRootNode());
        return nodeIds;
    }

    private void addChildNodeIdRecursive(List<String> nodeIds, INode node) {
        nodeIds.add(node.getIdent());
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChildAt(i).getClass() == CourseEditorTreeNode.class) {
                // CourseEditorTreeNodes will only be published if new, dirty or deleted
                final CourseEditorTreeNode child = (CourseEditorTreeNode) node.getChildAt(i);
                if (child.isNewnode() || child.isDirty() || child.isDeleted()) {
                    addChildNodeIdRecursive(nodeIds, node.getChildAt(i));
                }
            } else {
                addChildNodeIdRecursive(nodeIds, node.getChildAt(i));
            }
        }
    }

}
