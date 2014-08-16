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
import java.util.Iterator;
import java.util.List;

import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.calendar.ImportCalendarManager;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.course.calendar.CourseCalendarSubscription;
import org.olat.presentation.course.calendar.CourseLinkProviderController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for CalendarWrapperCreator
 * 
 * <P>
 * Initial Date: 30.08.2011 <br>
 * 
 * @author cg
 */
@Component
public class CalendarWrapperCreator {

    @Autowired
    CalendarService calendarService;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ImportCalendarManager importCalendarManager;

    /**
	 * 
	 */
    public List<CalendarRenderWrapper> getListOfCalendarWrappers(final UserRequest ureq, final WindowControl wControl) {
        final List<CalendarRenderWrapper> calendars = new ArrayList<CalendarRenderWrapper>();
        // get the personal calendar
        OlatCalendar calendar = calendarService.getPersonalCalendar(ureq.getIdentity());
        final CalendarRenderWrapper calendarWrapper = CalendarRenderWrapper.wrapPersonalCalendar(calendar, ureq.getIdentity().getName());

        calendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
        final CalendarConfig personalKalendarConfig = calendarService.findCalendarConfigForIdentity(calendarWrapper.getCalendar(), ureq.getUserSession()
                .getGuiPreferences());
        if (personalKalendarConfig != null) {
            calendarWrapper.getCalendarConfig().setCss(personalKalendarConfig.getCss());
            calendarWrapper.getCalendarConfig().setVis(personalKalendarConfig.isVis());
        }
        calendars.add(calendarWrapper);

        final List<BusinessGroup> ownerGroups = businessGroupService.findBusinessGroupsOwnedBy(null, ureq.getIdentity(), null);
        addCalendars(ureq, ownerGroups, true, calendars);
        final List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroupsAttendedBy(null, ureq.getIdentity(), null);
        for (final Iterator<BusinessGroup> ownerGroupsIterator = ownerGroups.iterator(); ownerGroupsIterator.hasNext();) {
            final BusinessGroup ownerGroup = ownerGroupsIterator.next();
            if (attendedGroups.contains(ownerGroup)) {
                attendedGroups.remove(ownerGroup);
            }
        }
        addCalendars(ureq, attendedGroups, false, calendars);

        // add course calendars
        CourseCalendarSubscription subs = new CourseCalendarSubscription(null, ureq.getIdentity());
        List<String> subscribedCourseCalendarIDs = subs.getSubscribedCourseCalendarIDs();

        final List<String> calendarIDsToBeRemoved = new ArrayList<String>();
        for (final Iterator<String> iter = subscribedCourseCalendarIDs.iterator(); iter.hasNext();) {
            final String courseCalendarID = iter.next();
            final long courseResourceableID = Long.parseLong(courseCalendarID);

            final RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntry(new OLATResourceable() {

                @Override
                public Long getResourceableId() {
                    return new Long(courseResourceableID);
                }

                @Override
                public String getResourceableTypeName() {
                    return CourseModule.getCourseTypeName();
                }
            }, false);
            if (repoEntry == null) {
                // mark calendar ID for cleanup
                calendarIDsToBeRemoved.add(courseCalendarID);
                continue;
            }
            final ICourse course = CourseFactory.loadCourse(new Long(courseResourceableID));
            // calendar course aren't enabled per default but course node of type calendar are always possible
            // REVIEW if (!course.getCourseEnvironment().getCourseConfig().isCalendarEnabled()) continue;
            // add course calendar
            final OlatCalendar courseCalendar = calendarService.getCourseCalendar(course);
            final CalendarRenderWrapper courseCalendarWrapper = CalendarRenderWrapper.wrapCourseCalendar(courseCalendar, course);
            final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
            final boolean isPrivileged = cgm.isIdentityCourseAdministrator(ureq.getIdentity(), course)
                    || cgm.hasRight(ureq.getIdentity(), CourseRights.RIGHT_COURSEEDITOR, course);
            if (isPrivileged) {
                courseCalendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
            } else {
                courseCalendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_ONLY);
            }
            final CalendarConfig courseKalendarConfig = calendarService.findCalendarConfigForIdentity(courseCalendarWrapper.getCalendar(), ureq.getUserSession()
                    .getGuiPreferences());
            if (courseKalendarConfig != null) {
                courseCalendarWrapper.getCalendarConfig().setCss(courseKalendarConfig.getCss());
                courseCalendarWrapper.getCalendarConfig().setVis(courseKalendarConfig.isVis());
            }
            courseCalendarWrapper.setLinkProvider(new CourseLinkProviderController(course, ureq, wControl));
            calendars.add(courseCalendarWrapper);
        }

        // do calendar ID cleanup
        if (!calendarIDsToBeRemoved.isEmpty()) {
            subscribedCourseCalendarIDs = subs.getSubscribedCourseCalendarIDs();
            for (final Iterator<String> iter = calendarIDsToBeRemoved.iterator(); iter.hasNext();) {
                subscribedCourseCalendarIDs.remove(iter.next());
            }
            subs.persistSubscribedCalendarIDs(subscribedCourseCalendarIDs, ureq.getIdentity());
        }
        return calendars;
    }

    public List getListOfImportedCalendarWrappers(final UserRequest ureq) {
        importCalendarManager.reloadUrlImportedCalendars(ureq);
        return importCalendarManager.getImportedCalendarsForIdentity(ureq);
    }

    private void addCalendars(final UserRequest ureq, final List<BusinessGroup> groups, final boolean isOwner, final List<CalendarRenderWrapper> calendars) {
        final CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
        for (final Iterator<BusinessGroup> iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup bGroup = iter.next();
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
            final CalendarConfig groupKalendarConfig = calendarService.findCalendarConfigForIdentity(groupCalendarWrapper.getCalendar(), ureq.getUserSession()
                    .getGuiPreferences());
            if (groupKalendarConfig != null) {
                groupCalendarWrapper.getCalendarConfig().setCss(groupKalendarConfig.getCss());
                groupCalendarWrapper.getCalendarConfig().setVis(groupKalendarConfig.isVis());
            }
            calendars.add(groupCalendarWrapper);
        }
    }

}
