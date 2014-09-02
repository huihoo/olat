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

package org.olat.presentation.portal.iframe;

import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;

/**
 * Description:<br>
 * Iframe portlet to embedd content from another server in the portal. The configuration must have an element uri and height. Title and description are optional elements.
 * They use the locale code for each language (eg. title_de, description_en)
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 */
public class IframePortlet extends AbstractPortlet {
    private IframePortletRunController runCtr;
    private String cssWrapperClass = "b_portlet_iframe";

    /**
	 */

    protected IframePortlet() {
    }

    @Override
    public String getTitle() {
        String title = (String) getConfiguration().get("title_" + getTranslator().getLocale().toString());
        if (title == null) {
            title = getTranslator().translate("iframe.title");
        }
        return title;
    }

    /**
	 */
    @Override
    public String getDescription() {
        String desc = (String) getConfiguration().get("description_" + getTranslator().getLocale().toString());
        if (desc == null) {
            desc = getTranslator().translate("iframe.description");
        }
        return desc;
    }

    /**
	 */
    @Override
    public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map configuration) {
        IframePortlet p = new IframePortlet();
        p.setName(this.getName());
        p.setConfiguration(configuration);
        p.setTranslator(new PackageTranslator(PackageUtil.getPackageName(IframePortlet.class), ureq.getLocale()));
        // override css class if configured
        String cssClass = (String) configuration.get("cssWrapperClass");
        if (cssClass != null)
            p.setCssWrapperClass(cssClass);
        return p;
    }

    /**
	 */
    @Override
    public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
        if (this.runCtr != null)
            runCtr.dispose();
        this.runCtr = new IframePortletRunController(ureq, wControl, getConfiguration());
        return this.runCtr.getInitialComponent();
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
    void setCssWrapperClass(String cssWrapperClass) {
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
