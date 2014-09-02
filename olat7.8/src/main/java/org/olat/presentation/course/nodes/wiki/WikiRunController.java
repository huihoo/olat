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

package org.olat.presentation.course.nodes.wiki;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.WikiCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiSecurityCallback;
import org.olat.lms.wiki.WikiSecurityCallbackImpl;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
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
import org.olat.presentation.wiki.WikiUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description: Initial Date: Oct 12, 2004
 * 
 * @author Guido Schnider
 */
public class WikiRunController extends BasicController {

    private Panel main;

    private CourseEnvironment courseEnv;
    private Controller wikiCtr;
    private ModuleConfiguration config;
    private CloneController cloneCtr;

    /**
     * @param wControl
     * @param ureq
     * @param wikiCourseNode
     * @param cenv
     */
    public WikiRunController(final WindowControl wControl, final UserRequest ureq, final WikiCourseNode wikiCourseNode, final CourseEnvironment cenv,
            final NodeEvaluation ne) {
        super(ureq, wControl);
        this.courseEnv = cenv;

        this.config = wikiCourseNode.getModuleConfiguration();
        main = new Panel("wikirunmain");
        addLoggingResourceable(LoggingResourceable.wrap(wikiCourseNode));

        // get repository entry in "strict" mode
        final RepositoryEntry re = wikiCourseNode.getReferencedRepositoryEntry(true);

        // check role
        final boolean isOLatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        final boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
        boolean isResourceOwner = false;
        if (isOLatAdmin) {
            isResourceOwner = true;
        } else {
            isResourceOwner = RepositoryServiceImpl.getInstance().isOwnerOfRepositoryEntry(ureq.getIdentity(), re);
        }

        // Check for jumping to certain wiki page
        final BusinessControl bc = wControl.getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();

        final SubscriptionContext subsContext = CourseModule.createSubscriptionContext(cenv, wikiCourseNode);
        // TODO: ORID-1011: remove OLD CREATION OF SUBS_CONTEXT: final SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(cenv,
        // wikiCourseNode);
        final WikiSecurityCallback callback = new WikiSecurityCallbackImpl(ne, isOLatAdmin, isGuestOnly, false, isResourceOwner, subsContext);

        if (ce != null) { // jump to a certain context
            final OLATResourceable ores = ce.getOLATResourceable();
            final String typeName = ores.getResourceableTypeName();
            String page = typeName.substring("page=".length());
            if (page.endsWith(":0")) {
                page = page.substring(0, page.length() - 2);
            }
            wikiCtr = WikiUIFactory.getInstance().createWikiMainController(ureq, wControl, re.getOlatResource(), callback, page);
        } else {
            wikiCtr = WikiUIFactory.getInstance().createWikiMainController(ureq, wControl, re.getOlatResource(), callback, null);
        }
        listenTo(wikiCtr);

        final Controller wrappedCtr = TitledWrapperHelper.getWrapper(ureq, wControl, wikiCtr, wikiCourseNode, Wiki.CSS_CLASS_WIKI_ICON);

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

        if (wrappedCtr instanceof CloneableController) {
            cloneCtr = new CloneController(ureq, getWindowControl(), (CloneableController) wrappedCtr, clccc);
            listenTo(cloneCtr);
            main.setContent(cloneCtr.getInitialComponent());
            putInitialPanel(main);
        }
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
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        //
    }

    /**
     * control.DefaultController#doDispose(boolean)
     */
    @Override
    protected void doDispose() {
        //
    }

}
