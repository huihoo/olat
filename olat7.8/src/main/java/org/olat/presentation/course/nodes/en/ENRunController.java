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

package org.olat.presentation.course.nodes.en;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.EnrollStatus;
import org.olat.lms.course.EnrollmentManager;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.course.nodes.ObjectivesHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.group.BusinessGroupTableModelWithMaxSize;
import org.olat.presentation.group.edit.BusinessGroupModifiedEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<BR>
 * Run controller for the entrollment course node
 * <p>
 * Fires BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, and BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT via the event agency (not controller events)
 * <P>
 * Initial Date: Sep 8, 2004
 * 
 * @author Felix Jost, gnaegi
 */
public class ENRunController extends BasicController implements GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CMD_ENROLL_IN_GROUP = "cmd.enroll.in.group";
    private static final String CMD_ENROLLED_CANCEL = "cmd.enrolled.cancel";

    private final ModuleConfiguration moduleConfig;
    private final List<String> enrollableGroupNames, enrollableAreaNames;
    private final VelocityContainer enrollVC;
    private final ENCourseNode enNode;

    private BusinessGroupTableModelWithMaxSize groupListModel;
    private TableController tableCtr;

    // Managers
    private final EnrollmentManager enrollmentManager;
    private final CourseGroupManager courseGroupManager;
    private final CoursePropertyManager coursePropertyManager;

    // workflow variables
    private BusinessGroup enrolledGroup;
    private BusinessGroup waitingListGroup;

    private final boolean cancelEnrollEnabled;

    private OLATResourceable ores;

    /**
     * @param moduleConfiguration
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param enNode
     */
    public ENRunController(final ModuleConfiguration moduleConfiguration, final UserRequest ureq, final WindowControl wControl,
            final UserCourseEnvironment userCourseEnv, final ENCourseNode enNode) {
        super(ureq, wControl);

        this.moduleConfig = moduleConfiguration;
        this.enNode = enNode;
        addLoggingResourceable(LoggingResourceable.wrap(enNode));

        // this.trans = new PackageTranslator(PACKAGE, ureq.getLocale());
        // init managers
        this.enrollmentManager = EnrollmentManager.getInstance();
        this.courseGroupManager = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
        this.coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        this.ores = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();

        // Get groupnames from configuration
        final String groupNamesConfig = (String) moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME);
        final String areaNamesConfig = (String) moduleConfig.get(ENCourseNode.CONFIG_AREANAME);
        this.enrollableGroupNames = splitNames(groupNamesConfig);
        this.enrollableAreaNames = splitNames(areaNamesConfig);
        cancelEnrollEnabled = ((Boolean) moduleConfig.get(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED)).booleanValue();

        final Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
        OLATResourceable ores = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();
        enrolledGroup = enrollmentManager.getBusinessGroupWhereEnrolled(ores, identity, this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
        waitingListGroup = enrollmentManager.getBusinessGroupWhereInWaitingList(ores, identity, this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
        registerGroupChangedEvents(enrollableGroupNames, enrollableAreaNames, courseGroupManager, ureq.getIdentity());
        // Set correct view
        enrollVC = createVelocityContainer("enrollmultiple");
        final List<BusinessGroup> groups = enrollmentManager.loadGroupsFromNames(ores, this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);

        tableCtr = createTableController(ureq, enrollmentManager.hasAnyWaitingList(groups));

        doEnrollView(ureq);

        // push title and learning objectives, only visible on intro page
        enrollVC.contextPut("menuTitle", enNode.getShortTitle());
        enrollVC.contextPut("displayTitle", enNode.getLongTitle());

        // Adding learning objectives
        final String learningObj = enNode.getLearningObjectives();
        if (learningObj != null) {
            final Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
            enrollVC.put("learningObjectives", learningObjectives);
            enrollVC.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator
        }

        putInitialPanel(enrollVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no such event expected
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        final String cmd = event.getCommand();
        if (source == tableCtr) {
            if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                final BusinessGroup choosenGroup = groupListModel.getBusinessGroupAt(rowid);
                addLoggingResourceable(LoggingResourceable.wrap(choosenGroup));

                if (actionid.equals(CMD_ENROLL_IN_GROUP)) {
                    log.debug("CMD_ENROLL_IN_GROUP ureq.getComponentID()=" + ureq.getComponentID() + "  ureq.getComponentTimestamp()=" + ureq.getComponentTimestamp());
                    final EnrollStatus enrollStatus = enrollmentManager.doEnroll(ores, ureq.getIdentity(), choosenGroup, enNode, coursePropertyManager,
                            enrollableGroupNames, enrollableAreaNames, courseGroupManager);

                    if (enrollStatus.isEnrolled()) {
                        enrolledGroup = choosenGroup;
                    } else if (enrollStatus.isInWaitingList()) {
                        waitingListGroup = choosenGroup;
                    } else {
                        getWindowControl().setError(translate(enrollStatus.getErrorMessageKey()));
                    }
                    // events are already fired BusinessGroupManager level ::
                    // BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, choosenGroup, ureq.getIdentity());
                    doEnrollView(ureq);
                    // fire event to indicate runmaincontroller that the menuview is to update
                    fireEvent(ureq, Event.DONE_EVENT);
                } else if (actionid.equals(CMD_ENROLLED_CANCEL)) {
                    if (waitingListGroup != null) {
                        final EnrollStatus enrollStatus = enrollmentManager.doCancelEnrollmentInWaitingList(ureq.getIdentity(), choosenGroup, enNode,
                                coursePropertyManager);

                        waitingListGroup = null;
                    } else {
                        final EnrollStatus enrollStatus = enrollmentManager.doCancelEnrollment(ureq.getIdentity(), choosenGroup, enNode, coursePropertyManager);
                        enrolledGroup = null;
                    }
                    doEnrollView(ureq);
                    if (enrolledGroup == null) {
                        // fire event to indicate runmaincontroller that the menuview is to update
                        fireEvent(ureq, Event.DONE_EVENT);
                    }
                    // events are already fired BusinessGroupManager level ::
                    // BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, ureq.getIdentity());

                }

            }
        }
    }

    @Override
    public void event(final Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            dispose();
        }
        if (event instanceof BusinessGroupModifiedEvent) {
            // TODO implement delay in order to let transaction finish on event generating node
            doEnrollMultipleView(getIdentity());
        }
    }

    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private void doEnrollView(final UserRequest ureq) {
        // TODO read from config: 1) user can choose or 2) round robin
        // for now only case 1
        if (enrolledGroup != null) {
            enrollVC.contextPut("isEnrolledView", Boolean.TRUE);
            enrollVC.contextPut("isWaitingList", Boolean.FALSE);
            final String desc = this.enrolledGroup.getDescription();
            enrollVC.contextPut("groupName", this.enrolledGroup.getName());
            enrollVC.contextPut("groupDesc", (desc == null) ? "" : this.enrolledGroup.getDescription());
        } else if (waitingListGroup != null) {
            enrollVC.contextPut("isEnrolledView", Boolean.TRUE);
            enrollVC.contextPut("isWaitingList", Boolean.TRUE);
            final String desc = this.waitingListGroup.getDescription();
            enrollVC.contextPut("groupName", this.waitingListGroup.getName());
            enrollVC.contextPut("groupDesc", (desc == null) ? "" : this.waitingListGroup.getDescription());
        } else {
            enrollVC.contextPut("isEnrolledView", Boolean.FALSE);
        }
        doEnrollMultipleView(ureq.getIdentity());
    }

    private void doEnrollMultipleView(final Identity identity) {
        // 1. Fetch groups from database
        final List<BusinessGroup> groups = enrollmentManager.loadGroupsFromNames(ores, this.enrollableGroupNames, this.enrollableAreaNames, courseGroupManager);
        final List<Integer> members = this.courseGroupManager.getNumberOfMembersFromGroups(groups);
        // 2. Build group list
        groupListModel = new BusinessGroupTableModelWithMaxSize(groups, members, getTranslator(), identity, cancelEnrollEnabled);
        tableCtr.setTableDataModel(groupListModel);
        tableCtr.modelChanged();
        // 3. Add group list to view
        enrollVC.put("grouplisttable", tableCtr.getInitialComponent());
    }

    private TableController createTableController(final UserRequest ureq, final boolean hasAnyWaitingList) {
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("grouplist.no.groups"));

        removeAsListenerAndDispose(tableCtr);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(tableCtr);

        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.name", 0, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.desc", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.partipiciant", 2, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(hasAnyWaitingList, new DefaultColumnDescriptor("grouplist.table.waitingList", 3, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.state", 4, null, ureq.getLocale()));
        final BooleanColumnDescriptor columnDesc = new BooleanColumnDescriptor("grouplist.table.enroll", 5, CMD_ENROLL_IN_GROUP, translate(CMD_ENROLL_IN_GROUP),
                translate("grouplist.table.no_action"));
        columnDesc.setSortingAllowed(false);
        tableCtr.addColumnDescriptor(columnDesc);
        tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("grouplist.table.cancel_enroll", 6, CMD_ENROLLED_CANCEL, translate(CMD_ENROLLED_CANCEL),
                translate("grouplist.table.no_action")));
        return tableCtr;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        deregisterGroupChangedEvents(enrollableGroupNames, enrollableAreaNames, courseGroupManager);
    }

    /*
     * Add as listener to BusinessGroups so we are being notified about changes.
     */
    private void registerGroupChangedEvents(final List<String> enrollableGroupNames, final List<String> enrollableAreaNames, final CourseGroupManager courseGroupManager,
            final Identity identity) {
        for (final BusinessGroup businessGroup : enrollmentManager.loadGroupsFromNames(ores, enrollableGroupNames, enrollableAreaNames, courseGroupManager)) {
            CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, businessGroup);
        }
    }

    private void deregisterGroupChangedEvents(final List<String> enrollableGroupNames, final List<String> enrollableAreaNames, final CourseGroupManager courseGroupManager) {
        for (final BusinessGroup businessGroup : enrollmentManager.loadGroupsFromNames(ores, enrollableGroupNames, enrollableAreaNames, courseGroupManager)) {
            CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, businessGroup);
        }
    }

    // ////////////////
    // Helper Methods
    // ////////////////
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

}
