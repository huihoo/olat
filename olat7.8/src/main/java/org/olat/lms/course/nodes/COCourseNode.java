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

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.UsedByXstream;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.editor.StatusDescription;
import org.olat.presentation.course.nodes.TitledWrapperHelper;
import org.olat.presentation.course.nodes.co.COEditController;
import org.olat.presentation.course.nodes.co.CORunController;
import org.olat.presentation.course.nodes.co.ContactRunUIModel;
import org.olat.presentation.course.nodes.co.ContactRunView;
import org.olat.presentation.course.nodes.co.CourseContactMessageUIModel;
import org.olat.presentation.course.run.navigation.NodeRunConstructionResult;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<BR/>
 * Course node of type contact form. Can be used to display an email form that has a preconfigured email address.
 * <P/>
 * Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 */
public class COCourseNode extends AbstractAccessableCourseNode implements UsedByXstream {

    private static final String TYPE = "co";

    /**
     * Default constructor for course node of type single page
     */
    public COCourseNode() {
        super(TYPE);
    }

    /**
	 */
    @Override
    public TabbableController createEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final UserCourseEnvironment euce) {
        final COEditController childTabCntrllr = new COEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
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
        // Do not allow guests to send anonymous emails
        final Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.COURSE_NODES_, ureq.getLocale());
            final String title = trans.translate("guestnoaccess.title");
            final String message = trans.translate("guestnoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {

            ContactRunView contactRunView = createRunView(ureq, wControl);

            CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
            OLATResourceable courseOLATResourceable = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();
            ContactRunUIModel contactRunUIModel = createUIModelFromModuleConfiguration(cgm, courseOLATResourceable, contactRunView.getTranslator(), ureq.getIdentity());
            controller = new CORunController(contactRunView, contactRunUIModel);

        }
        final Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_co_icon");
        return new NodeRunConstructionResult(ctrl);
    }

    private ContactRunUIModel createUIModelFromModuleConfiguration(CourseGroupManager cgm, OLATResourceable courseOLATResourceable, Translator translator,
            Identity identity) {

        // XmlWebApplicationContext context = CoreSpringFactory.getContext();
        // ContactRunUIModel contactRunUIModel = context.getBean(ContactRunUIModel.class);

        ModuleConfiguration moduleConfiguration = getModuleConfiguration();

        final List<String> emailListConfig = (List<String>) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
        final String mSubject = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
        final String mBody = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);

        Boolean partipsConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS);
        Boolean coachesConfigured = moduleConfiguration.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES);

        String grpNames = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
        List<String> grpList = splitNames(grpNames);
        String areas = (String) moduleConfiguration.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
        List<String> areaList = splitNames(areas);

        CourseContactMessageUIModel courseContactMessageUIModel = new CourseContactMessageUIModel(mSubject, mBody, emailListConfig, grpList, areaList,
                courseOLATResourceable, cgm, partipsConfigured, coachesConfigured, translator);

        ContactRunUIModel contactRunUIModel = new ContactRunUIModel(getShortTitle(), getLongTitle(), getLearningObjectives(), identity, courseContactMessageUIModel);

        return contactRunUIModel;
    }

    private ContactRunView createRunView(UserRequest ureq, WindowControl wControl) {
        ContactRunView contactRunView = new ContactRunView(ureq, wControl, "run");
        return contactRunView;
    }

    private List<String> splitNames(final String namesList) {
        final List<String> names = new ArrayList<String>();
        if (namesList != null) {
            final String[] name = namesList.split(",");
            for (int i = 0; i < name.length; i++) {
                names.add(name[i].trim());
            }
        }
        return names;
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

        /**
         * configuration is valid if the provided e-mail container result in at list one recipient e-mail adress. Hence we have always to perform the very expensive
         * operation to fetch the e-mail adresses for tutors, participants, group and area members. simple config here!
         */
        final List emailList = (List) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
        boolean isValid = (emailList != null && emailList.size() > 0);
        final Boolean email2coaches = getModuleConfiguration().getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES);
        final Boolean email2partips = getModuleConfiguration().getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS);
        isValid = isValid || (email2coaches != null && email2coaches.booleanValue());
        isValid = isValid || (email2partips != null && email2partips.booleanValue());
        final String email2Areas = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOAREAS);
        isValid = isValid || (!"".equals(email2Areas) && email2Areas != null);
        final String email2Groups = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
        isValid = isValid || (!"".equals(email2Groups) && email2Groups != null);
        //
        StatusDescription sd = StatusDescription.NOERROR;
        if (!isValid) {
            final String shortKey = "error.norecipients.short";
            final String longKey = "error.norecipients.long";
            final String[] params = new String[] { this.getShortTitle() };
            final String translPackage = PackageUtil.getPackageName(COEditController.class);
            sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translPackage);
            sd.setDescriptionForUnit(getIdent());
            // set which pane is affected by error
            sd.setActivateableViewIdentifier(COEditController.PANE_TAB_COCONFIG);
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
        final String translatorStr = PackageUtil.getPackageName(ConditionEditController.class);
        final List condErrs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        final List missingNames = new ArrayList();
        /*
         * check group and area names for existence
         */
        final ModuleConfiguration mc = getModuleConfiguration();
        final String areaStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
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
        final String groupStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
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

}
