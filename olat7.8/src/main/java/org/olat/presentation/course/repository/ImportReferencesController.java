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
import org.olat.lms.course.imports.ImportReferencesEBL;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.DetailsReadOnlyForm;
import org.olat.presentation.repository.RepositorySearchController;
import org.olat.presentation.repository.RepositoryTableModel;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 19.05.2005
 * 
 * @author Mike Stock
 */
public class ImportReferencesController extends BasicController {

    private static final String PACKAGE = PackageUtil.getPackageName(ImportReferencesController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(ImportReferencesController.class);

    private final CourseNode node;
    private final String resourceType;

    private final Translator translator;
    private final VelocityContainer main;
    private final Link importButton;
    private final Link reattachButton;
    private final Link noopButton;
    private Link continueButton;
    private RepositorySearchController searchController;
    private final RepositoryEntryImportExport importExport;
    private DetailsReadOnlyForm repoDetailsForm;
    private final Panel mainPanel;

    public ImportReferencesController(final UserRequest ureq, final WindowControl wControl, final CourseNode node, final String resourceType,
            final RepositoryEntryImportExport importExport) {
        super(ureq, wControl);
        this.node = node;
        this.resourceType = resourceType;
        this.importExport = importExport;

        translator = new PackageTranslator(PACKAGE, ureq.getLocale());
        main = new VelocityContainer("ref", VELOCITY_ROOT + "/import_repo.html", translator, this);
        importButton = LinkFactory.createButton("import.import.action", main, this);
        reattachButton = LinkFactory.createButton("import.reattach.action", main, this);
        noopButton = LinkFactory.createButton("import.noop.action", main, this);

        main.contextPut("nodename", node.getShortTitle());
        main.contextPut("type", translator.translate("node." + node.getType()));
        main.contextPut("displayname", importExport.getDisplayName());
        main.contextPut("resourcename", importExport.getResourceName());
        main.contextPut("description", importExport.getDescription());
        mainPanel = new Panel("mainPanel");
        mainPanel.setContent(main);

        putInitialPanel(mainPanel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == reattachButton) {
            String type = getResourceType();
            removeAsListenerAndDispose(searchController);
            searchController = new RepositorySearchController(translator.translate("command.linkresource"), ureq, getWindowControl(), true, false, type);
            listenTo(searchController);
            searchController.doSearchByOwnerLimitType(ureq.getIdentity(), type);
            // brasato:: check in gui
            // was: getWindowControl().pushAsModalDialog(translator.translate("command.linkresource"), searchController.getInitialComponent());
            mainPanel.setContent(searchController.getInitialComponent());
        } else if (source == importButton) {
            final RepositoryEntry importedRepositoryEntry = getImportReferencesEBL().doImport(importExport, node, false, ureq.getIdentity());
            // If not successfull, return. Any error messages have bean already set.
            if (importedRepositoryEntry == null) {
                getWindowControl().setError("Import failed.");
                return;
            }
            String typeName = getResourceType();
            final Translator repoTranslator = new PackageTranslator(PackageUtil.getPackageName(RepositoryServiceImpl.class), ureq.getLocale());
            removeAsListenerAndDispose(repoDetailsForm);
            repoDetailsForm = new DetailsReadOnlyForm(ureq, getWindowControl(), importedRepositoryEntry, typeName, false);
            listenTo(repoDetailsForm);
            main.put("repoDetailsForm", repoDetailsForm.getInitialComponent());
            main.setPage(VELOCITY_ROOT + "/import_repo_details.html");
            continueButton = LinkFactory.createButton("import.redetails.continue", main, this);
            return;
        } else if (source == noopButton) {
            node.removeRepositoryReference();
            fireEvent(ureq, Event.DONE_EVENT);
        } else if (source == continueButton) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    private ImportReferencesEBL getImportReferencesEBL() {
        return CoreSpringFactory.getBean(ImportReferencesEBL.class);
    }

    /**
     * @return
     */
    private String getResourceType() {
        return resourceType;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == searchController) {
            mainPanel.setContent(main);
            if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
                // repository search controller done
                final RepositoryEntry re = searchController.getSelectedEntry();
                if (re != null) {
                    getImportReferencesEBL().setReference(re, node);
                    getWindowControl().setInfo(translator.translate("import.reattach.success"));
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
