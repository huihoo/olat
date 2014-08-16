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

package org.olat.presentation.course.nodes.st;

/**
 * Initial Date: 22.03.04
 * 
 * @author Felix Jost
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionErrorMessage;
import org.olat.lms.course.condition.interpreter.ConditionExpression;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.scoring.ScoreCalculator;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: guido Class Description for EditScoreCalculationExpertForm
 */
class EditScoreCalculationExpertForm extends FormBasicController {
    private static final String[] EXAMPLE_PASSED = new String[] { "getPassed(\"69741247660309\")" };
    private static final String[] EXAMPLE_SCORE = new String[] { "getScore(\"69741247660309\") * 2" };
    private TextElement tscoreexpr, tpassedexpr;
    private final UserCourseEnvironment euce;
    private ScoreCalculator sc;
    private final List<CourseNode> assessableNodesList;
    private List<String> testElemWithNoResource = new ArrayList<String>();

    private Translator pt = null;

    /**
     * Constructor for a score calculation edit form
     * 
     * @param name
     */
    public EditScoreCalculationExpertForm(final UserRequest ureq, final WindowControl wControl, final ScoreCalculator sc, final UserCourseEnvironment euce,
            final List<CourseNode> assessableNodesList) {
        super(ureq, wControl);
        this.sc = sc;
        this.euce = euce;
        this.assessableNodesList = assessableNodesList;

        initForm(ureq);
    }

    private void setKeys(final UserRequest ureq, final FormItem fi, final ConditionErrorMessage[] cem) {

        if (pt == null) {
            pt = PackageUtil.createPackageTranslator(Condition.class, ureq.getLocale());
        }

        // the error message
        fi.setErrorKey("rules.error", new String[] { pt.translate(cem[0].errorKey, cem[0].errorKeyParams) });

        if (cem[0].solutionMsgKey != null && !"".equals(cem[0].solutionMsgKey)) {
            // and a hint or example to clarify the error message
            fi.setExampleKey("rules.error", new String[] { pt.translate(cem[0].solutionMsgKey, cem[0].errorKeyParams) });
        }
    }

    @Override
    public boolean validateFormLogic(final UserRequest ureq) {

        final String scoreExp = tscoreexpr.getValue().trim();

        if (StringHelper.containsNonWhitespace(scoreExp)) {

            final CourseEditorEnv cev = euce.getCourseEditorEnv();
            final ConditionExpression ce = new ConditionExpression("score", scoreExp);
            final ConditionErrorMessage[] cerrmsgs = cev.validateConditionExpression(ce);

            if (cerrmsgs != null && cerrmsgs.length > 0) {
                setKeys(ureq, tscoreexpr, cerrmsgs);
                return false;
            }
            testElemWithNoResource = getInvalidNodeDescriptions(ce);
        }

        final String passedExp = tpassedexpr.getValue().trim();
        if (StringHelper.containsNonWhitespace(passedExp)) {

            final CourseEditorEnv cev = euce.getCourseEditorEnv();
            final ConditionExpression ce = new ConditionExpression("passed", passedExp);
            final ConditionErrorMessage[] cerrmsgs = cev.validateConditionExpression(ce);

            if (cerrmsgs != null && cerrmsgs.length > 0) {
                setKeys(ureq, tpassedexpr, cerrmsgs);
                return false;
            }
        }

        // reset HINTS
        tscoreexpr.setExampleKey("rules.example", EXAMPLE_SCORE);
        tpassedexpr.setExampleKey("rules.example", EXAMPLE_PASSED);
        return true;
    }

    /**
     * @param sc
     */
    public void setScoreCalculator(final ScoreCalculator sc) {
        this.sc = sc;
        tscoreexpr.setValue(sc == null ? "" : sc.getScoreExpression());
        tpassedexpr.setValue(sc == null ? "" : sc.getPassedExpression());
    }

    /**
     * @return ScoreCalcualtor
     */
    public ScoreCalculator getScoreCalulator() {
        String scoreExp = tscoreexpr.getValue().trim();
        String passedExp = tpassedexpr.getValue().trim();
        if (scoreExp.equals("") && passedExp.equals("")) {
            return null;
        }
        if (passedExp.equals("")) {
            passedExp = null;
        }
        if (scoreExp.equals("")) {
            scoreExp = null;
        }

        sc.setScoreExpression(scoreExp);
        sc.setPassedExpression(passedExp);
        sc.setExpertMode(true);
        return sc;
    }

    /**
     * Get the list with the node description of the "invalid" nodes. The "invalid" nodes are not associated with any test resource so they are actually not assessable.
     * 
     * @param ce
     * @return
     */
    private List<String> getInvalidNodeDescriptions(final ConditionExpression ce) {
        final List<String> nodeDescriptionList = new ArrayList<String>();
        if (ce != null) {
            final Set<String> selectedNodesIds = ce.getSoftReferencesOf("courseNodeId");
            for (final Iterator nodeIter = assessableNodesList.iterator(); nodeIter.hasNext();) {
                final CourseNode node = (CourseNode) nodeIter.next();
                if (selectedNodesIds.contains(node.getIdent())) {
                    final StatusDescription isConfigValid = node.isConfigValid();
                    if (isConfigValid != null && isConfigValid.isError()) {
                        final String nodeDescription = node.getShortName() + " (Id:" + node.getIdent() + ")";
                        if (!nodeDescriptionList.contains(nodeDescription)) {
                            nodeDescriptionList.add(nodeDescription);
                        }
                    }
                }
            }
        }
        return nodeDescriptionList;
    }

    public List<String> getInvalidNodeDescriptions() {
        return testElemWithNoResource;
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        tscoreexpr = uifactory.addTextAreaElement("tscoreexpr", "scorecalc.score", 5000, 6, 45, true, sc.getScoreExpression(), formLayout);
        tpassedexpr = uifactory.addTextAreaElement("tpassedexpr", "scorecalc.passed", 5000, 6, 45, true, sc.getPassedExpression(), formLayout);
        tscoreexpr.setExampleKey("rules.example", EXAMPLE_SCORE);
        tpassedexpr.setExampleKey("rules.example", EXAMPLE_PASSED);

        // Button layout
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);

        uifactory.addFormSubmitButton("submit", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }

}
