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

package org.olat.presentation.wiki;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.forum.Forum;
import org.olat.data.portfolio.artefact.WikiArtefact;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.forum.ForumCallback;
import org.olat.lms.forum.ForumService;
import org.olat.lms.wiki.Wiki;
import org.olat.lms.wiki.WikiForumCallback;
import org.olat.lms.wiki.WikiInputValidation;
import org.olat.lms.wiki.WikiManager;
import org.olat.lms.wiki.WikiPage;
import org.olat.lms.wiki.WikiSecurityCallback;
import org.olat.lms.wiki.WikiToCPExport;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.forum.ForumUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.clone.CloneableController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.state.ControllerState;
import org.olat.presentation.notification.ContextualSubscriptionController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.search.SearchServiceUIFactory;
import org.olat.presentation.search.SearchServiceUIFactory.DisplayOption;
import org.olat.presentation.wiki.versioning.HistoryTableDateModel;
import org.olat.presentation.wiki.wikitohtml.ErrorEvent;
import org.olat.presentation.wiki.wikitohtml.RequestImageEvent;
import org.olat.presentation.wiki.wikitohtml.RequestMediaEvent;
import org.olat.presentation.wiki.wikitohtml.RequestNewPageEvent;
import org.olat.presentation.wiki.wikitohtml.RequestPageEvent;
import org.olat.presentation.wiki.wikitohtml.WikiMarkupComponent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This controller creates the whole GUI for a wiki with a tabbed pane containing an article view, per page forum view, edit pane and versioning pane. The rendering of
 * the
 * <P>
 * Initial Date: May 4, 2006 <br>
 * 
 * @author guido
 */
public class WikiMainController extends BasicController implements CloneableController {

    private static final Logger log = LoggerHelper.getLogger();

    private enum TabState {
        NEW_ARTICLE, IDLE, CHANGED_TAB
    };

    private TabState state = TabState.IDLE;
    private final TabbedPane tabs;
    private WikiPage selectedPage;
    private String pageId;
    private final VFSContainer wikiContainer;
    private final OLATResourceable ores;
    private final VelocityContainer articleContent, navigationContent, discussionContent, editContent, content, versioningContent;

    private final Link openDiscussionButton, editPageButton, showPageButton, showVersionButton;
    private String lastTab;

    private VelocityContainer mediaMgntContent;

    private final VelocityContainer imageDisplay;
    private final WikiEditArticleForm wikiEditForm;
    private final WikiMarkupComponent wikiMenuComp;

    private WikiMarkupComponent wikiArticleComp;

    private final WikiMarkupComponent wikiVersionDisplayComp;
    private ContextualSubscriptionController cSubscriptionCtrl;
    private TableController versioningTableCtr;
    private HistoryTableDateModel versioningTableModel;
    private final FileUploadController fileUplCtr;
    private final BreadCrumbController breadCrumpCtr;
    private DialogBoxController removePageDialogCtr, archiveWikiDialogCtr;
    private List diffs = new ArrayList(2);
    private final Identity ident;
    private final SubscriptionContext subsContext;
    private LockResult lockEntry;
    private final Link archiveLink, closePreviewButton, deletePageButton, manageMediaButton, toMainPageLink, a2zLink, changesLink;

    private Link editMenuButton, revertVersionButton;
    private TableController mediaTableCtr;
    private MediaFilesTableModel mediaFilesTableModel;
    private final TableGuiConfiguration tableConfig;
    private final WikiSecurityCallback securityCallback;
    private final WikiArticleSearchForm searchOrCreateArticleForm;
    private Controller searchCtrl;

    public static final String ACTION_COMPARE = "compare";
    public static final String ACTION_SHOW = "view.version";
    private static final String ACTION_EDIT_MENU = "editMenu";
    private static final String ACTION_CLOSE_PREVIEW = "preview.close";
    private static final String ACTION_DELETE_PAGE = "delete.page";
    private static final String ACTION_MANAGE_MEDIA = "manage.media";
    private static final String ACTION_DELETE_MEDIAS = "delete.medias";
    private static final String ACTION_DELETE_MEDIA = "delete.media";
    protected static final String ACTION_SHOW_MEDIA = "show.media";
    public static final String METADATA_SUFFIX = ".metadata";
    private static final String MEDIA_FILE_FILENAME = "filename";
    private static final String MEDIA_FILE_CREATIONDATE = "creation.date";
    private static final String MEDIA_FILE_CREATED_BY = "created.by";
    private static final String MEDIA_FILE_DELETIONDATE = "deleted.at";
    private static final String MEDIA_FILE_DELETED_BY = "deleted.by";

    // indicates if user is already on image-detail-view-page (OLAT-6233)
    private boolean isImageDetailView = false;

    private CloseableModalController cmc;

    private WikiNotificationTypeHandler wikiNotificationTypeHandler;

    private String newTopic;

    WikiMainController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final WikiSecurityCallback securityCallback,
            final String initialPageName) {
        super(ureq, wControl);
        this.wikiContainer = WikiManager.getInstance().getWikiRootContainer(ores);
        this.ores = ores;
        if (securityCallback == null) {
            throw new AssertException("WikiSecurityCallback is null! You must provide an security callback!", null);
        }
        this.securityCallback = securityCallback;
        this.subsContext = securityCallback.getSubscriptionContext();
        this.ident = ureq.getIdentity();

        WikiPage page = null;
        final Wiki wiki = getWiki();
        if (!ores.getResourceableTypeName().equals("BusinessGroup")) {
            addLoggingResourceable(LoggingResourceable.wrap(ores, OlatResourceableType.genRepoEntry));
        }
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
        // init the first page either startpage or an other page identified by initial page name
        if (initialPageName != null && wiki.pageExists(WikiManager.generatePageId(initialPageName))) {
            page = wiki.getPage(initialPageName, true);
        } else {
            page = wiki.getPage(WikiPage.WIKI_INDEX_PAGE);
            if (initialPageName != null)
                showError("wiki.error.page.not.found");
        }
        this.pageId = page.getPageId();

        final WikiPage menuPage = getWiki().getPage(WikiPage.WIKI_MENU_PAGE);
        tabs = new TabbedPane("userTabP", ureq.getLocale());
        tabs.dissableTabTitle();
        tabs.addListener(this);
        // init the tabbed pane container
        content = createVelocityContainer("index");

        // add a histroy ctr that displays visited pages
        breadCrumpCtr = new BreadCrumbController(ureq, getWindowControl());
        listenTo(breadCrumpCtr);

        breadCrumpCtr.addLink(WikiPage.WIKI_INDEX_PAGE, WikiPage.WIKI_INDEX_PAGE);
        wikiNotificationTypeHandler = CoreSpringFactory.getBean(WikiNotificationTypeHandler.class);
        if (subsContext != null) {
            final PublisherData data = new PublisherData(OresHelper.calculateTypeName(WikiPage.class), ores.getResourceableId().toString());
            cSubscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);

            listenTo(cSubscriptionCtrl);
            content.put("subscription", cSubscriptionCtrl.getInitialComponent());
        }
        // TODO:gs:a allow to jump into certain wiki page from email link

        /***************************************************************************
         * navigation container
         **************************************************************************/
        navigationContent = createVelocityContainer("navigation");
        toMainPageLink = LinkFactory.createLink("navigation.mainpage", navigationContent, this);
        a2zLink = LinkFactory.createLink("navigation.a-z", navigationContent, this);
        changesLink = LinkFactory.createLink("navigation.changes", navigationContent, this);
        archiveLink = LinkFactory.createLink("archive.wiki", navigationContent, this);
        archiveLink.setTitle("archive.wiki.title");
        if (this.securityCallback.mayEditWikiMenu()) {
            editMenuButton = LinkFactory.createButtonSmall("edit.menu", navigationContent, this);
        }

        navigationContent.put("breadcrumb", breadCrumpCtr.getInitialComponent());
        content.put("navigation", navigationContent);
        searchOrCreateArticleForm = new WikiArticleSearchForm(ureq, getWindowControl());
        searchOrCreateArticleForm.addControllerListener(this);
        navigationContent.put("searchArticleForm", searchOrCreateArticleForm.getInitialComponent());

        // search
        if (!ureq.getUserSession().getRoles().isGuestOnly()) {
            final SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory) CoreSpringFactory.getBean(SearchServiceUIFactory.class);
            searchCtrl = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD_TEXT, null);
            listenTo(searchCtrl);
            navigationContent.put("search_article", searchCtrl.getInitialComponent());
        }

        // attach menu
        // FIXME:gs:a pass only ores to component to make shure they have the same wiki instance
        wikiMenuComp = new WikiMarkupComponent("wikiArticle", ores, 0);
        wikiMenuComp.addListener(this);
        // load the menu page and set the image mapper
        wikiMenuComp.setWikiContent(menuPage.getContent());
        wikiMenuComp.setImageMapperUri(ureq, wikiContainer);
        navigationContent.put("wikiMenu", wikiMenuComp);

        // attach index article
        wikiArticleComp = new WikiMarkupComponent("wikiArticle", ores, 300);
        wikiArticleComp.addListener(this);
        wikiArticleComp.setImageMapperUri(ureq, wikiContainer);
        navigationContent.put("wikiArticle", wikiArticleComp);

        /***************************************************************************
         * wiki component
         **************************************************************************/
        articleContent = createVelocityContainer("article");
        wikiArticleComp = new WikiMarkupComponent("wikiArticle", ores, 300);
        wikiArticleComp.addListener(this);
        wikiArticleComp.setImageMapperUri(ureq, wikiContainer);
        articleContent.put("wikiArticle", wikiArticleComp);
        tabs.addTab(translate("tab.article"), articleContent);
        showPageButton = LinkFactory.createButtonSmall("showPageButton", content, this);

        /***************************************************************************
         * discussion container
         **************************************************************************/
        discussionContent = createVelocityContainer("discuss");
        tabs.addTab(translate("tab.discuss"), discussionContent);
        openDiscussionButton = LinkFactory.createButtonSmall("openDiscussionButton", content, this);
        /***************************************************************************
         * edit container
         **************************************************************************/
        editContent = createVelocityContainer("edit");
        imageDisplay = createVelocityContainer("imagedisplay");
        closePreviewButton = LinkFactory.createButtonSmall(ACTION_CLOSE_PREVIEW, editContent, this);
        deletePageButton = LinkFactory.createButtonSmall(ACTION_DELETE_PAGE, editContent, this);
        manageMediaButton = LinkFactory.createButtonSmall(ACTION_MANAGE_MEDIA, editContent, this);

        editContent.contextPut("isGuest", Boolean.valueOf(ureq.getUserSession().getRoles().isGuestOnly()));
        wikiEditForm = new WikiEditArticleForm(ureq, wControl, page);
        listenTo(wikiEditForm);
        editContent.contextPut("editformid", "ofo_" + wikiEditForm.hashCode());

        editContent.put("editForm", wikiEditForm.getInitialComponent());

        final JSAndCSSComponent js = new JSAndCSSComponent("js", this.getClass(), new String[] { "wiki-script.js" }, null, false);
        content.put("js", js);
        // FIXME:gs:a FileUploadCtr should accept vfsContainers instead of
        // OLATrootfolderimpl. Refactor it!!!!!!!
        fileUplCtr = new FileUploadController(getWindowControl(), WikiManager.getInstance().getMediaFolder(ores), ureq, (int) FolderConfig.getLimitULKB(),
                Quota.UNLIMITED, null, false);
        listenTo(fileUplCtr);
        editContent.put("fileUplCtr", fileUplCtr.getInitialComponent());
        editContent.contextPut("fileList", wiki.getMediaFileList());
        editContent.contextPut("linkList", wiki.getListOfAllPageNames());
        tabs.addTab(translate("tab.edit"), editContent);
        editPageButton = LinkFactory.createButtonSmall("editPageButton", content, this);
        /***************************************************************************
         * version container
         **************************************************************************/
        versioningContent = createVelocityContainer("versions");
        wikiVersionDisplayComp = new WikiMarkupComponent("versionDisplay", ores, 300);
        wikiVersionDisplayComp.addListener(this);
        wikiVersionDisplayComp.setImageMapperUri(ureq, wikiContainer);
        tabs.addTab(translate("tab.versions"), versioningContent);
        if (this.securityCallback.mayEditAndCreateArticle()) {
            revertVersionButton = LinkFactory.createButton("revert.old.version", versioningContent, this);
        }

        tableConfig = new TableGuiConfiguration();
        tableConfig.setPageingEnabled(true);
        tableConfig.setResultsPerPage(10);
        tableConfig.setSelectedRowUnselectable(true);

        showVersionButton = LinkFactory.createButtonSmall("showVersionButton", content, this);

        content.put("wikiTabs", tabs);
        // if not content yet switch to the edit tab
        if (page.getContent().equals("")) {
            tabs.setSelectedPane(2);
            lastTab = "";
            tryToSetEditLock(page, ureq, ores);
        }
        updatePageContext(ureq, page);
        setTabsEnabled(true); // apply security settings to tabs by may disabling edit tab
        putInitialPanel(content);

        // set pageId to the latest used
        this.pageId = page.getPageId();
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        setTabsEnabled(true);
        simulateTabsWithButtons(ureq, event);

        setTabsEnabled(true);
        // to make sure we use the latest page, reload from cache
        final Wiki wiki = getWiki();
        WikiPage page = null;

        final String command = event.getCommand();
        // FIXME:gs images and media should also be wiki pages -> see jamwiki
        if (!(event instanceof RequestNewPageEvent) && !(event instanceof RequestMediaEvent) && !(event instanceof RequestImageEvent)) {
            page = wiki.getPage(pageId, true);
            // set recent page id to the page currently used
            if (page != null) {
                this.pageId = page.getPageId();
            }
        }

        if (source == content) {

            // noting yet
        } else if (source == tabs) {
            /*************************************************************************
             * tabbed pane events
             ************************************************************************/
            // first release a potential lock on this page. only when the edit tab is acitve
            // a lock will be created. in all other cases it is save to release an existing lock
            doReleaseEditLock();
            final TabbedPaneChangedEvent tabEvent = (TabbedPaneChangedEvent) event;
            final Component comp = tabEvent.getNewComponent();
            final String compName = comp.getComponentName();
            if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED)) {
                updatePageContext(ureq, page);
            }
            if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_article")) {
                /***********************************************************************
                 * tabbed pane change to article
                 **********************************************************************/
                // if(page.getContent().equals("")) wikiArticleComp.setVisible(false);
                // FIXME:guido: ... && comp == articleContent)) etc.
            } else if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_edit")) {
                /***********************************************************************
                 * tabbed pane change to edit tab
                 **********************************************************************/
                wikiEditForm.resetUpdateComment();
                editContent.contextPut("mayDeleteArticle",
                        Boolean.valueOf(ident.getKey().equals(Long.valueOf(page.getInitalAuthor())) || securityCallback.mayEditWikiMenu()));
                editContent.contextPut("linkList", wiki.getListOfAllPageNames());
                editContent.contextPut("fileList", wiki.getMediaFileList());
                // try to edit acquire lock for this page
                tryToSetEditLock(page, ureq, ores);
            } else if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_versions")) {
                /***********************************************************************
                 * tabbed pane change to versioning tab
                 **********************************************************************/
                versioningTableModel = new HistoryTableDateModel(wiki.getHistory(page), getTranslator());
                removeAsListenerAndDispose(versioningTableCtr);
                versioningTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
                listenTo(versioningTableCtr);
                versioningTableModel.addColumnDescriptors(versioningTableCtr);
                versioningTableCtr.setTableDataModel(versioningTableModel);
                versioningTableCtr.modelChanged();
                versioningTableCtr.setSortColumn(1, false);
                versioningContent.put("versions", versioningTableCtr.getInitialComponent());
                versioningContent.contextPut("diffs", diffs);
            } else if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_discuss")) {
                /***********************************************************************
                 * tabbed pane change to discussion tab
                 **********************************************************************/
                Forum forum = null;
                if (page.getForumKey() == 0) {
                    forum = getForumService().addAForum();
                    page.setForumKey(forum.getKey().longValue());
                    WikiManager.getInstance().updateWikiPageProperties(ores, page);
                }
                forum = getForumService().loadForum(Long.valueOf(page.getForumKey()));
                // TODO enhance forum callback with subscription stuff
                final boolean isModerator = securityCallback.mayModerateForum();
                final ForumCallback forumCallback = new WikiForumCallback(ureq.getUserSession().getRoles().isGuestOnly(), isModerator);

                // calculate the new businesscontext for the coursenode being called.
                // FIXME:pb:mannheim discussion should not be "this.ores" -> may be the "forum" should go in here.
                //
                final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(forum);
                final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());

                discussionContent.put("articleforum", ForumUIFactory.getStandardForumController(ureq, bwControl, forum, forumCallback).getInitialComponent());
            }
        } else if (source == wikiArticleComp || source == wikiMenuComp) {
            /*************************************************************************
             * wiki component events
             ************************************************************************/
            if (event instanceof RequestPageEvent) {
                final RequestPageEvent pageEvent = (RequestPageEvent) event;
                page = wiki.getPage(pageEvent.getCommand(), true);
                page.incrementViewCount();
                updatePageContext(ureq, page);
                breadCrumpCtr.addLink(page.getPageName(), page.getPageName());
                tabs.setSelectedPane(0);
            } else if (event instanceof RequestNewPageEvent) {
                if (WikiInputValidation.validatePageName(event.getCommand())) {
                    page = handleRequestNewPageEvent(ureq, (RequestNewPageEvent) event);
                } else {
                    showWarning("wiki.error.page.name.validation.error");
                }
            } else if (event instanceof ErrorEvent) {
                showWarning(event.getCommand());
            } else if (event instanceof RequestMediaEvent) {
                deliverMediaFile(ureq, event.getCommand());
            } else if (event instanceof RequestImageEvent) {
                // OLAT-6233 if image-view page is shown 2nd time (click on image ), return to content-wiki-page
                // instead of linking to the image-view-page itself
                if (isImageDetailView) {
                    page = wiki.getPage(pageId, true);
                    updatePageContext(ureq, page);
                    isImageDetailView = false;
                } else {
                    final WikiPage imagePage = new WikiPage(event.getCommand());
                    imagePage.setContent("[[Image:" + event.getCommand() + "]]");
                    articleContent.contextPut("page", imagePage);
                    wikiArticleComp.setWikiContent(imagePage.getContent());
                    setTabsEnabled(false);
                    isImageDetailView = true;
                }
            }
        } else if (source == navigationContent) {
            /*************************************************************************
             * article container events
             ************************************************************************/
            if (command.equals(ACTION_EDIT_MENU)) {
                page = wiki.getPage(WikiPage.WIKI_MENU_PAGE);
                editContent.contextPut("linkList", wiki.getListOfAllPageNames());
                tryToSetEditLock(page, ureq, ores);
                updatePageContext(ureq, page);
                tabs.setSelectedPane(2);
            }
        } else if (source == toMainPageLink) { // home link
            page = wiki.getPage(WikiPage.WIKI_INDEX_PAGE, true);
            page.incrementViewCount();
            breadCrumpCtr.addLink(page.getPageName(), page.getPageName());
            updatePageContext(ureq, page);
            tabs.setSelectedPane(0);
        } else if (source == a2zLink) {
            final WikiPage a2zPage = wiki.getPage(WikiPage.WIKI_A2Z_PAGE);
            articleContent.contextPut("page", a2zPage);
            a2zPage.setContent(translate("nav.a-z.desc") + wiki.getAllPageNamesSorted());
            wikiArticleComp.setWikiContent(a2zPage.getContent());
            clearPortfolioLink();
            setTabsEnabled(false);
            setState(WikiPage.WIKI_A2Z_PAGE);
            tabs.setSelectedPane(0);
        } else if (source == changesLink) {
            final WikiPage recentChanges = wiki.getPage(WikiPage.WIKI_RECENT_CHANGES_PAGE);
            recentChanges.setContent(translate("nav.changes.desc") + wiki.getRecentChanges(ureq.getLocale()));
            clearPortfolioLink();
            articleContent.contextPut("page", recentChanges);
            wikiArticleComp.setWikiContent(recentChanges.getContent());
            setTabsEnabled(false);
            tabs.setSelectedPane(0);
        } else if (source == editMenuButton) {
            page = wiki.getPage(WikiPage.WIKI_MENU_PAGE);
            editContent.contextPut("linkList", wiki.getListOfAllPageNames());
            tryToSetEditLock(page, ureq, ores);
            updatePageContext(ureq, page);
            // wikiEditForm.setPage(page);
            tabs.setSelectedPane(2);
        } else if (source == archiveLink) {
            // archive a snapshot of the wiki in the users personal folder
            archiveWikiDialogCtr = activateOkCancelDialog(ureq, null, translate("archive.question"), archiveWikiDialogCtr);
            return;
        } else if (source == versioningContent) {
            /*************************************************************************
             * versioning container events
             ************************************************************************/

        } else if (source == editContent) {
            /*************************************************************************
             * edit container events
             ************************************************************************/
        } else if (source == closePreviewButton) {
            editContent.remove(wikiVersionDisplayComp);
        } else if (source == deletePageButton) {
            removePageDialogCtr = activateOkCancelDialog(ureq, null, translate("question", page.getPageName()), removePageDialogCtr);
            return;
        } else if (source == manageMediaButton) {
            if (wiki.getMediaFileListWithMetadata().size() > 0) {
                mediaMgntContent = createVelocityContainer("media");
                refreshTableDataModel(ureq);
                mediaMgntContent.put("mediaMgmtTable", mediaTableCtr.getInitialComponent());

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), mediaMgntContent, true, translate("manage.media"));
                listenTo(cmc);

                cmc.activate();
            }
        } else if (source == revertVersionButton) {
            wikiEditForm.setPage(selectedPage);
            tabs.setSelectedPane(2);
            tryToSetEditLock(page, ureq, ores);
        }

        // set recent page id to the page currently used
        if (page != null) {
            this.pageId = page.getPageId();
        }
    }

    private void simulateTabsWithButtons(final UserRequest ureq, final Event event) {
        if (state == TabState.IDLE || state == TabState.CHANGED_TAB) {
            if (event.getCommand().equals("openDiscussionButton")) {
                tabs.dispatchRequest(ureq, tabs.getPaneIdForComponent(discussionContent));
                return;
            } else if (event.getCommand().equals("editPageButton")) {
                tabs.dispatchRequest(ureq, tabs.getPaneIdForComponent(editContent));
                return;
            } else if (event.getCommand().equals("showPageButton")) {
                tabs.dispatchRequest(ureq, tabs.getPaneIdForComponent(articleContent));
                return;
            } else if (event.getCommand().equals("showVersionButton")) {
                tabs.dispatchRequest(ureq, tabs.getPaneIdForComponent(versioningContent));
                return;
            }
            state = TabState.CHANGED_TAB;
        } else if (state == TabState.NEW_ARTICLE) {
            state = TabState.IDLE;
            wikiArticleComp.dispatchRequestForNewTopic(ureq, newTopic);
        }
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    private void deliverMediaFile(final UserRequest ureq, final String command) {
        final VFSLeaf leaf = (VFSLeaf) WikiManager.getInstance().getMediaFolder(ores).resolve(command);
        if (leaf == null) {
            showError("wiki.error.file.not.found");
        } else {
            ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(leaf));
        }
    }

    private void refreshTableDataModel(final UserRequest ureq) {

        removeAsListenerAndDispose(mediaTableCtr);
        mediaTableCtr = new TableController(new TableGuiConfiguration(), ureq, getWindowControl(), getTranslator());
        listenTo(mediaTableCtr);

        mediaTableCtr.setMultiSelect(true);
        mediaTableCtr.addMultiSelectAction(ACTION_DELETE_MEDIAS, ACTION_DELETE_MEDIAS);

        final List filelist = getWiki().getMediaFileListWithMetadata();
        final Map files = new HashMap();
        for (final Iterator iter = filelist.iterator(); iter.hasNext();) {
            final VFSLeaf elem = (VFSLeaf) iter.next();
            if (elem.getName().endsWith(METADATA_SUFFIX)) { // *.metadata files go here
                final Properties p = new Properties();
                try {
                    p.load(elem.getInputStream());
                    final MediaFileElement mediaFileElement = new MediaFileElement(elem.getName(), p.getProperty(MEDIA_FILE_CREATED_BY),
                            p.getProperty(MEDIA_FILE_CREATIONDATE));
                    mediaFileElement.setDeletedBy(p.getProperty(MEDIA_FILE_DELETED_BY));
                    mediaFileElement.setDeletionDate(p.getProperty(MEDIA_FILE_DELETIONDATE));
                    files.put(p.getProperty(MEDIA_FILE_FILENAME), mediaFileElement);
                } catch (final IOException e) {
                    throw new OLATRuntimeException("Could'n read properties from media file: " + elem.getName(), e);
                }
            }
        }
        for (final Iterator iter = filelist.iterator(); iter.hasNext();) {
            final VFSLeaf elem = (VFSLeaf) iter.next();
            if (!elem.getName().endsWith(METADATA_SUFFIX)) {
                if (!files.containsKey(elem.getName())) {
                    // legacy file without metadata
                    files.put(elem.getName(), new MediaFileElement(elem.getName(), 0, elem.getLastModified()));
                } else {
                    // file with metadata, update name
                    final MediaFileElement element = (MediaFileElement) files.get(elem.getName());
                    element.setFileName(elem.getName());
                }
            }
        }

        mediaFilesTableModel = new MediaFilesTableModel(new ArrayList(files.values()), getTranslator());
        mediaFilesTableModel.addColumnDescriptors(mediaTableCtr);
        mediaTableCtr.setTableDataModel(mediaFilesTableModel);
        mediaTableCtr.setSortColumn(3, false);
        mediaTableCtr.modelChanged();
    }

    private WikiPage handleRequestNewPageEvent(final UserRequest ureq, final RequestNewPageEvent requestPage) {
        if (!securityCallback.mayEditAndCreateArticle()) {
            if (ureq.getUserSession().getRoles().isGuestOnly()) {
                showInfo("guest.no.edit");
            }
            showInfo("no.edit");
            return null;
        }
        // first check if no page exist
        WikiPage page = getWiki().findPage(requestPage.getCommand());
        if (page.getPageName().equals(Wiki.NEW_PAGE)) {
            // create new page
            log.debug("Page does not exist, create a new one...");
            page = new WikiPage(requestPage.getCommand());
            page.setCreationTime(System.currentTimeMillis());
            page.setInitalAuthor(ureq.getIdentity().getKey().longValue());
            getWiki().addPage(page);
            WikiManager.getInstance().saveWikiPage(ores, page, false, getWiki());
            log.debug("Safe new page=" + page);
            log.debug("Safe new pageId=" + page.getPageId());
        }
        updatePageContext(ureq, page);
        doReleaseEditLock();
        tryToSetEditLock(page, ureq, ores);
        tabs.setSelectedPane(2);
        return page;
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, Event event) {
        final Wiki wiki = getWiki();
        // reload page from cache
        WikiPage page = wiki.getPage(pageId, true);
        // set recent page id to the page currently used
        this.pageId = page.getPageId();

        if (source == versioningTableCtr) {
            /*************************************************************************
             * history table events
             ************************************************************************/
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                if (te.getActionId().equals(ACTION_COMPARE)) {
                    selectedPage = (WikiPage) versioningTableModel.getObject(te.getRowId());
                    diffs = wiki.getDiff(page, selectedPage.getVersion() - 1, selectedPage.getVersion());
                    versioningContent.contextPut("diffs", diffs);
                    versioningContent.remove(wikiVersionDisplayComp);
                    versioningContent.contextPut("page", selectedPage);
                } else if (te.getActionId().equals(ACTION_SHOW)) {
                    versioningContent.contextRemove("diffs");
                    selectedPage = (WikiPage) versioningTableModel.getObject(te.getRowId());
                    selectedPage = wiki.loadVersion(selectedPage, selectedPage.getVersion());
                    wikiVersionDisplayComp.setWikiContent(selectedPage.getContent());
                    wikiVersionDisplayComp.setImageMapperUri(ureq, wikiContainer);
                    versioningContent.put("versionDisplay", wikiVersionDisplayComp);
                    versioningContent.contextPut("page", selectedPage);
                }
            }
        } else if (source == fileUplCtr) {
            /*************************************************************************
             * file upload controller events
             ************************************************************************/
            if (event == Event.DONE_EVENT) {
                fileUplCtr.reset();
            } else if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
                final FolderEvent fEvent = (FolderEvent) event;
                createMediaMetadataFile(fEvent.getFilename(), ureq.getIdentity().getKey());
                editContent.contextPut("fileList", wiki.getMediaFileList());
            }
        } else if (source == breadCrumpCtr) {
            /*************************************************************************
             * BreadCrump controller events
             ************************************************************************/
            page = wiki.getPage(event.getCommand(), true);
            pageId = page.getPageId();
            updatePageContext(ureq, page);
            setTabsEnabled(true);
            breadCrumpCtr.addLink(page.getPageName(), page.getPageName());
            tabs.setSelectedPane(0);
        } else if (source == removePageDialogCtr) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                wiki.removePage(page);
                breadCrumpCtr.removeLink(page.getPageName());
                WikiManager.getInstance().deleteWikiPage(ores, page);
                page = wiki.getPage(WikiPage.WIKI_INDEX_PAGE);
                updatePageContext(ureq, page);
                tabs.setSelectedPane(0);
            }
        } else if (source == mediaTableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                if (te.getActionId().equals(ACTION_DELETE_MEDIA)) {
                    final List list = new ArrayList(1);
                    list.add(mediaFilesTableModel.getObject(te.getRowId()));
                    deleteMediaFile(list, ureq);
                } else if (te.getActionId().equals(ACTION_SHOW_MEDIA)) {
                    // show a selected file from the media folder
                    final MediaFileElement element = (MediaFileElement) mediaFilesTableModel.getObject(te.getRowId());
                    if (isImage(element.getFilename())) { // show images inline as modal overlay
                        imageDisplay.contextPut("mediaElement", element);
                        imageDisplay.contextPut("imageUri", wikiArticleComp.getImageBaseUri());

                        removeAsListenerAndDispose(cmc);
                        cmc = new CloseableModalController(getWindowControl(), translate("close"), imageDisplay);
                        listenTo(cmc);

                        cmc.activate();
                    } else {
                        deliverMediaFile(ureq, element.getFilename());
                    }
                }
            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(ACTION_DELETE_MEDIAS)) {
                    deleteMediaFile(mediaFilesTableModel.getObjects(tmse.getSelection()), ureq);
                    editContent.contextPut("fileList", wiki.getMediaFileList());
                }
            }
        } else if (source == archiveWikiDialogCtr) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                // convert wiki into IMS content package and copy to users home folder
                final WikiToCPExport utils = new WikiToCPExport(ores, ident, getTranslator());
                utils.archiveWikiToCP();
                showInfo("wiki.exported.done.infomessage");
            }
        }

        else if (source == searchOrCreateArticleForm) {
            String query = searchOrCreateArticleForm.getQuery();
            if (query == null) {
                query = WikiPage.WIKI_INDEX_PAGE;
            }

            page = wiki.findPage(query);
            pageId = page.getPageId();
            if (page.getPageName().equals(Wiki.NEW_PAGE)) {
                setTabsEnabled(false);
                newTopic = query;
                state = TabState.NEW_ARTICLE;
            } else {
                newTopic = null;
                state = TabState.IDLE;
            }
            page.incrementViewCount();
            updatePageContext(ureq, page);
            if (!page.getPageName().startsWith("O_")) {
                breadCrumpCtr.addLink(page.getPageName(), page.getPageName());
            }
            tabs.setSelectedPane(0);
        } else if (source == wikiEditForm) {
            // set recent page id to the page currently used
            this.pageId = page.getPageId();

            boolean wantPreview = false;
            boolean wantSave = false;
            boolean wantClose = false;

            if (event == Event.CANCELLED_EVENT) {
                wantClose = true;
            }

            if (event == Event.DONE_EVENT) {
                wantSave = true;
            }

            if (event.getCommand().equals("save.and.close")) {
                wantClose = true;
                wantSave = true;
                event = Event.DONE_EVENT;
            }

            if (event.getCommand().equals("preview")) {
                wantPreview = true;
                event = Event.DONE_EVENT;
            }

            final boolean dirty = !wikiEditForm.getWikiContent().equals(page.getContent());

            if (wantPreview) {
                final WikiPage preview = new WikiPage("temp");
                preview.setContent(wikiEditForm.getWikiContent());
                wikiVersionDisplayComp.setWikiContent(preview.getContent());
                editContent.put("versionDisplay", wikiVersionDisplayComp);
                // editContent.contextPut("isDirty", Boolean.valueOf(dirty));
            }

            if (wantSave && dirty) {

                editContent.contextPut("isDirty", Boolean.valueOf(false));
                page.setContent(wikiEditForm.getWikiContent());
                page.setModifyAuthor(ident.getKey().longValue());
                page.setUpdateComment(wikiEditForm.getUpdateComment());
                if (page.getInitalAuthor() == 0) {
                    page.setInitalAuthor(ident.getKey().longValue());
                }
                // menu page only editable by admin and owner set new content if changed
                if (page.getPageName().equals(WikiPage.WIKI_MENU_PAGE)) {
                    wikiMenuComp.setWikiContent(page.getContent());
                }
                WikiManager.getInstance().saveWikiPage(ores, page, true, wiki,
                        wikiNotificationTypeHandler.createPublishEventTO(subsContext, ores.getResourceableId(), ureq.getIdentity(), page, EventType.NEW));

                updatePageContext(ureq, page);
            }

            if (dirty && wantPreview && !wantSave) {
                // wikiEditForm.setDirty(true);
                editContent.contextPut("isDirty", Boolean.valueOf(dirty));
            }

            if (wantClose) {
                tabs.setSelectedPane(0);
                doReleaseEditLock();
                return;
            }

        }
    }

    private boolean isImage(final String filename) {
        final String fileSuffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length()).toLowerCase();
        if (fileSuffix.equals("jpg")) {
            return true;
        }
        if (fileSuffix.equals("jpeg")) {
            return true;
        }
        if (fileSuffix.equals("gif")) {
            return true;
        }
        if (fileSuffix.equals("png")) {
            return true;
        }
        return false;
    }

    private void createMediaMetadataFile(final String filename, final Long author) {
        final VFSContainer mediaFolder = WikiManager.getInstance().getMediaFolder(ores);
        // only create metadatafile if base file exists
        if ((VFSLeaf) mediaFolder.resolve(filename) != null) {
            // metafile may exists when files get overwritten
            VFSLeaf metaFile = (VFSLeaf) mediaFolder.resolve(filename + METADATA_SUFFIX);
            if (metaFile == null) {
                // metafile does not exist => create one
                metaFile = mediaFolder.createChildLeaf(filename + METADATA_SUFFIX);
            }
            final Properties p = new Properties();
            p.setProperty(MEDIA_FILE_FILENAME, filename);
            p.setProperty(MEDIA_FILE_CREATIONDATE, String.valueOf(System.currentTimeMillis()));
            p.setProperty(MEDIA_FILE_CREATED_BY, String.valueOf(author));
            try {
                p.store(metaFile.getOutputStream(false), "wiki media files meta properties");
            } catch (final IOException e) {
                throw new OLATRuntimeException(WikiManager.class, "failed to save media files properties for file: " + filename + " and olatresource: "
                        + ores.getResourceableId(), e);
            }
        }
    }

    private void deleteMediaFile(final List toDelete, final UserRequest ureq) {
        for (final Iterator iter = toDelete.iterator(); iter.hasNext();) {
            final VFSContainer mediaFolder = WikiManager.getInstance().getMediaFolder(ores);
            final MediaFileElement element = (MediaFileElement) iter.next();
            if (log.isDebugEnabled()) {
                log.debug("deleting media file: " + element.getFilename());
            }
            if (!element.getFilename().endsWith(METADATA_SUFFIX)) {
                final VFSLeaf file = (VFSLeaf) mediaFolder.resolve(element.getFilename());
                if (file != null) {
                    file.delete();
                    final VFSLeaf metadata = (VFSLeaf) mediaFolder.resolve(element.getFilename() + METADATA_SUFFIX);
                    if (metadata != null) {
                        final Properties p = new Properties();
                        try {
                            p.load(metadata.getInputStream());
                            p.setProperty(MEDIA_FILE_DELETIONDATE, String.valueOf(System.currentTimeMillis()));
                            p.setProperty(MEDIA_FILE_DELETED_BY, String.valueOf(ureq.getIdentity().getKey()));
                            final OutputStream os = metadata.getOutputStream(false);
                            p.store(os, "wiki media file meta properties");
                            os.close();
                        } catch (final IOException e) {
                            throw new OLATRuntimeException("Could'n read properties from media file: " + metadata.getName(), e);
                        }
                    }
                }
            }
        }
        getWindowControl().pop();
    }

    private void setTabsEnabled(final boolean enable) {
        tabs.setEnabled(1, enable);
        openDiscussionButton.setEnabled(enable);

        if (enable && securityCallback.mayEditAndCreateArticle()) {
            tabs.setEnabled(2, enable);
            editPageButton.setEnabled(enable);
        } else {
            tabs.setEnabled(2, false);
            editPageButton.setEnabled(false);
        }
        tabs.setEnabled(3, enable);
        showVersionButton.setEnabled(enable);
    }

    @Override
    protected void doDispose() {
        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());
        doReleaseEditLock();
    }

    @Override
    public Controller cloneController(final UserRequest ureq, final WindowControl wControl) {
        return WikiUIFactory.getInstance().createWikiMainController(ureq, wControl, ores, securityCallback, null);
    }

    private void doReleaseEditLock() {
        if (lockEntry != null && lockEntry.isSuccess()) {
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * Try to aquire a lock on the page
     * 
     * @param ureq
     * @param ores
     */
    private void tryToSetEditLock(final WikiPage page, final UserRequest ureq, final OLATResourceable ores) {
        lockEntry = getLockingService().acquireLock(ores, ureq.getIdentity(), page.getPageName());
        editContent.contextPut("lockEntry", lockEntry);
    }

    /**
     * called by the gui framework upon browser back/forward.
     */
    @Override
    protected void adjustState(final ControllerState cstate, final UserRequest ureq) {
        // the pagename is unique within a wiki.
        final String pageName = cstate.getSerializedState();
        final Wiki wiki = getWiki();

        if (pageName.equals(WikiPage.WIKI_A2Z_PAGE)) {
            final WikiPage a2zPage = wiki.getPage(WikiPage.WIKI_A2Z_PAGE);
            articleContent.contextPut("page", a2zPage);
            a2zPage.setContent(translate("nav.a-z.desc") + wiki.getAllPageNamesSorted());
            wikiArticleComp.setWikiContent(a2zPage.getContent());
            setTabsEnabled(false);
            setState(WikiPage.WIKI_A2Z_PAGE);
        } else {
            final WikiPage page = wiki.getPage(pageName, true);
            if (page != null) {
                this.pageId = page.getPageId();
            }
            page.incrementViewCount();
            updatePageContext(ureq, page);
            // breadCrumpCtr.addLink(page.getPageName(), page.getPageName());
        }

    }

    /**
     * update depended velocity contexts and componetens with latest globally used page
     */
    private void updatePageContext(final UserRequest ureq, final WikiPage page) {
        setState(page.getPageName());

        if (page.getPageName().equals(WikiPage.WIKI_ERROR)) {
            wikiArticleComp.setWikiContent(translate(page.getContent()));
        } else {
            wikiArticleComp.setWikiContent(page.getContent());
        }

        wikiEditForm.setPage(page);
        diffs.clear();
        content.contextPut("page", page);

        articleContent.contextPut("page", page);
        discussionContent.contextPut("page", page);

        editContent.remove(wikiVersionDisplayComp);
        editContent.contextPut("page", page);

        versioningContent.remove(wikiVersionDisplayComp);
        versioningContent.contextPut("page", page);

        final boolean userIsPageCreator = getIdentity().getKey().equals(page.getInitalAuthor());
        if (userIsPageCreator) {
            final String subPath = page.getPageName();
            String businessPath = getWindowControl().getBusinessControl().getAsString();
            businessPath += "[page=" + subPath + ":0]";

            final OLATResourceable wikiRes = OresHelper.createOLATResourceableInstance(WikiArtefact.ARTEFACT_TYPE, ores.getResourceableId());
            final Controller ePFCollCtrl = EPUIFactory.createArtefactCollectWizzardController(ureq, getWindowControl(), wikiRes, businessPath);
            if (ePFCollCtrl != null) {
                content.put("portfolio-link", ePFCollCtrl.getInitialComponent());
            }
        } else {
            clearPortfolioLink();
        }
    }

    private void clearPortfolioLink() {
        content.put("portfolio-link", new Panel("empty"));
    }

    private Wiki getWiki() {
        return WikiManager.getInstance().getOrLoadWiki(ores);
    }
}
