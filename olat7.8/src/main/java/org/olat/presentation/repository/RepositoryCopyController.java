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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryEBL;
import org.olat.lms.repository.RepositoryEntryInputData;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
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
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class RepositoryCopyController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private final VelocityContainer mainContainer;
    private final Link cancelButton;
    private final Link forwardButton;
    private final RepositoryEditDescriptionController descriptionController;

    private final RepositoryEntry sourceEntry;
    private RepositoryEntry newEntry;

    // flag is true when workflow has been finished successfully,
    // otherwhise when disposing the controller or in a case of
    // user abort / cancel the system will delete temporary data
    private boolean workflowSuccessful = false;

    private RepositoryEBL repositoryEBL;

    /**
     * Create a repository add controller that adds the given resourceable.
     * 
     * @param ureq
     * @param wControl
     * @param sourceEntry
     */
    public RepositoryCopyController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry sourceEntry) {
        super(ureq, wControl);

        this.sourceEntry = sourceEntry;
        this.newEntry = null;

        repositoryEBL = CoreSpringFactory.getBean(RepositoryEBL.class);

        mainContainer = createVelocityContainer("copy");
        cancelButton = LinkFactory.createButton("cmd.cancel", mainContainer, this);
        forwardButton = LinkFactory.createButton("cmd.forward", mainContainer, this);
        forwardButton.setEnabled(false);
        LinkFactory.markDownloadLink(forwardButton); // TODO:cg: for copy of large repositoryEntries => Remove when new long-running task is implemented
        forwardButton.setTextReasonForDisabling(translate("disabledforwardreason"));

        newEntry = createNewRepositoryEntry(sourceEntry, ureq);
        descriptionController = new RepositoryEditDescriptionController(ureq, getWindowControl(), newEntry, true);
        listenTo(descriptionController);

        mainContainer.put("details", descriptionController.getInitialComponent());

        putInitialPanel(mainContainer);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == descriptionController) {
            if (event == Event.CANCELLED_EVENT) {
                // abort transaction
                cleanup();
                fireEvent(ureq, Event.CANCELLED_EVENT);
                return;
            } else if (event == Event.DONE_EVENT) {
                forwardButton.setEnabled(true);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == forwardButton) {
            // check if repository entry is still available
            final RepositoryService rm = getRepositoryService();
            final RepositoryEntry checkEntry = rm.lookupRepositoryEntry(sourceEntry.getKey());
            if (checkEntry == null) { // entry has been deleted meanwhile
                showError("error.createcopy");
                fireEvent(ureq, Event.FAILED_EVENT);
                fireEvent(ureq, new EntryChangedEvent(sourceEntry, EntryChangedEvent.DELETED));
                return;
            }
            newEntry = descriptionController.getRepositoryEntry();
            // update needed to save changed name and desc.
            getRepositoryService().updateRepositoryEntry(newEntry);

            final RepositoryHandler typeToCopy = RepositoryHandlerFactory.getInstance().getRepositoryHandler(sourceEntry);
            final IAddController addController = typeToCopy.createAddController(null, null, ureq, getWindowControl());
            addController.repositoryEntryCreated(newEntry);
            // dispose immediately (cleanup temp files), not really used
            // as a controller, should be in a business logic frontend manager instead!
            addController.dispose();

            showInfo("add.success");
            workflowSuccessful = true;
            fireEvent(ureq, Event.DONE_EVENT);
            return;
        } else if (source == cancelButton) {
            // abort transaction
            cleanup();
            fireEvent(ureq, Event.CANCELLED_EVENT);
            return;
        }
    }

    private RepositoryEntry createNewRepositoryEntry(final RepositoryEntry src, final UserRequest ureq) {
        RepositoryEntryInputData repositoryEntryInputData = getRepositoryEntryInput(src, ureq.getIdentity());
        RepositoryEntry preparedEntry = repositoryEBL.copyRepositoryEntry(src, repositoryEntryInputData);

        if (preparedEntry == null) {
            getWindowControl().setError(this.getTranslator().translate("error.createcopy"));
            fireEvent(ureq, Event.FAILED_EVENT);
        }
        return preparedEntry;
    }

    private RepositoryEntryInputData getRepositoryEntryInput(final RepositoryEntry src, final Identity identity) {
        // FIXME:pb:ms translation for COPY OF
        String newDispalyname = "Copy of " + src.getDisplayname();
        if (newDispalyname.length() > DetailsReadOnlyForm.MAX_DISPLAYNAME) {
            newDispalyname = newDispalyname.substring(0, DetailsReadOnlyForm.MAX_DISPLAYNAME);
        }
        String resName = src.getResourcename();
        if (resName == null) {
            resName = "";
        }
        final RepositoryHandler typeToCopy = RepositoryHandlerFactory.getInstance().getRepositoryHandler(src);
        final OLATResourceable newResourceable = typeToCopy.createCopy(sourceEntry.getOlatResource(), identity);

        return new RepositoryEntryInputData(identity, resName, newDispalyname, newResourceable);
    }

    /**
     * @return
     */
    private RepositoryService getRepositoryService() {
        return RepositoryServiceImpl.getInstance();
    }

    protected RepositoryEntry getNewEntry() {
        return newEntry;
    }

    private void cleanup() {
        log.debug("Cleanup : newEntry=" + newEntry);
        repositoryEBL.deleteRepositoryEntryAndItsOwnerGroupIfCopyWasInterrupted(newEntry);
        newEntry = null;
        log.debug("Cleanup : finished");
    }

    /**
	 */
    @Override
    protected void doDispose() {
        log.debug("doDispose : newEntry=" + newEntry);
        if (!workflowSuccessful) {
            cleanup();
        }
    }
}
