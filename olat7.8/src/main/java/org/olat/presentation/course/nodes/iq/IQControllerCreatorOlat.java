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
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.nodes.CourseIQSecurityCallback;
import org.olat.lms.course.nodes.IQSELFCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.ims.qti.IQSecurityCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.ims.qti.QTIResultDetailsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * TODO: patrickb Class Description for IQControllerCreatorOlat
 * <P>
 * Initial Date: 18.06.2010 <br>
 * 
 * @author patrickb
 */
public class IQControllerCreatorOlat implements IQControllerCreator {

    protected IQControllerCreatorOlat() {
    }

    @Autowired
    LockingService lockingService;

    /**
     * The iq test edit screen in the course editor.
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     * @param groupMgr
     * @param euce
     * @return
     */
    @Override
    public TabbableController createIQTestEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final IQTESTCourseNode courseNode,
            final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        return new IQEditController(ureq, wControl, course, courseNode, groupMgr, euce);
    }

    /**
     * The iq test edit screen in the course editor.
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     * @param groupMgr
     * @param euce
     * @return
     */
    @Override
    public TabbableController createIQSelftestEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course,
            final IQSELFCourseNode courseNode, final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        return new IQEditController(ureq, wControl, course, courseNode, groupMgr, euce);
    }

    /**
     * The iq test edit screen in the course editor.
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     * @param groupMgr
     * @param euce
     * @return
     */
    @Override
    public TabbableController createIQSurveyEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final IQSURVCourseNode courseNode,
            final CourseGroupManager groupMgr, final UserCourseEnvironment euce) {
        return new IQEditController(ureq, wControl, course, courseNode, groupMgr, euce);
    }

    /**
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param ne
     * @param courseNode
     * @return
     */
    @Override
    public Controller createIQTestRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne,
            final IQTESTCourseNode courseNode) {

        Controller controller = null;

        // Do not allow guests to start tests
        final Roles roles = ureq.getUserSession().getRoles();
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
        if (roles.isGuestOnly()) {
            final String title = trans.translate("guestnoaccess.title");
            final String message = trans.translate("guestnoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
            final IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
            final RepositoryEntry repositoryEntry = ne.getCourseNode().getReferencedRepositoryEntry();
            final OLATResourceable ores = repositoryEntry.getOlatResource();
            final Long resId = ores.getResourceableId();
            final TestFileResource fr = CoreSpringFactory.getBean(TestFileResource.class);
            fr.overrideResourceableId(resId);
            if (!lockingService.isLocked(fr, null)) {
                // QTI1
                controller = new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
            } else {
                final String title = trans.translate("editor.lock.title");
                final String message = trans.translate("editor.lock.message");
                controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
            }
        }

        return controller;
    }

    @Override
    public Controller createIQTestPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQTESTCourseNode courseNode) {
        return new IQPreviewController(ureq, wControl, userCourseEnv, courseNode, ne);
    }

    @Override
    public Controller createIQSelftestRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQSELFCourseNode courseNode) {
        final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
        final IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
        return new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
    }

    @Override
    public Controller createIQSurveyRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final IQSURVCourseNode courseNode) {
        Controller controller = null;

        // Do not allow guests to start questionnaires
        final Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
            final String title = trans.translate("guestnoaccess.title");
            final String message = trans.translate("guestnoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            final RepositoryEntry repositoryEntry = ne.getCourseNode().getReferencedRepositoryEntry();
            final OLATResourceable ores = repositoryEntry.getOlatResource();
            final Long resId = ores.getResourceableId();
            final SurveyFileResource fr = CoreSpringFactory.getBean(SurveyFileResource.class);
            fr.overrideResourceableId(resId);
            if (!lockingService.isLocked(fr, null)) {
                final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
                final IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
                controller = new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
            } else {
                final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
                final String title = trans.translate("editor.lock.title");
                final String message = trans.translate("editor.lock.message");
                controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
            }
        }
        return controller;

    }

    @Override
    public Controller createIQTestDetailsEditController(final Long courseResourceableId, final String ident, final Identity identity,
            final RepositoryEntry referencedRepositoryEntry, final String qmdEntryTypeAssess, final UserRequest ureq, final WindowControl wControl) {
        return new QTIResultDetailsController(courseResourceableId, ident, identity, referencedRepositoryEntry, qmdEntryTypeAssess, ureq, wControl);
    }

}
