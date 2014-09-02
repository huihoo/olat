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

package org.olat.presentation.examples.guidemo;

import java.util.Stack;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.event.Event;

public class GuiDemoPushPopController extends BasicController {

    VelocityContainer vcMain, vcPush;
    Stack windowStack = new Stack();
    private final Link pushButton;
    private final Link popButton;

    public GuiDemoPushPopController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        vcMain = this.createVelocityContainer("guidemo-pushpop");
        pushButton = LinkFactory.createButton("guidemo.window.control.push", vcMain, this);
        popButton = LinkFactory.createButton("guidemo.window.control.pop", vcMain, this);

        vcMain.contextPut("stack", getStackHTMLRepresentation());
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, vcMain, null);
        listenTo(layoutCtr);

        this.putInitialPanel(layoutCtr.getInitialComponent());
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == pushButton) {
            final VelocityContainer container = this.createVelocityContainer("guidemo-pushpop");
            final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, container, null);
            listenTo(layoutCtr);
            windowStack.push(layoutCtr.getInitialComponent());

            container.put("guidemo.window.control.push", pushButton);
            container.put("guidemo.window.control.pop", popButton);
            container.contextPut("stack", getStackHTMLRepresentation());
            getWindowControl().pushToMainArea(container);
        } else if (source == popButton) {
            if (windowStack.isEmpty()) {
                fireEvent(ureq, Event.DONE_EVENT);
            } else {
                getWindowControl().pop();
                windowStack.pop();
            }
        }
    }

    private String getStackHTMLRepresentation() {
        final StringBuilder result = new StringBuilder();
        result.append("Current window stack:<br /><br />");
        for (int i = windowStack.size(); i > 0; i--) {
            final Component component = (Component) windowStack.get(i - 1);
            result.append("Stack position " + i + ": " + component.getComponentName() + "<br />");
        }
        return result.toString();
    }

    @Override
    protected void doDispose() {
    }

}
