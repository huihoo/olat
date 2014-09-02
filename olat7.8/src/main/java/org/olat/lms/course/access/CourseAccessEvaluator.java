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
package org.olat.lms.course.access;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;

/**
 * Initial Date: 26.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
public interface CourseAccessEvaluator {

    /**
     * Checks if given identity has access to this course.
     * 
     * @param evaluateExpertRules
     *            if set to
     *            <code>false<code> skips evaluation of expert rules (e.g. for batch processing, since condition might be dependent on shibboleth attributes => OLAT-6761)
     */
    boolean isCourseAccessibleForIdentity(Identity identity, RepositoryEntry courseRepositoryEntry, ICourse course, boolean evaluateExpertRules);

    /**
     * Checks if given identity is allowed to see this course node.
     * 
     * @param evaluateExpertRules
     *            if set to
     *            <code>false<code> skips evaluation of expert rules (e.g. for batch processing, since condition might be dependent on shibboleth attributes => OLAT-6761)
     */
    boolean isCourseNodeVisibleForIdentity(Identity identity, ICourse course, String courseNodeId, boolean evaluateExpertRules);

    /**
     * Checks if given identity has access to this course node.
     * 
     * @param evaluateExpertRules
     *            if set to
     *            <code>false<code> skips evaluation of expert rules (e.g. for batch processing, since condition might be dependent on shibboleth attributes => OLAT-6761)
     */
    boolean isCourseNodeAccessibleForIdentity(Identity identity, ICourse course, CourseNode node, boolean evaluateExpertRules);

}
