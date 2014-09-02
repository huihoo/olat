package org.olat.lms.course.auditing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Default implementation of the UserNodeAuditManager - storing the user node logs in the properties table.
 * <p>
 * Note that this has an inherent problem in that the property size is limited yet this class appends to that property constantly.
 * <p>
 * Initial Date: 22.10.2009 <br>
 * 
 * @author Stefan
 */
public class UserNodeAuditManagerImpl extends UserNodeAuditManager {

    protected static final String LOG_DELIMITER = "-------------------------------------------------------------------\n";
    protected static final String LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES = "Removed old log entires because of limited log size\n";
    private final OLATResourceable ores;

    public UserNodeAuditManagerImpl(final ICourse course) {
        ores = course;
    }

    /**
     * java.lang.String)
     */
    @Override
    public void appendToUserNodeLog(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final String logText) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        // Forma log message
        final Date now = new Date();
        final SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String date = sdb.format(now);
        final StringBuilder sb = new StringBuilder();
        sb.append(LOG_DELIMITER);
        sb.append("Date: ").append(date).append("\n");
        sb.append("User: ").append(identity.getName()).append("\n");
        sb.append(logText).append("\n");
        PropertyImpl logProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, LOG_IDENTIFYER);
        if (logProperty == null) {
            logProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, LOG_IDENTIFYER, null, null, null, sb.toString());
            cpm.saveProperty(logProperty);
        } else {
            final String newLog = logProperty.getTextValue() + sb.toString();
            final String limitedLogContent = createLimitedLogContent(newLog, 60000);
            logProperty.setTextValue(limitedLogContent);
            cpm.updateProperty(logProperty);
        }

    }

    protected String createLimitedLogContent(String logContent, final int maxLength) {
        if (logContent.length() < maxLength) {
            return logContent.toString();// nothing to limit
        }
        // too long => limit it by removing first log entries
        while (logContent.length() > maxLength) {
            final int posSecongLogDelimiter = logContent.indexOf(LOG_DELIMITER, LOG_DELIMITER.length());
            logContent = logContent.substring(posSecongLogDelimiter);
        }
        return LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES + logContent;
    }

    /**
	 */
    @Override
    public boolean hasUserNodeLogs(final CourseNode node) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final List<?> logsProperties = cpm.listCourseNodeProperties(node, null, null, LOG_IDENTIFYER);
        return logsProperties.size() == 0 ? false : true;
    }

    /**
	 */
    @Override
    public String getUserNodeLog(final CourseNode courseNode, final Identity identity) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final PropertyImpl property = cpm.findCourseNodeProperty(courseNode, identity, null, LOG_IDENTIFYER);
        if (property == null) {
            return null;
        }
        final String result = property.getTextValue();
        return result;
    }

}
