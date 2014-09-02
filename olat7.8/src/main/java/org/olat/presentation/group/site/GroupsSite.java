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

package org.olat.presentation.group.site;

import java.util.Locale;

import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.chiefcontrollers.BaseChiefController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.navigation.DefaultNavElement;
import org.olat.presentation.framework.core.control.navigation.NavElement;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.main.BGMainController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description:<br>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class GroupsSite implements SiteInstance {
    private static final OLATResourceable ORES_GROUPS = OresHelper.lookupType(BGMainController.class);

    // refer to the definitions in org.olat
    private static final String PACKAGE = PackageUtil.getPackageName(BaseChiefController.class);

    private final NavElement origNavElem;
    private NavElement curNavElem;

    /**
	 * 
	 */
    public GroupsSite(final Locale loc) {
        final Translator trans = new PackageTranslator(PACKAGE, loc);
        origNavElem = new DefaultNavElement(trans.translate("topnav.buddygroups"), trans.translate("topnav.buddygroups.alt"), "o_site_groups");
        origNavElem.setAccessKey("g".charAt(0));
        curNavElem = new DefaultNavElement(origNavElem);
    }

    /**
	 */
    @Override
    public NavElement getNavElement() {
        return curNavElem;
    }

    /**
	 */
    @Override
    public MainLayoutController createController(final UserRequest ureq, final WindowControl wControl) {
        final MainLayoutController c = ControllerFactory.createLaunchController(ORES_GROUPS, null, ureq, wControl, true);
        return c;
    }

    /**
	 */
    @Override
    public boolean isKeepState() {
        return true;
    }

    @Override
    public void reset() {
        curNavElem = new DefaultNavElement(origNavElem);
    }

}
