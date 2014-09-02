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
 * Copyright (c) 2008 frentix GmbH,<br>
 * http://www.frentix.com, Switzerland.
 * <p>
 */
package org.olat.presentation.catalog;

import org.apache.log4j.Logger;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.catalog.CatalogEntry;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.repository.handlers.WrongBookmarkHandlerException;
import org.olat.presentation.bookmark.BookmarkHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.repository.site.RepositorySite;
import org.olat.system.commons.Settings;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Bookmark handler for catalog bookmarks: activates the repository entry site, activates the catalog menu and then activates the catalog item with the given id
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author gnaegi
 */
@Component
public class CatalogBookmarkHandler implements BookmarkHandler {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    CatalogService catalogService;
    @Autowired
    BookmarkService bookmarkService;

    /**
     * [spring]
     */
    protected CatalogBookmarkHandler() {
        //
    }

    /**
	 */
    @Override
    public boolean tryToLaunch(final Bookmark bookmark, final UserRequest ureq, final WindowControl wControl) {
        final OLATResourceable reores = bookmarkService.getLaunchOlatResourceable(bookmark);
        // only launch bookmarks of type catalog entry
        if (reores.getResourceableTypeName().equals(CatalogService.CATALOGENTRY)) {
            // set catalog param to same syntax as used in jumpin activation process
            final DTabs dts = wControl.getWindowBackOffice().getWindow().getDynamicTabs();
            // encode sub view identifyer using ":" character
            dts.activateStatic(ureq, RepositorySite.class.getName(), "search.catalog:" + bookmark.getOlatreskey());
            return true;
        }
        return false;
    }

    /**
     * @throws WrongBookmarkHandlerException
     */
    @Override
    public String createJumpInURL(final Bookmark bookmark) throws WrongBookmarkHandlerException {
        final OLATResourceable reores = bookmarkService.getLaunchOlatResourceable(bookmark);
        if (reores.getResourceableTypeName().equals(CatalogService.CATALOGENTRY)) {
            CatalogEntry catEntry = catalogService.loadCatalogEntry(bookmark.getOlatreskey());
            return Settings.getServerContextPathURI() + "/url/CatalogEntry/" + catEntry.getKey();
        }
        throw new WrongBookmarkHandlerException("Bookmark is of type:" + bookmark.getOlatrestype());
    }

}
