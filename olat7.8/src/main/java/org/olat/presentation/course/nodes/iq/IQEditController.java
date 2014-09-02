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

import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.condition.Condition;
import org.olat.lms.course.nodes.AbstractAccessableCourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.lms.course.nodes.IQSELFCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.nodes.QtiEBL;
import org.olat.lms.course.nodes.TestConfiguration;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.tree.CourseInternalLinkTreeModel;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.ims.qti.IQManager;
import org.olat.lms.ims.qti.IQPreviewSecurityCallback;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.presentation.commons.filechooser.FileChooseCreateEditController;
import org.olat.presentation.commons.filechooser.LinkChooseCreateEditController;
import org.olat.presentation.course.condition.ConditionEditController;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.course.nodes.ta.ConfirmationSettingForm;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.presentation.repository.ReferencableEntriesSearchController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR/>
 * Edit controller for the qti test, selftest and survey course node
 * <P/>
 * Initial Date: Oct 13, 2004
 * 
 * @author Felix Jost
 */
public class IQEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

    public final String PANE_TAB_IQCONFIG_XXX;
    public static final String PANE_TAB_IQCONFIG_SURV = "pane.tab.iqconfig.surv";
    public static final String PANE_TAB_IQCONFIG_SELF = "pane.tab.iqconfig.self";
    public static final String PANE_TAB_IQCONFIG_TEST = "pane.tab.iqconfig.test";
    public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
    private static final String VC_CHOSENTEST = "chosentest";
    private static final String ACTION_CORRECT = "correcttest";
    /** configuration key: the disclaimer text */
    public static final String CONFIG_KEY_DISCLAIMER = "disc";
    /** configuration key: enable menu switch */
    public static final String CONFIG_KEY_ENABLEMENU = "enableMenu";
    /** configuration key: display menu switch */
    public static final String CONFIG_KEY_DISPLAYMENU = "displayMenu";
    /** configuration key: all questions, section titles only */
    public static final String CONFIG_KEY_RENDERMENUOPTION = "renderMenu";
    /** configuration key: enable score progress switch */
    public static final String CONFIG_KEY_SCOREPROGRESS = "displayScoreProgress";
    /** configuration key: enable cancel switch */
    public static final String CONFIG_KEY_ENABLECANCEL = "enableCancel";
    /** configuration key: enable suspend switch */
    public static final String CONFIG_KEY_ENABLESUSPEND = "enableSuspend";
    /** configuration key: enable question progress switch */
    public static final String CONFIG_KEY_QUESTIONPROGRESS = "displayQuestionProgss";
    /** configuration key: enable question progress switch */
    public static final String CONFIG_KEY_QUESTIONTITLE = "displayQuestionTitle";
    /** configuration key: enable automatic enumeration of "choice" options */
    public static final String CONFIG_KEY_AUTOENUM_CHOICES = "autoEnumerateChoices";
    /** configuration key: provide memo field */
    public static final String CONFIG_KEY_MEMO = "provideMemoField";
    /** configuration key: question sequence: item or selection */
    public static final String CONFIG_KEY_SEQUENCE = "sequence";
    /** configuration key: mode */
    public static final String CONFIG_KEY_TYPE = "mode";
    /** configuration key: show summary: compact or detailed */
    public static final String CONFIG_KEY_SUMMARY = "summary";
    /** configuration key: max attempts */
    public static final String CONFIG_KEY_ATTEMPTS = "attempts";
    /** configuration key: minimal score */
    public static final String CONFIG_KEY_MINSCORE = "minscore";
    /** configuration key: maximal score */
    public static final String CONFIG_KEY_MAXSCORE = "maxscore";
    /** configuration key: cut value (socre > cut = passed) */
    public static final String CONFIG_KEY_CUTVALUE = "cutvalue";
    /** configuration key for the filename */
    public static final String CONFIG_KEY_FILE = "file";
    /** configuration key: should relative links like ../otherfolder/my.css be allowed? **/
    public static final String CONFIG_KEY_ALLOW_RELATIVE_LINKS = "allowRelativeLinks";
    /** configuration key: enable 'show score infos' on start page */
    public static final String CONFIG_KEY_ENABLESCOREINFO = "enableScoreInfo";

    public static final String CONFIG_KEY_DATE_DEPENDENT_RESULTS = "dateDependentResults";
    public static final String CONFIG_KEY_RESULTS_START_DATE = "resultsStartDate";
    public static final String CONFIG_KEY_RESULTS_END_DATE = "resultsEndDate";
    public static final String CONFIG_KEY_RESULT_ON_FINISH = "showResultsOnFinish";
    public static final String CONFIG_KEY_RESULT_ON_HOME_PAGE = "showResultsOnHomePage";

    private final String[] paneKeys;

    private ModuleConfiguration moduleConfiguration;
    private Panel main;
    private VelocityContainer myContent;

    private IQEditForm modConfigForm;
    private ReferencableEntriesSearchController searchController;
    private ICourse course;
    private ConditionEditController accessibilityCondContr;
    private AbstractAccessableCourseNode courseNode;
    private final String type;
    private UserCourseEnvironment euce;
    private TabbedPane myTabbedPane;
    private FileChooseCreateEditController fccecontr;
    private Controller correctQTIcontroller;
    private Boolean allowRelativeLinks;
    private Link previewLink;
    private Link chooseTestButton;
    private Link changeTestButton;
    private LayoutMain3ColsPreviewController previewLayoutCtr;
    private CloseableModalController cmc;
    private Link editTestButton;
    private QtiEBL qtiEbl;
    private ConfirmationSettingForm confirmationSettingForm;
    private TestReplacer testReplacer;

    /**
     * Constructor for the IMS QTI edit controller for a test course node
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window controller
     * @param course
     *            The course
     * @param courseNode
     *            The test course node
     * @param groupMgr
     * @param euce
     */
    IQEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final IQTESTCourseNode courseNode, final CourseGroupManager groupMgr,
            final UserCourseEnvironment euce) {
        super(ureq, wControl);
        initFields(course, courseNode, euce);
        this.type = AssessmentInstance.QMD_ENTRY_TYPE_ASSESS;
        this.PANE_TAB_IQCONFIG_XXX = PANE_TAB_IQCONFIG_TEST;
        paneKeys = new String[] { PANE_TAB_IQCONFIG_XXX, PANE_TAB_ACCESSIBILITY };
        // put some default values
        if (moduleConfiguration.get(CONFIG_KEY_ENABLECANCEL) == null) {
            moduleConfiguration.set(CONFIG_KEY_ENABLECANCEL, Boolean.FALSE);
        }
        if (moduleConfiguration.get(CONFIG_KEY_ENABLESUSPEND) == null) {
            moduleConfiguration.set(CONFIG_KEY_ENABLESUSPEND, Boolean.FALSE);
        }
        if (moduleConfiguration.get(CONFIG_KEY_RENDERMENUOPTION) == null) {
            moduleConfiguration.set(CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
        }

        initIqEditPanel(ureq, groupMgr, wControl);
        myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
        myContent.contextPut("type", this.type);
    }

    /**
     * Constructor for the IMS QTI edit controller for a self-test course node
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window controller
     * @param course
     *            The course
     * @param courseNode
     *            The self course node
     * @param groupMgr
     * @param euce
     */
    IQEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final IQSELFCourseNode courseNode, final CourseGroupManager groupMgr,
            final UserCourseEnvironment euce) {
        super(ureq, wControl);
        initFields(course, courseNode, euce);
        this.type = AssessmentInstance.QMD_ENTRY_TYPE_SELF;
        this.PANE_TAB_IQCONFIG_XXX = PANE_TAB_IQCONFIG_SELF;
        paneKeys = new String[] { PANE_TAB_IQCONFIG_XXX, PANE_TAB_ACCESSIBILITY };
        // put some default values
        if (moduleConfiguration.get(CONFIG_KEY_ENABLECANCEL) == null) {
            moduleConfiguration.set(CONFIG_KEY_ENABLECANCEL, Boolean.TRUE);
        }
        if (moduleConfiguration.get(CONFIG_KEY_ENABLESUSPEND) == null) {
            moduleConfiguration.set(CONFIG_KEY_ENABLESUSPEND, Boolean.TRUE);
        }

        initIqEditPanel(ureq, groupMgr, wControl);
        myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
        myContent.contextPut("type", this.type);
    }

    /**
     * Constructor for the IMS QTI edit controller for a survey course node
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window controller
     * @param course
     *            The course
     * @param courseNode
     *            The survey course node
     * @param groupMgr
     * @param euce
     */
    IQEditController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final IQSURVCourseNode courseNode, final CourseGroupManager groupMgr,
            final UserCourseEnvironment euce) {
        super(ureq, wControl);
        initFields(course, courseNode, euce);
        this.type = AssessmentInstance.QMD_ENTRY_TYPE_SURVEY;
        this.PANE_TAB_IQCONFIG_XXX = PANE_TAB_IQCONFIG_SURV;
        paneKeys = new String[] { PANE_TAB_IQCONFIG_XXX, PANE_TAB_ACCESSIBILITY };

        // put some default values
        if (moduleConfiguration.get(CONFIG_KEY_SCOREPROGRESS) == null) {
            moduleConfiguration.set(CONFIG_KEY_SCOREPROGRESS, Boolean.FALSE);
        }
        if (moduleConfiguration.getBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS) == null) {
            moduleConfiguration.setBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS, false);
        }

        initIqEditPanel(ureq, groupMgr, wControl);
        myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
        myContent.contextPut("type", this.type);
        chooseTestButton.setCustomDisplayText(translate("command.createSurvey"));
    }

    private void initFields(final ICourse course, final AbstractAccessableCourseNode courseNode, final UserCourseEnvironment euce) {

        qtiEbl = CoreSpringFactory.getBean(QtiEBL.class);
        this.moduleConfiguration = courseNode.getModuleConfiguration();
        // o_clusterOk by guido: save to hold reference to course inside editor
        this.course = course;
        this.courseNode = courseNode;
        this.euce = euce;
    }

    private void initIqEditPanel(final UserRequest ureq, final CourseGroupManager groupMgr, final WindowControl wControl) {
        main = new Panel("iqeditpanel");

        myContent = this.createVelocityContainer("edit");
        chooseTestButton = LinkFactory.createButtonSmall("command.chooseRepFile", myContent, this);
        changeTestButton = LinkFactory.createButtonSmall("command.changeRepFile", myContent, this);

        testReplacer = getTestReplacer();
        if (testReplacer.hasStoredResults()) {
            // show how many have already finished the test
            myContent.contextPut("identitiesPassedOrStartedTest", new Boolean(true));
            String[] vars = new String[2];
            vars[0] = String.valueOf(testReplacer.getFinishedLearnersSize());
            vars[1] = "private/archive"; // TODO: add link to Home/Personal folder/private/archive
            String qtiReplaceInfo = translate("qti.replace.information", vars);
            if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
                qtiReplaceInfo = translate("qti.questionnaire.replace.information", vars);
            }
            myContent.contextPut("qtiReplaceInfo", qtiReplaceInfo);
        }

        modConfigForm = new IQEditForm(ureq, wControl, moduleConfiguration);
        listenTo(modConfigForm);
        myContent.put("iqeditform", modConfigForm.getInitialComponent());

        // fetch repository entry
        RepositoryEntry re = null;
        final String repoSoftkey = (String) moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
        if (repoSoftkey != null) {
            re = courseNode.getReferencedRepositoryEntry();
        }
        myContent.contextPut(VC_CHOSENTEST, re == null ? translate("no.file.chosen") : re.getDisplayname());
        if (re != null) {
            if (qtiEbl.isEditable(ureq.getIdentity(), re)) {
                editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
            }
            myContent.contextPut("dontRenderRepositoryButton", new Boolean(true));
            // Put values to velocity container
            myContent.contextPut(CONFIG_KEY_MINSCORE, moduleConfiguration.get(CONFIG_KEY_MINSCORE));
            myContent.contextPut(CONFIG_KEY_MAXSCORE, moduleConfiguration.get(CONFIG_KEY_MAXSCORE));
            myContent.contextPut(CONFIG_KEY_CUTVALUE, moduleConfiguration.get(CONFIG_KEY_CUTVALUE));
            previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, myContent, this);
            previewLink.setCustomEnabledLinkCSS("b_preview");
            previewLink.setTitle(getTranslator().translate("command.preview"));
        }

        final String disclaimer = (String) moduleConfiguration.get(CONFIG_KEY_DISCLAIMER);
        // allowRelativeLinks = courseNode.getModuleConfiguration().getBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS);

        final String legend = translate("fieldset.chosecreateeditfile");

        allowRelativeLinks = moduleConfiguration.getBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS);
        if (allowRelativeLinks == null) {
            allowRelativeLinks = Boolean.FALSE;
        }
        fccecontr = new LinkChooseCreateEditController(ureq, wControl, disclaimer, allowRelativeLinks, course.getCourseFolderContainer(), type, legend,
                new CourseInternalLinkTreeModel(course.getEditorTreeModel()));
        this.listenTo(fccecontr);

        final Component fcContent = fccecontr.getInitialComponent();
        myContent.put("filechoosecreateedit", fcContent);

        final Condition accessCondition = courseNode.getPreConditionAccess();
        accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), groupMgr, accessCondition, "accessabilityConditionForm",
                AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode), euce);
        this.listenTo(accessibilityCondContr);

        if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
            confirmationSettingForm = new ConfirmationSettingForm(ureq, wControl, isConfirmationEnabled(), "fieldset.dropbox.title.test", "form.dropbox.enablemail.test");
            listenTo(confirmationSettingForm);
            myContent.put("confirmationSettingForm", confirmationSettingForm.getInitialComponent());
        }

        main.setContent(myContent);
        // not needed for tabbledController: setInitialComponent(main);
    }

    private boolean isConfirmationEnabled() {
        Object isConfirmationEnabled = courseNode.getModuleConfiguration().get(IQTESTCourseNode.CONFIRMATION_REQUESTED);
        if (isConfirmationEnabled != null) {
            return (Boolean) isConfirmationEnabled;
        }
        return false;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == myContent) {
            if (event.getCommand().equals(ACTION_CORRECT)) {
                /*
                 * FIXME:pb:remove this elseif code, as the use case "correct" is started from details view only - check if the test is in use at the moment - check if
                 * already results exist - check if test is referenced from other courses
                 */
                final String repoSoftKey = (String) moduleConfiguration.get(ModuleConfiguration.CONFIG_KEY_REPOSITORY_SOFTKEY);
                final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(repoSoftKey, false);
                if (re == null) {
                    // not found
                } else {
                    final RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
                    final OLATResourceable ores = re.getOlatResource();
                    correctQTIcontroller = typeToEdit.createEditorController(ores, ureq, this.getWindowControl());
                    this.getWindowControl().pushToMainArea(correctQTIcontroller.getInitialComponent());
                    this.listenTo(correctQTIcontroller);
                }
            }
        } else if (source == previewLink) {
            // handle preview
            if (previewLayoutCtr != null) {
                previewLayoutCtr.dispose();
            }
            final Controller previewController = IQManager.getInstance().createIQDisplayController(moduleConfiguration, new IQPreviewSecurityCallback(), ureq,
                    getWindowControl(), course.getResourceableId().longValue(), courseNode.getIdent());
            previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, null, previewController.getInitialComponent(), null);
            previewLayoutCtr.addDisposableChildController(previewController);
            previewLayoutCtr.activate();

        } else if (source == chooseTestButton) {// initiate search controller
            if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
                searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, SurveyFileResource.TYPE_NAME, translate("command.chooseSurvey"));
            } else { // test and selftest use same repository resource type
                searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, TestFileResource.TYPE_NAME, translate("command.chooseTest"));
            }
            this.listenTo(searchController);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("command.chooseRepFile"));
            cmc.activate();
        } else if (source == changeTestButton) {// change associated test
            changeTestOrSurvey(ureq);
        } else if (source == editTestButton) {
            CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, courseNode);
        }
    }

    private void changeTestOrSurvey(final UserRequest ureq) {
        if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)) {// selftest
            final String[] types = new String[] { TestFileResource.TYPE_NAME };
            searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
            this.listenTo(searchController);
        } else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) | type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {// test, survey
            String[] types;
            if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {// test
                types = new String[] { TestFileResource.TYPE_NAME };
            } else {// survey
                types = new String[] { SurveyFileResource.TYPE_NAME };
            }

            if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {// test
                searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
            } else {// survey
                searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseSurvey"));
            }
            this.listenTo(searchController);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
        }
        cmc.activate();
    }

    private TestReplacer getTestReplacer() {
        return TestReplacer.createTestReplacer(course, courseNode);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (source.equals(searchController)) {
            if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
                // repository search controller done
                cmc.deactivate();
                if (testReplacer.hasAnyStoredData()) {
                    testReplacer.exportResults(urequest, course.getCourseTitle(), courseNode);
                    testReplacer.removeTestData();
                    testReplacer.sendConfirmation(urequest.getIdentity(), type);
                }
                final RepositoryEntry re = searchController.getSelectedEntry();
                doIQReference(urequest, re);
            }
        } else if (source == accessibilityCondContr) {
            if (event == Event.CHANGED_EVENT) {
                final Condition cond = accessibilityCondContr.getCondition();
                courseNode.setPreConditionAccess(cond);
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == fccecontr) {
            if (event == FileChooseCreateEditController.FILE_CHANGED_EVENT) {
                final String chosenFile = fccecontr.getChosenFile();
                if (chosenFile != null) {
                    moduleConfiguration.set(CONFIG_KEY_DISCLAIMER, fccecontr.getChosenFile());
                } else {
                    moduleConfiguration.remove(CONFIG_KEY_DISCLAIMER);
                }
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            } else if (event == FileChooseCreateEditController.ALLOW_RELATIVE_LINKS_CHANGED_EVENT) {
                allowRelativeLinks = fccecontr.getAllowRelativeLinks();
                courseNode.getModuleConfiguration().setBooleanEntry(CONFIG_KEY_ALLOW_RELATIVE_LINKS, allowRelativeLinks.booleanValue());
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
            }
        } else if (source == correctQTIcontroller) {
            if (event == Event.DONE_EVENT) {
                // getWindowControl().pop();
            }
        } else if (source == modConfigForm) { // config form action
            if (event == Event.CANCELLED_EVENT) {
                return;
            } else if (event == Event.DONE_EVENT) {

                moduleConfiguration.set(CONFIG_KEY_DISPLAYMENU, new Boolean(modConfigForm.isDisplayMenu()));

                if (modConfigForm.isDisplayMenu()) {
                    moduleConfiguration.set(CONFIG_KEY_RENDERMENUOPTION, modConfigForm.isMenuRenderSectionsOnly());
                    moduleConfiguration.set(CONFIG_KEY_ENABLEMENU, new Boolean(modConfigForm.isEnableMenu()));
                } else {
                    // set default values when menu is not displayed
                    moduleConfiguration.set(CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
                    moduleConfiguration.set(CONFIG_KEY_ENABLEMENU, Boolean.FALSE);
                }

                moduleConfiguration.set(CONFIG_KEY_QUESTIONPROGRESS, new Boolean(modConfigForm.isDisplayQuestionProgress()));
                moduleConfiguration.set(CONFIG_KEY_SEQUENCE, modConfigForm.getSequence());
                moduleConfiguration.set(CONFIG_KEY_ENABLECANCEL, new Boolean(modConfigForm.isEnableCancel()));
                moduleConfiguration.set(CONFIG_KEY_ENABLESUSPEND, new Boolean(modConfigForm.isEnableSuspend()));
                moduleConfiguration.set(CONFIG_KEY_QUESTIONTITLE, new Boolean(modConfigForm.isDisplayQuestionTitle()));
                moduleConfiguration.set(CONFIG_KEY_AUTOENUM_CHOICES, new Boolean(modConfigForm.isAutoEnumChoices()));
                moduleConfiguration.set(CONFIG_KEY_MEMO, new Boolean(modConfigForm.isProvideMemoField()));
                // Only tests and selftests have summaries and score progress
                if (!type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
                    moduleConfiguration.set(CONFIG_KEY_SUMMARY, modConfigForm.getSummary());
                    moduleConfiguration.set(CONFIG_KEY_SCOREPROGRESS, new Boolean(modConfigForm.isDisplayScoreProgress()));
                    moduleConfiguration.set(CONFIG_KEY_ENABLESCOREINFO, new Boolean(modConfigForm.isEnableScoreInfo()));
                    moduleConfiguration.set(CONFIG_KEY_DATE_DEPENDENT_RESULTS, new Boolean(modConfigForm.isShowResultsDateDependent()));
                    moduleConfiguration.set(CONFIG_KEY_RESULTS_START_DATE, modConfigForm.getShowResultsStartDate());
                    moduleConfiguration.set(CONFIG_KEY_RESULTS_END_DATE, modConfigForm.getShowResultsEndDate());
                    moduleConfiguration.set(CONFIG_KEY_RESULT_ON_FINISH, modConfigForm.isShowResultsAfterFinishTest());
                    moduleConfiguration.set(CONFIG_KEY_RESULT_ON_HOME_PAGE, modConfigForm.isShowResultsOnHomePage());
                }
                // Only tests have a limitation on number of attempts
                if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
                    moduleConfiguration.set(CONFIG_KEY_ATTEMPTS, modConfigForm.getAttempts());
                }

                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                return;
            }
        } else if (source == confirmationSettingForm) {
            if (event == Event.CANCELLED_EVENT) {
                return;
            } else if (event == Event.DONE_EVENT) {
                moduleConfiguration.set(IQTESTCourseNode.CONFIRMATION_REQUESTED, new Boolean(confirmationSettingForm.mailEnabled()));
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                return;
            }
        }
    }

    private void doIQReference(final UserRequest urequest, final RepositoryEntry re) {
        // repository search controller done
        if (re != null) {
            if (getLockingService().isLocked(re.getOlatResource(), null)) {
                this.showError("error.entry.locked");
            } else {
                courseNode.setRepositoryReference(re);
                previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", re.getDisplayname(), Link.NONTRANSLATED, myContent, this);
                previewLink.setCustomEnabledLinkCSS("b_preview");
                previewLink.setTitle(getTranslator().translate("command.preview"));
                myContent.contextPut("dontRenderRepositoryButton", new Boolean(true));
                // If of type test, get min, max, cut - put in module config and push
                // to velocity
                if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
                    updateModuleConfigFromQTIFile(re.getOlatResource());
                    // Put values to velocity container
                    myContent.contextPut(CONFIG_KEY_MINSCORE, moduleConfiguration.get(CONFIG_KEY_MINSCORE));
                    myContent.contextPut(CONFIG_KEY_MAXSCORE, moduleConfiguration.get(CONFIG_KEY_MAXSCORE));
                    myContent.contextPut(CONFIG_KEY_CUTVALUE, moduleConfiguration.get(CONFIG_KEY_CUTVALUE));
                }
                if (qtiEbl.isEditable(urequest.getIdentity(), re)) {
                    editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
                }
                fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
                if (!testReplacer.hasStoredResults()) {
                    myContent.contextPut("identitiesPassedOrStartedTest", new Boolean(false));
                }
            }
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
	 */
    @Override
    public void addTabs(final TabbedPane tabbedPane) {
        myTabbedPane = tabbedPane;
        tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
        // PANE_TAB_IQCONFIG_XXX is set during construction time
        tabbedPane.addTab(translate(PANE_TAB_IQCONFIG_XXX), main);
    }

    /**
     * Update the module configuration from the qti file: read min/max/cut values
     * 
     * @param res
     */
    private void updateModuleConfigFromQTIFile(final OLATResource res) {
        TestConfiguration testConfiguration = qtiEbl.getTestConfiguration(res);
        // Put values to module configuration
        moduleConfiguration.set(CONFIG_KEY_MINSCORE, testConfiguration.getMinValue());
        moduleConfiguration.set(CONFIG_KEY_MAXSCORE, testConfiguration.getMaxValue());
        moduleConfiguration.set(CONFIG_KEY_CUTVALUE, testConfiguration.getCutValue());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controllers registered with listenTo() get disposed in BasicController
        if (previewLayoutCtr != null) {
            previewLayoutCtr.dispose();
            previewLayoutCtr = null;
        }
    }

    @Override
    public String[] getPaneKeys() {
        return paneKeys;
    }

    @Override
    public TabbedPane getTabbedPane() {
        return myTabbedPane;
    }

}
