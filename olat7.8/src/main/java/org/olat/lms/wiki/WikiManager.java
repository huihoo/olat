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

package org.olat.lms.wiki;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemSuffixFilter;
import org.olat.data.commons.vfs.filters.VFSLeafFilter;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.group.BusinessGroup;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.WikiCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.presentation.wiki.versioning.DifferenceService;
import org.olat.presentation.wiki.versioning.diff.CookbookDifferenceService;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * This class handles several wiki's by storing them in a cache and also creates a new wikis. It handles also the file operation to persist the data in the wiki pages
 * which are stored on the file system.
 * <P>
 * Initial Date: May 5, 2006 <br>
 * 
 * @author guido
 */
@Service
public class WikiManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String VIEW_COUNT = "view.count";
    public static final String MODIFY_AUTHOR = "modify.author";
    public static final String M_TIME = "mTime";
    public static final String INITIAL_AUTHOR = "initial.author";
    public static final String FORUM_KEY = "forum.key";
    public static final String VERSION = "version";
    public static final String C_TIME = "cTime";
    public static final String PAGENAME = "pagename";
    private static WikiManager instance;
    public static final String WIKI_RESOURCE_FOLDER_NAME = "wiki";
    public static final String VERSION_FOLDER_NAME = "versions";
    public static final String WIKI_FILE_SUFFIX = "wp";
    public static final String WIKI_PROPERTIES_SUFFIX = "properties";
    public static final String UPDATE_COMMENT = "update.comment";

    // o_clusterNOK cache : 08.04.08/cg Not tested in cluster-mode
    CacheWrapper wikiCache;

    @Autowired
    OLATResourceManager resourceManager;
    @Autowired
    FileResourceManager fileResourceManager;
    @Autowired
    CoordinatorManager coordinator;
    @Autowired
    protected NotificationService notificationService;

    /**
     * spring only
     */
    private WikiManager() {
        instance = this;
    }

    /**
     * return singleton
     */
    public static WikiManager getInstance() {
        return instance;
    }

    /**
     * @return the new created resource
     */

    public FileResource createWiki() {
        final FileResource resource = new WikiResource();
        createFolders(resource);
        final OLATResourceManager rm = getResourceManager();
        final OLATResource ores = rm.createOLATResourceInstance(resource);
        rm.saveOLATResource(ores);
        return resource;
    }

    /**
     * API change stop
     */

    void createFolders(final OLATResourceable ores) {
        long start = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        final VFSContainer rootContainer = getWikiRootContainer(ores);
        final VFSContainer unzippedDir = (VFSContainer) rootContainer.resolve(getFileResourceManager().ZIPDIR);
        if (unzippedDir == null) { // check for _unzipped_ dir from imported wiki's
            if (rootContainer.createChildContainer(WIKI_RESOURCE_FOLDER_NAME) == null) {
                throwError(ores);
            }
            if (rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME) == null) {
                throwError(ores);
            }
            if (rootContainer.createChildContainer(VERSION_FOLDER_NAME) == null) {
                throwError(ores);
            }
        } else { // _unzipped_ dir found: move elements to wiki folder and delete
                 // unzipped dir and zip files
            final List files = unzippedDir.getItems();
            final VFSContainer wikiCtn = rootContainer.createChildContainer(WIKI_RESOURCE_FOLDER_NAME);
            final VFSContainer mediaCtn = rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME);
            if (rootContainer.createChildContainer(VERSION_FOLDER_NAME) == null) {
                throwError(ores);
            }
            if (wikiCtn == null) {
                throwError(ores);
            }
            // copy files to wiki and media folder
            for (final Iterator iter = files.iterator(); iter.hasNext();) {
                final VFSLeaf leaf = ((VFSLeaf) iter.next());
                if (leaf.getName().endsWith(WikiManager.WIKI_FILE_SUFFIX) || leaf.getName().endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
                    wikiCtn.copyFrom(leaf);
                } else {
                    if (leaf.getName().contains(WikiManager.WIKI_FILE_SUFFIX + "-") || leaf.getName().contains(WikiManager.WIKI_PROPERTIES_SUFFIX + "-")) {
                        leaf.delete(); // delete version history
                    } else {
                        mediaCtn.copyFrom(leaf);
                    }
                }
            }
            unzippedDir.delete();
            final List zipFiles = rootContainer.getItems(new VFSItemSuffixFilter(new String[] { "zip" }));
            // delete all zips
            for (final Iterator iter = zipFiles.iterator(); iter.hasNext();) {
                final VFSLeaf element = (VFSLeaf) iter.next();
                element.delete();
            }
            // reset forum key and author references keys back to default as users and forums may not exist
            final List propertyLeafs = wikiCtn.getItems(new VFSItemSuffixFilter(new String[] { WikiManager.WIKI_PROPERTIES_SUFFIX }));
            for (final Iterator iter = propertyLeafs.iterator(); iter.hasNext();) {
                final VFSLeaf element = (VFSLeaf) iter.next();
                final WikiPage page = Wiki.assignPropertiesToPage(element);
                page.setForumKey(0);
                page.setInitalAuthor(0);
                page.setModifyAuthor(0);
                page.setModificationTime(0);
                page.setViewCount(0);
                page.setVersion("0");
                page.setCreationTime(System.currentTimeMillis());
                saveWikiPageProperties(ores, page);
            }
        }
        if (log.isDebugEnabled()) {
            final long end = System.currentTimeMillis();
            log.debug("creating folders and move files and updating properties to default values took: (milliseconds)" + (end - start), null);
        }
    }

    private void throwError(final OLATResourceable ores) {
        throw new OLATRuntimeException(this.getClass(), "Unable to create wiki folder structure for resource: " + ores.getResourceableId(), null);
    }

    /**
     * @param ores
     * @return a wiki loaded from cache or the fileSystem
     */
    public Wiki getOrLoadWiki(final OLATResourceable ores) {
        final String wikiKey = OresHelper.createStringRepresenting(ores);
        // cluster_OK by guido
        if (wikiCache == null) {

            coordinator.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
                @Override
                public void execute() {
                    if (wikiCache == null) {
                        wikiCache = coordinator.getCoordinator().getCacher().getOrCreateCache(this.getClass(), "wiki");
                    }
                }
            });
        }
        final Wiki wiki = (Wiki) wikiCache.get(wikiKey);
        if (wiki != null) {
            log.debug("loading wiki from cache. Ores: " + ores.getResourceableId());
            return wiki;
        }
        // No wiki in cache => load it from file-system
        coordinator.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
            @Override
            public void execute() {

                long start = 0;
                // wiki not in cache load from filesystem
                if (log.isDebugEnabled()) {
                    log.debug("wiki not in cache. Loading wiki from filesystem. Ores: " + ores.getResourceableId());
                    start = System.currentTimeMillis();
                }

                Wiki wiki = null;
                VFSContainer folder = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
                // wiki folder structure does not yet exists, but resource does. Create
                // wiki in group context
                if (folder == null) {
                    // createWikiforExistingResource(ores);
                    createFolders(ores);
                    folder = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
                }
                // folders should be present, create the wiki
                wiki = new Wiki(getWikiRootContainer(ores));
                // filter for xyz.properties files
                final List wikiLeaves = folder.getItems(new VFSItemSuffixFilter(new String[] { WikiManager.WIKI_PROPERTIES_SUFFIX }));
                for (final Iterator iter = wikiLeaves.iterator(); iter.hasNext();) {
                    final VFSLeaf propertiesFile = (VFSLeaf) iter.next();
                    final WikiPage page = Wiki.assignPropertiesToPage(propertiesFile);
                    if (page == null) {
                        // broken pages get automatically cleaned from filesystem
                        final String contentFileToBeDeleted = (propertiesFile.getName().substring(0,
                                propertiesFile.getName().length() - WikiManager.WIKI_PROPERTIES_SUFFIX.length()) + WikiManager.WIKI_FILE_SUFFIX);
                        folder.resolve(contentFileToBeDeleted).delete();
                        propertiesFile.delete();
                        continue;
                    }
                    // index and menu page are loaded by default
                    if (page.getPageName().equals(WikiPage.WIKI_INDEX_PAGE) || page.getPageName().equals(WikiPage.WIKI_MENU_PAGE)) {
                        final VFSLeaf leaf = (VFSLeaf) folder.resolve(page.getPageId() + "." + WikiManager.WIKI_FILE_SUFFIX);
                        page.setContent(FileUtils.load(leaf.getInputStream(), "utf-8"));
                    }

                    // due to a bug we have to rename some pages that start with an non
                    // ASCII lowercase letter
                    final String idOutOfFileName = propertiesFile.getName().substring(0, propertiesFile.getName().indexOf("."));
                    if (!page.getPageId().equals(idOutOfFileName)) {
                        // rename corrupt prop file
                        propertiesFile.rename(page.getPageId() + "." + WikiManager.WIKI_PROPERTIES_SUFFIX);
                        // load content and delete corrupt content file
                        final VFSLeaf contentFile = (VFSLeaf) folder.resolve(idOutOfFileName + "." + WikiManager.WIKI_FILE_SUFFIX);
                        contentFile.rename(page.getPageId() + "." + WikiManager.WIKI_FILE_SUFFIX);
                    }

                    wiki.addPage(page);
                }
                // if index and menu page not present create the first page and save it
                if (wiki.getNumberOfPages() == 0) {
                    final WikiPage indexPage = new WikiPage(WikiPage.WIKI_INDEX_PAGE);
                    final WikiPage menuPage = new WikiPage(WikiPage.WIKI_MENU_PAGE);
                    indexPage.setCreationTime(System.currentTimeMillis());
                    wiki.addPage(indexPage);
                    menuPage.setCreationTime(System.currentTimeMillis());
                    menuPage.setContent("* [[Index]]\n* [[Index|Your link]]");
                    wiki.addPage(menuPage);
                    saveWikiPage(ores, indexPage, false, wiki);
                    saveWikiPage(ores, menuPage, false, wiki);
                }
                // add pages internally used for displaying dynamic data, they are not
                // persisted
                final WikiPage recentChangesPage = new WikiPage(WikiPage.WIKI_RECENT_CHANGES_PAGE);
                final WikiPage a2zPage = new WikiPage(WikiPage.WIKI_A2Z_PAGE);
                wiki.addPage(recentChangesPage);
                wiki.addPage(a2zPage);

                // wikiCache.put(OresHelper.createStringRepresenting(ores), wiki);
                if (log.isDebugEnabled()) {
                    final long stop = System.currentTimeMillis();
                    log.debug("loading of wiki from filessystem took (ms) " + (stop - start));
                }
                wikiCache.put(wikiKey, wiki);
            }
        });
        // at this point there will be something in the cache
        return (Wiki) wikiCache.get(wikiKey);

    }

    public DifferenceService getDiffService() {
        return new CookbookDifferenceService();
    }

    /**
     * persists a wiki page on the filesystem. It moves the recent page and the metadata to the versions folder with the version on the tail and saves new page with
     * metadata to the wiki folder. Does not need to be synchronized as editing is locked on page level by the
     * 
     * @param ores
     * @param page
     */
    public void saveWikiPage(final OLATResourceable ores, final WikiPage page, final boolean incrementVersion, final Wiki wiki) {
        // cluster_OK by guido
        final VFSContainer versionsContainer = getWikiContainer(ores, VERSION_FOLDER_NAME);
        final VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
        // rename existing content file to version x and copy it to the version
        // container
        VFSItem item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
        if (item != null && incrementVersion) {
            if (page.getVersion() > 0) {
                versionsContainer.copyFrom(item);
                final VFSItem copiedItem = versionsContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
                final String fileName = page.getPageId() + "." + WIKI_FILE_SUFFIX + "-" + page.getVersion();
                copiedItem.rename(fileName);
            }
            item.delete();
        }
        // rename existing meta file to version x and copy it to the version
        // container
        item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
        if (item != null && incrementVersion) {
            // TODO renaming and coping does not work. Bug?? felix fragen
            if (page.getVersion() > 0) {
                versionsContainer.copyFrom(item);
                final VFSItem copiedItem = versionsContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
                final String fileName = page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX + "-" + page.getVersion();
                copiedItem.rename(fileName);
            }
            item.delete();
        }
        // store recent content file
        VFSLeaf leaf = wikiContentContainer.createChildLeaf(page.getPageId() + "." + WIKI_FILE_SUFFIX);
        if (leaf == null) {
            throw new AssertException("Tried to save wiki page with id (" + page.getPageId() + ") and Olatresource: " + ores.getResourceableId()
                    + " but page already existed!");
        }
        FileUtils.save(leaf.getOutputStream(false), page.getContent(), "utf-8");

        // store recent properties file
        leaf = wikiContentContainer.createChildLeaf(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
        if (leaf == null) {
            throw new AssertException("could not create file for wiki page " + page.getPageId() + ", ores: " + ores.getResourceableTypeName() + ":"
                    + ores.getResourceableId() + ", wikicontainer:" + wikiContentContainer);
        }
        if (incrementVersion) {
            page.incrementVersion();
        }
        // update modification time
        if (!page.getContent().equals("")) {
            page.setModificationTime(System.currentTimeMillis());
        }
        final Properties p = getPageProperties(page);
        try {
            final OutputStream os = leaf.getOutputStream(false);
            p.store(os, "wiki page meta properties");
            os.close();
            // if (incrementVersion) page.incrementVersion();
        } catch (final IOException e) {
            throw new OLATRuntimeException(WikiManager.class, "failed to save wiki page properties for page with id: " + page.getPageId() + " and olatresource: "
                    + ores.getResourceableId(), e);
        }
        page.setViewCount(0); // reset view count of the page

        // update cache to inform all nodes about the change
        if (wikiCache != null) {
            wikiCache.update(OresHelper.createStringRepresenting(ores), wiki);
        }
        // do logging
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_UPDATE, getClass());
    }

    /**
     * delete a page completely by removing from the file system
     * 
     * @param ores
     * @param page
     */
    public void deleteWikiPage(final OLATResourceable ores, final WikiPage page) {
        final String name = page.getPageName();
        // do not delete default pages
        if (name.equals(WikiPage.WIKI_INDEX_PAGE) || name.equals(WikiPage.WIKI_MENU_PAGE)) {
            return;
        }
        final VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
        final VFSContainer versionsContainer = getWikiContainer(ores, VERSION_FOLDER_NAME);
        // delete content and property file
        VFSItem item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
        if (item != null) {
            item.delete();
        }
        item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
        if (item != null) {
            item.delete();
        }

        // delete all version files of the page
        final List leafs = versionsContainer.getItems(new VFSLeafFilter());
        if (leafs.size() > 0) {
            for (final Iterator iter = leafs.iterator(); iter.hasNext();) {
                final VFSLeaf leaf = (VFSLeaf) iter.next();
                final String filename = leaf.getName();
                if (filename.startsWith(page.getPageId())) {
                    leaf.delete();
                }
            }
        }
        log.info("Audit:Deleted wiki page with name: " + page.getPageName() + " from resourcable id: " + ores.getResourceableId());
        if (wikiCache != null) {
            wikiCache.update(OresHelper.createStringRepresenting(ores), getOrLoadWiki(ores));
        }
    }

    /**
     * delete a whole wiki from the cache and the filesystem
     * 
     * @param ores
     */
    public void deleteWiki(final OLATResourceable ores) {
        if (wikiCache != null) {
            wikiCache.remove(OresHelper.createStringRepresenting(ores));
        }
        getResourceManager().deleteOLATResourceable(ores);
    }

    /**
     * @param ores
     * @param page
     */
    public void updateWikiPageProperties(final OLATResourceable ores, final WikiPage page) {
        saveWikiPageProperties(ores, page);
        if (wikiCache != null) {
            wikiCache.update(OresHelper.createStringRepresenting(ores), getOrLoadWiki(ores));
        }
    }

    private void saveWikiPageProperties(final OLATResourceable ores, final WikiPage page) {
        final VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
        VFSLeaf leaf = (VFSLeaf) wikiContentContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
        if (leaf == null) {
            leaf = wikiContentContainer.createChildLeaf(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
        }
        final Properties p = getPageProperties(page);
        try {
            p.store(leaf.getOutputStream(false), "wiki page meta properties");
        } catch (final IOException e) {
            throw new OLATRuntimeException(WikiManager.class, "failed to save wiki page properties for page with id: " + page.getPageId() + " and olatresource: "
                    + ores.getResourceableId(), e);
        }
    }

    /**
     * @param page
     * @return the fields of the page object as properties
     */
    private Properties getPageProperties(final WikiPage page) {
        final Properties p = new Properties();
        p.setProperty(PAGENAME, page.getPageName());
        p.setProperty(VERSION, String.valueOf(page.getVersion()));
        p.setProperty(FORUM_KEY, String.valueOf(page.getForumKey()));
        p.setProperty(INITIAL_AUTHOR, String.valueOf(page.getInitalAuthor()));
        p.setProperty(MODIFY_AUTHOR, String.valueOf(page.getModifyAuthor()));
        p.setProperty(C_TIME, String.valueOf(page.getCreationTime()));
        p.setProperty(VIEW_COUNT, String.valueOf(page.getViewCount()));
        p.setProperty(M_TIME, String.valueOf(page.getModificationTime()));
        p.setProperty(UPDATE_COMMENT, page.getUpdateComment());
        return p;
    }

    /**
     * @param pageName
     * @return
     */
    public static String generatePageId(final String pageName) {
        try {
            String encoded = new String(Base64.encodeBase64(pageName.getBytes("utf-8")), "us-ascii");
            encoded = encoded.replace('/', '_'); // base64 can contain "/" so we have to replace them
            return encoded;
        } catch (final UnsupportedEncodingException e) {
            throw new OLATRuntimeException(WikiManager.class, "Encoding UTF-8 not supported by your platform!", e);
        }
    }

    /**
     * @param ores
     * @param folderName
     * @return the Vfs container or null if not found
     */
    public VFSContainer getWikiContainer(final OLATResourceable ores, final String folderName) {
        final VFSContainer wikiRootContainer = getWikiRootContainer(ores);
        return (VFSContainer) wikiRootContainer.resolve(folderName);
    }

    /**
     * Returns the root-container for certain OLAT-resourceable.
     * 
     * @param ores
     * @return
     */
    public VFSContainer getWikiRootContainer(final OLATResourceable ores) {
        // Check if Resource is a BusinessGroup, because BusinessGroup-wiki's are stored at a different place
        if (log.isDebugEnabled()) {
            log.debug("calculating wiki root container with ores id: " + ores.getResourceableId() + " and resourcable type name: " + ores.getResourceableTypeName(), null);
        }
        if (isGroupContextWiki(ores)) {
            // Group Wiki
            return new OlatRootFolderImpl(getGroupWikiRelPath(ores), null);
        } else {
            // Repository Wiki
            return getFileResourceManager().getFileResourceRootImpl(ores);
        }
    }

    /**
     * Get Wiki-File-Path for certain BusinessGroup.
     * 
     * @param businessGroup
     * @return
     */
    private String getGroupWikiRelPath(final OLATResourceable ores) {
        return "/cts/wikis/" + ores.getResourceableTypeName() + "/" + ores.getResourceableId();
    }

    /**
     * Return Media folder for uploading files.
     * 
     * @param ores
     * @return
     */
    public OlatRootFolderImpl getMediaFolder(final OLATResourceable ores) {
        // Check if Resource is a BusinessGroup, because BusinessGroup-wiki's are stored at a different place
        if (isGroupContextWiki(ores)) {
            // Group Wiki
            return new OlatRootFolderImpl(getGroupWikiRelPath(ores) + "/" + WikiContainer.MEDIA_FOLDER_NAME, null);
        } else {
            // Repository Wiki
            return new OlatRootFolderImpl("/repository/" + ores.getResourceableId() + "/" + WikiContainer.MEDIA_FOLDER_NAME, null);
        }
    }

    /**
     * @return false if repository wiki or true if group only wiki
     */
    /**
     * @return false if repository wiki or true if group only wiki
     */
    protected boolean isGroupContextWiki(final OLATResourceable ores) {
        return ores.getResourceableTypeName().equals(OresHelper.calculateTypeName(BusinessGroup.class));
    }

    /**
     * wiki subscription context for wikis in the course.
     * 
     * @param cenv
     * @param wikiCourseNode
     * @return
     */
    public static SubscriptionContext createTechnicalSubscriptionContextForCourse(final CourseEnvironment cenv, final WikiCourseNode wikiCourseNode) {
        return new SubscriptionContext(CourseModule.getCourseTypeName(), cenv.getCourseResourceableId(), wikiCourseNode.getIdent());
    }

    private OLATResourceManager getResourceManager() {
        return resourceManager;
    }

    private FileResourceManager getFileResourceManager() {
        return fileResourceManager;
    }

    public void saveWikiPage(OLATResourceable ores, WikiPage page, boolean b, Wiki wiki, PublishEventTO publishEventTO) {
        saveWikiPage(ores, page, b, wiki);
        if (!publishEventTO.getEvenType().equals(EventType.NO_PUBLISH) && page.getVersion() > 1) {
            publishEventTO.setEventType(EventType.CHANGED);
        }
        publishEvent(publishEventTO);
    }

    private void publishEvent(PublishEventTO publishEventTO) {
        try {
            notificationService.publishEvent(publishEventTO);
        } catch (RuntimeException e) {
            // if publishEvent retry fails catch a transient exception
            log.error("publishEvent failed: ", e);
        }
    }

}
