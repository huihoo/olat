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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.control;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.Window;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class DefaultChiefController extends DefaultController implements ChiefController {
    private Window window;
    private WindowControl windowControl;

    /**
	 * 
	 */
    public DefaultChiefController() {
        super(null);
        // nothing to do
    }

    /**
     * Gets the window.
     * 
     * @return the window
     */
    @Override
    public Window getWindow() {
        return window;
    }

    /**
     * Sets the window.
     * 
     * @param window
     *            The window to set
     */
    protected void setWindow(Window window) {
        this.window = window;
    }

    /**
	 */
    @Override
    protected abstract void event(UserRequest ureq, Component source, Event event);

    /**
	 */
    @Override
    public void addControllerListener(ControllerEventListener el) {
        throw new AssertException("cannot listen to a chiefcontroller");
    }

    @Override
    protected void setInitialComponent(Component initialComponent) {
        throw new AssertException("please use getWindow().setContentPane() instead!");
    }

    /**
	 */
    @Override
    public Component getInitialComponent() {
        throw new AssertException("please use getWindow().getContentPane() instead!");
    }

    /**
     * overrides the method in DefaultController since here we need the original WindowControl
     * 
     */
    @Override
    public WindowControl getWindowControl() {
        return windowControl;
    }

    protected void setWindowControl(WindowControl windowControl) {
        this.windowControl = windowControl;
    }

}
