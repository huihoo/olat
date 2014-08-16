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
package org.olat.system.support.hello.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.system.commons.service.ServiceContextFactory;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.SupportBaseService;
import org.olat.system.support.hello.impl.metric.HelloWorldSupportServiceContext;
import org.olat.system.support.hello.impl.metric.HelloWorldSupportServiceMetric;
import org.olat.system.support.hello.service.HelloWorldSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 31.10.2011 <br>
 * 
 * @author guretzki
 */
@Service
public class HelloWorldSupportServiceImpl extends SupportBaseService<HelloWorldSupportServiceMetric, HelloWorldSupportServiceContext> implements HelloWorldSupportService {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    ServiceContextFactory<HelloWorldSupportServiceContext> helloWorldSupportServiceContextFactory;

    HelloWorldSupportServiceImpl() {

    }

    @Override
    public String sayHello(String parameter) {
        return "Hello";
    }

    @Override
    protected void setMetrics(List<HelloWorldSupportServiceMetric> metrics) {

    }

}
