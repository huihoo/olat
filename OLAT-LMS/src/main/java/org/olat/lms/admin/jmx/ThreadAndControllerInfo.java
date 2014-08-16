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
package org.olat.lms.admin.jmx;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.dispatcher.DispatcherAction;

/**
 * Description:<br>
 * TODO:
 * <P>
 * Initial Date: 02.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ThreadAndControllerInfo {
    private static MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

    public int getConcurrentThreads() {
        return DispatcherAction.getConcurrentCounter();
    }

    public int getControllerCount() {
        return DefaultController.getControllerCount();
    }

    public int getAuthenticatedNodeUsersCount() {
        return UserSession.getAuthenticatedUserSessions().size();
    }

    public long getMemoryHeapUsageKB() {
        return mbean.getHeapMemoryUsage().getUsed() / 1024;
    }

    public long getThreadCount() {
        final ThreadGroup tg = Thread.currentThread().getThreadGroup();
        return tg.activeCount();
    }

}
