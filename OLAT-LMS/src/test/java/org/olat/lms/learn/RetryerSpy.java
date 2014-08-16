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

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is only needed for testing, it should be set to the TransactionRetryer only in the test environment to record the retries.
 * 
 * Initial Date: 09.04.2012 <br>
 * 
 * @author lavinia
 */
public class RetryerSpy {

    private ConcurrentHashMap<String, Long> concurrentHashMap = new ConcurrentHashMap<String, Long>(); // just for testing

    public void recordRetry(String runtimeExceptionClassName) {
        concurrentHashMap.put(runtimeExceptionClassName, new Long(1));
    }

    public int getRetriesForException(String runtimeExceptionClassName) {
        if (concurrentHashMap.containsKey(runtimeExceptionClassName)) {
            return concurrentHashMap.get(runtimeExceptionClassName).intValue();
        }
        return 0;
    }

    public void clearRetries() {
        concurrentHashMap.clear();
    }

}
