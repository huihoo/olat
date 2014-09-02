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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.lms.course.nodes;

import java.util.List;

import org.olat.data.infomessage.InfoMessage;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.infomessage.InfoMessageFrontendManager;
import org.olat.lms.infomessage.InfoMessageFrontendManagerImpl;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.info.InfoCourseNodeConfiguration;
import org.olat.presentation.course.nodes.info.InfoCourseNodeEditController;
import org.olat.presentation.course.nodes.info.InfoPeekViewController;
import org.olat.presentation.course.nodes.info.InfoRunController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Course node for info messages
 * <P>
 * Initial Date: 3 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {

    public static final String TYPE = "info";
    public static final String EDIT_CONDITION_ID = "editinfos";
    public static final String ADMIN_CONDITION_ID = "admininfos";
    private Condition preConditionEdit;
    private Condition preConditionAdmin;

    public InfoCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    @Override
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        final ModuleConfiguration config = getModuleConfiguration();
        if (isNewNode) {
            // use defaults for new course building blocks
            config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, "90");
            config.set(InfoCourseNodeConfiguration.CONFIG_LENGTH, "10");
        }
    }

    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return false;
    }

    @Override
    public RepositoryEntry getReferencedRepositoryEntry() {
        return null;
    }

    @Override
    public StatusDescription isConfigValid() {
        return StatusDescription.NOERROR;
    }

    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        final String translatorStr = PackageUtil.getPackageName(InfoCourseNodeEditController.class);
        final List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
        return oneClickStatusCache;
    }

    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        final InfoCourseNodeEditController childTabCntrllr = new InfoCourseNodeEditController(ureq, wControl, getModuleConfiguration(), this, course, euce);
        final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(), euce,
                childTabCntrllr);
    }

    @Override
    public Controller createPeekViewRunController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        if (ne.isAtLeastOneAccessible()) {
            final InfoPeekViewController ctrl = new InfoPeekViewController(ureq, wControl, userCourseEnv, this);
            return ctrl;
        } else {
            return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
        }
    }

    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final String nodecmd) {

        final InfoRunController infoCtrl = new InfoRunController(ureq, wControl, userCourseEnv, ne, this);
        final Controller titledCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, infoCtrl, this, "o_infomsg_icon");
        return new NodeRunConstructionResult(titledCtrl);
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
     * Default set the write privileges to coaches and admin only
     * 
     * @return
     */
    public Condition getPreConditionAdmin() {
        if (preConditionAdmin == null) {
            preConditionAdmin = new Condition();
            preConditionAdmin.setEasyModeCoachesAndAdmins(true);
            preConditionAdmin.setConditionExpression(preConditionAdmin.getConditionFromEasyModeConfiguration());
            preConditionAdmin.setExpertMode(false);
        }
        preConditionAdmin.setConditionId(ADMIN_CONDITION_ID);
        return preConditionAdmin;
    }

    /**
     * @param preConditionEdit
     */
    public void setPreConditionAdmin(Condition preConditionAdmin) {
        if (preConditionAdmin == null) {
            preConditionAdmin = getPreConditionAdmin();
        }
        preConditionAdmin.setConditionId(ADMIN_CONDITION_ID);
        this.preConditionAdmin = preConditionAdmin;
    }

    @Override
    protected void calcAccessAndVisibility(final ConditionInterpreter ci, final NodeEvaluation nodeEval) {
        // nodeEval.setVisible(true);
        super.calcAccessAndVisibility(ci, nodeEval);

        // evaluate the preconditions
        final boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
        nodeEval.putAccessStatus(EDIT_CONDITION_ID, editor);

        final boolean admin = (getPreConditionAdmin().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionAdmin()));
        nodeEval.putAccessStatus(ADMIN_CONDITION_ID, admin);
    }

    @Override
    /**
     * is called when deleting this node, clean up info-messages and subscriptions!
     */
    public void cleanupOnDelete(ICourse course) {
        // delete infoMessages and subscriptions (OLAT-6171)
        List<InfoMessage> messages = getInfoMessageFrontendManager().loadInfoMessageByResource(course, null, null, null, null, 0, 0);
        InfoMessageFrontendManager infoMessageManager = getInfoMessageFrontendManager();
        for (InfoMessage im : messages) {
            infoMessageManager.deleteInfoMessage(im);
        }

        super.cleanupOnDelete(course);
    }

    private InfoMessageFrontendManager getInfoMessageFrontendManager() {
        return CoreSpringFactory.getBean(InfoMessageFrontendManagerImpl.class);
    }

}
