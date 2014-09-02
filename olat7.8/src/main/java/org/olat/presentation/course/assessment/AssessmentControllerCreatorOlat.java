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
package org.olat.presentation.course.assessment;

import java.util.List;

import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.IAssessmentCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.ims.qti.exporter.QTIArchiveWizardController;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * TODO: patrickb Class Description for AssessmentControllerCreatorOlat
 * <P>
 * Initial Date: 29.06.2010 <br>
 * 
 * @author patrickb
 */
public class AssessmentControllerCreatorOlat implements AssessmentControllerCreator {

    protected AssessmentControllerCreatorOlat() {
    }

    @Override
    public Activateable createAssessmentMainController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores,
            final IAssessmentCallback assessmentCallback) {
        return new AssessmentMainController(ureq, wControl, ores, assessmentCallback);
    }

    /**
     * org.olat.lms.course.ICourse, org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createQTIArchiveWizardController(final boolean dummyMode, final UserRequest ureq, final List nodesTableObjectArrayList, final ICourse course,
            final WindowControl wControl) {
        return new QTIArchiveWizardController(dummyMode, ureq, nodesTableObjectArrayList, course, wControl);
    }

}
