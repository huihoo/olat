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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZsuzStepUserDataForm
 * <P>
 * Initial Date: 19.06.2008 <br>
 * 
 * @author patrickb
 */
public class ZsuzStepUserDataForm extends StepFormBasicController {

    private final static String FORMIDENTIFIER = ZsuzStepUserDataForm.class.getCanonicalName();
    private List<UserPropertyHandler> userPropertyHandlers;

    public ZsuzStepUserDataForm(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName) {
        super(ureq, control, rootForm, runContext, layout, customLayoutPageName);
        final Translator withUserProps = getUserService().getUserPropertiesConfig().getTranslator(getTranslator());
        setTranslator(withUserProps);
        flc.setTranslator(withUserProps);
        initForm(ureq);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // list with property-key-name and value for the MailTemplate in the next step
        final List<String[]> propsAndValues = new ArrayList<String[]>(userPropertyHandlers.size());
        //
        final BaseSecurity im = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        final Identity identity = im.findIdentityByName(getIdentity().getName());
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            final String propertyName = userPropertyHandler.getName();
            final FormItem fi = this.flc.getFormComponent(propertyName);
            final String propertyValue = userPropertyHandler.getStringValue(fi);
            // (propertyname, propertyvalue) for mailtemplate
            propsAndValues.add(new String[] { translate(userPropertyHandler.i18nFormElementLabelKey()), propertyValue });
            // set property value
            getUserService().setUserProperty(identity.getUser(), propertyName, propertyValue);
        }
        // save address information
        getUserService().updateUserFromIdentity(identity);
        //
        addToRunContext("userproperties", propsAndValues);
        // inform surrounding Step runner to proceed
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);

    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        // check users properties

        boolean isValid = true;

        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            if (userPropertyHandler == null) {
                continue;
            }
            // so far only textelement are supported in address field in this form here!
            final String compName = userPropertyHandler.getName();
            final TextElement currentTextelement = (TextElement) this.flc.getFormComponent(compName);
            final String currentPropValue = currentTextelement.getValue();
            if (currentTextelement.isMandatory() && currentPropValue.trim().equals("")) {
                currentTextelement.setErrorKey("new.form.mandatory", new String[] {});
                isValid = false;
            }
        }

        if (isValid) {
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }
        return isValid;
    }

    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        //
        // which fields are shown here is defined in
		// olat_userconfig.xml -> search for <entry key="ch.uzh.portal.zsuz.ZsuzStepUserDataForm">
        // validation of fields happens in validateFormLogic(..) and save/update is done
        // in formOK(..)
        //
        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(FORMIDENTIFIER, false);
        // Add all available user fields to this form
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            // adds the element to the formLayout
            userPropertyHandler.addFormItem(getLocale(), getIdentity().getUser(), FORMIDENTIFIER, false, formLayout);
        }

    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
