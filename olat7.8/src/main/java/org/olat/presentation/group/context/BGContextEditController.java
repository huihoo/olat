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

package org.olat.presentation.group.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.CourseModule;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.learn.DefaultContextTranslationHelper;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.presentation.group.securitygroup.IdentitiesRemoveEvent;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.presentation.repository.RepositoryTableModel;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Controller to edit a business group context. The editor proviedes a tabbed pane with the following tabs: - details / metadata - owner management (who can edit this
 * group context) - resource management (where to use this group context) This controller however does no functionality to create groups, learning areas etc. See
 * BGManagementController for this functionality
 * <P>
 * Initial Date: Jan 31, 2005
 * 
 * @author gnaegi
 */
public class BGContextEditController extends BasicController implements ControllerEventListener, GenericEventListener {
    private GroupController ownerCtr;
    private TabbedPane tabbedPane;
    private Panel content;
    private VelocityContainer editVC;
    private VelocityContainer tabDetailsVC;
    private VelocityContainer tabOwnersVC;
    private VelocityContainer tabResourcesVC;

    private BGContext groupContext;
    private BGContextFormController contextController;
    private TableController resourcesCtr;
    private RepositoryTableModel repoTableModel;
    private List repoTableModelEntries;
    private RepositoryEntry currentRepoEntry;
    private ReferencableEntriesSearchController repoSearchCtr;
    private CloseableModalController cmc;
    private DialogBoxController confirmRemoveResource;
    private Link addTabResourcesButton;
    private final LockResult lockEntry;
    private DialogBoxController alreadyLockedDialogController;
    private BusinessGroupService businessGroupService;

    /**
     * Constructor for a business group edit controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window control
     * @param groupContext
     *            The business group context to be edited
     */
    public BGContextEditController(final UserRequest ureq, final WindowControl wControl, final BGContext groupContext) {
        super(ureq, wControl);
        // reload context to minimize stale object exception
        this.groupContext = BGContextDaoImpl.getInstance().loadBGContext(groupContext);
        this.businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);

        // try to acquire edit lock on business group context
        final String lockSubKey = "contextEdit";
        this.lockEntry = getLockingService().acquireLock(groupContext, ureq.getIdentity(), lockSubKey);
        if (this.lockEntry.isSuccess()) {
            this.tabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
            this.tabbedPane.addListener(this);
            // details and metadata
            this.tabDetailsVC = doCreateTabDetails(ureq);
            this.tabbedPane.addTab(translate("edit.tab.details"), this.tabDetailsVC);
            // owner group management
            this.tabOwnersVC = doCreateTabOwners(ureq);
            this.tabbedPane.addTab(translate("edit.tab.owners"), this.tabOwnersVC);
            // associated resources management
            this.tabResourcesVC = doCreateTabResources(ureq, false);
            this.tabbedPane.addTab(translate("edit.tab.resources"), this.tabResourcesVC);
            // put everything in a velocity container
            this.editVC = createVelocityContainer("contextmanagement_edit");
            this.editVC.put("tabbedpane", this.tabbedPane);
            final String title = DefaultContextTranslationHelper.translateIfDefaultContextName(groupContext, getTranslator());
            this.editVC.contextPut("title", getTranslator().translate("edit.title", new String[] { "<i>" + StringEscapeUtils.escapeHtml(title) + "</i>" }));
            this.content = new Panel("contexteditpanel");
            this.content.setContent(this.editVC);
        } else {
            // lock was not successful !
            this.alreadyLockedDialogController = DialogBoxUIFactory.createResourceLockedMessage(ureq, wControl, this.lockEntry, "error.message.locked", getTranslator());
            listenTo(this.alreadyLockedDialogController);
            this.alreadyLockedDialogController.activate();
        }
        // register for changes in this group context
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), groupContext);

        putInitialPanel(this.content);
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == this.addTabResourcesButton) {
            if (this.groupContext.isDefaultContext()) {
                if (this.repoTableModelEntries.size() == 1) {
                    // display error and exit - do not remove resource
                    showError("resource.error.isDefault", null);
                    this.contextController.setValues(this.groupContext);
                    return;
                }
            }
            removeAsListenerAndDispose(repoSearchCtr);
            repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, CourseModule.getCourseTypeName(), translate("resources.add"));
            listenTo(repoSearchCtr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), this.repoSearchCtr.getInitialComponent(), true, translate("resources.add.title"));
            listenTo(cmc);

            cmc.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.repoSearchCtr) {
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // repository search controller done
                final RepositoryEntry re = this.repoSearchCtr.getSelectedEntry();
                removeAsListenerAndDispose(this.repoSearchCtr);
                this.cmc.deactivate();
                if (re != null && !this.repoTableModelEntries.contains(re)) {
                    // check if already in model
                    final boolean alreadyAssociated = PersistenceHelper.listContainsObjectByKey(this.repoTableModelEntries, re);
                    if (!alreadyAssociated) {
                        doAddRepositoryEntry(re);
                        fireEvent(ureq, Event.CHANGED_EVENT);
                        final MultiUserEvent mue = new BGContextEvent(BGContextEvent.RESOURCE_ADDED, this.groupContext);
                        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, this.groupContext);
                    }
                }
            }
        } else if (source == this.ownerCtr) {
            if (event instanceof IdentitiesAddEvent) {
                final List<Identity> addedIdentities = new ArrayList<Identity>();
                for (final Identity identity : ((IdentitiesAddEvent) event).getAddIdentities()) {
                    boolean isAdded = getBaseSecurityEBL().addIdentityToSecurityGroup(identity, this.groupContext.getOwnerGroup());
                    if (isAdded) {
                        addedIdentities.add(identity);
                    }
                }
                ((IdentitiesAddEvent) event).setIdentitiesAddedEvent(addedIdentities);
            } else if (event instanceof IdentitiesRemoveEvent) {
                for (final Identity identity : ((IdentitiesRemoveEvent) event).getRemovedIdentities()) {
                    getBaseSecurityEBL().removeIdentityFromSecurityGroup(identity, this.groupContext.getOwnerGroup());
                }
            }
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if (source == this.resourcesCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.currentRepoEntry = (RepositoryEntry) this.repoTableModel.getObject(rowid);
                if (actionid.equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
                    if (this.groupContext.isDefaultContext()) {
                        if (this.repoTableModelEntries.size() == 1) {
                            // display error and exit - do not remove resource
                            showError("resource.error.isDefault");
                            this.contextController.setValues(this.groupContext);
                            return;
                        }
                    }
                    // present dialog box if resource should be removed
                    final String text = getTranslator().translate("resource.remove",
                            new String[] { StringHelper.escapeHtml(this.groupContext.getName()), StringHelper.escapeHtml(this.currentRepoEntry.getDisplayname()) });
                    this.confirmRemoveResource = activateYesNoDialog(ureq, null, text, this.confirmRemoveResource);

                }
            }
        } else if (source == this.confirmRemoveResource) {
            if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
                doRemoveResource(this.currentRepoEntry);
                fireEvent(ureq, Event.CHANGED_EVENT);
                final MultiUserEvent mue = new BGContextEvent(BGContextEvent.RESOURCE_REMOVED, this.groupContext);
                CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, this.groupContext);
            }
        } else if (source == this.contextController) {
            if (event == Event.DONE_EVENT) {
                doUpdateContext(ureq);
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else if (event == Event.CANCELLED_EVENT) {
                // init the details form again

                removeAsListenerAndDispose(contextController);
                contextController = new BGContextFormController(ureq, getWindowControl(), this.groupContext.getGroupType(), ureq.getUserSession().getRoles()
                        .isOLATAdmin());
                listenTo(contextController);

                this.contextController.setValues(this.groupContext);
                this.tabDetailsVC.put("contextForm", this.contextController.getInitialComponent());
            }
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * persist the updates
     */
    private void doUpdateContext(final UserRequest ureq) {
        // refresh group to prevent stale object exception and context proxy issues
        final BGContextDao contextManager = BGContextDaoImpl.getInstance();
        this.groupContext = contextManager.loadBGContext(this.groupContext);
        // update defaultContext switch changes
        if (ureq.getUserSession().getRoles().isOLATAdmin()) {
            final boolean newisDefaultContext = this.contextController.isDefaultContext();
            if (newisDefaultContext) {
                if (this.repoTableModelEntries.size() == 0) {
                    showError("form.error.defaultButNoResource");
                    this.contextController.setValues(this.groupContext);
                    return;
                }
            }
            this.groupContext.setDefaultContext(newisDefaultContext);
        }
        // update name and descripton
        final String name = this.contextController.getName();
        final String desc = this.contextController.getDescription();
        this.groupContext.setName(name);
        this.groupContext.setDescription(desc);
        contextManager.updateBGContext(this.groupContext);
        // update velocity
        final String title = DefaultContextTranslationHelper.translateIfDefaultContextName(this.groupContext, getTranslator());
        this.editVC.contextPut("title", getTranslator().translate("edit.title", new String[] { "<i>" + StringEscapeUtils.escapeHtml(title) + "</i>" }));
    }

    private void doRemoveResource(final RepositoryEntry entry) {
        // remove on db
        final BGContextDao contextManager = BGContextDaoImpl.getInstance();
        contextManager.removeBGContextFromResource(this.groupContext, entry.getOlatResource());
        // remove on table model
        this.repoTableModelEntries.remove(entry);
        this.resourcesCtr.modelChanged();
    }

    private void doAddRepositoryEntry(final RepositoryEntry entry) {
        // persist on db
        businessGroupService.addBGContextToResource(this.groupContext, entry.getOlatResource());
        // update table model
        this.repoTableModelEntries.add(entry);
        this.resourcesCtr.modelChanged();
    }

    private VelocityContainer doCreateTabDetails(final UserRequest ureq) {
        this.tabDetailsVC = createVelocityContainer("tab_details");

        removeAsListenerAndDispose(this.contextController);
        this.contextController = new BGContextFormController(ureq, getWindowControl(), this.groupContext.getGroupType(), ureq.getUserSession().getRoles().isOLATAdmin());
        listenTo(this.contextController);

        this.contextController.setValues(this.groupContext);
        this.tabDetailsVC.put("contextForm", this.contextController.getInitialComponent());
        return this.tabDetailsVC;
    }

    private VelocityContainer doCreateTabOwners(final UserRequest ureq) {
        this.tabOwnersVC = createVelocityContainer("tab_owners");

        removeAsListenerAndDispose(this.ownerCtr);
        this.ownerCtr = new GroupController(ureq, getWindowControl(), true, true, false, this.groupContext.getOwnerGroup(), null);
        listenTo(this.ownerCtr);

        this.tabOwnersVC.put("owners", this.ownerCtr.getInitialComponent());
        return this.tabOwnersVC;
    }

    private VelocityContainer doCreateTabResources(final UserRequest ureq, final boolean initOnlyModel) {
        final Translator resourceTrans = PackageUtil.createPackageTranslator(RepositoryTableModel.class, getLocale(), getTranslator());
        if (!initOnlyModel) {
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setTableEmptyMessage(translate("resources.noresources"));

            removeAsListenerAndDispose(resourcesCtr);
            resourcesCtr = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans);
            listenTo(resourcesCtr);

            this.tabResourcesVC = createVelocityContainer("tab_resources");
            this.addTabResourcesButton = LinkFactory.createButtonSmall("cmd.addresource", this.tabResourcesVC, this);
        }

        this.repoTableModel = new RepositoryTableModel(resourceTrans);
        this.repoTableModelEntries = getBgContextService().findRepositoryEntriesForBGContext(this.groupContext);
        this.repoTableModel.setObjects(this.repoTableModelEntries);
        if (!initOnlyModel) {
            this.repoTableModel.addColumnDescriptors(this.resourcesCtr, translate("resources.remove"), false);
        }
        this.resourcesCtr.setTableDataModel(this.repoTableModel);

        this.tabResourcesVC.put("resources", this.resourcesCtr.getInitialComponent());
        return this.tabResourcesVC;
    }

    /**
     * @return
     */
    private BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

    /**
	 */
    @Override
    public void event(final Event event) {
        if (event instanceof BGContextEvent) {
            final BGContextEvent contextEvent = (BGContextEvent) event;
            if (contextEvent.getBgContextKey().equals(this.groupContext.getKey())) {
                if (contextEvent.getCommand().equals(BGContextEvent.CONTEXT_DELETED)) {
                    // this context is deleted, dispose this edit controller
                    dispose();
                } else if (contextEvent.getCommand().equals(BGContextEvent.RESOURCE_ADDED) || contextEvent.getCommand().equals(BGContextEvent.RESOURCE_REMOVED)) {
                    // update resource table model
                    this.tabResourcesVC = doCreateTabResources(null, true);
                }
            }
        }
    }

    /**
     * @return true if lock on group has been acquired, flase otherwhise
     */
    public boolean isLockAcquired() {
        return this.lockEntry.isSuccess();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // deregister for changes in this group context
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.groupContext);
        // release Lock
        releaseGroupContextLock();
    }

    private void releaseGroupContextLock() {
        if (this.lockEntry.isSuccess()) {
            // release lock
            getLockingService().releaseLock(this.lockEntry);
        } else if (this.alreadyLockedDialogController != null) {
            // dispose lock dialog if still visible.
            this.alreadyLockedDialogController.dispose();
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
