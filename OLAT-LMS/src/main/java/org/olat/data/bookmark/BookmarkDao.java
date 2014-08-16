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
package org.olat.data.bookmark;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

public interface BookmarkDao {

    /**
     * @param newBookmark
     */
    public abstract void createAndPersistBookmark(Bookmark newBookmark);

    /**
     * @param identity
     * @return a List of found bookmarks of given subject
     */
    public abstract List findBookmarksByIdentity(Identity identity);

    /**
     * Finds bookmarks of a specific type for an identity
     * 
     * @param identity
     * @param type
     * @return list of bookmarks for this identity
     */
    public abstract List findBookmarksByIdentity(Identity identity, String type);

    /**
     * @param changedBookmark
     * @return true if saved successfully
     */
    public abstract void updateBookmark(Bookmark changedBookmark);

    /**
     * @param deletableBookmark
     * @return true if success
     */
    public abstract void deleteBookmark(Bookmark deletableBookmark);

    /**
     * Delete all bookmarks pointing to the given resourceable.
     * 
     * @param res
     */
    public abstract void deleteAllBookmarksFor(OLATResourceable res);

    /**
     * @param identity
     * @param res
     * @return true if resourceable is bookmarked
     */
    public abstract boolean isResourceableBookmarked(Identity identity, OLATResourceable res);

}
