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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.repository.delete.DeletionModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for GroupDeletionServiceImpl
 * 
 * <P>
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
    public String sendDeleteEmailTo(final List selectedGroups, final MailTemplate mailTemplate, final boolean isTemplateChanged, final String keyEmailSubject,
            final String keyEmailBody, final Identity sender, final Translator pT) {
        final StringBuffer warningMessage = new StringBuffer();
        if (mailTemplate != null) {
            final MailerWithTemplate mailer = MailerWithTemplate.getInstance();
            final HashMap identityGroupList = new HashMap();
            for (final Iterator iter = selectedGroups.iterator(); iter.hasNext();) {
                final BusinessGroup group = (BusinessGroup) iter.next();

                // Build owner group, list of identities
                final SecurityGroup ownerGroup = group.getOwnerGroup();
                final List ownerIdentities = baseSecurity.getIdentitiesOfSecurityGroup(ownerGroup);
                // loop over this list and send email
                for (final Iterator iterator = ownerIdentities.iterator(); iterator.hasNext();) {
                    final Identity identity = (Identity) iterator.next();
                    if (identityGroupList.containsKey(identity)) {
                        final List groupsOfIdentity = (List) identityGroupList.get(identity);
                        groupsOfIdentity.add(group);
                    } else {
                        final List groupsOfIdentity = new ArrayList();
                        groupsOfIdentity.add(group);
                        identityGroupList.put(identity, groupsOfIdentity);
                    }
                }
            }
            // loop over identity list and send email
            for (final Iterator iterator = identityGroupList.keySet().iterator(); iterator.hasNext();) {
                final Identity identity = (Identity) iterator.next();

                mailTemplate.addToContext("responseTo", deletionModule.getEmailResponseTo());
                if (!isTemplateChanged) {
                    // Email template has NOT changed => take translated version of subject and body text
                    final Translator identityTranslator = PackageUtil.createPackageTranslator(this.getClass(),
                            I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage()));
                    mailTemplate.setSubjectTemplate(identityTranslator.translate(keyEmailSubject));
                    mailTemplate.setBodyTemplate(identityTranslator.translate(keyEmailBody));
                }
                // loop over all repositoriesOfIdentity to build email message
                final StringBuilder buf = new StringBuilder();
                for (final Iterator groupIterator = ((List) identityGroupList.get(identity)).iterator(); groupIterator.hasNext();) {
                    final BusinessGroup group = (BusinessGroup) groupIterator.next();
                    buf.append("\n  ").append(group.getName()).append(" / ").append(FilterFactory.getHtmlTagsFilter().filter(group.getDescription()));
                }
                mailTemplate.addToContext("groupList", buf.toString());
                mailTemplate.putVariablesInMailContext(mailTemplate.getContext(), identity);
                log.debug(" Try to send Delete-email to identity=" + identity.getName() + " with email="
                        + userService.getUserProperty(identity.getUser(), UserConstants.EMAIL));
                List<Identity> ccIdentities = new ArrayList<Identity>();
                if (mailTemplate.getCpfrom()) {
                    ccIdentities.add(sender);
                } else {
                    ccIdentities = null;
                }
                final MailerResult mailerResult = mailer.sendMailUsingTemplateContext(identity, ccIdentities, null, mailTemplate, sender);
                if (mailerResult.getReturnCode() == MailerResult.OK) {
                    // Email sended ok => set deleteEmailDate
                    for (final Iterator groupIterator = ((List) identityGroupList.get(identity)).iterator(); groupIterator.hasNext();) {
                        final BusinessGroup group = (BusinessGroup) groupIterator.next();
                        log.info("Audit:Group-Deletion: Delete-email send to identity=" + identity.getName() + " with email="
                                + userService.getUserProperty(identity.getUser(), UserConstants.EMAIL) + " for group=" + group);
                        markSendEmailEvent(group);
                    }
                } else {
                    warningMessage.append(
                            pT.translate("email.error.send.failed",
                                    new String[] { userService.getUserProperty(identity.getUser(), UserConstants.EMAIL), identity.getName() })).append("\n");
                }
            }
        } else {
            // no template => User decides to sending no delete-email, mark only in lifecycle table 'sendEmail'
            for (final Iterator iter = selectedGroups.iterator(); iter.hasNext();) {
                final BusinessGroup group = (BusinessGroup) iter.next();
                log.info("Audit:Group-Deletion: Move in 'Email sent' section without sending email, group=" + group);
                markSendEmailEvent(group);
            }
        }
        return warningMessage.toString();
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
