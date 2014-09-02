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
package org.olat.lms.course.wizard.create;

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.presentation.course.editor.PublishProcess;
import org.olat.presentation.course.editor.StatusDescription;

/**
 * Initial Date: 11.06.2012 <br>
 * 
 * @author cg
 */
public class CoursePublishHelper {

    public static void publish(ICourse course, Locale locale, Identity publisherIdentity, List<String> publishNodeIds) {
        CourseFactory.openCourseEditSession(course.getResourceableId());
        CourseEditorTreeModel cetm = course.getEditorTreeModel();
        PublishProcess pp = PublishProcess.getInstance(course, cetm, locale);
        pp.createPublishSetFor(publishNodeIds);
        StatusDescription[] sds = pp.testPublishSet(locale);
        // OLD Comment, exist before campuskurs-refactoring
        // final boolean isValid = sds.length == 0;
        // if (!isValid) {
        // // no error and no warnings -> return immediate
        // }
        pp.applyPublishSet(publisherIdentity, locale);
        CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
    }

}
