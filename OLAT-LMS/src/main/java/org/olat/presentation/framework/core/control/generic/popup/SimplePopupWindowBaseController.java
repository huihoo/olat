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
package org.olat.presentation.framework.core.control.generic.popup;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.chiefcontrollers.controller.simple.SimpleBaseController;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SimplePopupWindowBaseController
 * <P>
 * Initial Date: 25.07.2007 <br>
 * 
 * @author patrickb
 */
public class SimplePopupWindowBaseController extends BasicController implements PopupBrowserWindowController {

    private ControllerCreator contentControllerCreator;
    private SimpleBaseController layoutController;

    /**
     * @param ureq
     * @param wControl
     * @param contentControllerCreator
     */
    public SimplePopupWindowBaseController(UserRequest ureq, WindowControl wControl, ControllerCreator contentControllerCreator) {
        super(ureq, wControl);
        this.contentControllerCreator = contentControllerCreator;
        this.layoutController = new SimpleBaseController(ureq, wControl);
        putInitialPanel(layoutController.getInitialComponent());
    }

    /**
	 */
    @Override
    public void open(UserRequest ureq) {
        Controller contentController = contentControllerCreator.createController(ureq, getWindowControl());
        layoutController.setContentController(contentController);
        ureq.getDispatchResult().setResultingWindow(getWindowControl().getWindowBackOffice().getWindow());
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void doDispose() {
        //
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    public WindowControl getPopupWindowControl() {
        return getWindowControl();
    }

}
