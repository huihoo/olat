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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.home.site;

import java.util.Locale;

import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.chiefcontrollers.BaseChiefController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.navigation.DefaultNavElement;
import org.olat.presentation.framework.core.control.navigation.NavElement;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.home.InviteeHomeMainController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;

/**
 * Description:<br>
 * TODO: srosse Class Description for InviteeHomeSite
 * <P>
 * Initial Date: 7 déc. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InviteeHomeSite implements SiteInstance {
    private static final OLATResourceable ORES_INVITEE = OresHelper.lookupType(InviteeHomeMainController.class);

    private final NavElement origNavElem;
    private NavElement curNavElem;

    /**
	 * 
	 */
    public InviteeHomeSite(final Locale loc) {
        final Translator trans = PackageUtil.createPackageTranslator(BaseChiefController.class, loc);
        origNavElem = new DefaultNavElement(trans.translate("topnav.guesthome"), trans.translate("topnav.guesthome.alt"), "o_site_home");
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
        final MainLayoutController c = ControllerFactory.createLaunchController(ORES_INVITEE, null, ureq, wControl, true);
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
