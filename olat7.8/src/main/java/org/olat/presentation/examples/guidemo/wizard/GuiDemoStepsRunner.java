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
package org.olat.presentation.examples.guidemo.wizard;

import org.olat.presentation.examples.guidemo.GuiDemoFlexiForm;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepRunnerCallback;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsMainRunController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for GuiDemoStepsRunner
 * <P>
 * Initial Date: 10.01.2008 <br>
 * 
 * @author patrickb
 */
public class GuiDemoStepsRunner extends BasicController {
    private final VelocityContainer mainVC;
    private final Link startLink;
    private StepsMainRunController smrc;

    public GuiDemoStepsRunner(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        mainVC = createVelocityContainer("stepsrunnerindex");
        startLink = LinkFactory.createButton("start", mainVC, this);

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, control, this.getClass(), mainVC);
        mainVC.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(mainVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == smrc) {
            if (event == Event.CANCELLED_EVENT) {
                getWindowControl().pop();
                removeAsListenerAndDispose(smrc);
                showInfo("cancel");
            } else if (event == Event.DONE_EVENT) {
                getWindowControl().pop();
                removeAsListenerAndDispose(smrc);
                showInfo("ok");
            }
        }
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startLink) {
            /*
             * start step which spawns the whole wizard
             */
            final Step start = new StartStepImpl(ureq);
            /*
             * wizard finish callback called after "finish" is called
             */
            final StepRunnerCallback finishCallback = new StepRunnerCallback() {
                @Override
                public Step execute(final UserRequest ureq2, final WindowControl control, final StepsRunContext runContext) {
                    // here goes the code which reads out the wizards data from the
                    // runContext and then does some wizardry
                    //
                    // after successfully finishing -> send a NOSTEP to indicate proper
                    // finishing
                    return Step.NOSTEP;
                }

            };
            smrc = new StepsMainRunController(ureq, getWindowControl(), start, finishCallback, null, "A Workflow");
            listenTo(smrc);
            getWindowControl().pushAsModalDialog(smrc.getInitialComponent());

        }
    }

    /**
     * step classes
     */

    private final class StartStepImpl extends BasicStep {

        public StartStepImpl(final UserRequest ureq) {
            super(ureq);
            // set name of step and a short description
            setI18nTitleAndDescr("start", "start.short.desc");
            setNextStep(new StepTwo(ureq));
        }

        @Override
        public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext runContext, final Form form) {
            final StepFormController stepP = new StartStepForm(ureq, windowControl, form, runContext);
            return stepP;
        }

        @Override
        public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
            return new PrevNextFinishConfig(false, true, false);
        }
    }

    private final class StartStepForm extends StepFormBasicController {
        private TextElement firstName;
        private TextElement lastName;
        private TextElement institution;

        StartStepForm(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext) {
            super(ureq, control, rootForm, runContext, LAYOUT_DEFAULT, null);
            setBasePackage(GuiDemoFlexiForm.class);
            flc.setTranslator(getTranslator());
            initForm(ureq);
        }

        @Override
        @SuppressWarnings("unused")
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            setFormTitle("guidemo_flexi_form_simpleform");
            final int defaultDisplaySize = 32;
            //
            firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, "Patrick", formLayout);
            firstName.setDisplaySize(defaultDisplaySize);
            firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
            firstName.setMandatory(true);

            lastName = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, "Brunner", formLayout);
            lastName.setDisplaySize(defaultDisplaySize);
            lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
            lastName.setMandatory(true);

            institution = uifactory.addTextElement("institution", "guidemo.flexi.form.institution", 256, "insti", formLayout);
            institution.setDisplaySize(defaultDisplaySize);

        }

        @Override
        protected void formOK(final UserRequest ureq) {
            // form has no more errors
            // save info in run context for next step.
            addToRunContext("firstname", firstName.getValue());
            addToRunContext("lastname", lastName.getValue());
            addToRunContext("institution", institution.getValue());
            // inform surrounding Step runner to proceed
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        protected void doDispose() {
            // TODO Auto-generated method stub

        }
    }

    private final class StepTwo extends BasicStep {

        public StepTwo(final UserRequest ureq) {
            super(ureq);
            setI18nTitleAndDescr("step.two", "step.two.short.desc");
        }

        @Override
        public Step nextStep() {
            // indicate that no next step is possible
            return Step.NOSTEP;
        }

        @Override
        public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext runContext, final Form form) {
            final StepFormController stepP = new StepTwoForm(ureq, windowControl, form, runContext);
            return stepP;
        }

        @Override
        public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
            return new PrevNextFinishConfig(true, false, true);
        }

    }

    private final class StepTwoForm extends StepFormBasicController {

        private TextElement firstName;
        private TextElement lastName;
        private TextElement institution;

        public StepTwoForm(final UserRequest ureq, final WindowControl control, final Form mainForm, final StepsRunContext runContext) {
            super(ureq, control, mainForm, runContext, LAYOUT_DEFAULT, null);
            setBasePackage(GuiDemoFlexiForm.class);
            flc.setTranslator(getTranslator());
            initForm(ureq);
        }

        @Override
        protected void doDispose() {
            // TODO Auto-generated method stub

        }

        @Override
        protected void formOK(final UserRequest ureq) {
            // some code to commit the changes to database
            /*
             * after all, tell that this was last step, and that we are finished
             */
            fireEvent(ureq, StepsEvent.INFORM_FINISHED);
        }

        @Override
        @SuppressWarnings("unused")
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            setFormTitle("another one");
            //
            firstName = uifactory.addTextElement("firstname", null, 256, (String) getFromRunContext("firstname"), formLayout);
            firstName.setEnabled(false);

            lastName = uifactory.addTextElement("lastname", null, 256, (String) getFromRunContext("lastname"), formLayout);
            lastName.setEnabled(false);

            institution = uifactory.addTextElement("institution", null, 256, (String) getFromRunContext("institution"), formLayout);
            institution.setEnabled(false);
        }

    }

}
