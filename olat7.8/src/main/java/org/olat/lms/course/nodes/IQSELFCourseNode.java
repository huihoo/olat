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
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.qti.QTIResultSet;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.imports.CourseExportEBL;
import org.olat.lms.course.imports.ImportReferencesEBL;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.exporter.QTIExportFormatter;
import org.olat.lms.ims.qti.exporter.QTIExportFormatterCSVType2;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.qti.QTIResultService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.iq.IQEditController;
import org.olat.presentation.course.nodes.iq.IQRunController;
import org.olat.presentation.course.nodes.iq.IQUIFactory;
import org.olat.presentation.course.repository.ImportReferencesController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Feb 9, 2004
 * 
 * @author Mike Stock
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQSELFCourseNode extends AbstractAccessableCourseNode implements SelfAssessableCourseNode, UsedByXstream {

    private static final String PACKAGE_IQ = PackageUtil.getPackageName(IQRunController.class);
    private static final String TYPE = "iqself";

    /**
     * Constructor to create a course node of type IMS QTI.
     */
    public IQSELFCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    private QTIResultService getQtiResultService() {
        return CoreSpringFactory.getBean(QTIResultService.class);
    }

    private QtiEBL getQtiEBL() {
        return CoreSpringFactory.getBean(QtiEBL.class);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {

        final TabbableController childTabCntrllr = IQUIFactory.createIQSelftestEditController(ureq, wControl, course, this, course.getCourseEnvironment()
                .getCourseGroupManager(), euce);
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

        final Controller runController = IQUIFactory.createIQSelftestRunController(ureq, wControl, userCourseEnv, ne, this);
        final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runController, this, "o_iqself_icon");

        return new NodeRunConstructionResult(ctrl);
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

        boolean isValid = getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY) != null;
        if (isValid) {
            /*
             * COnfiugre an IQxxx BB with a repo entry, do not publish this BB, mark IQxxx as deleted, remove repo entry, undelete BB IQxxx and bang you enter this if.
             */
            final Object repoEntry = getReferencedRepositoryEntry();
            if (repoEntry == null) {
                isValid = false;
                removeRepositoryReference();
                // FIXME:ms: may be show a refined error message, that the former
                // referenced repo entry is meanwhile deleted.
            }
        }
        StatusDescription sd = StatusDescription.NOERROR;
        if (!isValid) {
            final String shortKey = "error.self.undefined.short";
            final String longKey = "error.self.undefined.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(IQEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(IQEditController.PANE_TAB_IQCONFIG_SELF);
        }
        return sd;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(IQEditController.class);
        final List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        oneClickStatusCache = StatusDescriptionHelper.sort(sds);
        return oneClickStatusCache;
    }

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return true;
    }

    /**
	 */
    @Override
    public String informOnDelete(final Locale locale, final ICourse course) {
        // Check if there are qtiresults for this selftest
        final String repositorySoftKey = (String) getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        QTIResultService qtiResultService = getQtiResultService();
        if (qtiResultService.hasResultSets(course.getResourceableId(), this.getIdent(), repKey)) {
            return new PackageTranslator(PACKAGE_IQ, locale).translate("info.nodedelete");
        }
        return null;
    }

    /**
	 */
    @Override
    public void cleanupOnDelete(final ICourse course) {
        // Delete all qtiresults for this node. No properties used on this node
        final String repositorySoftKey = (String) getModuleConfiguration().get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        final Long repKey = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
        QTIResultService qtiResultService = getQtiResultService();
        qtiResultService.deleteAllResults(course.getResourceableId(), this.getIdent(), repKey);

    }

    /**
     * Override default implementation
     * 
     */
    @Override
    public boolean archiveNodeData(final Locale locale, final ICourse course, final File exportDirectory, final String charset) {
        super.archiveNodeData(locale, course, exportDirectory, charset);

        final QTIExportFormatter formatter = new QTIExportFormatterCSVType2(locale, null, null, "\t", "\"", "\\", "\r\n", false);
        return getQtiEBL()
                .archiveIQTestCourseNode(formatter, getModuleConfiguration(), course.getResourceableId(), getShortTitle(), getIdent(), exportDirectory, charset);

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
        return TestFileResource.TYPE_NAME;
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
            // add default module configuration
            config.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
            config.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
            config.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_SELF);
            config.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED);
        }
    }

    /**
	 */
    @Override
    public ScoreEvaluation getUserScoreEvaluation(final UserCourseEnvironment userCourseEnv) {
        final Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
        final long olatResourceId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().longValue();
        final QTIResultSet qTIResultSet = IQManager.getInstance().getLastResultSet(identity, olatResourceId, this.getIdent());
        if (qTIResultSet != null) {
            final ScoreEvaluation scoreEvaluation = new ScoreEvaluation(new Float(qTIResultSet.getScore()), new Boolean(qTIResultSet.getIsPassed()), new Long(
                    qTIResultSet.getAssessmentID()));
            return scoreEvaluation;
        }
        return null;
    }

    /**
	 */
    public void incrementUserAttempts(final UserCourseEnvironment userCourseEnvironment) {
        final AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
        final Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
        am.incrementNodeAttempts(this, mySelf, userCourseEnvironment);
    }

}
