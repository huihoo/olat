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
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 * <p>
 */
package org.olat.presentation.calendar;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.calendar.CalendarEntry;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.group.BusinessGroup;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.course.CourseModule;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.lms.notifications.NotificationsUpgradeHelper;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.notifications.SubscriptionListItem;
import org.olat.presentation.notifications.TitleItem;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Implementation for NotificationHandler of calendars. For more information see JIRA ticket OLAT-3861.
 * <P>
 * Initial Date: 22.12.2008 <br>
 * 
 * @author bja
 */
@Component
public class CalendarNotificationHandler implements NotificationsHandler {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CSS_CLASS_CALENDAR_ICON = "o_calendar_icon";
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    CalendarService calendarService;
    @Autowired
    NotificationService notificationService;

    /**
     * [spring]
     */
    private CalendarNotificationHandler() {
        //
    }

    @Override
    public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, final Locale locale, final Date compareDate) {
        SubscriptionInfo si = null;
        final Publisher p = subscriber.getPublisher();
        final Date latestNews = p.getLatestNewsDate();

        // do not try to create a subscription info if state is deleted - results in
        // exceptions, course
        // can't be loaded when already deleted
        if (notificationService.isPublisherValid(p) && compareDate.before(latestNews)) {
            final Long id = p.getResId();
            final String type = p.getSubidentifier();

            try {
                final Translator translator = PackageUtil.createPackageTranslator(CalendarController.class, locale);

                String calType = null;
                String title = null;
                if (type.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
                    final String displayName = RepositoryServiceImpl.getInstance().lookupDisplayNameByOLATResourceableId(id);
                    calType = CalendarDao.TYPE_COURSE;
                    title = translator.translate("cal.notifications.header.course", new String[] { displayName });
                } else if (type.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
                    final BusinessGroup group = businessGroupService.loadBusinessGroup(id, false);
                    calType = CalendarDao.TYPE_GROUP;
                    title = translator.translate("cal.notifications.header.group", new String[] { group.getName() });
                }

                if (calType != null) {
                    final Formatter form = Formatter.getInstance(locale);
                    si = new SubscriptionInfo(new TitleItem(title, CSS_CLASS_CALENDAR_ICON), null);

                    String bPath;
                    if (StringHelper.containsNonWhitespace(p.getBusinessPath())) {
                        bPath = p.getBusinessPath();
                    } else if ("CalendarManager.course".equals(p.getResName())) {
                        try {
                            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.getCourseTypeName(), p.getResId());
                            final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(ores, true);
                            bPath = "[RepositoryEntry:" + re.getKey() + "]";// Fallback
                        } catch (final Exception e) {
                            log.error("Error processing calendar notifications of publisher:" + p.getKey(), e);
                            return notificationService.getNoSubscriptionInfo();
                        }
                    } else {
                        // cannot make link without business path
                        return notificationService.getNoSubscriptionInfo();
                    }

                    final OlatCalendar cal = calendarService.getCalendar(calType, id.toString());
                    final Collection<CalendarEntry> calEvents = cal.getAllCalendarEntries();
                    for (final CalendarEntry calendarEntry : calEvents) {
                        if (showEvent(compareDate, calendarEntry)) {
                            log.debug("found a KalendarEvent: " + calendarEntry.getSubject() + " with time: " + calendarEntry.getBegin() + " modified before: "
                                    + compareDate.toString(), null);
                            // found a modified event in this calendar
                            Date modDate = null;
                            if (calendarEntry.getLastModified() > 0) {
                                modDate = new Date(calendarEntry.getLastModified());
                            } else if (calendarEntry.getCreated() > 0) {
                                modDate = new Date(calendarEntry.getCreated());
                            } else if (calendarEntry.getBegin() != null) {
                                modDate = calendarEntry.getBegin();
                            }

                            final String subject = calendarEntry.getSubject();
                            String author = calendarEntry.getCreatedBy();
                            if (author == null) {
                                author = "";
                            }

                            String location = "";
                            if (StringHelper.containsNonWhitespace(calendarEntry.getLocation())) {
                                location = calendarEntry.getLocation() == null ? "" : translator.translate("cal.notifications.location",
                                        new String[] { calendarEntry.getLocation() });
                            }
                            String dateStr;
                            if (calendarEntry.isAllDayEvent()) {
                                dateStr = form.formatDate(calendarEntry.getBegin());
                            } else {
                                dateStr = form.formatDate(calendarEntry.getBegin()) + " - " + form.formatDate(calendarEntry.getEnd());
                            }
                            final String desc = translator.translate("cal.notifications.entry", new String[] { subject, dateStr, location, author });
                            final String businessPath = bPath + "[path=" + calendarEntry.getID() + ":0]";
                            final String urlToSend = NotificationHelper.getURLFromBusinessPathString(p, businessPath);
                            final SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, modDate, CSS_CLASS_CALENDAR_ICON);
                            si.addSubscriptionListItem(subListItem);
                        }
                    }
                }
            } catch (final Exception e) {
                log.error("Unexpected exception", e);
                checkPublisher(p);
                si = notificationService.getNoSubscriptionInfo();
            }
        } else {
            si = notificationService.getNoSubscriptionInfo();
        }
        return si;
    }

    private void checkPublisher(final Publisher p) {
        try {
            if (CalendarController.ACTION_CALENDAR_GROUP.equals(p.getSubidentifier())) {
                final BusinessGroup bg = businessGroupService.loadBusinessGroup(p.getResId(), false);
                if (bg == null) {
                    log.info("deactivating publisher with key; " + p.getKey(), null);
                    notificationService.deactivate(p);
                }
            } else if (CalendarController.ACTION_CALENDAR_COURSE.equals(p.getSubidentifier())) {
                if (!NotificationsUpgradeHelper.isCourseRepositoryEntryFound(p)) {
                    log.info("deactivating publisher with key; " + p.getKey(), null);
                    notificationService.deactivate(p);
                }
            }
        } catch (final Exception e) {
            log.error("", e);
        }
    }

    private boolean showEvent(final Date compareDate, final CalendarEntry calendarEntry) {
        if (calendarEntry.getLastModified() > 0) {
            return compareDate.getTime() < calendarEntry.getLastModified();
        }
        if (calendarEntry.getCreated() > 0) {
            return compareDate.getTime() < calendarEntry.getCreated();
        }
        return false;
    }

    @Override
    public String createTitleInfo(final Subscriber subscriber, final Locale locale) {
        try {
            final Translator translator = PackageUtil.createPackageTranslator(CalendarController.class, locale);
            String title = null;
            final Long id = subscriber.getPublisher().getResId();
            final String type = subscriber.getPublisher().getSubidentifier();
            if (type.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
                final String displayName = RepositoryServiceImpl.getInstance().lookupDisplayNameByOLATResourceableId(id);
                title = translator.translate("cal.notifications.header.course", new String[] { displayName });
            } else if (type.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
                final BusinessGroup group = businessGroupService.loadBusinessGroup(id, false);
                title = translator.translate("cal.notifications.header.group", new String[] { group.getName() });
            }
            return title;
        } catch (final Exception e) {
            log.error("Error while creating calendar notifications for subscriber: " + subscriber.getKey(), e);
            checkPublisher(subscriber.getPublisher());
            return "-";
        }
    }

    @Override
    public String getType() {
		return CalendarDao.CALENDAR_MANAGER;
    }

}
