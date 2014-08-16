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

package org.olat.presentation.user;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.security.authentication.SupportsAfterLoginInterceptor;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 29, 2003
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class ChangePasswordController extends BasicController implements SupportsAfterLoginInterceptor {

    private static final Logger log = LoggerHelper.getLogger();
    private VelocityContainer myContent;
    private ChangePasswordForm chPwdForm;

    /**
     * @param ureq
     * @param wControl
     */
    public ChangePasswordController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        // if a user is not allowed to change his/her own password, say it here
        if (!getBaseSecurityEBL().isPasswordChangeConfigured()) {
            final String text = translate("notallowedtochangepwd", new String[] { WebappHelper.getMailConfig("mailSupport") });
            final Controller simpleMsg = MessageUIFactory.createSimpleMessage(ureq, wControl, text);
            listenTo(simpleMsg);// register controller to be disposed automatically on dispose of Change password controller
            putInitialPanel(simpleMsg.getInitialComponent());
            return;
        }

        boolean isChangePasswordPermitted = getBaseSecurityEBL().isChangePasswordPermitted(ureq.getIdentity());
        if (!isChangePasswordPermitted) {
            throw new OLATSecurityException("Insufficient permission to access ChangePasswordController");
        }

        myContent = createVelocityContainer("pwd");
        // adds "provider_..." variables to myContent
        exposePwdProviders(ureq.getIdentity());

        chPwdForm = new ChangePasswordForm(ureq, wControl);
        listenTo(chPwdForm);

        myContent.put("chpwdform", chPwdForm.getInitialComponent());

        putInitialPanel(myContent);
    }

    /**
     * @return
     */
    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == chPwdForm) {
            if (event == Event.DONE_EVENT) {

                final String oldPwd = chPwdForm.getOldPasswordValue();
                Identity principal = ureq.getIdentity();
                Identity authenticatedIdentity = getBaseSecurityEBL().authenticateOlatOrLdap(oldPwd, principal);

                if (authenticatedIdentity == null) {
                    showError("error.password.noauth");
                } else {
                    final String newPwd = chPwdForm.getNewPasswordValue();
                    if (getBaseSecurityEBL().changePassword(principal, authenticatedIdentity, newPwd)) {
                        // TODO: verify that we are NOT in a transaction (changepwd should be commited immediately)
                        log.info("Audit:Changed password for identity." + authenticatedIdentity.getName());
                        showInfo("password.successful");
                    } else {
                        showError("password.failed");
                    }
                }
            } else if (event == Event.CANCELLED_EVENT) {
                removeAsListenerAndDispose(chPwdForm);
                chPwdForm = new ChangePasswordForm(ureq, getWindowControl());
                listenTo(chPwdForm);
                myContent.put("chpwdform", chPwdForm.getInitialComponent());
            }
        }
    }

    private void exposePwdProviders(final Identity identity) {

        final Iterator<String> iter = getBaseSecurityEBL().getAuthenticationProviders(identity).iterator();
        while (iter.hasNext()) {
            myContent.contextPut("provider_" + iter.next(), Boolean.TRUE);
        }

        if (getBaseSecurityEBL().isPropagatePasswordChangeOnLDAPServerConfigured()) {
            myContent.contextPut("provider_LDAP_pwdchange", Boolean.TRUE);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
