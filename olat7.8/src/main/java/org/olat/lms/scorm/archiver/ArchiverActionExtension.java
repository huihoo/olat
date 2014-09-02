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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.scorm.archiver;

import org.olat.lms.course.ICourse;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.extensions.action.GenericActionExtension;
import org.olat.presentation.scorm.archiver.ScormResultArchiveController;

public class ArchiverActionExtension extends GenericActionExtension {

    protected ArchiverActionExtension() {
    }

    @Override
    public Controller createController(final UserRequest ureq, final WindowControl wControl, final Object arg) {
        if (arg instanceof ICourse) {
            return new ScormResultArchiveController(ureq, wControl, (ICourse) arg);
        }
        return super.createController(ureq, wControl, arg);
    }
}
