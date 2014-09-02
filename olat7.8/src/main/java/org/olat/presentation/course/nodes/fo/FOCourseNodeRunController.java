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

package org.olat.presentation.course.nodes.fo;

import org.olat.data.forum.Forum;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.nodes.FOCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.forum.ForumCallback;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.forum.ForumUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.docking.DockController;
import org.olat.presentation.framework.core.control.generic.docking.DockLayoutControllerCreatorCallback;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.system.event.Event;

/**
 * Initial Date: Apr 22, 2004
 * 
 * @author gnaegi
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FOCourseNodeRunController extends BasicController {

    private DockController dockC;
    private final FOCourseNode courseNode;
    private final Panel main;
    private final CourseEnvironment courseEnv;
    private final Forum forum;
    private final ForumCallback foCallback;
    private Link showButton;

    /**
     * Constructor for a forum course building block runtime controller
     * 
     * @param ureq
     *            The user request
     * @param userCourseEnv
     * @param wContr
     *            The current window controller
     * @param forum
     *            The forum to be displayed
     * @param foCallback
     *            The forum security callback
     * @param foCourseNode
     *            The current course node
     */
    public FOCourseNodeRunController(final UserRequest ureq, final UserCourseEnvironment userCourseEnv, final WindowControl wControl, final Forum forum,
            final ForumCallback foCallback, final FOCourseNode foCourseNode) {
        super(ureq, wControl);
        this.courseNode = foCourseNode;
        this.courseEnv = userCourseEnv.getCourseEnvironment();
        this.forum = forum;
        this.foCallback = foCallback;
        // set logger on this run controller
        addLoggingResourceable(LoggingResourceable.wrap(foCourseNode));

        main = new Panel("forunmain");
        doLaunch(ureq);
        putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showButton) {
            doLaunch(ureq);
        }
    }

    private void doLaunch(final UserRequest ureq) {
        dockC = new DockController(ureq, getWindowControl(), false, new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                final Controller foCtr = ForumUIFactory.getStandardForumController(lureq, lwControl, forum, foCallback);
                listenTo(foCtr);
                final Controller titledCtrl = TitledWrapperHelper.getWrapper(lureq, lwControl, foCtr, courseNode, "o_fo_icon");
                return titledCtrl;
            }
        }, new DockLayoutControllerCreatorCallback() {
            @Override
            public ControllerCreator createLayoutControllerCreator(final UserRequest ureq, final ControllerCreator contentControllerCreator) {
                return BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, new ControllerCreator() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        // Wrap in column layout, popup window needs a layout controller
                        final Controller ctr = contentControllerCreator.createController(lureq, lwControl);
                        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, ctr.getInitialComponent(), null);
                        layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), courseEnv));
                        layoutCtr.addDisposableChildController(ctr);
                        return layoutCtr;
                    }
                });
            }
        });
        listenTo(dockC);
        main.setContent(dockC.getInitialComponent());
    }

    @Override
    protected void doDispose() {
        //
    }

}
