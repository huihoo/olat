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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.commons.fileresource.ImsCPFileResource;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.fileresource.ScormCPFileResource;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.portfolio.EPTemplateMapResource;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description: Implements a repository entry search workflow used by OLAT authors. Entries can be found that are either owned by the user or visible to authors in
 * conjunction with the canReference flag.
 * 
 * @author gnaegi Initial Date: Aug 17, 2006
 */
public class ReferencableEntriesSearchController extends BasicController {

    public static final Event EVENT_REPOSITORY_ENTRY_SELECTED = new Event("event.repository.entry.selected");

    private static final String CMD_SEARCH = "cmd.search";
    private static final String CMD_SEARCH_ENTRIES = "cmd.searchEntries";
    private static final String CMD_ALL_ENTRIES = "cmd.allEntries";
    private static final String CMD_MY_ENTRIES = "cmd.myEntries";
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_IMPORT = "import";

    private final VelocityContainer mainVC;
    private final RepositorySearchController searchCtr;
    private final String[] limitTypes;
    private CloseableModalController previewModalCtr;
    private Controller previewCtr;

    private Link myEntriesLink, allEntriesLink;
    private Link searchEntriesLink;
    private final String commandLabel;
    private Link createRessourceButton;
    private Link importRessourceButton;
    private RepositoryAddController addController;
    private CloseableModalController cmc;
    private RepositoryEntry selectedRepositoryEntry;

    private final boolean canImport;
    private final boolean canCreate;

    public ReferencableEntriesSearchController(final WindowControl wControl, final UserRequest ureq, final String limitType, final String commandLabel) {
        this(wControl, ureq, new String[] { limitType }, commandLabel, true, true, true);
    }

    public ReferencableEntriesSearchController(final WindowControl wControl, final UserRequest ureq, final String[] limitTypes, final String commandLabel) {
        this(wControl, ureq, limitTypes, commandLabel, true, true, true);
    }

    public ReferencableEntriesSearchController(final WindowControl wControl, final UserRequest ureq, final String[] limitTypes, final String commandLabel,
            final boolean canImport, final boolean canCreate, final boolean canDirectLaunch) {
        super(ureq, wControl);
        this.canImport = canImport;
        this.canCreate = canCreate;
        this.limitTypes = limitTypes;
        this.commandLabel = commandLabel;
        mainVC = createVelocityContainer("referencableSearch");
        // add all link to velocity
        initLinks();

        // add repo search controller
        searchCtr = new RepositorySearchController(commandLabel, ureq, getWindowControl(), false, canDirectLaunch, limitTypes);
        listenTo(searchCtr);

        // do instantiate buttons
        boolean isVisible = isCreateButtonVisible();
        if (isVisible) {
            createRessourceButton = LinkFactory.createButtonSmall("cmd.create.ressource", mainVC, this);
        }
        mainVC.contextPut("hasCreateRessourceButton", new Boolean(isVisible));
        isVisible = isImportButtonVisible();
        if (isVisible) {
            importRessourceButton = LinkFactory.createButtonSmall("cmd.import.ressource", mainVC, this);
        }
        mainVC.contextPut("hasImportRessourceButton", new Boolean(isVisible));

        event(ureq, myEntriesLink, null);
        searchCtr.enableSearchforAllReferencalbeInSearchForm(true);
        mainVC.put("searchCtr", searchCtr.getInitialComponent());

        putInitialPanel(mainVC);
    }

    /**
     * if building block can be imported, return true
     * 
     * @return
     */
    private boolean isImportButtonVisible() {
        if (!canImport) {
            return false;
        }

        final List<String> limitTypeList = Arrays.asList(this.limitTypes);

        final String[] importAllowed = new String[] { TestFileResource.TYPE_NAME, WikiResource.TYPE_NAME, ImsCPFileResource.TYPE_NAME, ScormCPFileResource.TYPE_NAME,
                SurveyFileResource.TYPE_NAME, BlogFileResource.TYPE_NAME, PodcastFileResource.TYPE_NAME };

        if (Collections.indexOfSubList(Arrays.asList(importAllowed), limitTypeList) != -1) {
            return true;
        }

        return false;
    }

    /**
     * if building block can be created during choose-process, return true
     * 
     * @return
     */
    private boolean isCreateButtonVisible() {
        if (!canCreate) {
            return false;
        }

        final List<String> limitTypeList = Arrays.asList(this.limitTypes);

        final String[] createAllowed = new String[] { TestFileResource.TYPE_NAME, WikiResource.TYPE_NAME, ImsCPFileResource.TYPE_NAME, SurveyFileResource.TYPE_NAME,
                BlogFileResource.TYPE_NAME, PodcastFileResource.TYPE_NAME, EPTemplateMapResource.TYPE_NAME };

        if (Collections.indexOfSubList(Arrays.asList(createAllowed), limitTypeList) != -1) {
            return true;
        }
        return false;
    }

    /**
     * get action like 'new test'
     * 
     * @param type
     * @return
     */
    private String getAction(final String type) {
        String action = new String();
        final List<String> limitTypeList = Arrays.asList(this.limitTypes);
        if (limitTypeList.contains(TestFileResource.TYPE_NAME)) {
            // it's a test
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_TEST;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_TEST;
            }
        } else if (limitTypeList.contains(TestFileResource.TYPE_NAME)) {
            // it's a self test
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_TEST;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_TEST;
            }
        } else if (limitTypeList.contains(WikiResource.TYPE_NAME)) {
            // it's a wiki
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_WIKI;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_WIKI;
            }
        } else if (limitTypeList.contains(ImsCPFileResource.TYPE_NAME)) {
            // it's a CP
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_CP;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_CP;
            }
        } else if (limitTypeList.contains(BlogFileResource.TYPE_NAME)) {
            // it's a Blog
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_BLOG;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_BLOG;
            }
        } else if (limitTypeList.contains(PodcastFileResource.TYPE_NAME)) {
            // it's a Podcast
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_PODCAST;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_PODCAST;
            }
        } else if (limitTypeList.contains(SurveyFileResource.TYPE_NAME)) {
            // it's a survey
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_SURVEY;
            } else if (type.equals(ACTION_IMPORT)) {
                action = RepositoryAddController.ACTION_ADD_SURVEY;
            }
        } else if (limitTypeList.contains(EPTemplateMapResource.TYPE_NAME)) {
            // it's a portfolio tempate
            if (type.equals(ACTION_CREATE)) {
                action = RepositoryAddController.ACTION_NEW_PORTFOLIO;
            }
        }
        if (type.equals(ACTION_IMPORT)) {
            if (limitTypeList.contains(ScormCPFileResource.TYPE_NAME)) {
                // it's a scorm CP
                action = RepositoryAddController.ACTION_ADD_SCORM;
            }
        }
        return action;
    }

    /**
     * Create all link components for workflow navigation
     */
    private void initLinks() {
        // link to search all referencable entries
        searchEntriesLink = LinkFactory.createCustomLink("searchEntriesLink", CMD_SEARCH_ENTRIES, "referencableSearch." + CMD_SEARCH_ENTRIES, Link.LINK, mainVC, this);
        // link to show all referencable entries
        allEntriesLink = LinkFactory.createCustomLink("allEntriesLink", CMD_ALL_ENTRIES, "referencableSearch." + CMD_ALL_ENTRIES, Link.LINK, mainVC, this);
        // link to show all my entries
        myEntriesLink = LinkFactory.createCustomLink("myEntriesLink", CMD_MY_ENTRIES, "referencableSearch." + CMD_MY_ENTRIES, Link.LINK, mainVC, this);
    }

    /**
     * @return Returns the selectedEntry.
     */
    public RepositoryEntry getSelectedEntry() {
        return selectedRepositoryEntry;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == myEntriesLink) {
            searchCtr.doSearchByOwnerLimitType(ureq.getIdentity(), limitTypes);
            mainVC.contextPut("subtitle", translate("referencableSearch." + CMD_MY_ENTRIES));
            myEntriesLink.setCustomEnabledLinkCSS("b_selected");
            allEntriesLink.removeCSS();
            searchEntriesLink.removeCSS();
        }
        if (source == allEntriesLink) {
            searchCtr.doSearchForReferencableResourcesLimitType(ureq.getIdentity(), limitTypes, ureq.getUserSession().getRoles());
            mainVC.contextPut("subtitle", translate("referencableSearch." + CMD_ALL_ENTRIES));
            allEntriesLink.setCustomEnabledLinkCSS("b_selected");
            myEntriesLink.removeCSS();
            searchEntriesLink.removeCSS();
        }
        if (source == searchEntriesLink) {
            mainVC.contextPut("subtitle", translate("referencableSearch." + CMD_SEARCH_ENTRIES));
            // start with search view
            searchCtr.displaySearchForm();
            mainVC.contextPut("subtitle", translate("referencableSearch." + CMD_SEARCH));

            searchEntriesLink.setCustomEnabledLinkCSS("b_selected");
            myEntriesLink.removeCSS();
            allEntriesLink.removeCSS();
        }
        if (source == createRessourceButton) {

            removeAsListenerAndDispose(addController);
            addController = new RepositoryAddController(ureq, getWindowControl(), getAction(ACTION_CREATE));
            listenTo(addController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        }
        if (source == importRessourceButton) {

            removeAsListenerAndDispose(addController);
            addController = new RepositoryAddController(ureq, getWindowControl(), getAction(ACTION_IMPORT));
            listenTo(addController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        final String cmd = event.getCommand();
        if (source == searchCtr) {
            if (cmd.equals(RepositoryTableModel.TABLE_ACTION_SELECT_ENTRY)) {
                // user selected entry to get a preview
                selectedRepositoryEntry = searchCtr.getSelectedEntry();
                final RepositoryEntry repositoryEntry = searchCtr.getSelectedEntry();
                final RepositoryHandler typeToLaunch = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
                if (typeToLaunch == null) {
                    final StringBuilder sb = new StringBuilder(translate("error.launch"));
                    sb.append(": No launcher for repository entry: ");
                    sb.append(repositoryEntry.getKey());
                    throw new OLATRuntimeException(RepositoryDetailsController.class, sb.toString(), null);
                }
                // do skip the increment launch counter, this is only a preview!
                final OLATResourceable ores = repositoryEntry.getOlatResource();

                removeAsListenerAndDispose(previewCtr);
                previewCtr = typeToLaunch.createLaunchController(ores, null, ureq, getWindowControl());
                listenTo(previewCtr);

                removeAsListenerAndDispose(previewModalCtr);
                previewModalCtr = new CloseableModalController(getWindowControl(), translate("referencableSearch.preview.close"), previewCtr.getInitialComponent());
                listenTo(previewModalCtr);

                previewModalCtr.activate();

            } else if (cmd.equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
                // done, user selected a repo entry
                selectedRepositoryEntry = searchCtr.getSelectedEntry();
                fireEvent(ureq, EVENT_REPOSITORY_ENTRY_SELECTED);
            }
            initLinks();
        } else if (source == addController) {
            if (event.equals(Event.DONE_EVENT)) {
                cmc.deactivate();

                selectedRepositoryEntry = addController.getAddedEntry();
                fireEvent(ureq, EVENT_REPOSITORY_ENTRY_SELECTED);
                // get a generic type info:
                Translator pT = new PackageTranslator(this.getClass().getPackage().getName(), getLocale(), getTranslator());
                String typeInfo = pT.translate(addController.getAddedEntry().getOlatResource().getResourceableTypeName());
                String message;
                if (typeInfo.length() > 30) {
                    // probably a non translateable type, therefore an error from translation.
                    message = translate("message.entry.selected.notype", new String[] { addController.getAddedEntry().getDisplayname() });
                } else {
                    message = translate("message.entry.selected", new String[] { addController.getAddedEntry().getDisplayname(), typeInfo });
                }
                getWindowControl().setInfo(message);
            } else if (event.equals(Event.CANCELLED_EVENT)) {
                cmc.deactivate();

            } else if (event.equals(Event.FAILED_EVENT)) {
                showError("add.failed");
            }

        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
