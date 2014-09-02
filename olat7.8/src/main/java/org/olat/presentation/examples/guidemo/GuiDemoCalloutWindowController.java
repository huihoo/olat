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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.examples.guidemo;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.text.TextComponent;
import org.olat.presentation.framework.core.components.text.TextFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.presentation.user.administration.UserSearchController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Demo of the callout window usage
 * <P>
 * Initial Date: 25.10.2010 <br>
 * 
 * @author gnaegi
 */
public class GuiDemoCalloutWindowController extends BasicController {
    private final VelocityContainer contentVC;
    private final Link calloutTriggerLink, calloutTriggerLink2;
    private CloseableCalloutWindowController calloutCtr, calloutCtr2, calloutCtr3;

    public GuiDemoCalloutWindowController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        contentVC = createVelocityContainer("calloutWindow");

        // Add callout trigger links
        calloutTriggerLink = LinkFactory.createButton("calloutTriggerLink", contentVC, this);
        calloutTriggerLink2 = LinkFactory.createButton("calloutTriggerLink2", contentVC, this);
        // The third callout trigger is implemented via javascript, see
        // calloutWindow.html file

        // Add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), contentVC);
        contentVC.put("sourceview", sourceview.getInitialComponent());

        final CloseableModalController cmc = new CloseableModalController(wControl, "close", contentVC);
        putInitialPanel(new Panel("sf"));

        cmc.activate();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers are auto disposed by basic controller
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == calloutTriggerLink) {
            if (calloutCtr == null) {
                // Open callout window and provide a DOM trigger ID manually from a
                // link. The content is a little two step workflow (user search)
                final UserSearchController ctr = new UserSearchController(ureq, getWindowControl(), false);
                calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), ctr.getInitialComponent(), "o_c" + calloutTriggerLink.getDispatchID(), null,
                        true, null);
                calloutCtr.addDisposableChildController(ctr);
                calloutCtr.activate();
                listenTo(calloutCtr);
            } else {
                // When window is already opened, second click on trigger should close
                // it again.
                removeAsListenerAndDispose(calloutCtr);
                calloutCtr = null;
            }
        } else if (source == calloutTriggerLink2) {
            if (calloutCtr2 == null) {
                // Open callout window and provide a DOM trigger link. The content is
                // some static text.
                final TextComponent calloutPanel = TextFactory.createTextComponentFromString("bla",
                        "Just some random hardcoded text here. Note that this window has no close button! Click the button a second time to close this window.", null,
                        false, null);
                calloutCtr2 = new CloseableCalloutWindowController(ureq, getWindowControl(), calloutPanel, calloutTriggerLink2, "This is a title in a callout window",
                        false, null);
                calloutCtr2.activate();
                listenTo(calloutCtr2);
            } else {
                // When window is already opened, second click on trigger should close
                // it again.
                removeAsListenerAndDispose(calloutCtr2);
                calloutCtr2 = null;
            }

        } else if (source == contentVC) {
            // A more complex example with a manualy crafted event to trigger the
            // callout window
            if ("trigger3".equals(event.getCommand())) {
                if (calloutCtr3 == null) {
                    // open callout window
                    final TextComponent calloutPanel = TextFactory
                            .createTextComponentFromString(
                                    "blu",
                                    "Cras dictum. Maecenas ut turpis. In vitae erat ac orci dignissim eleifend. Nunc quis justo. Sed vel ipsum in purus tincidunt pharetra. Sed pulvinar, felis id consectetuer malesuada, enim nisl mattis elit, a facilisis tortor nibh quis leo. Sed augue lacus, pretium vitae, molestie eget, rhoncus quis, elit. Donec in augue. Fusce orci wisi, ornare id, mollis vel, lacinia vel, massa. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.",
                                    "b_error", false, null);
                    calloutCtr3 = new CloseableCalloutWindowController(ureq, getWindowControl(), calloutPanel, "myspecialdomid", null, true, "trigger3");
                    calloutCtr3.activate();
                    listenTo(calloutCtr3);
                } else {
                    // When window is already opened, second click on trigger should close
                    // it again.
                    removeAsListenerAndDispose(calloutCtr3);
                    calloutCtr3 = null;
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // The CloseableCalloutWindowController fires the CLOSE_WINDOW_EVENT. When
        // you get this event you don't have to do anything, the window is already
        // deactivated. Alternatively you could also call the deactivate method
        // manually to close the window whenever you like.
        if (source == calloutCtr) {
            if (event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
                removeAsListenerAndDispose(calloutCtr);
                calloutCtr = null;
            }
        } else if (source == calloutCtr2) {
            if (event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
                removeAsListenerAndDispose(calloutCtr2);
                calloutCtr2 = null;
            }
        } else if (source == calloutCtr3) {
            if (event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
                removeAsListenerAndDispose(calloutCtr3);
                calloutCtr3 = null;
            }
        }

    }

}
