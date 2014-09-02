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
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;

/**
 * Description:<br>
 * Function to get the number of attempts a user tried to solve a test, do a questionnaire and alike. Meaning may be different on the various course building blocks.
 * <p>
 * 
 * @author gnaegi Initial Date: Oct 26, 2004
 */
public class GetAttemptsFunction extends AbstractFunction {

    public static final String name = "getAttempts";

    /**
     * Default constructor to use the get attempts object
     * 
     * @param userCourseEnv
     */
    public GetAttemptsFunction(final UserCourseEnvironment userCourseEnv) {
        super(userCourseEnv);
    }

    /**
	 */
    @Override
    public Object call(final Object[] inStack) {
        /*
         * argument check
         */
        if (inStack.length > 1) {
            return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
                    "solution.provideone.nodereference"));
        } else if (inStack.length < 1) {
            return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "", "error.moreargs",
                    "solution.provideone.nodereference"));
        }
        /*
         * argument type check
         */
        if (!(inStack[0] instanceof String)) {
            return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "", "error.argtype.coursnodeidexpeted",
                    "solution.example.node.infunction"));
        }
        final String nodeId = (String) inStack[0];
        /*
         * check reference integrity
         */
        final CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
        if (cev != null) {
            if (!cev.existsNode(nodeId)) {
                return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, nodeId, "error.notfound.coursenodeid",
                        "solution.copypastenodeid"));
            }
            // remember the reference to the node id for this condtion
            cev.addSoftReference("courseNodeId", nodeId);
            // return a valid value to continue with condition evaluation test
            return defaultValue();
        }

        /*
         * the real function evaluation which is used during run time
         */
        final CourseNode node = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(nodeId);
        final AssessmentManager am = getUserCourseEnv().getCourseEnvironment().getAssessmentManager();
        final Identity identity = getUserCourseEnv().getIdentityEnvironment().getIdentity();

        return am.getNodeAttempts(node, identity);
    }

    /**
	 */
    @Override
    protected Object defaultValue() {
        return new Integer(Integer.MIN_VALUE);
    }

}
