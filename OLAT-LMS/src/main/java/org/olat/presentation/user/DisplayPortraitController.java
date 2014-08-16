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

package org.olat.presentation.user;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.user.DisplayPortraitManager;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.GenderPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Sept 08, 2005
 * 
 * @author Alexander Schneider Comment:
 */
public class DisplayPortraitController extends BasicController {

    private final VelocityContainer myContent;
    private final Identity portraitIdent;

    /**
     * @param ureq
     * @param wControl
     * @param portrait
     */
    public DisplayPortraitController(final UserRequest ureq, final WindowControl wControl, final Identity portraitIdent, final boolean useLarge,
            final boolean canLinkToHomePage) {
        super(ureq, wControl);
        myContent = createVelocityContainer("displayportrait");
        myContent.contextPut("canLinkToHomePage", canLinkToHomePage ? Boolean.TRUE : Boolean.FALSE);
        if (portraitIdent == null) {
            throw new AssertException("identity can not be null!");
        }
        this.portraitIdent = portraitIdent;

        ImageComponent ic = null;

        final GenderPropertyHandler genderHander = (GenderPropertyHandler) getUserService().getUserPropertiesConfig().getPropertyHandler(UserConstants.GENDER);
        String gender = "-"; // use as default
        if (genderHander != null) {
            gender = genderHander.getInternalValue(portraitIdent.getUser());
        }

        MediaResource portrait = null;
        if (useLarge) {
            portrait = DisplayPortraitManager.getInstance().getPortrait(portraitIdent, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
            if (gender.equals("-")) {
                myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_BIG_CSS_CLASS);
            } else if (gender.equals("male")) {
                myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS);
            } else if (gender.equals("female")) {
                myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS);
            }
        } else {
            portrait = DisplayPortraitManager.getInstance().getPortrait(portraitIdent, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME);
            if (gender.equals("-")) {
                myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_SMALL_CSS_CLASS);
            } else if (gender.equals("male")) {
                myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_MALE_SMALL_CSS_CLASS);
            } else if (gender.equals("female")) {
                myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_FEMALE_SMALL_CSS_CLASS);
            }
        }
        myContent.contextPut("hasPortrait", (portrait != null) ? Boolean.TRUE : Boolean.FALSE);
        myContent.contextPut("identityKey", portraitIdent.getKey().toString());

        if (portrait != null) {
            ic = new ImageComponent("image");
            ic.setMediaResource(portrait);
            myContent.put(ic);
        }
        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == myContent) {
            if (event.getCommand().equals("showuserinfo")) {
                final ControllerCreator ctrlCreator = new ControllerCreator() {
                    @Override
                    public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                        return new UserInfoMainController(lureq, lwControl, portraitIdent);
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
        // nothing to dispatch
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do yet
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
