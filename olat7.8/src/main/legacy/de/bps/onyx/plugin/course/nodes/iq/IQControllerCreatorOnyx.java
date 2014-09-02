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
package de.bps.onyx.plugin.course.nodes.iq;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.CourseIQSecurityCallback;
import org.olat.course.nodes.iq.IQControllerCreatorOlat;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.QTIResultManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import de.bps.ims.qti.QTIResultDetailsController;
import de.bps.onyx.plugin.OnyxExportManager;
import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.run.OnyxRunController;

/**
 * Description:<br>
 * TODO: thomasw Class Description for IQControllerCreatorOnyx
 * <P>
 * Initial Date: 28.06.2010 <br>
 * 
 * @author thomasw
 */
public class IQControllerCreatorOnyx extends IQControllerCreatorOlat {

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
		final ModuleConfiguration config = courseNode.getModuleConfiguration();
		final boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null
				&& config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

		if (qti2) {
			final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			final IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
			controller = new OnyxRunController(userCourseEnv, config, sec, ureq, wControl, courseNode);
		} else {
			controller = super.createIQTestRunController(ureq, wControl, userCourseEnv, ne, courseNode);
		}

		return controller;
	}

	@Override
	public Controller createIQTestPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
			final NodeEvaluation ne, final IQTESTCourseNode courseNode) {
		Controller controller = null;
		final ModuleConfiguration config = courseNode.getModuleConfiguration();
		final boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null
				&& config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

		if (qti2) {
			controller = new OnyxRunController(ureq, wControl, courseNode.getReferencedRepositoryEntry().getOlatResource());
		} else {
			controller = super.createIQTestPreviewController(ureq, wControl, userCourseEnv, ne, courseNode);
		}
		return controller;
	}

	@Override
	public Controller createIQSelftestRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
			final NodeEvaluation ne, final IQSELFCourseNode courseNode) {
		Controller controller = null;
		final ModuleConfiguration config = courseNode.getModuleConfiguration();
		final boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null
				&& config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

		if (qti2) {
			final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			final IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
			controller = new OnyxRunController(userCourseEnv, config, sec, ureq, wControl, courseNode);
		} else {
			controller = super.createIQSelftestRunController(ureq, wControl, userCourseEnv, ne, courseNode);
		}
		return controller;
	}

	@Override
	public Controller createIQSurveyRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
			final NodeEvaluation ne, final IQSURVCourseNode courseNode) {
		Controller controller = null;

		final Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			final Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
			final String title = trans.translate("guestnoaccess.title");
			final String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			final ModuleConfiguration config = courseNode.getModuleConfiguration();
			final boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null
					&& config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

			if (qti2) {
				final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
				final IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
				controller = new OnyxRunController(userCourseEnv, config, sec, ureq, wControl, courseNode);
			} else {
				controller = super.createIQSurveyRunController(ureq, wControl, userCourseEnv, ne, courseNode);
			}
		}
		return controller;
	}

	@Override
	public Controller createIQTestDetailsEditController(final Long courseResourceableId, final String ident, final Identity identity,
			final RepositoryEntry referencedRepositoryEntry, final String qmdEntryTypeAssess, final UserRequest ureq, final WindowControl wControl) {
		return new QTIResultDetailsController(courseResourceableId, ident, identity, referencedRepositoryEntry, qmdEntryTypeAssess, ureq, wControl);
	}

	@Override
	public boolean archiveIQTestCourseNode(final Locale locale, final String repositorySoftkey, final Long courseResourceableId, final String shortTitle,
			final String ident, final File exportDirectory, final String charset) {
		final boolean qti2 = OnyxModule.isOnyxTest(RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true).getOlatResource());
		if (qti2) {
			final ICourse course = CourseFactory.loadCourse(courseResourceableId);
			final CourseNode currentCourseNode = course.getRunStructure().getNode(ident);
			final Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true).getKey();
			final QTIResultManager qrm = QTIResultManager.getInstance();
			final List results = qrm.selectResults(courseResourceableId, ident, repKey, 1);
			if (results.size() > 0) {
				OnyxExportManager.getInstance().exportResults(results, exportDirectory, currentCourseNode);
			}
			return true;
		} else {
			return super.archiveIQTestCourseNode(locale, repositorySoftkey, courseResourceableId, shortTitle, ident, exportDirectory, charset);
		}
	}
}
