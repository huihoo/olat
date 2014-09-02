package org.olat.lms.course.statistic;

import java.util.Date;
import java.util.Locale;

import org.olat.lms.course.ICourse;

/**
 * An IStatisticManager is used by the StatisticDisplayController in order to generate the statistics table plus the column headers.
 * <p>
 * Usually an IStatisticManager comes in a bundle with a Hibernate xml file, a Hibernate object class plus an implementation for IStatisticUpdater - the latter taking
 * care of generating the statistics table in the first place.
 * <P>
 * Initial Date: 12.02.2010 <br>
 * 
 * @author Stefan
 */
public interface IStatisticManager {

    public enum STATISTIC_TYPE {
        DAILY, DAY_OF_WEEK, HOME_ORG, HOUR_OF_DAY, GENERAL_WEEKLY, WEEKLY, ORG_TYPE, STUDY_BRANCH, STUDY_LEVEL
    }

    /**
     * Generates the statistic table for the given Course (and matching repo entry key)
     * <p>
     * 
     * @param course
     *            the course for which to generate the StatisticResult
     * @param courseRepositoryEntryKey
     *            the key of the RepositoryEntry matching the course passed to this method
     * @return the StatisticResult - which carries the table subsequently shown by the StatisticDisplayController
     */
    StatisticResult generateStatisticResult(Locale locale, ICourse course, long courseRepositoryEntryKey);

    /**
     * Generates the statistic table for the given Course (and matching repo entry key)
     * 
     * @param ureq
     * @param course
     * @param courseRepositoryEntryKey
     * @param fromDate
     * @param toDate
     * @return
     */
    StatisticResult generateStatisticResult(Locale locale, ICourse course, long courseRepositoryEntryKey, Date fromDate, Date toDate);

    public STATISTIC_TYPE getStatisticType();

}
