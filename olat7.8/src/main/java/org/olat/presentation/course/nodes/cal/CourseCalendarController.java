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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.presentation.course.nodes.cal;

import java.util.Date;
import java.util.List;

import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.presentation.calendar.WeeklyCalendarController;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.presentation.course.calendar.CourseCalendarSubscription;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.clone.CloneableController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> This is wrapper around the WeeklyCalendarController.
 * <p>
 * Initial Date: 10 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseCalendarController extends DefaultController implements CloneableController {

    private final WeeklyCalendarController calendarController;
    private CalendarRenderWrapper courseKalendarWrapper;
    private CourseCalendarSubscription calendarSubscription;

    private final NodeEvaluation nodeEvaluation;

    private final OLATResourceable ores;
    private List<CalendarRenderWrapper> calendars;

    public CourseCalendarController(final UserRequest ureq, final WindowControl wControl, final CourseCalendars myCal,
            final CourseCalendarSubscription calendarSubscription, final OLATResourceable course, final NodeEvaluation ne) {
        super(wControl);
        this.ores = course;
        this.nodeEvaluation = ne;
        calendars = myCal.getCalendars();
        courseKalendarWrapper = myCal.getCourseKalendarWrapper();
        calendarController = new WeeklyCalendarController(ureq, wControl, calendars, WeeklyCalendarController.CALLER_COURSE, calendarSubscription, true);
        calendarController.setEnableRemoveFromPersonalCalendar(false);
        setInitialComponent(calendarController.getInitialComponent());
    }

    public CourseCalendarSubscription getCalendarSubscription() {
        return calendarSubscription;
    }

    public CalendarRenderWrapper getCourseKalendarWrapper() {
        return courseKalendarWrapper;
    }

    public OLATResourceable getOres() {
        return ores;
    }

    public void setFocus(final Date date) {
        calendarController.setFocus(date);
    }

    public void setFocusOnEvent(final String eventId) {
        calendarController.setFocusOnEvent(eventId);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event instanceof CalendarModifiedEvent) {
            final CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, getWindowControl(), ores, nodeEvaluation);
            calendars = myCal.getCalendars();
            courseKalendarWrapper = myCal.getCourseKalendarWrapper();
            calendarController.setCalendars(calendars);
        }
    }

    @Override
    protected void doDispose() {
        calendarController.dispose();
    }

    @Override
    public Controller cloneController(final UserRequest ureq, final WindowControl wControl) {
        final CourseCalendars myCal = new CourseCalendars(courseKalendarWrapper, calendars);
        final CourseCalendarSubscription calSubscription = myCal.createSubscription(ureq);

        final int weekOfYear = calendarController.getFocusWeekOfYear();
        final int year = calendarController.getFocusYear();

        final CourseCalendarController ctrl = new CourseCalendarController(ureq, wControl, myCal, calSubscription, ores, nodeEvaluation);
        ctrl.calendarController.setFocus(year, weekOfYear);
        return ctrl;
    }
}
