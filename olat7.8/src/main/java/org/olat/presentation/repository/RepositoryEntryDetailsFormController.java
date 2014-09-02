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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.repository;

import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * This form controller allows users to edit the repository details and upload an image
 * <P>
 * Initial Date: 16.07.2009 <br>
 * 
 * @author gnaegi
 */
public class RepositoryEntryDetailsFormController extends FormBasicController {

    private final boolean isSubWorkflow;
    private final RepositoryEntry entry;

    private TextElement displayName;
    private RichTextElement description;

    public RepositoryEntryDetailsFormController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry entry, final boolean isSubWorkflow) {
        super(ureq, wControl);
        this.entry = entry;
        this.isSubWorkflow = isSubWorkflow;
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("details.entryinfoheader");
        setFormContextHelp("org.olat.presentation.repository", "rep-meta-desc.html", "help.hover.rep.detail");
        // Add static fields
        uifactory.addStaticTextElement("cif.id", entry.getResourceableId() == null ? "-" : entry.getResourceableId().toString(), formLayout);
        uifactory.addStaticTextElement("cif.initialAuthor", entry.getInitialAuthor() == null ? "-" : entry.getInitialAuthor().toString(), formLayout);
        // Add resource type
        String typeName = null;
        final OLATResource res = entry.getOlatResource();
        if (res != null) {
            typeName = res.getResourceableTypeName();
        }
        final StringBuilder typeDisplayText = new StringBuilder(100);
        if (typeName != null) { // add image and typename code
            final RepositoryEntryIconRenderer reir = new RepositoryEntryIconRenderer(ureq.getLocale());
            typeDisplayText.append("<span class=\"b_with_small_icon_left ");
            typeDisplayText.append(reir.getIconCssClass(entry));
            typeDisplayText.append("\">");
            final String tName = ControllerFactory.translateResourceableTypeName(typeName, ureq.getLocale());
            typeDisplayText.append(tName);
            typeDisplayText.append("</span>");
        } else {
            typeDisplayText.append(translate("cif.type.na"));
        }
        uifactory.addStaticExampleText("cif.type", typeDisplayText.toString(), formLayout);
        //
        uifactory.addSpacerElement("spacer1", formLayout, false);
        //
        displayName = uifactory.addTextElement("cif.displayname", "cif.displayname", -1, entry.getDisplayname(), formLayout);
        displayName.setDisplaySize(30);
        displayName.setMaxLength(RepositoryEntry.MAX_DISPLAYNAME_LENGTH);
        displayName.setMandatory(true);
        //
        description = uifactory.addRichTextElementForStringDataMinimalistic("cif.description", "cif.description",
                (entry.getDescription() != null ? entry.getDescription() : " "), 10, -1, false, formLayout, ureq.getUserSession(), getWindowControl());
        description.setMandatory(true);
        //
        final FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
        formLayout.add("buttonContainer", buttonContainer);

        uifactory.addFormSubmitButton("submit", buttonContainer);
        if (!isSubWorkflow) {
            uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
        }
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        // Check for empty display name
        if (!StringHelper.containsNonWhitespace(displayName.getValue())) {
            displayName.setErrorKey("cif.error.displayname.empty", new String[] {});
            return false;
        }
        displayName.clearError();
        // Check for empty description
        if (!StringHelper.containsNonWhitespace(description.getValue())) {
            description.setErrorKey("cif.error.description.empty", new String[] {});
            return false;
        }
        description.clearError();
        // Ok, passed all checks
        return super.validateFormLogic(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // update model
        entry.setDisplayname(displayName.getValue().trim());
        entry.setDescription(description.getValue().trim());
        // notify parent controller
        fireEvent(ureq, Event.CHANGED_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

}
