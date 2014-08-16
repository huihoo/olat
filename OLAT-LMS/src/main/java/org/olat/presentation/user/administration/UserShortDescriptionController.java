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

package org.olat.presentation.user.administration;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Container for userProperty list, configurable in olat_userconfig.xml.
 * <P>
 * Initial Date: 15.01.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class UserShortDescriptionController extends BasicController {

    private final VelocityContainer velocityContainer;
    private static final String usageIdentifyer = UserShortDescriptionController.class.getCanonicalName();
    private final List<UserPropertyHandler> userPropertyHandlers;

    public UserShortDescriptionController(final UserRequest ureq, final WindowControl wControl, final Identity identity) {
        super(ureq, wControl);

        final String usernameLabel = translate("table.user.login");
        // use the PropertyHandlerTranslator for the velocityContainer
        setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
        velocityContainer = this.createVelocityContainer("userShortDescription");

        final Roles roles = ureq.getUserSession().getRoles();
        final boolean isAdministrativeUser = roles.isAdministrativeUser();
        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        velocityContainer.contextPut("userPropertyHandlers", userPropertyHandlers);
        velocityContainer.contextPut("user", identity.getUser());
        velocityContainer.contextPut("username", identity.getName());
        velocityContainer.contextPut("usernameLabel", usernameLabel);

        this.putInitialPanel(velocityContainer);
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // No event expected
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
