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
package org.olat.presentation.portal.zsuz;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerWithTemplate;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZsuzStep01Form
 * <P>
 * Initial Date: 19.06.2008 <br>
 * 
 * @author patrickb
 */
public class ZsuzStep01Form extends StepFormBasicController implements StepFormController {

    public ZsuzStep01Form(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName) {
        super(ureq, control, rootForm, runContext, layout, customLayoutPageName);
        initForm(ureq);
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
        // inform surrounding Step runner to proceed
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);

    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final MailTemplate template = (MailTemplate) getFromRunContext("mailtemplate");
        final Identity replyto = (Identity) getFromRunContext("replyto");
        final String[] subjectAndBody = MailerWithTemplate.getInstance().previewSubjectAndBody(ureq.getIdentity(), null, null, template, replyto);
        // add disabled textelements.
        final String email = getUserService().getUserProperty(getIdentity().getUser(), UserConstants.EMAIL, getLocale());
        uifactory.addStaticTextElement("form.howtoproceed", null, translate("form.howtoproceed", email), formLayout);
        uifactory.addStaticExampleText("form.subject", subjectAndBody[0], formLayout);
        uifactory.addStaticExampleText("form.email", subjectAndBody[1].replaceAll("\n", "<br>"), formLayout);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
