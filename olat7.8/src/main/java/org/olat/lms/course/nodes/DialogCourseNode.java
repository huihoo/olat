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

package org.olat.lms.course.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.filters.VFSLeafFilter;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.vfs.securitycallbacks.FullAccessCallback;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.dialogelements.DialogElement;
import org.olat.lms.dialogelements.DialogElementsPropertyManager;
import org.olat.lms.dialogelements.DialogPropertyElements;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.archiver.ForumArchiveManager;
import org.olat.lms.forum.archiver.ForumFormatter;
import org.olat.lms.forum.archiver.ForumRTFFormatter;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.dialog.DialogConfigForm;
import org.olat.presentation.course.nodes.dialog.DialogCourseNodeEditController;
import org.olat.presentation.course.nodes.dialog.DialogCourseNodeRunController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: guido Class Description for DialogCourseNode
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author Guido Schnider
 */
public class DialogCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {

    public static final String TYPE = "dialog";
    private Condition preConditionReader, preConditionPoster, preConditionModerator;

    public DialogCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        final DialogCourseNodeEditController childTabCntrllr = new DialogCourseNodeEditController(ureq, wControl, this, course, euce);
        final CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment().getCourseGroupManager(), euce,
                childTabCntrllr);
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation, java.lang.String)
     */
    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv,
            final NodeEvaluation ne, final String nodecmd) {
        // FIXME:gs:a nodecmd has now the subsubId in it -> pass to DialogCourseNodeRunController below
        final DialogCourseNodeRunController ctrl = new DialogCourseNodeRunController(ureq, userCourseEnv, wControl, this, ne);
        final Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, this, "o_dialog_icon");
        return new NodeRunConstructionResult(wrappedCtrl);
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(DialogCourseNodeEditController.class);
        final List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
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
	 */
    @Override
    public StatusDescription isConfigValid() {
        /*
         * first check the one click cache
         */
        if (oneClickStatusCache != null) {
            return oneClickStatusCache[0];
        }

        return StatusDescription.NOERROR;
    }

    /**
     * Update the module configuration to have all mandatory configuration flags set to usefull default values
     * 
     * @param isNewNode
     *            true: an initial configuration is set; false: upgrading from previous node configuration version, set default to maintain previous behaviour
     */
    @Override
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        final ModuleConfiguration config = getModuleConfiguration();
        if (isNewNode) {
            // use defaults for new course building blocks
            // REVIEW:pb version should go to 2 now and the handling for 1er should be to remove
            config.setConfigurationVersion(1);
            config.set(DialogConfigForm.DIALOG_CONFIG_INTEGRATION, DialogConfigForm.CONFIG_INTEGRATION_VALUE_INLINE);
        }
    }

    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        return null;
    }

    /**
     * life cycle of node data e.g properties stuff should be deleted if node gets deleted life cycle: create - delete - migrate
     */
    @Override
    public void cleanupOnDelete(final ICourse course) {
        final DialogElementsPropertyManager depm = DialogElementsPropertyManager.getInstance();

        // remove all possible forum subscriptions
        final DialogPropertyElements findDialogElements = depm.findDialogElements(course.getResourceableId(), getIdent());
        if (findDialogElements != null) {
            final List<DialogElement> dialogElments = findDialogElements.getDialogPropertyElements();
            for (final DialogElement dialogElement : dialogElments) {
                final Long forumKey = dialogElement.getForumKey();
                // also delete forum -> was archived in archiveNodeData step
                getForumService().deleteForum(forumKey);
            }
        }

        // delete property
        depm.deleteProperty(course.getResourceableId(), this.getIdent());

    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean(ForumService.class);

    }

    @Override
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
        boolean dataFound = false;
        final DialogElementsPropertyManager depm = DialogElementsPropertyManager.getInstance();
        final DialogPropertyElements elements = depm.findDialogElements(course.getCourseEnvironment().getCoursePropertyManager(), this);
        List list = new ArrayList();
        if (elements != null) {
            list = elements.getDialogPropertyElements();
        }

        for (final Iterator iter = list.iterator(); iter.hasNext();) {
            final DialogElement element = (DialogElement) iter.next();
            doArchiveElement(element, exportDirectory);
            // at least one element found
            dataFound = true;
        }
        return dataFound;
    }

    /**
     * Archive a single dialog element with files and forum
     * 
     * @param element
     * @param exportDirectory
     */
    public void doArchiveElement(final DialogElement element, final File exportDirectory) {
        final VFSContainer forumContainer = getForumService().getForumContainer(element.getForumKey());
        // there is only one file (leave) in the top forum container
        final VFSItem dialogFile = forumContainer.getItems(new VFSLeafFilter()).get(0);
        final VFSContainer exportContainer = new LocalFolderImpl(exportDirectory);

        // append export timestamp to avoid overwriting previous export
        final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss_SSS");
        final String exportDirName = getShortTitle() + "_" + element.getForumKey() + "_" + formatter.format(new Date(System.currentTimeMillis()));
        final VFSContainer diaNodeElemExportContainer = exportContainer.createChildContainer(exportDirName);
        // don't check quota
        diaNodeElemExportContainer.setLocalSecurityCallback(new FullAccessCallback());
        diaNodeElemExportContainer.copyFrom(dialogFile);

        final ForumArchiveManager fam = ForumArchiveManager.getInstance();
        final ForumFormatter ff = new ForumRTFFormatter(diaNodeElemExportContainer, false);
        fam.applyFormatter(ff, element.getForumKey().longValue(), null);
    }

    @Override
    protected void calcAccessAndVisibility(final ConditionInterpreter ci, final NodeEvaluation nodeEval) {
        // evaluate the preconditions
        final boolean reader = (getPreConditionReader().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionReader()));
        nodeEval.putAccessStatus("reader", reader);
        final boolean poster = (getPreConditionPoster().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionPoster()));
        nodeEval.putAccessStatus("poster", poster);
        final boolean moderator = (getPreConditionModerator().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionModerator()));
        nodeEval.putAccessStatus("moderator", moderator);

        final boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionVisibility()));
        nodeEval.setVisible(visible);
    }

    /**
     * @return Returns the preConditionModerator.
     */
    public Condition getPreConditionModerator() {
        if (this.preConditionModerator == null) {
            this.preConditionModerator = new Condition();
            // learner should not be able to delete files by default
            this.preConditionModerator.setEasyModeCoachesAndAdmins(true);
            this.preConditionModerator.setEasyModeAlwaysAllowCoachesAndAdmins(true);
            this.preConditionModerator.setConditionExpression("(  ( isCourseCoach(0) | isCourseAdministrator(0) ) )");
        }
        this.preConditionModerator.setConditionId("moderator");
        return this.preConditionModerator;
    }

    /**
     * @param preConditionModerator
     *            The preConditionModerator to set.
     */
    public void setPreConditionModerator(Condition preConditionMod) {
        if (preConditionMod == null) {
            preConditionMod = getPreConditionModerator();
        }
        preConditionMod.setConditionId("moderator");
        this.preConditionModerator = preConditionMod;
    }

    /**
     * @return Returns the preConditionPoster.
     */
    public Condition getPreConditionPoster() {
        if (preConditionPoster == null) {
            preConditionPoster = new Condition();
        }
        preConditionPoster.setConditionId("poster");
        return preConditionPoster;
    }

    /**
     * @param preConditionPoster
     *            The preConditionPoster to set.
     */
    public void setPreConditionPoster(Condition preConditionPoster) {
        if (preConditionPoster == null) {
            preConditionPoster = getPreConditionPoster();
        }
        preConditionPoster.setConditionId("poster");
        this.preConditionPoster = preConditionPoster;
    }

    /**
     * @return Returns the preConditionReader.
     */
    public Condition getPreConditionReader() {
        if (preConditionReader == null) {
            preConditionReader = new Condition();
        }
        preConditionReader.setConditionId("reader");
        return preConditionReader;
    }

    /**
     * @param preConditionReader
     *            The preConditionReader to set.
     */
    public void setPreConditionReader(Condition preConditionReader) {
        if (preConditionReader == null) {
            preConditionReader = getPreConditionReader();
        }
        preConditionReader.setConditionId("reader");
        this.preConditionReader = preConditionReader;
    }

}
