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
import org.olat.lms.course.wizard.create.CourseCreationConfiguration;
import org.olat.lms.course.wizard.create.CourseCreationHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
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
 * First step of the course creation wizard: <br/>
 * - choose course nodes <br/>
 * - do some simple configuration
 * <P>
 * 
 * @author Marcel Karras (toka@freebits.de)
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 * @author skoeber
 */
public class CcStep00 extends BasicStep {

    CourseCreationConfiguration courseConfig;

    private final PrevNextFinishConfig prevNextConfig;
    private CloseableModalController cmc;
    private EnrollmentEditForm formEditEnrol;

    /**
     * First step of the course creation wizard
     * 
     * @param ureq
     * @param courseConfig
     * @param repoEntry
     */
    public CcStep00(final UserRequest ureq, final CourseCreationConfiguration courseConfig, final RepositoryEntry repoEntry) {
        super(ureq);

        this.courseConfig = courseConfig;
        setI18nTitleAndDescr("coursecreation.choosecourseelements.title", "coursecreation.choosecourseelements.shortDescription");
        setNextStep(new CcStep01(ureq, courseConfig, repoEntry));
        prevNextConfig = PrevNextFinishConfig.NEXT;
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return prevNextConfig;
    }

    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl windowControl, final StepsRunContext stepsRunContext, final Form form) {
        final StepFormController stepP = new CcStep00Form(ureq, windowControl, form, stepsRunContext, null);
        return stepP;
    }

    class CcStep00Form extends StepFormBasicController {

        private final Translator translator;
        private MultipleSelectionElement rightsChooser;
        private FormLayoutContainer fic;
        private FormLink editButtonEnrollment = null;

        public CcStep00Form(final UserRequest ureq, final WindowControl wControl, final Form rootForm, final StepsRunContext runContext, final String customLayoutPageName) {
            super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, customLayoutPageName);
            translator = PackageUtil.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale());
            super.setTranslator(translator);
            // first set the courseConfig to default
            initWorkflowItem();
            // show gui
            initForm(ureq);
        }

        @Override
        protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
            super.formInnerEvent(ureq, source, event);
            // show edit button if enrollment is activated
            if (this.isEnrollmentSelected()) {
                if (editButtonEnrollment != null) {
                    editButtonEnrollment.setVisible(true);
                }
            } else {
                if (editButtonEnrollment != null) {
                    editButtonEnrollment.setVisible(false);
                }
            }

            // overlay with configuration of enrollment
            if (source == editButtonEnrollment) {
                this.finishWorkflowItem();
                final VelocityContainer vcEditEnrol = new VelocityContainer("cceedit", velocity_root + "/cceedit.html", getTranslator(), null);
                formEditEnrol = new EnrollmentEditForm(ureq, getWindowControl(), courseConfig);
                listenTo(formEditEnrol);
                vcEditEnrol.put("formEditEnrol", formEditEnrol.getInitialComponent());
                cmc = new CloseableModalController(getWindowControl(), "close", vcEditEnrol);
                cmc.activate();
            }
        }

        @Override
        protected void event(final UserRequest ureq, final Controller source, final Event event) {
            if (source == formEditEnrol) {
                cmc.deactivate();
            }
        }

        @Override
        protected void doDispose() {
            // nothing to do here
        }

        @Override
        protected void formOK(final UserRequest ureq) {
            finishWorkflowItem();
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

            fic = FormLayoutContainer.createCustomFormLayout("cc00", this.getTranslator(), this.velocity_root + "/CcStep00_form.html");
            formLayout.add(fic);

            // prepare checkboxes
            final String[] keys = new String[] { "sp", "en", "bc", "fo", "co" };
            final String[] values = new String[] { translator.translate("cce.informationpage"), translator.translate("cce.enrollment"),
                    translator.translate("cce.downloadfolder"), translator.translate("cce.forum"), translator.translate("cce.contactform") };

            // CSS for thumbs
            final String[] cssClasses = new String[] { "cc_sp", "en_sp", "bc_sp", "fo_sp", "co_sp", };
            // show checkbox
            rightsChooser = FormUIFactory.getInstance().addCheckboxesVertical("rightsChooser", fic, keys, values, cssClasses, 1);
            rightsChooser.addActionListener(this, FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753
            // create edit button for enrollment and hide it
            editButtonEnrollment = FormUIFactory.getInstance().addFormLink("cce.edit", fic);
            editButtonEnrollment.addActionListener(this, FormEvent.ONCLICK);
            editButtonEnrollment.setVisible(false);
        }

        public void finishWorkflowItem() {
            // update course config
            courseConfig.setCreateSinglePage(isSinglePageSelected());
            courseConfig.setCreateDownloadFolder(isDownloadSelected());
            courseConfig.setCreateEnrollment(isEnrollmentSelected());
            courseConfig.setCreateForum(isForumSelected());
            courseConfig.setCreateContactForm(isContactSelected());
        }

        public void initWorkflowItem() {
            // reset course config
            courseConfig.setCreateSinglePage(false);
            courseConfig.setCreateDownloadFolder(false);
            courseConfig.setCreateEnrollment(false);
            courseConfig.setCreateForum(false);
            courseConfig.setCreateContactForm(false);
        }

        private final boolean isSinglePageSelected() {
            return rightsChooser.getSelectedKeys().contains("sp");
        }

        private final boolean isEnrollmentSelected() {
            return rightsChooser.getSelectedKeys().contains("en");
        }

        private final boolean isDownloadSelected() {
            return rightsChooser.getSelectedKeys().contains("bc");
        }

        private final boolean isForumSelected() {
            return rightsChooser.getSelectedKeys().contains("fo");
        }

        private final boolean isContactSelected() {
            return rightsChooser.getSelectedKeys().contains("co");
        }
    }
}
