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
package org.olat.lms.learn.hello.impl.metric;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 08.11.2011 <br>
 * 
 * @author Branislav Balaz
 * 
 *         implementation of concrete ServiceMetric - in concrete service Spring By Type instantiation base on relevant abstract type (HelloWorldLearnServiceMetric). JMX
 *         bean for monitoring.
 * 
 */
@Component("helloWorldLearnAverageFailureServiceMetric")
@ManagedResource(objectName = "org.olat.lms.learn.hello.impl.metric:name=helloWorldLearnAverageFailureServiceMetric", description = "Example KPI Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "foo", persistName = "bar")
final public class HelloWorldLearnAverageFailureServiceMetric extends HelloWorldLearnServiceMetric {

    private String averageFailures;
    private final String percentageString = "%";
    private final AtomicInteger numberOfFailedCalls = new AtomicInteger(0);

    @Override
    protected void update(HelloWorldLearnServiceContext serviceContext) {
        if (serviceContext.isError()) {
            numberOfFailedCalls.incrementAndGet();
        }
        averageFailures = getAverageFailedDelivery(numberOfFailedCalls.get(), counter.get());
    }

    @ManagedAttribute(description = "Average of failed messages")
    public String getAverageFailures() {
        return averageFailures;
    }

    private String getAverageFailedDelivery(int numberOfFailedCalls, int counter) {
        double averageFailedDelivery = ((double) numberOfFailedCalls / (double) counter) * 100;
        return new StringBuilder().append(averageFailedDelivery).append(percentageString).toString();
    }

}
