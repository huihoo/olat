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
package org.olat.lms.course;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;

/**
 * Initial Date: 07.06.2012 <br>
 * 
 * @author cg
 */
public class CourseTitleHelper {

    public static void saveCourseTitleInCourseModel(ICourse course, String title) {
        saveCourseTitleInCourseModel(course, title, null, true);
    }

    public static void saveCourseTitleInCourseModel(ICourse course, String title, Translator translator, boolean defaultTemplate) {
        CourseFactory.openCourseEditSession(course.getResourceableId());
        course.getRunStructure().getRootNode().setShortTitle(Formatter.truncate(title, CourseNode.SHORT_TITLE_MAX_LENGTH));
        course.getRunStructure().getRootNode().setLongTitle(title);
        if (!defaultTemplate) {
            course.getRunStructure().getRootNode().setLearningObjectives(translator.translate("campus.course.learningObj"));
        }
        CourseFactory.saveCourse(course.getResourceableId());
        CourseEditorTreeModel cetm = course.getEditorTreeModel();
        CourseNode rootNode = cetm.getCourseNode(course.getRunStructure().getRootNode().getIdent());
        rootNode.setShortTitle(Formatter.truncate(title, CourseNode.SHORT_TITLE_MAX_LENGTH));
        rootNode.setLongTitle(title);
        if (!defaultTemplate) {
            rootNode.setLearningObjectives(translator.translate("campus.course.learningObj"));
            cetm.setLatestPublishTimestamp(-1);
        }
        course.getEditorTreeModel().nodeConfigChanged(course.getRunStructure().getRootNode());
        CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
        CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
    }
}
