package org.olat.system.logging.log4j;

import org.apache.log4j.Logger;

/**
 * Helper class for common Log4J interactions
 * 
 * @author Kasper B. Graversen, (c) 2007-2008
 */
public class LoggerHelper {
    /**
     * Returns a Log4J logger configured as the calling class. This ensures copy-paste safe code to get a logger instance, an ensures the logger is always fetched in a
     * consistent manner. <br>
     * <b>usage:</b><br>
     * 
     * <pre>
     * private final static Logger log = LoggerHelper.getLogger();
     * </pre>
     * 
     * Since the logger is found by accessing the call stack it is important, that references are static.
     * <p>
     * The code is JDK1.4 compatible
     * 
     * @since 0.05
     * @return log4j logger instance for the calling class
     * @author Kasper B. Graversen
     */
    public static Logger getLogger() {
        final Throwable t = new Throwable();
        t.fillInStackTrace();
        return Logger.getLogger(t.getStackTrace()[1].getClassName());
    }

}
