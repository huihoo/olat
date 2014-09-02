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

package org.olat.lms.repository.delete;

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
import org.olat.data.commons.database.DBFactory;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.repository.RepositoryDeletionDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.core.notification.service.RepositoryEntriesConfirmationInfo;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.user.UserDataDeletable;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.repository.delete.SelectionController;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Mar 31, 2004
 * 
 * @author Mike Stock Comment:
 */
public class RepositoryDeletionManager extends BasicManager implements UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String REPOSITORY_ARCHIVE_DIR = "archive_deleted_resources";

    private static final String PROPERTY_CATEGORY = "RepositoryDeletion";
    private static final String LAST_USAGE_DURATION_PROPERTY_NAME = "LastUsageDuration";
    private static final int DEFAULT_LAST_USAGE_DURATION = 24;
    private static final String DELETE_EMAIL_DURATION_PROPERTY_NAME = "DeleteEmailDuration";
    private static final int DEFAULT_DELETE_EMAIL_DURATION = 30;

    private static RepositoryDeletionManager INSTANCE;
    private static final String PACKAGE = PackageUtil.getPackageName(SelectionController.class);

    private static final String REPOSITORY_DELETED_ACTION = "respositoryEntryDeleted";

    @Autowired
    private DeletionModule deletionModule;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    private UserService userService;
    @Autowired
    private RepositoryDeletionDao repositoryDeletionDao;

    /**
     * [used by spring]
     */
    private RepositoryDeletionManager() {
        INSTANCE = this;
    }

    /**
     * @return Singleton.
     */
    public static RepositoryDeletionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Remove identity as owner and initial author. Used in user-deletion. If there is no other owner and/or author, the olat-administrator (defined in olat.properties)
     * will be added as owner.
     * 
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        // Remove as owner
        List repoEntries = RepositoryServiceImpl.getInstance().queryByOwner(identity, new String[] {}/* no type limit */);
        for (final Iterator iter = repoEntries.iterator(); iter.hasNext();) {
            final RepositoryEntry repositoryEntry = (RepositoryEntry) iter.next();

            baseSecurity.removeIdentityFromSecurityGroup(identity, repositoryEntry.getOwnerGroup());
            if (baseSecurity.countIdentitiesOfSecurityGroup(repositoryEntry.getOwnerGroup()) == 0) {
                // This group has no owner anymore => add OLAT-Admin as owner
                baseSecurity.addIdentityToSecurityGroup(deletionModule.getAdminUserIdentity(), repositoryEntry.getOwnerGroup());
                log.info("Delete user-data, add Administrator-identity as owner of repositoryEntry=" + repositoryEntry.getDisplayname());
            }
        }
        // Remove as initial author
        repoEntries = RepositoryServiceImpl.getInstance().queryByInitialAuthor(identity.getName());
        for (final Iterator iter = repoEntries.iterator(); iter.hasNext();) {
            final RepositoryEntry repositoryEntry = (RepositoryEntry) iter.next();
            repositoryEntry.setInitialAuthor(deletionModule.getAdminUserIdentity().getName());
            log.info("Delete user-data, add Administrator-identity as initial-author of repositoryEntry=" + repositoryEntry.getDisplayname());
        }
        log.debug("All owner and initial-author entries in repository deleted for identity=" + identity);
    }

    // ////////////////////
    // REPOSITORY_DELETION
    // ////////////////////
    public void setLastUsageDuration(final int lastUsageDuration) {
        setProperty(LAST_USAGE_DURATION_PROPERTY_NAME, lastUsageDuration);
    }

    public void setDeleteEmailDuration(final int deleteEmailDuration) {
        setProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, deleteEmailDuration);
    }

    public int getLastUsageDuration() {
        return getPropertyByName(LAST_USAGE_DURATION_PROPERTY_NAME, DEFAULT_LAST_USAGE_DURATION);
    }

    public int getDeleteEmailDuration() {
        return getPropertyByName(DELETE_EMAIL_DURATION_PROPERTY_NAME, DEFAULT_DELETE_EMAIL_DURATION);
    }

    public void sendDeleteEmailTo(final List<RepositoryEntry> selectedRepositoryEntries, final Identity sender) {
        final Map<Identity, List<RepositoryEntry>> identityRepositoryList = collectRepositoryEntriesForIdentities(selectedRepositoryEntries);
        // loop over identity list and send email
        for (final Identity identity : identityRepositoryList.keySet()) {
            final List<RecipientInfo> recipients = getConfirmationLearnService().createRecipientInfos(Collections.singletonList(identity));
            final RepositoryEntriesConfirmationInfo repositoryEntriesConfirmationInfo = new RepositoryEntriesConfirmationInfo(recipients, sender, new Date(),
                    identityRepositoryList.get(identity), DEFAULT_LAST_USAGE_DURATION, DEFAULT_DELETE_EMAIL_DURATION,
                    RepositoryEntriesConfirmationInfo.REPOSITORY_ENTRIES_CONFIRMATION_TYPE.DELETE_REPOSITORY_ENTRIES);
            getConfirmationLearnService().sendGroupConfirmation(repositoryEntriesConfirmationInfo);
        }

        for (final RepositoryEntry repositoryEntry : selectedRepositoryEntries) {
            markSendEmailEvent(repositoryEntry);
        }
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    private void markSendEmailEvent(RepositoryEntry repositoryEntry) {
        repositoryEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry);
        LifeCycleManager.createInstanceFor(repositoryEntry).markTimestampFor(RepositoryDeletionDao.SEND_DELETE_EMAIL_ACTION);
        DBFactory.getInstance().updateObject(repositoryEntry);
    }

    /**
     * Loop over all repository-entries and collect repository-entries with the same owner identites
     * 
     * @param repositoryList
     * @return Map with Identity as key elements, List of RepositoryEntry as objects
     */
    private Map<Identity, List<RepositoryEntry>> collectRepositoryEntriesForIdentities(final List<RepositoryEntry> repositoryList) {
        final Map<Identity, List<RepositoryEntry>> identityRepositoryList = new HashMap<Identity, List<RepositoryEntry>>();
        for (final RepositoryEntry repositoryEntry : repositoryList) {

            // Build owner group, list of identities
            final SecurityGroup ownerGroup = repositoryEntry.getOwnerGroup();
            List<Identity> ownerIdentities;
            if (ownerGroup != null) {
                ownerIdentities = baseSecurity.getIdentitiesOfSecurityGroup(ownerGroup);
            } else {
                log.info("collectRepositoryEntriesForIdentities: ownerGroup is null, add adminUserIdentity as owner repositoryEntry=" + repositoryEntry.getDisplayname()
                        + "  repositoryEntry.key=" + repositoryEntry.getKey());
                // Add admin user
                ownerIdentities = new ArrayList<Identity>();
                ownerIdentities.add(deletionModule.getAdminUserIdentity());
            }

            // Loop over owner to collect all repository-entry for each user
            for (final Identity identity : ownerIdentities) {
                List<RepositoryEntry> repositoriesOfIdentity = identityRepositoryList.get(identity);
                if (repositoriesOfIdentity == null) {
                    repositoriesOfIdentity = new ArrayList<RepositoryEntry>();
                    identityRepositoryList.put(identity, repositoriesOfIdentity);
                }

                repositoriesOfIdentity.add(repositoryEntry);
            }

        }
        return identityRepositoryList;
    }

    private String trimDescription(final String description, final int maxlength) {
        if (description.length() > (maxlength)) {
            return description.substring(0, maxlength - 3) + "...";
        }
        return description;
    }

    public void deleteRepositoryEntries(final UserRequest ureq, final WindowControl wControl, final List repositoryEntryList) {
        for (final Iterator iter = repositoryEntryList.iterator(); iter.hasNext();) {
            final RepositoryEntry repositoryEntry = (RepositoryEntry) iter.next();
            final RepositoryHandler repositoryHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
            final File archiveDir = new File(getArchivFilePath());
            if (!archiveDir.exists()) {
                archiveDir.mkdirs();
            }
            final String archiveFileName = repositoryHandler.archive(ureq.getIdentity(), getArchivFilePath(), repositoryEntry);
            log.info("Audit:Repository-Deletion: archived repositoryEntry=" + repositoryEntry + " , archive-file-name=" + archiveFileName);
            RepositoryServiceImpl.getInstance().deleteRepositoryEntryWithAllData(ureq, wControl, repositoryEntry);
            LifeCycleManager.createInstanceFor(repositoryEntry).deleteTimestampFor(RepositoryDeletionDao.SEND_DELETE_EMAIL_ACTION);
            LifeCycleManager.createInstanceFor(repositoryEntry).markTimestampFor(REPOSITORY_DELETED_ACTION, createLifeCycleLogDataFor(repositoryEntry));
            log.info("Audit:Repository-Deletion: deleted repositoryEntry=" + repositoryEntry);
            DBFactory.getInstance().intermediateCommit();
        }
    }

    private String createLifeCycleLogDataFor(final RepositoryEntry repositoryEntry) {
        final StringBuilder buf = new StringBuilder();
        buf.append("<repositoryentry>");
        buf.append("<name>").append(repositoryEntry.getDisplayname()).append("</name>");
        buf.append("<description>").append(trimDescription(repositoryEntry.getDescription(), 60)).append("</description>");
        buf.append("<resid>").append(repositoryEntry.getOlatResource().getResourceableId()).append("</resid>");
        buf.append("<initialauthor>").append(repositoryEntry.getInitialAuthor()).append("</initialauthor>");
        buf.append("</repositoryentry>");
        return buf.toString();
    }

    private String getArchivFilePath() {
        return deletionModule.getArchiveRootPath() + File.separator + REPOSITORY_ARCHIVE_DIR + File.separator + DeletionModule.getArchiveDatePath();
    }

    private int getPropertyByName(final String name, final int defaultValue) {
        final List properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, name);
        if (properties.size() == 0) {
            return defaultValue;
        } else {
            return ((PropertyImpl) properties.get(0)).getLongValue().intValue();
        }
    }

    private void setProperty(final String propertyName, final int value) {
        final List properties = PropertyManager.getInstance().findProperties(null, null, null, PROPERTY_CATEGORY, propertyName);
        PropertyImpl property = null;
        if (properties.size() == 0) {
            property = PropertyManager.getInstance().createPropertyInstance(null, null, null, PROPERTY_CATEGORY, propertyName, null, new Long(value), null, null);
        } else {
            property = (PropertyImpl) properties.get(0);
            property.setLongValue(new Long(value));
        }
        PropertyManager.getInstance().saveProperty(property);
    }

    public List<RepositoryEntry> getDeletableRepositoryEntries(int lastUsageDuration) {
        return repositoryDeletionDao.getDeletableRepositoryEntries(lastUsageDuration);
    }

    public List<RepositoryEntry> getRepositoryEntriesInDeletionProcess(int deleteEmailDuration) {
        return repositoryDeletionDao.getRepositoryEntriesInDeletionProcess(deleteEmailDuration);
    }

    public List<RepositoryEntry> getRepositoryEntriesReadyToDelete(int deleteEmailDuration) {
        return repositoryDeletionDao.getRepositoryEntriesReadyToDelete(deleteEmailDuration);
    }

}
