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

package org.olat.presentation.course.nodes.info;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Panel for the configuration of the info messages course node
 * <P>
 * Initial Date: 3 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoConfigForm extends FormBasicController {

    private static final String[] maxDurationValues = new String[] { "5", "10", "30", "90", "365", "\u221E" };

    private static final String[] maxLengthValues = new String[] { "1", "2", "3", "4", "5", "7", "10", "25", "\u221E" };

    private final ModuleConfiguration config;

    private SingleSelection durationSelection;
    private SingleSelection lengthSelection;

    public InfoConfigForm(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config) {
        super(ureq, wControl);

        this.config = config;

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("pane.tab.infos_config.title");
        setFormContextHelp(InfoConfigForm.class.getPackage().getName(), "ced-info-config.html", "help.hover.info.config");

        final String page = velocity_root + "/editShow.html";
        final FormLayoutContainer showLayout = FormLayoutContainer.createCustomFormLayout("pane.tab.infos_config.shown", getTranslator(), page);
        showLayout.setLabel("pane.tab.infos_config.shown", null);
        formLayout.add(showLayout);

        durationSelection = uifactory.addDropdownSingleselect("pane.tab.infos_config.max_duration", showLayout, maxDurationValues, maxDurationValues, null);
        durationSelection.setLabel("pane.tab.infos_config.max", null);
        final String durationStr = (String) config.get(InfoCourseNodeConfiguration.CONFIG_DURATION);
        if (StringHelper.containsNonWhitespace(durationStr)) {
            durationSelection.select(durationStr, true);
        } else {
            durationSelection.select("30", true);
        }

        lengthSelection = uifactory.addDropdownSingleselect("pane.tab.infos_config.max_shown", showLayout, maxLengthValues, maxLengthValues, null);
        lengthSelection.setLabel("pane.tab.infos_config.max", null);
        final String lengthStr = (String) config.get(InfoCourseNodeConfiguration.CONFIG_LENGTH);
        if (StringHelper.containsNonWhitespace(lengthStr)) {
            lengthSelection.select(lengthStr, true);
        } else {
            lengthSelection.select("5", true);
        }

        uifactory.addFormSubmitButton("save", formLayout);
    }

    protected ModuleConfiguration getUpdatedConfig() {
        final String durationStr = durationSelection.getSelectedKey();
        config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, durationStr);

        final String lengthStr = lengthSelection.getSelectedKey();
        config.set(InfoCourseNodeConfiguration.CONFIG_LENGTH, lengthStr);

        return config;
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }
}
