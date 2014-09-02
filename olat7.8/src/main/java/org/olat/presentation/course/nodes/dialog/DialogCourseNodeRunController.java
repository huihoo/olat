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

package org.olat.presentation.course.nodes.dialog;

import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.course.nodes.DialogCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.dialogelements.DialogElementsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * chief controller for the run mode of the course node 'dialog elements'
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeRunController extends BasicController {

    public DialogCourseNodeRunController(final UserRequest ureq, final UserCourseEnvironment userCourseEnv, final WindowControl wControl,
            final DialogCourseNode dialogCourseNode, final NodeEvaluation ne) {
        super(ureq, wControl);
        addLoggingResourceable(LoggingResourceable.wrap(dialogCourseNode));

        final Controller dialogCtr = new DialogElementsController(ureq, getWindowControl(), dialogCourseNode, userCourseEnv, ne);
        listenTo(dialogCtr);

        putInitialPanel(dialogCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events yet
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers disposed by basic controller
    }

}
