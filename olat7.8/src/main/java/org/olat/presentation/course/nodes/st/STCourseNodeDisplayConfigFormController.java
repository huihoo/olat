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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.course.nodes.st;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.SpacerElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.rules.RulesFactory;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * <h3>Description:</h3> The STCourseNodeDisplayConfigFormController displays the layout configuration form for an ST node. It lets the user decide if he wants to display
 * a custom file layout, a system generated TOC layout or a system generated peekview layout. In both system generated layouts he can further define the number of columns
 * (1 or 2).
 * <p>
 * When the peek view configuration is used, the children that should be displayed in the peekview must be selected manually. For performance reasons only 10 direct
 * children can be selected by default. This behavior can be overridden by using the spring setter method setMaxPeekviewChildNodes() on the STCourseNodeConfiguration bean
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>Event.DONE_EVENT when the form is submitted</li>
 * </ul>
 * <p>
 * Initial Date: 15.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class STCourseNodeDisplayConfigFormController extends FormBasicController {
    private static final String[] keys_displayType = new String[] { "system", "peekview", "file" };

    // read current configuration
    private String displayConfig = null;
    private int columnsConfig = 1;
    private SingleSelection displayTypeRadios;
    private MultipleSelectionElement displayTwoColumns;
    private MultipleSelectionElement selectedPeekviewChildren;
    private final String[] selectedPeekviewChildKeys;
    private final String[] selectedPeekviewChildValues;
    private final String[] selectedPeekviewChildCssClasses;
    private String selectedPeekviewChildNodesConfig = null;

    /**
     * Constructor for the config form
     * 
     * @param ureq
     * @param wControl
     * @param config
     *            the module configuration
     * @param node
     *            the course editor node
     */
    STCourseNodeDisplayConfigFormController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final CourseEditorTreeNode node) {
        super(ureq, wControl);
        // Read current configuration
        this.displayConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
        this.columnsConfig = config.getIntegerSafe(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
        // Build data model for the selected child peekview checkboxes
        final int childCount = node.getChildCount();
        selectedPeekviewChildKeys = new String[childCount];
        selectedPeekviewChildValues = new String[childCount];
        selectedPeekviewChildCssClasses = new String[childCount];
        for (int i = 0; i < childCount; i++) {
            final CourseEditorTreeNode child = node.getCourseEditorTreeNodeChildAt(i);
            selectedPeekviewChildKeys[i] = child.getIdent();
            selectedPeekviewChildValues[i] = child.getTitle() + " (" + child.getIdent() + ")";
            selectedPeekviewChildCssClasses[i] = child.getIconCssClass() + " b_with_small_icon_left";
        }
        selectedPeekviewChildNodesConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, "");
        // initialize the form now
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // No explicit submit button. Form is submitted every time when a radio or
        // checkbox is clicked (OLAT-5610)
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("config.fieldset.view");
        setFormContextHelp("org.olat.presentation.course.nodes.st", "ced-st-overview.html", "help.st.design");
        //
        final FormUIFactory formFact = FormUIFactory.getInstance();
        // Display type
        final String[] values_displayType = new String[] { translate("form.system"), translate("form.peekview"), translate("form.self") };
        displayTypeRadios = formFact.addRadiosVertical("selforsystemoverview", formLayout, keys_displayType, values_displayType);
        displayTypeRadios.addActionListener(this, FormEvent.ONCLICK);
        if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
            displayTypeRadios.select("file", true);
        } else if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW)) {
            displayTypeRadios.select("peekview", true);
        } else {
            displayTypeRadios.select("system", true);
        }
        // Peekview details configuration - allow only MAX_PEEKVIEW_CHILD_NODES
        // peekviews to be selected
        if (selectedPeekviewChildKeys.length > 0) {
            final SpacerElement spacerChild = formFact.addSpacerElement("spacerChild", formLayout, true);
            selectedPeekviewChildren = formFact.addCheckboxesVertical("selectedPeekviewChildren", formLayout, selectedPeekviewChildKeys, selectedPeekviewChildValues,
                    selectedPeekviewChildCssClasses, 1);
            selectedPeekviewChildren.setLabel("selectedPeekviewChildren", new String[] { STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES + "" });
            // visibility rules for peekview children selection
            RulesFactory.createHideRule(displayTypeRadios, "file", selectedPeekviewChildren, formLayout);
            RulesFactory.createHideRule(displayTypeRadios, "system", selectedPeekviewChildren, formLayout);
            RulesFactory.createShowRule(displayTypeRadios, "peekview", selectedPeekviewChildren, formLayout);
            RulesFactory.createHideRule(displayTypeRadios, "file", spacerChild, formLayout);
            RulesFactory.createHideRule(displayTypeRadios, "system", spacerChild, formLayout);
            RulesFactory.createShowRule(displayTypeRadios, "peekview", spacerChild, formLayout);
            // Pre-select the first MAX_PEEKVIEW_CHILD_NODES child nodes if none is
            // selected to reflect meaningfull default configuration
            preselectConfiguredOrMaxChildNodes();
            // Add as listener for any changes
            selectedPeekviewChildren.addActionListener(this, FormEvent.ONCLICK);
        }
        //
        // Number of rows (only available in system or peekview type)
        final SpacerElement spacerCols = formFact.addSpacerElement("spacerCols", formLayout, true);
        displayTwoColumns = formFact.addCheckboxesVertical("displayTwoColumns", formLayout, new String[] { "on" }, new String[] { "" }, null, 1);
        displayTwoColumns.setLabel("displayTwoColumns", null);
        displayTwoColumns.addActionListener(this, FormEvent.ONCLICK);
        if (columnsConfig == 2) {
            displayTwoColumns.selectAll();
        }
        if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
            displayTwoColumns.setVisible(false);
        }
        //
        // Visibility rules for display columns switch
        RulesFactory.createHideRule(displayTypeRadios, "file", displayTwoColumns, formLayout);
        RulesFactory.createShowRule(displayTypeRadios, "peekview", displayTwoColumns, formLayout);
        RulesFactory.createShowRule(displayTypeRadios, "system", displayTwoColumns, formLayout);
        RulesFactory.createHideRule(displayTypeRadios, "file", spacerCols, formLayout);
        RulesFactory.createShowRule(displayTypeRadios, "peekview", spacerCols, formLayout);
        RulesFactory.createShowRule(displayTypeRadios, "system", spacerCols, formLayout);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == selectedPeekviewChildren) {
            if (selectedPeekviewChildren.getSelectedKeys().size() == 0) {
                // There must be at least one selected child
                selectedPeekviewChildren.setErrorKey("form.peekview.error.mandatory.child");
                return; // abort
            }
            // Clean potential previous error and continue with rules to
            // enable/disable the checkboxes to ensure that users can't select more
            // than the allowed number of child nodes
            selectedPeekviewChildren.clearError();
            if (selectedPeekviewChildren.getSelectedKeys().size() >= STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
                // Max reached, disabled all not already enabled checkboxes.
                for (int i = 0; i < selectedPeekviewChildKeys.length; i++) {
                    if (!selectedPeekviewChildren.isSelected(i)) {
                        selectedPeekviewChildren.setEnabled(selectedPeekviewChildKeys[i], false);
                    }
                }
                showInfo("form.peekview.max.reached", STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES + "");
            } else {
                // Enable all checkboxes
                selectedPeekviewChildren.setEnabled(true);
            }
        } else {
            // Fix problem of not-preselected items (OLAT-5610). The initial status
            // from the initForm method gets lost by the re-evaluation of the
            // selection element. Seems to be a flexi form bug, could not find other
            // solution as this workaround.
            preselectConfiguredOrMaxChildNodes();
        }
        // Submit form on each click on any radio or checkbox (OLAT-5610)
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
     * Helper to select the configured child nodes or the maximum of child nodes when no child is selected at all
     */
    private void preselectConfiguredOrMaxChildNodes() {
        // Pre-select configured keys. Discard configured selections that are not
        // found (e.g. deleted or moved nodes)
        //
        // SelectedPeekviewChildren can be NULL in case this structure element has
        // no child elements at all, e.g. during development of the course.
        if (selectedPeekviewChildren != null) {
            final String[] preSelectedChildNodes = (selectedPeekviewChildNodesConfig == null ? new String[0] : selectedPeekviewChildNodesConfig.split(","));
            for (final String preSelectedNode : preSelectedChildNodes) {
                for (final String selectableNode : selectedPeekviewChildKeys) {
                    if (preSelectedNode.equals(selectableNode)) {
                        selectedPeekviewChildren.select(selectableNode, true);
                        break;
                    }
                }
            }
            // Allow only MAX_PEEKVIEW_CHILD_NODES child nodes to be enabled
            if (selectedPeekviewChildren.getSelectedKeys().size() >= STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
                for (int i = 0; i < selectedPeekviewChildKeys.length; i++) {
                    if (!selectedPeekviewChildren.isSelected(i)) {
                        selectedPeekviewChildren.setEnabled(selectedPeekviewChildKeys[i], false);
                    }
                }
            }
            // Pre-select the first MAX_PEEKVIEW_CHILD_NODES child nodes if none is
            // selected to reflect meaningfull default configuration.
            //
            if (selectedPeekviewChildren.getSelectedKeys().size() == 0) {
                for (int i = 0; i < selectedPeekviewChildKeys.length; i++) {
                    if (i < STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
                        selectedPeekviewChildren.select(selectedPeekviewChildKeys[i], true);
                    } else {
                        selectedPeekviewChildren.setEnabled(selectedPeekviewChildKeys[i], false);
                    }
                }
            }
            // remove errors from previous invalid form selection
            selectedPeekviewChildren.clearError();
        }
    }

    /**
     * Update the given module config object from the data in the form
     * 
     * @param moduleConfig
     */
    public void updateModuleConfiguration(final ModuleConfiguration moduleConfig) {
        final String displayType = displayTypeRadios.getSelectedKey();
        if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(displayType)) {
            // manual file view selected, remove columns config
            moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE);
            // Let other config values from old config setup remain in config, maybe
            // used when user switches back to other config (OLAT-5610)
        } else {
            // auto generated view selected, set TOC or peekview
            if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(displayType)) {
                moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);
                // update selected peekview children
                if (selectedPeekviewChildren == null || selectedPeekviewChildren.getSelectedKeys().size() == 0) {
                    moduleConfig.remove(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES);
                } else {
                    selectedPeekviewChildNodesConfig = "";
                    for (final String childKey : selectedPeekviewChildren.getSelectedKeys()) {
                        if (selectedPeekviewChildNodesConfig.length() != 0) {
                            // separate node id's with commas
                            selectedPeekviewChildNodesConfig += ",";
                        }
                        selectedPeekviewChildNodesConfig += childKey;
                    }
                    moduleConfig.set(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, selectedPeekviewChildNodesConfig);
                }
            } else {
                // the old auto generated TOC view without peekview
                moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
                // Let other config values from old config setup remain in config, maybe
                // used when user switches back to other config (OLAT-5610)
            }
            // in both cases, also set the columns configuration
            final int cols = (displayTwoColumns.isSelected(0) ? 2 : 1);
            moduleConfig.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, Integer.valueOf(cols));
            // Let other config values from old config setup remain in config, maybe
            // used when user switches back to other config (OLAT-5610)
        }
    }

}
