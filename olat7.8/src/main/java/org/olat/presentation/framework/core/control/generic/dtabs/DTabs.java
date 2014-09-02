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

package org.olat.presentation.framework.core.control.generic.dtabs;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public interface DTabs {

    /**
     * @param ores
     * @return the tab
     */
    public DTab getDTab(OLATResourceable ores);

    /**
     * adds the tab. (upon Event.DONE of the contained controller && if controller is DTabAware -> controller.dispose called by dtabs)
     * 
     * @param dt
     */
    /**
     * @param ores
     * @param controller
     * @param title
     * @return the tab or null if the headerbar is full. if null, the implementation of the DTabs should issue a warning to the current windowcontrol
     */
    public DTab openDTab(OLATResourceable ores, Controller controller, String title);

    /**
     * Remove a tab from tabs-list.
     * 
     * @param dt
     *            Remove this tab
     */
    public void removeDTab(DTab dt);

    /**
     * @param ureq
     * @param dTab
     * @param viewIdentifier
     *            if null, no activation takes places
     */
    public void activate(UserRequest ureq, DTab dTab, String viewIdentifier);

    /**
     * FIXME:fj:b change string arg to class
     * 
     * @param ureq
     * @param className
     *            the name of the class implementing the siteinstance we would like to activate
     * @param viewIdentifier
     *            the subcommand (see docu of each controller implementing Activatable
     */
    public void activateStatic(UserRequest ureq, String className, String viewIdentifier);

    /**
     * @return WindowControl for this set of DynamicTabs
     */
    public WindowControl getWindowControl();

}
