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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.StatusDescriptionHelper;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
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
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * <P>
 * Initial Date: 18.01.2008 <br>
 * 
 * @author patrickb
 */
class PublishStep00 extends BasicStep {

    private final PublishProcess publishProcess;
    private final OLATResourceable ores;

    public PublishStep00(final UserRequest ureq, final CourseEditorTreeModel cetm, final ICourse course) {
        super(ureq);
        this.ores = course;
        publishProcess = PublishProcess.getInstance(course, cetm, ureq.getLocale());
        setI18nTitleAndDescr("publish.header", null);
        // proceed with direct access as next step.
        setNextStep(new PublishStep01(ureq, publishProcess.hasPublishableChanges()));
    }

    /**
	 */
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        // in any case allow next or finish
        if (publishProcess.hasPublishableChanges()) {
            // this means we have possible steps 00a (error messages) and 00b (warning messages)
            return PrevNextFinishConfig.NEXT;
        } else {
            // proceed with direct access as next step.
            return PrevNextFinishConfig.NEXT_FINISH;
        }
    }

    /**
     * org.olat.presentation.framework.control.generic.wizard.StepsRunContext, org.olat.presentation.framework.components.form.flexible.impl.Form)
     */
    @Override
    public StepFormController getStepController(final UserRequest ureq, final WindowControl wControl, final StepsRunContext runContext, final Form form) {

        final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(ores, false);
        /*
         * first step is to show selection tree for selecting prepares all data needed for next step(s)
         */
        runContext.put("publishProcess", publishProcess);
        runContext.put("selectedCourseAccess", String.valueOf(repoEntry.getAccess()));
        return new PublishStep00Form(ureq, wControl, form, publishProcess, runContext);
    }

    /**
     * Description:<br>
     * TODO: patrickb Class Description for PublishStep00Form
     * <P>
     * Initial Date: 18.01.2008 <br>
     * 
     * @author patrickb
     */
    class PublishStep00Form extends StepFormBasicController {

        private final PublishProcess publishManager2;
        private MultipleSelectionElement multiSelectTree;
        private StatusDescription[] sds;
        private FormLayoutContainer fic;
        private StaticTextElement errorElement;
        private FormLink selectAllLink;
        private FormLink uncheckallLink;

        public PublishStep00Form(final UserRequest ureq, final WindowControl wControl, final Form form, final PublishProcess publishManager2,
                final StepsRunContext runContext) {
            super(ureq, wControl, form, runContext, LAYOUT_VERTICAL, null);
            this.publishManager2 = publishManager2;
            initForm(ureq);
        }

        @Override
        protected void doDispose() {
            // nothing to dispose

        }

        @SuppressWarnings("synthetic-access")
        @Override
        protected boolean validateFormLogic(final UserRequest ureq) {
            //
            // create publish set
            // test for errors
            // errors are shown as error text for the form
            //
            boolean createPublishSet = true;
            if (containsRunContextKey("publishSetCreatedFor")) {
                createPublishSet = getFromRunContext("publishSetCreated") != multiSelectTree.getSelectedKeys();
            }
            if (createPublishSet && publishManager2.hasPublishableChanges()) {
                // only add selection if changes were possible
                final List<String> asList = new ArrayList<String>(multiSelectTree.getSelectedKeys());
                publishManager2.createPublishSetFor(asList);
                addToRunContext("publishSetCreatedFor", multiSelectTree.getSelectedKeys());
                //
                sds = publishProcess.testPublishSet(ureq.getLocale());
                //
                final boolean isValid = sds.length == 0;
                if (isValid) {
                    // no error and no warnings -> return immediate
                    return true;
                }
                // sort status -> first are errors, followed by warnings
                sds = StatusDescriptionHelper.sort(sds);

                // assemble warnings and errors as styled text (that is rendering information), <br>
                // this is why the untrusted text must be escaped here!
                String generalErrorTxt = null;
                String errorTxt = getTranslator().translate("publish.notpossible.setincomplete");
                String warningTxt = getTranslator().translate("publish.withwarnings");

                String errors = "<UL>";
                int errCnt = 0;
                String warnings = "<UL>";
                int warCnt = 0;
                for (int i = 0; i < sds.length; i++) {
                    final StatusDescription description = sds[i];
                    final String nodeId = sds[i].getDescriptionForUnit();
                    if (nodeId == null) {
                        // a general error
                        generalErrorTxt = StringHelper.escapeHtml(sds[i].getShortDescription(ureq.getLocale()));
                        break;
                    }
                    final String nodeName = publishProcess.getCourseEditorTreeModel().getCourseNode(nodeId).getShortName();
                    final String isFor = "<b>" + nodeName + "</b><br/>";
                    if (description.isError()) {
                        errors += "<LI>" + isFor + description.getShortDescription(ureq.getLocale()) + "</LI>";
                        errCnt++;
                    } else if (description.isWarning()) {
                        warnings += "<LI>" + isFor + description.getShortDescription(ureq.getLocale()) + "</LI>";
                        warCnt++;
                    }
                }
                warnings += "</UL>";
                errors += "</UL>";
                //
                errorTxt += "<P/>" + errors;
                warningTxt += "<P/>" + warnings;

                if (errCnt > 0) {
                    // if an error found
                    // normally this should already be prevented by offering only correct
                    // tree nodes in the selection tree.

                    return false;
                }

                if (generalErrorTxt != null) {
                    addToRunContext("STEP00.generalErrorText", generalErrorTxt);
                    // TODO: PB: errorElement.setErrorComponent doesn't work, used setValue as workaround
                    /*
                     * FormItem errorFormItem = uifactory.createSimpleErrorText("errorElement", generalErrorTxt); errorElement.setErrorComponent(errorFormItem, fic);
                     */
                    errorElement.setValue(generalErrorTxt);
                    errorElement.setVisible(true);
                    return false;
                } else if (errCnt > 0) {
                    addToRunContext("STEP00.errorMessage", errorTxt);
                    /*
                     * FormItem errorFormItem = uifactory.createSimpleErrorText("errorElement", errorTxt); errorElement.setErrorComponent(errorFormItem, this.flc);
                     */
                    errorElement.setValue(errorTxt);
                    errorElement.setVisible(true);
                    return false;
                } else /* must be true then if (warCnt > 0) */{
                    addToRunContext("STEP00.warningMessage", warningTxt);
                    return true;
                }
            } else {
                // no new publish set to be calculated
                // check if some error was detected before.
                boolean retVal = !containsRunContextKey("STEP00.generalErrorText");
                retVal = retVal && !containsRunContextKey("STEP00.erroMessage");
                return retVal;
            }
        }

        @Override
        protected void formOK(final UserRequest ureq) {
            addToRunContext("validPublish", Boolean.valueOf(publishManager2.hasPublishableChanges()));
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        @SuppressWarnings("unused")
        protected void formNOK(final UserRequest ureq) {
            addToRunContext("validPublish", Boolean.FALSE);
        }

        @Override
        @SuppressWarnings("unused")
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            if (publishManager2.hasPublishableChanges()) {
                //
                fic = FormLayoutContainer.createCustomFormLayout("publish", getTranslator(), this.velocity_root + "/publish.html");
                formLayout.add(fic);
                errorElement = uifactory.addStaticTextElement("errorElement", null, null, fic);// null > no label, null > no value
                errorElement.setVisible(false);
                // publish treemodel is tree model and INodeFilter at the same time
                multiSelectTree = uifactory.addTreeMultiselect("seltree", null, fic, publishManager2.getPublishTreeModel(), publishManager2.getPublishTreeModel());
                selectAllLink = uifactory.addFormLink("checkall", fic);
                selectAllLink.addActionListener(this, FormEvent.ONCLICK);
                uncheckallLink = uifactory.addFormLink("uncheckall", fic);
                uncheckallLink.addActionListener(this, FormEvent.ONCLICK);
            } else {
                // set message container - telling nothing to publish.
                formLayout.add(FormLayoutContainer.createCustomFormLayout("nothingtopublish", getTranslator(), this.velocity_root + "/nothingtopublish.html"));
            }
        }

        @Override
        @SuppressWarnings("unused")
        protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
            if (source == selectAllLink) {
                multiSelectTree.selectAll();
            }
            if (source == uncheckallLink) {
                multiSelectTree.uncheckAll();
            }
        }

    }// endclass

}// endclass
