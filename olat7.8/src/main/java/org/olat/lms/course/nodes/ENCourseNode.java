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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.properties.PersistingCoursePropertyManager;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.en.ENEditController;
import org.olat.presentation.course.nodes.en.ENRunController;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<BR>
 * Enrollement Course Node: users can enroll in group / groups / areas
 * <P>
 * Initial Date: Sep 8, 2004
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class ENCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {

    /**
     * property name for the initial enrollment date will be set only the first time the users enrolls to this node.
     */
    public static final String PROPERTY_INITIAL_ENROLLMENT_DATE = "initialEnrollmentDate";
    /**
     * property name for the recent enrollemtn date will be changed everytime the user enrolls to this node.
     */
    public static final String PROPERTY_RECENT_ENROLLMENT_DATE = "recentEnrollmentDate";

    private static final String TYPE = "en";

    /**
     * property name for the initial waiting-list date will be set only the first time the users is put into the waiting-list of this node.
     */
    public static final String PROPERTY_INITIAL_WAITINGLIST_DATE = "initialWaitingListDate";
    /**
     * property name for the recent waiting-list date will be changed everytime the user is put into the waiting-list of this node.
     */
    public static final String PROPERTY_RECENT_WAITINGLIST_DATE = "recentWaitingListDate";

    /** CONFIG_GROUPNAME configuration parameter key. */
    public static final String CONFIG_GROUPNAME = "groupname";

    /** CONFIG_AREANAME configuration parameter key. */
    public static final String CONFIG_AREANAME = "areaname";

    /** CONF_CANCEL_ENROLL_ENABLED configuration parameter key. */
    public static final String CONF_CANCEL_ENROLL_ENABLED = "cancel_enroll_enabled";

    private static final int CURRENT_CONFIG_VERSION = 2;

    /**
     * Constructor for enrollment buildig block
     */
    public ENCourseNode() {
        super(TYPE);
        initDefaultConfig();
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        migrateConfig();
        final ENEditController childTabCntrllr = new ENEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
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
        Controller controller;
        migrateConfig();
        // Do not allow guests to enroll to groups
        final Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
            final String title = trans.translate("guestnoaccess.title");
            final String message = trans.translate("guestnoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            controller = new ENRunController(getModuleConfiguration(), ureq, wControl, userCourseEnv, this);
        }
        final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_en_icon");
        return new NodeRunConstructionResult(ctrl);
    }

    @Override
    public boolean isModuleConfigValid() {
        return (getModuleConfiguration().get(CONFIG_GROUPNAME) != null);
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
            // FIXME: refine statusdescriptions
            final String shortKey = "error.nogroupdefined.short";
            final String longKey = "error.nogroupdefined.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(ENEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(ENEditController.PANE_TAB_ENCONFIG);
        }
        return sd;
    }

    /**
	 */
    @Override
    public StatusDescription[] isConfigValid(final CourseEditorEnv cev) {
        // this must be nulled before isConfigValid() is called!!
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        final String translatorStr = PackageUtil.getPackageName(ConditionEditController.class);

        final List condErrs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        final List missingNames = new ArrayList();
        /*
         * check group and area names for existence
         */
        final ModuleConfiguration mc = getModuleConfiguration();
        final String areaStr = (String) mc.get(CONFIG_AREANAME);
        final String nodeId = getIdent();
        if (areaStr != null) {
            final String[] areas = areaStr.split(",");
            for (int i = 0; i < areas.length; i++) {
                final String trimmed = areas[i] != null ? areas[i].trim() : areas[i];
                if (!trimmed.equals("") && !cev.existsArea(trimmed)) {
                    final StatusDescription sd = new StatusDescription(ValidationStatus.WARNING, "error.notfound.name", "solution.checkgroupmanagement", new String[] {
                            "NONE", trimmed }, translatorStr);
                    sd.setDescriptionForUnit(nodeId);
                    missingNames.add(sd);
                }
            }
        }
        final String groupStr = (String) mc.get(CONFIG_GROUPNAME);
        if (groupStr != null) {
            final String[] groups = groupStr.split(",");
            for (int i = 0; i < groups.length; i++) {
                final String trimmed = groups[i] != null ? groups[i].trim() : groups[i];
                if (!trimmed.equals("") && !cev.existsGroup(trimmed)) {
                    final StatusDescription sd = new StatusDescription(ValidationStatus.WARNING, "error.notfound.name", "solution.checkgroupmanagement", new String[] {
                            "NONE", trimmed }, translatorStr);
                    sd.setDescriptionForUnit(nodeId);
                    missingNames.add(sd);
                }
            }
        }
        missingNames.addAll(condErrs);
        /*
         * sort -> Errors > Warnings > Infos and remove NOERRORS, if Error/Warning/Info around.
         */
        oneClickStatusCache = StatusDescriptionHelper.sort(missingNames);
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
    public void cleanupOnDelete(final ICourse course) {
        super.cleanupOnDelete(course);
        final CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
        cpm.deleteNodeProperties(this, PROPERTY_INITIAL_ENROLLMENT_DATE);
        cpm.deleteNodeProperties(this, PROPERTY_RECENT_ENROLLMENT_DATE);
    }

    /**
     * Init config parameter with default values for a new course node.
     */
    private void initDefaultConfig() {
        final ModuleConfiguration config = getModuleConfiguration();
        // defaults
        config.set(CONF_CANCEL_ENROLL_ENABLED, Boolean.TRUE);
        config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
    }

    /**
     * Migrate (add new config parameter/values) config parameter for a existing course node.
     */
    private void migrateConfig() {
        final ModuleConfiguration config = getModuleConfiguration();
        int version = config.getConfigurationVersion();
        if (version < CURRENT_CONFIG_VERSION) {
            // Loaded config is older than current config version => migrate
            if (version == 1) {
                // migrate V1 => V2
                config.set(CONF_CANCEL_ENROLL_ENABLED, Boolean.TRUE);
                version = 2;
            }
            config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
        }
    }

}
