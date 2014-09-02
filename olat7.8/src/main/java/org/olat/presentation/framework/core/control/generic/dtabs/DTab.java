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

package org.olat.presentation.framework.core.control.generic.dtabs;

import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.StackedBusinessControl;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.presentation.framework.core.components.htmlheader.jscss.CustomCSS;
import org.olat.presentation.framework.core.components.htmlheader.jscss.CustomCSSProvider;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Disposable;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.layout.MainLayoutController;
import org.olat.presentation.framework.core.control.guistack.GuiStack;
import org.olat.presentation.framework.core.control.navigation.DefaultNavElement;
import org.olat.presentation.framework.core.control.navigation.NavElement;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * the api user view
 * <P>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 */
public class DTab implements Disposable, CustomCSSProvider {

    private final OLATResourceable ores;
    private final Controller controller;
    private final String title;
    private final WindowControl wControl;
    private final NavElement navElement;

    private GuiStack guiStackHandle;

    /**
     * @param ores
     * @param title
     * @param wControl
     */
    public DTab(OLATResourceable ores, Controller controller, String title, WindowControl wOrigControl) {
        this.ores = ores;
        this.controller = controller;
        this.title = title;
        // Root the JumpInPath - typically all resources are opened in tabs
        StackedBusinessControl businessControl = new StackedBusinessControl(null, wOrigControl.getBusinessControl());
        this.wControl = BusinessControlFactory.getInstance().createBusinessWindowControl(businessControl, wOrigControl);

        // TODO:fj:c calculate truncation depending on how many tabs are already open
        String typeName = ores.getResourceableTypeName();
        String shortTitle = title;
        if (!title.startsWith(I18nManager.IDENT_PREFIX)) {
            // don't truncate titles when in inline translation mode (OLAT-3811)
            shortTitle = Formatter.truncate(title, 15);
        }
        navElement = new DefaultNavElement(shortTitle, title, "b_resource_" + typeName.replace(".", "-"));
    }

    /**
     * @return the olat resourceable
     */
    public OLATResourceable getOLATResourceable() {
        return ores;
    }

    /**
     * @return the controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    public WindowControl getWindowControl() {
        return wControl;
    }

    /**
     * [used by velocity]
     * 
     * @return the navigation element for this dtab
     */
    public NavElement getNavElement() {
        return navElement;
    }

    /**
     * @return the gui stack handle
     */
    public GuiStack getGuiStackHandle() {
        if (guiStackHandle == null) {
            guiStackHandle = wControl.getWindowBackOffice().createGuiStack(controller.getInitialComponent());
        }
        return guiStackHandle;
    }

    @Override
    public void dispose() {
        if (controller != null) {// OLAT-3500
            controller.dispose();
        }
    }

    @Override
    public String toString() {
        return "DTab [ores: " + ores.getResourceableTypeName() + "," + ores.getResourceableId() + ", title: " + title + "]";
    }

    @Override
    public CustomCSS getCustomCSS() {
        // delegate to content controller if of type main layout controller
        if (controller != null && controller instanceof MainLayoutController) {
            MainLayoutController layoutController = (MainLayoutController) controller;
            return layoutController.getCustomCSS();
        }
        return null;
    }

}
