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

package org.olat.presentation.course.nodes.ta;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ta.Solution_EBL;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.commons.filechooser.FileChooserController;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.notification.ContextualSubscriptionController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Mike Stock
 */

public class SolutionController extends BasicController {

    private final VelocityContainer myContent;
    private FileChooserController fileChooserController;
    private final FolderRunController solutionFolderRunController;
    private ContextualSubscriptionController contextualSubscriptionCtr;

    /**
     * Implements a dropbox.
     * 
     * @param ureq
     * @param wControl
     * @param config
     * @param node
     * @param userCourseEnv
     * @param previewMode
     */
    public SolutionController(final UserRequest ureq, final WindowControl wControl, final CourseNode node, final UserCourseEnvironment userCourseEnv,
            final boolean previewMode) {
        super(ureq, wControl);

        myContent = createVelocityContainer("solutionRun");
        Solution_EBL solutionEbl = CoreSpringFactory.getBean(Solution_EBL.class);

        VFSContainer solutionNamedContainer = solutionEbl.getReadonlyFolderContainer(userCourseEnv.getCourseEnvironment(), node);
        solutionFolderRunController = new FolderRunController(solutionNamedContainer, false, ureq, wControl);
        solutionFolderRunController.addControllerListener(this);
        myContent.put("solutionbox", solutionFolderRunController.getInitialComponent());

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (fileChooserController != null) {
            fileChooserController.dispose();
            fileChooserController = null;
        }
    }
}
