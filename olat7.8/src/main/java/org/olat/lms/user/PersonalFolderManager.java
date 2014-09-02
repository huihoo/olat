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

package org.olat.lms.user;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.filebrowser.FolderWebDAVProvider;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Manager for the personal-folder of a user.
 */
public class PersonalFolderManager extends FolderWebDAVProvider implements UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    private static PersonalFolderManager instance;

    private PersonalFolderManager() {
        // [Spring]
        instance = this;
    }

    /**
     * @return Instance of a UserManager
     */
    public static PersonalFolderManager getInstance() {
        return instance;
    }

    /**
     * Delete personal-folder homes/<username> (private & public) of an user.
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        new OlatRootFolderImpl(getRootPathFor(identity), null).delete();
        log.debug("Personal-folder deleted for identity=" + identity);
    }

}
