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
package org.olat.presentation.home;

import org.olat.lms.commons.context.ContextEntry;
import org.olat.presentation.commons.context.ContextEntryControllerCreator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.home.site.HomeSite;

/**
 * Initial Date: 08.06.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class HomeContextEntryControllerCreator implements ContextEntryControllerCreator {

    @Override
    public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
        return null;
    }

    @Override
    public String getTabName(ContextEntry ce) {
        return null;
    }

    @Override
    public String getSiteClassName(ContextEntry ce) {
        return HomeSite.class.getName();
    }

    @Override
    public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
        return true;
    }

}
