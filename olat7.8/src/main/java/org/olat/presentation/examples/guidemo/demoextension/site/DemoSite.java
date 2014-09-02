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

package org.olat.presentation.examples.guidemo.demoextension.site;

import java.util.Locale;

import org.olat.presentation.examples.guidemo.demoextension.controller.DemoMainLayoutController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.navigation.DefaultNavElement;
import org.olat.presentation.framework.core.control.navigation.NavElement;
import org.olat.presentation.framework.core.control.navigation.SiteInstance;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for HomeSite
 * <P>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class DemoSite implements SiteInstance {

    private final NavElement origNavElem;
    private NavElement curNavElem;

    /**
     * @param loc
     */
    public DemoSite(final Locale loc) {
        final Translator trans = PackageUtil.createPackageTranslator(this.getClass(), loc);
        origNavElem = new DefaultNavElement(trans.translate("site.title"), trans.translate("site.title.alt"), "site_demo_icon");
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
    public MainLayoutController createController(final UserRequest userRequest, final WindowControl wControl) {
        return new DemoMainLayoutController(userRequest, wControl);
    }

    /**
	 */
    @Override
    public boolean isKeepState() {
        return true;
    }

    /**
	 */
    @Override
    public void reset() {
        curNavElem = new DefaultNavElement(origNavElem);
    }

}
