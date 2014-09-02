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

package org.olat.presentation.portal.campus;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.learn.campus.service.CampusCourseLearnService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableSingleSelectEvent;
import org.olat.presentation.framework.core.components.text.TextComponent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.RepositoryTableModel;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * 
 * Initial Date: 23.10.2012 <br>
 * 
 * @author aabouc
 */
public class CampusCourseCreationController extends BasicController {

    public static final int COURSE_CREATION_BY_TEMPLATE = 0;
    public static final int COURSE_CREATION_BY_COPYING = 1;
    public static final int COURSE_CONTINUATION = 2;

    private static final String CREATION_SUBMIT = "campus.course.creation.submit";
    private static final String CONTINUATION_SUBMIT = "campus.course.continuation.submit";
    private static final String CANCEL = "cancel";

    private static final int RESULTS_PER_PAGE = 5;

    private final VelocityContainer campusCourseVC;

    private CourseCreationChoiceController courseCreationChoiceCtrl;

    private RepositoryTableModel creationTableModel, continuationTableModel;

    private TableController creationTableCtrl, continuationTableCtrl;

    private TextComponent infoTextComp;

    private DialogBoxController courseContinuationDialog;

    private Link cancelButton, submitButton;

    private long selectedResouceableId;

    private String campusCourseTitle;

    private CampusCourseLearnService campusLearnService;

    /**
     * Constructor
     * 
     * @param ureq
     * @param component
     */
    public CampusCourseCreationController(final WindowControl wControl, final UserRequest ureq, String campusCourseTitle) {
        super(ureq, wControl);

        campusLearnService = getService(LearnServices.campusCourseLearnService);

        setCampusCourseTitle(campusCourseTitle);

        this.campusCourseVC = this.createVelocityContainer("campusCourseCreation");

        final Translator resourceTrans = PackageUtil.createPackageTranslator(RepositoryTableModel.class, ureq.getLocale(), getTranslator());

        // Create the CourseCreationChoiceController
        courseCreationChoiceCtrl = new CourseCreationChoiceController(wControl, ureq);
        listenTo(courseCreationChoiceCtrl);

        // Create the TableGuiConfiguration
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setResultsPerPage(RESULTS_PER_PAGE);
        tableConfig.setPreferencesOffered(true, "CampusCourseCreationTableGuiPrefs");

        // Create the RepositoryTableModel
        creationTableModel = new RepositoryTableModel(resourceTrans);
        continuationTableModel = new RepositoryTableModel(resourceTrans);

        // Create the TableController
        creationTableCtrl = createTableCtrl(resourceTrans, tableConfig, ureq, CREATION_SUBMIT);
        creationTableCtrl.setTableDataModel(creationTableModel);

        continuationTableCtrl = createTableCtrl(resourceTrans, tableConfig, ureq, CONTINUATION_SUBMIT);
        continuationTableCtrl.setTableDataModel(continuationTableModel);

        // Create the submit and the cancel Buttons
        cancelButton = LinkFactory.createButton(CANCEL, campusCourseVC, this);
        submitButton = LinkFactory.createButton(CREATION_SUBMIT, campusCourseVC, this);

        campusCourseVC.put("courseCreationChoice", courseCreationChoiceCtrl.getInitialComponent());

        campusCourseVC.contextPut("createByTemplate", true);

        putInitialPanel(campusCourseVC);
    }

    private TableController createTableCtrl(Translator resourceTrans, TableGuiConfiguration tableConfig, UserRequest ureq, final String SUBMIT) {
        TableController tableCtrl = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans, true);

        tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("campus.course.table.header.displayname", 1, null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 2, null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(false, new DefaultColumnDescriptor("campus.course.table.header.access", 3, null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", 4, null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", 5, null, resourceTrans.getLocale()));

        tableCtrl.setSortColumn(0, true);
        tableCtrl.setSingleSelect(true);
        tableCtrl.addSingleSelectAction(CANCEL, CANCEL);
        tableCtrl.addSingleSelectAction(SUBMIT, SUBMIT);

        listenTo(tableCtrl);

        return tableCtrl;
    }

    public int getCourseCreationSelected() {
        return courseCreationChoiceCtrl.campusCourseCreationRadioButtons.getSelected();
    }

    public long getSelectedResouceableId() {
        return selectedResouceableId;
    }

    public String getCampusCourseTitle() {
        return campusCourseTitle;
    }

    public void setCampusCourseTitle(String campusCourseTitle) {
        this.campusCourseTitle = campusCourseTitle;
    }

    private void refreshCreationModel(List<RepositoryEntry> entries) {
        creationTableModel.setObjects(entries);
        creationTableCtrl.modelChanged(true);
    }

    private void refreshContinuationModel(List<RepositoryEntry> entries) {
        continuationTableModel.setObjects(entries);
        continuationTableCtrl.modelChanged(true);
    }

    private void update(int courseCreationSelection, UserRequest ureq) {
        switch (courseCreationSelection) {
        case COURSE_CREATION_BY_TEMPLATE:
            campusCourseVC.remove(infoTextComp);
            campusCourseVC.remove(creationTableCtrl.getInitialComponent());
            campusCourseVC.remove(continuationTableCtrl.getInitialComponent());
            campusCourseVC.contextPut("createByTemplate", true);
            break;
        case COURSE_CREATION_BY_COPYING:
            campusCourseVC.remove(continuationTableCtrl.getInitialComponent());
            refreshCreationModel(RepositoryServiceImpl.getInstance().queryByOwner(ureq.getIdentity(), "CourseModule"));
            campusCourseVC.put("tableController", creationTableCtrl.getInitialComponent());
            campusCourseVC.contextPut("createByTemplate", false);
            break;

        case COURSE_CONTINUATION:
            campusCourseVC.remove(creationTableCtrl.getInitialComponent());
            List<RepositoryEntry> campusCourseEntries = new ArrayList<RepositoryEntry>();
            List<RepositoryEntry> entries = RepositoryServiceImpl.getInstance().queryByOwner(ureq.getIdentity(), "CourseModule");
            if (!entries.isEmpty()) {
                List<Long> allCreatedSapCourcesResourceableIds = campusLearnService.getAllCreatedSapCourcesResourceableIds();
                for (RepositoryEntry entry : entries) {
                    Long resourcableId = entry.getOlatResource().getResourceableId();
                    if (allCreatedSapCourcesResourceableIds.contains(resourcableId)) {
                        if (StringUtils.left(getCampusCourseTitle(), 4).compareTo(StringUtils.left(entry.getDisplayname(), 4)) > 0) {
                            campusCourseEntries.add(entry);
                        }
                    }
                }
            }
            refreshContinuationModel(campusCourseEntries);
            campusCourseVC.put("tableController", continuationTableCtrl.getInitialComponent());
            campusCourseVC.contextPut("createByTemplate", false);
            break;
        }
        campusCourseVC.setDirty(true);
    }

    private void openErrorDialog(final UserRequest ureq, String errorText) {
        showError(errorText);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == cancelButton) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else if (source == submitButton) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        if (source == courseCreationChoiceCtrl) {
            update(courseCreationChoiceCtrl.campusCourseCreationRadioButtons.getSelected(), ureq);
        }

        else if (source == courseContinuationDialog) {
            if (event.equals(Event.CANCELLED_EVENT)) {
                // nothing to do
            } else if (DialogBoxUIFactory.getButtonPos(event) == 0) {
                fireEvent(ureq, Event.DONE_EVENT);
            }
        } else if (source == creationTableCtrl || source == continuationTableCtrl) {
            if (!(event instanceof TableSingleSelectEvent)) {
                throw new AssertException("Expected TableSingleSelectEvent");
            }
            TableSingleSelectEvent tsse = (TableSingleSelectEvent) event;
            if (tsse.getAction().equals(CANCEL)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }

            else if (tsse.getAction().equals(CREATION_SUBMIT)) {
                if (tsse.getSelection() == Table.NO_ROW_SELECTED || creationTableModel.getObject(tsse.getSelection()) == null) {
                    openErrorDialog(ureq, "popup.course.creation.noSelection.text");
                } else {
                    final RepositoryEntry repoEntry = (RepositoryEntry) creationTableCtrl.getTableDataModel().getObject(tsse.getSelection());
                    final OLATResource ores = OLATResourceManager.getInstance().findResourceable(repoEntry.getOlatResource());
                    selectedResouceableId = ores.getResourceableId();
                    fireEvent(ureq, Event.DONE_EVENT);
                }
            }

            else if (tsse.getAction().equals(CONTINUATION_SUBMIT)) {
                if (tsse.getSelection() == Table.NO_ROW_SELECTED || continuationTableModel.getObject(tsse.getSelection()) == null) {
                    openErrorDialog(ureq, "popup.course.creation.noSelection.text");
                } else {

                    final RepositoryEntry repoEntry = (RepositoryEntry) continuationTableCtrl.getTableDataModel().getObject(tsse.getSelection());
                    final OLATResource ores = OLATResourceManager.getInstance().findResourceable(repoEntry.getOlatResource());
                    selectedResouceableId = ores.getResourceableId();
                    openCourseContinuationDialog(ureq, repoEntry.getDisplayname());
                }
            }
        }
    }

    private void openCourseContinuationDialog(final UserRequest ureq, String courseTitle) {
        List<String> buttonLabels = new ArrayList<String>();
        buttonLabels.add(translate("popup.course.continuation.button.label.yes"));
        buttonLabels.add(translate("popup.course.continuation.button.label.no"));
        String text = translate("popup.course.continuation.text", StringHelper.escapeHtml(courseTitle));
        courseContinuationDialog = activateGenericDialog(ureq, translate("popup.course.continuation.title", courseTitle), text, buttonLabels, courseContinuationDialog);
    }

    @Override
    protected void doDispose() {
    }

    class CourseCreationChoiceController extends FormBasicController {
        SingleSelection campusCourseCreationRadioButtons;

        static final String CC_CREATION_BY_TEMPLTE = "campus.course.creation.by.template";
        static final String CC_CREATION_BY_COPYING = "campus.course.creation.by.copying";
        static final String CC_CONTINUATION = "campus.course.continuation";

        String[] campusCourseCreationKeys = new String[] { CC_CREATION_BY_TEMPLTE, CC_CREATION_BY_COPYING, CC_CONTINUATION };
        String[] campusCourseCreationOptions = new String[campusCourseCreationKeys.length];

        public CourseCreationChoiceController(final WindowControl wControl, final UserRequest ureq) {
            super(ureq, wControl, LAYOUT_VERTICAL);
            initForm(ureq);
        }

        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
            this.addSelections(formLayout);
        }

        private void addSelections(final FormItemContainer formLayout) {
            for (int i = 0; i < campusCourseCreationKeys.length; i++) {
                campusCourseCreationOptions[i] = translate(campusCourseCreationKeys[i]);
            }
            campusCourseCreationRadioButtons = uifactory.addRadiosVertical("campus.course.creation.radio", formLayout, campusCourseCreationKeys,
                    campusCourseCreationOptions);
            campusCourseCreationRadioButtons.select(campusCourseCreationKeys[0], true);
            campusCourseCreationRadioButtons.addActionListener(this, FormEvent.ONCLICK);
        }

        public int getSelection() {
            return campusCourseCreationRadioButtons.getSelected();
        }

        @Override
        protected void doDispose() {
        }

        @Override
        protected void formOK(UserRequest ureq) {
        }

        @Override
        protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
            fireEvent(ureq, event);
        }

    }

}
