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

package org.olat.presentation.course.nodes.tu;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.nodes.TUCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.clone.CloneController;
import org.olat.presentation.framework.core.control.generic.clone.CloneLayoutControllerCreatorCallback;
import org.olat.presentation.framework.core.control.generic.clone.CloneableController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.tunneling.IframeTunnelController;
import org.olat.presentation.tunneling.TunnelController;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * is the controller for displaying contents using olat tunnel
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class TURunController extends BasicController {

    private Controller startPage;
    private Link showButton;
    private final TUCourseNode courseNode;
    private final Panel main;
    private final ModuleConfiguration config;
    private final CourseEnvironment courseEnv;
    private CloneController cloneC;

    /**
     * Constructor for tunneling run controller
     * 
     * @param wControl
     * @param config
     *            The module configuration
     * @param ureq
     *            The user request
     * @param tuCourseNode
     *            The current course node
     * @param cenv
     *            the course environment
     */
    public TURunController(final WindowControl wControl, final ModuleConfiguration config, final UserRequest ureq, final TUCourseNode tuCourseNode,
            final CourseEnvironment cenv) {
        super(ureq, wControl);
        this.courseNode = tuCourseNode;
        this.config = config;
        this.courseEnv = cenv;

        main = new Panel("turunmain");
        if (config.getBooleanSafe(TUConfigForm.CONFIG_EXTERN, false)) {
            doStartPage(ureq);
        } else {
            doLaunch(ureq);
        }
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

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == startPage) {
            if (event == Event.DONE_EVENT) {
                doLaunch(ureq);
            }
        }
    }

    private void doStartPage(final UserRequest ureq) {
        final Controller startPageInner = new TUStartController(ureq, getWindowControl(), config);
        startPage = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), startPageInner, courseNode, "o_tu_icon");
        listenTo(startPage);
        main.setContent(startPage.getInitialComponent());
    }

    private void doLaunch(final UserRequest ureq) {
        final boolean iniframe = config.getBooleanSafe(TUConfigForm.CONFIG_IFRAME);
        // create the possibility to float
        CloneableController controller;
        if (iniframe) {
            // Do not dispose this controller if the course is closed...
            final IframeTunnelController ifC = new IframeTunnelController(ureq, getWindowControl(), config);
            controller = ifC;
        } else {
            final TunnelController tuC = new TunnelController(ureq, getWindowControl(), config);
            controller = tuC;
        }
        listenTo(controller);

        // create clone wrapper layout
        final CloneLayoutControllerCreatorCallback clccc = new CloneLayoutControllerCreatorCallback() {
            @Override
            public ControllerCreator createLayoutControllerCreator(final UserRequest ureq, final ControllerCreator contentControllerCreator) {
                return BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, new ControllerCreator() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        // wrapp in column layout, popup window needs a layout controller
                        final Controller ctr = contentControllerCreator.createController(lureq, lwControl);
                        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, ctr.getInitialComponent(), null);
                        layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), courseEnv));
                        layoutCtr.addDisposableChildController(ctr);
                        return layoutCtr;
                    }
                });
            }
        };

        final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), controller, courseNode, "o_tu_icon");
        if (ctrl instanceof CloneableController) {
            cloneC = new CloneController(ureq, getWindowControl(), (CloneableController) ctrl, clccc);
            listenTo(cloneC);
            main.setContent(cloneC.getInitialComponent());
        } else {
            throw new AssertException("Controller must be cloneable");
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controller registered with listenTo gets disposed in BasicController
    }

}
