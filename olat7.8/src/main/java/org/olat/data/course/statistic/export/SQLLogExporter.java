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

package org.olat.data.course.statistic.export;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * ICourseLogExporter used for the case where a separate DB should be used to retrieve the o_loggingtable.
 * <p>
 * This would be a non-standard situation
 * <P>
 * Initial Date: 06.01.2010 <br>
 * 
 * @author Stefan
 */
public class SQLLogExporter implements ICourseLogExporter {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    private SessionFactory sessionFactory_;

    private String anonymizedUserSql_;
    private String nonAnonymizedUserSql_;

    public SQLLogExporter() {
        // this empty constructor is ok - instantiated via spring
    }

    /** set via spring **/
    public void setAnonymizedUserSql(final String anonymizedUserSql) {
        anonymizedUserSql_ = anonymizedUserSql;
    }

    /** set via spring **/
    public void setNonAnonymizedUserSql(final String nonAnonymizedUserSql) {
        nonAnonymizedUserSql_ = nonAnonymizedUserSql;
    }

    /** set via spring **/
    public void setSessionFactory(final SessionFactory sessionFactory) {
        sessionFactory_ = sessionFactory;
    }

    /**
     * @TODO: charSet is currently ignored!!!!!
     */
    @Override
    public void exportCourseLog(final File outFile, final String charSet, final Long resourceableId, final Date begin, Date end, final boolean resourceAdminAction,
            final boolean anonymize) {
        log.info("exportCourseLog: BEGIN outFile=" + outFile + ", charSet=" + charSet + ", resourceableId=" + resourceableId + ", begin=" + begin + ", end=" + end
                + ", resourceAdminAction=" + resourceAdminAction + ", anonymize=" + anonymize);
        try {
            if (!outFile.exists()) {
                if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Cannot create parent of OutFile " + outFile.getAbsolutePath());
                }
                if (!outFile.createNewFile()) {
                    throw new IllegalArgumentException("Cannot create outFile " + outFile.getAbsolutePath());
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Cannot create outFile " + outFile.getAbsolutePath());
        }
        if (!outFile.delete()) {
            throw new IllegalStateException("Could not delete temporary outfile " + outFile.getAbsolutePath());
        }

        // try to make sure the database can write into this directory
        if (!outFile.getParentFile().setWritable(true, false)) {
            log.warn("exportCourseLog: COULD NOT SET DIR TO WRITEABLE: " + outFile.getParent());
        }

        String query = String.valueOf(anonymize ? anonymizedUserSql_ : nonAnonymizedUserSql_);
        if (begin != null) {
            query = query.concat(" AND (v.creationDate >= :createdAfter)");
        }
        if (end != null) {
            query = query.concat(" AND (v.creationDate <= :createdBefore)");
        }

        final Session session = sessionFactory_.openSession();
        final long startTime = System.currentTimeMillis();
        try {
            session.beginTransaction();
            final Query dbQuery = session.createSQLQuery(query);

            dbQuery.setBoolean("resAdminAction", resourceAdminAction);
            dbQuery.setString("resId", Long.toString(resourceableId));
            if (begin != null) {
                dbQuery.setDate("createdAfter", begin);
            }
            if (end != null) {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(end);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                end = cal.getTime();
                dbQuery.setDate("createdBefore", end);
            }

            dbQuery.setString("outFile", outFile.getAbsolutePath());

            dbQuery.scroll();
        } catch (final RuntimeException e) {
            e.printStackTrace(System.out);
        } catch (final Error er) {
            er.printStackTrace(System.out);
        } finally {
            if (session != null) {
                session.close();
            }
            final long diff = System.currentTimeMillis() - startTime;
            log.info("exportCourseLog: END DURATION=" + diff + ", outFile=" + outFile + ", charSet=" + charSet + ", resourceableId=" + resourceableId + ", begin="
                    + begin + ", end=" + end + ", resourceAdminAction=" + resourceAdminAction + ", anonymize=" + anonymize);
        }
    }

}
