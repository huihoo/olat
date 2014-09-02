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

package org.olat.presentation.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SpacerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Provides a controller which lets the user edit their user profile and choose the fields which are made publicly visible.
 * 
 * @author twuersch
 */
public class ProfileFormController extends FormBasicController {

    private final HomePageConfig conf;

    private final List<UserPropertyHandler> userPropertyHandlers;

    private final Map<String, FormItem> formItems;

    private final Map<String, String> formContext;

    private RichTextElement textAboutMe;

    private final String usageIdentifier;

    private final Map<String, MultipleSelectionElement> publishCheckboxes;

    private final Identity identity;

    private final boolean isAdministrativeUser;

    private List<String> propertyNames;

    /**
     * Creates this controller.
     * 
     * @param ureq
     *            The user request.
     * @param wControl
     *            The window control.
     * @param conf
     *            The homepage configuration (decides which user profile fields are visible for everyone).
     * @param identity
     *            The identity of the user.
     * @param isAdministrativeUser
     *            true: user is editing another users profile as user manager; false: use is editing his own profile
     */
    public ProfileFormController(final UserRequest ureq, final WindowControl wControl, final HomePageConfig conf, final Identity identity,
            final boolean isAdministrativeUser) {
        super(ureq, wControl, "combinedform");
        setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
        this.publishCheckboxes = new HashMap<String, MultipleSelectionElement>();
        this.conf = conf;
        this.usageIdentifier = ProfileFormController.class.getCanonicalName();
        this.userPropertyHandlers = getUserService().getUserPropertyHandlersFor(this.usageIdentifier, isAdministrativeUser);
        this.identity = identity;
        this.formItems = new HashMap<String, FormItem>();
        this.formContext = new HashMap<String, String>();
        this.isAdministrativeUser = isAdministrativeUser;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose.

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
    protected void formNOK(final UserRequest ureq) {
        fireEvent(ureq, Event.FAILED_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /*
     * (non-Javadoc) org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormDescription("form.description");

        this.formContext.put("username", this.identity.getName());

        // Add the noneditable username field.
        final MultipleSelectionElement usernameCheckbox = uifactory.addCheckboxesHorizontal("checkbox_username", null, formLayout, new String[] { "checkbox_username" },
                new String[] { "" }, null);
        usernameCheckbox.select("checkbox_username", true);
        usernameCheckbox.setEnabled(false);
        final StaticTextElement usernameText = uifactory.addStaticTextElement("username", this.identity.getName(), formLayout);
        usernameText.setMandatory(true);
        this.formItems.put("username", usernameText);

        String currentGroup = null;
        final List<UserPropertyHandler> homepagePropertyHanders = getUserService().getUserPropertyHandlersFor(HomePageConfig.class.getCanonicalName(),
                isAdministrativeUser);
        // show a form element for each property handler
        for (final UserPropertyHandler userPropertyHandler : this.userPropertyHandlers) {
            if (userPropertyHandler == null) {
                continue;
            }

            // add spacer if necessary (i.e. when group name changes)
            final String group = userPropertyHandler.getGroup();
            if (!group.equals(currentGroup)) {
                if (currentGroup != null) {
                    final SpacerElement spacerElement = uifactory.addSpacerElement("spacer_" + group, formLayout, false);
                    this.formItems.put("spacer_" + group, spacerElement);
                }
                currentGroup = group;
            }

            if (homepagePropertyHanders.contains(userPropertyHandler)) {
                // add checkbox to container if configured for homepage usage identifier
                final String checkboxName = "checkbox_" + userPropertyHandler.getName();
                final MultipleSelectionElement publishCheckbox = uifactory.addCheckboxesHorizontal(checkboxName, null, formLayout,
                        new String[] { userPropertyHandler.i18nFormElementLabelKey() }, new String[] { "" }, null);
                this.publishCheckboxes.put(checkboxName, publishCheckbox);
                final boolean isEnabled = this.conf.isEnabled(userPropertyHandler.getName());
                if (isEnabled) {
                    publishCheckbox.select(userPropertyHandler.i18nFormElementLabelKey(), true);
                } else {
                    publishCheckbox.select(userPropertyHandler.i18nFormElementLabelKey(), false);
                }
                // Mandatory homepage properties can not be changed by user
                if (getUserService().isMandatoryUserProperty(HomePageConfig.class.getCanonicalName(), userPropertyHandler)) {
                    publishCheckbox.select(userPropertyHandler.i18nFormElementLabelKey(), true);
                    publishCheckbox.setEnabled(false);
                }
            } else {
                uifactory.addSpacerElement("spacer_" + userPropertyHandler.getName(), formLayout, true);
            }

            // add input field to container
            final FormItem formItem = userPropertyHandler.addFormItem(getLocale(), this.identity.getUser(), this.usageIdentifier, this.isAdministrativeUser, formLayout);
            final String propertyName = userPropertyHandler.getName();
            this.formItems.put(propertyName, formItem);

            if (formItem instanceof TextElement) {
                // it's a text field, so get the value of this property into the text field
                final TextElement textElement = (TextElement) formItem;
                textElement.setValue(getUserService().getUserProperty(this.identity.getUser(), propertyName, getLocale()));
            } else if (formItem instanceof MultipleSelectionElement) {
                // it's a checkbox, so set the box to checked if the corresponding property is set to "true"
                final MultipleSelectionElement checkbox = (MultipleSelectionElement) formItem;
                final String value = getUserService().getUserProperty(this.identity.getUser(), propertyName, getLocale());
                if (value != null) {
                    checkbox.select(propertyName, value.equals("true"));
                } else {
                    // assume "false" if the property is not present
                    checkbox.select(propertyName, false);
                }
            }

            // special case for email field
            if (userPropertyHandler.getName().equals("email")) {
                final String key = getUserService().getUserProperty(this.identity.getUser(), UserConstants.EMAILCHANGE);
                final TemporaryKeyImpl tempKey = getRegistrationService().loadTemporaryKeyByRegistrationKey(key);
                if (tempKey != null) {
                    final XStream xml = new XStream();
                    final HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(tempKey.getEmailAddress());
                    formItem.setExampleKey("email.change.form.info", new String[] { mails.get("changedEMail") });
                }
            }
        }

        // add the "about me" text field.
        this.textAboutMe = uifactory.addRichTextElementForStringData("form.text", "form.text", this.conf.getTextAboutMe(), 10, -1, false, false, null, null, formLayout,
                ureq.getUserSession(), getWindowControl());
        this.textAboutMe.setMaxLength(10000);
        this.formItems.put("form.text", this.textAboutMe);

        // Create submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("save", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
        formItems.put("buttonLayout", buttonLayout);

        final Set<String> userPropertyNames = formItems.keySet();
        ((VelocityContainer) this.flc.getComponent()).contextPut("userPropertyNames", userPropertyNames);
    }

    /**
     * Stores the data from the form into a) the user's home page configuration and b) the user's properties.
     * 
     * @param config
     *            The user's home page configuration (i.e. flags for publicly visible fields).
     * @param identity
     *            The user's identity
     */
    public void updateFromFormData(final HomePageConfig config, final Identity identity) {
        final User user = identity.getUser();
        // For each user property...
        for (final UserPropertyHandler userPropertyHandler : this.userPropertyHandlers) {

            // ...get the value from the form field and store it into the user
            // property...
            final FormItem formItem = this.formItems.get(userPropertyHandler.getName());

            userPropertyHandler.updateUserFromFormItem(user, formItem);

            // ...and store the "publish" flag for each property.
            final MultipleSelectionElement checkbox = this.publishCheckboxes.get("checkbox_" + userPropertyHandler.getName());
            if (checkbox != null) {
                // null when not enabled for the HomePageConfig usage identifier key
                if (checkbox.getSelectedKeys().size() == 0) {
                    config.setEnabled(userPropertyHandler.getName(), false);
                } else {
                    config.setEnabled(userPropertyHandler.getName(), true);
                }
            }
        }
        // Store the "about me" text.
        conf.setTextAboutMe(textAboutMe.getValue());
    }

    /**
     * Take form data and set it in the user fields for the current subject
     * 
     * @param id
     *            The identity to be updated (transient, does not do any db calls)
     * @return the updated identity object
     */
    public Identity updateIdentityFromFormData(final Identity id) {
        final User user = id.getUser();
        // update each user field
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            final FormItem formItem = this.formItems.get(userPropertyHandler.getName());

            userPropertyHandler.updateUserFromFormItem(user, formItem);
        }
        return id;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean formOK = true;
        for (final UserPropertyHandler userPropertyHandler : this.userPropertyHandlers) {

            final FormItem formItem = this.formItems.get(userPropertyHandler.getName());

            if (!userPropertyHandler.isValid(formItem, this.formContext)) {
                formOK = false;
            } else {
                formItem.clearError();
            }
        }

        try {
            final String aboutMe = textAboutMe.getValue();
            if (aboutMe.length() > 10000) {
                textAboutMe.setErrorKey("input.toolong", new String[] { "10000" });
                formOK = false;
            } else {
                textAboutMe.clearError();
            }
        } catch (final Exception e) {
            textAboutMe.setErrorKey("input.toolong", new String[] { "10000" });
            formOK = false;
        }
        return formOK && super.validateFormLogic(ureq);
    }

    /**
     * Sets the dirty mark for this form.
     * 
     * @param isDirtyMarking
     *            <code>true</code> sets this form dirty.
     */
    public void setDirtyMarking(final boolean isDirtyMarking) {
        mainForm.setDirtyMarking(isDirtyMarking);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    private RegistrationService getRegistrationService() {
        return CoreSpringFactory.getBean(RegistrationService.class);
    }

}
