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

package org.olat.presentation.admin.sysinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olat.connectors.webdav.WebDAVManager;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.admin.sysinfo.LogFileParser;
import org.olat.lms.admin.sysinfo.MaintenanceMsgManager;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.admin.cache.AllCachesController;
import org.olat.presentation.admin.sysinfo.logging.LogRealTimeViewerController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.AutoCreator;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.Tracing;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

import com.anthonyeden.lib.config.Configuration;
import com.anthonyeden.lib.config.ConfigurationException;
import com.anthonyeden.lib.config.XMLConfiguration;

/**
 * Description:<br>
 * all you wanted to know about your running OLAT system
 * 
 * @author Felix Jost
 */
public class SysinfoController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String ACTION_SNOOP = "snoop";
    private static final String ACTION_ERRORS = "errors";
    private static final String ACTION_MAINTENANCE_MSG = "infomsg";
    private static final String ACTION_SETLEVEL = "setlevel";
    private static final String ACTION_LOGLEVELS = "loglevels";
    private static final String ACTION_VIEWLOG = "viewlog";
    private static final String ACTION_VIEWLOG_PACKAGE = "p";
    private static final String ACTION_SESSIONS = "sessions";
    private static final String ACTION_SYSINFO = "sysinfo";
    private static final String ACTION_HIBERNATEINFO = "hibernate";
    private static final String ACTION_LOCKS = "locks";

    private VelocityContainer mySessions;
    private final VelocityContainer mySnoop, myErrors, myLoglevels, mySysinfo, myLocks, myMultiUserEvents, myHibernateInfo;
    private final Panel cachePanel;
    private UserSessionController usessC;
    private final LockController lockController;
    private final SessionAdministrationController sessionAdministrationController;
    private final TabbedPane tabbedPane;

    private final RequestLoglevelController requestLoglevelController;

    private String err_nr;
    private String err_dd;
    private String err_mm;
    private String err_yyyy;
    private AllCachesController cacheController;

    private final Link resetloglevelsButton;
    private final Link gcButton;
    private final Link redScreenButton;
    private final Controller clusterController;
    private final Link enableHibernateStatisticsButton;
    private final Link disableHibernateStatisticsButton;
    private final Link clearHibernateStatisticsButton;
    private CloseableModalController cmc;
    private LogRealTimeViewerController logViewerCtr;
    private final Controller maintenanceMsgCtrl;

    /**
     * @param ureq
     * @param wControl
     */
    public SysinfoController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        if (!getBaseSecurityEBL().isIdentityPermittedOnResourceable(ureq.getIdentity(), OresHelper.lookupType(this.getClass()))) {
            throw new OLATSecurityException("Insufficient permissions to access SysinfoController");
        }

        usessC = new UserSessionController(ureq, getWindowControl());
        lockController = new LockController(ureq, getWindowControl());
        myErrors = createVelocityContainer("errors");
        myLoglevels = createVelocityContainer("loglevels");
        resetloglevelsButton = LinkFactory.createButton("resetloglevels", myLoglevels, this);

        mySysinfo = createVelocityContainer("sysinfo");
        gcButton = LinkFactory.createButton("run.gc", mySysinfo, this);
        redScreenButton = LinkFactory.createButton("error.button.redscreen", myErrors, this);
        // add system startup time
        final SimpleDateFormat startupTimeFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", ureq.getLocale());
        mySysinfo.contextPut("startupTime", startupTimeFormatter.format(new Date(WebappHelper.getTimeOfServerStartup())));

        mySnoop = createVelocityContainer("snoop");

        myHibernateInfo = createVelocityContainer("hibernateinfo");
        enableHibernateStatisticsButton = LinkFactory.createButton("enable.hibernate.statistics", myHibernateInfo, this);
        disableHibernateStatisticsButton = LinkFactory.createButton("disable.hibernate.statistics", myHibernateInfo, this);
        clearHibernateStatisticsButton = LinkFactory.createButton("clear.hibernate.statistics", myHibernateInfo, this);

        sessionAdministrationController = new SessionAdministrationController(ureq, getWindowControl());
        requestLoglevelController = new RequestLoglevelController(ureq, getWindowControl());
        myLocks = createVelocityContainer("locks");
        myMultiUserEvents = createVelocityContainer("multiuserevents");

        // info message controller has two implementations (SingleVM or cluster)
        final MaintenanceMsgManager InfoMgr = CoreSpringFactory.getBean(MaintenanceMsgManager.class);
        maintenanceMsgCtrl = InfoMgr.getMaintenanceMessageController(ureq, getWindowControl());

        tabbedPane = new TabbedPane("tp", ureq.getLocale());
        tabbedPane.addTab(ACTION_SESSIONS, usessC.getInitialComponent());
        tabbedPane.addTab(ACTION_MAINTENANCE_MSG, maintenanceMsgCtrl.getInitialComponent());
        tabbedPane.addTab(ACTION_ERRORS, myErrors);
        tabbedPane.addTab(ACTION_LOGLEVELS, myLoglevels);
        tabbedPane.addTab(ACTION_SYSINFO, mySysinfo);
        tabbedPane.addTab(ACTION_SNOOP, mySnoop);
        tabbedPane.addTab("requestloglevel", requestLoglevelController.getInitialComponent());
        tabbedPane.addTab("usersessions", sessionAdministrationController.getInitialComponent());
        tabbedPane.addTab(ACTION_LOCKS, lockController.getInitialComponent());
        tabbedPane.addTab(getTranslator().translate("sess.multiuserevents"), myMultiUserEvents);
        tabbedPane.addTab(ACTION_HIBERNATEINFO, myHibernateInfo);

        final AutoCreator controllerCreator = (AutoCreator) CoreSpringFactory.getBean("clusterAdminControllerCreator");
        clusterController = controllerCreator.createController(ureq, wControl);
        tabbedPane.addTab("Cluster", clusterController.getInitialComponent());

        cachePanel = new Panel("cachepanel");
        tabbedPane.addTab("caches", cachePanel);

        final VelocityContainer myBuildinfo = createVelocityContainer("buildinfo");
        fillBuildInfoTab(myBuildinfo);
        tabbedPane.addTab("buildinfo", myBuildinfo);

        tabbedPane.addListener(this);
        putInitialPanel(tabbedPane);

        final Date now = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        final String sNow = sdf.format(now);
        err_dd = sNow.substring(0, 2);
        err_mm = sNow.substring(3, 5);
        err_yyyy = sNow.substring(6, 10);
        myErrors.contextPut("highestError", Tracing.getTotalErrorCount());
        myErrors.contextPut("mydd", err_dd);
        myErrors.contextPut("mymm", err_mm);
        myErrors.contextPut("myyyyy", err_yyyy);
        myErrors.contextPut("olat_formatter", Formatter.getInstance(ureq.getLocale()));
        myErrors.contextPut("example_error", Settings.getNodeInfo() + "-E12 " + Settings.getNodeInfo() + "-E64...");
        // FIXME:fj:b do not use this call
        event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, mySessions));
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private void fillBuildInfoTab(final VelocityContainer myBuildinfo) {
        final List<Map> properties = new LinkedList<Map>();
        Map<String, String> m = new HashMap<String, String>();
        m.put("key", "Version");
        m.put("value", Settings.getFullVersionInfo());
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "isClusterMode");
        m.put("value", Settings.getClusterMode().equals("Cluster") ? "true" : "false");
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "nodeId");
        m.put("value", Settings.getNodeInfo().equals("") ? "N1" : Settings.getNodeInfo());
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "serverStartTime");
        final Date timeOfServerStartup = new Date(WebappHelper.getTimeOfServerStartup());
        m.put("value", String.valueOf(timeOfServerStartup));
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "Build date");
        m.put("value", String.valueOf(Settings.getBuildDate()));
        properties.add(m);

        final File baseDir = new File(WebappHelper.getContextRoot(), "..");
        m = new HashMap<String, String>();
        try {
            m.put("key", "baseDir");
            m.put("value", baseDir.getCanonicalPath());
        } catch (final IOException e1) {
            // then fall back to unresolved path
            m.put("key", "baseDir");
            m.put("value", baseDir.getAbsolutePath());
        }
        properties.add(m);

        m = new HashMap<String, String>();
        m.put("key", "WebDAVEnabled");
        final boolean webDavEnabled = WebDAVManager.getInstance().isEnabled();
        m.put("value", Boolean.toString(webDavEnabled));
        properties.add(m);

        myBuildinfo.contextPut("properties", properties);

        final File deploymentInfoProperties = new File(WebappHelper.getContextRoot(), "deployment-info.properties");

        // defaults
        myBuildinfo.contextPut("existsActivePatchFile", false);
        myBuildinfo.contextPut("existsDeploymentInfoProperties", false);
        myBuildinfo.contextPut("existsPatchFile", false);

        if (deploymentInfoProperties.exists()) {
            myBuildinfo.contextPut("existsDeploymentInfoProperties", true);
            myBuildinfo.contextPut("fileDateDeploymentInfoProperties", new Date(deploymentInfoProperties.lastModified()));
            final List<Map> deploymentInfoPropertiesLines = new LinkedList<Map>();
            try {
                final BufferedReader r = new BufferedReader(new FileReader(deploymentInfoProperties));
                while (true) {
                    final String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    final Map<String, String> lineMap = new HashMap<String, String>();
                    lineMap.put("line", line);
                    deploymentInfoPropertiesLines.add(lineMap);
                }
            } catch (final IOException ioe) {
                final Map<String, String> lineMap = new HashMap<String, String>();
                lineMap.put("line", "Problems reading deployment-info.properties: " + ioe);
                deploymentInfoPropertiesLines.add(lineMap);
            }
            myBuildinfo.contextPut("deploymentInfoPropertiesLines", deploymentInfoPropertiesLines);

            final File patchesNewest = new File(WebappHelper.getContextRoot(), "patches.xml.newest");
            if (!patchesNewest.exists()) {
                myBuildinfo.contextPut("existsPatchFile", false);
            } else {
                myBuildinfo.contextPut("existsPatchFile", true);
                final Date patchesFileDate = new Date(patchesNewest.lastModified());
                myBuildinfo.contextPut("patchesFileDate", patchesFileDate);

                final boolean patchesActive = patchesFileDate.before(timeOfServerStartup);
                if (patchesActive) {
                    myBuildinfo.contextPut("patchesActive", "yes, patch(es) active");
                } else {
                    myBuildinfo.contextPut("patchesActive", "probably not: they are deployed but server hasn't been restarted since. Will be active after restart!");
                }

                final List<Map> patches = new LinkedList<Map>();
                final String baseTag = readPatchesXml(patchesNewest, patches);
                myBuildinfo.contextPut("patchesBaseTag", baseTag);
                myBuildinfo.contextPut("patches", patches);

                if (!patchesActive) {
                    // find the active patch
                    final File[] allPatches = new File(WebappHelper.getContextRoot()).listFiles(new FilenameFilter() {

                        /**
						 */
                        public boolean accept(final File dir, final String name) {
                            if (name == null) {
                                return false;
                            } else {
                                return name.startsWith("patches.xml.");
                            }
                        }

                    });
                    File activePatchFile = null;
                    for (int i = 0; i < allPatches.length; i++) {
                        final File aPatchFile = allPatches[i];
                        if (new Date(aPatchFile.lastModified()).before(timeOfServerStartup)) {
                            // then it was potentially active at some point. Let's see if it is the newest before the
                            // timeOfServerStartup
                            if (activePatchFile == null) {
                                activePatchFile = aPatchFile;
                            } else if (new Date(activePatchFile.lastModified()).before(new Date(aPatchFile.lastModified()))) {
                                activePatchFile = aPatchFile;
                            }
                        }
                    }
                    if (activePatchFile != null) {
                        myBuildinfo.contextPut("existsActivePatchFile", true);
                        myBuildinfo.contextPut("activePatchFileName", activePatchFile.getName());
                        final Date activePatchesFileDate = new Date(activePatchFile.lastModified());
                        myBuildinfo.contextPut("activePatchesFileDate", activePatchesFileDate);
                        final List<Map> activePatches = new LinkedList<Map>();
                        final String activeBaseTag = readPatchesXml(activePatchFile, activePatches);
                        myBuildinfo.contextPut("activePatchesBaseTag", activeBaseTag);
                        myBuildinfo.contextPut("activePatches", activePatches);
                    }
                }
            }
        }
    }

    private String readPatchesXml(final File patchesNewest, final List<Map> patches) {
        XMLConfiguration patchConfig = null;
        Map<String, String> m;
        try {
            patchConfig = new XMLConfiguration(patchesNewest);
            for (final Iterator<Configuration> it = patchConfig.getChildren().iterator(); it.hasNext();) {
                final Configuration aPatchConfig = it.next();
                m = new HashMap<String, String>();
                m.put("id", aPatchConfig.getAttribute("patch-id"));
                m.put("enabled", aPatchConfig.getAttribute("enabled"));
                m.put("jira", aPatchConfig.getAttribute("jira"));
                m.put("tag", aPatchConfig.getAttribute("tag"));
                m.put("title", aPatchConfig.getChildValue("description"));
                patches.add(m);
            }
            return patchConfig.getAttribute("basetag");
        } catch (final ConfigurationException e) {
            m = new HashMap<String, String>();
            m.put("id", "Problems reading patches.xml.newest: " + e);
            m.put("enabled", "");
            m.put("jira", "");
            m.put("tag", "");
            m.put("title", "");
            patches.add(m);
            return "";
        }
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == tabbedPane) { // those must be links
            final TabbedPaneChangedEvent tbcEvent = (TabbedPaneChangedEvent) event;
            final Component newComponent = tbcEvent.getNewComponent();
            if (newComponent == cachePanel) {
                if (cacheController != null) {
                    cacheController.dispose();
                }
                cacheController = new AllCachesController(ureq, getWindowControl());
                cachePanel.setContent(cacheController.getInitialComponent());
            } else if (newComponent == maintenanceMsgCtrl.getInitialComponent()) {

            }

            else if (newComponent == mySysinfo) {
                final Runtime r = Runtime.getRuntime();
                final StringBuilder sb = new StringBuilder();
                appendFormattedKeyValue(sb, "Processors", new Integer(r.availableProcessors()));
                appendFormattedKeyValue(sb, "Total Memory", StringHelper.formatMemory(r.totalMemory()));
                appendFormattedKeyValue(sb, "Free Memory", StringHelper.formatMemory(r.freeMemory()));
                appendFormattedKeyValue(sb, "Max Memory", StringHelper.formatMemory(r.maxMemory()));
                final int controllerCnt = DefaultController.getControllerCount();
                sb.append("<br />Controller Count (active and not disposed):" + controllerCnt);
                sb.append("<br />Concurrent Dispatching Threads: " + DispatcherAction.getConcurrentCounter());
                mySysinfo.contextPut("memory", sb.toString());
                mySysinfo.contextPut("threads", getThreadsInfo());
                mySysinfo.contextPut("javaenv", getJavaenv());

            } else if (newComponent == usessC.getInitialComponent()) {
                usessC.reset();
            } else if (newComponent == lockController.getInitialComponent()) {
                lockController.resetTableModel();
            } else if (newComponent == myMultiUserEvents) {
                final StringBuilder sb = new StringBuilder();
                final Map infocenter = CoordinatorManager.getInstance().getCoordinator().getEventBus().getUnmodifiableInfoCenter();
                final int cntRes = infocenter.size();
                // cluster::: sort the entries (table?): sort by count and name
                // REVIEW:2008-12-11:pb access ea.getListenerCount -> possible dead lock
                // -> look for a different way to show info
                // see also OLAT-3681
                //
                /*
                 * sb.append("Total (possible weak-referenced) Resources: "+cntRes+
                 * " (showing only those with listeners, 'null' for a listener value meaning the OLAT system), count is cluster-wide, identities only vm-wide<br /><br />"
                 * ); for (Iterator it_ores = infocenter.entrySet().iterator(); it_ores.hasNext();) { Map.Entry entry = (Map.Entry) it_ores.next(); String
                 * oresDerivedString = (String) entry.getKey(); EventAgency ea = (EventAgency) entry.getValue(); Set listenIdentNames = ea.getListeningIdentityNames(); if
                 * (listenIdentNames.size() > 0) {
                 * sb.append("<b>Resource:</b> [").append(ea.getListenerCount()).append("] on ").append(oresDerivedString).append("<br />Listeners: "); for (Iterator
                 * it_id = listenIdentNames.iterator(); it_id.hasNext();) { String login = (String) it_id.next(); sb.append(login).append("; "); }
                 * sb.append("<br /><br />"); } }
                 */
                sb.append(" <a href=\"http://bugs.olat.org/jira/browse/OLAT-3681\">OLAT-3681</a> ");
                myMultiUserEvents.contextPut("info", sb.toString());
            } else if (newComponent == sessionAdministrationController.getInitialComponent()) {
            } else if (newComponent == myHibernateInfo) {
                myHibernateInfo.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
                myHibernateInfo.contextPut("hibernateStatistics", DBFactory.getInstance(false).getStatistics());
            } else if (newComponent == myLoglevels) {
                final List loggers = Tracing.getLoggersSortedByName(); // put it in a list in case of a reload (enum can only be used once)
                myLoglevels.contextPut("loggers", loggers);

            } else if (newComponent == mySnoop) {
                mySnoop.contextPut("snoop", getSnoop(ureq));
            }
        } else if (source == myLoglevels) {
            if (event.getCommand().equals(ACTION_SETLEVEL)) {
                final String level = ureq.getHttpReq().getParameter("level");
                final String logger = ureq.getHttpReq().getParameter("logger");
                if (log.equals(org.olat.system.logging.Tracing.class.getName())) {
                    getWindowControl().setError("log level of " + org.olat.system.logging.Tracing.class.getName() + " must not be changed!");
                    return;
                }
                Level l;
                if (level.equals("debug")) {
                    l = Level.DEBUG;
                } else if (level.equals("info")) {
                    l = Level.INFO;
                } else if (level.equals("warn")) {
                    l = Level.WARN;
                } else {
                    l = Level.ERROR;
                }

                Tracing.setLevelForLogger(l, logger);
                getWindowControl().setInfo("Set logger " + logger + " to level " + level);

            } else if (event.getCommand().equals(ACTION_VIEWLOG)) {
                final String toBeViewed = ureq.getParameter(ACTION_VIEWLOG_PACKAGE);
                if (toBeViewed == null) {
                    return; // should not happen
                }
                if (logViewerCtr != null) {
                    logViewerCtr.dispose();
                }
                logViewerCtr = new LogRealTimeViewerController(ureq, getWindowControl(), toBeViewed, Level.ALL, true);
                if (cmc != null) {
                    cmc.dispose();
                }
                cmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), logViewerCtr.getInitialComponent());
                cmc.addControllerListener(this);
                cmc.activate();
            }
            // push loglevel list again
            event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myLoglevels));
        } else if (source == resetloglevelsButton) {
            Tracing.setLevelForAllLoggers(Level.INFO);
            getWindowControl().setInfo("All loglevels set to INFO");
        } else if (source == gcButton) {
            Runtime.getRuntime().gc();
            getWindowControl().setInfo("Garbage collection done.");
            event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, mySysinfo));
        } else if (source == myErrors) {
            final HttpServletRequest hreq = ureq.getHttpReq();
            err_nr = hreq.getParameter("mynr");
            if (hreq.getParameter("mydd") != null) {
                err_dd = hreq.getParameter("mydd");
            }
            if (hreq.getParameter("mymm") != null) {
                err_mm = hreq.getParameter("mymm");
            }
            if (hreq.getParameter("myyyyy") != null) {
                err_yyyy = hreq.getParameter("myyyyy");
            }
            if (err_nr != null) {
                myErrors.contextPut("mynr", err_nr);
                myErrors.contextPut("errormsgs", getLogFileParser().getError(err_nr, err_dd, err_mm, err_yyyy, true));
            }

            myErrors.contextPut("highestError", Tracing.getTotalErrorCount());
            myErrors.contextPut("mydd", err_dd);
            myErrors.contextPut("mymm", err_mm);
            myErrors.contextPut("myyyyy", err_yyyy);
            myErrors.contextPut("olat_formatter", Formatter.getInstance(ureq.getLocale()));

        } else if (source == enableHibernateStatisticsButton) {
            DBFactory.getInstance(false).getStatistics().setStatisticsEnabled(true);
            myHibernateInfo.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
            getWindowControl().setInfo("Hibernate statistics enabled.");
            event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myHibernateInfo));
        } else if (source == disableHibernateStatisticsButton) {
            DBFactory.getInstance(false).getStatistics().setStatisticsEnabled(false);
            myHibernateInfo.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
            getWindowControl().setInfo("Hibernate statistics disabled.");
            event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myHibernateInfo));
        } else if (source == clearHibernateStatisticsButton) {
            DBFactory.getInstance(false).getStatistics().clear();
            getWindowControl().setInfo("Hibernate statistics clear done.");
            event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myHibernateInfo));
        } else if (source == redScreenButton) {
            throw new RuntimeException("Redscreen test from admin console!");
        }
    }

    private LogFileParser getLogFileParser() {
        return CoreSpringFactory.getBean(LogFileParser.class);
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == cmc) {
            cmc.dispose();
            cmc = null;
            if (logViewerCtr != null) {
                logViewerCtr.dispose();
                logViewerCtr = null;
            }
        }
    }

    private String getJavaenv() {
        final Set<Object> keySet = System.getProperties().keySet();
        final List<String> propertyList = new ArrayList<String>(keySet.size());
        for (Object key : keySet) {
            propertyList.add((String) key);
        }
        Collections.sort(propertyList);

        final StringBuilder props = new StringBuilder();
        final int lineCut = 100;
        for (String property : propertyList) {
            props.append("<b>" + property + "</b>&nbsp;=&nbsp;");
            String value = System.getProperty(property);
            if (value.length() <= lineCut) {
                props.append(value);
            } else {
                props.append(value.substring(0, lineCut - property.length()));
                while (value.length() > lineCut) {
                    value = "<br />" + value.substring(lineCut);
                    props.append(value.substring(0, value.length() > lineCut ? lineCut : value.length()));
                }
            }
            props.append("<br />");
        }
        return props.toString();
    }

    /**
     * @param ureq
     * @return Formatted HTML
     */
    private String getSnoop(final UserRequest ureq) {
        final StringBuilder sb = new StringBuilder();
        final HttpServletRequest hreq = ureq.getHttpReq();
        sb.append("<h4>Request attributes:</h4>");
        Enumeration e = hreq.getAttributeNames();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            final Object value = hreq.getAttribute(key);
            appendFormattedKeyValue(sb, key, value);
        }

        appendFormattedKeyValue(sb, "Protocol", hreq.getProtocol());
        appendFormattedKeyValue(sb, "Scheme", hreq.getScheme());
        appendFormattedKeyValue(sb, "Server Name", hreq.getServerName());
        appendFormattedKeyValue(sb, "Server Port", new Integer(hreq.getServerPort()));
        appendFormattedKeyValue(sb, "Remote Addr", hreq.getRemoteAddr());
        appendFormattedKeyValue(sb, "Remote Host", hreq.getRemoteHost());
        appendFormattedKeyValue(sb, "Character Encoding", hreq.getCharacterEncoding());
        appendFormattedKeyValue(sb, "Content Length", new Integer(hreq.getContentLength()));
        appendFormattedKeyValue(sb, "Content Type", hreq.getContentType());
        appendFormattedKeyValue(sb, "Locale", hreq.getLocale());

        sb.append("<h4>Parameter names in this hreq:</h4>");
        e = hreq.getParameterNames();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            final String[] values = hreq.getParameterValues(key);
            String value = "";
            for (int i = 0; i < values.length; i++) {
                value = value + " " + values[i];
            }
            appendFormattedKeyValue(sb, key, value);
        }

        sb.append("<h4>Headers in this hreq:</h4>");
        e = hreq.getHeaderNames();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            final String value = hreq.getHeader(key);
            appendFormattedKeyValue(sb, key, value);
        }
        sb.append("<h4>Cookies in this hreq:</h4>");
        final Cookie[] cookies = hreq.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                final Cookie cookie = cookies[i];
                appendFormattedKeyValue(sb, cookie.getName(), cookie.getValue());
            }
        }

        sb.append("<h4>Hreq parameters:</h4>");
        appendFormattedKeyValue(sb, "Request Is Secure", new Boolean(hreq.isSecure()));
        appendFormattedKeyValue(sb, "Auth Type", hreq.getAuthType());
        appendFormattedKeyValue(sb, "HTTP Method", hreq.getMethod());
        appendFormattedKeyValue(sb, "Remote User", hreq.getRemoteUser());
        appendFormattedKeyValue(sb, "Request URI", hreq.getRequestURI());
        appendFormattedKeyValue(sb, "Context Path", hreq.getContextPath());
        appendFormattedKeyValue(sb, "Servlet Path", hreq.getServletPath());
        appendFormattedKeyValue(sb, "Path Info", hreq.getPathInfo());
        appendFormattedKeyValue(sb, "Path Trans", hreq.getPathTranslated());
        appendFormattedKeyValue(sb, "Query String", hreq.getQueryString());

        final HttpSession hsession = hreq.getSession();
        appendFormattedKeyValue(sb, "Requested Session Id", hreq.getRequestedSessionId());
        appendFormattedKeyValue(sb, "Current Session Id", hsession.getId());
        appendFormattedKeyValue(sb, "Session Created Time", new Long(hsession.getCreationTime()));
        appendFormattedKeyValue(sb, "Session Last Accessed Time", new Long(hsession.getLastAccessedTime()));
        appendFormattedKeyValue(sb, "Session Max Inactive Interval Seconds", new Long(hsession.getMaxInactiveInterval()));

        sb.append("<h4>Session values:</h4> ");
        final Enumeration names = hsession.getAttributeNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            appendFormattedKeyValue(sb, name, hsession.getAttribute(name));
        }
        return sb.toString();
    }

    private void appendFormattedKeyValue(final StringBuilder sb, final String key, final Object value) {
        sb.append("&nbsp;&nbsp;&nbsp;<b>");
        sb.append(key);
        sb.append(":</b>&nbsp;");
        sb.append(value);
        sb.append("<br />");
    }

    private String getThreadsInfo() {
        final StringBuilder sb = new StringBuilder("<pre>threads:<br />");
        try { // to be sure
            final ThreadGroup tg = Thread.currentThread().getThreadGroup();
            final int actCnt = tg.activeCount();
            final int grpCnt = tg.activeGroupCount();
            sb.append("about " + actCnt + " threads, " + grpCnt + " groups<br /><br />");
            final Thread[] threads = new Thread[actCnt];
            tg.enumerate(threads, true);
            for (int i = 0; i < actCnt; i++) {
                final Thread tr = threads[i];
                if (tr != null) { // thread may have finished in the meantime
                    final String name = tr.getName();
                    final boolean alive = tr.isAlive();
                    final boolean interrupted = tr.isInterrupted();
                    sb.append("Thread: (alive = " + alive + ", interrupted: " + interrupted + ", group:" + tr.getThreadGroup().getName() + ") " + name + "<br />");
                }
            }
        } catch (final Exception e) {
            sb.append("exception occured:" + e.getMessage());
        }
        return sb.toString() + "</pre>";
    }

    /**
	 */
    protected void doDispose() {
        if (usessC != null) {
            usessC.dispose();
            usessC = null;
        }
        if (cacheController != null) {
            cacheController.dispose();
            cacheController = null;
        }
        if (clusterController != null) {
            clusterController.dispose();
        }
        if (cmc != null) {
            cmc.dispose();
            cmc = null;
        }
        if (logViewerCtr != null) {
            logViewerCtr.dispose();
            logViewerCtr = null;
        }
    }
}
