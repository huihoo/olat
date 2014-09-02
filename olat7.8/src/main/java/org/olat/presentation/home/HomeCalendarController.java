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

package org.olat.presentation.home;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.calendar.CalendarDao;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.calendar.CalendarWrapperCreator;
import org.olat.presentation.calendar.WeeklyCalendarController;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

public class HomeCalendarController extends BasicController implements Activateable, GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    private final UserSession userSession;
    private final CalendarController calendarController;
    private CalendarWrapperCreator calendarWrapperCreator;

    public HomeCalendarController(final UserRequest ureq, final WindowControl windowControl) {
        super(ureq, windowControl);
        this.userSession = ureq.getUserSession();
        calendarWrapperCreator = CoreSpringFactory.getBean(CalendarWrapperCreator.class);

        userSession.getSingleUserEventCenter().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarDao.class));
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarDao.class));

        final List<CalendarRenderWrapper> calendars = calendarWrapperCreator.getListOfCalendarWrappers(ureq, windowControl);
        final List importedCalendars = calendarWrapperCreator.getListOfImportedCalendarWrappers(ureq);
        calendarController = new WeeklyCalendarController(ureq, windowControl, calendars, importedCalendars, WeeklyCalendarController.CALLER_HOME, false);
        listenTo(calendarController);

        putInitialPanel(calendarController.getInitialComponent());
    }

    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        final String[] splitted = viewIdentifier.split("\\.");
        if (splitted.length != 3) {
            // do nothing for user, just ignore it maybe this is a javascript
            // problem of the browser. However, log the problem
            log.warn("Can't parse date from user request: " + viewIdentifier);
            return;
        }
        final String year = splitted[0];
        final String month = splitted[1];
        final String day = splitted[2];
        final Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
        calendarController.setFocus(cal.getTime());
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do here
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event instanceof CalendarModifiedEvent) {
            final List<CalendarRenderWrapper> calendars = calendarWrapperCreator.getListOfCalendarWrappers(ureq, getWindowControl());
            final List importedCalendars = calendarWrapperCreator.getListOfImportedCalendarWrappers(ureq);
            calendarController.setCalendars(calendars, importedCalendars);
        }
    }

    @Override
    protected void doDispose() {
        // remove from event bus
        userSession.getSingleUserEventCenter().deregisterFor(this, OresHelper.lookupType(CalendarDao.class));
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CalendarDao.class));
    }

    @Override
    public void event(final Event event) {
        if (event instanceof CalendarModifiedEvent) {
            if (calendarController != null) {
                // could theoretically be disposed
                calendarController.setDirty();
            }
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
