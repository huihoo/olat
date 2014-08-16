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

import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description: <br>
 * Displays a small form where in preview mode the users results can be simulated Initial Date: 13.01.2005 <br>
 * 
 * @author Felix Jost
 */
class IQPreviewController extends BasicController {

    private final PreviewForm pf;
    private final UserCourseEnvironment userCourseEnv;
    private final IQTESTCourseNode cn;

    /**
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param cn
     * @param ne
     */
    IQPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final IQTESTCourseNode cn,
            final NodeEvaluation ne) {
        super(ureq, wControl);

        this.userCourseEnv = userCourseEnv;
        this.cn = cn;
        pf = new PreviewForm(ureq, wControl);
        listenTo(pf);
        putInitialPanel(pf.getInitialComponent());
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == pf) {
            if (event == Event.DONE_EVENT) {
                final int score = pf.getPointValue();
                final Float cutValue = cn.getCutValueConfiguration();
                final boolean passed = score >= (cutValue == null ? 0 : cutValue.floatValue());
                final ScoreEvaluation sceval = new ScoreEvaluation(new Float(score), new Boolean(passed));
                final boolean incrementUserAttempts = true;
                cn.updateUserScoreEvaluation(sceval, userCourseEnv, ureq.getIdentity(), incrementUserAttempts);
                userCourseEnv.getScoreAccounting().scoreInfoChanged(cn, sceval);
                getWindowControl().setInfo(translate("preview.points.set"));
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }
}
