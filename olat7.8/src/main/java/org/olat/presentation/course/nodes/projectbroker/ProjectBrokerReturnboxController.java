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
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ta.AbstractReturnboxBaseController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Christian Guretzki
 */
public class ProjectBrokerReturnboxController extends AbstractReturnboxBaseController {

    private final Project project;
    private ProjectBrokerManager projectBrokerManager;

    public ProjectBrokerReturnboxController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final CourseNode node,
            final UserCourseEnvironment userCourseEnv, final boolean previewMode, final Project project) {
        super(ureq, wControl);
        this.project = project;
        projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        initReturnbox(ureq, wControl, config, node, userCourseEnv, previewMode);
    }

    /**
     * Return returnbox base-path. e.g. course/<COURSE_ID>/returnbox/<NODE_id>/<USER_NAME>
     */
    @Override
    protected String getReturnboxPathForIdentity(final CourseEnvironment courseEnv, final CourseNode node, final Identity identity) {
        return projectBrokerManager.getReturnboxPathForProjectAndIdentity(project, courseEnv, node, identity);
    }

}
