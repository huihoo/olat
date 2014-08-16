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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.lms.calendar.CalendarService;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class CalendarEntryDetailsController extends BasicController {

    private final Collection<CalendarRenderWrapper> availableCalendars;
    private final boolean isNew, isReadOnly;
    private CalendarEntry calendarEntry;
    private final Panel mainPanel;
    private final VelocityContainer mainVC, eventVC, linkVC;
    private final TabbedPane pane;
    private final CalendarEntryForm eventForm;
    private LinkProvider activeLinkProvider;
    private DialogBoxController deleteYesNoController;
    private CopyEventToCalendarController copyEventToCalendarController;
    private final Link deleteButton;
    private final CalendarService calendarService;

    public CalendarEntryDetailsController(final UserRequest ureq, final CalendarEntry calendarEntry, final CalendarRenderWrapper calendarWrapper,
            final List<CalendarRenderWrapper> availableCalendars, final boolean isNew, final String caller, final WindowControl wControl) {
        super(ureq, wControl);
        calendarService = CoreSpringFactory.getBean(CalendarService.class);
        this.availableCalendars = availableCalendars;
        this.calendarEntry = calendarEntry;
        this.isNew = isNew;
        mainVC = createVelocityContainer("calEditMain");
        mainVC.contextPut("caller", caller);
        pane = new TabbedPane("pane", getLocale());
        pane.addListener(this);
        mainVC.put("pane", pane);

        // eventVC = new VelocityContainer("calEditDetails", VELOCITY_ROOT + "/calEditDetails.html", getTranslator(), this);
        eventVC = createVelocityContainer("calEditDetails");
        deleteButton = LinkFactory.createButton("cal.edit.delete", eventVC, this);
        eventVC.contextPut("caller", caller);
        eventForm = new CalendarEntryForm(ureq, wControl, calendarEntry, calendarWrapper, availableCalendars, isNew);
        listenTo(eventForm);
        eventVC.put("eventForm", eventForm.getInitialComponent());
        eventVC.contextPut("isNewEvent", new Boolean(isNew));
        isReadOnly = calendarWrapper.getAccess() == CalendarRenderWrapper.ACCESS_READ_ONLY;
        eventVC.contextPut("isReadOnly", new Boolean(isReadOnly));
        pane.addTab(translate("tab.event"), eventVC);

        // linkVC = new VelocityContainer("calEditLinks", VELOCITY_ROOT + "/calEditLinks.html", getTranslator(), this);
        linkVC = createVelocityContainer("calEditLinks");
        linkVC.contextPut("caller", caller);
        if (!isReadOnly) {
            pane.addTab(translate("tab.links"), linkVC);
        }

        // wrap everything in a panel
        mainPanel = putInitialPanel(mainVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == pane) {
            if (event instanceof TabbedPaneChangedEvent) {
                // prepare links tab
                final TabbedPaneChangedEvent tpce = (TabbedPaneChangedEvent) event;
                if (tpce.getNewComponent().equals(linkVC)) {
                    // display link provider if any
                    final String calendarID = eventForm.getChoosenKalendarID();
                    CalendarRenderWrapper calendarWrapper = null;
                    for (final Iterator iter = availableCalendars.iterator(); iter.hasNext();) {
                        calendarWrapper = (CalendarRenderWrapper) iter.next();
                        if (calendarWrapper.getCalendar().getCalendarID().equals(calendarID)) {
                            break;
                        }
                    }

                    if (activeLinkProvider == null) {
                        activeLinkProvider = calendarWrapper.getLinkProvider();
                        if (activeLinkProvider != null) {
                            activeLinkProvider.addControllerListener(this);
                            activeLinkProvider.setCalendarEntry(calendarEntry);
                            activeLinkProvider.setDisplayOnly(isReadOnly);
                            linkVC.put("linkprovider", activeLinkProvider.getControler().getInitialComponent());
                            linkVC.contextPut("hasLinkProvider", Boolean.TRUE);
                        } else {
                            linkVC.contextPut("hasLinkProvider", Boolean.FALSE);
                        }
                    }
                }
            }
        } else if (source == deleteButton) {
            // delete calendar entry
            deleteYesNoController = activateYesNoDialog(ureq, null, translate("cal.delete.dialogtext"), deleteYesNoController);
            return;
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == deleteYesNoController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final OlatCalendar cal = calendarEntry.getCalendar();
                calendarService.removeEntryFrom(cal, calendarEntry);
                fireEvent(ureq, Event.DONE_EVENT);
            }
        } else if (source == copyEventToCalendarController) {
            if (event.equals(Event.DONE_EVENT)) {
                fireEvent(ureq, Event.DONE_EVENT);
            } else if (event.equals(Event.CANCELLED_EVENT)) {
                mainPanel.setContent(mainVC);
            }
        } else if (source == activeLinkProvider) {
            fireEvent(ureq, Event.DONE_EVENT);
        } else if (source == eventForm) {
            if (event == Event.DONE_EVENT) {
                // ok, save edited entry
                calendarEntry = eventForm.getUpdatedKalendarEvent();
                boolean doneSuccessfully = true;
                if (isNew) {
                    // this is a new event, add event to calendar
                    final String calendarID = eventForm.getChoosenKalendarID();
                    for (final Iterator iter = availableCalendars.iterator(); iter.hasNext();) {
                        final CalendarRenderWrapper calendarWrapper = (CalendarRenderWrapper) iter.next();
                        if (!calendarWrapper.getCalendar().getCalendarID().equals(calendarID)) {
                            continue;
                        }
                        final OlatCalendar cal = calendarWrapper.getCalendar();
                        final boolean result = calendarService.addEntryTo(cal, calendarEntry);
                        if (result == false) {
                            // if one failed => done not successfully
                            doneSuccessfully = false;
                        }
                    }
                } else {
                    // this is an existing event, so we get the previousely assigned calendar from the event
                    final OlatCalendar cal = calendarEntry.getCalendar();
                    doneSuccessfully = calendarService.updateEntryFrom(cal, calendarEntry);
                }
                // check if event is still available
                if (!doneSuccessfully) {
                    showError("cal.error.save");
                    fireEvent(ureq, Event.FAILED_EVENT);
                    return;
                }

                if (eventForm.isMulti()) {
                    // offer to copy event to multiple calendars.
                    removeAsListenerAndDispose(copyEventToCalendarController);
                    copyEventToCalendarController = new CopyEventToCalendarController(ureq, getWindowControl(), calendarEntry, availableCalendars, getTranslator());
                    listenTo(copyEventToCalendarController);
                    // copyEventToCalendarController.addControllerListener(this);
                    mainPanel.setContent(copyEventToCalendarController.getInitialComponent());
                    return;
                }

                // saving was ok, finish workflow
                fireEvent(ureq, Event.DONE_EVENT);

            } else if (event == Event.CANCELLED_EVENT) {
                eventForm.setEntry(calendarEntry);
                // user canceled, finish workflow
                fireEvent(ureq, Event.DONE_EVENT);
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }

    public CalendarEntry getKalendarEvent() {
        return calendarEntry;
    }

}
