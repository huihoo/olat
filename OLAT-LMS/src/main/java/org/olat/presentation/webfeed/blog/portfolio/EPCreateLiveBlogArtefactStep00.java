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
package org.olat.presentation.webfeed.blog.portfolio;

import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.portfolio.artefacts.collect.EPCollectStep00;

/**
 * Description:<br>
 * Create a live blog
 * <P>
 * Initial Date: 8 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPCreateLiveBlogArtefactStep00 extends EPCollectStep00 {

    private final AbstractArtefact artefact;

    public EPCreateLiveBlogArtefactStep00(final UserRequest ureq, final AbstractArtefact artefact) {
        super(ureq, artefact);
        this.artefact = artefact;
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepI = new EPCreateLiveBlogArtefactStepForm00(ureq, windowControl, form, stepsRunContext, FormBasicController.LAYOUT_DEFAULT, null,
                artefact);
        return stepI;
    }
}
