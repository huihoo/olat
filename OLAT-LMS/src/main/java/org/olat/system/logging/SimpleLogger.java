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
package org.olat.system.logging;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.core.Ordered;

/**
 * Experimental class as logging advice.
 * 
 * Initial Date: 06.03.2012 <br>
 * 
 * @author lavinia
 */
public class SimpleLogger implements Ordered {

    private static final Logger log = LoggerHelper.getLogger();
    private int order;

    private int maxRetries = 1;

    public void logOperation() {
        System.out.println("LOG OPERATION - with System.out");
        log.info("LOG OPERATION - with logger");
    }

    public void doAroundOperation(final ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("LOG doAroundOperation - with System.out");
        log.info("LOG doAroundOperation - with logger");
        if (maxRetries == 1) {
            pjp.proceed();
        }
    }

    // allows us to control the ordering of advice
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
