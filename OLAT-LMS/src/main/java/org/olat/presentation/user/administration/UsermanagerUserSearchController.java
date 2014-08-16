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

package org.olat.presentation.user.administration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.connectors.webdav.WebDAVManager;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.PermissionOnResourceable;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.UserSearchFilter;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.lms.security.authentication.WebDAVAuthManager;
import org.olat.lms.user.UserService;
import org.olat.lms.user.administration.bulkchange.UserBulkChangeManager;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
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
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepRunnerCallback;
import org.olat.presentation.framework.core.control.generic.wizard.StepsMainRunController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.security.authentication.AuthenticationProvider;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.presentation.user.administration.bulkchange.UserBulkChangeStep00;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.mail.ContactList;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jan 31, 2006
 * 
 * @author gnaegi Description: This workflow has two constructors. The first one provides the user an advanced user search form with many search criterias that can be
 *         defined. The second one has the criterias in the constructor as attributes, so the search form won't appear. The following is a list with the search results.
 *         Form the list an identity can be selected which results in a SingleIdentityChosenEvent Alternatively a Canceled Event is fired.
 */
public class UsermanagerUserSearchController extends BasicController {

    private static final String CMD_MAIL = "exeMail";
    private static final String CMD_BULKEDIT = "bulkEditUsers";

    private final VelocityContainer userListVC;
    private VelocityContainer userSearchVC;
    private final VelocityContainer mailVC;
    private final Panel panel;

    private UsermanagerUserSearchForm searchform;
    private TableController tableCtr;
    private List<Identity> identitiesList, selectedIdentities;
    private final ArrayList<String> notUpdatedIdentities = new ArrayList<String>();
    private ExtendedIdentitiesTableDataModel tdm;
    private Identity foundIdentity = null;
    private ContactFormController contactCtr;
    private final Link backFromMail;
    private Link backFromList;
    private boolean showEmailButton = true;
    private StepsMainRunController userBulkChangeStepsController;
    private boolean isAdministrativeUser = false;

    /**
     * Constructor to trigger the user search workflow using a generic search form
     * 
     * @param ureq
     * @param wControl
     */
    public UsermanagerUserSearchController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        userSearchVC = createVelocityContainer("usermanagerUsersearch");

        mailVC = createVelocityContainer("usermanagerMail");
        backFromMail = LinkFactory.createLinkBack(mailVC, this);

        userListVC = createVelocityContainer("usermanagerUserlist");

        backFromList = LinkFactory.createLinkBack(userListVC, this);

        userListVC.contextPut("showBackButton", Boolean.TRUE);
        userListVC.contextPut("emptyList", Boolean.FALSE);
        userListVC.contextPut("showTitle", Boolean.TRUE);

        searchform = new UsermanagerUserSearchForm(ureq, wControl);
        listenTo(searchform);

        userSearchVC.put("usersearch", searchform.getInitialComponent());

        panel = putInitialPanel(userSearchVC);
    }

    /**
     * Constructor to trigger the user search workflow using the given attributes. The user has no possibility to manually search, the search will be performed using the
     * constructor attributes.
     * 
     * @param ureq
     * @param wControl
     * @param userSearchFilter
     * @param showEmailButton
     */
    public UsermanagerUserSearchController(final UserRequest ureq, final WindowControl wControl, final UserSearchFilter userSearchFilter, final boolean showEmailButton) {
        super(ureq, wControl);

        mailVC = createVelocityContainer("usermanagerMail");

        backFromMail = LinkFactory.createLinkBack(mailVC, this);

        userListVC = createVelocityContainer("usermanagerUserlist");
        this.showEmailButton = showEmailButton;

        userListVC.contextPut("showBackButton", Boolean.FALSE);
        userListVC.contextPut("showTitle", Boolean.TRUE);

        identitiesList = getBaseSecurityEBL().searchUsers(userSearchFilter);

        initUserListCtr(ureq, identitiesList, userSearchFilter.status);
        userListVC.put("userlist", tableCtr.getInitialComponent());
        userListVC.contextPut("emptyList", (identitiesList.size() == 0 ? Boolean.TRUE : Boolean.FALSE));

        panel = putInitialPanel(userListVC);
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Constructor to trigger the user search workflow using the predefined list of identities. The user has no possibility to manually search.
     * 
     * @param ureq
     * @param wControl
     * @param identitiesList
     * @param status
     * @param showEmailButton
     */
    public UsermanagerUserSearchController(final UserRequest ureq, final WindowControl wControl, final List<Identity> identitiesList, final Integer status,
            final boolean showEmailButton, final boolean showTitle) {
        super(ureq, wControl);

        mailVC = createVelocityContainer("usermanagerMail");

        backFromMail = LinkFactory.createLinkBack(mailVC, this);

        userListVC = createVelocityContainer("usermanagerUserlist");
        this.showEmailButton = showEmailButton;

        userListVC.contextPut("showBackButton", Boolean.FALSE);
        userListVC.contextPut("showTitle", new Boolean(showTitle));

        initUserListCtr(ureq, identitiesList, status);
        userListVC.put("userlist", tableCtr.getInitialComponent());
        userListVC.contextPut("emptyList", (identitiesList.size() == 0 ? Boolean.TRUE : Boolean.FALSE));

        panel = putInitialPanel(userListVC);
    }

    /**
     * Remove the given identites from the list of identites in the table model and reinitialize the table controller
     * 
     * @param ureq
     * @param tobeRemovedIdentities
     */
    @Deprecated
    private void removeIdentitiesFromSearchResult(final UserRequest ureq, final List<Identity> tobeRemovedIdentities) {
        PersistenceHelper.removeObjectsFromList(identitiesList, tobeRemovedIdentities);
        initUserListCtr(ureq, identitiesList, null);
        userListVC.put("userlist", tableCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        if (source == backFromMail) {
            panel.setContent(userListVC);
        } else if (source == backFromList) {
            panel.setContent(userSearchVC);
        }
    }

    /**
     * Initialize the table controller using the list of identities
     * 
     * @param ureq
     * @param identitiesList
     */
    private void initUserListCtr(final UserRequest ureq, final List<Identity> myIdentities, final Integer searchStatusField) {
        boolean actionEnabled = true;
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
        if ((searchStatusField != null) && (searchStatusField.equals(Identity.STATUS_DELETED))) {
            actionEnabled = false;
        }
        tdm = ExtendedIdentitiesTableControllerFactory.createTableDataModel(ureq, myIdentities, actionEnabled);

        removeAsListenerAndDispose(tableCtr);
        tableCtr = ExtendedIdentitiesTableControllerFactory.createController(tdm, ureq, getWindowControl(), actionEnabled);
        listenTo(tableCtr);

        if (showEmailButton) {
            tableCtr.addMultiSelectAction("command.mail", CMD_MAIL);
        }
        if (actionEnabled) {
            tableCtr.addMultiSelectAction("action.bulkedit", CMD_BULKEDIT);
        }
        if (showEmailButton || actionEnabled) {
            tableCtr.setMultiSelect(true);
        }
    }

    /**
     * @return List of identities that match the criterias from the search form
     */
    private List<Identity> findIdentitiesFromSearchForm() {
        // get user attributes from form
        final String login = searchform.getStringValue("login");
        Integer status = null;

        // get user fields from form
        // build user fields search map
        Map<String, String> userPropertiesSearch = new HashMap<String, String>();
        for (final UserPropertyHandler userPropertyHandler : searchform.getPropertyHandlers()) {
            if (userPropertyHandler == null) {
                continue;
            }
            final FormItem ui = searchform.getItem(userPropertyHandler.getName());
            final String uiValue = userPropertyHandler.getStringValue(ui);
            if (StringHelper.containsNonWhitespace(uiValue)) {
                userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
            }
        }
        if (userPropertiesSearch.isEmpty()) {
            userPropertiesSearch = null;
        }

        // get group memberships from form
        Roles roles = getRolesFilter();

        status = searchform.getStatus();

        // no permissions in this form so far
        final PermissionOnResourceable[] permissionOnResources = null;

        final String[] authProviders = searchform.getAuthProviders();

        // get date constraints from form
        final Date createdBefore = searchform.getBeforeDate();
        final Date createdAfter = searchform.getAfterDate();
        final Date userLoginBefore = searchform.getUserLoginBefore();
        final Date userLoginAfter = searchform.getUserLoginAfter();

        UserSearchFilter userSearchFilter = new UserSearchFilter((login.equals("") ? null : login), userPropertiesSearch, roles, permissionOnResources, authProviders,
                createdAfter, createdBefore, userLoginAfter, userLoginBefore, status);

        // now perform power search
        final List<Identity> myIdentities = getBaseSecurityEBL().searchUsers(userSearchFilter);

        return myIdentities;
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private Roles getRolesFilter() {

        boolean isAdmin = false;
        boolean isAuthor = false;
        boolean isGroupManager = false;
        boolean isUserManager = false;
        boolean isOresManager = false;

        if (searchform.getRole("admin")) {
            isAdmin = true;
        }
        if (searchform.getRole("author")) {
            isAuthor = true;
        }
        if (searchform.getRole("groupmanager")) {
            isGroupManager = true;
        }
        if (searchform.getRole("usermanager")) {
            isUserManager = true;
        }
        if (searchform.getRole("oresmanager")) {
            isOresManager = true;
        }

        return new Roles(isAdmin, isUserManager, isGroupManager, isAuthor, false, isOresManager, false);
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchform) {
            if (event == Event.DONE_EVENT) {
                // form validation was ok
                identitiesList = findIdentitiesFromSearchForm();
                initUserListCtr(ureq, identitiesList, null);
                userListVC.put("userlist", tableCtr.getInitialComponent());
                userListVC.contextPut("emptyList", (identitiesList.size() == 0 ? Boolean.TRUE : Boolean.FALSE));
                panel.setContent(userListVC);
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(ExtendedIdentitiesTableControllerFactory.COMMAND_SELECTUSER)) {
                    final int rowid = te.getRowId();
                    foundIdentity = tdm.getIdentityAt(rowid);
                    // Tell parentController that a subject has been found
                    fireEvent(ureq, new SingleIdentityChosenEvent(foundIdentity));
                } else if (actionid.equals(ExtendedIdentitiesTableControllerFactory.COMMAND_VCARD)) {
                    // get identitiy and open new visiting card controller in new window
                    final int rowid = te.getRowId();
                    final Identity identity = tdm.getIdentityAt(rowid);
                    final ControllerCreator userInfoMainControllerCreator = new ControllerCreator() {
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
                }
            }
            if (event instanceof TableMultiSelectEvent) {
                // Multiselect events
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(CMD_BULKEDIT)) {
                    if (tmse.getSelection().isEmpty()) {
                        // empty selection
                        showWarning("msg.selectionempty");
                        return;
                    }
                    selectedIdentities = tdm.getIdentities(tmse.getSelection());
                    final UserBulkChangeManager ubcMan = UserBulkChangeManager.getInstance();
                    // valid selection: load in wizard
                    final Step start = new UserBulkChangeStep00(ureq, selectedIdentities);
                    final Roles roles = ureq.getUserSession().getRoles();
                    isAdministrativeUser = roles.isAdministrativeUser();

                    // callback executed in case wizard is finished.
                    final StepRunnerCallback finish = new StepRunnerCallback() {
                        public Step execute(final UserRequest ureq1, final WindowControl wControl1, final StepsRunContext runContext) {
                            // all information to do now is within the runContext saved
                            boolean hasChanges = false;
                            try {
                                if (runContext.containsKey("validChange") && ((Boolean) runContext.get("validChange")).booleanValue()) {
                                    final HashMap<String, String> attributeChangeMap = (HashMap<String, String>) runContext.get("attributeChangeMap");
                                    final HashMap<String, String> roleChangeMap = (HashMap<String, String>) runContext.get("roleChangeMap");
                                    if (!(attributeChangeMap.size() == 0 && roleChangeMap.size() == 0)) {
                                        ubcMan.changeSelectedIdentities(selectedIdentities, attributeChangeMap, roleChangeMap, notUpdatedIdentities,
                                                isAdministrativeUser, getTranslator());
                                        hasChanges = true;
                                    }
                                }
                            } catch (final Exception any) {
                                // return new ErrorStep
                            }
                            // signal correct completion and tell if changes were made or not.
                            return hasChanges ? StepsMainRunController.DONE_MODIFIED : StepsMainRunController.DONE_UNCHANGED;
                        }
                    };

                    removeAsListenerAndDispose(userBulkChangeStepsController);
                    userBulkChangeStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("bulkChange.title"));
                    listenTo(userBulkChangeStepsController);

                    getWindowControl().pushAsModalDialog(userBulkChangeStepsController.getInitialComponent());

                } else if (tmse.getAction().equals(CMD_MAIL)) {
                    if (tmse.getSelection().isEmpty()) {
                        // empty selection
                        showWarning("msg.selectionempty");
                        return;
                    }
                    // create e-mail message
                    final ContactMessage cmsg = new ContactMessage(ureq.getIdentity());

                    selectedIdentities = tdm.getIdentities(tmse.getSelection());
                    final ContactList contacts = new ContactList(translate("mailto.userlist"));
                    contacts.addAllIdentites(selectedIdentities);
                    cmsg.addEmailTo(contacts);

                    // create contact form controller with ContactMessage
                    removeAsListenerAndDispose(contactCtr);
                    contactCtr = new ContactFormController(ureq, getWindowControl(), false, true, false, false, cmsg);
                    listenTo(contactCtr);

                    mailVC.put("mailform", contactCtr.getInitialComponent());
                    panel.setContent(mailVC);
                }
            }
        } else if (source == contactCtr) {
            // in any case go back to list (events: done, failed or cancel)
            panel.setContent(userListVC);
        } else if (source == userBulkChangeStepsController) {
            if (event == Event.CANCELLED_EVENT) {
                getWindowControl().pop();
            } else if (event == Event.CHANGED_EVENT) {
                getWindowControl().pop();
                final Integer selIdentCount = selectedIdentities.size();
                if (notUpdatedIdentities.size() > 0) {
                    final Integer notUpdatedIdentCount = notUpdatedIdentities.size();
                    final Integer sucChanges = selIdentCount - notUpdatedIdentCount;
                    String changeErrors = "";
                    for (final String err : notUpdatedIdentities) {
                        changeErrors += err + "<br />";
                    }
                    getWindowControl().setError(translate("bulkChange.partialsuccess", new String[] { sucChanges.toString(), selIdentCount.toString(), changeErrors }));
                } else {
                    showInfo("bulkChange.success");
                }
                // update table model - has changed
                reloadDataModel(ureq);

            } else if (event == Event.DONE_EVENT) {
                showError("bulkChange.failed");
            }

        }
    }

    /**
     * Reload the currently used identitiesList and rebuild the table controller
     * 
     * @param ureq
     */
    private void reloadDataModel(final UserRequest ureq) {
        if (identitiesList == null) {
            return;
        }
        final BaseSecurity secMgr = getBaseSecurity();
        for (int i = 0; i < identitiesList.size(); i++) {
            final Identity ident = identitiesList.get(i);
            final Identity refrshed = secMgr.loadIdentityByKey(ident.getKey());
            identitiesList.set(i, refrshed);
        }
        initUserListCtr(ureq, identitiesList, null);
        userListVC.put("userlist", tableCtr.getInitialComponent());
    }

    /**
     * Reload the identity used currently in the workflow and in the currently activated user table list model. The identity will be reloaded from the database to have
     * accurate values.
     */
    public void reloadFoundIdentity() {
        if (foundIdentity == null) {
            throw new AssertException("reloadFoundIdentity called but foundIdentity is null");
        }
        // reload the found identity
        foundIdentity = getBaseSecurity().loadIdentityByKey(foundIdentity.getKey());
        // replace the found identity in the table list model to display changed
        // values
        final List identities = tdm.getObjects();
        PersistenceHelper.replaceObjectInListByKey(identities, foundIdentity);
    }

    /**
	 */
    protected void doDispose() {
        //
    }

}

/**
 * Initial Date: Jan 31, 2006
 * 
 * @author gnaegi Description: Search form for the usermanager power search. Should only be used by the UserManagerSearchController
 */
class UsermanagerUserSearchForm extends FormBasicController {
    private static final String formIdentifyer = UsermanagerUserSearchForm.class.getCanonicalName();
    private TextElement login;
    private SelectionElement roles;
    private SingleSelection status;
    private SelectionElement auth;
    private DateChooser beforeDate, afterDate, userLoginBefore, userLoginAfter;
    private FormLink searchButton;

    private final List<UserPropertyHandler> userPropertyHandlers;

    private final String[] statusKeys, statusValues;
    private final String[] roleKeys, roleValues;
    private final String[] authKeys, authValues;

    Map<String, FormItem> items;

    /**
     * @param name
     * @param cancelbutton
     */
    public UsermanagerUserSearchForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(formIdentifyer, true);

        items = new HashMap<String, FormItem>();

        roleKeys = new String[] { "admin", "author", "groupmanager", "usermanager", "oresmanager" };

        roleValues = new String[] { translate("search.form.constraint.admin"), translate("search.form.constraint.author"),
                translate("search.form.constraint.groupmanager"), translate("search.form.constraint.usermanager"), translate("search.form.constraint.oresmanager") };

        statusKeys = new String[] { Integer.toString(Identity.STATUS_VISIBLE_LIMIT), Integer.toString(Identity.STATUS_ACTIV),
                Integer.toString(Identity.STATUS_PERMANENT), Integer.toString(Identity.STATUS_LOGIN_DENIED) };
        statusValues = new String[] { translate("rightsForm.status.any.visible"), translate("rightsForm.status.activ"), translate("rightsForm.status.permanent"),
                translate("rightsForm.status.login_denied") };

        // take all providers from the config file
        // convention is that a translation key "search.form.constraint.auth." +
        // providerName
        // must exist. the element is stored using the name "auth." + providerName
        final List<String> authKeyList = new ArrayList<String>();
        final List<String> authValueList = new ArrayList<String>();

        final Collection<AuthenticationProvider> providers = LoginModule.getAuthenticationProviders();
        for (final AuthenticationProvider provider : providers) {
            if (provider.isEnabled()) {
                authKeyList.add(provider.getName());
                authValueList.add(translate("search.form.constraint.auth." + provider.getName()));
            }
        }
        if (WebDAVManager.getInstance().isEnabled()) {
            authKeyList.add(WebDAVAuthManager.PROVIDER_WEBDAV);
            authValueList.add(translate("search.form.constraint.auth.WEBDAV"));
        }

        // add additional no authentication element
        authKeyList.add("noAuth");
        authValueList.add(translate("search.form.constraint.auth.none"));

        authKeys = authKeyList.toArray(new String[authKeyList.size()]);
        authValues = authValueList.toArray(new String[authValueList.size()]);

        initForm(ureq);
    }

    public List<UserPropertyHandler> getPropertyHandlers() {
        return userPropertyHandlers;
    }

    protected Date getBeforeDate() {
        return beforeDate.getDate();
    }

    protected Date getAfterDate() {
        return afterDate.getDate();
    }

    protected Date getUserLoginBefore() {
        return userLoginBefore.getDate();
    }

    protected Date getUserLoginAfter() {
        return userLoginAfter.getDate();
    }

    protected FormItem getItem(final String name) {
        return items.get(name);
    }

    protected String getStringValue(final String key) {
        final FormItem f = items.get(key);
        if (f == null) {
            return null;
        }
        if (f instanceof TextElement) {
            return ((TextElement) f).getValue();
        }
        return null;
    }

    protected boolean getRole(final String key) {
        return roles.isSelected(Arrays.asList(roleKeys).indexOf(key));
    }

    protected Integer getStatus() {
        return new Integer(status.getSelectedKey());
    }

    protected String[] getAuthProviders() {
        final List<String> apl = new ArrayList<String>();
        for (int i = 0; i < authKeys.length; i++) {
            if (auth.isSelected(i)) {
                if ("noAuth".equals(authKeys[i])) {
                    apl.add(null);// special case
                } else {
                    apl.add(authKeys[i]);
                }
            }
        }
        return apl.toArray(new String[apl.size()]);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);
        items.put("login", login);

        final Translator tr = PackageUtil.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator());

        String currentGroup = null;
        // Add all available user fields to this form
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            if (userPropertyHandler == null) {
                continue;
            }

            final FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, getClass().getCanonicalName(), false, formLayout);

            fi.setTranslator(tr);
            items.put(fi.getName(), fi);

            final String group = userPropertyHandler.getGroup();
            if (!group.equals(currentGroup)) {
                if (currentGroup != null) {
                    uifactory.addSpacerElement("spacer_" + group, formLayout, false);
                }
                currentGroup = group;
            }
        }

        uifactory.addSpacerElement("space1", formLayout, false);
        roles = uifactory.addCheckboxesVertical("roles", "search.form.title.roles", formLayout, roleKeys, roleValues, null, 1);

        uifactory.addSpacerElement("space2", formLayout, false);
        auth = uifactory.addCheckboxesVertical("auth", "search.form.title.authentications", formLayout, authKeys, authValues, null, 1);

        uifactory.addSpacerElement("space3", formLayout, false);
        status = uifactory.addRadiosVertical("status", "search.form.title.status", formLayout, statusKeys, statusValues);
        status.select(statusKeys[0], true);

        uifactory.addSpacerElement("space4", formLayout, false);
        afterDate = uifactory.addDateChooser("search.form.afterDate", "", formLayout);
        afterDate.setValidDateCheck("error.search.form.no.valid.datechooser");
        beforeDate = uifactory.addDateChooser("search.form.beforeDate", "", formLayout);
        beforeDate.setValidDateCheck("error.search.form.no.valid.datechooser");

        uifactory.addSpacerElement("space5", formLayout, false);
        userLoginAfter = uifactory.addDateChooser("search.form.userLoginAfterDate", "", formLayout);
        userLoginAfter.setValidDateCheck("error.search.form.no.valid.datechooser");
        userLoginBefore = uifactory.addDateChooser("search.form.userLoginBeforeDate", "", formLayout);
        userLoginBefore.setValidDateCheck("error.search.form.no.valid.datechooser");

        // creation date constraints
        /*
         * addFormElement("space3", new SpacerElement(true, false)); addFormElement("title.date", new TitleElement("search.form.title.date")); afterDate = new
         * DateElement("search.form.afterDate", getLocale()); addFormElement("afterDate", afterDate); beforeDate = new DateElement("search.form.beforeDate", getLocale());
         * addFormElement("beforeDate", beforeDate); addSubmitKey("submit.search", "submit.search");
         */

        uifactory.addSpacerElement("spaceBottom", formLayout, false);

        // Don't use submit button, form should not be marked as dirty since this is
        // not a configuration form but only a search form (OLAT-5626)
        searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
        searchButton.addActionListener(this, FormEvent.ONCLICK);

    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    @SuppressWarnings("unused")
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == searchButton) {
            source.getRootForm().submit(ureq);
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
