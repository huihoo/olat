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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.lms.commons.fileresource.AnimationFileResource;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.commons.fileresource.DocFileResource;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.commons.fileresource.ImageFileResource;
import org.olat.lms.commons.fileresource.ImsCPFileResource;
import org.olat.lms.commons.fileresource.MovieFileResource;
import org.olat.lms.commons.fileresource.PdfFileResource;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.fileresource.PowerpointFileResource;
import org.olat.lms.commons.fileresource.ScormCPFileResource;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.commons.fileresource.SoundFileResource;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.commons.fileresource.XlsFileResource;
import org.olat.lms.course.CourseModule;
import org.olat.lms.portfolio.EPTemplateMapResource;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 08.07.2003
 * 
 * @author Mike Stock Comment: The search form captures data to search for a repository entry. The form can be restricted to a special type
 */
public class SearchForm extends FormBasicController {

    private TextElement id; // only for admins
    private TextElement displayName;
    private TextElement author;
    private TextElement description;
    private SelectionElement typesSelection;
    private MultipleSelectionElement types;
    private FormLink searchButton;

    private String limitUsername;
    private String limitType;
    private String[] limitTypes;
    private final boolean withCancel;
    private final boolean isAdmin;

    /**
     * Generic search form.
     * 
     * @param name
     *            Internal form name.
     * @param translator
     *            Translator
     * @param withCancel
     *            Display a cancel button?
     * @param isAdmin
     *            Is calling identity an administrator? If yes, allow search by ID
     * @param limitType
     *            Limit searches to a specific type.
     * @param limitUser
     *            Limit searches to a specific user.
     */
    public SearchForm(final UserRequest ureq, final WindowControl wControl, final boolean withCancel, final boolean isAdmin, final String limitType,
            final String limitUser) {
        this(ureq, wControl, withCancel, isAdmin);
        this.limitType = limitType;
        this.limitUsername = limitUser;
    }

    /**
     * Generic search form.
     * 
     * @param name
     *            Internal form name.
     * @param translator
     *            Translator
     * @param withCancel
     *            Display a cancel button?
     * @param isAdmin
     *            Is calling identity an administrator? If yes, allow search by ID
     * @param limitTypes
     *            Limit searches to specific types.
     */
    public SearchForm(final UserRequest ureq, final WindowControl wControl, final boolean withCancel, final boolean isAdmin) {
        super(ureq, wControl);
        this.withCancel = withCancel;
        this.isAdmin = isAdmin;
        initForm(ureq);
    }

    public SearchForm(final UserRequest ureq, final WindowControl wControl, final boolean withCancel, final boolean isAdmin, final String[] limitTypes) {
        this(ureq, wControl, withCancel, isAdmin);
        this.limitTypes = limitTypes;
        update();
    }

    @Override
    protected boolean validateFormLogic(@SuppressWarnings("unused") final UserRequest ureq) {
        if (displayName.isEmpty() && author.isEmpty() && description.isEmpty() && (id != null && id.isEmpty())) {
            showWarning("cif.error.allempty", null);
            return false;
        }
        return true;
    }

    /**
     * @return Is ID field available?
     */
    public boolean hasId() {
        return (id != null && !id.isEmpty());
    }

    /**
     * @return Return value of ID field.
     */
    public Long getId() {
        if (!hasId()) {
            throw new AssertException("Should not call getId() if there is no id. Check with hasId() before.");
        }
        return new Long(id.getValue());
    }

    /**
     * @return Display name filed value.
     */
    public String getDisplayName() {
        return displayName.getValue();
    }

    /**
     * @return Author field value.
     */
    public String getAuthor() {
        return author.getValue();
    }

    /**
     * @return Descritpion field value.
     */
    public String getDescription() {
        return description.getValue();
    }

    /**
     * @return Limiting type selections.
     */
    public Set<String> getRestrictedTypes() {

        if (limitTypes != null && limitTypes.length > 0) {
            return new HashSet<String>(Arrays.asList(limitTypes));
        }

        return types.getSelectedKeys();
    }

    public void setVisible(final boolean onoff) {
        flc.setVisible(onoff);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        flc.reset();
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    private void update() {
        if (limitTypes != null && limitTypes.length > 0) {
            typesSelection.setVisible(false);
            types.setVisible(false);
        } else {
            types.setVisible(typesSelection.isSelected(0));
            types.uncheckAll();
        }
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == searchButton) {
            flc.getRootForm().submit(ureq);
        } else if (source == typesSelection && event.getCommand().equals("ONCLICK")) {
            update();
        }
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        setFormTitle("search.generic");

        displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", formLayout);
        displayName.setFocus(true);

        author = uifactory.addTextElement("cif_author", "cif.author", 255, "", formLayout);
        if (limitUsername != null) {
            author.setValue(limitUsername);
            author.setEnabled(false);
        }
        description = uifactory.addTextElement("cif_description", "cif.description", 255, "", formLayout);

        id = uifactory.addTextElement("cif_id", "cif.id", 12, "", formLayout);
        id.setVisible(isAdmin);
        id.setRegexMatchCheck("\\d*", "search.id.format");

        typesSelection = uifactory.addCheckboxesVertical("search.limit.type", formLayout, new String[] { "xx" }, new String[] { "" }, new String[] { null }, 1);
        typesSelection.addActionListener(listener, FormEvent.ONCLICK);

        types = uifactory.addCheckboxesVertical("cif_types", "cif.type", formLayout, getResources().toArray(new String[0]), getTranslatedResources(getResources()), null,
                1);

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);

        searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
        if (withCancel) {
            uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
        }

        update();
    }

    @Override
    protected void doDispose() {
        //
    }

    private String[] getTranslatedResources(final List<String> resources) {
        final List<String> l = new ArrayList<String>();
        for (final String key : resources) {
            l.add(translate(key));
        }
        return l.toArray(new String[0]);
    }

    private List<String> getResources() {
        final List<String> resources = new ArrayList<String>();
        resources.add(CourseModule.getCourseTypeName());
        resources.add(ImsCPFileResource.TYPE_NAME);
        resources.add(ScormCPFileResource.TYPE_NAME);
        resources.add(WikiResource.TYPE_NAME);
        resources.add(PodcastFileResource.TYPE_NAME);
        resources.add(BlogFileResource.TYPE_NAME);
        resources.add(TestFileResource.TYPE_NAME);
        resources.add(SurveyFileResource.TYPE_NAME);
        resources.add(EPTemplateMapResource.TYPE_NAME);
        resources.add(SharedFolderFileResource.TYPE_NAME);
        resources.add(GlossaryResource.TYPE_NAME);
        resources.add(PdfFileResource.TYPE_NAME);
        resources.add(XlsFileResource.TYPE_NAME);
        resources.add(PowerpointFileResource.TYPE_NAME);
        resources.add(DocFileResource.TYPE_NAME);
        resources.add(AnimationFileResource.TYPE_NAME);
        resources.add(ImageFileResource.TYPE_NAME);
        resources.add(SoundFileResource.TYPE_NAME);
        resources.add(MovieFileResource.TYPE_NAME);
        resources.add(FileResource.GENERIC_TYPE_NAME);
        return resources;
    }

}
