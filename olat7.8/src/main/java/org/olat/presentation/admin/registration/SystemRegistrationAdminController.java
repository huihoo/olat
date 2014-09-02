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
package org.olat.presentation.admin.registration;

import org.olat.lms.admin.registration.RegistrationModel;
import org.olat.lms.admin.registration.SystemRegistrationManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.rules.RulesFactory;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.mail.MailHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.util.StringUtils;

/**
 * Description:<br>
 * The system registration controller allows the administrator to enable or disable elements from the system registration process:
 * <ul>
 * <li>anonymous statistics about the server setup. enabled by default (opt-out)</li>
 * <li>register installation on olat.org (opt-in)</li>
 * <li>register email address (opt-in)</li>
 * </ul>
 * <p>
 * The configuration is stored in <code>olatdata/system/config/org.olat.presentation.admin.registration.SystemRegistrationAdminController.properties</code>
 * <P>
 * Initial Date: 11.12.2008 <br>
 * 
 * @author gnaegi
 */
public class SystemRegistrationAdminController extends FormBasicController {
    private static final String YES = "yes";
    // personal information
    private MultipleSelectionElement addToAnnounceListSelection;
    private TextElement email;
    private MultipleSelectionElement publishWebSiteSelection;

    // summary of data that will be sent to server
    private TextElement summary, webSiteDescription;
    // where is your instance running, e.g. "Winterthurerstrasse 190, Zürich" or "Dresden"
    private TextElement locationBox;

    /**
     * Constructor for the system registration controller.
     * 
     * @param ureq
     * @param control
     */
    public SystemRegistrationAdminController(final UserRequest ureq, final WindowControl control) {
        super(ureq, control, "registration");
        initForm(ureq);
    }

    protected SystemRegistrationManager getSystemRegistrationManager() {
        return CoreSpringFactory.getBean(SystemRegistrationManager.class);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // Add statistics
        // always send statistics
        this.flc.contextPut("isRegisteredStatistics", Boolean.valueOf(true));
        //
        RegistrationModel model = getSystemRegistrationManager().getRegistrationModel();

        // Add website
        publishWebSiteSelection = uifactory.addCheckboxesVertical("registration.publishWebSiteSelection", null, formLayout, new String[] { YES }, new String[] { "" },
                null, 1);
        publishWebSiteSelection.addActionListener(this, FormEvent.ONCLICK);
        publishWebSiteSelection.select(YES, model.publishWebsite());
        this.flc.contextPut("isRegisteredWeb", model.publishWebsite());

        // Add website description
        webSiteDescription = uifactory.addTextAreaElement("registration.webSiteDescription", 5, 60, model.getWebsiteDescription(), formLayout);
        webSiteDescription.addActionListener(this, FormEvent.ONCHANGE);
        this.flc.contextPut("webSiteURL", Settings.getServerContextPathURI());
        RulesFactory.createHideRule(publishWebSiteSelection, null, webSiteDescription, formLayout);
        RulesFactory.createShowRule(publishWebSiteSelection, YES, webSiteDescription, formLayout);
        // Add location input
        locationBox = uifactory.addTextElement("registration.location", "registration.location", -1, model.getLocation(), formLayout);
        locationBox.setExampleKey("registration.location.example", null);
        if (StringUtils.hasText(model.getLocationCoordinates())) {
            this.flc.contextPut("locationCoordinates", model.getLocationCoordinates());
        }
        //
        // Add announce list
        addToAnnounceListSelection = uifactory.addCheckboxesVertical("registration.addToAnnounceListSelection", null, formLayout, new String[] { YES },
                new String[] { "" }, null, 1);
        addToAnnounceListSelection.addActionListener(this, FormEvent.ONCLICK);
        addToAnnounceListSelection.select(YES, model.notifyAboutNewReleases());
        //
        // Add email field
        email = uifactory.addTextElement("registration.email", "registration.email", 60, model.getNotificationEmail(), formLayout);
        RulesFactory.createHideRule(addToAnnounceListSelection, null, email, formLayout);
        RulesFactory.createShowRule(addToAnnounceListSelection, YES, email, formLayout);
        //
        // Add summary field
        String summaryText = getSystemRegistrationManager().getRegistrationPropertiesMessage();
        summaryText = StringHelper.escapeHtml(summaryText).toString();
        summary = uifactory.addTextAreaElement("registration.summary", null, -1, 5, 60, true, summaryText, formLayout);
        summary.setEnabled(false);
        //
        // Add submit button
        uifactory.addFormSubmitButton("save", formLayout);
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        // Now collect valid data
        RegistrationModel model = getSystemRegistrationManager().getRegistrationModel();
        // always send statistics
        this.flc.contextPut("isRegisteredStatistics", Boolean.valueOf(true));
        final boolean publishWebsiteCofig = publishWebSiteSelection.isSelected(0);
        this.flc.contextPut("isRegisteredWeb", Boolean.valueOf(publishWebsiteCofig));
        model.setPublishWebsite(Boolean.valueOf(publishWebsiteCofig));

        model.setWebsiteDescription(webSiteDescription.getValue());

        if (model.isValidEmail()) {
            model.setNotifyAboutNewReleases(addToAnnounceListSelection.isSelected(0));
            model.setNotificationEmail(email.getValue());
        } else {
            model.setNotifyAboutNewReleases(false);
        }

        //

        model.setLocationCoordinates(locationBox.getValue());
        this.flc.contextPut("locationCoordinates", model.getLocationCoordinates());

        // Update summary view
        String summaryText = getSystemRegistrationManager().getRegistrationPropertiesMessage();
        summaryText = StringHelper.escapeHtml(summaryText).toString();
        summary.setValue(summaryText);
        // Submit to olat.org
        getSystemRegistrationManager().process();
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        // First check for valid email address

        RegistrationModel model = getSystemRegistrationManager().getRegistrationModel();

        if (source == email && addToAnnounceListSelection.isSelected(0)) {
            if (!model.isValidEmail()) {
                email.setErrorKey("registration.email.error", null);
            }
        }
        // Now collect temporary valid data
        final boolean publishWebsiteCofig = publishWebSiteSelection.isSelected(0);
        model.setPublishWebsite(publishWebsiteCofig);
        final String webSiteDesc = webSiteDescription.getValue();
        model.setWebsiteDescription(webSiteDesc);
        if (MailHelper.isValidEmailAddress(email.getValue()) && StringHelper.containsNonWhitespace(email.getValue())) {
            final boolean notifyConfig = addToAnnounceListSelection.isSelected(0);
            model.setNotifyAboutNewReleases(notifyConfig);
            model.setNotificationEmail(email.getValue());
        } else {
            model.setNotifyAboutNewReleases(false);
        }
        // Update summary view
        String summaryText = getSystemRegistrationManager().getRegistrationPropertiesMessage();
        summaryText = StringHelper.escapeHtml(summaryText).toString();
        summary.setValue(summaryText);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
