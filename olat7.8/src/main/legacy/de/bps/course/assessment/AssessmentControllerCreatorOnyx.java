package de.bps.course.assessment;

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentControllerCreatorOlat;
import org.olat.course.assessment.IAssessmentCallback;

import de.bps.ims.qti.export.QTIArchiveWizardController;

public class AssessmentControllerCreatorOnyx extends AssessmentControllerCreatorOlat {

	/**
	 * @see org.olat.course.assessment.AssessmentMainControllerCreator#create(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.WindowControl,
	 *      org.olat.core.id.OLATResourceable, org.olat.course.assessment.IAssessmentCallback)
	 */
	@Override
	public Activateable createAssessmentMainController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores,
			final IAssessmentCallback assessmentCallback) {
		return new AssessmentMainController(ureq, wControl, ores, assessmentCallback);
	}

	/**
	 * @see org.olat.course.assessment.AssessmentControllerCreator#createQTIArchiveWizardController(boolean, org.olat.presentation.framework.UserRequest, java.util.List,
	 *      org.olat.course.ICourse, org.olat.presentation.framework.control.WindowControl)
	 */
	@Override
	public Controller createQTIArchiveWizardController(final boolean dummyMode, final UserRequest ureq, final List nodesTableObjectArrayList, final ICourse course,
			final WindowControl wControl) {
		return new QTIArchiveWizardController(dummyMode, ureq, nodesTableObjectArrayList, course, wControl);
	}
}
