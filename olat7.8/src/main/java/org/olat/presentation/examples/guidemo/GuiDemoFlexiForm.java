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
package org.olat.presentation.examples.guidemo;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.Submit;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.WebappHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for GuiDemoFlexiForm
 * <P>
 * Initial Date: 06.09.2007 <br>
 * 
 * @author patrickb
 */
public class GuiDemoFlexiForm extends FormBasicController {

    private TextElement firstName;
    private TextElement lastName;
    private TextElement institution;
    private FileElement fileElement;
    private Submit submit;
    private GuiDemoFlexiFormPersonData personData;
    private VelocityContainer confirm;
    private GuiDemoFlexiForm confirmController;
    private File tmpFile;

    public GuiDemoFlexiForm(final UserRequest ureq, final WindowControl wControl, final GuiDemoFlexiFormPersonData data) {
        super(ureq, wControl);
        // first you may preprocess data to fit into the form items
        // if all preprocessing is done, create the form items
        //
        // example for simple preprocessing - check for NULL
        if (data != null) {
            personData = data;
        } else {
            personData = new GuiDemoFlexiFormPersonData();
        }
        //
        // calls our initForm(formlayout,listener,ureq) with default values.
        initForm(ureq);
        //
        // after initialisation you may need to do some stuff
        // but typically initForm(..) is the last call in the constructor.
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void doDispose() {
        // cleanup tempt files
        if (tmpFile != null && tmpFile.exists()) {
            tmpFile.delete();
        }

    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void formOK(final UserRequest ureq) {
        // this method is called if the form has validated
        // which means that all form items are filled without error
        // and all complex business rules validated also to true.
        //
        // typically the form values are now read out and persisted
        //
        // in our case, save value to data object and prepare a confirm page
        personData.setFirstName(firstName.getValue());
        personData.setLastName(lastName.getValue());
        personData.setInstitution(institution.getValue());
        personData.setReadOnly(true);

        // get file and store it in temporary location
        tmpFile = new File(WebappHelper.getUserDataRoot() + "/tmp/" + fileElement.getUploadFileName());
        fileElement.moveUploadFileTo(tmpFile);
        personData.setFile(tmpFile);

        // show the same form in readonly mode.
        confirmController = new GuiDemoFlexiForm(ureq, getWindowControl(), personData);
        listenTo(confirmController); // guarantees autodispose later
        confirm = createVelocityContainer("confirm");
        confirm.put("data", confirmController.getInitialComponent());

        initialPanel.pushContent(confirm);

    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        /*
         * create a form with a title and 4 input fields to enter some persons data
         */
        setFormTitle("guidemo_flexi_form_simpleform");
        final int defaultDisplaySize = 32;
        final boolean inputMode = !personData.isReadOnly();

        firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, personData.getFirstName(), formLayout);
        firstName.setDisplaySize(defaultDisplaySize);
        firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
        firstName.setMandatory(true);
        firstName.setEnabled(inputMode);

        lastName = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, personData.getLastName(), formLayout);
        lastName.setDisplaySize(defaultDisplaySize);
        lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
        lastName.setMandatory(true);
        lastName.setEnabled(inputMode);

        fileElement = uifactory.addFileElement("file", formLayout);
        fileElement.setLabel("guidemo.flexi.form.file", null);
        fileElement.setMaxUploadSizeKB(500, "guidemo.flexi.form.filetobig", null);
        final Set<String> mimeTypes = new HashSet<String>();
        mimeTypes.add("image/*");
        fileElement.limitToMimeType(mimeTypes, "guidemo.flexi.form.wrongfiletype", null);
        fileElement.setMandatory(true, "guidemo.flexi.form.mustbefilled");
        fileElement.setEnabled(inputMode);

        institution = uifactory.addTextElement("institution", "guidemo.flexi.form.institution", 256, personData.getInstitution(), formLayout);
        institution.setDisplaySize(defaultDisplaySize);
        institution.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
        institution.setMandatory(true);
        institution.setEnabled(inputMode);

        if (inputMode) {
            // submit only if in input mode
            submit = new FormSubmit("submit", "submit");
            formLayout.add(submit);
        }
    }

}
