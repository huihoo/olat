/**

 * 
 * 
 * 
 * 
 * 
 * 
 * 
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
package org.olat.presentation.course;

import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.messages.MessageController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DisposedCourseRestartController
 * <P>
 * Initial Date: 19.04.2008 <br>
 * 
 * @author patrickb
 */
public class DisposedCourseRestartController extends BasicController {

    private final VelocityContainer initialContent;
    private final Link restartLink;
    private final RepositoryEntry courseRepositoryEntry;
    private final Panel panel;

    public DisposedCourseRestartController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry courseRepositoryEntry) {
        super(ureq, wControl);
        initialContent = createVelocityContainer("disposedcourserestart");
        restartLink = LinkFactory.createButton("course.disposed.command.restart", initialContent, this);
        this.courseRepositoryEntry = courseRepositoryEntry;
        panel = putInitialPanel(initialContent);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == restartLink) {
            final DTabs dts = (DTabs) getWindowControl().getWindowBackOffice().getWindow().getAttribute("DTabs");
            final OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(courseRepositoryEntry.getOlatResource().getResourceableId(),
                    courseRepositoryEntry.getOlatResource().getResourceableTypeName());
            if (ores == null) {
                // course was deleted!
                final MessageController msgController = MessageUIFactory.createInfoMessage(ureq, this.getWindowControl(), translate("course.deleted.title"),
                        translate("course.deleted.text"));
                panel.setContent(msgController.getInitialComponent());
                return;
            }
            DTab dt = dts.getDTab(ores);
            // remove and dispose "old course run"
            dts.removeDTab(dt);// disposes also dt and controllers
            /*
             * create new tab with "refreshed course run" and activate the course
             */
            dt = dts.createDTab(ores, courseRepositoryEntry.getDisplayname());
            if (dt == null) {
                return; // full tabs -> warning already set by
            }
            // dts.create...
            final Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dt.getWindowControl(), true);
            dt.setController(launchController);
            dts.addDTab(dt);
            dts.activate(ureq, dt, null);
            /*
             * last but not least dispose myself - to clean up.
             */
            dispose();
        }
    }

}
