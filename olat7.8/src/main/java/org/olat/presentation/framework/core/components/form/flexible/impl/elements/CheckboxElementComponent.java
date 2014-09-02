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

import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for CheckboxElementComponent
 * <P>
 * Initial Date: 04.01.2007 <br>
 * 
 * @author patrickb
 */
class CheckboxElementComponent extends FormBaseComponentImpl {

    private final SelectionElement selectionWrapper;
    private final int which;
    private final String cssClass;
    private static final ComponentRenderer RENDERER = new CheckboxRenderer();
    private boolean isTrustedText = false; // per default is NOT

    /**
     * Constructor for a check box component. Set to private, use the MultipleSelectionElementImpl or even better the FormUIFactory.addCheckboxesVertical() or
     * FormUIFactory.addCheckboxesHorizontal() methods instead
     * 
     * @param name
     * @param translator
     * @param selectionWrapper
     *            The seection wrapper element
     * @param which
     *            The position of the checkbox within the selection wrapper
     * @param cssClass
     *            Optional css class to be added to the checkbox in a span element. Can be NULL
     */
    CheckboxElementComponent(String name, Translator translator, SelectionElement selectionWrapper, int which, String cssClass) {
        super(name, translator);
        this.selectionWrapper = selectionWrapper;
        this.which = which;
        this.cssClass = cssClass;
    }

    /**
	 */
    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    String getGroupingName() {
        return selectionWrapper.getName();
    }

    int getWhichWeAre() {
        return which;
    }

    String getKey() {
        return selectionWrapper.getKey(which);
    }

    public String getValue() {
        return selectionWrapper.getValue(which);
    }

    public boolean isSelected() {
        return selectionWrapper.isSelected(which);
    }

    public String getCssClass() {
        return cssClass;
    }

    public int getAction() {
        return selectionWrapper.getAction();
    }

    public Form getRootForm() {
        return selectionWrapper.getRootForm();
    }

    public String getSelectionElementFormDisId() {
        return selectionWrapper.getFormDispatchId();
    }

    /**
     * Called if the value is already escape, this is only used for rendering.
     */
    public void setTrustedText() {
        this.isTrustedText = true;
    }

    /**
     * This information is typically used by the Renderer.
     */
    public boolean isTrustedText() {
        return isTrustedText;
    }

}
