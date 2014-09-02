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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.calendar.ICalTokenGenerator;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.calendar.events.CalendarGUIAddEvent;
import org.olat.presentation.course.calendar.CourseCalendarSubscription;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class CalendarConfigurationController extends BasicController {

    private static final Object CMD_ADD = "add";
    private static final Object CMD_TOGGLE_DISPLAY = "tglvis";
    private static final Object CMD_CHOOSE_COLOR = "cc";
    private static final Object CMD_ICAL_FEED = "if";
    private static final Object CMD_ICAL_REGENERATE = "rf";
    private static final Object CMD_ICAL_REMOVE_FEED = "rmif";
    private static final Object CMD_UNSUBSCRIBE = "unsub";
    private static final String PARAM_ID = "id";

    private final VelocityContainer configVC;
    private List<CalendarRenderWrapper> calendars;
    private CalendarColorChooserController colorChooser;
    private CalendarRenderWrapper lastCalendarWrapper;
    private CloseableModalController cmc;
    private String currentCalendarID;
    private CalendarExportController exportController;
    private DialogBoxController confirmRemoveDialog;
    private DialogBoxController confirmRegenerateDialog;
    final CalendarService calendarService;

    private List<String> subscriptionIds;

    public CalendarConfigurationController(final List<CalendarRenderWrapper> calendars, final UserRequest ureq, final WindowControl wControl,
            final boolean insideManager, final boolean canUnsubscribe) {
        super(ureq, wControl);
        calendarService = CoreSpringFactory.getBean(CalendarService.class);
        configVC = createVelocityContainer("calConfig");
        setCalendarRenderWrappers(ureq, calendars);
        configVC.contextPut("insideManager", insideManager);
        configVC.contextPut("identity", ureq.getIdentity());
        configVC.contextPut("removeFromPersonalCalendar", Boolean.TRUE);
        putInitialPanel(configVC);
    }

    public void setEnableRemoveFromPersonalCalendar(final boolean enable) {
        configVC.contextPut("removeFromPersonalCalendar", new Boolean(enable));
    }

    public void setCalendarRenderWrappers(final UserRequest ureq, final List<CalendarRenderWrapper> calendars) {
        CourseCalendarSubscription subs = new CourseCalendarSubscription(null, ureq.getIdentity());
        subscriptionIds = subs.getSubscribedCourseCalendarIDs();
        setCalendarRenderWrappers(calendars);
    }

    public void setCalendarRenderWrappers(final List<CalendarRenderWrapper> calendars) {
        this.calendars = calendars;
        for (final CalendarRenderWrapper calendar : calendars) {
            calendar.setSubscribed(subscriptionIds.contains(calendar.getCalendar().getCalendarID()));
        }

        configVC.contextPut("calendars", calendars);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == configVC) {
            final String command = event.getCommand();
            if (command.equals(CMD_ADD)) {
                // add new event to calendar
                final String calendarID = ureq.getParameter(PARAM_ID);
                fireEvent(ureq, new CalendarGUIAddEvent(calendarID, new Date()));
            } else if (command.equals(CMD_TOGGLE_DISPLAY)) {
                final String calendarID = ureq.getParameter(PARAM_ID);
                final CalendarRenderWrapper calendarWrapper = findCalendarRenderWrapper(calendarID);
                final CalendarConfig config = calendarWrapper.getCalendarConfig();
                config.setVis(!config.isVis());
                calendarService.saveCalendarConfigForIdentity(config, calendarWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else if (command.equals(CMD_CHOOSE_COLOR)) {
                final String calendarID = ureq.getParameter(PARAM_ID);
                lastCalendarWrapper = findCalendarRenderWrapper(calendarID);
                removeAsListenerAndDispose(colorChooser);
                colorChooser = new CalendarColorChooserController(ureq, getWindowControl(), lastCalendarWrapper.getCalendarConfig().getCss());
                listenTo(colorChooser);
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), colorChooser.getInitialComponent());
                cmc.activate();
                listenTo(cmc);
            } else if (command.equals(CMD_ICAL_FEED)) {
                final String calendarID = ureq.getParameter(PARAM_ID);
                final CalendarRenderWrapper calendarWrapper = findCalendarRenderWrapper(calendarID);
                final String calFeedLink = ICalTokenGenerator.getIcalFeedLink(calendarWrapper.getCalendar().getType(), calendarID, ureq.getIdentity());
                exportController = new CalendarExportController(ureq, getWindowControl(), calFeedLink);
                listenTo(exportController);
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), exportController.getInitialComponent());
                cmc.activate();
                listenTo(cmc);
            } else if (command.equals(CMD_ICAL_REGENERATE)) {
                currentCalendarID = ureq.getParameter(PARAM_ID);
                confirmRegenerateDialog = activateOkCancelDialog(ureq, translate("cal.icalfeed.regenerate.title"), translate("cal.icalfeed.regenerate.warning"),
                        confirmRegenerateDialog);
            } else if (command.equals(CMD_ICAL_REMOVE_FEED)) {
                currentCalendarID = ureq.getParameter(PARAM_ID);
                confirmRemoveDialog = activateOkCancelDialog(ureq, translate("cal.icalfeed.remove.title"), translate("cal.icalfeed.remove.confirmation_message"),
                        confirmRemoveDialog);
            } else if (command.equals(CMD_UNSUBSCRIBE)) {
                currentCalendarID = ureq.getParameter(PARAM_ID);
                final CalendarRenderWrapper calendarWrapper = findCalendarRenderWrapper(currentCalendarID);
                final CalendarSubscription subscription = new CourseCalendarSubscription(currentCalendarID, ureq.getIdentity());
                subscription.unsubscribe();

                for (final Iterator<CalendarRenderWrapper> it = calendars.iterator(); it.hasNext();) {
                    final CalendarRenderWrapper calendar = it.next();
                    if (calendarWrapper.getCalendar().getCalendarID().equals(calendar.getCalendar().getCalendarID())) {
                        it.remove();
                    }
                }
                configVC.contextPut("calendars", calendars);
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == colorChooser) {
            cmc.deactivate();
            if (event == Event.DONE_EVENT) {
                final String choosenColor = colorChooser.getChoosenColor();
                final CalendarConfig config = lastCalendarWrapper.getCalendarConfig();
                config.setCss(choosenColor);
                calendarService.saveCalendarConfigForIdentity(config, lastCalendarWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == confirmRemoveDialog) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                final CalendarRenderWrapper calendarWrapper = findCalendarRenderWrapper(currentCalendarID);
                ICalTokenGenerator.destroyIcalAuthToken(calendarWrapper.getCalendar().getType(), currentCalendarID, ureq.getIdentity());
                showInfo("cal.icalfeed.remove.info");
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == confirmRegenerateDialog) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                final CalendarRenderWrapper calendarWrapper = findCalendarRenderWrapper(currentCalendarID);
                final String regeneratedIcalFeedLink = ICalTokenGenerator.regenerateIcalAuthToken(calendarWrapper.getCalendar().getType(), currentCalendarID,
                        ureq.getIdentity());
                final String calFeedLink = ICalTokenGenerator.getIcalFeedLink(calendarWrapper.getCalendar().getType(), currentCalendarID, ureq.getIdentity());
                exportController = new CalendarExportController(ureq, getWindowControl(), calFeedLink);
                listenTo(exportController);
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), exportController.getInitialComponent());
                cmc.activate();
                listenTo(cmc);
            }
        }
        configVC.setDirty(true);
    }

    private CalendarRenderWrapper findCalendarRenderWrapper(final String calendarID) {
        for (final CalendarRenderWrapper calendarWrapper : calendars) {
            if (calendarWrapper.getCalendar().getCalendarID().equals(calendarID)) {
                return calendarWrapper;
            }
        }
        return null;
    }

    @Override
    protected void doDispose() {
        // controllers disposed by BasicController
        cmc = null;
        colorChooser = null;
    }

}
