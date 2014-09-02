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

package org.olat.presentation.campusmgnt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.presentation.course.archiver.BulkActionGetNodePassed;
import org.olat.presentation.course.archiver.BulkActionGetNodeScore;
import org.olat.presentation.course.assessment.IndentedNodeRenderer;
import org.olat.presentation.course.assessment.NodeTableDataModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
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
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * TODO: schneider Class Description for BringTogetherController
 * <P>
 * Initial Date: 19.12.2005 <br>
 * 
 * @author schneider
 */
public class CampusManagementController extends BasicController {

    private static final String CMD_SELECT_NODE = "cmd.select.node";

    private final VelocityContainer nodeChoose;

    private NodeTableDataModel nodeTableModel;
    private TableController nodeListCtr;

    private CourseNode currentCourseNode;
    private InOutWizardController iowc;
    private CloseableModalController cmc;

    private final OLATResourceable ores;

    /**
     * @param ureq
     * @param wControl
     * @param course
     */
    public CampusManagementController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores) {
        super(ureq, wControl);
        if (ores instanceof ICourse) {
            this.ores = ores;
        } else {
            throw new AssertException("SAPCampusMgntExtension needs a ICourse as the argument parameter: arg = " + ores);
        }
        nodeChoose = this.createVelocityContainer("btnodechoose");
        doNodeChoose(ureq, ores);

        putInitialPanel(nodeChoose);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == nodeListCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals(CMD_SELECT_NODE)) {

                    final ICourse course = CourseFactory.loadCourse(ores);
                    final int rowid = te.getRowId();
                    final Map nodeData = (Map) nodeTableModel.getObject(rowid);
                    this.currentCourseNode = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));

                    final List bulkActions = new ArrayList();
                    final BulkActionGetNodeScore baGetNodeScore = new BulkActionGetNodeScore(course, currentCourseNode, getTranslator());
                    baGetNodeScore.setDisplayName(translate("bulk.action.getnodescore"));
                    bulkActions.add(baGetNodeScore);

                    final BulkActionGetNodePassed baGetNodePassed = new BulkActionGetNodePassed(course, currentCourseNode, getTranslator());
                    baGetNodePassed.setDisplayName(translate("bulk.action.getnodepassed"));
                    bulkActions.add(baGetNodePassed);

                    removeAsListenerAndDispose(iowc);
                    iowc = new InOutWizardController(ureq, bulkActions, getWindowControl());
                    listenTo(iowc);

                    removeAsListenerAndDispose(cmc);
                    cmc = new CloseableModalController(getWindowControl(), translate("close"), iowc.getInitialComponent());
                    listenTo(cmc);

                    cmc.activate();
                }
            }
        } else if (source == iowc) {
            if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
                cmc.deactivate();
            }
        }
    }

    /**
     * @param ureq
     */
    private void doNodeChoose(final UserRequest ureq, final OLATResourceable ores) {

        final ICourse course = CourseFactory.loadCourse(ores);
        // table configuraton
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("btnodesoverview.nonodes"));
        tableConfig.setDownloadOffered(false);
        tableConfig.setColumnMovingOffered(false);
        tableConfig.setSortingEnabled(false);
        tableConfig.setDisplayTableHeader(true);
        tableConfig.setDisplayRowCount(false);
        tableConfig.setPageingEnabled(false);

        removeAsListenerAndDispose(nodeListCtr);
        nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(nodeListCtr);

        // table columns
        nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new IndentedNodeRenderer()));
        nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, ureq.getLocale()));

        // get list of course node data and populate table data model
        final CourseNode rootNode = course.getRunStructure().getRootNode();
        final List nodesTableObjectArrayList = addAssessableNodesAndParentsToList(0, rootNode);

        // only populate data model if data available
        if (nodesTableObjectArrayList == null) {
            nodeChoose.contextPut("hasAssessableNodes", Boolean.FALSE);
        } else {
            nodeChoose.contextPut("hasAssessableNodes", Boolean.TRUE);
            nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
            nodeListCtr.setTableDataModel(nodeTableModel);
            nodeChoose.put("nodeTable", nodeListCtr.getInitialComponent());
        }
    }

    /**
     * Recursive method that adds tasks nodes and all its parents to a list
     * 
     * @param recursionLevel
     * @param courseNode
     * @return A list of Object[indent, courseNode, selectable]
     */
    private List addAssessableNodesAndParentsToList(final int recursionLevel, final CourseNode courseNode) {
        // 1) Get list of children data using recursion of this method
        final List childrenData = new ArrayList();
        for (int i = 0; i < courseNode.getChildCount(); i++) {
            final CourseNode child = (CourseNode) courseNode.getChildAt(i);
            final List childData = addAssessableNodesAndParentsToList((recursionLevel + 1), child);
            if (childData != null) {
                childrenData.addAll(childData);
            }
        }

        if (childrenData.size() > 0 || courseNode instanceof AssessableCourseNode) {
            // Store node data in hash map. This hash map serves as data model for
            // the tasks overview table. Leave user data empty since not used in
            // this table. (use only node data)
            final Map nodeData = new HashMap();
            // indent
            nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
            // course node data
            nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
            nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());
            nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
            nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());

            // apply special assessable case for STCourseNode which is dynamically assessable or not.
            if (AssessmentHelper.checkIfNodeIsAssessable(courseNode)) {
                nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
            } else {
                nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
            }

            final List nodeAndChildren = new ArrayList();
            nodeAndChildren.add(nodeData);

            nodeAndChildren.addAll(childrenData);
            return nodeAndChildren;
        }
        return null;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
