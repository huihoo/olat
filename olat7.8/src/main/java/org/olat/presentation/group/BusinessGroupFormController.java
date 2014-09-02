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
 * Copyright (c) 2009 frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.group;

import java.util.HashSet;
import java.util.Set;

import org.olat.data.group.BusinessGroup;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Implements a Business group creation dialog using FlexiForms.
 * 
 * @author twuersch
 * @author oliver.buehler@agility-informatik.ch refactored
 */
public class BusinessGroupFormController extends FormBasicController {

    /**
     * Text entry field for the name of this business group.
     */
    private TextElement businessGroupName;

    /**
     * Text entry field for the description for this business group.
     */
    private RichTextElement businessGroupDescription;

    /**
     * Text entry field for the minimum number of members for this business group.<br>
     * This field is currently not shown.
     */
    private TextElement businessGroupMinimumMembers;

    /**
     * Text entry field for the maximum number of members for this business group.
     */
    private TextElement businessGroupMaximumMembers;

    /**
     * Check box for enabling/disabling waiting list
     */
    private MultipleSelectionElement enableWaitingList;

    /**
     * Check box for automatic moving up from waiting to participants list
     */
    private MultipleSelectionElement enableAutoCloseRanks;

    /**
     * The {@link BusinessGroup} object this form refers to.
     */
    private final BusinessGroup businessGroup;

    /**
     * Decides whether minimum and maximum number of group members can be applied.
     */
    private final boolean minMaxEnabled;

    /**
     * Enables bulk processing of a comma separated list of group names. <br>
     * A new business group is created foreach group name.
     */
    private final boolean bulkMode;

    private HashSet<String> validNames;

    /** The key for the waiting list checkbox. */
    String[] waitingListKeys = new String[] { "create.form.enableWaitinglist" };

    /** The value for the waiting list checkbox. */
    String[] waitingListValues = new String[] { translate("create.form.enableWaitinglist") };

    /** The key for the autoCloseRanks checkbox. */
    String[] autoCloseKeys = new String[] { "create.form.enableAutoCloseRanks" };

    /** The value for the autoCloseRanks checkbox. */
    String[] autoCloseValues = new String[] { translate("create.form.enableAutoCloseRanks") };

    private BusinessGroupService businessGroupService;

    /**
     * @param ureq
     * @param wControl
     * @param businessGroup
     *            The group object which will be modified by this dialog.
     * @param minMaxEnabled
     *            Decides whether to limit the number of people that can enrol to a group or not
     */
    public BusinessGroupFormController(final UserRequest ureq, final WindowControl wControl, final BusinessGroup businessGroup, final boolean minMaxEnabled) {
        this(ureq, wControl, businessGroup, minMaxEnabled, false);
    }

    /**
     * @param ureq
     * @param wControl
     * @param businessGroup
     *            The group object which will be modified by this dialog.
     * @param minMaxEnabled
     *            Decides whether to limit the number of people that can enrol to a group or not
     * @param bulkMode
     *            when passing group names as CSV you have to set this to true and all groups will be created at once
     */
    public BusinessGroupFormController(final UserRequest ureq, final WindowControl wControl, final BusinessGroup businessGroup, final boolean minMaxEnabled,
            final boolean bulkMode) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
        this.businessGroup = businessGroup;
        this.minMaxEnabled = minMaxEnabled;
        this.bulkMode = bulkMode;
        if (!bulkMode) {
            this.businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        }
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // Create the business group name input text element
        if (bulkMode) {
            businessGroupName = uifactory.addTextElement("create.form.title.bgnames", "create.form.title.bgnames", 10 * BusinessGroup.MAX_GROUP_NAME_LENGTH, "",
                    formLayout);
            businessGroupName.setExampleKey("create.form.message.example.group", null);
        } else {
            businessGroupName = uifactory.addTextElement("create.form.title.bgname", "create.form.title.bgname", BusinessGroup.MAX_GROUP_NAME_LENGTH, "", formLayout);
            businessGroupName.setNotLongerThanCheck(BusinessGroup.MAX_GROUP_NAME_LENGTH, "create.form.error.nameTooLong");
            businessGroupName.setRegexMatchCheck(BusinessGroup.VALID_GROUPNAME_REGEXP, "create.form.error.illegalName");
        }
        businessGroupName.setMandatory(true);

        // Create the business group description input rich text element
        businessGroupDescription = uifactory.addRichTextElementForStringDataMinimalistic("create.form.title.description", "create.form.title.description", "", 10, -1,
                false, formLayout, ureq.getUserSession(), getWindowControl());

        uifactory.addSpacerElement("myspacer", formLayout, true);

        // Minimum members input
        businessGroupMinimumMembers = uifactory.addTextElement("create.form.title.min", "create.form.title.min", 5, "", formLayout);
        businessGroupMinimumMembers.setDisplaySize(6);
        businessGroupMinimumMembers.setVisible(false); // currently the minimum feature is not enabled

        // Maximum members input
        businessGroupMaximumMembers = uifactory.addTextElement("create.form.title.max", "create.form.title.max", 5, "", formLayout);
        businessGroupMaximumMembers.setDisplaySize(6);

        // Checkboxes
        enableWaitingList = uifactory.addCheckboxesHorizontal("create.form.enableWaitinglist", null, formLayout, waitingListKeys, waitingListValues, null);
        enableAutoCloseRanks = uifactory.addCheckboxesHorizontal("create.form.enableAutoCloseRanks", null, formLayout, autoCloseKeys, autoCloseValues, null);

        // Enable only if specification of min and max members is possible
        if (minMaxEnabled) {
            businessGroupMinimumMembers.setVisible(false); // currently the minimum feature is not enabled
            businessGroupMaximumMembers.setVisible(true);
            enableWaitingList.setVisible(true);
            enableAutoCloseRanks.setVisible(true);
        } else {
            businessGroupMinimumMembers.setVisible(false);
            businessGroupMaximumMembers.setVisible(false);
            enableWaitingList.setVisible(false);
            enableAutoCloseRanks.setVisible(false);
        }

        if ((businessGroup != null) && (!bulkMode)) {
            businessGroupName.setValue(businessGroup.getName());
            businessGroupDescription.setValue(businessGroup.getDescription());
            final Integer minimumMembers = businessGroup.getMinParticipants();
            final Integer maximumMembers = businessGroup.getMaxParticipants();
            businessGroupMinimumMembers.setValue(minimumMembers == null ? "" : minimumMembers.toString());
            businessGroupMaximumMembers.setValue(maximumMembers == null ? "" : maximumMembers.toString());
            if (businessGroup.getWaitingListEnabled() != null) {
                enableWaitingList.select("create.form.enableWaitinglist", businessGroup.getWaitingListEnabled());
            }
            if (businessGroup.getAutoCloseRanksEnabled() != null) {
                enableAutoCloseRanks.select("create.form.enableAutoCloseRanks", businessGroup.getAutoCloseRanksEnabled());
            }
        }

        // Create submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("finish", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        // 1) Check valid group names
        if (!StringHelper.containsNonWhitespace(businessGroupName.getValue())) {
            businessGroupName.setErrorKey("form.legende.mandatory");
            return false;
        }

        if (bulkMode) {
            // check all names to be valid and check that at least one is entered
            // e.g. find "," | " , " | ",,," errors => no group entered
            final String selectionAsCsvStr = businessGroupName.getValue();
            final String[] activeSelection = selectionAsCsvStr != null ? selectionAsCsvStr.split(",") : new String[] {};
            validNames = new HashSet<String>();
            final Set<String> wrongNames = new HashSet<String>();
            boolean nameTooLong = false;
            for (int i = 0; i < activeSelection.length; i++) {
                final String currentName = activeSelection[i].trim();
                if (currentName.getBytes().length > BusinessGroup.MAX_GROUP_NAME_LENGTH) {
                    nameTooLong = true;
                } else if ((currentName).matches(BusinessGroup.VALID_GROUPNAME_REGEXP)) {
                    validNames.add(currentName);
                } else {
                    wrongNames.add(currentName);
                }
            }
            if (validNames.size() == 0 && wrongNames.size() == 0 && !nameTooLong) {
                // no valid name and no invalid names, this is no names
                businessGroupName.setErrorKey("create.form.error.illegalName");
                return false;
            } else if (nameTooLong) {
                businessGroupName.setErrorKey("create.form.error.nameTooLong", BusinessGroup.MAX_GROUP_NAME_LENGTH + "");
                return false;
            } else if (wrongNames.size() == 1) {
                // one invalid name
                businessGroupName.setErrorKey("create.form.error.illegalName");
                return false;
            } else if (wrongNames.size() > 1) {
                // two or more invalid names
                final String[] args = new String[] { StringHelper.formatAsCSVString(wrongNames) };
                businessGroupName.setErrorKey("create.form.error.illegalNames", args);
                return false;
            }
        } else {
            if (businessGroupName.hasError())
                return false; // auto-validations from form, return false, because of that clearError()-calls everywhere...
        }
        // all group name tests passed
        businessGroupName.clearError();

        // 2) Check valid description
        if (businessGroupDescription.getValue().length() > 4000) {
            businessGroupDescription.setErrorKey("input.toolong");
            return false;
        }
        businessGroupDescription.clearError();

        if (minMaxEnabled) {
            // 3) Check min/max validity
            final String minValue = businessGroupMinimumMembers.getValue().trim();
            if (!minValue.isEmpty()) {
                try {
                    Integer.parseInt(businessGroupMinimumMembers.getValue());
                } catch (NumberFormatException ex) {
                    businessGroupMaximumMembers.setErrorKey("create.form.error.numberOrNull");
                    return false;
                }
            }
            final String maxValue = businessGroupMaximumMembers.getValue().trim();
            if (!maxValue.isEmpty()) {
                try {
                    Integer.parseInt(businessGroupMaximumMembers.getValue());
                } catch (NumberFormatException ex) {
                    businessGroupMaximumMembers.setErrorKey("create.form.error.numberOrNull");
                    return false;
                }
            }
            businessGroupMaximumMembers.clearError();

            // 4) Check waiting list / auto close
            if (isWaitingListEnabled()) {
                if (maxValue.isEmpty()) {
                    enableAutoCloseRanks.setErrorKey("create.form.error.enableWaitinglist");
                    return false;
                }
            } else {
                if (hasWaitingGroup(businessGroup) && hasWaitingParticipants(businessGroup)) {
                    enableAutoCloseRanks.setErrorKey("form.error.disableNonEmptyWaitingList");
                    return false;
                }
            }

            if (isAutoCloseRanksEnabled() && !isWaitingListEnabled()) {
                enableAutoCloseRanks.setErrorKey("create.form.error.enableAutoCloseRanks");
                return false;
            }
            enableAutoCloseRanks.clearError();
        }

        // 7) check for name duplication
        if (checkIfDuplicateGroupName()) {
            businessGroupName.setErrorKey("error.group.name.exists");
            return false;
        }
        // group name duplication test passed
        businessGroupName.clearError();

        // all checks passed
        return true;
    }

    /**
     * @return
     */
    private boolean hasWaitingParticipants(final BusinessGroup businessGroup) {
        boolean hasWaitingParticipants = businessGroup.getWaitingListEnabled();
        final int waitingPartipiciantSize = getBusinessGroupEBL().countWaiting(businessGroup);
        hasWaitingParticipants &= waitingPartipiciantSize > 0;
        return hasWaitingParticipants;
    }

    /**
     * @return
     */
    private BusinessGroupEBL getBusinessGroupEBL() {
        return CoreSpringFactory.getBean(BusinessGroupEBL.class);
    }

    /**
     * @return
     */
    private boolean hasWaitingGroup(final BusinessGroup businessGroup) {
        return (businessGroup != null) && (businessGroup.getWaitingGroup() != null);
    }

    /**
     * @param name
     */
    public void setGroupName(final String name) {
        businessGroupName.setValue(name);
    }

    /**
     * @return
     */
    public String getGroupName() {
        return businessGroupName.getValue();
    }

    /**
     * @return
     */
    public Set<String> getGroupNames() {
        return validNames;
    }

    /**
     * Checks if this a learning group or right group, if the group name changes, and if the name is already used in this context.
     * 
     * @return
     */
    private boolean checkIfDuplicateGroupName() {
        final Set<String> names = new HashSet<String>();
        names.add(businessGroupName.getValue());
        // group name changes to an already used name, and is a learning group
        if (businessGroup != null && businessGroup.getGroupContext() != null && !businessGroup.getName().equals(businessGroupName.getValue())
                && businessGroupService.checkIfOneOrMoreNameExistsInContext(names, businessGroup.getGroupContext())) {
            return true;
        }
        return false;
    }

    /**
     * @param nonexistingnames
     */
    public void setGroupNameExistsError(final Set<String> nonexistingnames) {
        if (nonexistingnames == null || nonexistingnames.size() == 0) {
            businessGroupName.setErrorKey("error.group.name.exists");
        } else {
            final String[] args = new String[] { StringHelper.formatAsCSVString(nonexistingnames) };
            businessGroupName.setErrorKey("error.group.name.exists", args);
        }
    }

    /**
     * @return
     */
    public String getGroupDescription() {
        return businessGroupDescription.getValue();
    }

    /**
     * @return group min if set null otherwise
     */
    public Integer getGroupMin() {
        try {
            return Integer.valueOf(businessGroupMinimumMembers.getValue());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * @return group max if set null otherwise
     */
    public Integer getGroupMax() {
        try {
            return Integer.valueOf(businessGroupMaximumMembers.getValue());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * @return
     */
    public Boolean isAutoCloseRanksEnabled() {
        return new Boolean(enableAutoCloseRanks.getSelectedKeys().size() != 0);
    }

    /**
     * @param enableAutoCloseRanks
     */
    public void setEnableAutoCloseRanks(final Boolean enableAutoCloseRanks) {
        this.enableAutoCloseRanks.select("create.form.enableAutoCloseRanks", enableAutoCloseRanks);
    }

    /**
     * @return
     */
    public Boolean isWaitingListEnabled() {
        return new Boolean(enableWaitingList.getSelectedKeys().size() != 0);
    }

    /**
     * @param enableWaitingList
     */
    public void setEnableWaitingList(final Boolean enableWaitingList) {
        this.enableWaitingList.select("create.form.enableWaitinglist", enableWaitingList);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Nothing to dispose
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
	 */
    @Override
    protected void formNOK(final UserRequest ureq) {
        fireEvent(ureq, Event.FAILED_EVENT);
    }
}
