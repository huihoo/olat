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
package org.olat.lms.search.indexer;

import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.forum.Forum;
import org.olat.data.forum.ForumDao;
import org.olat.data.forum.Message;
import org.olat.data.forum.MessageImpl;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.lms.properties.PropertyParameterObject;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.search.SearchClientLocal;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.SearchService;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.searcher.SearchClient;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.webfeed.FeedManager;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Initial Date: 16.04.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class IndexerITCase extends OlatTestCase {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private static final SearchClient SEARCH_CLIENT = new SearchClientLocal();

    private static Identity OWNER_IDENTITY;

    private static Identity OTHER_IDENTITY;

    @Before
    public void setUp() {
        // we can't do this initialization stuff statically since we need Spring context in advance
        if (!INITIALIZED.get()) {
            OWNER_IDENTITY = JunitTestHelper.createAndPersistIdentityAsUser("owner");
            OTHER_IDENTITY = JunitTestHelper.createAndPersistIdentityAsUser("other");
            INITIALIZED.set(true);
        }
    }

    @Test
    public void callSearchService() throws Exception {
        createIndex();
        doSimpleSearch("");
    }

    @Test
    public void testGroupIndexing() throws Exception {
        final String groupName = "indexedBuddyGroupName";
        final String groupDesc = "indexedBuddyGroupDescription";

        createIndex();
        SearchResults result = doSimpleSearch(groupName);
        Assert.assertEquals(0, result.getTotalHits());

        BusinessGroupService businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        BusinessGroup group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, groupName, groupDesc, null, null, null,
                null, null);
        DBFactory.getInstance(false).commit();

        try {
            createIndex();
            result = doSimpleSearch(groupName);
            Assert.assertEquals(1, result.getTotalHits());

            businessGroupService.deleteBusinessGroup(group);
            DBFactory.getInstance(false).commit();
            createIndex();
            result = doSimpleSearch(groupName);
            Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining groups
                businessGroupService.deleteBusinessGroups(businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, null));
                DBFactory.getInstance(false).commitAndCloseSession();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Test
    public void testGroupFolderIndexing() throws Exception {
        final String folderFileContent = "indexedBuddyGroupFolderFileContent";

        // search before must not return any results
        createIndex();
        SearchResults result = doSimpleSearch(folderFileContent);
        Assert.assertEquals(0, result.getTotalHits());

        // create business group with folder and file containing some content
        BusinessGroupService businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        BusinessGroup group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, "indexedBuddyGroupName",
                "indexedBuddyGroupDescription", null, null, null, null, null);
        DBFactory.getInstance(false).commit();

        final String relPath = CollaborationTools.getFolderRelPath(group);
        final OlatRootFolderImpl rootContainer = new OlatRootFolderImpl(relPath, null);
        final VFSLeaf file = rootContainer.createChildLeaf("indexedBuddyGroupFolderFile.txt");
        final OutputStreamWriter out = new OutputStreamWriter(file.getOutputStream(false), "utf-8");
        out.write("indexedBuddyGroupFolderFileContent");
        out.flush();
        out.close();

        try {
            // now search has to find file content
            createIndex();
            result = doSimpleSearch(folderFileContent);
            Assert.assertEquals(1, result.getTotalHits());

            // delete business group again
            businessGroupService.deleteBusinessGroup(group);
            DBFactory.getInstance(false).commit();
            Assert.assertFalse("Group folder still exists after deletion of group", rootContainer.exists());

            // content must no longer be available
            createIndex();
            result = doSimpleSearch(folderFileContent);
            Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining groups
                businessGroupService.deleteBusinessGroups(businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, null));
                DBFactory.getInstance(false).commitAndCloseSession();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Test
    public void testGroupForumIndexing() throws Exception {
        final String forumMessageTitle = "indexedBuddyGroupForumTitle";

        // search before must not return any results
        createIndex();
        SearchResults result = doSimpleSearch(forumMessageTitle);
        Assert.assertEquals(0, result.getTotalHits());

        // create business group with forum
        BusinessGroupService businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        BusinessGroup group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, "indexedBuddyGroupName",
                "indexedBuddyGroupDescription", null, null, null, null, null);

        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(group).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_FORUM).build();
        final Forum forum = CoreSpringFactory.getBean(PropertyManagerEBL.class).getCollaborationToolsForum(propertyParameterObject);
        Message message = new MessageImpl();
        message.setForum(forum);
        message.setParent(null);
        message.setThreadtop(null);
        message.setTitle(forumMessageTitle);
        message.setCreator(OWNER_IDENTITY);
        CoreSpringFactory.getBean(ForumDao.class).saveMessage(message);
        DBFactory.getInstance(false).commit();

        try {
            // now search has to find file content
            createIndex();
            result = doSimpleSearch(forumMessageTitle);
            Assert.assertEquals(1, result.getTotalHits());

            // delete business group again
            businessGroupService.deleteBusinessGroup(group);
            DBFactory.getInstance(false).commit();

            // content must no longer be available
            createIndex();
            result = doSimpleSearch(forumMessageTitle);
            Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining groups
                businessGroupService.deleteBusinessGroups(businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, null));
                DBFactory.getInstance(false).commitAndCloseSession();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Test
    public void testGroupWikiIndexing() throws Exception {
        final String wikiPageName = "indexedBuddyGroupWikiName";
        final String wikiPageContent = "indexedBuddyGroupWikiContent";

        // search before must not return any results
        createIndex();
        SearchResults result = doSimpleSearch(wikiPageName);
        Assert.assertEquals(0, result.getTotalHits());
        result = doSimpleSearch(wikiPageContent);
        Assert.assertEquals(0, result.getTotalHits());

        // create business group with forum
        BusinessGroupService businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        BusinessGroup group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, "indexedBuddyGroupName",
                "indexedBuddyGroupDescription", null, null, null, null, null);

        Wiki wiki = WikiManager.getInstance().getOrLoadWiki(group);
        WikiPage wikiPage = new WikiPage(wikiPageName);
        wikiPage.setContent(wikiPageContent);
        wiki.addPage(wikiPage);
        WikiManager.getInstance().saveWikiPage(group, wikiPage, false, wiki);
        DBFactory.getInstance(false).commit();

        try {
            // now search has to find file content
            final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
            collabTools.setToolEnabled(CollaborationTools.TOOL_WIKI, true);
            createIndex();
            result = doSimpleSearch(wikiPageName);
            Assert.assertEquals(1, result.getTotalHits());
            result = doSimpleSearch(wikiPageContent);
            Assert.assertEquals(1, result.getTotalHits());

            // delete business group again
            businessGroupService.deleteBusinessGroup(group);
            DBFactory.getInstance(false).commit();

            // content must no longer be available
            createIndex();
            result = doSimpleSearch(wikiPageName);
            Assert.assertEquals(0, result.getTotalHits());
            result = doSimpleSearch(wikiPageContent);
            Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining groups
                businessGroupService.deleteBusinessGroups(businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, null));
                DBFactory.getInstance(false).commitAndCloseSession();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Test
    public void testGroupPortfolioIndexing() throws Exception {
        final String portfolioStructureTitle = "indexedBuddyGroupPortfolioStructureTitle";
        final String portfolioStructureDescription = "indexedBuddyGroupPortfolioStructureDescription";

        // search before must not return any results
        createIndex();
        SearchResults result = doSimpleSearch(portfolioStructureTitle);
        Assert.assertEquals(0, result.getTotalHits());
        result = doSimpleSearch(portfolioStructureDescription);
        Assert.assertEquals(0, result.getTotalHits());

        // create business group with forum
        BusinessGroupService businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        BusinessGroup group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, "indexedBuddyGroupName",
                "indexedBuddyGroupDescription", null, null, null, null, null);

        PropertyParameterObject propertyParameterObject = new PropertyParameterObject.Builder().resourceable(group).category(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS)
                .name(PropertyManagerEBL.KEY_PORTFOLIO).group(group).build();
        final PortfolioStructureMap map = CoreSpringFactory.getBean(PropertyManagerEBL.class).getCollaborationToolsPortfolioStructureMap(propertyParameterObject);
        map.setTitle(portfolioStructureTitle);
        map.setDescription(portfolioStructureDescription);
        CoreSpringFactory.getBean(EPFrontendManager.class).savePortfolioStructure(map);
        DBFactory.getInstance(false).commit();

        try {
            // now search has to find file content
            createIndex();
            result = doSimpleSearch(portfolioStructureTitle);
            Assert.assertEquals(1, result.getTotalHits());
            result = doSimpleSearch(portfolioStructureDescription);
            Assert.assertEquals(1, result.getTotalHits());

            // delete business group again
            businessGroupService.deleteBusinessGroup(group);
            DBFactory.getInstance(false).commit();

            // content must no longer be available
            createIndex();
            result = doSimpleSearch(portfolioStructureTitle);
            Assert.assertEquals(0, result.getTotalHits());
            result = doSimpleSearch(portfolioStructureDescription);
            Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining groups
                businessGroupService.deleteBusinessGroups(businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, OWNER_IDENTITY, null));
                DBFactory.getInstance(false).commitAndCloseSession();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Test
    public void testRepoEntryWikiIndexing() throws Exception {
        final String wikiPageName = "indexedRepoEntryWikiName";
        final String wikiPageContent = "indexedRepoEntryWikiContent";

        // search before must not return any results
        createIndex();
        checkRepoEntryTitleAndDescription(false);
        SearchResults result = doSimpleSearch(wikiPageName);
        Assert.assertEquals(0, result.getTotalHits());
        result = doSimpleSearch(wikiPageContent);
        Assert.assertEquals(0, result.getTotalHits());

        // create content
        final FileResource wikiResource = WikiManager.getInstance().createWiki();
        Wiki wiki = WikiManager.getInstance().getOrLoadWiki(wikiResource);
        WikiPage wikiPage = new WikiPage(wikiPageName);
        wikiPage.setContent(wikiPageContent);
        wiki.addPage(wikiPage);
        WikiManager.getInstance().saveWikiPage(wikiResource, wikiPage, false, wiki);
        RepositoryEntry repoEntry = createRepoEntry(wikiResource);
        DBFactory.getInstance(false).commit();

        try {
            // now search has to find content
            createIndex();
            checkRepoEntryTitleAndDescription(true);
            result = doSimpleSearch(wikiPageName);
            Assert.assertEquals(1, result.getTotalHits());
            result = doSimpleSearch(wikiPageContent);
            Assert.assertEquals(1, result.getTotalHits());

            // delete content again
            WikiManager.getInstance().deleteWiki(wikiResource);
            deleteRepoEntry(repoEntry);
            DBFactory.getInstance(false).commit();

            // content must no longer be available
            createIndex();
            checkRepoEntryTitleAndDescription(false);
            result = doSimpleSearch(wikiPageName);
            Assert.assertEquals(0, result.getTotalHits());
            result = doSimpleSearch(wikiPageContent);
            Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining data
                WikiManager.getInstance().deleteWiki(wikiResource);
                deleteRepoEntry(repoEntry);
            } catch (Exception ex) {
                // ignore
            } finally {
                DBFactory.getInstance(false).commitAndCloseSession();
            }
        }
    }

    @Test
    public void testRepoEntryBlogIndexing() throws Exception {
        // final String wikiPageName = "indexedRepoEntryBlogName";
        // final String wikiPageContent = "indexedRepoEntryWikiContent";

        // search before must not return any results
        createIndex();
        SearchResults result = doSimpleSearch("Hallo");
        Assert.assertEquals(0, result.getTotalHits());
        // result = doSimpleSearch(wikiPageContent);
        // Assert.assertEquals(0, result.getTotalHits());

        // create content
        final FeedManager feedManager = FeedManager.getInstance();
        final OLATResourceable blogResource = feedManager.createBlogResource();
        final RepositoryEntry repoEntry = createRepoEntry(blogResource);
        DBFactory.getInstance(false).commit();

        try {
            // now search has to find content
            createIndex();
            // result = doSimpleSearch(wikiPageName);
            // Assert.assertEquals(1, result.getTotalHits());
            // result = doSimpleSearch(wikiPageContent);
            // Assert.assertEquals(1, result.getTotalHits());

            // delete content again
            feedManager.delete(blogResource);
            deleteRepoEntry(repoEntry);
            DBFactory.getInstance(false).commit();

            // content must no longer be available
            createIndex();
            // result = doSimpleSearch(wikiPageName);
            // Assert.assertEquals(0, result.getTotalHits());
            // result = doSimpleSearch(wikiPageContent);
            // Assert.assertEquals(0, result.getTotalHits());
        } finally {
            try {
                // cleanup remaining data
                feedManager.delete(blogResource);
                deleteRepoEntry(repoEntry);
                DBFactory.getInstance(false).commitAndCloseSession();
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    private RepositoryEntry createRepoEntry(final OLATResourceable resource) {
        final String repoEntryDisplayName = "indexedRepoEntryTitle";
        final String repoEntryDescription = "indexedRepoEntryDescription";
        final String repoEntryResourceName = "indexedRepoEntryResourceName";

        final RepositoryService repositoryService = CoreSpringFactory.getBean(RepositoryService.class);
        RepositoryEntry repoEntry = repositoryService.createRepositoryEntryInstance(OWNER_IDENTITY.getName(), repoEntryResourceName, repoEntryDescription);
        repoEntry.setDisplayname(repoEntryDisplayName);

        OLATResourceManager resourceManager = CoreSpringFactory.getBean(OLATResourceManager.class);

        // Set the resource on the repository entry and save the entry.
        final OLATResource ores = resourceManager.findOrPersistResourceable(resource);
        repoEntry.setOlatResource(ores);

        BaseSecurityEBL baseSecurityEBL = CoreSpringFactory.getBean(BaseSecurityEBL.class);
        final SecurityGroup securityGroup = baseSecurityEBL.createOwnerGroupWithIdentity(OWNER_IDENTITY);
        repoEntry.setOwnerGroup(securityGroup);

        repositoryService.saveRepositoryEntry(repoEntry);
        return repoEntry;
    }

    private void checkRepoEntryTitleAndDescription(boolean repoEntryExists) throws Exception {
        SearchResults result = doSimpleSearch("indexedRepoEntryTitle");
        Assert.assertEquals(repoEntryExists ? 1 : 0, result.getTotalHits());
        result = doSimpleSearch("indexedRepoEntryDescription");
        Assert.assertEquals(repoEntryExists ? 1 : 0, result.getTotalHits());
    }

    private void deleteRepoEntry(final RepositoryEntry repoEntry) {
        try {
            CoreSpringFactory.getBean(RepositoryService.class).deleteRepositoryEntry(repoEntry);
        } catch (Exception ex) {
            // possibly already deleted
            DBFactory.getInstance(false).commitAndCloseSession();
        }
        BaseSecurity securityDao = CoreSpringFactory.getBean(BaseSecurity.class);
        securityDao.deleteSecurityGroup(repoEntry.getOwnerGroup());
    }

    private void createIndex() {
        SearchService searchService = SearchServiceFactory.getService();
        searchService.startIndexing(false);
        String status = searchService.getStatus().getStatus();
        while (status != FullIndexerStatus.STATUS_FINISHED && status != FullIndexerStatus.STATUS_STOPPED) {
            try {
                Thread.sleep(1000);
                status = searchService.getStatus().getStatus();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private SearchResults doSimpleSearch(String searchQuery) throws Exception {
        return SEARCH_CLIENT.doSearch(searchQuery, null, OTHER_IDENTITY, new Roles(true, true, true, true, false, true, false), 1, 10000, false);
    }
}
