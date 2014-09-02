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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.presentation.security.authentication.ldap;

import java.util.Locale;

import org.olat.lms.security.authentication.ldap.LDAPLoginModule;
import org.olat.presentation.admin.SystemAdminMainController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.extensions.AbstractExtension;
import org.olat.presentation.framework.extensions.Extension;
import org.olat.presentation.framework.extensions.ExtensionElement;
import org.olat.presentation.framework.extensions.action.ActionExtension;
import org.olat.presentation.framework.extensions.helpers.ExtensionElements;

/**
 * Description:<br>
 * The LDAP admin extension launches the LDAPAdminController to manage the LDAP connection
 * <P>
 * Initial Date: 21.08.2008 <br>
 * 
 * @author gnaegi
 */
public class LDAPAdminExtension extends AbstractExtension implements Extension {
    /**
	 */
    @Override
    public boolean isEnabled() {
        return LDAPLoginModule.isLDAPEnabled();
    }

    private final ExtensionElements elements = new ExtensionElements();

    /**
     * Constructor to create an extension that registers in the admin site
     */
    protected LDAPAdminExtension() {
        elements.putExtensionElement(SystemAdminMainController.class.getName(), new ActionExtension() {

            /**
			 */
            @Override
            public String getActionText(final Locale loc) {
                final Translator transl = PackageUtil.createPackageTranslator(LDAPAdminExtension.class, loc);
                return transl.translate("admin.menu.ldap");
            }

            /**
			 */
            @Override
            public String getDescription(final Locale loc) {
                final Translator transl = PackageUtil.createPackageTranslator(LDAPAdminExtension.class, loc);
                return transl.translate("admin.menu.ldap.desc");
            }

            /**
             * java.lang.Object)
             */
            @Override
            public Controller createController(final UserRequest ureq, final WindowControl control, @SuppressWarnings("unused") final Object arg) {
                return new LDAPAdminController(ureq, control);
            }

        });
    }

    /**
	 */
    @Override
    public ExtensionElement getExtensionFor(final String extensionPoint) {
        if (isEnabled()) {
            return elements.getExtensionElement(extensionPoint);
        } else {
            return null;
        }
    }

}
