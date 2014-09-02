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
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.lms.learn.campus.service.CampusCourseLearnService;
import org.olat.lms.learn.campus.service.SapCampusCourseTo;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.presentation.framework.core.control.generic.portal.PortletEntry;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.repository.EntryChangedEvent;
import org.olat.presentation.repository.RepositoryDetailsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Run view controller for campus course to see my-course.
 * 
 * @author Christian Guretzki
 */
public class CampusCoursePortletRunController extends BasicController implements GenericEventListener {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String CREATE_CAMPUS_COURSE_LOCK = "createCampusCourseLock";

    private static final String GO_TO_COURSE_TBL_VELOCITY_KEY = "go_to_course_tbl";
    private static final String TO_CREATE_COURSE_LIST_VELOCITY_KEY = "to_create_course_list";

    private static final String NOT_YET_CREATED_COURSE_LIST_VELOCITY_KEY = "not_yet_created_course_list";
    private static final String NOT_YET_ACTIVATED_COURSE_LIST_VELOCITY_KEY = "not_yet_activated_course_list";

    private static final int PORTLET_TITLE_MAX_LENGTH = 70;

    private final VelocityContainer campusVC;

    private CampusCourseLearnService campusLearnService;
    private RepositoryService repositoryService;

    private List<CampusCoursePortletEntry> coursesToCreateForLecturer;

    private List<Long> olatcourseIds = new ArrayList<Long>();
    private List<Long> sapcourseIds = new ArrayList<Long>();

    private DialogBoxController courseCreatedDialog;

    private CampusCourse campusCourse;

    private LockResult lockEntry;

    private CampusCourseCreationController campusCourseCreationController;

    private Controller editorController;

    private CampusCoursePortletEntry clickedEntry;

    private CloseableModalController cmc;

    private TableController courseTableCtr;
    private CourseTableDataModel courseTableModel;

    private static final String CMD_LAUNCH = "cmd.launch";

    private Roles roles;

    private String[] campusCourseIdentifiers;

    /**
     * Constructor
     * 
     * @param ureq
     * @param component
     */
    public CampusCoursePortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName) {
        super(ureq, wControl);

        campusLearnService = getService(LearnServices.campusCourseLearnService);
        repositoryService = getService(LearnServices.repositoryService);

        this.roles = ureq.getUserSession().getRoles();

        this.campusVC = this.createVelocityContainer("campusPortlet");

        campusCourseIdentifiers = CoreSpringFactory.getBean(org.olat.lms.core.course.campus.CampusConfiguration.class).getDescriptionStartWithStringAsArray();

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setDisplayTableHeader(false);
        tableConfig.setTableEmptyMessage(trans.translate("campusPortlet.noCourseToBeOpened"));
        tableConfig.setCustomCssClass("b_portlet_table");
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);
        tableConfig.setDownloadOffered(false);
        tableConfig.setSortingEnabled(false);
        courseTableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
        courseTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("", 0, CMD_LAUNCH, trans.getLocale()));
        listenTo(courseTableCtr);

        updateContainerContent();

        putInitialPanel(campusVC);

        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(RepositoryEntry.class));
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CampusCourse.class));

    }

    private void updateContainerContent() {
        for (Object key : campusVC.getContext().getKeys()) {
            campusVC.getContext().remove(key);
        }

        olatcourseIds.clear();
        sapcourseIds.clear();

        coursesToCreateForLecturer = initializeCreateCourseLinksForLecturer(getIdentity());
        initializeCreateCourseLinksForStudent(getIdentity());
        reloadCourseTableModel();

        campusVC.setDirty(true);
    }

    private void prepareModelEntries(List<PortletEntry> entries, List<SapCampusCourseTo> notYetActivatedEntries, SapOlatUser.SapUserType userType) {
        for (SapCampusCourseTo campusCourseTo : campusLearnService.getCoursesWhichCouldBeOpened(getIdentity(), userType)) {
            if (campusCourseTo.getOlatCourseId() != null) {
                final OLATResource olatResource = CoreSpringFactory.getBean(OLATResourceManager.class).findResourceable(campusCourseTo.getOlatCourseId(), "CourseModule");
                final RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntry(olatResource, false);
                if (repoEntry != null) {
                    olatcourseIds.add(repoEntry.getOlatResource().getResourceableId());
                    if (userType.equals(SapOlatUser.SapUserType.LECTURER)
                            || (userType.equals(SapOlatUser.SapUserType.STUDENT) && repositoryService.isAllowedToLaunch(getIdentity(), this.roles, repoEntry))) {
                        String title = DynamicTabHelper.getDisplayName(repoEntry, getLocale());
                        campusCourseTo.setTitle(title);
                        campusCourseTo.setActivated(repoEntry.getAccess() >= RepositoryEntry.ACC_USERS);
                        entries.add(new SapCampusCoursePortletEntry(campusCourseTo));
                    } else {
                        notYetActivatedEntries.add(campusCourseTo);
                    }
                }
            }
        }
    }

    private void reloadCourseTableModel() {
        final List<PortletEntry> entries = new ArrayList<PortletEntry>();
        final List<SapCampusCourseTo> notYetActivatedEntries = new ArrayList<SapCampusCourseTo>();
        // PREPARE THE MODEL ENTRIES OF COURSES TO BE OPENED IN THE CASE OF LECTURER
        prepareModelEntries(entries, notYetActivatedEntries, SapOlatUser.SapUserType.LECTURER);
        // PREPARE THE MODEL ENTRIES OF COURSES TO BE OPENED IN THE CASE OF STUDENT
        prepareModelEntries(entries, notYetActivatedEntries, SapOlatUser.SapUserType.STUDENT);

        courseTableModel = new CourseTableDataModel(entries, getLocale());
        courseTableCtr.setTableDataModel(courseTableModel);

        campusVC.put(GO_TO_COURSE_TBL_VELOCITY_KEY, courseTableCtr.getInitialComponent());

        List<CampusCoursePortletEntry> campusCourses = getCampusCoursePortletEntryList(notYetActivatedEntries, "to_create_button_id");
        for (CampusCoursePortletEntry campusCourse : campusCourses) {
            Link link = LinkFactory.createLink(campusCourse.getCourseTitleForPortlet(), campusVC, this);
            link.setTooltip(getTranslator().translate("campusPortlet.course.notYetActivatedForStudents.hover"), false);
            link.setUserObject(campusCourse);
            link.setCustomDisplayText(campusCourse.getCourseTitleForPortlet());
            link.setEnabled(false);

            sapcourseIds.add(campusCourse.getSapCourseId());
        }

        campusVC.contextPut(NOT_YET_ACTIVATED_COURSE_LIST_VELOCITY_KEY, campusCourses);
    }

    private List<CampusCoursePortletEntry> initializeCreateCourseLinksForLecturer(Identity identity) {
        List<CampusCoursePortletEntry> campusCourses = getCoursesWhichCouldBeCreated(identity, SapOlatUser.SapUserType.LECTURER);
        for (CampusCoursePortletEntry campusCourse : campusCourses) {
            Link link = LinkFactory.createButtonSmall(campusCourse.getButtonId(), campusVC, this);
            link.setUserObject(campusCourse);
            link.setCustomDisplayText(getTranslator().translate("button.create.course"));

            sapcourseIds.add(campusCourse.getSapCourseId());
        }
        campusVC.contextPut(TO_CREATE_COURSE_LIST_VELOCITY_KEY, campusCourses);
        return campusCourses;
    }

    private List<CampusCoursePortletEntry> initializeCreateCourseLinksForStudent(Identity identity) {
        List<CampusCoursePortletEntry> campusCourses = getCoursesWhichCouldBeCreated(identity, SapOlatUser.SapUserType.STUDENT);
        for (CampusCoursePortletEntry campusCourse : campusCourses) {
            Link link = LinkFactory.createLink(campusCourse.getCourseTitleForPortlet(), campusVC, this);
            link.setTooltip(getTranslator().translate("campusPortlet.course.notYetCreated.hover"), false);
            link.setUserObject(campusCourse);
            link.setCustomDisplayText(campusCourse.getCourseTitleForPortlet());
            link.setEnabled(false);

            sapcourseIds.add(campusCourse.getSapCourseId());
        }
        campusVC.contextPut(NOT_YET_CREATED_COURSE_LIST_VELOCITY_KEY, campusCourses);
        return campusCourses;

    }

    private List<CampusCoursePortletEntry> getCoursesWhichCouldBeCreated(Identity identity, SapOlatUser.SapUserType userType) {
        String buttonIdPrefix = "to_create_button_id";
        return getCampusCoursePortletEntryList(campusLearnService.getCoursesWhichCouldBeCreated(identity, userType), buttonIdPrefix);
    }

    private List<CampusCoursePortletEntry> getCampusCoursePortletEntryList(List<SapCampusCourseTo> coursesWhichCouldBeCreated, String buttonIdPrefix) {
        List<CampusCoursePortletEntry> portletEntryList = new ArrayList<CampusCoursePortletEntry>();
        int i = 1;
        for (SapCampusCourseTo campusCourseTo : coursesWhichCouldBeCreated) {
            portletEntryList.add(new CampusCoursePortletEntry(campusCourseTo.getTitle(), campusCourseTo.getSapCourseId(), campusCourseTo.getOlatCourseId(),
                    buttonIdPrefix + i++));
        }

        return portletEntryList;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source instanceof Link) {
            Link clickedLink = (Link) source;
            CampusCoursePortletEntry clickedEntry = (CampusCoursePortletEntry) clickedLink.getUserObject();
            setClickedEntry(clickedEntry);
            log.debug("Clicked entry title=" + clickedEntry.getCourseTitle() + "  buttonId=" + clickedEntry.getButtonId());
            if (coursesToCreateForLecturer.contains(clickedEntry)) {
                RepositoryEntry repositoryEntry = campusLearnService.getRepositoryEntryFor(clickedEntry.getSapCourseId());
                if (repositoryEntry != null) {
                    showAboutCourseAlreadyCreated(repositoryEntry.getInitialAuthor());
                    refreshContainer();
                    return;
                }
                if (!campusLearnService.checkDelegation(clickedEntry.getSapCourseId(), ureq.getIdentity())) {
                    openCourseNotCreatedDialogBecauseOfRemovedDelegation(ureq, clickedEntry.getCourseTitle());
                    refreshContainer();
                    return;
                }
                OLATResourceable lockResourceable = OresHelper.createOLATResourceableInstance(CampusCoursePortletRunController.class, clickedEntry.getSapCourseId());
                lockEntry = getLockingService().acquireLock(lockResourceable, ureq.getIdentity(), CREATE_CAMPUS_COURSE_LOCK);
                if (lockEntry.isSuccess()) {
                    campusCourseCreationController = new CampusCourseCreationController(getWindowControl(), ureq, clickedEntry.getCourseTitle());
                    listenTo(campusCourseCreationController);
                    cmc = new CloseableModalController(getWindowControl(), translate("close"), campusCourseCreationController.getInitialComponent(), true,
                            translate("campus.course.creation.title"));
                    listenTo(cmc);
                    cmc.activate();

                } else {
                    this.showInfo("info.campus.course.create.locked.by", lockEntry.getOwner().getName());
                }
            }
        }
    }

    private void openCourseNotCreatedDialog(final UserRequest ureq, String courseTitle) {
        showError("popup.course.notCreated.text", courseTitle);
    }

    private void openCourseNotCreatedDialogBecauseOfRemovedDelegation(final UserRequest ureq, String courseTitle) {
        showError("popup.course.notCreated.becauseOfRemovedDelegation.text", courseTitle);
    }

    private void openCourseNotContinuedDialog(final UserRequest ureq, String courseTitle) {
        showError("popup.course.notContinued.text", courseTitle);
    }

    private void openCourseNotContinuedDialogBecauseOfRemovedDelegation(final UserRequest ureq, String courseTitle) {
        showError("popup.course.notContinued.becauseOfRemovedDelegation.text", courseTitle);
    }

    private void showAboutCourseAlreadyCreated(final String createdBy) {
        showInfo("info.campus.course.already.created.by", createdBy);
    }

    private void createCampusCourse(final UserRequest ureq) {
        RepositoryEntry repositoryEntry = campusLearnService.getRepositoryEntryFor(clickedEntry.getSapCourseId());
        if (repositoryEntry != null) {
            showAboutCourseAlreadyCreated(repositoryEntry.getInitialAuthor());
            refreshContainer();
            return;
        }
        switch (campusCourseCreationController.getCourseCreationSelected()) {
        case CampusCourseCreationController.COURSE_CREATION_BY_TEMPLATE:
            createCampusCourseFromDefaultTemplate(ureq);
            break;
        case CampusCourseCreationController.COURSE_CREATION_BY_COPYING:
            createCampusCourseFromCustomTemplate(ureq);
            break;

        case CampusCourseCreationController.COURSE_CONTINUATION:
            continueCampusCourse(ureq);
            break;
        }
        // TODO: ??
        // refreshContainer();
        CoordinatorManager.getInstance().getCoordinator().getEventBus()
                .fireEventToListenersOf(new CampusCourseEvent(clickedEntry.getSapCourseId(), CampusCourseEvent.CREATED), OresHelper.lookupType(CampusCourse.class));
    }

    private void createCampusCourseFromDefaultTemplate(final UserRequest ureq) {
        if (!campusLearnService.checkDelegation(clickedEntry.getSapCourseId(), ureq.getIdentity())) {
            openCourseNotCreatedDialogBecauseOfRemovedDelegation(ureq, clickedEntry.getCourseTitle());
            return;
        }
        campusCourse = campusLearnService.createCampusCourseFromTemplate(null, clickedEntry.getSapCourseId(), ureq.getIdentity());
        checkCreatedCampusCourse(campusCourse, ureq, false);

    }

    private void createCampusCourseFromCustomTemplate(final UserRequest ureq) {
        if (!campusLearnService.checkDelegation(clickedEntry.getSapCourseId(), ureq.getIdentity())) {
            openCourseNotCreatedDialogBecauseOfRemovedDelegation(ureq, clickedEntry.getCourseTitle());
            return;
        }
        campusCourse = campusLearnService.createCampusCourseFromTemplate(campusCourseCreationController.getSelectedResouceableId(), clickedEntry.getSapCourseId(),
                ureq.getIdentity());
        checkCreatedCampusCourse(campusCourse, ureq, true);
        showWarning("warning.campus.course.creationByCopying.adapt.visibilityAndAccess");
    }

    private void checkCreatedCampusCourse(CampusCourse campusCourse, final UserRequest ureq, boolean openWithCourseEditor) {
        if (campusCourse == null) {
            openCourseNotCreatedDialog(ureq, clickedEntry.getCourseTitle());
        } else {
            if (alreadyCreated(campusCourse, ureq)) {
                showAboutCourseAlreadyCreated(campusCourse.getRepositoryEntry().getInitialAuthor());
            } else {
                if (openWithCourseEditor) {
                    openTabWithCourseEditor(ureq, campusCourse.getRepositoryEntry());
                } else {
                    openTabWithCourse(ureq, campusCourse.getRepositoryEntry());
                }
            }
        }
    }

    private void continueCampusCourse(final UserRequest ureq) {
        if (!campusLearnService.checkDelegation(clickedEntry.getSapCourseId(), ureq.getIdentity())) {
            openCourseNotContinuedDialogBecauseOfRemovedDelegation(ureq, clickedEntry.getCourseTitle());
            return;
        }
        campusCourse = campusLearnService.continueCampusCourse(campusCourseCreationController.getSelectedResouceableId(), clickedEntry.getSapCourseId(),
                clickedEntry.getCourseTitle(), ureq.getIdentity());
        checkContinuedCampusCourse(campusCourse, ureq);
    }

    private void checkContinuedCampusCourse(CampusCourse campusCourse, final UserRequest ureq) {
        if (campusCourse == null) {
            openCourseNotContinuedDialog(ureq, clickedEntry.getCourseTitle());
        } else {
            openTabWithCourseEditor(ureq, campusCourse.getRepositoryEntry());
        }
    }

    boolean alreadyCreated(CampusCourse campusCourse, UserRequest ureq) {
        return !ureq.getIdentity().getName().equalsIgnoreCase(campusCourse.getRepositoryEntry().getInitialAuthor());
    }

    private void refreshContainer() {
        updateContainerContent();
        campusVC.setDirty(true);
    }

    /**
     * org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == campusCourseCreationController) {
            if (event.equals(Event.CANCELLED_EVENT)) {
                cmc.deactivate();
                doReleaseCreateCampusCourseLock();
            } else if (event.equals(Event.DONE_EVENT)) {
                cmc.deactivate();
                try {
                    createCampusCourse(ureq);
                } finally {
                    doReleaseCreateCampusCourseLock();
                }
            }

        } else if (source == cmc && event == CloseableModalController.CLOSE_MODAL_EVENT) {
            doReleaseCreateCampusCourseLock();
        }

        // TODO: TO BE REMOVED
        else if (source == courseCreatedDialog) {
            if (event.equals(Event.CANCELLED_EVENT)) {
                // nothing to do
            } else {
                if (DialogBoxUIFactory.getButtonPos(event) == 0) {
                    // nothing to do
                } else if (DialogBoxUIFactory.getButtonPos(event) == 1) {
                    openTabWithCourse(ureq, campusCourse.getRepositoryEntry());
                }
            }
        } else if (source == courseTableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(CMD_LAUNCH)) {
                    final int rowId = te.getRowId();
                    // ticket I-130423-0137 reported a timeout while loading this repo entry
                    // please investigate further in case this happens again ...
                    RepositoryEntry repositoryEntry = campusLearnService.getRepositoryEntryFor(courseTableModel.getSapCampusCourseToAt(rowId).getSapCourseId());
                    if (repositoryEntry != null) {
                        if (repositoryEntry.getAccess() < RepositoryEntry.ACC_USERS) {
                            openTabWithCourseEditor(ureq, repositoryEntry);
                        } else {
                            openTabWithCourse(ureq, repositoryEntry);
                        }

                    } else {
                        log.warn("Could not open course because repositoryEntry is null");
                        showWarning("warning.could.not.found.course");
                    }

                }
            }
        }
    }

    private void openTabWithCourse(final UserRequest ureq, final RepositoryEntry repositoryEntry) {
        if (!repositoryService.isAllowedToLaunch(ureq.getIdentity(), this.roles, repositoryEntry)) {
            final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.BOOKMARK_, ureq.getLocale());
            getWindowControl().setWarning(trans.translate("warn.cantlaunch"));
        } else {
            RepositoryHandler typeHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
            DynamicTabHelper.openRepoEntryTabInRunMode(repositoryEntry, ureq, typeHandler);
        }
    }

    private void openTabWithCourseEditor(final UserRequest ureq, final RepositoryEntry repositoryEntry) {
        final RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
        final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        editorController = typeToEdit.createEditorController(repositoryEntry.getOlatResource(), ureq, dts.getWindowControl());

        if (editorController == null) {
            // editor could not be created -> warning is shown
            return;
        }
        DynamicTabHelper.openRepoEntryTab(repositoryEntry, ureq, editorController, repositoryEntry.getDisplayname(), RepositoryDetailsController.ACTIVATE_EDITOR);
    }

    @Override
    protected void doDispose() {
        doReleaseCreateCampusCourseLock();
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(RepositoryEntry.class));
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CampusCourse.class));
        super.dispose();
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    private void doReleaseCreateCampusCourseLock() {
        if (lockEntry != null && lockEntry.isSuccess()) {
            getLockingService().releaseLock(lockEntry);
            lockEntry = null;
        }
    }

    public CampusCoursePortletEntry getClickedEntry() {
        return clickedEntry;
    }

    public void setClickedEntry(CampusCoursePortletEntry clickedEntry) {
        this.clickedEntry = clickedEntry;
    }

    @Override
    public void event(Event event) {
        if (event instanceof EntryChangedEvent) {
            RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(((EntryChangedEvent) event).getChangedEntryKey());
            // TODO: ADAPT THE startsWithAny
            // if (!StringUtils.startsWithAny(repositoryEntry.getDescription().toLowerCase(), campusCourseIdentifiers)) {
            // return;
            // }
            if (olatcourseIds.contains(repositoryEntry.getOlatResource().getResourceableId())) {
                refreshContainer();
                return;
            }
        }
        if (event instanceof CampusCourseEvent) {
            CampusCourseEvent campusCourseEvent = (CampusCourseEvent) event;
            if ((campusCourseEvent.getStatus() == CampusCourseEvent.DELETED && olatcourseIds.contains(campusCourseEvent.getCampusCourseId()))
                    || (campusCourseEvent.getStatus() == CampusCourseEvent.CREATED && sapcourseIds.contains(campusCourseEvent.getCampusCourseId()))) {
                refreshContainer();
                return;
            }
        }
    }

    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private class CourseTableDataModel extends PortletDefaultTableDataModel {

        public CourseTableDataModel(final List<PortletEntry> objects, final Locale locale) {
            super(objects, 2);
            super.setLocale(locale);
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final PortletEntry entry = getObject(row);
            final SapCampusCourseTo sapCourseTo = (SapCampusCourseTo) entry.getValue();

            switch (col) {
            case 0:
                return getTitle(sapCourseTo);
            case 1:
                return sapCourseTo.getOlatCourseId();
            default:
                return "ERROR";
            }
        }

        private String getTitle(SapCampusCourseTo sapCourseTo) {
            String title = sapCourseTo.getTitle();
            String resultTitle = (title.length() > PORTLET_TITLE_MAX_LENGTH) ? title.substring(0, PORTLET_TITLE_MAX_LENGTH) + "..." : title;
            if (!sapCourseTo.isActivated()) {
                StringBuffer sb = new StringBuffer();
                sb.append("<div ext:qtip='").append(getTranslator().translate("campusPortlet.course.notYetActivated.hover")).append("' >").append(resultTitle)
                        .append("</div>");
                return sb.toString();
            } else {
                return resultTitle;
            }
        }

        public SapCampusCourseTo getSapCampusCourseToAt(final int row) {
            return (SapCampusCourseTo) getObject(row).getValue();
        }

    }

}
