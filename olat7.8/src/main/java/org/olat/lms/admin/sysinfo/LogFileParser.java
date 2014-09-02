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

package org.olat.lms.admin.sysinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.SystemPropertiesLoader;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Component;

/**
 * Description: logfile handling
 * 
 * @author Sabina Jeger
 */
@Component
public class LogFileParser {

    private final Logger log = LoggerHelper.getLogger();
    private String logfilepathBase;
    private int linecount = 40; // default number of lines
    private String filename = "olat.log";

    /**
     * [spring]
     */
    @SuppressWarnings("unused")
    private LogFileParser(String configuredLogDir, String configuredUserDataDir, int linecount) {
        this.linecount = linecount;
        String logDir = configuredLogDir;
        if (!StringHelper.containsNonWhitespace(logDir)) {
            // if logDir is not configured use default: ${userdata.dir}/logs
            String userDataDir = configuredUserDataDir;
            if (!StringHelper.containsNonWhitespace(userDataDir)) {
                userDataDir = SystemPropertiesLoader.USERDATA_DIR_DEFAULT;
            }

            logDir = userDataDir + File.separator + "logs";
        }

        if (logDir.endsWith(File.separator)) {
            logfilepathBase = logDir + filename;
        } else {
            logfilepathBase = logDir + File.separator + filename;
        }
    }

    /**
     * just for testing
     */
    protected LogFileParser(String logfilepathBase, int linecount) {
        this.logfilepathBase = logfilepathBase;
        this.linecount = linecount;
    }

    /**
     * @param date
     *            the date of the log to retrieve, or null when no date suffix should be appended (= take today's log)
     * @return the VFSLeaf of the Logfile given the Date, or null if no such file could be found
     */
    public VFSLeaf getLogfilePath(Date date) {
        if (date != null) {
            SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd");
            String suffix = sdb.format(date);
            filename += "." + suffix;
        }
        File logf = new File(filename);
        if (!logf.exists())
            return null;
        return new LocalFileImpl(logf);
    }

    /**
     * @param errorNumber
     * @param dd
     * @param mm
     * @param yyyy
     * @param asHTML
     * @return
     */
    public Collection<String> getErrorToday(String errorNumber) {
        Date d = new Date();
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        SimpleDateFormat month = new SimpleDateFormat("MM");
        SimpleDateFormat day = new SimpleDateFormat("dd");
        return getError(errorNumber, day.format(d), month.format(d), year.format(d), false);
    }

    /**
     * looks through the logfile
     * 
     * @param s
     * @param dd
     *            requested day
     * @param mm
     *            requested month
     * @param yyyy
     *            requested yyyy
     * @return the first found errormessage
     */
    public Collection<String> getError(String errorNumber, String dd, String mm, String yyyy, boolean asHTML) {

        if (logfilepathBase == null) {
            // this is null when olat is setup with an empty olat.local.properties file and no log.dir path is set.
            return Collections.emptyList();
        }
        String logfilepath = getLogFilePath(dd, mm, yyyy);

        return getError(errorNumber, logfilepath, asHTML);
    }

    protected Collection<String> getError(String errorNumber, String logfilepath, boolean asHTML) {
        String line;

        Collection<String> errormsg = new ArrayList<String>();

        int counter = linecount;
        int founderror = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(logfilepath));
            while ((line = br.readLine()) != null && counter > 0) {
                if (line.matches(getMatchErrorRegex(errorNumber))) {
                    founderror++;
                }
                if (founderror > 0) {
                    if (asHTML) {
                        errormsg.add(formatAsHTML(line));
                    } else {
                        errormsg.add(line);
                    }
                    // System.out.println("------------- added log line --- counter: " + counter + " line: " + line);
                    counter--;
                }
            }

            br.close();
            return errormsg;
        } catch (FileNotFoundException e) {
            throw new OLATRuntimeException("Could not read OLAT-log file at " + logfilepath
                    + " [hint: Check if olat.log file exist, check log4j.xml configuration, 'log.dir' variable in olat.local.properties]", e);
        } catch (IOException e) {
            throw new OLATRuntimeException("error reading OLAT error log at " + logfilepath
                    + " [hint: Check if olat.log file exist, check log4j.xml configuration, 'log.dir' variable in olat.local.properties]", e);
        }
    }

    private String formatAsHTML(String inputString) {
        return inputString + "</br>";
    }

    private String getMatchErrorRegex(String errorNumber) {
        String matchError = ".*" + "ERROR" + ".*" + errorNumber + ".*";
        return matchError;
    }

    private String getLogFilePath(String dd, String mm, String yyyy) {
        Date now = new Date();
        String reqdate = yyyy + "-" + mm + "-" + dd;
        SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdb.format(now);
        String logfilepath = null;
        if (today.equals(reqdate) == false) {
            logfilepath = logfilepathBase + "." + yyyy + "-" + mm + "-" + dd;
        } else {
            logfilepath = logfilepathBase;
        }
        log.info("logfilepath changed to " + logfilepath + " (" + today + "|" + reqdate + ")");
        return logfilepath;
    }
}
