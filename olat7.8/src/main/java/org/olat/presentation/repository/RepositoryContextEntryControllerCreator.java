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
package org.olat.presentation.repository;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.commons.context.ContextEntryControllerCreator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * <h3>Description:</h3>
 * <p>
 * This class can create run controllers for repository entries in the given context
 * <p>
 * Initial Date: 19.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class RepositoryContextEntryControllerCreator implements ContextEntryControllerCreator {

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createController(final ContextEntry ce, final UserRequest ureq, final WindowControl wControl) {
        final OLATResourceable ores = ce.getOLATResourceable();

        final RepositoryService repom = RepositoryServiceImpl.getInstance();
        final RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
        final Controller ctrl = RepositoyUIFactory.createLaunchController(re, null, ureq, wControl);
        return ctrl;

    }

    /**
	 */
    @Override
    public String getTabName(final ContextEntry ce) {
        final OLATResourceable ores = ce.getOLATResourceable();

        final RepositoryService repom = RepositoryServiceImpl.getInstance();
        final RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
        return re.getDisplayname();
    }

    /**
	 */
    @Override
    public String getSiteClassName(final ContextEntry ce) {
        return null;
    }

    @Override
    public boolean validateContextEntryAndShowError(final ContextEntry ce, final UserRequest ureq, final WindowControl wControl) {
        final OLATResourceable ores = ce.getOLATResourceable();
        final RepositoryService repom = RepositoryServiceImpl.getInstance();
        final RepositoryEntry re = repom.lookupRepositoryEntry(ores.getResourceableId());
        return re != null;
    }

}
