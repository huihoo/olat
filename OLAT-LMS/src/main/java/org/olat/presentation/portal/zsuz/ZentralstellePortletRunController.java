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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.Step;
import org.olat.presentation.framework.core.control.generic.wizard.StepRunnerCallback;
import org.olat.presentation.framework.core.control.generic.wizard.StepsMainRunController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.MailerResult;
import org.olat.system.mail.MailerWithTemplate;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZentralstellePortletRunController
 * <P>
 * Initial Date: 06.06.2008 <br>
 * 
 * @author patrickb
 */
class ZentralstellePortletRunController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer mainVC;
    private final Link startWizardButton;
    private StepsMainRunController smrc;

    protected ZentralstellePortletRunController(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        mainVC = createVelocityContainer("mainzusz");
        startWizardButton = LinkFactory.createButton("startWizard", mainVC, this);
        putInitialPanel(mainVC);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose

    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startWizardButton) {
            final Step startStep = new ZsuzStepUserData(ureq);

            final StepRunnerCallback finishCallback = new StepRunnerCallback() {
                @Override
                @SuppressWarnings({ "synthetic-access", "unused" })
                public Step execute(final UserRequest ureq2, final WindowControl control, final StepsRunContext runContext) {
                    final MailTemplate mailtemplate = (MailTemplate) runContext.get("mailtemplate");
                    // fetch data from runContext and send eMail with it
                    final MailerWithTemplate mailer = MailerWithTemplate.getInstance();
                    final Identity replyto = (Identity) runContext.get("replyto");
                    final MailerResult mr = mailer.sendMail(ureq2.getIdentity(), null, null, mailtemplate, replyto);
                    log.info("Audit:DRUCKEREI-TEMPLATE-ERSTELLT", null);
                    if (mr.getReturnCode() == MailerResult.OK) {
                        return StepsMainRunController.DONE_UNCHANGED;
                    } else {
                        return Step.NOSTEP;
                    }
                }

            };
            smrc = new StepsMainRunController(ureq, getWindowControl(), startStep, finishCallback, null, translate("wizard.title"));
            listenTo(smrc);
            getWindowControl().pushAsModalDialog(smrc.getInitialComponent());
        }
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == smrc) {
            // remove wizard
            getWindowControl().pop();
            removeAsListenerAndDispose(smrc);
            if (event == Event.CANCELLED_EVENT) {
                // cancelled nothing to do
            } else if (event == Event.DONE_EVENT) {
                showInfo("email.sent");
            }
        }
    }

}
