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
 * Description:<br>
 * Iframe portlet to embedd content from another server in the portal. The configuration must have an element uri and height. Title and description are optional elements.
 * They use the locale code for each language (eg. title_de, description_en)
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 **/

package org.olat.presentation.portal.shiblogin;

import java.util.Map;

import org.olat.lms.security.authentication.shibboleth.ShibbolethModule;
import org.olat.lms.security.authentication.shibboleth.SwitchShibbolethAuthenticationConfigurator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.exception.OLATSecurityException;

public class ShibLoginPortlet extends AbstractPortlet {

    private String cssWrapperClass = "b_portlet_iframe";
    private Controller runCtr;
    private final SwitchShibbolethAuthenticationConfigurator config;

    protected ShibLoginPortlet(final SwitchShibbolethAuthenticationConfigurator config) {
        this.config = config;
    }

    /**
     * The portlet ins only enabled if the configuration of the portlet say so AND if the shibboleth authentication is enabled too.
     * 
     */
    @Override
    public boolean isEnabled() {
        return ShibbolethModule.isEnableShibbolethLogins() && super.isEnabled();
    }

    /**
	 */
    @Override
    public String getTitle() {
        String title = (String) getConfiguration().get("title_" + getTranslator().getLocale().toString());
        if (title == null) {
            title = getTranslator().translate("portlet.title");
        }
        return title;
    }

    /**
	 */
    @Override
    public String getDescription() {
        String desc = (String) getConfiguration().get("description_" + getTranslator().getLocale().toString());
        if (desc == null) {
            desc = getTranslator().translate("portlet.description");
        }
        return desc;
    }

    /**
	 */
    @Override
    public Portlet createInstance(final WindowControl wControl, final UserRequest ureq, final Map configuration) {
        if (!ShibbolethModule.isEnableShibbolethLogins()) {
            throw new OLATSecurityException("Got shibboleth wayf form request but shibboleth is not enabled.");
        }
        final ShibLoginPortlet p = new ShibLoginPortlet(config);
        p.setName(this.getName());
        p.setConfiguration(configuration);
        p.setTranslator(PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale()));
        // override css class if configured
        final String cssClass = (String) configuration.get("cssWrapperClass");
        if (cssClass != null) {
            p.setCssWrapperClass(cssClass);
        }
        return p;
    }

    /**
	 */
    @Override
    public Component getInitialRunComponent(final WindowControl wControl, final UserRequest ureq) {
        if (this.runCtr != null) {
            runCtr.dispose();
        }
        this.runCtr = new ShibLoginPortletRunController(ureq, wControl, config);
        return runCtr.getInitialComponent();
    }

    /**
	 */
    @Override
    public void dispose() {
        disposeRunComponent();
    }

    /**
	 */
    @Override
    public String getCssClass() {
        return cssWrapperClass;
    }

    /**
     * Helper used to overwrite the default css class with the configured class
     * 
     * @param cssWrapperClass
     */
    void setCssWrapperClass(final String cssWrapperClass) {
        this.cssWrapperClass = cssWrapperClass;
    }

    /**
	 */
    @Override
    public void disposeRunComponent() {
        if (runCtr != null) {
            runCtr.dispose();
            runCtr = null;
        }
    }

}
