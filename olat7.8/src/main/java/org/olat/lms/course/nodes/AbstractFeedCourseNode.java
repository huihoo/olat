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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.lms.course.nodes;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.imports.ImportReferencesEBL;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.webfeed.FeedManager;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.repository.ImportReferencesController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;

/**
 * The podcast course node.
 * <P>
 * Initial Date: Mar 30, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class AbstractFeedCourseNode extends GenericCourseNode {

    protected ModuleConfiguration config;
    protected Condition preConditionReader, preConditionPoster, preConditionModerator;

    /**
     * @param type
     */
    public AbstractFeedCourseNode(final String type) {
        super(type);
        updateModuleConfigDefaults(true);
    }

    /**
	 */
    @Override
    public void updateModuleConfigDefaults(final boolean isNewNode) {
        this.config = getModuleConfiguration();
        if (isNewNode) {
            // No startpage
            config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
            config.setConfigurationVersion(1);
            // restrict moderator access to course admins and owners
            preConditionModerator = getPreConditionModerator();
            preConditionModerator.setEasyModeCoachesAndAdmins(true);
            preConditionModerator.setConditionExpression(preConditionModerator.getConditionFromEasyModeConfiguration());
            preConditionModerator.setExpertMode(false);
            // restrict poster access to course admins and owners
            preConditionPoster = getPreConditionPoster();
            preConditionPoster.setEasyModeCoachesAndAdmins(true);
            preConditionPoster.setConditionExpression(preConditionPoster.getConditionFromEasyModeConfiguration());
            preConditionPoster.setExpertMode(false);
        }
    }

    /**
     * org.olat.lms.course.ICourse, org.olat.lms.course.run.userview.UserCourseEnvironment)
     */
    @Override
    public abstract TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce);

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation, java.lang.String)
     */
    @Override
    public abstract NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl control, UserCourseEnvironment userCourseEnv,
            NodeEvaluation ne, String nodecmd);

    /**
	 */
    @Override
    public abstract StatusDescription[] isConfigValid(CourseEditorEnv cev);

    /**
	 */
    @Override
    public RepositoryEntry getReferencedRepositoryEntry() {
        this.config = getModuleConfiguration();
        final String repoSoftkey = (String) config.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF);
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
        return entry;
    }

    /**
	 */
    @Override
    public abstract StatusDescription isConfigValid();

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return true;
    }

    /**
     * @return Returns the preConditionModerator.
     */
    public Condition getPreConditionModerator() {
        if (preConditionModerator == null) {
            preConditionModerator = new Condition();
        }
        preConditionModerator.setConditionId("moderator");
        return preConditionModerator;
    }

    /**
     * @param preConditionModerator
     *            The preConditionModerator to set.
     */
    public void setPreConditionModerator(Condition preConditionModerator) {
        if (preConditionModerator == null) {
            preConditionModerator = getPreConditionModerator();
        }
        preConditionModerator.setConditionId("moderator");
        this.preConditionModerator = preConditionModerator;
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

    /**
     * org.olat.lms.course.run.userview.NodeEvaluation)
     */
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
	 */
    @Override
    public void exportNode(final File exportDirectory, final ICourse course) {
        final RepositoryEntry re = getReferencedRepositoryEntry();
        if (re == null) {
            return;
        }
        // build current export ZIP for feed learning resource
        FeedManager.getInstance().getFeedArchive(re.getOlatResource());
        // trigger resource file export
        final File fExportDirectory = new File(exportDirectory, getIdent());
        fExportDirectory.mkdirs();
        final RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
        reie.exportDoExport();
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    public Controller importNode(final File importDirectory, final ICourse course, final boolean unattendedImport, final UserRequest ureq, final WindowControl wControl,
            final String resourceType) {
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
            return new ImportReferencesController(ureq, wControl, this, resourceType, rie);
        }
    }

    private ImportReferencesEBL getImportReferencesEBL() {
        return CoreSpringFactory.getBean(ImportReferencesEBL.class);
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
	 */
    public void archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset, final String type) {
        final VFSContainer exportContainer = new LocalFolderImpl(exportDirectory);
        VFSContainer exportDir = (VFSContainer) exportContainer.resolve(type);
        if (exportDir == null) {
            exportDir = exportContainer.createChildContainer(type);
        }
        final String exportDirName = getShortTitle() + "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
        final VFSContainer destination = exportDir.createChildContainer(exportDirName);
        final String repoRef = (String) getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_REF);
        if (repoRef != null) {
            final OLATResourceable ores = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repoRef, true).getOlatResource();

            final VFSContainer container = FeedManager.getInstance().getFeedContainer(ores);
            if (container != null) {
                final VFSLeaf archive = FeedManager.getInstance().getFeedArchive(ores);
                destination.copyFrom(archive);
            }
            // FIXME:FG:6.3 Archive user comments as soon as implemented.
        }
    }
}
