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
package org.olat.presentation.course.nodes.iq;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.IQSELFCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;

/**
 * Description:<br>
 * TODO: patrickb Class Description for IQUIFactory
 * <P>
 * Initial Date: 18.06.2010 <br>
 * 
 * @author patrickb
 */
public class IQUIFactory {

    /**
     * [SPRING] builds the SpecificControllerCreator as argument in the constructor. This "extension point" was created during ONYX integration review. It delegates
     * creation to the factory instead of using new XXXController(..) creation of different IQxyzRun / IQxyzEdit / IQxyzPreviewControllers within the IQxyzCourseNodes.
     */
    static IQControllerCreator iqControllerCreator = null;

    IQUIFactory(final IQControllerCreator specificIqControllerCreator) {
        IQUIFactory.iqControllerCreator = specificIqControllerCreator;
    }

    public static TabbableController createIQTestEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course,
            final IQTESTCourseNode courseNode, final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        return IQUIFactory.iqControllerCreator.createIQTestEditController(ureq, wControl, course, courseNode, groupMgr, euce);
    }

    public static TabbableController createIQSelftestEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course,
            final IQSELFCourseNode courseNode, final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        return IQUIFactory.iqControllerCreator.createIQSelftestEditController(ureq, wControl, course, courseNode, groupMgr, euce);
    }

    public static TabbableController createIQSurveyEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course,
            final IQSURVCourseNode courseNode, final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        return IQUIFactory.iqControllerCreator.createIQSurveyEditController(ureq, wControl, course, courseNode, groupMgr, euce);
    }

    public static Controller createIQTestRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQTESTCourseNode courseNode) {
        return IQUIFactory.iqControllerCreator.createIQTestRunController(ureq, wControl, userCourseEnv, ne, courseNode);
    }

    public static Controller createIQTestPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQTESTCourseNode courseNode) {
        return IQUIFactory.iqControllerCreator.createIQTestPreviewController(ureq, wControl, userCourseEnv, ne, courseNode);
    }

    public static Controller createIQSelftestRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQSELFCourseNode courseNode) {
        return IQUIFactory.iqControllerCreator.createIQSelftestRunController(ureq, wControl, userCourseEnv, ne, courseNode);
    }

    public static Controller createIQSurveyRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQSURVCourseNode courseNode) {
        return IQUIFactory.iqControllerCreator.createIQSurveyRunController(ureq, wControl, userCourseEnv, ne, courseNode);
    }

    public static Controller createIQTestDetailsEditController(final Long courseResourceableId, final String ident, final Identity identity,
            final RepositoryEntry referencedRepositoryEntry, final String qmdEntryTypeAssess, final UserRequest ureq, final WindowControl wControl) {
        return IQUIFactory.iqControllerCreator.createIQTestDetailsEditController(courseResourceableId, ident, identity, referencedRepositoryEntry, qmdEntryTypeAssess,
                ureq, wControl);
    }

}
