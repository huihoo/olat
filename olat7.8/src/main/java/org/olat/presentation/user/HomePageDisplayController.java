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

import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 25, 2005
 * 
 * @author Alexander Schneider Comment:
 */
public class HomePageDisplayController extends BasicController {
    private static final String usageIdentifyer = HomePageConfig.class.getCanonicalName();

    /**
     * @param ureq
     * @param wControl
     * @param hpc
     */
    public HomePageDisplayController(final UserRequest ureq, final WindowControl wControl, final HomePageConfig hpc) {
        super(ureq, wControl);

        // use property handler translator for translating of user fields
        setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
        final VelocityContainer myContent = createVelocityContainer("homepagedisplay");

        final String userName = hpc.getUserName();
        final Identity identity = getBaseSecurity().findIdentityByName(userName);
        final User u = identity.getUser();

        myContent.contextPut("userName", identity.getName());
        myContent.contextPut("deleted", identity.getStatus().equals(Identity.STATUS_DELETED));
        myContent.contextPut("userFirstLastName", getUserService().getFirstAndLastname(u));
        myContent.contextPut("locale", getLocale());

        // add configured property handlers and the homepage config
        // do the looping in the velocity context
        final List<UserPropertyHandler> userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, false);

        myContent.contextPut("user", u);
        myContent.contextPut("userPropertyHandlers", userPropertyHandlers);
        myContent.contextPut("homepageConfig", hpc);

        final Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), identity, true, false);
        listenTo(dpc); // auto dispose
        myContent.put("image", dpc.getInitialComponent());
        putInitialPanel(myContent);
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to catch
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controller sposed by basic controller
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
