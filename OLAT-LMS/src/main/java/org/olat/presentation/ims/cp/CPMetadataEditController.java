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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.ims.cp;

import org.olat.lms.ims.cp.CPPage;
import org.olat.lms.ims.cp.ContentPackage;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This controller controls the workflows regarding the edit-process of a cp-page
 * <P>
 * Initial Date: 25.07.2008 <br>
 * 
 * @author sergio
 */
public class CPMetadataEditController extends BasicController {

    private Link closeLink;
    private final CPMDFlexiForm mdCtr; // MetadataController
    private final ContentPackage cp;
    private CPPage page;

    protected CPMetadataEditController(final UserRequest ureq, final WindowControl control, final CPPage page, final ContentPackage cp) {
        super(ureq, control);
        this.cp = cp;
        this.page = page;
        mdCtr = new CPMDFlexiForm(ureq, getWindowControl(), page);
        listenTo(mdCtr);
        putInitialPanel(mdCtr.getInitialComponent());
    }

    protected void newPageAdded(final String newNodeID) {
        this.page.setIdentifier(newNodeID);
    }

    /**
     * returns the CPPage, which is edited
     * 
     * @return
     */
    public CPPage getCurrentPage() {
        return page;
    }

    @Override
    protected void doDispose() {
        // nothing to do
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == closeLink) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == mdCtr) {
            page = mdCtr.getPage();
            fireEvent(ureq, event);
        }

    }

}
