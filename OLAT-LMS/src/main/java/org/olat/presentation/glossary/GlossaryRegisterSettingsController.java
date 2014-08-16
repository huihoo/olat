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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.presentation.glossary;

import java.util.Properties;

import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.resource.OLATResource;
import org.olat.lms.glossary.GlossaryItemManager;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * allows to set register/index on/off for repository typ glossary
 * <P>
 * Initial Date: 20.01.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryRegisterSettingsController extends FormBasicController {

    private final OLATResource olatresource;
    private MultipleSelectionElement regOnOff;
    private final OlatRootFolderImpl glossaryFolder;

    public GlossaryRegisterSettingsController(final UserRequest ureq, final WindowControl control, final OLATResource resource) {
        super(ureq, control);
        this.olatresource = resource;
        glossaryFolder = GlossaryManager.getInstance().getGlossaryRootFolder(olatresource);

        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == regOnOff) {
            final boolean regOnChecked = regOnOff.isSelected(0);
            final GlossaryItemManager gIM = GlossaryItemManager.getInstance();
            final Properties glossProps = gIM.getGlossaryConfig(glossaryFolder);
            glossProps.put(GlossaryItemManager.REGISTER_ONOFF, String.valueOf(regOnChecked));
            gIM.setGlossaryConfig(glossaryFolder, glossProps);
        }
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // saved in innerEvent
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("register.title");
        setFormDescription("register.intro");
        final String[] regKeys = { "true" };
        final String[] regValues = { "" };
        final String[] regCSS = new String[1];

        regOnOff = uifactory.addCheckboxesHorizontal("register.onoff", formLayout, regKeys, regValues, regCSS);
        regOnOff.addActionListener(listener, FormEvent.ONCLICK);

        final Properties glossProps = GlossaryItemManager.getInstance().getGlossaryConfig(glossaryFolder);
        final String configuredStatus = glossProps.getProperty(GlossaryItemManager.REGISTER_ONOFF);
        if (configuredStatus != null) {
            regOnOff.select(configuredStatus, true);
        }
    }

}
