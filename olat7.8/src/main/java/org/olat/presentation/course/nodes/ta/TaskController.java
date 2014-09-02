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

package org.olat.presentation.course.nodes.ta;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.ta.Task_EBL;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 02.09.2004
 * 
 * @author Mike Stock Comment:
 */

public class TaskController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String ACTION_PREVIEW = "ta.preview";
    private static final String ACTION_SELECT = "seltask";
    private static final String ACTION_DESELECT = "deseltask";

    private static final String VC_NOMORETASKS = "nomoretasks";
    private static final String VC_ASSIGNEDTASK = "assignedtask";
    private static final String VC_ASSIGNEDTASK_NEWWINDOW = "newwindow";
    private static final String VC_TASKTEXT = "taskText";

    /** Configuration parameter indicating manual selection task type. */
    public static final String TYPE_MANUAL = "manual";
    /** Configuration parameter indicating auto selection task type. */
    public static final String TYPE_AUTO = "auto";
    /** Configuration parameter indicating task-preview mode. */
    public static final String WITH_PREVIEW = "preview";
    /** Configuration parameter indicating non task-preview mode. */
    public static final String WITHOUT_PREVIEW = "no_preview";
    /** Configuration parameter indicating task-deselect mode. */
    public static final String WITH_DESELECT = "deselect";
    /** Configuration parameter indicating non task-deselect mode. */
    public static final String WITHOUT_DESELECT = "no_deselect";

    // config
    private String taskType;
    private String taskText;
    public boolean samplingWithReplacement = true;
    private Boolean hasPreview = Boolean.FALSE;
    private Boolean isDeselectable = Boolean.FALSE;

    public final CourseEnvironment courseEnv;
    public final CourseNode node;

    private VelocityContainer myContent;
    private final Link taskLaunchButton;
    private TableController tableCtr;
    private DeselectableTaskTableModel taskTableModel;
    private String assignedTask;

    private final Panel panel;

    private Task_EBL taskEbl;

    /**
     * Implements a task component.
     * 
     * @param ureq
     * @param wControl
     * @param config
     * @param node
     * @param courseEnv
     */
    public TaskController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final CourseNode node, final CourseEnvironment courseEnv) {
        super(ureq, wControl);

        this.node = node;
        this.courseEnv = courseEnv;
        taskEbl = CoreSpringFactory.getBean(Task_EBL.class);
        readConfig(config);

        panel = new Panel("myContentPanel");

        myContent = createVelocityContainer("taskAssigned");

        taskLaunchButton = LinkFactory.createButtonSmall("task.launch", myContent, this);
        taskLaunchButton.setTarget("_blank");
        taskLaunchButton.setAjaxEnabled(false); // opened in new window

        if ((taskText != null) && (taskText.length() > 0)) {
            myContent.contextPut(VC_TASKTEXT, taskText);
        }

        // check if user already chose a task
        assignedTask = taskEbl.getAssignedTask(ureq.getIdentity(), courseEnv, node);
        if (assignedTask != null && !isDeselectable()) { //
            pushTaskToVC();
        } else {
            // prepare choose task
            if (taskType.equals(TYPE_AUTO)) { // automatically choose a task
                assignedTask = taskEbl.autoAssignTask(ureq.getIdentity(), courseEnv, node, samplingWithReplacement);
                if (assignedTask != null) {
                    pushTaskToVC();
                } else {
                    myContent.contextPut(VC_NOMORETASKS, translate("task.nomoretasks"));
                    panel.setContent(myContent);
                }
            } else { // let user choose a task, or show the table with the available/selected task

                myContent = createVelocityContainer("taskChoose");

                final List availableTasks = taskEbl.compileAvailableTasks(courseEnv, node);
                if (availableTasks.size() == 0 && assignedTask == null) { // no more tasks available
                    myContent.contextPut(VC_NOMORETASKS, translate("task.nomoretasks"));
                } else {

                    final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
                    tableCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
                    listenTo(tableCtr);

                    // No Preview Mode, Show only file-name
                    tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("task.table.th_task", 0, null, ureq.getLocale()));
                    if ((hasPreview != null) && (hasPreview.booleanValue() == true)) {
                        // Preview Mode
                        final DefaultColumnDescriptor columnDescriptor = new DefaultColumnDescriptor("task.table.th_task", 1, ACTION_PREVIEW, ureq.getLocale());
                        columnDescriptor.setIsPopUpWindowAction(true, DefaultColumnDescriptor.DEFAULT_POPUP_ATTRIBUTES);
                        tableCtr.addColumnDescriptor(columnDescriptor);
                    }
                    // always have a select column
                    tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("task.table.th_action", 2, ACTION_SELECT, translate("task.table.choose"), "-"));

                    int numCols = 0;
                    final Boolean taskCouldBeDeselected = config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);
                    if (!hasPreview) {
                        numCols = 2;
                    } else if (taskCouldBeDeselected == null || !taskCouldBeDeselected) {
                        numCols = 3;
                    } else if (taskCouldBeDeselected) {
                        numCols = 4;
                        tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("task.table.th_deselect", 3, ACTION_DESELECT, translate("task.table.deselect"), "-"));
                    }
                    // the table model shows the available tasks, plus the selected one, if deselectable
                    if (isDeselectable() && assignedTask != null && !availableTasks.contains(assignedTask)) {
                        availableTasks.add(assignedTask);
                    }
                    taskTableModel = new DeselectableTaskTableModel(availableTasks, numCols);
                    tableCtr.setTableDataModel(taskTableModel);
                    myContent.put("taskTable", tableCtr.getInitialComponent());
                }
            }
        }
        panel.setContent(myContent);
        putInitialPanel(panel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        log.debug("Test Component.event source" + source + "  , event=" + event);
        if (source == taskLaunchButton) {
            // deliver files the same way as in preview
            doFileDelivery(ureq, assignedTask);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        log.debug("Test Controller.event source" + source + "  , event=" + event);
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent ta = (TableEvent) event;
                if (ta.getActionId().equals(TaskController.ACTION_PREVIEW)) {
                    final String previewTask = (String) taskTableModel.getValueAt(ta.getRowId(), 0);
                    doFileDelivery(ureq, previewTask);
                } else if (ta.getActionId().equals(TaskController.ACTION_SELECT)) {
                    // select a task
                    assignedTask = (String) taskTableModel.getValueAt(ta.getRowId(), 0);
                    final List availableTasks = taskEbl.compileAvailableTasks(courseEnv, node);
                    if (!availableTasks.contains(assignedTask)) {
                        showWarning("task.chosen");
                        taskTableModel.setObjects(availableTasks);
                        tableCtr.modelChanged();
                        if (availableTasks.size() == 0) { // no more tasks available
                            myContent.contextPut(VC_NOMORETASKS, translate("task.nomoretasks"));
                        }
                    } else {
                        taskEbl.setAssignedTask(ureq.getIdentity(), courseEnv, node, assignedTask);
                        if (!samplingWithReplacement) {
                            taskEbl.markTaskAsSampled(assignedTask, courseEnv, node);
                        }
                        if (!isDeselectable()) {
                            pushTaskToVC();
                        } else {
                            // if assignedTask selected, and deselectable, update taskTableModel
                            final List allTasks = taskEbl.compileAvailableTasks(courseEnv, node);
                            if (!samplingWithReplacement) {
                                // if assignable to only one user, this means that the assignedTask is no more in the availableTasks, but show it in taskTableModel
                                allTasks.add(assignedTask);
                            }
                            taskTableModel.setObjects(allTasks);
                            tableCtr.modelChanged();
                        }
                    }
                } else if (ta.getActionId().equals(TaskController.ACTION_DESELECT)) {
                    if (assignedTask != null) {
                        taskEbl.removeAssignedTask(ureq.getIdentity(), assignedTask, courseEnv, node);
                        assignedTask = null;
                        final List availableTasks = taskEbl.compileAvailableTasks(courseEnv, node);
                        taskTableModel.setObjects(availableTasks);
                        tableCtr.modelChanged();
                    }
                }
            }
        }
    }

    private void pushTaskToVC() {
        if (assignedTask == null) {
            return;
        }

        myContent = createVelocityContainer("taskAssigned");
        myContent.put("task.launch", taskLaunchButton);
        myContent.contextPut(VC_ASSIGNEDTASK, assignedTask);
        myContent.contextPut(VC_ASSIGNEDTASK_NEWWINDOW, Boolean.TRUE);
        panel.setContent(myContent);
    }

    private void readConfig(final ModuleConfiguration config) {
        // get task type
        taskType = (String) config.get(TACourseNode.CONF_TASK_TYPE);
        if (!(taskType.equals(TYPE_MANUAL) || taskType.equals(TYPE_AUTO))) {
            throw new AssertException("Invalid task type: " + taskType);
        }

        // get sampling type
        final Boolean bSampling = (Boolean) config.get(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT);
        samplingWithReplacement = (bSampling == null) ? true : bSampling.booleanValue();

        // get task introductory text
        taskText = (String) config.get(TACourseNode.CONF_TASK_TEXT);

        hasPreview = config.getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW) == null ? false : config.getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW);

        isDeselectable = config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT) == null ? false : config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);

    }

    private boolean isDeselectable() {
        return isDeselectable;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    /**
     * deliver the selected file and show in a popup tableController*
     * 
     * @param ureq
     * @param command
     */
    private boolean doFileDelivery(final UserRequest ureq, final String taskFileName) {
        final VFSItem item = taskEbl.getTaskFile(taskFileName, courseEnv, node);
        if (item instanceof VFSLeaf) {
            final VFSLeaf leaf = (VFSLeaf) item;
            ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(leaf));
            return true;
        } else if (item == null) {
            log.warn("Can not cast to VFSLeaf. item==null, taskFile=" + taskFileName);
            return false;
        } else {
            log.warn("Can not cast to VFSLeaf. item.class.name=" + item.getClass().getName() + ", taskFile=" + taskFileName);
            return false;
        }
    }

    /**
     * Description:<br>
     * Model holding available tasks. Contains 4 cols: task title, view, select, and deselect.
     * <P>
     * Initial Date: 20.04.2010 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    class DeselectableTaskTableModel extends DefaultTableDataModel {
        private final int COLUMN_COUNT;

        public DeselectableTaskTableModel(final List objects, final int num_cols) {
            super(objects);
            COLUMN_COUNT = num_cols;
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            final String taskTitle = (String) objects.get(row);
            if (col == 0) {
                return taskTitle;
            } else if (col == 1) {
                return "View";
            } else if (col == 2) {
                return new Boolean(!hasAnyTaskAssigned());
            } else if (col == 3) {
                return new Boolean(isTaskAssigned(taskTitle));
            }

            return "ERROR";
        }

        private boolean hasAnyTaskAssigned() {
            return assignedTask != null;
        }

        private boolean isTaskAssigned(final String task) {
            return task.equals(assignedTask);
        }
    }

}
