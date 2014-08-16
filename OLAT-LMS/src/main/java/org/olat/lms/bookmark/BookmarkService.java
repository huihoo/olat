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
package org.olat.lms.bookmark;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.user.UserDataDeletable;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * TODO: Class Description for BookmarkService
 * 
 * <P>
 * Initial Date: 10.05.2011 <br>
 * 
 * @author lavinia
 */
public interface BookmarkService extends UserDataDeletable {

    /**
     * @param newBookmark
     */
    public void createAndPersistBookmark(Bookmark newBookmark);

    /**
     * @param identity
     * @return a List of found bookmarks of given subject
     */
    public List findBookmarksByIdentity(Identity identity);

    /**
     * Finds bookmarks of a specific type for an identity
     * 
     * @param identity
     * @param type
     * @return list of bookmarks for this identity
     */
    public List findBookmarksByIdentity(Identity identity, String type);

    /**
     * @param changedBookmark
     * @return true if saved successfully
     */
    public void updateBookmark(Bookmark changedBookmark);

    /**
     * @param deletableBookmark
     * @return true if success
     */
    public void deleteBookmark(Bookmark deletableBookmark);

    /**
     * calculates the URL for launching a bookmark
     * 
     * @param chosenBm
     * @return resourceablea instance
     */
    public OLATResourceable getLaunchOlatResourceable(Bookmark chosenBm);

    /**
     * Delete all bookmarks pointing to the given resourceable.
     * 
     * @param res
     */
    public void deleteAllBookmarksFor(OLATResourceable res);

    /**
     * @param identity
     * @param res
     * @return true if resourceable is bookmarked
     */
    public boolean isResourceableBookmarked(Identity identity, OLATResourceable res);

    /**
     * Delete all bookmarks for certain identity.
     * 
     */
    public void deleteUserData(Identity identity, String newDeletedUserName);

    /**
     * Launch the given bookmark
     * 
     * @param bookmark
     * @param ureq
     * @param wControl
     * @return TRUE: launch successful; FALSE: no launcher found (unknonwn bookmark type)
     */
    public boolean launchBookmark(Bookmark bookmark, UserRequest ureq, WindowControl wControl);

    /**
     * Create a fully qualified URL that can be used to launch this bookmark e.g. from a browser bookmark or an RSS feed document
     * 
     * @param bookmark
     * @return URL or NULL if not successful
     */
    public String createJumpInURL(Bookmark bookmark);

    /**
     * 
     * @param chosenBm
     * @return
     */
    public RepositoryEntry getBookmarkRepositoryEntry(Bookmark chosenBm);

}
