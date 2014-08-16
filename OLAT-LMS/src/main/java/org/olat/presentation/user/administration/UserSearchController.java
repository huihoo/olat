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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.events.MultiIdentityChosenEvent;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
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
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.AutoCompleterController;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.ListProvider;
import org.olat.presentation.framework.core.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.presentation.framework.core.control.state.ControllerState;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 29, 2003
 * 
 * @author Felix Jost, Florian Gnaegi
 * 
 *         <pre>
 * Comment:  
 * Subworkflow that allows the user to search for a user and choose the user from 
 * the list of users that match the search criteria. Users can be searched by
 * <ul>
 * <li />
 * Username
 * <li />
 * First name
 * <li />
 * Last name
 * <li />
 * Email address
 * </ul>
 * 
 * </pre>
 * 
 *         Events:<br>
 *         Fires a SingleIdentityChoosenEvent when an identity has been chosen which contains the choosen identity<br>
 *         Fires a MultiIdentityChoosenEvent when multiples identities have been chosen which contains the choosen identities<br>
 *         <p>
 *         Optionally set the useMultiSelect boolean to true which allows to select multiple identities from within the search results.
 */
public class UserSearchController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    // Needs PACKAGE and VELOCITY_ROOT because DeletableUserSearchController extends UserSearchController and re-use translations
    private static final String PACKAGE = UserSearchController.class.getPackage().getName();
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
    private static final String ACTION_MULTISELECT_CHOOSE = "msc";

    private final VelocityContainer myContent;
    private final Panel searchPanel;
    private final UserSearchForm searchform;
    private TableController tableCtr;
    private final TableGuiConfiguration tableConfig;
    private UserTableDataModel tdm;
    private List<Identity> foundIdentities = new ArrayList<Identity>();
    private boolean useMultiSelect = false;

    private AutoCompleterController autocompleterC;
    private String actionKeyChoose;
    private Map<String, String> userPropertiesSearch;
    private final boolean isAdministrativeUser;
    private final Link backLink;

    private static final String STATE_SEARCHFORM = "searchform";
    private static final String STATE_RESULTS = "results";

    public static final String ACTION_KEY_CHOOSE = "action.choose";
    public static final String ACTION_KEY_CHOOSE_FINISH = "action.choose.finish";

    /**
     * @param ureq
     * @param wControl
     * @param cancelbutton
     */
    public UserSearchController(final UserRequest ureq, final WindowControl wControl, final boolean cancelbutton) {
        this(ureq, wControl, cancelbutton, false, false);
    }

    /**
     * @param ureq
     * @param windowControl
     * @param cancelbutton
     * @param userMultiSelect
     * @param statusEnabled
     * @param actionKeyChooseFinish
     */
    public UserSearchController(final UserRequest ureq, final WindowControl windowControl, final boolean cancelbutton, final boolean userMultiSelect,
            final boolean statusEnabled, final String actionKeyChooseFinish) {
        this(ureq, windowControl, cancelbutton, userMultiSelect, statusEnabled);
        this.actionKeyChoose = actionKeyChooseFinish;
    }

    /**
     * @param ureq
     * @param wControl
     * @param cancelbutton
     * @param userMultiSelect
     * @param statusEnabled
     */
    public UserSearchController(final UserRequest ureq, final WindowControl wControl, final boolean cancelbutton, final boolean userMultiSelect,
            final boolean statusEnabled) {
        super(ureq, wControl);
        this.useMultiSelect = userMultiSelect;
        this.actionKeyChoose = ACTION_KEY_CHOOSE;
        // Needs PACKAGE and VELOCITY_ROOT because DeletableUserSearchController extends UserSearchController and re-use translations
        final Translator pT = getUserService().getUserPropertiesConfig().getTranslator(new PackageTranslator(PACKAGE, ureq.getLocale()));
        myContent = new VelocityContainer("olatusersearch", VELOCITY_ROOT + "/usersearch.html", pT, this);
        backLink = LinkFactory.createButton("btn.back", myContent, this);

        searchPanel = new Panel("usersearchPanel");
        searchPanel.addListener(this);
        myContent.put("usersearchPanel", searchPanel);

        if (ureq.getUserSession() == null) {
            log.error("UserSearchController<init>: session is null!", null);
        } else if (ureq.getUserSession().getRoles() == null) {
            log.error("UserSearchController<init>: roles is null!", null);
        }
        final boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin();

        searchform = new UserSearchForm(ureq, wControl, isAdmin, cancelbutton);
        listenTo(searchform);

        searchPanel.setContent(searchform.getInitialComponent());

        myContent.contextPut("noList", "false");
        myContent.contextPut("showButton", "false");

        final boolean ajax = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
        final Locale loc = ureq.getLocale();
        if (ajax) {
            // insert a autocompleter search
            final ListProvider provider = new ListProvider() {
                /**
                 * org.olat.presentation.framework.control.generic.ajax.autocompletion.ListReceiver)
                 */
                @Override
                public void getResult(final String searchValue, final ListReceiver receiver) {
                    final Map<String, String> userProperties = getAnyUserPropertiesSearchFilter(searchValue);
                    // Search in all fileds -> non intersection search
                    final List<Identity> res = searchUsers(searchValue, userProperties, false);
                    int maxEntries = 15;
                    boolean hasMore = false;
                    for (final Iterator<Identity> it_res = res.iterator(); (hasMore = it_res.hasNext()) && maxEntries > 0;) {
                        maxEntries--;
                        final Identity ident = it_res.next();
                        final User u = ident.getUser();
                        final String key = ident.getKey().toString();
                        final String displayKey = ident.getName();
                        final String first = getUserService().getUserProperty(u, UserConstants.FIRSTNAME, loc);
                        final String last = getUserService().getUserProperty(u, UserConstants.LASTNAME, loc);
                        final String displayText = last + " " + first;
                        receiver.addEntry(key, displayKey, displayText, CSSHelper.CSS_CLASS_USER);
                    }
                    if (hasMore) {
                        receiver.addEntry(".....", ".....");
                    }
                }

                /**
                 * @param searchValue
                 * @return
                 */
                private Map<String, String> getAnyUserPropertiesSearchFilter(final String searchValue) {
                    final Map<String, String> userProperties = new HashMap<String, String>();
                    // We can only search in mandatory User-Properties due to problems
                    // with hibernate query with join and not existing rows
                    userProperties.put(UserConstants.FIRSTNAME, searchValue);
                    userProperties.put(UserConstants.LASTNAME, searchValue);
                    userProperties.put(UserConstants.EMAIL, searchValue);
                    return userProperties;
                }
            };
            autocompleterC = new AutoCompleterController(ureq, getWindowControl(), provider, null, isAdmin, 60, 3, null);
            listenTo(autocompleterC);
            myContent.put("autocompletionsearch", autocompleterC.getInitialComponent());
        }

        tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
        tableConfig.setDownloadOffered(false);// no download because user should not download user-list
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myContent.getTranslator());
        listenTo(tableCtr);

        isAdministrativeUser = ureq.getUserSession().getRoles().isAdministrativeUser();

        putInitialPanel(myContent);
        setState(STATE_SEARCHFORM);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == backLink) {
            myContent.contextPut("noList", "false");
            myContent.contextPut("showButton", "false");
            searchPanel.popContent();
            setState(STATE_SEARCHFORM);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
                    final int rowid = te.getRowId();
                    final Identity foundIdentity = (Identity) tdm.getObject(rowid);
                    foundIdentities.add(foundIdentity);
                    // Tell parentController that a subject has been found
                    fireEvent(ureq, new SingleIdentityChosenEvent(foundIdentity));
                }
            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
                    foundIdentities = tdm.getObjects(tmse.getSelection());
                    fireEvent(ureq, new MultiIdentityChosenEvent(foundIdentities));
                }
            }
        } else if (source == autocompleterC) {
            final EntriesChosenEvent ece = (EntriesChosenEvent) event;
            final List res = ece.getEntries();
            // if we get the event, we have a result or an incorrect selection see OLAT-5114 -> check for empty
            final String mySel = res.isEmpty() ? null : (String) res.get(0);
            if ((mySel == null) || mySel.trim().equals("")) {
                getWindowControl().setWarning(translate("error.search.form.notempty"));
                return;
            }
            Long key = -1l; // default not found
            try {
                key = Long.valueOf(mySel);
                if (key > 0) {
                    final Identity chosenIdent = getBaseSecurity().loadIdentityByKey(key);
                    // No need to check for null, exception is thrown when identity does not exist which really
                    // should not happen at all.
                    // Tell that an identity has been chosen
                    fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
                }
            } catch (final NumberFormatException e) {
                getWindowControl().setWarning(translate("error.no.user.found"));
                return;
            }
        } else if (source == searchform) {
            if (event == Event.DONE_EVENT) {
                // form validation was ok
                tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myContent.getTranslator());
                listenTo(tableCtr);

                final String login = searchform.login.getValue();
                // build user fields search map
                Map<String, String> userPropertiesSearch = getUserPropertiesSearchFilter();

                final List<Identity> users = searchUsers(login, userPropertiesSearch, true);
                if (!users.isEmpty()) {
                    tdm = new UserTableDataModel(users, ureq.getLocale(), isAdministrativeUser);
                    // add the data column descriptors
                    tdm.addColumnDescriptors(tableCtr, null);
                    // add the action columns
                    if (useMultiSelect) {
                        // add multiselect action
                        tableCtr.addMultiSelectAction(this.actionKeyChoose, ACTION_MULTISELECT_CHOOSE);
                    } else {
                        // add single column selec action
                        tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent.getTranslator().translate(
                                "action.choose")));
                    }
                    tableCtr.setTableDataModel(tdm);
                    tableCtr.setMultiSelect(useMultiSelect);
                    searchPanel.pushContent(tableCtr.getInitialComponent());
                    myContent.contextPut("showButton", "true");
                    setState(STATE_RESULTS);
                } else {
                    getWindowControl().setInfo(translate("error.no.user.found"));
                    setState(STATE_SEARCHFORM);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }

    }

    /**
     * @return
     */
    private Map<String, String> getUserPropertiesSearchFilter() {
        Map<String, String> userPropertiesSearch = new HashMap<String, String>();
        for (final UserPropertyHandler userPropertyHandler : searchform.userPropertyHandlers) {
            if (userPropertyHandler == null) {
                continue;
            }
            final FormItem ui = searchform.propFormItems.get(userPropertyHandler.getName());
            final String uiValue = userPropertyHandler.getStringValue(ui);
            if (StringHelper.containsNonWhitespace(uiValue)) {
                userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
                log.info("Search property:" + userPropertyHandler.getName() + "=" + uiValue);
            }
        }
        if (userPropertiesSearch.isEmpty()) {
            userPropertiesSearch = null;
        }
        return userPropertiesSearch;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    @Override
    protected void adjustState(final ControllerState cstate, final UserRequest ureq) {
        final String state = cstate.getSerializedState();
        if (state.equals(STATE_SEARCHFORM)) {
            // we should and can adjust to the searchform
            searchPanel.popContent();
            setState(STATE_SEARCHFORM);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Child controllers auto-disposed by basic controller
    }

    /**
     * Can be overwritten by subclassen to search other users or filter users.
     * 
     * @param login
     * @param userPropertiesSearch
     * @return
     */
    protected List<Identity> searchUsers(final String login, final Map<String, String> userPropertiesSearch, final boolean userPropertiesAsIntersectionSearch) {
        return getBaseSecurityEBL().searchUsers(login, userPropertiesSearch, userPropertiesAsIntersectionSearch);
    }

    protected BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}

/**
 * <pre>
 * 
 * Initial Date:  Jul 29, 2003
 * 
 * @author gnaegi
 * 
 * Comment:  
 * The user search form
 * </pre>
 */
class UserSearchForm extends FormBasicController {

    private final boolean isAdmin, cancelButton;
    private FormLink searchButton;

    protected TextElement login;
    protected List<UserPropertyHandler> userPropertyHandlers;
    protected Map<String, FormItem> propFormItems;

    /**
     * @param name
     * @param cancelbutton
     * @param isAdmin
     *            if true, no field must be filled in at all, otherwise validation takes place
     */

    public UserSearchForm(final UserRequest ureq, final WindowControl wControl, final boolean isAdmin, final boolean cancelButton) {
        super(ureq, wControl);

        this.isAdmin = isAdmin;
        this.cancelButton = cancelButton;

        initForm(ureq);
    }

    @Override
    @SuppressWarnings("unused")
    public boolean validateFormLogic(final UserRequest ureq) {
        // override for admins
        if (isAdmin) {
            return true;
        }

        boolean filled = !login.isEmpty();
        final StringBuffer full = new StringBuffer(login.getValue().trim());
        FormItem lastFormElement = login;

        // DO NOT validate each user field => see OLAT-3324
        // this are custom fields in a Search Form
        // the same validation logic can not be applied
        // i.e. email must be searchable and not about getting an error like
        // "this e-mail exists already"
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            final FormItem ui = propFormItems.get(userPropertyHandler.getName());
            final String uiValue = userPropertyHandler.getStringValue(ui);
            // add value for later non-empty search check
            if (StringHelper.containsNonWhitespace(uiValue)) {
                full.append(uiValue.trim());
                filled = true;
            } else {
                // its an empty field
                filled = filled || false;
            }
            lastFormElement = ui;
        }

        // Don't allow searches with * or % or @ chars only (wild cards). We don't want
        // users to get a complete list of all OLAT users this easily.
        final String fullString = full.toString();
        final boolean onlyStar = fullString.matches("^[\\*\\s@\\%]*$");

        if (!filled || onlyStar) {
            // set the error message
            lastFormElement.setErrorKey("error.search.form.notempty", null);
            return false;
        }
        if (fullString.contains("**")) {
            lastFormElement.setErrorKey("error.search.form.no.wildcard.dublicates", null);
            return false;
        }
        final int MIN_LENGTH = 4;
        if (fullString.length() < MIN_LENGTH) {
            lastFormElement.setErrorKey("error.search.form.to.short", null);
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);

        final Translator tr = PackageUtil.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator());

        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(getClass().getCanonicalName(), isAdmin);

        propFormItems = new HashMap<String, FormItem>();
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            if (userPropertyHandler == null) {
                continue;
            }

            final FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, getClass().getCanonicalName(), false, formLayout);
            fi.setTranslator(tr);

            propFormItems.put(userPropertyHandler.getName(), fi);
        }

        final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);

        // Don't use submit button, form should not be marked as dirty since this is
        // not a configuration form but only a search form (OLAT-5626)
        searchButton = uifactory.addFormLink("submit.search", buttonGroupLayout, Link.BUTTON);
        searchButton.addActionListener(this, FormEvent.ONCLICK);
        if (cancelButton) {
            uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
        }
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, @SuppressWarnings("unused") final FormEvent event) {
        if (source == searchButton) {
            source.getRootForm().submit(ureq);
        }
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
