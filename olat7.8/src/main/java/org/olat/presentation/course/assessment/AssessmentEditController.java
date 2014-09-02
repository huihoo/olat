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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.user.UserConstants;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessedIdentityWrapper;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Edit controller to change a users assessment for a particular course node. Make sure when using this controller that the current user has the permission to edit the
 * assessed users assessment. This controller does not check for security. When finished this controller fires a Event.CANCELLED_EVENT or a Event.CHANGED_EVENT and a
 * global assessment_changed_event //TODO doku events <BR>
 * When finished do not forget to call doDispose() to release the edit lock!
 * <P>
 * Initial Date: Oct 28, 2004
 * 
 * @author gnaegi
 */
public class AssessmentEditController extends BasicController {

    private VelocityContainer detailView;
    private AssessmentForm assessmentForm;
    private Controller detailsEditController;
    private AssessedIdentityWrapper assessedIdentityWrapper;
    private AssessableCourseNode courseNode;

    private Link backLink;
    private Link hideLogButton;
    private Link showLogButton;
    private LockResult lockEntry;
    private DialogBoxController alreadyLockedDialogController;
    private final AssessmentConfirmationSender assessmentConfirmationSender;

    /**
     * Constructor for the identity assessment edit controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window control
     * @param course
     * @param courseNode
     *            The assessable course node
     * @param assessedIdentityWrapper
     *            The wrapped assessed identity
     */
    public AssessmentEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final AssessableCourseNode courseNode,
            final AssessedIdentityWrapper assessedIdentityWrapper) {
        super(ureq, wControl);
        this.assessedIdentityWrapper = assessedIdentityWrapper;
        this.courseNode = courseNode;
        this.assessmentConfirmationSender = new AssessmentConfirmationSenderImpl(ureq.getIdentity(), courseNode, course, assessedIdentityWrapper);
        addLoggingResourceable(LoggingResourceable.wrap(course));
        addLoggingResourceable(LoggingResourceable.wrap(courseNode));

        if (courseNode == null) {
            throw new OLATRuntimeException(AssessmentEditController.class, "Can not initialize the assessment detail view when the current course node is null", null);
        }
        if (assessedIdentityWrapper == null) {
            throw new OLATRuntimeException(AssessmentEditController.class,
                    "Can not initialize the assessment detail view when the current assessedIdentityWrapper is null", null);
        }

        // acquire lock and show dialog box on failure.
        final String lockSubKey = "AssessmentLock-NID::" + courseNode.getIdent() + "-IID::" + assessedIdentityWrapper.getIdentity().getKey();
        lockEntry = getLockingService().acquireLock(course, ureq.getIdentity(), lockSubKey);
        if (lockEntry.isSuccess()) {
            // Initialize the assessment detail view
            detailView = createVelocityContainer("detailview");
            hideLogButton = LinkFactory.createButtonSmall("command.hidelog", detailView, this);
            showLogButton = LinkFactory.createButtonSmall("command.showlog", detailView, this);
            backLink = LinkFactory.createLinkBack(detailView, this);

            // Add the user object to the view
            final Identity assessedIdentity = assessedIdentityWrapper.getIdentity();
            detailView.contextPut("userFirstAndLastName", getUserService().getFirstAndLastname(assessedIdentity.getUser()));
            String userEmail = getUserService().getUserProperty(assessedIdentity.getUser(), UserConstants.EMAIL);
            detailView.contextPut("userEmail", userEmail);
            String userInstitutionalEmail = getUserService().getUserProperty(assessedIdentity.getUser(), UserConstants.INSTITUTIONALEMAIL);
            if ((userInstitutionalEmail != null) && (userInstitutionalEmail != "") && (userEmail != userInstitutionalEmail)) {
                detailView.contextPut("userInstitutionalEmail", userInstitutionalEmail);
            }
            detailView.contextPut("userInstitutionalName", getUserService().getUserProperty(assessedIdentity.getUser(), UserConstants.INSTITUTIONALNAME));
            detailView.contextPut("userInstitutionalUserIdentifier", getUserService().getInstitutionalIdentifier(assessedIdentity.getUser()));
            // Add the coaching info message
            final ModuleConfiguration modConfig = courseNode.getModuleConfiguration();
            String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
            infoCoach = Formatter.formatLatexFormulas(infoCoach);
            detailView.contextPut("infoCoach", infoCoach);
            // Add the assessment details form
            assessmentForm = new AssessmentForm(ureq, wControl, courseNode, assessedIdentityWrapper);
            listenTo(assessmentForm);

            detailView.put("assessmentform", assessmentForm.getInitialComponent());
            // Add user log. Get it from user properties
            final UserCourseEnvironment uce = assessedIdentityWrapper.getUserCourseEnvironment();
            final String nodeLog = courseNode.getUserLog(uce);
            detailView.contextPut("log", nodeLog);
            // Add the users details controller
            if (courseNode.hasDetails()) {
                detailView.contextPut("hasDetails", Boolean.TRUE);
                detailsEditController = courseNode.getDetailsEditController(ureq, wControl, uce);
                listenTo(detailsEditController);
                detailView.put("detailsController", detailsEditController.getInitialComponent());
            } else {
                detailView.contextPut("hasDetails", Boolean.FALSE);
            }

            // push node for page header
            detailView.contextPut("courseNode", courseNode);
            // push node css class
            detailView.contextPut("courseNodeCss", CourseNodeFactory.getInstance().getCourseNodeConfiguration(courseNode.getType()).getIconCSSClass());

            // push infos about users groups
            final List<BusinessGroup> participantGroups = course.getCourseEnvironment().getCourseGroupManager()
                    .getParticipatingLearningGroupsFromAllContexts(assessedIdentity, course);
            final Collator collator = Collator.getInstance(ureq.getLocale());
            Collections.sort(participantGroups, new Comparator<BusinessGroup>() {
                @Override
                public int compare(final BusinessGroup a, final BusinessGroup b) {
                    return collator.compare(a.getName(), b.getName());
                }
            });
            detailView.contextPut("participantGroups", participantGroups);
            detailView.contextPut("noParticipantGroups", (participantGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));

            putInitialPanel(detailView);
        } else {
            // lock was not successful !
            alreadyLockedDialogController = DialogBoxUIFactory.createResourceLockedMessage(ureq, wControl, lockEntry, "assessmentLock", getTranslator());
            listenTo(alreadyLockedDialogController);
            alreadyLockedDialogController.activate();
            // no initial component set -> empty behind dialog box!
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * @return
     */
    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == backLink) {
            releaseEditorLock();
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else if (source == hideLogButton) {
            detailView.contextPut("showLog", Boolean.FALSE);
        } else if (source == showLogButton) {
            detailView.contextPut("showLog", Boolean.TRUE);
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == assessmentForm) {
            if (event == Event.CANCELLED_EVENT) {
                releaseEditorLock();
                fireEvent(ureq, Event.CANCELLED_EVENT);
            } else if (event == Event.DONE_EVENT) {
                releaseEditorLock();
                doUpdateAssessmentData(ureq.getIdentity());
                assessmentConfirmationSender.sendAssessmentConfirmation();
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        } else if (source == detailsEditController) {
            // anything to do??
        } else if (source == alreadyLockedDialogController) {
            if (event == Event.CANCELLED_EVENT || DialogBoxUIFactory.isOkEvent(event)) {
                // ok clicked or box closed
                releaseEditorLock();
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    /**
     * Persists the changed form data in the user node properties and updates the wrapped identity that has been used to initialize the form
     * 
     * @param coachIdentity
     *            The identity of the coach who changes the users values (will be written to the user node log)
     */
    protected void doUpdateAssessmentData(final Identity coachIdentity) {
        final UserCourseEnvironment userCourseEnvironment = assessedIdentityWrapper.getUserCourseEnvironment();
        ScoreEvaluation scoreEval = null;
        Float newScore = null;
        Boolean newPassed = null;
        // String userName = userCourseEnvironment.getIdentityEnvironment().getIdentity().getName();

        if (assessmentForm.isHasAttempts() && assessmentForm.isAttemptsDirty()) {
            this.courseNode.updateUserAttempts(new Integer(assessmentForm.getAttempts()), userCourseEnvironment, coachIdentity);
        }

        if (assessmentForm.isHasScore() && assessmentForm.isScoreDirty()) {
            newScore = new Float(assessmentForm.getScore());
            // Update properties in db later... see
            // courseNode.updateUserSocreAndPassed...
        }

        if (assessmentForm.isHasPassed()) {
            if (assessmentForm.getCut() != null && StringHelper.containsNonWhitespace(assessmentForm.getScore())) {
                newPassed = Float.parseFloat(assessmentForm.getScore()) >= assessmentForm.getCut().floatValue() ? Boolean.TRUE : Boolean.FALSE;
            } else {
                // "passed" info was changed or not
                final String selectedKeyString = assessmentForm.getPassed().getSelectedKey();
                if ("true".equalsIgnoreCase(selectedKeyString) || "false".equalsIgnoreCase(selectedKeyString)) {
                    newPassed = Boolean.valueOf(selectedKeyString);
                } else {
                    // "undefined" was choosen
                    newPassed = null;
                }
            }
        }
        // Update score,passed properties in db
        scoreEval = new ScoreEvaluation(newScore, newPassed);
        this.courseNode.updateUserScoreEvaluation(scoreEval, userCourseEnvironment, coachIdentity, false);

        if (assessmentForm.isHasComment() && assessmentForm.isUserCommentDirty()) {
            final String newComment = assessmentForm.getUserComment().getValue();
            // Update properties in db
            courseNode.updateUserUserComment(newComment, userCourseEnvironment, coachIdentity);
        }

        if (assessmentForm.isCoachCommentDirty()) {
            final String newCoachComment = assessmentForm.getCoachComment().getValue();
            // Update properties in db
            this.courseNode.updateUserCoachComment(newCoachComment, userCourseEnvironment);
        }

        // Refresh score view
        userCourseEnvironment.getScoreAccounting().scoreInfoChanged(this.courseNode, scoreEval);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // first release editor lock
        releaseEditorLock();
    }

    private void releaseEditorLock() {
        if (lockEntry == null) {
            return;
        }

        if (lockEntry.isSuccess()) {
            // release lock
            getLockingService().releaseLock(lockEntry);
        } else {
            removeAsListenerAndDispose(alreadyLockedDialogController);
        }
        lockEntry = null;
    }

}
