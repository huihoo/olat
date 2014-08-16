/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package org.olat.presentation.notifications;

import org.olat.data.calendar.CalendarDao;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.ICourse;
import org.olat.lms.notifications.PublisherData;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.presentation.calendar.CalendarController;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * Managed different subscription sources.
 * <P>
 * Initial Date: 29.04.2009 <br>
 * 
 * @author bja
 */
public class SubscriptionProviderImpl implements SubscriptionProvider {

    private final String caller;
    private final CalendarRenderWrapper calendarRenderWrapper;
    private final SubscriptionContext subscriptionContext;
    private ICourse course;
    private BusinessGroup businessGroup;

    public SubscriptionProviderImpl(final CalendarRenderWrapper calendarRenderWrapper) {
        this.calendarRenderWrapper = calendarRenderWrapper;
        this.caller = calendarRenderWrapper.getCalendar().getType();
        this.subscriptionContext = setSubscriptionContext();
    }

    public SubscriptionProviderImpl(final String caller, final CalendarRenderWrapper calendarRenderWrapper) {
        this.calendarRenderWrapper = calendarRenderWrapper;
        this.caller = caller;
        this.subscriptionContext = setSubscriptionContext();
    }

    public SubscriptionProviderImpl(final CalendarRenderWrapper calendarRenderWrapper, final ICourse course) {
        this.calendarRenderWrapper = calendarRenderWrapper;
        this.caller = calendarRenderWrapper.getCalendar().getType();
        this.course = course;
        this.subscriptionContext = setSubscriptionContext();
    }

    private SubscriptionContext setSubscriptionContext() {
        SubscriptionContext disabledSubsContext = null;
        return disabledSubsContext;
    }

    @Override
    public ContextualSubscriptionController getContextualSubscriptionController(final UserRequest ureq, final WindowControl wControl) {
        ContextualSubscriptionController csc = null;
        if (getSubscriptionContext() != null) {
            if ((caller.equals(CalendarController.CALLER_COURSE) || caller.equals(CalendarDao.TYPE_COURSE)) && course != null) {
                final String businessPath = wControl.getBusinessControl().getAsString();
                final PublisherData pdata = new PublisherData(CalendarDao.CALENDAR_MANAGER, String.valueOf(course.getResourceableId()), businessPath);
                csc = new ContextualSubscriptionController(ureq, wControl, getSubscriptionContext(), pdata);
            }
            if ((caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarDao.TYPE_GROUP)) && businessGroup != null) {
                final String businessPath = wControl.getBusinessControl().getAsString();
                final PublisherData pdata = new PublisherData(CalendarDao.CALENDAR_MANAGER, String.valueOf(businessGroup.getResourceableId()), businessPath);
                csc = new ContextualSubscriptionController(ureq, wControl, getSubscriptionContext(), pdata);
            }
        }
        return csc;
    }

    @Override
    public SubscriptionContext getSubscriptionContext() {
        return this.subscriptionContext;
    }

}
