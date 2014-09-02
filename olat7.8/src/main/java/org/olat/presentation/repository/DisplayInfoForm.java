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
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * @author Ingmar Kroll Comment:
 */
public class DisplayInfoForm extends FormBasicController {

    private SelectionElement canCopy;
    private SelectionElement canReference;
    private SelectionElement canLaunch;
    private SelectionElement canDownload;
    private SelectionElement access;

    private final RepositoryEntry entry;
    private final RepositoryHandler handler;

    public DisplayInfoForm(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry entry) {
        super(ureq, wControl);
        this.entry = entry;
        handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(entry);
        initForm(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        //
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("tab.public");
        setFormContextHelp("org.olat.presentation.repository", "rep-meta-olatauthor.html", "help.hover.rep.detail");

        canCopy = uifactory.addCheckboxesVertical("cif_canCopy", "cif.canCopy", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        canCopy.select("xx", entry.getCanCopy());

        canReference = uifactory.addCheckboxesVertical("cif_canReference", "cif.canReference", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        canReference.select("xx", entry.getCanReference());

        canLaunch = uifactory.addCheckboxesVertical("cif_canLaunch", "cif.canLaunch", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        canLaunch.select("xx", entry.getCanLaunch());
        canLaunch.setVisible(handler != null && handler.supportsLaunch(this.entry));

        canDownload = uifactory.addCheckboxesVertical("cif_canDownload", "cif.canDownload", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        canDownload.select("xx", entry.getCanDownload());
        canDownload.setVisible(handler != null && handler.supportsDownload(this.entry));

        final String[] keys = new String[] { "" + RepositoryEntry.ACC_OWNERS, "" + RepositoryEntry.ACC_OWNERS_AUTHORS, "" + RepositoryEntry.ACC_USERS,
                "" + RepositoryEntry.ACC_USERS_GUESTS };
        final String[] values = new String[] { translate("cif.access.owners"), translate("cif.access.owners_authors"), translate("cif.access.users"),
                translate("cif.access.users_guests"), };
        access = uifactory.addRadiosVertical("cif_access", "cif.access", formLayout, keys, values);
        access.select("" + entry.getAccess(), true);

        flc.setEnabled(false);
    }

    @Override
    protected void doDispose() {
        //
    }
}
