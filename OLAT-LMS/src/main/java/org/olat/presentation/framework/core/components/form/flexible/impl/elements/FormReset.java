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
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.elements.Reset;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;

/**
 * @author patrickb
 */
public class FormReset extends FormButton implements Reset {

    private String i18nKey;
    private FormButtonComponent component;

    public FormReset(String name, String i18nKey) {
        super(name);
        this.i18nKey = i18nKey;
        this.action = FormEvent.ONCLICK;
    }

    /**
	 */
    @Override
    public void dispatchFormRequest(UserRequest ureq) {
        if (getRootForm().getAction() == FormEvent.ONCLICK) {
            getRootForm().reset(ureq);
        }
    }

    @Override
    protected Component getFormItemComponent() {
        return component;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected void rootFormAvailable() {
        component = new FormButtonComponent(this);
    }

    /**
	 */
    @Override
    String getTranslated() {
        return getTranslator().translate(i18nKey);
    }

}
