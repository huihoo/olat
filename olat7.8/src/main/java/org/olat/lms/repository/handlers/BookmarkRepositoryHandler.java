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
package org.olat.lms.repository.handlers;

import org.apache.log4j.Logger;
import org.olat.data.bookmark.Bookmark;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.bookmark.BookmarkService;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.bookmark.BookmarkHandler;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.system.commons.Settings;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Bookmark launch handler for repository entry bookmarks. Creates or activates a dynamic tab and loads the olat resource from the repository entry into the tab
 * <P>
 * Initial Date: 27.05.2008 <br>
 * 
 * @author gnaegi
 */
@Component
public class BookmarkRepositoryHandler implements BookmarkHandler {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    BookmarkService bookmarkService;
    @Autowired
    RepositoryService repositoryService;

    /**
	 * 
	 */
    protected BookmarkRepositoryHandler() {
        //
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public boolean tryToLaunch(final Bookmark bookmark, final UserRequest ureq, final WindowControl wControl) {

        final RepositoryEntry re = bookmarkService.getBookmarkRepositoryEntry(bookmark);
        if (re == null) {
            // we can't launch this bookmark, exit with false
            return false;
        }
        // ok, this bookmark represents a repo entry, try to launch
        if (!repositoryService.isAllowedToLaunch(ureq.getIdentity(), ureq.getUserSession().getRoles(), re)) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.BOOKMARK_, ureq.getLocale());
            wControl.setWarning(trans.translate("warn.cantlaunch"));
        } else {
            final OLATResourceable ores = re.getOlatResource();
            final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
            final Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dts.getWindowControl(), true);
            DynamicTabHelper.openRepoEntryTab(re, ureq, launchController, bookmark.getTitle(), null);
        }
        // in any case return true - this was a repo entry bookmark!
        return true;
    }

    /**
	 */
    @Override
    public String createJumpInURL(final Bookmark bookmark) throws WrongBookmarkHandlerException {
        RepositoryEntry repositoryEntry = bookmarkService.getBookmarkRepositoryEntry(bookmark);
        if (repositoryEntry == null) {
            throw new WrongBookmarkHandlerException("Bookmark is of type:" + bookmark.getOlatrestype());
        }
        return Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
    }

}
