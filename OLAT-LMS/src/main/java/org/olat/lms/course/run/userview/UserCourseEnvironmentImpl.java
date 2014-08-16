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

package org.olat.lms.course.run.userview;

import java.util.HashMap;
import java.util.Map;

import org.olat.lms.course.condition.interpreter.ConditionInterpreter;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.scoring.ScoreAccounting;
import org.olat.lms.security.IdentityEnvironment;

/**
 * Initial Date: Feb 6, 2004
 * 
 * @author Felix Jost
 */
public class UserCourseEnvironmentImpl implements UserCourseEnvironment {
    private final IdentityEnvironment identityEnvironment;
    private final CourseEnvironment courseEnvironment;
    private final ConditionInterpreter conditionInterpreter;
    private final ScoreAccounting scoreAccounting;
    private final Map allTempMaps = new HashMap(4);

    public UserCourseEnvironmentImpl(final IdentityEnvironment identityEnvironment, final CourseEnvironment courseEnvironment) {
        this.courseEnvironment = courseEnvironment;
        this.identityEnvironment = identityEnvironment;
        this.scoreAccounting = new ScoreAccounting(this);
        this.conditionInterpreter = new ConditionInterpreter(this);
    }

    /**
     * @return Returns the courseEnvironment.
     */
    @Override
    public CourseEnvironment getCourseEnvironment() {
        return courseEnvironment;
    }

    @Override
    public IdentityEnvironment getIdentityEnvironment() {
        return identityEnvironment;
    }

    @Override
    public ConditionInterpreter getConditionInterpreter() {
        return conditionInterpreter;
    }

    @Override
    public ScoreAccounting getScoreAccounting() {
        return scoreAccounting;
    }

    @Override
    public CourseEditorEnv getCourseEditorEnv() {
        // return null signalling this is real user environment
        return null;
    }

    @Override
    public Map getTempMap(final Class owner, final String key) {
        // thread safe since only called by the gui dispatch thread of one user
        final String compKey = owner.getName() + ":" + key;
        Map m = (Map) allTempMaps.get(compKey);
        if (m == null) {
            m = new HashMap(4);
            allTempMaps.put(compKey, m);
        }
        return m;
    }

}
