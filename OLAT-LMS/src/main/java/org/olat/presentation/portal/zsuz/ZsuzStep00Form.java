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

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.wizard.StepFormBasicController;
import org.olat.presentation.framework.core.control.generic.wizard.StepsEvent;
import org.olat.presentation.framework.core.control.generic.wizard.StepsRunContext;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.mail.MailTemplate;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZsuzStep00Form
 * <P>
 * Initial Date: 09.06.2008 <br>
 * 
 * @author patrickb
 */
class ZsuzStep00Form extends StepFormBasicController {

    private IntegerElement copies;
    private SingleSelection color;
    private SingleSelection pickup;
    private SingleSelection print;
    private SingleSelection finish;
    private final static String[] COLORS_KEYS = new String[] { "form.color.bw", "form.color.color" };
    // if the order is not picked up, sending the order is done from irchel printing
    private final static String[] PICKUP_KEYS = new String[] { "form.pickup.irchel", "form.pickup.zentrum", "form.pickup.sending" };
    private static Identity[] PICKUP_IDS;
    private static final String[] PRINT_KEYS = new String[] { "form.print.oneside", "form.print.doubleside" };
    private static final String[] FINISH_KEYS = new String[] { "form.finish.none", "form.finish.bindung", "form.finish.lochung", "form.finish.scripts" };

    /**
     * @param ureq
     * @param control
     * @param rootForm
     * @param runContext
     * @param layout
     * @param customLayoutPageName
     */
    public ZsuzStep00Form(final UserRequest ureq, final WindowControl control, final Form rootForm, final StepsRunContext runContext, final int layout,
            final String customLayoutPageName) {
        super(ureq, control, rootForm, runContext, layout, customLayoutPageName);
        ZentralstelleIrchel irchel = new ZentralstelleIrchel();
        PICKUP_IDS = new Identity[] { irchel, new ZentralstelleZentrum(), irchel };

        flc.setTranslator(getTranslator());
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

        // form has no more errors
        // save info in run context for next step.
        final String copiesV = copies.getValue();
        final String colorV = color.getValue(color.getSelected());
        final String pickupV = pickup.getValue(pickup.getSelected());
        final Identity replyto = PICKUP_IDS[pickup.getSelected()];
        final String printV = print.getValue(print.getSelected());
        final String finishV = finish.getValue(finish.getSelected());
        addToRunContext("copies", copiesV);
        addToRunContext("color", colorV);
        addToRunContext("pickup", pickupV);
        addToRunContext("print", printV);
        addToRunContext("finish", finishV);

        @SuppressWarnings("unchecked")
        final List<String[]> userproperties = (List<String[]>) getFromRunContext("userproperties");

        final MailTemplate mailtemplate = createMailTemplate(userproperties, copiesV, colorV, pickupV, printV, finishV);
        addToRunContext("mailtemplate", mailtemplate);
        addToRunContext("replyto", replyto);
        // inform surrounding Step runner to proceed
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);

    }

    private MailTemplate createMailTemplate(final List<String[]> userprops, final String copiesV, final String colorV, final String pickupV, final String printV,
            final String finishV) {
        final String subject = translate("email.subject");
        final String body = translate("email.body");
        final Translator translator = getTranslator();
        final MailTemplate mt = new MailTemplate(subject, body, null, null) {
            private final String mycopiesV = copiesV;
            private final String mycolorV = colorV;
            private final String myprintV = printV;
            private final String myfinishV = finishV;
            private final String mypickupV = pickupV;
            private final Translator myTranslator = translator;

            @Override
            public void putVariablesInMailContext(final VelocityContext context, final OLATPrincipal recipient) {// Put user variables into velocity context

                context.put("firstname", recipient.getAttributes().getFirstName());
                context.put("lastname", recipient.getAttributes().getLastName());
                context.put("login", recipient.getName());
                // make translator available instead of putting translated keys into context
                context.put("t", myTranslator);
                final StringBuffer userPropsString = new StringBuffer();
                for (final String[] keyValue : userprops) {
                    userPropsString.append(" ");
                    userPropsString.append(keyValue[0]);
                    userPropsString.append(": ");
                    userPropsString.append(keyValue[1]);
                    userPropsString.append("\n");
                }
                context.put("userproperties", userPropsString);
                context.put("copies", mycopiesV);
                context.put("color", mycolorV);
                context.put("print", myprintV);
                context.put("finish", myfinishV);
                context.put("pickup", mypickupV);
            }
        };
        return mt;
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    @SuppressWarnings("unused")
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("step00.title");
        copies = uifactory.addIntegerElement("form.copies", 1, formLayout);
        copies.setMaxValueCheck(10, null);
        copies.setMinValueCheck(1, null);
        copies.setDisplaySize(2);

        print = uifactory.addRadiosVertical("form.print", formLayout, PRINT_KEYS, null);
        print.select("form.print.oneside", true);
        color = uifactory.addRadiosVertical("form.color", formLayout, COLORS_KEYS, null);
        color.select("form.color.bw", true);
        finish = uifactory.addRadiosVertical("form.finish", formLayout, FINISH_KEYS, null);
        finish.select("form.finish.none", true);
        pickup = uifactory.addRadiosVertical("form.pickup", formLayout, PICKUP_KEYS, null);
        pickup.select("form.pickup.irchel", true);

        uifactory.addStaticTextElement("form.konditionen", "form.label.konditionen", translate("form.konditionen"), formLayout);

    }

}
