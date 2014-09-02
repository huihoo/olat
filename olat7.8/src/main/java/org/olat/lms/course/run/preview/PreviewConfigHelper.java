package org.olat.lms.course.run.preview;

import java.util.ArrayList;
import java.util.Date;

import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;

/**
 * Provides a PreviewCourseEnvironment without using the PreviewConfigController and the PreviewSettingsForm.
 * <P>
 * Initial Date: 03.12.2009 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class PreviewConfigHelper {

    public static CourseEnvironment getPreviewCourseEnvironment(final boolean isCoach, final boolean isCourseAdmin, final ICourse course) {
        // generateEnvironment();
        final CourseGroupManager cgm = new PreviewCourseGroupManager(new ArrayList(), new ArrayList(), isCoach, isCourseAdmin);
        final UserNodeAuditManager auditman = new PreviewAuditManager();
        final AssessmentManager am = new PreviewAssessmentManager();
        final CoursePropertyManager cpm = new PreviewCoursePropertyManager();

        final CourseEnvironment previewCourseEnvironment = new PreviewCourseEnvironment(course, new Date(), cpm, cgm, auditman, am);

        return previewCourseEnvironment;
    }
}
