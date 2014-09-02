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

package org.olat.presentation.admin.quota;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * 
 * @author Christian Guretzki
 */
public class QuotaControllerFactory {

    /**
     * Factory method to create a controller that is capable of editing the quota for the given path.
     * <p>
     * The controller must fire the following events:
     * <ul>
     * <li>Event.CANCELLED_EVENT</li>
     * <li>Event.CHANGED_EVENT</li>
     * </ul>
     * 
     * @param ureq
     * @param wControl
     * @param relPath
     * @param modalMode
     * @return
     */
    public static Controller getQuotaEditorInstance(final UserRequest ureq, final WindowControl wControl, final String relPath, final boolean modalMode) {
        final Controller ctr = new GenericQuotaEditController(ureq, wControl, relPath, modalMode);
        return ctr;
    }

}
