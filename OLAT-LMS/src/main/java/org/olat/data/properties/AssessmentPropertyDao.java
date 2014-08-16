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
package org.olat.data.properties;

import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * Initial Date: 08.11.2011 <br>
 * 
 * @author cg
 */
public interface AssessmentPropertyDao {

    // Names used to save user data in the properties table
    public static final String SCORE = "SCORE";
    public static final String PASSED = "PASSED";
    public static final String ATTEMPTS = "ATTEMPTS";
    public static final String COMMENT = "COMMENT";
    public static final String COACH_COMMENT = "COACH_COMMENT";
    public static final String ASSESSMENT_ID = "ASSESSMENT_ID";

    public List loadPropertiesFor(final Identity identity, String resourceableTypeName, Long resourceableId);

    /**
     * @return a list of all identities that have generated any assessment properties within this courses
     */
    public List getAllIdentitiesWithCourseAssessmentData(String resourceableTypeName, Long resourceableId);

}
