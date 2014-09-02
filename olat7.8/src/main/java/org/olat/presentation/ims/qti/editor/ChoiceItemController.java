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

import java.util.List;

import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.objects.ChoiceQuestion;
import org.olat.lms.ims.qti.objects.ChoiceResponse;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.Material;
import org.olat.lms.ims.qti.objects.Mattext;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.lms.ims.qti.objects.Response;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.impl.components.SimpleFormErrorText;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 * @author oliver.buehler@agility-informatik.ch
 */
public class ChoiceItemController extends BasicController implements ControllerEventListener {
    /*
     * Logging, Velocity
     */
    private static final String PACKAGE = PackageUtil.getPackageName(ChoiceItemController.class);
    private static final String VC_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private VelocityContainer main;
    private Translator trnsltr;

    private Item item;
    private final QTIEditorPackageEBL qtiPackage;
    private DialogBoxController delYesNoCtrl;
    private final boolean restrictedEdit;
    private Material editQuestion;
    private Response editResponse;
    private CloseableModalController dialogCtr;
    private MaterialFormController matFormCtr;

    /**
     * @param item
     * @param qtiPackage
     * @param trnsltr
     * @param wControl
     */
    public ChoiceItemController(final Item item, final QTIEditorPackageEBL qtiPackage, final Translator trnsltr, final UserRequest ureq, final WindowControl wControl,
            final boolean restrictedEdit) {
        super(ureq, wControl);

        this.restrictedEdit = restrictedEdit;
        this.item = item;
        this.qtiPackage = qtiPackage;
        this.trnsltr = trnsltr;
        main = new VelocityContainer("scitem", VC_ROOT + "/tab_scItem.html", trnsltr, this);
        main.contextPut("question", item.getQuestion());
        main.contextPut("isSurveyMode", qtiPackage.getQTIDocument().isSurvey() ? "true" : "false");
        main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
        main.contextPut("mediaBaseURL", qtiPackage.getMediaBaseURL());
        if (item.getQuestion().getType() == Question.TYPE_MC) {
            main.setPage(VC_ROOT + "/tab_mcItem.html");
        } else if (item.getQuestion().getType() == Question.TYPE_KPRIM) {
            main.setPage(VC_ROOT + "/tab_kprimItem.html");
        }
        putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == main) {
            // olat::: as: improve easy fix since almost all operations change the main vc.
            main.setDirty(true);
            main.contextRemove("formError");
            final String cmd = event.getCommand();
            final String sPosid = ureq.getParameter("posid");
            int posid = 0;
            if (sPosid != null) {
                posid = Integer.parseInt(sPosid);
            }
            if (cmd.equals("up")) {
                if (posid > 0) {
                    final List<Response> elements = item.getQuestion().getResponses();
                    final Response obj = elements.remove(posid);
                    elements.add(posid - 1, obj);
                }
            } else if (cmd.equals("down")) {
                final List<Response> elements = item.getQuestion().getResponses();
                if (posid < elements.size() - 1) {
                    final Response obj = elements.remove(posid);
                    elements.add(posid + 1, obj);
                }
            } else if (cmd.equals("editq")) {
                editQuestion = item.getQuestion().getQuestion();
                displayMaterialFormController(ureq, editQuestion, restrictedEdit);

            } else if (cmd.equals("editr")) {
                editResponse = item.getQuestion().getResponses().get(posid);
                final Material responseMat = editResponse.getContent();
                displayMaterialFormController(ureq, responseMat, restrictedEdit);

            } else if (cmd.equals("addchoice")) {
                final ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
                final List<Response> choices = question.getResponses();
                final ChoiceResponse newChoice = new ChoiceResponse();
                newChoice.getContent().add(new Mattext(trnsltr.translate("newresponsetext")));
                newChoice.setCorrect(false);
                newChoice.setPoints(-1f); // default value is negative to make sure
                // people understand the meaning of this value
                choices.add(newChoice);
            } else if (cmd.equals("del")) {
                delYesNoCtrl = activateYesNoDialog(ureq, "", trnsltr.translate("confirm.delete.element"), null);
                delYesNoCtrl.setUserObject(new Integer(posid));

            } else if (cmd.equals("ssc")) { // submit sc
                float score = 0;
                try {
                    score = Float.parseFloat(ureq.getParameter("single_score"));
                    if (score <= 0) {
                        main.contextPut("formError", true);
                        main.put("formErrorText", new SimpleFormErrorText("questionform.validation.ssc.score", translate("questionform.validation.ssc.score")));
                        return;
                    }
                } catch (NumberFormatException ex) {
                    main.contextPut("formError", true);
                    main.put("formErrorText", new SimpleFormErrorText("questionform.validation.score.nan", translate("questionform.validation.score.nan")));
                    return;
                }

                final ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
                final List<Response> q_choices = question.getResponses();

                boolean correctChoiceDefined = false;
                final String correctChoice = ureq.getParameter("correctChoice");
                for (int i = 0; i < q_choices.size(); i++) {
                    final Response choice = q_choices.get(i);
                    if (correctChoice != null && correctChoice.equals("value_q" + i)) {
                        choice.setCorrect(true);
                        choice.setPoints(score);
                        correctChoiceDefined = true;
                    } else {
                        choice.setCorrect(false);
                    }
                }
                if (!correctChoiceDefined) {
                    showWarning("questionform.validation.ssc.selection");
                    return;
                }
                question.setSingleCorrectScore(score);
                question.setMaxValue(score);

            } else if (cmd.equals("smc")) { // submit mc
                final ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
                question.setSingleCorrect(ureq.getParameter("valuation_method").equals("single"));
                if (question.isSingleCorrect()) {
                    float score = 0;
                    try {
                        score = Float.parseFloat(ureq.getParameter("single_score"));
                        if (score <= 0) {
                            main.contextPut("formError", true);
                            main.put("formErrorText", new SimpleFormErrorText("questionform.validation.smc.score", translate("questionform.validation.smc.score")));
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        main.contextPut("formError", true);
                        main.put("formErrorText", new SimpleFormErrorText("questionform.validation.score.nan", translate("questionform.validation.score.nan")));
                        return;
                    }
                    question.setSingleCorrectScore(score);
                }

                final List<Response> choices = question.getResponses();
                boolean hasZeroPointChoice = false;
                for (int i = 0; i < choices.size(); i++) {
                    final Response choice = choices.get(i);
                    if (ureq.getParameter("value_q" + i) != null && ureq.getParameter("value_q" + i).equalsIgnoreCase("true")) {
                        choice.setCorrect(true);
                    } else {
                        choice.setCorrect(false);
                    }
                    choice.setPoints(ureq.getParameter("points_q" + i));
                    if (choice.getPoints() == 0) {
                        hasZeroPointChoice = true;
                    }
                }
                if (hasZeroPointChoice && !question.isSingleCorrect()) {
                    getWindowControl().setInfo(trnsltr.translate("editor.info.mc.zero.points"));
                }

                // set min/max before single_correct score
                // will be corrected by single_correct score afterwards
                question.setMinValue(ureq.getParameter("min_value"));
                question.setMaxValue(ureq.getParameter("max_value"));

            } else if (cmd.equals("skprim")) { // submit kprim
                float maxValue = 0;
                try {
                    maxValue = Float.parseFloat(ureq.getParameter("max_value"));
                } catch (final NumberFormatException e) {
                    // invalid input, set maxValue 0
                }
                final ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
                final List<Response> q_choices = question.getResponses();
                for (int i = 0; i < q_choices.size(); i++) {
                    final String correctChoice = ureq.getParameter("correctChoice_q" + i);
                    final ChoiceResponse choice = (ChoiceResponse) q_choices.get(i);
                    choice.setPoints(maxValue / 4);
                    if ("correct".equals(correctChoice)) {
                        choice.setCorrect(true);
                    } else {
                        choice.setCorrect(false);
                    }
                }
                question.setMaxValue(maxValue);
            }
            qtiPackage.serializeQTIDocument();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller controller, final Event event) {
        if (controller == matFormCtr) {
            if (event instanceof QTIObjectBeforeChangeEvent) {
                final QTIObjectBeforeChangeEvent qobce = (QTIObjectBeforeChangeEvent) event;
                final NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
                if (editQuestion != null) {
                    nce.setNewQuestionMaterial(qobce.getContent());
                    nce.setItemIdent(item.getIdent());
                    nce.setQuestionIdent(editQuestion.getId());
                    nce.setMatIdent(qobce.getId());
                    fireEvent(ureq, nce);
                } else if (editResponse != null) {
                    nce.setNewResponseMaterial(qobce.getContent());
                    nce.setItemIdent(item.getIdent());
                    nce.setResponseIdent(editResponse.getIdent());
                    nce.setMatIdent(qobce.getId());
                    fireEvent(ureq, nce);
                }
            } else if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                if (event == Event.DONE_EVENT) {
                    // serialize document
                    qtiPackage.serializeQTIDocument();
                    // force rerendering of view
                    main.setDirty(true);
                    editQuestion = null;
                    editResponse = null;
                }
                // dispose controllers
                dialogCtr.deactivate();
                dialogCtr.dispose();
                dialogCtr = null;
                matFormCtr.dispose();
                matFormCtr = null;
            }
        } else if (controller == dialogCtr) {
            if (event == Event.CANCELLED_EVENT) {
                dialogCtr.dispose();
                dialogCtr = null;
                matFormCtr.dispose();
                matFormCtr = null;
            }
        } else if (controller == delYesNoCtrl) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                item.getQuestion().getResponses().remove(((Integer) delYesNoCtrl.getUserObject()).intValue());
                main.setDirty(true);// repaint
            }
        }
    }

    /**
     * Displays the MaterialFormController in a closable box.
     * 
     * @param ureq
     * @param mat
     * @param isRestrictedEditMode
     */
    private void displayMaterialFormController(final UserRequest ureq, final Material mat, final boolean isRestrictedEditMode) {
        matFormCtr = new MaterialFormController(ureq, getWindowControl(), mat, qtiPackage, isRestrictedEditMode);
        matFormCtr.addControllerListener(this);
        dialogCtr = new CloseableModalController(getWindowControl(), "close", matFormCtr.getInitialComponent());
        matFormCtr.addControllerListener(dialogCtr);
        dialogCtr.activate();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        main = null;
        item = null;
        trnsltr = null;
        if (dialogCtr != null) {
            dialogCtr.dispose();
            dialogCtr = null;
        }
        if (matFormCtr != null) {
            matFormCtr.dispose();
            matFormCtr = null;
        }
    }

}
