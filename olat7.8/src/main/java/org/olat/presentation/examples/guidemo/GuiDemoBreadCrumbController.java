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

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.breadcrumb.BreadCrumbController;
import org.olat.presentation.framework.core.control.generic.breadcrumb.CrumbBasicController;
import org.olat.presentation.framework.core.control.generic.breadcrumb.CrumbController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This demo shown you how you can use the bread crumb controller to build simple bread crumb navigation. Bread crumb is good to use when the user can start a workflow
 * that runs within your parent workflow and maybe want to go back.
 * <p>
 * Don't use it to build wizards, see the steps controller for this
 * <P>
 * Initial Date: 08.09.2008 <br>
 * 
 * @author gnaegi
 */
public class GuiDemoBreadCrumbController extends BasicController {
    private BreadCrumbController breadCrumbCtr;
    private final VelocityContainer content;

    /**
     * Constructor for a demo of the bread crumb controller
     * 
     * @param ureq
     * @param control
     */
    public GuiDemoBreadCrumbController(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        content = createVelocityContainer("breadcrump");
        // init bread crumb controller and add first element
        breadCrumbCtr = new BreadCrumbController(ureq, control);
        final int level = 0;
        final CrumbController ctr = new GuiDemoBreadCrumbContentController(ureq, getWindowControl(), level);
        breadCrumbCtr.activateFirstCrumbController(ctr);
        content.put("breadcrump", breadCrumbCtr.getInitialComponent());

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, control, this.getClass(), content);
        content.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(content);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (breadCrumbCtr != null) {
            breadCrumbCtr.dispose();
            breadCrumbCtr = null;
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to catch
    }

    /**
     * Inner class that implements the actual content. Description:<br>
     * <P>
     * Initial Date: 08.09.2008 <br>
     * 
     * @author gnaegi
     */
    private class GuiDemoBreadCrumbContentController extends CrumbBasicController {
        private final int crumbLevel;
        private final Link createNewLink;
        private Link removeCurrentLink;
        private final VelocityContainer main = createVelocityContainer("guidemo-breadcrumb");

        protected GuiDemoBreadCrumbContentController(final UserRequest ureq, final WindowControl control, final int crumbLevel) {
            super(ureq, control);
            this.crumbLevel = crumbLevel;
            main.contextPut("level", this.crumbLevel);
            createNewLink = LinkFactory.createButton("GuiDemoBreadCrumbController.button.add", main, this);
            if (crumbLevel != 0) {
                removeCurrentLink = LinkFactory.createButton("GuiDemoBreadCrumbController.button.remove", main, this);
            }
            putInitialPanel(main);
        }

        @Override
        protected void doDispose() {
            // child crumb controller auto disposed by deactivate method
        }

        @Override
        protected void event(final UserRequest ureq, final Component source, final Event event) {
            if (source.equals(createNewLink)) {
                final CrumbController ctr = new GuiDemoBreadCrumbContentController(ureq, getWindowControl(), this.crumbLevel + 1);
                activateAndListenToChildCrumbController(ctr);
            } else if (source.equals(removeCurrentLink)) {
                removeFromBreadCrumbPathAndDispose();
            }
        }

        @Override
        public String getCrumbLinkText() {
            return "crumb " + crumbLevel;
        }

        @Override
        public String getCrumbLinkHooverText() {
            return "click here to to go crumb " + 1;
        }

    }

}
