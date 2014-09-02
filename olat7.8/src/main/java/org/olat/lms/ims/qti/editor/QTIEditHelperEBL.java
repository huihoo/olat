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

package org.olat.lms.ims.qti.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.lms.ims.qti.editor.localstrings.QtiEditorLocalStrings;
import org.olat.lms.ims.qti.objects.Assessment;
import org.olat.lms.ims.qti.objects.ChoiceQuestion;
import org.olat.lms.ims.qti.objects.ChoiceResponse;
import org.olat.lms.ims.qti.objects.Control;
import org.olat.lms.ims.qti.objects.EssayQuestion;
import org.olat.lms.ims.qti.objects.EssayResponse;
import org.olat.lms.ims.qti.objects.FIBQuestion;
import org.olat.lms.ims.qti.objects.FIBResponse;
import org.olat.lms.ims.qti.objects.Feedback;
import org.olat.lms.ims.qti.objects.Item;
import org.olat.lms.ims.qti.objects.Material;
import org.olat.lms.ims.qti.objects.Mattext;
import org.olat.lms.ims.qti.objects.Metadata;
import org.olat.lms.ims.qti.objects.QTIDocument;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.lms.ims.qti.objects.Question;
import org.olat.lms.ims.qti.objects.Response;
import org.olat.lms.ims.qti.objects.Section;
import org.olat.lms.ims.qti.parser.ParserManager;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.system.commons.CodeHelper;

/**
 * @author Branislav Balaz
 */
public class QTIEditHelperEBL {

    private static String EDITOR_IDENT = "QTIEDIT";
    private static String ITEM_TYPE_SC = "SCQ";
    private static String ITEM_TYPE_MC = "MCQ";
    private static String ITEM_TYPE_FIB = "FIB";
    private static String ITEM_TYPE_ESSAY = "ESSAY";
    private static String ITEM_TYPE_KPRIM = "KPRIM";

    private static ParserManager parserManager = new ParserManager();

    /**
     * Counts the number of sections in this assessment.
     * 
     * @param assessment
     * @return Number of sections in this assessment.
     */
    public static int countSections(final Assessment assessment) {
        return assessment.getSections().size();
    }

    /**
     * Counts the number of items in this assessment.
     * 
     * @param assessment
     * @return Number of items in this assessment.
     */
    public static int countItems(final Assessment assessment) {
        int itemCount = 0;
        final Iterator sectionIter = assessment.getSections().iterator();
        while (sectionIter.hasNext()) {
            itemCount += ((Section) sectionIter.next()).getItems().size();
        }
        return itemCount;
    }

    /**
     * Creates an empty assessment
     * 
     * @param title
     * @param type
     * @return Assessment
     */
    public static Assessment createAssessment(final String title, final String type) {
        final Assessment assessment = new Assessment();
        assessment.setIdent(CodeHelper.getGlobalForeverUniqueID());
        assessment.setTitle(title);
        final Metadata meta = new Metadata();
        meta.setField(AssessmentInstance.QMD_LABEL_TYPE, type);
        assessment.setMetadata(meta);
        return assessment;
    }

    /**
     * Creates an empty section
     * 
     * @param trans
     * @return Section
     */
    public static Section createSection(QtiEditorLocalStrings qtiEditorLocalStrings) {
        final Section section = new Section();
        section.setIdent(CodeHelper.getGlobalForeverUniqueID());
        section.setTitle(qtiEditorLocalStrings.getNewSection());
        return section;
    }

    /**
     * Creates a new Single Choice item
     * 
     * @param trans
     * @return New Singe Choice item.
     */
    public static Item createSCItem(QtiEditorLocalStrings qtiEditorLocalStrings) {
        final Item newItem = new Item();
        newItem.setIdent(EDITOR_IDENT + ":" + ITEM_TYPE_SC + ":" + String.valueOf(CodeHelper.getRAMUniqueID()));
        newItem.setTitle(qtiEditorLocalStrings.getNewQuestion());
        newItem.setLabel("");
        // conrols
        final Control control = new Control();
        final ArrayList controls = new ArrayList();
        controls.add(control);
        newItem.setItemcontrols(controls);

        // pepare question
        final ChoiceQuestion question = new ChoiceQuestion();
        question.setLable(qtiEditorLocalStrings.getNewQuestion());
        question.getQuestion().getElements().add(new Mattext(qtiEditorLocalStrings.getNewQuestionText()));
        question.setType(Question.TYPE_SC);
        question.setSingleCorrect(true);
        question.setSingleCorrectScore(1);

        final ChoiceResponse newChoice = new ChoiceResponse();
        newChoice.setCorrect(true);
        newChoice.setPoints(1.0f);
        newChoice.getContent().add(new Mattext(qtiEditorLocalStrings.getNewResponseText()));
        question.getResponses().add(newChoice);

        QTIEditHelperEBL.setFeedbackMastery(newItem, "");
        QTIEditHelperEBL.setFeedbackFail(newItem, "");

        newItem.setQuestion(question);
        return newItem;
    }

    /**
     * Creates a new Multiple Choice item.
     * 
     * @param trans
     * @return New Multiple Choice item.
     */
    public static Item createMCItem(QtiEditorLocalStrings qtiEditorLocalStrings) {
        // create item
        final Item newItem = new Item();
        newItem.setIdent(EDITOR_IDENT + ":" + ITEM_TYPE_MC + ":" + String.valueOf(CodeHelper.getRAMUniqueID()));
        newItem.setTitle(qtiEditorLocalStrings.getNewQuestion());
        newItem.setLabel("");

        // conrols
        final Control control = new Control();
        final ArrayList controls = new ArrayList();
        controls.add(control);
        newItem.setItemcontrols(controls);

        // pepare question
        final ChoiceQuestion question = new ChoiceQuestion();
        question.setLable(qtiEditorLocalStrings.getNewQuestion());
        question.getQuestion().getElements().add(new Mattext(qtiEditorLocalStrings.getNewQuestionText()));
        question.setType(Question.TYPE_MC);
        question.setSingleCorrect(true);
        question.setSingleCorrectScore(1);

        final ChoiceResponse newChoice = new ChoiceResponse();
        newChoice.getContent().add(new Mattext(qtiEditorLocalStrings.getNewResponseText()));
        newChoice.setCorrect(true);
        newChoice.setPoints(1);
        question.getResponses().add(newChoice);
        newItem.setQuestion(question);

        QTIEditHelperEBL.setFeedbackMastery(newItem, "");
        QTIEditHelperEBL.setFeedbackFail(newItem, "");

        return newItem;
    }

    /**
     * Creates a new Kprim item
     * 
     * @param trans
     * @return New Kprim item.
     */
    public static Item createKPRIMItem(QtiEditorLocalStrings qtiEditorLocalStrings) {
        // create item
        final Item newItem = new Item();
        newItem.setIdent(EDITOR_IDENT + ":" + ITEM_TYPE_KPRIM + ":" + String.valueOf(CodeHelper.getRAMUniqueID()));
        newItem.setTitle(qtiEditorLocalStrings.getNewQuestion());
        newItem.setLabel("");

        // controls
        final Control control = new Control();
        final ArrayList controls = new ArrayList();
        controls.add(control);
        newItem.setItemcontrols(controls);

        // prepare question
        final float maxValue = 1;
        final ChoiceQuestion question = new ChoiceQuestion();
        question.setLable(qtiEditorLocalStrings.getNewQuestion());
        question.getQuestion().getElements().add(new Mattext(qtiEditorLocalStrings.getNewQuestionText()));
        question.setType(Question.TYPE_KPRIM);
        question.setSingleCorrect(false);

        // Kprim has always 4 answers, each of them score 1/4 of the maximum value
        final ChoiceResponse newChoice = new ChoiceResponse();
        newChoice.getContent().add(new Mattext(qtiEditorLocalStrings.getNewResponseText()));
        newChoice.setCorrect(false);
        newChoice.setPoints(maxValue / 4);
        question.getResponses().add(newChoice);
        final ChoiceResponse newChoice2 = new ChoiceResponse();
        newChoice2.getContent().add(new Mattext(qtiEditorLocalStrings.getNewResponseText()));
        newChoice2.setCorrect(false);
        newChoice2.setPoints(maxValue / 4);
        question.getResponses().add(newChoice2);
        final ChoiceResponse newChoice3 = new ChoiceResponse();
        newChoice3.getContent().add(new Mattext(qtiEditorLocalStrings.getNewResponseText()));
        newChoice3.setCorrect(false);
        newChoice3.setPoints(maxValue / 4);
        question.getResponses().add(newChoice3);
        final ChoiceResponse newChoice4 = new ChoiceResponse();
        newChoice4.getContent().add(new Mattext(qtiEditorLocalStrings.getNewResponseText()));
        newChoice4.setCorrect(false);
        newChoice4.setPoints(maxValue / 4);
        question.getResponses().add(newChoice4);
        question.setMaxValue(maxValue);
        newItem.setQuestion(question);

        QTIEditHelperEBL.setFeedbackMastery(newItem, "");
        QTIEditHelperEBL.setFeedbackFail(newItem, "");

        return newItem;
    }

    /**
     * Creates a new FIB item
     * 
     * @param trans
     * @return New fib item.
     */
    public static Item createFIBItem(QtiEditorLocalStrings qtiEditorLocalStrings) {
        // create item
        final Item newItem = new Item();
        newItem.setIdent(EDITOR_IDENT + ":" + ITEM_TYPE_FIB + ":" + String.valueOf(CodeHelper.getRAMUniqueID()));
        newItem.setTitle(qtiEditorLocalStrings.getNewQuestion());
        newItem.setLabel("");

        // conrols
        final Control control = new Control();
        final ArrayList controls = new ArrayList();
        controls.add(control);
        newItem.setItemcontrols(controls);

        QTIEditHelperEBL.setFeedbackMastery(newItem, "");
        QTIEditHelperEBL.setFeedbackFail(newItem, "");

        final FIBQuestion fibquestion = new FIBQuestion();
        fibquestion.getQuestion().getElements().add(new Mattext(qtiEditorLocalStrings.getNewQuestionText()));
        fibquestion.setSingleCorrect(true);
        fibquestion.setSingleCorrectScore(1);

        final FIBResponse response = new FIBResponse();
        response.setType(FIBResponse.TYPE_CONTENT);
        final Material mat = new Material();
        mat.add(new Mattext(qtiEditorLocalStrings.getNewTextElement()));
        response.setContent(mat);
        fibquestion.getResponses().add(response);
        newItem.setQuestion(fibquestion);

        return newItem;
    }

    /**
     * Creates a new essay item
     * 
     * @param trans
     * @return New essay item.
     */
    public static Item createEssayItem(QtiEditorLocalStrings qtiEditorLocalStrings) {
        // create item
        final Item newItem = new Item();
        newItem.setIdent(EDITOR_IDENT + ":" + ITEM_TYPE_ESSAY + ":" + String.valueOf(CodeHelper.getRAMUniqueID()));
        newItem.setTitle(qtiEditorLocalStrings.getNewQuestion());
        newItem.setLabel("");

        // conrols
        final Control control = new Control();
        final ArrayList controls = new ArrayList();
        controls.add(control);
        newItem.setItemcontrols(controls);

        QTIEditHelperEBL.setFeedbackMastery(newItem, "");
        QTIEditHelperEBL.setFeedbackFail(newItem, "");

        final EssayQuestion essayquestion = new EssayQuestion();
        essayquestion.getQuestion().getElements().add(new Mattext(qtiEditorLocalStrings.getNewQuestionText()));
        essayquestion.setSingleCorrect(true);
        essayquestion.setSingleCorrectScore(1);

        final EssayResponse response = new EssayResponse();
        final Material mat = new Material();
        mat.add(new Mattext(qtiEditorLocalStrings.getNewTextElement()));
        response.setContent(mat);
        essayquestion.getResponses().add(response);
        newItem.setQuestion(essayquestion);

        return newItem;
    }

    /**
     * Configure max score for a question.
     * 
     * @param question
     * @param decvar
     */
    public static void configureMinMaxScore(final Question question, final Element decvar) {
        // set min/max score
        boolean doCalculate = true;
        if (decvar != null) {
            final String min = decvar.attributeValue("minvalue");
            if (min != null) {
                question.setMinValue(min);
            }
            final String max = decvar.attributeValue("maxvalue");
            if (max != null) {
                question.setMaxValue(max);
                doCalculate = false;
            }
        }
        if (doCalculate) {
            question.setMaxValue(QTIEditHelperEBL.calculateMaxScore(question));
        }
    }

    /**
     * Get controls.
     * 
     * @param object
     * @return Controls.
     */
    public static Control getControl(final QTIObject object) {
        Control control = null;
        List controls = null;
        if (Item.class.isAssignableFrom(object.getClass())) {
            final Item item = (Item) object;
            controls = item.getItemcontrols();
        } else if (Section.class.isAssignableFrom(object.getClass())) {
            final Section section = (Section) object;
            controls = section.getSectioncontrols();
        } else if (Assessment.class.isAssignableFrom(object.getClass())) {
            final Assessment assessment = (Assessment) object;
            controls = assessment.getAssessmentcontrols();
        }
        for (final Iterator i = controls.iterator(); i.hasNext();) {
            final Control tmp = (Control) i.next();
            if (tmp.getView() != null) {
                if (tmp.getView().equalsIgnoreCase("all")) {
                    control = tmp;
                    break;
                }
            } else {
                control = tmp;
            }
        }
        return control;
    }

    /**
     * Calculates the max score for a question (sum of scores)
     * 
     * @param question
     * @return max score.
     */
    public static float calculateMaxScore(final Question question) {
        float tmpScore = 0;
        if (question.isSingleCorrect()) {
            return question.getSingleCorrectScore();
        }
        for (final Iterator iter = question.getResponses().iterator(); iter.hasNext();) {
            final Response resp = (Response) iter.next();
            final float points = resp.getPoints();
            if (points > 0) {
                tmpScore = tmpScore + points;
            }
        }
        return tmpScore;
    }

    /**
     * Returns a hasmap with responselabel_idents as keys and points as values.
     * 
     * @param respconditions
     * @param type
     * @return hasmap with responselabel_idents as keys and points as values.
     */

    public static Map<String, Float> fetchPoints(List respconditions, int type) {
        final Map<String, Float> points = new HashMap<String, Float>();
        for (Iterator i = respconditions.iterator(); i.hasNext();) {
            Element el_resp_condition = (Element) i.next();
            // /todo
            float fPoints = 0;
            try {
                Element el_setvar = el_resp_condition.element("setvar");
                if (el_setvar == null)
                    continue;
                if (!el_setvar.attributeValue("action").equals("Add") && !el_setvar.attributeValue("action").equals("Subtract")
                        && !el_setvar.attributeValue("action").equals("Set"))
                    continue;
                fPoints = new Float(el_setvar.getTextTrim()).floatValue();
                if (el_setvar.attributeValue("action").equals("Subtract"))
                    fPoints = fPoints * -1;
            } catch (NumberFormatException nfe) {
                continue;
            }
            if (fPoints != 0) {
                // this code has been reworked for http://bugs.olat.org/jira/browse/OLAT-6672
                Element conditionvar = el_resp_condition.element("conditionvar");
                if (type == Question.TYPE_SC) {
                    // see ChoiceQuestion#buildRespconditionSC_mastery
                    final Element el_varequal = (Element) conditionvar.selectNodes(".//varequal").get(0);
                    points.put(el_varequal.getTextTrim(), new Float(fPoints));
                } else if (type == Question.TYPE_MC) {
                    // see ChoiceQuestion#buildRespconditionMCSingle_mastery
                    final Element el_and = conditionvar.element("and");
                    final Element el_not = conditionvar.element("not");
                    List<Element> tmp_points = new ArrayList<Element>();
                    if (el_and != null || el_not != null) {
                        // this is "singleCorrect" MCQ => select all nodes under <and> tag
                        if (el_and != null) {
                            tmp_points = el_and.selectNodes(".//varequal");
                        }
                    } else {
                        tmp_points = conditionvar.selectNodes(".//varequal");
                    }
                    for (Iterator iter = tmp_points.iterator(); iter.hasNext();) {
                        Element el_varequal = (Element) iter.next();
                        points.put(el_varequal.getTextTrim(), new Float(fPoints));
                    }
                } else {
                    // left this part of the code (KPRIM and FIB) unchained in order to avoid collateral damage ...
                    Element and = conditionvar.element("and");
                    List tmp_points = (and == null) ? conditionvar.selectNodes(".//varequal") : and.selectNodes(".//varequal");
                    for (Iterator iter = tmp_points.iterator(); iter.hasNext();) {
                        Element el_varequal = (Element) iter.next();
                        if (type == Question.TYPE_KPRIM) {
                            points.put(el_varequal.getTextTrim(), new Float(fPoints));
                        } else if (type == Question.TYPE_FIB) {
                            points.put(el_varequal.attributeValue("respident"), new Float(fPoints));
                        }
                    }
                }
            }
        }
        return points;
    }

    /**
     * Fetch choices.
     * 
     * @param response_labels
     * @return Map of choices.
     */
    public static List<Response> fetchChoices(List response_labels) {
        List<Response> choices = new ArrayList<Response>();
        for (Iterator i = response_labels.iterator(); i.hasNext();) {
            ChoiceResponse choice = new ChoiceResponse();
            Element response_label = (Element) i.next();
            choice.setIdent(response_label.attributeValue("ident"));

            List materials = response_label.selectNodes(".//material");
            Material content = new Material();
            for (Iterator iter = materials.iterator(); iter.hasNext();) {
                Element el_material = (Element) iter.next();
                Material mat = (Material) parserManager.parse(el_material);
                content.getElements().addAll(mat.getElements());
            }
            // assure material always has some content
            if (content.getElements().size() == 0)
                content.getElements().add(new Mattext("[blank]"));
            choice.setContent(content);
            choices.add(choice);
        }
        return choices;
    }

    /**
     * Get olat response feddback
     * 
     * @param object
     * @param respident
     * @return feedback
     */
    public static String getFeedbackOlatRespText(final QTIObject object, final String respident) {
        return getFeedbackText(object, respident);
    }

    /**
     * Get olat response feddback
     * 
     * @param object
     * @param respident
     * @return feedback
     */
    public static Material getFeedbackOlatRespMaterial(final QTIObject object, final String respident) {
        return getFeedbackMaterial(object, respident);
    }

    /**
     * Get mastery feedback
     * 
     * @param object
     * @return mastery feedback
     */
    public static String getFeedbackMasteryText(final QTIObject object) {
        return getFeedbackText(object, "Mastery");
    }

    /**
     * Get mastery feedback
     * 
     * @param object
     * @return mastery feedback
     */
    public static Material getFeedbackMasteryMaterial(final QTIObject object) {
        return getFeedbackMaterial(object, "Mastery");
    }

    /**
     * Get fail feedback
     * 
     * @param object
     * @return fail feedback
     */
    public static String getFeedbackFailText(final QTIObject object) {
        return getFeedbackText(object, "Fail");
    }

    /**
     * Get fail feedback
     * 
     * @param object
     * @return fail feedback
     */
    public static Material getFeedbackFailMaterial(final QTIObject object) {
        return getFeedbackMaterial(object, "Fail");
    }

    /**
     * Get feedback
     * 
     * @param object
     * @param sIdent
     * @return feedback
     */
    public static String getFeedbackText(final QTIObject object, final String sIdent) {
        final Feedback feedback = getFeedback(object, sIdent);
        try {
            final Material mat = (Material) feedback.getMaterials().get(0);
            return mat.renderAsText();
        } catch (final Exception e) {
            //
        }
        return "";
    }

    /**
     * @param object
     * @param sIdent
     * @return
     */
    public static Feedback getFeedback(final QTIObject object, final String sIdent) {
        final List<Feedback> feedbacks = getFeedbacks(object);
        return getFeedback(sIdent, feedbacks);
    }

    /**
     * @param object
     * @param sIdent
     * @return
     */
    public static Material getFeedbackMaterial(final QTIObject object, final String sIdent) {
        final Feedback feedback = getFeedback(object, sIdent);
        Material mat = null;
        try {
            mat = (Material) feedback.getMaterials().get(0);
        } catch (final NullPointerException e) {
            // feedback is null
        }
        return mat;
    }

    /**
     * @param sIdent
     * @param feedback
     * @param feedbacks
     * @return
     */
    private static Feedback getFeedback(final String sIdent, final List<Feedback> feedbacks) {
        Feedback feedback = null;
        for (final Feedback tmp : feedbacks) {
            if (tmp.getIdent().equalsIgnoreCase(sIdent)) {
                if (tmp.getView() != null) {
                    if (tmp.getView().equalsIgnoreCase("all")) {
                        feedback = tmp;
                        break;
                    }
                } else {
                    feedback = tmp;
                }
            }
        }
        return feedback;
    }

    /**
     * @param object
     * @return
     */
    private static List<Feedback> getFeedbacks(final QTIObject object) {
        List<Feedback> feedbacks = null;
        if (Item.class.isAssignableFrom(object.getClass())) {
            final Item item = (Item) object;
            feedbacks = item.getItemfeedbacks();
        } else if (Section.class.isAssignableFrom(object.getClass())) {
            final Section section = (Section) object;
            feedbacks = section.getSectionfeedbacks();
        } else if (Assessment.class.isAssignableFrom(object.getClass())) {
            final Assessment assessment = (Assessment) object;
            feedbacks = assessment.getAssessfeedbacks();
        }
        return feedbacks;
    }

    /**
     * Set response feedback
     * 
     * @param object
     * @param feedbackString
     * @param respident
     */
    public static void setFeedbackOlatResp(final QTIObject object, final String feedbackString, final String respident) {
        setFeedback(object, feedbackString, respident);
    }

    /**
     * Set mastery feedback.
     * 
     * @param object
     * @param feedbackString
     */
    public static void setFeedbackMastery(final QTIObject object, final String feedbackString) {
        setFeedback(object, feedbackString, "Mastery");
    }

    /**
     * Set fail feedback.
     * 
     * @param object
     * @param feedbackString
     */
    public static void setFeedbackFail(final QTIObject object, final String feedbackString) {
        setFeedback(object, feedbackString, "Fail");
    }

    /**
     * Set feedback
     * 
     * @param object
     * @param feedbackString
     * @param sIdent
     */
    public static void setFeedback(final QTIObject object, String feedbackString, final String sIdent) {
        final List feedbacks = getFeedbacks(object);
        final Feedback feedback = getFeedback(sIdent, feedbacks);

        if (feedbackString == null || feedbackString.trim().length() == 0) {
            feedbacks.remove(feedback);
            return;
        }

        if (feedback != null) {
            feedbackString = feedbackString.trim();
            final List matList = feedback.getMaterials();
            if (matList.size() > 0) {
                Material mat = (Material) feedback.getMaterials().get(0);
                if (mat == null) {
                    mat = new Material();
                    mat.getElements().add(new Mattext(feedbackString));
                    feedback.getMaterials().add(mat);
                } else if (mat.getElements().size() > 0) {
                    mat.getElements().set(0, new Mattext(feedbackString));
                } else {
                    mat.getElements().add(new Mattext(feedbackString));
                }
            } else {
                final Material mat = new Material();
                mat.getElements().add(new Mattext(feedbackString));
                feedback.getMaterials().add(mat);
            }
        } else {
            final Feedback newFeedback = new Feedback();
            newFeedback.setIdent(sIdent);
            newFeedback.setView("All");
            final Mattext newMattext = new Mattext(feedbackString);

            final ArrayList newMattextL = new ArrayList();
            newMattextL.add(newMattext);

            final Material material = new Material();
            material.setElements(newMattextL);

            final ArrayList newMaterialL = new ArrayList();
            newMaterialL.add(material);
            newFeedback.setMaterials(newMaterialL);
            feedbacks.add(newFeedback);
        }

    }

    /**
     * Add objectives.
     * 
     * @param root
     * @param objectives
     */
    public static void addObjectives(final Element root, final String objectives) {
        if (objectives != null && objectives.length() > 0) {
            final Element mattext = root.addElement("objectives").addElement("material").addElement("mattext");
            mattext.addCDATA(objectives);
        }
    }

    /**
     * Add response feedback.
     * 
     * @param root
     * @param respident
     */
    public static void addFeedbackOlatResp(final Element root, final String respident) {
        addFeedback(root, "Response", respident);
    }

    /**
     * Add mastery feedback
     * 
     * @param root
     */
    public static void addFeedbackMastery(final Element root) {
        addFeedback(root, "Response", "Mastery");
    }

    /**
     * Add fail feedback
     * 
     * @param root
     */
    public static void addFeedbackFail(final Element root) {
        addFeedback(root, "Response", "Fail");
    }

    /**
     * Add hint
     * 
     * @param root
     */
    public static void addFeedbackHint(final Element root) {
        addFeedback(root, "Solution", "Solution");
    }

    /**
     * Add solution
     * 
     * @param root
     */
    public static void addFeedbackSolution(final Element root) {
        addFeedback(root, "Hint", "Hint");
    }

    private static void addFeedback(final Element root, final String feedbacktype, final String linkrefid) {
        final Element displayfeedback = root.addElement("displayfeedback");
        displayfeedback.addAttribute("feedbacktype", feedbacktype);
        displayfeedback.addAttribute("linkrefid", linkrefid);
    }

    /**
     * Add solution
     * 
     * @param root
     * @param solutionText
     */
    public static void addSolutionElement(final Element root, final String solutionText) {
        final Element el_solution = root.addElement("itemfeedback");
        el_solution.addAttribute("ident", "Solution");
        el_solution.addAttribute("view", "All");
        el_solution.addElement("solution").addElement("solutionmaterial").addElement("material").addElement("mattext").addCDATA(solutionText);
    }

    /**
     * Add hint
     * 
     * @param root
     * @param hintText
     */
    public static void addHintElement(final Element root, final String hintText) {
        final Element el_feedback = root.addElement("itemfeedback");
        el_feedback.addAttribute("ident", "Hint");
        el_feedback.addAttribute("view", "All");
        final Element el_hint = el_feedback.addElement("hint");
        el_hint.addAttribute("feedbackstyle", "Incremental");
        el_hint.addElement("hintmaterial").addElement("material").addElement("mattext").addCDATA(hintText);
    }

    /**
     * Retrieves all deleteable media files, that is the media files that are referenced by thisItem only. Note: doesn't retrieve the media from the question description
     * because of OLAT-4647
     * 
     * @param qtiDocument
     * @param thisItem
     * @return
     */
    public static Set<String> getDeletableMedia(final QTIDocument qtiDocument, final Item thisItem) {
        final Set<String> deletableMediaFiles = QTIEditHelperEBL.getMediaReferencesForItem(qtiDocument, thisItem);
        if (deletableMediaFiles.size() > 0) {
            final Set<String> referencedMediaFiles = QTIEditHelperEBL.getMediaReferencesExceptForItem(qtiDocument, thisItem);
            deletableMediaFiles.removeAll(referencedMediaFiles);
        }
        return deletableMediaFiles;
    }

    /**
     * Retrieves all referenced media by thisItem if filterOut is false, or all referenced media by other items if filterOut is true.
     * <p>
     * Iterates over all sections, items, etc. </br> -> if filterOut is true gets all references except those for thisItem. -> if filterOut is false gets all references
     * for thisItem.
     * 
     * @param qtiDocument
     * @param thisItem
     * @param filterOut
     * @return Returns empty set if no reference found.
     */
    private static Set<String> getMediaReferences(final QTIDocument qtiDocument, final Item thisItem, final boolean filterOut) {
        final HashSet<String> returnSet = new HashSet<String>();
        // sections
        final List sectionList = qtiDocument.getAssessment().getSections();
        final Iterator sectionIterator = sectionList.iterator();
        while (sectionIterator.hasNext()) {
            // section
            final Section section = (Section) sectionIterator.next();
            final List itemList = section.getItems();
            final Iterator listIterator = itemList.iterator();
            while (listIterator.hasNext()) {
                // item
                final Item item = (Item) listIterator.next();
                if ((filterOut && thisItem.getIdent().equals(item.getIdent())) || (!filterOut && !thisItem.getIdent().equals(item.getIdent()))) {
                    continue;
                }
                // question
                final Material material = item.getQuestion().getQuestion();
                if (material != null) {
                    final String htmlContent = material.renderAsHtmlForEditor();
                    // parse filenames
                    returnSet.addAll(getMediaFileNames(htmlContent));
                }
                // responses
                final List responseList = item.getQuestion().getResponses();
                final Iterator responseIterator = responseList.iterator();
                while (responseIterator.hasNext()) {
                    final Response response = (Response) responseIterator.next();
                    final Material responseMat = response.getContent();
                    // parse filenames
                    if (responseMat != null) {
                        returnSet.addAll(getMediaFileNames(responseMat.renderAsHtmlForEditor()));
                    }
                    // response-level feedback
                    final Material responseFeedbackMat = QTIEditHelperEBL.getFeedbackOlatRespMaterial(item, response.getIdent());
                    if (responseFeedbackMat != null) {
                        returnSet.addAll(getMediaFileNames(responseFeedbackMat.renderAsHtmlForEditor()));
                    }
                }
                // feedback
                final Material masteryMat = QTIEditHelperEBL.getFeedbackMasteryMaterial(item);
                if (masteryMat != null) {
                    returnSet.addAll(getMediaFileNames(masteryMat.renderAsHtmlForEditor()));
                }
                final Material failureMat = QTIEditHelperEBL.getFeedbackFailMaterial(item);
                if (failureMat != null) {
                    returnSet.addAll(getMediaFileNames(failureMat.renderAsHtmlForEditor()));
                }
            }
        }
        return returnSet;
    }

    /**
     * @param qtiDocument
     * @param thisItem
     * @return Returns a Set with the media file names referenced by thisItem.
     */
    private static Set<String> getMediaReferencesForItem(final QTIDocument qtiDocument, final Item thisItem) {
        return getMediaReferences(qtiDocument, thisItem, false);
    }

    /**
     * @param qtiDocument
     * @param thisItem
     * @return Returns a Set with the media file names referenced by all except thisItem.
     */
    private static Set<String> getMediaReferencesExceptForItem(final QTIDocument qtiDocument, final Item thisItem) {
        return getMediaReferences(qtiDocument, thisItem, true);
    }

    /**
     * Extracts substrings between media/ and next ", and add them to a set. The htmlString contains something like: ... img src=".../media/filename.jpg" ...
     * 
     * @param htmlString
     * @return
     */
    private static Set<String> getMediaFileNames(final String htmlString) {
        final HashSet<String> returnSet = new HashSet<String>();
        String current = htmlString;
        while (current.indexOf("media/") > 0) {
            current = current.substring(current.indexOf("media/") + 6);
            final int position = current.indexOf("\"");
            if (position > 0) {
                final String filename = current.substring(0, position);
                returnSet.add(filename);
                current = current.substring(position + 1, current.length());
            }
        }
        return returnSet;
    }

    /**
     * Deletes the files found in the referencedMediaSet.
     * 
     * @param referencedMediaSet
     * @param allMedia
     */
    public static void removeUnusedMedia(final Set<String> deleteableSet, final List<VFSItem> allMedia) {
        final Iterator<VFSItem> itemIterator = allMedia.iterator();
        while (itemIterator.hasNext()) {
            final VFSItem item = itemIterator.next();
            if (deleteableSet.contains(item.getName())) {
                // System.out.println("Delete unused media file: " + item.getName());
                item.delete();
            }
        }
    }
}
