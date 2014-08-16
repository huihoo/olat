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

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.fileresource.ImsCPFileResource;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.imports.CourseExportEBL;
import org.olat.lms.course.imports.ImportReferencesEBL;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.cp.CPEditController;
import org.olat.presentation.course.nodes.cp.CPRunController;
import org.olat.presentation.course.repository.ImportReferencesController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {
    private static final String TYPE = "cp";

    /**
     * Constructor for a course building block of the type IMS CP learning content
     */
    public CPCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        final CPEditController childTabCntrllr = new CPEditController(this, ureq, wControl, course, euce);
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
        NodeRunConstructionResult ncr;
        updateModuleConfigDefaults(false);
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(ICourse.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
        final CPRunController cprunC = new CPRunController(getModuleConfiguration(), ureq, userCourseEnv, wControl, this, nodecmd, ores);
        ncr = cprunC.createNodeRunConstructionResult(ureq);
        return ncr;
    }

    /**
     * org.olat.lms.course.run.userview.UserCourseEnvironment, org.olat.lms.course.run.userview.NodeEvaluation)
     */
    @Override
    public Controller createPreviewController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne) {
        return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, ne, null).getRunController();
    }

    @Override
    protected String getDefaultTitleOption() {
        return CourseNode.DISPLAY_OPTS_CONTENT;
    }

    /**
	 */
    @Override
    public StatusDescription isConfigValid() {/*
                                               * first check the one click cache
                                               */
        if (oneClickStatusCache != null) {
            return oneClickStatusCache[0];
        }

        StatusDescription sd = StatusDescription.NOERROR;
        if (!isModuleConfigValid()) {
            // FIXME: refine statusdescriptions
            final String shortKey = "error.noreference.short";
            final String longKey = "error.noreference.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(CPEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(CPEditController.PANE_TAB_CPCONFIG);
        }
        return sd;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(ConditionEditController.class);
        final List statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        return StatusDescriptionHelper.sort(statusDescs);
    }

    /**
	 */
    @Override
    public boolean needsReferenceToARepositoryEntry() {
        return true;
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
            config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
            config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
            config.setConfigurationVersion(2);
        } else {
            config.remove(NodeEditController.CONFIG_INTEGRATION);
            if (config.getConfigurationVersion() < 2) {
                // update new configuration options using default values for existing
                // nodes
                config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
                final Boolean componentMenu = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
                if (componentMenu == null) {
                    config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
                }
                config.setConfigurationVersion(2);
            }

            if (config.getConfigurationVersion() < 3) {
                config.set(NodeEditController.CONFIG_CONTENT_ENCODING, NodeEditController.CONFIG_CONTENT_ENCODING_AUTO);
                config.set(NodeEditController.CONFIG_JS_ENCODING, NodeEditController.CONFIG_JS_ENCODING_AUTO);
                config.setConfigurationVersion(3);
            }
            // else node is up-to-date - nothing to do
        }
    }

    /**
	 */
    @Override
    public void exportNode(final File exportDirectory, final ICourse course) {
        getCourseExportEBL().exportNode(exportDirectory, this);
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
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private CourseExportEBL getCourseExportEBL() {
        return CoreSpringFactory.getBean(CourseExportEBL.class);
    }

    private String getResourceType() {
        return ImsCPFileResource.TYPE_NAME;
    }

}
