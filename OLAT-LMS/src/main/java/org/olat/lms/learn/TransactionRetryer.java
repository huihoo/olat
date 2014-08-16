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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.mail.MailSendException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Declarative Retry Support Advice. <br/>
 * It retries operations failed because of a transient exception. <br/>
 * A transient exception is an non-deterministic exception, e.g. a concurrency exception.
 * 
 * This advice should be used (lmsContext.xml) as custom interceptor/advisor for transaction retry. <br/>
 * (See: http://static.springsource.org/spring/docs/3.0.5.RELEASE/reference/transaction.html#transaction-declarative-applying-more-than-just-tx-advice, and
 * http://static.springsource.org/spring/docs/3.0.5.RELEASE/reference/transaction.html#tx-decl-explained)
 * 
 * Initial Date: 06.03.2012 <br>
 * 
 * @author lavinia
 */

@Component
@Scope("prototype")
public class TransactionRetryer implements Ordered {

    private static final Logger log = LoggerHelper.getLogger();

    int order; // allows us to control the ordering of advice

    Map<String, Long> maxRetriesPerException;

    public void setMaxRetriesPerException(Map<String, Long> maxRetriesPerException) {
        this.maxRetriesPerException = maxRetriesPerException;
    }

    /**
     * Calls the ProceedingJoinPoint as many times as we configured via maxRetries, <br/>
     * if and only if we catch the specified runtime exception(s). <br/>
     * If the exception(s) is still catched after the maxRetries times, the exception if thrown further.
     */
    public Object retry(final ProceedingJoinPoint call) throws Throwable {

        Throwable exception = new Throwable("oops");
        Map<String, Long> retriesPerException = new HashMap<String, Long>();
        while (isRetryStillAllowed(retriesPerException)) {
            try {
                log.info("TransactionRetryer intercepted the call");
                return (call.proceed());

            } catch (final ConstraintViolationException ex) {
                // delay
                try {
                    Thread.sleep(1000); // do we want to tune the delay?
                } catch (InterruptedException e) {
                    log.error(e);
                }
                recordRetry("ConstraintViolationException", ex);
                addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
                exception = ex;
                // retry
            } catch (final StaleObjectStateException ex) {
                // delay
                try {
                    Thread.sleep(1000); // do we want to tune the delay?
                } catch (InterruptedException e) {
                    log.error(e);
                }
                recordRetry("StaleObjectStateException", ex);
                addOrIncrementRetries(retriesPerException, StaleObjectStateException.class.getName());
                exception = ex;
                // retry
            } catch (final HibernateOptimisticLockingFailureException ex) {
                // delay
                try {
                    Thread.sleep(1000); // do we want to tune the delay?
                } catch (InterruptedException e) {
                    log.error(e);
                }
                recordRetry("HibernateOptimisticLockingFailureException", ex);
                addOrIncrementRetries(retriesPerException, HibernateOptimisticLockingFailureException.class.getName());
                exception = ex;
                // retry
            } catch (final MailSendException ex) {
                // delay
                try {
                    Thread.sleep(1000); // do we want to tune the delay?
                } catch (InterruptedException e) {
                    log.error(e);
                }
                recordRetry("MailSendException", ex);
                addOrIncrementRetries(retriesPerException, MailSendException.class.getName());
                exception = ex;
                // retry
            } catch (Exception ex) { // find out whether there are more exceptions to catch for a retry
                log.info("catch unknown exception: ", ex);
                throw ex;
            }
        }

        // the catched exception is thrown further, if the isRetryStillAllowed returns false
        throw (exception);
    }

    /**
     * Proofs if the retriesPerException, for any exception, have not reached the upper limit (maxRetriesPerException).
     * 
     */
    boolean isRetryStillAllowed(Map<String, Long> retriesPerException) {
        boolean veto = false;
        Iterator<String> keys = maxRetriesPerException.keySet().iterator();
        while (keys.hasNext()) {
            String currentException = keys.next();
            Long maxRetries = maxRetriesPerException.get(currentException);
            if (retriesPerException.get(currentException) != null
                    && (retriesPerException.get(currentException).equals(maxRetries) || retriesPerException.get(currentException).longValue() > maxRetries.longValue())) {
                veto = true;
            }
            if (veto)
                break;
        }
        return !veto;
    }

    /**
     * Increments the retries for this runtimeExceptionClassName in this retriesPerException map.
     */
    void addOrIncrementRetries(Map<String, Long> retriesPerException, String runtimeExceptionClassName) {
        if (!retriesPerException.containsKey(runtimeExceptionClassName)) {
            retriesPerException.put(runtimeExceptionClassName, new Long(0)); // starts with 0
        } else {
            Long retries = retriesPerException.get(runtimeExceptionClassName);
            retries += 1;
            retriesPerException.put(runtimeExceptionClassName, retries);
        }
    }

    void recordRetry(String runtimeExceptionClassName, RuntimeException ex) {
        log.info("catch " + runtimeExceptionClassName + " and retry, exception:", ex);
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
