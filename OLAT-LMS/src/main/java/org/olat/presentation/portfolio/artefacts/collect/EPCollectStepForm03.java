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
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.portfolio.artefacts.run.EPArtefactViewController;
import org.olat.system.commons.StringHelper;

/**
 * Description:<br>
 * controller for entering a reflexion about the choosen artefact can be used inside the collect-wizzard or standalone (depending on used constructor)
 * <P>
 * Initial Date: 28.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm03 extends StepFormBasicController {

    private static final Integer REFLEXION_MAX_SIZE = 16383;
    private final AbstractArtefact artefact;
    private RichTextElement reflexionEl;
    private RichTextElement reflexionOrigEl;
    private String reflexion;
    private String artefactReflexion = "";
    private boolean showNoReflexionOnStructLinkYetWarning = false;

    /**
     * preset controller with reflexion of the artefact. used by artefact-pool
     * 
     * @param ureq
     * @param wControl
     * @param artefact
     */
    public EPCollectStepForm03(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        this.artefactReflexion = artefact.getReflexion();
        initForm(this.flc, this, ureq);
    }

    /**
     * no reflexion on link yet, show warning and preset with artefacts-reflexion
     * 
     * @param ureq
     * @param wControl
     * @param artefact
     * @param showHint
     */
    public EPCollectStepForm03(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean showHint) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.showNoReflexionOnStructLinkYetWarning = showHint;
        this.artefact = artefact;
        this.reflexion = artefact.getReflexion();
        initForm(this.flc, this, ureq);
    }

    /**
     * edit an existing reflexion
     * 
     * @param ureq
     * @param wControl
     * @param artefact
     * @param reflexion
     */
    public EPCollectStepForm03(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final String reflexion) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        this.reflexion = reflexion;
        this.artefactReflexion = artefact.getReflexion();

        initForm(this.flc, this, ureq);
    }

    // used while collecting an artefact
    public EPCollectStepForm03(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName, final AbstractArtefact artefact) {
        super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
        // set fallback translator to re-use given strings
        final PackageTranslator pt = new PackageTranslator(EPArtefactViewController.class.getPackage().getName(), ureq.getLocale(), getTranslator());
        this.flc.setTranslator(pt);
        this.artefact = artefact;
        this.artefactReflexion = artefact.getReflexion();
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormDescription("step3.short.descr");
        setFormContextHelp("org.olat.presentation.portfolio.artefacts.collect", "reflexion.html", "step3.chelp.hover");

        if (showNoReflexionOnStructLinkYetWarning) {
            uifactory.addStaticTextElement("hint", "info.no.reflexion.yet", "", formLayout);
        }

        final String text = StringHelper.containsNonWhitespace(reflexion) ? reflexion : artefactReflexion;
        reflexionEl = uifactory.addRichTextElementForStringDataMinimalistic("reflexion", "artefact.reflexion", text, 12, -1, false, formLayout, ureq.getUserSession(),
                getWindowControl());
        reflexionEl.setExtDelay(true);
        reflexionEl.setNotLongerThanCheck(REFLEXION_MAX_SIZE, "reflexion.too.long");
        reflexionEl.setMaxLength(REFLEXION_MAX_SIZE);

        if (!isUsedInStepWizzard()) {
            // add form buttons
            uifactory.addFormSubmitButton("stepform.submit", formLayout);
        }

        if (artefact != null && StringHelper.containsNonWhitespace(artefactReflexion)) {
            uifactory.addSpacerElement("reflexion-in-space", formLayout, false);
            reflexionOrigEl = uifactory.addRichTextElementForStringDataMinimalistic("reflexion_original", "artefact.reflexion.original", artefactReflexion, 12, -1,
                    false, formLayout, ureq.getUserSession(), getWindowControl());
            reflexionOrigEl.setExtDelay(true);
            reflexionOrigEl.setEnabled(false);
        }
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        final String reflexionVal = reflexionEl.getValue();
        if (isUsedInStepWizzard()) {
            artefact.setReflexion(reflexionVal);
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        } else {
            // form is used outside of wizzard:
            // - changing the reflexion of the link "structure <-> artefact", therefore
            // the reflexion has not to be set on the artefact itself, but is transmitted with the event
            // - changing the reflexion of the artefact itself
            fireEvent(ureq, new EPReflexionChangeEvent(reflexionVal, artefact));
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing
    }

}
