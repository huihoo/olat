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

package org.olat.data.course.statistic.daily;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * Implementation of the IStatisticManager for 'weekly' statistic - specific for Mysql since it uses the mysql specific week(date,mode) function
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class DailyStatisticDaoImpl extends BasicManager implements IStatisticManager {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * the SimpleDateFormat with which the column headers will be created formatted by the database, so change this in coordination with any db changes if you really need
     * to
     **/
    private final SimpleDateFormat columnHeaderFormat_ = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey) {
        final DBQuery dbQuery = DBFactory.getInstance().createQuery(
                "select businessPath,day,value from org.olat.data.course.statistic.daily.DailyStat sv " + "where sv.resId=:resId");
        dbQuery.setLong("resId", courseRepositoryEntryKey);

        return new StatisticResult(course, dbQuery.list());
    }

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey, final Date fromDate, final Date toDate) {
        if (fromDate == null && toDate == null) {
            // no restrictions, return the defaults
            final StatisticResult statisticResult = generateStatisticResult(locale, course, courseRepositoryEntryKey);
            fillGapsInColumnHeaders(statisticResult);
            return statisticResult;
        }

        final StringBuffer dateClause = new StringBuffer();
        if (fromDate != null) {
            dateClause.append(" and (day>=:fromDate) ");
        }
        if (toDate != null) {
            dateClause.append(" and (day<=:toDate) ");
        }
        final DBQuery dbQuery = DBFactory.getInstance().createQuery(
                "select businessPath,day,value from org.olat.data.course.statistic.daily.DailyStat sv " + "where sv.resId=:resId " + dateClause);
        dbQuery.setLong("resId", courseRepositoryEntryKey);
        if (fromDate != null) {
            dbQuery.setDate("fromDate", fromDate);
        }
        if (toDate != null) {
            dbQuery.setDate("toDate", toDate);
        }

        final StatisticResult statisticResult = new StatisticResult(course, dbQuery.list());
        fillGapsInColumnHeaders(statisticResult);
        return statisticResult;
    }

    /** fill any gaps in the column headers between the first and the last days **/
    private void fillGapsInColumnHeaders(final StatisticResult statisticResult) {
        if (statisticResult == null) {
            throw new IllegalArgumentException("statisticResult must not be null");
        }
        final List<String> columnHeaders = statisticResult.getColumnHeaders();
        if (columnHeaders.size() <= 1) {
            // if the resulting set one or less, don't bother
            return;
        }
        try {
            final String firstDate = columnHeaders.get(0);
            final Date fromDate = columnHeaderFormat_.parse(firstDate);
            Date previousDate = new Date(fromDate.getTime()); // copy fromDate
            final long DAY_DIFF = 24 * 60 * 60 * 1000;
            for (int i = 1; i < columnHeaders.size(); i++) {
                final String aDate = columnHeaders.get(i);
                final Date currDate = columnHeaderFormat_.parse(aDate);
                final long diff = currDate.getTime() - previousDate.getTime();
                // note that we should have full days - we have the HH:MM:SS set to 00:00:00 - hence the
                // difference should always be a full day
                if (diff > DAY_DIFF) {
                    // then we should add a few days in here
                    final Date additionalDate = new Date(previousDate.getTime() + DAY_DIFF);
                    final String additionalDateStr = columnHeaderFormat_.format(additionalDate);
                    columnHeaders.add(i, additionalDateStr);
                    previousDate = additionalDate;
                } else {
                    previousDate = currDate;
                }
            }

            statisticResult.setColumnHeaders(columnHeaders);
        } catch (final ParseException e) {
            log.warn("fillGapsInColumnHeaders: Got a ParseException while trying to fill gaps. Giving up. ", e);
        }
    }

    /**
	 */
    @Override
    public STATISTIC_TYPE getStatisticType() {
        return STATISTIC_TYPE.DAILY;
    }

}
