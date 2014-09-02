package org.olat.presentation.course.assessment;

import java.util.List;

import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.IAssessmentCallback;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.system.commons.resource.OLATResourceable;

public interface AssessmentControllerCreator {

    public Activateable createAssessmentMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, IAssessmentCallback assessmentCallback);

    public Controller createQTIArchiveWizardController(boolean dummyMode, UserRequest ureq, List nodesTableObjectArrayList, ICourse course, WindowControl windowControl);

}
