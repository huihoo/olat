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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.commons;

import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.user.UserDataDeletable;
import org.olat.presentation.bookmark.BookmarkHandler;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.spring.BeanType;

/**
 * Define all classes which can be loaded via spring by type
 * 
 * @author Christian Guretzki
 */
public enum LmsSpringBeanTypes implements BeanType {
    basicManager(BasicManager.class), repositoryHandler(RepositoryHandler.class), bookmarkHandler(BookmarkHandler.class), userDataDeletable(UserDataDeletable.class);

    private Class extensionType;

    private LmsSpringBeanTypes(Class extensionType) {
        this.extensionType = extensionType;
    }

    @Override
    public Class getExtensionTypeClass() {
        return extensionType;
    }
}
