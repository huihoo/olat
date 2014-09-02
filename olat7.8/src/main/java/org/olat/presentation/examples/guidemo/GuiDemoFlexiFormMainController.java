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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.spacesaver.ShrinkController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Manages the sequence of flexi form demos, and provides a flexi form demo navigation.
 * <P>
 * Initial Date: 10.09.2007 <br>
 * 
 * @author patrickb
 */
public class GuiDemoFlexiFormMainController extends BasicController {

    private final VelocityContainer mainVC;
    private final Map<String, ControllerCreator> demos = new HashMap<String, ControllerCreator>();
    List<String> demolinknames;
    private Controller demoController;
    private final Panel contentP;
    private final VelocityContainer content_sourceVC;
    private final Panel sourceP;
    {
        // create the demos
        // could also be injected with spring
        //
        // for the order
        demolinknames = new ArrayList<String>();
        //
        demolinknames.add("guidemo_flexi_form_simpleform");
        demos.put("guidemo_flexi_form_simpleform", new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl wControl) {
                return new GuiDemoFlexiForm(ureq, wControl, null);
            }
        });
        demolinknames.add("guidemo_flexi_form_withchooser");
        demos.put("guidemo_flexi_form_withchooser", new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl wControl) {
                return new GuiDemoFlexiFormSubworkflow(ureq, wControl, null);
            }
        });
        demolinknames.add("guidemo_flexi_form_customlayout");
        demos.put("guidemo_flexi_form_customlayout", new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl wControl) {
                return new GuiDemoFlexiFormCustomlayout(ureq, wControl, null);
            }
        });
        demolinknames.add("guidemo_flexi_form_hideunhide");
        demos.put("guidemo_flexi_form_hideunhide", new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl wControl) {
                return new GuiDemoFlexiFormHideUnhide(ureq, wControl, null);
            }
        });
        demolinknames.add("guidemo_flexi_form_inline");
        demos.put("guidemo_flexi_form_inline", new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl wControl) {
                return new GuiDemoInlineEditingBasedOnFlexiForm(ureq, wControl);
            }
        });
        demolinknames.add("guidemo_flexi_form_advanced");
        demos.put("guidemo_flexi_form_advanced", new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl wControl) {
                return new GuiDemoFlexiFormAdvancedController(ureq, wControl);
            }
        });
    }

    public GuiDemoFlexiFormMainController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        mainVC = createVelocityContainer("flexiformdemos");

        for (final String linkName : demolinknames) {
            final Link tmpLink = LinkFactory.createLink(linkName, mainVC, this);
            tmpLink.setUserObject(linkName);
        }

        mainVC.contextPut("demolinknames", demolinknames);

        // all democontroller content goes in this panel
        contentP = new Panel("content");
        content_sourceVC = createVelocityContainer("content_source");
        mainVC.put("democontent", contentP);
        //
        final String firstDemo = demolinknames.iterator().next();
        final ControllerCreator cc = demos.get(firstDemo);
        demoController = cc.createController(ureq, getWindowControl());
        contentP.setContent(demoController.getInitialComponent());

        sourceP = new Panel("sourceP");
        final VelocityContainer sourceVC = createVelocityContainer(firstDemo);
        final ShrinkController sc = new ShrinkController(ureq, getWindowControl(), false, sourceVC, "toggle source");
        sourceP.setContent(sc.getInitialComponent());

        content_sourceVC.put("content", mainVC);
        content_sourceVC.put("source", sourceP);
        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), content_sourceVC);
        mainVC.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(content_sourceVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (demoController != null) {
            demoController.dispose();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // there are only events of type link from the demos navigation
        if (source instanceof Link) {
            final Link sl = (Link) source;
            // userobject tells which demo to activate
            final String uob = (String) sl.getUserObject();
            if (uob != null) {
                final ControllerCreator cc = demos.get(uob);
                // update source
                final VelocityContainer sourceVC = createVelocityContainer(uob);
                final ShrinkController sc = new ShrinkController(ureq, getWindowControl(), false, sourceVC, "toggle source");
                sourceP.setContent(sc.getInitialComponent());

                // cleanup former democontroller
                if (demoController != null) {
                    demoController.dispose();
                }
                contentP.popContent();
                // create new demo controller
                demoController = cc.createController(ureq, getWindowControl());
                contentP.pushContent(demoController.getInitialComponent());
            }
        }

    }

}
