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

import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

/**
 * Initial Date: 14.12.2011 <br>
 * 
 * @author lavinia
 */
public class DateUtilTest {

    @Test
    public void isMoreThanOneDayBefore() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        assertTrue(DateUtil.isMoreThanOneDayBefore(cal.getTime()));
    }

    @Test
    public void getEndDate_validDate() {
        String someDate = "10.05.14";
        String deFormattedDate = DateUtil.getEndDate(someDate, 30, Locale.GERMAN);
        System.out.println("deFormattedDate: " + deFormattedDate);
        assertTrue(deFormattedDate.equals("09.06.2014"));
    }

    @Test
    public void getEndDate_invalidDate() {
        String someDate = "noDate";
        String deFormattedDate = DateUtil.getEndDate(someDate, 30, Locale.GERMAN);
        System.out.println("deFormattedDate: " + deFormattedDate);

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
        String today = df.format(new Date());
        assertTrue(deFormattedDate.equals(today));
    }

    @Test
    public void getRemainingDays_future() {
        String someDate = "20.03.24";
        int days = DateUtil.getRemainingDays(someDate, 3);
        System.out.println("getRemainingDays: " + days);
        assertTrue(days > 0);

        int oneDay = DateUtil.getRemainingDays(someDate, 3) - DateUtil.getRemainingDays(someDate, 2);
        assertTrue(oneDay == 1);
    }

    @Test
    public void getRemainingDays_past() {
        String someDate = "20.03.04";
        int days = DateUtil.getRemainingDays(someDate, 3);
        System.out.println("getRemainingDays: " + days);
        assertTrue(days < 0);
    }

    @Test
    public void getRemainingDays_invalidDate() {
        String someDate = "noDate";
        int days = DateUtil.getRemainingDays(someDate, 3);
        System.out.println("getRemainingDays: " + days);
        assertTrue(days == 0);
    }

    @Test
    public void getRemainingDays_emptyArgs() {
        String someDate = "";
        int days = DateUtil.getRemainingDays(someDate, 0);
        System.out.println("getRemainingDays: " + days);
        assertTrue(days == 0);
    }

}
