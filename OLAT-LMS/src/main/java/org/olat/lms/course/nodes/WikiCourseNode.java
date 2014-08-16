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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.imports.CourseExportEBL;
import org.olat.lms.course.imports.ImportReferencesEBL;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiToZipUtils;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.wiki.WikiEditController;
import org.olat.presentation.course.nodes.wiki.WikiRunController;
import org.olat.presentation.course.repository.ImportReferencesController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class WikiCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String TYPE = "wiki";
    private Condition preConditionEdit;

    /**
     * Default constructor for course node of type single page
     */
    public WikiCourseNode() {
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
        }
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        final WikiEditController childTabCntrllr = new WikiEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
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
        if (ne.isCapabilityAccessible("access")) {
            final WikiRunController wikiController = new WikiRunController(wControl, ureq, this, userCourseEnv.getCourseEnvironment(), ne);
            return new NodeRunConstructionResult(wikiController);
        }
        final Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, null, this.getNoAccessExplanation());
        return new NodeRunConstructionResult(controller, null, null, null);
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

        StatusDescription sd = StatusDescription.NOERROR;
        if (!isModuleConfigValid()) {
            final String shortKey = "error.noreference.short";
            final String longKey = "error.noreference.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(WikiEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(WikiEditController.PANE_TAB_WIKICONFIG);
        }
        return sd;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition error messages
        final String translatorStr = PackageUtil.getPackageName(WikiEditController.class);
        final List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        // wiki is a repo entry
        return true;
    }

    @Override
    public void exportNode(final File exportDirectory, final ICourse course) {
        getCourseExportEBL().exportNode(exportDirectory, this);
    }

    private CourseExportEBL getCourseExportEBL() {
        return CoreSpringFactory.getBean(CourseExportEBL.class);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl) {
        final File importSubdir = new File(importDirectory, getIdent());
        final RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importSubdir);
        if (!rie.anyExportedPropertiesAvailable()) {
            return null;
        }

        // do import referenced repository entries
        if (unattendedImport) {
            final Identity admin = getBaseSecurity().findIdentityByName("administrator");
            getImportReferencesEBL().doImport(rie, this, true, admin);
            return null;
        } else {
            return new ImportReferencesController(ureq, wControl, this, getResourceType(), rie);
        }
    }

    private ImportReferencesEBL getImportReferencesEBL() {
        return CoreSpringFactory.getBean(ImportReferencesEBL.class);
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private String getResourceType() {
        return WikiResource.TYPE_NAME;
    }

    /**
	 */
    @Override
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
        final String repoRef = (String) this.getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF);
        final OLATResourceable ores = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repoRef, true).getOlatResource();

        if (WikiManager.getInstance().getOrLoadWiki(ores).getAllPagesWithContent().size() > 0) {
            // OK, there is somthing to archive
            final VFSContainer exportContainer = new LocalFolderImpl(exportDirectory);
            VFSContainer wikiExportContainer = (VFSContainer) exportContainer.resolve(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
            if (wikiExportContainer == null) {
                wikiExportContainer = exportContainer.createChildContainer(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
            }
            final String exportDirName = getShortTitle() + "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
            final VFSContainer destination = wikiExportContainer.createChildContainer(exportDirName);
            if (destination == null) {
                log.error("archiveNodeData: Could not create destination directory: wikiExportContainer=" + wikiExportContainer + exportDirName);
            }

            final VFSContainer container = WikiManager.getInstance().getWikiContainer(ores, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
            if (container != null) { // the container could be null if the wiki is an old empty one - so nothing to archive
                final VFSContainer parent = container.getParentContainer();
                final VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(parent);
                destination.copyFrom(wikiZip);
            }
            return true;
        }
        // empty wiki, no need to archive
        return false;
    }

    /**
     * @return
     */
    public Condition getPreConditionEdit() {
        if (preConditionEdit == null) {
            preConditionEdit = new Condition();
        }
        preConditionEdit.setConditionId("editarticle");
        return preConditionEdit;
    }

    /**
     * @param preConditionEdit
     */
    public void setPreConditionEdit(Condition preConditionEdit) {
        if (preConditionEdit == null) {
            preConditionEdit = getPreConditionEdit();
        }
        preConditionEdit.setConditionId("editarticle");
        this.preConditionEdit = preConditionEdit;
    }

    /**
     * The access condition for wiki is composed of 2 dimensions: readonly (or access) and read&write (or editarticle). <br/>
     * If the access is readonly, the read&write dimension is no more relevant.<br/>
     * If the access is not readonly, read&write condition should be evaluated. <br/>
     * 
     * org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    protected void calcAccessAndVisibility(final ConditionInterpreter ci, final NodeEvaluation nodeEval) {
        super.calcAccessAndVisibility(ci, nodeEval);

        final boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
        nodeEval.putAccessStatus("editarticle", editor);
    }

    @Override
    public void cleanupOnDelete(final ICourse course) {
        // mark the subscription to this node as deleted
        final SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(course.getCourseEnvironment(), this);
        getNotificationService().delete(subsContext);

    }

    private NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

}
