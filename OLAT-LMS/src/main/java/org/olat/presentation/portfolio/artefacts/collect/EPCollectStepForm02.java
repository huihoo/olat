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
package org.olat.presentation.portfolio.artefacts.collect;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;

/**
 * Description:<br>
 * controller to let the user ensure that he has the copyright on this artefact
 * <P>
 * Initial Date: 28.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm02 extends StepFormBasicController {

    private final AbstractArtefact artefact;
    private MultipleSelectionElement crCheck;

    public EPCollectStepForm02(final UserRequest ureq, final WindowControl windowControl, final Form form, final StepsRunContext stepsRunContext,
            final int layoutDefault, final String customLayoutPageName, final AbstractArtefact artefact) {
        super(ureq, windowControl, form, stepsRunContext, layoutDefault, customLayoutPageName);
        this.artefact = artefact;
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormDescription("copyright.intro.text");

        final String[] theKeys = new String[] { Boolean.TRUE.toString() };
        final String[] theValues = new String[] { translate("copyright.yes") };
        crCheck = uifactory.addCheckboxesHorizontal("copyright.label", null, formLayout, theKeys, theValues, null);
        if (getFromRunContext("copyright.accepted") != null && (Boolean) getFromRunContext("copyright.accepted")) {
            crCheck.select(Boolean.TRUE.toString(), true);
        }
        // signature > 0 means, collection wizzard can be sure its from OLAT, < 0 means get an approval by user (the target value is the negative one)
        if (artefact.getSignature() > 0) {
            crCheck.select(Boolean.TRUE.toString(), true);
        }
        crCheck.addActionListener(this, FormEvent.ONCHANGE);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == crCheck) {
            validateAndSetError();
        }
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        return super.validateFormLogic(ureq) && validateAndSetError();
    }

    private boolean validateAndSetError() {
        if (!crCheck.isSelected(0) || (getFromRunContext("copyright.accepted") != null && !(Boolean) getFromRunContext("copyright.accepted"))) {
            crCheck.setErrorKey("copyright.error", null);
            return false;
        } else {
            crCheck.showError(false);
            return true;
        }
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // its accepted, as form has been validated before
        if (isUsedInStepWizzard()) {
            addToRunContext("copyright.accepted", true);
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        } else {
            // if used outside steps wizzard, persist stuff here
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }
}
