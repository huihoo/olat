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

import org.olat.presentation.framework.core.control.state.ControllerState;
import org.olat.presentation.framework.core.control.state.ExtendedControllerState;

/**
 * @author Felix Jost, http://www.goodsolutions.ch represents the controller and its state. the important thing is that this object is stored in history and must
 *         therefore be immutable (otherwise we could simply take a controller to provide this data)
 */
public class ExtendedControllerStateImpl implements ExtendedControllerState {

    private final ControllerState prevState, curState;
    private final String controllerClassName;
    private final long controllerUniqueId;

    ExtendedControllerStateImpl(ControllerState prevState, ControllerState curState, long controllerUniqueId, String controllerClassName) {
        this.prevState = prevState;
        this.curState = curState;
        this.controllerUniqueId = controllerUniqueId;
        this.controllerClassName = controllerClassName;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public String getControllerClassName() {
        return controllerClassName;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public ControllerState getCurrentState() {
        return curState;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public ControllerState getPreviousState() {
        return prevState;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public long getControllerUniqueId() {
        return controllerUniqueId;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public boolean isSame(ExtendedControllerState mystate) {
        return (mystate.getControllerClassName().equals(controllerClassName) && mystate.getControllerUniqueId() == controllerUniqueId && curState.isSame(mystate
                .getCurrentState()));
    }

    @Override
    public String toString() {
        if (prevState.isSame(curState)) {
            return "class:" + controllerClassName + " (" + controllerUniqueId + ") [ NO_TRANS " + curState + "]";
        } else {
            return "class:" + controllerClassName + " (" + controllerUniqueId + ") [" + prevState + " -> " + curState + "]";
        }
    }

}
