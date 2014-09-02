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
package org.olat.presentation.repository;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.DTab;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 21.06.2012 <br>
 * 
 * @author cg
 */
public class DynamicTabHelper {

    private static final Logger log = LoggerHelper.getLogger();

    public static void openRepoEntryTabInRunMode(RepositoryEntry repositoryEntry, UserRequest ureq, RepositoryHandler typeToLaunch) {
        final OLATResourceable ores = repositoryEntry.getOlatResource();
        final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        final OLATResourceable businessOres = repositoryEntry;
        final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
        final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, dts.getWindowControl());
        final Controller ctrl = typeToLaunch.createLaunchController(ores, null, ureq, bwControl);
        openRepoEntryTab(repositoryEntry, ureq, ctrl, getDisplayName(repositoryEntry, ureq.getLocale()), RepositoryDetailsController.ACTIVATE_RUN);
    }

    public static void openRepoEntryTab(RepositoryEntry repositoryEntry, UserRequest ureq, Controller controller, String displayName, String openingMode) {
        getRepositoryService().incrementLaunchCounter(repositoryEntry);
        final OLATResourceable ores = repositoryEntry.getOlatResource();
        openResourceTab(ores, ureq, controller, displayName, openingMode);
    }

    public static void openResourceTab(OLATResourceable ores, UserRequest ureq, Controller controller, String displayName, String openingMode) {
        if (Windows.getWindows(ureq) == null) {
            log.error("I-130614-0535: Windows.getWindows(ureq) == null [ureq=" + ureq + "].", new RuntimeException("I-130614-0535 stacktrace"));
            return;
        }
        if (Windows.getWindows(ureq).getWindow(ureq) == null) {
            log.error("I-130614-0535: Windows.getWindows(ureq)getWindow(ureq) == null [ureq=" + ureq + "].", new RuntimeException("I-130614-0535 stacktrace"));
            return;
        }
        // was brasato:: DTabs dts = getWindowControl().getDTabs();
        final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        final DTab dt = dts.openDTab(ores, controller, displayName);
        if (dt != null) {
            dts.activate(ureq, dt, openingMode);
        }
    }

    /**
     * Get displayname of a repository entry. If repository entry is a course and is this course closed then add a prefix to the title.
     */
    public static String getDisplayName(RepositoryEntry repositoryEntry, Locale locale) {
        final Translator defaultTranslator = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, locale);
        // load repositoryEntry again because the hibernate object is 'detached'.
        // Otherwise you become an exception when you check owner-group.
        repositoryEntry = getRepositoryService().loadRepositoryEntry(repositoryEntry);
        String displayName = repositoryEntry.getDisplayname();
        if (isRepositoryEntryClosed(repositoryEntry)) {
            displayName = "[" + defaultTranslator.translate("title.prefix.closed") + "] ".concat(displayName);
        }

        return displayName;
    }

    public static boolean isRepositoryEntryClosed(RepositoryEntry repositoryEntry) {
        return repositoryEntry != null && getRepositoryService().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed();
    }

    private static RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

}
