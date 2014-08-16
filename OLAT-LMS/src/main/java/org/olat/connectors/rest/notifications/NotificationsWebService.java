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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.connectors.rest.notifications;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.SubscriptionInfo;
import org.olat.system.commons.StringHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description:</h3> REST API for notifications
 * <p>
 * Initial Date: 25 aug 2010 <br>
 * 
 * @author srosse, srosse@frentix.com, http://www.frentix.com
 */
@Path("notifications")
public class NotificationsWebService {

    /**
     * Retrieves the notification of the logged in user.
     * 
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The notifications
     * @response.representation.200.example {@link org.olat.connectors.rest.notifications.Examples#SAMPLE_INFOVOes}
     * @response.representation.404.doc The identity not found
     * @param date
     *            The date (optional)
     * @param httpRequest
     *            The HTTP request
     * @return an xml or json representation of a the user being search. The xml correspond to a <code>SubscriptionInfoVO</code>. <code>SubscriptionInfoVO</code>
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getNotifications(@QueryParam("date") final String date, @Context final HttpServletRequest httpRequest) {
        final Identity identity = RestSecurityHelper.getIdentity(httpRequest);
        final Locale locale = RestSecurityHelper.getLocale(httpRequest);

        Date compareDate;
        if (StringHelper.containsNonWhitespace(date)) {
            compareDate = parseDate(date, locale);
        } else {
            final NotificationService man = getNotificationService();
            compareDate = man.getCompareDateFromInterval(man.getUserIntervalOrDefault(identity));
        }

        final Map<Subscriber, SubscriptionInfo> subsInfoMap = NotificationHelper.getSubscriptionMap(identity, locale, true, compareDate);
        final List<SubscriptionInfoVO> voes = new ArrayList<SubscriptionInfoVO>();
        for (final Map.Entry<Subscriber, SubscriptionInfo> entry : subsInfoMap.entrySet()) {
            final SubscriptionInfo info = entry.getValue();
            if (info.hasNews()) {
                voes.add(new SubscriptionInfoVO(info));
            }
        }
        final SubscriptionInfoVO[] voesArr = new SubscriptionInfoVO[voes.size()];
        voes.toArray(voesArr);
        return Response.ok(voesArr).build();
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    private Date parseDate(final String date, final Locale locale) {
        if (StringHelper.containsNonWhitespace(date)) {
            if (date.indexOf('T') > 0) {
                if (date.indexOf('.') > 0) {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.S").parse(date);
                    } catch (final ParseException e) {
                        // fail silently
                    }
                } else {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(date);
                    } catch (final ParseException e) {
                        // fail silently
                    }
                }
            }

            // try with the locale
            if (date.length() > 10) {
                // probably date time
                try {
                    final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
                    format.setLenient(true);
                    return format.parse(date);
                } catch (final ParseException e) {
                    // fail silently
                }
            } else {
                try {
                    final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
                    format.setLenient(true);
                    return format.parse(date);
                } catch (final ParseException e) {
                    // fail silently
                }
            }
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }
}
