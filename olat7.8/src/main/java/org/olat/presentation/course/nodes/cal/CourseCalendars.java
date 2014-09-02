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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CalCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.calendar.LinkProvider;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.course.calendar.CourseCalendarSubscription;
import org.olat.presentation.course.calendar.CourseLinkProviderController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;

public class CourseCalendars {

    private CalendarRenderWrapper courseKalendarWrapper;
    private List<CalendarRenderWrapper> calendars;

    public CourseCalendars(final CalendarRenderWrapper courseKalendarWrapper, final List<CalendarRenderWrapper> calendars) {
        this.courseKalendarWrapper = courseKalendarWrapper;
        this.calendars = calendars;
    }

    public List<CalendarRenderWrapper> getCalendars() {
        return calendars;
    }

    public void setCalendars(final List<CalendarRenderWrapper> calendars) {
        this.calendars = calendars;
    }

    public CalendarRenderWrapper getCourseKalendarWrapper() {
        return courseKalendarWrapper;
    }

    public void setCourseKalendarWrapper(final CalendarRenderWrapper courseKalendarWrapper) {
        this.courseKalendarWrapper = courseKalendarWrapper;
    }

    public OlatCalendar getKalendar() {
        return courseKalendarWrapper.getCalendar();
    }

    public CourseCalendarSubscription createSubscription(final UserRequest ureq) {
        final CourseCalendarSubscription calSubscription = new CourseCalendarSubscription(getKalendar().getCalendarID(), ureq.getIdentity());
        return calSubscription;
    }

    public static CourseCalendars createCourseCalendarsWrapper(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final NodeEvaluation ne) {
        final List<CalendarRenderWrapper> calendars = new ArrayList<CalendarRenderWrapper>();
        final CalendarService calendarService = CoreSpringFactory.getBean(CalendarService.class);
        // add course calendar
        final ICourse course = CourseFactory.loadCourse(ores);
        final OlatCalendar courseCalendar = calendarService.getCourseCalendar(course);
        final CalendarRenderWrapper courseKalendarWrapper = CalendarRenderWrapper.wrapCourseCalendar(courseCalendar, course);
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        final Identity identity = ureq.getIdentity();
        final boolean isPrivileged = cgm.isIdentityCourseAdministrator(identity, course)
                || ne.isCapabilityAccessible(CalCourseNode.EDIT_CONDITION_ID)
                || RepositoryServiceImpl.getInstance().isInstitutionalRessourceManagerFor(RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, false),
                        identity) || ureq.getUserSession().getRoles().isOLATAdmin();
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
        final CourseLinkProviderController clpc = new CourseLinkProviderController(course, ureq, wControl);
        courseKalendarWrapper.setLinkProvider(clpc);
        calendars.add(courseKalendarWrapper);

        // add course group calendars
        final boolean isGroupManager = cgm.isIdentityCourseAdministrator(identity, course) || cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT, course);
        if (isGroupManager) {
            // learning groups
            List<BusinessGroup> allGroups = cgm.getAllLearningGroupsFromAllContexts(course);
            addCalendars(ureq, allGroups, true, clpc, calendars);
            // right groups
            allGroups = cgm.getAllRightGroupsFromAllContexts(course);
            addCalendars(ureq, allGroups, true, clpc, calendars);
        } else {
            // learning groups
            final List<BusinessGroup> ownerGroups = cgm.getOwnedLearningGroupsFromAllContexts(identity, course);
            addCalendars(ureq, ownerGroups, true, clpc, calendars);
            final List<BusinessGroup> attendedGroups = cgm.getParticipatingLearningGroupsFromAllContexts(identity, course);
            for (final BusinessGroup ownerGroup : ownerGroups) {
                if (attendedGroups.contains(ownerGroup)) {
                    attendedGroups.remove(ownerGroup);
                }
            }
            addCalendars(ureq, attendedGroups, false, clpc, calendars);

            // right groups
            final List<BusinessGroup> rightGroups = cgm.getParticipatingRightGroupsFromAllContexts(identity, course);
            addCalendars(ureq, rightGroups, false, clpc, calendars);
        }
        return new CourseCalendars(courseKalendarWrapper, calendars);
    }

    private static void addCalendars(final UserRequest ureq, final List<BusinessGroup> groups, final boolean isOwner, final LinkProvider linkProvider,
            final List<CalendarRenderWrapper> calendars) {
        final CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
        final CalendarService calendarService = CoreSpringFactory.getBean(CalendarService.class);
        for (final BusinessGroup bGroup : groups) {
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
}
