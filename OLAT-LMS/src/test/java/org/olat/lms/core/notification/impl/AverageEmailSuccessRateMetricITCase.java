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
package org.olat.lms.core.notification.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.notification.DaoObjectMother;
import org.olat.lms.core.notification.impl.metric.AverageEmailSuccessRateMetric;
import org.olat.lms.core.notification.impl.metric.NotificationServiceContext;
import org.olat.lms.core.notification.impl.metric.NotificationServiceMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: 23.12.2011 <br>
 * 
 * @author Branislav Balaz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class AverageEmailSuccessRateMetricITCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private NotificationServiceImpl notificationServiceImpl;
    @Autowired
    private DaoObjectMother daoObjectMother;
    @Autowired
    UriBuilder uriBuilder;

    @Before
    public void setup() {
        uriBuilder.serverContextPathURI = "/test";
    }

    @Test
    public void notifySubscribers_AverageEmailSuccesRateThreeIdentitiesOnePublisherOneEvent() {
        daoObjectMother.createThreeIdentities_subscribe_publishOneEvent(notificationServiceImpl);
        notificationServiceImpl.notifySubscribers();
        List<NotificationServiceMetric<NotificationServiceContext>> metrics = notificationServiceImpl.getMetrics();
        double rate = 0;
        for (NotificationServiceMetric<?> metric : metrics) {
            if (metric instanceof AverageEmailSuccessRateMetric) {
                rate = ((AverageEmailSuccessRateMetric) metric).getAverageEmailSuccessRate();
            }
        }
        assertTrue(rate == 1.0d);
    }
}
