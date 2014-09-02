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

package org.olat.presentation.group.edit;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.lms.activitylogging.ActionType;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.change.ChangeManager;
import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupAddResponse;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupPropertyManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.BusinessGroupTransferObject;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.group.right.BGRightManager;
import org.olat.lms.group.right.BGRightManagerImpl;
import org.olat.lms.group.right.BGRights;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.collaboration.CollaborationToolsSettingsController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.choice.Choice;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.presentation.group.BusinessGroupFormController;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.presentation.group.securitygroup.IdentitiesMoveEvent;
import org.olat.presentation.group.securitygroup.IdentitiesRemoveEvent;
import org.olat.presentation.group.securitygroup.WaitingGroupController;
import org.olat.presentation.group.securitygroup.confirmation.AbstractGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.AbstractGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.AbstractWaitingListGroupConfirmationSender;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <BR>
 * This controller displays a tabbed pane that lets the user configure and modify a business group. The functionality must be configured using the BGConfigFlags.
 * <P>
 * Fires BusinessGroupModifiedEvent via the OLATResourceableEventCenter
 * <P>
 * Initial Date: Aug 17, 2004
 * 
 * @author patrick
 */

public class BusinessGroupEditController extends BasicController implements ControllerEventListener, GenericEventListener {
    // needed for complicated fall back translator chaining
    private static final String PACKAGE = PackageUtil.getPackageName(BusinessGroupEditController.class);

    private BusinessGroupFormController modifyBusinessGroupController;
    private BusinessGroup currBusinessGroup;
    private CollaborationToolsSettingsController ctc;
    private GroupController ownerGrpCntrllr;
    private GroupController partipGrpCntrllr;
    private WaitingGroupController waitingGruppeController;
    private final BGAreaDao areaManager;
    private AreasToGroupDataModel areaDataModel;
    private final BGRightManager rightManager;
    private RightsToGroupDataModel rightDataModel;
    private Choice areasChoice, rightsChoice;
    private List<BGArea> allAreas, selectedAreas;
    private List<String> selectedRights;
    private BGRights bgRights;
    private final BGContext bgContext;
    private TabbedPane tabbedPane;
    private VelocityContainer vc_edit;
    private VelocityContainer vc_tab_bgDetails;
    private VelocityContainer vc_tab_grpmanagement;
    private VelocityContainer vc_tab_bgCTools;
    private VelocityContainer vc_tab_bgAreas;
    private VelocityContainer vc_tab_bgRights;
    private final BGConfigFlags flags;
    private DisplayMemberSwitchForm dmsForm;

    private final LockResult lockEntry;

    private DialogBoxController alreadyLockedDialogController;

    private final BusinessGroupService businessGroupService;
    private final BusinessGroupEBL businessGroupEBL;

    /**
     * Never call this constructor directly, use the BGControllerFactory instead!!
     * 
     * @param ureq
     * @param wControl
     * @param currBusinessGroup
     * @param configurationFlags
     *            Flags to configure the controllers features. The controller does no type specific stuff implicit just by looking at the group type. Type specifig
     *            features must be flagged.
     */
    public BusinessGroupEditController(final UserRequest ureq, final WindowControl wControl, BusinessGroup currBusinessGroup, final BGConfigFlags configurationFlags) {
        super(ureq, wControl);

        // OLAT-4955: setting the stickyActionType here passes it on to any controller defined in the scope of the editor,
        // basically forcing any logging action called within the bg editor to be of type 'admin'
        getUserActivityLogger().setStickyActionType(ActionType.admin);
        addLoggingResourceable(LoggingResourceable.wrap(currBusinessGroup));

        // Initialize managers
        this.areaManager = BGAreaDaoImpl.getInstance();
        this.rightManager = BGRightManagerImpl.getInstance();
        businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        businessGroupEBL = CoreSpringFactory.getBean(BusinessGroupEBL.class);
        // Initialize other members

        this.currBusinessGroup = businessGroupService.loadBusinessGroup(currBusinessGroup); // reload
        // group
        this.flags = configurationFlags;
        this.bgContext = this.currBusinessGroup.getGroupContext();
        // Initialize translator:
        // package translator with default group fallback translators and type
        // translator
        setTranslator(BGTranslatorFactory.createBGPackageTranslator(PACKAGE, this.currBusinessGroup.getType(), ureq.getLocale()));
        // Initialize available rights
        if (flags.isEnabled(BGConfigFlags.RIGHTS)) {
            // for now only course rights are relevant
            // would be nice if this was shomehow defined when initializing
            // the group context
            this.bgRights = new CourseRights(ureq.getLocale());
        }

        Identity identity = ureq.getIdentity();
        lockEntry = businessGroupEBL.acquireGroupLockForEditing(identity, this.currBusinessGroup);
        if (lockEntry.isSuccess()) {
            // add as listener to BusinessGroup so we are being notified about changes.
            CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, currBusinessGroup);

            // add Tabbed Pane for configuration
            tabbedPane = new TabbedPane("bgTabbs", ureq.getLocale());
            tabbedPane.addListener(this);
            vc_tab_bgDetails = createTabDetails(ureq, this.currBusinessGroup);// modifies vc_tab_bgDetails
            tabbedPane.addTab(translate("group.edit.tab.details"), vc_tab_bgDetails);
            if (flags.isEnabled(BGConfigFlags.GROUP_COLLABTOOLS)) {
                vc_tab_bgCTools = createTabCollabTools(ureq, flags);
                tabbedPane.addTab(translate("group.edit.tab.collabtools"), vc_tab_bgCTools);
            }
            if (flags.isEnabled(BGConfigFlags.AREAS)) {
                vc_tab_bgAreas = createTabAreas();
                tabbedPane.addTab(translate("group.edit.tab.areas"), vc_tab_bgAreas);
            }
            if (flags.isEnabled(BGConfigFlags.RIGHTS)) {
                vc_tab_bgRights = createTabRights();
                tabbedPane.addTab(translate("group.edit.tab.rights"), vc_tab_bgRights);
            }
            vc_tab_grpmanagement = createTabGroupManagement(ureq);
            tabbedPane.addTab(translate("group.edit.tab.members"), vc_tab_grpmanagement);

            vc_edit = createVelocityContainer("edit");
            vc_edit.put("tabbedpane", tabbedPane);
            vc_edit.contextPut("title",
                    getTranslator().translate("group.edit.title", new String[] { StringEscapeUtils.escapeHtml(this.currBusinessGroup.getName()).toString() }));
            vc_edit.contextPut("type", this.currBusinessGroup.getType());
            putInitialPanel(vc_edit);
        } else {
            // lock was not successful !
            alreadyLockedDialogController = DialogBoxUIFactory.createResourceLockedMessage(ureq, wControl, lockEntry, "error.message.locked", getTranslator());
            listenTo(alreadyLockedDialogController);
            alreadyLockedDialogController.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == areasChoice) {
            if (event == Choice.EVNT_VALIDATION_OK) {
                updateGroupAreaRelations();
                // do loggin
                for (final Iterator<BGArea> iter = selectedAreas.iterator(); iter.hasNext();) {
                    final BGArea area = iter.next();
                    ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_AREA_UPDATED, getClass(), LoggingResourceable.wrap(area));
                }
                if (selectedAreas.size() == 0) {
                    ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_AREA_UPDATED_EMPTY, getClass());
                }
            }
        } else if (source == rightsChoice) {
            if (event == Choice.EVNT_VALIDATION_OK) {
                updateGroupRightsRelations();
                // do logging
                for (final Iterator<String> iter = selectedRights.iterator(); iter.hasNext();) {
                    ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_RIGHT_UPDATED, getClass(), LoggingResourceable.wrapBGRight(iter.next()));
                }
                if (selectedRights.size() == 0) {
                    ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_RIGHT_UPDATED_EMPTY, getClass());
                }
                // notify current active users of this business group
                BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT, currBusinessGroup, null);
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.dmsForm && event == Event.CHANGED_EVENT) {
            BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(currBusinessGroup);
            bgpm.updateDisplayMembers(dmsForm.getShowOwners(), dmsForm.getShowPartipiciants(), dmsForm.getShowWaitingList());
            bgpm = null;
            // notify current active users of this business group
            BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, currBusinessGroup, null);
            // do logging
            ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());

        } else if (source == ctc) {
            if (event == Event.CHANGED_EVENT) {
                // notify current active users of this business group
                BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, currBusinessGroup, null);
            }
        } else if (source == alreadyLockedDialogController) {
            // closed dialog box either by clicking ok, or closing the box
            if (event == Event.CANCELLED_EVENT || DialogBoxUIFactory.isOkEvent(event)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (event instanceof IdentitiesAddEvent) {
            final IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
            BusinessGroupAddResponse response = null;
            addLoggingResourceable(LoggingResourceable.wrap(currBusinessGroup));
            if (source == ownerGrpCntrllr) {
                response = businessGroupService.addOwnersAndFireEvent(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), currBusinessGroup, flags);
            } else if (source == partipGrpCntrllr) {
                response = businessGroupService.addParticipantsAndFireEvent(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), currBusinessGroup, flags);
            } else if (source == waitingGruppeController) {
                response = businessGroupService.addToWaitingListAndFireEvent(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), currBusinessGroup, flags);
            }
            identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
            identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
            identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if (event instanceof IdentitiesRemoveEvent) {
            final List<Identity> identities = ((IdentitiesRemoveEvent) event).getRemovedIdentities();
            if (source == ownerGrpCntrllr) {
                businessGroupService.removeOwnersAndFireEvent(ureq.getIdentity(), identities, currBusinessGroup, flags);
            } else if (source == partipGrpCntrllr) {
                businessGroupService.removeParticipantsAndFireEvent(ureq.getIdentity(), identities, currBusinessGroup, flags);
                if (currBusinessGroup.getWaitingListEnabled().booleanValue()) {
                    // It is possible that a user is transfered from waiting-list to participants => reload data to see transfered user in right group.
                    partipGrpCntrllr.reloadData();
                    waitingGruppeController.reloadData();
                }
            } else if (source == waitingGruppeController) {
                businessGroupService.removeFromWaitingListAndFireEvent(ureq.getIdentity(), identities, currBusinessGroup, flags);
            }
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if (source == waitingGruppeController) {
            if (event instanceof IdentitiesMoveEvent) {
                final IdentitiesMoveEvent identitiesMoveEvent = (IdentitiesMoveEvent) event;
                final BusinessGroupAddResponse response = businessGroupService.moveIdentitiesFromWaitingListToParticipant(identitiesMoveEvent.getChosenIdentities(),
                        ureq.getIdentity(), currBusinessGroup, flags);
                identitiesMoveEvent.setNotMovedIdentities(response.getIdentitiesAlreadyInGroup());
                identitiesMoveEvent.setMovedIdentities(response.getAddedIdentities());
                // Participant and waiting-list were changed => reload both
                partipGrpCntrllr.reloadData();
                waitingGruppeController.reloadData();
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == this.modifyBusinessGroupController) {
            if (event == Event.DONE_EVENT) {
                // update business group with the specified values
                // values are taken from the modifyBusinessGroupForm
                updateBusinessGroup();
                // inform index about change
                ChangeManager.changed(ChangeManager.ACTION_UPDATE, this.currBusinessGroup);
                refreshAllTabs(ureq);
                // notify current active users of this business group
                BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, this.currBusinessGroup, null);
                // rename the group also in the IM servers group list
                if (this.flags.isEnabled(BGConfigFlags.BUDDYLIST)) {
                    if (InstantMessagingModule.isEnabled()) {
                        final String groupID = InstantMessagingModule.getAdapter().createChatRoomString(this.currBusinessGroup);
                        InstantMessagingModule.getAdapter().renameRosterGroup(groupID, this.modifyBusinessGroupController.getGroupName());
                    }
                }
                // do logging
                ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());
            } else if (event == Event.CANCELLED_EVENT) {
                // reinit details form
                // TODO:fj:b introduce reset() for a form

                if (this.modifyBusinessGroupController != null) {
                    removeAsListenerAndDispose(this.modifyBusinessGroupController);
                }
                this.modifyBusinessGroupController = new BusinessGroupFormController(ureq, getWindowControl(), this.currBusinessGroup,
                        this.flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE));
                listenTo(this.modifyBusinessGroupController);
                this.vc_tab_bgDetails.put("businessGroupForm", this.modifyBusinessGroupController.getInitialComponent());

            }
        }
    }

    /**
     * persist the updates
     */
    private void updateBusinessGroup() {
        final String bgName = this.modifyBusinessGroupController.getGroupName();
        final String bgDesc = this.modifyBusinessGroupController.getGroupDescription();
        final Integer bgMin = this.modifyBusinessGroupController.getGroupMin();
        final Integer bgMax = this.modifyBusinessGroupController.getGroupMax();
        final Boolean waitingListEnabled = this.modifyBusinessGroupController.isWaitingListEnabled();
        final Boolean autoCloseRanksEnabled = this.modifyBusinessGroupController.isAutoCloseRanksEnabled();

        final Integer prevMaxParticipants = currBusinessGroup.getMaxParticipants();
        final BusinessGroupTransferObject businessGroupTransferObject = new BusinessGroupTransferObject(bgName, bgDesc, bgMax, bgMin, waitingListEnabled,
                autoCloseRanksEnabled, new Date(System.currentTimeMillis()));
        this.currBusinessGroup = businessGroupEBL.updateBusinessGroup(waitingListEnabled, businessGroupTransferObject, this.currBusinessGroup);

        // OLAT-6702: move waiting persons to participant list if waiting list increased
        if (waitingListEnabled) {
            final Integer newMaxParticipants = modifyBusinessGroupController.getGroupMax();
            if (newMaxParticipants != null && (prevMaxParticipants == null || newMaxParticipants.compareTo(prevMaxParticipants) > 0)) {
                final int currentParticipantCount = businessGroupEBL.getParticipants(currBusinessGroup).size();
                if (newMaxParticipants > currentParticipantCount) {
                    // take waiting identities from waiting list
                    final List<Identity> waitingIdentities = businessGroupService.getWaitingListFor(currBusinessGroup);
                    if (!waitingIdentities.isEmpty()) {
                        final int numberToMove = Math.min(newMaxParticipants - currentParticipantCount, waitingIdentities.size());
                        businessGroupService.moveIdentitiesFromWaitingListToParticipant(waitingIdentities.subList(0, numberToMove), getIdentity(), currBusinessGroup,
                                flags);
                        // reload data to see transfered user in right group.
                        partipGrpCntrllr.reloadData();
                        waitingGruppeController.reloadData();
                    }
                }
            }
        }

        // switch on/off waiting-list in member tab
        vc_tab_grpmanagement.contextPut("hasWaitingGrp", waitingListEnabled);
    }

    /**
     * Update the areas associated to this group: remove and add areas
     */
    private void updateGroupAreaRelations() {
        // refresh group to prevent stale object exception and context proxy issues
        this.currBusinessGroup = businessGroupService.loadBusinessGroup(this.currBusinessGroup);
        // 1) add areas to group
        final List<Integer> addedAreas = areasChoice.getAddedRows();
        Iterator<Integer> iterator = addedAreas.iterator();
        while (iterator.hasNext()) {
            final Integer position = iterator.next();
            final BGArea area = areaDataModel.getArea(position.intValue());
            areaManager.addBGToBGArea(this.currBusinessGroup, area);
            this.selectedAreas.add(area);
        }
        // 2) remove areas from group
        final List<Integer> removedAreas = areasChoice.getRemovedRows();
        iterator = removedAreas.iterator();
        while (iterator.hasNext()) {
            final Integer position = iterator.next();
            final BGArea area = areaDataModel.getArea(position.intValue());
            areaManager.removeBGFromArea(this.currBusinessGroup, area);
            this.selectedAreas.remove(area);
        }
    }

    /**
     * Update the rights associated to this group: remove and add rights
     */
    private void updateGroupRightsRelations() {
        // refresh group to prevent stale object exception and context proxy issues
        this.currBusinessGroup = businessGroupService.loadBusinessGroup(this.currBusinessGroup);
        // 1) add rights to group
        final List<Integer> addedRights = rightsChoice.getAddedRows();
        Iterator<Integer> iterator = addedRights.iterator();
        while (iterator.hasNext()) {
            final Integer position = iterator.next();
            final String right = rightDataModel.getRight(position.intValue());
            rightManager.addBGRight(right, this.currBusinessGroup);
            this.selectedRights.add(right);
        }
        // 2) remove rights from group
        final List<Integer> removedRights = rightsChoice.getRemovedRows();
        iterator = removedRights.iterator();
        while (iterator.hasNext()) {
            final Integer position = iterator.next();
            final String right = rightDataModel.getRight(position.intValue());
            rightManager.removeBGRight(right, this.currBusinessGroup);
            this.selectedRights.remove(right);
        }
    }

    /**
     * get a CollaborationToolController via CollaborationToolsFactory, the CTC is initialised with the UserRequest. The CTC provides a List of Collaboration Tools which
     * can be enabled and disabled (so far) through checkboxes.
     * 
     * @param ureq
     */
    private VelocityContainer createTabCollabTools(final UserRequest ureq, final BGConfigFlags flags) {
        final VelocityContainer tmp = createVelocityContainer("tab_bgCollabTools");
        final CollaborationTools ctsm = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(currBusinessGroup);
        ctc = ctsm.createCollaborationToolsSettingsController(ureq, getWindowControl(), flags);
        // we are listening on CollaborationToolsSettingsController events
        // which are just propagated to our attached controllerlistener...
        // e.g. the BusinessGroupMainRunController, updating the MenuTree
        // if a CollaborationToolsSetting has changed... so far this means
        // enabling/disabling a Tool within the tree.
        listenTo(ctc);
        tmp.put("collabTools", ctc.getInitialComponent());
        tmp.contextPut("type", this.currBusinessGroup.getType());
        return tmp;
    }

    /**
     * adds a area-to-group tab to the tabbed pane
     */
    private VelocityContainer createTabAreas() {
        final VelocityContainer tmp = createVelocityContainer("tab_bgAreas");
        this.allAreas = areaManager.findBGAreasOfBGContext(this.bgContext);
        this.selectedAreas = areaManager.findBGAreasOfBusinessGroup(this.currBusinessGroup);
        this.areaDataModel = new AreasToGroupDataModel(this.allAreas, this.selectedAreas);

        areasChoice = new Choice("areasChoice", getTranslator());
        areasChoice.setSubmitKey("submit");
        areasChoice.setCancelKey("cancel");
        areasChoice.setTableDataModel(this.areaDataModel);
        areasChoice.addListener(this);
        tmp.put("areasChoice", areasChoice);
        tmp.contextPut("noAreasFound", (allAreas.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
        tmp.contextPut("isGmAdmin", Boolean.valueOf(flags.isEnabled(BGConfigFlags.IS_GM_ADMIN)));
        tmp.contextPut("type", this.currBusinessGroup.getType());
        return tmp;
    }

    /**
     * adds a right-to-group tab to the tabbed pane
     */
    private VelocityContainer createTabRights() {
        final VelocityContainer tmp = createVelocityContainer("tab_bgRights");
        this.selectedRights = rightManager.findBGRights(this.currBusinessGroup);
        this.rightDataModel = new RightsToGroupDataModel(bgRights, this.selectedRights);

        rightsChoice = new Choice("rightsChoice", getTranslator());
        rightsChoice.setSubmitKey("submit");
        rightsChoice.setCancelKey("cancel");
        rightsChoice.setTableDataModel(this.rightDataModel);
        rightsChoice.addListener(this);
        tmp.put("rightsChoice", rightsChoice);
        tmp.contextPut("type", this.currBusinessGroup.getType());
        return tmp;
    }

    /**
     * @param businessGroup
     */
    private VelocityContainer createTabDetails(final UserRequest ureq, final BusinessGroup businessGroup) {
        final VelocityContainer tmp = createVelocityContainer("tab_bgDetail");

        if (this.modifyBusinessGroupController != null) {
            removeAsListenerAndDispose(this.modifyBusinessGroupController);
        }
        this.modifyBusinessGroupController = new BusinessGroupFormController(ureq, getWindowControl(), businessGroup,
                this.flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE));
        listenTo(this.modifyBusinessGroupController);

        tmp.put("businessGroupForm", this.modifyBusinessGroupController.getInitialComponent());
        tmp.contextPut("BuddyGroup", businessGroup);
        tmp.contextPut("type", this.currBusinessGroup.getType());
        tmp.contextPut("groupid", this.currBusinessGroup.getKey());
        return tmp;
    }

    /**
     * @param ureq
     */
    private VelocityContainer createTabGroupManagement(final UserRequest ureq) {
        final boolean hasOwners = flags.isEnabled(BGConfigFlags.GROUP_OWNERS);
        final boolean hasPartips = true;//
        final boolean hasWaitingList = currBusinessGroup.getWaitingListEnabled().booleanValue();
        //
        final VelocityContainer tmp = createVelocityContainer("tab_bgGrpMngmnt");
        // Member Display Form, allows to enable/disable that others partips see
        // partips and/or owners
        //
        BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(currBusinessGroup);
        // configure the form with checkboxes for owners and/or partips according
        // the booleans
        removeAsListenerAndDispose(dmsForm);
        dmsForm = new DisplayMemberSwitchForm(ureq, getWindowControl(), hasOwners, hasPartips, hasWaitingList);
        listenTo(dmsForm);
        // set if the checkboxes are checked or not.
        if (hasOwners) {
            dmsForm.setShowOwnersChecked(bgpm.showOwners());
        }
        if (hasPartips) {
            dmsForm.setShowPartipsChecked(bgpm.showPartips());
        }
        if (hasWaitingList) {
            dmsForm.setShowWaitingListChecked(bgpm.showWaitingList());
        }
        bgpm = null;

        tmp.put("displayMembers", dmsForm.getInitialComponent());
        final boolean enableTablePreferences = flags.isEnabled(BGConfigFlags.ADMIN_SEE_ALL_USER_DATA);
        BusinessGroupConfirmationSenderFactory confirmationSenderFactory = new BusinessGroupConfirmationSenderFactoryImpl(ureq.getIdentity(), currBusinessGroup);
        if (hasOwners) {
            final boolean requiresOwner = flags.isEnabled(BGConfigFlags.GROUP_OWNER_REQURED);
            // groupcontroller which allows to remove all members depending on
            // configuration.
            removeAsListenerAndDispose(ownerGrpCntrllr);
            AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractGroupConfirmationSender = confirmationSenderFactory
                    .getOwnersConfirmationSender();
            ownerGrpCntrllr = new GroupController(ureq, getWindowControl(), true, requiresOwner, enableTablePreferences, currBusinessGroup.getOwnerGroup(),
                    abstractGroupConfirmationSender);
            listenTo(ownerGrpCntrllr);
            // expose to velocity
            tmp.put("ownerGrpMngmnt", ownerGrpCntrllr.getInitialComponent());
            tmp.contextPut("hasOwnerGrp", Boolean.TRUE);
        } else {
            tmp.contextPut("hasOwnerGrp", Boolean.FALSE);
        }
        // groupcontroller which allows to remove all members
        removeAsListenerAndDispose(partipGrpCntrllr);
        AbstractGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractGroupConfirmationSender = confirmationSenderFactory
                .getMemberConfirmationSender();
        partipGrpCntrllr = new GroupController(ureq, getWindowControl(), true, false, enableTablePreferences, currBusinessGroup.getPartipiciantGroup(),
                abstractGroupConfirmationSender);
        listenTo(partipGrpCntrllr);

        // expose to velocity
        tmp.put("partipGrpMngmnt", partipGrpCntrllr.getInitialComponent());
        tmp.contextPut("type", this.currBusinessGroup.getType());

        // Show waiting list only if enabled
        if (hasWaitingList) {
            // waitinglist-groupcontroller which allows to remove all members
            final SecurityGroup waitingList = currBusinessGroup.getWaitingGroup();
            removeAsListenerAndDispose(waitingGruppeController);
            AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractWaitingListGroupConfirmationSender = confirmationSenderFactory
                    .getWaitingListConfirmationSender();
            waitingGruppeController = new WaitingGroupController(ureq, getWindowControl(), true, false, enableTablePreferences, waitingList,
                    abstractWaitingListGroupConfirmationSender);
            listenTo(waitingGruppeController);
            // expose to velocity
            tmp.put("waitingGrpMngmnt", waitingGruppeController.getInitialComponent());
            tmp.contextPut("hasWaitingGrp", Boolean.TRUE);
        } else {
            tmp.contextPut("hasWaitingGrp", Boolean.FALSE);
        }
        return tmp;
    }

    /**
	 */
    @Override
    public void event(final Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent delEvent = (OLATResourceableJustBeforeDeletedEvent) event;
            if (!delEvent.targetEquals(currBusinessGroup)) {
                throw new AssertException("receiving a delete event for a olatres we never registered for!!!:" + delEvent.getDerivedOres());
            }
            dispose();
        }
    }

    /**
     * @return true if lock on group has been acquired, flase otherwhise
     */
    public boolean isLockAcquired() {
        return lockEntry.isSuccess();
    }

    /**
	 */
    @Override
    protected void doDispose() {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.currBusinessGroup);
        // release lock on dispose
        releaseBusinessGroupEditLock();
    }

    private void releaseBusinessGroupEditLock() {
        if (lockEntry.isSuccess()) {
            businessGroupEBL.releaseGroupLock(lockEntry);
        } else if (alreadyLockedDialogController != null) {
            // dispose if dialog still visible
            alreadyLockedDialogController.dispose();
        }
    }

    /**
     * Refresh Member-tab when waiting-list configuration change.
     * 
     * @param ureq
     */
    private void refreshAllTabs(final UserRequest ureq) {
        tabbedPane.removeAll();
        tabbedPane.addTab(translate("group.edit.tab.details"), vc_tab_bgDetails);
        if (flags.isEnabled(BGConfigFlags.GROUP_COLLABTOOLS)) {
            tabbedPane.addTab(translate("group.edit.tab.collabtools"), vc_tab_bgCTools);
        }
        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            tabbedPane.addTab(translate("group.edit.tab.areas"), vc_tab_bgAreas);
        }
        if (flags.isEnabled(BGConfigFlags.RIGHTS)) {
            tabbedPane.addTab(translate("group.edit.tab.rights"), vc_tab_bgRights);
        }
        tabbedPane.addTab(translate("group.edit.tab.members"), createTabGroupManagement(ureq));
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
