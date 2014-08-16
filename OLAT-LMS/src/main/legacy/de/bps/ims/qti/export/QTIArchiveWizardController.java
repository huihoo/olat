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

package de.bps.ims.qti.export;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Event;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.presentation.framework.core.media.FileMediaResource;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.NodeTableDataModel;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.presentation.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.DelimChooseForm;
import org.olat.ims.qti.export.OptionsChooseForm;
import org.olat.ims.qti.export.QTIExportEssayItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFIBItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportFormatterCSVType2;
import org.olat.ims.qti.export.QTIExportFormatterCSVType3;
import org.olat.ims.qti.export.QTIExportKPRIMItemFormatConfig;
import org.olat.ims.qti.export.QTIExportMCQItemFormatConfig;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.export.QTIExportSCQItemFormatConfig;
import org.olat.presentation.ims.qti.exporter.helper.QTIItemObject;
import org.olat.presentation.ims.qti.exporter.helper.QTIObjectTreeBuilder;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;

import de.bps.onyx.plugin.OnyxExportManager;
import de.bps.onyx.plugin.course.nodes.iq.IQEditController;

/**
 * Initial Date: June 06, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIArchiveWizardController extends BasicController {
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	private static final String CMD_BACK = "back";

	private final boolean dummyMode;

	// Delimiters and file name suffix for the export file
	private String sep = "\\t"; // fields separated by
	private String emb = "\""; // fields embedded by
	private String esc = "\\"; // fields escaped by
	private String car = "\\r\\n"; // carriage return
	private String suf = ".xls"; // file name suffix

	private final WizardController wc;

	private int steps = 4;
	private final Panel main;

	private CourseNode currentCourseNode;

	private final VelocityContainer nodeChooseVC;
	private VelocityContainer noResultsVC;
	private VelocityContainer optionsChooseVC;
	private VelocityContainer delimChooseVC;
	private VelocityContainer finishedVC;

	private OptionsChooseForm ocForm;
	private DelimChooseForm dcForm;
	private QTIExportFormatter formatter;

	private final NodeTableDataModel nodeTableModel;

	private final TableController nodeListCtr;
	private Long olatResource;
	private File exportDir;
	private String targetFileName;
	private int type;
	private Map qtiItemConfigs;
	private List results;
	private List qtiItemObjectList;
	private Link showFileButton;
	private Link backLinkAtOptionChoose;
	private Link backLinkAtNoResults;
	private Link backLinkAtDelimChoose;
	private final OLATResourceable ores;

	public QTIArchiveWizardController(final boolean dummyMode, final UserRequest ureq, final List nodesTableObjectArrayList, final OLATResourceable ores,
			final WindowControl wControl) {
		super(ureq, wControl);

		final Translator translator = Util
				.createPackageTranslator(org.olat.ims.qti.export.QTIArchiveWizardController.class, getTranslator().getLocale(), getTranslator());
		setTranslator(translator);

		this.dummyMode = dummyMode;
		this.ores = ores;
		if (dummyMode) {
			this.steps = 2;
		}

		main = new Panel("main");
		nodeChooseVC = createVelocityContainer("nodechoose");

		// table configuraton
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(getTranslator().translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);

		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);

		// table columns
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
				new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, ureq.getLocale()));

		nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
		nodeListCtr.setTableDataModel(nodeTableModel);
		nodeChooseVC.put("nodeTable", nodeListCtr.getInitialComponent());

		wc = new WizardController(ureq, wControl, steps);
		listenTo(wc);

		wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
		wc.setNextWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeChooseVC);
		main.setContent(wc.getInitialComponent());
		putInitialPanel(main);
	}

	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		if (source == backLinkAtOptionChoose) {
			wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeChooseVC);
		} else if (source == backLinkAtNoResults) {
			wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeChooseVC);
		} else if (source == backLinkAtDelimChoose) {
			wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.optionschoose.howto"), optionsChooseVC);
		} else if (source == showFileButton) {
			ureq.getDispatchResult().setResultingMediaResource(new FileMediaResource(new File(exportDir, targetFileName), true));
		}
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.presentation.framework.control.DefaultController#event(org.olat.presentation.framework.UserRequest, org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == ocForm) {
			if (event == Event.DONE_EVENT) {
				delimChooseVC = createVelocityContainer("delimchoose");

				backLinkAtDelimChoose = LinkFactory.createLinkBack(delimChooseVC, this);
				delimChooseVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
				removeAsListenerAndDispose(dcForm);
				dcForm = new DelimChooseForm(ureq, getWindowControl(), sep, emb, esc, car, suf);
				listenTo(dcForm);
				delimChooseVC.put("dcForm", dcForm.getInitialComponent());
				wc.setWizardTitle(getTranslator().translate("wizard.delimchoose.title"));
				wc.setNextWizardStep(getTranslator().translate("wizard.delimchoose.howto"), delimChooseVC);
			}
		} else if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final TableEvent te = (TableEvent) event;
				final String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					final int rowid = te.getRowId();
					final Map nodeData = (Map) nodeTableModel.getObject(rowid);
					final ICourse course = CourseFactory.loadCourse(ores);
					this.currentCourseNode = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));

					boolean isOnyx = false;
					if (currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI) != null) {
						isOnyx = currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);
					}
					boolean success = false;
					final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
					final Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();

					if (isOnyx) {
						if (currentCourseNode.getClass().equals(IQSURVCourseNode.class)) {
							final File surveyDir = new File(course.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator
									+ currentCourseNode.getIdent() + File.separator);
							if (surveyDir != null && surveyDir.exists() && surveyDir.listFiles().length > 0) {
								success = true;
							}
						} else {
							success = QTIResultManager.getInstance().hasResultSets(course.getResourceableId(), currentCourseNode.getIdent(), repKey);
						}
					} else {
						success = hasResultSets(ureq);
					}

					olatResource = course.getResourceableId();

					if (success) {
						if (isOnyx) {
							finishedVC = createVelocityContainer("finished");
							showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
							finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
							targetFileName = exportOnyx(ureq, course);
							finishedVC.contextPut("filename", targetFileName);
							wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
							wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
						} else {
							final QTIResultManager qrm = QTIResultManager.getInstance();
							results = qrm.selectResults(olatResource, currentCourseNode.getIdent(), repKey, type);
							final QTIResult res0 = (QTIResult) results.get(0);

							final QTIObjectTreeBuilder qotb = new QTIObjectTreeBuilder(new Long(res0.getResultSet().getRepositoryRef()));
							qtiItemObjectList = qotb.getQTIItemObjectList();

							this.qtiItemConfigs = getQTIItemConfigs();

							if (dummyMode) {
								finishedVC = createVelocityContainer("finished");
								showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
								finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());

								this.sep = convert2CtrlChars(sep);
								this.car = convert2CtrlChars(car);

								formatter = getFormatter(ureq.getLocale(), sep, emb, esc, car, false);
								formatter.setMapWithExportItemConfigs(qtiItemConfigs);

								exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
								final UserManager um = UserManager.getInstance();
								final String charset = um.getUserCharset(ureq.getIdentity());

								final QTIExportManager qem = QTIExportManager.getInstance();

								targetFileName = qem.exportResults(formatter, results, qtiItemObjectList, currentCourseNode.getShortTitle(), exportDir, charset, suf);
								finishedVC.contextPut("filename", targetFileName);
								wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
								wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);

							} else { // expert mode
								optionsChooseVC = createVelocityContainer("optionschoose");
								backLinkAtOptionChoose = LinkFactory.createLinkBack(optionsChooseVC, this);
								optionsChooseVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
								removeAsListenerAndDispose(ocForm);
								ocForm = new OptionsChooseForm(ureq, getWindowControl(), qtiItemConfigs);
								listenTo(ocForm);
								optionsChooseVC.put("ocForm", ocForm.getInitialComponent());

								wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
								wc.setNextWizardStep(getTranslator().translate("wizard.optionschoose.howto"), optionsChooseVC);
							}
						}

					} else { // no success
						noResultsVC = createVelocityContainer("noresults");
						backLinkAtNoResults = LinkFactory.createLinkBack(noResultsVC, this);
						noResultsVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
						if (dummyMode) {
							wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
							wc.setNextWizardStep(getTranslator().translate("wizard.optionschoose.howto"), noResultsVC);
						} else { // expert mode
							wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
							wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), noResultsVC);
						}
					}
				}
			}
		} else if (source == wc) {
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == dcForm) {
			if (event == Event.DONE_EVENT) {
				finishedVC = createVelocityContainer("finished");
				showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
				finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());

				this.sep = dcForm.getSeparatedBy();
				this.emb = dcForm.getEmbeddedBy();
				this.esc = dcForm.getEscapedBy();
				this.car = dcForm.getCarriageReturn();
				this.suf = dcForm.getFileNameSuffix();

				this.sep = convert2CtrlChars(sep);
				this.car = convert2CtrlChars(car);

				formatter = getFormatter(ureq.getLocale(), sep, emb, esc, car, dcForm.isTagless());

				formatter.setMapWithExportItemConfigs(qtiItemConfigs);

				final ICourse course = CourseFactory.loadCourse(ores);
				exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
				final UserManager um = UserManager.getInstance();
				final String charset = um.getUserCharset(ureq.getIdentity());

				final QTIExportManager qem = QTIExportManager.getInstance();

				targetFileName = qem.exportResults(formatter, results, qtiItemObjectList, currentCourseNode.getShortTitle(), exportDir, charset, suf);
				finishedVC.contextPut("filename", targetFileName);
				wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
				wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
			}
		}
	}

	private String exportOnyx(final UserRequest ureq, final ICourse course) {
		String filename = "";
		exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		final OnyxExportManager onyxExportManager = OnyxExportManager.getInstance();
		if (currentCourseNode.getClass().equals(IQSURVCourseNode.class)) {
			// it is an onyx survey
			final String surveyPath = course.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator + currentCourseNode.getIdent()
					+ File.separator;
			filename = onyxExportManager.exportResults(surveyPath, exportDir, currentCourseNode);
		} else {
			final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
			final Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
			final QTIResultManager qrm = QTIResultManager.getInstance();
			final List<QTIResultSet> resultSets = qrm.getResultSets(course.getResourceableId(), currentCourseNode.getIdent(), repKey, null);
			filename = onyxExportManager.exportResults(resultSets, exportDir, currentCourseNode);
		}
		return filename;
	}

	private boolean hasResultSets(final UserRequest ureq) {
		if (currentCourseNode instanceof IQTESTCourseNode) {
			type = 1;
		} else if (currentCourseNode instanceof IQSELFCourseNode) {
			type = 2;
		} else {
			type = 3;
		}

		final QTIResultManager qrm = QTIResultManager.getInstance();

		final String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);

		final Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
		final boolean hasSets = qrm.hasResultSets(olatResource, currentCourseNode.getIdent(), repKey);

		if (hasSets) {
			return true;
		} else {
			return false;
		}
	}

	private String convert2CtrlChars(final String source) {
		if (source == null) { return null; }
		final StringBuilder sb = new StringBuilder(300);
		final int len = source.length();
		final char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			final char c = cs[i];
			switch (c) {
				case '\\':
					// check on \\ first
					if (i < len - 1 && cs[i + 1] == 't') { // we have t as next char
						sb.append("\t");
						i++;
					} else if (i < len - 1 && cs[i + 1] == 'r') { // we have r as next char
						sb.append("\r");
						i++;
					} else if (i < len - 1 && cs[i + 1] == 'n') { // we have n as next char
						sb.append("\n");
						i++;
					} else {
						sb.append("\\");
					}
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	private Map getQTIItemConfigs() {
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
			}
			// if cannot find the type via the ItemParser, look for the QTIItemObject type
			else if (item.getItemType().equals(QTIItemObject.TYPE.A)) {
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

	private QTIExportFormatter getFormatter(final Locale locale, final String se, final String em, final String es, final String ca, final boolean tagless) {
		QTIExportFormatter frmtr = null;
		if (type == 1) {
			frmtr = new QTIExportFormatterCSVType1(locale, se, em, es, ca, tagless);
		} else if (type == 2) {
			frmtr = new QTIExportFormatterCSVType2(locale, null, se, em, es, ca, tagless);
		} else { // type == 3
			frmtr = new QTIExportFormatterCSVType3(locale, null, se, em, es, ca, tagless);
		}
		return frmtr;
	}

	@Override
	protected void doDispose() {
		//
	}
}
