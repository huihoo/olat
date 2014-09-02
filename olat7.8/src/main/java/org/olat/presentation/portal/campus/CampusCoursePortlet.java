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
package org.olat.presentation.portal.campus;

import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Initial Date: 23.05.2012 <br>
 * 
 * @author cg
 */
// TODO: Code duplication : Refactor common code to AbstractPortlet
public class CampusCoursePortlet extends AbstractPortlet {

    private Controller runCtrl;

    @Override
    public String getTitle() {
        return getTranslator().translate("portlet.title");
    }

    @Override
    public String getDescription() {
        return getTranslator().translate("portlet.description");
    }

    @Override
    public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map portletConfig) {
        final Translator translator = PackageUtil.createPackageTranslator(CampusCoursePortlet.class, ureq.getLocale());
        final Portlet p = new CampusCoursePortlet();
        p.setName(getName());
        p.setTranslator(translator);
        return p;
    }

    @Override
    public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
        if (runCtrl != null) {
            runCtrl.dispose();
        }
        runCtrl = new CampusCoursePortletRunController(wControl, ureq, getTranslator(), getName());
        return runCtrl.getInitialComponent();
    }

    @Override
    public void dispose() {
        disposeRunComponent();
    }

    @Override
    public void disposeRunComponent() {
        if (this.runCtrl != null) {
            this.runCtrl.dispose();
            this.runCtrl = null;
        }
    }

    @Override
    public String getCssClass() {
        return "o_portlet_campus";
    }

}
