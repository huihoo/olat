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

package org.olat.data.course.statistic.dayofweek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.statistic.IStatisticManager;
import org.olat.lms.course.statistic.StatisticResult;
import org.olat.system.commons.manager.BasicManager;

/**
 * Implementation of the IStatisticManager for 'dayofweek' statistic
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class DayOfWeekStatisticDaoImpl extends BasicManager implements IStatisticManager {

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey) {
        final DBQuery dbQuery = DBFactory.getInstance().createQuery(
                "select businessPath,day,value from org.olat.data.course.statistic.dayofweek.DayOfWeekStat sv " + "where sv.resId=:resId");
        dbQuery.setLong("resId", courseRepositoryEntryKey);

        final StatisticResult result = new StatisticResult(course, dbQuery.list());

        // now sort by user's preferred firstDayOfWeek
        final Calendar c = Calendar.getInstance(locale);
        final int firstDayOfWeek = c.getFirstDayOfWeek();

        final List<String> columnHeaders = new ArrayList<String>(7);
        for (int i = firstDayOfWeek; i < firstDayOfWeek + 7; i++) {
            int mod = i % 7;
            if (mod == 0) {
                // jdk calendar calculations don't start at 0 - they start at 1
                mod = 7;
            }
            columnHeaders.add(String.valueOf(mod));
        }

        result.setColumnHeaders(columnHeaders);
        return result;
    }

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey, final Date fromDate, final Date toDate) {
        return generateStatisticResult(locale, course, courseRepositoryEntryKey);
    }

    /**
	 */
    @Override
    public STATISTIC_TYPE getStatisticType() {
        return STATISTIC_TYPE.DAY_OF_WEEK;
    }

}
