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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;

/**
 * ICourseLogExporter used for the case where a separate DB should be used to retrieve the o_loggingtable.
 * <p>
 * This would be a non-standard situation specifically for a mysql/stored procedure setup
 * <P>
 * Initial Date: 06.01.2010 <br>
 * 
 * @author Stefan
 */
public class MySQLStoredProcedureLogExporter implements ICourseLogExporter {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    private JdbcTemplate jdbcTemplate_;

    public MySQLStoredProcedureLogExporter() {
        // this empty constructor is ok - instantiated via spring
    }

    /** set via spring **/
    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate_ = jdbcTemplate;
    }

    /**
     * @TODO: charSet is currently ignored!!!!!
     */
    @Override
    public void exportCourseLog(final File outFile, final String charSet, final Long resourceableId, final Date begin, final Date end, final boolean resourceAdminAction,
            final boolean anonymize) {
        final long startTime = System.currentTimeMillis();
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

        try {
            final List<SqlParameter> emptyOutParams = new LinkedList<SqlParameter>();

            // we ignore the result of the stored procedure
            jdbcTemplate_.call(new CallableStatementCreator() {

                @Override
                public CallableStatement createCallableStatement(final Connection con) throws SQLException {
                    final CallableStatement cs = con.prepareCall("call olatng.o_logging_export(?,?,?,?,?,?)");
                    cs.setString(1, outFile.getAbsolutePath());
                    cs.setBoolean(2, resourceAdminAction);
                    cs.setString(3, Long.toString(resourceableId));
                    cs.setBoolean(4, anonymize);
                    cs.setTimestamp(5, begin == null ? null : new Timestamp(begin.getTime()));
                    cs.setTimestamp(6, end == null ? null : new Timestamp(end.getTime()));
                    MySQLStoredProcedureLogExporter.log.info("exportCourseLog: executing stored procedure right about now");
                    return cs;
                }

            }, emptyOutParams);

            log.info("exportCourseLog: adding header...");
            final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(outFile));
            final File tmpOutFile = new File(outFile.getParent(), "tmp_" + outFile.getName());
            final BufferedOutputStream bos = FileUtils.getBos(tmpOutFile);
            bos.write(("creationDate, username, actionVerb, actionObject, greatGrandParent, grandParent, parent, target" + System.getProperty("line.separator"))
                    .getBytes(StringHelper.check4xMacRoman(charSet)));

            FileUtils.cpio(bis, bos, "exportCourseLogCSV");

            bos.flush();
            bos.close();
            bis.close();

            outFile.delete();
            tmpOutFile.renameTo(outFile);

        } catch (final RuntimeException e) {
            log.error("exportCourseLog: runtime exception ", e);
        } catch (final Error er) {
            log.error("exportCourseLog: error ", er);
        } catch (final FileNotFoundException e) {
            log.error("exportCourseLog: FileNotFoundException: ", e);
        } catch (final IOException e) {
            log.error("exportCourseLog: IOException: ", e);
        } finally {
            final long diff = System.currentTimeMillis() - startTime;
            log.info("exportCourseLog: END DURATION=" + diff);
        }
    }

}
