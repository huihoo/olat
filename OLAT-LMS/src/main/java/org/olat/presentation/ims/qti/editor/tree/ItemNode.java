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

package org.olat.presentation.ims.qti.editor.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.lms.ims.qti.editor.QTIEditHelperEBL;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.objects.Control;
import org.olat.lms.ims.qti.objects.EssayResponse;
import org.olat.lms.ims.qti.objects.FIBResponse;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.Material;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.lms.ims.qti.objects.Response;
import org.olat.presentation.commons.memento.Memento;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.ims.qti.editor.ItemNodeTabbedFormController;
import org.olat.presentation.ims.qti.editor.QTIEditorMainController;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: Nov 21, 2004 <br>
 * 
 * @author patrick
 */
public class ItemNode extends GenericQtiNode {

    private final Item item;
    private final QTIEditorPackageEBL qtiPackage;
    private TabbedPane myTabbedPane;
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * @param theItem
     * @param qtiPackage
     */
    public ItemNode(final Item theItem, final QTIEditorPackageEBL qtiPackage) {
        item = theItem;
        this.qtiPackage = qtiPackage;
        setMenuTitleAndAlt(item.getTitle());
        setUserObject(item.getIdent());
        if (item.isAlient()) {
            setIconCssClass("o_mi_qtialientitem");
        } else {
            final int questionType = item.getQuestion().getType();
            switch (questionType) {
            case Question.TYPE_SC:
                setIconCssClass("o_mi_qtisc");
                break;
            case Question.TYPE_MC:
                setIconCssClass("o_mi_qtimc");
                break;
            case Question.TYPE_KPRIM:
                setIconCssClass("o_mi_qtikprim");
                break;
            case Question.TYPE_FIB:
                setIconCssClass("o_mi_qtifib");
                break;
            case Question.TYPE_ESSAY:
                setIconCssClass("o_mi_qtiessay");
                break;
            }
        }
    }

    /**
     * Set's the node's title and alt text (truncates title)
     * 
     * @param title
     */
    @Override
    public void setMenuTitleAndAlt(final String title) {
        super.setMenuTitleAndAlt(title);
        item.setTitle(title);
    }

    /**
	 */
    @Override
    public Controller createRunController(final UserRequest ureq, final WindowControl wControl) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * org.olat.presentation.framework.translator.Translator, QTIEditorMainController)
     */
    @Override
    public TabbedPane createEditTabbedPane(final UserRequest ureq, final WindowControl wControl, final Translator trnsltr,
            final QTIEditorMainController editorMainController) {
        if (myTabbedPane == null) {
            try {
                myTabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
                final TabbableController tabbCntrllr = new ItemNodeTabbedFormController(item, qtiPackage, ureq, wControl, trnsltr,
                        editorMainController.isRestrictedEdit());
                tabbCntrllr.addTabs(myTabbedPane);
                tabbCntrllr.addControllerListener(editorMainController);
            } catch (final Exception e) {
                myTabbedPane = null;
                log.warn("Cannot create editor for the current item - item.getIdent(): " + item.getIdent());
            }
        }
        return myTabbedPane;
    }

    /**
	 */
    @Override
    public void insertQTIObjectAt(final QTIObject object, final int position) {
        throw new AssertException("Can't insert objects on ItemNode.");
    }

    /**
	 */
    @Override
    public QTIObject removeQTIObjectAt(final int position) {
        throw new AssertException("Can't remove objects on ItemNode.");
    }

    /**
	 */
    @Override
    public QTIObject getQTIObjectAt(final int position) {
        throw new AssertException("Can't get objects from ItemNode.");
    }

    /**
	 */
    @Override
    public QTIObject getUnderlyingQTIObject() {
        return item;
    }

    @Override
    public Memento createMemento() {
        final Question question = item.getQuestion();
        // special case switches as question types are encoded into integers!!
        final boolean isFIB = question.getType() == Question.TYPE_FIB;
        final boolean isESSAY = question.getType() == Question.TYPE_ESSAY;

        // Item metadata
        final QtiNodeMemento qnm = new QtiNodeMemento();
        final Map qtiState = new HashMap();
        qtiState.put("ID", item.getIdent());
        qtiState.put("TITLE", item.getTitle());
        qtiState.put("OBJECTIVES", item.getObjectives());
        // question and responses
        qtiState.put("QUESTION.ID", question.getIdent());
        qtiState.put("QUESTION.HINTTEXT", question.getHintText());
        final Material questMaterial = question.getQuestion();
        qtiState.put("QUESTION.MATERIAL.ASTEXT", questMaterial.renderAsText());
        final List ids = new ArrayList();
        final List asTexts = new ArrayList();
        final List feedbacks = new ArrayList();
        final List responses = question.getResponses();
        for (final Iterator iter = responses.iterator(); iter.hasNext();) {
            final Response resp = (Response) iter.next();
            if (isFIB) {
                if (FIBResponse.TYPE_BLANK.equals(((FIBResponse) resp).getType())) {
                    asTexts.add(formatFIBResponseAsText((FIBResponse) resp));
                    ids.add(resp.getIdent());
                    feedbacks.add(QTIEditHelperEBL.getFeedbackOlatRespText(item, resp.getIdent()));
                }
            } else if (isESSAY) {
                asTexts.add(formatESSAYResponseAsText((EssayResponse) resp));
                ids.add(resp.getIdent());
                feedbacks.add(QTIEditHelperEBL.getFeedbackOlatRespText(item, resp.getIdent()));
            } else {
                // not a FIB or ESSAY response
                asTexts.add(resp.getContent().renderAsText());
                ids.add(resp.getIdent());
                feedbacks.add(QTIEditHelperEBL.getFeedbackOlatRespText(item, resp.getIdent()));
            }
        }
        qtiState.put("QUESTION.RESPONSES.IDS", ids);
        qtiState.put("QUESTION.RESPONSES.ASTEXT", asTexts);
        qtiState.put("QUESTION.RESPONSES.FEEDBACK", feedbacks);
        // feedback
        qtiState.put("FEEDBACK.MASTERY", QTIEditHelperEBL.getFeedbackMasteryText(item));
        qtiState.put("FEEDBACK.FAIL", QTIEditHelperEBL.getFeedbackFailText(item));
        final Control control = QTIEditHelperEBL.getControl(item);
        qtiState.put("FEEDBACK.ENABLED", control.getFeedback() == 1 ? Boolean.TRUE : Boolean.FALSE);
        //
        qnm.setQtiState(qtiState);
        //
        return qnm;
    }

    /**
     * @param resp
     * @return
     */
    private String formatFIBResponseAsText(final FIBResponse fresp) {
        String asText = "Correct inputs:[" + fresp.getCorrectBlank() + "]";
        asText += " Case sensitive:[" + fresp.getCaseSensitive() + "]";
        asText += " Points:[" + fresp.getPoints() + "]";
        asText += " Blank size:[" + fresp.getSize() + "]";
        asText += " Blank Max size:[" + fresp.getMaxLength() + "]";
        return asText;
    }

    private String formatESSAYResponseAsText(final EssayResponse eresp) {
        String asText = "Response field size, columns: [" + eresp.getColumns() + "]";
        asText += " rows: [" + eresp.getRows() + "]";
        asText += " points: [" + eresp.getPoints() + "]";
        return asText;
    }

    @Override
    public void setMemento(final Memento state) {
        throw new UnsupportedOperationException("setting a Memento is not supported yet! \n" + state);
    }

    public String createChangeMessage(final Memento mem) {
        String retVal = null;
        if (mem instanceof QtiNodeMemento) {
            final QtiNodeMemento qnm = (QtiNodeMemento) mem;
            final Map qtiState = qnm.getQtiState();
            //
            final String oldTitle = (String) qtiState.get("TITLE");
            final String newTitle = item.getTitle();
            String titleChange = null;
            //
            final String oldObjectives = (String) qtiState.get("OBJECTIVES");
            final String newObjectives = item.getObjectives();
            String objectChange = null;
            //
            final Question question = item.getQuestion();
            final boolean isFIB = question.getType() == Question.TYPE_FIB;
            final boolean isESSAY = question.getType() == Question.TYPE_ESSAY;
            final String oldHinttext = (String) qtiState.get("QUESTION.HINTTEXT");
            final String newHinttext = question.getHintText();
            String hinttextChange = null;
            //
            final String oldQuestion = (String) qtiState.get("QUESTION.MATERIAL.ASTEXT");
            final String newQuestion = question.getQuestion().renderAsText();
            String questionChange = null;
            // feedback
            String feedbackChanges = "";
            final String oldFeedbackMastery = (String) qtiState.get("FEEDBACK.MASTERY");
            final String newFeedbackMastery = QTIEditHelperEBL.getFeedbackMasteryText(item);
            final String oldFeedbackFail = (String) qtiState.get("FEEDBACK.FAIL");
            final String newFeedbackFail = QTIEditHelperEBL.getFeedbackFailText(item);
            final Control control = QTIEditHelperEBL.getControl(item);
            final Boolean oldHasFeedback = (Boolean) qtiState.get("FEEDBACK.ENABLED");
            final Boolean newHasFeedback = control != null ? new Boolean(control.getFeedback() == 1) : null;
            //
            final List asTexts = (List) qtiState.get("QUESTION.RESPONSES.ASTEXT");
            final List feedbacks = (List) qtiState.get("QUESTION.RESPONSES.FEEDBACK");
            String oldResp = null;
            String newResp = null;
            String oldFeedback = null;
            String newFeedback = null;
            String responsesChanges = "";
            final List responses = question.getResponses();
            int i = 0;
            boolean nothingToDo = false;
            for (final Iterator iter = responses.iterator(); iter.hasNext();) {
                nothingToDo = false;
                final Response resp = (Response) iter.next();
                if (isFIB) {
                    if (FIBResponse.TYPE_BLANK.equals(((FIBResponse) resp).getType())) {
                        newResp = formatFIBResponseAsText((FIBResponse) resp);
                    } else {
                        // skip
                        nothingToDo = true;
                    }
                } else if (isESSAY) {
                    newResp = formatESSAYResponseAsText((EssayResponse) resp);
                } else {
                    newResp = resp.getContent().renderAsText();
                }
                // if NOT nothingToDO
                if (!nothingToDo) {
                    oldResp = (String) asTexts.get(i);
                    if ((oldResp != null && !oldResp.equals(newResp)) || (newResp != null && !newResp.equals(oldResp))) {
                        if (isFIB) {
                            responsesChanges += "\nBlank changed:";
                            responsesChanges += "\nold blank: \n\t" + formatVariable(oldResp) + "\n\nnew blank: \n\t" + formatVariable(newResp);
                        } else {
                            responsesChanges += "\nResponse changed:";
                            responsesChanges += "\nold response: \n\t" + formatVariable(oldResp) + "\n\nnew response: \n\t" + formatVariable(newResp);
                        }
                    }
                    // feedback to response changed?
                    newFeedback = QTIEditHelperEBL.getFeedbackOlatRespText(item, resp.getIdent());
                    oldFeedback = (String) feedbacks.get(i);
                    if ((oldFeedback != null && !oldFeedback.equals(newFeedback)) || (newFeedback != null && !newFeedback.equals(oldFeedback))) {
                        feedbackChanges += "\nFeedback changed:";
                        feedbackChanges += "\nold feedback: \n\t" + formatVariable(oldFeedback) + "\n\nnew feedback: \n\t" + formatVariable(newFeedback);
                    }
                    i++;
                }
            }
            //
            retVal = "\n---+++ Item changes [" + oldTitle + "]:";
            if ((oldTitle != null && !oldTitle.equals(newTitle)) || (newTitle != null && !newTitle.equals(oldTitle))) {
                titleChange = "\n\nold title: \n\t" + formatVariable(oldTitle) + "\n\nnew title: \n\t" + formatVariable(newTitle);
            }
            if ((oldObjectives != null && !oldObjectives.equals(newObjectives)) || (newObjectives != null && !newObjectives.equals(oldObjectives))) {
                objectChange = "\n\nold objectives: \n\t" + formatVariable(oldObjectives) + "\n\nnew objectives: \n\t" + formatVariable(newObjectives);
            }
            if (titleChange != null || objectChange != null) {
                retVal += "\nMetadata changed:";
                if (titleChange != null) {
                    retVal += titleChange;
                }
                if (objectChange != null) {
                    retVal += objectChange;
                }
            }
            //
            if ((oldHinttext != null && !oldHinttext.equals(newHinttext)) || (newHinttext != null && !newHinttext.equals(oldHinttext))) {
                hinttextChange = "\n---+++ old hinttext: \n\t" + formatVariable(oldHinttext) + "\n\nnew hinttext: \n\t" + formatVariable(newHinttext);
                retVal += hinttextChange;
            }
            if ((oldQuestion != null && !oldQuestion.equals(newQuestion)) || (newQuestion != null && !newQuestion.equals(oldQuestion))) {
                questionChange = "\n---+++ old question: \n\t" + formatVariable(oldQuestion) + "\n\nnew question: \n\t" + formatVariable(newQuestion);
                retVal += questionChange;
            }
            if (!responsesChanges.equals("")) {
                retVal += responsesChanges;
            }
            if ((oldFeedbackMastery != null && !oldFeedbackMastery.equals(newFeedbackMastery))
                    || (newFeedbackMastery != null && !newFeedbackMastery.equals(oldFeedbackMastery))) {
                final String tmp = "\n---+++ old master feedback: \n\t" + formatVariable(oldFeedbackMastery) + "\n\nnew master feedback: \n\t"
                        + formatVariable(newFeedbackMastery);
                feedbackChanges = tmp + feedbackChanges;
            }
            if ((oldFeedbackFail != null && !oldFeedbackFail.equals(newFeedbackFail)) || (newFeedbackFail != null && !newFeedbackFail.equals(oldFeedbackFail))) {
                final String tmp = "\n---+++ old fail feedback: \n\t" + formatVariable(oldFeedbackFail) + "\n\nnew fail feedback: \n\t" + formatVariable(newFeedbackFail);
                feedbackChanges = tmp + feedbackChanges;
            }
            if ((oldHasFeedback != null && newHasFeedback != null && oldHasFeedback != newHasFeedback)) {
                final String oldF = oldHasFeedback.booleanValue() ? "enabled" : "disabled";
                final String newF = newHasFeedback.booleanValue() ? "enabled" : "disabled";
                feedbackChanges = "\n---+++ feedback was : \n\t" + oldF + "\n\n feedback is now: \n\t" + newF + feedbackChanges;
            }
            if (!feedbackChanges.equals("")) {
                retVal += feedbackChanges;
            }
            return retVal;
        }
        return "undefined";
    }

}
