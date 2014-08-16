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

package org.olat.presentation.user.administration.delete;

import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.user.administration.UserSearchController;

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
 *         Optionally set the useMultiSelect boolean to true whcih allows to select multiple identities from within the search results.
 */
public class DeletableUserSearchController extends UserSearchController {

    public DeletableUserSearchController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl, true, true, true);
    }

    /**
     * Can be overwritten by subclassen to search other users or filter users.
     * 
     * @param login
     * @param userPropertiesSearch
     * @return
     */
    @Override
    protected List<Identity> searchUsers(final String login, final Map<String, String> userPropertiesSearch, final boolean userPropertiesAsIntersectionSearch) {
        return getBaseSecurityEBL().searchDeletableUsers(login, userPropertiesSearch, userPropertiesAsIntersectionSearch);
    }

}
