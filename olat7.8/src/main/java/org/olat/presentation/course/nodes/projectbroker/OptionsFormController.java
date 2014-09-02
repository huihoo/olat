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

package org.olat.presentation.course.nodes.projectbroker;

import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * @author guretzki
 */

public class OptionsFormController extends FormBasicController {

    private final ProjectBrokerModuleConfiguration config;
    private IntegerElement nbrOfAttendees;
    private MultipleSelectionElement selectionAccept;
    private MultipleSelectionElement selectionAutoSignOut;
    private MultipleSelectionElement selectionLimitedAttendees;
    private final Long projectBrokerId;

    private final static String[] keys = new String[] { "form.modules.enabled.yes" };
    private final static String[] values = new String[] { "" };
    private static final int NBR_PARTICIPANTS_DEFAULT = 1;

    /**
     * Modules selection form.
     * 
     * @param name
     * @param config
     */
    public OptionsFormController(final UserRequest ureq, final WindowControl wControl, final ProjectBrokerModuleConfiguration config, final Long projectBrokerId) {
        super(ureq, wControl);
        this.config = config;
        this.projectBrokerId = projectBrokerId;
        initForm(this.flc, this, ureq);
    }

    /**
	 */
    public boolean validate() {
        return true;
    }

    /**
     * Initialize form.
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        // create form elements
        final int nbrOfParticipantsValue = config.getNbrParticipantsPerTopic();
        selectionLimitedAttendees = uifactory.addCheckboxesHorizontal("form.options.number.of.topics.per.participant", formLayout, keys, values, null);
        nbrOfAttendees = uifactory.addIntegerElement("form.options.number.of.participants.per.topic_nbr", nbrOfParticipantsValue, formLayout);
        nbrOfAttendees.setMinValueCheck(0, null);
        nbrOfAttendees.setDisplaySize(3);
        nbrOfAttendees.addActionListener(listener, FormEvent.ONCHANGE);
        if (nbrOfParticipantsValue == ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) {
            nbrOfAttendees.setVisible(false);
            selectionLimitedAttendees.select(keys[0], false);
        } else {
            selectionLimitedAttendees.select(keys[0], true);
        }
        selectionLimitedAttendees.addActionListener(listener, FormEvent.ONCLICK);

        final Boolean selectionAcceptValue = config.isAcceptSelectionManually();
        selectionAccept = uifactory.addCheckboxesVertical("form.options.selection.accept", formLayout, keys, values, null, 1);
        selectionAccept.select(keys[0], selectionAcceptValue);
        selectionAccept.addActionListener(this, FormEvent.ONCLICK);

        final Boolean autoSignOut = config.isAutoSignOut();
        selectionAutoSignOut = uifactory.addCheckboxesVertical("form.options.auto.sign.out", formLayout, keys, values, null, 1);
        selectionAutoSignOut.select(keys[0], autoSignOut);
        // enable auto-sign-out only when 'accept-selection' is enabled
        selectionAutoSignOut.setVisible(selectionAcceptValue);
        selectionAutoSignOut.addActionListener(this, FormEvent.ONCLICK);

        uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // enable auto-sign-out only when 'accept-selection' is enabled
        fireEvent(ureq, Event.DONE_EVENT);
        fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == selectionAccept) {
            selectionAutoSignOut.setVisible(selectionAccept.isSelected(0));
            if (!selectionAccept.isSelected(0) && ProjectBrokerManagerFactory.getProjectGroupManager().hasProjectBrokerAnyCandidates(projectBrokerId)) {
                this.showInfo("info.all.candidates.will.be.accepted.automatically");
            }
        } else if (source == selectionLimitedAttendees) {
            if (selectionLimitedAttendees.isSelected(0)) {
                nbrOfAttendees.setVisible(true);
                nbrOfAttendees.setIntValue(NBR_PARTICIPANTS_DEFAULT);
            } else {
                nbrOfAttendees.setVisible(false);
                nbrOfAttendees.setIntValue(ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED);
            }
        }
        this.flc.setDirty(true);
    }

    @Override
    protected void doDispose() {
        // nothing
    }

    public int getNnbrOfAttendees() {
        return nbrOfAttendees.getIntValue();
    }

    public boolean getSelectionAccept() {
        return selectionAccept.isSelected(0);
    }

    public boolean getSelectionAutoSignOut() {
        return getSelectionAccept() && selectionAutoSignOut.isSelected(0);
    }

}
