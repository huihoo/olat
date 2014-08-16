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

package org.olat.presentation.framework.core.control.generic.tabbable;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: Apr 28, 2004
 * 
 * @author gnaegi<br>
 *         Comment: A TabbableDefaultController has the ability to add some of its components to another controller as tabbs in a tabbed pane. This is usefull when the
 *         parent controller has a tabbed pane component and wants to add more than one tab from the child controller to its tabbed pane. See the EditorMainController and
 *         the various NodeEditControllers for an example </pre>
 */
public abstract class TabbableDefaultController extends BasicController implements TabbableController {

    public TabbableDefaultController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    /**
	 */
    @Override
    public Component getInitialComponent() {
        throw new AssertException("please use addTabs(..) instead");
    }
}
