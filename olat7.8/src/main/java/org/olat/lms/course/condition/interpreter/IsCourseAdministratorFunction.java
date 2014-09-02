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

package org.olat.lms.course.condition.interpreter;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<BR/>
 * a user is course administrator if he/she is in the owner group of the course repository entry of the course
 * <P/>
 * Initial Date: Sep 16, 2004
 * 
 * @author gnaegi
 */
public class IsCourseAdministratorFunction extends AbstractFunction {

    private static final Logger log = LoggerHelper.getLogger();

    public static final String name = "isCourseAdministrator";

    /**
     * Constructor
     * 
     * @param userCourseEnv
     */
    public IsCourseAdministratorFunction(final UserCourseEnvironment userCourseEnv) {
        super(userCourseEnv);
    }

    /**
	 */
    @Override
    public Object call(final Object[] inStack) {
        /*
         * expression check only if cev != null
         */
        final CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
        if (cev != null) {
            // return a valid value to continue with condition evaluation test
            return defaultValue();
        }

        final Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
        final CourseGroupManager cgm = getUserCourseEnv().getCourseEnvironment().getCourseGroupManager();
        OLATResourceable ores = getUserCourseEnv().getCourseEnvironment().getCourseOLATResourceable();
        final boolean isCourseAdmin = cgm.isIdentityCourseAdministrator(ident, ores);
        if (log.isDebugEnabled()) {
            log.debug("identity " + ident.getName() + " , courseadministrator:" + isCourseAdmin + getUserCourseEnv().getCourseEnvironment().getCourseResourceableId());
        }

        return isCourseAdmin ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE;
    }

    @Override
    protected Object defaultValue() {
        return ConditionInterpreter.INT_TRUE;
    }

}
