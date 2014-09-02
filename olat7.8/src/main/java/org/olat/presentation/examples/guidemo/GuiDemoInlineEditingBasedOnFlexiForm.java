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

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Description:<br>
 * TODO: patrickb Class Description for GuiDemoInlineEditingBasedOnFlexiForm
 * <P>
 * Initial Date: 26.09.2009 <br>
 * 
 * @author patrickb
 */
public class GuiDemoInlineEditingBasedOnFlexiForm extends FormBasicController {

    private final TextElement[] elements = new TextElement[5];
    private final IntegerElement[] intelems = new IntegerElement[5];
    private TextElement inlineLabel;

    public GuiDemoInlineEditingBasedOnFlexiForm(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        initForm(this.flc, this, ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        String msg = "";
        for (int i = 0; i < elements.length; i++) {
            msg += elements[i].getValue() + " | ";
        }
        msg += "CustomLabel is named: " + inlineLabel.getValue();
        showInfo("placeholder", msg);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("inline.editing.flexiform");
        setFormDescription("inline.editing.flexiform.rem");
        final FormUIFactory formUIf = FormUIFactory.getInstance();
        int i = 0;
        for (; i < elements.length; i++) {
            elements[i] = formUIf.addInlineTextElement("inline.label.text" + i, "some", formLayout, this);
            elements[i].setLabel("inline.label.text", null);
            elements[i].setNotLongerThanCheck(5, "text.element.error.notlongerthan");
            if (i % 2 == 0) {
                elements[i].setEnabled(false);
            }
        }
        int ii = 0;
        for (; ii < intelems.length; ii++) {
            intelems[ii] = formUIf.addInlineIntegerElement("inline.label.int" + i + ii, i + ii, formLayout, this);
            intelems[ii].setLabel("inline.label.integer", null);
            if (ii % 2 == 0) {
                intelems[ii].setEnabled(false);
            }
        }

        // test for inline editable label field
        // the inlineLable is used as Label for the addStaticTextElement
        // Avoid translation error by setting i18nLabel key null first and then set the LabelComponent, and also you need to call showLabel(true)
        inlineLabel = formUIf.addInlineTextElement("inline.label.int" + i + ii + 1, "mytext" + i + ii + 1, formLayout, null);
        final StaticTextElement theElement = formUIf.addStaticTextElement("inline.label.text" + i + ii, null, "my bony", formLayout);
        theElement.setLabelComponent(inlineLabel, formLayout).showLabel(true);

    }

}
