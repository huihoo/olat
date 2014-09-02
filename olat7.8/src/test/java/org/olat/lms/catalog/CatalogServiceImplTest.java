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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.catalog.CatalogDao;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.catalog.CatalogEntryImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.repository.delete.DeletionModule;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Tests for Catalog-service.
 * 
 * @author Christian Guretzki
 */
public class CatalogServiceImplTest {

    private CatalogDao catalogDaoMock;
    private CatalogServiceImpl catalogService;
    private BaseSecurity securityManagerMock;
    private BookmarkService bookmarkServiceMock;
    private UserDeletionManager userDeletionMock;
    private DeletionModule deletionModule;

    // common test objects
    Identity myIdentity;
    CatalogEntry commonTestCatalogEntryMock;
    // list of used catalog-entry keys
    int catalogEntryKey1 = 1;
    int catalogEntryKey2 = 2;
    int catalogEntryKey3 = 3;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        catalogDaoMock = mock(CatalogDao.class);
        securityManagerMock = mock(BaseSecurity.class);
        userDeletionMock = mock(UserDeletionManager.class);
        deletionModule = mock(DeletionModule.class);
        catalogService = new CatalogServiceImpl(catalogDaoMock, securityManagerMock, bookmarkServiceMock, deletionModule);

        myIdentity = mock(Identity.class);
        commonTestCatalogEntryMock = createCatalogEntryMockOfTypeLeaf(catalogEntryKey1);
    }

    /**
     * Test method 'filterOwnedLeafs', check empty input-list will be return empty filtered list. Input : empty list Output: empty list
     */
    @Test
    public void testFilterOwnedLeafs_emptyInputList() {
        List filteredList = catalogService.filterOwnedLeafs(myIdentity, new ArrayList());
        assertNotNull("result could not be null", filteredList);
        assertTrue("Should filter out catalog-entry of TYPE_NODE", filteredList.isEmpty());
    }

    /**
     * Test method 'filterOwnedLeafs', check if catalog-entry of type NODE will be filtered out. Input: List with CatalogEntry of TYPE_NODE Output: Empty List
     */
    @Test
    public void testFilterOwnedLeafs_FindNoTypeNode() {
        List catalogEntries = new ArrayList();
        catalogEntries.add(createCatalogEntryMockOfTypeNode(catalogEntryKey2));

        List filteredList = catalogService.filterOwnedLeafs(myIdentity, catalogEntries);
        assertNotNull("result could not be null", filteredList);
        assertTrue("Should filter out catalog-entry of TYPE_NODE", filteredList.isEmpty());
    }

    /**
     * Test method 'filterOwnedLeafs', check if catalog-entry leafs of owner will be found. Input: List with CatalogEntry of TYPE_LEAF with myIdentity as owner Output:
     * Empty List with one entry
     */
    @Test
    public void testFilterOwnedLeafs_FindTypeLeafAsOwner() {
        List catalogEntries = new ArrayList();
        catalogEntries.add(commonTestCatalogEntryMock);

        SecurityGroup ownerGroupMock = commonTestCatalogEntryMock.getOwnerGroup();

        when(securityManagerMock.isIdentityInSecurityGroup(myIdentity, ownerGroupMock)).thenReturn(true);

        List filteredList = catalogService.filterOwnedLeafs(myIdentity, catalogEntries);
        assertNotNull("result could not be null", filteredList);
        assertEquals("Missing catalog-entry in filtered list, type_leaf and owner must be in list", 1, filteredList.size());
    }

    /**
     * Test method 'filterOwnedLeafs', check if filtered list is empty when identity is not owner. Input: List with CatalogEntry of TYPE_LEAF with myIdentity as owner
     * Output: Empty List with one entry
     */
    @Test
    public void testFilterOwnedLeafs_FoundNothingTypeLeafNotOwner() {
        List catalogEntries = new ArrayList();

        catalogEntries.add(commonTestCatalogEntryMock);
        SecurityGroup ownerGroup = commonTestCatalogEntryMock.getOwnerGroup();

        when(securityManagerMock.isIdentityInSecurityGroup(myIdentity, ownerGroup)).thenReturn(false);

        List filteredList = catalogService.filterOwnedLeafs(myIdentity, catalogEntries);
        assertNotNull("result could not be null", filteredList);
        assertTrue("Result list should be empty, type_leaf and not-owner must be filtered", filteredList.isEmpty());
    }

    /**
     * Test method 'deleteCatalogEntry', check if deleteSecurityGroup of securityManager is NOT called when a catalog-entry has no owner-group. Check if
     * deleteCatalogEntry is called on catalogDao.
     */
    @Test
    public void testDeleteCatalogEntry_WithoutOwnerGroup() {
        when(commonTestCatalogEntryMock.getOwnerGroup()).thenReturn(null);

        catalogService.deleteCatalogEntry(commonTestCatalogEntryMock);
        verify(catalogDaoMock).deleteCatalogEntry(commonTestCatalogEntryMock);
        SecurityGroup ownerGroup = commonTestCatalogEntryMock.getOwnerGroup();
        verify(securityManagerMock, never()).deleteSecurityGroup(ownerGroup);
    }

    /**
     * Test method 'deleteCatalogEntry', check if method 'deleteSecurityGroup' of securityManager is called when a catalog-entry has an owner-group. Check if method
     * 'deleteCatalogEntry' is called on catalogDao.
     */
    @Test
    public void testDeleteCatalogEntry_WithOwnerGroup() {
        SecurityGroup ownerGroup = commonTestCatalogEntryMock.getOwnerGroup();

        catalogService.deleteCatalogEntry(commonTestCatalogEntryMock);
        verify(catalogDaoMock).deleteCatalogEntry(commonTestCatalogEntryMock);
        verify(securityManagerMock).deleteSecurityGroup(ownerGroup);
    }

    /**
     * Test method 'addCatalogEntry(catalogParentEntry, catalogEntry), check if parent-catalog-entry is set after call addCatalogEntry(). Input : catalogEntry,
     * parentCatalogEntry Result: catalogEntry with parent=parentCatalogEntry
     */
    @Test
    public void testAddCatalogEntry() {
        CatalogEntry catalogEntry = new CatalogEntryImpl();
        catalogService.addCatalogEntry(commonTestCatalogEntryMock, catalogEntry);
        assertNotNull("Parent attribute should not be null", catalogEntry.getParent());
        assertTrue("Parent attribute is not set correctly", catalogEntry.getParent() == commonTestCatalogEntryMock);
    }

    /**
     * Test method 'moveCatalogEntry', check catalog-entry could not be moved to catalog-entry of type LEAF. Verify that method 'setParent' of 'toBeMovedEntry' is not
     * called. Input : new-parent-catalog-entry of type LEAF Result: return false
     */
    @Test
    public void testMoveCatalogEntry_CouldNotMoveToLeaf() {
        CatalogEntry newParentEntry = createCatalogEntryMockOfTypeLeaf(catalogEntryKey2);
        when(catalogDaoMock.loadCatalogEntry(commonTestCatalogEntryMock)).thenReturn(commonTestCatalogEntryMock);
        when(catalogDaoMock.loadCatalogEntry(newParentEntry)).thenReturn(newParentEntry);

        CatalogService spyedCatalogService = spy(catalogService);
        boolean result = spyedCatalogService.moveCatalogEntry(commonTestCatalogEntryMock, newParentEntry);
        assertFalse("Catalog-entry sould not be moved to catalog-entry of type LEAF", result);
        verify(commonTestCatalogEntryMock, never()).setParent(newParentEntry);
        verify(spyedCatalogService, never()).updateCatalogEntry(newParentEntry);
    }

    /**
     * Test method 'moveCatalogEntry', check catalog-entry could not be moved to catalog-entry of type LEAF. Verify that method 'setParent' of 'toBeMovedEntry' is not
     * called. Input : new-parent-catalog-entry of type LEAF Result: return false
     */
    @Test
    public void testMoveCatalogEntry_sameCatalogEntry() {
        CatalogService spyedCatalogService = spy(catalogService);
        when(catalogDaoMock.loadCatalogEntry(commonTestCatalogEntryMock)).thenReturn(commonTestCatalogEntryMock);
        boolean result = spyedCatalogService.moveCatalogEntry(commonTestCatalogEntryMock, commonTestCatalogEntryMock);
        assertFalse("Catalog-entry sould not be moved to catalog-entry of type LEAF", result);
        verify(commonTestCatalogEntryMock, never()).setParent(commonTestCatalogEntryMock);
        verify(spyedCatalogService, never()).updateCatalogEntry(commonTestCatalogEntryMock);
    }

    /**
     * Test method 'moveCatalogEntry', check catalog-entry could not be moved to catalog-entry of type LEAF. Verify that method 'setParent' of 'toBeMovedEntry' is not
     * called. Input : new-parent-catalog-entry of type LEAF Result: return false
     */
    @Test
    public void testMoveCatalogEntry_movedCatalogEntry() {
        CatalogEntry newParentEntry = createCatalogEntryMockOfTypeNode(catalogEntryKey2);
        when(catalogDaoMock.loadCatalogEntry(commonTestCatalogEntryMock)).thenReturn(commonTestCatalogEntryMock);
        when(catalogDaoMock.loadCatalogEntry(newParentEntry)).thenReturn(newParentEntry);

        CatalogService spyedCatalogService = spy(catalogService);
        boolean result = spyedCatalogService.moveCatalogEntry(commonTestCatalogEntryMock, newParentEntry);
        assertTrue("Catalog-entry sould not be moved to catalog-entry of type LEAF", result);
        verify(commonTestCatalogEntryMock).setParent(newParentEntry);
        verify(spyedCatalogService).updateCatalogEntry(commonTestCatalogEntryMock);
    }

    /**
     * Test method 'resourceableDeleted'. Input : List with two catalog-entry of type leaf Result: Service method 'deleteCatalogEntry' is called for the two
     * catalog-entries.
     */
    @Test
    public void testResourceableDeleted() {
        RepositoryEntry repositoryEntry = mock(RepositoryEntry.class);
        List catalogEntryReferencingList = new ArrayList();
        catalogEntryReferencingList.add(commonTestCatalogEntryMock);
        CatalogEntry catalogEntry2 = createCatalogEntryMockOfTypeLeaf(catalogEntryKey2);
        catalogEntryReferencingList.add(catalogEntry2);
        when(catalogDaoMock.getCatalogEntriesReferencing(repositoryEntry)).thenReturn(catalogEntryReferencingList);

        CatalogService spyedCatalogService = spy(catalogService);
        spyedCatalogService.resourceableDeleted(repositoryEntry);
        verify(spyedCatalogService).deleteCatalogEntry(commonTestCatalogEntryMock);
        verify(spyedCatalogService).deleteCatalogEntry(catalogEntry2);
    }

    /**
     * Test method 'deleteUserData' when dleted user is not last owner of catalog-entry. Input : List of catalog-entry with one entry Result: Identity would be removed
     * from owner group by calling 'removeIdentityFromSecurityGroup'.
     */
    @Test
    public void testDeleteUserData_notLastOwner() {
        Identity adminIdentity = mock(Identity.class);
        String newDeletedUserName = "newDeletedUserName";
        CatalogService spyedCatalogService = spy(catalogService);
        List listOfMyCatalogEntries = new ArrayList();
        SecurityGroup ownerGroup = commonTestCatalogEntryMock.getOwnerGroup();
        listOfMyCatalogEntries.add(commonTestCatalogEntryMock);

        when(spyedCatalogService.getCatalogEntriesOwnedBy(myIdentity)).thenReturn(listOfMyCatalogEntries);
        // not last user => return 1
        when(securityManagerMock.countIdentitiesOfSecurityGroup(ownerGroup)).thenReturn(1);
        when(userDeletionMock.getAdminIdentity()).thenReturn(adminIdentity);
        spyedCatalogService.deleteUserData(myIdentity, newDeletedUserName);

        verify(securityManagerMock).removeIdentityFromSecurityGroup(myIdentity, ownerGroup);
        verify(securityManagerMock, never()).addIdentityToSecurityGroup(adminIdentity, ownerGroup);
    }

    /**
     * Test method 'deleteUserData' when dleted user is not last owner of catalog-entry. Input : List of catalog-entry with one entry Result: Identity would be removed
     * from owner group by calling 'removeIdentityFromSecurityGroup'.
     */
    @Test
    public void testDeleteUserData_identityIsLastOwner() {
        Identity adminIdentity = mock(Identity.class);
        CatalogService spyedCatalogService = spy(catalogService);
        List listOfMyCatalogEntries = new ArrayList();
        listOfMyCatalogEntries.add(commonTestCatalogEntryMock);
        SecurityGroup ownerGroup = commonTestCatalogEntryMock.getOwnerGroup();
        when(spyedCatalogService.getCatalogEntriesOwnedBy(myIdentity)).thenReturn(listOfMyCatalogEntries);
        // last user => return 0
        when(securityManagerMock.countIdentitiesOfSecurityGroup(ownerGroup)).thenReturn(0);
        when(userDeletionMock.getAdminIdentity()).thenReturn(adminIdentity);
        when(deletionModule.getAdminUserIdentity()).thenReturn(adminIdentity);
        String newDeletedUserName = "newDeletedUserName";
        spyedCatalogService.deleteUserData(myIdentity, newDeletedUserName);

        verify(securityManagerMock).removeIdentityFromSecurityGroup(myIdentity, ownerGroup);
        verify(securityManagerMock).addIdentityToSecurityGroup(adminIdentity, ownerGroup);
    }

    /**
     * Test method 'isEntryWithinCategory', catalog-entry has other category. CatalogEntry 1 (root) => CatalogEntry 2 CatalogEntry 3
     */
    @Test
    public void testIsEntryWithinCategory_notFound() {

        List entriesList = new ArrayList();
        entriesList.add(createParentCatalogEntryWithChildren());

        CatalogEntry toBeCheckedEntry = mock(CatalogEntry.class);
        when(toBeCheckedEntry.getKey()).thenReturn(new Long(catalogEntryKey3));

        assertFalse(catalogService.isEntryWithinCategory(toBeCheckedEntry, entriesList));
    }

    /**
     * Test method 'isEntryWithinCategory' , catalog-entry is in same category. CatalogEntry 1 (root) => CatalogEntry 2 => CatalogEntry 3
     */
    @Test
    public void testIsEntryWithinCategory_found() {
        List entriesList = new ArrayList();
        CatalogEntry parentCatalogEntryRoot = createParentCatalogEntryWithChildren();
        entriesList.add(parentCatalogEntryRoot);

        CatalogEntry toBeCheckedEntry = mock(CatalogEntry.class);
        when(toBeCheckedEntry.getKey()).thenReturn(new Long(catalogEntryKey2));
        when(toBeCheckedEntry.getParent()).thenReturn(parentCatalogEntryRoot);

        assertTrue(catalogService.isEntryWithinCategory(toBeCheckedEntry, entriesList));
    }

    /**
     * Test method 'createOLATResouceableFor' with parameter null. Input : null Result: null
     */
    @Test
    public void testCreateOLATResouceableFor_nullInput() {
        OLATResourceable olatResourcable = catalogService.createOLATResouceableFor(null);
        assertNull("null input must return null", olatResourcable);
    }

    /**
     * Test method 'createOLATResouceableFor' , with catalog-entry as parameter. Input : catalog-entry (key=1) Result: OLATResourceable must have same key like
     * catalog-entry.
     */
    @Test
    public void testCreateOLATResouceableFor_withCatalogEntry() {
        OLATResourceable olatResourcable = catalogService.createOLATResouceableFor(commonTestCatalogEntryMock);
        assertNotNull("Must return olatResourcable object and not null", olatResourcable);
        assertEquals("OLATResourceable has not same key like catalog-entry", olatResourcable.getResourceableId(), commonTestCatalogEntryMock.getKey());
        assertEquals("Wrong OLATResourceable type name", olatResourcable.getResourceableTypeName(), CatalogService.CATALOGENTRY);
    }

    // ////////////////////////
    // Helper : Mock creator's
    // ////////////////////////
    /**
     * 
     * @param key
     *            key-value of catalog-entry which will be created
     * @return catalog entry of type LEAF
     */
    private CatalogEntry createCatalogEntryMockOfTypeLeaf(int key) {
        CatalogEntry catalogEntry = mock(CatalogEntry.class);
        when(catalogEntry.getType()).thenReturn(CatalogEntry.TYPE_LEAF);
        when(catalogEntry.getKey()).thenReturn(new Long(key));
        SecurityGroup ownerGroup = mock(SecurityGroup.class);
        when(catalogEntry.getOwnerGroup()).thenReturn(ownerGroup);
        RepositoryEntry repositoryEntryMock = mock(RepositoryEntry.class);
        when(catalogEntry.getRepositoryEntry()).thenReturn(repositoryEntryMock);
        when(repositoryEntryMock.getOwnerGroup()).thenReturn(ownerGroup);
        return catalogEntry;
    }

    /**
     * 
     * @param key
     *            key-value of catalog-entry which will be created
     * @return catalog entry of type NODE
     */
    private CatalogEntry createCatalogEntryMockOfTypeNode(int key) {
        CatalogEntry catalogEntry = mock(CatalogEntry.class);
        when(catalogEntry.getType()).thenReturn(CatalogEntry.TYPE_NODE);
        when(catalogEntry.getKey()).thenReturn(new Long(key));
        return catalogEntry;
    }

    /*
     * @return chain : CatalogEntry 1 (root) => CatalogEntry 2 (child of root) => CatalogEntry 3 (child of 2)
     */
    private CatalogEntry createParentCatalogEntryWithChildren() {
        // root catalog-entry => parent = null
        CatalogEntry parentCatalogEntry = mock(CatalogEntry.class);
        when(parentCatalogEntry.getKey()).thenReturn(new Long(catalogEntryKey1));
        when(parentCatalogEntry.getParent()).thenReturn(null);
        // child catalog-entry => parent = root catalog-entry
        CatalogEntry catalogEntryChild1 = mock(CatalogEntry.class);
        when(catalogEntryChild1.getKey()).thenReturn(new Long(catalogEntryKey2));
        when(catalogEntryChild1.getParent()).thenReturn(parentCatalogEntry);
        CatalogEntry catalogEntryChild2 = mock(CatalogEntry.class);
        when(catalogEntryChild2.getKey()).thenReturn(new Long(catalogEntryKey3));
        when(catalogEntryChild2.getParent()).thenReturn(catalogEntryChild1);
        return parentCatalogEntry;
    }

}
