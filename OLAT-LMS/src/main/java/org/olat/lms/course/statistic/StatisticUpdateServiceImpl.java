package org.olat.lms.course.statistic;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.course.statistic.StatisticUpdateConfig;
import org.olat.data.course.statistic.StatisticUpdaterDao;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.commons.taskexecutor.TaskExecutorService;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation for IStatisticUpdateManager
 * <P>
 * Initial Date: 11.02.2010 <br>
 * 
 * @author Stefan
 */
class StatisticUpdateServiceImpl extends BasicManager implements StatisticUpdateService, GenericEventListener {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    /** the category used for statistics properties (in the o_properties table) **/
    private static final String STATISTICS_PROPERTIES_CATEGORY = "STATISTICS_PROPERTIES";

    /** the name used for last_updated property (in the o_properties table) **/
    private static final String LAST_UPDATED_PROPERTY_NAME = "LAST_UPDATED";

    /** the event string used to ensure that only one StatisticUpdateManagerImpl is active in a cluster **/
    private static final String STARTUP_EVENT = "startupEvent";

    /** all the IStatisticUpdaters that registered with the StatisticUpdaterManager **/
    final List<StatisticUpdaterDao> updaters_ = new LinkedList<StatisticUpdaterDao>();

    private final MultiUserEvent startupEvent_ = new MultiUserEvent(STARTUP_EVENT);

    /** whether or not this manager is enabled - disables itself when there is more than 1 in the cluster **/
    private boolean enabled_ = true;

    boolean updateOngoing_ = false;

    @Autowired
    private TaskExecutorService taskExecutorService;

    /** spring **/
    private StatisticUpdateServiceImpl(final CoordinatorManager coordinatorManager, final StatisticUpdateConfig config, final String enabled) {
        enabled_ = enabled != null && "enabled".equals(enabled);
        if (!enabled_) {
            log.info("<init> disabled by configuration");
            return;
        }
        updaters_.addAll(config.getUpdaters());

        // note: not using CoordinatorManager.getInstance().getCoordinator() in this spring-called-constructor
        // as we have a problem in 6.3 where Tracing calls into CoordinatorManager.getInstance().getCoordinator()
        // which in turn causes the coord variable there to be initialized, which in turn
        // initializes the CoreSpringFactory, which in turn creates this bean *BEFORE*
        // the CoordinatorManager.coord was set ... hence we'd get a NullPointerException here
        coordinatorManager.getCoordinator().getEventBus()
                .registerFor(this, null, OresHelper.createOLATResourceableTypeWithoutCheck(StatisticUpdateServiceImpl.class.getName()));
        coordinatorManager.getCoordinator().getEventBus()
                .fireEventToListenersOf(startupEvent_, OresHelper.createOLATResourceableTypeWithoutCheck(StatisticUpdateServiceImpl.class.getName()));
    }

    @Override
    public void addStatisticUpdater(final StatisticUpdaterDao updater) {
        updaters_.add(updater);
    }

    @Override
    public synchronized boolean isEnabled() {
        return enabled_;
    }

    @Override
    public synchronized boolean updateOngoing() {
        return updateOngoing_;
    }

    @Override
    public boolean updateStatistics(final boolean fullRecalculation, final Runnable finishedCallback) {

        synchronized (this) {
            if (!enabled_) {
                log.warn("updateStatistics: cannot update statistics, manager is not enabled!", new Exception("updateStatistics"));
                return false;
            }
            if (updateOngoing_) {
                log.warn("updateStatistics: cannot update statistics since an update is currently ongoing");
                return false;
            }
            updateOngoing_ = true;
        }

        final Runnable r = new Runnable() {

            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                try {
                    log.info("updateStatistics: initialization for update");

                    final long nowInMilliseconds = System.currentTimeMillis();
                    long lastUpdatedInMilliseconds = getAndUpdateLastUpdated(nowInMilliseconds);
                    if (fullRecalculation || (lastUpdatedInMilliseconds == -1)) {
                        final Calendar nineteennintyeight = Calendar.getInstance();
                        nineteennintyeight.set(1998, 12, 31);
                        lastUpdatedInMilliseconds = nineteennintyeight.getTimeInMillis();
                    }

                    final Date lastUpdatedDate = new Date(lastUpdatedInMilliseconds);
                    final Date nowDate = new Date(nowInMilliseconds);

                    log.info("updateStatistics: starting the update");
                    DBFactory.getInstance().intermediateCommit();
                    for (final Iterator<StatisticUpdaterDao> it = updaters_.iterator(); it.hasNext();) {
                        final StatisticUpdaterDao statisticUpdater = it.next();
                        log.info("updateStatistics: starting updater " + statisticUpdater);
                        statisticUpdater.updateStatistic(fullRecalculation || (lastUpdatedInMilliseconds == -1), lastUpdatedDate, nowDate);
                        log.info("updateStatistics: done with updater " + statisticUpdater);
                        DBFactory.getInstance().intermediateCommit();
                    }
                } finally {
                    synchronized (StatisticUpdateServiceImpl.this) {
                        updateOngoing_ = false;
                    }
                    final long diff = System.currentTimeMillis() - start;
                    log.info("updateStatistics: total time for updating all statistics was " + diff + " milliseconds");

                    if (finishedCallback != null) {
                        finishedCallback.run();
                    }
                }
            }

        };
        try {
            taskExecutorService.runTask(r);
            log.info("updateStatistics: starting the update in its own thread");
            return true;
        } catch (final AssertException ae) {
            log.info("updateStatistics: Could not start update due to TaskExecutorManager not yet initialized. Will be done next time Cron/User calls!");
            synchronized (StatisticUpdateServiceImpl.this) {
                updateOngoing_ = false;
            }
            return false;
        }

    }

    @Override
    public long getLastUpdated() {
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(null, null, null, STATISTICS_PROPERTIES_CATEGORY, LAST_UPDATED_PROPERTY_NAME);
        if (p == null) {
            return -1;
        } else {
            return p.getLongValue();
        }
    }

    @Override
    public long getAndUpdateLastUpdated(final long lastUpdated) {
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(null, null, null, STATISTICS_PROPERTIES_CATEGORY, LAST_UPDATED_PROPERTY_NAME);
        if (p == null) {
            final PropertyImpl newp = pm.createPropertyInstance(null, null, null, STATISTICS_PROPERTIES_CATEGORY, LAST_UPDATED_PROPERTY_NAME, null, lastUpdated, null,
                    null);
            pm.saveProperty(newp);
            return -1;
        } else {
            final long result = p.getLongValue();
            p.setLongValue(lastUpdated);
            pm.saveProperty(p);
            return result;
        }
    }

    @Override
    public void event(final Event event) {
        // event from EventBus
        if (event != startupEvent_) {
            // that means some other StatisticUpdateManager is in the cluster
            // not good!
            log.error("event: CONFIG ERROR: there is more than one StatisticUpdateManager in this Cluster! I'll disable myself.");
            synchronized (this) {
                enabled_ = false;
            }
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
