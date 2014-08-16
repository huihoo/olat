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
package org.olat.presentation.portal.zsuz;

import java.util.Map;

import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZentrallStellePortlet
 * <P>
 * Initial Date: 06.06.2008 <br>
 * 
 * @author patrickb
 */
public class ZentralstellePortlet extends AbstractPortlet {

    private ZentralstellePortletRunController runCtrl;

    protected ZentralstellePortlet() {
    }

    @Autowired
    UserService userService;

    /**
	 */
    @Override
    @SuppressWarnings({ "unused", "unchecked" })
    public Portlet createInstance(final WindowControl control, final UserRequest ureq, final Map portletConfig) {
        final Portlet p = new ZentralstellePortlet();
        p.setName(this.getName());
        p.setConfiguration(portletConfig);
        p.setTranslator(new PackageTranslator(PackageUtil.getPackageName(ZentralstellePortlet.class), ureq.getLocale()));
        return p;
    }

    /**
	 */
    @Override
    public void disposeRunComponent() {
        if (this.runCtrl != null) {
            runCtrl.dispose();
            runCtrl = null;
        }
    }

    /**
	 */
    @Override
    public String getCssClass() {
        // the zentralstelle icon
        return "o_portlet_zsuz";
    }

    /**
	 */
    @Override
    public String getDescription() {
        return getTranslator().translate("zsuz.infotext0");
    }

    /**
	 */
    @Override
    public Component getInitialRunComponent(final WindowControl wControl, final UserRequest ureq) {
        if (this.runCtrl != null) {
            runCtrl.dispose();
        }
        runCtrl = new ZentralstellePortletRunController(ureq, wControl);
        return runCtrl.getInitialComponent();
    }

    /**
	 */
    @Override
    public String getTitle() {
        return getTranslator().translate("zsuz.title");
    }

    /**
	 */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
