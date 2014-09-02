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

package org.olat.presentation.registration;

import org.olat.lms.security.BaseSecurityEBL;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: srosse Class Description for AbstractUserNameCreationInterceptor
 * <P>
 * Initial Date: 5 mars 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public abstract class AbstractUserNameCreationInterceptor implements UserNameCreationInterceptor {

    private boolean allowChangeOfUsername;
    private boolean makeUniqueProposal;

    public boolean isAllowChangeOfUsername() {
        return allowChangeOfUsername;
    }

    public void setAllowChangeOfUsername(final boolean allowChangeOfUsername) {
        this.allowChangeOfUsername = allowChangeOfUsername;
    }

    public boolean isMakeUniqueProposal() {
        return makeUniqueProposal;
    }

    public void setMakeUniqueProposal(final boolean makeUniqueProposal) {
        this.makeUniqueProposal = makeUniqueProposal;
    }

    @Override
    public boolean allowChangeOfUsername() {
        return allowChangeOfUsername;
    }

    protected String makeUniqueProposal(final String proposedUsername) {
        return getBaseSecurityEBL().getUniqueUsername(proposedUsername);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

}
