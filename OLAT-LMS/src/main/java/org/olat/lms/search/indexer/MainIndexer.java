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

package org.olat.lms.search.indexer;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;

/**
 * Top class of indexer tree.
 * 
 * @author Christian Guretzki
 */
public class MainIndexer extends AbstractIndexer {

    private static final MainIndexer INSTANCE = new MainIndexer();

    /**
     * Singleton
     */
    private MainIndexer() {
        // singleton
    }

    /**
     * @return Instance of MainIndexer
     */
    public static final MainIndexer getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSupportedTypeName() {
        return MainIndexer.class.getName();
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        return super.checkAccess(businessControl, identity, roles);
    }

}
