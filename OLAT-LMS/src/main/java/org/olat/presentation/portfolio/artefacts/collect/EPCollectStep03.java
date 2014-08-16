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

import java.util.List;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Step to collect reflexion
 * <P>
 * Initial Date: 28.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStep03 extends BasicStep {

    private final AbstractArtefact artefact;
    private boolean hasNextStep;

    public EPCollectStep03(final UserRequest ureq, final AbstractArtefact artefact) {
        super(ureq);
        this.artefact = artefact;
        final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        final List<PortfolioStructure> structs = ePFMgr.getStructureElementsForUser(getIdentity());
        this.hasNextStep = false;
        if (structs != null && structs.size() != 0) {
            setNextStep(new EPCollectStep04(ureq, artefact));
            hasNextStep = true;
        } else {
            setNextStep(NOSTEP);
        }
        setI18nTitleAndDescr("step3.description", "step3.short.descr");
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        if (hasNextStep) {
            return new PrevNextFinishConfig(true, true, true);
        } else {
            return new PrevNextFinishConfig(true, false, true);
        }
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController step = new EPCollectStepForm03(ureq, windowControl, form, stepsRunContext, FormBasicController.LAYOUT_VERTICAL, null, artefact);
        return step;
    }

}
