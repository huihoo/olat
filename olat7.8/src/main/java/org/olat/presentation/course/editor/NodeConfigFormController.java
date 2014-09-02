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

package org.olat.presentation.course.editor;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Provides a FlexiForm that lets the user configure details for a course node.
 * 
 * @author twuersch
 */
public class NodeConfigFormController extends FormBasicController {

    private final static String[] displayOptionsKeys = new String[] { CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT, CourseNode.DISPLAY_OPTS_TITLE_CONTENT,
            CourseNode.DISPLAY_OPTS_CONTENT };

    private final String menuTitle;

    private final String displayTitle;

    private final String learningObjectives;

    private final String displayOption;

    /**
     * Input element for this course's short title.
     */
    private TextElement shortTitle;

    /**
     * Input element for this course's title.
     */
    private TextElement title;

    /**
     * Input element for the description of this course's objectives.
     */
    private RichTextElement objectives;

    /**
     * Selection fot the options title
     */
    private SingleSelection displayOptions;

    /**
     * Decides whether to show a <i>cancel</i> button.
     */
    private final boolean withCancel;

    /**
     * Initializes this controller.
     * 
     * @param ureq
     *            The user request.
     * @param wControl
     *            The window control.
     * @param courseNode
     *            The course node this controller will access.
     * @param withCancel
     *            Decides whether to show a <i>cancel</i> button.
     */
    public NodeConfigFormController(final UserRequest ureq, final WindowControl wControl, final CourseNode courseNode, final boolean withCancel) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
        this.withCancel = withCancel;
        menuTitle = Formatter.truncate(courseNode.getShortTitle(), CourseNode.SHORT_TITLE_MAX_LENGTH);
        displayTitle = courseNode.getLongTitle();
        learningObjectives = courseNode.getLearningObjectives();
        displayOption = courseNode.getDisplayOption();
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Don't dispose anything.

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
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // add the short title text input element
        shortTitle = uifactory.addTextElement("nodeConfigForm.menutitle", "nodeConfigForm.menutitle", CourseNode.SHORT_TITLE_MAX_LENGTH, (menuTitle == null ? ""
                : menuTitle), formLayout);
        shortTitle.setMandatory(true);

        // add the title input text element
        title = uifactory.addTextElement("nodeConfigForm.displaytitle", "nodeConfigForm.displaytitle", 255, (displayTitle == null ? "" : displayTitle), formLayout);

        // add the learning objectives rich text input element
        objectives = uifactory.addRichTextElementForStringData("nodeConfigForm.learningobjectives", "nodeConfigForm.learningobjectives", (learningObjectives == null ? ""
                : learningObjectives), 10, -1, false, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
        objectives.setMaxLength(4000);

        final String[] values = new String[] { translate("nodeConfigForm.title_desc_content"), translate("nodeConfigForm.title_content"),
                translate("nodeConfigForm.content_only") };
        displayOptions = uifactory.addDropdownSingleselect("displayOptions", "nodeConfigForm.display_options", formLayout, displayOptionsKeys, values, null);
        displayOptions.select(displayOption, true);

        // Create submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("nodeConfigForm.save", buttonLayout);
        if (withCancel) {
            uifactory.addFormCancelButton("search.form.cancel", buttonLayout, ureq, getWindowControl());
        }
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean shortTitleOk = true;
        if (!StringHelper.containsNonWhitespace(shortTitle.getValue())) {
            // the short title is mandatory
            shortTitle.setErrorKey("nodeConfigForm.menumust", new String[] {});
            shortTitleOk = false;
        }
        if (shortTitleOk && super.validateFormLogic(ureq)) {
            shortTitle.clearError();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the short title.
     * 
     * @return The short title.
     */
    public String getMenuTitle() {
        return shortTitle.getValue();
    }

    /**
     * Gets the title.
     * 
     * @return The title.
     */
    public String getDisplayTitle() {
        return title.getValue();
    }

    /**
     * Gets the description of this course's objectives.
     * 
     * @return The description of this course's objectives.
     */
    public String getLearningObjectives() {
        return objectives.getValue();
    }

    /**
     * Return the selected option
     * 
     * @return
     */
    public String getDisplayOption() {
        return displayOptions.getSelectedKey();
    }
}
