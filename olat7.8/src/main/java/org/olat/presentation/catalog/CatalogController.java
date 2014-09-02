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

package org.olat.presentation.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.bookmark.AddAndEditBookmarkController;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.presentation.group.securitygroup.IdentitiesRemoveEvent;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.repository.EntryChangedEvent;
import org.olat.presentation.repository.RepositoryEditDescriptionController;
import org.olat.presentation.repository.RepositoryEntryIconRenderer;
import org.olat.presentation.repository.RepositorySearchController;
import org.olat.presentation.repository.RepositoryTableModel;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.ContactList;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <pre>
 * Description:
 * <P>
 * The CatalogController is responsible for:
 * <ul>
 * <li>displaying the catalog with its categories and linked resources,
 * starting from the supplied root node</li>
 * <li>handling the access to the actual category:
 * <ul>
 * <li>OLATAdmin is/should be the owner of catalog root</li>
 * <li>LocalTreeAdmin is administrator of a subtree within the catalog</li>
 * <li>Author is responsible for adding his resources to the catalog</li>
 * <li>ordinary user browses the catalog for quickly finding his desired
 * resources</li>
 * </ul>
 * The CatalogController accomplish this access rights by modifying the
 * corresponding toolbox entries in the GUI, which grant access to the mentioned
 * actions.</li>
 * <li>handles the controllers and forms allowing to edit, create the catalog
 * structure
 * <ol>
 * <li>change category data (name, description)</li>
 * <li>modify category's localTreeAdmin group</li>
 * <li>contact category's localTreeAdmin group</li>
 * <li>add a new subcategory</li>
 * <li>add a resource link</li>
 * <li>remove a resource link</li>
 * <li>paste structure</li>
 * </ol>
 * The OLATAdmin as superuser can invoke all of the above listed actions. <br>
 * Whereas the LocalTreeAdmin is restricted to the follwing set: on the
 * LocalTreeAdmin's subtree-root-node only 4,6 are possible. But from the
 * children on he/she can invoke any of 1-6 except 5.<br>
 * The author is solely able to invoke 5.
 * </ul>
 * </li>
 * </ul>
 * The catalog is based upon the idea of having a lot of resources which must
 * somehow be ordered to find them quickly. Frankly speaking the catalog only
 * makes sense if no access restrictions to the linked resources apply. This in
 * mind, it is solely possible to link resources which are accessible for the
 * users of the installation.
 * </pre>
 * 
 * Date: 2005/10/14 12:35:40 <br>
 * 
 * @author Felix Jost
 */
public class CatalogController extends BasicController implements Activateable {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String ENABLE_GROUPMNGMNT = "enableGroupMngmnt";
    private static final String ENABLE_EDIT_CATEGORY = "enableEditCategory";
    private static final String ENABLE_REPOSITORYSELECTION = "enableRepositorySelection";
    private static final String ENABLE_ADD_SUBCATEGORY = "enableAddSubcategory";
    private static final String ENABLE_EDIT_LINK = "enableLinkEdit";
    private static final String ENABLE_EDIT_CATALOG_LINK = "enableEditCatalogLink";
    private static final String ENABLE_REQUESTFORM = "enableRequestForm";

    // catalog actions

    private static final String ACTION_ADD_CTLGLINK = "addCtlglink";
    private static final String ACTION_ADD_CTLGCATEGORY = "addCtlgCategory";
    private static final String ACTION_EDIT_CTLGCATEGORY = "editCtlgCategory";
    private static final String ACTION_EDIT_CTLGCATOWNER = "editCtlgCategoryOwnerGroup";
    private static final String ACTION_DELETE_CTLGCATEGORY = "actionDeleteCtlgCategory";
    private static final String ACTION_NEW_CTGREQUEST = "actionCategoryRequest";
    private static final String ACTION_ADD_STRUCTURE = "addStructure";

    private static final String ACTION_ADD_BOOKMARK = "addBookmark";
    private static final String ACTION_MOVE_ENTRY = "moveEntry";

    // commands in table and history

    private static final String CATCMD_HISTORY = "history";
    private static final String CATCMD_REMOVE = "remove.";
    private static final String CATCMD_EDIT = "edit.";
    private static final String CATCMD_DETAIL = "detail.";
    private static final String CATCMD_MOVE = "move.";

    // URL command

    private static final String CATENTRY_CHILD = "child";
    private static final String CATENTRY_LEAF = "leaf";
    private static final String CATENTRY_NODE = "node";

    // NLS support

    private static final String NLS_DIALOG_MODAL_LEAF_DELETE_TEXT = "dialog.modal.leaf.delete.text";
    private static final String NLS_CHOOSE = "choose";
    private static final String NLS_DIALOG_MODAL_SUBTREE_DELETE_TEXT = "dialog.modal.subtree.delete.text";
    private static final String NLS_CONTACT_TO_GROUPNAME_CARETAKER = "contact.to.groupname.caretaker";
    private static final String NLS_TOOLS_EDIT_CATALOG_CATEGORY = "tools.edit.catalog.category";
    private static final String NLS_TOOLS_EDIT_CATALOG_CATEGORY_OWNERGROUP = "tools.edit.catalog.category.ownergroup";
    private static final String NLS_TOOLS_NEW_CATALOG_CATEGORYREQUEST = "tools.new.catalog.categoryrequest";
    private static final String NLS_TOOLS_DELETE_CATALOG_ENTRY = "tools.delete.catalog.entry";
    private static final String NLS_TOOLS_ADD_HEADER = "tools.add.header";
    private static final String NLS_TOOLS_ADD_CATALOG_CATEGORY = "tools.add.catalog.category";
    private static final String NLS_TOOLS_ADD_CATALOG_LINK = "tools.add.catalog.link";
    private static final String NLS_TOOLS_PASTESTRUCTURE = "tools.pastestructure";
    private static final String NLS_TOOLS_ADD_BOOKMARK = "tools.add.catalog.bookmark";
    private static final String NLS_TOOLS_MOVE_CATALOG_ENTRY = "tools.move.catalog.entry";

    // private stuff

    private final VelocityContainer myContent;

    private CatalogService catalogService;
    private CatalogEntry currentCatalogEntry;
    private CatalogEntry newLinkNotPersistedYet;
    private int currentCatalogEntryLevel = -1;
    private List<CatalogEntry> historyStack = new ArrayList<CatalogEntry>(5);
    private List childCe;
    private final boolean isOLATAdmin;
    private final boolean isAuthor;
    private boolean isLocalTreeAdmin = false;
    private int isLocalTreeAdminLevel = -1;
    private boolean canAddLinks;
    private boolean canAdministrateCategory;
    private boolean canAddSubCategories;
    private boolean canRemoveAllLinks;
    private ToolController catalogToolC;
    private RepositorySearchController rsc;
    private EntryForm addEntryForm;
    private EntryForm editEntryForm;
    private GroupController groupController;
    private DialogBoxController dialogDeleteSubtree;
    private CatalogEntry linkMarkedToBeDeleted;
    private CatalogEntry linkMarkedToBeEdited;
    private DialogBoxController dialogDeleteLink;
    private ContactFormController cfc;
    private EntryForm addStructureForm;
    private final boolean isGuest;
    private final Link loginLink;
    private CloseableModalController cmc;
    private AddAndEditBookmarkController bookmarkController;
    private boolean canBookmark = true;
    private Controller catEntryMoveController;
    private RepositoryEditDescriptionController repositoryEditDescriptionController;
    private final VelocityContainer mailVC;
    private final Link backFromMail;
    private final Panel panel;

    // locking stuff for cataloge edit operations
    private LockResult catModificationLock;
    public static final String LOCK_TOKEN = "catalogeditlock";

    // key also needed by BookmarksPortletRunController to identify type of bookmark
    private static final String TOOL_BOOKMARK = "tool_bookmark";

    /**
     * Init with catalog root
     * 
     * @param ureq
     * @param wControl
     * @param rootce
     */
    public CatalogController(final UserRequest ureq, final WindowControl wControl, final String jumpToNode) {
        // fallback translator to repository package to reduce redundant translations
        super(ureq, wControl, PackageUtil.createPackageTranslator(RepositoryEditDescriptionController.class, ureq.getLocale()));

        this.catalogService = CoreSpringFactory.getBean(CatalogService.class);

        final List<CatalogEntry> rootNodes = catalogService.getRootCatalogEntries();
        CatalogEntry rootce;
        if (rootNodes.isEmpty()) {
            throw new AssertException("No RootNodes found for Catalog! failed module init? corrupt DB?");
        }
        rootce = (CatalogEntry) catalogService.getRootCatalogEntries().get(0);

        // Check AccessRights
        isAuthor = ureq.getUserSession().getRoles().isAuthor();
        isOLATAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        isGuest = ureq.getUserSession().getRoles().isGuestOnly();
        // and also if user is localTreeAdmin
        updateToolAccessRights(ureq, rootce, 0);
        canAddLinks = isOLATAdmin || isAuthor;
        canAdministrateCategory = isOLATAdmin;
        canAddSubCategories = isOLATAdmin || isLocalTreeAdmin;
        canRemoveAllLinks = isOLATAdmin || isLocalTreeAdmin;

        myContent = createVelocityContainer("catalog");

        if (isOLATAdmin) {
            myContent.contextPut("RepoAccessVal", new Integer(RepositoryEntry.ACC_OWNERS));
        } else if (isAuthor) {
            myContent.contextPut("RepoAccessVal", new Integer(RepositoryEntry.ACC_OWNERS_AUTHORS));
        } else if (isGuest) {
            myContent.contextPut("RepoAccessVal", new Integer(RepositoryEntry.ACC_USERS_GUESTS));
        } else {
            // a daily user
            myContent.contextPut("RepoAccessVal", new Integer(RepositoryEntry.ACC_USERS));
        }

        myContent.contextPut(CATENTRY_LEAF, new Integer(CatalogEntry.TYPE_LEAF));
        myContent.contextPut(CATENTRY_NODE, new Integer(CatalogEntry.TYPE_NODE));
        // access rights for use in the Velocity Container
        myContent.contextPut("canAddLinks", new Boolean(canAddLinks));
        myContent.contextPut("canRemoveAllLinks", new Boolean(canRemoveAllLinks));
        myContent.contextPut("isGuest", new Boolean(isGuest));
        // add icon renderer
        myContent.contextPut("iconRenderer", new RepositoryEntryIconRenderer(getLocale()));
        // add this root node as history start
        historyStack.add(rootce);
        updateContent(ureq.getIdentity(), rootce, 0);

        // jump to a specific node in the catalog structure, build corresponding
        // historystack and update tool access
        if (jumpToNode != null) {
            activate(ureq, jumpToNode);
        }
        loginLink = LinkFactory.createLink("cat.login", myContent, this);

        mailVC = createVelocityContainer("contactForm");
        backFromMail = LinkFactory.createLinkBack(mailVC, this);

        panel = putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == myContent) { // links from vc -> here a link used in a
            // form action
            final String command = event.getCommand();
            // FIXME:fj:c in .hbm.xml file: optimize loading (secgroup
            // outer-join=false)
            //
            // events:
            // - subcategory selection fires a 'child' event
            // - 'navigation path' history
            // - link selectionfires a leaf event
            //
            if (command.startsWith(CATENTRY_CHILD)) { // child clicked
                final int pos = Integer.parseInt(command.substring(CATENTRY_CHILD.length()));
                final CatalogEntry cur = (CatalogEntry) childCe.get(pos);
                // put new as trail on stack
                historyStack.add(cur);
                updateToolAccessRights(ureq, cur, historyStack.indexOf(cur));
                updateContent(ureq.getIdentity(), cur, historyStack.indexOf(cur));
                fireEvent(ureq, Event.CHANGED_EVENT);

            } else if (command.startsWith(CATCMD_HISTORY)) { // history clicked
                final int pos = Integer.parseInt(command.substring(CATCMD_HISTORY.length()));
                final CatalogEntry cur = historyStack.get(pos);
                historyStack = historyStack.subList(0, pos + 1);
                updateToolAccessRights(ureq, cur, historyStack.indexOf(cur));
                updateContent(ureq.getIdentity(), cur, historyStack.indexOf(cur));
                fireEvent(ureq, Event.CHANGED_EVENT);

            } else if (command.startsWith(CATENTRY_LEAF)) { // link clicked
                final int pos = Integer.parseInt(command.substring(CATENTRY_LEAF.length()));
                final CatalogEntry cur = (CatalogEntry) childCe.get(pos);
                final RepositoryEntry repoEntry = cur.getRepositoryEntry();
                if (repoEntry == null) {
                    throw new AssertException("a leaf did not have a repositoryentry! catalogEntry = key:" + cur.getKey() + ", title " + cur.getName());
                }
                // launch entry if launchable, otherwise offer it as download / launch
                // it as non-html in browser
                final String displayName = cur.getName();
                final RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repoEntry);
                final OLATResource ores = repoEntry.getOlatResource();
                if (ores == null) {
                    throw new AssertException("repoEntry had no olatresource, repoKey = " + repoEntry.getKey());
                }
                if (repoEntry.getCanLaunch()) {
                    // we can create a controller and launch
                    // it in OLAT, e.g. if it is a
                    // content-packacking or a course

                    // was brasato:: DTabs dts = getWindowControl().getDTabs();
                    final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
                    final Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dts.getWindowControl(), true);
                    DynamicTabHelper.openRepoEntryTab(repoEntry, ureq, launchController, displayName, null);
                } else if (repoEntry.getCanDownload()) {
                    // else not launchable in olat, but downloadable -> send the document
                    // directly to browser but "downloadable" (pdf, word, excel)
                    final MediaResource mr = handler.getAsMediaResource(ores);
                    RepositoryServiceImpl.getInstance().incrementDownloadCounter(repoEntry);
                    ureq.getDispatchResult().setResultingMediaResource(mr);
                    return;
                } else { // neither launchable nor downloadable -> show details
                    // REVIEW:pb:replace EntryChangedEvent with a more specific event
                    fireEvent(ureq, new EntryChangedEvent(repoEntry, EntryChangedEvent.MODIFIED));
                    return;
                }

            } else if (command.startsWith(CATCMD_MOVE)) {
                final String s = command.substring(CATCMD_MOVE.length());
                if (s.startsWith(CATENTRY_LEAF)) {
                    // move a resource in the catalog - moving of catalog leves is triggered by a toolbox action
                    final int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
                    linkMarkedToBeEdited = (CatalogEntry) childCe.get(pos);
                    removeAsListenerAndDispose(catEntryMoveController);
                    final boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
                    if (ajax) {
                        // fancy ajax tree
                        catEntryMoveController = new CatalogAjaxMoveController(ureq, getWindowControl(), linkMarkedToBeEdited);
                    } else {
                        // old-school selection tree
                        catEntryMoveController = new CatalogEntryMoveController(getWindowControl(), ureq, linkMarkedToBeEdited, getTranslator());
                    }
                    listenTo(catEntryMoveController);
                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), "close", catEntryMoveController.getInitialComponent());
                    listenTo(cmc);
                    cmc.activate();
                }
            } else if (command.startsWith(CATCMD_REMOVE)) {
                final String s = command.substring(CATCMD_REMOVE.length());
                if (s.startsWith(CATENTRY_LEAF)) {
                    final int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
                    linkMarkedToBeDeleted = (CatalogEntry) childCe.get(pos);
                    // create modal dialog
                    final String[] trnslP = { StringHelper.escapeHtml(linkMarkedToBeDeleted.getName()) };
                    dialogDeleteLink = activateYesNoDialog(ureq, null, getTranslator().translate(NLS_DIALOG_MODAL_LEAF_DELETE_TEXT, trnslP), dialogDeleteLink);
                    return;
                }
            } else if (command.startsWith(CATCMD_EDIT)) {
                final String s = command.substring(CATCMD_EDIT.length());
                if (s.startsWith(CATENTRY_LEAF)) {
                    final int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
                    linkMarkedToBeEdited = (CatalogEntry) childCe.get(pos);
                    repositoryEditDescriptionController = new RepositoryEditDescriptionController(ureq, getWindowControl(), linkMarkedToBeEdited.getRepositoryEntry(),
                            false);
                    repositoryEditDescriptionController.addControllerListener(this);
                    // open form in dialog
                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), "close", repositoryEditDescriptionController.getInitialComponent(), true,
                            translate("tools.edit.catalog.category"));
                    listenTo(cmc);
                    cmc.activate();
                }
            } else if (command.startsWith(CATCMD_DETAIL)) {
                final String s = command.substring(CATCMD_DETAIL.length());
                if (s.startsWith(CATENTRY_LEAF)) {
                    final int pos = Integer.parseInt(s.substring(CATENTRY_LEAF.length()));
                    final CatalogEntry showDetailForLink = (CatalogEntry) childCe.get(pos);
                    final RepositoryEntry repoEnt = showDetailForLink.getRepositoryEntry();
                    fireEvent(ureq, new EntryChangedEvent(repoEnt, EntryChangedEvent.MODIFIED));
                    // TODO [ingkr]
                    // getWindowControl().getDTabs().activateStatic(ureq, RepositorySite.class.getName(),
                    // RepositoryMainController.JUMPFROMEXTERN+RepositoryMainController.JUMPFROMCATALOG+repoEnt.getKey().toString());
                    return;
                }
            }
        }
        /*
         * login link clicked
         */
        else if (source == loginLink) {
            DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
        } else if (source == backFromMail) {
            panel.setContent(myContent);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == catalogToolC) {
            if (event.getCommand().equals(ACTION_ADD_CTLGCATEGORY)) {
                // add new subcategory to the currentCategory
                catModificationLock = getLockingService().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
                if (!catModificationLock.isSuccess()) {
                    showError("catalog.locked.by", catModificationLock.getOwner().getName());
                    return;
                }
                removeAsListenerAndDispose(addEntryForm);
                addEntryForm = new EntryForm(ureq, getWindowControl(), false);
                listenTo(addEntryForm);

                // open form in dialog
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", addEntryForm.getInitialComponent(), true, translate("tools.add.catalog.category"));
                listenTo(cmc);
                cmc.activate();
            } else if (event.getCommand().equals(ACTION_ADD_CTLGLINK)) {
                // add a link to the currentCategory
                removeAsListenerAndDispose(rsc);
                rsc = new RepositorySearchController(translate(NLS_CHOOSE), ureq, getWindowControl(), true, false);
                listenTo(rsc);
                // OLAT-Admin has search form
                if (isOLATAdmin) {
                    rsc.displaySearchForm();
                }
                // an Author gets the list of his repository
                else {
                    // admin is responsible for not inserting wrong visibility entries!!
                    rsc.doSearchByOwnerLimitAccess(ureq.getIdentity(), RepositoryEntry.ACC_USERS);
                }
                // open form in dialog
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", rsc.getInitialComponent(), true, translate("tools.add.catalog.link"));
                listenTo(cmc);
                cmc.activate();
            } else if (event.getCommand().equals(ACTION_EDIT_CTLGCATEGORY)) {
                // edit the currentCategory
                catModificationLock = getLockingService().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
                if (!catModificationLock.isSuccess()) {
                    showError("catalog.locked.by", catModificationLock.getOwner().getName());
                    return;
                }
                removeAsListenerAndDispose(editEntryForm);
                editEntryForm = new EntryForm(ureq, getWindowControl(), false);
                listenTo(editEntryForm);

                editEntryForm.setFormFields(currentCatalogEntry);// fill the

                // open form in dialog
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", editEntryForm.getInitialComponent(), true, translate("tools.edit.catalog.category"));
                listenTo(cmc);

                cmc.activate();
            } else if (event.getCommand().equals(ACTION_EDIT_CTLGCATOWNER)) {
                // edit current category's ownergroup
                // add ownership management
                SecurityGroup secGroup = currentCatalogEntry.getOwnerGroup();
                if (secGroup == null) {
                    CatalogEntry reloadedCatalogEntry = catalogService.setEmptyOwnerGroup(currentCatalogEntry);
                    currentCatalogEntry = reloadedCatalogEntry;
                    secGroup = currentCatalogEntry.getOwnerGroup();
                }
                final boolean keepAtLeastOne = currentCatalogEntryLevel == 0;

                removeAsListenerAndDispose(groupController);
                groupController = new GroupController(ureq, getWindowControl(), true, keepAtLeastOne, false, secGroup, null);
                listenTo(groupController);

                // open form in dialog
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", groupController.getInitialComponent(), true,
                        translate("tools.edit.catalog.category.ownergroup"));
                listenTo(cmc);

                cmc.activate();
            } else if (event.getCommand().equals(ACTION_DELETE_CTLGCATEGORY)) {
                // delete category (subtree)
                catModificationLock = getLockingService().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
                if (!catModificationLock.isSuccess()) {
                    showError("catalog.locked.by", catModificationLock.getOwner().getName());
                    return;
                }
                final String[] trnslP = { StringHelper.escapeHtml(currentCatalogEntry.getName()) };
                dialogDeleteSubtree = activateYesNoDialog(ureq, null, getTranslator().translate(NLS_DIALOG_MODAL_SUBTREE_DELETE_TEXT, trnslP), dialogDeleteSubtree);
                return;
            } else if (event.getCommand().equals(ACTION_NEW_CTGREQUEST)) {
                // contact caretaker, request subcategory, request deletion of an entry, etc.
                // find the first caretaker, looking from the leaf towards the root, following the selected branch.
                final ContactMessage cmsg = createContactMessage(ureq);

                removeAsListenerAndDispose(cfc);
                cfc = new ContactFormController(ureq, getWindowControl(), false, true, false, false, cmsg);
                listenTo(cfc);

                mailVC.put("mailform", cfc.getInitialComponent());
                panel.setContent(mailVC);

            } else if (event.getCommand().equals(ACTION_ADD_STRUCTURE)) {
                // add a structure
                removeAsListenerAndDispose(addStructureForm);
                addStructureForm = new EntryForm(ureq, getWindowControl(), false);
                listenTo(addStructureForm);

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", addStructureForm.getInitialComponent(), true, translate("contact.caretaker"));
                listenTo(cmc);

                cmc.activate();
            } else if (event.getCommand().equals(ACTION_ADD_BOOKMARK)) {
                // add bookmark
                removeAsListenerAndDispose(bookmarkController);
                final OLATResourceable ores = catalogService.createOLATResouceableFor(currentCatalogEntry);
                bookmarkController = new AddAndEditBookmarkController(ureq, getWindowControl(), currentCatalogEntry.getName(), "", ores, CatalogService.CATALOGENTRY);
                listenTo(bookmarkController);
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", bookmarkController.getInitialComponent());
                listenTo(cmc);
                cmc.activate();
            } else if (event.getCommand().equals(ACTION_MOVE_ENTRY)) {
                // move catalogentry
                // Move catalog level - moving of resources in the catalog (leafs) is triggered by a velocity command
                // so, reset stale link to the current resource first (OLAT-4253), the linkMarkedToBeEdited will be reset
                // when an edit or move operation on the resource is done
                linkMarkedToBeEdited = null;
                //
                catModificationLock = getLockingService().acquireLock(OresHelper.createOLATResourceableType(CatalogController.class), ureq.getIdentity(), LOCK_TOKEN);
                if (!catModificationLock.isSuccess()) {
                    showError("catalog.locked.by", catModificationLock.getOwner().getName());
                    return;
                }
                // check if user surfs in ajax mode
                removeAsListenerAndDispose(catEntryMoveController);
                final boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
                if (ajax) {
                    // fancy ajax tree
                    catEntryMoveController = new CatalogAjaxMoveController(ureq, getWindowControl(), currentCatalogEntry);
                } else {
                    // old-school selection tree
                    catEntryMoveController = new CatalogEntryMoveController(getWindowControl(), ureq, currentCatalogEntry, getTranslator());
                }
                listenTo(catEntryMoveController);
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), "close", catEntryMoveController.getInitialComponent());
                listenTo(cmc);
                cmc.activate();
            } else if (source == addStructureForm) {
                // remove modal dialog first
                removeModalDialog();
                if (event == Event.DONE_EVENT) {
                    CatalogEntry dummy = catalogService.importStructure();
                    addStructureForm.fillEntry(dummy);
                }
                final CatalogEntry newRoot = (CatalogEntry) catalogService.getRootCatalogEntries().get(0);
                historyStack = new ArrayList<CatalogEntry>();
                historyStack.add(newRoot);
                updateContent(ureq.getIdentity(), newRoot, 0);
                updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == rsc) {
            // from the repository search, a entry was selected to add
            removeModalDialog();
            if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
                /*
                 * succesfully selected a repository entry which will be a link within the current Category
                 */
                final RepositoryEntry re = rsc.getSelectedEntry();
                newLinkNotPersistedYet = catalogService.createCatalogEntryLeafAndAddParent(re, currentCatalogEntry);
                newLinkNotPersistedYet = null;
                updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
                updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else if (event == Event.CANCELLED_EVENT) {
                updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
                updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
                fireEvent(ureq, Event.CHANGED_EVENT);

            }
        } else if (source == dialogDeleteSubtree) {
            // from remove subtree dialog -> yes or no
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // remember the parent of the subtree being deleted
                final CatalogEntry parent = currentCatalogEntry.getParent();
                // delete the subtree!!!
                catalogService.deleteCatalogEntry(currentCatalogEntry);
                // display the parent
                historyStack.remove(historyStack.size() - 1);
                updateContent(ureq.getIdentity(), parent, historyStack.indexOf(parent));
                updateToolAccessRights(ureq, parent, historyStack.indexOf(parent));
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
            removeModificationLock();

        } else if (source == dialogDeleteLink) {
            // from remove link dialog -> yes or no
            if (DialogBoxUIFactory.isYesEvent(event)) {
                catalogService.deleteCatalogEntry(linkMarkedToBeDeleted);
                updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
            }
            removeModificationLock();
        } else if (source == cfc) {
            // from contactform controller, aka sending e-mail to caretaker
            panel.setContent(myContent);
            if (event.equals(Event.DONE_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
                updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
            }
        } else if (source == groupController) {
            removeModalDialog();
            if (event instanceof IdentitiesAddEvent) { // FIXME:chg: Move into seperate RepositoryOwnerGroupController like BusinessGroupEditController ?
                final IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
                catalogService.addOwners(currentCatalogEntry, identitiesAddedEvent.getAddIdentities());
            } else if (event instanceof IdentitiesRemoveEvent) {
                final IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
                catalogService.removeOwners(currentCatalogEntry, identitiesRemoveEvent.getRemovedIdentities());
            }
        } else if (source == bookmarkController) {
            removeModalDialog();
            if (event.equals(Event.DONE_EVENT)) {
                // Add bookmark workflow did successfully save the bookmark, nothing to
                // do here
                // User did set a bookmark - bookmarking no longer enabled and disable
                // it in toolbox
                canBookmark = false;
                catalogToolC.setEnabled(TOOL_BOOKMARK, canBookmark);
            }
        } else if (source == catEntryMoveController) {
            removeModalDialog();
            if (event.equals(Event.DONE_EVENT)) {
                // linkMarkedToBeEdited is the catalog entry - "leaf" - which is moved
                showInfo("tools.move.catalog.entry.success", (linkMarkedToBeEdited == null ? currentCatalogEntry.getName() : linkMarkedToBeEdited.getName()));
                // currentCatalogEntry is the current active "Folder" - reload model to reflect change.
                reloadHistoryStack(ureq, currentCatalogEntry.getKey());
            } else if (event.equals(Event.FAILED_EVENT)) {
                showError("tools.move.catalog.entry.failed");
                updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
            }
            removeModificationLock();
        } else if (source == cmc) {
            removeModificationLock();
        } else if (source == repositoryEditDescriptionController) {
            if (event == Event.CHANGED_EVENT) {
                linkMarkedToBeEdited.setRepositoryEntry(repositoryEditDescriptionController.getRepositoryEntry());
                updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
                RepositoryEntry reloadedRepositoryEntry = RepositoryServiceImpl.getInstance().updateDisplaynameDescriptionOfRepositoryEntry(
                        repositoryEditDescriptionController.getRepositoryEntry());
                // inform anybody interested about this change
                final MultiUserEvent modifiedEvent = new EntryChangedEvent(reloadedRepositoryEntry, EntryChangedEvent.MODIFIED_DESCRIPTION);
                CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, reloadedRepositoryEntry);
            } else if (event == Event.CANCELLED_EVENT) {
                removeModalDialog();
            }
        } else if (source == addEntryForm) {
            removeModalDialog();

            if (event == Event.DONE_EVENT) {
                final CatalogEntry ce = catalogService.createCatalogEntryWithoutRepositoryEntry(currentCatalogEntry);
                addEntryForm.fillEntry(ce);
                catalogService.saveCatalogEntry(ce);
            } else if (event == Event.CANCELLED_EVENT) {
                // nothing to do
            }
            final CatalogEntry reloaded = catalogService.loadCatalogEntry(currentCatalogEntry);
            currentCatalogEntry = reloaded;// FIXME:pb:
            updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
            updateToolAccessRights(ureq, currentCatalogEntry, currentCatalogEntryLevel);
            removeModificationLock();
            fireEvent(ureq, Event.CHANGED_EVENT);

        } else if (source == editEntryForm) {
            removeModalDialog();
            // optimistic save: might fail in case the current entry has been deleted
            // in the meantime by someone else
            CatalogEntry reloaded = catalogService.loadCatalogEntry(currentCatalogEntry);
            currentCatalogEntry = reloaded;// FIXME:pb
            if (event == Event.DONE_EVENT) {
                editEntryForm.fillEntry(currentCatalogEntry);
                catalogService.updateCatalogEntry(currentCatalogEntry);
                // update the changed name in the history path
                historyStack.remove(historyStack.size() - 1);
                historyStack.add(currentCatalogEntry);
            } else if (event == Event.CANCELLED_EVENT) {
                // nothing to do
            }
            removeModificationLock();
            updateContent(ureq.getIdentity(), currentCatalogEntry, currentCatalogEntryLevel);
        }
    }

    /**
     * Creates a ContactList for the catalog entry caretaker and a ContactMessage.
     */
    private ContactMessage createContactMessage(final UserRequest ureq) {
        String contactListName = translate(NLS_CONTACT_TO_GROUPNAME_CARETAKER); // this is used as EmailTo string
        CatalogEntry entry = historyStack.get(historyStack.size() - 1);
        if (entry != null) {
            contactListName += " " + entry.getName();
        }
        final ContactList caretaker = new ContactList(contactListName);
        caretaker.addAllIdentites(catalogService.getCaretakerFormCatalogEntryList(historyStack));

        // create e-mail Message
        final ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
        cmsg.addEmailTo(caretaker);
        return cmsg;
    }

    /**
	 * 
	 */
    private void removeModificationLock() {
        if (catModificationLock != null && catModificationLock.isSuccess()) {
            getLockingService().releaseLock(catModificationLock);
            catModificationLock = null;
        }
    }

    /**
	 * 
	 */
    private void removeModalDialog() {
        cmc.deactivate();
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * before calling this method make sure the person has the right to add categories. The method checks further if the person also can add links as author.
     * 
     * @return configured tool controller
     */
    public ToolController createCatalogToolController() {
        removeAsListenerAndDispose(catalogToolC);
        catalogToolC = ToolFactory.createToolController(getWindowControl());
        listenTo(catalogToolC);
        // if (isOLATAdmin || isLocalTreeAdmin || isAuthor ) {
        // at least a person being able to do something...
        if (!isGuest) {
            // included normal user now for bookmark functionality
            /*
             * edit tools
             */
            catalogToolC.addHeader(getTranslator().translate("tools.edit.header"));

            catalogToolC.addLink(ACTION_ADD_BOOKMARK, translate(NLS_TOOLS_ADD_BOOKMARK), TOOL_BOOKMARK, null); // new bookmark link
            catalogToolC.setEnabled(TOOL_BOOKMARK, canBookmark);

            if (canAdministrateCategory || canAddLinks) {
                if (canAdministrateCategory) {
                    catalogToolC.addLink(ACTION_EDIT_CTLGCATEGORY, translate(NLS_TOOLS_EDIT_CATALOG_CATEGORY));
                }
                if (canAdministrateCategory) {
                    catalogToolC.addLink(ACTION_EDIT_CTLGCATOWNER, translate(NLS_TOOLS_EDIT_CATALOG_CATEGORY_OWNERGROUP));
                }
                if (canAddLinks) {
                    catalogToolC.addLink(ACTION_NEW_CTGREQUEST, translate(NLS_TOOLS_NEW_CATALOG_CATEGORYREQUEST));
                }

                if (canAdministrateCategory && currentCatalogEntryLevel > 0) {
                    // delete root? very dangerous, disabled!
                    catalogToolC.addLink(ACTION_DELETE_CTLGCATEGORY, translate(NLS_TOOLS_DELETE_CATALOG_ENTRY));
                }
                if (canAdministrateCategory && currentCatalogEntryLevel > 0) {
                    catalogToolC.addLink(ACTION_MOVE_ENTRY, translate(NLS_TOOLS_MOVE_CATALOG_ENTRY));
                }
            }

            /*
             * add tools
             */
            if (isOLATAdmin || isLocalTreeAdmin || isAuthor) {
                catalogToolC.addHeader(translate(NLS_TOOLS_ADD_HEADER));
                if (canAddSubCategories) {
                    catalogToolC.addLink(ACTION_ADD_CTLGCATEGORY, translate(NLS_TOOLS_ADD_CATALOG_CATEGORY));
                }
                if (canAddLinks) {
                    catalogToolC.addLink(ACTION_ADD_CTLGLINK, translate(NLS_TOOLS_ADD_CATALOG_LINK));
                }
                if (currentCatalogEntryLevel == 0 && isOLATAdmin && catalogService.getChildrenOf(currentCatalogEntry).isEmpty()) {
                    catalogToolC.addLink(ACTION_ADD_STRUCTURE, translate(NLS_TOOLS_PASTESTRUCTURE));
                }
            }
        }
        return catalogToolC;
    }

    /**
	 */
    protected void doDispose() {
        // remove any locks
        if (catModificationLock != null && catModificationLock.isSuccess()) {
            getLockingService().releaseLock(catModificationLock);
            catModificationLock = null;
        }
        // controllers autodisposed by basic controller
    }

    /**
     * refresh content of current category for displaying
     * 
     * @param ce
     * @param ceLevel
     */
    private void updateContent(final Identity identity, final CatalogEntry ce, final int ceLevel) {
        /*
         * FIXME:pb:c include lookahead feature, displaying the 1st 3 children if any, to give a google directory feeling
         */
        currentCatalogEntry = ce;
        currentCatalogEntryLevel = ceLevel;
        myContent.contextPut("canAddLinks", new Boolean(canAddLinks));
        myContent.contextPut("canRemoveAllLinks", new Boolean(canRemoveAllLinks));
        myContent.contextPut("currentCatalogEntry", currentCatalogEntry);
        childCe = catalogService.getChildrenOf(ce);
        myContent.contextPut("children", childCe);
        for (final Object leaf : childCe) {
            final CatalogEntry entry = (CatalogEntry) leaf;
            if (entry.getType() == CatalogEntry.TYPE_NODE) {
                continue;
            }
            final String name = "image" + childCe.indexOf(leaf);
            /* STATIC_METHOD_REFACTORING */
            final ImageComponent ic = RepositoryServiceImpl.getInstance().getImageComponentForRepositoryEntry(name, entry.getRepositoryEntry());
            if (ic == null) {
                myContent.remove(myContent.getComponent(name));
                continue;
            }
            ic.setMaxWithAndHeightToFitWithin(200, 100);
            myContent.put(name, ic);
        }
        myContent.contextPut(CATCMD_HISTORY, historyStack);

        final String url = Settings.getServerContextPathURI() + "/url/CatalogEntry/" + ce.getKey();
        myContent.contextPut("guestExtLink", url + "?guest=true&amp;lang=" + getLocale().getLanguage());
        if (!isGuest) {
            myContent.contextPut("extLink", url);
        }
        // check which of the entries are owned entries. users who can add links
        // can also remove links. users who can remove all links do not need to be
        // checked
        if (canAddLinks && !canRemoveAllLinks) {
            final List ownedLinks = catalogService.filterOwnedLeafs(identity, childCe);
            if (ownedLinks.size() > 0) {
                myContent.contextPut("hasOwnedLinks", Boolean.TRUE);
                myContent.contextPut("ownedLinks", ownedLinks);
            } else {
                myContent.contextPut("hasOwnedLinks", Boolean.FALSE);
            }

        } else {
            myContent.contextPut("hasOwnedLinks", Boolean.FALSE);
        }
    }

    /**
     * Internal helper to calculate the users rights within the controller. The method will fire change events if necessary to signal the parent controller that he need
     * to rebuild the tool controller
     * 
     * @param ureq
     * @param ce
     *            The current catalog category element from the given level
     * @param pos
     *            The current level in the catalog
     */
    private void updateToolAccessRights(final UserRequest ureq, final CatalogEntry ce, final int pos) {
        // 1) check if user has already a bookmark for this level
        final CatalogEntry tmp = ce;
        final OLATResourceable catEntryOres = catalogService.createOLATResouceableFor(ce);
        if (tmp != null && getBookmarkService().isResourceableBookmarked(ureq.getIdentity(), catEntryOres)) {
            canBookmark = false;
            if (catalogToolC != null) {
                catalogToolC.setEnabled(TOOL_BOOKMARK, canBookmark);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else {
            canBookmark = true;
        }
        // 2) check if insert structure must be removed or showed
        if (isOLATAdmin && currentCatalogEntryLevel == 0) {
            fireEvent(ureq, Event.CHANGED_EVENT);
        }
        if (isOLATAdmin) {
            return;
        }

        // 3) check other cases that might change default values from constructor
        if (isLocalTreeAdminLevel > pos) {
            // 3a) below branch that user has admin rights - revoke all rights
            isLocalTreeAdminLevel = -1;
            isLocalTreeAdmin = false;
            canAddLinks = isOLATAdmin || isAuthor;
            canAdministrateCategory = isOLATAdmin;
            canAddSubCategories = isOLATAdmin;
            canRemoveAllLinks = isOLATAdmin;
            fireEvent(ureq, Event.CHANGED_EVENT);

        } else if (isLocalTreeAdminLevel == -1) {
            if (catalogService.isOwner(ce, ureq.getIdentity())) {
                isLocalTreeAdminLevel = pos;
                isLocalTreeAdmin = true;
                canAddLinks = isOLATAdmin || isAuthor;
                canAdministrateCategory = true;
                canAddSubCategories = true;
                canRemoveAllLinks = true;
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    private BookmarkService getBookmarkService() {
        return (BookmarkService) CoreSpringFactory.getBean(BookmarkService.class);
    }

    /**
	 */
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        // transforms the parameter jumpToNode into a long value and calls jumpToNode(UserRequest, long)
        try {
            final long parsed = Long.parseLong(viewIdentifier);
            reloadHistoryStack(ureq, parsed);
        } catch (final Exception e) {
            log.warn("Could not activate catalog entry with ID::" + viewIdentifier, null);
        }
    }

    /**
     * Internal helper: Get's the requested catalog node and set it as active content, builds also the history stack from the root-node to this node.
     * 
     * @return true if successful otherwise false (e.c. jumpToNode referenced a catalog leaf or no catalog entry at all)
     */
    private boolean jumpToNode(final UserRequest ureq, final long jumpToNode) {
        CatalogEntry cE = catalogService.loadCatalogEntry(Long.valueOf(jumpToNode));
        if (cE != null) {
            final Stack<CatalogEntry> stack = new Stack<CatalogEntry>();
            // get elements, and add to filo stack
            while (cE != null) {
                stack.push(cE);
                cE = cE.getParent();
            }
            // read filo stack
            while (!stack.isEmpty()) {
                cE = stack.pop();
                historyStack.add(cE);
                updateContent(ureq.getIdentity(), cE, historyStack.size() - 1);
                updateToolAccessRights(ureq, cE, historyStack.size() - 1);
            }
            return true;
        }
        return false;
    }

    /**
     * Internal helper: clear history and jumpt to the given node
     * 
     * @param ureq
     * @param jumpToNode
     */
    private void reloadHistoryStack(final UserRequest ureq, final long jumpToNode) {
        historyStack.clear();
        jumpToNode(ureq, jumpToNode);
    }

}
