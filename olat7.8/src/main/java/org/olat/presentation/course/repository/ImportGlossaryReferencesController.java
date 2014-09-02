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
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */

package org.olat.presentation.course.repository;

import org.olat.data.reference.ReferenceDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.config.CourseConfigManager;
import org.olat.lms.course.config.CourseConfigManagerImpl;
import org.olat.lms.course.imports.ImportGlossaryEBL;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.repository.RepositoryEntryImportExport;
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
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.repository.DetailsReadOnlyForm;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class ImportGlossaryReferencesController extends BasicController {

    private final VelocityContainer importGlossaryVC;
    private final Link importButton;
    private final Link reattachButton;
    private final Link noopButton;
    private Link continueButton;
    private ReferencableEntriesSearchController searchController;
    private final RepositoryEntryImportExport importExport;
    private DetailsReadOnlyForm repoDetailsForm;
    private final Panel main;
    private final OLATResourceable ores;

    /**
     * Constructor for the import workflow. Use this only in the repository course import workflow as a subworkflow
     * 
     * @param importExport
     * @param course
     * @param ureq
     * @param wControl
     */
    public ImportGlossaryReferencesController(final RepositoryEntryImportExport importExport, final OLATResourceable ores, final UserRequest ureq,
            final WindowControl wControl) {
        super(ureq, wControl);
        this.ores = ores;
        this.importExport = importExport;
        importGlossaryVC = createVelocityContainer("import_glossary");
        importButton = LinkFactory.createButton("import.import.action", importGlossaryVC, this);
        reattachButton = LinkFactory.createButton("import.reattach.action", importGlossaryVC, this);
        noopButton = LinkFactory.createButton("import.noop.action", importGlossaryVC, this);

        importGlossaryVC.contextPut("displayname", importExport.getDisplayName());
        importGlossaryVC.contextPut("resourcename", importExport.getResourceName());
        importGlossaryVC.contextPut("description", importExport.getDescription());
        main = new Panel("main");
        main.setContent(importGlossaryVC);
        putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final ICourse course = CourseFactory.loadCourse(ores);
        if (source == reattachButton) {
            removeAsListenerAndDispose(searchController);
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, GlossaryResource.TYPE_NAME, getTranslator().translate(
                    "command.linkresource"));
            listenTo(searchController);
            main.setContent(ComponentUtil.createTitledComponent("command.linkresource", null, getTranslator(), searchController.getInitialComponent()));
        } else if (source == importButton) {
            final RepositoryEntry importedRepositoryEntry = getImportGlossaryEBL().doImport(importExport, course, false, ureq.getIdentity());
            // If not successfull, return. Any error messages have baan already set.
            if (importedRepositoryEntry == null) {
                getWindowControl().setError("Import failed.");
                return;
            }
            // Translator repoTranslator = new PackageTranslator(Util.getPackageName(RepositoryManager.class), ureq.getLocale());
            removeAsListenerAndDispose(repoDetailsForm);
            repoDetailsForm = new DetailsReadOnlyForm(ureq, getWindowControl(), importedRepositoryEntry, GlossaryResource.TYPE_NAME, false);
            listenTo(repoDetailsForm);
            importGlossaryVC.put("repoDetailsForm", repoDetailsForm.getInitialComponent());
            final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(this.getClass());
            importGlossaryVC.setPage(VELOCITY_ROOT + "/import_repo_details.html");
            continueButton = LinkFactory.createButton("import.redetails.continue", importGlossaryVC, this);
            return;
        } else if (source == noopButton) {
            // delete reference
            setGlossarySoftkey(course, null);
            fireEvent(ureq, Event.DONE_EVENT);
        } else if (source == continueButton) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    /**
     * @param course
     */
    private void setGlossarySoftkey(final ICourse course, final String glossarySoftkey) {
        final CourseConfigManager ccm = CourseConfigManagerImpl.getInstance();
        final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
        courseConfig.setGlossarySoftKey(glossarySoftkey);
        ccm.saveConfigTo(course, courseConfig);
    }

    private static ImportGlossaryEBL getImportGlossaryEBL() {
        return CoreSpringFactory.getBean(ImportGlossaryEBL.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchController) {
            final ICourse course = CourseFactory.loadCourse(ores);
            main.setContent(importGlossaryVC);
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // repository search controller done
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    setGlossarySoftkey(course, re.getSoftkey());
                    ReferenceDao.getInstance().addReference(course, re.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);
                    getWindowControl().setInfo(getTranslator().translate("import.reattach.success"));
                    fireEvent(ureq, Event.DONE_EVENT);
                }
                // else cancelled repo search, display import options again.
            }
        }
    }

    @Override
    protected void doDispose() {
        // Controllers autodisposed by basic controller
    }

}
