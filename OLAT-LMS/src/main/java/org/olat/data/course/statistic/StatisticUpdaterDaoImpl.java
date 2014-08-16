package org.olat.data.course.statistic;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Default implementation for IStatisticUpdater. Could be renamed into StatisticUpdaterDaoImpl.
 * <p>
 * This implementation takes the following properties (via spring):
 * <ul>
 * <li>jdbcTemplate: the JdbcTemplate to be used to access the o_loggingtable and to create the o_stat_* tables - note that this might be different from the default
 * database configured in OLAT when using a master-slave setup for example</li>
 * <li>updateSQL: a list of (raw) sql statements which update the o_stat_* table (only the one to which this updater belongs!). The idea of these sql statements is to
 * support incremental updates, i.e. to only update the difference, not having to delete the whole o_stat_* table away on each update (in order to improve speed)</li>
 * <li>deleteSQL: a list of (raw) sql statements which delete the o_stat_* table (only the one to which this updater belongs!)</li>
 * </ul>
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public class StatisticUpdaterDaoImpl implements StatisticUpdaterDao {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * the jdbcTemplate is used to allow access to other than the default database and allow raw sql code
     */
    private JdbcTemplate jdbcTemplate_;

    /** holds the update SQL statements - set via spring **/
    private String[] updateSQL_;

    /** holds the delete SQL statements - set via spring **/
    private String[] deleteSQL_;

    /** name used to identify this StatisticUpdater for logging purpose **/
    private String loggingName_;

    /**
	 * 
	 */
    private StatisticUpdaterDaoImpl() {
        //
    }

    /** set via spring **/
    public void setLoggingName(final String loggingName) {
        loggingName_ = loggingName;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + loggingName_ + "]";
    }

    /** set via spring **/
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate_ = jdbcTemplate;
    }

    /** set via spring **/
    public void setUpdateSQL(final String[] updateSQL) {
        updateSQL_ = updateSQL;
    }

    /** set via spring **/
    public void setDeleteSQL(final String[] deleteSQL) {
        deleteSQL_ = deleteSQL;
    }

    /**
     * get the query which updates the whole table
     * 
     * @return
     */
    protected String[] getUpdateQueries() {
        return updateSQL_;
    }

    /**
     * get the query which deletes the whole table
     * 
     * @return
     */
    protected String[] getDeleteQueries() {
        return deleteSQL_;
    }

    @Override
    public final void updateStatistic(final boolean fullRecalculation, final Date from, final Date until) {
        log.info("updateStatistic<" + loggingName_ + ">: START");
        final long startTime = System.currentTimeMillis();
        try {
            if (fullRecalculation) {
                final String[] deleteQueries = getDeleteQueries();
                if (deleteQueries != null) {
                    for (int i = 0; i < deleteQueries.length; i++) {
                        final String aDeleteQuery = deleteQueries[i];
                        if (aDeleteQuery != null && aDeleteQuery.length() > 0) {
                            jdbcTemplate_.execute(aDeleteQuery);
                        }
                    }
                }
            }

            final String[] updateQueries = getUpdateQueries();
            if (updateQueries != null) {
                for (int i = 0; i < updateQueries.length; i++) {
                    final String anUpdateQuery = updateQueries[i];
                    if (anUpdateQuery != null && anUpdateQuery.length() > 0) {
                        jdbcTemplate_.execute(anUpdateQuery);
                    }
                }
            }

        } catch (final RuntimeException e) {
            log.error("updateStatistic<" + loggingName_ + ">: RuntimeException while updating the statistics: " + e, e);
        } finally {
            final long diff = System.currentTimeMillis() - startTime;
            log.info("updateStatistic<" + loggingName_ + ">: END. duration=" + diff + " milliseconds");
        }
    }
}
