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

package org.olat.presentation.examples;

import org.apache.log4j.Logger;
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
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * The famous "hello world" example the OLAT style
 * <P>
 * Initial Date: 29.08.2007 <br>
 * 
 * @author guido
 */
public class HelloWorldController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer myContent = createVelocityContainer("helloworld");
    private final VelocityContainer newsVc = createVelocityContainer("hello");

    private final String myString = "Hello World!";
    private final Panel panel = new Panel("panel");
    private final Link link;
    private final Link button;

    public HelloWorldController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // we pass a variable to the velocity container
        // which can be accessed in our helloworld.html file
        myContent.contextPut("myContentVariable", myString);

        // links and buttons are also components
        link = LinkFactory.createLink("sayhello_i18n_key", myContent, this);
        button = LinkFactory.createButton("sayhello_i18n_key2", myContent, this);

        // panels are content holders that are initially empty and can be filled
        // with different contents
        // the panel itself stays in the layout and if you are in AJAX mode only the
        // new content gets sent and replaced by DOM replacement.
        myContent.put("panel", panel);
        panel.setContent(null);

        // add sourceview control for easy access of sourcecode from browser for learing reason
        final Controller sourceView = new SourceViewController(ureq, wControl, this.getClass(), myContent);
        myContent.put("sourceview", sourceView.getInitialComponent());

        // our velocity contrainer will be the first component to display
        // when somebody decides to render the GUI of this controller.
        putInitialPanel(myContent);
    }

    /**
     * This dispatches component events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // first check, which component this event comes from...
        if (source == link) {
            // OK, we have to say hello... do so.
            // logging writes a message to the olat.log file
            log.info("Someone asked us to say hello... so we do.", null);
            // we say hello to the and display the userName which is part of the
            // user identity and stored in the user session
            getWindowControl().setInfo("Hi, your name is " + ureq.getIdentity().getName());
        } else if (source == button) {
            // someone pressed the button
            panel.setContent(newsVc);
        }
    }

    /**
     * This dispatches controller events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // at this time, we do not have any other controllers we'd like to listen for
        // events to...

        // If you have a formular or a table component in your velocity file the
        // events (like clicking an element in the table)
        // this method gets called and the event can be handled
    }

    @Override
    protected void doDispose() {
        // use this method to finish thing at the end of the lifetime of this
        // controller
        // like closing files or connections...
        // this method does no get called automatically, you have to maintain the
        // controller chain
        // and make sure that you call dispose on the place where you create the
        // controller
    }

}
