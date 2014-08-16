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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.bookmark.BookmarkDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.handlers.WrongBookmarkHandlerException;
import org.olat.presentation.bookmark.BookmarkEvent;
import org.olat.presentation.bookmark.BookmarkHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for BookmarkServiceImpl
 * 
 * <P>
 * Initial Date: 10.05.2011 <br>
 * 
 * @author lavinia
 */
@Service("bookmarkService")
public class BookmarkServiceImpl implements BookmarkService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    List<BookmarkHandler> bookmarkHandlers;
    @Autowired
    BookmarkDao bookmarkDao;
    @Autowired
    RepositoryService repositoryService;

    private BookmarkServiceImpl() {
        // [Spring]
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#createAndPersistBookmark(org.olat.data.bookmark.Bookmark)
     */
    @Override
    public void createAndPersistBookmark(Bookmark newBookmark) {
        bookmarkDao.createAndPersistBookmark(newBookmark);
        fireBookmarkEvent(newBookmark.getOwner());

    }

    /**
     * Fire MultiUserEvent - BookmarkEvent - after add/modify/delete bookmark.
     * <p>
     * If the input identity not null the event is intended only for one user, else for all users.
     * 
     * @param bookmark
     */
    private void fireBookmarkEvent(final Identity identity) {
        // event for all users
        BookmarkEvent bookmarkEvent = new BookmarkEvent();
        OLATResourceable eventBusOres = OresHelper.createOLATResourceableType(Identity.class);
        if (identity != null) {
            // event for the specified user
            bookmarkEvent = new BookmarkEvent(identity.getName());
            eventBusOres = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
        }
        // TODO: LD: use this: //UserSession.getSingleUserEventCenter().fireEventToListenersOf(bookmarkEvent, eventBusOres);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(bookmarkEvent, eventBusOres);
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#findBookmarksByIdentity(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List findBookmarksByIdentity(Identity identity) {
        return bookmarkDao.findBookmarksByIdentity(identity);
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#findBookmarksByIdentity(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    @Override
    public List findBookmarksByIdentity(Identity identity, String type) {
        return bookmarkDao.findBookmarksByIdentity(identity, type);
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#updateBookmark(org.olat.data.bookmark.Bookmark)
     */
    @Override
    public void updateBookmark(Bookmark changedBookmark) {
        bookmarkDao.updateBookmark(changedBookmark);
        fireBookmarkEvent(changedBookmark.getOwner());
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#deleteBookmark(org.olat.data.bookmark.Bookmark)
     */
    @Override
    public void deleteBookmark(Bookmark deletableBookmark) {
        bookmarkDao.deleteBookmark(deletableBookmark);
        fireBookmarkEvent(deletableBookmark.getOwner());
    }

    /**
     * calculates the URL for launching a bookmark
     * 
     * @see org.olat.lms.bookmark.BookmarkService#getLaunchOlatResourceable(org.olat.data.bookmark.Bookmark)
     */
    @Override
    public OLATResourceable getLaunchOlatResourceable(final Bookmark chosenBm) {
        final String finalType = chosenBm.getOlatrestype();
        final Long finalKey = chosenBm.getOlatreskey();
        final OLATResourceable res = OresHelper.createOLATResourceableInstance(finalType, finalKey);
        return res;
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#deleteAllBookmarksFor(org.olat.system.commons.resource.OLATResourceable)
     */
    @Override
    public void deleteAllBookmarksFor(OLATResourceable res) {
        bookmarkDao.deleteAllBookmarksFor(res);
        fireBookmarkEvent(null);
    }

    /**
     * @see org.olat.lms.bookmark.BookmarkService#isResourceableBookmarked(org.olat.data.basesecurity.Identity, org.olat.system.commons.resource.OLATResourceable)
     */
    @Override
    public boolean isResourceableBookmarked(Identity identity, OLATResourceable res) {

        return bookmarkDao.isResourceableBookmarked(identity, res);
    }

    /**
     * Delete all bookmarks for certain identity.
     * 
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        final List bookmarks = findBookmarksByIdentity(identity);
        for (final Iterator iter = bookmarks.iterator(); iter.hasNext();) {
            deleteBookmark((Bookmark) iter.next());
        }
        log.debug("All bookmarks deleted for identity=" + identity);
        // no need to fire BookmarkEvent - the user was deleted
    }

    /**
     * Launch the given bookmark
     * 
     * @param bookmark
     * @param ureq
     * @param wControl
     * @return TRUE: launch successful; FALSE: no launcher found (unknonwn bookmark type)
     */
    @Override
    public boolean launchBookmark(final Bookmark bookmark, final UserRequest ureq, final WindowControl wControl) {
        for (final BookmarkHandler handler : getBookmarkHandlers()) {
            final boolean success = handler.tryToLaunch(bookmark, ureq, wControl);
            log.debug("Tried to launch bookmark::" + bookmark + " with handler::" + handler + " with result::" + success);
            if (success) {
                return true;
            }
        }
        // no handler found that could launch the given bookmark
        log.warn("Could not find a launcher for bookmark::" + bookmark + " with displayType::" + bookmark.getDisplayrestype(), null);
        return false;
    }

    /**
     * Create a fully qualified URL that can be used to launch this bookmark e.g. from a browser bookmark or an RSS feed document
     * 
     * @param bookmark
     * @return URL or NULL if not successful
     */
    @Override
    public String createJumpInURL(final Bookmark bookmark) {
        for (final BookmarkHandler handler : getBookmarkHandlers()) {
            try {
                log.debug("Trying to create jump in URL for bookmark::" + bookmark + " with handler::" + handler);
                return handler.createJumpInURL(bookmark);
            } catch (WrongBookmarkHandlerException e) {
                continue;
            }
        }
        // no handler found that could launch the given bookmark
        log.warn("Could not create a jump in URL for bookmark::" + bookmark + " with displayType::" + bookmark.getDisplayrestype(), null);
        return null;
    }

    /**
     * gets the list with all BookmarkHandler implementations.
     * 
     * @return
     */
    private List<BookmarkHandler> getBookmarkHandlers() {
        return bookmarkHandlers;
    }

    @Override
    public RepositoryEntry getBookmarkRepositoryEntry(final Bookmark chosenBm) {
        return repositoryService.lookupRepositoryEntry(getLaunchOlatResourceable(chosenBm).getResourceableId());
    }

}
