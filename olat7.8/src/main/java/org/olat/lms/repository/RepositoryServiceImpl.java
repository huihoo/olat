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

package org.olat.lms.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.repository.RepositoryDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.data.user.UserConstants;
import org.olat.lms.activitylogging.ActionType;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.commons.mediaresource.FileMediaResource;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.repository.async.BackgroundTaskQueueManager;
import org.olat.lms.repository.async.tasks.IncrementDownloadCounterBackgroundTask;
import org.olat.lms.repository.async.tasks.IncrementLaunchCounterBackgroundTask;
import org.olat.lms.repository.async.tasks.SetAccessBackgroundTask;
import org.olat.lms.repository.async.tasks.SetDescriptionNameBackgroundTask;
import org.olat.lms.repository.async.tasks.SetLastUsageBackgroundTask;
import org.olat.lms.repository.async.tasks.SetPropertiesBackgroundTask;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: Mar 31, 2004
 * 
 * @author Mike Stock Comment:
 */
@Service("repositoryManager")
public class RepositoryServiceImpl extends BasicManager implements RepositoryService {

    private static final Logger log = LoggerHelper.getLogger();

    private static RepositoryService INSTANCE;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    BackgroundTaskQueueManager taskQueueManager;
    @Autowired
    CatalogService catalogService;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    BookmarkService bookmarkService;
    @Autowired
    RepositoryDao repositoryDao;
    @Autowired
    OLATResourceManager olatresourceManager;
    @Autowired
    UserService userService;
    @Autowired
    BusinessGroupContextService bgContextService;

    /**
     * [used by spring]
     */
    protected RepositoryServiceImpl() {
        INSTANCE = this;
    }

    /**
     * @deprecated
     * @return Singleton.
     */
    public static RepositoryService getInstance() {
        return INSTANCE;
    }

    @Override
    public RepositoryEntry createRepositoryEntryInstance(final String initialAuthor) {
        if (initialAuthor == null)
            throw new IllegalArgumentException("author cannot be null.");
        return repositoryDao.createRepositoryEntryInstance(initialAuthor);
    }

    @Override
    public RepositoryEntry createRepositoryEntryInstance(final String initialAuthor, final String resourceName, final String description) {
        if (initialAuthor == null)
            throw new IllegalArgumentException("author cannot be null.");
        return repositoryDao.createRepositoryEntryInstance(initialAuthor, resourceName, description);
    }

    /**
     * @param repositoryEntryStatusCode
     */
    @Override
    public RepositoryEntryStatus createRepositoryEntryStatus(final int repositoryEntryStatusCode) {
        if (repositoryEntryStatusCode > 2 && repositoryEntryStatusCode < 1)
            throw new IllegalArgumentException("status code must be 1 or 2");
        return new RepositoryEntryStatus(repositoryEntryStatusCode);
    }

    @Override
    public void saveRepositoryEntry(final RepositoryEntry re) {
        repositoryDao.saveRepositoryEntry(re);
    }

    @Override
    public void updateRepositoryEntry(final RepositoryEntry re) {
        repositoryDao.updateRepositoryEntry(re);
    }

    @Override
    public void deleteRepositoryEntry(RepositoryEntry re) {
        repositoryDao.deleteRepositoryEntry(re);
        deleteRepositoryImage(re);
    }

    private void deleteRepositoryImage(final RepositoryEntry re) {
        final File srcFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()), re.getResourceableId() + ".jpg");
        if (srcFile.exists()) {
            srcFile.delete();
        }
    }

    /**
     * @param addedEntry
     */
    @Override
    public void deleteRepositoryEntryAndBasesecurity(RepositoryEntry entry) {
        entry = repositoryDao.lookupRepositoryEntry(entry.getKey());
        final SecurityGroup ownerGroup = entry.getOwnerGroup();
        deleteRepositoryEntry(entry);
        olatresourceManager.deleteOLATResourceable(entry);
        if (ownerGroup != null) {
            // delete secGroup
            log.debug("deleteRepositoryEntry deleteSecurityGroup ownerGroup=" + ownerGroup);
            baseSecurity.deleteSecurityGroup(ownerGroup);
            olatresourceManager.deleteOLATResourceable(ownerGroup);
        }
    }

    /**
     * clean up a repo entry with all children and associated data like bookmarks and user references to it
     * 
     * @param ureq
     * @param wControl
     * @param entry
     * @return FIXME: we need a delete method without ureq, wControl for manager use. In general, very bad idea to pass ureq and wControl down to the manger layer.
     */
    @Override
    public boolean deleteRepositoryEntryWithAllData(final UserRequest ureq, final WindowControl wControl, RepositoryEntry entry) {
        final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
        final OLATResource ores = entry.getOlatResource();
        if (!handler.readyToDelete(ores, ureq, wControl)) {
            return false;
        }

        // delete all bookmarks referencing deleted entry
        bookmarkService.deleteAllBookmarksFor(entry);
        // delete all catalog entries referencing deleted entry
        catalogService.resourceableDeleted(entry);
        // delete the entry
        deleteRepositoryEntryAndBasesecurity(entry);
        // inform handler to do any cleanup work... handler must delete the
        // referenced resourceable aswell.
        handler.cleanupOnDelete(entry.getOlatResource());
        log.debug("deleteRepositoryEntry Done");
        return true;
    }

    @Override
    public RepositoryEntry lookupRepositoryEntry(final Long key) {
        return repositoryDao.lookupRepositoryEntry(key);
    }

    @Override
    public RepositoryEntry lookupRepositoryEntry(final OLATResourceable resourceable, final boolean strict) {
        final OLATResource resource = olatresourceManager.findResourceable(resourceable);
        if (resource == null) {
            if (!strict) {
                return null;
            }
            throw new AssertException("Unable to fetch OLATResource for resourceable: " + resourceable.getResourceableTypeName() + ", "
                    + resourceable.getResourceableId());
        }
        return repositoryDao.lookupRepositoryEntry(resource, strict);
    }

    @Override
    public RepositoryEntry lookupRepositoryEntryBySoftkey(final String softkey, final boolean strict) {
        return repositoryDao.lookupRepositoryEntryBySoftkey(softkey, strict);
    }

    @Override
    public String lookupDisplayNameByOLATResourceableId(final Long resId) {
        return repositoryDao.lookupDisplayNameByOLATResourceableId(resId);
    }

    /**
     * Test a repo entry if identity is allowed to launch.
     * 
     * @param identity
     * @param roles
     * @param re
     * @return True if current identity is allowed to launch the given repo entry.
     */
    @Override
    public boolean isAllowedToLaunch(final Identity identity, final Roles roles, final RepositoryEntry re) {
        if (!re.getCanLaunch()) {
            return false; // deny if not launcheable
        }
        // allow if identity is owner
        if (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, re.getOwnerGroup())) {
            return true;
        }
        // allow if access limit matches identity's role
        // allow for olat administrators
        if (roles.isOLATAdmin()) {
            return true;
        }
        // allow for institutional resource manager
        if (isInstitutionalRessourceManagerFor(re, identity)) {
            return true;
        }
        // allow for authors if access granted at least for authors
        if (roles.isAuthor() && re.getAccess() >= RepositoryEntry.ACC_OWNERS_AUTHORS) {
            return true;
        }
        // allow for guests if access granted for guests
        if (roles.isGuestOnly()) {
            if (re.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS) {
                return true;
            } else {
                return false;
            }
        }
        // else allow if access granted for users
        return re.getAccess() >= RepositoryEntry.ACC_USERS;

    }

    /**
     * Increment the launch counter.
     * 
     * @param re
     */
    @Override
    public void incrementLaunchCounter(final RepositoryEntry re) {
        taskQueueManager.addTask(new IncrementLaunchCounterBackgroundTask(re));
    }

    /**
     * Increment the download counter.
     * 
     * @param re
     */
    @Override
    public void incrementDownloadCounter(final RepositoryEntry re) {
        taskQueueManager.addTask(new IncrementDownloadCounterBackgroundTask(re));
    }

    /**
     * Set last-usage date to to now for certain repository-entry.
     * 
     * @param
     */
    @Override
    public void setLastUsageNowFor(final RepositoryEntry re) {
        if (re != null) {
            taskQueueManager.addTask(new SetLastUsageBackgroundTask(re));
        }
    }

    @Override
    public void setAccess(final RepositoryEntry re, final int access) {
        final SetAccessBackgroundTask task = new SetAccessBackgroundTask(re, access);
        taskQueueManager.addTask(task);
        task.waitForDone();
    }

    @Override
    public void setDescriptionAndName(final RepositoryEntry re, final String displayName, final String description) {
        final SetDescriptionNameBackgroundTask task = new SetDescriptionNameBackgroundTask(re, displayName, description);
        taskQueueManager.addTask(task);
        task.waitForDone();
    }

    @Override
    public void setProperties(final RepositoryEntry re, final boolean canCopy, final boolean canReference, final boolean canLaunch, final boolean canDownload) {
        final SetPropertiesBackgroundTask task = new SetPropertiesBackgroundTask(re, canCopy, canReference, canLaunch, canDownload);
        taskQueueManager.addTask(task);
        task.waitForDone();
    }

    @Override
    public int countByTypeLimitAccess(final String restrictedType, final int restrictedAccess) {
        return repositoryDao.countByTypeLimitAccess(restrictedType, restrictedAccess);
    }

    @Override
    public List queryByType(final String restrictedType) {
        return repositoryDao.queryByType(restrictedType);
    }

    @Override
    public List queryByTypeLimitAccess(final String restrictedType, final Roles roles) {
        return repositoryDao.queryByTypeLimitAccess(restrictedType, roles);
    }

    @Override
    public List queryByTypeLimitAccess(final String restrictedType, final UserRequest ureq, String institution) {
        final Roles roles = ureq.getUserSession().getRoles();
        institution = userService.getUserProperty(ureq.getIdentity().getUser(), UserConstants.INSTITUTIONALNAME);
        return repositoryDao.queryByTypeLimitAccess(restrictedType, institution, roles);
    }

    /**
     * Query by ownership, optionally limit by type.
     * 
     * @param identity
     * @param limitType
     * @return Results
     */
    @Override
    public List queryByOwner(final Identity identity, final String limitType) {
        return queryByOwner(identity, new String[] { limitType });
    }

    @Override
    public List queryByOwner(final Identity identity, final String[] limitTypes) {
        return repositoryDao.queryByOwner(identity, limitTypes);
    }

    @Override
    public List queryByInitialAuthor(final String initialAuthor) {
        return repositoryDao.queryByInitialAuthor(initialAuthor);
    }

    @Override
    public List queryReferencableResourcesLimitType(final Identity identity, final Roles roles, List resourceTypes, String displayName, String author, String desc) {
        return repositoryDao.queryReferencableResourcesLimitType(identity, roles, resourceTypes, displayName, author, desc);
    }

    @Override
    public List queryByOwnerLimitAccess(final Identity identity, final int limitAccess) {
        return repositoryDao.queryByOwnerLimitAccess(identity, limitAccess);
    }

    /**
     * check ownership of identiy for a resource
     * 
     * @return true if the identity is member of the security group of the repository entry
     */
    @Override
    public boolean isOwnerOfRepositoryEntry(final Identity identity, final RepositoryEntry entry) {
        final SecurityGroup ownerGroup = lookupRepositoryEntry(entry.getOlatResource(), true).getOwnerGroup();
        return baseSecurity.isIdentityInSecurityGroup(identity, ownerGroup);
    }

    @Override
    public List<RepositoryEntry> genericANDQueryWithRolesRestriction(String displayName, String author, String desc, final List resourceTypes, final Roles roles,
            final String institution) {
        return repositoryDao.genericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles, institution);
    }

    /**
     * add provided list of identities as owners to the repo entry. silently ignore if some identities were already owners before.
     * 
     * @param ureqIdentity
     * @param addIdentities
     * @param re
     * @param userActivityLogger
     */
    @Override
    public void addOwners(final Identity ureqIdentity, final IdentitiesAddEvent iae, final RepositoryEntry re) {
        final List<Identity> addIdentities = iae.getAddIdentities();
        final List<Identity> reallyAddedId = new ArrayList<Identity>();
        final SecurityGroup group = re.getOwnerGroup();
        for (final Identity identity : addIdentities) {
            if (!baseSecurity.isIdentityInSecurityGroup(identity, re.getOwnerGroup())) {
                baseSecurity.addIdentityToSecurityGroup(identity, re.getOwnerGroup());
                reallyAddedId.add(identity);
                final ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
                ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
                try {
                    ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry),
                            LoggingResourceable.wrap(identity));
                } finally {
                    ThreadLocalUserActivityLogger.setStickyActionType(actionType);
                }
                log.info("Audit:Idenitity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getName() + "' to securitygroup with key "
                        + re.getOwnerGroup().getKey());
            }// else silently ignore already owner identities
        }
        iae.setIdentitiesAddedEvent(reallyAddedId);
    }

    /**
     * remove list of identities as owners of given repository entry.
     * 
     * @param ureqIdentity
     * @param removeIdentities
     * @param re
     * @param logger
     */
    @Override
    public void removeOwners(final Identity ureqIdentity, final List<Identity> removeIdentities, final RepositoryEntry re) {
        for (final Identity identity : removeIdentities) {
            baseSecurity.removeIdentityFromSecurityGroup(identity, re.getOwnerGroup());
            final String details = "Remove Owner from RepoEntry:" + re.getKey() + " USER:" + identity.getName();

            final ActionType actionType = ThreadLocalUserActivityLogger.getStickyActionType();
            ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
            try {
                ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(), LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry),
                        LoggingResourceable.wrap(identity));
            } finally {
                ThreadLocalUserActivityLogger.setStickyActionType(actionType);
            }
            log.info("Audit:Idenitity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getName() + "' from securitygroup with key "
                    + re.getOwnerGroup().getKey());
        }
    }

    /**
     * has one owner of repository entry the same institution like the resource manager
     * 
     * @param RepositoryEntry
     *            repositoryEntry
     * @param Identity
     *            identity
     */
    @Override
    public boolean isInstitutionalRessourceManagerFor(final RepositoryEntry repositoryEntry, final Identity identity) {
        if (repositoryEntry == null || repositoryEntry.getOwnerGroup() == null) {
            return false;
        }

        // list of owners
        final List<Identity> listIdentities = baseSecurity.getIdentitiesOfSecurityGroup(repositoryEntry.getOwnerGroup());
        final String currentUserInstitutionalName = userService.getUserProperty(identity.getUser(), UserConstants.INSTITUTIONALNAME);
        final boolean isInstitutionalResourceManager = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE,
                Constants.ORESOURCE_INSTORESMANAGER);

        boolean sameInstitutional = false;
        String identInstitutionalName = "";
        for (final Identity ident : listIdentities) {
            identInstitutionalName = userService.getUserProperty(ident.getUser(), UserConstants.INSTITUTIONALNAME);
            if ((identInstitutionalName != null) && (identInstitutionalName.equals(currentUserInstitutionalName))) {
                sameInstitutional = true;
                break;
            }
        }
        return isInstitutionalResourceManager && sameInstitutional;
    }

    /**
     * Gets all learning resources where the user is in a learning group as participant.
     * 
     * @param identity
     * @return list of RepositoryEntries
     */
    @Override
    public List<RepositoryEntry> getLearningResourcesAsStudent(final Identity identity) {
        final List<RepositoryEntry> allRepoEntries = new ArrayList<RepositoryEntry>();
        final List<BusinessGroup> groupList = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
        for (final BusinessGroup group : groupList) {
            final BGContext bgContext = group.getGroupContext();
            if (bgContext == null) {
                continue;
            }
            final List<RepositoryEntry> repoEntries = bgContextService.findRepositoryEntriesForBGContext(bgContext);
            if (repoEntries == null || repoEntries.size() == 0) {
                continue;
            }
            for (final RepositoryEntry repositoryEntry : repoEntries) {
                // only find resources that are published
                if (!PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry) && repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS) {
                    allRepoEntries.add(repositoryEntry);
                }
            }
        }
        return allRepoEntries;
    }

    /**
     * Gets all learning resources where the user is coach of a learning group or where he is in a rights group or where he is in the repository entry owner group (course
     * administrator)
     * 
     * @param identity
     * @return list of RepositoryEntries
     */
    @Override
    public List<RepositoryEntry> getLearningResourcesAsTeacher(final Identity identity) {
        final List<RepositoryEntry> allRepoEntries = new ArrayList<RepositoryEntry>();
        // 1: search for all learning groups where user is coach
        final List<BusinessGroup> groupList = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
        for (final BusinessGroup group : groupList) {
            final BGContext bgContext = group.getGroupContext();
            if (bgContext == null) {
                continue;
            }
            final List<RepositoryEntry> repoEntries = bgContextService.findRepositoryEntriesForBGContext(bgContext);
            if (repoEntries.size() == 0) {
                continue;
            }
            for (final RepositoryEntry repositoryEntry : repoEntries) {
                // only find resources that are published
                if (!PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry) && repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS) {
                    allRepoEntries.add(repositoryEntry);
                }
            }
        }
        // 2: search for all learning groups where user is coach
        final List<BusinessGroup> rightGrougList = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
        for (final BusinessGroup group : rightGrougList) {
            final BGContext bgContext = group.getGroupContext();
            if (bgContext == null) {
                continue;
            }
            final List<RepositoryEntry> repoEntries = bgContextService.findRepositoryEntriesForBGContext(bgContext);
            if (repoEntries.size() == 0) {
                continue;
            }
            for (final RepositoryEntry repositoryEntry : repoEntries) {
                // only find resources that are published
                if (!PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry) && repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS) {
                    allRepoEntries.add(repositoryEntry);
                }
            }
        }
        // 3) search for all published learning resources that user owns
        final List<RepositoryEntry> repoEntries = queryByOwnerLimitAccess(identity, RepositoryEntry.ACC_USERS);
        for (final RepositoryEntry repositoryEntry : repoEntries) {
            if (!PersistenceHelper.listContainsObjectByKey(allRepoEntries, repositoryEntry)) {
                allRepoEntries.add(repositoryEntry);
            }
        }
        return allRepoEntries;
    }

    @Override
    public RepositoryEntry updateDisplaynameDescriptionOfRepositoryEntry(final RepositoryEntry repositoryEntry) {
        final RepositoryEntry reloaded = lookupRepositoryEntry(repositoryEntry.getKey());
        reloaded.setDisplayname(repositoryEntry.getDisplayname());
        reloaded.setDescription(repositoryEntry.getDescription());
        updateRepositoryEntry(reloaded);
        return reloaded;
    }

    /**
     * @param displayName
     * @param description
     */
    @Override
    public RepositoryEntry updateNewRepositoryEntry(final RepositoryEntry repositoryEntry) {
        // Do set access for owner at the end, because unfinished course should be invisible
        // (OLAT-5631) need a reload from hibernate because create a new cp load a repository-entry
        RepositoryEntry loadedEntry = repositoryDao.loadRepositoryEntry(repositoryEntry);
        loadedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
        loadedEntry.setDisplayname(repositoryEntry.getDisplayname());
        loadedEntry.setDescription(repositoryEntry.getDescription());
        updateRepositoryEntry(loadedEntry);
        return loadedEntry;
    }

    /* STATIC_METHOD_REFACTORING moved from RepositoryEntryImageController */
    /**
     * Copy the repo entry image from the source to the target repository entry. If the source repo entry does not exists, nothing will happen
     * 
     * @param src
     * @param target
     * @return
     */
    @Override
    public boolean copyImage(final RepositoryEntry src, final RepositoryEntry target) {
        final File srcFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()), getImageFilename(src));
        final File targetFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()), getImageFilename(target));
        if (srcFile.exists()) {
            try {
                FileUtils.bcopy(srcFile, targetFile, "copyRepoImageFile");
            } catch (final IOException ioe) {
                return false;
            }
        }
        return true;
    }

    /* STATIC_METHOD_REFACTORING moved from RepositoryEntryImageController */
    /**
     * Check if the repo entry does have an images and if yes create an image component that displays the image of this repo entry.
     * 
     * @param componentName
     * @param repositoryEntry
     * @return The image component or NULL if the repo entry does not have an image
     */
    @Override
    public ImageComponent getImageComponentForRepositoryEntry(final String componentName, final RepositoryEntry repositoryEntry) {
        final File repositoryEntryImageFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()), getImageFilename(repositoryEntry));
        if (!repositoryEntryImageFile.exists()) {
            return null;
        }
        final ImageComponent imageComponent = new ImageComponent(componentName);
        imageComponent.setMediaResource(new FileMediaResource(repositoryEntryImageFile));
        return imageComponent;
    }

    /* STATIC_METHOD_REFACTORING moved from RepositoryEntryImageController and made public for using there */
    /**
     * Internal helper to create the image name
     * 
     * @param re
     * @return
     */
    public static String getImageFilename(final RepositoryEntry re) {
        return re.getResourceableId() + ".jpg";
    }

    /**
     * attach object to Hibernate session
     * 
     * @param repositoryEntry
     * @return attached Hibernate object
     */
    @Override
    public RepositoryEntry loadRepositoryEntry(final RepositoryEntry repositoryEntry) {
        return repositoryDao.loadRepositoryEntry(repositoryEntry);
    }

    @Override
    public Long getRepositoryEntryIdFromResourceable(Long resourceableId, String resourceableTypeName) {
        OLATResource olatResource = olatresourceManager.findResourceable(resourceableId, resourceableTypeName);
        return lookupRepositoryEntry(olatResource, true).getKey();
    }

}
