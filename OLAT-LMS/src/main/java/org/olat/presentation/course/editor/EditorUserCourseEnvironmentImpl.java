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

package org.olat.presentation.course.editor;

import java.util.Map;

import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.scoring.ScoreAccounting;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * TODO: patrick Class Description for EditorUserCourseEnvironment
 * <P>
 * Initial Date: Jul 6, 2005 <br>
 * 
 * @author patrick
 */
public class EditorUserCourseEnvironmentImpl implements UserCourseEnvironment {

    private final CourseEditorEnv courseEditorEnv;
    private final ConditionInterpreter ci;
    private final ScoreAccounting sa;

    EditorUserCourseEnvironmentImpl(final CourseEditorEnv courseEditorEnv) {
        this.courseEditorEnv = courseEditorEnv;
        this.ci = new ConditionInterpreter(this);
        this.courseEditorEnv.setConditionInterpreter(ci);
        this.sa = new ScoreAccounting(this);
    }

    /**
	 */
    @Override
    public CourseEnvironment getCourseEnvironment() {
        throw new AssertException("should never be called since it is the EDITOR user course environment");
    }

    /**
	 */
    @Override
    public CourseEditorEnv getCourseEditorEnv() {
        return courseEditorEnv;
    }

    /**
	 */
    @Override
    public ConditionInterpreter getConditionInterpreter() {
        return ci;
    }

    /**
	 */
    @Override
    public IdentityEnvironment getIdentityEnvironment() {
        throw new AssertException("should never be called since it is the EDITOR user course environment");
    }

    /**
	 */
    @Override
    public ScoreAccounting getScoreAccounting() {
        return sa;
    }

    @Override
    public Map getTempMap(final Class owner, final String key) {
        // TODO Auto-generated method stub
        return null;
    }

}
