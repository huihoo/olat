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
package org.olat.presentation.admin.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.coordinate.ClusterCoordinator;
import org.olat.data.coordinate.jms.ClusterEventBus;
import org.olat.data.coordinate.jms.NodeInfo;
import org.olat.data.coordinate.lock.ClusterLockDao;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.OncePanel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowBackOffice;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.user.administration.UserSearchController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.coordinate.jms.PerfItem;
import org.olat.system.coordinate.jms.PerformanceMonitorHelper;
import org.olat.system.event.Event;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * provides a control panel for the olat system administrator. displays the status of all running olat cluster nodes and also displays the latest sent messages.
 * <P>
 * Initial Date: 29.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterAdminControllerCluster extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private static final OLATResourceable ORES_TEST = OresHelper.createOLATResourceableInstanceWithoutCheck(ClusterAdminControllerCluster.class.getName(), new Long(123));
    private static final OLATResourceable ORES_CACHE_TEST = OresHelper.createOLATResourceableInstance("subcachetypetest", new Long(123));

    ClusterEventBus clusBus;

    private final VelocityContainer mainVc;
    boolean disposed = false;

    private final VelocityContainer nodeInfoVc;

    private final VelocityContainer perfInfoVc;
    private final Link toggleStartStop;
    private final Link resetStats;

    private final Link syncLong;
    private final Link syncShort;
    private final Link testPerf;

    private final Link testCachePut;
    private final Link testCachePut2;

    private final Link testSFUPerf;

    private final Link releaseAllLocksFor;

    private final VelocityContainer cachetest;

    private UserSearchController usc;

    /**
     * @param ureq
     * @param wControl
     */
    public ClusterAdminControllerCluster(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        final CoordinatorManager clustercoord = CoreSpringFactory.getBean(CoordinatorManager.class);
        final ClusterCoordinator cCord = (ClusterCoordinator) clustercoord.getCoordinator();
        clusBus = cCord.getClusterEventBus();

        mainVc = createVelocityContainer("cluster");

        // information about the cluster nodes
        mainVc.contextPut("own_nodeid", "This node is node: '" + clusBus.clusterConfig.getNodeId() + "'");

        nodeInfoVc = createVelocityContainer("nodeinfos");
        final Formatter f = Formatter.getInstance(ureq.getLocale());
        nodeInfoVc.contextPut("f", f);
        mainVc.put("nodeinfos", nodeInfoVc);
        updateNodeInfos();

        toggleStartStop = LinkFactory.createButtonSmall("toggleStartStop", mainVc, this);
        resetStats = LinkFactory.createButtonSmall("resetStats", mainVc, this);

        perfInfoVc = createVelocityContainer("performanceinfos");
        final Formatter f2 = Formatter.getInstance(ureq.getLocale());
        perfInfoVc.contextPut("f", f2);
        mainVc.put("performanceinfos", perfInfoVc);
        updatePerfInfos();

        // test for the distributed cache
        cachetest = createVelocityContainer("cachetest");
        testCachePut = LinkFactory.createButtonSmall("testCachePut", cachetest, this);
        testCachePut2 = LinkFactory.createButtonSmall("testCachePut2", cachetest, this);
        mainVc.put("cachetest", cachetest);
        updateCacheInfo();

        final VelocityContainer busMsgs = createVelocityContainer("busmsgs");
        busMsgs.contextPut("time", Formatter.formatDatetime(new Date()));

        mainVc.put("busmsgs", busMsgs);
        // let a thread repeatively dump all messages
        // final Formatter f = Formatter.getInstance(ureq.getLocale());
        final WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
        final Thread pollThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!disposed) {
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                    wbo.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // simple reput the new lists into the velocity container.
                            // the container is then dirty and automatically rerendered since polling has been turned on here.
                            busMsgs.contextPut("time", Formatter.formatDatetime(new Date()));
                            busMsgs.contextPut("recmsgs", clusBus.getListOfReceivedMsgs());
                            busMsgs.contextPut("sentmsgs", clusBus.getListOfSentMsgs());
                            // also let node infos refresh
                            updateNodeInfos();
                            // also let perf infos refresh
                            updatePerfInfos();
                            // update cache info
                            updateCacheInfo();
                        }
                    });
                }
            }
        });
        pollThread.setDaemon(true);
        pollThread.start();

        // activate polling
        mainVc.put("updatecontrol", new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, 3000));

        // add a few buttons
        syncLong = LinkFactory.createButtonSmall("sync.long", mainVc, this);
        syncShort = LinkFactory.createButtonSmall("sync.short", mainVc, this);
        testPerf = LinkFactory.createButtonSmall("testPerf", mainVc, this);
        testSFUPerf = LinkFactory.createButtonSmall("testSFUPerf", mainVc, this);
        releaseAllLocksFor = LinkFactory.createButtonSmall("releaseAllLocksFor", mainVc, this);

        mainVc.contextPut("eventBusListener", clusBus.toString());
        mainVc.contextPut("busListenerInfos", clusBus.busInfos.getAsString());

        putInitialPanel(mainVc);
    }

    void updateNodeInfos() {
        final Map<Integer, NodeInfo> stats = clusBus.getNodeInfos();
        final List<NodeInfo> li = new ArrayList<NodeInfo>(stats.values());
        Collections.sort(li, new Comparator<NodeInfo>() {
            @Override
            public int compare(final NodeInfo o1, final NodeInfo o2) {
                return o1.getNodeId().compareTo(o2.getNodeId());
            }
        });
        nodeInfoVc.contextPut("stats", li);
        nodeInfoVc.contextPut("thisNodeId", clusBus.clusterConfig.getNodeId());
        mainVc.contextPut("eventBusListener", clusBus.toString());
        mainVc.contextPut("busListenerInfos", clusBus.busInfos.getAsString());
    }

    void updatePerfInfos() {
        // collect performance information

        final List<PerfItem> li = PerformanceMonitorHelper.getPerfItems();
        li.addAll(clusBus.getPerfItems());
        perfInfoVc.contextPut("perfs", li);
        if (PerformanceMonitorHelper.isStarted()) {
            perfInfoVc.contextPut("started", "started");
        } else {
            perfInfoVc.contextPut("started", "notstarted");
        }
    }

    @Override
    protected void doDispose() {
        disposed = true;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == syncLong) {
            // sync on a olatresourceable and hold the lock for 5 seconds.
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ORES_TEST, new SyncerExecutor() {
                @Override
                public void execute() {
                    sleep(5000);
                }
            });
            // the runnable is executed within the same thread->
            getWindowControl().setInfo("done syncing on the test olatresourceable for 5 seconds");
        } else if (source == syncShort) {
            // sync on a olatresourceable and hold the lock for 1 second.
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ORES_TEST, new SyncerExecutor() {
                @Override
                public void execute() {
                    sleep(1000);
                }
            });
            // the runnable is executed within the same thread->
            getWindowControl().setInfo("done syncing on the test olatresourceable for 1 second");
        } else if (source == testPerf) {
            // send 1000 (short) messages over the cluster bus
            final int cnt = 1000;
            final long start = System.nanoTime();
            for (int i = 0; i < cnt; i++) {
                clusBus.fireEventToListenersOf(new MultiUserEvent("jms-perf-test-" + i + " of " + cnt), ORES_TEST);
            }
            final long stop = System.nanoTime();
            final long dur = stop - start;
            final double inmilis = dur / 1000000;
            final double avg = dur / cnt;
            final double avgmilis = avg / 1000000;
            getWindowControl().setInfo("sending " + cnt + " messages took " + inmilis + " ms, avg per messages was " + avg + " ns = " + avgmilis + " ms");
        } else if (source == testCachePut) {
            final CacheWrapper cw = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(this.getClass(), "cachetest")
                    .getOrCreateChildCacheWrapper(ORES_CACHE_TEST);
            // we explicitly use put and not putSilent to show that a put invalidates (and thus removes) this key of this cache in all other cluster nodes.
            cw.update("akey", "hello");
            updateCacheInfo();
        } else if (source == testCachePut2) {
            // we explicitly use put and not putSilent to show that a put invalidates (and thus removes) this key of this cache in all other cluster nodes.
            final CacheWrapper cw = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(this.getClass(), "cachetest")
                    .getOrCreateChildCacheWrapper(ORES_CACHE_TEST);
            cw.update("akey", "world");
            updateCacheInfo();
        } else if (source == testSFUPerf) {
            // acquire a sync 1000x times (does internally a select-for-update on the database)
            final int cnt = 1000;
            final long start = System.nanoTime();
            for (int i = 0; i < cnt; i++) {
                CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ORES_TEST, new SyncerExecutor() {
                    @Override
                    public void execute() {
                        // empty
                    }
                });
            }
            final long stop = System.nanoTime();
            final long dur = stop - start;
            final double inmilis = dur / 1000000;
            final double avg = dur / cnt;
            final double avgmilis = avg / 1000000;
            getWindowControl().setInfo(
                    "acquiring " + cnt + " locks for syncing (using db's \"select for update\") took " + inmilis + " ms, avg per messages was " + avg + " ns = "
                            + avgmilis + " ms");
        } else if (source == releaseAllLocksFor) {
            // let a user search pop up
            usc = new UserSearchController(ureq, getWindowControl(), true);
            listenTo(usc);
            getWindowControl().pushAsModalDialog(usc.getInitialComponent());
        } else if ((source == nodeInfoVc) && (event.getCommand().equals("switchToNode"))) {
            String nodeIdStr = ureq.getHttpReq().getParameter("nodeId");
            if (nodeIdStr.length() == 1) {
                nodeIdStr = "0" + nodeIdStr;
            }
            final Cookie[] cookies = ureq.getHttpReq().getCookies();
            for (int i = 0; i < cookies.length; i++) {
                final Cookie cookie = cookies[i];
                if ("JSESSIONID".equals(cookie.getName())) {
                    String redirectedButInvalidSessionId = cookie.getValue();
                    redirectedButInvalidSessionId = redirectedButInvalidSessionId.substring(0, redirectedButInvalidSessionId.length() - 2) + nodeIdStr;
                    log.info("redirecting session to node " + nodeIdStr + " , new sessionid=" + redirectedButInvalidSessionId);
                    cookie.setValue(redirectedButInvalidSessionId);
                    replaceCookie(ureq.getHttpReq(), ureq.getHttpResp(), cookie);

                    // OLAT-5165: make sure we can always bypass the dmz reject mechanism (for 5min that is)
                    final Cookie newCookie = new Cookie("bypassdmzreject", String.valueOf(System.currentTimeMillis()));
                    newCookie.setMaxAge(5 * 60); // 5min lifetime
                    newCookie.setPath(WebappHelper.getServletContextPath());
                    newCookie.setSecure(ureq.getHttpReq().isSecure());
                    newCookie.setComment("cookie allowing olat admin users to bypass dmz rejects");
                    ureq.getHttpResp().addCookie(newCookie);

                    final OncePanel oncePanel = new OncePanel("refresh");
                    oncePanel.setContent(createVelocityContainer("refresh"));
                    mainVc.put("refresh", oncePanel);
                    break;
                }
            }
        } else if (source == toggleStartStop) {
            if (!PerformanceMonitorHelper.toggleStartStop()) {
                getWindowControl().setInfo("Could not start PerformanceMonitor. CodepointServer not enabled in VM?");
            } else {
                clusBus.resetStats();
                updatePerfInfos();
            }
        } else if (source == resetStats) {
            PerformanceMonitorHelper.resetStats();
            clusBus.resetStats();
            updatePerfInfos();
        }
    }

    private void replaceCookie(final HttpServletRequest request, final HttpServletResponse response, final Cookie cookie) {
        // for a generalized version of this, use org/apache/tomcat/util/http/ServerCookie.java
        response.setHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue() + "; Path=" + request.getContextPath() + (request.isSecure() ? "" : "; Secure"));
    }

    @Override
    public void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == usc) {
            getWindowControl().pop();
            if (event != Event.CANCELLED_EVENT) {
                // we configured usc to either cancel or to only accept single user selection.
                final SingleIdentityChosenEvent sce = (SingleIdentityChosenEvent) event;
                final Identity ident = sce.getChosenIdentity();
                ClusterLockDao clusterLockManager = CoreSpringFactory.getBean(ClusterLockDao.class);
                clusterLockManager.releaseAllLocksFor(ident.getName());
                showInfo("locks.released", ident.getName());
            }
        }
    }

    void sleep(final int milis) {
        try {
            Thread.sleep(milis);
        } catch (final InterruptedException e) {
            // ignore
        }
    }

    void updateCacheInfo() {
        final CacheWrapper cw = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(this.getClass(), "cachetest")
                .getOrCreateChildCacheWrapper(ORES_CACHE_TEST);
        final Object val = cw.get("akey");
        cachetest.contextPut("cacheval", val == null ? "-null-" : val);
    }

}
