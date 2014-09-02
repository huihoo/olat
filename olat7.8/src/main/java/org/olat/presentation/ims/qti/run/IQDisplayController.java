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

package org.olat.presentation.ims.qti.run;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.StringResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.IQSecurityCallback;
import org.olat.lms.ims.qti.QTIConstants;
import org.olat.lms.ims.qti.container.AssessmentContext;
import org.olat.lms.ims.qti.navigator.Navigator;
import org.olat.lms.ims.qti.navigator.SequentialItemNavigator;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.ims.qti.process.FilePersister;
import org.olat.lms.ims.qti.process.ImsRepositoryResolver;
import org.olat.lms.ims.qti.process.Persister;
import org.olat.lms.ims.qti.process.Resolver;
import org.olat.lms.ims.qti.run.IqDisplayEBL;
import org.olat.lms.ims.qti.run.IqDisplayParameterObjectEBL;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.course.nodes.iq.IQEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.progressbar.ProgressBar;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.winmgr.BackHandler;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Felix Jost
 */
public class IQDisplayController extends DefaultController implements BackHandler {

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(IQDisplayController.class);

    private static final Logger log = LoggerHelper.getLogger();

    private VelocityContainer myContent;

    private Translator translator;
    private String repositorySoftkey = null;
    private Resolver resolver = null;
    private Persister persister = null;

    private ProgressBar qtiscoreprogress, qtiquestionprogress;
    private IQComponent qticomp;
    private IQStatus qtistatus;
    private IQManager iqm;
    private IQSecurityCallback iqsec;
    private final ModuleConfiguration modConfig;
    // TODO: make nicer separation
    private long callingResId = 0;
    private String callingResDetail = "";
    private boolean ready;
    private Link closeButton;

    /**
     * IMS QTI Display Controller used by the course nodes concurrency protection is solved on IQManager. -> do not make constructor public -> create controller only via
     * IQManager
     * 
     * @param moduleConfiguration
     * @param secCallback
     * @param ureq
     * @param wControl
     * @param callingResId
     * @param callingResDetail
     */
    public IQDisplayController(final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback, final UserRequest ureq, final WindowControl wControl,
            final long callingResId, final String callingResDetail) {
        super(wControl);

        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());

        this.modConfig = moduleConfiguration;
        this.callingResId = callingResId;
        this.callingResDetail = callingResDetail;
        this.repositorySoftkey = (String) moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);

        init(secCallback, ureq);
    }

    /**
     * IMS QTI Display Controller used by QTI Editor for preview. concurrency protection is solved on IQManager. -> do not make constructor public -> create controller
     * only via IQManager
     * 
     * @param resolver
     * @param type
     * @param secCallback
     * @param ureq
     * @param wControl
     */
    public IQDisplayController(final Resolver resolver, final String type, final IQSecurityCallback secCallback, final UserRequest ureq, final WindowControl wControl) {
        super(wControl);

        ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());

        this.modConfig = new ModuleConfiguration();
        modConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.TRUE);
        modConfig.set(IQEditController.CONFIG_KEY_TYPE, type);
        modConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
        modConfig.set(IQEditController.CONFIG_KEY_SCOREPROGRESS, Boolean.TRUE);
        modConfig.set(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, Boolean.FALSE);
        modConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, Boolean.TRUE);
        modConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, Boolean.FALSE);
        modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED);
        modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
        this.resolver = resolver;
        this.persister = null;
        init(secCallback, ureq);
    }

    private void init(final IQSecurityCallback secCallback, final UserRequest ureq) {
        this.iqsec = secCallback;
        this.translator = PackageUtil.createPackageTranslator(IQDisplayController.class, ureq.getLocale());
        this.ready = false;

        // acquire the back-handling, so that (unintended) back-clicks will not throw you out of a test, but shows a message instead.
        getWindowControl().getWindowBackOffice().acquireBackHandling(this);

        iqm = IQManager.getInstance();

        myContent = new VelocityContainer("olatmodiqrun", VELOCITY_ROOT + "/qti.html", translator, this);

        // Check if fibautocompl.js and fibautocompl.css exists for enhance FIB autocomplete feature
        Resolver autcompResolver = null;
        if (resolver == null) {
            final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true);
            autcompResolver = new ImsRepositoryResolver(re.getKey());
        } else {
            autcompResolver = this.resolver;
        }

        if (autcompResolver != null && autcompResolver.hasAutocompleteFiles()) {
            // Add Autocomplte JS and CSS file to header
            final StringBuilder sb = new StringBuilder();
            // must be like <script type="text/javascript" src="/olat/secstatic/qti/74579818809617/_unzipped_/fibautocompl.js"></script>
            sb.append("<script type=\"text/javascript\" src=\"").append(autcompResolver.getStaticsBaseURI()).append("/")
                    .append(ImsRepositoryResolver.QTI_FIB_AUTOCOMPLETE_JS_FILE).append("\"></script>\n");
            // must be like <link rel="StyleSheet" href="/olat/secstatic/qti/74579818809617/_unzipped_/fibautocompl.css" type="text/css" media="screen, print">
            sb.append("<link rel=\"StyleSheet\" href=\"").append(autcompResolver.getStaticsBaseURI()).append("/")
                    .append(ImsRepositoryResolver.QTI_FIB_AUTOCOMPLETE_CSS_FILE).append("\" type=\"text/css\" media=\"screen\" >\n");
            final JSAndCSSComponent autoCompleteJsCss = new JSAndCSSComponent("auto_complete_js_css", this.getClass(), null, null, true, sb.toString());
            myContent.put("autoCompleteJsCss", autoCompleteJsCss);
        }
        closeButton = LinkFactory.createButton("close", myContent, this);

        qtiscoreprogress = new ProgressBar("qtiscoreprogress", 150, 0, 0, "");
        myContent.put("qtiscoreprogress", qtiscoreprogress);
        Boolean displayScoreProgress = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_SCOREPROGRESS);
        if (displayScoreProgress == null) {
            displayScoreProgress = Boolean.TRUE; // migration,
        }
        // display
        // menu
        if (!displayScoreProgress.booleanValue()) {
            qtiscoreprogress.setVisible(false);
        }
        myContent.contextPut("displayScoreProgress", displayScoreProgress);

        qtiquestionprogress = new ProgressBar("qtiquestionprogress", 150, 0, 0, "");
        myContent.put("qtiquestionprogress", qtiquestionprogress);
        Boolean displayQuestionProgress = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_QUESTIONPROGRESS);
        if (displayQuestionProgress == null) {
            displayQuestionProgress = Boolean.FALSE; // migration,
                                                     // don't
                                                     // display
                                                     // progress
        }

        if (!displayQuestionProgress.booleanValue()) {
            qtiquestionprogress.setVisible(false);
        }
        myContent.contextPut("displayQuestionProgress", displayQuestionProgress);

        Boolean displayMenu = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
        if (displayMenu == null) {
            displayMenu = Boolean.TRUE; // migration
        }
        myContent.contextPut("displayMenu", displayMenu);

        Boolean enableCancel = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
        if (enableCancel == null) {
            if (modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
                enableCancel = Boolean.FALSE; // migration:
            } else {
                enableCancel = Boolean.TRUE; // migration: enable otherwise
            }
        }
        myContent.contextPut("enableCancel", enableCancel);

        Boolean enableSuspend = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
        if (enableSuspend == null) {
            enableSuspend = Boolean.FALSE; // migration
        }
        myContent.contextPut("enableSuspend", enableSuspend);

        qtistatus = new IQStatus(translator);
        qtistatus.setPreview(iqsec.isPreview());
        myContent.contextPut("qtistatus", qtistatus);

        setInitialComponent(myContent);

        IqDisplayParameterObjectEBL iqDisplayParameterObject = new IqDisplayParameterObjectEBL(repositorySoftkey, this.callingResId, this.callingResDetail,
                ureq.getIdentity(), modConfig, iqsec.isPreview(), resolver, persister);

        AssessmentInstance assessment = getIqDisplayEBL().getAssessment(iqDisplayParameterObject);

        if (!iqsec.isAllowed(assessment)) { // security check
            getWindowControl().setError(translator.translate("status.notallowed"));
            return;
        }

        if (iqsec.attemptsLeft(assessment) < 1) { // security check
            // note: important: do not check on == 0 since the nr of attempts can be
            // republished for the same test with a smaller number as the latest time.
            getWindowControl().setInfo(translator.translate(assessment.isSurvey() ? "status.survey.nomoreattempts" : "status.assess.nomoreattempts"));
            return;
        }

        if (assessment.isResuming()) {
            getWindowControl().setInfo(translator.translate(assessment.isSurvey() ? "status.survey.resumed" : "status.assess.resumed"));
        }

        assessment.setPreview(iqsec.isPreview());

        /*
         * menu render option: render only section titles or titles and questions.
         */
        Object tmp = modConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
        Boolean renderSectionsOnly;
        if (tmp == null) {
            // migration
            modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
            renderSectionsOnly = Boolean.FALSE;
        } else {
            renderSectionsOnly = (Boolean) tmp;
        }
        final boolean enabledMenu = ((Boolean) modConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU)).booleanValue();
        final boolean itemPageSequence = ((String) modConfig.get(IQEditController.CONFIG_KEY_SEQUENCE)).equals(AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
        final IQMenuDisplayConf mdc = new IQMenuDisplayConf(renderSectionsOnly.booleanValue(), enabledMenu, itemPageSequence);

        tmp = modConfig.get(IQEditController.CONFIG_KEY_MEMO);
        final boolean memo = tmp == null ? false : ((Boolean) tmp).booleanValue();

        qticomp = new IQComponent("qticomponent", translator, assessment, mdc, memo);

        qticomp.addListener(this);
        myContent.put("qticomp", qticomp);
        if (!assessment.isResuming()) {
            final Navigator navigator = assessment.getNavigator();
            navigator.startAssessment();
        }

        qtistatus.update(assessment);
        if (!qtistatus.isSurvey()) {
            qtiscoreprogress.setMax(assessment.getAssessmentContext().getMaxScore());
            qtiscoreprogress.setActual(assessment.getAssessmentContext().getScore());
        }

        qtiquestionprogress.setMax(Integer.parseInt(qtistatus.getMaxQuestions()));
        updateQuestionProgressDisplay(assessment);

        ready = true;
    }

    private IqDisplayEBL getIqDisplayEBL() {
        return CoreSpringFactory.getBean(IqDisplayEBL.class);
    }

    /**
     * Wether the qti is ready to be launched.
     * 
     * @return boolean
     */
    public boolean isReady() {
        return ready;
    }

    private void updateQuestionProgressDisplay(final AssessmentInstance ai) {

        final int answered = ai.getAssessmentContext().getItemsAnsweredCount();
        qtiquestionprogress.setActual(answered);
        qtistatus.setQuestionProgressLabel(translator.translate("question.progress.answered", new String[] { "" + ai.getAssessmentContext().getItemsAnsweredCount(),
                qtistatus.getMaxQuestions() }));
    }

    /**
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        if (source == myContent || source == qticomp) { // those must be links
            final String wfCommand = event.getCommand();
            // process workflow
            final AssessmentInstance ai = qticomp.getAssessmentInstance();
            if (qticomp == null || ai == null) {
                throw new RuntimeException("AssessmentInstance not valid.");
            }

            final Navigator navig = ai.getNavigator();

            if (wfCommand.equals("mark")) {
                ai.mark(ureq.getParameter("id"), "true".equals(ureq.getParameter("p")));
                ai.persist();
                return;
            }

            if (wfCommand.equals("memo")) {
                ai.setMemo(ureq.getParameter("id"), ureq.getParameter("p"));
                ai.persist();
                return;
            }

            logAudit(ureq);

            if (wfCommand.equals("sitse")) { // submitItemOrSection
                if (IQComponentRenderer.OLAT6824Logger.isDebugEnabled()) {
                    IQComponentRenderer.OLAT6824Logger.debug("OLAT-6824 submit request [" + ureq.getHttpReq().getRequestURL() + ": "
                            + ureq.getHttpReq().getParameterMap() + "]");
                }

                navig.submitItems(iqm.getItemsInput(ureq)); //
                if (ai.isClosed()) { // do all the finishing stuff
                    event(ureq, source, new Event(QTIConstants.QTI_WF_SUBMIT));
                    return;
                }
            } else if (wfCommand.equals("sflash")) { // submit flash answer
                navig.submitItems(iqm.getItemsInput(ureq)); //
                if (ai.isClosed()) { // do all the finishing stuff
                    event(ureq, source, new Event(QTIConstants.QTI_WF_SUBMIT));
                    return;
                }
            } else if (wfCommand.equals("git")) { // goToItem
                final String seid = ureq.getParameter("seid");
                final String itid = ureq.getParameter("itid");
                if (seid != null && seid.length() != 0 && itid != null && itid.length() != 0) {
                    final int sectionPos = Integer.parseInt(seid);
                    final int itemPos = Integer.parseInt(itid);
                    navig.goToItem(sectionPos, itemPos);
                }
            } else if (wfCommand.equals("gitnext")) { // goToNextItem
                // newly implemented for bug:
                // http://bugs.olat.org/jira/browse/OLAT-6617
                // in order to display feedback of CURRENT item we won't proceed automatically
                // after editing an item. This is triggered by the user via the "next" button which executes the "gitnext" command.
                // This applies only to the SequentialItemNavigator in case navigation is not displayed or is not editable
                // and only one item per page is shown.
                ((SequentialItemNavigator) navig).goToNextItem();
                if (ai.isClosed()) { // do all the finishing stuff
                    event(ureq, source, new Event(QTIConstants.QTI_WF_SUBMIT));
                    return;
                }
            } else if (wfCommand.equals("gse")) { // goToSection
                final String seid = ureq.getParameter("seid");
                if (seid != null && seid.length() != 0) {
                    final int sectionPos = Integer.parseInt(seid);
                    navig.goToSection(sectionPos);
                }
            } else if (wfCommand.equals(QTIConstants.QTI_WF_SUBMIT)) { // submit
                                                                       // Assessment
                navig.submitAssessment();
                // Persist data in all cases: test, selftest, surveys except previews
                // In case of survey, data will be anonymized when reading from the
                // table (using the archiver)
                if (!qtistatus.isPreview()) {
                    iqm.persistResults(ai, callingResId, callingResDetail, ureq);
                    getWindowControl().setInfo(translator.translate("status.results.saved"));
                } else {
                    getWindowControl().setInfo(translator.translate("status.results.notsaved"));
                }

                // TODO Lavinia asessment: save results to file system before DB
                if (!qtistatus.isSurvey()) { // for test and self-assessment, generate
                                             // detailed results
                    final Document docResReporting = iqm.getResultsReporting(ai, ureq);
                    if (!iqsec.isPreview()) {
                        FilePersister.createResultsReporting(docResReporting, ureq.getIdentity(), ai.getFormattedType(), ai.getAssessID());
                        // Send score and passed to parent controller. Maybe it is necessary
                        // to save some data there
                        // Do this now and not later, maybe user will never click on
                        // 'close'...
                        final AssessmentContext ac = ai.getAssessmentContext();
                        fireEvent(ureq, new IQSubmittedEvent(ac.getScore(), ac.isPassed(), ai.getAssessID()));
                    }

                    final Boolean showResultsOnFinishObj = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
                    final boolean showResultsOnFinish = showResultsOnFinishObj == null || showResultsOnFinishObj != null && showResultsOnFinishObj.booleanValue();
                    if (ai.getSummaryType() == AssessmentInstance.SUMMARY_NONE || !showResultsOnFinish) {
                        // do not display results reporting
                        myContent.contextPut("displayreporting", Boolean.FALSE);
                    } else { // display results reporting
                        final String resReporting = iqm.transformResultsReporting(docResReporting, ureq.getLocale(), ai.getSummaryType());
                        myContent.contextPut("resreporting", resReporting);
                        myContent.contextPut("displayreporting", Boolean.TRUE);
                    }
                    myContent.setPage(VELOCITY_ROOT + "/result.html");
                } else {
                    // Send also finished event in case of survey
                    fireEvent(ureq, new IQSubmittedEvent());
                }
            } else if (wfCommand.equals(QTIConstants.QTI_WF_CANCEL)) { // cancel
                                                                       // assessment
                navig.cancelAssessment();
            } else if (wfCommand.equals(QTIConstants.QTI_WF_SUSPEND)) { // suspend
                                                                        // assessment
                // just close the controller
                fireEvent(ureq, Event.DONE_EVENT);
                return;
            } else if (wfCommand.equals("close")) {
                qtistatus.update(null);
                // Parent controller need to pop, if they pushed previously
                fireEvent(ureq, Event.DONE_EVENT);
                return;
            }
            qtistatus.update(ai);
            if (!qtistatus.isSurvey()) {
                qtiscoreprogress.setActual(ai.getAssessmentContext().getScore());
            }

            updateQuestionProgressDisplay(ai);

        } else if (source == closeButton) { // close component
            qtistatus.update(null);
            // Parent controller need to pop, if they pushed previously
            fireEvent(ureq, Event.DONE_EVENT);
            return;
        }
    }

    /**
     * @param ureq
     */
    private void logAudit(final UserRequest ureq) {
        final Set<String> params = ureq.getParameterSet();
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<String> iter = params.iterator(); iter.hasNext();) {
            final String paramName = iter.next();
            sb.append("|");
            sb.append(paramName);
            sb.append("=");
            sb.append(ureq.getParameter(paramName));
        }

        log.info("qti audit logging: hreq=" + ureq.getHttpReq().getRequestURL() + ", params=" + sb.toString());

        final String command = ureq.getParameter("cid");

        final String qtiDetails = LoggingResourceable.restrictStringLength("cid=" + command + StringHelper.stripLineBreaks(sb.toString()),
                LoggingResourceable.MAX_NAME_LEN);
        ThreadLocalUserActivityLogger.log(QTILoggingAction.QTI_AUDIT, getClass(),
                LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiParams, "", qtiDetails));
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // we are finished with the test, allow the use of the browser-back button again.
        getWindowControl().getWindowBackOffice().releaseBackHandling(this);
    }

    /**
	 * 
	 */
    @Override
    public void browserBackOrForward(final UserRequest ureq, final int diff) {
        // user is using back -> simply warn, do nothing more.
        getWindowControl().setWarning(translator.translate("test.nobackusage"));
    }
}
