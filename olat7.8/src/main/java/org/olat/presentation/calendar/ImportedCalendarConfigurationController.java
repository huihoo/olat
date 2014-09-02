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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.calendar.ImportCalendarManager;
import org.olat.lms.commons.LearnServices;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.calendar.events.CalendarGUIAddEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;

public class ImportedCalendarConfigurationController extends BasicController {

    private static final Object CMD_ADD = "add";
    private static final Object CMD_TOGGLE_DISPLAY = "tglvis";
    private static final Object CMD_CHOOSE_COLOR = "cc";
    private static final Object CMD_REMOVE_CALENDAR = "rm";
    private static final String PARAM_ID = "id";

    private final VelocityContainer configVC;
    private List importedCalendarWrappers;
    private CalendarColorChooserController colorChooser;
    private CalendarRenderWrapper lastCalendarWrapper;
    private CloseableModalController cmc;
    private DialogBoxController confirmRemoveDialog;
    private String currentCalendarID;
    private final Link manageCalendarsButton;
    private ManageCalendarsController manageCalendarsController;
    final CalendarService calendarService;
    final ImportCalendarManager importCalendarManager;

    public ImportedCalendarConfigurationController(final List importedCalendarWrappers, final UserRequest ureq, final WindowControl wControl, final boolean insideManager) {
        super(ureq, wControl);
        this.importedCalendarWrappers = importedCalendarWrappers;
        calendarService = getService(LearnServices.calendarService);
        importCalendarManager = getService(LearnServices.importCalendarManager);
        configVC = createVelocityContainer("importedCalConfig");
        configVC.contextPut("calendars", importedCalendarWrappers);
        configVC.contextPut("insideManager", insideManager);
        manageCalendarsButton = LinkFactory.createButton("cal.managecalendars", configVC, this);
        putInitialPanel(configVC);
    }

    public void setCalendars(final List calendars) {
        this.importedCalendarWrappers = calendars;
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
                final CalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
                final CalendarConfig config = calendarWrapper.getCalendarConfig();
                config.setVis(!config.isVis());
                calendarService.saveCalendarConfigForIdentity(config, calendarWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
                fireEvent(ureq, Event.CHANGED_EVENT);
            } else if (command.equals(CMD_CHOOSE_COLOR)) {
                final String calendarID = ureq.getParameter(PARAM_ID);
                lastCalendarWrapper = findKalendarRenderWrapper(calendarID);
                removeAsListenerAndDispose(colorChooser);
                colorChooser = new CalendarColorChooserController(ureq, getWindowControl(), lastCalendarWrapper.getCalendarConfig().getCss());
                listenTo(colorChooser);
                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), colorChooser.getInitialComponent());
                cmc.activate();
                listenTo(cmc);
            } else if (command.equals(CMD_REMOVE_CALENDAR)) {
                currentCalendarID = ureq.getParameter(PARAM_ID);
                confirmRemoveDialog = activateOkCancelDialog(ureq, translate("cal.import.remove.title"), translate("cal.import.remove.confirmation_message"),
                        confirmRemoveDialog);
            }
        } else if (source == manageCalendarsButton) {
            removeAsListenerAndDispose(manageCalendarsController);
            importedCalendarWrappers = importCalendarManager.getImportedCalendarsForIdentity(ureq);
            manageCalendarsController = new ManageCalendarsController(ureq, ureq.getLocale(), getWindowControl(), importedCalendarWrappers);
            listenTo(manageCalendarsController);
            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), this.translate("close"), manageCalendarsController.getInitialComponent());
            cmc.activate();
            listenTo(cmc);
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
                // remove the imported calendar
                importCalendarManager.deleteCalendar(currentCalendarID, ureq);

                // update the calendar list
                importedCalendarWrappers = importCalendarManager.getImportedCalendarsForIdentity(ureq);
                configVC.contextPut("calendars", importedCalendarWrappers);

                // show the information that the calendar has been deleted
                showInfo("cal.import.remove.info");
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == cmc) {
            importedCalendarWrappers = importCalendarManager.getImportedCalendarsForIdentity(ureq);
            configVC.setDirty(true);
            fireEvent(ureq, Event.CHANGED_EVENT);
        }
    }

    private CalendarRenderWrapper findKalendarRenderWrapper(final String calendarID) {
        for (final Iterator iter = importedCalendarWrappers.iterator(); iter.hasNext();) {
            final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) iter.next();
            if (calendarWrapper.getCalendar().getCalendarID().equals(calendarID)) {
                return calendarWrapper;
            }
        }
        return null;
    }

    private String getCalendarType(final String calendarID) {
        final CalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
        return calendarWrapper.getCalendar().getType();
    }

    @Override
    protected void doDispose() {
        // controllers disposed by BasicController
        cmc = null;
        colorChooser = null;
    }

}
