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

package org.olat.system.coordinate.jms;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.security.OLATPrincipal;
import org.olat.test.OlatTestCase;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class JMSITCase extends OlatTestCase {
    private OLATPrincipal olatPrincipal;
    private OLATResourceable ores1;

    private Event event = null;

    @Before
    public void setup() throws Exception {
        olatPrincipal = Mockito.mock(OLATPrincipal.class);
        ores1 = Mockito.mock(OLATResourceable.class);
    }

    /**
	 * 
	 */
    @Test
    public void testSendReceive() {
        // enable test only if we have the cluster configuration enabled.
        // this test requires that an JMS Provider is running
        // (see file serviceconfig/org/olat/core/_spring/coreextconfig.xml)
        final EventBus bus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
        // TODO: 1.6.2011/cg TODO for Event-Refactoring should not must check for cluster-mode
        if (CoordinatorManager.getInstance().getCoordinator().isClusterMode()) {
            // send and wait some time until a message should arrive at the latest.
            ores1 = OresHelper.createOLATResourceableInstance("hellojms", new Long(123));

            bus.registerFor(new GenericEventListener() {

                @Override
                public void event(final Event event) {
                    // TODO Auto-generated method stub
                    System.out.println("event received!" + event);
                    JMSITCase.this.event = event;
                }

                /**
                 * @see org.olat.system.event.GenericEventListener#isControllerAndNotDisposed()
                 */
                @Override
                public boolean isControllerAndNotDisposed() {
                    return false;
                }

            }, olatPrincipal, ores1);

            final MultiUserEvent mue = new MultiUserEvent("amuecommand");
            bus.fireEventToListenersOf(mue, ores1);
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                fail("InterruptedException=" + e);
            }
            assertNotNull("after 2 secs, an answer from the jms should have arrived", event);

        }
        // else no tests to pass here
    }

    @After
    public void tearDown() throws Exception {

    }

}
