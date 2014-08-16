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

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jan 27, 2006
 * 
 * @author gnaegi
 * 
 *         <pre>
 * Description:
 * Controller that is used to manipulate the users system roles and rights. When calling
 * this controller make sure the user who calls the controller meets the following 
 * criterias:
 * - user is system administrator
 * or
 * - user tries not to modify a system administrator or user administrator
 * - user tries not to modify an author if author rights are not enabled for user managers
 * - user tries not to modify a group manager if group manager rights are not enabled for user managers 
 * - user tries not to modify a guest if guest rights are not enabled for user managers 
 * 
 * Usually this controller is called by the UserAdminController that takes care of all this.
 * There should be no need to use it anywhere else.
 */
public class SystemRolesAndRightsController extends BasicController {

    private static final String PACKAGE = PackageUtil.getPackageName(SystemRolesAndRightsController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private final VelocityContainer main;
    private final PackageTranslator translator;
    private SystemRolesAndRightsForm sysRightsForm;
    private final Identity identity;

    /**
     * Constructor for a controller that lets you edit the users system roles and rights.
     * 
     * @param wControl
     * @param ureq
     * @param identity
     *            identity to be edited
     */
    public SystemRolesAndRightsController(final WindowControl wControl, final UserRequest ureq, final Identity identity) {
        super(ureq, wControl);
        translator = new PackageTranslator(PACKAGE, ureq.getLocale());
        main = new VelocityContainer("sysRolesVC", VELOCITY_ROOT + "/usysRoles.html", translator, null);
        this.identity = identity;
        putInitialPanel(main);
        createForm(ureq, identity);
        main.put("sysRightsForm", sysRightsForm.getInitialComponent());
    }

    /**
     * Initialize a new SystemRolesAndRightsForm for the given identity using the security manager
     * 
     * @param ureq
     * @param identity
     * @return SystemRolesAndRightsForm
     */
    private void createForm(final UserRequest ureq, final Identity identity) {
        removeAsListenerAndDispose(sysRightsForm);
        sysRightsForm = new SystemRolesAndRightsForm(ureq, getWindowControl(), identity);
        listenTo(sysRightsForm);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        if (source == sysRightsForm) {
            if (event == Event.DONE_EVENT) {
                saveFormData(ureq, identity, sysRightsForm);
            }
            createForm(ureq, identity);
            main.put("sysRightsForm", sysRightsForm.getInitialComponent());
        }
    }

    /**
     * Persist form data in database. User needs to logout / login to activate changes. A bit tricky here is that only form elements should be gettet that the user is
     * allowed to manipulate. See also the comments in SystemRolesAndRightsForm.
     * 
     * @param targetIdentity
     * @param form
     */
    private void saveFormData(final UserRequest ureq, final Identity targetIdentity, final SystemRolesAndRightsForm form) {
        Roles myRoles = ureq.getUserSession().getRoles();

        boolean isAnonymous = form.isAnonymous();
        final boolean isGroupManager = form.isGroupmanager();
        final boolean isAuthor = form.isAuthor();
        final boolean isUserManager = form.isUsermanager();
        final boolean isInstitutionalResourceManager = form.isInstitutionalResourceManager();
        final boolean isAdmin = form.isAdmin();

        Roles newRoles = new Roles(isAdmin, isUserManager, isGroupManager, isAuthor, isAnonymous, isInstitutionalResourceManager, false);
        getBaseSecurityEBL().changeIdentityRoles(myRoles, targetIdentity, newRoles);

        final Integer status = form.getStatus();
        getBaseSecurityEBL().changeIdentityStatus(myRoles, targetIdentity, status);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

    private static BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

}
