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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupService;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Extended business group table model with max size and current number of participants as additional rows
 * <P>
 * Initial Date: Sep 9, 2004
 * 
 * @author gnaegi
 */
public class BusinessGroupTableModelWithMaxSize extends DefaultTableDataModel<BusinessGroup> {

    private static final Logger log = LoggerHelper.getLogger();

    private static final int COLUMN_COUNT = 7;
    private final List<Integer> members;
    private final Translator trans;
    private final Identity identity;
    private final boolean cancelEnrollEnabled;
    private BusinessGroupService businessGroupService;

    /**
     * @param groups
     *            List of business groups
     * @param members
     *            List containing the number of participants for each group. The index of the list corresponds with the index of the group list
     * @param trans
     */
    public BusinessGroupTableModelWithMaxSize(final List<BusinessGroup> groups, final List<Integer> members, final Translator trans, final Identity identity,
            final boolean cancelEnrollEnabled) {
        super(groups);
        this.members = members;
        this.trans = trans;
        this.identity = identity;

        this.businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        this.cancelEnrollEnabled = cancelEnrollEnabled;
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final BusinessGroup businessGroup = objects.get(row);
        final Integer numbParts = members.get(row);
        final Integer max = businessGroup.getMaxParticipants();
        switch (col) {
        case 0:
            return businessGroup.getName();
        case 1:
            String description = businessGroup.getDescription();
            description = FilterFactory.getHtmlTagsFilter().filter(description);
            description = Formatter.truncate(description, 256);
            return description;
        case 2:
            // Belegt/Plätze
            if (max == null) {
                // no limit => return only members
                return numbParts;
            }
            // return format 2/10
            final StringBuilder buf = new StringBuilder();
            buf.append(numbParts);
            buf.append(trans.translate("grouplist.table.partipiciant.delimiter"));
            buf.append(businessGroup.getMaxParticipants());
            if (numbParts > businessGroup.getMaxParticipants()) {
                log.info("Group overflow detected for the group: " + businessGroup + ", participants: " + numbParts + " maxParticipamts: "
                        + businessGroup.getMaxParticipants());
            }
            return buf.toString();
        case 3:
            // Waiting-list
            if (businessGroup.getWaitingListEnabled().booleanValue()) {
                // Waitinglist is enabled => show current size
                final int intValue = getBusinessGroupEBL().countWaiting(businessGroup);
                return new Integer(intValue);
            }
            return trans.translate("grouplist.table.noWaitingList");
        case 4:
            // Status
            if (getBusinessGroupEBL().isParticipant(this.identity, businessGroup)) {
                return trans.translate("grouplist.table.state.onPartipiciantList");
            } else if (getBusinessGroupEBL().isWaiting(this.identity, businessGroup)) {
                final int pos = businessGroupService.getPositionInWaitingListFor(identity, businessGroup);
                final String[] onWaitingListArgs = new String[] { Integer.toString(pos) };
                return trans.translate("grouplist.table.state.onWaitingList", onWaitingListArgs);
            } else if (max != null && !businessGroup.getWaitingListEnabled().booleanValue() && (numbParts.intValue() >= max.intValue())) {
                return trans.translate("grouplist.table.state.enroll.full");
            } else if (max != null && businessGroup.getWaitingListEnabled().booleanValue() && (numbParts.intValue() >= max.intValue())) {
                return trans.translate("grouplist.table.state.WaitingList");
            }
            return trans.translate("grouplist.table.state.notEnrolled");
        case 5:
            // Action enroll
            if (isEnrolledInAnyGroup(identity)) {
                // Allready enrolled => does not show action-link 'enroll'
                return Boolean.FALSE;
            }
            if (max != null && !businessGroup.getWaitingListEnabled().booleanValue() && (numbParts.intValue() >= max.intValue())) {
                // group is full => => does not show action-link 'enroll'
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        case 6:
            // Action cancel enrollment
            if (getBusinessGroupEBL().isEnrolledIn(businessGroup, identity)) {
                // check if user is on waiting-list
                if (getBusinessGroupEBL().isWaiting(this.identity, businessGroup)) {
                    // user is on waitinglist => show allways action cancelEnrollment for waitinglist
                    return Boolean.TRUE;
                }
                // user is not on waitinglist => show action cancelEnrollment only if enabled
                if (cancelEnrollEnabled) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        default:
            return "ERROR";
        }
    }

    /**
     * @return
     */
    private BusinessGroupEBL getBusinessGroupEBL() {
        return CoreSpringFactory.getBean(BusinessGroupEBL.class);
    }

    /**
     * @param row
     * @return the business group at the given row
     */
    public BusinessGroup getBusinessGroupAt(final int row) {
        return objects.get(row);
    }

    /**
     * Check if an identity is in any security-group.
     * 
     * @param identity
     * @return true: Found identity in any security-group of this table model.
     */
    private boolean isEnrolledInAnyGroup(final Identity identity) {
        for (final BusinessGroup businessGroup : objects) {
            if (getBusinessGroupEBL().isEnrolledIn(businessGroup, identity)) {
                return true;
            }
        }
        return false;
    }

}
