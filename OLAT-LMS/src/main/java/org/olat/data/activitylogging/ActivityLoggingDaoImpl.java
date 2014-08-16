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
 * Copyright (c) 1999-2009 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.data.activitylogging;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 
 * 
 * @author Christian Guretzki
 */
@Repository
public class ActivityLoggingDaoImpl implements ActivityLoggingDao {
    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    protected ActivityLoggingDaoImpl() {
    }

    @Autowired
    DB db;

    public void updateDuration(LoggingObject lastLogObj, long duration) {
        if (db != null && db.isError()) {
            // then we would run into an ERROR when we'd do more with this DB
            // hence we just issue a log.info here with the details
            // @TODO: lower to log.info once we checked that it doesn't occur very often (best for 6.4)
            log.warn("log: DB is in Error state therefore the UserActivityLoggerImpl cannot update the simpleDuration of log_id " + lastLogObj.getKey() + " with value "
                    + duration + ", loggingObject: " + lastLogObj);
        } else {
            DBQuery update = db.createQuery("update LoggingObject set simpleDuration = :duration where log_id = :logid");
            update.setLong("duration", duration);
            update.setLong("logid", lastLogObj.getKey());
            // we have to do FlushMode.AUTO (which is the default anyway)
            update.executeUpdate(FlushMode.AUTO);
        }
    }

    public void saveLogObject(LoggingObject logObj) {
        // and store it
        if (db != null && db.isError()) {
            // then we would run into an ERROR when we'd do more with this DB
            // hence we just issue a log.info here with the details
            // @TODO: lower to log.info once we checked that it doesn't occur very often (best for 6.4)
            log.warn("log: DB is in Error state therefore the UserActivityLoggerImpl cannot store the following logging action into the loggingtable: " + logObj);
        } else {
            db.saveObject(logObj);
        }
    }

}
