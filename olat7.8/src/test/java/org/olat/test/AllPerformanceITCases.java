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
package org.olat.test;

/**
 * The following Integration Tests focus on non-functional requirements:
 * <ul>
 * <li>loops where hundreds of things are created, loaded, tested</li>
 * <li>time measurements</li>
 * </ul> 
 * 
 * @author patrick
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.olat.data.commons.database.DBPerformanceITCase;
import org.olat.lms.basesecurity.UserPropertiesPerformanceITCase;
import org.olat.lms.group.BGAreaDaoITCase;
import org.olat.lms.repository.RepositoryServiceITCase;
import org.olat.lms.user.EmailCheckPerformanceITCase;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BGAreaDaoITCase.class, RepositoryServiceITCase.class, DBPerformanceITCase.class, UserPropertiesPerformanceITCase.class,
        EmailCheckPerformanceITCase.class

})
public class AllPerformanceITCases {
    //
}
