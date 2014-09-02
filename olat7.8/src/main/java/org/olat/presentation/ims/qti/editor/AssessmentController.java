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

package org.olat.presentation.ims.qti.editor;

import org.olat.lms.ims.qti.editor.QTIEditHelperEBL;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.objects.Assessment;
import org.olat.lms.ims.qti.objects.Control;
import org.olat.lms.ims.qti.objects.Duration;
import org.olat.lms.ims.qti.objects.OutcomesProcessing;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableDefaultController;
import org.olat.system.event.Event;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class AssessmentController extends TabbableDefaultController implements ControllerEventListener {

    private VelocityContainer main;

    private final Assessment assessment;
    private final QTIEditorPackageEBL qtiPackage;
    private boolean surveyMode = false;
    private final boolean restrictedEdit;

    /**
     * @param assessment
     * @param qtiPackage
     * @param trnsltr
     * @param wControl
     */
    public AssessmentController(final Assessment assessment, final QTIEditorPackageEBL qtiPackage, final UserRequest ureq, final WindowControl wControl,
            final boolean restrictedEdit) {
        super(ureq, wControl);

        this.restrictedEdit = restrictedEdit;
        this.assessment = assessment;
        this.qtiPackage = qtiPackage;

        main = this.createVelocityContainer("tab_assess");
        main.contextPut("assessment", assessment);
        main.contextPut("mediaBaseURL", qtiPackage.getMediaBaseURL());
        main.contextPut("control", QTIEditHelperEBL.getControl(assessment));
        main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
        surveyMode = qtiPackage.getQTIDocument().isSurvey();
        main.contextPut("isSurveyMode", surveyMode ? "true" : "false");

        if (!surveyMode && !restrictedEdit) {
            if (assessment.getDuration() != null) {
                main.contextPut("duration", assessment.getDuration());
            }
        }

        // Adding outcomes processing parameters
        final OutcomesProcessing outcomesProcessing = assessment.getOutcomes_processing();
        if (outcomesProcessing == null) {
            main.contextPut(OutcomesProcessing.CUTVALUE, "0.0");
        } else {
            main.contextPut(OutcomesProcessing.CUTVALUE, outcomesProcessing.getField(OutcomesProcessing.CUTVALUE));
        }
        this.putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == main) {
            if (event.getCommand().equals("sao")) { // asessment options submitted
                // Handle all data that is useless in survey mode
                final String newTitle = ureq.getParameter("title");
                final String oldTitle = assessment.getTitle();
                final boolean hasTitleChange = newTitle != null && !newTitle.equals(oldTitle);
                final String newObjectives = ureq.getParameter("objectives");
                final String oldObjectives = assessment.getObjectives();
                final boolean hasObjectivesChange = newObjectives != null && !newObjectives.equals(oldObjectives);
                final NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
                if (hasTitleChange) {
                    nce.setNewTitle(newTitle);
                }
                if (hasObjectivesChange) {
                    nce.setNewObjectives(newObjectives);
                }
                if (hasTitleChange || hasObjectivesChange) {
                    // create a memento first
                    fireEvent(ureq, nce);
                    // then apply changes
                    assessment.setTitle(newTitle);
                    assessment.setObjectives(newObjectives);
                }
                //
                if (!surveyMode && !restrictedEdit) {
                    final Control tmpControl = QTIEditHelperEBL.getControl(assessment);
                    final boolean oldInheritControls = assessment.isInheritControls();
                    final boolean newInheritControls = ureq.getParameter("inheritswitch").equals("Yes");
                    assessment.setInheritControls(newInheritControls);

                    final String feedbackswitchTmp = ureq.getParameter("feedbackswitch");
                    final String hintswitchTmp = ureq.getParameter("hintswitch");
                    final String solutionswitchTmp = ureq.getParameter("solutionswitch");
                    tmpControl.setSwitches(feedbackswitchTmp, hintswitchTmp, solutionswitchTmp);
                    if (tmpControl.getHint() != Control.CTRL_UNDEF || tmpControl.getHint() != Control.CTRL_UNDEF || tmpControl.getSolution() != Control.CTRL_UNDEF) {
                        assessment.setInheritControls(true);
                    }

                    if (oldInheritControls && !newInheritControls) {
                        tmpControl.setSwitches(Control.CTRL_UNDEF, Control.CTRL_UNDEF, Control.CTRL_UNDEF);
                        assessment.setInheritControls(false);
                    }

                    OutcomesProcessing outcomesProcessing = assessment.getOutcomes_processing();
                    if (outcomesProcessing == null) {
                        // Create outcomes processing object if it doesn't already exist.
                        // Happens
                        // when creating a new assessment
                        outcomesProcessing = new OutcomesProcessing();
                        assessment.setOutcomes_processing(outcomesProcessing);
                    }
                    String cutval = ureq.getParameter(OutcomesProcessing.CUTVALUE);
                    try {
                        Float.parseFloat(cutval);
                    } catch (final NumberFormatException nfe) {
                        cutval = "0.0";
                        this.showWarning("error.cutval");
                    }
                    outcomesProcessing.setField(OutcomesProcessing.CUTVALUE, cutval);
                    main.contextPut(OutcomesProcessing.CUTVALUE, cutval);

                    if (ureq.getParameter("duration").equals("Yes")) {
                        String durationMin = ureq.getParameter("duration_min");
                        String durationSec = ureq.getParameter("duration_sec");
                        try {
                            Integer.parseInt(durationMin);
                            final int sec = Integer.parseInt(durationSec);
                            if (sec > 60) {
                                throw new NumberFormatException();
                            }
                        } catch (final NumberFormatException nfe) {
                            durationMin = "0";
                            durationSec = "0";
                            this.showWarning("error.duration");
                        }
                        final Duration d = new Duration(durationMin, durationSec);
                        assessment.setDuration(d);
                        main.contextPut("duration", assessment.getDuration());
                    } else {
                        assessment.setDuration(null);
                        main.contextRemove("duration");
                    }
                }
                qtiPackage.serializeQTIDocument();

                // refresh for removing dirty marking of button even if nothing changed
                main.setDirty(true);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        main = null;
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        tabbedPane.addTab(translate(surveyMode ? "tab.survey" : "tab.assessment"), main);
    }

}
