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

package org.olat.presentation.course.nodes.iq;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.IQSELFCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.nodes.SelfAssessableCourseNode;
import org.olat.lms.course.run.scoring.ScoreEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.IQSecurityCallback;
import org.olat.lms.ims.qti.QTIChangeLogMessage;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.ImsRepositoryResolver;
import org.olat.lms.instantmessaging.InstantMessaging;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.course.assessment.AssessmentNotificationsHandler;
import org.olat.presentation.course.nodes.ObjectivesHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.HtmlStaticPageComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.iframe.IFrameDisplayController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.ims.qti.run.IQDisplayController;
import org.olat.presentation.ims.qti.run.IQSubmittedEvent;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<BR>
 * Run controller for the qti test, selftest and survey course node. Call assessmentStopped if test is finished, closed or at dispose (e.g. course tab gets closed).
 * Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 */
public class IQRunController extends BasicController implements GenericEventListener {

    private final VelocityContainer myContent;

    private final IQSecurityCallback secCallback;
    private final ModuleConfiguration modConfig;

    private IQDisplayController displayController;
    private final CourseNode courseNode;
    private final String type;
    private final UserCourseEnvironment userCourseEnv;
    private Link startButton;
    private Link showResultsButton;
    private Link hideResultsButton;

    private IFrameDisplayController iFrameCtr;

    private final Panel mainPanel;

    private boolean assessmentStopped = true; // default: true
    private EventBus singleUserEventCenter;
    private OLATResourceable assessmentEventOres;
    private UserSession userSession;

    private OLATResourceable chatEventOres;
    private OLATResourceable assessmentInstanceOres;
    private AssessmentNotificationsHandler notificationHandler;

    /**
     * Constructor for a test run controller
     * 
     * @param userCourseEnv
     * @param moduleConfiguration
     * @param secCallback
     * @param ureq
     * @param wControl
     * @param testCourseNode
     */
    IQRunController(final UserCourseEnvironment userCourseEnv, final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback,
            final UserRequest ureq, final WindowControl wControl, final IQTESTCourseNode testCourseNode, final AssessmentNotificationsHandler notificationHandler) {
        super(ureq, wControl);

        this.modConfig = moduleConfiguration;
        this.secCallback = secCallback;
        this.userCourseEnv = userCourseEnv;
        this.courseNode = testCourseNode;
        this.type = AssessmentInstance.QMD_ENTRY_TYPE_ASSESS;
        this.singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
        this.assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);
        this.assessmentInstanceOres = OresHelper.createOLATResourceableType(AssessmentInstance.class);
        this.chatEventOres = OresHelper.createOLATResourceableType(InstantMessaging.class);
        this.notificationHandler = notificationHandler;
        this.userSession = ureq.getUserSession();

        addLoggingResourceable(LoggingResourceable.wrap(courseNode));

        myContent = createVelocityContainer("testrun");

        mainPanel = putInitialPanel(myContent);

        if (!modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
            throw new OLATRuntimeException("IQRunController launched with Test constructor but module configuration not configured as test", null);
        }
        init(ureq);
        exposeUserTestDataToVC(ureq);

        final StringBuilder qtiChangelog = createChangelogMsg(ureq);
        // decide about changelog in VC
        if (qtiChangelog.length() > 0) {
            // there is some message
            myContent.contextPut("changeLog", qtiChangelog);
        }

        // if show results on test home page configured - show log
        final Boolean showResultOnHomePage = (Boolean) testCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
        myContent.contextPut("showChangelog", showResultOnHomePage);
    }

    /**
     * @param ureq
     * @return
     */
    private StringBuilder createChangelogMsg(final UserRequest ureq) {
        /*
         * TODO:pb:is ImsRepositoryResolver the right place for getting the change log?
         */
        final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
        // re could be null, but if we are here it should not be null!
        final Roles userRoles = ureq.getUserSession().getRoles();
        boolean showAll = false;
        showAll = userRoles.isAuthor() || userRoles.isOLATAdmin();
        // get changelog
        final Formatter formatter = Formatter.getInstance(ureq.getLocale());
        final ImsRepositoryResolver resolver = new ImsRepositoryResolver(re.getKey());
        final QTIChangeLogMessage[] qtiChangeLog = resolver.getDocumentChangeLog();
        final StringBuilder qtiChangelog = new StringBuilder();
        Date msgDate = null;
        if (qtiChangeLog.length > 0) {
            // there are resource changes
            Arrays.sort(qtiChangeLog);
            for (int i = qtiChangeLog.length - 1; i >= 0; i--) {
                // show latest change first
                if (!showAll && qtiChangeLog[i].isPublic()) {
                    // logged in person is a normal user, hence public messages only
                    msgDate = new Date(qtiChangeLog[i].getTimestmp());
                    qtiChangelog.append("\nChange date: ").append(formatter.formatDateAndTime(msgDate)).append("\n");
                    qtiChangelog.append(qtiChangeLog[i].getLogMessage());
                    qtiChangelog.append("\n********************************\n");
                } else if (showAll) {
                    // logged in person is an author, olat admin, owner, show all messages
                    msgDate = new Date(qtiChangeLog[i].getTimestmp());
                    qtiChangelog.append("\nChange date: ").append(formatter.formatDateAndTime(msgDate)).append("\n");
                    qtiChangelog.append(qtiChangeLog[i].getLogMessage());
                    qtiChangelog.append("\n********************************\n");
                }// else non public messages are not shown to normal user
            }
        }
        return qtiChangelog;
    }

    /**
     * Constructor for a self-test run controller
     * 
     * @param userCourseEnv
     * @param moduleConfiguration
     * @param secCallback
     * @param ureq
     * @param wControl
     * @param selftestCourseNode
     */
    IQRunController(final UserCourseEnvironment userCourseEnv, final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback,
            final UserRequest ureq, final WindowControl wControl, final IQSELFCourseNode selftestCourseNode) {
        super(ureq, wControl);

        this.modConfig = moduleConfiguration;
        this.secCallback = secCallback;
        this.userCourseEnv = userCourseEnv;
        this.courseNode = selftestCourseNode;
        this.type = AssessmentInstance.QMD_ENTRY_TYPE_SELF;

        addLoggingResourceable(LoggingResourceable.wrap(courseNode));

        myContent = createVelocityContainer("selftestrun");

        mainPanel = putInitialPanel(myContent);

        if (!modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)) {
            throw new OLATRuntimeException("IQRunController launched with Selftest constructor but module configuration not configured as selftest", null);
        }
        init(ureq);
        exposeUserSelfTestDataToVC(ureq);

        final StringBuilder qtiChangelog = createChangelogMsg(ureq);
        // decide about changelog in VC
        if (qtiChangelog.length() > 0) {
            // there is some message
            myContent.contextPut("changeLog", qtiChangelog);
        }
        // per default change log is not open
        myContent.contextPut("showChangelog", Boolean.FALSE);
    }

    /**
     * Constructor for a survey run controller
     * 
     * @param userCourseEnv
     * @param moduleConfiguration
     * @param secCallback
     * @param ureq
     * @param wControl
     * @param surveyCourseNode
     */
    IQRunController(final UserCourseEnvironment userCourseEnv, final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback,
            final UserRequest ureq, final WindowControl wControl, final IQSURVCourseNode surveyCourseNode) {
        super(ureq, wControl);

        this.modConfig = moduleConfiguration;
        this.secCallback = secCallback;
        this.userCourseEnv = userCourseEnv;
        this.courseNode = surveyCourseNode;
        this.type = AssessmentInstance.QMD_ENTRY_TYPE_SURVEY;

        addLoggingResourceable(LoggingResourceable.wrap(courseNode));

        myContent = createVelocityContainer("surveyrun");

        mainPanel = putInitialPanel(myContent);

        if (!modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
            throw new OLATRuntimeException("IQRunController launched with Survey constructor but module configuration not configured as survey", null);
        }
        init(ureq);
        exposeUserQuestionnaireDataToVC();

        final StringBuilder qtiChangelog = createChangelogMsg(ureq);
        // decide about changelog in VC
        if (qtiChangelog.length() > 0) {
            // there is some message
            myContent.contextPut("changeLog", qtiChangelog);
        }
        // per default change log is not open
        myContent.contextPut("showChangelog", Boolean.FALSE);
    }

    private void init(final UserRequest ureq) {
        startButton = LinkFactory.createButton("start", myContent, this);
        // fetch disclaimer file
        String sDisclaimer = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
        if (sDisclaimer != null) {
            VFSContainer baseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
            final int lastSlash = sDisclaimer.lastIndexOf('/');
            if (lastSlash != -1) {
                baseContainer = (VFSContainer) baseContainer.resolve(sDisclaimer.substring(0, lastSlash));
                sDisclaimer = sDisclaimer.substring(lastSlash);
                // first check if disclaimer exists on filesystem
                if (baseContainer == null || baseContainer.resolve(sDisclaimer) == null) {
                    showWarning("disclaimer.file.invalid", sDisclaimer);
                } else {
                    // screenreader do not like iframes, display inline
                    if (getWindowControl().getWindowBackOffice().getWindowManager().isForScreenReader()) {
                        final HtmlStaticPageComponent disclaimerComp = new HtmlStaticPageComponent("disc", baseContainer);
                        myContent.put("disc", disclaimerComp);
                        disclaimerComp.setCurrentURI(sDisclaimer);
                        myContent.contextPut("hasDisc", Boolean.TRUE);
                    } else {
                        iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), baseContainer);
                        listenTo(iFrameCtr);// dispose automatically
                        myContent.put("disc", iFrameCtr.getInitialComponent());
                        iFrameCtr.setCurrentURI(sDisclaimer);
                        myContent.contextPut("hasDisc", Boolean.TRUE);
                    }

                }
            }
        }

        // push title and learning objectives, only visible on intro page
        myContent.contextPut("menuTitle", courseNode.getShortTitle());
        myContent.contextPut("displayTitle", courseNode.getLongTitle());

        // Adding learning objectives
        final String learningObj = courseNode.getLearningObjectives();
        if (learningObj != null) {
            final Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
            myContent.put("learningObjectives", learningObjectives);
            myContent.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator
        }

        if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
            checkChats(ureq);
            singleUserEventCenter.registerFor(this, getIdentity(), chatEventOres);
        }
    }

    private List allChats;

    private void checkChats(final UserRequest ureq) {
        if (ureq != null) {
            allChats = (List) ureq.getUserSession().getEntry("chats");
        }
        if (allChats == null || allChats.size() == 0) {
            startButton.setEnabled(true);
            myContent.contextPut("hasChatWindowOpen", false);
        } else {
            startButton.setEnabled(false);
            myContent.contextPut("hasChatWindowOpen", true);
        }
    }

    @Override
    public void event(final Event event) {
        if (type == AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) {
            if (event.getCommand().startsWith("ChatWindow")) {
                checkChats(null);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startButton && startButton.isEnabled()) {
            final long callingResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().longValue();
            final String callingResDetail = courseNode.getIdent();
            removeAsListenerAndDispose(displayController);
            final Controller returnController = IQManager.getInstance().createIQDisplayController(modConfig, secCallback, ureq, getWindowControl(), callingResId,
                    callingResDetail);
            /*
             * either returnController is a MessageController or it is a IQDisplayController this should not serve as pattern to be copy&pasted. FIXME:2008-11-21:pb
             * INTRODUCED because of read/write QTI Lock solution for scalability II, 6.1.x Release
             */
            if (returnController instanceof IQDisplayController) {
                displayController = (IQDisplayController) returnController;
                listenTo(displayController);
                if (displayController.isReady()) {
                    // in case displayController was unable to initialize, a message was set by displayController
                    // this is the case if no more attempts or security check was unsuccessfull
                    final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null,
                            displayController.getInitialComponent(), null);
                    listenTo(layoutCtr); // autodispose

                    // need to wrap a course restart controller again, because IQDisplay
                    // runs on top of GUIStack
                    final ICourse course = CourseFactory.loadCourse(callingResId);
                    final RepositoryEntry courseRepositoryEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(course, true);
                    final Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
                    final Controller courseCloser = CourseFactory.createDisposedCourseRestartController(ureq, getWindowControl(),
                            courseRepositoryEntry.getResourceableId());
                    final Controller disposedRestartController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, empty,
                            courseCloser.getInitialComponent(), "disposed course whily in iqRun" + callingResId);
                    layoutCtr.setDisposedMessageController(disposedRestartController);

                    getWindowControl().pushToMainArea(layoutCtr.getInitialComponent());
                    if (modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
                        assessmentStopped = false;
                        singleUserEventCenter.registerFor(this, getIdentity(), assessmentInstanceOres);
                        singleUserEventCenter.fireEventToListenersOf(new AssessmentEvent(AssessmentEvent.TYPE.STARTED, ureq.getUserSession()), assessmentEventOres);
                    }
                }// endif isReady
            } else {
                // -> qti file was locked -> show info message
                // user must click again on course node to activate
                mainPanel.pushContent(returnController.getInitialComponent());
            }
        } else if (source == showResultsButton) {
            final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
            Long assessmentID = am.getAssessmentID(courseNode, ureq.getIdentity());
            if (assessmentID == null) {
                // fallback solution: if the assessmentID is not available via AssessmentManager than try to get it via IQManager
                final long callingResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().longValue();
                final String callingResDetail = courseNode.getIdent();
                assessmentID = IQManager.getInstance().getLastAssessmentID(ureq.getIdentity(), callingResId, callingResDetail);
            }
            if (assessmentID != null && !assessmentID.equals("")) {
                final Document doc = IQManager.getInstance().getResultsReportingFromFile(ureq.getIdentity(), type, assessmentID);
                // StringBuilder resultsHTML = LocalizedXSLTransformer.getInstance(ureq.getLocale()).renderResults(doc);
                final String summaryConfig = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
                final int summaryType = AssessmentInstance.getSummaryType(summaryConfig);
                final String resultsHTML = IQManager.getInstance().transformResultsReporting(doc, ureq.getLocale(), summaryType);
                myContent.contextPut("displayreporting", resultsHTML);
                myContent.contextPut("resreporting", resultsHTML);
                myContent.contextPut("showResults", Boolean.TRUE);
            }
        } else if (source == hideResultsButton) {
            myContent.contextPut("showResults", Boolean.FALSE);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source == displayController) {
            if (event instanceof IQSubmittedEvent) {
                final IQSubmittedEvent se = (IQSubmittedEvent) event;
                final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();

                // Save results in case of test
                if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
                    // update scoring overview for the user in the current course
                    final Float score = new Float(se.getScore());
                    final Boolean passed = new Boolean(se.isPassed());
                    final ScoreEvaluation sceval = new ScoreEvaluation(score, passed, new Long(se.getAssessmentID()));
                    final AssessableCourseNode acn = (AssessableCourseNode) courseNode; // assessment nodes are assesable
                    final boolean incrementUserAttempts = true;
                    acn.updateUserScoreEvaluation(sceval, userCourseEnv, urequest.getIdentity(), incrementUserAttempts);
                    // userCourseEnv.getScoreAccounting().scoreInfoChanged(acn, sceval);
                    exposeUserTestDataToVC(urequest);

                    // Mark publisher for notifications
                    final Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
                    notificationHandler.markPublisherNews(urequest.getIdentity(), courseId);
                    if (!assessmentStopped) {
                        assessmentStopped = true;
                        final AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
                        singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
                        singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
                    }
                }
                // Save results in case of questionnaire
                else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
                    // save number of attempts
                    // although this is not an assessable node we still use the assessment
                    // manager since this one uses caching
                    am.incrementNodeAttempts(courseNode, urequest.getIdentity(), userCourseEnv);
                    exposeUserQuestionnaireDataToVC();
                    getWindowControl().pop();
                }
                // Don't save results in case of self-test
                // but do safe attempts !
                else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)) {
                    am.incrementNodeAttempts(courseNode, urequest.getIdentity(), userCourseEnv);
                }
            } else if (event.equals(Event.DONE_EVENT)) {
                getWindowControl().pop();
                if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) && !assessmentStopped) {
                    assessmentStopped = true;
                    final AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
                    singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
                    singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
                }
                fireEvent(urequest, Event.DONE_EVENT);

            }
        }
    }

    private void exposeUserTestDataToVC(final UserRequest ureq) {
        // config : show score info
        final Object enableScoreInfoObject = modConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
        if (enableScoreInfoObject != null) {
            myContent.contextPut("enableScoreInfo", enableScoreInfoObject);
        } else {
            myContent.contextPut("enableScoreInfo", Boolean.TRUE);
        }

        // configuration data
        myContent.contextPut("attemptsConfig", modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS));
        // user data
        if (!(courseNode instanceof AssessableCourseNode)) {
            throw new AssertException("exposeUserTestDataToVC can only be called for test nodes, not for selftest or questionnaire");
        }
        final AssessableCourseNode acn = (AssessableCourseNode) courseNode; // assessment nodes are assesable
        final ScoreEvaluation scoreEval = acn.getUserScoreEvaluation(userCourseEnv);

        final Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
        myContent.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
        myContent.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
        myContent.contextPut("passed", scoreEval.getPassed());
        myContent.contextPut("comment", acn.getUserUserComment(userCourseEnv));
        myContent.contextPut("attempts", acn.getUserAttempts(userCourseEnv));

        final UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
        myContent.contextPut("log", am.getUserNodeLog(courseNode, identity));

        exposeResults(ureq);
    }

    /**
     * Provides the self test score and results, if any, to the velocity container.
     * 
     * @param ureq
     */
    private void exposeUserSelfTestDataToVC(final UserRequest ureq) {
        // config : show score info
        final Object enableScoreInfoObject = modConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
        if (enableScoreInfoObject != null) {
            myContent.contextPut("enableScoreInfo", enableScoreInfoObject);
        } else {
            myContent.contextPut("enableScoreInfo", Boolean.TRUE);
        }

        if (!(courseNode instanceof SelfAssessableCourseNode)) {
            throw new AssertException("exposeUserSelfTestDataToVC can only be called for selftest nodes, not for test or questionnaire");
        }
        final SelfAssessableCourseNode acn = (SelfAssessableCourseNode) courseNode;
        final ScoreEvaluation scoreEval = acn.getUserScoreEvaluation(userCourseEnv);
        if (scoreEval != null) {
            myContent.contextPut("hasResults", Boolean.TRUE);
            myContent.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
            myContent.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
            myContent.contextPut("passed", scoreEval.getPassed());
            myContent.contextPut("attempts", new Integer(1)); // at least one attempt

            exposeResults(ureq);
        }
    }

    /**
     * Provides the show results button if results available or a message with the visibility period.
     * 
     * @param ureq
     */
    private void exposeResults(final UserRequest ureq) {
        // migration: check if old tests have no summary configured
        final String configuredSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
        final boolean noSummary = configuredSummary == null || (configuredSummary != null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE));
        if (!noSummary) {
            final Boolean showResultsObj = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
            final boolean showResultsOnHomePage = (showResultsObj != null && showResultsObj.booleanValue());
            myContent.contextPut("showResultsOnHomePage", new Boolean(showResultsOnHomePage));
            final boolean dateRelatedVisibility = AssessmentHelper.isResultVisible(modConfig);
            if (showResultsOnHomePage && dateRelatedVisibility) {
                myContent.contextPut("showResultsVisible", Boolean.TRUE);
                showResultsButton = LinkFactory.createButton("command.showResults", myContent, this);
                hideResultsButton = LinkFactory.createButton("command.hideResults", myContent, this);
            } else if (showResultsOnHomePage) {
                final Date startDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
                final Date endDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
                final String visibilityStartDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(startDate);
                String visibilityEndDate = "-";
                if (endDate != null) {
                    visibilityEndDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(endDate);
                }
                final String visibilityPeriod = getTranslator().translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate });
                myContent.contextPut("visibilityPeriod", visibilityPeriod);
                myContent.contextPut("showResultsVisible", Boolean.FALSE);
            }
        }
    }

    private void exposeUserQuestionnaireDataToVC() {
        final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
        final Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
        // although this is not an assessable node we still use the assessment
        // manager since this one uses caching
        myContent.contextPut("attempts", am.getNodeAttempts(courseNode, identity));
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers disposed by basic controller
        if (!type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
            return;
        }

        singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
        singleUserEventCenter.deregisterFor(this, chatEventOres);

        if (!assessmentStopped) {
            final AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
            singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
        }

    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
