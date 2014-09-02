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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.admin.layout;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * 
 * 
 * @author Christian Guretzki
 */
public class LayoutAdminController extends FormBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private SingleSelection themeSelection;
    private SystemPropertiesService properties;

    public LayoutAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        properties = (SystemPropertiesService) CoreSpringFactory.getBean(SystemPropertiesService.class);
        initForm(this.flc, this, ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // wrapper container that generates standard layout for the form elements
        final FormItemContainer themeAdminFormContainer = FormLayoutContainer.createDefaultFormLayout("themeAdminFormContainer", getTranslator());
        formLayout.add(themeAdminFormContainer);

        final String[] keys = getThemes();
        final String enabledTheme = properties.getStringProperty(PropertyLocator.LAYOUT_THEME);
        themeSelection = uifactory.addDropdownSingleselect("themeSelection", "form.theme", themeAdminFormContainer, keys, keys, null);
        themeSelection.select(enabledTheme, true);
        themeSelection.addActionListener(listener, FormEvent.ONCHANGE);
    }

    /**
     * TODO service method, move to LMS layer
     * 
     * @return
     */
    private String[] getThemes() {
        // get all themes from disc
        final String staticAbsPath = WebappHelper.getContextRoot() + "/static/themes";
        final File themesDir = new File(staticAbsPath);
        if (!themesDir.exists()) {
            log.warn("Themes dir not found: " + staticAbsPath, null);
        }
        final File[] themes = themesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                // remove files - only accept dirs
                if (!new File(dir, name).isDirectory()) {
                    return false;
                }
                // remove unwanted meta-dirs
                if (name.equalsIgnoreCase("CVS")) {
                    return false;
                } else if (name.equalsIgnoreCase(".DS_Store")) {
                    return false;
                } else {
                    return true;
                }
            }
        });

        final String[] themesStr = new String[themes.length];
        for (int i = 0; i < themes.length; i++) {
            final File theme = themes[i];
            themesStr[i] = theme.getName();
        }
        return themesStr;
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        // set new theme in Settings
        final String newThemeIdentifyer = themeSelection.getSelectedKey();

        // OLAT-6438 see reason for commenting this
        // Settings.setGuiThemeIdentifyerGlobally(newThemeIdentifyer);
        properties.setProperty(PropertyLocator.LAYOUT_THEME, newThemeIdentifyer);
        // use new theme in current window
        getWindowControl().getWindowBackOffice().getWindow().getGuiTheme().init(newThemeIdentifyer);
        getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
        //
        log.info("Audit:GUI theme changed: " + newThemeIdentifyer);
        fireEvent(ureq, Event.CHANGED_EVENT);

        throw new NotImplementedException("the stoage of the gui theme hase been removed from the settings class and has to be remplemented in a better way");
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to clean up
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // saving already done in formInnerEvent method - no submit button
    }

}
