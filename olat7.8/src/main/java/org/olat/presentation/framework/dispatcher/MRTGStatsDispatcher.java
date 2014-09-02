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

package org.olat.presentation.framework.dispatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.coordinate.jms.ClusterEventBus;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.lms.course.CourseModule;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.monitoring.SimpleProbeObject;
import org.olat.lms.monitoring.SimpleProbeService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.commons.session.SessionInfo;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.jms.SimpleProbe;
import org.olat.system.logging.Tracing;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.Probe;
import org.olat.testutils.codepoints.client.StatId;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Wraps the request to get MRTGStatistics into a dispatcher.
 * <P>
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 */
public class MRTGStatsDispatcher implements Dispatcher {

    private static final Logger log = LoggerHelper.getLogger();

    // default allows monitoring only from localhost
    // "*" means allow from any host (not recommended in real world setups)
    private String monitoringHost = "127.0.0.1";
    private long lastErrorCount = 0;
    private String instanceId;

    private CodepointClient codepointClient_;
    private Probe dispatchProbe_;
    private Probe doInSyncEnterProbe_;
    private Probe doInSyncInsideProbe_;
    private Probe dbQueryListProbe_;

    private final CoordinatorManager coordinatorManager;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    SimpleProbeService simpleProbeServiceImpl;

    /**
     * [spring only]
     */
    private MRTGStatsDispatcher(final CoordinatorManager coordinatorManager) {
        this.coordinatorManager = coordinatorManager;
        try {
            codepointClient_ = Codepoint.getLocalLoopCodepointClient();
            dispatchProbe_ = codepointClient_.startProbingBetween("org.olat.presentation.framework.dispatcher.DispatcherAction.execute-start",
                    "org.olat.presentation.framework.dispatcher.DispatcherAction.execute-end");
            doInSyncEnterProbe_ = codepointClient_.startProbingBetween(
                    "org.olat.system.coordinate.ClusterSyncer.doInSync-before-sync.org.olat.system.coordinate.ClusterSyncer.doInSync",
                    "org.olat.system.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.system.coordinate.ClusterSyncer.doInSync");
            doInSyncInsideProbe_ = codepointClient_.startProbingBetween(
                    "org.olat.system.coordinate.ClusterSyncer.doInSync-in-sync.org.olat.system.coordinate.ClusterSyncer.doInSync",
                    "org.olat.system.coordinate.ClusterSyncer.doInSync-after-sync.org.olat.system.coordinate.ClusterSyncer.doInSync");
            dbQueryListProbe_ = codepointClient_.startProbingBetween("org.olat.data.DBQueryImpl.list-entry", "org.olat.data.DBQueryImpl.list-exit");
            dispatchProbe_.logifSlowerThan(8000, Level.WARNING);
            doInSyncEnterProbe_.logifSlowerThan(200, Level.WARNING);
            doInSyncInsideProbe_.logifSlowerThan(1000, Level.WARNING);
            dbQueryListProbe_.logifSlowerThan(1300, Level.WARNING);
        } catch (final RuntimeException re) {
            log.info("Certain MRTG Statistics will not be available since Codepoints are disabled");
        } catch (final CommunicationException e) {
            log.info("Certain MRTG Statistics will not be available since Codepoints are disabled");
        }
    }

    /**
     * javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public void execute(final HttpServletRequest request, final HttpServletResponse response, final String uriPrefix) {
        if (log.isDebugEnabled()) {
            log.debug("serving MRTGStats on uriPrefix [[[" + uriPrefix + "]]]");
        }
        returnMRTGStats(request, response);
    }

    private String roundedStatValueOf(final Probe probe, final StatId statId) {
        try {
            final int value = Math.round(probe.getStatValue(statId) * 100) / 100;
            if (value >= 0) {
                return String.valueOf(value);
            } else {
                return "0";
            }
        } catch (final CommunicationException e) {
            e.printStackTrace(System.out);
            return "0";
        }
    }

    private String roundedValueOf(final long value) {
        if (value >= 0) {
            return String.valueOf(value);
        } else {
            return "0";
        }
    }

    /**
     * @param request
     * @param response
     */
    private void returnMRTGStats(final HttpServletRequest request, final HttpServletResponse response) {
        if (!request.getRemoteAddr().equals(monitoringHost) && !monitoringHost.equals("*")) {
            // limit to allowed hosts
            log.info("Audit:Trying to access stats from other host than configured (" + monitoringHost + ") : " + request.getRemoteAddr());
            DispatcherAction.sendForbidden(request.getPathInfo(), response);
        }
        String command = request.getParameter("cmd");
        if (command == null) {
            command = "users";
        }
        final StringBuilder result = new StringBuilder();
        int httpsCount = 0;
        int activeSessionCnt = 0;
        if (command.equals("users")) { // get user stats of (authenticated)
                                       // usersessions
            final Set userSessions = UserSession.getAuthenticatedUserSessions();
            for (final Iterator it_usess = userSessions.iterator(); it_usess.hasNext();) {
                final UserSession usess = (UserSession) it_usess.next();
                activeSessionCnt++;
                final SessionInfo sessInfo = usess.getSessionInfo();
                if (sessInfo.isSecure()) {
                    httpsCount++;
                }
            }
            result.append(activeSessionCnt); // active authenticated sessions
            result.append("\n");
            result.append(httpsCount); // ,,, of which are secure
            result.append("\n0\n");
            result.append(instanceId);
        } else if (command.equals("webdav")) { // get webdav stats of
                                               // (authenticated) usersessions
            final Set userSessions = UserSession.getAuthenticatedUserSessions();
            int webdavcount = 0;
            int securewebdavcount = 0;
            for (final Iterator it_usess = userSessions.iterator(); it_usess.hasNext();) {
                final UserSession usess = (UserSession) it_usess.next();
                final SessionInfo sessInfo = usess.getSessionInfo();
                if (sessInfo.isWebDAV()) {
                    webdavcount++;
                    if (sessInfo.isSecure()) {
                        securewebdavcount++;
                    }
                }
            }
            result.append(webdavcount); // webdav sessions
            result.append("\n");
            result.append(securewebdavcount); // ,,, of which are secure
            result.append("\n0\n");
            result.append(instanceId);
        } else if (command.equals("imstats")) { // get Jabber info
            if (InstantMessagingModule.isEnabled()) {
                result.append(InstantMessagingModule.getAdapter().countConnectedUsers());
                result.append("\n");
                // result.append(InstantMessagingModule.getAdapter().countUsersRunningFlashClient());
                result.append(0);
                result.append("\n0\n");
                result.append(instanceId);
            } else {
                result.append("0\n0\n0\n");
                result.append(instanceId);
            }
        } else if (command.equals("debug")) { // get debug stats
            // IMPORTANT: do not call too often, since .size() of a weakhashmap
            // may be an expensive operation.
            // our mrtg default is: once every five minutes.
            final int controllerCnt = DefaultController.getControllerCount();
            result.append(controllerCnt); // active and not yet disposed
            result.append("\n0\n0\n");
            result.append(instanceId);

        } else if (command.equals("mem")) { // get VM memory stats
            final Runtime r = Runtime.getRuntime();
            final long totalMem = r.totalMemory();
            // Total used memory in megabyptes
            result.append((totalMem - r.freeMemory()) / 1000000).append("\n");
            // Max available memory in VM in megabytes
            result.append(r.maxMemory() / 1000000).append("\n");
            result.append("0\n");
            result.append(instanceId);

        } else if (command.equals("proc")) { // get VM process stats
            // Number of concurrent dispatching OLAT threads (concurrent user
            // requests)
            result.append(DispatcherAction.getConcurrentCounter()).append("\n");
            // Number of active threads
            final ThreadGroup group = Thread.currentThread().getThreadGroup();
            final Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads, false);
            int counter = 0;
            for (final Thread t : threads) {
                if (t == null) {
                    continue;
                }
                // http-8080-Processor and TP-Processor
                // not precise, but good enouth
                if (t.getName().indexOf("-Processor") != -1) {
                    counter++;
                }
            }
            result.append(counter).append("\n");
            result.append("0\n");
            result.append(instanceId);

        } else if (command.equals("err")) { // get error stats
            // Average number of errors per minute since last call
            final long currentErrorCount = Tracing.getTotalErrorCount();
            final long errorDifference = currentErrorCount - lastErrorCount;

            lastErrorCount = currentErrorCount;
            result.append(errorDifference).append("\n");
            result.append("0\n0\n");
            result.append(instanceId);

        } else if (command.startsWith("dispatch")) {

            if (dispatchProbe_ == null) {
                result.append("0\n0\n0\n");
            } else {
                if (command.equals("dispatchAvg")) {
                    result.append(roundedStatValueOf(dispatchProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
                } else if (command.equals("dispatchMax")) {
                    result.append(roundedStatValueOf(dispatchProbe_, StatId.MAX_TIME_ELAPSED));
                } else if (command.equals("dispatchCnt")) {
                    result.append(roundedStatValueOf(dispatchProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
                } else if (command.equals("dispatchReset")) {
                    try {
                        dispatchProbe_.clearStats();
                    } catch (final CommunicationException e) {
                        e.printStackTrace(System.out);
                        // ignore otherwise
                    }
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);

        } else if (command.startsWith("dbQueryList")) {

            if (dbQueryListProbe_ == null) {
                result.append("0\n0\n0\n");
            } else {
                if (command.equals("dbQueryListAvg")) {
                    result.append(roundedStatValueOf(dbQueryListProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
                } else if (command.equals("dbQueryListMax")) {
                    result.append(roundedStatValueOf(dbQueryListProbe_, StatId.MAX_TIME_ELAPSED));
                } else if (command.equals("dbQueryListCnt")) {
                    result.append(roundedStatValueOf(dbQueryListProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
                } else if (command.equals("dbQueryListReset")) {
                    try {
                        dbQueryListProbe_.clearStats();
                    } catch (final CommunicationException e) {
                        e.printStackTrace(System.out);
                        // ignore otherwise
                    }
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);

        } else if (command.startsWith("doInSyncEnter")) {

            if (doInSyncEnterProbe_ == null) {
                result.append("0\n0\n0\n");
            } else {
                if (command.equals("doInSyncEnterAvg")) {
                    result.append(roundedStatValueOf(doInSyncEnterProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
                } else if (command.equals("doInSyncEnterMax")) {
                    result.append(roundedStatValueOf(doInSyncEnterProbe_, StatId.MAX_TIME_ELAPSED));
                } else if (command.equals("doInSyncEnterCnt")) {
                    result.append(roundedStatValueOf(doInSyncEnterProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
                } else if (command.equals("doInSyncEnterReset")) {
                    try {
                        doInSyncEnterProbe_.clearStats();
                    } catch (final CommunicationException e) {
                        e.printStackTrace(System.out);
                        // ignore otherwise
                    }
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);

        } else if (command.startsWith("doInSyncInside")) {

            if (doInSyncInsideProbe_ == null) {
                result.append("0\n0\n0\n");
            } else {
                if (command.equals("doInSyncInsideAvg")) {
                    result.append(roundedStatValueOf(doInSyncInsideProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
                } else if (command.equals("doInSyncInsideMax")) {
                    result.append(roundedStatValueOf(doInSyncInsideProbe_, StatId.MAX_TIME_ELAPSED));
                } else if (command.equals("doInSyncInsideCnt")) {
                    result.append(roundedStatValueOf(doInSyncInsideProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
                } else if (command.equals("doInSyncInsideReset")) {
                    try {
                        doInSyncInsideProbe_.clearStats();
                    } catch (final CommunicationException e) {
                        e.printStackTrace(System.out);
                        // ignore otherwise
                    }
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.startsWith("jmsDelivery")) {

            final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
            if (clusterEventBus == null) {
                result.append("0\n0\n0\n");
            } else {
                final SimpleProbe probe = clusterEventBus.getMrtgProbeJMSDeliveryTime();
                if (command.equals("jmsDeliveryAvg")) {
                    result.append(roundedValueOf(probe.getAvg()));
                } else if (command.equals("jmsDeliveryMax")) {
                    result.append(roundedValueOf(probe.getMax()));
                } else if (command.equals("jmsDeliveryCnt")) {
                    result.append(roundedValueOf(probe.getNum()));
                } else if (command.equals("jmsDeliveryReset")) {
                    probe.reset();
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.startsWith("jmsProcessing")) {

            final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
            if (clusterEventBus == null) {
                result.append("0\n0\n0\n");
            } else {
                final SimpleProbe probe = clusterEventBus.getMrtgProbeJMSProcessingTime();
                if (command.equals("jmsProcessingAvg")) {
                    result.append(roundedValueOf(probe.getAvg()));
                } else if (command.equals("jmsProcessingMax")) {
                    result.append(roundedValueOf(probe.getMax()));
                } else if (command.equals("jmsProcessingCnt")) {
                    result.append(roundedValueOf(probe.getNum()));
                } else if (command.equals("jmsProcessingReset")) {
                    probe.reset();
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.startsWith("jmsWaiting")) {

            final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
            if (clusterEventBus == null) {
                result.append("0\n0\n0\n");
            } else {
                final SimpleProbe probe = clusterEventBus.getMrtgProbeJMSLoad();
                if (command.equals("jmsWaitingAvg")) {
                    result.append(roundedValueOf(probe.getAvg()));
                } else if (command.equals("jmsWaitingMax")) {
                    result.append(roundedValueOf(probe.getMax()));
                } else if (command.equals("jmsWaitingCnt")) {
                    result.append(roundedValueOf(probe.getNum()));
                } else if (command.equals("jmsWaitingReset")) {
                    probe.reset();
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.startsWith("jmsQueued")) {

            final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
            if (clusterEventBus == null) {
                result.append("0\n0\n0\n");
            } else {
                final SimpleProbe probe = clusterEventBus.getMrtgProbeJMSEnqueueTime();
                if (command.equals("jmsQueuedAvg")) {
                    result.append(roundedValueOf(probe.getAvg()));
                } else if (command.equals("jmsQueuedMax")) {
                    result.append(roundedValueOf(probe.getMax()));
                } else if (command.equals("jmsQueuedCnt")) {
                    result.append(roundedValueOf(probe.getNum()));
                } else if (command.equals("jmsQueuedReset")) {
                    probe.reset();
                    result.append("0");
                }
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("SecurityGroupMembershipImpl")) { // SecurityGroupMembershipImpl
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe("org.olat.data.basesecurity.SecurityGroupMembershipImpl");
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("BGAreaImpl")) { // BGAreaImpl
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe("org.olat.data.group.area.BGAreaImpl");
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("BusinessGroupImpl")) { // BusinessGroupImpl
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe("org.olat.data.group.BusinessGroupImpl");
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("OLATResourceImpl")) { // OLATResourceImpl
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe(OLATResourceImpl.class.getName());
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("TheRest")) { // PolicyImpl
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe("THEREST");
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("PolicyImpl")) { // PolicyImpl
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe("THEREST");
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);

            for (SimpleProbeObject simpleProbeTO : simpleProbeServiceImpl.getSimpleProbeNonRegisteredList()) {
                log.info("MRTGStats: table '" + simpleProbeTO.getKey() + "' uses up " + simpleProbeTO.getSum() + "ms of a total of " + simpleProbeTO.getTotalSum()
                        + "ms, which is " + Math.round(1000.0 * simpleProbeTO.getSum() / simpleProbeTO.getTotalSum()) / 10 + "%");
            }

        } else if (command.equals("LifeCycleEntry")) { // LifeCycleEntry
            final SimpleProbeObject probe = simpleProbeServiceImpl.getSimpleProbe("org.olat.data.lifecycle.LifeCycleEntry");
            if (probe == null) {
                result.append("0\n0\n0\n");
            } else {
                result.append(roundedValueOf(probe.getSum()));
                result.append("\n0\n0\n");
            }
            result.append(instanceId);
        } else if (command.equals("usercount")) { // get number of useraccounts
                                                  // counter
            final SecurityGroup olatuserGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
            final int users = baseSecurity.countIdentitiesOfSecurityGroup(olatuserGroup);
            final int disabled = baseSecurity.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_LOGIN_DENIED).size();
            result.append(users - disabled).append("\n"); // number of active
                                                          // users
            result.append(disabled).append("\n"); // number of disabled users
            result.append("0\n");
            result.append(instanceId);

        } else if (command.equals("usercountmonthly")) { // get number of
                                                         // different users
                                                         // logged in during
                                                         // last month and
                                                         // last half year
            final Calendar lastLoginLimit = Calendar.getInstance();
            lastLoginLimit.add(Calendar.MONTH, -1);
            result.append(baseSecurity.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
            lastLoginLimit.add(Calendar.MONTH, -5); // -1 -5 = -6 for half a
                                                    // year
            result.append(baseSecurity.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
            result.append("0\n");
            result.append(instanceId);

        } else if (command.equals("usercountdaily")) { // get number of
                                                       // different users
                                                       // logged in during last
                                                       // day and last week
            final Calendar lastLoginLimit = Calendar.getInstance();
            lastLoginLimit.add(Calendar.DAY_OF_YEAR, -1);
            result.append(baseSecurity.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
            lastLoginLimit.add(Calendar.DAY_OF_YEAR, -6); // -1 - 6 = -7 for
                                                          // last week
            result.append(baseSecurity.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
            result.append("0\n");
            result.append(instanceId);

        } else if (command.equals("usercountsince")) { // get number of
                                                       // different users
                                                       // logged in since a
                                                       // period which is
                                                       // specified by
                                                       // parameter date
            final String dateParam = request.getParameter("date");
            if (dateParam == null) {
                result.append("date parameter missing. add date=yyyy-MM-dd\n");
            } else {
                final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    final Date mydate = df.parse(dateParam);
                    result.append(baseSecurity.countUniqueUserLoginsSince(mydate)).append("\n");
                } catch (final ParseException e) {
                    result.append("date parameter format error. expected: yyyy-MM-dd\n");
                }

                result.append("0\n0\n");
                result.append(instanceId);
            }

        } else if (command.equals("coursecount")) { // get number of activated
                                                    // courses
            final RepositoryService repoMgr = RepositoryServiceImpl.getInstance();
            final int allCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_OWNERS);
            final int publishedCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_USERS);
            result.append(allCourses).append("\n"); // number of all courses
            result.append(publishedCourses).append("\n"); // number of published
                                                          // courses
            result.append("0\n");
            result.append(instanceId);
        }

        ServletUtil.serveStringResource(request, response, result.toString());
    }

    /**
     * Setter for Spring configuration
     * 
     * @param monitoringHost
     */
    public void setMonitoringHost(final String monitoringHost) {
        this.monitoringHost = monitoringHost;
    }

    /**
     * Spring getter
     * 
     * @return
     */
    public String getMonitoringHost() {
        return monitoringHost;
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

}
