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

package org.olat.presentation.course.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.util.BulkAction;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentHelper;
import org.olat.lms.course.assessment.BulkActionSetNodePassed;
import org.olat.lms.course.assessment.BulkActionSetNodeScore;
import org.olat.lms.course.assessment.BulkActionSetNodeUserComment;
import org.olat.lms.course.nodes.AssessableCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.GenericObjectArrayTableDataModel;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.WizardController;
import org.olat.presentation.framework.core.translator.HeaderColumnTranslator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * 6 step wizard to assess many users at once with scores, passed or comments
 * <P>
 * Initial Date: 19.12.2005 <br>
 * 
 * @author Alexander Schneider
 */
public class BulkAssessmentWizardController extends BasicController {
    private static final String CMD_SELECT_NODE = "cmd.select.node";
    private final WizardController wc;

    private CourseNode currentCourseNode;

    private final int steps = 6;
    private final Panel main;
    private final VelocityContainer step1VC; // user chooses bulkaction
    private VelocityContainer step2VC; // user chooses ManualScoreNode or TaskNode
    private VelocityContainer step3VC; // user pastes separated values
    private VelocityContainer step4VC; // user matches column with olat key (username, email, institutionalident)
    private VelocityContainer step5VC; // user matches column with bulkaction
    private VelocityContainer step6VC; // feedback

    private Link back2;
    private Link back3;
    private Link back4;
    private Link back5;
    private Link back6;

    private final BulkStep1Form bulkStep1Form; // select bulkaction
    private BulkStep3Form bulkStep3Form; // input of separated values
    private BulkStep4Form bulkStep4Form; // match column with olat key
    private BulkStep5Form bulkStep5Form; // match column with bulkaction

    private TableController nodeListCtr;
    private TableController feedbackDataTableCtr;

    private NodeTableDataModel nodeTableModel;
    private final List bulkActions;
    private List olatKeys;
    private List rows; // contains String arrays which are representing an input row
    private int numOfLines;
    private int numOfValPerLine;
    private List columnNames;
    private TableGuiConfiguration tableConfig;
    private HeaderColumnTranslator hcTranslator;
    private TableController dataTableCtr;
    private GenericObjectArrayTableDataModel dataModel;
    private int selectedColForOlatKey;
    private int selectedOlatKey;
    private String selectedValueOfOlatKey;
    private String selectedBulk;
    private String bulkActionDisplayName;
    private String currentNodeShortTitle;
    private int bulkType;
    private Link finishedButton;
    private final OLATResourceable ores;
    private List<Object[]> bulkResults;

    /**
     * @param ureq
     * @param wControl
     */
    public BulkAssessmentWizardController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final List allowedIdentities) {
        super(ureq, wControl);
        // use user property handler translator as fallback
        setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
        this.ores = ores;
        main = new Panel("main");
        step1VC = createVelocityContainer("bulkstep1");

        bulkActions = new ArrayList();
        // bulkType: 0
        final BulkActionSetNodeScore baSetNodeScore = new BulkActionSetNodeScore(ores, allowedIdentities, ureq.getIdentity(), getTranslator());
        baSetNodeScore.setDisplayName(translate("bulk.action.setnodescore"));
        bulkActions.add(baSetNodeScore);

        // bulkType: 1
        final BulkActionSetNodePassed baSetNodePassed = new BulkActionSetNodePassed(ores, allowedIdentities, ureq.getIdentity(), getTranslator());
        baSetNodePassed.setDisplayName(translate("bulk.action.setnodepassed"));
        bulkActions.add(baSetNodePassed);

        // bulkType: 2
        final BulkActionSetNodeUserComment baSetNodeUserComment = new BulkActionSetNodeUserComment(ores, allowedIdentities, ureq.getIdentity(), getTranslator());
        baSetNodeUserComment.setDisplayName(translate("bulk.action.setnodeusercomment"));
        bulkActions.add(baSetNodeUserComment);

        bulkStep1Form = new BulkStep1Form(ureq, wControl, bulkActions);
        listenTo(bulkStep1Form);

        step1VC.put("step1Form", bulkStep1Form.getInitialComponent());

        wc = new WizardController(ureq, wControl, steps);
        listenTo(wc);
        wc.setWizardTitle(translate("wizard.step1.title"));
        wc.setNextWizardStep(translate("wizard.step1.howto"), step1VC);
        main.setContent(wc.getInitialComponent());
        putInitialPanel(main);
    }

    /**
     * This dispatches component events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        // component events from step 2, controller events are handled by controller events dispatcher
        if (source == back2) {
            wc.setWizardTitle(translate("wizard.step1.title"));
            wc.setBackWizardStep(translate("wizard.step1.howto"), step1VC);
        }

        else if (source == back3) {
            wc.setWizardTitle(translate("wizard.step2.title"));
            wc.setBackWizardStep(translate("wizard.step2.howto"), step2VC);
        }

        else if (source == back4) {
            wc.setWizardTitle(translate("wizard.step3.title"));
            wc.setBackWizardStep(translate("wizard.step3.howto"), step3VC);
        }

        else if (source == back5) {
            wc.setWizardTitle(translate("wizard.step4.title"));
            wc.setBackWizardStep(translate("wizard.step4.howto"), step4VC);
        }
        // events from step 6
        else if (source == back6) {
            wc.setWizardTitle(translate("wizard.step5.title"));
            wc.setBackWizardStep(translate("wizard.step5.howto"), step5VC);
        } else if (source == finishedButton) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    /**
     * This dispatches controller events...
     * 
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // events from step 1, preparing step 2
        if (source == bulkStep1Form && event == Event.DONE_EVENT) {
            step2VC = createVelocityContainer("bulkstep2");
            back2 = LinkFactory.createLinkBack(step2VC, this);

            selectedBulk = bulkStep1Form.getSelectedBulkAction();
            bulkType = Integer.parseInt(selectedBulk);
            final BulkAction ba = (BulkAction) bulkActions.get(bulkType);
            bulkActionDisplayName = ba.getDisplayName();
            step2VC.contextPut("bulkActionDisplayName", bulkActionDisplayName);

            doNodeChoose(ureq);
            wc.setWizardTitle(translate("wizard.step2.title"));
            wc.setNextWizardStep(translate("wizard.step2.howto"), step2VC);
        }
        // event from step 2, preparing step 3
        if (source == nodeListCtr && event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
            final TableEvent te = (TableEvent) event;
            final String actionid = te.getActionId();
            if (actionid.equals(CMD_SELECT_NODE)) {
                final int rowid = te.getRowId();
                final Map nodeData = (Map) nodeTableModel.getObject(rowid);
                final ICourse course = CourseFactory.loadCourse(ores);
                this.currentCourseNode = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
                currentNodeShortTitle = currentCourseNode.getShortTitle();

                removeAsListenerAndDispose(bulkStep3Form);
                bulkStep3Form = new BulkStep3Form(ureq, getWindowControl());
                listenTo(bulkStep3Form);

                step3VC = createVelocityContainer("bulkstep3");
                step3VC.contextPut("currentNodeShortTitle", currentNodeShortTitle);
                step3VC.contextPut("bulkActionDisplayName", bulkActionDisplayName);

                back3 = LinkFactory.createLinkBack(step3VC, this);

                step3VC.put("step3Form", bulkStep3Form.getInitialComponent());

                wc.setWizardTitle(translate("wizard.step3.title"));
                wc.setNextWizardStep(translate("wizard.step3.howto"), step3VC);
            }
        }
        // events from step 3, preparing step 4
        else if (source == bulkStep3Form && event == Event.DONE_EVENT) {

            this.rows = bulkStep3Form.getInputRows();
            this.numOfLines = bulkStep3Form.getNumOfLines();
            this.numOfValPerLine = bulkStep3Form.getNumOfValPerLine();

            columnNames = new ArrayList();
            tableConfig = new TableGuiConfiguration();
            tableConfig.setDownloadOffered(false);
            tableConfig.setSortingEnabled(false);
            tableConfig.setColumnMovingOffered(false);
            hcTranslator = new HeaderColumnTranslator(getTranslator());

            removeAsListenerAndDispose(dataTableCtr);
            dataTableCtr = new TableController(tableConfig, ureq, getWindowControl(), hcTranslator);
            listenTo(dataTableCtr);

            for (int i = 0; i < numOfValPerLine; i++) {
                dataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ccc" + (i + 1), i, null, ureq.getLocale()));
                // add every name of a column to a list deployed as pulldown to the user for matching column with olat key
                columnNames.add(getTranslator().translate("column", new String[] { "" + (i + 1) }));
            }

            step4VC = createVelocityContainer("bulkstep4");
            step4VC.contextPut("bulkActionDisplayName", bulkActionDisplayName);
            step4VC.contextPut("currentNodeShortTitle", currentNodeShortTitle);

            back4 = LinkFactory.createLinkBack(step4VC, this);

            dataModel = new GenericObjectArrayTableDataModel(rows, numOfLines);
            // the data model already contains the column for the feedback,
            // which is used at step 6 and not yet shown to the user
            dataTableCtr.setTableDataModel(dataModel);
            step4VC.put("dataTable", dataTableCtr.getInitialComponent());

            olatKeys = new ArrayList();
            // adding order is relevant for the "if-else if"-statement below at events from step 5
            olatKeys.add(translate("username"));
            olatKeys.add(translate("form.name.email"));

            removeAsListenerAndDispose(bulkStep4Form);
            bulkStep4Form = new BulkStep4Form(ureq, getWindowControl(), olatKeys, columnNames);
            listenTo(bulkStep4Form);

            step4VC.put("step4Form", bulkStep4Form.getInitialComponent());

            wc.setWizardTitle(translate("wizard.step4.title"));
            wc.setNextWizardStep(translate("wizard.step4.howto"), step4VC);

        }
        // events from step 4, preparing step 5
        else if (source == bulkStep4Form && event == Event.DONE_EVENT) {

            selectedColForOlatKey = Integer.parseInt(bulkStep4Form.getSelectedColumn());
            selectedOlatKey = Integer.parseInt(bulkStep4Form.getSelectedOlatKey());
            selectedValueOfOlatKey = (String) olatKeys.get(selectedOlatKey);

            step5VC = createVelocityContainer("bulkstep5");
            step5VC.contextPut("bulkActionDisplayName", bulkActionDisplayName);
            step5VC.contextPut("currentNodeShortTitle", currentNodeShortTitle);

            back5 = LinkFactory.createLinkBack(step5VC, this);

            removeAsListenerAndDispose(dataTableCtr);
            dataTableCtr = new TableController(tableConfig, ureq, getWindowControl(), hcTranslator);
            listenTo(dataTableCtr);

            columnNames = null;
            columnNames = new ArrayList();
            for (int i = 0; i < numOfValPerLine; i++) {
                if (i != selectedColForOlatKey) {
                    dataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ccc" + (i + 1), i, null, ureq.getLocale()));
                    // add every name of a column to a list deployed as pulldown to the user for matching column with bulk action
                    columnNames.add(getTranslator().translate("column", new String[] { "" + (i + 1) }));
                } else {
                    dataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("hhh" + selectedValueOfOlatKey, i, null, ureq.getLocale()));
                }
            }

            dataTableCtr.setTableDataModel(dataModel);
            step5VC.put("dataTable", dataTableCtr.getInitialComponent());

            removeAsListenerAndDispose(bulkStep5Form);
            bulkStep5Form = new BulkStep5Form(ureq, getWindowControl(), columnNames, bulkActions);
            listenTo(bulkStep5Form);

            step5VC.put("step5Form", bulkStep5Form.getInitialComponent());

            wc.setWizardTitle(translate("wizard.step5.title"));
            wc.setNextWizardStep(translate("wizard.step5.howto"), step5VC);
        }
        // events from step 5, preparing step 6
        else if (source == bulkStep5Form && event == Event.DONE_EVENT) { // user clicked 'next'-button !!!!!!!
            final List rowsFeedbackStep = new ArrayList(rows.size());
            for (final Iterator iter = rows.iterator(); iter.hasNext();) {
                final Object[] values = (Object[]) iter.next();
                rowsFeedbackStep.add(values.clone());
            }
            final String selectedColForBulk = bulkStep5Form.getSelectedColumn();
            int colForBulk = Integer.parseInt(selectedColForBulk);
            // While the user is matching the olatKey with a table column the relation between
            // table indices and pulldown menu indices is ok, but now while matching the bulkaction
            // with a table column the indices are only ok in certain cases, because the user choosen
            // column for the olatKey is not more shown in the second pulldown menu. It means by example
            // if the user choose pulldown index [1] for the olatKey, it really meets the table index[1],
            // then in the next step the pulldown has not more as many indices as before.
            // If the user is now choosing the pulldown index[1] for the bulkaction he would meet the before selected
            // column for the olatKey, if the system id not adding 1 to the index count.
            if (selectedColForOlatKey <= colForBulk) {
                colForBulk++;
            }

            step6VC = createVelocityContainer("bulkstep6");
            finishedButton = LinkFactory.createButtonSmall("finished", step6VC, this);
            step6VC.put("finishedButton", finishedButton);

            step6VC.contextPut("bulkActionDisplayName", bulkActionDisplayName);
            step6VC.contextPut("currentNodeShortTitle", currentNodeShortTitle);

            back6 = LinkFactory.createLinkBack(step6VC, this);

            final GenericObjectArrayTableDataModel feedbackDataModel = new GenericObjectArrayTableDataModel(rowsFeedbackStep, numOfLines);

            final BulkAction ba = (BulkAction) bulkActions.get(bulkType);

            // TODO: as:b may be add additional class hierarchy (ask pb !!!)
            if ((ba instanceof BulkActionSetNodeScore)) {
                ((BulkActionSetNodeScore) ba).setCourseNode((AssessableCourseNode) currentCourseNode);
            } else if ((ba instanceof BulkActionSetNodePassed)) {
                ((BulkActionSetNodePassed) ba).setCourseNode((AssessableCourseNode) currentCourseNode);
            } else if ((ba instanceof BulkActionSetNodeUserComment)) {
                ((BulkActionSetNodeUserComment) ba).setCourseNode((AssessableCourseNode) currentCourseNode);
            }

            final List identitiesAndTheirAssessments = new ArrayList(feedbackDataModel.getRowCount());
            // read values from the column which the user has defined as olat key (e.g. username)
            // and add them to a list.
            for (int i = 0; i < feedbackDataModel.getRowCount(); i++) {
                String val = (String) feedbackDataModel.getValueAt(i, selectedColForOlatKey);
                val = val.trim();
                final String assessment = (String) feedbackDataModel.getValueAt(i, colForBulk);
                Identity identity = null;

                if (selectedOlatKey == 0) { // username
                    identity = getBaseSecurity().findIdentityByName(val);
                } else if (selectedOlatKey == 1) { // email
                    identity = getUserService().findIdentityByEmail(val);
                }
                identitiesAndTheirAssessments.add(new Object[] { identity, assessment });
            }
            bulkResults = ba.doAction(identitiesAndTheirAssessments);

            // add the bulk results to the data model
            for (int i = 0; i < feedbackDataModel.getRowCount(); i++) {
                final Object[] feedback = bulkResults.get(i);
                feedbackDataModel.setValueAt(feedback[2], i, numOfValPerLine);
            }

            tableConfig.setDownloadOffered(true);

            removeAsListenerAndDispose(feedbackDataTableCtr);
            feedbackDataTableCtr = new TableController(tableConfig, ureq, getWindowControl(), hcTranslator);
            listenTo(feedbackDataTableCtr);

            columnNames = null;
            columnNames = new ArrayList();
            for (int i = 0; i < numOfValPerLine; i++) {
                if (i == selectedColForOlatKey) {
                    feedbackDataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("hhh" + selectedValueOfOlatKey, i, null, ureq.getLocale()));
                } else if (i == colForBulk) {
                    feedbackDataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("hhh" + ba.getDisplayName(), i, null, ureq.getLocale()));
                } else {
                    feedbackDataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ccc" + (i + 1), i, null, ureq.getLocale()));
                }
            }
            // add feedback columns
            feedbackDataTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("wizard.step6.feedback.column", numOfValPerLine, null, ureq.getLocale()));

            feedbackDataTableCtr.setTableDataModel(feedbackDataModel);
            step6VC.put("dataTable", feedbackDataTableCtr.getInitialComponent());

            wc.setWizardTitle(translate("wizard.step6.title"));
            wc.setNextWizardStep(translate("wizard.step6.howto"), step6VC);
        } else if (source == wc && event == Event.CANCELLED_EVENT) {
            if (bulkResults != null) {
                sendConfirmation(ureq.getIdentity());
            }
            fireEvent(ureq, event);
        }
    }

    private void sendConfirmation(Identity identity) {
        ICourse course = CourseFactory.loadCourse(ores);
        AssessmentConfirmationSender confirmationSender = new BulkAssessmentConfirmationSenderImpl(identity, (AssessableCourseNode) currentCourseNode, course,
                bulkResults);
        confirmationSender.sendAssessmentConfirmation();
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * @param ureq
     */
    @SuppressWarnings("unchecked")
    private void doNodeChoose(final UserRequest ureq) {
        // table configuraton
        final TableGuiConfiguration tableConfigNodeChoose = new TableGuiConfiguration();
        tableConfigNodeChoose.setTableEmptyMessage(translate("wizard.step2.nonodes"));
        tableConfigNodeChoose.setDownloadOffered(false);
        tableConfigNodeChoose.setColumnMovingOffered(false);
        tableConfigNodeChoose.setSortingEnabled(false);
        tableConfigNodeChoose.setDisplayTableHeader(true);
        tableConfigNodeChoose.setDisplayRowCount(false);
        tableConfigNodeChoose.setPageingEnabled(false);

        removeAsListenerAndDispose(nodeListCtr);
        nodeListCtr = new TableController(tableConfigNodeChoose, ureq, getWindowControl(), getTranslator());
        listenTo(nodeListCtr);

        // table columns
        nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new IndentedNodeRenderer()));
        nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, ureq.getLocale()));

        // get list of course node data and populate table data model
        final ICourse course = CourseFactory.loadCourse(ores);
        final CourseNode rootNode = course.getRunStructure().getRootNode();
        final List nodesTableObjectArrayList = addManualTaskNodesAndParentsToList(0, rootNode);

        // only populate data model if data available
        if (nodesTableObjectArrayList == null) {
            step2VC.contextPut("hasMsOrTaNodes", Boolean.FALSE);
        } else {
            step2VC.contextPut("hasMsOrTaNodes", Boolean.TRUE);
            nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
            nodeListCtr.setTableDataModel(nodeTableModel);
            step2VC.put("nodeTable", nodeListCtr.getInitialComponent());
        }
    }

    /**
     * Recursive method that adds manual and tasks nodes and all its parents to a list
     * 
     * @param recursionLevel
     * @param courseNode
     * @return A list of Object[indent, courseNode, selectable]
     */
    @SuppressWarnings("unchecked")
    private List addManualTaskNodesAndParentsToList(final int recursionLevel, final CourseNode courseNode) {
        // 1) Get list of children data using recursion of this method
        final List childrenData = new ArrayList();
        for (int i = 0; i < courseNode.getChildCount(); i++) {
            final CourseNode child = (CourseNode) courseNode.getChildAt(i);
            final List childData = addManualTaskNodesAndParentsToList((recursionLevel + 1), child);
            if (childData != null) {
                childrenData.addAll(childData);
            }
        }

        boolean bulkAssessability = false;

        if (courseNode instanceof MSCourseNode || courseNode instanceof TACourseNode || courseNode instanceof ProjectBrokerCourseNode) {
            if (bulkType == 0) {
                bulkAssessability = ((AssessableCourseNode) courseNode).hasScoreConfigured();
            } else if (bulkType == 1) {
                if (((AssessableCourseNode) courseNode).hasPassedConfigured() && ((AssessableCourseNode) courseNode).getCutValueConfiguration() == null) {
                    bulkAssessability = true;
                }
            } else if (bulkType == 2) {
                bulkAssessability = ((AssessableCourseNode) courseNode).hasCommentConfigured();
            } else {
                throw new OLATRuntimeException("Undefined bulk assessement type in:" + this.getClass(), null);
            }
        }

        if (childrenData.size() > 0 || bulkAssessability) {
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

            if (bulkAssessability) {
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

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
