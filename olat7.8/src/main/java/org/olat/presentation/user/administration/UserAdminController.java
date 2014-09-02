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

import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.user.UserConstants;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.presentation.admin.policy.PolicyController;
import org.olat.presentation.admin.quota.QuotaControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.user.ChangePrefsController;
import org.olat.presentation.user.DisplayPortraitController;
import org.olat.presentation.user.ProfileAndHomePageEditController;
import org.olat.presentation.user.PropFoundEvent;
import org.olat.presentation.user.UserPropertiesController;
import org.olat.presentation.user.administration.groups.GroupOverviewController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 29, 2003
 * 
 * @author Sabina Jeger
 * 
 *         <pre>
 *  Complete rebuild on 17. jan 2006 by Florian Gnaegi
 *  
 *  Functionality to change or view all kind of things for this user 
 *  based on the configuration for the user manager. 
 *  This controller should only be used by the UserAdminMainController.
 * 
 * </pre>
 */
public class UserAdminController extends BasicController implements Activateable {

    // NLS support

    private static final String NLS_ERROR_NOACCESS_TO_USER = "error.noaccess.to.user";
    private static final String NLS_FOUND_PROPERTY = "found.property";
    private static final String NLS_EDIT_UPROFILE = "edit.uprofile";
    private static final String NLS_EDIT_UPREFS = "edit.uprefs";
    private static final String NLS_EDIT_UPWD = "edit.upwd";
    private static final String NLS_EDIT_UAUTH = "edit.uauth";
    private static final String NLS_EDIT_UPROP = "edit.uprop";
    private static final String NLS_EDIT_UPOLICIES = "edit.upolicies";
    private static final String NLS_EDIT_UROLES = "edit.uroles";
    private static final String NLS_EDIT_UQUOTA = "edit.uquota";
    private static final String NLS_VIEW_GROUPS = "view.groups";
    private static final String NLS_EDIT_DELEGATION = "edit.delegation";

    private BusinessGroup currBusinessGroup;

    private VelocityContainer myContent;

    private Identity myIdentity = null;

    // controllers used in tabbed pane
    private TabbedPane userTabP;
    private Controller prefsCtr, propertiesCtr, pwdCtr, quotaCtr, policiesCtr, rolesCtr, userShortDescrCtr, partipGrpCntrllr;
    private DisplayPortraitController portraitCtr;
    private UserAuthenticationsEditorController authenticationsCtr;
    private Link backLink;
    private ProfileAndHomePageEditController userProfileCtr;
    private GroupOverviewController grpCtr;

    /**
     * Constructor that creates a back - link as default
     * 
     * @param ureq
     * @param wControl
     * @param identity
     */
    public UserAdminController(final UserRequest ureq, final WindowControl wControl, final Identity identity) {
        super(ureq, wControl);

        if (!getBaseSecurityEBL().isUserAdministrationPermitted(ureq.getIdentity())) {
            throw new OLATSecurityException("Insufficient permissions to access UserAdminController");
        }

        myIdentity = identity;

        if (allowedToManageUser(ureq, myIdentity)) {
            myContent = this.createVelocityContainer("udispatcher");
            backLink = LinkFactory.createLinkBack(myContent, this);
            userShortDescrCtr = new UserShortDescriptionController(ureq, wControl, identity);
            myContent.put("userShortDescription", userShortDescrCtr.getInitialComponent());

            setBackButtonEnabled(true); // default
            initTabbedPane(myIdentity, ureq);
            exposeUserDataToVC(ureq, myIdentity);
            this.putInitialPanel(myContent);
        } else {
            final String supportAddr = WebappHelper.getMailConfig("mailSupport");
            this.showWarning(NLS_ERROR_NOACCESS_TO_USER, supportAddr);
            this.putInitialPanel(new Panel("empty"));
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * Possible activation parameters are: edit.uprofile edit.uprefs edit.upwd edit.uauth edit.uprop edit.upolicies edit.uroles edit.uquota
     * 
     * @param ureq
     * @param viewIdentifier
     */
    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        if (userTabP != null) {
            userTabP.setSelectedPane(translate(viewIdentifier));
            // do nothing if not initialized
        }
    }

    /**
     * @param backButtonEnabled
     */
    public void setBackButtonEnabled(final boolean backButtonEnabled) {
        if (myContent != null) {
            myContent.contextPut("showButton", Boolean.valueOf(backButtonEnabled));
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == backLink) {
            fireEvent(ureq, Event.BACK_EVENT);
        }
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == propertiesCtr) {
            if (event.getCommand().equals("PropFound")) {
                final PropFoundEvent foundEvent = (PropFoundEvent) event;
                final PropertyImpl myfoundProperty = foundEvent.getProperty();
                this.showInfo(NLS_FOUND_PROPERTY, myfoundProperty.getKey().toString());
            }
        } else if (source == pwdCtr) {
            if (event == Event.DONE_EVENT) {
                // rebuild authentication tab, could be wrong now
                if (authenticationsCtr != null) {
                    authenticationsCtr.rebuildAuthenticationsTableDataModel();
                }
            }
        } else if (source == userProfileCtr) {
            if (event == Event.DONE_EVENT) {
                // reload profile data on top
                // TODO: ORID-1007 if load is required or not could not be reliable tested - loading is let here */
                myIdentity = getBaseSecurity().loadIdentityByKey(myIdentity.getKey());
                exposeUserDataToVC(ureq, myIdentity);
                userProfileCtr.resetForm(ureq, getWindowControl());
            }
        }
    }

    /**
     * Check if user allowed to modify this identity. Only modification of user that have lower rights is allowed. No one exept admins can manage usermanager and admins
     * 
     * @param ureq
     * @param identity
     * @return boolean
     */
    private boolean allowedToManageUser(final UserRequest ureq, final Identity identity) {
        Roles roles = ureq.getUserSession().getRoles();
        return getBaseSecurityEBL().isManageUserPermitted(identity, roles);
    }

    /**
     * Initialize the tabbed pane according to the users rights and the system configuration
     * 
     * @param identity
     * @param ureq
     */
    private void initTabbedPane(final Identity identity, final UserRequest ureq) {
        // first Initialize the user details tabbed pane
        final boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
        userTabP = new TabbedPane("userTabP", ureq.getLocale());

        /**
         * Determine, whether the user admin is or is not able to edit all fields in user profile form. The system admin is always able to do so.
         */
        Boolean canEditAllFields = getBaseSecurityEBL().isEditAllProfileFieldPermitted(identity);

        userProfileCtr = new ProfileAndHomePageEditController(ureq, getWindowControl(), identity, canEditAllFields.booleanValue());
        listenTo(userProfileCtr);
        userTabP.addTab(translate(NLS_EDIT_UPROFILE), userProfileCtr.getInitialComponent());

        prefsCtr = new ChangePrefsController(ureq, getWindowControl(), identity);
        userTabP.addTab(translate(NLS_EDIT_UPREFS), prefsCtr.getInitialComponent());

        boolean canChangeAndCreatePwd = getBaseSecurityEBL().isChangeAndCreatePasswordPermitted(identity, isOlatAdmin);
        if (canChangeAndCreatePwd) {
            pwdCtr = new UserChangePasswordController(ureq, getWindowControl(), identity);
            this.listenTo(pwdCtr); // listen when finished to update
                                   // authentications model
            userTabP.addTab(translate(NLS_EDIT_UPWD), pwdCtr.getInitialComponent());
        }

        boolean canEditAuthentication = getBaseSecurityEBL().isEditAuthenticationsPermitted(isOlatAdmin);
        if (canEditAuthentication) {
            authenticationsCtr = new UserAuthenticationsEditorController(ureq, getWindowControl(), identity);
            userTabP.addTab(translate(NLS_EDIT_UAUTH), authenticationsCtr.getInitialComponent());
        }

        boolean canEditUserProperties = getBaseSecurityEBL().isEditUserPropertiesPermitted(isOlatAdmin);
        if (canEditUserProperties) {
            propertiesCtr = new UserPropertiesController(ureq, getWindowControl(), identity);
            this.listenTo(propertiesCtr);
            userTabP.addTab(translate(NLS_EDIT_UPROP), propertiesCtr.getInitialComponent());
        }

        boolean canAccessPolicies = getBaseSecurityEBL().isPoliciesAccessPermitted(isOlatAdmin);
        if (canAccessPolicies) {
            policiesCtr = new PolicyController(ureq, getWindowControl(), identity);
            userTabP.addTab(translate(NLS_EDIT_UPOLICIES), policiesCtr.getInitialComponent());
        }

        final Boolean canStartGroups = getBaseSecurityEBL().isStartGroupPermitted();
        grpCtr = new GroupOverviewController(ureq, getWindowControl(), identity, canStartGroups);
        listenTo(grpCtr);
        userTabP.addTab(translate(NLS_VIEW_GROUPS), grpCtr.getInitialComponent());

        rolesCtr = new SystemRolesAndRightsController(getWindowControl(), ureq, identity);
        userTabP.addTab(translate(NLS_EDIT_UROLES), rolesCtr.getInitialComponent());

        DelegationController delegationController = new DelegationController(ureq, getWindowControl(), identity);
        userTabP.addTab(translate(NLS_EDIT_DELEGATION), delegationController.getInitialComponent());

        boolean canAccessQuota = getBaseSecurityEBL().isAccessToQuotaPermitted(isOlatAdmin);
        if (canAccessQuota) {
            final String relPath = FolderConfig.getUserHome(identity.getName());
            quotaCtr = QuotaControllerFactory.getQuotaEditorInstance(ureq, getWindowControl(), relPath, false);
            userTabP.addTab(translate(NLS_EDIT_UQUOTA), quotaCtr.getInitialComponent());
        }

        // now push to velocity
        myContent.put("userTabP", userTabP);
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * Add some user data to velocity container including the users portrait
     * 
     * @param ureq
     * @param identity
     */
    private void exposeUserDataToVC(final UserRequest ureq, final Identity identity) {
        final Locale loc = ureq.getLocale();
        myContent.contextPut("foundUserName", identity.getName());
        myContent.contextPut("foundFirstName", getUserService().getUserProperty(identity.getUser(), UserConstants.FIRSTNAME, loc));
        myContent.contextPut("foundLastName", getUserService().getUserProperty(identity.getUser(), UserConstants.LASTNAME, loc));
        myContent.contextPut("foundEmail", getUserService().getUserProperty(identity.getUser(), UserConstants.EMAIL, loc));
        removeAsListenerAndDispose(portraitCtr);
        portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), identity, true, true);
        myContent.put("portrait", portraitCtr.getInitialComponent());
        removeAsListenerAndDispose(userShortDescrCtr);
        userShortDescrCtr = new UserShortDescriptionController(ureq, getWindowControl(), identity);
        myContent.put("userShortDescription", userShortDescrCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo get disposed in
        // BasicController
        if (quotaCtr != null) {
            quotaCtr.dispose();
            quotaCtr = null;
        }
        if (authenticationsCtr != null) {
            authenticationsCtr.dispose();
            authenticationsCtr = null;
        }
        if (prefsCtr != null) {
            prefsCtr.dispose();
            prefsCtr = null;
        }
        if (policiesCtr != null) {
            policiesCtr.dispose();
            policiesCtr = null;
        }
        if (rolesCtr != null) {
            rolesCtr.dispose();
            rolesCtr = null;
        }
        if (portraitCtr != null) {
            portraitCtr.dispose();
            portraitCtr = null;
        }
        if (userShortDescrCtr != null) {
            userShortDescrCtr.dispose();
            userShortDescrCtr = null;
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
