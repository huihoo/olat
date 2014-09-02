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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.system.logging;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.olat.system.logging.log4j.LogNumberPatternConverter;

/**
 * 
 * @author Guido Schnider
 */
public class Tracing {

    private static final String USERNAME = "username";
    private static final String IP = "ip";
    private static final String REFERER = "referer";
    private static final String USER_AGENT = "useragent";
    public static String nodeId = "-n/a";

    /**
     * [spring]
     */
    private Tracing(String nodeId) {
        Tracing.nodeId = nodeId;
    }

    /**
     * sets the HttpServletRequest for the actual click/user request. This method should only be called once per thread(/servlet invocation) and also be the first method
     * call in the <code>void service(HttpServletRequest req, HttpServletResponse resp)</code> or the respective
     * <code>void doXYZ(HttpServletRequest request, HttpServletResponse response)</code> methods.<br>
     * This method accesses the thread local data store.
     * 
     * @param ureq
     */
    public static void setLogRequestInfo(LogRequestInfo logRequestInfo) {
        MDC.put(USERNAME, logRequestInfo.getIdentityName());
        MDC.put(IP, logRequestInfo.getRemoteIp());
        MDC.put(USER_AGENT, logRequestInfo.getUserAgent());
        MDC.put(REFERER, logRequestInfo.getReferer());
    }

    /**
     * clears the tread local data at the end of the request and *MUST* be called
     */
    public static void clearLogRequestInfo() {
        MDC.clear();
    }

    public static long getNextErrorNumber() {
        return LogNumberPatternConverter.errorCounter.get() + 1;
    }

    public static long getTotalErrorCount() {
        return LogNumberPatternConverter.errorCounter.get();
    }

    /**
     * @return list all current loggers
     */
    // can't avoid warnung since log4j doesn't implement generics
    @SuppressWarnings("unchecked")
    private static List<Logger> getLoggers() {
        return Collections.list(LogManager.getCurrentLoggers());
    }

    /**
     * set provided log level for all active loggers.
     * 
     * @param logLevel
     */
    public static void setLevelForAllLoggers(Level logLevel) {
        for (Logger logger : getLoggers()) {
            logger.setLevel(logLevel);
        }
    }

    /**
     * set log level of specified logger
     * 
     * @param logLevel
     * @param name
     */
    public static void setLevelForLogger(Level logLevel, String name) {
        Logger logger = LogManager.getLogger(name);
        if (logger != null) {
            logger.setLevel(logLevel);
        }
    }

    /**
     * generates active loggers list sorted by name.
     * 
     * @return
     */
    public static List<Logger> getLoggersSortedByName() {
        List<Logger> loggers = getLoggers();
        Collections.sort(loggers, new Comparator<Logger>() {
            @Override
            public int compare(Logger l1, Logger l2) {
                return l1.getName().compareTo(l2.getName());
            }
        });
        return loggers;
    }

}
