package org.olat.data.course.statistic;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MySQL specific class which drops the temporary table with the result of 'o_loggingtable where actionverb=launch and actionobject=node'.
 * <P>
 * Initial Date: 16.02.2010 <br>
 * 
 * @author Stefan
 */
public class MySQLTempStatTableDropper implements StatisticUpdaterDao {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * the jdbcTemplate is used to allow access to other than the default database and allow raw sql code
     */
    private JdbcTemplate jdbcTemplate_;

    /** set via spring **/
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate_ = jdbcTemplate;
    }

    @Override
    public void updateStatistic(final boolean fullRecalculation, final Date from, final Date until) {
        // note: fullRecalculation has no affect to the dropper

        // create temp table
        final long startTime = System.currentTimeMillis();
        try {
            jdbcTemplate_.execute("drop table o_stat_temptable;");

        } catch (final RuntimeException e) {
            e.printStackTrace(System.out);
        } catch (final Error er) {
            er.printStackTrace(System.out);
        } finally {
            final long diff = System.currentTimeMillis() - startTime;
            log.info("updateStatistic: END. duration=" + diff);
        }
    }

}
