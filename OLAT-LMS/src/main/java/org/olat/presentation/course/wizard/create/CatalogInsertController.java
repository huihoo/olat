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
 * Technische Universitaet Chemnitz Lehrstuhl Technische Informatik Author Marcel Karras (toka@freebits.de) Author Norbert Englisch
 * (norbert.englisch@informatik.tu-chemnitz.de) Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */

package org.olat.presentation.course.wizard.create;

import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.course.wizard.create.CatalogHelper;
import org.olat.lms.course.wizard.create.CourseCreationConfiguration;
import org.olat.presentation.catalog.CatalogAjaxAddController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller for inserting the given course into the catalog.
 * <P>
 * Initial Date: 02.12.2008 <br>
 * 
 * @author Marcel Karras (toka@freebits.de)
 */
public class CatalogInsertController extends CatalogAjaxAddController {

    private final CourseCreationConfiguration courseConfig;
    private CatalogService catalogService;
    private CatalogEntry selectedParent;

    public CatalogInsertController(final UserRequest ureq, final WindowControl control, final RepositoryEntry repositoryEntry,
            final CourseCreationConfiguration courseConfig) {
        super(ureq, control, repositoryEntry);

        catalogService = CoreSpringFactory.getBean(CatalogService.class);

        this.courseConfig = courseConfig;

        cancelLink.setVisible(false);
        selectLink.setVisible(false);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == treeCtr) {
            if (event instanceof TreeNodeClickedEvent) {
                final TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                String nodeIdent = clickedEvent.getNodeId();
                if (!catalogService.hasParentAllreadyCatalogEntryAsChild(nodeIdent, toBeAddedEntry)) {
                    // don't create entry right away, user must select submit button first
                    Long newParentId = Long.parseLong(nodeIdent);
                    this.selectedParent = catalogService.loadCatalogEntry(newParentId);
                    fireEvent(ureq, Event.DONE_EVENT);
                }
            }
        }

    }

    /**
     * initialize the controller or re-initialize with existing configuration
     */
    public void init() {
        if (getCourseCreationConfiguration().getSelectedCatalogEntry() != null) {
            this.selectedParent = getCourseCreationConfiguration().getSelectedCatalogEntry();
            treeCtr.selectPath(CatalogHelper.getPath(this.selectedParent));
        }
    }

    private CourseCreationConfiguration getCourseCreationConfiguration() {
        return this.courseConfig;
    }

    /**
     * @return the selected catalogEntry
     */
    public CatalogEntry getSelectedParent() {
        return this.selectedParent;
    }
}
