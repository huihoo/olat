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
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

public class GuiDemoPanesController extends BasicController {

    VelocityContainer vcMain;
    TabbedPane tabbedPane;
    VelocityContainer pane1, pane2, pane3, pane4;

    public GuiDemoPanesController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        vcMain = createVelocityContainer("guidemo-panes");

        tabbedPane = new TabbedPane("pane", ureq.getLocale());
        tabbedPane.addListener(this);

        pane1 = createVelocityContainer("guidemo-pane1");
        pane2 = createVelocityContainer("guidemo-pane2");
        pane3 = createVelocityContainer("guidemo-pane3");
        pane4 = createVelocityContainer("guidemo-pane4");

        tabbedPane.addTab("Pane 1", pane1);
        tabbedPane.addTab("Pane 2", pane2);
        tabbedPane.addTab("Pane 3", pane3);
        tabbedPane.addTab("Pane 4", pane4);
        // add a disabled tab
        tabbedPane.addTab("Disabled pane", new Panel("disabled"));
        tabbedPane.setEnabled(4, false);

        vcMain.put("panes", tabbedPane);

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), vcMain);
        vcMain.put("sourceview", sourceview.getInitialComponent());

        this.putInitialPanel(vcMain);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
    }

    @Override
    protected void doDispose() {
    }

}
