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
 * Copyright (c) 2009 frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.group.area;

import java.util.HashSet;
import java.util.Set;

import org.olat.data.group.area.BGArea;
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
 * Provides a dialog to create or edit business groups.
 * 
 * @author twuersch
 */
public class BGAreaFormController extends FormBasicController {

    private TextElement name;

    private RichTextElement description;

    private String origName;

    private Set<String> validNames;

    private boolean bulkMode = false;

    private final BGArea bgArea;

    /**
     * Creates this controller.
     * 
     * @param ureq
     *            The user request.
     * @param wControl
     *            The window control.
     * @param bgArea
     *            The business group area object this dialog is referring to .
     * @param bulkMode
     *            <code>true</code> means edit more than one group at once.
     */
    public BGAreaFormController(final UserRequest ureq, final WindowControl wControl, final BGArea bgArea, final boolean bulkMode) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
        this.bgArea = bgArea;
        this.bulkMode = bulkMode;
        initForm(ureq);
    }

    /*
     * (non-Javadoc) org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, @SuppressWarnings("unused") final Controller listener, final UserRequest ureq) {
        // add the name field
        name = uifactory.addTextElement("area.form.name", "area.form.name", 255, "", formLayout);
        name.setMandatory(true);
        if (bulkMode) {
            name.setExampleKey("area.form.name.example", null);
        }

        // add the description field
        description = uifactory.addRichTextElementForStringDataMinimalistic("area.form.description", "area.form.description", "", 10, -1, false, formLayout,
                ureq.getUserSession(), getWindowControl());

        if (bgArea != null) {
            name.setValue(bgArea.getName());
            description.setValue(bgArea.getDescription());
            origName = bgArea.getName();
        }

        // Create submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("finish", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(@SuppressWarnings("unused") final UserRequest ureq) {
        // check name first
        if (!StringHelper.containsNonWhitespace(this.name.getValue())) {
            name.setErrorKey("form.legende.mandatory", new String[] {});
            return false;
        }
        if (bulkMode) {
            // check all names to be valid and check that at least one is entered
            // e.g. find "," | "   , " | ",,," errors => no group entered
            final String selectionAsCsvStr = name.getValue();
            final String[] activeSelection = selectionAsCsvStr != null ? selectionAsCsvStr.split(",") : new String[] {};
            validNames = new HashSet<String>();
            final Set<String> wrongNames = new HashSet<String>();
            for (int i = 0; i < activeSelection.length; i++) {
                if ((activeSelection[i].trim()).matches(BGArea.VALID_AREANAME_REGEXP)) {
                    validNames.add(activeSelection[i].trim());
                } else {
                    wrongNames.add(activeSelection[i].trim());
                }
            }
            if (validNames.size() == 0 && wrongNames.size() == 0) {
                // no valid name and no invalid names, this is no names
                name.setErrorKey("area.form.error.illegalName", new String[] {});
                return false;
            } else if (wrongNames.size() == 1) {
                // one invalid name
                name.setErrorKey("area.form.error.illegalName", new String[] {});
                return false;
            } else if (wrongNames.size() > 1) {
                // two or more invalid names
                final String[] args = new String[] { StringHelper.formatAsCSVString(wrongNames) };
                name.setErrorKey("create.form.error.illegalNames", args);
                return false;
            }
        } else {
            if (!name.getValue().matches(BGArea.VALID_AREANAME_REGEXP)) {
                name.setErrorKey("area.form.error.illegalName", new String[] {});
                return false;
            }
        }
        name.clearError();
        // done with name checks, now check description
        if (description.getValue().length() > 4000) {
            // description has maximum length
            description.setErrorKey("input.toolong", new String[] { "4000" });
            return false;
        }
        // ok, all checks passed
        return true;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Don't dispose anything
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void formNOK(final UserRequest ureq) {
        fireEvent(ureq, Event.FAILED_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
     * Sets this business group area's name.
     * 
     * @param areaName
     *            The new name.
     */
    public void setAreaName(final String areaName) {
        name.setValue(areaName);
    }

    /**
     * Displays an error message that this group already exists.
     * 
     * @param object
     *            <i>(unused)</i>
     */
    public void setAreaNameExistsError(@SuppressWarnings("unused") final Object object) {
        name.setErrorKey("error.area.name.exists", new String[] {});
    }

    /**
     * Gets the description text.
     * 
     * @return The description text.
     */
    public String getAreaDescription() {
        return description.getValue();
    }

    /**
     * Gets the group names (used in bulk mode).
     * 
     * @return The group names.
     */
    public Set<String> getGroupNames() {
        return validNames;
    }

    /**
     * Gets the name of this business group area.
     * 
     * @return The name of this business group area.
     */
    public String getAreaName() {
        return name.getValue().trim();
    }

    /**
     * Resets the name of this business group area to its original value.
     */
    public void resetAreaName() {
        name.setValue(origName);
    }
}
