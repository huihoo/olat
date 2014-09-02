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

package org.olat.presentation.note;

import org.olat.data.basesecurity.Identity;
import org.olat.data.note.Note;
import org.olat.lms.note.NoteService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Dec 9, 2004
 * 
 * @author Alexander Schneider
 * @author Roman Haag, frentix GmbH, 17.06.09 refactored to use FlexiForm Comment: Displays one note. Is called from every course or from the notelist in the users home.
 */
public class NoteController extends FormBasicController implements GenericEventListener {

    private NoteService noteService;
    private Note n;
    private EventBus sec;
    private RichTextElement noteField;
    private FormLink editButton;
    private FormSubmit submitButton;

    /**
     * @param ureq
     * @param wControl
     * @param n
     * @param popupTrue
     */
    public NoteController(final UserRequest ureq, final WindowControl wControl, final Note n) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        final String resourceTypeName = n.getResourceTypeName();
        final Long resourceTypeId = n.getResourceTypeId();
        final String noteTitle = n.getNoteTitle();

        init(ureq, resourceTypeName, resourceTypeId, noteTitle);
    }

    /**
     * @param ureq
     * @param ores
     *            the OLATResourceable to which this note refers to (the context of the note, e.g. a certain course)
     * @param noteTitle
     * @param popupTrue
     * @param wControl
     */
    public NoteController(final UserRequest ureq, final OLATResourceable ores, final String noteTitle, final WindowControl wControl) {
        super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
        final String resourceTypeName = ores.getResourceableTypeName();
        final Long resourceTypeId = ores.getResourceableId();

        init(ureq, resourceTypeName, resourceTypeId, noteTitle);
    }

    private void init(final UserRequest ureq, final String resourceTypeName, final Long resourceTypeId, final String noteTitle) {
        final Identity owner = ureq.getIdentity();

        this.noteService = geNoteService();
        this.n = noteService.getNote(owner, resourceTypeName, resourceTypeId);
        n.setNoteTitle(noteTitle);

        // register for local event (for the same user), is used to dispose
        // window/popup if note is deleted while open!
        sec = ureq.getUserSession().getSingleUserEventCenter();
        sec.registerFor(this, ureq.getIdentity(), OresHelper.lookupType(Note.class));

        initForm(ureq);
    }

    private NoteService geNoteService() {
        return (NoteService) CoreSpringFactory.getBean(NoteService.class);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // At beginning, the forms shows the note as a disabled field and an edit button. When the user
        // clicks the edit button, the rich text field turns to the enabled state and the edit button
        // is set to visible false and the submit button to visible true.
        setFormTitle("note", new String[] { StringHelper.escapeHtml(n.getNoteTitle()) });
        // set custom css style to override default read-only view of rich text element
        setFormStyle("o_notes");

        // we don't use FormUIFactory.addFormSubmitButton(...) here since that would cause the following custom CSS setting to get ignored.
        editButton = new FormLinkImpl("edit", "edit", "edit", Link.BUTTON_SMALL);
        editButton.setCustomEnabledLinkCSS("b_float_right b_button b_small");
        formLayout.add(editButton);

        noteField = uifactory.addRichTextElementForStringData("noteField", null, n.getNoteText(), 20, -1, false, false, null, null, formLayout, ureq.getUserSession(),
                getWindowControl());
        noteField.setEnabled(false);
        noteField.setMaxLength(4000);

        this.submitButton = uifactory.addFormSubmitButton("submit", formLayout);
        this.submitButton.setVisible(false);
    }

    private void createOrUpdateNote(final String content) {
        n.setNoteText(content);
        noteService.setNote(n);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        sec.deregisterFor(this, OresHelper.lookupType(Note.class));
    }

    /**
	 */
    @Override
    public void event(final Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent bdev = (OLATResourceableJustBeforeDeletedEvent) event;
            final Long key = n.getKey();
            if (key != null) { // already persisted
                if (bdev.getOresId().equals(key)) {
                    dispose();
                }
            }
        }
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        final String text = noteField.getValue();
        boolean allOk = true;
        if (text.length() <= 4000) {
            noteField.clearError();
        } else {
            noteField.setErrorKey("input.toolong", new String[] { "4000" });
            allOk = false;
        }
        return allOk && super.validateFormLogic(ureq);
    }

    @Override
    @SuppressWarnings("unused")
    protected void formOK(final UserRequest ureq) {
        // if the user clicked on the submit button...
        final String text = noteField.getValue();
        // ...store the text...
        createOrUpdateNote(text);

        // ...and then hide the submit button, show the edit button, and make the field disabled (i.e. display-only) again.
        this.submitButton.setVisible(false);
        this.editButton.setVisible(true);
        this.noteField.setEnabled(false);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    @SuppressWarnings("unused")
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        // persisting: see formOK

        // If the user clicked the edit button, set the rich text input field to enabled and hide the edit button.
        if ((source == this.editButton) && (this.editButton.isEnabled())) {
            this.noteField.setEnabled(true);
            this.editButton.setVisible(false);
            this.submitButton.setVisible(true);

            // this is to force the redraw of the form so that the submit button gets shown:
            flc.setDirty(true);

            // since clicking the edit button is registered as a change on the form, the submit button would get orange,
            // so we need the following line to set the form back to unchanged since at this point, the user has not
            // yet really changed anything.
            this.mainForm.setDirtyMarking(false);
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
