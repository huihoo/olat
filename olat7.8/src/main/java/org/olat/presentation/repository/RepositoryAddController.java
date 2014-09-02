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

package org.olat.presentation.repository;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.LearningResourceLoggingAction;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.fileresource.BlogFileResource;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.commons.fileresource.ImsCPFileResource;
import org.olat.lms.commons.fileresource.PodcastFileResource;
import org.olat.lms.commons.fileresource.ScormCPFileResource;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.commons.fileresource.SurveyFileResource;
import org.olat.lms.commons.fileresource.TestFileResource;
import org.olat.lms.commons.fileresource.WikiResource;
import org.olat.lms.course.CourseModule;
import org.olat.lms.portfolio.EPTemplateMapResource;
import org.olat.lms.repository.RepositoryEBL;
import org.olat.lms.repository.RepositoryEntryInputData;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.BlogRepositoryHandler;
import org.olat.lms.repository.handlers.CourseRepositoryHandler;
import org.olat.lms.repository.handlers.GlossaryRepositoryHandler;
import org.olat.lms.repository.handlers.ImsCPRepositoryHandler;
import org.olat.lms.repository.handlers.PodcastRepositoryHandler;
import org.olat.lms.repository.handlers.PortfolioRepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.repository.handlers.SharedFolderRepositoryHandler;
import org.olat.lms.repository.handlers.WikiRepositoryHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class RepositoryAddController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    public static final String PROCESS_NEW = "new";
    public static final String PROCESS_ADD = "add";

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(RepositoryAddController.class);

    static final String ACTION_ADD_PREFIX = "a.";
    static final String ACTION_ADD_COURSE = ACTION_ADD_PREFIX + "co";
    static final String ACTION_ADD_CP = ACTION_ADD_PREFIX + "cp";
    static final String ACTION_ADD_SCORM = ACTION_ADD_PREFIX + "scorm";
    static final String ACTION_ADD_TEST = ACTION_ADD_PREFIX + "te";
    static final String ACTION_ADD_SURVEY = ACTION_ADD_PREFIX + "sv";
    static final String ACTION_ADD_WIKI = ACTION_ADD_PREFIX + "wiki";
    static final String ACTION_ADD_PODCAST = ACTION_ADD_PREFIX + "podcast";
    static final String ACTION_ADD_BLOG = ACTION_ADD_PREFIX + "blog";
    static final String ACTION_ADD_GLOSSARY = ACTION_ADD_PREFIX + "glossary";
    static final String ACTION_ADD_DOC = ACTION_ADD_PREFIX + "dc";
    static final String ACTION_NEW_COURSE = ACTION_ADD_PREFIX + "nco";
    static final String ACTION_NEW_CP = ACTION_ADD_PREFIX + "ncp";
    static final String ACTION_NEW_TEST = ACTION_ADD_PREFIX + "nte";
    static final String ACTION_NEW_SURVEY = ACTION_ADD_PREFIX + "nsu";
    static final String ACTION_NEW_SHAREDFOLDER = ACTION_ADD_PREFIX + "nsf";
    static final String ACTION_NEW_WIKI = ACTION_ADD_PREFIX + "nwiki";
    static final String ACTION_NEW_PODCAST = ACTION_ADD_PREFIX + "npodcast";
    static final String ACTION_NEW_BLOG = ACTION_ADD_PREFIX + "nblog";
    static final String ACTION_NEW_GLOSSARY = ACTION_ADD_PREFIX + "nglossary";
    static final String ACTION_NEW_PORTFOLIO = ACTION_ADD_PREFIX + "nportfolio";
    static final String ACTION_CANCEL = "cancel";
    static final String ACTION_FORWARD = "forward";

    private VelocityContainer repositoryadd;

    private RepositoryEditDescriptionController detailsController;
    private IAddController addController;
    private RepositoryHandler typeToAdd;
    private RepositoryAddCallback addCallback;
    private RepositoryEntry addedEntry;

    // flag is true when workflow has been finished successfully,
    // otherwise when disposing the controller or in a case of
    // user abort / cancel the system will delete temporary data
    private boolean workflowSuccessful = false;
    private Link cancelButton;
    private Link forwardButton;
    private Panel panel;
    private String actionAddCommand, actionProcess;
    private RepositoryEBL repositoryEBL;

    /**
     * Controller implementing "Add Repository Entry"-workflow.
     * 
     * @param ureq
     * @param wControl
     * @param actionAddCommand
     */
    public RepositoryAddController(final UserRequest ureq, final WindowControl wControl, final String actionAddCommand) {
        super(ureq, wControl);

        this.actionAddCommand = actionAddCommand;
        repositoryEBL = CoreSpringFactory.getBean(RepositoryEBL.class);
        /*
         * FIXME:pb: review: during constructor call -> /addDelegate.html is active first, then typeToAdd.getAddController() with this as addCallback may/should/must?
         * call protected finished(..); which in turn replaces /addDelegate.html by /addDetails.html.... what are the concepts here?
         */

        repositoryadd = createVelocityContainer("addDelegate");
        cancelButton = LinkFactory.createButton("cmd.cancel", repositoryadd, this);
        forwardButton = LinkFactory.createButton("cmd.forward", repositoryadd, this);

        String translatedTypeName = null;
        String typeIntro = null;
        addCallback = new RepositoryAddCallback(this);
        RepositoryHandlerFactory factory = (RepositoryHandlerFactory) CoreSpringFactory.getBean(RepositoryHandlerFactory.class);
        if (actionAddCommand.equals(ACTION_ADD_COURSE)) {
            typeToAdd = factory.getRepositoryHandler(CourseModule.getCourseTypeName());
            addController = typeToAdd.createAddController(addCallback, CourseRepositoryHandler.PROCESS_IMPORT, ureq, getWindowControl());
            translatedTypeName = translate("add.course");
            typeIntro = translate("add.course.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_DOC)) {
            typeToAdd = factory.getRepositoryHandler(FileResource.GENERIC_TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
            translatedTypeName = translate("add.webdoc");
            typeIntro = translate("add.webdoc.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_TEST)) {
            // get registered Handler instead of using new with a concrete Handler Class -> introduced during onxy review
            typeToAdd = factory.getRepositoryHandler(TestFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, PROCESS_ADD, ureq, getWindowControl());
            translatedTypeName = translate("add.test");
            typeIntro = translate("add.test.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_SURVEY)) {
            // get registered Handler instead of using new with a concrete Handler Class -> introduced during onxy review
            typeToAdd = factory.getRepositoryHandler(SurveyFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, PROCESS_ADD, ureq, getWindowControl());
            translatedTypeName = translate("add.survey");
            typeIntro = translate("add.survey.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_CP)) {
            typeToAdd = factory.getRepositoryHandler(ImsCPFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, ImsCPRepositoryHandler.PROCESS_IMPORT, ureq, getWindowControl());
            translatedTypeName = translate("add.cp");
            typeIntro = translate("add.cp.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_SCORM)) {
            typeToAdd = factory.getRepositoryHandler(ScormCPFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
            translatedTypeName = translate("add.scorm");
            typeIntro = translate("add.scorm.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_WIKI)) {
            typeToAdd = factory.getRepositoryHandler(WikiResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
            translatedTypeName = translate("add.wiki");
            typeIntro = translate("add.wiki.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_PODCAST)) {
            typeToAdd = factory.getRepositoryHandler(PodcastFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
            translatedTypeName = translate("add.podcast");
            typeIntro = translate("add.podcast.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_BLOG)) {
            typeToAdd = factory.getRepositoryHandler(BlogFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
            translatedTypeName = translate("add.blog");
            typeIntro = translate("add.blog.intro");
        } else if (actionAddCommand.equals(ACTION_ADD_GLOSSARY)) {
            typeToAdd = factory.getRepositoryHandler(GlossaryResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
            translatedTypeName = translate("add.glossary");
            typeIntro = translate("add.glossary.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_COURSE)) {
            typeToAdd = factory.getRepositoryHandler(CourseModule.getCourseTypeName());
            this.actionProcess = RepositoryAddController.PROCESS_NEW;
            addController = typeToAdd.createAddController(addCallback, CourseRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.course");
            typeIntro = translate("new.course.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_TEST)) {
            typeToAdd = factory.getRepositoryHandler(TestFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, PROCESS_NEW, ureq, getWindowControl());
            translatedTypeName = translate("new.test");
            typeIntro = translate("new.test.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_SURVEY)) {
            typeToAdd = factory.getRepositoryHandler(SurveyFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, PROCESS_NEW, ureq, getWindowControl());
            translatedTypeName = translate("new.survey");
            typeIntro = translate("new.survey.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_SHAREDFOLDER)) {
            typeToAdd = factory.getRepositoryHandler(SharedFolderFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, SharedFolderRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.sharedfolder");
            typeIntro = translate("new.sharedfolder.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_WIKI)) {
            typeToAdd = factory.getRepositoryHandler(WikiResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, WikiRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.wiki");
            typeIntro = translate("new.wiki.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_PODCAST)) {
            typeToAdd = factory.getRepositoryHandler(PodcastFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, PodcastRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.podcast");
            typeIntro = translate("new.podcast.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_BLOG)) {
            typeToAdd = factory.getRepositoryHandler(BlogFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, BlogRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.blog");
            typeIntro = translate("new.blog.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_GLOSSARY)) {
            typeToAdd = factory.getRepositoryHandler(GlossaryResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, GlossaryRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.glossary");
            typeIntro = translate("new.glossary.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_PORTFOLIO)) {
            typeToAdd = factory.getRepositoryHandler(EPTemplateMapResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, PortfolioRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("new.portfolio");
            typeIntro = translate("new.portfolio.intro");
        } else if (actionAddCommand.equals(ACTION_NEW_CP)) {
            typeToAdd = factory.getRepositoryHandler(ImsCPFileResource.TYPE_NAME);
            addController = typeToAdd.createAddController(addCallback, ImsCPRepositoryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
            translatedTypeName = translate("tools.add.cp");
            typeIntro = translate("new.cp.intro");
        } else {
            throw new AssertException("Unsuported Repository Type.");
        }

        // AddControllers may not need a GUI-based workflow.
        // In such cases, they do not have to return a transactional component,
        // but they must call addCallback.finished().
        final Component transactionComponent = addController.getTransactionComponent();
        if (transactionComponent != null) {
            repositoryadd.put("subcomp", transactionComponent);
        }
        repositoryadd.contextPut("typeHeader",
                (translatedTypeName == null) ? translate("add.header") : translate("add.header.specific", new String[] { translatedTypeName }));
        repositoryadd.contextPut("typeIntro", typeIntro);
        forwardButton.setEnabled(false);
        forwardButton.setTextReasonForDisabling(translate("disabledforwardreason"));
        panel = putInitialPanel(repositoryadd);
        return;
    }

    /**
     * Used by RepositoryMainController to identify which command was executed.
     */
    protected String getActionAddCommand() {
        return actionAddCommand;
    }

    /**
     * Used by RepositoryMainController to identify which process was executed.
     */
    protected String getActionProcess() {
        return actionProcess != null ? actionProcess : "";
    }

    /**
     * Used by RepositoryMainController to identify which resource has been added.
     */
    RepositoryEntry getAddedEntry() {
        return addedEntry;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == forwardButton) {
            // finish transaction and add repository entry
            if (!addController.transactionFinishBeforeCreate()) {
                return;
            }
            // save current name and description from create from
            addedEntry = getRepositoryService().updateNewRepositoryEntry(this.addedEntry);
            addController.repositoryEntryCreated(addedEntry);
            workflowSuccessful = true;

            // do logging
            ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
                    LoggingResourceable.wrap(addedEntry, OlatResourceableType.genRepoEntry));

            fireEvent(ureq, Event.DONE_EVENT);
            fireEvent(ureq, new EntryChangedEvent(addedEntry, EntryChangedEvent.ADDED));
            return;
        } else if (source == cancelButton) {
            // FIXME:pb: review is it really as intended to pass here from /addDelegate.html or /addDetails.html
            // clean up temporary data and abort transaction
            cleanup();
            fireEvent(ureq, Event.CANCELLED_EVENT);
            return;
        }
    }

    /**
     * @return
     */
    private RepositoryService getRepositoryService() {
        return RepositoryServiceImpl.getInstance();
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == detailsController) {
            if (event == Event.CANCELLED_EVENT) {
                // clean up temporary data and abort transaction
                cleanup();
                fireEvent(ureq, Event.CANCELLED_EVENT);
                return;
            } else if (event == Event.DONE_EVENT) {
                forwardButton.setEnabled(true);
                addedEntry = detailsController.getRepositoryEntry();
            }
        }
    }

    protected void addFinished(final UserRequest ureq) {
        RepositoryEntryInputData repositoryEntryInput = getRepositoryEntryInput(ureq);
        addedEntry = repositoryEBL.createRepositoryEntryWithOresAndOwnerGroup(repositoryEntryInput, typeToAdd);

        removeAsListenerAndDispose(detailsController);
        detailsController = new RepositoryEditDescriptionController(ureq, getWindowControl(), addedEntry, true);
        listenTo(detailsController);

        repositoryadd.put("details", detailsController.getInitialComponent());
        // try to get type description based on handlertype
        repositoryadd.contextPut("header", translate("add.header.specific", new String[] { translate(addCallback.getResourceable().getResourceableTypeName()) }));
        repositoryadd.setPage(VELOCITY_ROOT + "/addDetails.html");
    }

    private RepositoryEntryInputData getRepositoryEntryInput(final UserRequest ureq) {
        Identity identity = ureq.getIdentity();
        String dispName = addCallback.getDisplayName();
        if (dispName == null) {
            dispName = "";
        }
        String resName = addCallback.getResourceName();
        if (resName == null) {
            resName = "";
        }
        return new RepositoryEntryInputData(identity, resName, dispName, addCallback.getResourceable());
    }

    protected void addCanceled(final UserRequest ureq) {
        cleanup();
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    protected void addFailed(final UserRequest ureq) {
        cleanup();
        fireEvent(ureq, Event.FAILED_EVENT);
    }

    private void cleanup() {
        // FIXME: this belongs to manager code!
        if (detailsController != null) {
            addedEntry = detailsController.getRepositoryEntry();
            if (addedEntry != null) {
                getRepositoryService().deleteRepositoryEntryAndBasesecurity(addedEntry);
            }
            if (detailsController != null) {
                detailsController.dispose();
                detailsController = null;
            }
        }
        // tell add controller about this
        if (addController != null) {
            addController.transactionAborted();
        }
        log.debug("cleanup : finished");
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (!workflowSuccessful) {
            cleanup();
        }
        // OLAT-4619 In any case execute controller dispose chain (e.g. clean tmp upload files)
        if (addController != null) {
            addController.dispose();
            addController = null;
        }
    }

}
