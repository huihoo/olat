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
package org.olat.presentation.portfolio.artefacts.collect;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;

/**
 * Description:<br>
 * TODO: rhaag Class Description for EPCollectStep01
 * <P>
 * Initial Date: 27.07.2010 <br>
 * 
 * @author rhaag
 */
public class EPCollectStep01 extends BasicStep {

    private final AbstractArtefact artefact;

    public EPCollectStep01(final UserRequest ureq, final AbstractArtefact artefact) {
        super(ureq);
        this.artefact = artefact;
        setI18nTitleAndDescr("step1.description", "step1.short.descr");
        // signature > 0 means, collection wizzard can be sure its from OLAT, < 0 means get an approval by user (the target value is the negative one)
        if (artefact.getSignature() > 0) {
            setNextStep(new EPCollectStep03(ureq, artefact));
        } else {
            setNextStep(new EPCollectStep02(ureq, artefact));
        }
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return new PrevNextFinishConfig(true, true, false);
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepI = new EPCollectStepForm01(ureq, windowControl, form, stepsRunContext, FormBasicController.LAYOUT_DEFAULT, null, artefact);
        return stepI;
    }

}
