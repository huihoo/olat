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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.scoring.ScoreCalculator;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This form is used to generate score and passed expression for structure course nodes in an easy way. See EditScoreCalculationExpertForm for the expert way.
 * <p>
 * 
 * @author gnaegi
 */
public class EditScoreCalculationEasyForm extends FormBasicController {

    private MultipleSelectionElement hasScore, hasPassed;
    private SingleSelection passedType;
    private MultipleSelectionElement scoreNodeIdents, passedNodeIdents;
    private IntegerElement passedCutValue;
    private final ScoreCalculator sc;

    private static final String DELETED_NODE_IDENTIFYER = "deletedNode";
    private final List<CourseNode> assessableNodesList;

    private final String[] hasScoreKeys = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
    private final String[] hasScoreValues = new String[] { translate("scform.hasScore.yes"), translate("no") };
    private final List<CourseNode> nodeIdentList;

    /**
     * @param name
     * @param trans
     * @param scoreCalculator
     * @param nodeIdentList
     */
    public EditScoreCalculationEasyForm(final UserRequest ureq, final WindowControl wControl, final ScoreCalculator scoreCalculator, final List<CourseNode> nodeIdentList) {
        super(ureq, wControl);

        sc = scoreCalculator;
        this.assessableNodesList = nodeIdentList;
        this.nodeIdentList = nodeIdentList;

        initForm(ureq);
    }

    /**
	 */
    @Override
    public boolean validateFormLogic(final UserRequest ureq) {
        boolean rv = true;
        if (hasScore.isSelected(0)) {
            if (scoreNodeIdents.getSelectedKeys().size() == 0) {
                scoreNodeIdents.setErrorKey("scform.scoreNodeIndents.error", null);
                rv = false;
            } else if (scoreNodeIdents.getSelectedKeys().contains(DELETED_NODE_IDENTIFYER)) {
                scoreNodeIdents.setErrorKey("scform.deletedNode.error", null);
                rv = false;
            } else {
                scoreNodeIdents.clearError();
            }
        }

        if (hasPassed.isSelected(0)) {
            if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_INHERIT)) {
                if (passedNodeIdents.getSelectedKeys().size() == 0) {
                    passedNodeIdents.setErrorKey("scform.passedNodeIndents.error", null);
                    rv = false;
                } else {
                    passedNodeIdents.clearError();
                }
            } else if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_CUTVALUE)) {
                if (!hasScore.isSelected(0)) {
                    passedType.setErrorKey("scform.passedType.error", null);
                    rv = false;
                } else {
                    passedType.clearError();
                }
            }

        }

        return rv;
    }

    /**
     * @return ScoreCalcualtor or null if no score calculator is set
     */
    public ScoreCalculator getScoreCalulator() {
        if (!hasScore.isSelected(0) && !hasPassed.isSelected(0)) {
            return null;
        }

        // 1) score configuration
        if (hasScore.isSelected(0)) {
            sc.setSumOfScoreNodes(new ArrayList(scoreNodeIdents.getSelectedKeys()));
        } else {
            // reset
            sc.setSumOfScoreNodes(null);
        }

        // 2) passed configuration
        if (!hasPassed.isSelected(0)) {
            sc.setPassedType(ScoreCalculator.PASSED_TYPE_NONE);
        } else if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_CUTVALUE)) {
            sc.setPassedType(ScoreCalculator.PASSED_TYPE_CUTVALUE);
            sc.setPassedCutValue(passedCutValue.getIntValue());
        } else if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_INHERIT)) {
            sc.setPassedType(ScoreCalculator.PASSED_TYPE_INHERIT);
            sc.setPassedNodes(new ArrayList(passedNodeIdents.getSelectedKeys()));
        }

        // update score and passed expression from easy mode configuration
        sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
        sc.setPassedExpression(sc.getPassedExpressionFromEasyModeConfiguration());

        if (sc.getScoreExpression() == null && sc.getPassedExpression() == null) {
            return null;
        }
        sc.setExpertMode(false);
        return sc;
    }

    /**
     * @return Returns a list with the invalid node descriptions, ("invalid" is a node that is not associated with a test resource)
     */
    public List<String> getInvalidNodeDescriptions() {
        final List<String> testElemWithNoResource = new ArrayList<String>();
        final List<String> selectedNodesIds = new ArrayList<String>(scoreNodeIdents.getSelectedKeys());
        for (final Iterator nodeIter = assessableNodesList.iterator(); nodeIter.hasNext();) {
            final CourseNode node = (CourseNode) nodeIter.next();
            if (selectedNodesIds.contains(node.getIdent())) {
                final StatusDescription isConfigValid = node.isConfigValid();
                if (isConfigValid != null && isConfigValid.isError()) {
                    final String nodeDescription = node.getShortName() + " (Id:" + node.getIdent() + ")";
                    if (!testElemWithNoResource.contains(nodeDescription)) {
                        testElemWithNoResource.add(nodeDescription);
                    }
                }
            }
        }
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

    /**
     * Initializes the node selection form elements first check if the form has a selection on a node that has been deleted in since the last edition of this form. if so,
     * set remember this to later add a dummy placeholder for the deleted node. We do not just ignore this since the form would look ok then to the user, the generated
     * rule visible in the expert mode however would still be invalid. user must explicitly uncheck this deleted node reference.
     * 
     * @param elemId
     *            name of the generated form element
     * @param scoreCalculator
     * @param selectedNodeList
     *            List of course nodes that are preselected
     * @param allNodesList
     *            List of all assessable course nodes
     * @return StaticMultipleSelectionElement The configured form element
     */
    private MultipleSelectionElement initNodeSelectionElement(final FormItemContainer formLayout, final String elemId, final ScoreCalculator scoreCalculator,
            final List selectedNodeList, final List allNodesList) {

        boolean addDeletedNodeIdent = false;
        if (scoreCalculator != null && selectedNodeList != null) {
            for (final Iterator iter = selectedNodeList.iterator(); iter.hasNext();) {
                final String nodeIdent = (String) iter.next();
                boolean found = false;
                for (final Iterator nodeIter = allNodesList.iterator(); nodeIter.hasNext();) {
                    final CourseNode node = (CourseNode) nodeIter.next();
                    if (node.getIdent().equals(nodeIdent)) {
                        found = true;
                    }
                }
                if (!found) {
                    addDeletedNodeIdent = true;
                }
            }
        }

        final String[] nodeKeys = new String[allNodesList.size() + (addDeletedNodeIdent ? 1 : 0)];
        final String[] nodeValues = new String[allNodesList.size() + (addDeletedNodeIdent ? 1 : 0)];
        for (int i = 0; i < allNodesList.size(); i++) {
            final CourseNode courseNode = (CourseNode) allNodesList.get(i);
            nodeKeys[i] = courseNode.getIdent();
            nodeValues[i] = courseNode.getShortName() + " (Id:" + courseNode.getIdent() + ")";
        }
        // add a deleted dummy node at last position
        if (addDeletedNodeIdent) {
            nodeKeys[allNodesList.size()] = DELETED_NODE_IDENTIFYER;
            nodeValues[allNodesList.size()] = translate("scform.deletedNode");
        }

        final MultipleSelectionElement mse = uifactory.addCheckboxesVertical(elemId, formLayout, nodeKeys, nodeValues, null, 2);
        // preselect nodes from configuration
        if (scoreCalculator != null && selectedNodeList != null) {
            for (final Iterator iter = selectedNodeList.iterator(); iter.hasNext();) {
                final String nodeIdent = (String) iter.next();
                boolean found = false;
                for (final Iterator nodeIter = allNodesList.iterator(); nodeIter.hasNext();) {
                    final CourseNode node = (CourseNode) nodeIter.next();
                    if (node.getIdent().equals(nodeIdent)) {
                        found = true;
                    }
                }
                if (found) {
                    mse.select(nodeIdent, true);
                } else {
                    mse.select(DELETED_NODE_IDENTIFYER, true);
                }
            }
        }
        return mse;
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem item, final FormEvent event) {
        if (item == passedType) {
            passedType.clearError();
        }
        scoreNodeIdents.setVisible(hasScore.isSelected(0));
        if (!scoreNodeIdents.isVisible()) {
            scoreNodeIdents.clearError();
        }
        passedType.setVisible(hasPassed.isSelected(0));
        passedCutValue.setVisible(passedType.isVisible() && passedType.isSelected(0));
        if (!passedCutValue.isVisible()) {
            passedCutValue.setIntValue(0);
            passedCutValue.clearError();
        }
        passedNodeIdents.setVisible(passedType.isVisible() && passedType.isSelected(1));
        if (!passedNodeIdents.isVisible()) {
            passedNodeIdents.clearError();
        }
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        hasScore = uifactory.addCheckboxesHorizontal("scform.hasScore", formLayout, new String[] { "xx" }, new String[] { null }, null);
        hasScore.select("xx", sc != null && sc.getSumOfScoreNodes() != null && sc.getSumOfScoreNodes().size() > 0);
        hasScore.addActionListener(listener, FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753

        scoreNodeIdents = initNodeSelectionElement(formLayout, "scform.scoreNodeIndents", sc, (sc == null ? null : sc.getSumOfScoreNodes()), nodeIdentList);
        scoreNodeIdents.setVisible(hasScore.isSelected(0));

        uifactory.addSpacerElement("spacer", formLayout, false);

        hasPassed = uifactory.addCheckboxesHorizontal("scform.passedtype", formLayout, new String[] { "xx" }, new String[] { null }, null);
        hasPassed.select("xx", sc != null && sc.getPassedType() != null && !sc.getPassedType().equals(ScoreCalculator.PASSED_TYPE_NONE));
        hasPassed.addActionListener(listener, FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753

        final String[] passedTypeKeys = new String[] { ScoreCalculator.PASSED_TYPE_CUTVALUE, ScoreCalculator.PASSED_TYPE_INHERIT };
        final String[] passedTypeValues = new String[] { translate("scform.passedtype.cutvalue"), translate("scform.passedtype.inherit") };

        passedType = uifactory.addRadiosVertical("passedType", null, formLayout, passedTypeKeys, passedTypeValues);
        passedType.setVisible(hasPassed.isSelected(0));
        if (sc != null && sc.getPassedType() != null && !sc.getPassedType().equals(ScoreCalculator.PASSED_TYPE_NONE)) {
            passedType.select(sc.getPassedType(), true);
        } else {
            passedType.select(ScoreCalculator.PASSED_TYPE_CUTVALUE, true);
        }
        passedType.addActionListener(listener, FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753

        int cutinitval = 0;
        if (sc != null) {
            cutinitval = sc.getPassedCutValue();
        }
        passedCutValue = uifactory.addIntegerElement("scform.passedCutValue", cutinitval, formLayout);
        passedCutValue.setDisplaySize(4);
        passedCutValue.setVisible(passedType.isVisible() && passedType.isSelected(0));
        passedCutValue.setMandatory(true);

        passedNodeIdents = initNodeSelectionElement(formLayout, "scform.passedNodeIndents", sc, (sc == null ? null : sc.getPassedNodes()), nodeIdentList);
        passedNodeIdents.setVisible(passedType.isVisible() && passedType.isSelected(1));

        final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);
        uifactory.addFormSubmitButton("submit", buttonGroupLayout);
        uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }
}
