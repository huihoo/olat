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

package org.olat.presentation.portal.didYouKnow;

import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;

/**
 * Description:<br>
 * Displays a random tip about the OLAT usage
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 */
public class DidYouKnowPortlet extends AbstractPortlet {
    private Controller runCtr;

    /**
	 */

    protected DidYouKnowPortlet() {
    }

    @Override
    public String getTitle() {
        return getTranslator().translate("didYouKnow.title");
    }

    /**
	 */
    @Override
    public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map configuration) {
        Portlet p = new DidYouKnowPortlet();
        p.setName(this.getName());
        p.setConfiguration(configuration);
        p.setTranslator(new PackageTranslator(PackageUtil.getPackageName(DidYouKnowPortlet.class), ureq.getLocale()));
        return p;
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
    public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
        if (this.runCtr != null)
            runCtr.dispose();
        this.runCtr = new DidYouKnowPortletRunController(ureq, wControl);
        return this.runCtr.getInitialComponent();
    }

    /**
	 */
    @Override
    public String getDescription() {
        return getTranslator().translate("didYouKnow.description");
    }

    /**
	 */
    @Override
    public String getCssClass() {
        return "b_portlet_dyk";
    }

    /**
	 */
    @Override
    public void disposeRunComponent() {
        if (this.runCtr != null) {
            this.runCtr.dispose();
            this.runCtr = null;
        }
    }

}
