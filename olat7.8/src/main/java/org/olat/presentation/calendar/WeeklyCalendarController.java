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

package org.olat.presentation.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.lms.activitylogging.ILoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.calendar.CalendarComparator;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.calendar.CalendarUtils;
import org.olat.lms.calendar.GotoDateEvent;
import org.olat.lms.calendar.ImportCalendarManager;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.user.UserService;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.calendar.components.WeeklyCalendarComponent;
import org.olat.presentation.calendar.events.CalendarGUIAddEvent;
import org.olat.presentation.calendar.events.CalendarGUIEditEvent;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.util.ComponentUtil;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.notification.ContextualSubscriptionController;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

public class WeeklyCalendarController extends BasicController implements CalendarController, GenericEventListener {

    private static final String CMD_PREVIOUS_WEEK = "pw";
    private static final String CMD_NEXT_WEEK = "nw";

    public static final String CALLER_HOME = "home";
    public static final String CALLER_PROFILE = "profile";
    public static final String CALLER_COLLAB = "collab";
    public static final String CALLER_COURSE = "course";

    private final Panel mainPanel;
    private final VelocityContainer vcMain;
    private List<CalendarRenderWrapper> calendarWrappers;
    private List<CalendarRenderWrapper> importedCalendarWrappers;
    private final WeeklyCalendarComponent weeklyCalendar;
    private final CalendarConfigurationController calendarConfig;
    private final ImportedCalendarConfigurationController importedCalendarConfig;
    private CalendarEntryDetailsController editController;
    private SearchAllCalendarsController searchController;
    private final CalendarSubscription calendarSubscription;
    private Controller subscriptionController;
    private final String caller;
    private boolean dirty = false;
    private final Link thisWeekLink;
    private final Link searchLink;
    private final Link subscribeButton;
    private final Link unsubscribeButton;

    private CloseableModalController cmc;
    private final GotoDateCalendarsForm gotoDateForm;
    private SubscriptionContext subsContext;
    private ContextualSubscriptionController csc;
    private final CalendarService calendarService;

    /**
     * three options: 1. edit sequence 2. delete single date 3. delete whole sequence
     */
    private DialogBoxController dbcSequence;
    private DialogBoxController deleteSingleYesNoController, deleteSequenceYesNoController;
    private String modifiedCalendarId;
    private boolean modifiedCalenderDirty = false;

    private ILoggingAction calLoggingAction;
    private ImportCalendarManager importCalendarManager;

    /**
     * Display week view of calendar. Add the calendars to be displayed via addKalendarWrapper(KalendarRenderWrapper calendarWrapper) method.
     * 
     * @param ureq
     * @param wControl
     * @param calendarWrappers
     * @param caller
     * @param eventAlwaysVisible
     *            When true, the 'isVis()' check is disabled and events will be displayed always.
     */
    public WeeklyCalendarController(final UserRequest ureq, final WindowControl wControl, final List<CalendarRenderWrapper> calendarWrappers, final String caller,
            final boolean eventAlwaysVisible) {
        this(ureq, wControl, calendarWrappers, new ArrayList<CalendarRenderWrapper>(), caller, null, eventAlwaysVisible);
    }

    /**
     * Used for Home
     * 
     * @param ureq
     * @param wControl
     * @param calendarWrappers
     * @param importedCalendarWrappers
     * @param caller
     * @param eventAlwaysVisible
     *            When true, the 'isVis()' check is disabled and events will be displayed always.
     */
    public WeeklyCalendarController(final UserRequest ureq, final WindowControl wControl, final List<CalendarRenderWrapper> calendarWrappers,
            final List<CalendarRenderWrapper> importedCalendarWrappers, final String caller, final boolean eventAlwaysVisible) {
        this(ureq, wControl, calendarWrappers, importedCalendarWrappers, caller, null, eventAlwaysVisible);
    }

    /**
     * @param ureq
     * @param wControl
     * @param calendarWrappers
     * @param caller
     * @param calendarSubscription
     * @param eventAlwaysVisible
     *            When true, the 'isVis()' check is disabled and events will be displayed always.
     */
    public WeeklyCalendarController(final UserRequest ureq, final WindowControl wControl, final List<CalendarRenderWrapper> calendarWrappers, final String caller,
            final CalendarSubscription calendarSubscription, final boolean eventAlwaysVisible) {
        this(ureq, wControl, calendarWrappers, new ArrayList<CalendarRenderWrapper>(), caller, calendarSubscription, eventAlwaysVisible);
    }

    /**
     * Display week view of calendar. Add the calendars to be displayed via addKalendarWrapper(KalendarRenderWrapper calendarWrapper) method.
     * 
     * @param ureq
     * @param wControl
     * @param calendarWrappers
     * @param importedCalendarWrappers
     * @param caller
     * @param calendarSubscription
     * @param eventAlwaysVisible
     *            When true, the 'isVis()' check is disabled and events will be displayed always.
     */
    public WeeklyCalendarController(final UserRequest ureq, final WindowControl wControl, final List<CalendarRenderWrapper> calendarWrappers,
            final List<CalendarRenderWrapper> importedCalendarWrappers, final String caller, final CalendarSubscription calendarSubscription,
            final boolean eventAlwaysVisible) {
        super(ureq, wControl);
        calendarService = getService(LearnServices.calendarService);
        importCalendarManager = getService(LearnServices.importCalendarManager);
        this.calendarWrappers = calendarWrappers;
        this.importedCalendarWrappers = importedCalendarWrappers;
        this.calendarSubscription = calendarSubscription;
        this.caller = caller;

        // main panel
        mainPanel = new Panel("mainPanel");

        boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();

        // main velocity controller
        vcMain = createVelocityContainer("indexWeekly");
        thisWeekLink = LinkFactory.createLink("cal.thisweek", vcMain, this);
        gotoDateForm = new GotoDateCalendarsForm(ureq, wControl, getTranslator());
        listenTo(gotoDateForm);
        vcMain.put("cal.gotodate", gotoDateForm.getInitialComponent());
        searchLink = LinkFactory.createLink("cal.search.button", vcMain, this);
        subscribeButton = LinkFactory.createButtonXSmall("cal.subscribe", vcMain, this);
        unsubscribeButton = LinkFactory.createButtonXSmall("cal.unsubscribe", vcMain, this);

        vcMain.contextPut("caller", caller);
        mainPanel.setContent(vcMain);

        Collections.sort(calendarWrappers, CalendarComparator.getInstance());
        Collections.sort(importedCalendarWrappers, CalendarComparator.getInstance());

        final List<CalendarRenderWrapper> allCalendarWrappers = new ArrayList<CalendarRenderWrapper>(calendarWrappers);
        allCalendarWrappers.addAll(importedCalendarWrappers);
        weeklyCalendar = new WeeklyCalendarComponent("weeklyCalendar", allCalendarWrappers, 7, getTranslator(), eventAlwaysVisible);
        weeklyCalendar.addListener(this);

        ComponentUtil.registerForValidateEvents(vcMain, this);

        vcMain.put("calendar", weeklyCalendar);

        // calendarConfiguration component
        calendarConfig = new CalendarConfigurationController(calendarWrappers, ureq, getWindowControl(), eventAlwaysVisible, true);
        listenTo(calendarConfig);

        vcMain.put("calendarConfig", calendarConfig.getInitialComponent());
        importedCalendarConfig = new ImportedCalendarConfigurationController(importedCalendarWrappers, ureq, getWindowControl(), false);
        importedCalendarConfig.addControllerListener(this);
        vcMain.put("importedCalendarConfig", importedCalendarConfig.getInitialComponent());

        // calendar subscription
        if (calendarSubscription == null || isGuest) {
            vcMain.contextPut("hasSubscription", Boolean.FALSE);
        } else {
            vcMain.contextPut("hasSubscription", Boolean.TRUE);
            vcMain.contextPut("isSubscribed", new Boolean(calendarSubscription.isSubscribed()));
        }
        setWeekYearInVelocityPage(vcMain, weeklyCalendar);

        this.putInitialPanel(mainPanel);

        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarDao.class));
    }

    public void setEnableRemoveFromPersonalCalendar(final boolean enable) {
        calendarConfig.setEnableRemoveFromPersonalCalendar(enable);
    }

    public int getFocusYear() {
        return weeklyCalendar.getYear();
    }

    public int getFocusWeekOfYear() {
        return weeklyCalendar.getWeekOfYear();
    }

    @Override
    public void setFocus(final Date date) {
        final Calendar focus = CalendarUtils.createCalendarInstance(getLocale());
        focus.setTime(date);
        weeklyCalendar.setFocus(focus.get(Calendar.YEAR), focus.get(Calendar.WEEK_OF_YEAR));
        setWeekYearInVelocityPage(vcMain, weeklyCalendar);
    }

    public void setFocus(final int year, final int weekOfYear) {
        weeklyCalendar.setFocus(year, weekOfYear);
    }

    private void setWeekYearInVelocityPage(final VelocityContainer vc, final WeeklyCalendarComponent weeklyCalendar) {
        vc.contextPut("week", weeklyCalendar.getWeekOfYear());
        vc.contextPut("year", weeklyCalendar.getYear());
    }

    @Override
    public void setFocusOnEvent(final String eventId) {
        if (eventId.length() > 0) {
            for (final CalendarRenderWrapper wrapper : calendarWrappers) {
                final CalendarEntry event = wrapper.getCalendar().getCalendarEntry(eventId);
                if (event != null) {
                    setFocus(event.getBegin());
                    break;
                }
            }
        }
    }

    @Override
    public void setCalendars(final List calendars) {
        setCalendars(calendars, new ArrayList());
    }

    @Override
    public void setCalendars(final List calendars, final List importedCalendars) {
        this.calendarWrappers = calendars;
        Collections.sort(calendarWrappers, CalendarComparator.getInstance());
        weeklyCalendar.setCalendarRenderWrappers(calendarWrappers);
        calendarConfig.setCalendarRenderWrappers(calendarWrappers);

        this.importedCalendarWrappers = importedCalendars;
        Collections.sort(calendarWrappers, CalendarComparator.getInstance());
        Collections.sort(importedCalendarWrappers, CalendarComparator.getInstance());

        final ArrayList allCalendarWrappers = new ArrayList(calendarWrappers);
        allCalendarWrappers.addAll(importedCalendarWrappers);
        weeklyCalendar.setCalendarRenderWrappers(allCalendarWrappers);

        calendarConfig.setCalendarRenderWrappers(calendarWrappers);
        importedCalendarConfig.setCalendars(importedCalendarWrappers);
    }

    @Override
    public void setDirty() {
        dirty = true;
    }

    private ILoggingAction getCalLoggingAction() {
        return calLoggingAction;
    }

    private void setCalLoggingAction(final ILoggingAction calLoggingAction) {
        this.calLoggingAction = calLoggingAction;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (event == ComponentUtil.VALIDATE_EVENT && dirty) {
            dirty = false;
            fireEvent(ureq, new CalendarModifiedEvent());
        } else if (event == ComponentUtil.VALIDATE_EVENT && weeklyCalendar.isDirty() && modifiedCalenderDirty) {
            // reload calendar (28.06.11/cg: don't know if we really need this code, but this code fragement was in KalendarRenderWrapper to reload calendar
            final CalendarRenderWrapper calendarRenderWrapper = weeklyCalendar.getCalendarRenderWrapper(modifiedCalendarId);
            Object calendar = calendarService.getCalendar(calendarRenderWrapper.getCalendar().getType(), calendarRenderWrapper.getCalendar().getCalendarID());
        } else if (source == vcMain) {
            if (event.getCommand().equals(CMD_PREVIOUS_WEEK)) {
                weeklyCalendar.previousWeek();
            } else if (event.getCommand().equals(CMD_NEXT_WEEK)) {
                weeklyCalendar.nextWeek();
            }
            setWeekYearInVelocityPage(vcMain, weeklyCalendar);
        } else if (source == thisWeekLink) {
            final Calendar cal = CalendarUtils.createCalendarInstance(ureq.getLocale());
            weeklyCalendar.setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
        } else if (source == searchLink) {

            final ArrayList allCalendarWrappers = new ArrayList(calendarWrappers);
            allCalendarWrappers.addAll(importedCalendarWrappers);

            removeAsListenerAndDispose(searchController);
            searchController = new SearchAllCalendarsController(ureq, getWindowControl(), allCalendarWrappers);
            listenTo(searchController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        } else if (source == subscribeButton || source == unsubscribeButton) {
            removeAsListenerAndDispose(subscriptionController);
            if (calendarSubscription.isSubscribed() == (source == unsubscribeButton)) {
                subscriptionController = calendarSubscription.triggerSubscribeAction();
            }
            if (subscriptionController != null) {
                // activate subscription controller
                listenTo(subscriptionController);
                mainPanel.setContent(subscriptionController.getInitialComponent());
            } else {
                vcMain.contextPut("isSubscribed", new Boolean(calendarSubscription.isSubscribed()));
                CoordinatorManager.getInstance().getCoordinator().getEventBus()
                        .fireEventToListenersOf(new CalendarModifiedEvent(), OresHelper.lookupType(CalendarDao.class));
            }
        } else if (source == weeklyCalendar) {
            if (event instanceof CalendarGUIEditEvent) {
                final CalendarGUIEditEvent guiEvent = (CalendarGUIEditEvent) event;
                final CalendarEntry calendarEntry = guiEvent.getKalendarEvent();
                if (calendarEntry == null) {
                    // event already deleted
                    getWindowControl().setError(translate("cal.error.eventDeleted"));
                    return;
                }
                final String recurrence = calendarEntry.getRecurrenceRule();
                boolean isImported = false;
                final CalendarRenderWrapper calendarRenderWrapper = guiEvent.getKalendarRenderWrapper();
                if (calendarRenderWrapper != null) {
                    isImported = calendarRenderWrapper.isImported();
                }
                if (!isImported && recurrence != null && !recurrence.equals("")) {
                    final List<String> btnLabels = new ArrayList<String>();
                    btnLabels.add(translate("cal.edit.dialog.sequence"));
                    btnLabels.add(translate("cal.edit.dialog.delete.single"));
                    btnLabels.add(translate("cal.edit.dialog.delete.sequence"));
                    if (dbcSequence != null) {
                        dbcSequence.dispose();
                    }
                    dbcSequence = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("cal.edit.dialog.title"), translate("cal.edit.dialog.text"),
                            btnLabels);
                    dbcSequence.addControllerListener(this);
                    dbcSequence.setUserObject(guiEvent);
                    dbcSequence.activate();
                    return;
                }
                final CalendarRenderWrapper calendarWrapper = guiEvent.getKalendarRenderWrapper();
                pushEditEventController(ureq, calendarEntry, calendarWrapper);
            } else if (event instanceof CalendarGUIAddEvent) {
                pushAddEventController((CalendarGUIAddEvent) event, ureq);
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        OlatCalendar affectedCal = null;
        if (dirty) {
            dirty = false;
            fireEvent(ureq, new CalendarModifiedEvent());
        }
        if (source == editController) {
            affectedCal = editController.getKalendarEvent().getCalendar();
            cmc.deactivate();
            weeklyCalendar.setDirty(true);
            // do logging if affectedCal not null
            if (affectedCal != null) {
                ThreadLocalUserActivityLogger.log(getCalLoggingAction(), getClass(), LoggingResourceable.wrap(ureq.getIdentity()), LoggingResourceable.wrap(affectedCal));
            }
        } else if (source == cmc && event == CloseableModalController.CLOSE_MODAL_EVENT) {
            // DO NOT DEACTIVATE AS ALREADY CLOSED BY CloseableModalController INTERNALLY
            weeklyCalendar.setDirty(true);
        } else if (source == calendarConfig || source == importedCalendarConfig) {
            if (event instanceof CalendarGUIAddEvent) {
                pushAddEventController((CalendarGUIAddEvent) event, ureq);
            } else if (event == Event.CHANGED_EVENT) {
                importedCalendarWrappers = importCalendarManager.getImportedCalendarsForIdentity(ureq);
                importedCalendarConfig.setCalendars(importedCalendarWrappers);
                this.setCalendars(calendarWrappers, importedCalendarWrappers);
                weeklyCalendar.setDirty(true);
                vcMain.setDirty(true);
            }
        } else if (source == searchController) {
            if (event instanceof GotoDateEvent) {
                final Date gotoDate = ((GotoDateEvent) event).getGotoDate();
                weeklyCalendar.setDate(gotoDate);
                setWeekYearInVelocityPage(vcMain, weeklyCalendar);
            }
            cmc.deactivate();
        } else if (source == subscriptionController) {
            // nothing to do here
            mainPanel.setContent(vcMain);
            vcMain.contextPut("isSubscribed", new Boolean(calendarSubscription.isSubscribed()));
        } else if (source == gotoDateForm) {
            weeklyCalendar.setDate(gotoDateForm.getGotoDate());
            setWeekYearInVelocityPage(vcMain, weeklyCalendar);
        } else if (source == dbcSequence) {
            if (event != Event.CANCELLED_EVENT) {
                final int pos = DialogBoxUIFactory.getButtonPos(event);
                final CalendarGUIEditEvent guiEvent = (CalendarGUIEditEvent) dbcSequence.getUserObject();
                final CalendarRenderWrapper calendarWrapper = guiEvent.getKalendarRenderWrapper();
                final CalendarEntry calendarEntry = guiEvent.getKalendarEvent();
                if (pos == 0) { // edit the sequence
                    // load the parent event of this sequence
                    final CalendarEntry parentEvent = calendarWrapper.getCalendar().getCalendarEntry(calendarEntry.getID());
                    pushEditEventController(ureq, parentEvent, calendarWrapper);
                } else if (pos == 1) { // delete a single event of the sequence
                    deleteSingleYesNoController = activateYesNoDialog(ureq, null, translate("cal.delete.dialogtext"), deleteSingleYesNoController);
                    deleteSingleYesNoController.setUserObject(calendarEntry);
                } else if (pos == 2) { // delete the whole sequence
                    deleteSequenceYesNoController = activateYesNoDialog(ureq, null, translate("cal.delete.dialogtext.sequence"), deleteSequenceYesNoController);
                    deleteSequenceYesNoController.setUserObject(calendarEntry);
                }
            }
            dbcSequence.dispose();
        } else if (source == deleteSingleYesNoController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final CalendarEntry calendarEntry = (CalendarEntry) deleteSingleYesNoController.getUserObject();
                affectedCal = calendarEntry.getCalendar();
                final CalendarEntry kEvent = affectedCal.getCalendarEntry(calendarEntry.getID());
                kEvent.addRecurrenceExc(calendarEntry.getBegin());
                calendarService.updateEntryFrom(affectedCal, kEvent);
                deleteSingleYesNoController.dispose();
                weeklyCalendar.setDirty(true);
                vcMain.setDirty(true);
            }
        } else if (source == deleteSequenceYesNoController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final CalendarEntry calendarEntry = (CalendarEntry) deleteSequenceYesNoController.getUserObject();
                affectedCal = calendarEntry.getCalendar();
                calendarService.removeEntryFrom(affectedCal, calendarEntry);
                deleteSequenceYesNoController.dispose();
                weeklyCalendar.setDirty(true);
                vcMain.setDirty(true);
            }
        }

    }

    /**
     * @param ureq
     * @param calendarEntry
     * @param calendarWrapper
     */
    private void pushEditEventController(final UserRequest ureq, final CalendarEntry calendarEntry, final CalendarRenderWrapper calendarWrapper) {
        removeAsListenerAndDispose(editController);

        boolean canEdit = false;
        for (final Iterator<CalendarRenderWrapper> iter = calendarWrappers.iterator(); iter.hasNext();) {
            final CalendarRenderWrapper wrapper = iter.next();
            if (wrapper.getAccess() == CalendarRenderWrapper.ACCESS_READ_WRITE
                    && calendarWrapper.getCalendar().getCalendarID().equals(wrapper.getCalendar().getCalendarID())) {
                canEdit = true;
            }
        }

        if (canEdit) {
            editController = new CalendarEntryDetailsController(ureq, calendarEntry, calendarWrapper, calendarWrappers, false, caller, getWindowControl());
            listenTo(editController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();

            // set logging action
            setCalLoggingAction(CalendarLoggingAction.CALENDAR_ENTRY_MODIFIED);
        } else {
            showError("cal.error.readonly");
        }
    }

    private void pushAddEventController(final CalendarGUIAddEvent addEvent, final UserRequest ureq) {
        final CalendarRenderWrapper calendarWrapper = weeklyCalendar.getCalendarRenderWrapper(addEvent.getCalendarID());
        // create new KalendarEvent
        final CalendarEntry newEvent = new CalendarEntry(CodeHelper.getGlobalForeverUniqueID(), "", addEvent.getStartDate(), (1000 * 60 * 60 * 1));
        if (calendarWrapper.getCalendar().getType().equals(CalendarDao.TYPE_COURSE) || calendarWrapper.getCalendar().getType().equals(CalendarDao.TYPE_GROUP)) {
            newEvent.setClassification(CalendarEntry.CLASS_PUBLIC);
        }

        newEvent.setAllDayEvent(addEvent.isAllDayEvent());
        newEvent.setCreatedBy(getUserService().getFirstAndLastname(ureq.getIdentity().getUser()));
        newEvent.setCreated(new Date().getTime());
        final ArrayList<CalendarRenderWrapper> allCalendarWrappers = new ArrayList<CalendarRenderWrapper>(calendarWrappers);
        allCalendarWrappers.addAll(importedCalendarWrappers);

        removeAsListenerAndDispose(editController);
        editController = new CalendarEntryDetailsController(ureq, newEvent, calendarWrapper, allCalendarWrappers, true, caller, getWindowControl());
        listenTo(editController);

        removeAsListenerAndDispose(cmc);
        cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent());
        listenTo(cmc);

        cmc.activate();

        // set logging action
        setCalLoggingAction(CalendarLoggingAction.CALENDAR_ENTRY_CREATED);
    }

    @Override
    protected void doDispose() {
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CalendarDao.class));
    }

    @Override
    public void event(final Event event) {
        if (event instanceof CalendarModifiedEvent) {
            final CalendarModifiedEvent calendarModifiedEvent = (CalendarModifiedEvent) event;
            if (calendarModifiedEvent.getType() != null
                    && calendarModifiedEvent.getCalendarId() != null
                    && weeklyCalendar.getCalendarRenderWrapper(calendarModifiedEvent.getCalendarId()) != null
                    && calendarModifiedEvent.getType().equals(weeklyCalendar.getCalendarRenderWrapper(calendarModifiedEvent.getCalendarId()).getCalendar().getType())
                    && calendarModifiedEvent.getCalendarId().equals(
                            weeklyCalendar.getCalendarRenderWrapper(calendarModifiedEvent.getCalendarId()).getCalendar().getCalendarID())) {
                // the event is for my calendar => reload it

                // keeping a reference to the dirty calendar as reloading here raises an nested do in sync error. Using the component validation event to reload
                modifiedCalendarId = calendarModifiedEvent.getCalendarId();
                modifiedCalenderDirty = true;
                weeklyCalendar.setDirty(true);
            }
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
