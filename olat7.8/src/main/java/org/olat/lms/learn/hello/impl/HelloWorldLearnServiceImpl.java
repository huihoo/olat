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
package org.olat.lms.learn.hello.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.lms.core.hello.service.HelloWorldCoreService;
import org.olat.lms.core.hello.service.MessageCoreTO;
import org.olat.lms.core.hello.service.MessageModelCoreTO;
import org.olat.lms.learn.LearnBaseService;
import org.olat.lms.learn.hello.impl.metric.HelloWorldLearnServiceContext;
import org.olat.lms.learn.hello.impl.metric.HelloWorldLearnServiceMetric;
import org.olat.lms.learn.hello.service.HelloTransferObjectFactory;
import org.olat.lms.learn.hello.service.HelloWorldLearnService;
import org.olat.lms.learn.hello.service.MessageModelTO;
import org.olat.lms.learn.hello.service.MessageTO;
import org.olat.system.commons.service.ServiceContextFactory;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 31.10.2011 <br>
 * 
 * @author guretzki
 * 
 *         concrete implementation of learn service. class must use relevant parametrized types for correct method overriding (like setMetrics). Uses Spring By Type
 *         autowiring in overrided method setMetrics
 * 
 */
@Service
public class HelloWorldLearnServiceImpl extends LearnBaseService<HelloWorldLearnServiceMetric, HelloWorldLearnServiceContext> implements HelloWorldLearnService {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    HelloWorldCoreService helloWorldCoreService;
    @Autowired
    HelloTransferObjectFactory parameterObjectFactory;
    @Autowired
    HelloMessageModelTOFactory returnObjectFactory;
    @Autowired
    ServiceContextFactory<HelloWorldLearnServiceContext> helloWorldLearnServiceContextFactory;

    HelloWorldLearnServiceImpl() {

    }

    @Override
    public MessageModelTO sayHello(MessageTO messagePO) {
        log.info("LearningService sayHello start...");
        MessageCoreTO coreMessagePO = helloWorldCoreService.getParamterObjectFactory().createCoreMessageTransferObject(messagePO.getMessage1(), messagePO.getMessage2());
        MessageModelCoreTO helloMessage = new MessageModelCoreTO("");
        boolean isException = false;
        try {
            helloMessage = helloWorldCoreService.sayHelloCore(coreMessagePO);
        } catch (Exception e) {
            log.error(e);
            isException = true;
        } finally {
            doNotify(isException);
        }

        return returnObjectFactory.createMessageModelTransferObject(helloMessage.getMessage());
    }

    @Override
    public HelloTransferObjectFactory getTransferObjectFactory() {
        return parameterObjectFactory;
    }

    @Autowired
    @Override
    protected void setMetrics(List<HelloWorldLearnServiceMetric> metrics) {
        for (HelloWorldLearnServiceMetric metric : metrics) {
            attach(metric);
        }
    }

    private void doNotify(boolean isException) {
        Map<String, Object> helloWorldLearnServiceContextMap = new HashMap<String, Object>();
        helloWorldLearnServiceContextMap.put(HelloWorldLearnServiceContext.HelloWorldLearnServiceContextKeys.IS_ERROR.name(), new Boolean(isException));
        notifyMetrics(helloWorldLearnServiceContextFactory.getServiceContext(helloWorldLearnServiceContextMap));
    }

}
