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
package org.olat.lms.activitylogging;

import org.olat.system.event.EventLogger;
import org.olat.system.event.GenericEventListener;
import org.springframework.stereotype.Component;

/**
 * 
 * for EventLoggerImpl
 * 
 * <P>
 * Initial Date: 05.07.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class EventLoggerImpl implements EventLogger {

    /**
     * [spring]
     */
    private EventLoggerImpl() {
        //
    }

    /**
     * @see org.olat.system.event.EventLogger#runAndLog(java.lang.Runnable, org.olat.system.event.GenericEventListener)
     */
    @Override
    public void runAndLog(Runnable runnable, GenericEventListener listener) {
        if (listener != null) {
            ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(runnable, UserActivityLoggerImpl.newLoggerForEventBus(listener));
        } else {
            ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(runnable, ThreadLocalUserActivityLoggerInstaller.createEmptyUserActivityLogger());
        }

    }

    @Override
    public void initEmptyLogger() {
        ThreadLocalUserActivityLoggerInstaller.initEmptyUserActivityLogger();
    }

}
