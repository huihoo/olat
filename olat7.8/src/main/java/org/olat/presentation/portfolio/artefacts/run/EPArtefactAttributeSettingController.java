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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.portfolio.artefacts.run;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Allows to set the attributes which then will be displayed for an artefact. settings are persisted as property.
 * <P>
 * Initial Date: 13.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactAttributeSettingController extends FormBasicController {

    private final Map<String, Boolean> artAttribConfig;
    private final EPFrontendManager ePFMgr;

    public EPArtefactAttributeSettingController(final UserRequest ureq, final WindowControl wControl, final Map<String, Boolean> artAttribConfig) {
        super(ureq, wControl);
        this.artAttribConfig = artAttribConfig;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        initForm(ureq);
    }

    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormDescription("display.option.intro");

        final String[] keys = new String[] { "onoff" };
        final String[] values = new String[] { translate("display.option.enabled") };
        final Map<String, Boolean> allArtAttribs = ePFMgr.getArtefactAttributeConfig(null);
        for (final Iterator<Entry<String, Boolean>> iterator = allArtAttribs.entrySet().iterator(); iterator.hasNext();) {
            final Entry<String, Boolean> entry = iterator.next();
            final String attKey = entry.getKey();
            Boolean attVal = artAttribConfig.get(attKey);
            final MultipleSelectionElement chkBox = uifactory.addCheckboxesHorizontal(attKey, formLayout, keys, values, null);
            chkBox.addActionListener(this, FormEvent.ONCHANGE);
            if (attVal == null) {
                attVal = entry.getValue(); // either use users settings or the defaults
            }
            chkBox.select(keys[0], attVal);
        }
        uifactory.addFormSubmitButton("display.option.submit", formLayout);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    @SuppressWarnings("unused")
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source instanceof MultipleSelectionElement) {
            final MultipleSelectionElement chkBox = (MultipleSelectionElement) source;
            artAttribConfig.put(chkBox.getName(), chkBox.isSelected(0));
        }
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // fire event to close overlay and update the displayed
        // artefacts
        ePFMgr.setArtefactAttributeConfig(getIdentity(), artAttribConfig);
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
