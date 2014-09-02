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
package org.olat.presentation.group;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Shows the group form until a a group could be created or the form is cancelled. A group can not be created if its name already exists in the given context. This will
 * show an error on the form.<br>
 * Sends {@link Event#DONE_EVENT} in the case of successfully group creation and {@link Event#CANCELLED_EVENT} if the user no longer wishes to create a group.
 * <P>
 * Initial Date: 28.06.2007 <br>
 * 
 * @author patrickb
 */
public class NewBGController extends BasicController {

    private final BGContext bgContext;
    private BusinessGroupService businessGroupService;
    private final VelocityContainer contentVC;
    private final BusinessGroupFormController groupCreateController;
    private boolean bulkMode = false;
    private Set<BusinessGroup> newGroups;
    private boolean minMaxEnabled = false;

    /**
     * @param ureq
     * @param wControl
     * @param minMaxEnabled
     * @param bgContext
     * @param bulkMode
     */
    NewBGController(final UserRequest ureq, final WindowControl wControl, final boolean minMaxEnabled, final BGContext bgContext) {
        this(ureq, wControl, minMaxEnabled, bgContext, true, null);
    }

    /**
     * @param ureq
     * @param wControl
     * @param minMaxEnabled
     * @param bgContext
     * @param bulkMode
     * @param csvGroupNames
     */
    NewBGController(final UserRequest ureq, final WindowControl wControl, final boolean minMaxEnabled, final BGContext bgContext, final boolean bulkMode,
            final String csvGroupNames) {
        super(ureq, wControl);

        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);

        this.bgContext = bgContext;
        this.minMaxEnabled = minMaxEnabled;
        this.bulkMode = bulkMode;
        //
        this.contentVC = this.createVelocityContainer("bgform");
        this.contentVC.contextPut("bulkMode", bulkMode ? Boolean.TRUE : Boolean.FALSE);

        this.groupCreateController = new BusinessGroupFormController(ureq, wControl, null, minMaxEnabled, bulkMode);
        listenTo(this.groupCreateController);
        this.contentVC.put("groupForm", this.groupCreateController.getInitialComponent());
        if (csvGroupNames != null) {
            this.groupCreateController.setGroupName(csvGroupNames);
        }
        this.putInitialPanel(contentVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose.
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.groupCreateController) {
            if (event == Event.DONE_EVENT) {
                final String bgDesc = this.groupCreateController.getGroupDescription();
                final Integer bgMax = this.groupCreateController.getGroupMax();
                final Integer bgMin = this.groupCreateController.getGroupMin();
                final Boolean enableWaitingList = this.groupCreateController.isWaitingListEnabled();
                final Boolean enableAutoCloseRanks = this.groupCreateController.isAutoCloseRanksEnabled();

                Set<String> allNames = new HashSet<String>();
                if (this.bulkMode) {
                    allNames = this.groupCreateController.getGroupNames();
                } else {
                    allNames.add(this.groupCreateController.getGroupName());
                }

                this.newGroups = this.businessGroupService.createUniqueBusinessGroupsFor(allNames, this.bgContext, bgDesc, bgMin, bgMax, enableWaitingList,
                        enableAutoCloseRanks);
                if (this.newGroups != null) {
                    for (final Iterator<BusinessGroup> iter = this.newGroups.iterator(); iter.hasNext();) {
                        final BusinessGroup bg = iter.next();
                        final LoggingResourceable resourceInfo = LoggingResourceable.wrap(bg);
                        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CREATED, getClass(), resourceInfo);
                    }
                    // workflow successfully finished
                    // so far no events on the systembus to inform about new groups in BGContext
                    fireEvent(ureq, Event.DONE_EVENT);
                } else {
                    // Could not create any group, because one or groups-name already exist. Set error of non existing name
                    this.groupCreateController.setGroupNameExistsError(null);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                // workflow cancelled
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do here.
    }

    /**
     * if Event.DONE_EVENT received the return value is always NOT NULL. If Event_FORM_CANCELLED ist received this will be null.
     * 
     * @return
     */
    public BusinessGroup getCreatedGroup() {
        return newGroups.iterator().next();
    }

    /**
     * Returns the new business groups.
     * 
     * @return the new groups.
     */
    public Set<BusinessGroup> getCreatedGroups() {
        return newGroups;
    }

    /**
     * Returns the names of the new business groups.
     * 
     * @return the new group names.
     */
    public Set<String> getCreatedGroupNames() {
        final Set<String> groupNames = new HashSet<String>();
        for (final Iterator<BusinessGroup> iterator = this.newGroups.iterator(); iterator.hasNext();) {
            groupNames.add(iterator.next().getName());
        }
        return groupNames;
    }

    /**
     * if bulkmode is on or not
     * 
     * @return
     */
    public boolean isBulkMode() {
        return bulkMode;
    }

}
