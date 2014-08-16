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

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
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
public class CreateInfoStepController extends StepFormBasicController {

    private final StepsRunContext runContext;
    private final InfoEditFormController infoEditFormController;

    public CreateInfoStepController(final UserRequest ureq, final WindowControl wControl, final StepsRunContext runContext, final Form rootForm) {
        super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

        this.runContext = runContext;

        infoEditFormController = new InfoEditFormController(ureq, wControl, rootForm);
        listenTo(infoEditFormController);

        initForm(ureq);
    }

    @Override
    public FormItem getStepFormItem() {
        return infoEditFormController.getInitialFormItem();
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        formLayout.add(infoEditFormController.getInitialFormItem());
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        return infoEditFormController.validateFormLogic(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        runContext.put(WizardConstants.MSG_TITLE, infoEditFormController.getTitle());
        runContext.put(WizardConstants.MSG_MESSAGE, infoEditFormController.getMessage());
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }
}
