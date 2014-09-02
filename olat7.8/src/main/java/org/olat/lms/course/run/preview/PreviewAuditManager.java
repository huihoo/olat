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

package org.olat.lms.course.run.preview;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.CourseNode;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final public class PreviewAuditManager extends UserNodeAuditManager {

    /**
	 */
    @Override
    public boolean hasUserNodeLogs(final CourseNode node) {
        // no logging in preview
        return false;
    }

    /**
	 */
    @Override
    public String getUserNodeLog(final CourseNode courseNode, final Identity identity) {
        // no logging in preview
        return null;
    }

    /**
     * java.lang.String)
     */
    @Override
    public void appendToUserNodeLog(final CourseNode courseNode, final Identity identity, final Identity assessedIdentity, final String logText) {
        // no logging in preview
    }

}
