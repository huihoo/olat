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

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormItemImpl;

/**
 * Description:<br>
 * TODO: patrickb Class Description for StaticTextElementImpl
 * <P>
 * Initial Date: 02.02.2007 <br>
 * 
 * @author patrickb
 */
public class StaticTextElementImpl extends FormItemImpl implements StaticTextElement {

    private String value;
    private StaticTextElementComponent component;

    public StaticTextElementImpl(String name, String value) {
        super(name);
        this.value = value;
        this.component = new StaticTextElementComponent(this);
    }

    @Override
    @SuppressWarnings("unused")
    public void evalFormRequest(UserRequest ureq) {
        // static text must not evaluate
    }

    @Override
    @SuppressWarnings("unused")
    public void validate(List validationResults) {
        // static text must not validate
    }

    @Override
    public void reset() {
        // static text can not be resetted
    };

    @Override
    protected Component getFormItemComponent() {
        return component;
    }

    @Override
    protected void rootFormAvailable() {
        // root form not interesting for Static text
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String replacementValue) {
        this.value = replacementValue;
        this.getFormItemComponent().setDirty(true);
    }

    /**
     * Tells the component that the value is already escape, this is only used for rendering. <br>
     * The <code>value</code> contains html tags, so we want to preserve them.
     */
    public void setTrustedText() {
        component.setTrustedText();
    }

}
