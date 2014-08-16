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
import org.olat.lms.ims.qti.objects.FIBQuestion;
import org.olat.lms.ims.qti.objects.FIBResponse;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.Material;
import org.olat.lms.ims.qti.objects.Mattext;
import org.olat.lms.ims.qti.objects.Response;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dialog.DialogController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class FIBItemController extends DefaultController implements ControllerEventListener {
    /*
     * Logging, Velocity
     */
    private static final String PACKAGE = PackageUtil.getPackageName(FIBItemController.class);
    private static final String VC_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private VelocityContainer main;
    private Translator trnsltr;

    private Item item;
    private final QTIEditorPackageEBL qtiPackage;
    private boolean surveyMode = false;
    private DialogController delYesNoCtrl;
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
    public FIBItemController(final Item item, final QTIEditorPackageEBL qtiPackage, final Translator trnsltr, final WindowControl wControl, final boolean restrictedEdit) {
        super(wControl);

        this.restrictedEdit = restrictedEdit;
        this.item = item;
        this.qtiPackage = qtiPackage;
        this.trnsltr = trnsltr;
        main = new VelocityContainer("fibitem", VC_ROOT + "/tab_fibItem.html", trnsltr, this);
        main.contextPut("question", item.getQuestion());
        surveyMode = qtiPackage.getQTIDocument().isSurvey();
        main.contextPut("isSurveyMode", surveyMode ? "true" : "false");
        main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
        main.contextPut("mediaBaseURL", qtiPackage.getMediaBaseURL());
        setInitialComponent(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == main) {
            // olat::: as: improve easy fix since almost all operations change the main vc.
            main.setDirty(true);
            final String cmd = event.getCommand();
            final String sPosid = ureq.getParameter("posid");
            int posid = 0;
            if (sPosid != null) {
                posid = Integer.parseInt(sPosid);
            }
            if (cmd.equals("up")) {
                if (posid > 0) {
                    final List elements = item.getQuestion().getResponses();
                    final Object obj = elements.remove(posid);
                    elements.add(posid - 1, obj);
                }
            } else if (cmd.equals("down")) {
                final List elements = item.getQuestion().getResponses();
                if (posid < elements.size() - 1) {
                    final Object obj = elements.remove(posid);
                    elements.add(posid + 1, obj);
                }
            } else if (cmd.equals("editq")) {
                editQuestion = item.getQuestion().getQuestion();
                displayMaterialFormController(ureq, editQuestion, restrictedEdit);

            } else if (cmd.equals("editr")) {
                editResponse = ((Response) item.getQuestion().getResponses().get(posid));
                final Material responseMat = ((Response) item.getQuestion().getResponses().get(posid)).getContent();
                displayMaterialFormController(ureq, responseMat, restrictedEdit);

            } else if (cmd.equals("addtext")) {
                final FIBQuestion fib = (FIBQuestion) item.getQuestion();
                final FIBResponse response = new FIBResponse();
                response.setType(FIBResponse.TYPE_CONTENT);
                final Material mat = new Material();
                mat.add(new Mattext(trnsltr.translate("newtextelement")));
                response.setContent(mat);
                fib.getResponses().add(response);
            } else if (cmd.equals("addblank")) {
                final FIBQuestion fib = (FIBQuestion) item.getQuestion();
                final FIBResponse response = new FIBResponse();
                response.setType(FIBResponse.TYPE_BLANK);
                response.setCorrectBlank("");
                response.setPoints(1f); // default value
                fib.getResponses().add(response);
            } else if (cmd.equals("del")) {
                delYesNoCtrl = DialogController.createYesNoDialogController(ureq.getLocale(), trnsltr.translate("confirm.delete.element"), this, new Integer(posid));
                getWindowControl().pushAsModalDialog(delYesNoCtrl.getInitialComponent());
            } else if (cmd.equals("sfib")) { // submit fib
                final FIBQuestion question = (FIBQuestion) item.getQuestion();
                // Survey specific variables
                if (surveyMode) {
                    final List responses = question.getResponses();
                    for (int i = 0; i < responses.size(); i++) {
                        final FIBResponse response = (FIBResponse) responses.get(i);
                        if (FIBResponse.TYPE_BLANK.equals(response.getType())) {
                            // Set size of input field
                            final String size = ureq.getParameter("size_q" + i);
                            if (size != null) {
                                response.setSizeFromString(size);
                            }
                            final String maxLength = ureq.getParameter("maxl_q" + i);
                            if (maxLength != null) {
                                response.setMaxLengthFromString(maxLength);
                            }
                        }
                    }

                } else {
                    // For all other cases, non-surveys
                    // set min/max values before single_correct !!
                    if (!restrictedEdit) {
                        // only in full edit mode the following fields are available:
                        // min_value, max_value, valuation_method
                        question.setMinValue(ureq.getParameter("min_value"));
                        question.setMaxValue(ureq.getParameter("max_value"));
                        question.setSingleCorrect(ureq.getParameter("valuation_method").equals("single"));
                        if (question.isSingleCorrect()) {
                            question.setSingleCorrectScore(ureq.getParameter("single_score"));
                        } else {
                            question.setSingleCorrectScore(0);
                        }
                    }

                    final NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
                    nce.setItemIdent(item.getIdent());

                    final List responses = question.getResponses();
                    for (int i = 0; i < responses.size(); i++) {
                        final FIBResponse response = (FIBResponse) responses.get(i);
                        nce.setResponseIdent(response.getIdent());
                        fireEvent(ureq, nce);

                        response.setPoints(ureq.getParameter("points_q" + i));
                        if (FIBResponse.TYPE_BLANK.equals(response.getType())) {
                            response.setCorrectBlank(ureq.getParameter("content_q" + i));
                            // Set case sensitiveness
                            String caseSensitive = ureq.getParameter("case_q" + i);
                            if (caseSensitive == null) {
                                caseSensitive = "No";
                            }
                            response.setCaseSensitive(caseSensitive);
                            // Set size of input field
                            final String size = ureq.getParameter("size_q" + i);
                            if (size != null) {
                                response.setSizeFromString(size);
                            }
                            final String maxLength = ureq.getParameter("maxl_q" + i);
                            if (maxLength != null) {
                                response.setMaxLengthFromString(maxLength);
                            }
                            if (response.getCorrectBlank().length() > response.getMaxLength()) {
                                response.setMaxLength(response.getCorrectBlank().length());
                            }
                        }
                    }
                }
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
                // dispose modal dialog controller
                dialogCtr.dispose();
                dialogCtr = null;
                matFormCtr.dispose();
                matFormCtr = null;
            }
        } else if (controller == delYesNoCtrl) {
            getWindowControl().pop();
            if (event == DialogController.EVENT_FIRSTBUTTON) {
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
