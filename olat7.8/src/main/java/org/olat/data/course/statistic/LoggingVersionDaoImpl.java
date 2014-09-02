package org.olat.data.course.statistic;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Default implementation for LogginVersionManager - stores the version in the properties table using the following fields:
 * <ul>
 * <li>category is set to LOGGING_PROPERTIES_CATEGORY</li>
 * <li>name is set to LOGGING_VERSION_PROPERTY_NAME</li>
 * <li>stringValue is set to String.valueOf(version)</li>
 * <li>longValue is set to the unix-millis-time</li>
 * </ul>
 * <P>
 * Initial Date: 04.03.2010 <br>
 * 
 * @author Stefan
 */
public class LoggingVersionDaoImpl extends BasicManager implements LoggingVersionDao {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    /** the category used for logging properties (in the o_properties table) **/
    private static final String LOGGING_PROPERTIES_CATEGORY = "LOGGING_PROPERTIES";

    /** the name used for logging version property (in the o_properties table) **/
    private static final String LOGGING_VERSION_PROPERTY_NAME = "LOGGING_VERSION";

    @Override
    public void setLoggingVersionStartingNow(final int version) {
        setLoggingVersion(version, System.currentTimeMillis());
    }

    @Override
    public void setLoggingVersion(final int version, final long startingTimeMillis) {
        if (version <= 0) {
            throw new IllegalArgumentException("version must be > 1");
        }
        if (startingTimeMillis < 0) {
            throw new IllegalArgumentException("startingTimeMillis must be >= 0");
        }
        final PropertyManager pm = PropertyManager.getInstance();
        final List properties = pm.findProperties(null, null, null, LOGGING_PROPERTIES_CATEGORY, LOGGING_VERSION_PROPERTY_NAME);
        if (properties != null && properties.size() > 0) {
            // when there are already versions defined, lets see if the one which should be set now is already defined
            for (final Iterator it = properties.iterator(); it.hasNext();) {
                final PropertyImpl property = (PropertyImpl) it.next();
                if (property.getStringValue().equals(String.valueOf(version))) {
                    // yes, the version is already set... overwrite this property then
                    log.info("setLoggingVersion: overwriting existing version property for version=" + version + " from starttime " + property.getLongValue() + " to "
                            + startingTimeMillis);
                    property.setLongValue(startingTimeMillis);
                    pm.saveProperty(property);
                    return;
                }
            }
        }

        log.info("setLoggingVersion: setting version property for version=" + version + " to " + startingTimeMillis);
        final PropertyImpl newp = pm.createPropertyInstance(null, null, null, LOGGING_PROPERTIES_CATEGORY, LOGGING_VERSION_PROPERTY_NAME, null, startingTimeMillis,
                String.valueOf(version), null);
        pm.saveProperty(newp);

    }

    @Override
    public long getStartingTimeForVersion(final int version) {
        final PropertyManager pm = PropertyManager.getInstance();
        final List properties = pm.findProperties(null, null, null, LOGGING_PROPERTIES_CATEGORY, LOGGING_VERSION_PROPERTY_NAME);
        if (properties != null && properties.size() > 0) {
            // when there are already versions defined, lets see if the one which should be set now is already defined
            for (final Iterator it = properties.iterator(); it.hasNext();) {
                final PropertyImpl property = (PropertyImpl) it.next();
                if (property.getStringValue().equals(String.valueOf(version))) {
                    return property.getLongValue();
                }
            }
        }
        return -1;
    }

}
