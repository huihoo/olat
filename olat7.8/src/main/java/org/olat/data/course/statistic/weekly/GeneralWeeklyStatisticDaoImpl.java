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

package org.olat.data.course.statistic.weekly;

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
import org.olat.system.logging.log4j.LoggerHelper;

import com.ibm.icu.util.StringTokenizer;

/**
 * Implementation of the IStatisticManager for 'weekly' statistic - specific for Mysql since it uses the mysql specific week(date,mode) function
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class GeneralWeeklyStatisticDaoImpl implements IStatisticManager {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public StatisticResult generateStatisticResult(final Locale locale, final ICourse course, final long courseRepositoryEntryKey) {
        final DBQuery dbQuery = DBFactory.getInstance().createQuery(
                "select businessPath,week,value from org.olat.data.course.statistic.weekly.WeeklyStat sv " + "where sv.resId=:resId");
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

        final SimpleDateFormat sdf = new SimpleDateFormat();
        final StringBuffer dateClause = new StringBuffer();
        // concat(year(creationdate),'-',week(creationdate)) week
        if (fromDate != null) {
            dateClause.append(" and (week=concat(:yearFromDate,'-',:weekFromDate) or week>=concat(:yearFromDate,'-',:weekFromDate)) ");
        }
        if (toDate != null) {
            dateClause.append(" and (week=concat(:yearToDate,'-',:weekToDate) or week<=concat(:yearToDate,'-',:weekToDate)) ");
        }
        final DBQuery dbQuery = DBFactory.getInstance().createQuery(
                "select businessPath,week,value from org.olat.data.course.statistic.weekly.WeeklyStat sv " + "where sv.resId=:resId " + dateClause);
        dbQuery.setLong("resId", courseRepositoryEntryKey);
        if (fromDate != null) {
            dbQuery.setString("yearFromDate", getYear(sdf, fromDate));
            dbQuery.setString("weekFromDate", getWeek(sdf, fromDate, MysqlWeekHelper.getMysqlWeekMode(Locale.getDefault())));
        }
        if (toDate != null) {
            dbQuery.setString("yearToDate", getYear(sdf, toDate));
            dbQuery.setString("weekToDate", getWeek(sdf, toDate, MysqlWeekHelper.getMysqlWeekMode(Locale.getDefault())));
        }
        // note: we use the default locale here since the WeeklyStatisticUpdater uses the default as well
        // there is no such concept as 'weekly stats table per user locale'
        // instead, the weekly stats table is generated with the system default locale
        // dbQuery.setInteger("weekMode", MysqlWeekHelper.getMysqlWeekMode(Locale.getDefault()));

        final StatisticResult statisticResult = new StatisticResult(course, dbQuery.list());
        fillGapsInColumnHeaders(statisticResult);
        return statisticResult;
    }

    private String getWeek(final SimpleDateFormat sdf, final Date date, final int mysqlWeekMode) {
        sdf.applyPattern("w");
        return sdf.format(date);
    }

    private String getYear(final SimpleDateFormat sdf, final Date date) {
        sdf.applyPattern("yyyy");
        return sdf.format(date);
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
            final String firstWeek = columnHeaders.get(0);
            String previousWeek = firstWeek;
            for (int i = 1; i < columnHeaders.size(); i++) {
                final String currWeek = columnHeaders.get(i);

                if (!isNextWeek(previousWeek, currWeek)) {
                    final String additionalWeek = nextWeek(previousWeek);
                    if (columnHeaders.contains(additionalWeek)) {
                        // oups, then we have a bug in our algorithm or what?
                        log.warn("fillGapsInColumnHeaders: throwing a ParseException, can't add " + additionalWeek + " to " + columnHeaders);
                        throw new ParseException("Can't add " + additionalWeek + " to the list of weeks - it is already there", 0);
                    }
                    columnHeaders.add(i, additionalWeek);
                    previousWeek = additionalWeek;
                } else {
                    previousWeek = currWeek;
                }
            }
            statisticResult.setColumnHeaders(columnHeaders);
        } catch (final ParseException e) {
            log.warn("fillGapsInColumnHeaders: Got a ParseException while trying to fill gaps. Giving up. ", e);
        }

    }

    private String nextWeek(final String week) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-w");
        Date d = sdf.parse(week);
        d = new Date(d.getTime() + 7 * 24 * 60 * 60 * 1000);
        String result = sdf.format(d);

        // bug with SimpleDateFormat:
        // Mon Dec 29 00:00:00 CET 2008
        // returns
        // 2008-01
        // which should probably rather be
        // 2009-01 or 2008-53

        if (result.compareTo(week) < 0) {
            // then we might have hit the bug mentioned above
            // calculate manually
            try {
                final StringTokenizer st = new StringTokenizer(week, "-");
                final Integer year = Integer.parseInt(st.nextToken());
                final Integer w = Integer.parseInt(st.nextToken());
                if (result.equals(year + "-1")) {
                    // then it looks like we need to switch to the next year already
                    return (year + 1) + "-" + 1;
                }
                if (w == 51) {
                    return year + "-" + 52;
                } else if (w == 52) {
                    return year + "-" + 53;
                } else if (w >= 53) {
                    return (year + 1) + "-" + 0;
                }
            } catch (final NumberFormatException nfe) {
                log.warn("nextWeek: Got a NumberFormatException: " + nfe, nfe);
                throw new ParseException("Got a NumberFormatException, rethrowing", 0);
            }
        } else if (result.equals(week)) {
            // daylight saving
            d = new Date(d.getTime() + 1 * 60 * 60 * 1000);
            result = sdf.format(d);
        }

        return result;
    }

    private boolean isNextWeek(final String week, final String nextWeek) throws ParseException {
        return nextWeek(week).equals(nextWeek);
    }

    /**
	 */
    @Override
    public STATISTIC_TYPE getStatisticType() {
        return STATISTIC_TYPE.GENERAL_WEEKLY;
    }

}
