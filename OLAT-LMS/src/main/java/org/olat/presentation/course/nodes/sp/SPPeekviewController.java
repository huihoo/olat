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
package org.olat.presentation.course.nodes.sp;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.framework.common.htmlpageview.SinglePageController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.download.DownloadComponent;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This is the implementatino of the peekview for the sp course node. Files of type html, htm or xhtml are displayed in the peekview with a 75% scaling. For other types
 * only a download link is displayed.
 * <P>
 * Initial Date: 09.12.2009 <br>
 * 
 * @author gnaegi
 */
public class SPPeekviewController extends BasicController {

    /**
     * Constructor for the sp peek view
     * 
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param config
     * @param ores
     */
    // TODO: MERGE PROBLEM ????
    // public SPPeekviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final ModuleConfiguration config,
    // final OLATResourceable ores) {
    // super(ureq, wControl);
    // // just display the page
    // final String file = config.getStringValue(SPEditController.CONFIG_KEY_FILE);
    // }

    /**
     * Constructor for the sp peek view
     * 
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param config
     * @param ores
     */
    public SPPeekviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final ModuleConfiguration config,
            final OLATResourceable ores) {
        super(ureq, wControl);
        // just display the page
        final String file = config.getStringValue(SPEditController.CONFIG_KEY_FILE);
        Component resPanel = new Panel("empty"); // empty panel to use if no file could be found
        if (file != null) {
            String fileLC = file.toLowerCase();
            if (fileLC.endsWith(".html") || fileLC.endsWith(".htm") || fileLC.endsWith(".xhtml")) {
                // Render normal view but scaled down to 75%
                SinglePageController spController = new SinglePageController(ureq, wControl, userCourseEnv.getCourseEnvironment().getCourseFolderContainer(), file, null,
                        config.getBooleanEntry(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS), ores);
                // but add scaling to fit preview into minimized space
                spController.setScaleFactorAndHeight(0.75f, 400, true);
                listenTo(spController);
                resPanel = spController.getInitialComponent();
            } else {
                // Render a download link for file
                VFSContainer courseFolder = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
                VFSItem downloadItem = courseFolder.resolve(file);
                if (file != null && downloadItem instanceof VFSLeaf) {
                    DownloadComponent downloadComp = new DownloadComponent("downloadComp", (VFSLeaf) downloadItem);
                    VelocityContainer peekviewVC = createVelocityContainer("peekview");
                    peekviewVC.put("downloadComp", downloadComp);
                    resPanel = peekviewVC;
                }
            }
            putInitialPanel(resPanel);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // autodisposed by basic controller
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to catch
    }

}
