/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.instantmessaging.groupchat;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * <P>
 * Initial Date: 26.03.2007 <br />
 * 
 * @author guido
 */
public class SendMessageForm extends FormBasicController {

    private TextElement msg;
    private FormLink submit;

    public SendMessageForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl, "sendMessageForm");
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        msg = uifactory.addTextElement("msg", null, 1024, null, formLayout);
        msg.setFocus(true);// always focus to the message field
        msg.setDisplaySize(40);
        submit = uifactory.addFormLink("subm", "msg.send", "msg.send", formLayout, Link.BUTTON);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == submit) {
            flc.getRootForm().submit(ureq);
        }
    }

    public String getMessage() {
        return msg.getValue();
    }

    public void resetTextField() {
        msg.setValue("");
    }
}
