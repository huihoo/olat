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

package org.olat.presentation.group.securitygroup;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.commons.mail.MailNotificationEditController;
import org.olat.presentation.events.MultiIdentityChosenEvent;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.group.securitygroup.wizard.UsersToGroupWizardController;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.presentation.user.administration.UserSearchController;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Generic group management controller. Displays the list of users that are in the given security group and features an add button to add users to the group.
 * <p>
 * Fired events:
 * <ul>
 * <li>IdentityAddedEvent</li>
 * <li>IdentityRemovedEvent</li>
 * <li>SingleIdentityChosenEvent</li>
 * <li>Event.CANCELLED_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: Jan 25, 2005
 * 
 * @author Felix Jost, Florian Gnägi
 */

public class GroupController extends BasicController {

    protected boolean keepAtLeastOne;
    protected boolean mayModifyMembers;

    protected static final String COMMAND_REMOVEUSER = "removesubjectofgroup";
    protected static final String COMMAND_VCARD = "show.vcard";
    protected static final String COMMAND_SELECTUSER = "select.user";

    protected SecurityGroup securityGroup;
    protected BaseSecurity securityManager;
    private UserService userService;
    protected VelocityContainer groupmemberview;
    protected Panel content;

    protected IdentitiesOfGroupTableDataModel identitiesTableModel;

    private List<Identity> toAdd, toRemove;

    private UserSearchController usc;
    private MailNotificationEditController addUserMailCtr, removeUserMailCtr;
    private UsersToGroupWizardController userToGroupWizardCtr;
    private DialogBoxController confirmDelete;

    protected TableController tableCtr;
    private Link addUsersButton;
    private Link addUserButton;
    private Translator myTrans;

    private MailTemplate addUserMailDefaultTempl, removeUserMailDefaultTempl, removeUserMailCustomTempl;
    private boolean showSenderInAddMailFooter;
    private boolean showSenderInRemovMailFooter;
    private CloseableModalController cmc;
    protected static final String usageIdentifyer = IdentitiesOfGroupTableDataModel.class.getCanonicalName();
    protected boolean isAdministrativeUser;

    /**
     * @param ureq
     * @param wControl
     * @param mayModifyMembers
     * @param keepAtLeastOne
     * @param enableTablePreferences
     * @param aSecurityGroup
     */
    public GroupController(final UserRequest ureq, final WindowControl wControl, final boolean mayModifyMembers, final boolean keepAtLeastOne,
            final boolean enableTablePreferences, final SecurityGroup aSecurityGroup) {
        super(ureq, wControl);
        init(ureq, mayModifyMembers, keepAtLeastOne, enableTablePreferences, false, aSecurityGroup);
    }

    /**
     * @param ureq
     * @param wControl
     * @param mayModifyMembers
     * @param keepAtLeastOne
     * @param enableTablePreferences
     * @param aSecurityGroup
     * @param enableUserSelection
     */
    public GroupController(final UserRequest ureq, final WindowControl wControl, final boolean mayModifyMembers, final boolean keepAtLeastOne,
            final boolean enableTablePreferences, final boolean enableUserSelection, final SecurityGroup aSecurityGroup) {
        super(ureq, wControl);
        init(ureq, mayModifyMembers, keepAtLeastOne, enableTablePreferences, enableUserSelection, aSecurityGroup);
    }

    protected void init(final UserRequest ureq, final boolean mayModifyMembers, final boolean keepAtLeastOne, final boolean enableTablePreferences,
            final boolean enableUserSelection, final SecurityGroup aSecurityGroup) {
        userService = CoreSpringFactory.getBean(UserService.class);
        this.securityGroup = aSecurityGroup;
        this.mayModifyMembers = mayModifyMembers;
        this.keepAtLeastOne = keepAtLeastOne;
        this.securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        final Roles roles = ureq.getUserSession().getRoles();
        isAdministrativeUser = roles.isAdministrativeUser();

        // default group controller has no mail functionality
        this.addUserMailDefaultTempl = null;
        this.removeUserMailDefaultTempl = null;

        groupmemberview = createVelocityContainer("index");

        addUsersButton = LinkFactory.createButtonSmall("overview.addusers", groupmemberview, this);
        addUserButton = LinkFactory.createButtonSmall("overview.adduser", groupmemberview, this);

        if (mayModifyMembers) {
            groupmemberview.contextPut("mayadduser", Boolean.TRUE);
        }

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        if (enableTablePreferences) {
            // save table preferences for each group seperatly
            if (mayModifyMembers) {
                tableConfig.setPreferencesOffered(true, "groupcontroller" + securityGroup.getKey());
            } else {
                // different rowcount...
                tableConfig.setPreferencesOffered(true, "groupcontrollerreadonly" + securityGroup.getKey());
            }
        }
        // TODO:fj:c move to UserControllerFactory class
        myTrans = userService.getUserPropertiesConfig().getTranslator(getTranslator());
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myTrans);
        listenTo(tableCtr);

        initGroupTable(tableCtr, ureq, enableTablePreferences, enableUserSelection);

        // set data model
        final List combo = securityManager.getIdentitiesAndDateOfSecurityGroup(this.securityGroup);
        final List<UserPropertyHandler> userPropertyHandlers = userService.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        identitiesTableModel = new IdentitiesOfGroupTableDataModel(combo, ureq.getLocale(), userPropertyHandlers);
        tableCtr.setTableDataModel(identitiesTableModel);
        groupmemberview.put("subjecttable", tableCtr.getInitialComponent());

        content = putInitialPanel(groupmemberview);
    }

    /**
     * @param addUserMailDefaultTempl
     *            Set a template to send mail when adding users to group
     */
    public void setAddUserMailTempl(final MailTemplate addUserMailTempl, final boolean showSenderInAddFooter) {
        this.addUserMailDefaultTempl = addUserMailTempl;
        this.showSenderInAddMailFooter = showSenderInAddFooter;
    }

    /**
     * @param removeUserMailDefaultTempl
     *            Set a template to send mail when removing a user from the group
     */
    public void setRemoveUserMailTempl(final MailTemplate removeUserMailTempl, final boolean showSenderInRemoveFooter) {
        this.removeUserMailDefaultTempl = removeUserMailTempl;
        this.showSenderInRemovMailFooter = showSenderInRemoveFooter;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == addUserButton) {
            if (!mayModifyMembers) {
                throw new AssertException("not allowed to add a member!");
            }

            removeAsListenerAndDispose(usc);
            usc = new UserSearchController(ureq, getWindowControl(), true, true, false);
            listenTo(usc);

            final Component usersearchview = usc.getInitialComponent();
            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), usersearchview, true, translate("add.searchuser"));
            listenTo(cmc);

            cmc.activate();
        } else if (source == addUsersButton) {
            if (!mayModifyMembers) {
                throw new AssertException("not allowed to add members!");
            }

            removeAsListenerAndDispose(userToGroupWizardCtr);
            userToGroupWizardCtr = new UsersToGroupWizardController(ureq, getWindowControl(), this.securityGroup, addUserMailDefaultTempl);
            listenTo(userToGroupWizardCtr);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), userToGroupWizardCtr.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller sourceController, final Event event) {
        if (sourceController == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                // Single row selects
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(COMMAND_VCARD)) {
                    // get identitiy and open new visiting card controller in new window
                    final int rowid = te.getRowId();
                    final Identity identity = identitiesTableModel.getIdentityAt(rowid);
                    final ControllerCreator userInfoMainControllerCreator = new ControllerCreator() {
                        @Override
                        public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                            return new UserInfoMainController(lureq, lwControl, identity);
                        }
                    };
                    // wrap the content controller into a full header layout
                    final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
                    // open in new browser window
                    final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
                    pbw.open(ureq);
                    //
                } else if (actionid.equals(COMMAND_SELECTUSER)) {
                    final int rowid = te.getRowId();
                    final Identity identity = identitiesTableModel.getIdentityAt(rowid);
                    // TODO whats this??
                    fireEvent(ureq, new SingleIdentityChosenEvent(identity));
                }

            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                // Multiselect events
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(COMMAND_REMOVEUSER)) {
                    if (tmse.getSelection().isEmpty()) {
                        // empty selection
                        content.setDirty(true);
                        showWarning("msg.selectionempty");
                        return;
                    }
                    final int size = identitiesTableModel.getObjects().size();
                    toRemove = identitiesTableModel.getIdentities(tmse.getSelection());
                    // list is never null, but can be empty
                    if (keepAtLeastOne && (size == 1 || size - toRemove.size() == 0)) {
                        // at least one must be kept
                        // do not delete the last one => ==1
                        // do not allow to delete all => size - selectedCnt == 0
                        content.setDirty(true);
                        showError("msg.atleastone");
                    } else {
                        // valid selection to be deleted.
                        if (removeUserMailDefaultTempl == null) {
                            doBuildConfirmDeleteDialog(ureq);
                        } else {
                            removeAsListenerAndDispose(removeUserMailCtr);
                            removeUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, removeUserMailDefaultTempl, true);
                            listenTo(removeUserMailCtr);

                            removeAsListenerAndDispose(cmc);
                            cmc = new CloseableModalController(getWindowControl(), translate("close"), removeUserMailCtr.getInitialComponent());
                            listenTo(cmc);

                            cmc.activate();
                        }
                    }
                }
            }
        } else if (sourceController == removeUserMailCtr) {
            if (event == Event.DONE_EVENT) {
                removeUserMailCustomTempl = removeUserMailCtr.getMailTemplate();
                cmc.deactivate();
                doBuildConfirmDeleteDialog(ureq);
            } else if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            } else {
                throw new RuntimeException("unknown event ::" + event.getCommand());
            }

        } else if (sourceController == usc) {
            if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            } else {
                if (event instanceof SingleIdentityChosenEvent) {
                    final SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
                    final Identity choosenIdentity = singleEvent.getChosenIdentity();
                    if (choosenIdentity == null) {
                        showError("msg.selectionempty");
                        return;
                    }
                    toAdd = new ArrayList<Identity>();
                    toAdd.add(choosenIdentity);
                } else if (event instanceof MultiIdentityChosenEvent) {
                    final MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
                    toAdd = multiEvent.getChosenIdentities();
                    if (toAdd.size() == 0) {
                        showError("msg.selectionempty");
                        return;
                    }
                } else {
                    throw new RuntimeException("unknown event ::" + event.getCommand());
                }

                if (toAdd.size() == 1) {
                    // check if already in group [makes only sense for a single choosen identity]
                    if (securityManager.isIdentityInSecurityGroup(toAdd.get(0), securityGroup)) {
                        getWindowControl().setInfo(translate("msg.subjectalreadyingroup", new String[] { toAdd.get(0).getName() }));
                        return;
                    }
                } else if (toAdd.size() > 1) {
                    // check if already in group
                    boolean someAlreadyInGroup = false;
                    final List<Identity> alreadyInGroup = new ArrayList<Identity>();
                    for (int i = 0; i < toAdd.size(); i++) {
                        if (securityManager.isIdentityInSecurityGroup(toAdd.get(i), securityGroup)) {
                            tableCtr.setMultiSelectSelectedAt(i, false);
                            alreadyInGroup.add(toAdd.get(i));
                            someAlreadyInGroup = true;
                        }
                    }
                    if (someAlreadyInGroup) {
                        String names = "";
                        for (final Identity ident : alreadyInGroup) {
                            names += " " + ident.getName();
                            toAdd.remove(ident);
                        }
                        getWindowControl().setInfo(translate("msg.subjectsalreadyingroup", names));
                    }
                    if (toAdd.isEmpty()) {
                        return;
                    }
                }

                // in both cases continue adding the users or asking for the mail
                // template if available (=not null)
                cmc.deactivate();
                if (addUserMailDefaultTempl == null) {
                    doAddIdentitiesToGroup(ureq, toAdd, null);
                } else {
                    removeAsListenerAndDispose(addUserMailCtr);
                    addUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, addUserMailDefaultTempl, true);
                    listenTo(addUserMailCtr);

                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), translate("close"), addUserMailCtr.getInitialComponent());
                    listenTo(cmc);

                    cmc.activate();
                }
            }
            // in any case cleanup this controller, not used anymore
            usc.dispose();
            usc = null;

        } else if (sourceController == addUserMailCtr) {
            if (event == Event.DONE_EVENT) {
                final MailTemplate customTemplate = addUserMailCtr.getMailTemplate();
                doAddIdentitiesToGroup(ureq, toAdd, customTemplate);
                cmc.deactivate();
            } else if (event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            } else {
                throw new RuntimeException("unknown event ::" + event.getCommand());
            }

        } else if (sourceController == confirmDelete) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // before deleting, assure it is allowed
                if (!mayModifyMembers) {
                    throw new AssertException("not allowed to remove member!");
                }
                // list is never null, but can be empty
                // TODO: Theoretically it can happen that the table model here is not accurate!
                // the 'keep at least one' should be handled by the security manager that should
                // synchronizes the method on the group
                final int size = identitiesTableModel.getObjects().size();
                if (keepAtLeastOne && (size - toRemove.size() == 0)) {
                    showError("msg.atleastone");
                } else {
                    doRemoveIdentitiesFromGroup(ureq, toRemove, removeUserMailCustomTempl);
                }
            }

        } else if (sourceController == userToGroupWizardCtr) {
            if (event instanceof MultiIdentityChosenEvent) {
                final MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
                final List<Identity> choosenIdentities = multiEvent.getChosenIdentities();
                final MailTemplate customTemplate = multiEvent.getMailTemplate();
                if (choosenIdentities.size() == 0) {
                    showError("msg.selectionempty");
                    return;
                }
                doAddIdentitiesToGroup(ureq, choosenIdentities, customTemplate);

            } else if (event == Event.CANCELLED_EVENT) {
                // nothing special to do
            }

            // else cancelled
            cmc.deactivate();

        }
    }

    private void doBuildConfirmDeleteDialog(final UserRequest ureq) {
        if (confirmDelete != null) {
            confirmDelete.dispose();
        }
        final StringBuilder names = new StringBuilder();
        for (final Identity identity : toRemove) {
            names.append(identity.getName()).append(" ");
        }
        confirmDelete = activateYesNoDialog(ureq, null, translate("remove.text", names.toString()), confirmDelete);
        return;
    }

    private void doRemoveIdentitiesFromGroup(final UserRequest ureq, final List<Identity> toBeRemoved, final MailTemplate mailTemplate) {
        fireEvent(ureq, new IdentitiesRemoveEvent(toBeRemoved));
        identitiesTableModel.remove(toBeRemoved);
        if (tableCtr != null) {
            // can be null in the follwoing case.
            // the user which does the removal is also in the list of toBeRemoved
            // hence the fireEvent does trigger a disposal of a GroupController, which
            // in turn nullifies the tableCtr... see also OLAT-3331
            tableCtr.modelChanged();
        }

        // send the notification mail
        if (mailTemplate != null) {
            final MailerWithTemplate mailer = MailerWithTemplate.getInstance();
            Identity sender = null; // means no sender in footer
            if (this.showSenderInRemovMailFooter) {
                sender = ureq.getIdentity();
            }
            List<Identity> ccIdentities = new ArrayList<Identity>();
            if (mailTemplate.getCpfrom()) {
                ccIdentities.add(ureq.getIdentity());// add sender to cc-list
            } else {
                ccIdentities = null;
            }
            final MailerResult mailerResult = mailer.sendMailAsSeparateMails(toBeRemoved, ccIdentities, null, mailTemplate, sender);
            MailTemplateHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
        }
    }

    /**
     * Add users from the identites array to the group if they are not guest users and not already in the group
     * 
     * @param ureq
     * @param choosenIdentities
     */
    private void doAddIdentitiesToGroup(final UserRequest ureq, final List<Identity> choosenIdentities, final MailTemplate mailTemplate) {
        // additional security check
        if (!mayModifyMembers) {
            throw new AssertException("not allowed to add member!");
        }

        final IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(choosenIdentities);
        // process workflow to BusinessGroupManager via BusinessGroupEditController
        fireEvent(ureq, identitiesAddedEvent);
        if (!identitiesAddedEvent.getAddedIdentities().isEmpty()) {
            // update table model
            identitiesTableModel.add(identitiesAddedEvent.getAddedIdentities());
            tableCtr.modelChanged();
        }
        // build info message for identities which could be added.
        final StringBuilder infoMessage = new StringBuilder();
        for (final Identity identity : identitiesAddedEvent.getIdentitiesWithoutPermission()) {
            infoMessage.append(translate("msg.isingroupanonymous", identity.getName())).append("<br />");
        }
        for (final Identity identity : identitiesAddedEvent.getIdentitiesAlreadyInGroup()) {
            infoMessage.append(translate("msg.subjectalreadyingroup", identity.getName())).append("<br />");
        }
        // send the notification mail fro added users
        final StringBuilder errorMessage = new StringBuilder();
        if (mailTemplate != null) {
            final MailerWithTemplate mailer = MailerWithTemplate.getInstance();
            Identity sender = null; // means no sender in footer
            if (this.showSenderInAddMailFooter) {
                sender = ureq.getIdentity();
            }
            List<Identity> ccIdentities = new ArrayList<Identity>();
            if (mailTemplate.getCpfrom()) {
                ccIdentities.add(ureq.getIdentity());// add sender to cc-list
            } else {
                ccIdentities = null;
            }
            final MailerResult mailerResult = mailer.sendMailAsSeparateMails(identitiesAddedEvent.getAddedIdentities(), ccIdentities, null, mailTemplate, sender);
            MailTemplateHelper.appendErrorsAndWarnings(mailerResult, errorMessage, infoMessage, ureq.getLocale());
        }
        // report any errors on screen
        if (infoMessage.length() > 0) {
            getWindowControl().setWarning(infoMessage.toString());
        }
        if (errorMessage.length() > 0) {
            getWindowControl().setError(errorMessage.toString());
        }
    }

    @Override
    protected void doDispose() {
        // DialogBoxController and TableController get disposed by BasicController
        // usc, userToGroupWizardCtr, addUserMailCtr, and removeUserMailCtr are registerd with listenTo and get disposed in BasicController
        super.doPreDispose();
    }

    /**
     * Init GroupList-table-controller for non-waitinglist (participant-list, owner-list).
     */
    protected void initGroupTable(final TableController tableCtr, final UserRequest ureq, final boolean enableTablePreferences, final boolean enableUserSelection) {
        final List<UserPropertyHandler> userPropertyHandlers = userService.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        // first the login name
        final DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("table.user.login", 0, COMMAND_VCARD, ureq.getLocale());
        cd0.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
        tableCtr.addColumnDescriptor(cd0);
        int visibleColId = 0;
        // followed by the users fields
        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            final boolean visible = userService.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
            tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i + 1, null, ureq.getLocale()));
            if (visible) {
                visibleColId++;
            }
        }

        // in the end
        if (enableTablePreferences) {
            tableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.subject.addeddate", userPropertyHandlers.size() + 1, null, ureq.getLocale()));
            tableCtr.setSortColumn(++visibleColId, true);
        }
        if (enableUserSelection) {
            tableCtr.addColumnDescriptor(new StaticColumnDescriptor(COMMAND_SELECTUSER, "table.subject.action", myTrans.translate("action.general")));
        }

        if (mayModifyMembers) {
            tableCtr.addMultiSelectAction("action.remove", COMMAND_REMOVEUSER);
            tableCtr.setMultiSelect(true);
        } else {
            // neither offer a table delete column nor allow adduser link
        }
    }

    public void reloadData() {
        // refresh view
        final List combo = securityManager.getIdentitiesAndDateOfSecurityGroup(this.securityGroup);
        final List<UserPropertyHandler> userPropertyHandlers = userService.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
        identitiesTableModel = new IdentitiesOfGroupTableDataModel(combo, myTrans.getLocale(), userPropertyHandlers);
        tableCtr.setTableDataModel(identitiesTableModel);
    }
}
