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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.lms.course.nodes;

import java.util.List;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.cal.CalEditController;
import org.olat.presentation.course.nodes.cal.CalRunController;
import org.olat.presentation.course.nodes.cal.CourseCalendarPeekViewController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;

/**
 * <h3>Description:</h3> Course node for calendar Initial Date: 4 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {

    public static final String TYPE = "cal";
    public static final String EDIT_CONDITION_ID = "editarticle";
    private Condition preConditionEdit;

    /**
     * Default constructor for course node of type calendar
     */
    public CalCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    @Override
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        final ModuleConfiguration config = getModuleConfiguration();
        if (isNewNode) {
            // use defaults for new course building blocks
            config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
            config.setConfigurationVersion(1);
        } else {

            if (config.getConfigurationVersion() < 2) {
                final Condition cond = getPreConditionEdit();
                if (!cond.isExpertMode() && cond.isEasyModeCoachesAndAdmins() && cond.getConditionExpression() == null) {
                    // ensure that the default config has a condition expression
                    cond.setConditionExpression(cond.getConditionFromEasyModeConfiguration());
                }
                config.setConfigurationVersion(2);
            }
        }
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        final CalEditController childTabCntrllr = new CalEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
        final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(), euce,
                childTabCntrllr);

    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final String nodecmd) {
        updateModuleConfigDefaults(false);
        final CalRunController calCtlr = new CalRunController(wControl, ureq, this, userCourseEnv.getCourseEnvironment(), ne);
        final Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, calCtlr, this, "o_cal_icon");
        return new NodeRunConstructionResult(wrapperCtrl);
    }

    @Override
    public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        final CourseCalendarPeekViewController peekViewCtrl = new CourseCalendarPeekViewController(ureq, wControl, userCourseEnv, this, ne);
        return peekViewCtrl;
    }

    /**
	 */
    @Override
    public StatusDescription isConfigValid() {
        // first check the one click cache
        if (oneClickStatusCache != null) {
            return oneClickStatusCache[0];
        }
        return StatusDescription.NOERROR;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(CalEditController.class);
        final List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public RepositoryEntry getReferencedRepositoryEntry() {
        return null;
    }

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return false;
    }

    /**
     * Default set the write privileges to coaches and admin only
     * 
     * @return
     */
    public Condition getPreConditionEdit() {
        if (preConditionEdit == null) {
            preConditionEdit = new Condition();
            preConditionEdit.setEasyModeCoachesAndAdmins(true);
            preConditionEdit.setConditionExpression(preConditionEdit.getConditionFromEasyModeConfiguration());
            preConditionEdit.setExpertMode(false);
        }
        preConditionEdit.setConditionId(EDIT_CONDITION_ID);
        return preConditionEdit;
    }

    /**
     * @param preConditionEdit
     */
    public void setPreConditionEdit(Condition preConditionEdit) {
        if (preConditionEdit == null) {
            preConditionEdit = getPreConditionEdit();
        }
        preConditionEdit.setConditionId(EDIT_CONDITION_ID);
        this.preConditionEdit = preConditionEdit;
    }

    /**
     * org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    protected void calcAccessAndVisibility(final ConditionInterpreter ci, final NodeEvaluation nodeEval) {
        super.calcAccessAndVisibility(ci, nodeEval);
        // evaluate the preconditions
        final boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
        nodeEval.putAccessStatus(EDIT_CONDITION_ID, editor);
    }

}
