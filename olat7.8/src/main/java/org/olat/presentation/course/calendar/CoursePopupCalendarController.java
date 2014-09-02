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

package org.olat.presentation.course.calendar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.calendar.LinkProvider;
import org.olat.presentation.calendar.WeeklyCalendarController;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.calendar.events.CalendarModifiedEvent;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class CoursePopupCalendarController extends BasicController {

    private final CalendarController calendarController;
    private CalendarRenderWrapper courseKalendarWrapper;
    private final OLATResourceable ores;
    private final CalendarService calendarService;

    public CoursePopupCalendarController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable course) {
        super(ureq, wControl);
        this.ores = course;
        calendarService = CoreSpringFactory.getBean(CalendarService.class);
        final List calendars = getListOfCalendarWrappers(ureq);
        final CourseCalendarSubscription calendarSubscription = new CourseCalendarSubscription(courseKalendarWrapper.getCalendar().getCalendarID(), ureq.getIdentity());
        calendarController = new WeeklyCalendarController(ureq, wControl, calendars, WeeklyCalendarController.CALLER_COURSE, calendarSubscription, true);
        listenTo(calendarController);
        putInitialPanel(calendarController.getInitialComponent());
    }

    private List getListOfCalendarWrappers(final UserRequest ureq) {
        final List calendars = new ArrayList();
        // add course calendar
        final ICourse course = CourseFactory.loadCourse(ores);
        final OlatCalendar courseCalendar = calendarService.getCourseCalendar(course);
        courseKalendarWrapper = CalendarRenderWrapper.wrapCourseCalendar(courseCalendar, course);
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        final Identity identity = ureq.getIdentity();
        final boolean isPrivileged = cgm.isIdentityCourseAdministrator(identity, course)
                || cgm.hasRight(identity, CourseRights.RIGHT_COURSEEDITOR, course)
                || RepositoryServiceImpl.getInstance().isInstitutionalRessourceManagerFor(RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, false),
                        identity);
        if (isPrivileged) {
            courseKalendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
        } else {
            courseKalendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_ONLY);
        }
        final CalendarConfig config = calendarService.findCalendarConfigForIdentity(courseKalendarWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
        if (config != null) {
            courseKalendarWrapper.getCalendarConfig().setCss(config.getCss());
            courseKalendarWrapper.getCalendarConfig().setVis(config.isVis());
        }
        // add link provider
        final CourseLinkProviderController clpc = new CourseLinkProviderController(course, ureq, getWindowControl());
        courseKalendarWrapper.setLinkProvider(clpc);
        calendars.add(courseKalendarWrapper);

        // add course group calendars
        final boolean isGroupManager = cgm.isIdentityCourseAdministrator(identity, course) || cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT, course);
        if (isGroupManager) {
            // learning groups
            List allGroups = cgm.getAllLearningGroupsFromAllContexts(course);
            addCalendars(ureq, allGroups, true, clpc, calendars);
            // right groups
            allGroups = cgm.getAllRightGroupsFromAllContexts(course);
            addCalendars(ureq, allGroups, true, clpc, calendars);
        } else {
            // learning groups
            final List ownerGroups = cgm.getOwnedLearningGroupsFromAllContexts(identity, course);
            addCalendars(ureq, ownerGroups, true, clpc, calendars);
            final List attendedGroups = cgm.getParticipatingLearningGroupsFromAllContexts(identity, course);
            for (final Iterator ownerGroupsIterator = ownerGroups.iterator(); ownerGroupsIterator.hasNext();) {
                final BusinessGroup ownerGroup = (BusinessGroup) ownerGroupsIterator.next();
                if (attendedGroups.contains(ownerGroup)) {
                    attendedGroups.remove(ownerGroup);
                }
            }
            addCalendars(ureq, attendedGroups, false, clpc, calendars);

            // right groups
            final List rightGroups = cgm.getParticipatingRightGroupsFromAllContexts(identity, course);
            addCalendars(ureq, rightGroups, false, clpc, calendars);
        }
        return calendars;
    }

    private void addCalendars(final UserRequest ureq, final List groups, final boolean isOwner, final LinkProvider linkProvider, final List calendars) {
        final CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup bGroup = (BusinessGroup) iter.next();
            final CollaborationTools collabTools = collabFactory.getOrCreateCollaborationTools(bGroup);
            if (!collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
                continue;
            }
            final OlatCalendar groupCalendar = calendarService.getGroupCalendar(bGroup);
            final CalendarRenderWrapper groupCalendarWrapper = CalendarRenderWrapper.wrapGroupCalendar(groupCalendar, bGroup);
            // set calendar access
            int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
            final Long lCalAccess = collabTools.lookupCalendarAccess();
            if (lCalAccess != null) {
                iCalAccess = lCalAccess.intValue();
            }
            if (iCalAccess == CollaborationTools.CALENDAR_ACCESS_OWNERS && !isOwner) {
                groupCalendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_ONLY);
            } else {
                groupCalendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
            }
            final CalendarConfig config = calendarService.findCalendarConfigForIdentity(groupCalendarWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
            if (config != null) {
                groupCalendarWrapper.getCalendarConfig().setCss(config.getCss());
                groupCalendarWrapper.getCalendarConfig().setVis(config.isVis());
            }
            groupCalendarWrapper.setLinkProvider(linkProvider);
            calendars.add(groupCalendarWrapper);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to do
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event instanceof CalendarModifiedEvent) {
            final List calendars = getListOfCalendarWrappers(ureq);
            calendarController.setCalendars(calendars);
        }
    }

    @Override
    protected void doDispose() {
        //
    }

}
