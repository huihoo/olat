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

import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.group.BGConfigFlags;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.group.edit.BusinessGroupEditController;
import org.olat.presentation.group.main.BGMainController;
import org.olat.presentation.group.management.BGManagementController;
import org.olat.presentation.group.run.BusinessGroupMainRunController;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Description: <BR>
 * Factory to create all controllers needed to work with business groups. The methods will configure generic run, edit and management controllers to make the desired
 * featureset available
 * <P>
 * Initial Date: Aug 19, 2004
 * 
 * @author patrick
 */

public class BGControllerFactory {

    private static BGControllerFactory INSTANCE = null;

    static {
        INSTANCE = new BGControllerFactory();
    }

    /**
     * Use getInstance instead
     */
    private BGControllerFactory() {
        //
    }

    /**
     * @return business group controller factory
     */
    public static BGControllerFactory getInstance() {
        return INSTANCE;
    }

    //
    // 1) Group edit controllers
    //

    /**
     * Factory method to create a configured group edit controller
     * 
     * @param ureq
     * @param wControl
     * @param businessGroup
     * @return an edit controller for this busines group
     */
    public BusinessGroupEditController createEditControllerFor(final UserRequest ureq, final WindowControl wControl, final BusinessGroup businessGroup) {
        final String bgTyp = businessGroup.getType();
        if (BusinessGroup.TYPE_BUDDYGROUP.equals(bgTyp)) {
            final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
            return new BusinessGroupEditController(ureq, wControl, businessGroup, flags);
        } else if (BusinessGroup.TYPE_LEARNINGROUP.equals(bgTyp)) {
            final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
            return new BusinessGroupEditController(ureq, wControl, businessGroup, flags);
        } else if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgTyp)) {
            final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
            return new BusinessGroupEditController(ureq, wControl, businessGroup, flags);
        }
        // else
        throw new AssertException("unknown BusinessGroupType::" + bgTyp);
    }

    //
    // 2) Group run controllers
    //

    /**
     * Factory method to create a configured group run controller
     * 
     * @param ureq
     * @param wControl
     * @param businessGroup
     * @param isGMAdmin
     *            true if user is group management administrator
     * @param initialViewIdentifier
     * @return a run controller for this business group
     */
    public BusinessGroupMainRunController createRunControllerFor(final UserRequest ureq, final WindowControl wControl, final BusinessGroup businessGroup,
            final boolean isGMAdmin, final String initialViewIdentifier) {

        // build up the context path
        WindowControl bwControl;
        final OLATResourceable businessOres = businessGroup;
        final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
        // OLAT-5944: check if the current context entry is not already the group entry to avoid duplicate in the business path
        if (ce.equals(wControl.getBusinessControl().getCurrentContextEntry())) {
            bwControl = wControl;
        } else {
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
        }

        final String bgTyp = businessGroup.getType();
        if (BusinessGroup.TYPE_BUDDYGROUP.equals(bgTyp)) {
            final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
            flags.setEnabled(BGConfigFlags.IS_GM_ADMIN, false);
            return new BusinessGroupMainRunController(ureq, bwControl, businessGroup, flags, initialViewIdentifier);
        } else if (BusinessGroup.TYPE_LEARNINGROUP.equals(bgTyp)) {
            final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
            flags.setEnabled(BGConfigFlags.IS_GM_ADMIN, isGMAdmin);
            return new BusinessGroupMainRunController(ureq, bwControl, businessGroup, flags, initialViewIdentifier);
        } else if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgTyp)) {
            final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
            flags.setEnabled(BGConfigFlags.IS_GM_ADMIN, isGMAdmin);
            return new BusinessGroupMainRunController(ureq, bwControl, businessGroup, flags, initialViewIdentifier);
        }
        // else
        throw new AssertException("unknown BusinessGroupType::" + bgTyp);
    }

    /**
     * Creates a runtime environment for this business group as a tab in the top navigation bar
     * 
     * @param businessGroup
     * @param ureq
     * @param wControl
     * @param userActivityLogger
     *            The logger used to log the user activities or null if no logger used
     * @param isGMAdmin
     * @param initialViewIdentifier
     * @return BusinessGroupMainRunController or null if already initialized
     */
    public BusinessGroupMainRunController createRunControllerAsTopNavTab(final BusinessGroup businessGroup, final UserRequest ureq, final WindowControl wControl,
            final boolean isGMAdmin, final String initialViewIdentifier) {
        final String displayName = businessGroup.getName();

        BusinessGroupMainRunController bgMrc = null;

        final OLATResourceable ores = businessGroup;
        final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        bgMrc = this.createRunControllerFor(ureq, dts.getWindowControl(), businessGroup, isGMAdmin, initialViewIdentifier);
        DynamicTabHelper.openResourceTab(ores, ureq, bgMrc, displayName, null);
        return bgMrc;
    }

    //
    // group management controllers
    //

    /**
     * Factory method to create a configured buddy group main controller for the management of the users own buddygroup
     * 
     * @param ureq
     * @param wControl
     * @param initialViewIdentifier
     * @return a configured buddy group main controller
     */
    public BGMainController createBuddyGroupMainController(final UserRequest ureq, final WindowControl wControl, final String initialViewIdentifier) {
        return new BGMainController(ureq, wControl, initialViewIdentifier);
    }

    /**
     * Factory method to create a configured group management controller for learning groups and right groups.
     * 
     * @param ureq
     * @param wControl
     * @param bgContext
     * @param useBackLink
     * @return a business group management controller for this group context
     */
    public BGManagementController createManagementController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext, final boolean useBackLink) {
        if (bgContext == null) {
            throw new AssertException("Group context must not be null");
        }

        if (BusinessGroup.TYPE_LEARNINGROUP.equals(bgContext.getGroupType())) {
            return createLearningGroupManagementController(ureq, wControl, bgContext, useBackLink);
        } else if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgContext.getGroupType())) {
            return createRightGroupManagementController(ureq, wControl, bgContext, useBackLink);
        } else {
            throw new AssertException("Can't handle group type ::" + bgContext.getGroupType());
        }
    }

    /**
     * create Controller for new business group creation
     * 
     * @param ureq
     * @param wControl
     * @param ual
     * @param flags
     * @param bgContext
     * @param groupManager
     * @return
     */
    public NewBGController createNewBGController(final UserRequest ureq, final WindowControl wControl, final boolean minMaxEnabled, final BGContext bgContext) {
        return createNewBGController(ureq, wControl, minMaxEnabled, bgContext, true, null);
    }

    /**
     * create controller for (mass) creation of business groups (bulkmode) with a group name(s) proposition.
     * 
     * @param ureq
     * @param wControl
     * @param ual
     * @param minMaxEnabled
     * @param bgContext
     * @param bulkMode
     * @param csvGroupNames
     * @return
     */
    public NewBGController createNewBGController(final UserRequest ureq, final WindowControl wControl, final boolean minMaxEnabled, final BGContext bgContext,
            final boolean bulkMode, final String csvGroupNames) {
        if (bgContext == null) {
            throw new AssertException("Group context must not be null");
        }
        final NewBGController retVal = new NewBGController(ureq, wControl, minMaxEnabled, bgContext, bulkMode, csvGroupNames);
        return retVal;
    }

    private BGManagementController createLearningGroupManagementController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext,
            final boolean useBackLink) {
        // controller configuration
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
        flags.setEnabled(BGConfigFlags.BACK_SWITCH, useBackLink);
        return new BGManagementController(ureq, wControl, bgContext, flags);
    }

    private BGManagementController createRightGroupManagementController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext,
            final boolean useBackLink) {
        final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
        flags.setEnabled(BGConfigFlags.BACK_SWITCH, useBackLink);
        return new BGManagementController(ureq, wControl, bgContext, flags);
    }

    /**
     * a new area creation controller
     * 
     * @param ureq
     * @param wControl
     * @param ual
     * @param bgContext
     * @return
     */
    public NewAreaController createNewAreaController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext) {
        return createNewAreaController(ureq, wControl, bgContext, true, null);
    }

    /**
     * a new area creation controller in bulkmode
     * 
     * @param ureq
     * @param wControl
     * @param ual
     * @param bgContext
     * @param bulkMode
     * @param csvNames
     * @return
     */
    public NewAreaController createNewAreaController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext, final boolean bulkMode,
            final String csvNames) {
        if (bgContext == null) {
            throw new AssertException("Group context must not be null");
        }
        final NewAreaController nac = new NewAreaController(ureq, wControl, bgContext, bulkMode, csvNames);
        return nac;
    }

}
