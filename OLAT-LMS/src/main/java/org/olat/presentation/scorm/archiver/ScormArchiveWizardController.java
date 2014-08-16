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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.scorm.archiver;

import java.util.List;
import java.util.Map;

import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.scorm.archiver.ScormArchiverEBL;
import org.olat.lms.scorm.archiver.ScormExportManager;
import org.olat.presentation.course.archiver.GenericArchiveController;
import org.olat.presentation.course.assessment.IndentedNodeRenderer;
import org.olat.presentation.course.assessment.NodeTableDataModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
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
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * A wizard to export the results of a SCORM course in a tab separated file. There is two steps: select a SCORM element and then download the file.
 * <P>
 * Initial Date: 17 août 2009 <br>
 * 
 * @author srosse
 */
public class ScormArchiveWizardController extends BasicController {

    private static final String CMD_SELECT_NODE = "cmd.select.node";

    private final ICourse course;
    private final NodeTableDataModel nodeTableModel;

    private Link showFileButton;
    private final WizardController wc;
    private final TableController nodeListCtr;
    private VelocityContainer finishedVC;
    private VelocityContainer noResultsVC;
    private Link backLinkAtNoResults;

    private String targetFileName;

    private ScormArchiverEBL scormArchiverEBL;

    public ScormArchiveWizardController(final UserRequest ureq, final List<Map<String, Object>> nodesTableObjectArrayList, final Long courseId,
            final WindowControl wControl) {
        super(ureq, wControl, PackageUtil.createPackageTranslator(GenericArchiveController.class, ureq.getLocale()));
        course = CourseFactory.loadCourse(courseId);
        scormArchiverEBL = getScormArchiverEBL(ureq.getIdentity(), course.getCourseTitle());

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

        wc = new WizardController(ureq, wControl, 2);
        listenTo(wc);
        wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
        wc.setNextWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeListCtr.getInitialComponent());
        putInitialPanel(wc.getInitialComponent());
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showFileButton) {
            final MediaResource resource = scormArchiverEBL.getScormArchiverMediaResource(targetFileName);
            ureq.getDispatchResult().setResultingMediaResource(resource);
            showFileButton.setDirty(false);
        } else if (source == backLinkAtNoResults) {
            wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
            wc.setBackWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeListCtr.getInitialComponent());
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == nodeListCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent tae = (TableEvent) event;

                final Map<String, Object> nodeData = (Map<String, Object>) nodeTableModel.getObject(tae.getRowId());
                final CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
                if (node instanceof ScormCourseNode) {
                    finishedVC = createVelocityContainer("finished");
                    showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
                    finishedVC.contextPut("nodetitle", node.getShortTitle());

                    final boolean hasResults = ScormExportManager.getInstance().hasResults(course.getCourseEnvironment(), node, getTranslator());
                    if (hasResults) {
                        doExport(ureq, (ScormCourseNode) node);

                        finishedVC.contextPut("filename", targetFileName);
                        wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
                        wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
                    } else { // no success
                        noResultsVC = createVelocityContainer("noresults");
                        backLinkAtNoResults = LinkFactory.createLinkBack(noResultsVC, this);
                        noResultsVC.contextPut("nodetitle", node.getShortTitle());

                        wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
                        wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), noResultsVC);
                    }
                }
            }
        } else if (source == wc) {
            if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, event);
            }
        }
    }

    private void doExport(final UserRequest ureq, final ScormCourseNode node) {

        final ScormExportManager sreManager = ScormExportManager.getInstance();
        targetFileName = sreManager.exportResults(course.getCourseEnvironment(), node, getTranslator(), scormArchiverEBL.getExportDirPath(),
                scormArchiverEBL.getCharset());
    }

    private ScormArchiverEBL getScormArchiverEBL(Object... parameters) {
        return CoreSpringFactory.getBean(ScormArchiverEBL.class, parameters);
    }

}
