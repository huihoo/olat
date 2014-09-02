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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.presentation.course.archiver.GenericArchiveController;
import org.olat.presentation.course.assessment.IndentedNodeRenderer;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Main controller for the SCORM archive export. All the job is done in the wizard
 * <P>
 * Initial Date: 17 august 2009 <br>
 * 
 * @author srosse
 */
public class ScormResultArchiveController extends BasicController {

    private static final String CMD_SELECT_NODE = "cmd.select.node";

    private final VelocityContainer introVC;

    private TableController nodeListCtr;
    private ScormArchiveWizardController wizardController;
    private CloseableModalController cmc;

    private final Long courseId;

    private final List<Map<String, Object>> nodesTableObjectArrayList;
    private final Link startExportButton;

    public ScormResultArchiveController(final UserRequest ureq, final WindowControl wControl, final ICourse course) {
        super(ureq, wControl, PackageUtil.createPackageTranslator(GenericArchiveController.class, ureq.getLocale()));

        courseId = course.getResourceableId();

        introVC = createVelocityContainer("intro");
        startExportButton = LinkFactory.createButtonSmall("command.start.exportwizard", introVC, this);

        nodesTableObjectArrayList = doNodeChoose(ureq);

        if (nodesTableObjectArrayList == null) {
            introVC.contextPut("hasScormNodes", Boolean.FALSE);
        } else {
            introVC.contextPut("hasScormNodes", Boolean.TRUE);
        }

        putInitialPanel(introVC);
    }

    @Override
    protected void doDispose() {
        // controllers autodisposed by basic controller
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startExportButton) {

            removeAsListenerAndDispose(wizardController);
            wizardController = new ScormArchiveWizardController(ureq, nodesTableObjectArrayList, courseId, getWindowControl());
            listenTo(wizardController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), wizardController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == wizardController) {
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            }
        }
    }

    private List<Map<String, Object>> doNodeChoose(final UserRequest ureq) {
        // table configuraton
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
        tableConfig.setDownloadOffered(false);
        tableConfig.setColumnMovingOffered(false);
        tableConfig.setSortingEnabled(false);
        tableConfig.setDisplayTableHeader(true);
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);

        removeAsListenerAndDispose(nodeListCtr);
        nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), this.getTranslator());
        listenTo(nodeListCtr);
        // table columns
        nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new IndentedNodeRenderer()));
        nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, ureq.getLocale()));

        // get list of course node data and populate table data model
        final ICourse course = CourseFactory.loadCourse(courseId);
        final CourseNode rootNode = course.getRunStructure().getRootNode();
        final List<Map<String, Object>> objectArrayList = addScormNodesAndParentsToList(0, rootNode);
        return objectArrayList;
    }

    private List<Map<String, Object>> addScormNodesAndParentsToList(final int recursionLevel, final CourseNode courseNode) {
        // 1) Get list of children data using recursion of this method
        final List<Map<String, Object>> childrenData = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < courseNode.getChildCount(); i++) {
            final CourseNode child = (CourseNode) courseNode.getChildAt(i);
            final List<Map<String, Object>> childData = addScormNodesAndParentsToList((recursionLevel + 1), child);
            if (childData != null) {
                childrenData.addAll(childData);
            }
        }

        if (!childrenData.isEmpty() || courseNode instanceof ScormCourseNode) {
            // Store node data in hash map. This hash map serves as data model for
            // the tasks overview table. Leave user data empty since not used in
            // this table. (use only node data)
            final Map<String, Object> nodeData = new HashMap<String, Object>();
            // indent
            nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
            // course node data
            nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
            nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());
            nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
            nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());
            nodeData.put(AssessmentHelper.KEY_SELECTABLE, (courseNode instanceof ScormCourseNode) ? Boolean.TRUE : Boolean.FALSE);

            final List<Map<String, Object>> nodeAndChildren = new ArrayList<Map<String, Object>>();
            nodeAndChildren.add(nodeData);
            nodeAndChildren.addAll(childrenData);
            return nodeAndChildren;
        }
        return null;
    }
}
