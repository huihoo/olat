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

package org.olat.lms.calendar;

import java.io.File;

import org.olat.connectors.webdav.WebDAVProvider;
import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.CalendarDao;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VirtualContainer;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.system.spring.CoreSpringFactory;

public class CalendarWebDAVProvider implements WebDAVProvider {

    private static final String MOUNT_POINT = "calendars";

    @Override
    public VFSContainer getContainer(final Identity identity) {
        final VirtualContainer calendars = new VirtualContainer("calendars");
        calendars.setLocalSecurityCallback(new ReadOnlyCallback());
        // get private calendar
        final File fPersonalCalendar = getCalendarService().getCalendarFile(CalendarDao.TYPE_USER, identity.getName());
        calendars.addItem(new LocalFileImpl(fPersonalCalendar));
        return calendars;
    }

    @Override
    public String getMountPoint() {
        return MOUNT_POINT;
    }

    private CalendarService getCalendarService() {
        return CoreSpringFactory.getBean(CalendarService.class);
    }
}
