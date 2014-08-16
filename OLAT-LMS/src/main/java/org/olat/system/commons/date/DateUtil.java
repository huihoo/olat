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
package org.olat.system.commons.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * TODO: needs new method, isAlmostOneDayBefore(Date date, int toleranceInHours)
 * 
 * Initial Date: 14.12.2011 <br>
 * 
 * @author lavinia
 */
public class DateUtil {

    private static final String emailBodyDayPattern = "dd.MM.yyyy";
    private static final String emailBodyTimePattern = "HH:mm";

    /**
     * date is older than yesterday same time.
     */
    public static boolean isMoreThanOneDayBefore(Date date) {
        return date.before(getYesterday());
    }

    public static Date getYesterday() {
        return getDayBefore(new Date());
    }

    /**
     * Gets Date for yesterday.
     */
    public static Date getDayBefore(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    /**
     * Gets Date for <code> numberOfDays </code> ago.
     */
    public static Date getDayBefore(Date date, int numberOfDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -numberOfDays);
        return cal.getTime();
    }

    public static String extractDate(Date date, Locale locale) {
        SimpleDateFormat format = new SimpleDateFormat(emailBodyDayPattern, locale);
        return format.format(date);
    }

    public static String extractTime(Date date, Locale locale) {
        SimpleDateFormat format = new SimpleDateFormat(emailBodyTimePattern, locale);
        return format.format(date);
    }

    /**
     * Creates a DateFilter(fromDate,toDate) with toDate: right now, and fromDate: <code>days</code> before.
     */
    public static DateFilter getDateFilterFromDDaysBeforeToToday(int days) {
        return new DateFilter(getDayBefore(new Date(), days), new Date());
    }
}
