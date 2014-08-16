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
@Component("helloWorldLearnCountServiceMetric")
@ManagedResource(objectName = "org.olat.lms.learn.hello.impl.metric:name=helloWorldLearnCountServiceMetric", description = "Example KPI Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "foo", persistName = "bar")
final public class HelloWorldLearnCountServiceMetric extends HelloWorldLearnServiceMetric {

    @Override
    protected void update(HelloWorldLearnServiceContext serviceContext) {
        incrementCounter();
    }

    @Override
    protected void doUpdate(HelloWorldLearnServiceContext serviceContext) {
        update(serviceContext);
    }

    @ManagedAttribute(description = "Counter of send messages")
    public int getCounter() {
        return counter.intValue();
    }

}
