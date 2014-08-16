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

package org.olat.presentation.examples.guidemo.guisoa;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.presentation.user.administration.UserSearchControllerFactory;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class GuiDemoSoaController extends BasicController {

    private Panel usersearchHolder;
    private final VelocityContainer mainVC;
    private final Link button;
    private Link button2;

    private Controller userSearch;

    public GuiDemoSoaController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        mainVC = createVelocityContainer("demo");
        button = LinkFactory.createButton("button.usc", mainVC, this);
        mainVC.put("usersearchholder", usersearchHolder = new Panel("usersearchholder"));

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), mainVC);
        mainVC.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(mainVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == button) {
            showInfo("hello.world", ureq.getIdentity().getName());
            final UserSearchControllerFactory searchService = (UserSearchControllerFactory) CoreSpringFactory.getBean(UserSearchControllerFactory.class);
            userSearch = searchService.createUserSearchController(false, ureq, getWindowControl());
            listenTo(userSearch);
            usersearchHolder.setContent(userSearch.getInitialComponent());
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == userSearch) {
            final UserSearchControllerFactory searchService = (UserSearchControllerFactory) CoreSpringFactory.getBean(UserSearchControllerFactory.class);
            final Identity user = searchService.getChosenUser(event);
            final String chosenName = user.getName();
            showInfo("user.chosen", chosenName);

        }
    }

    @Override
    protected void doDispose() {
        // nothing to do yet
    }

}
