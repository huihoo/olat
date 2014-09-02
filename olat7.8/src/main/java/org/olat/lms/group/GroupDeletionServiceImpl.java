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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.GroupDeletionDao;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.UserConstants;
import org.olat.lms.core.notification.service.GroupsConfirmationInfo;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.lms.repository.delete.DeletionModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 29.06.2011 <br>
 * 
 * @author guido
 */
@Service
public class GroupDeletionServiceImpl implements GroupDeletionService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    UserService userService;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    DeletionModule deletionModule;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    GroupDeletionDao groupDeletionDao;
    @Autowired
    PropertyManager propertyManager;
    @Autowired
    DB database;
    @Autowired
    CollaborationToolsFactory collaborationTools;

    private static final String GROUP_ARCHIVE_DIR = "archive_deleted_groups";
    private static final String GROUPEXPORT_XML = "groupexport.xml";
    private static final String GROUPARCHIVE_XLS = "grouparchive.xls";
    private static final String GROUP_DELETED_ACTION = "groupDeleted";
    private static final String PROPERTY_CATEGORY = "GroupDeletion";
    private static final String LAST_USAGE_DURATION_PROPERTY_NAME = "LastUsageDuration";
    private static final int DEFAULT_LAST_USAGE_DURATION = 24;
    private static final String DELETE_EMAIL_DURATION_PROPERTY_NAME = "DeleteEmailDuration";
    private static final int DEFAULT_DELETE_EMAIL_DURATION = 30;

    /**
	 * 
	 */
    private GroupDeletionServiceImpl() {
        //
    }

    @Override
    public void setLastUsageNowFor(BusinessGroup group) {
        group = (BusinessGroup) database.loadObject(group, true);
        group.setLastUsage(new Date());
        final LifeCycleManager lcManager = LifeCycleManager.createInstanceFor(group);
        if (lcManager.lookupLifeCycleEntry(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION) != null) {
            log.info("Audit:Group-Deletion: Remove from delete-list group=" + group);
            LifeCycleManager.createInstanceFor(group).deleteTimestampFor(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION);
        }
        businessGroupService.updateBusinessGroup(group);
    }

    @Override
    public void deleteGroups(List<BusinessGroup> groupsReadyToDelete) {
        for (final Iterator iter = groupsReadyToDelete.iterator(); iter.hasNext();) {
            final BusinessGroup businessGroup = (BusinessGroup) iter.next();
            final String archiveFileName = archive(getArchivFilePath(businessGroup), businessGroup);
            log.info("Audit:Group-Deletion: archived businessGroup=" + businessGroup + " , archive-file-name=" + archiveFileName);
            collaborationTools.getOrCreateCollaborationTools(businessGroup).deleteTools(businessGroup);
            businessGroupService.deleteBusinessGroup(businessGroup);
            LifeCycleManager.createInstanceFor(businessGroup).deleteTimestampFor(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION);
            LifeCycleManager.createInstanceFor(businessGroup).markTimestampFor(GROUP_DELETED_ACTION, createLifeCycleLogDataFor(businessGroup));
            log.info("Audit:Group-Deletion: deleted businessGroup=" + businessGroup);
        }
    }

    private String createLifeCycleLogDataFor(final BusinessGroup businessGroup) {
        final StringBuilder buf = new StringBuilder();
        buf.append("<businessgroup>");
        buf.append("<name>").append(businessGroup.getName()).append("</name>");
        final String desc = FilterFactory.getHtmlTagsFilter().filter(businessGroup.getDescription());
        buf.append("<description>").append(trimDescription(desc, 60)).append("</description>");
        buf.append("<resid>").append(businessGroup.getResourceableId()).append("</resid>");
        buf.append("</businessgroup>");
        return buf.toString();
    }

    private String trimDescription(final String description, final int maxlength) {
        if (description.length() > (maxlength)) {
            return description.substring(0, maxlength - 3) + "...";
        }
        return description;
    }

    private String getArchivFilePath(final BusinessGroup businessGroup) {
        return deletionModule.getArchiveRootPath() + File.separator + GROUP_ARCHIVE_DIR + File.separator + DeletionModule.getArchiveDatePath() + File.separator
                + "del_group_" + businessGroup.getResourceableId();
    }

    /**
     * Archive group runtime-data in xls file and export group as xml file
     * 
     * @param archiveFilePath
     * @param businessGroup
     * @return
     */
    private String archive(final String archiveFilePath, final BusinessGroup businessGroup) {
        final File exportRootDir = new File(archiveFilePath);
        if (!exportRootDir.exists()) {
            exportRootDir.mkdirs();
        }
        businessGroupService.archiveGroup(businessGroup, new File(archiveFilePath, GROUPARCHIVE_XLS));
        final File exportFile = new File(archiveFilePath, GROUPEXPORT_XML);
        if (businessGroup.getGroupContext() == null) {
            businessGroupService.exportGroup(businessGroup, exportFile);
        } else {
            businessGroupService.exportGroups(businessGroup.getGroupContext(), exportFile);
        }
        return GROUPEXPORT_XML;
    }

    @Override
    public List<BusinessGroup> getGroupsReadyToDelete(int deleteEmailDuration) {
        return groupDeletionDao.getGroupsReadyToDelete(deleteEmailDuration);
    }

    @Override
    public List<BusinessGroup> getGroupsInDeletionProcess(int deleteEmailDuration) {
        return groupDeletionDao.getGroupsInDeletionProcess(deleteEmailDuration);
    }

    @Override
    public String sendDeleteEmailTo(final List<BusinessGroup> selectedGroups, final Identity sender, final Translator translator) {
        final Map<Identity, List<BusinessGroup>> identityGroupList = new HashMap<Identity, List<BusinessGroup>>();
        for (final BusinessGroup group : selectedGroups) {
            // Build owner group, list of identities
            final SecurityGroup ownerGroup = group.getOwnerGroup();
            final List<Identity> ownerIdentities = baseSecurity.getIdentitiesOfSecurityGroup(ownerGroup);
            for (final Identity identity : ownerIdentities) {
                List<BusinessGroup> groupsOfIdentity = identityGroupList.get(identity);
                if (groupsOfIdentity == null) {
                    groupsOfIdentity = new ArrayList<BusinessGroup>();
                    identityGroupList.put(identity, groupsOfIdentity);
                }
                groupsOfIdentity.add(group);
            }
        }

        // loop over identity list and send email
        final StringBuilder warningMessage = new StringBuilder();
        for (final Identity identity : identityGroupList.keySet()) {
            final List<RecipientInfo> recipients = getConfirmationLearnService().createRecipientInfos(Collections.singletonList(identity));
            final GroupsConfirmationInfo groupsConfirmationInfo = new GroupsConfirmationInfo(recipients, sender, new Date(), identityGroupList.get(identity),
                    GroupsConfirmationInfo.GROUPS_CONFIRMATION_TYPE.DELETE_GROUPS, DEFAULT_LAST_USAGE_DURATION, DEFAULT_DELETE_EMAIL_DURATION);
            if (getConfirmationLearnService().sendGroupConfirmation(groupsConfirmationInfo)) {
                // Email sent successfully => set deleteEmailDate
                for (final BusinessGroup group : identityGroupList.get(identity)) {
                    log.info("Audit:Group-Deletion: Delete-email send to identity=" + identity.getName() + " with email="
                            + userService.getUserProperty(identity.getUser(), UserConstants.EMAIL) + " for group=" + group);
                    markSendEmailEvent(group);
                }
            } else {
                warningMessage.append(
                        translator.translate("email.error.send.failed",
                                new String[] { userService.getUserProperty(identity.getUser(), UserConstants.EMAIL), identity.getName() })).append("<br>");
            }
        }
        return warningMessage.toString();
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    private void markSendEmailEvent(BusinessGroup group) {
        group = (BusinessGroup) database.loadObject(group);
        LifeCycleManager.createInstanceFor(group).markTimestampFor(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION);
        database.updateObject(group);
    }

    @Override
    public List<BusinessGroup> getDeletableGroups(int lastUsageDuration) {
        return groupDeletionDao.getDeletableGroups(lastUsageDuration);
    }

    @Override
    public void setLastUsageDuration(final int lastUsageDuration) {
        setProperty(LAST_USAGE_DURATION_PROPERTY_NAME, lastUsageDuration);
    }

    @Override
    public void setDeleteEmailDuration(final int deleteEmailDuration) {
        setProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, deleteEmailDuration);
    }

    @Override
    public int getLastUsageDuration() {
        return getPropertyByName(LAST_USAGE_DURATION_PROPERTY_NAME, DEFAULT_LAST_USAGE_DURATION);
    }

    @Override
    public int getDeleteEmailDuration() {
        return getPropertyByName(DELETE_EMAIL_DURATION_PROPERTY_NAME, DEFAULT_DELETE_EMAIL_DURATION);
    }

    private int getPropertyByName(final String name, final int defaultValue) {
        final List properties = propertyManager.findProperties(null, null, null, PROPERTY_CATEGORY, name);
        if (properties.size() == 0) {
            return defaultValue;
        } else {
            return ((PropertyImpl) properties.get(0)).getLongValue().intValue();
        }
    }

    private void setProperty(final String propertyName, final int value) {
        final List properties = propertyManager.findProperties(null, null, null, PROPERTY_CATEGORY, propertyName);
        PropertyImpl property = null;
        if (properties.size() == 0) {
            property = propertyManager.createPropertyInstance(null, null, null, PROPERTY_CATEGORY, propertyName, null, new Long(value), null, null);
        } else {
            property = (PropertyImpl) properties.get(0);
            property.setLongValue(new Long(value));
        }
        propertyManager.saveProperty(property);
    }

}
