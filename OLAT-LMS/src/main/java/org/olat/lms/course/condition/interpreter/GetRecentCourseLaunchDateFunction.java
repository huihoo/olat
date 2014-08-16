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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.lms.course.condition.interpreter;

import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.editor.CourseEditorEnv;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.userview.UserCourseEnvironment;

/**
 * Description:<br>
 * Function to get the users recent launch date for this course. If no launch has taken place so far, the date will have a future date
 * <P>
 * Initial Date: 12 jan. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class GetRecentCourseLaunchDateFunction extends AbstractFunction {

    public static final String name = "getRecentCourseLaunchDate";

    /**
     * Default constructor to use the get initial enrollment date
     * 
     * @param userCourseEnv
     */
    public GetRecentCourseLaunchDateFunction(final UserCourseEnvironment userCourseEnv) {
        super(userCourseEnv);
    }

    /**
	 */
    @Override
    public Object call(final Object[] inStack) {
        final CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
        if (cev != null) {
            return defaultValue();
        }

        final CourseNode node = getUserCourseEnv().getCourseEnvironment().getRunStructure().getRootNode();
        final CoursePropertyManager pm = getUserCourseEnv().getCourseEnvironment().getCoursePropertyManager();
        final Identity identity = getUserCourseEnv().getIdentityEnvironment().getIdentity();
        final PropertyImpl recentTime = pm.findCourseNodeProperty(node, identity, null, ICourse.PROPERTY_RECENT_LAUNCH_DATE);

        if (recentTime != null) {
            final String firstTimeMillis = recentTime.getStringValue();
            return Double.valueOf(firstTimeMillis);
        } else {
            // what to do in case of no date available??? -> return date in the future
            return new Double(Double.POSITIVE_INFINITY);
        }
    }

    @Override
    protected Object defaultValue() {
        return new Double(Double.MIN_VALUE);
    }
}
