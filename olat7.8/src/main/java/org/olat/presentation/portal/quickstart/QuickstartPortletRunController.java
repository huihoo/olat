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

package org.olat.presentation.portal.quickstart;

import org.olat.data.basesecurity.Roles;
import org.olat.lms.course.CourseFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.group.site.GroupsSite;
import org.olat.presentation.home.site.HomeSite;
import org.olat.presentation.repository.site.RepositorySite;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Run view controller of quickstart portlet
 * <P>
 * Initial Date: 11.07.2005 <br>
 * 
 * @author gnaegi
 */
public class QuickstartPortletRunController extends DefaultController {

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(QuickstartPortletRunController.class);
    private final Translator trans;
    private VelocityContainer quickstartVC;
    private final Link helpLink;

    /**
     * Constructor
     * 
     * @param ureq
     * @param wControl
     */
    protected QuickstartPortletRunController(final UserRequest ureq, final WindowControl wControl) {
        super(wControl);
        this.trans = new PackageTranslator(PackageUtil.getPackageName(QuickstartPortletRunController.class), ureq.getLocale());

        final Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            this.quickstartVC = new VelocityContainer("quickstartVC", VELOCITY_ROOT + "/quickstartPortletGuest.html", trans, this);
        } else {
            this.quickstartVC = new VelocityContainer("quickstartVC", VELOCITY_ROOT + "/quickstartPortlet.html", trans, this);
        }
        helpLink = LinkFactory.createLink("quickstart.link.help", quickstartVC, this);
        helpLink.setTooltip("quickstart.ttip.help", false);
        helpLink.setTarget("_help");

        setInitialComponent(this.quickstartVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == quickstartVC) {
            final String cmd = event.getCommand();
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            if (cmd.equals("cmd.repo.course")) {
                // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, RepositorySite.class.getName(), "search.course");
                dts.activateStatic(ureq, RepositorySite.class.getName(), "search.course");
            } else if (cmd.equals("cmd.repo.catalog")) {
                // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, RepositorySite.class.getName(), "search.catalog");
                dts.activateStatic(ureq, RepositorySite.class.getName(), "search.catalog");
            } else if (cmd.equals("cmd.settings")) {
                // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, HomeSite.class.getName(), "mysettings");
                dts.activateStatic(ureq, HomeSite.class.getName(), "mysettings");
            } else if (cmd.equals("cmd.buddygroup.new")) {
                // was brasato:: getWindowControl().getDTabs().activateStatic(ureq, GroupsSite.class.getName(), "addBuddyGroup");
                dts.activateStatic(ureq, GroupsSite.class.getName(), "addBuddyGroup");
            }
        } else if (source == helpLink) {
            final ControllerCreator ctrlCreator = new ControllerCreator() {
                @Override
                public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                    return CourseFactory.createHelpCourseLaunchController(lureq, lwControl);
                }
            };
            // wrap the content controller into a full header layout
            final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
            // open in new browser window
            final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
            pbw.open(ureq);
            //
        }
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
