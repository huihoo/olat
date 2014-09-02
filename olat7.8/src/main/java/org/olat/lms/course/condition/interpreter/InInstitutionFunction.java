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

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.user.UserService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Courseinterpreter method to check if a user is in a given institution
 * <P>
 * Initial Date: March 05 2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class InInstitutionFunction extends AbstractFunction {

    public static final String name = "inInstitution";

    /**
     * @param userCourseEnv
     */
    public InInstitutionFunction(final UserCourseEnvironment userCourseEnv) {
        super(userCourseEnv);
    }

    /**
	 */
    @Override
    public Object call(final Object[] inStack) {/*
                                                 * argument check
                                                 */
        if (inStack.length > 1) {
            return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
                    "solution.provideone.institutionalname"));
        } else if (inStack.length < 1) {
            return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "", "error.moreargs",
                    "solution.provideone.institutionalname"));
        }
        /*
         * argument type check
         */
        if (!(inStack[0] instanceof String)) {
            return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "", "error.argtype.institutionalname",
                    "solution.example.institutionalname.infunction"));
        }
        final String configInstname = (String) inStack[0];
        /*
         * expression check only if cev != null
         */
        final CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
        if (cev != null) {
            // return a valid value to continue with condition evaluation test
            return defaultValue();
        }

        final Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
        final String instName = getUserService().getUserProperty(ident.getUser(), UserConstants.INSTITUTIONALNAME,
                getUserCourseEnv().getIdentityEnvironment().getLocale());

        return instName.equals(configInstname) ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE;
    }

    @Override
    protected Object defaultValue() {
        return ConditionInterpreter.INT_TRUE;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
