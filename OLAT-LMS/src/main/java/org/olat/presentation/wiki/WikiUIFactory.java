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
package org.olat.presentation.wiki;

import org.olat.lms.wiki.WikiSecurityCallback;
import org.olat.presentation.commons.OLATResourceableListeningWrapperController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Creates the available wiki controllers
 * 
 * <P>
 * Initial Date: 10.03.2011 <br>
 * 
 * @author guido
 */
public class WikiUIFactory {

    private static WikiUIFactory INSTANCE = new WikiUIFactory();

    /**
     * singleton to access the wiki controllers
     */
    private WikiUIFactory() {
        // singleton
    }

    public static WikiUIFactory getInstance() {
        return INSTANCE;
    }

    public Controller createWikiMainController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores,
            final WikiSecurityCallback securityCallback, final String initialPageName) {
        return new WikiMainController(ureq, wControl, ores, securityCallback, initialPageName);
    }

    /**
     * brasato:::: to discuss, are two methods needed at all? probably not, unless there are cases to launch this controller without an ores known. such as
     * contentPackacking which requires only a fileroot (but this file directory depends on a ores in the end) create a wikiMaincontroller which disposes itself when the
     * associated olatresourceable is disposed.
     */
    public Controller createWikiMainControllerDisposeOnOres(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores,
            final WikiSecurityCallback securityCallback, final String initialPageName) {
        final Controller controller = new WikiMainController(ureq, wControl, ores, securityCallback, initialPageName);
        final OLATResourceableListeningWrapperController dwc = new OLATResourceableListeningWrapperController(ureq, wControl, ores, controller, ureq.getIdentity());
        return dwc;
    }

}
