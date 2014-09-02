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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * TODO: needs new method, isAlmostOneDayBefore(Date date, int toleranceInHours)
 * 
 * Initial Date: 14.12.2011 <br>
 * 
 * @author lavinia
 */
public class DateUtil {
    private static final Logger LOG = LoggerHelper.getLogger();

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

    /**
     * Calculates the endDate, which is noDays days after startDate. <br>
     * If the date parse fails, returns the current date. <br>
     * 
     * @return the formatted date as string.
     */
    public static String getEndDate(String startDateString, int noDays, Locale locale) {
        Date endDate = getEndDate(startDateString, noDays);
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        return df.format(endDate);
    }

    /**
     * Calculates the endDate, which is noDays days after startDate. <br>
     * If the date parse fails, returns the current date.
     */
    private static Date getEndDate(String startDateString, int noDays) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
        try {
            Date date = dateFormat.parse(startDateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, noDays);
            return cal.getTime();
        } catch (ParseException e) {
            LOG.error("getEndDate - cannot parse date: " + startDateString, e);
        }
        return new Date();
    }

    /**
     * Calculates the remaining days from NOW to the endDate (which is noDays days after startDate). <br>
     * If the date parse fails, returns 0. <br>
     */
    public static int getRemainingDays(String startDateString, int noDays) {
        Date endDate = getEndDate(startDateString, noDays);
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        long remainingMills = cal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        long days = remainingMills / (24 * 60 * 60 * 1000);
        return Math.round(days);
    }

}
