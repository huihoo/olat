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

package org.olat.lms.catalog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.catalog.CatalogDao;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.repository.delete.DeletionModule;
import org.olat.lms.user.UserDataDeletable;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The CatalogService is responsible to provides access methods to retrieve structures of CatalogEntries for a given CatalogEntry, e.g. children, catalog entries which
 * act as roots, delete subcategory structure.
 * <p>
 * Moreover it also has access methods providing all catalog entries referencing a given repository entry.
 * <p>
 * The CatalogService also provides hooks used by the repository entry manager to signal changes on a repository entry which might have changed. Such changes can invoke
 * the removal from the catalog, e.g. restricting access, deleting a repository entry.
 * 
 * @author Felix Jost, Christian Guretzki
 */
public class CatalogServiceImpl extends BasicManager implements UserDataDeletable, Initializable, CatalogService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private CatalogDao catalogDao;

    @Autowired
    private BaseSecurity securityManager;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    DeletionModule deletionModule;

    /**
     * [spring]
     */
    @SuppressWarnings("unused")
    private CatalogServiceImpl() {
        super();
    }

    /**
     * [Testing]
     */
    CatalogServiceImpl(CatalogDao catalogDao, BaseSecurity securityManager, BookmarkService bookmarkService, DeletionModule deletionModule) {
        this.catalogDao = catalogDao;
        this.securityManager = securityManager;
        this.bookmarkService = bookmarkService;
        this.deletionModule = deletionModule;
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#createCatalogEntry()
     */
    @Override
    public CatalogEntry createCatalogEntry() {
        return catalogDao.createCatalogEntry();
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#getChildrenOf(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public List<CatalogEntry> getChildrenOf(final CatalogEntry ce) {
        return catalogDao.getChildrenOf(ce);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#getAllCatalogNodes()
     */
    @Override
    public List<CatalogEntry> getAllCatalogNodes() {
        return catalogDao.getAllCatalogNodes();
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#hasChildEntries(org.olat.data.catalog.CatalogEntry, int)
     */
    @Override
    public boolean hasChildEntries(final CatalogEntry ce, final int type) {
        return catalogDao.hasChildEntries(ce, type);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#filterOwnedLeafs(org.olat.data.basesecurity.Identity, java.util.List)
     */
    @Override
    public List filterOwnedLeafs(final Identity identity, final List catalogEntries) {
        final List ownedEntries = new ArrayList();

        final Iterator iter = catalogEntries.iterator();
        while (iter.hasNext()) {
            final CatalogEntry cate = (CatalogEntry) iter.next();
            if (cate.getType() == CatalogEntry.TYPE_LEAF) {
                final RepositoryEntry repe = cate.getRepositoryEntry();
                final SecurityGroup secGroup = repe.getOwnerGroup();
                if (securityManager.isIdentityInSecurityGroup(identity, secGroup)) {
                    ownedEntries.add(cate);
                }
            }
        }
        return ownedEntries;
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#loadCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public CatalogEntry loadCatalogEntry(final CatalogEntry catalogEntry) {
        return catalogDao.loadCatalogEntry(catalogEntry);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#loadCatalogEntry(java.lang.Long)
     */
    @Override
    public CatalogEntry loadCatalogEntry(final Long catEntryId) {
        return catalogDao.loadCatalogEntry(catEntryId);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#saveCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void saveCatalogEntry(final CatalogEntry ce) {
        catalogDao.saveCatalogEntry(ce);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#updateCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void updateCatalogEntry(final CatalogEntry ce) {
        catalogDao.updateCatalogEntry(ce);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#deleteCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void deleteCatalogEntry(final CatalogEntry ce) {
        log.debug("deleteCatalogEntry start... ce=" + ce);
        if (ce.getType() == CatalogEntry.TYPE_LEAF) {
            // delete catalog entry, then delete owner group
            final SecurityGroup owner = ce.getOwnerGroup();
            catalogDao.deleteCatalogEntry(ce);
            if (owner != null) {
                log.debug("deleteCatalogEntry case_1: delete owner-group=" + owner);
                securityManager.deleteSecurityGroup(owner);
            }
        } else {
            final List secGroupsToBeDeleted = new ArrayList();
            // FIXME pb: the transaction must also include the deletion of the security
            // groups. Why not using this method as a recursion and seperating the
            // deletion of the ce and the groups by collecting the groups? IMHO there
            // are not less db queries. This way the code is much less clear, e.g. the method
            // deleteCatalogSubtree does not really delete the subtree, it leaves the
            // security groups behind. I would preferre to have one delete method that
            // deletes its children first by calling itself on the children and then deletes
            // itself ant its security group. The nested transaction that occures is actually
            // not a problem, the DB object can handel this.
            deleteCatalogSubtree(ce, secGroupsToBeDeleted);
            // after deleting all entries, delete all secGroups corresponding
            for (final Iterator iter = secGroupsToBeDeleted.iterator(); iter.hasNext();) {
                final SecurityGroup grp = (SecurityGroup) iter.next();
                log.debug("deleteCatalogEntry case_2: delete groups of deleteCatalogSubtree grp=" + grp);
                securityManager.deleteSecurityGroup(grp);
            }
        }
        log.debug("deleteCatalogEntry END");
    }

    /**
     * recursively delete the structure starting from the catalog entry.
     * 
     * @param ce
     */
    private void deleteCatalogSubtree(CatalogEntry ce, final List secGroupsToBeDeleted) {

        final List children = getChildrenOf(ce);
        final Iterator iter = children.iterator();
        while (iter.hasNext()) {
            final CatalogEntry nextCe = (CatalogEntry) iter.next();
            deleteCatalogSubtree(nextCe, secGroupsToBeDeleted);
        }
        CatalogEntry reloadedCe = (CatalogEntry) catalogDao.loadCatalogEntry(ce);
        // mark owner group for deletion.
        final SecurityGroup owner = reloadedCe.getOwnerGroup();
        if (owner != null) {
            secGroupsToBeDeleted.add(owner);
        }
        // delete user bookmarks
        final OLATResourceable ores = createOLATResouceableFor(reloadedCe);
        bookmarkService.deleteAllBookmarksFor(ores);
        // delete catalog entry itself
        catalogDao.deleteCatalogEntry(reloadedCe);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#getCatalogCategoriesFor(org.olat.data.repository.RepositoryEntry)
     */
    @Override
    public List getCatalogCategoriesFor(final RepositoryEntry repoEntry) {
        return catalogDao.getCatalogCategoriesFor(repoEntry);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#getCatalogEntriesOwnedBy(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List getCatalogEntriesOwnedBy(final Identity identity) {
        return catalogDao.getCatalogEntriesOwnedBy(identity);

    }

    /**
     * @see org.olat.lms.catalog.CatalogService#addCatalogEntry(org.olat.data.catalog.CatalogEntry, org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void addCatalogEntry(final CatalogEntry parent, final CatalogEntry newEntry) {
        log.debug("addCatalogEntry parent=" + parent);
        newEntry.setParent(parent);
        log.debug("addCatalogEntry newEntry=" + newEntry);
        log.debug("addCatalogEntry newEntry.getOwnerGroup()=" + newEntry.getOwnerGroup());
        saveCatalogEntry(newEntry);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#getRootCatalogEntries()
     */
    @Override
    public List getRootCatalogEntries() {
        return catalogDao.getRootCatalogEntries();
    }

    /**
	 */
    @Override
    public void init() {

        final List roots = getRootCatalogEntries();
        if (roots.isEmpty()) { // not initialized yet
            // copy a snapshot of olatAdmins into catalogAdmins do not put secMgr.findSecurityGroupByName(Constants.GROUP_ADMIN) directly into a CatalogEntry!!
            final SecurityGroup olatAdmins = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
            final List olatAdminIdents = securityManager.getIdentitiesOfSecurityGroup(olatAdmins);
            final SecurityGroup catalogAdmins = securityManager.createAndPersistSecurityGroup();
            for (int i = 0; i < olatAdminIdents.size(); i++) {
                securityManager.addIdentityToSecurityGroup((Identity) olatAdminIdents.get(i), catalogAdmins);
            }
            /*
             * start with something called CATALOGROOT, you can rename it to whatever name you like later as OLATAdmin
             */
            // parent == null -> no parent -> I am a root node.
            saveCatEntry(CATALOGROOT, null, CatalogEntry.TYPE_NODE, catalogAdmins, null, null);
        }
    }

    private CatalogEntry saveCatEntry(final String name, final String desc, final int type, final SecurityGroup ownerGroup, final RepositoryEntry repoEntry,
            final CatalogEntry parent) {
        final CatalogEntry ce = createCatalogEntry();
        ce.setName(name);
        ce.setDescription(desc);
        ce.setOwnerGroup(ownerGroup);
        ce.setRepositoryEntry(repoEntry);
        ce.setParent(parent);
        ce.setType(type);
        saveCatalogEntry(ce);
        return ce;
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#moveCatalogEntry(org.olat.data.catalog.CatalogEntry, org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public boolean moveCatalogEntry(CatalogEntry toBeMovedEntry, CatalogEntry newParentEntry) {
        // reload current item to prevent stale object modification
        toBeMovedEntry = this.loadCatalogEntry(toBeMovedEntry);
        newParentEntry = this.loadCatalogEntry(newParentEntry);
        // check that the new parent is not a leaf
        if (newParentEntry.getType() == CatalogEntry.TYPE_LEAF) {
            return false;
        }
        // check that the new parent is not a child of the to be moved entry
        CatalogEntry tempEntry = newParentEntry;
        while (tempEntry != null) {
            if (tempEntry.getKey().equals(toBeMovedEntry.getKey())) {
                // ups, the new parent is within the to be moved entry - abort
                return false;
            }
            tempEntry = tempEntry.getParent();
        }
        // set new parent and save
        toBeMovedEntry.setParent(newParentEntry);
        this.updateCatalogEntry(toBeMovedEntry);
        return true;
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#resourceableDeleted(org.olat.data.repository.RepositoryEntry)
     */
    @Override
    public void resourceableDeleted(final RepositoryEntry repositoryEntry) {
        // if a repository entry gets deleted, the referencing Catalog Entries gets
        // retired to
        log.debug("sourceableDeleted start... repositoryEntry=" + repositoryEntry);
        final List references = catalogDao.getCatalogEntriesReferencing(repositoryEntry);
        if (references != null && !references.isEmpty()) {
            for (int i = 0; i < references.size(); i++) {
                deleteCatalogEntry((CatalogEntry) references.get(i));
            }
        }
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#deleteUserData(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        // Remove as owner
        final List catalogEntries = getCatalogEntriesOwnedBy(identity);
        for (final Iterator iter = catalogEntries.iterator(); iter.hasNext();) {
            final CatalogEntry catalogEntry = (CatalogEntry) iter.next();

            securityManager.removeIdentityFromSecurityGroup(identity, catalogEntry.getOwnerGroup());
            if (securityManager.countIdentitiesOfSecurityGroup(catalogEntry.getOwnerGroup()) == 0) {
                // This group has no owner anymore => add OLAT-Admin as owner
                securityManager.addIdentityToSecurityGroup(deletionModule.getAdminUserIdentity(), catalogEntry.getOwnerGroup());
                log.info("Delete user-data , add Administrator-identity as owner of catalogEntry=" + catalogEntry.getName());
            }
        }
        log.debug("All owner entries in catalog deleted for identity=" + identity);
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#isEntryWithinCategory(org.olat.data.catalog.CatalogEntry, java.util.List)
     */
    @Override
    public boolean isEntryWithinCategory(final CatalogEntry toBeCheckedEntry, final List<CatalogEntry> entriesList) {
        CatalogEntry tempEntry = toBeCheckedEntry;
        while (tempEntry != null) {
            if (PersistenceHelper.listContainsObjectByKey(entriesList, tempEntry)) {
                return true;
            }
            tempEntry = tempEntry.getParent();
        }
        return false;
    }

    /**
     * @see org.olat.lms.catalog.CatalogService#createOLATResouceableFor(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public OLATResourceable createOLATResouceableFor(final CatalogEntry currentCatalogEntry) {
        if (currentCatalogEntry == null) {
            return null;
        }
        return new OLATResourceable() {
            @Override
            public Long getResourceableId() {
                return new Long(currentCatalogEntry.getKey());
            }

            @Override
            public String getResourceableTypeName() {
                return CATALOGENTRY;
            }
        };
    }

    @Override
    public CatalogEntry createCatalogEntryLeaf(RepositoryEntry repositoryEntry, String parentNodeIdent) {
        final Long newParentId = Long.parseLong(parentNodeIdent);
        final CatalogEntry parentEntry = loadCatalogEntry(newParentId);
        return createCatalogEntryLeafAndAddParent(repositoryEntry, parentEntry);
    }

    @Override
    public CatalogEntry createCatalogEntryLeafAndAddParent(RepositoryEntry repositoryEntry, CatalogEntry parentCatalogEntry) {
        CatalogEntry newEntry = createCatalogEntry();
        newEntry.setRepositoryEntry(repositoryEntry);
        newEntry.setName(repositoryEntry.getDisplayname());
        newEntry.setDescription(repositoryEntry.getDescription());
        newEntry.setType(CatalogEntry.TYPE_LEAF);
        newEntry.setOwnerGroup(securityManager.createAndPersistSecurityGroup());
        addCatalogEntry(parentCatalogEntry, newEntry);
        return newEntry;
    }

    @Override
    public boolean hasParentAllreadyCatalogEntryAsChild(String nodeIdent, RepositoryEntry toBeAddedEntry) {
        final Long newParentId = Long.parseLong(nodeIdent);
        final CatalogEntry newParent = loadCatalogEntry(newParentId);
        // check first if this repo entry is already attached to this new parent
        final List<CatalogEntry> existingChildren = getChildrenOf(newParent);
        for (final CatalogEntry existingChild : existingChildren) {
            final RepositoryEntry existingRepoEntry = existingChild.getRepositoryEntry();
            if (existingRepoEntry != null && existingRepoEntry.equalsByPersistableKey(toBeAddedEntry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to imports simple tree structure, for simplicity
     */
    public CatalogEntry importStructure() {
        final CatalogEntry oldRoot = (CatalogEntry) getRootCatalogEntries().get(0);
        final SecurityGroup rootOwners = oldRoot.getOwnerGroup();
        final List olatAdminIdents = securityManager.getIdentitiesOfSecurityGroup(rootOwners);
        final SecurityGroup catalogAdmins = securityManager.createAndPersistSecurityGroup();
        for (int i = 0; i < olatAdminIdents.size(); i++) {
            securityManager.addIdentityToSecurityGroup((Identity) olatAdminIdents.get(i), catalogAdmins);
        }
        deleteCatalogEntry(oldRoot);

        final CatalogEntry dummy = createCatalogEntry();
        final String structure = dummy.getDescription();
        final String[] lines = structure.split("\n");
        final Stack<CatalogEntry> treeStack = new Stack<CatalogEntry>();
        //
        final CatalogEntry newRoot = createCatalogEntry();
        newRoot.setParent(null);
        newRoot.setType(CatalogEntry.TYPE_NODE);
        newRoot.setDescription("fill it");
        newRoot.setName(lines[0]);
        newRoot.setOwnerGroup(catalogAdmins);
        saveCatalogEntry(newRoot);
        treeStack.push(newRoot);
        for (int i = 1; i < lines.length; i++) {
            int level = 0;
            int pos = 0;
            while ("".equals(lines[i].substring(pos, pos + 2).trim())) {
                level++;
                pos += 3;
            }
            final CatalogEntry tmp = createCatalogEntry();
            tmp.setType(CatalogEntry.TYPE_NODE);
            tmp.setDescription("fill it");
            tmp.setName(lines[i].trim());
            if (treeStack.size() == level) {
                tmp.setParent(treeStack.lastElement());
                treeStack.push(tmp);
            } else if (treeStack.size() > level) {
                // moving towards root
                for (int ii = treeStack.size() - 1; ii >= level; ii--) {
                    treeStack.pop();
                }
                tmp.setParent(treeStack.lastElement());
                treeStack.push(tmp);
            }
            saveCatalogEntry(tmp);
        }
        return dummy;
    }

    @Override
    public boolean isOwner(CatalogEntry ce, Identity identity) {
        final SecurityGroup owners = ce.getOwnerGroup();
        if (owners != null) {
            return securityManager.isIdentityInSecurityGroup(identity, owners);
        }
        return false;
    }

    @Override
    public void addOwners(CatalogEntry catalogEntry, List<Identity> addIdentities) {
        for (final Identity identity : addIdentities) {
            if (!securityManager.isIdentityInSecurityGroup(identity, catalogEntry.getOwnerGroup())) {
                securityManager.addIdentityToSecurityGroup(identity, catalogEntry.getOwnerGroup());
            }
        }
    }

    @Override
    public void removeOwners(CatalogEntry catalogEntry, List<Identity> removeIdentities) {
        for (final Identity identity : removeIdentities) {
            securityManager.removeIdentityFromSecurityGroup(identity, catalogEntry.getOwnerGroup());
        }
    }

    @Override
    public CatalogEntry createCatalogEntryWithoutRepositoryEntry(CatalogEntry parentCatalogEntry) {
        CatalogEntry ce = createCatalogEntry();
        ce.setOwnerGroup(securityManager.createAndPersistSecurityGroup());
        ce.setRepositoryEntry(null);
        ce.setParent(parentCatalogEntry);
        return ce;
    }

    @Override
    public CatalogEntry setEmptyOwnerGroup(CatalogEntry currentCatalogEntry) {
        final CatalogEntry reloaded = loadCatalogEntry(currentCatalogEntry);
        SecurityGroup secGroup = securityManager.createAndPersistSecurityGroup();
        reloaded.setOwnerGroup(secGroup);
        saveCatalogEntry(reloaded);
        return reloaded;
    }

    /**
     * @param historyStack2
     * @return
     */
    public List<Identity> getCaretakerFormCatalogEntryList(List<CatalogEntry> historyStack) {
        List<Identity> tmpIdent = new ArrayList<Identity>();
        for (int i = historyStack.size() - 1; i >= 0 && tmpIdent.isEmpty(); i--) {
            // start at the selected category, the root category is asserted to
            // have the OLATAdministrator
            // so we end up having always at least one identity as receiver for a
            // request ;-)
            final CatalogEntry tmp = historyStack.get(i);
            final SecurityGroup tmpOwn = tmp.getOwnerGroup();
            if (tmpOwn != null) {
                tmpIdent = securityManager.getIdentitiesOfSecurityGroup(tmpOwn);
            }
        }
        return tmpIdent;
    }

}
