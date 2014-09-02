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

package org.olat.presentation.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryEBL;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.CourseRepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.IdentityRolesForResource;
import org.olat.presentation.bookmark.AddAndEditBookmarkController;
import org.olat.presentation.catalog.CatalogAjaxAddController;
import org.olat.presentation.catalog.CatalogEntryAddController;
import org.olat.presentation.catalog.RepoEntryCategoriesTableController;
import org.olat.presentation.course.run.RunMainController;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.presentation.group.securitygroup.IdentitiesRemoveEvent;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RepositoryDetailsController extends BasicController implements GenericEventListener {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String ACTION_CLOSE = "cmd.close";
    private static final String ACTION_DOWNLOAD = "dl";
    private static final String ACTION_LAUNCH = "lch";
    private static final String ACTION_COPY = "cp";
    private static final String ACTION_BOOKMARK = "bm";
    private static final String ACTION_EDIT = "edt";
    private static final String ACTION_DETAILSEDIT = "dtedt";
    private static final String ACTION_ADD_CATALOG = "add.cat";
    private static final String ACTION_DELETE = "del";
    private static final String ACTION_CLOSE_RESSOURCE = "close.ressource";
    private static final String ACTION_GROUPS = "grp";
    private static final String ACTION_EDITDESC = "chdesc";
    private static final String ACTION_EDITPROP = "chprop";

    private static final String TOOL_BOOKMARK = "b";
    private static final String TOOL_COPY = "c";
    private static final String TOOL_DOWNLOAD = "d";
    private static final String TOOL_EDIT = "e";
    private static final String TOOL_CATALOG = "cat";
    private static final String TOOL_CHDESC = "chd";
    private static final String TOOL_CHPROP = "chp";
    private static final String TOOL_LAUNCH = "l";
    private static final String TOOL_CLOSE_RESSOURCE = "cr";

    private final VelocityContainer main;
    private final Link downloadButton;
    private final Link backLink;
    private final Link launchButton;
    private final Link loginLink;

    private GroupController groupController, groupEditController;
    private SecurityGroup ownerGroup;
    private AddAndEditBookmarkController bookmarkController;
    private ToolController detailsToolC = null;
    private RepositoryCopyController copyController;
    private RepositoryEditPropertiesController repositoryEditPropertiesController;
    private RepositoryEditDescriptionController repositoryEditDescriptionController;
    private RepoEntryCategoriesTableController repoEntryCategoriesTableController;
    private CloseableModalController closeableModalController;
    private DialogBoxController deleteDialogController;
    private Controller catalogAdddController;
    private Controller detailsForm;

    private RepositoryEntry repositoryEntry;
    private IdentityRolesForResource identityRoles = new IdentityRolesForResource(false, false, false, false);

    private boolean jumpfromcourse = false;
    public static final String ACTIVATE_EDITOR = "activateEditor";
    public static final String ACTIVATE_RUN = "activateRun";

    private DisplayCourseInfoForm courseInfoForm;
    private DisplayInfoForm displayInfoForm;

    private LockResult lockResult;
    private WizardCloseResourceController wc;
    private CloseableModalController cmc;

    // different instances for "copy" and "settings change", since it is important to know what triggered the CLOSE_MODAL_EVENT
    private CloseableModalController copyCloseableModalController;
    private CloseableModalController settingsCloseableModalController;

    private BaseSecurityEBL baseSecurityEBL;
    private RepositoryEBL repositoryEBL;

    private CampusConfiguration campusConfiguration;

    /**
     * Controller displaying details of a given repository entry.
     * 
     * @param ureq
     * @param wControl
     * @param mainPanel
     */
    public RepositoryDetailsController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        baseSecurityEBL = CoreSpringFactory.getBean(BaseSecurityEBL.class);
        repositoryEBL = CoreSpringFactory.getBean(RepositoryEBL.class);
        if (log.isDebugEnabled()) {
            log.debug("Constructing ReposityMainController using velocity root " + velocity_root);
        }
        // main component layed out in panel
        main = createVelocityContainer("details");

        downloadButton = LinkFactory.createButton("details.download", main, this);
        LinkFactory.markDownloadLink(downloadButton);
        launchButton = LinkFactory.createButton("details.launch", main, this);

        backLink = LinkFactory.createLinkBack(main, this);
        loginLink = LinkFactory.createLink("repo.login", main, this);

        campusConfiguration = CoreSpringFactory.getBean(org.olat.lms.core.course.campus.CampusConfiguration.class);

        putInitialPanel(main);
    }

    /**
     * @param ureq
     */
    private void updateRepositoryEntryView(final UserRequest ureq) {

        main.contextPut("isOwner", new Boolean(identityRoles.isOwner()));
        main.contextPut("isAuthor", new Boolean(identityRoles.isAuthor()));
        main.contextPut("isOlatAdmin", new Boolean(identityRoles.isOLATAdmin()));
        main.contextPut("launchableTyp", new Boolean(checkIsRepositoryEntryTypeLaunchable()));
        final String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
        main.contextPut("extlink", url);

        final String displayName = DynamicTabHelper.getDisplayName(repositoryEntry, getLocale());
        main.contextPut("title", displayName);

        final boolean isLaunchable = repositoryEBL.checkIsRepositoryEntryLaunchable(ureq.getIdentity(), ureq.getUserSession().getRoles(), repositoryEntry);
        launchButton.setEnabled(isLaunchable);
        launchButton.setTextReasonForDisabling(translate("launch.noaccess"));
        downloadButton.setEnabled(repositoryEntry.getCanDownload());
        downloadButton.setTextReasonForDisabling(translate("disabledexportreason"));

        if (repositoryEntry.getDescription() != null) {
            main.contextPut("description", Formatter.formatLatexFormulas(repositoryEntry.getDescription()));
        }
        /* STATIC_METHOD_REFACTORING */
        final ImageComponent ic = getRepositoryService().getImageComponentForRepositoryEntry("image", repositoryEntry);

        if (ic != null) {
            // display only within 600x300 - everything else looks ugly
            ic.setMaxWithAndHeightToFitWithin(600, 300);
            main.contextPut("hasImage", Boolean.TRUE);
            main.put("image", ic);
        } else {
            main.contextPut("hasImage", Boolean.FALSE);
        }

        main.contextPut("id", repositoryEntry.getResourceableId());
        main.contextPut("ores_id", repositoryEntry.getOlatResource().getResourceableId());
        main.contextPut("initialauthor", repositoryEntry.getInitialAuthor());
        main.contextPut("userlang", I18nManager.getInstance().getLocaleKey(ureq.getLocale()));
        main.contextPut("isGuestAllowed", (repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS_GUESTS ? Boolean.TRUE : Boolean.FALSE));
        main.contextPut("isGuest", Boolean.valueOf(ureq.getUserSession().getRoles().isGuestOnly()));

        final String typeName = repositoryEntry.getOlatResource().getResourceableTypeName();
        final StringBuilder typeDisplayText = new StringBuilder(100);
        if (typeName != null) { // add image and typename code
            final RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer(ureq.getLocale());
            typeDisplayText.append("<span class=\"b_with_small_icon_left ");
            typeDisplayText.append(reir.getIconCssClass(repositoryEntry));
            typeDisplayText.append("\">");
            final String tName = ControllerFactory.translateResourceableTypeName(typeName, getLocale());
            typeDisplayText.append(tName);
            if (DynamicTabHelper.isRepositoryEntryClosed(repositoryEntry)) {
                typeDisplayText.append(" " + "(" + translate("title.prefix.closed") + ")");
            }
            typeDisplayText.append("</span>");
        } else {
            typeDisplayText.append(translate("cif.type.na"));
        }
        main.contextPut("type", typeDisplayText.toString());
        final VelocityContainer infopanelVC = createVelocityContainer("infopanel");
        // show how many users are currently using this resource
        String numUsers;
        final OLATResourceable ores = repositoryEntry.getOlatResource();
        int cnt = 0;
        final OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource()
                .getResourceableId());
        if (ores != null) {
            cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
        }
        numUsers = String.valueOf(cnt);
        infopanelVC.contextPut("numUsers", numUsers);

        removeAsListenerAndDispose(displayInfoForm);
        displayInfoForm = new DisplayInfoForm(ureq, getWindowControl(), repositoryEntry);
        listenTo(displayInfoForm);
        main.put("displayform", displayInfoForm.getInitialComponent());

        infopanelVC.contextPut("isAuthor", Boolean.valueOf(identityRoles.isAuthor()));
        infopanelVC.contextPut("isOwner", Boolean.valueOf(identityRoles.isOwner()));
        // init handler details
        final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        infopanelVC.contextPut("iscourse", new Boolean(handler instanceof CourseRepositoryHandler));
        main.contextPut("iscourse", new Boolean(handler instanceof CourseRepositoryHandler));
        // brasato:: review why such a type check was necessary

        if (handler instanceof CourseRepositoryHandler) {
            removeAsListenerAndDispose(courseInfoForm);
            courseInfoForm = new DisplayCourseInfoForm(ureq, getWindowControl(), CourseFactory.loadCourse(repositoryEntry.getOlatResource()));
            listenTo(courseInfoForm);
            infopanelVC.put("CourseInfoForm", courseInfoForm.getInitialComponent());
        }
        removeAsListenerAndDispose(detailsForm);
        detailsForm = handler.createDetailsForm(ureq, getWindowControl(), repositoryEntry.getOlatResource());
        if (detailsForm != null) { // push handler specific details view
            listenTo(detailsForm);
            infopanelVC.contextPut("hasHandlerDetails", Boolean.valueOf("true"));
            infopanelVC.put("handlerDetails", detailsForm.getInitialComponent());
        } else {
            infopanelVC.contextRemove("hasHandlerDetails");
        }
        // init reference usage where is it in use
        ReferenceService referenceService = CoreSpringFactory.getBean(ReferenceService.class);
        final String referenceDetails = referenceService.getReferencesToSummary(repositoryEntry.getOlatResource(), ureq.getLocale());
        if (referenceDetails != null) {
            infopanelVC.contextPut("referenceDetails", referenceDetails);
        } else {
            infopanelVC.contextRemove("referenceDetails");
        }

        // Number of launches
        String numLaunches;
        if (repositoryEntry.getCanLaunch()) {
            numLaunches = String.valueOf(repositoryEntry.getLaunchCounter());
        } else {
            numLaunches = translate("cif.canLaunch.na");
        }
        infopanelVC.contextPut("numLaunches", numLaunches);

        // Number of downloads
        String numDownloads;
        if (repositoryEntry.getCanDownload()) {
            numDownloads = String.valueOf(repositoryEntry.getDownloadCounter());
        } else {
            numDownloads = translate("cif.canDownload.na");
        }

        infopanelVC.contextPut("numDownloads", numDownloads);

        if (repositoryEntry.getLastUsage() != null) {
            infopanelVC.contextPut("lastUsage", repositoryEntry.getLastUsage());
        } else {
            infopanelVC.contextPut("lastUsage", translate("cif.lastUsage.na"));
        }

        main.put(infopanelVC.getComponentName(), infopanelVC);

        removeAsListenerAndDispose(groupController);
        groupController = new GroupController(ureq, getWindowControl(), false, true, false, repositoryEntry.getOwnerGroup(), null);
        listenTo(groupController);

        main.put("ownertable", groupController.getInitialComponent());

    }

    /**
     * @return
     */
    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

    /**
     * @param ureq
     * @param newToolController
     */
    private void updateView(final UserRequest ureq) {
        identityRoles = baseSecurityEBL.getIdentityRolesWithLoadRepositoryEntry(ureq.getIdentity(), ureq.getUserSession().getRoles(), repositoryEntry);
        updateRepositoryEntryView(ureq);
        updateDetailsToolC(ureq);
        updateCategoriesTableC(ureq);
    }

    private void updateCategoriesTableC(final UserRequest ureq) {
        // load category links
        removeAsListenerAndDispose(repoEntryCategoriesTableController);
        repoEntryCategoriesTableController = new RepoEntryCategoriesTableController(ureq, getWindowControl(), this.repositoryEntry, identityRoles.isOwnerOrAdmin());
        listenTo(repoEntryCategoriesTableController);
        main.put("repoEntryCategoriesTable", repoEntryCategoriesTableController.getInitialComponent());
    }

    /**
     * @param newToolController
     */
    private void updateDetailsToolC(final UserRequest ureq) {
        boolean isNewController = false;
        if (detailsToolC == null) {
            detailsToolC = ToolFactory.createToolController(getWindowControl());
            listenTo(detailsToolC);
            isNewController = true;
        }
        // init handler details
        final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        if (isNewController) {
            detailsToolC.addHeader(translate("tools.details.header"));
            detailsToolC.addLink(ACTION_LAUNCH, translate("details.launch"), TOOL_LAUNCH, null);
        }
        detailsToolC.setEnabled(TOOL_LAUNCH, repositoryEBL.checkIsRepositoryEntryLaunchable(ureq.getIdentity(), ureq.getUserSession().getRoles(), repositoryEntry));
        if (!identityRoles.isGuestOnly()) {
            if (isNewController) {
                // mark as download link
                detailsToolC.addLink(ACTION_DOWNLOAD, translate("details.download"), TOOL_DOWNLOAD, null, true);
                detailsToolC.addLink(ACTION_BOOKMARK, translate("details.bookmark"), TOOL_BOOKMARK, null);
            }
            boolean canDownload = repositoryEntry.getCanDownload() && handler.supportsDownload(repositoryEntry);
            // disable download for courses if not author or owner
            if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && !(identityRoles.isOwnerOrAuthor())) {
                canDownload = false;
            }
            // always enable download for owners
            if (identityRoles.isOwner() && handler.supportsDownload(repositoryEntry)) {
                canDownload = true;
            }
            detailsToolC.setEnabled(TOOL_DOWNLOAD, canDownload);
            boolean canBookmark = true;
            if (getBookmarkService().isResourceableBookmarked(ureq.getIdentity(), repositoryEntry) || !repositoryEntry.getCanLaunch()) {
                canBookmark = false;
            }
            detailsToolC.setEnabled(TOOL_BOOKMARK, canBookmark);
        }
        if (identityRoles.isOwnerOrAuthor()) {
            boolean canCopy = repositoryEntry.getCanCopy();
            if (identityRoles.isOwner()) {
                if (isNewController) {
                    detailsToolC.addLink(ACTION_EDIT, translate("details.openeditor"), TOOL_EDIT, null);
                    detailsToolC.addLink(ACTION_EDITDESC, translate("details.chdesc"), TOOL_CHDESC, null);
                    detailsToolC.addLink(ACTION_EDITPROP, translate("details.chprop"), TOOL_CHPROP, null);
                    detailsToolC.addLink(ACTION_ADD_CATALOG, translate("details.catadd"), TOOL_CATALOG, null);
                    if (isCourseAndNotClosed()) {
                        detailsToolC.addLink(ACTION_CLOSE_RESSOURCE, translate("details.close.ressoure"), TOOL_CLOSE_RESSOURCE, null);
                    }
                }
                // update catalog link
                detailsToolC.setEnabled(TOOL_CATALOG, (repositoryEntry.getAccess() >= RepositoryEntry.ACC_USERS));
            }
            if (isNewController) {
                detailsToolC.addLink(ACTION_COPY, translate("details.copy"), TOOL_COPY, null);
            }
            if (identityRoles.isOwner()) {
                if (isNewController) {
                    detailsToolC.addLink(ACTION_DELETE, translate("details.delete"));
                    detailsToolC.addLink(ACTION_GROUPS, translate("details.groups"));
                }
                // enable
                detailsToolC.setEnabled(TOOL_EDIT, handler.supportsEdit(repositoryEntry));
                detailsToolC.setEnabled(TOOL_CHDESC, true);
                // disable in the case of campus course
                if (isCampusCourseAndSynchronizeTitleAndDescriptionEnabled()) {
                    detailsToolC.setEnabled(TOOL_CHDESC, false);
                }
                detailsToolC.setEnabled(TOOL_CHPROP, true);
                canCopy = true;
            }
            detailsToolC.setEnabled(TOOL_COPY, canCopy);
        }
        if (isNewController) {
            detailsToolC.addLink(ACTION_CLOSE, translate("details.close"), null, "b_toolbox_close");
        }
    }

    /**
     * @return
     */
    private boolean isCourseAndNotClosed() {
        return (OresHelper.isOfType(repositoryEntry.getOlatResource(), CourseModule.class))
                && (!getRepositoryService().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed());
    }

    private BookmarkService getBookmarkService() {
        return CoreSpringFactory.getBean(BookmarkService.class);
    }

    /**
     * Sets a repository entry for this details controller. Returns a corresponding tools controller
     * 
     * @param entry
     * @param ureq
     * @return A tool controller representing available tools for the given entry.
     */
    public ToolController setEntry(final RepositoryEntry entry, final UserRequest ureq, final boolean jumpfromcourse) {
        this.jumpfromcourse = jumpfromcourse;
        if (repositoryEntry != null) {
            // The controller has already a repository-entry => do de-register it
            CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, repositoryEntry);
        }
        repositoryEntry = entry;
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), repositoryEntry);
        identityRoles = baseSecurityEBL.getIdentityRolesWithLoadRepositoryEntry(ureq.getIdentity(), ureq.getUserSession().getRoles(), repositoryEntry);
        removeAsListenerAndDispose(detailsToolC);
        detailsToolC = null; // force recreation of tool controller
        updateView(ureq);
        return detailsToolC;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final String cmd = event.getCommand();

        if (source == main) {
            if (cmd.equals(ACTION_DETAILSEDIT)) {
                // detailsForm.setDisplayOnly(false);
                main.contextPut("enableEdit", Boolean.valueOf(false)); // disable edit
                // button
                return;
            } else if (cmd.equals(ACTION_CLOSE)) { // close details
                doCloseDetailView(ureq);
                return;
            } else if (cmd.equals(ACTION_LAUNCH)) { // launch resource

            }
        } else if (source == backLink) {
            doCloseDetailView(ureq);
            return;
        } else if (source == downloadButton) {
            doDownload(ureq);
        } else if (source == launchButton) {
            doLaunch(ureq);
        } else if (source == loginLink) {
            DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
        }
    }

    private void doCloseDetailView(final UserRequest ureq) {
        // REVIEW:pb:note:handles jumps from Catalog and Course
        if (jumpfromcourse && repositoryEntry.getCanLaunch()) {
            doLaunch(ureq);
        } else {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    void deleteRepositoryEntry(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry entry) {
        if (getRepositoryService().deleteRepositoryEntryWithAllData(ureq, wControl, repositoryEntry)) {
            fireEvent(ureq, new EntryChangedEvent(entry, EntryChangedEvent.DELETED));
            showInfo("info.entry.deleted");
        } else {
            showInfo("info.could.not.delete.entry");
        }
    }

    /**
     * Also used by RepositoryMainController
     * 
     * @param ureq
     */
    void doLaunch(final UserRequest ureq) {
        final RepositoryHandler typeToLaunch = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        if (typeToLaunch == null) {
            final StringBuilder sb = new StringBuilder(translate("error.launch"));
            sb.append(": No launcher for repository entry: ");
            sb.append(repositoryEntry.getKey());
            throw new OLATRuntimeException(RepositoryDetailsController.class, sb.toString(), null);
        }
        if (getRepositoryService().lookupRepositoryEntry(repositoryEntry.getKey()) == null) {
            showInfo("info.entry.deleted");
            return;
        }
        DynamicTabHelper.openRepoEntryTabInRunMode(repositoryEntry, ureq, typeToLaunch);
        /**
         * close detail page after resource is closed DONE_EVENT will be catched by RepositoryMainController
         */
        fireEvent(ureq, Event.DONE_EVENT);
    }

    private boolean checkIsRepositoryEntryTypeLaunchable() {
        final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        return handler.supportsLaunch(repositoryEntry);
    }

    /**
     * Activates the closeableModalController with the input controller's component, if not null
     * 
     * @param ureq
     * @param contentController
     */
    private void doEditSettings(final UserRequest ureq, final Controller contentController) {
        if (!identityRoles.isOwnerOrAuthor()) {
            throw new OLATSecurityException("Trying to edit properties , but user is not author: user = " + ureq.getIdentity());
        }

        final Component component = contentController.getInitialComponent();

        if (component != null) {
            removeAsListenerAndDispose(settingsCloseableModalController);
            settingsCloseableModalController = new CloseableModalController(getWindowControl(), translate("close"), contentController.getInitialComponent());
            listenTo(settingsCloseableModalController);

            settingsCloseableModalController.activate();
        }
        return;
    }

    private void doAddBookmark(final Controller contentController) {
        removeAsListenerAndDispose(closeableModalController);
        closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), contentController.getInitialComponent());
        listenTo(closeableModalController);

        closeableModalController.activate();
        return;
    }

    /**
     * Also used by RepositoryMainController
     * 
     * @param ureq
     */
    void doDownload(final UserRequest ureq) {
        final RepositoryHandler typeToDownload = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);

        if (typeToDownload == null) {
            final StringBuilder sb = new StringBuilder(translate("error.download"));
            sb.append(": No download handler for repository entry: ");
            sb.append(repositoryEntry.getKey());
            throw new OLATRuntimeException(RepositoryDetailsController.class, sb.toString(), null);
        }
        final OLATResource ores = OLATResourceManager.getInstance().findResourceable(repositoryEntry.getOlatResource());
        if (ores == null) {
            showError("error.download");
            return;
        }
        final boolean isAlreadyLocked = typeToDownload.isLocked(ores);
        try {
            lockResult = typeToDownload.acquireLock(ores, ureq.getIdentity());
            if (lockResult == null || (lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
                final MediaResource mr = typeToDownload.getAsMediaResource(ores);
                if (mr != null) {
                    getRepositoryService().incrementDownloadCounter(repositoryEntry);
                    ureq.getDispatchResult().setResultingMediaResource(mr);
                } else {
                    showError("error.export");
                    fireEvent(ureq, Event.FAILED_EVENT);
                }
            } else if (lockResult != null && lockResult.isSuccess() && isAlreadyLocked) {
                showInfo("warning.course.alreadylocked.bySameUser", lockResult.getOwner().getName());
                lockResult = null; // invalid lock, it was already locked
            } else {
                showInfo("warning.course.alreadylocked", lockResult.getOwner().getName());
            }
        } finally {
            if ((lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
                typeToDownload.releaseLock(lockResult);
                lockResult = null;
            }
        }
    }

    /**
     * If lock successfully aquired start copy, else show warning.
     * 
     * @param ureq
     */
    private void doCopy(final UserRequest ureq) {
        final OLATResource ores = OLATResourceManager.getInstance().findResourceable(repositoryEntry.getOlatResource());
        final boolean isAlreadyLocked = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).isLocked(ores);
        lockResult = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).acquireLock(ores, ureq.getIdentity());
        if (lockResult == null || (lockResult != null && lockResult.isSuccess()) && !isAlreadyLocked) {
            removeAsListenerAndDispose(copyController);
            copyController = new RepositoryCopyController(ureq, getWindowControl(), repositoryEntry);
            listenTo(copyController);

            removeAsListenerAndDispose(copyCloseableModalController);
            copyCloseableModalController = new CloseableModalController(getWindowControl(), translate("close"), copyController.getInitialComponent());
            listenTo(copyCloseableModalController);

            copyCloseableModalController.activate();
        } else if (lockResult != null && lockResult.isSuccess() && isAlreadyLocked) {
            showWarning("warning.course.alreadylocked.bySameUser");
            lockResult = null;
        } else {
            showWarning("warning.course.alreadylocked", lockResult.getOwner().getName());
        }
    }

    /**
     * Also used by RepositoryMainController
     * 
     * @param ureq
     */
    void doEdit(final UserRequest ureq) {
        if (!identityRoles.isOwner()) {
            throw new OLATSecurityException("Trying to launch editor, but not allowed: user = " + ureq.getIdentity());
        }
        final RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        if (!typeToEdit.supportsEdit(repositoryEntry)) {
            throw new AssertException("Trying to edit repository entry which has no assoiciated editor: " + typeToEdit);
        }

        final OLATResourceable ores = repositoryEntry.getOlatResource();

        // user activity logger is set by course factory
        final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        final Controller editorController = typeToEdit.createEditorController(ores, ureq, dts.getWindowControl());
        if (editorController == null) {
            // editor could not be created -> warning is shown
            return;
        }
        DynamicTabHelper.openRepoEntryTab(repositoryEntry, ureq, editorController, repositoryEntry.getDisplayname(), RepositoryDetailsController.ACTIVATE_EDITOR);
    }

    /**
     * Internal helper to initiate the add to catalog workflow
     * 
     * @param ureq
     */
    private void doAddCatalog(final UserRequest ureq) {
        removeAsListenerAndDispose(catalogAdddController);
        final boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
        if (ajax) {
            // fancy ajax tree
            catalogAdddController = new CatalogAjaxAddController(ureq, getWindowControl(), repositoryEntry);
        } else {
            // old-school selection tree
            catalogAdddController = new CatalogEntryAddController(ureq, getWindowControl(), repositoryEntry);
        }

        listenTo(catalogAdddController);
        removeAsListenerAndDispose(closeableModalController);
        closeableModalController = new CloseableModalController(getWindowControl(), "close", catalogAdddController.getInitialComponent());
        listenTo(closeableModalController);
        closeableModalController.activate();
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (repositoryEntry != null) {
            // reload since already 'detached'
            repositoryEntry = getRepositoryService().lookupRepositoryEntry(repositoryEntry.getKey());
        }
        final String cmd = event.getCommand();
        if (source == groupEditController) {
            if (event instanceof IdentitiesAddEvent) { // FIXME:chg: Move into seperate RepositoryOwnerGroupController like BusinessGroupEditController ?
                final IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
                final RepositoryService rm = getRepositoryService();
                // add to group and also adds identities really added to the event.
                // this is then later used by the GroupController to determine if the
                // model should be updated or not.
                rm.addOwners(ureq.getIdentity(), identitiesAddedEvent, repositoryEntry);
            } else if (event instanceof IdentitiesRemoveEvent) {
                final IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
                final RepositoryService rm = getRepositoryService();
                rm.removeOwners(ureq.getIdentity(), identitiesRemoveEvent.getRemovedIdentities(), repositoryEntry);
            }
            updateView(ureq);
        } else if (source == detailsToolC) {
            if (cmd.equals(ACTION_DOWNLOAD)) { // download
                doDownload(ureq);
                return;
            } else if (cmd.equals(ACTION_LAUNCH)) { // launch resource
                doLaunch(ureq);
                return;
            } else if (cmd.equals(ACTION_EDIT)) { // start editor
                doEdit(ureq);
                return;
            } else if (cmd.equals(ACTION_EDITDESC)) { // change description
                removeAsListenerAndDispose(repositoryEditDescriptionController);
                repositoryEditDescriptionController = new RepositoryEditDescriptionController(ureq, getWindowControl(), repositoryEntry, false);
                listenTo(repositoryEditDescriptionController);
                doEditSettings(ureq, repositoryEditDescriptionController);
                return;
            } else if (cmd.equals(ACTION_ADD_CATALOG)) { // start add to catalog workflow
                doAddCatalog(ureq);
                return;
            } else if (cmd.equals(ACTION_EDITPROP)) { // change properties
                removeAsListenerAndDispose(repositoryEditPropertiesController);
                repositoryEditPropertiesController = new RepositoryEditPropertiesController(ureq, getWindowControl(), repositoryEntry, false);
                listenTo(repositoryEditPropertiesController);
                doEditSettings(ureq, repositoryEditPropertiesController);
                return;
            } else if (cmd.equals(ACTION_CLOSE)) {
                doCloseDetailView(ureq);
                return;
            } else if (cmd.equals(ACTION_BOOKMARK)) {
                removeAsListenerAndDispose(bookmarkController);
                bookmarkController = new AddAndEditBookmarkController(ureq, getWindowControl(), repositoryEntry.getDisplayname(), "", repositoryEntry, repositoryEntry
                        .getOlatResource().getResourceableTypeName());
                listenTo(bookmarkController);

                doAddBookmark(bookmarkController);
                return;
            } else if (cmd.equals(ACTION_COPY)) { // copy
                // OLAT-6786 => owner is updated on refresh, author just after logout/login
                if (!identityRoles.isOwnerOrAuthor()) {
                    throw new OLATSecurityException("Trying to copy, but user is not owner/author: user = " + ureq.getIdentity());
                }
                doCopy(ureq);
                return;
            } else if (cmd.equals(ACTION_GROUPS)) { // edit authors group
                if (!identityRoles.isOwner()) {
                    throw new OLATSecurityException("Trying to access groupmanagement, but not allowed: user = " + ureq.getIdentity());
                }
                ownerGroup = repositoryEntry.getOwnerGroup();

                removeAsListenerAndDispose(groupEditController);
                groupEditController = new GroupController(ureq, getWindowControl(), true, true, false, ownerGroup, null);
                listenTo(groupEditController);

                final VelocityContainer groupContainer = createVelocityContainer("groups");
                groupContainer.put("groupcomp", groupEditController.getInitialComponent());

                removeAsListenerAndDispose(cmc);
                final CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"), groupContainer);
                listenTo(cmc);

                cmc.activate();
                return;
            } else if (cmd.equals(ACTION_CLOSE_RESSOURCE)) {
                doCloseResource(ureq);
                return;
            } else if (cmd.equals(ACTION_DELETE)) { // delete
                if (!identityRoles.isOwner()) {
                    throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
                }
                // show how many users are currently using this resource
                final OLATResourceable ores = repositoryEntry.getOlatResource();

                final String dialogTitle = translate("del.header", repositoryEntry.getDisplayname());
                final OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry
                        .getOlatResource().getResourceableId());
                final int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);

                final String dialogText = translate("del.confirm", String.valueOf(cnt));
                deleteDialogController = activateYesNoDialog(ureq, dialogTitle, dialogText, deleteDialogController);
                return;
            }
        } else if (source == wc) {
            if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();

            } else if (event == Event.DONE_EVENT) {
                cmc.deactivate();

                removeAsListenerAndDispose(detailsToolC);
                detailsToolC = null; // force recreation of tool controller
                updateView(ureq);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == bookmarkController) {
            closeableModalController.deactivate();
            if (event.equals(Event.DONE_EVENT)) { // bookmark added... remove tool
                if (detailsToolC != null) {
                    detailsToolC.setEnabled(TOOL_BOOKMARK, false);
                }
            }
        } else if (source == copyController) {
            RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).releaseLock(lockResult);
            lockResult = null;
            copyCloseableModalController.deactivate();
            if (event == Event.DONE_EVENT) {
                fireEvent(ureq, Event.DONE_EVENT); // go back to overview on success
                fireEvent(ureq, new EntryChangedEvent(copyController.getNewEntry(), EntryChangedEvent.ADDED)); // go
                // back to overview on success
            } else if (event == Event.FAILED_EVENT) { // copy failed, go back to
                // overview
                fireEvent(ureq, Event.DONE_EVENT); // go back to overview on failure
            } else if (event instanceof EntryChangedEvent) {
                fireEvent(ureq, event);
            }
            removeAsListenerAndDispose(copyController);
            copyController = null;
        } else if (source == repositoryEditDescriptionController) {
            if (event == Event.CHANGED_EVENT) {
                this.repositoryEntry = updateRepositoryEntry(repositoryEditDescriptionController.getRepositoryEntry());
                // do not close upon save/upload image closeableModalController.deactivate();
                updateView(ureq);
            } else if (event == Event.CANCELLED_EVENT) {
                settingsCloseableModalController.deactivate();
                removeAsListenerAndDispose(repositoryEditDescriptionController);
                this.repositoryEntry = repositoryEditDescriptionController.getRepositoryEntry();
            }
        } else if (source == repositoryEditPropertiesController) {
            if (event == Event.CHANGED_EVENT || event.getCommand().equals("courseChanged")) {
                // RepositoryEntry changed and was already reloaded at the beginning of the event method
                updateView(ureq);
                final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
                final boolean canDownload = repositoryEntry.getCanDownload() && handler.supportsDownload(repositoryEntry);
                detailsToolC.setEnabled(TOOL_DOWNLOAD, canDownload);
                if (checkIsRepositoryEntryTypeLaunchable()) {
                    detailsToolC.setEnabled(TOOL_LAUNCH,
                            repositoryEBL.checkIsRepositoryEntryLaunchable(ureq.getIdentity(), ureq.getUserSession().getRoles(), repositoryEntry));
                }
                if (event.getCommand().equals("courseChanged")) {
                    removeAsListenerAndDispose(repositoryEditPropertiesController);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                settingsCloseableModalController.deactivate();
                removeAsListenerAndDispose(repositoryEditPropertiesController);
            } else if (event == Event.DONE_EVENT) {
                removeAsListenerAndDispose(repositoryEditPropertiesController);
                repositoryEditPropertiesController = null;
            }
        } else if (source == deleteDialogController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                deleteRepositoryEntry(ureq, getWindowControl(), this.repositoryEntry);
            }
        } else if (source == settingsCloseableModalController) {
            if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
                updateView(ureq);
                // check if commit or not the course conf changes
                if (repositoryEditPropertiesController != null) {
                    final boolean configsChanged = repositoryEditPropertiesController.checkIfCourseConfigChanged(ureq);
                    if (!configsChanged) {
                        removeAsListenerAndDispose(repositoryEditPropertiesController);
                        repositoryEditPropertiesController = null;
                    }
                }
            }
        } else if (source == copyCloseableModalController) {
            if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
                updateView(ureq);
                if (copyController != null) {
                    // copyController's modal dialog was closed, that is cancel copy
                    RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).releaseLock(lockResult);
                    lockResult = null;
                    removeAsListenerAndDispose(copyController);
                    copyController = null;
                }
            }
        } else if (source == catalogAdddController) {
            // finish modal dialog and reload categories list controller
            closeableModalController.deactivate();
            updateCategoriesTableC(ureq);
        }
    }

    /**
     * @param guiRepositoryEntry
     */
    private RepositoryEntry updateRepositoryEntry(RepositoryEntry guiRepositoryEntry) {
        // RepositoryEntry changed
        // need a reload from hibernate because create a new cp load a repository-entry (OLAT-5631)
        RepositoryEntry reloaded = getRepositoryService().loadRepositoryEntry(guiRepositoryEntry);
        reloaded.setDisplayname(guiRepositoryEntry.getDisplayname());
        reloaded.setDescription(guiRepositoryEntry.getDescription());
        getRepositoryService().updateRepositoryEntry(reloaded);
        return reloaded;
    }

    /**
     * @param ureq
     */
    private void doCloseResource(final UserRequest ureq) {
        final RepositoryHandler repoHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);

        removeAsListenerAndDispose(wc);
        wc = repoHandler.createCloseResourceController(ureq, getWindowControl(), repositoryEntry);
        listenTo(wc);

        wc.startWorkflow();

        removeAsListenerAndDispose(cmc);
        cmc = new CloseableModalController(getWindowControl(), translate("close"), wc.getInitialComponent());
        listenTo(cmc);

        cmc.activate();
    }

    @Override
    protected void doDispose() {
        if (lockResult != null) {
            // the lock it is assumed to be released after export/copy operation, but release it anyway in case it failed to release
            if (repositoryEntry != null) {
                RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry).releaseLock(lockResult);
            }
            lockResult = null;
        }
        if (repositoryEntry != null) {
            CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, repositoryEntry);
        }
        if (copyCloseableModalController != null) {
            copyCloseableModalController.dispose();
            copyCloseableModalController = null;
        }
        if (settingsCloseableModalController != null) {
            settingsCloseableModalController.dispose();
            settingsCloseableModalController = null;
        }
    }

    @Override
    public void event(final Event event) {
        if (event instanceof EntryChangedEvent) {
            repositoryEntry = getRepositoryService().lookupRepositoryEntry(repositoryEntry.getKey());
        }
    }

    /**
     * @return
     */
    public ToolController getDetailsToolController() {
        return this.detailsToolC;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    /**
     * @return 'true' if the course is a campuskurs and the the appropriate flag indicating the synchronization of the description and the title is true and 'false'
     *         otherwise.
     */
    private boolean isCampusCourseAndSynchronizeTitleAndDescriptionEnabled() {
        String[] descriptionStartWithArray = campusConfiguration.getDescriptionStartWithStringAsArray();
        return campusConfiguration.isSynchronizeTitleAndDescriptionEnabled() && repositoryEntry.getDescription() != null
                && StringUtils.startsWithAny(repositoryEntry.getDescription().toLowerCase(), descriptionStartWithArray);
    }

}
