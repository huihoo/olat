package org.olat.system.logging.threadlog;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class manages the usernames and the associated loglevel/appender pairs in conjunction with the ThreadLocalLogLevelManager which it calls to do the actual
 * threadlocal based log level controlling.
 * <p>
 * This class is basically a Map containing the usernames for which log levels/appenders are modified.
 * <P>
 * Initial Date: 13.09.2010 <br>
 * 
 * @author Stefan
 */
public class UserBasedLogLevelManager implements Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    /** The core of this class is this map containing the list of usernames mapped to logconfigs **/
    private final Map<String, LogConfig> username2LogConfig = new HashMap<String, LogConfig>();

    /** A reference to the ThreadLocalLogLevelManager is used to trigger the actual threadlocal based log level controlling **/
    private ThreadLocalLogLevelManager threadLocalLogLevelManager;

    /** semi-old-school way of allowing controllers to access a manager - via this INSTANCE construct **/
    private static UserBasedLogLevelManager INSTANCE;

    @Autowired
    private SystemPropertiesService propertiesService;

    /**
     * [spring]
     */
    protected UserBasedLogLevelManager() {
        //
    }

    /** semi-old-school way of allowing controllers to access a manager - via this INSTANCE construct **/
    public static UserBasedLogLevelManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates the UserBasedLogLevelManager - which is a SINGLETON and should only be installed once per VM.
     * 
     * @param threadLocalLogLevelManager
     *            the ThreadLocalLogLevelManager is used to trigger the actual threadlocal based log level controlling
     */
    protected UserBasedLogLevelManager(ThreadLocalLogLevelManager threadLocalLogLevelManager) {
        if (threadLocalLogLevelManager == null) {
            throw new IllegalArgumentException("threadLocalLogLevelManager must not be null");
        }
        this.threadLocalLogLevelManager = threadLocalLogLevelManager;
        INSTANCE = this;
    }

    /** (re)initializes the manager by resetting the map and loading it using the PersistentProperties mechanism **/
    @Override
    public void init() {
        reset();
        String usernameAndLevels = loadUsernameAndLevels();

        if (usernameAndLevels != null) {
            String[] usernameAndLevelArray = usernameAndLevels.split("\r\n");
            for (int i = 0; i < usernameAndLevelArray.length; i++) {
                String aUsernameAndLevel = usernameAndLevelArray[i];
                if (aUsernameAndLevel != null && aUsernameAndLevel.length() > 0 && aUsernameAndLevel.contains("=")) {
                    setLogLevelAndAppender(aUsernameAndLevel);
                }
            }
        }
    }

    /** Loads the username to loglevel/appender map using the PersistentProperties mechanism **/
    public String loadUsernameAndLevels() {
        try {
            return propertiesService.getStringProperty(PropertyLocator.USERNAMES_TO_LEVELS);
        } catch (Exception e) {
            log.warn("loadUsernameAndLevels: Error loading property value " + PropertyLocator.USERNAMES_TO_LEVELS.getPropertyName(), e);
            return null;
        }
    }

    /** Stores the username to loglevel/appender map using the PersistentProperties mechanism **/
    public void storeUsernameAndLevels(String usernameAndLevels) {
        try {
            propertiesService.setProperty(PropertyLocator.USERNAMES_TO_LEVELS, usernameAndLevels);
        } catch (Exception e) {
            log.warn("storeUsernameAndLevels: Error storing property value " + PropertyLocator.USERNAMES_TO_LEVELS.getPropertyName(), e);
        }
    }

    /**
     * Clears the in-memory username to loglevel/appender map - not a full reinit method, use init for that
     * 
     **/
    public void reset() {
        username2LogConfig.clear();
    }

    /**
     * Sets a particular username to a particular loglevel/appender using the format administrator=DEBUG,DebugLog
     * 
     * @param configStr
     *            a one line configuration string in the following format: administrator=DEBUG,DebugLog
     */
    public void setLogLevelAndAppender(String configStr) {
        StringTokenizer st = new StringTokenizer(configStr, "=");
        String username = st.nextToken();
        String logConfig = st.nextToken();
        Level level;
        Appender appender;
        if (logConfig.contains(",")) {
            st = new StringTokenizer(logConfig, ",");
            level = Level.toLevel(st.nextToken());
            String categoryAppenderStr = st.nextToken();
            Logger l = Logger.getLogger(categoryAppenderStr);
            if (l != null) {
                appender = l.getAppender(categoryAppenderStr);
                if (appender == null) {
                    appender = Logger.getRootLogger().getAppender(categoryAppenderStr);
                }
            } else {
                appender = null;
            }
        } else {
            level = Level.toLevel(logConfig);
            appender = null;
        }
        setLogLevelAndAppenderForUsername(username, level, appender);
    }

    /** internal helper method which takes care of the actual modifying of the username to loglevel/appender map **/
    private void setLogLevelAndAppenderForUsername(String username, Priority level, Appender appender) {
        if (level == null && appender == null) {
            username2LogConfig.remove(username);
        } else {
            username2LogConfig.put(username, new LogConfig(level, appender));
        }
    }

    /**
     * Activates the ThreadLocalAwareLogger for a given username.
     * <p>
     * This method is used very frequently and should hence be performant!
     * 
     * @param username
     *            the username for which the ThreadLocalAwareLogger should be enabled if configured to do so
     */
    public void activateUsernameBasedLogLevel(String username) {
        LogConfig logConfig = username2LogConfig.get(username);
        if (logConfig != null) {
            threadLocalLogLevelManager.forceThreadLocalLogLevel(logConfig);
        } else {
            threadLocalLogLevelManager.releaseForcedThreadLocalLogLevel();
        }
    }

    /**
     * Deactivate the ThreadLocalAwareLogger if it was previously activated - does nothing otherwise
     */
    public void deactivateUsernameBasedLogLevel() {
        threadLocalLogLevelManager.releaseForcedThreadLocalLogLevel();
    }

}
