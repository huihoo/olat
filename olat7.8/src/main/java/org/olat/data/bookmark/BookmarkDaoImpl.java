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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description: This bookmark manager persists the bookmarks in the database.
 * 
 * @author Sabina Jeger
 */
@Repository
public class BookmarkDaoImpl extends BasicManager implements BookmarkDao {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private DB db;

    /**
     * Spring constructor.
     */
    private BookmarkDaoImpl() {
        //
    }

    /**
     * @param newBookmark
     */
    @Override
    public void createAndPersistBookmark(final Bookmark newBookmark) {
        db.saveObject(newBookmark);
        if (log.isDebugEnabled()) {
            log.debug("Bookmark has been created: " + newBookmark.getTitle());
        }
    }

    /**
     * @param identity
     * @return a List of found bookmarks of given subject
     */
    @Override
    public List<Bookmark> findBookmarksByIdentity(final Identity identity) {
        final String query = "from org.olat.data.bookmark.BookmarkImpl as b where b.owner = ?";
        return db.find(query, identity.getKey(), Hibernate.LONG);
    }

    /**
     * Finds bookmarks of a specific type for an identity
     * 
     * @param identity
     * @param type
     * @return list of bookmarks for this identity
     */
    @Override
    public List<Bookmark> findBookmarksByIdentity(final Identity identity, final String type) {
        final String query = "from org.olat.data.bookmark.BookmarkImpl as b where b.owner = ? and b.displayrestype = ?";
        final List<Bookmark> found = db.find(query, new Object[] { identity.getKey(), type }, new Type[] { Hibernate.LONG, Hibernate.STRING });
        return found;
    }

    /**
     * @param changedBookmark
     * @return true if saved successfully
     */
    @Override
    public void updateBookmark(final Bookmark changedBookmark) {
        db.updateObject(changedBookmark);
    }

    /**
     * @param deletableBookmark
     * @return true if success
     */
    @Override
    public void deleteBookmark(final Bookmark deletableBookmark) {
        db.deleteObject(deletableBookmark);
    }

    /**
     * Delete all bookmarks pointing to the given resourceable.
     * 
     * @param res
     */
    @Override
    public void deleteAllBookmarksFor(final OLATResourceable res) {
        final String query = "from org.olat.data.bookmark.BookmarkImpl as b where b.olatrestype = ? and b.olatreskey = ?";
        db.delete(query, new Object[] { res.getResourceableTypeName(), res.getResourceableId() }, new Type[] { Hibernate.STRING, Hibernate.LONG });

    }

    /**
     * @param identity
     * @param res
     * @return true if resourceable is bookmarked
     */
    @Override
    public boolean isResourceableBookmarked(final Identity identity, final OLATResourceable res) {
        final String query = "from org.olat.data.bookmark.BookmarkImpl as b where b.olatrestype = ? and b.olatreskey = ? and b.owner.key = ?";

        final List results = db.find(query, new Object[] { res.getResourceableTypeName(), res.getResourceableId(), identity.getKey() }, new Type[] { Hibernate.STRING,
                Hibernate.LONG, Hibernate.LONG });
        return results.size() != 0;
    }

}
