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
package org.olat.presentation.course.statistic.studybranch3;

import org.olat.lms.course.ICourse;
import org.olat.lms.course.statistic.IStatisticManager;
import org.olat.presentation.course.statistic.StatisticDisplayController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * The only reason this controller exists is to provide the right translation (this package translations).
 * 
 * <P>
 * Initial Date: 11.04.2011 <br>
 * 
 * @author lavinia
 */
public class StudyBranch3StatisticDisplayController extends StatisticDisplayController {

    /**
     * @param ureq
     * @param windowControl
     * @param course
     * @param statisticManager
     */
    public StudyBranch3StatisticDisplayController(UserRequest ureq, WindowControl windowControl, ICourse course, IStatisticManager statisticManager) {
        super(ureq, windowControl, course, statisticManager);

    }

}
