/**
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * All rights reserved.
 */
package de.bps.onyx.plugin.course.nodes.iq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.presentation.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.QTIExportEssayItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFIBItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportKPRIMItemFormatConfig;
import org.olat.ims.qti.export.QTIExportMCQItemFormatConfig;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.export.QTIExportSCQItemFormatConfig;
import org.olat.presentation.ims.qti.exporter.helper.QTIItemObject;
import org.olat.presentation.ims.qti.exporter.helper.QTIObjectTreeBuilder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.user.UserManager;

import de.bps.onyx.plugin.OnyxExportManager;

/**
 * Description:<br>
 * Wizard for replacement of an already linked test
 * <P>
 * Initial Date: 21.10.2008 <br>
 * 
 * @author skoeber
 */
public class IQEditReplaceWizard extends WizardController {

	/** three steps: information + export results, search, mail */
	private static final int STEPS = 3;

	// needed objects
	private String resultExportFile;
	private File exportDir;
	private final ICourse course;
	private final CourseNode courseNode;
	private RepositoryEntry selectedRepositoryEntry;
	private List<Identity> learners;
	private final List<QTIResult> results;
	private final String[] types;
	private final boolean isOnyx;

	// presentation
	private VelocityContainer vcStep1, vcStep2, vcStep3;
	private Link nextBtn, showFileButton;
	private MailNotificationEditController mailCtr;
	private ReferencableEntriesSearchController searchCtr;

	/**
	 * a number of identities with qti.ser entry
	 */
	private final int numberOfQtiSerEntries;

	public IQEditReplaceWizard(final UserRequest ureq, final WindowControl wControl, final ICourse course, final CourseNode courseNode, final String[] types,
			final List<Identity> learners, final List<QTIResult> results, final int numberOfQtiSerEntries) {
		this(ureq, wControl, course, courseNode, types, learners, results, numberOfQtiSerEntries, false);
	}

	/**
	 * Standard constructor
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 */
	public IQEditReplaceWizard(final UserRequest ureq, final WindowControl wControl, final ICourse course, final CourseNode courseNode, final String[] types,
			final List<Identity> learners, final List<QTIResult> results, final int numberOfQtiSerEntries, final boolean isOnyx) {
		super(ureq, wControl, STEPS);

		setBasePackage(org.olat.course.nodes.iq.IQEditReplaceWizard.class);

		this.course = course;
		this.courseNode = courseNode;
		this.types = types;
		this.learners = learners;
		this.results = results;
		this.numberOfQtiSerEntries = numberOfQtiSerEntries;
		this.isOnyx = isOnyx;

		setWizardTitle(translate("replace.wizard.title"));
		doStep1(ureq);
	}

	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == nextBtn) {
			doStep3(ureq);
		} else if (source == showFileButton) {
			ureq.getDispatchResult().setResultingMediaResource(new DownloadeableMediaResource(new File(exportDir, resultExportFile)));
		} else if (event.getCommand().equals("cmd.wizard.cancel")) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	@Override
	protected void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == mailCtr && event == Event.DONE_EVENT) {
			final MailTemplate mailTemplate = mailCtr.getMailTemplate();
			if (mailTemplate != null) {
				List<Identity> recipientsCC = null;
				if (mailTemplate.getCpfrom()) {
					recipientsCC = new ArrayList<Identity>();
					recipientsCC.add(ureq.getIdentity());
				}
				MailerWithTemplate.getInstance().sendMailAsSeparateMails(learners, recipientsCC, null, mailCtr.getMailTemplate(), ureq.getIdentity());
			}
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == searchCtr && event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
			selectedRepositoryEntry = searchCtr.getSelectedEntry();
			if (isOnyx) {
				doStep2Onyx(ureq);
			} else {
				doStep2(ureq);
			}
		}
	}

	private void doStep1(final UserRequest ureq) {
		searchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
		searchCtr.addControllerListener(this);
		vcStep1 = createVelocityContainer("replacewizard_step1");
		vcStep1.put("search", searchCtr.getInitialComponent());
		setNextWizardStep(translate("replace.wizard.title.step1"), vcStep1);
	}

	private void doStep2(final UserRequest ureq) {
		final String nodeTitle = courseNode.getShortTitle();
		if (results != null && results.size() > 0) {
			exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
			final UserManager um = UserManager.getInstance();
			final String charset = um.getUserCharset(ureq.getIdentity());
			final QTIExportManager qem = QTIExportManager.getInstance();
			final Long repositoryRef = results.get(0).getResultSet().getRepositoryRef();
			final QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(repositoryRef);
			final List qtiItemObjectList = qotb.getQTIItemObjectList();
			final QTIExportFormatter formatter = new QTIExportFormatterCSVType1(ureq.getLocale(), "\t", "\"", "\\", "\r\n", false);
			final Map qtiItemConfigs = getQTIItemConfigs(qtiItemObjectList);
			formatter.setMapWithExportItemConfigs(qtiItemConfigs);
			resultExportFile = qem.exportResults(formatter, results, qtiItemObjectList, courseNode.getShortTitle(), exportDir, charset, ".xls");
			vcStep2 = createVelocityContainer("replacewizard_step2");
			final String[] args1 = new String[] { Integer.toString(learners.size()) };
			vcStep2.contextPut("information", translate("replace.wizard.information.paragraph1", args1));
			final String[] args2 = new String[] { exportDir.getName(), resultExportFile };
			vcStep2.contextPut("information_par2", translate("replace.wizard.information.paragraph2", args2));
			vcStep2.contextPut("nodetitle", nodeTitle);
			showFileButton = LinkFactory.createButton("replace.wizard.showfile", vcStep2, this);
		} else {
			// it exists no result
			final String[] args = new String[] { Integer.toString(numberOfQtiSerEntries) };
			vcStep2 = createVelocityContainer("replacewizard_step2");
			vcStep2.contextPut("information", translate("replace.wizard.information.empty.results", args));
			vcStep2.contextPut("nodetitle", nodeTitle);
		}
		nextBtn = LinkFactory.createButton("replace.wizard.next", vcStep2, this);
		setNextWizardStep(translate("replace.wizard.title.step2"), vcStep2);
	}

	/**
	 * Does export of test results for onyx tests
	 * 
	 * @param ureq
	 */
	private void doStep2Onyx(final UserRequest ureq) {
		final String nodeTitle = courseNode.getShortTitle();
		exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		final OnyxExportManager onyxExportManager = OnyxExportManager.getInstance();
		if (courseNode.getClass().equals(IQSURVCourseNode.class)) {
			// it is an onyx survey
			final String surveyPath = course.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator + courseNode.getIdent() + File.separator;
			resultExportFile = onyxExportManager.exportResults(surveyPath, exportDir, courseNode);
		} else {
			final String repositorySoftKey = (String) courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
			final Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
			final QTIResultManager qrm = QTIResultManager.getInstance();
			final List<QTIResultSet> resultSets = qrm.getResultSets(course.getResourceableId(), courseNode.getIdent(), repKey, null);
			learners = new ArrayList<Identity>();
			for (final QTIResultSet resultSet : resultSets) {
				if (!learners.contains(resultSet.getIdentity())) {
					learners.add(resultSet.getIdentity());
				}
			}
			resultExportFile = onyxExportManager.exportResults(resultSets, exportDir, courseNode);
		}
		// vcStep2 = new VelocityContainer("replaceWizard", VELOCITY_ROOT + "/replacewizard_step2.html", translator, this);
		vcStep2 = createVelocityContainer("replacewizard_step2");
		final String[] args = new String[] { Integer.toString(learners != null ? learners.size() : 0), exportDir.getName(), resultExportFile };
		vcStep2.contextPut("information", getTranslator().translate("replace.wizard.information", args));
		vcStep2.contextPut("nodetitle", nodeTitle);
		vcStep2.contextPut("filename", resultExportFile);
		showFileButton = LinkFactory.createButton("replace.wizard.showfile", vcStep2, this);
		nextBtn = LinkFactory.createButton("replace.wizard.next", vcStep2, this);
		this.setNextWizardStep(getTranslator().translate("replace.wizard.title.step2"), vcStep2);
	}

	private void doStep3(final UserRequest ureq) {
		final StringBuilder extLink = new StringBuilder();
		extLink.append(Settings.getServerContextPathURI());
		final RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		extLink.append("/url/RepositoryEntry/").append(re.getKey());
		extLink.append("/CourseNode/").append(courseNode.getIdent());

		final String[] bodyArgs = new String[] { course.getCourseTitle(), extLink.toString() };

		final String subject = translate("inform.users.subject", bodyArgs);
		final String body = translate("inform.users.body", bodyArgs);

		final MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			@SuppressWarnings({ "unused" })
			public void putVariablesInMailContext(final VelocityContext context, final Identity identity) {
				// nothing to do
			}
		};

		removeAsListenerAndDispose(mailCtr);
		mailCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTempl, false);
		listenTo(mailCtr);

		vcStep3 = createVelocityContainer("replacewizard_step3");
		vcStep3.put("mailform", mailCtr.getInitialComponent());
		setNextWizardStep(translate("replace.wizard.title.step3"), vcStep3);
	}

	/**
	 * @return the selected RepositoryEntry
	 */
	protected RepositoryEntry getSelectedRepositoryEntry() {
		return selectedRepositoryEntry;
	}

	private Map getQTIItemConfigs(final List qtiItemObjectList) {
		final Map itConfigs = new HashMap();

		for (final Iterator iter = qtiItemObjectList.iterator(); iter.hasNext();) {
			final QTIItemObject item = (QTIItemObject) iter.next();
			if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)) {
				if (itConfigs.get(QTIExportSCQItemFormatConfig.class) == null) {
					final QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)) {
				if (itConfigs.get(QTIExportMCQItemFormatConfig.class) == null) {
					final QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
				if (itConfigs.get(QTIExportKPRIMItemFormatConfig.class) == null) {
					final QTIExportKPRIMItemFormatConfig confKPRIM = new QTIExportKPRIMItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportKPRIMItemFormatConfig.class, confKPRIM);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)) {
				if (itConfigs.get(QTIExportEssayItemFormatConfig.class) == null) {
					final QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
					itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)) {
				if (itConfigs.get(QTIExportFIBItemFormatConfig.class) == null) {
					final QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
					itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
				}
			} else if (item.getItemType().equals(QTIItemObject.TYPE.A)) {
				final QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
				itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.R)) {
				final QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.C)) {
				final QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.B)) {
				final QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
				itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
			} else {
				throw new OLATRuntimeException(null, "Can not resolve QTIItem type", null);
			}
		}
		return itConfigs;
	}

}