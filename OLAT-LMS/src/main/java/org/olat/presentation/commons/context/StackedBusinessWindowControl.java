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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.presentation.commons.context;

import org.olat.lms.commons.context.BusinessControl;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.info.WindowControlInfo;

/**
 * Description:<br>
 * <P>
 * Initial Date: 14.06.2006 <br>
 * 
 * @author Felix Jost
 */
public class StackedBusinessWindowControl implements WindowControl {
    private final WindowControl origWControl;
    private final BusinessControl businessControl;

    public StackedBusinessWindowControl(WindowControl origWControl, BusinessControl businessControl) {
        this.origWControl = origWControl;
        this.businessControl = businessControl;

    }

    @Override
    public BusinessControl getBusinessControl() {
        // inject the new business control here
        return businessControl;
    }

    @Override
    public WindowControlInfo getWindowControlInfo() {
        return origWControl.getWindowControlInfo();
    }

    @Override
    public void makeFlat() {
        origWControl.makeFlat();
    }

    @Override
    public void pop() {
        origWControl.pop();
    }

    @Override
    public void pushAsModalDialog(Component comp) {
        origWControl.pushAsModalDialog(comp);
    }

    @Override
    public void pushToMainArea(Component comp) {
        origWControl.pushToMainArea(comp);
    }

    @Override
    public void setError(String string) {
        origWControl.setError(string);
    }

    @Override
    public void setInfo(String string) {
        origWControl.setInfo(string);
    }

    @Override
    public void setWarning(String string) {
        origWControl.setWarning(string);
    }

    @Override
    public WindowBackOffice getWindowBackOffice() {
        return origWControl.getWindowBackOffice();
    }

}
