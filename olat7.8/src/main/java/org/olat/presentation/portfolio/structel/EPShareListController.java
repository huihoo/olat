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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.portfolio.structel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.Policy;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.EPMapPolicy;
import org.olat.lms.portfolio.EPMapPolicy.Type;
import org.olat.lms.portfolio.EmailParameterObject;
import org.olat.lms.portfolio.PortfolioDataEBL;
import org.olat.lms.portfolio.PortfolioEBL;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextBoxListElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.textboxlist.ResultMapProvider;
import org.olat.presentation.framework.core.components.textboxlist.TextBoxListComponent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.mail.MailHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Manage the list of share policies
 * <P>
 * Initial Date: 4 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPShareListController extends FormBasicController {

    private final List<PolicyWrapper> policyWrappers = new ArrayList<PolicyWrapper>();
    private final PortfolioStructureMap portfolioStrukturMap;
    private final EPFrontendManager ePFMgr;
    private final BusinessGroupService businessGroupService;
    private final String[] targetKeys = EPMapPolicy.Type.names();
    private final String[] targetValues = new String[targetKeys.length];
    protected final List<BusinessGroup> groupList = new ArrayList<BusinessGroup>();

    private FormLink addPolicyButton;
    private PortfolioEBL portfolioEbl;

    public EPShareListController(final UserRequest ureq, final WindowControl wControl, final PortfolioStructureMap map) {
        super(ureq, wControl, "shareList");

        this.portfolioStrukturMap = map;
        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);
        portfolioEbl = CoreSpringFactory.getBean(PortfolioEBL.class);
        for (int i = targetKeys.length; i-- > 0;) {
            targetValues[i] = translate("map.share.to." + targetKeys[i]);
        }
        for (final EPMapPolicy policy : ePFMgr.getMapPolicies(map)) {
            policyWrappers.add(new PolicyWrapper(policy));
        }
        initForm(ureq);
    }

    @SuppressWarnings("unused")
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        addPolicyButton = uifactory.addFormLink("map.share.add.policy", flc, Link.BUTTON);

        updateUI();

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok_cancel", getTranslator());
        buttonLayout.setRootForm(mainForm);
        uifactory.addFormSubmitButton("ok", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
        flc.add("ok_cancel", buttonLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean allOk = true;
        applyValues();
        String genericError = null;

        for (final PolicyWrapper policyWrapper : policyWrappers) {
            final Type type = policyWrapper.getType();
            final TextElement mailEl = policyWrapper.getMailEl();
            if (mailEl != null) {
                final String mail = mailEl.getValue();
                if (StringHelper.containsNonWhitespace(mail)) {
                    if (MailHelper.isValidEmailAddress(mail)) {
                        if (getBaseSecurityEBL().isEmailAdressAlreadyUsed(mail)) {
                            mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[] { mail });
                            allOk &= false;
                        }
                    } else {
                        mailEl.setErrorKey("map.share.with.mail.error", null);
                        allOk &= false;
                    }
                } else if (type.equals(Type.invitation)) {
                    genericError = translate("map.share.error.invite");
                    allOk &= false;
                }
            } else if (type.equals(Type.group)) {
                final List<BusinessGroup> groups = policyWrapper.getGroups();
                if (groups.size() == 0) {
                    genericError = translate("map.share.error.group");
                    allOk &= false;
                }
            } else if (type.equals(Type.user)) {
                final List<Identity> idents = policyWrapper.getIdentities();
                if (idents.size() == 0) {
                    genericError = translate("map.share.error.user");
                    allOk &= false;
                }
            }
            if (policyWrapper.getFromChooser().hasError() || policyWrapper.getToChooser().hasError()) {
                genericError = translate("map.share.date.invalid");
                allOk &= false;
            }
            if (policyWrapper.getFrom() != null && policyWrapper.getTo() != null && policyWrapper.getFrom().after(policyWrapper.getTo())) {
                // show invalid date warning
                policyWrapper.getFromChooser().setErrorKey("from.date.behind.to", null);
                policyWrapper.getFromChooser().showError(true);

                genericError = translate("from.date.behind.to");
                allOk &= false;
            }
            final FormLayoutContainer cmp = (FormLayoutContainer) flc.getFormComponent(policyWrapper.getComponentName());
            final String errorCompName = policyWrapper.calc("errorpanel");
            final StaticTextElement errTextEl = (StaticTextElement) cmp.getFormComponent(errorCompName);
            if (genericError != null) {
                errTextEl.setValue(genericError);
            }
        }

        return allOk && super.validateFormLogic(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        applyValues();
        final List<EPMapPolicy> mapPolicies = new ArrayList<EPMapPolicy>();
        for (final PolicyWrapper wrapper : policyWrappers) {
            mapPolicies.add(wrapper.getMapPolicy());
            if (wrapper.getType().equals(EPMapPolicy.Type.invitation)) {
                // always send an invitation mail for invited-non-olat users
                sendInvitation(ureq.getLocale(), wrapper);
            }
        }
        ePFMgr.updateMapPolicies(portfolioStrukturMap, mapPolicies);
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        applyValues();
        // dont allow any manipulation as long as errors exist!! else some wrong
        // policy might be persisted. check with validateFormLogic()
        if (source == addPolicyButton) {
            if (validateFormLogic(ureq)) {
                addPolicyWrapper(null);
                updateUI();
            }
        } else if (source instanceof FormLink && source.getUserObject() instanceof PolicyWrapper) {
            final FormLink link = (FormLink) source;
            final PolicyWrapper wrapper = (PolicyWrapper) link.getUserObject();
            if (link.getName().startsWith("map.share.policy.add")) {
                if (validateFormLogic(ureq)) {
                    addPolicyWrapper(wrapper);
                    updateUI();
                }
            } else if (link.getName().startsWith("map.share.policy.delete")) {
                removePolicyWrapper(wrapper);
                updateUI();
            } else if (link.getName().startsWith("map.share.policy.invite")) {
                if (validateFormLogic(ureq)) {
                    sendInvitation(ureq.getLocale(), wrapper);
                    updateUI();
                }
            }
        } else if (source instanceof SingleSelection && source.getUserObject() instanceof PolicyWrapper) {
            final SingleSelection selection = (SingleSelection) source;
            if (selection.isOneSelected()) {
                final String type = selection.getSelectedKey();
                final PolicyWrapper wrapper = (PolicyWrapper) selection.getUserObject();
                changeType(wrapper, type);
            }
            updateUI();
        }
    }

    // send a link to the map to permitted users by email
    private void sendInvitation(final Locale locale, final PolicyWrapper wrapper) {
        String mailSubject = translate("map.share.invitation.mail.subject");
        String mailBodyText = translate("map.share.invitation.mail.body", createMailBodyArguments(wrapper));
        EmailParameterObject emailParameterObject = new EmailParameterObject(locale, wrapper.getMapPolicy(), mailSubject, mailBodyText,
                translate("map.share.invitation.mail.list"), wrapper.isInvitationSend());
        PortfolioDataEBL portfolioDataEBL = portfolioEbl.sendMail(emailParameterObject);
        wrapper.setInvitationSend(portfolioDataEBL.isInvitationSend());
    }

    private String[] createMailBodyArguments(PolicyWrapper wrapper) {
        final String sender = getUserService().getFirstAndLastname(getIdentity().getUser());
        String busLink = createBusinessLink(wrapper.getInvitation(), portfolioStrukturMap);
        return new String[] { busLink, sender };
    }

    /**
     * @param invitation
     * @return
     */
    private String createBusinessLink(Invitation invitation, PortfolioStructureMap portfolioStrukturMap) {
        String busLink;
        if (invitation != null) {
            busLink = getInvitationLink(invitation, portfolioStrukturMap);
        } else {
            final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
            final ContextEntry mapCE = bCF.createContextEntry(portfolioStrukturMap.getOlatResource());
            final ArrayList<ContextEntry> cEList = new ArrayList<ContextEntry>();
            cEList.add(mapCE);
            busLink = bCF.getAsURIString(cEList, true);
        }

        String mapTitle = portfolioStrukturMap.getTitle();
        return Formatter.getHtmlHref(busLink, mapTitle);
    }

    // writes GUI element data of map share rule elements to underlying model
    private void applyValues() {
        for (final PolicyWrapper policyWrapper : policyWrappers) {
            // user
            final TextBoxListElement userList = policyWrapper.getUserListBox();
            if (userList != null) {
                final List<String> values = userList.getValueList();
                final List<Identity> identities = new ArrayList<Identity>();
                for (final String value : values) {
                    final Identity id = getIdentityByLogin(value);
                    if (id != null) {
                        identities.add(id);
                    }
                }
                policyWrapper.setIdentities(identities);
            }

            // group
            final TextBoxListElement groupListBox = policyWrapper.getGroupListBox();
            if (groupListBox != null) {
                final List<String> values = groupListBox.getValueList();
                final List<BusinessGroup> selectedGroups = new ArrayList<BusinessGroup>();
                for (final BusinessGroup group : groupList) {
                    if (values.contains(group.getKey().toString())) {
                        selectedGroups.add(group);
                    }
                }
                policyWrapper.setGroups(selectedGroups);
            }

            // external user
            final TextElement firstNameEl = policyWrapper.getFirstNameEl();
            if (firstNameEl != null) {
                policyWrapper.getInvitation().setFirstName(firstNameEl.getValue());
            }
            final TextElement lastNameEl = policyWrapper.getLastNameEl();
            if (lastNameEl != null) {
                policyWrapper.getInvitation().setLastName(lastNameEl.getValue());
            }
            final TextElement mailEl = policyWrapper.getMailEl();
            if (mailEl != null) {
                policyWrapper.getInvitation().setMail(mailEl.getValue());
            }

            // generic
            policyWrapper.setFrom(policyWrapper.getFromChooser().getDate());
            policyWrapper.setTo(policyWrapper.getToChooser().getDate());
        }
    }

    private void updateUI() {
        final String template = PackageUtil.getPackageVelocityRoot(this.getClass()) + "/sharePolicy.html";

        for (final PolicyWrapper policyWrapper : policyWrappers) {
            String cmpName = policyWrapper.getComponentName();
            if (cmpName != null && flc.getFormComponent(cmpName) != null) {
                flc.remove(cmpName);
            }

            cmpName = UUID.randomUUID().toString();
            policyWrapper.setComponentName(cmpName);
            final FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout(cmpName, getTranslator(), template);
            container.contextPut("wrapper", policyWrapper);
            container.setRootForm(mainForm);

            final SingleSelection type = uifactory.addDropdownSingleselect("map.share.target." + cmpName, "map.share.target", container, targetKeys, targetValues, null);
            type.addActionListener(this, FormEvent.ONCHANGE);
            type.setUserObject(policyWrapper);
            if (policyWrapper.getType() != null) {
                type.select(policyWrapper.getType().name(), true);
                switch (policyWrapper.getType()) {
                case user:
                    final Map<String, String> initialUsers = policyWrapper.getIdentitiesValue();
                    final TextBoxListElement userListBox = uifactory.addTextBoxListElement("map.share.with." + cmpName, "map.share.to.user", "map.share.to.user.hint",
                            initialUsers, container, getTranslator());
                    userListBox.setNoFormSubmit(true);
                    userListBox.addActionListener(this, FormEvent.ONCHANGE);
                    userListBox.setUserObject(policyWrapper);
                    ((TextBoxListComponent) userListBox.getComponent()).setMapperProvider(new UserMapperProvider());
                    ((TextBoxListComponent) userListBox.getComponent()).setAllowNewValues(false);
                    ((TextBoxListComponent) userListBox.getComponent()).setAllowDuplicates(false);
                    ((TextBoxListComponent) userListBox.getComponent()).setMaxResults(15);
                    policyWrapper.setUserListBox(userListBox);
                    break;
                case group:
                    final Map<String, String> initialGroups = policyWrapper.getGroupsValues();
                    final TextBoxListElement groupListBox = uifactory.addTextBoxListElement("map.share.with." + cmpName, "map.share.to.group", "map.share.to.group.hint",
                            initialGroups, container, getTranslator());
                    groupListBox.setNoFormSubmit(true);
                    groupListBox.addActionListener(this, FormEvent.ONCHANGE);
                    groupListBox.setUserObject(policyWrapper);
                    ((TextBoxListComponent) groupListBox.getComponent()).setMapperProvider(new GroupMapperProvider());
                    ((TextBoxListComponent) groupListBox.getComponent()).setAllowNewValues(false);
                    ((TextBoxListComponent) groupListBox.getComponent()).setAllowDuplicates(false);
                    ((TextBoxListComponent) groupListBox.getComponent()).setMaxResults(15);
                    policyWrapper.setGroupListBox(groupListBox);
                    break;
                case invitation:
                    Invitation invitation = portfolioEbl.createAndPersistInvitationWhenNoOneExits(policyWrapper.getMapPolicy());
                    final FormLayoutContainer invitationContainer = FormLayoutContainer.createDefaultFormLayout("map.share.with." + cmpName, getTranslator());
                    invitationContainer.contextPut("wrapper", policyWrapper);
                    invitationContainer.setRootForm(mainForm);
                    container.add("map.share.with." + cmpName, invitationContainer);
                    uifactory.addSpacerElement("map.share.with.spacer." + cmpName, invitationContainer, true);

                    final TextElement firstNameEl = uifactory.addTextElement("map.share.with.firstName." + cmpName, "map.share.with.firstName", 64,
                            invitation.getFirstName(), invitationContainer);
                    firstNameEl.setMandatory(true);
                    firstNameEl.setNotEmptyCheck("map.share.empty.warn");
                    final TextElement lastNameEl = uifactory.addTextElement("map.share.with.lastName." + cmpName, "map.share.with.lastName", 64,
                            invitation.getLastName(), invitationContainer);
                    lastNameEl.setMandatory(true);
                    lastNameEl.setNotEmptyCheck("map.share.empty.warn");
                    final TextElement mailEl = uifactory.addTextElement("map.share.with.mail." + cmpName, "map.share.with.mail", 128, invitation.getMail(),
                            invitationContainer);
                    mailEl.setMandatory(true);
                    mailEl.setNotEmptyCheck("map.share.empty.warn");

                    if (getBaseSecurityEBL().isEmailAdressAlreadyUsed(invitation.getMail())) {
                        mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[] { invitation.getMail() });
                    }

                    policyWrapper.setFirstNameEl(firstNameEl);
                    policyWrapper.setLastNameEl(lastNameEl);
                    policyWrapper.setMailEl(mailEl);

                    final String link = getInvitationLink(invitation, portfolioStrukturMap);
                    final StaticTextElement linkEl = uifactory.addStaticTextElementWithTrustedText("map.share.with.link." + cmpName, link, invitationContainer);
                    linkEl.setLabel("map.share.with.link", null);
                    break;
                case allusers:
                    final String text = translate("map.share.with.allOlatUsers");
                    uifactory.addStaticTextElement("map.share.with." + cmpName, text, container);
                    break;
                }
            }

            final DateChooser fromChooser = uifactory.addDateChooser("map.share.from." + cmpName, "map.share.from", "", container);
            fromChooser.setDate(policyWrapper.getFrom());
            fromChooser.setValidDateCheck("map.share.date.invalid");
            policyWrapper.setFromChooser(fromChooser);
            final DateChooser toChooser = uifactory.addDateChooser("map.share.to." + cmpName, "map.share.to", "", container);
            toChooser.setDate(policyWrapper.getTo());
            toChooser.setValidDateCheck("map.share.date.invalid");
            policyWrapper.setToChooser(toChooser);

            final FormLink addLink = uifactory.addFormLink("map.share.policy.add." + cmpName, "map.share.policy.add", null, container, Link.BUTTON_SMALL);
            addLink.setUserObject(policyWrapper);
            final FormLink removeLink = uifactory.addFormLink("map.share.policy.delete." + cmpName, "map.share.policy.delete", null, container, Link.BUTTON_SMALL);
            removeLink.setUserObject(policyWrapper);
            if (!policyWrapper.getType().equals(EPMapPolicy.Type.allusers)) {
                final FormLink inviteLink = uifactory.addFormLink("map.share.policy.invite." + cmpName, "map.share.policy.invite", null, container, Link.BUTTON_XSMALL);
                inviteLink.setUserObject(policyWrapper);
                inviteLink.setEnabled(!policyWrapper.isInvitationSend());
            }
            final StaticTextElement genErrorPanel = uifactory.addStaticTextElement("errorpanel." + cmpName, "", container);
            genErrorPanel.setUserObject(policyWrapper);

            policyWrapper.setComponentName(cmpName);

            flc.add(container);
            flc.contextPut("wrapper", policyWrapper);
        }
        flc.contextPut("wrappers", policyWrappers);
    }

    private String getInvitationLink(final Invitation invitation, final PortfolioStructure theMap) {
        return Settings.getServerContextPathURI() + "/url/MapInvitation/" + theMap.getKey() + "?invitation=" + invitation.getToken();
    }

    protected void changeType(final PolicyWrapper wrapper, final String type) {
        wrapper.setType(EPMapPolicy.Type.valueOf(type));
    }

    protected void removePolicyWrapper(final PolicyWrapper wrapper) {
        policyWrappers.remove(wrapper);
        flc.remove(wrapper.getComponentName());
    }

    protected void addPolicyWrapper(final PolicyWrapper wrapper) {
        if (wrapper == null) {
            policyWrappers.add(new PolicyWrapper());
        } else {
            final int index = policyWrappers.indexOf(wrapper);
            if (index + 1 >= policyWrappers.size()) {
                policyWrappers.add(new PolicyWrapper());
            } else {
                policyWrappers.add(index + 1, new PolicyWrapper());
            }
        }
    }

    protected Identity getIdentityByLogin(final String login) {
        for (final PolicyWrapper wrapper : policyWrappers) {
            final List<Identity> ids = wrapper.getIdentities();
            if (ids != null && !ids.isEmpty()) {
                for (final Identity id : ids) {
                    if (id.getName().equals(login)) {
                        return id;
                    }
                }
            }
        }

        final List<Identity> ids = getBaseSecurityEBL().searchUsers(login, null, false);
        for (final Identity id : ids) {
            if (id.getName().equals(login)) {
                return id;
            }
        }
        return null;
    }

    /**
     * @return
     */
    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    protected String formatIdentity(final Identity ident) {
        final User u = ident.getUser();
        final String login = ident.getName();
        return login + ": " + getUserService().getFirstAndLastname(u);
    }

    public class UserMapperProvider implements ResultMapProvider {

        @Override
        public void getAutoCompleteContent(final String searchValue, final Map<String, String> resMap) {
            final Map<String, String> userProperties = new HashMap<String, String>();
            userProperties.put(UserConstants.FIRSTNAME, searchValue);
            userProperties.put(UserConstants.LASTNAME, searchValue);
            userProperties.put(UserConstants.EMAIL, searchValue);
            if (StringHelper.containsNonWhitespace(searchValue)) {
                final List<Identity> res = getBaseSecurityEBL().searchUsers(searchValue, userProperties, false);
                int maxEntries = 14;
                boolean hasMore = false;
                for (final Identity ident : res) {
                    maxEntries--;
                    final String login = ident.getName();
                    resMap.put(formatIdentity(ident), login);
                    if (maxEntries <= 0) {
                        hasMore = true;
                        break;
                    }
                }
                if (hasMore) {
                    resMap.put(TextBoxListComponent.MORE_RESULTS_INDICATOR, TextBoxListComponent.MORE_RESULTS_INDICATOR);
                }
            }
        }
    }

    public class GroupMapperProvider implements ResultMapProvider {

        public GroupMapperProvider() {
            if (groupList.isEmpty()) {
                groupList.addAll(businessGroupService.findBusinessGroupsAttendedBy(null, getIdentity(), null));
                groupList.addAll(businessGroupService.findBusinessGroupsOwnedBy(null, getIdentity(), null));
            }
        }

        @Override
        public void getAutoCompleteContent(final String searchValue, final Map<String, String> resMap) {
            if (StringHelper.containsNonWhitespace(searchValue)) {
                final String searchValueLower = searchValue.toLowerCase();
                for (final BusinessGroup group : groupList) {
                    if (group.getName().toLowerCase().indexOf(searchValueLower) >= 0) {
                        resMap.put(group.getName(), group.getKey().toString());
                    }
                }
            }
        }
    }

    public class PolicyWrapper {
        private final EPMapPolicy mapPolicy;
        private String componentName;
        private TextBoxListElement userListBox;
        private TextBoxListElement groupListBox;
        private DateChooser fromChooser;
        private DateChooser toChooser;
        private TextElement firstNameEl;
        private TextElement lastNameEl;
        private TextElement mailEl;
        private boolean invitationSend = false;;

        public PolicyWrapper() {
            this.mapPolicy = new EPMapPolicy();
        }

        public boolean isInvitationType() {
            return this.getType().equals(EPMapPolicy.Type.invitation);
        }

        public PolicyWrapper(final EPMapPolicy mapPolicy) {
            this.mapPolicy = mapPolicy;
        }

        public EPMapPolicy getMapPolicy() {
            return mapPolicy;
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(final String componentName) {
            this.componentName = componentName;
        }

        public boolean isInvitationSend() {
            return invitationSend;
        }

        public void setInvitationSend(final boolean invitationSend) {
            this.invitationSend = invitationSend;
        }

        public Invitation getInvitation() {
            return mapPolicy.getInvitation();
        }

        public void setInvitation(final Invitation invitation) {
            mapPolicy.setInvitation(invitation);
        }

        public List<Policy> getPolicies() {
            return mapPolicy.getPolicies();
        }

        public void setPolicies(final List<Policy> policies) {
            mapPolicy.setPolicies(policies);
        }

        public void addPolicy(final Policy policy) {
            mapPolicy.addPolicy(policy);
        }

        public Date getTo() {
            return mapPolicy.getTo();
        }

        public void setTo(final Date to) {
            mapPolicy.setTo(to);
        }

        public Date getFrom() {
            return mapPolicy.getFrom();
        }

        public void setFrom(final Date from) {
            mapPolicy.setFrom(from);
        }

        public EPMapPolicy.Type getType() {
            return mapPolicy.getType();
        }

        public void setType(final EPMapPolicy.Type type) {
            if (!type.equals(mapPolicy.getType())) {
                mapPolicy.setType(type);
                mapPolicy.getPolicies().clear();
            }
        }

        public Map<String, String> getIdentitiesValue() {
            if (mapPolicy.getIdentities() == null) {
                return new HashMap<String, String>();
            }

            final Map<String, String> values = new HashMap<String, String>();
            for (final Identity identity : mapPolicy.getIdentities()) {
                final String login = identity.getName();
                values.put(formatIdentity(identity), login);
            }
            return values;
        }

        public List<Identity> getIdentities() {
            return mapPolicy.getIdentities();
        }

        public void setIdentities(final List<Identity> identities) {
            mapPolicy.setIdentities(identities);
        }

        public Map<String, String> getGroupsValues() {
            if (mapPolicy.getGroups() == null) {
                return new HashMap<String, String>();
            }

            final Map<String, String> values = new HashMap<String, String>();
            for (final BusinessGroup group : mapPolicy.getGroups()) {
                values.put(group.getName(), group.getKey().toString());
            }
            return values;
        }

        public List<BusinessGroup> getGroups() {
            return mapPolicy.getGroups();
        }

        public void setGroups(final List<BusinessGroup> groups) {
            mapPolicy.setGroups(groups);
        }

        public void addGroup(final BusinessGroup group) {
            mapPolicy.addGroup(group);
        }

        public String calc(final String cmpName) {
            return cmpName + "." + componentName;
        }

        public TextBoxListElement getUserListBox() {
            return userListBox;
        }

        public void setUserListBox(final TextBoxListElement userListBox) {
            this.groupListBox = null;
            this.userListBox = userListBox;
        }

        public TextBoxListElement getGroupListBox() {
            return groupListBox;
        }

        public void setGroupListBox(final TextBoxListElement groupListBox) {
            this.userListBox = null;
            this.groupListBox = groupListBox;
        }

        public DateChooser getFromChooser() {
            return fromChooser;
        }

        public void setFromChooser(final DateChooser fromChooser) {
            this.fromChooser = fromChooser;
        }

        public DateChooser getToChooser() {
            return toChooser;
        }

        public void setToChooser(final DateChooser toChooser) {
            this.toChooser = toChooser;
        }

        public TextElement getFirstNameEl() {
            return firstNameEl;
        }

        public void setFirstNameEl(final TextElement firstNameEl) {
            this.firstNameEl = firstNameEl;
        }

        public TextElement getLastNameEl() {
            return lastNameEl;
        }

        public void setLastNameEl(final TextElement lastNameEl) {
            this.lastNameEl = lastNameEl;
        }

        public TextElement getMailEl() {
            return mailEl;
        }

        public void setMailEl(final TextElement mailEl) {
            this.mailEl = mailEl;
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
