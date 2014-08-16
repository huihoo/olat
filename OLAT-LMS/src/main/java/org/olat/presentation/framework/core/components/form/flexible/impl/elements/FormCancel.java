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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.elements.Cancel;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormItemImpl;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.Disposable;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * The form cancel triggers the form cancelled event and bypasses the normal form event infrastructure by using a conventional link and an inner controller that
 * dispatches the link event and forwards it as an inner form event.
 * <P>
 * Initial Date: 06.07.2009 <br>
 * 
 * @author gnaegi
 */
public class FormCancel extends FormItemImpl implements Disposable, Cancel {
    private final Link cancelLink;
    private Controller dispatchLinkController;
    private final FormCancel self;

    public FormCancel(String name, FormLayoutContainer formLayoutContainer, UserRequest ureq, WindowControl wControl) {
        super(name);
        self = this;
        // Create inner link dispatch controller as a hack to catch event that
        // should bypass the form infrastructure. This inner controller is
        // disposed by this form item.
        dispatchLinkController = new BasicController(ureq, wControl) {
            @Override
            protected void doDispose() {
                // nothing to dispose
            }

            @Override
            protected void event(UserRequest ureq, Component source, Event event) {
                if (source == cancelLink) {
                    getRootForm().fireFormEvent(ureq, new FormEvent(Form.EVNT_FORM_CANCELLED, self, -1));
                }
            }
        };
        // The link component that is used by this form element
        cancelLink = LinkFactory.createButton("cancel", (VelocityContainer) formLayoutContainer.getComponent(), dispatchLinkController);
        cancelLink.setSuppressDirtyFormWarning(true);
    }

    /**
	 */
    @Override
    public void evalFormRequest(UserRequest ureq) {
        // nothing to do
    }

    /**
	 */
    @Override
    protected Component getFormItemComponent() {
        return cancelLink;
    }

    /**
	 */
    @Override
    public void reset() {
        // nothing to do
    }

    /**
	 */
    @Override
    protected void rootFormAvailable() {
        // nothing to do
    }

    /**
	 */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(List validationResults) {
        // nothing to do
    }

    /**
	 */
    @Override
    public void setCustomDisabledLinkCSS(String customDisabledLinkCSS) {
        cancelLink.setCustomDisabledLinkCSS(customDisabledLinkCSS);
    }

    /**
	 */
    @Override
    public void setCustomEnabledLinkCSS(String customEnabledLinkCSS) {
        cancelLink.setCustomEnabledLinkCSS(customEnabledLinkCSS);
    }

    /**
	 */
    @Override
    public void setI18nKey(String i18n) {
        cancelLink.setTitle(i18n);
    }

    /**
	 */
    @Override
    public void dispose() {
        if (dispatchLinkController != null) {
            dispatchLinkController.dispose();
            dispatchLinkController = null;
        }
    }
}
