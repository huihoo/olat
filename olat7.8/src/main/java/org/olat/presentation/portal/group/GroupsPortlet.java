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

package org.olat.presentation.portal.group;

import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.control.generic.portal.PortletToolController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.BGControllerFactory;

/**
 * Description:<br>
 * Displays the list of most used groups
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupsPortlet extends AbstractPortlet {
    private static final String PACKAGE_UI = PackageUtil.getPackageName(BGControllerFactory.class);
    private GroupsPortletRunController runCtr;

    /**
	 */

    protected GroupsPortlet() {
    }

    @Override
    public Portlet createInstance(final WindowControl wControl, final UserRequest ureq, final Map configuration) {
        final Translator translator = new PackageTranslator(PACKAGE_UI, ureq.getLocale());
        final Portlet p = new GroupsPortlet();
        p.setName(this.getName());
        p.setConfiguration(configuration);
        p.setTranslator(translator);
        return p;
    }

    /**
	 */
    @Override
    public String getTitle() {
        return getTranslator().translate("groupsPortlet.title");
    }

    /**
	 */
    @Override
    public String getDescription() {
        return getTranslator().translate("groupsPortlet.description");
    }

    /**
	 */
    @Override
    public Component getInitialRunComponent(final WindowControl wControl, final UserRequest ureq) {
        if (this.runCtr != null) {
            runCtr.dispose();
        }
        this.runCtr = new GroupsPortletRunController(wControl, ureq, getTranslator(), this.getName());
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
        return "o_portlet_groups";
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

    @Override
    public PortletToolController getTools(final UserRequest ureq, final WindowControl wControl) {
        // portlet was not yet visible
        if (runCtr == null) {
            this.runCtr = new GroupsPortletRunController(wControl, ureq, getTranslator(), this.getName());
        }
        return runCtr.createSortingTool(ureq, wControl);
    }

}
