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
package org.olat.test.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * OLAT-6437 This is an example how to use and test the scheduler system from spring which is annotation configured. You can do fixed rate scheduled task and also cronjob
 * like scheduled tasks.
 * <P>
 * Initial Date: 03.08.2011 <br>
 * 
 * @author guido
 */

@ContextConfiguration(locations = { "classpath*:/org/olat/test/scheduler/simpleScheduler.xml" })
public class AnnotationSchedulerITCase extends AbstractJUnit4SpringContextTests {

    @Test
    public void testAnnotationScheduler() {

        Scheduler scheduler = applicationContext.getBean(Scheduler.class);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(42, Scheduler.result);
    }

}
