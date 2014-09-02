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
package org.olat.presentation.course.editor;

import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.repository.PropPupForm;

/**
 * Description:<br>
 * TODO: patrickb Class Description for PublishStep01
 * <P>
 * Initial Date: 21.01.2008 <br>
 * 
 * @author patrickb
 */
class PublishStep01 extends BasicStep {

    private PrevNextFinishConfig prevNextConfig;
    private final boolean hasPublishableChanges;

    public PublishStep01(final UserRequest ureq, final boolean hasPublishableChanges) {
        super(ureq);
        setI18nTitleAndDescr("publish.access.header", null);

        this.hasPublishableChanges = hasPublishableChanges;
        if (hasPublishableChanges) {
            setNextStep(new PublishStep00a(ureq));
            prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
        } else {
            setNextStep(Step.NOSTEP);
            prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
        }
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        // can go back and finish immediately
        return prevNextConfig;
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl wControl, final StepsRunContext stepsRunContext, final Form form) {
        return new PublishStep01AccessForm(ureq, wControl, form, stepsRunContext, hasPublishableChanges);
    }

    class PublishStep01AccessForm extends StepFormBasicController {

        private SingleSelection accessSelbox;
        private final String selectedAccess;
        private final boolean hasPublishableChanges2;

        PublishStep01AccessForm(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext,
                final boolean hasPublishableChanges2) {
            super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
            this.hasPublishableChanges2 = hasPublishableChanges2;
            selectedAccess = (String) getFromRunContext("selectedCourseAccess");
            initForm(ureq);
        }

        @Override
        protected void doDispose() {
            // TODO Auto-generated method stub
        }

        @Override
        protected void formOK(final UserRequest ureq) {
            final String newAccess = accessSelbox.getKey(accessSelbox.getSelected());
            if (!selectedAccess.equals(newAccess)) {
                // only change if access was changed
                addToRunContext("changedaccess", newAccess);
            }
            if (hasPublishableChanges2) {
                fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
            } else {
                fireEvent(ureq, StepsEvent.INFORM_FINISHED);
            }

        }

        @Override
        @SuppressWarnings("unused")
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            final PackageTranslator pt = (PackageTranslator) PackageUtil.createPackageTranslator(PropPupForm.class, getLocale(), getTranslator());

            final FormItemContainer fic = FormLayoutContainer.createCustomFormLayout("access", pt, this.velocity_root + "/publish_courseaccess.html");
            formLayout.add(fic);
            final String[] keys = new String[] { "" + RepositoryEntry.ACC_OWNERS, "" + RepositoryEntry.ACC_OWNERS_AUTHORS, "" + RepositoryEntry.ACC_USERS,
                    "" + RepositoryEntry.ACC_USERS_GUESTS };
            final String[] values = new String[] { pt.translate("cif.access.owners"), pt.translate("cif.access.owners_authors"), pt.translate("cif.access.users"),
                    pt.translate("cif.access.users_guests"), };
            // use the addDropDownSingleselect method with null as label i18n - key, because there is no label to set. OLAT-3682
            accessSelbox = uifactory.addDropdownSingleselect("accessBox", null, fic, keys, values, null);
            accessSelbox.select(selectedAccess, true);

        }

    }

}
