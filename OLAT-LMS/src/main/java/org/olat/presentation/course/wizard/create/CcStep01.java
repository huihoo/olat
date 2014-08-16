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
 * Technische Universitaet Chemnitz Lehrstuhl Technische Informatik Author Marcel Karras (toka@freebits.de) Author Norbert Englisch
 * (norbert.englisch@informatik.tu-chemnitz.de) Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */

package org.olat.presentation.course.wizard.create;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.wizard.create.CourseCreationConfiguration;
import org.olat.lms.course.wizard.create.CourseCreationHelper;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.BasicStep;
import org.olat.presentation.framework.core.control.generic.wizard.PrevNextFinishConfig;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Second step of course creation wizard: <br/>
 * - choose catalog path
 * <P>
 * 
 * @author Marcel Karras (toka@freebits.de)
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 * @author skoeber
 */
class CcStep01 extends BasicStep {

    private final PrevNextFinishConfig prevNextConfig;
    private final RepositoryEntry repoEntry;
    private final CourseCreationConfiguration courseConfig;

    /**
     * Second step of the course creation wizard
     * 
     * @param ureq
     * @param courseConfig
     * @param repoEntry
     */
    public CcStep01(final UserRequest ureq, final CourseCreationConfiguration courseConfig, final RepositoryEntry repoEntry) {
        super(ureq);
        this.repoEntry = repoEntry;
        this.courseConfig = courseConfig;

        setI18nTitleAndDescr("coursecreation.catalog.title", "coursecreation.catalog.description");
        setNextStep(new CcStep02(ureq, courseConfig));
        prevNextConfig = PrevNextFinishConfig.BACK_NEXT;
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return prevNextConfig;
    }

    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepP = new CcStep01Form(ureq, windowControl, form, stepsRunContext, null);
        return stepP;
    }

    public CourseCreationConfiguration getCourseConfig() {
        return courseConfig;
    }

    public RepositoryEntry getRepoEntry() {
        return repoEntry;
    }

    class CcStep01Form extends StepFormBasicController {

        private final Translator translator;
        private FormLayoutContainer fic;
        private CatalogInsertController cic;

        public CcStep01Form(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext, final String customLayoutPageName) {
            super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, customLayoutPageName);
            translator = PackageUtil.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale());
            super.setTranslator(translator);
            initForm(ureq);
        }

        @Override
        protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
            super.formInnerEvent(ureq, source, event);
            if (source.getName().equals(Event.BACK_EVENT.getCommand())) {
                cic.init();
            } else {
                // save item
                finishWorkflowItem(ureq);
            }
        }

        @Override
        protected void doDispose() {
            // nothing to do here
        }

        @Override
        protected boolean validateFormLogic(final UserRequest ureq) {
            return true;
        }

        @Override
        protected void formOK(final UserRequest ureq) {
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            fic = FormLayoutContainer.createCustomFormLayout("cc01", this.getTranslator(), this.velocity_root + "/CcStep01_form.html");
            formLayout.add(fic);

            // load course
            final ICourse course = CourseFactory.loadCourse(getRepoEntry().getOlatResource());

            // show catalog selection tree
            if (course != null) {
                cic = new CatalogInsertController(ureq, getWindowControl(), RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, false), getCourseConfig());
                cic.addControllerListener(this);
                fic.put("cc", cic.getInitialComponent());
            }
            cic.init();
        }

        public void finishWorkflowItem(final UserRequest ureq) {
            if (cic != null) {
                // close selection tree and remember selection
                getCourseConfig().setSelectedCatalogEntry(cic.getSelectedParent());
            }
        }

        public boolean isValid(final UserRequest ureq) {
            return validateFormLogic(ureq);
        }
    }
}
