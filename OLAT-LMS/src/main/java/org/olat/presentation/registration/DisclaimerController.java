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

package org.olat.presentation.registration;

import java.util.Locale;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.registration.RegistrationDisclaimerEBL;
import org.olat.lms.registration.RegistrationModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 10.08.2004
 * 
 * @author Mike Stock Comment: Presents a disclaimer form with two text paragraphs and a checkbox. The text can be changed using the i18n tool.
 *         <p />
 *         The controller tries to read the following keys from the i18n files to add some optional features:
 *         <ul>
 *         <li>disclaimer.additionalcheckbox : if defined, a second checkbox is added with the text translated by this key</li>
 *         <li>disclaimer.filedownloadurl : a relative filename to a file that must be located in the olatdata/customizing/disclaimer/ directory. If defined and the file
 *         exists, a file download to this file is offered</li>
 *         </ul>
 */

public class DisclaimerController extends BasicController {

    private final String SR_ERROR_DISCLAIMER_CHECKBOX = "sr.error.disclaimer.checkbox";
    private final String SR_ERROR_DISCLAIMER_CHECKBOXES = "sr.error.disclaimer.checkboxes";

    private final VelocityContainer main;
    private final DisclaimerFormController disclaimerFormController;
    private Link downloadLink;
    private VFSLeaf downloadFile;

    /**
     * Display a disclaimer which can be accepted or denied.
     * 
     * @param ureq
     * @param wControl
     */
    public DisclaimerController(final UserRequest ureq, final WindowControl wControl) {
        this(ureq, wControl, false);
    }

    /**
     * Display a disclaimer which can be accepted or denied or in a read only manner
     * 
     * @param ureq
     * @param wControl
     * @param readOnly
     *            true: show only read only; false: allow user to accept
     */
    public DisclaimerController(final UserRequest ureq, final WindowControl wControl, final boolean readOnly) {
        super(ureq, wControl);

        disclaimerFormController = new DisclaimerFormController(ureq, wControl, readOnly);
        listenTo(disclaimerFormController);

        main = createVelocityContainer("disclaimer");
        main.put("dclform", this.disclaimerFormController.getInitialComponent());

        // add optinal download link, see class comments in DisclaimerFormController
        // Add the additional link to the form (depending on the configuration)
        if (RegistrationModule.isDisclaimerAdditionaLinkText()) {
            final VFSContainer disclaimerContainer = getRegistrationDisclaimerEBL().getDisclaimerVfsContainer();
            final String i18nIfiedFilename = translate("disclaimer.filedownloadurl");
            this.downloadFile = (VFSLeaf) disclaimerContainer.resolve(i18nIfiedFilename);
            if (this.downloadFile != null) {
                this.downloadLink = LinkFactory.createLink("disclaimer.additionallinktext", main, this);
                this.downloadLink.setTarget("_blank");

                if (i18nIfiedFilename.toLowerCase().endsWith(".pdf")) {
                    this.downloadLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_filetype_pdf");
                } else if (i18nIfiedFilename.toLowerCase().endsWith(".html") || i18nIfiedFilename.toLowerCase().endsWith(".htm")) {
                    this.downloadLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_filetype_html");
                } else if (i18nIfiedFilename.toLowerCase().endsWith(".doc")) {
                    this.downloadLink.setCustomEnabledLinkCSS("b_with_small_icon_left b_filetype_doc");
                }
            }
        }
        putInitialPanel(main);
    }

    private RegistrationDisclaimerEBL getRegistrationDisclaimerEBL() {
        return CoreSpringFactory.getBean(RegistrationDisclaimerEBL.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == this.downloadLink) {
            ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(this.downloadFile));
            // Prevent "do not press reload" message.
            this.downloadLink.setDirty(false);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == this.disclaimerFormController) {
            if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            } else if (event == Event.DONE_EVENT) {
                // Verify that, if the additional checkbox is configured to be visible, it is checked as well
                final boolean acceptCheckboxChecked = (disclaimerFormController.acceptCheckbox != null) ? (disclaimerFormController.acceptCheckbox.isSelected(0)) : false;
                // configure additional checkbox, see class comments in DisclaimerFormController
                final boolean additionalCheckboxConfigured = RegistrationModule.isDisclaimerAdditionalCheckbox();
                final boolean additionalCheckboxChecked = (disclaimerFormController.additionalCheckbox != null) ? (disclaimerFormController.additionalCheckbox
                        .isSelected(0)) : false;
                if (!additionalCheckboxConfigured) {
                    if (acceptCheckboxChecked) {
                        fireEvent(ureq, Event.DONE_EVENT);
                    } else {
                        showError(SR_ERROR_DISCLAIMER_CHECKBOX);
                    }
                } else {
                    if (acceptCheckboxChecked && additionalCheckboxChecked) {
                        fireEvent(ureq, Event.DONE_EVENT);
                    } else {
                        showError(SR_ERROR_DISCLAIMER_CHECKBOXES);
                    }
                }
            }
        }
    }

    /**
     * Change the locale of this controller.
     * 
     * @param locale
     */
    public void changeLocale(final Locale locale) {
        getTranslator().setLocale(locale);
        main.put("dclform", this.disclaimerFormController.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
