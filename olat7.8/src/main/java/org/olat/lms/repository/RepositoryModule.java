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
package org.olat.lms.repository;

import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.repository.CatalogContextEntryControllerCreator;
import org.olat.presentation.repository.RepositoryContextEntryControllerCreator;
import org.olat.system.commons.configuration.AbstractOLATModule;

/**
 * Description:<br>
 * The business group module initializes the OLAT repository environment. Configurations are loaded from here.
 * <P>
 * Initial Date: 04.11.2009 <br>
 * 
 * @author gnaegi
 */
public class RepositoryModule extends AbstractOLATModule {
    /**
	 */

    protected RepositoryModule() {
    }

    @Override
    public void initialize() {
        // Add controller factory extension point to launch groups
        NewControllerFactory.getInstance().addContextEntryControllerCreator(RepositoryEntry.class.getSimpleName(), new RepositoryContextEntryControllerCreator());

        NewControllerFactory.getInstance().addContextEntryControllerCreator(CatalogEntry.class.getSimpleName(), new CatalogContextEntryControllerCreator());
    }

    /**
	 */
    @Override
    protected void initDefaultProperties() {
        // nothing to init
    }

    /**
	 */
    @Override
    protected void initFromChangedProperties() {
        // nothing to init
    }

}
