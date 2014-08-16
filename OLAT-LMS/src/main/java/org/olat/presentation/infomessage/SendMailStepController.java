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

package org.olat.presentation.infomessage;

import java.util.List;
import java.util.Set;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;

/**
 * Description:<br>
 * <P>
 * Initial Date: 27 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMailStepController extends StepFormBasicController {

    private final String[] sendOptionKeys;
    private final String[] sendOptionValues;

    private MultipleSelectionElement sendSelection;

    public SendMailStepController(final UserRequest ureq, final WindowControl wControl, final StepsRunContext runContext, final List<SendMailOption> options,
            final Form rootForm) {
        super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

        sendOptionKeys = new String[options.size()];
        sendOptionValues = new String[options.size()];
        int count = 0;
        for (final SendMailOption option : options) {
            sendOptionKeys[count] = option.getOptionKey();
            sendOptionValues[count++] = option.getOptionTranslatedName(ureq.getLocale());
        }

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("wizard.step1.title");
        setFormDescription("wizard.step1.form_description");
        sendSelection = uifactory.addCheckboxesVertical("wizard.step1.send_option", formLayout, sendOptionKeys, sendOptionValues, null, 1);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        final Set<String> selectedOptions = sendSelection.getSelectedKeys();
        addToRunContext(WizardConstants.SEND_MAIL, selectedOptions);
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }
}
