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
package org.olat.lms.core.hello.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.lms.core.CoreBaseService;
import org.olat.lms.core.hello.impl.metric.HelloWorldCoreServiceContext;
import org.olat.lms.core.hello.impl.metric.HelloWorldCoreServiceMetric;
import org.olat.lms.core.hello.service.HelloCoreTransferObjectFactory;
import org.olat.lms.core.hello.service.HelloWorldCoreService;
import org.olat.lms.core.hello.service.MessageCoreTO;
import org.olat.lms.core.hello.service.MessageModelCoreTO;
import org.olat.lms.learn.hello.service.HelloWorldLearnService;
import org.olat.system.commons.service.ServiceContextFactory;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.mail.impl.MailServiceImpl;
import org.olat.system.support.mail.service.SimpleMailTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date: 31.10.2011 <br>
 * 
 * @author guretzki
 */
@Service
public class HelloWorldCoreServiceImpl extends CoreBaseService<HelloWorldCoreServiceMetric, HelloWorldCoreServiceContext> implements HelloWorldCoreService {
    private static final Logger log = LoggerHelper.getLogger();

    // DAO COULD BE USED HERE
    // @Autowired
    // HelloMessageDao helloMessageDao;
    @Autowired
    HelloCoreTransferObjectFactory paramterObjectFactory;
    @Autowired
    MessageBuilderFactory messageBuilderFactory;
    @Autowired
    ServiceContextFactory<HelloWorldCoreServiceContext> helloWorldCoreServiceContextFactory;
    HelloWorldLearnService helloWorldService;

    @Autowired
    MailServiceImpl mailService;

    // ReturnObjectFactory COULD BE USED DIRECTLY IN CORE-SERVICE
    // @Autowired
    // ReturnObjectFactory returnObjectFactory;

    HelloWorldCoreServiceImpl() {

    }

    @Override
    public MessageModelCoreTO sayHelloCore(MessageCoreTO coreMessageParameterObject) {
        log.info("CoreService getMessage start");
        MessageBuilder messageBuilder = messageBuilderFactory.createMessageBuilder(coreMessageParameterObject.getMessage1(), coreMessageParameterObject.getMessage2());
        log.info("CoreService getMessage try to send mail");
        SimpleMailTO mail = SimpleMailTO.getValidInstance("ch_g@bluewin.ch", "olat@id.uzh.ch", "mail Test", "Mail Test Body");
        mailService.sendSimpleMail(mail);
        log.info("CoreService getMessage send mail");
        return messageBuilder.build();
    }

    @Override
    public HelloCoreTransferObjectFactory getParamterObjectFactory() {
        return paramterObjectFactory;
    }

    @Override
    protected void setMetrics(List<HelloWorldCoreServiceMetric> metrics) {

    }

}
