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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.framework.core.control.generic.wizard;

import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.StaticTextElementImpl;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * TODO: patrickb Class Description for BasicStep
 * <P>
 * Initial Date: 18.01.2008 <br>
 * 
 * @author patrickb
 */
public abstract class BasicStep implements Step {

    private Locale locale;
    private Identity identity;
    private Translator translator;
    private Step nextStep;
    private String i18nStepTitle;
    private String i18nStepDescription;

    public BasicStep(UserRequest ureq) {
        this.locale = ureq.getLocale();
        this.identity = ureq.getIdentity();
        this.translator = PackageUtil.createPackageTranslator(this.getClass(), locale);
        nextStep = Step.NOSTEP;
    }

    @Override
    public abstract PrevNextFinishConfig getInitialPrevNextFinishConfig();

    @Override
    public abstract StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form);

    /**
     * generates a StaticTextElement with i18n key defined, or returns null if i18n key undefined.
     * 
     */
    @Override
    public FormItem getStepShortDescription() {
        if (i18nStepDescription == null) {
            return null;
        }
        return new StaticTextElementImpl(i18nStepDescription, getTranslator().translate(i18nStepDescription));
    }

    /**
     * generates FormLink with defined i18nKey, otherwise override and provide your own FormItem here.
     * 
     */
    @Override
    public FormItem getStepTitle() {
        if (i18nStepTitle == null) {
            throw new AssertException("no i18n key set for step title, or getStepTitle() not overridden.");
        }
        FormLink fl = new FormLinkImpl(i18nStepTitle, i18nStepTitle);
        fl.setTranslator(getTranslator());
        return fl;
    }

    @Override
    public Step nextStep() {
        return nextStep;
    }

    protected Identity getIdentity() {
        return identity;
    }

    protected Translator getTranslator() {
        return translator;
    }

    protected Locale getLocale() {
        return locale;
    }

    protected void setNextStep(Step nextStep) {
        this.nextStep = nextStep;
    }

    protected void setI18nTitleAndDescr(String i18nKeyTitle, String i18nKeyDescription) {
        this.i18nStepTitle = i18nKeyTitle;
        this.i18nStepDescription = i18nKeyDescription;
    }
}
