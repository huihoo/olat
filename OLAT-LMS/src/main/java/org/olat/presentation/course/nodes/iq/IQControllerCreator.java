package org.olat.presentation.course.nodes.iq;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.IQSELFCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.tabbable.TabbableController;

public interface IQControllerCreator {

    /**
     * The iq test edit screen in the course editor.
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     * @param groupMgr
     * @param euce
     * @return
     */
    public TabbableController createIQTestEditController(UserRequest ureq, WindowControl wControl, ICourse course, IQTESTCourseNode courseNode,
            CourseGroupManager groupMgr, UserCourseEnvironment euce);

    /**
     * The iq test edit screen in the course editor.
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     * @param groupMgr
     * @param euce
     * @return
     */
    public TabbableController createIQSelftestEditController(UserRequest ureq, WindowControl wControl, ICourse course, IQSELFCourseNode courseNode,
            CourseGroupManager groupMgr, UserCourseEnvironment euce);

    /**
     * The iq test edit screen in the course editor.
     * 
     * @param ureq
     * @param wControl
     * @param course
     * @param courseNode
     * @param groupMgr
     * @param euce
     * @return
     */
    public TabbableController createIQSurveyEditController(UserRequest ureq, WindowControl wControl, ICourse course, IQSURVCourseNode courseNode,
            CourseGroupManager groupMgr, UserCourseEnvironment euce);

    /**
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param ne
     * @param courseNode
     * @return
     */
    public Controller createIQTestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
            IQTESTCourseNode courseNode);

    public Controller createIQTestPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
            IQTESTCourseNode courseNode);

    public Controller createIQSelftestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
            IQSELFCourseNode courseNode);

    public Controller createIQSurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
            IQSURVCourseNode courseNode);

    public Controller createIQTestDetailsEditController(Long courseResourceableId, String ident, Identity identity, RepositoryEntry referencedRepositoryEntry,
            String qmdEntryTypeAssess, UserRequest ureq, WindowControl wControl);

}
