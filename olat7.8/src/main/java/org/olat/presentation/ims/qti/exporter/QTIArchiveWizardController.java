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

package org.olat.presentation.ims.qti.exporter;

import java.util.List;
import java.util.Map;

import org.olat.data.qti.QTIResult;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ExportResult;
import org.olat.lms.course.nodes.QtiExportEBL;
import org.olat.lms.ims.qti.exporter.ExportFormatConfig;
import org.olat.lms.ims.qti.exporter.QTIExportItemFormatConfig;
import org.olat.presentation.course.assessment.IndentedNodeRenderer;
import org.olat.presentation.course.assessment.NodeTableDataModel;
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
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: June 06, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIArchiveWizardController extends BasicController {
    private static final String CMD_SELECT_NODE = "cmd.select.node";

    private final boolean dummyMode;

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

    private final NodeTableDataModel nodeTableModel;

    private final TableController nodeListCtr;
    private List<QTIResult> results;
    private Link showFileButton;
    private Link backLinkAtOptionChoose;
    private Link backLinkAtNoResults;
    private Link backLinkAtDelimChoose;
    private final OLATResourceable ores;
    private QtiExportEBL qtiExportEbl;
    private ExportResult exportResult;
    private Map<Class<? extends QTIExportItemFormatConfig>, QTIExportItemFormatConfig> qtiItemConfig;

    public QTIArchiveWizardController(final boolean dummyMode, final UserRequest ureq, final List nodesTableObjectArrayList, final OLATResourceable ores,
            final WindowControl wControl) {
        super(ureq, wControl);
        qtiExportEbl = CoreSpringFactory.getBean(QtiExportEBL.class);
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
    protected void event(final UserRequest ureq, final Component source, final Event event) {
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
            ureq.getDispatchResult().setResultingMediaResource(qtiExportEbl.getFileMediaResourceAsAttachment(exportResult));
        }
    }

    /**
     * This dispatches controller events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        if (source == ocForm) {
            if (event == Event.DONE_EVENT) {
                delimChooseVC = createVelocityContainer("delimchoose");

                backLinkAtDelimChoose = LinkFactory.createLinkBack(delimChooseVC, this);
                delimChooseVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
                removeAsListenerAndDispose(dcForm);
                dcForm = new DelimChooseForm(ureq, getWindowControl(), QtiExportEBL.FILEDS_SEPARATOR, QtiExportEBL.FIELDS_EMBEDDED_BY, QtiExportEBL.FIELDS_ESCAPED_BY,
                        QtiExportEBL.CARRIAGE_RETURN, QtiExportEBL.FILE_NAME_SUFFIX);
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

                    if (qtiExportEbl.hasResultSets(course.getResourceableId(), currentCourseNode)) {
                        results = qtiExportEbl.getResults(course.getResourceableId(), currentCourseNode);

                        if (dummyMode) {
                            finishedVC = createVelocityContainer("finished");
                            finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
                            showFileButton = LinkFactory.createButton("showfile", finishedVC, this);

                            exportResult = qtiExportEbl.exportResultsForArchiveWithDefaultFormat(ureq.getIdentity(), ureq.getLocale(), qtiItemConfig, results,
                                    currentCourseNode, course.getCourseTitle());

                            finishedVC.contextPut("filename", exportResult.getExportFileName());
                            wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
                            wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);

                        } else { // expert mode
                            optionsChooseVC = createVelocityContainer("optionschoose");
                            backLinkAtOptionChoose = LinkFactory.createLinkBack(optionsChooseVC, this);
                            optionsChooseVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
                            removeAsListenerAndDispose(ocForm);
                            qtiItemConfig = qtiExportEbl.getQtiItemConfig(qtiExportEbl.getFirstResult(results));
                            ocForm = new OptionsChooseForm(ureq, getWindowControl(), qtiItemConfig);
                            listenTo(ocForm);
                            optionsChooseVC.put("ocForm", ocForm.getInitialComponent());

                            wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
                            wc.setNextWizardStep(getTranslator().translate("wizard.optionschoose.howto"), optionsChooseVC);
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

                final ICourse course = CourseFactory.loadCourse(ores);
                ExportFormatConfig exportFormatConfig = new ExportFormatConfig();
                exportFormatConfig.setSeparator(dcForm.getSeparatedBy());
                exportFormatConfig.setEmbeddedBy(dcForm.getEmbeddedBy());
                exportFormatConfig.setEscapedBy(dcForm.getEscapedBy());
                exportFormatConfig.setCarriageReturn(dcForm.getCarriageReturn());
                exportFormatConfig.setFileNameSuffix(dcForm.getFileNameSuffix());
                exportFormatConfig.setTagless(dcForm.isTagless());

                exportResult = qtiExportEbl.exportResultsForArchiveWithCustomFormat(ureq.getIdentity(), ureq.getLocale(), qtiItemConfig, results, exportFormatConfig,
                        currentCourseNode, course.getCourseTitle());

                finishedVC.contextPut("filename", exportResult.getExportFileName());
                wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
                wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
            }
        }
    }

    @Override
    protected void doDispose() {
        //
    }

}
