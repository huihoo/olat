/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.examples.helloworldpackage;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

public class HelloWorldController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private VelocityContainer startPage;
    private final String myString = "Hello World";
    private final Link sayHelloLink;
    private final Link sayHelloButtonXSmall;
    private final Link sayHelloButtonSmall;
    private final Link sayHelloButton;
    private final Panel helloWorldPanel;

    public HelloWorldController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // if you need WindowControl, you -MUST- use WindowControl w2 = getWindowControl(); do not use variable wControl!
        // the windowcontrol is used e.g. for displaying info or error messages or to
        // push components to the main window. See further down for an example

        // create a panel where content is pushed and taken away.
        helloWorldPanel = new Panel("Hello World Panel");

        // create a new VelocityContainer that display helloworld.html, has its translator
        // for any i18n specifics in the container and registers this HelloWorldController
        // as its event dispatcher
        startPage = createVelocityContainer("helloworld");
        // create a link. In the velocity template helloworld.html it's rendered by $r.render("say.hello"). "say.hello" is the
        // name of the component, the command and the i18n key.
        sayHelloLink = LinkFactory.createLink("say.hello", startPage, this);

        sayHelloButtonXSmall = LinkFactory.createButtonXSmall("say.hello.xsmall", startPage, this);
        sayHelloButtonSmall = LinkFactory.createButtonSmall("say.hello.small", startPage, this);
        sayHelloButton = LinkFactory.createButton("say.hello", startPage, this);

        // we pass a variable
        startPage.contextPut("myContentVariable", myString);

        // display image via css
        // - put the image under _static/css/img
        // - put the css file under _static/css
        // - add to css file: .your_css_class { background: url(img/your_image.png) }
        // - add to your velocity-template: <div class="your_css_class"></div>
        final JSAndCSSComponent demoext = new JSAndCSSComponent("demoext", this.getClass(), null, "demoext.css", true);
        startPage.put("demoext", demoext);

        // our velocity container will be the first component to display
        // when somebody decides to render the GUI of this controller.
        helloWorldPanel.setContent(startPage);

        putInitialPanel(helloWorldPanel);
    }

    /**
     * This dispatches component events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // first check, which component this event comes from...
        if (source == sayHelloLink) {
            // ok, we have to say hello... do so.
            log.info("Someone asked us to say hello... so we do.", null);
            // we say hello to the and display the username which is part of the user identity and stored in the user session
            getWindowControl().setInfo("Hi " + ureq.getIdentity().getName());
        } else if (source == sayHelloButtonXSmall) {
            log.info("Someone asked us to say hello... so we do.", null);
            getWindowControl().setInfo("Hi " + ureq.getIdentity().getName() + " (ButtonXSmall)");
        } else if (source == sayHelloButtonSmall) {
            log.info("Someone asked us to say hello... so we do.", null);
            getWindowControl().setInfo("Hi " + ureq.getIdentity().getName() + " (ButtonSmall)");
        } else if (source == sayHelloButton) {
            log.info("Someone asked us to say hello... so we do.", null);
            getWindowControl().setInfo("Hi " + ureq.getIdentity().getName() + " (ButtonDefault)");
        }
    }

    /**
     * This dispatches controller events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // at this time, we do not have any other controllers we'd like to listen for events to...

        // If you have a form or a table component in your velocity file the events (like clicking an element in the table)
        // this method gets called and the event can be handled

    }

    @Override
    protected void doDispose() {
        // this is just to help the Java Garbage Collector or other stuff to clean up before it gets destroyed
        startPage = null;
    }

}
