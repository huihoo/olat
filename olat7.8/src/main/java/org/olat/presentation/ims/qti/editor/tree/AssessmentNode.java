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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.ims.qti.objects.Assessment;
import org.olat.lms.ims.qti.objects.QTIObject;
import org.olat.presentation.commons.memento.Memento;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.ims.qti.editor.AssessmentController;
import org.olat.presentation.ims.qti.editor.QTIEditorMainController;

/**
 * Initial Date: Nov 18, 2004 <br>
 * 
 * @author patrick
 */
public class AssessmentNode extends GenericQtiNode {

    private final Assessment assmnt;
    private final QTIEditorPackageEBL qtiPackage;
    private TabbedPane myTabbedPane;

    /**
     * @param ass
     * @param qtiPackage
     */
    public AssessmentNode(final Assessment ass, final QTIEditorPackageEBL qtiPackage) {
        this.assmnt = ass;
        this.qtiPackage = qtiPackage;
        setMenuTitleAndAlt(ass.getTitle());
        setUserObject(ass.getIdent());
        if (qtiPackage.getQTIDocument().isSurvey()) {
            setIconCssClass("o_mi_iqsurv");
        } else {
            setIconCssClass("o_mi_iqtest");
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
        assmnt.setTitle(title);
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
            myTabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
            final TabbableController tabbCntrllr = new AssessmentController(assmnt, qtiPackage, ureq, wControl, editorMainController.isRestrictedEdit());
            tabbCntrllr.addTabs(myTabbedPane);
            tabbCntrllr.addControllerListener(editorMainController);
        }
        return myTabbedPane;
    }

    /**
     * @return Assessment
     */
    public Assessment getAssessment() {
        return assmnt;
    }

    /**
	 */
    @Override
    public void insertQTIObjectAt(final QTIObject object, final int position) {
        final List sections = assmnt.getSections();
        sections.add(position, object);
    }

    /**
	 */
    @Override
    public QTIObject removeQTIObjectAt(final int position) {
        final List sections = assmnt.getSections();
        return (QTIObject) sections.remove(position);
    }

    /**
	 */
    @Override
    public QTIObject getQTIObjectAt(final int position) {
        final List sections = assmnt.getSections();
        return (QTIObject) sections.get(position);
    }

    /**
	 */
    @Override
    public QTIObject getUnderlyingQTIObject() {
        return assmnt;
    }

    @Override
    public Memento createMemento() {
        // so far only TITLE and OBJECTIVES are stored in the memento
        final QtiNodeMemento qnm = new QtiNodeMemento();
        final Map qtiState = new HashMap();
        qtiState.put("ID", assmnt.getIdent());
        qtiState.put("TITLE", assmnt.getTitle());
        qtiState.put("OBJECTIVES", assmnt.getObjectives());
        qnm.setQtiState(qtiState);
        return qnm;
    }

    @Override
    public void setMemento(final Memento state) {
        // TODO Auto-generated method stub

    }

    public String createChangeMessage(final Memento mem) {
        String retVal = null;
        if (mem instanceof QtiNodeMemento) {
            final QtiNodeMemento qnm = (QtiNodeMemento) mem;
            final Map qtiState = qnm.getQtiState();
            final String oldTitle = (String) qtiState.get("TITLE");
            final String newTitle = assmnt.getTitle();
            String titleChange = null;
            final String oldObjectives = (String) qtiState.get("OBJECTIVES");
            final String newObjectives = assmnt.getObjectives();
            String objectChange = null;
            retVal = "\nMetadata changes:";
            if ((oldTitle != null && !oldTitle.equals(newTitle)) || (newTitle != null && !newTitle.equals(oldTitle))) {
                titleChange = "\n\nold title: \n\t" + formatVariable(oldTitle) + "\n\nnew title: \n\t" + formatVariable(newTitle);
                retVal += titleChange;
            }
            if ((oldObjectives != null && !oldObjectives.equals(newObjectives)) || (newObjectives != null && !newObjectives.equals(oldObjectives))) {
                objectChange = "\n\nold objectives: \n\t" + formatVariable(oldObjectives) + "\n\nnew objectives: \n\t" + formatVariable(newObjectives);
                retVal += objectChange;
            }
            return retVal;
        }
        return "undefined";
    }
}
