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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.presentation.framework.core.control.JSAndCSSAdder;
import org.olat.presentation.framework.core.render.ValidationResult;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for JSDateChooserComponent
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
class JSDateChooserComponent extends FormBaseComponentImpl {

    /**
     * datechooser-span so the component cant be found while dispatching. and therefore eventhandling won't work. by using the id from the text-component, it should work
     * as expected. See OLAT-4735.
     */
    @Override
    public long getDispatchID() {
        return (element.getTextElementComponent().getDispatchID());
    }

    private final static ComponentRenderer RENDERER = new JSDateChooserRenderer();
    private JSDateChooser element;

    public JSDateChooserComponent(JSDateChooser element) {
        super(element.getName());
        this.element = element;
    }

    /**
	 */
    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    public TextElementComponent getTextElementComponent() {
        return element.getTextElementComponent();
    }

    public String getDateChooserDateFormat() {
        return element.getDateChooserDateFormat();
    }

    public boolean isDateChooserTimeEnabled() {
        return element.isDateChooserTimeEnabled();
    }

    public Translator getElementTranslator() {
        return element.getTranslator();
    }

    public String getExampleDateString() {
        return element.getExampleDateString();
    }

    /**
	 */
    @Override
    public void validate(UserRequest ureq, ValidationResult vr) {
        super.validate(ureq, vr);
        JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
        // FIXME:FG:THEME: calendar.css files for themes
        jsa.addRequiredCSSFile(Form.class, "css/jscalendar.css", false);
        jsa.addRequiredJsFile(Form.class, "js/jscalendar/calendar.js");
        jsa.addRequiredJsFile(Form.class, "js/jscalendar/olatcalendartranslator.js");
        jsa.addRequiredJsFile(Form.class, "js/jscalendar/calendar-setup.js");
    }

}
