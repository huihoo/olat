/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.onyx.plugin.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.HtmlStaticPageComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.iframe.IFrameDisplayController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.OnyxResultManager;
import de.bps.onyx.plugin.course.nodes.iq.IQEditController;
import de.bps.onyx.plugin.wsclient.OnyxPluginServices;
import de.bps.onyx.plugin.wsclient.PluginService;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManager;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManagerFactory;

/**
 * @author Ingmar Kroll
 */
public class OnyxRunController extends BasicController {

	private VelocityContainer myContent, onyxReporterVC, onyxPlugin;
	private ModuleConfiguration modConfig;
	private IQTESTCourseNode courseNodeTest;
	private IQSURVCourseNode courseNodeSurvey;
	private IQSELFCourseNode courseNodeSelf;
	private UserCourseEnvironment userCourseEnv;
	private Link startButton, onyxBack;
	private IFrameDisplayController iFrameCtr;
	private String CP;
	private String uniqueId;
	private OnyxModalController onyxPluginController;

	private final static int NOENTRYVIEW = -1;
	private final static int DISCVIEW = 0;
	private final static int SURVEYVIEW = 1;
	private final static int ENDVIEW = 2;

	private Link showOnyxReporterButton;

	public OnyxRunController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable fileResource) {
		super(ureq, wControl);
		final VelocityContainer vc = showOnyxTestInModalController(ureq, fileResource);
		putInitialPanel(vc);
	}

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
	public OnyxRunController(final UserCourseEnvironment userCourseEnv, final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback,
			final UserRequest ureq, final WindowControl wControl, final IQSURVCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		myContent = createVelocityContainer("onyxrun");
		this.courseNodeSurvey = courseNode;
		this.CP = getCP(courseNode.getReferencedRepositoryEntry().getOlatResource());
		putInitialPanel(myContent);
		Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);
		showView(ureq, SURVEYVIEW);
	}

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
	public OnyxRunController(final UserCourseEnvironment userCourseEnv, final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback,
			final UserRequest ureq, final WindowControl wControl, final IQSELFCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		myContent = createVelocityContainer("onyxrun");
		this.courseNodeSelf = courseNode;
		this.CP = getCP(courseNode.getReferencedRepositoryEntry().getOlatResource());
		putInitialPanel(myContent);
		Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);
		showView(ureq, DISCVIEW);
	}

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
	public OnyxRunController(final UserCourseEnvironment userCourseEnv, final ModuleConfiguration moduleConfiguration, final IQSecurityCallback secCallback,
			final UserRequest ureq, final WindowControl wControl, final IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		myContent = createVelocityContainer("onyxrun");
		this.courseNodeTest = courseNode;
		this.CP = getCP(courseNode.getReferencedRepositoryEntry().getOlatResource());
		putInitialPanel(myContent);
		Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);
		int confValue = 1;
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS) == null) {
			confValue = 0;
		} else {
			confValue = Integer.valueOf(moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
		}
		if (confValue == 0 || courseNode.getUserAttempts(userCourseEnv).intValue() < confValue) {
			// running allowed
			showView(ureq, DISCVIEW);
		} else {
			// only one time allowed
			showView(ureq, NOENTRYVIEW);
		}
	}

	private void showView(final UserRequest ureq, final int viewmode) {
		myContent.contextPut("viewmode", new Integer(viewmode));
		int c = 1; // per default

		try {
			c = Integer.valueOf(modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
		} catch (final Exception e) {
			// nothing to do
		}

		if (c != 0) {
			myContent.contextPut("attemptsConfig", String.valueOf(c));
		}

		if (viewmode != SURVEYVIEW) {
			ScoreEvaluation se = null;
			if (courseNodeTest != null) {
				myContent.contextPut("attempts", courseNodeTest.getUserAttempts(userCourseEnv));
				myContent.contextPut("comment", courseNodeTest.getUserUserComment(userCourseEnv));

				if (courseNodeTest.getUserAttempts(userCourseEnv) > 0) {
					se = courseNodeTest.getUserScoreEvaluation(userCourseEnv);
				}
			} else if (courseNodeSelf != null) {
				myContent.contextPut("self", Boolean.TRUE);
				se = courseNodeSelf.getUserScoreEvaluation(userCourseEnv);
			}
			final boolean hasResult = se != null && se.getScore() != null;
			if (hasResult) {
				myContent.contextPut("hasResult", Boolean.TRUE);
				final boolean isPassesSet = se.getPassed() != null;
				myContent.contextPut(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE));
				if (isPassesSet) {
					myContent.contextPut("passed", se.getPassed());
				} else {
					myContent.contextPut("passed", "");
				}
				final float score = se.getScore();
				myContent.contextPut("score", AssessmentHelper.getRoundedScore(score));
			} else {
				myContent.contextPut("hasResult", Boolean.FALSE);
			}
			showOnyxReporterButton = LinkFactory.createButtonSmall("cmd.showOnyxReporter", myContent, this);
		}

		switch (viewmode) {
			case DISCVIEW:
				// push title and learning objectives, only visible on intro page
				if (courseNodeTest != null) {
					myContent.contextPut("menuTitle", courseNodeTest.getShortTitle());
					myContent.contextPut("displayTitle", courseNodeTest.getLongTitle());
				} else if (courseNodeSelf != null) {
					myContent.contextPut("menuTitle", courseNodeSelf.getShortTitle());
					myContent.contextPut("displayTitle", courseNodeSelf.getLongTitle());
				}

				// fetch disclaimer file
				final String sDisclaimer = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
				if (sDisclaimer != null) {
					setDisclaimer(sDisclaimer, ureq);
				}
				startButton = LinkFactory.createButton("startapplet", myContent, this);
				break;
			case ENDVIEW:
				break;
			case SURVEYVIEW:
				// fetch disclaimer file
				final String sDisc = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
				if (sDisc != null) {
					setDisclaimer(sDisc, ureq);
				}
				// push title and learning objectives, only visible on intro page
				myContent.contextPut("menuTitle", courseNodeSurvey.getShortTitle());
				myContent.contextPut("displayTitle", courseNodeSurvey.getLongTitle());
				myContent.contextPut("showReporter", Boolean.FALSE);

				myContent.contextPut("attempts", courseNodeSurvey.getUserAttempts(userCourseEnv));
				startButton = LinkFactory.createButton("startapplet", myContent, this);

				final boolean isAuthor = ureq.getUserSession().getRoles().isAuthor() || ureq.getUserSession().getRoles().isOLATAdmin();
				if (isAuthor) {
					myContent.contextPut("showReporter", Boolean.TRUE);
					showOnyxReporterButton = LinkFactory.createCustomLink("cmd.showOnyxReporter", "cmd.showOnyxReporter", "onyxreporter.button.survey",
							Link.BUTTON_SMALL, myContent, this);
				}
				break;
		}
	}

	/**
	 * @param sDisclaimer
	 * @param ureq
	 */
	private void setDisclaimer(String sDisclaimer, final UserRequest ureq) {
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

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.components.Component, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Controller controller, final Event event) {
		if (controller == onyxPluginController) {
			if (courseNodeSurvey != null) {
				showView(ureq, SURVEYVIEW);
			} else if (courseNodeTest != null) {
				int confValue = 1;
				try {
					confValue = Integer.valueOf(modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
				} catch (final NullPointerException e) {
	private static final Logger log = LoggerHelper.getLogger();

				}
				int userAttempts = 0;
				userAttempts = courseNodeTest.getUserAttempts(userCourseEnv).intValue();
				if (confValue == 0 || userAttempts < confValue) {
					// running allowed
					showView(ureq, DISCVIEW);
				} else {
					// only one time allowed
					showView(ureq, NOENTRYVIEW);
				}
			} else {
				showView(ureq, DISCVIEW);
			}
			// tell the RunMainController that it has to be updated
			userCourseEnv.getScoreAccounting().evaluateAll();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	/**
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.components.Component, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == startButton) {
			// increase attempts when starting the test -> attempts do not depend on test
			// running correct
			onyxPlugin = createVelocityContainer("onyxstart");
			String onyxBackLabel = "onyx.back";
			if (courseNodeTest != null) {
				onyxPlugin.contextPut("isSurvey", Boolean.FALSE);
			} else if (courseNodeSurvey != null) {
				onyxPlugin.contextPut("isSurvey", Boolean.TRUE);
				onyxBackLabel = "onyx.survey.back";
			}
			try {
				connectToOnyxWS(ureq);
				final String urlonyxplugin = OnyxModule.getUserViewLocation() + "?id=" + this.uniqueId;
				onyxPlugin.contextPut("urlonyxplugin", urlonyxplugin);
				onyxPluginController = new OnyxModalController(getWindowControl(), "close", onyxPlugin, false);
				onyxBack = LinkFactory.createCustomLink("onyx.back", "onyx.back", onyxBackLabel, Link.BUTTON_SMALL, onyxPlugin, onyxPluginController);
				onyxPluginController.setBackButton(onyxBack);
				listenTo(onyxPluginController);
				onyxPluginController.activate();
				// now increase attampts; if an exception occurred before, this will be not reached
				if (courseNodeTest != null) {
					courseNodeTest.incrementUserAttempts(userCourseEnv);
				} else if (courseNodeSurvey != null) {
					courseNodeSurvey.incrementUserAttempts(userCourseEnv);
				} else if (courseNodeSelf != null) {
					courseNodeSelf.incrementUserAttempts(userCourseEnv);
				}
			} catch (final Exception e) {
				getWindowControl().setError(translate("error.connectonyxws"));
	private static final Logger log = LoggerHelper.getLogger();

			}
			return;
		} else if (source == showOnyxReporterButton) {
			onyxReporterVC = createVelocityContainer("onyxreporter");
			final int error = showOnyxReporter(ureq);
			if (error == 0) {
				final CloseableModalController cmc = new CloseableModalController(getWindowControl(), "close", onyxReporterVC);
				cmc.activate();
			} else if (error == 1) {
				getWindowControl().setInfo(translate("oynxreporter.noresults"));
	private static final Logger log = LoggerHelper.getLogger();

			} else if (error == 2) {
				getWindowControl().setError(translate("onyxreporter.error"));
	private static final Logger log = LoggerHelper.getLogger();

			}
			return;
		}
		showView(ureq, ENDVIEW);
	}

	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * 
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 * @return 0 OK 1 NO RESULTS 2 ERROR
	 */
	private int showOnyxReporter(final UserRequest ureq) {
		final OnyxReporterWebserviceManager onyxReporter = OnyxReporterWebserviceManagerFactory.getInstance().fabricate("OnyxReporterWebserviceClient");
		if (onyxReporter != null) {
			final List<Identity> identity = new ArrayList<Identity>();
			String iframeSrc = "";
			try {
				if (courseNodeTest != null) {
					identity.add(userCourseEnv.getIdentityEnvironment().getIdentity());
					final long assasmentId = courseNodeTest.getUserScoreEvaluation(userCourseEnv).getAssessmentID();
					onyxReporter.setAssassmentId(assasmentId);
					iframeSrc = onyxReporter.startReporter(ureq, identity, courseNodeTest, true);
				} else {
					final String path = userCourseEnv.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator + courseNodeSurvey.getIdent()
							+ File.separator;
					iframeSrc = onyxReporter.startReporterForSurvey(ureq, courseNodeSurvey, path);
				}
			} catch (final OnyxReporterException oE) {
				if (oE.getMessage().equals("noresults")) {
					oE.printStackTrace();
					return 1;
				}
			} catch (final Exception e) {
				e.printStackTrace();
				return 2;
			}
			onyxReporterVC.contextPut("iframeOK", Boolean.TRUE);
			onyxReporterVC.contextPut("onyxReportLink", iframeSrc);
			return 0;
		} else {
			return 2;
		}
	}

	/**
	 * Static metohd to start the an onyx test as learningressource or bookmark.
	 * 
	 * @param ureq
	 * @param repositoryEntry
	 */
	public VelocityContainer showOnyxTestInModalController(final UserRequest ureq, final OLATResourceable fileResource) {
		OnyxPluginServices onyxplugin = null;
		try {
			onyxplugin = new PluginService().getOnyxPluginServicesPort();
		} catch (final Exception e) {
			getWindowControl().setError(translate("error.connectonyxws"));
	private static final Logger log = LoggerHelper.getLogger();

			return null;
		}

		final String CP = getCP(fileResource);
		final String language = ureq.getLocale().toString().toLowerCase();
		final String tempalteId = "onyxdefault";

		this.uniqueId = OnyxResultManager.getUniqueIdForShowOnly();

		try {
			final File cpFile = new File(CP);
			final Long fileLength = cpFile.length();
			final byte[] byteArray = new byte[fileLength.intValue()];
			System.out.println("CP : " + CP);
			final java.io.FileInputStream inp = new java.io.FileInputStream(cpFile);
			inp.read(byteArray);
			onyxplugin.run(this.uniqueId, byteArray, language, "", tempalteId, OnyxModule.getConfigName(), true);
		} catch (final FileNotFoundException e) {
	private static final Logger log = LoggerHelper.getLogger();

		} catch (final IOException e) {
	private static final Logger log = LoggerHelper.getLogger();

		}

		final String urlonyxplugin = OnyxModule.getUserViewLocation() + "?id=" + this.uniqueId;

		onyxPlugin = createVelocityContainer("onyxstart");
		onyxPlugin.contextPut("isSurvey", Boolean.FALSE);
		onyxPlugin.contextPut("urlonyxplugin", urlonyxplugin);
		onyxPlugin.contextPut("nohint", Boolean.TRUE);
		onyxPluginController = new OnyxModalController(getWindowControl(), "close", onyxPlugin, false);
		onyxBack = LinkFactory.createCustomLink("onyx.back", "onyx.back", "onyx.back", Link.BUTTON_SMALL, onyxPlugin, onyxPluginController);
		onyxBack.setVisible(false);
		onyxPluginController.setBackButton(onyxBack);
		return onyxPlugin;
	}

	private void connectToOnyxWS(final UserRequest ureq) {
		final OnyxPluginServices onyxplugin = new PluginService().getOnyxPluginServicesPort();
		CourseNode courseNode = null;
		boolean allowShowSolution = false;
		if (courseNodeTest != null) {
			courseNode = courseNodeTest;
		} else if (courseNodeSurvey != null) {
			courseNode = courseNodeSurvey;
		} else if (courseNodeSelf != null) {
			courseNode = courseNodeSelf;
			allowShowSolution = true;
		}
		this.uniqueId = OnyxResultManager.getUniqueId(ureq.getIdentity(), courseNode, this.userCourseEnv);
		String instructions = new String();
		try {
			final String sDisclaimer = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
			if (sDisclaimer != null) {
				final VFSLeaf disc = (VFSLeaf) this.userCourseEnv.getCourseEnvironment().getCourseFolderContainer().resolve(sDisclaimer);
				final BufferedReader r_disc = new BufferedReader(new InputStreamReader(disc.getInputStream()));
				String disc_s = new String();
				while (r_disc.ready()) {
					disc_s += r_disc.readLine();
				}
				instructions = disc_s;
			}
		} catch (final IOException e) {
	private static final Logger log = LoggerHelper.getLogger();

		}
		final String language = ureq.getLocale().toString().toLowerCase();
		String tempalteId = "onyxdefault";

		final String tempalteId_config = (String) modConfig.get(IQEditController.CONFIG_KEY_TEMPLATE);
		if (tempalteId_config != null && tempalteId_config.length() > 0) {
			tempalteId = tempalteId_config;
		}

		try {

			final File cpFile = new File(CP);
			final Long fileLength = cpFile.length();
			final byte[] byteArray = new byte[fileLength.intValue()];
			System.out.println("CP : " + CP);
			final java.io.FileInputStream inp = new java.io.FileInputStream(cpFile);
			inp.read(byteArray);
			onyxplugin.run(this.uniqueId, byteArray, language, instructions, tempalteId, OnyxModule.getConfigName(), allowShowSolution);
		} catch (final FileNotFoundException e) {
	private static final Logger log = LoggerHelper.getLogger();

		} catch (final IOException e) {
	private static final Logger log = LoggerHelper.getLogger();

		}
	}

	private static String getCP(final OLATResourceable fileResource) {
		// get content-package (= onyx test zip-file)
		// OLATResourceable fileResource = repositoryEntry.getOlatResource();
		final String unzipedDir = FileResourceManager.getInstance().unzipFileResource(fileResource).getAbsolutePath();
		final String zipdirName = FileResourceManager.ZIPDIR;

		// String name = repositoryEntry.getResourcename();//getDisplayname();
		final String name = FileResourceManager.getInstance().getFileResource(fileResource).getName();
		final String pathToFile = unzipedDir.substring(0, unzipedDir.indexOf(zipdirName));
		final String completePath = (pathToFile + name);
		final File cpFile = new File(completePath);
		if (!cpFile.exists()) {
			// look for imported file
			final String importedFileName = "repo.zip";
			final File impFile = new File(pathToFile + importedFileName);
			if (impFile.exists()) {
				impFile.renameTo(cpFile);
			} else {
	private static final Logger log = LoggerHelper.getLogger();

			}
		}
		return completePath;
	}

	@Override
	protected void doDispose() {
		if (onyxPluginController != null && onyxPluginController.isOpen()) {
			onyxPluginController.close();
		}
	}

	private class OnyxModalController extends CloseableModalController {
		public OnyxModalController(final WindowControl wControl, final String closeButtonText, final Component modalContent, final boolean showCloseIcon) {
			super(wControl, closeButtonText, modalContent, showCloseIcon);
		}

		private Link backButton;
		private boolean open = false;

		@Override
		public void activate() {
			super.activate();
			open = true;
		}

		public boolean isOpen() {
			return open;
		}

		public void setBackButton(final Link button) {
			backButton = button;
		}

		public void close() {
			open = false;
			getWindowControl().pop();
		}

		@Override
		public void event(final UserRequest ureq, final Component source, final Event event) {
			if (source == backButton) {
				close();
				fireEvent(ureq, CLOSE_MODAL_EVENT);
			}
		}
	}

}
