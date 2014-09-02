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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.commons.vfs.NamedContainerImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.reference.Reference;
import org.olat.data.reference.ReferenceDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.StringResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.EfficiencyStatementManager;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.reference.ReferenceEnum;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.presentation.course.config.CourseCalendarConfigController;
import org.olat.presentation.course.config.CourseChatSettingController;
import org.olat.presentation.course.config.CourseConfigEvent;
import org.olat.presentation.course.config.CourseConfigGlossaryController;
import org.olat.presentation.course.config.CourseEfficencyStatementController;
import org.olat.presentation.course.config.CourseLayoutController;
import org.olat.presentation.course.config.CourseSharedFolderController;
import org.olat.presentation.course.run.RunMainController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.glossary.GlossaryRegisterSettingsController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * If the resource is a course it tries to aquire the lock for editing the properties of this course.
 * 
 * @author Ingmar Kroll
 */
public class RepositoryEditPropertiesController extends BasicController {

    private static final String ACTION_PUB = "pub";
    private static final String ACTION_FORWARD = "forw";
    private static final String ACTION_BACKWARD = "bckw";

    public static final Event BACKWARD_EVENT = new Event("backward");
    public static final Event FORWARD_EVENT = new Event("forward");

    private VelocityContainer bgVC;
    private VelocityContainer editproptabpubVC;
    private PropPupForm propPupForm;
    private CourseChatSettingController ccc;
    private CourseSharedFolderController csfC;
    private CourseLayoutController clayoutC;
    private CourseEfficencyStatementController ceffC;
    private CourseCalendarConfigController calCfgCtr;
    private CourseConfigGlossaryController cglosCtr;
    private TabbedPane tabbedPane;
    private RepositoryEntry repositoryEntry;

    private LockResult courseLockEntry;

    private CourseConfig initialCourseConfig; // deep clone of the courseConfig
    private CourseConfig changedCourseConfig; // deep clone of the courseConfig
    private DialogBoxController yesNoCommitConfigsCtr;
    private boolean repositoryEntryChanged; // false per default
    private boolean courseConfigChanged;

    private static final Logger log = LoggerHelper.getLogger();

    private final static String RELEASE_LOCK_AT_CATCH_EXCEPTION = "Must release course lock since an exception occured in " + RepositoryEditPropertiesController.class;

    /**
     * Create a repository add controller that adds the given resourceable.
     * 
     * @param ureq
     * @param wControl
     * @param sourceEntry
     */
    public RepositoryEditPropertiesController(final UserRequest ureq, final WindowControl wControl, final RepositoryEntry entry, final boolean usedInWizard) {
        super(ureq, wControl);

        addLoggingResourceable(LoggingResourceable.wrap(entry, OlatResourceableType.genRepoEntry));

        this.repositoryEntry = entry;

        bgVC = createVelocityContainer("bgrep");
        bgVC.contextPut("title", entry.getDisplayname());
        tabbedPane = new TabbedPane("descTB", ureq.getLocale());

        editproptabpubVC = createVelocityContainer("editproptabpub");
        tabbedPane.addTab(translate("tab.public"), editproptabpubVC);
        propPupForm = new PropPupForm(ureq, wControl, entry);
        listenTo(propPupForm);
        editproptabpubVC.put("proppupform", propPupForm.getInitialComponent());
        tabbedPane.addListener(this);
        try {
            if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
                // FIXME: This is duplicate code!!!! See CourseConfigMainController.
                // it is a course
                final ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
                this.changedCourseConfig = course.getCourseEnvironment().getCourseConfig().clone();
                this.initialCourseConfig = course.getCourseEnvironment().getCourseConfig().clone();

                final boolean isAlreadyLocked = getLockingService().isLocked(repositoryEntry.getOlatResource(), CourseFactory.COURSE_EDITOR_LOCK);
                // try to acquire edit lock for this course and show dialog box on failure..
                courseLockEntry = getLockingService().acquireLock(repositoryEntry.getOlatResource(), ureq.getIdentity(), CourseFactory.COURSE_EDITOR_LOCK);
                if (!courseLockEntry.isSuccess()) {
                    this.showWarning("error.course.alreadylocked", courseLockEntry.getOwner().getName());
                    // beware: the controller is not properly initialized - the initial component is null
                    return;
                } else if (courseLockEntry.isSuccess() && isAlreadyLocked) {
                    this.showWarning("warning.course.alreadylocked.bySameUser");
                    // beware: the controller is not properly initialized - the initial component is null
                    courseLockEntry = null; // invalid lock
                    return;
                } else {
                    // editproptabinfVC.put(CourseFactory.getDetailsComponent(repositoryEntry.getOlatResource(),ureq));
                    // enable course chat settings, if instant messenger module is available
                    // and course chat is enabled.
                    initializeChatTab(ureq);
                    initializeLayoutTab(ureq, course);

                    initializeSharedFolderTab(ureq);

                    initializeEfficencyStatementTab(ureq);

                    initializeCalendarTab(ureq);

                    initializeGlossaryTab(ureq, course);
                }
            } else if (repositoryEntry.getOlatResource().getResourceableTypeName().equals(GlossaryResource.TYPE_NAME)) {
                initializeGlossaryRegisterTab(ureq);
            }

            bgVC.put("descTB", tabbedPane);
            bgVC.contextPut("wizardfinish", new Boolean(usedInWizard));

            putInitialPanel(bgVC);
        } catch (final RuntimeException e) {
            log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION);
            this.dispose();
            throw e;
        }
    }

    /**
     * @param ureq
     */
    private void initializeGlossaryRegisterTab(final UserRequest ureq) {
        final GlossaryRegisterSettingsController glossRegisterSetCtr = new GlossaryRegisterSettingsController(ureq, getWindowControl(), repositoryEntry.getOlatResource());
        tabbedPane.addTab(translate("tab.glossary.register"), glossRegisterSetCtr.getInitialComponent());
    }

    /**
     * @param ureq
     * @param course
     */
    private void initializeGlossaryTab(final UserRequest ureq, final ICourse course) {
        cglosCtr = new CourseConfigGlossaryController(ureq, getWindowControl(), changedCourseConfig, course.getResourceableId());
        this.listenTo(cglosCtr);
        tabbedPane.addTab(translate("tab.glossary"), cglosCtr.getInitialComponent());
    }

    /**
     * @param ureq
     */
    private void initializeCalendarTab(final UserRequest ureq) {
        calCfgCtr = new CourseCalendarConfigController(ureq, getWindowControl(), changedCourseConfig);
        this.listenTo(calCfgCtr);
        tabbedPane.addTab(translate("tab.calendar"), calCfgCtr.getInitialComponent());
    }

    /**
     * @param ureq
     */
    private void initializeEfficencyStatementTab(final UserRequest ureq) {
        ceffC = new CourseEfficencyStatementController(ureq, getWindowControl(), changedCourseConfig);
        this.listenTo(ceffC);
        tabbedPane.addTab(translate("tab.efficencystatement"), ceffC.getInitialComponent());
    }

    /**
     * @param ureq
     */
    private void initializeSharedFolderTab(final UserRequest ureq) {
        csfC = new CourseSharedFolderController(ureq, getWindowControl(), changedCourseConfig);
        this.listenTo(csfC);
        tabbedPane.addTab(translate("tab.sharedfolder"), csfC.getInitialComponent());
    }

    /**
     * @param ureq
     * @param course
     */
    private void initializeLayoutTab(final UserRequest ureq, final ICourse course) {
        final VFSContainer namedContainerImpl = new NamedContainerImpl(translate("coursefolder", course.getCourseTitle()), course.getCourseFolderContainer());
        clayoutC = new CourseLayoutController(ureq, getWindowControl(), changedCourseConfig, namedContainerImpl);
        this.listenTo(clayoutC);
        tabbedPane.addTab(translate("tab.layout"), clayoutC.getInitialComponent());
    }

    /**
     * @param ureq
     */
    private void initializeChatTab(final UserRequest ureq) {
        if (InstantMessagingModule.isEnabled() && CourseModule.isCourseChatEnabled()) {
            ccc = new CourseChatSettingController(ureq, getWindowControl(), changedCourseConfig);
            this.listenTo(ccc);
            // push on controller stack and register <this> as controllerlistener
            tabbedPane.addTab(translate("tab.chat"), ccc.getInitialComponent());
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        try {
            if (source == this.bgVC) {
                if (event.getCommand().equals(ACTION_BACKWARD)) {
                    fireEvent(ureq, BACKWARD_EVENT);
                } else if (event.getCommand().equals(ACTION_FORWARD)) {
                    fireEvent(ureq, FORWARD_EVENT);
                }
            }
        } catch (final RuntimeException e) {
            log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION);
            this.dispose();
            throw e;
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        try {
            if (source == ccc || source == clayoutC || source == csfC || source == ceffC || source == calCfgCtr || source == cglosCtr) {

                if (!initialCourseConfig.equals(changedCourseConfig)) {
                    courseConfigChanged = true;
                }
            } else if (source == yesNoCommitConfigsCtr) {
                if (repositoryEntryChanged) {
                    if (DialogBoxUIFactory.isYesEvent(event)) {
                        getRepositoryService().setProperties(repositoryEntry, propPupForm.canCopy(), propPupForm.canReference(), propPupForm.canLaunch(),
                                propPupForm.canDownload());
                        getRepositoryService().setAccess(repositoryEntry, propPupForm.getAccess());
                        repositoryEntry = getRepositoryService().lookupRepositoryEntry(repositoryEntry.getKey());
                        repositoryEntryChanged = false;

                        final MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, EntryChangedEvent.MODIFIED);
                        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, repositoryEntry);

                        // do logging
                        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES, getClass());

                        fireEvent(ureq, new Event("courseChanged"));
                    } else {
                        // yesNoCommitConfigsCtr => NO => do not change repository, reset changed flag
                        repositoryEntryChanged = false;
                    }
                }
                if (courseConfigChanged && !initialCourseConfig.equals(changedCourseConfig) && DialogBoxUIFactory.isYesEvent(event)) {
                    // ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
                    final ICourse course = CourseFactory.openCourseEditSession(repositoryEntry.getOlatResource().getResourceableId());
                    // change course config
                    final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
                    courseConfig.setCalendarEnabled(changedCourseConfig.isCalendarEnabled());
                    courseConfig.setChatIsEnabled(changedCourseConfig.isChatEnabled());
                    courseConfig.setCssLayoutRef(changedCourseConfig.getCssLayoutRef());
                    courseConfig.setEfficencyStatementIsEnabled(changedCourseConfig.isEfficencyStatementEnabled());
                    courseConfig.setGlossarySoftKey(changedCourseConfig.getGlossarySoftKey());
                    courseConfig.setSharedFolderSoftkey(changedCourseConfig.getSharedFolderSoftkey());
                    CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
                    CourseFactory.closeCourseEditSession(course.getResourceableId(), true);

                    // CourseChatSettingController
                    if (ccc != null) {
                        if (changedCourseConfig.isChatEnabled() != initialCourseConfig.isChatEnabled()) {
                            // log instant messaging enabled disabled settings
                            if (changedCourseConfig.isChatEnabled()) {
                                ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_ENABLED, getClass());
                            } else {
                                ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_IM_DISABLED, getClass());
                            }
                        }
                    }

                    // CourseLayoutController
                    if (!changedCourseConfig.getCssLayoutRef().equals(initialCourseConfig.getCssLayoutRef()) && clayoutC.getLoggingAction() != null) {
                        // log removing custom course layout
                        ThreadLocalUserActivityLogger.log(clayoutC.getLoggingAction(), getClass());
                    }
                    // CourseSharedFolderController
                    if (!changedCourseConfig.getSharedFolderSoftkey().equals(initialCourseConfig.getSharedFolderSoftkey()) && csfC.getLoggingAction() != null) {
                        final String logDetail = csfC.getSharedFolderRepositoryEntry() != null ? csfC.getSharedFolderRepositoryEntry().getDisplayname() : null;
                        ThreadLocalUserActivityLogger.log(csfC.getLoggingAction(), getClass(), LoggingResourceable.wrapBCFile(logDetail));
                        if (!changedCourseConfig.getSharedFolderSoftkey().equals(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY)) {
                            getReferenceService().updateRefTo(csfC.getSharedFolderRepositoryEntry().getOlatResource(), course, ReferenceEnum.SHARE_FOLDER_REF.getValue());
                        } else {
                            getReferenceService().deleteRefTo(course, ReferenceEnum.SHARE_FOLDER_REF.getValue());
                        }
                    }
                    // CourseEfficencyStatementController
                    if ((changedCourseConfig.isEfficencyStatementEnabled() != initialCourseConfig.isEfficencyStatementEnabled() && ceffC.getLoggingAction() != null)) {
                        if (changedCourseConfig.isEfficencyStatementEnabled()) {
                            // first create the efficiencies, send event to agency (all courses add link)
                            EfficiencyStatementManager.getInstance().updateAllEfficiencyStatementsOf(course);
                        } else {
                            // delete really the efficiencies of the users.
                            final RepositoryEntry courseRepoEntry = getRepositoryService().lookupRepositoryEntry(course, true);
                            EfficiencyStatementManager.getInstance().deleteEfficiencyStatementsFromCourse(courseRepoEntry.getKey());
                        }
                        // inform everybody else
                        final EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
                        final CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigEvent.EFFICIENCY_STATEMENT_TYPE, course.getResourceableId());
                        eventBus.fireEventToListenersOf(courseConfigEvent, course);
                        ThreadLocalUserActivityLogger.log(ceffC.getLoggingAction(), getClass());
                    }
                    // CourseCalendarConfigController
                    if (changedCourseConfig.isCalendarEnabled() != initialCourseConfig.isCalendarEnabled() && calCfgCtr.getLoggingAction() != null) {
                        ThreadLocalUserActivityLogger.log(calCfgCtr.getLoggingAction(), getClass());
                        // notify calendar components to refresh their calendars
                        CoordinatorManager.getInstance().getCoordinator().getEventBus()
                                .fireEventToListenersOf(new CalendarModifiedEvent(), OresHelper.lookupType(CalendarDao.class));
                    }
                    // CourseConfigGlossaryController
                    if ((changedCourseConfig.getGlossarySoftKey() == null && initialCourseConfig.getGlossarySoftKey() != null)
                            || (changedCourseConfig.getGlossarySoftKey() != null && initialCourseConfig.getGlossarySoftKey() == null)
                            && cglosCtr.getLoggingAction() != null) {

                        final String glossarySoftKey = changedCourseConfig.getGlossarySoftKey();
                        LoggingResourceable lri = null;
                        if (glossarySoftKey != null) {
                            lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, glossarySoftKey, glossarySoftKey);
                        } else {
                            final String deleteGlossarySoftKey = initialCourseConfig.getGlossarySoftKey();
                            if (deleteGlossarySoftKey != null) {
                                lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, deleteGlossarySoftKey, deleteGlossarySoftKey);
                            }
                        }
                        if (lri != null) {
                            ThreadLocalUserActivityLogger.log(cglosCtr.getLoggingAction(), getClass(), lri);
                        }
                        if (changedCourseConfig.getGlossarySoftKey() == null) {
                            // update references
                            ReferenceService referenceService = (ReferenceService) CoreSpringFactory.getBean(ReferenceService.class);
                            final List repoRefs = referenceService.getReferences(course);
                            for (final Iterator iter = repoRefs.iterator(); iter.hasNext();) {
                                final Reference ref = (Reference) iter.next();
                                if (ref.getUserdata().equals(GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER)) {
                                    referenceService.delete(ref);
                                    continue;
                                }
                            }
                        } else if (changedCourseConfig.getGlossarySoftKey() != null) {
                            // update references
                            final RepositoryService rm = getRepositoryService();
                            final RepositoryEntry repoEntry = rm.lookupRepositoryEntryBySoftkey(changedCourseConfig.getGlossarySoftKey(), false);
                            ReferenceDao.getInstance().addReference(course, repoEntry.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);
                        }
                    }
                    // course config transaction fihished
                    initialCourseConfig = course.getCourseEnvironment().getCourseConfig().clone();

                    // fire CourseConfigEvent for this course channel
                    final EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
                    final CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigEvent.CALENDAR_TYPE, course.getResourceableId());
                    eventBus.fireEventToListenersOf(courseConfigEvent, course);

                    this.fireEvent(ureq, Event.DONE_EVENT);
                } else if (!DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
                    this.fireEvent(ureq, Event.DONE_EVENT);
                }
            } else if (source == this.propPupForm) { // process details form events
                if (event == Event.CANCELLED_EVENT) {
                    fireEvent(ureq, Event.CANCELLED_EVENT);
                } else if (event == Event.DONE_EVENT) {
                    repositoryEntryChanged = true;
                    // inform user about inconsistent configuration: doesn't make sense to set a repositoryEntry canReference=true if it is only accessible to owners
                    if (!repositoryEntry.getCanReference() && propPupForm.canReference() && propPupForm.getAccess() < RepositoryEntry.ACC_OWNERS_AUTHORS) {
                        this.showError("warn.config.reference.no.access");
                    }
                    // if not a course, update the repositoryEntry NOW!
                    if (!repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
                        getRepositoryService().setProperties(repositoryEntry, propPupForm.canCopy(), propPupForm.canReference(), propPupForm.canLaunch(),
                                propPupForm.canDownload());
                        getRepositoryService().setAccess(repositoryEntry, propPupForm.getAccess());
                        // inform anybody interrested about this change
                        final MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, EntryChangedEvent.MODIFIED);
                        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, repositoryEntry);
                        fireEvent(ureq, Event.CHANGED_EVENT);
                    }
                    return;
                }
            }
        } catch (final RuntimeException e) {
            log.warn(RELEASE_LOCK_AT_CATCH_EXCEPTION);
            this.dispose();
            throw e;
        }
    }

    /**
     * @return
     */
    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

    private ReferenceService getReferenceService() {
        return CoreSpringFactory.getBean(ReferenceService.class);
    }

    /**
     * Releases the course lock, the child controlers are disposed on the superclass.
     * 
     */
    @Override
    protected void doDispose() {
        releaseCourseLock();
    }

    /**
     * Must always release the lock upon dispose! Releases course lock and closes the CourseEditSession, if any open.
     */
    private void releaseCourseLock() {
        if (courseLockEntry != null && courseLockEntry.isSuccess()) {
            getLockingService().releaseLock(courseLockEntry);
            // cleanup course edit session, in case some error occurred during property editing.
            CourseFactory.closeCourseEditSession(repositoryEntry.getOlatResource().getResourceableId(), false);
            courseLockEntry = null; // invalidate lock
        }
    }

    /**
     * @return Returns the repositoryEntry.
     */
    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    /**
     * @param ureq
     * @return Return false if nothing changed, else true and activateYesNoDialog for save confirmation.
     */
    public boolean checkIfCourseConfigChanged(final UserRequest ureq) {
        if (isCourseAndRepoEntryOrConfigChanged()) {
            final OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource()
                    .getResourceableId());
            int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres) - 1; // -1: Remove myself from list;
            if (cnt < 0) {
                cnt = 0; // do not show any negative value
            }
            yesNoCommitConfigsCtr = this.activateYesNoDialog(ureq, translate("course.config.changed.title"),
                    translate("course.config.changed.text", String.valueOf(cnt)), yesNoCommitConfigsCtr);
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    private boolean isCourseAndRepoEntryOrConfigChanged() {
        return repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && (repositoryEntryChanged || courseConfigChanged);
    }
}
