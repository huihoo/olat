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

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.lms.commons.i18n.I18nModule;
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
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This form allows the user to delete language packs that are found in the olatdata/customizing/lang/packs directory <h3>Events fired by this controller</h3>
 * <ul>
 * <li>Event.CANCELLED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 09.12.2008 <br>
 * 
 * @author gnaegi
 */
class I18nConfigSubDeletePackageController extends FormBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private MultipleSelectionElement deleteLangPackSelection;
    private DialogBoxController dialogCtr;
    private FormLink cancelButton;
    private FormSubmit submitButton;

    /**
     * Constructor for the delete-language pack workflow
     * 
     * @param ureq
     * @param control
     */
    public I18nConfigSubDeletePackageController(UserRequest ureq, WindowControl control) {
        super(ureq, control, LAYOUT_VERTICAL);
        initForm(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(FormItemContainer formLayout, @SuppressWarnings("unused") Controller listener, @SuppressWarnings("unused") UserRequest ureq) {
        // A title, displayed in fieldset
        setFormTitle("configuration.management.package.delete.title");
        setFormDescription("configuration.management.package.delete.description");
        // Add cancel and submit in button group layout
        FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);
        cancelButton = uifactory.addFormLink("cancel", buttonGroupLayout, Link.BUTTON);
        submitButton = uifactory.addFormSubmitButton("configuration.management.package.delete", buttonGroupLayout);
        submitButton.setEnabled(false); // enable as soon as something is checked
    }

    /**
	 */
    @Override
    protected void formOK(UserRequest ureq) {
        Set<String> toDelete = deleteLangPackSelection.getSelectedKeys();
        if (toDelete.size() == 0) {
            // should not happen since button disabled
            return;
        }
        String text = translate("configuration.management.package.delete.confirm", StringHelper.escapeHtml(toDelete.toString()));
        dialogCtr = activateYesNoDialog(ureq, translate("configuration.management.package.delete.confirm.title"), text, dialogCtr);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == dialogCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // Yes case, delete now
                for (String deleteLangPack : deleteLangPackSelection.getSelectedKeys()) {
                    File file = new File(I18nModule.LANG_PACKS_DIRECTORY, deleteLangPack);
                    if (file.exists())
                        file.delete();
                    log.info("Audit:Deleted language pack::" + deleteLangPack, null);
                }
                // Reset i18n system
                I18nModule.reInitializeAndFlushCache();
                // wow, everything worked fine
                showInfo("configuration.management.package.delete.success", deleteLangPackSelection.getSelectedKeys().toString());
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
            showInfo("configuration.management.package.delete.cancel");

        } else if (source == deleteLangPackSelection) {
            if (deleteLangPackSelection.getSelectedKeys().size() == 0) {
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
