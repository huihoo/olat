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

package org.olat.presentation.course.nodes.projectbroker;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManager;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerReturnboxFullAccessWithoutDeleteCallback;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ta.AbstractDropboxScoringViewBaseController;
import org.olat.presentation.course.nodes.ta.DropboxScoringViewController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Christian Guretzki
 */
public class ProjectBrokerDropboxScoringViewController extends AbstractDropboxScoringViewBaseController {

    private final Project project;
    private ProjectBrokerManager projectBrokerManager;

    /**
     * Scoring view of the dropbox.
     * 
     * @param ureq
     * @param wControl
     * @param node
     * @param userCourseEnv
     */
    public ProjectBrokerDropboxScoringViewController(final Project project, final UserRequest ureq, final WindowControl wControl, final CourseNode node,
            final UserCourseEnvironment userCourseEnv) {
        super(ureq, wControl, node, userCourseEnv);
        this.project = project;
        projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        this.setVelocityRoot(PackageUtil.getPackageVelocityRoot(DropboxScoringViewController.class));
        final Translator fallbackTranslator = new PackageTranslator(PackageUtil.getPackageName(this.getClass()), ureq.getLocale());
        final Translator myTranslator = new PackageTranslator(PackageUtil.getPackageName(DropboxScoringViewController.class), ureq.getLocale(), fallbackTranslator);
        this.setTranslator(myTranslator);
        init(ureq);
    }

    @Override
    protected String getDropboxFilePath(final Identity assesseeIdentity) {
        return projectBrokerManager.getDropboxBasePathForProject(this.project, userCourseEnv.getCourseEnvironment(), node);
    }

    @Override
    protected String getReturnboxFilePath(final Identity assesseeIdentity) {
        return projectBrokerManager.getReturnboxBasePathForProject(project, userCourseEnv.getCourseEnvironment(), node);
    }

    @Override
    protected String getDropboxRootFolderName(final String assesseeName) {
        return translate("scoring.dropbox.rootfolder.name");
    }

    @Override
    protected String getReturnboxRootFolderName(final String assesseeName) {
        return translate("scoring.returnbox.rootfolder.name");
    }

    @Override
    protected VFSSecurityCallback getDropboxVfsSecurityCallback() {
        return new ReadOnlyCallback();
    }

    @Override
    protected VFSSecurityCallback getReturnboxVfsSecurityCallback(final String returnboxRelPath) {
        return new ProjectBrokerReturnboxFullAccessWithoutDeleteCallback(returnboxRelPath);
    }

}
