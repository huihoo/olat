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

import java.io.IOException;

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.system.event.Event;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Description: Loads a test textmarker file and applies it to the content of the html file
 * 
 * @author gnaegi Initial Date: Jul 14, 2006
 */
public class GuiDemoTextMarkerController extends BasicController {

    VelocityContainer vcMain;
    GlossaryMarkupItemController glossCtr;

    public GuiDemoTextMarkerController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        vcMain = createVelocityContainer("guidemo-textmarker");
        final Resource resource = new ClassPathResource("/org/olat/presentation/examples/guidemo/_static/" + GlossaryManager.INTERNAL_FOLDER_NAME);
        final String glossaryId = "guiDemoTestGlossary";
        try {
            glossCtr = new GlossaryMarkupItemController(ureq, getWindowControl(), vcMain, new LocalFolderImpl(resource.getFile()), glossaryId);
            listenTo(glossCtr);
        } catch (final IOException e) {
            showInfo("GuiDemoTextMarkerController.notWorking");
        }
        glossCtr.setTextMarkingEnabled(true);
        putInitialPanel(glossCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to catch
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
