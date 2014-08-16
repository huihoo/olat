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

import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * 
 * @author Ingmar Kroll
 */
public class RepositoryEditDescriptionController extends BasicController {
    private final VelocityContainer chdesctabVC;
    private final Controller repoEntryDetailsFormCtr;
    private final Controller imageUploadController;
    private final TabbedPane tabbedPane;
    private final RepositoryEntry repositoryEntry;
    private final VelocityContainer descVC;

    private static final int picUploadlimitKB = 1024;

    /**
     * Create a repository add controller that adds the given resourceable.
     * 
     * @param ureq
     * @param wControl
     * @param sourceEntry
     */
    public RepositoryEditDescriptionController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry entry, final boolean isSubWorkflow) {
        super(ureq, wControl);
        this.repositoryEntry = entry;
        // wrapper velocity container with a tabbed pane
        descVC = createVelocityContainer("bgrep");
        descVC.contextPut("title", entry.getDisplayname());
        tabbedPane = new TabbedPane("descTB", ureq.getLocale());
        chdesctabVC = createVelocityContainer("changedesctab1");
        chdesctabVC.contextPut("id", entry.getResourceableId() == null ? "-" : entry.getResourceableId().toString());
        chdesctabVC.contextPut("initialauthor", entry.getInitialAuthor());
        descVC.contextPut("disabledforwardreason", translate("disabledforwardreason"));
        // repo entry details form
        repoEntryDetailsFormCtr = new RepositoryEntryDetailsFormController(ureq, getWindowControl(), entry, isSubWorkflow);
        listenTo(repoEntryDetailsFormCtr);
        chdesctabVC.put("repoEntryDetailsFormCtr", repoEntryDetailsFormCtr.getInitialComponent());
        // file upload form - should be refactored to RepositoryEntryDetailsFormController, need more time to do this
        imageUploadController = new RepositoryEntryImageController(ureq, wControl, entry, getTranslator(), picUploadlimitKB);
        listenTo(imageUploadController);
        chdesctabVC.put("imageupload", imageUploadController.getInitialComponent());

        tabbedPane.addTab(translate("table.header.description"), chdesctabVC);
        tabbedPane.addListener(this);
        descVC.put("descTB", tabbedPane);
        putInitialPanel(descVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.imageUploadController) {
            if (event.equals(Event.DONE_EVENT)) {
                fireEvent(ureq, Event.CHANGED_EVENT);
            }

        } else if (source == this.repoEntryDetailsFormCtr) { // process details form events

            if (event.equals(Event.CANCELLED_EVENT)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);

            } else if (event == Event.CHANGED_EVENT) {
                fireEvent(ureq, Event.CHANGED_EVENT);
                fireEvent(ureq, Event.DONE_EVENT);
                descVC.contextPut("title", getRepositoryEntry().getDisplayname());
            }
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Controllers autodisposed by basic controller
    }

    /**
     * @return Returns the repositoryEntry.
     */
    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

}
