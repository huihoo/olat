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

package org.olat.presentation.course.nodes.projectbroker;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.course.nodes.projectbroker.CustomField;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectEvent;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManager;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */

public class InlineEditDetailsFormController extends FormBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String CUSTOM_DATE_FORMAT = "dd.MM.yyyy HH:mm";
    private static final String CHOOSER_DATE_FORMAT = "%d.%m.%Y %H:%M";
    private final String DROPDOWN_NO_SELECETION = "dropdown.nothing.selected";
    private final Project project;
    private TextElement projectTitle;
    private RichTextElement projectDescription;

    private SingleSelection projectState;
    private final String[] stateKeys;
    private final String[] stateValues;

    private IntegerElement maxMembers;
    private TextElement projectRemarks;
    private FileElement attachmentFileName;

    private TextElement projectLeaders;
    private final CourseEnvironment courseEnv;
    private final CourseNode courseNode;
    private final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;

    private final List customfieldElementList;
    private final HashMap<Project.EventType, DateChooser> eventStartElementList;
    private final HashMap<Project.EventType, DateChooser> eventEndElementList;
    private final TopicChangeConfirmationSender topicChangeConfirmationSender;

    /**
     * Modules selection form.
     * 
     * @param name
     * @param config
     */
    public InlineEditDetailsFormController(final UserRequest ureq, final WindowControl wControl, final Project project, final boolean editMode,
            final CourseEnvironment courseEnv, final CourseNode courseNode, final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
        super(ureq, wControl);
        this.project = project;
        this.courseEnv = courseEnv;
        this.courseNode = courseNode;
        this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;
        this.topicChangeConfirmationSender = new TopicChangeConfirmationSenderImpl(ureq.getIdentity(), project, courseEnv, courseNode);
        stateKeys = new String[] { Project.STATE_NOT_ASSIGNED, Project.STATE_ASSIGNED };
        stateValues = new String[] { translate(Project.STATE_NOT_ASSIGNED), translate(Project.STATE_ASSIGNED) };
        customfieldElementList = new ArrayList();
        eventStartElementList = new HashMap<Project.EventType, DateChooser>();
        eventEndElementList = new HashMap<Project.EventType, DateChooser>();
        initForm(this.flc, this, ureq);
    }

    /**
	 */
    public boolean validate() {
        return true;
    }

    /**
     * Initialize form.
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        // create form elements
        projectTitle = uifactory.addInlineTextElement("detailsform.title.label", project.getTitle(), formLayout, this);
        projectTitle.setMaxLength(100);
        projectTitle.setLabel("detailsform.title.label", null);

        // account-Managers
        final StringBuilder projectLeaderString = new StringBuilder();
        for (final Iterator iterator = project.getProjectLeaders().iterator(); iterator.hasNext();) {
            final Identity identity = (Identity) iterator.next();
            if (projectLeaderString.length() > 0) {
                projectLeaderString.append(",");
            }
            projectLeaderString.append(getUserService().getFirstAndLastname(identity.getUser()));
        }
        projectLeaders = uifactory.addTextElement("projectleaders", "detailsform.projectleaders.label", 100, projectLeaderString.toString(), formLayout);
        projectLeaders.setEnabled(false);

        // add the learning objectives rich text input element
        projectDescription = uifactory.addRichTextElementForStringData("description", "detailsform.description.label", project.getDescription(), 10, -1, false, false,
                null, null, formLayout, ureq.getUserSession(), getWindowControl());
        projectDescription.setMaxLength(2500);
        uifactory.addSpacerElement("spacer_1", formLayout, false);

        // customfields
        final List<CustomField> customFields = projectBrokerModuleConfiguration.getCustomFields();
        int customFieldIndex = 0;
        for (final Iterator<CustomField> iterator = customFields.iterator(); iterator.hasNext();) {
            final CustomField customField = iterator.next();
            log.info("customField: " + customField.getName() + "=" + customField.getValue());
            final StringTokenizer tok = new StringTokenizer(customField.getValue(), ProjectBrokerManager.CUSTOMFIELD_LIST_DELIMITER);
            if (customField.getValue() == null || customField.getValue().equals("") || !tok.hasMoreTokens()) {
                // no value define => Text-input
                // Add StaticTextElement as workaroung for non translated label
                uifactory.addStaticTextElement("customField_label" + customFieldIndex, customField.getName(), formLayout);
                final TextElement textElement = uifactory.addTextElement("customField_" + customFieldIndex, "", 20, project.getCustomFieldValue(customFieldIndex),
                        formLayout);
                // textElement.setLabelComponent(null, null);
                textElement.showLabel(false);
                // textElement.setTranslator(null);
                // textElement.setLabel(customField.getName(), null);

                customfieldElementList.add(textElement);
            } else {
                // values define => dropdown selection
                final List<String> valueList = new ArrayList<String>();
                while (tok.hasMoreTokens()) {
                    valueList.add(tok.nextToken());
                }
                final String[] theValues = new String[valueList.size() + 1];
                final String[] theKeys = new String[valueList.size() + 1];
                int arrayIndex = 0;
                theValues[arrayIndex] = translate(DROPDOWN_NO_SELECETION);
                theKeys[arrayIndex] = DROPDOWN_NO_SELECETION;
                arrayIndex++;
                for (final Iterator<String> iterator2 = valueList.iterator(); iterator2.hasNext();) {
                    final String value = iterator2.next();
                    theValues[arrayIndex] = value;
                    theKeys[arrayIndex] = value;
                    arrayIndex++;
                }
                // Add StaticTextElement as workaround for non translated label
                uifactory.addStaticTextElement("customField_label" + customFieldIndex, null, customField.getName(), formLayout);
                final SingleSelection selectionElement = uifactory.addDropdownSingleselect("customField_" + customFieldIndex, null, formLayout, theKeys, theValues, null);
                if (project.getCustomFieldValue(customFieldIndex) != null && !project.getCustomFieldValue(customFieldIndex).equals("")) {
                    if (valueList.contains(project.getCustomFieldValue(customFieldIndex))) {
                        selectionElement.select(project.getCustomFieldValue(customFieldIndex), true);
                    } else {
                        this.showInfo("warn.customfield.key.does.not.exist", project.getCustomFieldValue(customFieldIndex));
                    }
                }
                customfieldElementList.add(selectionElement);
            }
            uifactory.addSpacerElement("customField_spacer" + customFieldIndex, formLayout, false);
            customFieldIndex++;
        }

        // Events
        for (final Project.EventType eventType : Project.EventType.values()) {
            if (projectBrokerModuleConfiguration.isProjectEventEnabled(eventType)) {
                final ProjectEvent projectEvent = project.getProjectEvent(eventType);
                final DateChooser dateChooserStart = uifactory.addDateChooser(eventType + "start", "", formLayout);
                dateChooserStart.setLabel(eventType.getI18nKey() + ".start.label", null);
                dateChooserStart.setDateChooserTimeEnabled(true);
                // not i18n'ified yet
                dateChooserStart.setDateChooserDateFormat(CHOOSER_DATE_FORMAT);
                dateChooserStart.setCustomDateFormat(CUSTOM_DATE_FORMAT);
                dateChooserStart.setDisplaySize(CUSTOM_DATE_FORMAT.length());
                log.info("Event=" + eventType + ", startDate=" + projectEvent.getStartDate());
                dateChooserStart.setDate(projectEvent.getStartDate());
                eventStartElementList.put(eventType, dateChooserStart);
                final DateChooser dateChooserEnd = uifactory.addDateChooser(eventType + "end", "", formLayout);
                dateChooserEnd.setLabel(eventType.getI18nKey() + ".end.label", null);
                dateChooserEnd.setDateChooserTimeEnabled(true);
                // not i18n'ified yet
                dateChooserEnd.setDateChooserDateFormat(CHOOSER_DATE_FORMAT);
                dateChooserEnd.setCustomDateFormat(CUSTOM_DATE_FORMAT);
                dateChooserEnd.setDisplaySize(CUSTOM_DATE_FORMAT.length());
                log.info("Event=" + eventType + ", endDate=" + projectEvent.getEndDate());
                dateChooserEnd.setDate(projectEvent.getEndDate());
                eventEndElementList.put(eventType, dateChooserEnd);
                uifactory.addSpacerElement(eventType + "spacer", formLayout, false);
            }
        }

        projectState = uifactory.addDropdownSingleselect("detailsform.state.label", formLayout, stateKeys, stateValues, null);
        projectState.select(project.getState(), true);

        maxMembers = uifactory.addInlineIntegerElement("detailsform.max.members.label", project.getMaxMembers(), formLayout, this);
        maxMembers.setLabel("detailsform.max.members.label", null);
        maxMembers.setMinValueCheck(0, null);

        attachmentFileName = uifactory.addFileElement("detailsform.attachmentfilename.label", formLayout);
        attachmentFileName.setLabel("detailsform.attachmentfilename.label", null);
        if (project.getAttachmentFileName() != null && !project.getAttachmentFileName().equals("")) {
            attachmentFileName.setInitialFile(new File(project.getAttachmentFileName()));
        }
        attachmentFileName.addActionListener(this, FormEvent.ONCHANGE);

        uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        boolean projectChanged = false;
        if (!project.getTitle().equals(projectTitle.getValue())) {
            // title has been changed => change project-group name too
            final String newProjectGroupName = translate("project.member.groupname", projectTitle.getValue());
            final String newProjectGroupDescription = translate("project.member.groupdescription", projectTitle.getValue());
            ProjectBrokerManagerFactory.getProjectGroupManager().changeProjectGroupName(project.getProjectGroup(), newProjectGroupName, newProjectGroupDescription);
            ProjectBrokerManagerFactory.getProjectGroupManager().sendGroupChangeEvent(project, courseEnv.getCourseResourceableId(), ureq.getIdentity());
            projectChanged = true;
        }
        if (!project.getTitle().equals(projectTitle.getValue())) {
            project.setTitle(projectTitle.getValue());
            projectChanged = true;
        }
        if (!project.getDescription().equals(projectDescription.getValue())) {
            project.setDescription(projectDescription.getValue());
            projectChanged = true;
        }
        if (projectState.isSelected(0)) {
            if (!project.getState().equals(stateKeys[0])) {
                project.setState(stateKeys[0]);
                projectChanged = true;
            }
        } else {
            if (!project.getState().equals(stateKeys[1])) {
                project.setState(stateKeys[1]);
                projectChanged = true;
            }
        }
        if (project.getMaxMembers() != maxMembers.getIntValue()) {
            project.setMaxMembers(maxMembers.getIntValue());
            projectChanged = true;
        }
        if (attachmentFileName.getUploadFileName() != null && !attachmentFileName.getUploadFileName().equals("")) {
            project.setAttachedFileName(attachmentFileName.getUploadFileName());
            uploadFiles(attachmentFileName);
        }
        // store customfields
        int index = 0;
        for (final Iterator iterator = customfieldElementList.iterator(); iterator.hasNext();) {
            final Object element = iterator.next();
            String value = "";
            if (element instanceof TextElement) {
                final TextElement textElement = (TextElement) element;
                value = textElement.getValue();
            } else if (element instanceof SingleSelection) {
                final SingleSelection selectionElement = (SingleSelection) element;
                if (!selectionElement.getSelectedKey().equals(DROPDOWN_NO_SELECETION)) {
                    value = selectionElement.getSelectedKey();
                } else {
                    value = "";
                }
            }
            if (!project.getCustomFieldValue(index).equals(value)) {
                project.setCustomFieldValue(index, value);
                projectChanged = true;
            }
            index++;
        }
        // store events
        for (final Project.EventType eventType : eventStartElementList.keySet()) {
            final Date startDate = eventStartElementList.get(eventType).getDate();
            final Date endDate = eventEndElementList.get(eventType).getDate();
            if (!project.getProjectEvent(eventType).getStartDate().equals(startDate) || !project.getProjectEvent(eventType).getEndDate().equals(endDate)) {
                project.setProjectEvent(new ProjectEvent(eventType, startDate, endDate));
                projectChanged = true;
            }
        }
        if (projectChanged) {
            ProjectBrokerManagerFactory.getProjectBrokerManager().updateProject(project);
            topicChangeConfirmationSender.sendTopicEditConfirmation();
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    @Override
    protected void doDispose() {
        // nothing
    }

    /**
	 * 
	 */
    private void uploadFiles(final FileElement attachmentFileElement) {
        attachmentFileElement.logUpload();
        final VFSLeaf uploadedItem = new LocalFileImpl(attachmentFileElement.getUploadFile());
        ProjectBrokerManagerFactory.getProjectBrokerManager().saveAttachedFile(project, attachmentFileElement.getUploadFileName(), uploadedItem, courseEnv, courseNode);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
