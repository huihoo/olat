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

package org.olat.lms.group;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.BusinessGroupDao;
import org.olat.data.group.BusinessGroupImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * creates new business groups, do not use directly but use service instead
 * 
 * @author gnaegi
 */

class BusinessGroupCreateHelper {

    // vis setters as non spring managed
    private BaseSecurity baseSecurity;
    private BusinessGroupDao businessGroupManager;
    private OLATResourceManager olatResourceManager;
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * No constructor available
     */
    protected BusinessGroupCreateHelper() {
        // no constructor needed, all methods are static
    }

    /**
     * setter based dependency injection
     * 
     * @param baseSecurity
     */
    public void setBaseSecurity(BaseSecurity baseSecurity) {
        this.baseSecurity = baseSecurity;
    }

    public void setBusinessGroupManager(BusinessGroupDao businessGroupManager) {
        this.businessGroupManager = businessGroupManager;
    }

    public void setOlatResourceManager(OLATResourceManager olatResourceManager) {
        this.olatResourceManager = olatResourceManager;
    }

    /**
     * Factory method to create new business groups
     * 
     * @param type
     *            The business group type
     * @param identity
     *            The identity that will be an initial owner or participant of the group (depends on type). Can be null (depends on type)
     * @param name
     *            The group name
     * @param description
     *            The group description
     * @param minParticipants
     *            The minimal number of participants (only declarative)
     * @param maxParticipants
     *            The maximal number of participants
     * @param groupContext
     *            The group context or null
     * @return The newly created group or null if this groupname is already taken by another group in the given context.
     */
    protected BusinessGroup createAndPersistBusinessGroup(final String type, final Identity identity, final String name, final String description,
            final Integer minParticipants, final Integer maxParticipants, final Boolean waitingListEnabled, final Boolean autoCloseRanksEnabled,
            final BGContext groupContext) {
        if (BusinessGroup.TYPE_BUDDYGROUP.equals(type)) {
            return createAndPersistBuddyGroup(identity, name, description, minParticipants, maxParticipants);
        } else if (BusinessGroup.TYPE_LEARNINGROUP.equals(type)) {
            return createAndPersistLearningGroup(identity, name, description, minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, groupContext);
        } else if (BusinessGroup.TYPE_RIGHTGROUP.equals(type)) {
            return createAndPersistRightGroup(identity, name, description, minParticipants, maxParticipants, groupContext);
        } else {
            throw new AssertException("Unknown business group type::" + type);
        }
    }

    /**
     * Create a group of type buddy group
     * 
     * @param identity
     * @param name
     * @param description
     * @return the group
     */
    private BusinessGroup createAndPersistBuddyGroup(final Identity identity, final String name, final String description, final Integer minParticipants,
            final Integer maxParticipants) {
        /*
         * [1] create 2 security groups -> ownerGroup, partipiciantGroup........ [2] create a buddyGroup with name, description, introMsg and the 2 security
         * groups...................................................... [3] create 2 policies, ownerGroup -> PERMISSION_ACCESS -> buddygroup. ....partipiciantGroup ->
         * PERMISSION_READ -> buddygroup ..............
         */
        BusinessGroupImpl businessgroup = null;
        // groups
        final SecurityGroup ownerGroup = baseSecurity.createAndPersistSecurityGroup();
        final SecurityGroup partipiciantGroup = baseSecurity.createAndPersistSecurityGroup();

        businessgroup = new BusinessGroupImpl(BusinessGroup.TYPE_BUDDYGROUP, name, description, ownerGroup, partipiciantGroup, null/* no waitingGroup */, null);
        businessgroup.setMinParticipants(minParticipants);
        businessgroup.setMaxParticipants(maxParticipants);

        DBFactory.getInstance().saveObject(businessgroup);
        if (log.isDebugEnabled()) {
            log.debug("created Buddy Group named " + name + " for Identity " + identity);
        }
        /*
         * policies: - ownerGroup can do everything on this businessgroup -> is an admin, can invite people to owner.- & partipiciantgroup - partipiciantGroup can read
         * this businessgroup
         */
        final OLATResource businessgroupOlatResource = olatResourceManager.createOLATResourceInstance(businessgroup);
        olatResourceManager.saveOLATResource(businessgroupOlatResource);

        // baseSecurity.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, businessgroup);
        baseSecurity.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_ACCESS, businessgroupOlatResource);
        baseSecurity.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
        // membership: add identity
        baseSecurity.addIdentityToSecurityGroup(identity, ownerGroup);

        // per default all collaboration-tools are disabled

        // group members visibility
        final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
        bgpm.createAndPersistDisplayMembers(true, false, false);
        return businessgroup;
    }

    /**
     * Create a group of type learning group
     * 
     * @param identity
     * @param name
     * @param description
     * @param groupContext
     * @return the group or null if the groupname is not unique in the given context
     */
    private BusinessGroup createAndPersistLearningGroup(final Identity identity, final String name, final String description, final Integer minParticipants,
            final Integer maxParticipants, final Boolean waitingListEnabled, final Boolean autoCloseRanksEnabled, final BGContext groupContext) {
        /*
         * [1] create 2 security groups -> ownerGroup, partipiciantGroup........ [2] create a learningGroup with name, description, introMsg and the 2 security
         * groups...................................................... [3] create 2 policies, ownerGroup -> PERMISSION_ACCESS ....partipiciantGroup -> PERMISSION_READ
         */
        BusinessGroupImpl businessgroup = null;

        // check if group does already exist in this learning context
        final boolean groupExists = businessGroupManager.testIfGroupAlreadyExists(name, BusinessGroup.TYPE_LEARNINGROUP, groupContext);
        if (groupExists) {
            // there is already a group with this name, return without
            // creating a new group
            log.warn("A group with this name already exists! You will get null instead of a businessGroup returned!");
            return null;
        }
        Codepoint.codepoint(BusinessGroupCreateHelper.class, "createAndPersistLearningGroup");
        // groups
        final SecurityGroup ownerGroup = baseSecurity.createAndPersistSecurityGroup();
        final SecurityGroup partipiciantGroup = baseSecurity.createAndPersistSecurityGroup();
        final SecurityGroup waitingGroup = baseSecurity.createAndPersistSecurityGroup();
        //
        businessgroup = new BusinessGroupImpl(BusinessGroup.TYPE_LEARNINGROUP, name, description, ownerGroup, partipiciantGroup, waitingGroup, groupContext);
        businessgroup.setMinParticipants(minParticipants);
        businessgroup.setMaxParticipants(maxParticipants);
        businessgroup.setWaitingListEnabled(waitingListEnabled);
        businessgroup.setAutoCloseRanksEnabled(autoCloseRanksEnabled);

        DBFactory.getInstance().saveObject(businessgroup);
        if (log.isDebugEnabled()) {
            log.debug("created Learning Group named " + name);
        }
        /*
         * policies: - ownerGroup can do everything on this businessgroup -> is an admin, can invite people to owner.- & partipiciantgroup - partipiciantGroup can read
         * this businessgroup
         */
        final OLATResource businessgroupOlatResource = olatResourceManager.createOLATResourceInstance(businessgroup);
        olatResourceManager.saveOLATResource(businessgroupOlatResource);
        OLATResource groupContextOlatResource = olatResourceManager.findResourceable(groupContext);
        if (groupContextOlatResource == null) {
            groupContextOlatResource = olatResourceManager.createOLATResourceInstance(groupContext);
            olatResourceManager.saveOLATResource(groupContextOlatResource);
        }
        baseSecurity.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_ACCESS, businessgroupOlatResource);
        baseSecurity.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_COACH, groupContextOlatResource);
        baseSecurity.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
        baseSecurity.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_PARTI, groupContextOlatResource);
        // membership: add identity if available
        if (identity != null) {
            baseSecurity.addIdentityToSecurityGroup(identity, ownerGroup);
        }

        // per default all collaboration-tools are disabled

        // group members visibility
        final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
        bgpm.createAndPersistDisplayMembers(true, false, false);
        return businessgroup;
    }

    /**
     * Create a group of type right group
     * 
     * @param identity
     * @param name
     * @param description
     * @param groupContext
     * @return the group or null if the groupname is not unique in the given context
     */
    private BusinessGroup createAndPersistRightGroup(final Identity identity, final String name, final String description, final Integer minParticipants,
            final Integer maxParticipants, final BGContext groupContext) {
        /*
         * [1] create 1 security group -> partipiciantGroup........ [2] create a learningGroup with name, description, introMsg and the security
         * group...................................................... [3] create 2 policies, partipiciantGroup -> PERMISSION_READ
         */
        BusinessGroupImpl businessgroup = null;

        // check if group does already exist in this learning context
        final boolean groupExists = businessGroupManager.testIfGroupAlreadyExists(name, BusinessGroup.TYPE_RIGHTGROUP, groupContext);
        if (groupExists) {
            // there is already a group with this name, return without
            // creating a new group
            return null;
        }

        // group
        final SecurityGroup partipiciantGroup = baseSecurity.createAndPersistSecurityGroup();
        //
        businessgroup = new BusinessGroupImpl(BusinessGroup.TYPE_RIGHTGROUP, name, description, null, partipiciantGroup, null/* no waitingGroup */, groupContext);
        businessgroup.setMinParticipants(minParticipants);
        businessgroup.setMaxParticipants(maxParticipants);
        //
        DBFactory.getInstance().saveObject(businessgroup);
        if (log.isDebugEnabled()) {
            log.debug("Created Right Group named " + name);
        }
        /*
         * policies: - partipiciantGroup can read this businessgroup
         */
        final OLATResource businessgroupOlatResource = olatResourceManager.createOLATResourceInstance(businessgroup);
        olatResourceManager.saveOLATResource(businessgroupOlatResource);
        baseSecurity.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
        // membership: add identity if available
        if (identity != null) {
            baseSecurity.addIdentityToSecurityGroup(identity, partipiciantGroup);
        }

        // per default all collaboration-tools are disabled

        // group members visibility
        final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
        bgpm.createAndPersistDisplayMembers(false, true, false);
        return businessgroup;
    }

}
