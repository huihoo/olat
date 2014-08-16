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
package org.olat.presentation.user;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.presentation.commons.context.ContextEntryControllerCreator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description:</h3>
 * <p>
 * This class offers a way to launch the users homepage (alias visiting card) controller in a new tab
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class IdentityContextEntryControllerCreator implements ContextEntryControllerCreator {
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createController(final ContextEntry ce, final UserRequest ureq, final WindowControl wControl) {
        final Identity identity = extractIdentity(ce);
        if (identity == null) {
            return null;
        }
        final UserInfoMainController uimc = new UserInfoMainController(ureq, wControl, identity);
        return uimc;
    }

    /**
	 */
    @Override
    public String getSiteClassName(final ContextEntry ce) {
        // opened as tab not site
        return null;
    }

    /**
	 */
    @Override
    public String getTabName(final ContextEntry ce) {
        final Identity identity = extractIdentity(ce);
        if (identity == null) {
            return null;
        }
        return identity.getName();
    }

    /**
     * Helper to get the identity that is encoded into the context entry
     * 
     * @param ce
     * @return the identity or NULL if not found
     */
    private Identity extractIdentity(final ContextEntry ce) {
        final OLATResourceable resource = ce.getOLATResourceable();
        final Long key = resource.getResourceableId();
        if (key == null || key.equals(0)) {
            log.error("Can not load identity with key::" + key);
            return null;
        }
        final Identity identity = getBaseSecurity().loadIdentityByKey(key);
        if (identity == null) {
            log.error("Can not load identity with key::" + key);
        }
        return identity;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    @Override
    public boolean validateContextEntryAndShowError(final ContextEntry ce, final UserRequest ureq, final WindowControl wControl) {
        final Identity identity = extractIdentity(ce);
        return identity != null;
    }
}
