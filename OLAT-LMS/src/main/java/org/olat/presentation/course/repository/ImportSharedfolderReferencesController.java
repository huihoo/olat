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
package org.olat.presentation.course.repository;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.config.CourseConfigManager;
import org.olat.lms.course.config.CourseConfigManagerImpl;
import org.olat.lms.course.imports.ImportSharedFolderEBL;
import org.olat.lms.reference.ReferenceEnum;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.util.ComponentUtil;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.DetailsReadOnlyForm;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 19.05.2005
 * 
 * @author Mike Stock
 */
public class ImportSharedfolderReferencesController extends BasicController {

    private final VelocityContainer importSharedfolderVC;
    private final Link importButton;
    private final Link reattachButton;
    private final Link noopButton;
    private Link continueButton;
    private ReferencableEntriesSearchController searchController;
    private final RepositoryEntryImportExport importExport;
    private DetailsReadOnlyForm repoDetailsForm;
    private final Panel main;
    private final OLATResourceable ores;

    public ImportSharedfolderReferencesController(final RepositoryEntryImportExport importExport, final OLATResourceable ores, final UserRequest ureq,
            final WindowControl wControl) {
        super(ureq, wControl);
        this.ores = ores;
        this.importExport = importExport;
        importSharedfolderVC = createVelocityContainer("import_sharedfolder");
        importButton = LinkFactory.createButton("sf.import.action", importSharedfolderVC, this);
        reattachButton = LinkFactory.createButton("sf.reattach.action", importSharedfolderVC, this);
        noopButton = LinkFactory.createButton("sf.noop.action", importSharedfolderVC, this);

        importSharedfolderVC.contextPut("displayname", importExport.getDisplayName());
        importSharedfolderVC.contextPut("resourcename", importExport.getResourceName());
        importSharedfolderVC.contextPut("description", importExport.getDescription());
        main = new Panel("main");
        main.setContent(importSharedfolderVC);
        putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final ICourse course = CourseFactory.loadCourse(ores);
        if (source == reattachButton) {
            removeAsListenerAndDispose(searchController);
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, SharedFolderFileResource.TYPE_NAME, getTranslator().translate(
                    "command.linkresource"));
            listenTo(searchController);
            main.setContent(ComponentUtil.createTitledComponent("command.linkresource", null, getTranslator(), searchController.getInitialComponent()));

        } else if (source == importButton) {
            final RepositoryEntry importedRepositoryEntry = getImportSharedFolderEBL().doImport(importExport, course, false, ureq.getIdentity());
            // If not successfull, return. Any error messages have baan already set.
            if (importedRepositoryEntry == null) {
                getWindowControl().setError("Import failed.");
                return;
            }
            final Translator repoTranslator = new PackageTranslator(PackageUtil.getPackageName(RepositoryServiceImpl.class), ureq.getLocale());
            removeAsListenerAndDispose(repoDetailsForm);
            repoDetailsForm = new DetailsReadOnlyForm(ureq, getWindowControl(), importedRepositoryEntry, SharedFolderFileResource.TYPE_NAME, false);
            listenTo(repoDetailsForm);
            importSharedfolderVC.put("repoDetailsForm", repoDetailsForm.getInitialComponent());
            final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(this.getClass());
            importSharedfolderVC.setPage(VELOCITY_ROOT + "/import_repo_details.html");
            continueButton = LinkFactory.createButton("import.redetails.continue", importSharedfolderVC, this);
            return;
        } else if (source == noopButton) {
            // delete reference
            setSharedFolderSoftkey(course, CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
            fireEvent(ureq, Event.DONE_EVENT);
        } else if (source == continueButton) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    private void setSharedFolderSoftkey(final ICourse course, final String sharedFolderSoftkey) {
        final CourseConfigManager ccm = CourseConfigManagerImpl.getInstance();
        final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
        courseConfig.setSharedFolderSoftkey(sharedFolderSoftkey);
        ccm.saveConfigTo(course, courseConfig);
    }

    private ImportSharedFolderEBL getImportSharedFolderEBL() {
        return CoreSpringFactory.getBean(ImportSharedFolderEBL.class);
    }

    public static ReferenceService getReferenceService() {
        return (ReferenceService) CoreSpringFactory.getBean(ReferenceService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchController) {
            final ICourse course = CourseFactory.loadCourse(ores);
            main.setContent(importSharedfolderVC);
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // repository search controller done
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    setSharedFolderSoftkey(course, re.getSoftkey());
                    getReferenceService().updateRefTo(re.getOlatResource(), course, ReferenceEnum.SHARE_FOLDER_REF.getValue());
                    getWindowControl().setInfo(getTranslator().translate("import.reattach.success"));
                    fireEvent(ureq, Event.DONE_EVENT);
                }
                // else cancelled repo search, display import options again.
            }
        }
    }

    @Override
    protected void doDispose() {
        // Controllers autodisposed by BasicController
    }

}
