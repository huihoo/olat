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

import org.olat.lms.commons.context.BusinessControl;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.info.WindowControlInfo;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * Initial Date: Aug 10, 2005 <br>
 * 
 * @author Felix Jost
 */
public class LocalWindowControl implements WindowControl {
    private final WindowControl origWControl;
    private int localHeight = 0;
    // private final Controller controller;
    private final WindowControlInfoImpl wci;

    LocalWindowControl(WindowControl origWControl, DefaultController defaultcontroller) {
        this.origWControl = origWControl;
        wci = new WindowControlInfoImpl(defaultcontroller, (origWControl == null ? null : origWControl.getWindowControlInfo()));
    }

    /**
	 */
    @Override
    public void pop() {
        if (localHeight == 0)
            throw new AssertException("cannot pop below surface...");
        origWControl.pop();
        localHeight--;
    }

    /**
	 */
    @Override
    public void pushAsModalDialog(Component comp) {
        origWControl.pushAsModalDialog(comp);
        localHeight++;
    }

    /**
	 */
    @Override
    public void pushToMainArea(Component comp) {
        origWControl.pushToMainArea(comp);
        localHeight++;
    }

    /**
	 */
    @Override
    public void setError(String string) {
        origWControl.setError(string);
    }

    /**
	 */
    @Override
    public void setInfo(String string) {
        origWControl.setInfo(string);
    }

    /**
	 */
    @Override
    public void setWarning(String string) {
        origWControl.setWarning(string);
    }

    @Override
    public void makeFlat() {
        for (int i = 0; i < localHeight; i++) {
            origWControl.pop();
        }
        localHeight = 0;
    }

    @Override
    public WindowControlInfo getWindowControlInfo() {
        return wci;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public BusinessControl getBusinessControl() {
        return origWControl.getBusinessControl();
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public WindowBackOffice getWindowBackOffice() {
        return origWControl.getWindowBackOffice();
    }
}
