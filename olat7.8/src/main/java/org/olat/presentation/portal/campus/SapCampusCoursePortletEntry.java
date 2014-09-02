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
package org.olat.presentation.portal.campus;

import org.olat.lms.learn.campus.service.SapCampusCourseTo;
import org.olat.presentation.framework.core.control.generic.portal.PortletEntry;

/**
 * Initial Date: 24.05.2012 <br>
 * 
 * @author cg
 */
public final class SapCampusCoursePortletEntry implements PortletEntry<SapCampusCourseTo> {

    private final SapCampusCourseTo value;
    private final Long key;

    public SapCampusCoursePortletEntry(SapCampusCourseTo sapCourseTo) {
        this.value = sapCourseTo;
        this.key = sapCourseTo.getOlatCourseId();
    }

    @Override
    public SapCampusCourseTo getValue() {
        return value;
    }

    @Override
    public Long getKey() {
        return key;
    }

}
