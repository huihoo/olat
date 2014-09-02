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

package org.olat.data.course.statistic.hourofday;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.statistic.IStatisticManager;
import org.olat.lms.course.statistic.StatisticResult;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Implementation of the IStatisticManager for 'hourofday' statistic
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class HourOfDayStatisticDaoImpl extends BasicManager implements IStatisticManager {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey) {
        final DBQuery dbQuery = DBFactory.getInstance().createQuery(
                "select businessPath,hour,value from org.olat.data.course.statistic.hourofday.HourOfDayStat sv " + "where sv.resId=:resId");
        dbQuery.setLong("resId", courseRepositoryEntryKey);

        final StatisticResult statisticResult = new StatisticResult(course, dbQuery.list());
        final List<String> columnHeaders = statisticResult.getColumnHeaders();
        if (columnHeaders != null && columnHeaders.size() > 1) {
            try {
                final int start = Integer.parseInt(columnHeaders.get(0));
                final int end = Integer.parseInt(columnHeaders.get(columnHeaders.size() - 1));
                final List<String> resultingColumnHeaders = new ArrayList<String>((end - start) + 1);
                for (int hour = start; hour <= end; hour++) {
                    resultingColumnHeaders.add(String.valueOf(hour));
                }
                statisticResult.setColumnHeaders(resultingColumnHeaders);
            } catch (final NumberFormatException nfe) {
                log.warn("generateStatisticResult: Got a NumberFormatException: " + nfe, nfe);
            }
        }
        return statisticResult;
    }

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey, final Date fromDate, final Date toDate) {
        return generateStatisticResult(locale, course, courseRepositoryEntryKey);
    }

    /**
	 */
    @Override
    public STATISTIC_TYPE getStatisticType() {
        return STATISTIC_TYPE.HOUR_OF_DAY;
    }

}
