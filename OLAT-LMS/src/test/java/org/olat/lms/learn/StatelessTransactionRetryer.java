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
package org.olat.lms.learn;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * just for testing, it allows to record/spy retries.
 * 
 * 
 * Initial Date: 17.04.2012 <br>
 * 
 * @author lavinia
 */
public class StatelessTransactionRetryer extends TransactionRetryer {
    static final Logger log = LoggerHelper.getLogger();

    // just for testing
    private RetryerSpy retryerSpy;

    public RetryerSpy getRetryerSpy() {
        return retryerSpy;
    }

    public void setRetryerSpy(RetryerSpy retryerSpy) {
        this.retryerSpy = retryerSpy;
    }

    @Override
    void recordRetry(String runtimeExceptionClassName, RuntimeException ex) {
        log.info("catch " + runtimeExceptionClassName + " and retry, exception:", ex);
        if (retryerSpy != null) {
            log.info("recordRetry");
            retryerSpy.recordRetry(runtimeExceptionClassName);
        }
    }

}
