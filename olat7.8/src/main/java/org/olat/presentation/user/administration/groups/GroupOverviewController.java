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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.presentation.user.administration.groups;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupMembershipParameter;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.BusinessGroupTableModel;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * GroupOverviewController creates a model and displays a table with all groups a user is in. The following rows are shown: type of group, groupname, role of user in
 * group (participant, owner, on waiting list), date of joining the group
 * <P>
 * Initial Date: 22.09.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupOverviewController extends BasicController {
    private final VelocityContainer vc;
    private TableController tblCtr;
    private GroupOverviewModel tableDataModel;
    private final WindowControl wControl;
    private final Identity identity;
    private static String TABLE_ACTION_LAUNCH;
    private BusinessGroupService businessGroupService;

    public GroupOverviewController(final UserRequest ureq, final WindowControl control, final Identity identity, final Boolean canStartGroups) {
        super(ureq, control, PackageUtil.createPackageTranslator(BusinessGroupTableModel.class, ureq.getLocale()));
        this.wControl = control;
        this.identity = identity;
        if (canStartGroups) {
            TABLE_ACTION_LAUNCH = "bgTblLaunch";
        } else {
            TABLE_ACTION_LAUNCH = null;
        }
        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        vc = createVelocityContainer("groupoverview");
        buildTableController(ureq, control);
        vc.put("table.groups", tblCtr.getInitialComponent());
        putInitialPanel(vc);
    }

    /**
     * @param ureq
     * @param control
     * @param identity
     * @return
     */
    private void buildTableController(final UserRequest ureq, final WindowControl control) {

        removeAsListenerAndDispose(tblCtr);
        tblCtr = new TableController(null, ureq, control, getTranslator());
        listenTo(tblCtr);

        tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.type", 0, null, ureq.getLocale()));
        tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.course.name", 1, null, ureq.getLocale()));
        tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.name", 2, TABLE_ACTION_LAUNCH, ureq.getLocale()));
        tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.role", 3, null, ureq.getLocale()));
        tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.joindate", 4, null, ureq.getLocale()));

        // build data model
        final List<Object[]> userGroups = getBusinessGroupsModel();
        tableDataModel = new GroupOverviewModel(userGroups, 5);
        tblCtr.setTableDataModel(tableDataModel);
    }

    private List<Object[]> getBusinessGroupsModel() {
        final List<Object[]> userGroups = new ArrayList<Object[]>();

        List<GroupMembershipParameter> groupMembershipParameters = getBusinessGroupEBL().getBusinessGroupMembership(identity);
        for (final GroupMembershipParameter groupMembershipParameter : groupMembershipParameters) {
            final Object[] groupEntry = new Object[5];
            groupEntry[0] = translate(groupMembershipParameter.getGroupType());

            final BusinessGroup businessGroup = groupMembershipParameter.getBusinessGroup();
            groupEntry[1] = (businessGroup.getType().equalsIgnoreCase(BusinessGroup.TYPE_LEARNINGROUP)) ? getResourceDisplayName(businessGroup) : StringUtils.EMPTY;

            groupEntry[2] = groupMembershipParameter.getBusinessGroup();

            if (groupMembershipParameter.getRoleTranslationArgument() != null) {
                groupEntry[3] = translate(groupMembershipParameter.getUserRole(), groupMembershipParameter.getRoleTranslationArgument());
            } else {
                groupEntry[3] = translate(groupMembershipParameter.getUserRole());
            }
            groupEntry[4] = groupMembershipParameter.getJoinDate();

            userGroups.add(groupEntry);
        }
        return userGroups;
    }

    private String getResourceDisplayName(BusinessGroup businessGroup) {
        final List<RepositoryEntry> repoTableModelEntries = getBgContextService().findRepositoryEntriesForBGContext(businessGroup.getGroupContext());
        return (!repoTableModelEntries.isEmpty()) ? repoTableModelEntries.get(0).getDisplayname() : StringUtils.EMPTY;
    }

    /**
     * @return
     */
    private BusinessGroupEBL getBusinessGroupEBL() {
        return CoreSpringFactory.getBean(BusinessGroupEBL.class);
    }

    private BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to catch
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        super.event(ureq, source, event);
        if (source == tblCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                BusinessGroup currBusinessGroup = tableDataModel.getBusinessGroupAtRow(rowid);
                if (actionid.equals(TABLE_ACTION_LAUNCH)) {
                    currBusinessGroup = businessGroupService.loadBusinessGroup(currBusinessGroup.getKey(), false);
                    if (currBusinessGroup == null) {
                        // group seems to be removed meanwhile, reload table and show error
                        showError("group.removed");
                        buildTableController(ureq, wControl);
                        vc.put("table.groups", tblCtr.getInitialComponent());
                    } else {
                        BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), true, null);
                    }
                }
            }
        }
    }

}
