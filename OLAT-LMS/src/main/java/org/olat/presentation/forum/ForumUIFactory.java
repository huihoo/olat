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

package org.olat.presentation.forum;

import org.olat.data.forum.Forum;
import org.olat.lms.forum.ForumCallback;
import org.olat.lms.forum.ForumHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.control.generic.title.TitleInfo;
import org.olat.presentation.framework.core.control.generic.title.TitledWrapperController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * Factory for a Titled <code>ForumController</code>, either in a popup or not.
 * <P>
 * Initial Date: 25.06.2007 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class ForumUIFactory {

    /**
     * Provides a popable ForumController wrapped in a titled controller.
     * 
     * @param ureq
     * @param forum
     * @param forumCallback
     * @param title
     * @return a ChiefController
     */
    public static PopupBrowserWindow getPopupableForumController(final UserRequest ureq, final WindowControl wControl, final Forum forum,
            final ForumCallback forumCallback, final TitleInfo titleInfo) {
        final ControllerCreator ctrlCreator = new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                final Controller forumWrapperController = getTitledForumController(lureq, lwControl, forum, forumCallback, titleInfo);
                // use on column layout
                final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, forumWrapperController.getInitialComponent(),
                        null);
                layoutCtr.addDisposableChildController(forumWrapperController); // dispose content on layout dispose
                return layoutCtr;
            }
        };
        // wrap the content controller into a full header layout
        final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
        final PopupBrowserWindow pbw = wControl.getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
        return pbw;
    }

    /**
     * Provides a ForumController wrapped in a titled controller.
     * 
     * @param ureq
     * @param forum
     * @param forumCallback
     * @param title
     * @return a TitledWrapperController
     */
    public static Controller getTitledForumController(final UserRequest ureq, final WindowControl wControl, final Forum forum, final ForumCallback forumCallback,
            final TitleInfo titleInfo) {
        final ForumController popupFoCtr = new ForumController(forum, forumCallback, ureq, wControl);
        popupFoCtr.setShowHeader(Boolean.FALSE);

        final TitledWrapperController forumWrapperController = new TitledWrapperController(ureq, wControl, popupFoCtr, "o_course_run", titleInfo);
        // Set CSS values to default forum icons if no values are set in the title info
        if (!StringHelper.containsNonWhitespace(titleInfo.getCssClass())) {
            forumWrapperController.setTitleCssClass(" b_with_small_icon_left " + ForumHelper.CSS_ICON_CLASS_FORUM + " ");
        }
        return forumWrapperController;
    }

    /**
     * Provides a standard forum controller without a title element
     * 
     * @param ureq
     * @param wControl
     * @param forum
     * @param forumCallback
     * @return
     */
    public static ForumController getStandardForumController(final UserRequest ureq, final WindowControl wControl, final Forum forum, final ForumCallback forumCallback) {
        ForumController controller = new ForumController(forum, forumCallback, ureq, wControl);
        controller.setShowHeader(Boolean.TRUE);
        return controller;
    }

    public static ForumController getStandardForumControllerWithoutHeader(final UserRequest ureq, final WindowControl wControl, final Forum forum,
            final ForumCallback forumCallback) {
        ForumController controller = new ForumController(forum, forumCallback, ureq, wControl);
        controller.setShowHeader(Boolean.FALSE);
        return controller;
    }

}
