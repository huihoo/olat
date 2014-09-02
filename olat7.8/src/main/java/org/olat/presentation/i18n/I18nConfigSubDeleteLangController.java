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
package org.olat.presentation.i18n;

import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This form allows the user to delete languages available on the translation tool source path. The form implements the user interface and calls the necessary manager
 * code to delete the languages. <h3>Events fired by this controller</h3>
 * <ul>
 * <li>Event.CANCELLED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 27.11.2008 <br>
 * 
 * @author gnaegi
 */
class I18nConfigSubDeleteLangController extends FormBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private MultipleSelectionElement deleteLangSelection;
    private DialogBoxController dialogCtr;
    private FormLink cancelButton;
    private FormSubmit submitButton;

    /**
     * Constructor for the delete-language workflow
     * 
     * @param ureq
     * @param control
     */
    public I18nConfigSubDeleteLangController(UserRequest ureq, WindowControl control) {
        super(ureq, control, LAYOUT_VERTICAL);
        if (!I18nModule.isTransToolEnabled()) {
            throw new AssertException(
                    "Languages can only be deleted when the translation tool is enabled and the translation tool source pathes are configured in the olat.properties");
        }
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(FormItemContainer formLayout, @SuppressWarnings("unused") Controller listener, @SuppressWarnings("unused") UserRequest ureq) {
        I18nManager i18nMgr = I18nManager.getInstance();
        // A title, displayed in fieldset
        setFormTitle("configuration.management.delete.title");
        setFormDescription("configuration.management.delete.description");
        //
        // Add languages checkboxes
        Set<String> deletableKeysUnsorted = I18nModule.getTranslatableLanguageKeys();
        String[] deletableKeys = ArrayHelper.toArray(deletableKeysUnsorted);
        String[] availableValues = new String[deletableKeys.length];
        for (int i = 0; i < deletableKeys.length; i++) {
            String key = deletableKeys[i];
            String explLang = i18nMgr.getLanguageInEnglish(key, false);
            String all = explLang;
            if (explLang != null && !explLang.equals(key))
                all += " (" + key + ")";
            availableValues[i] = all;
        }
        ArrayHelper.sort(deletableKeys, availableValues, false, true, false);
        String[] availableLangCssClasses = i18nMgr.createLanguageFlagsCssClasses(deletableKeys, "b_with_small_icon_left");
        deleteLangSelection = uifactory.addCheckboxesVertical("configuration.deleteLangSelection", null, formLayout, deletableKeys, availableValues,
                availableLangCssClasses, 2);
        deleteLangSelection.addActionListener(this, FormEvent.ONCLICK);
        // Add cancel and submit in button group layout
        FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);
        cancelButton = uifactory.addFormLink("cancel", buttonGroupLayout, Link.BUTTON);
        submitButton = uifactory.addFormSubmitButton("configuration.management.delete", buttonGroupLayout);
        submitButton.setEnabled(false); // enable as soon as something is checked
    }

    /**
	 */
    @Override
    protected void formOK(UserRequest ureq) {
        Set<String> toDelete = deleteLangSelection.getSelectedKeys();
        if (toDelete.size() == 0) {
            // should not happen since button disabled
            return;
        }
        String defaultKey = I18nModule.getDefaultLocale().toString();
        if (toDelete.contains(defaultKey)) {
            deleteLangSelection.select(defaultKey, false);
            showError("configuration.management.delete.error", defaultKey);
            return;
        }
        String fallbackKey = I18nModule.getFallbackLocale().toString();
        if (toDelete.contains(fallbackKey)) {
            deleteLangSelection.select(fallbackKey, false);
            showError("configuration.management.delete.error", I18nModule.getFallbackLocale().toString());
            return;
        }
        String text = translate("configuration.management.delete.confirm", StringHelper.escapeHtml(toDelete.toString()));
        dialogCtr = activateYesNoDialog(ureq, translate("configuration.management.delete.confirm.title"), text, dialogCtr);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == dialogCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // Yes case, delete now
                for (String deleteLang : deleteLangSelection.getSelectedKeys()) {
                    I18nManager.getInstance().deleteLanguage(deleteLang, true);
                    log.info("Audit:Deleted language::" + deleteLang, null);
                }
                // wow, everything worked fine
                showInfo("configuration.management.delete.success", deleteLangSelection.getSelectedKeys().toString());
                fireEvent(ureq, Event.DONE_EVENT);
            } else {
                // No case, do nothing.
            }
        }
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == cancelButton) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
            showInfo("configuration.management.delete.cancel");

        } else if (source == deleteLangSelection) {
            if (deleteLangSelection.getSelectedKeys().size() == 0) {
                submitButton.setEnabled(false);
            } else {
                submitButton.setEnabled(true);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
