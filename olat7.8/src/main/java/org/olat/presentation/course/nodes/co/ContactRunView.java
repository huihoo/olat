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
package org.olat.presentation.course.nodes.co;

import org.olat.lms.commons.mail.ContactMessage;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.contactform.ContactFormView;
import org.olat.presentation.contactform.ContactUIModel;
import org.olat.presentation.course.nodes.ObjectivesHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicView;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * This creates a contact message sending form in case a contact message is set. In the case where no contact message is defined it shows an info message that no
 * recipients are defined.
 * 
 * Initial Date: Oct 5, 2011 <br>
 * 
 * @author patrick
 */
public class ContactRunView extends BasicView {

    private VelocityContainer myContent;
    private ContactMessage courseContactMessage = null;

    /**
     * @param ureq
     * @param wControl
     */
    public ContactRunView(UserRequest ureq, WindowControl wControl, String pageName) {
        super(ureq, wControl);
        myContent = createVelocityContainer("run");
    }

    /**
     * @param shortTitle
     */
    public void setShortTitle(String shortTitle) {
        myContent.contextPut("menuTitle", shortTitle);
    }

    /**
     * @param longTitle
     */
    public void setLongTitle(String longTitle) {
        myContent.contextPut("displayTitle", longTitle);
    }

    /**
     * @param learningObjectives
     */
    public void setLearninObjectives(String learningObjectives) {
        final Panel panel = new Panel("panel");
        myContent.put("learningObjectives", panel);
        if (learningObjectives != null) {
            final Component learningObjectivesComponent = ObjectivesHelper.createLearningObjectivesComponent(learningObjectives, getLocale());
            panel.setContent(learningObjectivesComponent);
        }
    }

    /**
     * 
     * @param courseContactMessage
     * @param coRunController
     */
    public void setCourseContactMessage(ContactMessage courseContactMessage) {
        this.courseContactMessage = courseContactMessage;
    }

    /**
     * seam to brasato
     * 
     * @param listeningController
     * @return
     */
    public Component getInitialComponent(DefaultController listeningController) {
        Controller viewController;
        if (courseContactMessage != null) {
            ContactUIModel contactUImodel = new ContactUIModel(courseContactMessage);
            ContactFormView contactFormView = new ContactFormView(ureq, wControl, courseContactMessage.getFrom(), contactUImodel.hasAtLeastOneAddress(), false, false,
                    false);
            viewController = new ContactFormController(contactFormView, contactUImodel);
        } else {
            String message = translate("error.msg.send.no.rcps");
            viewController = MessageUIFactory.createInfoMessage(ureq, wControl, null, message);
        }

        listenTo(listeningController, viewController);

        return viewController.getInitialComponent();
    }

}
