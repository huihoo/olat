package org.olat.presentation.user.administration.bulkchange;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.BulkPasswordChangeParameter;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.FormUIFactory;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This is an extension for the admin site for changing password for a user list (adding respectively OLAT authentication for the ones that doesn't have one).
 * <P>
 * Initial Date: 25.05.2010 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class UserBulkChangePasswordController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private final ChangePasswordForm changePasswordForm;

    public UserBulkChangePasswordController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        final Panel main = new Panel("changePsw");
        final VelocityContainer mainVC = createVelocityContainer("index");

        changePasswordForm = new ChangePasswordForm(ureq, wControl);
        this.listenTo(changePasswordForm);
        mainVC.put("form", changePasswordForm.getInitialComponent());

        main.setContent(mainVC);
        putInitialPanel(main);
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event == Event.DONE_EVENT) {
            final String[] usernames = changePasswordForm.getUsernames();
            final String password = changePasswordForm.getPassword();
            final boolean autodisc = changePasswordForm.getDisclaimerAccept();
            final boolean langGerman = changePasswordForm.getLangGerman();

            Identity identityMe = ureq.getIdentity();
            BulkPasswordChangeParameter passwordChangeParameter = new BulkPasswordChangeParameter(usernames, password, autodisc, langGerman, identityMe);

            int c = getBaseSecurityEBL().changePaswords(passwordChangeParameter);

            // notify done
            getWindowControl().setInfo(translate("bulk.psw.done", "" + c));

            // TODO: clear the form
            // changePasswordForm.clearForm(); //???
        }

    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * ChangePasswordForm.
     * <P>
     * Initial Date: 08.06.2010 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class ChangePasswordForm extends FormBasicController {

        private TextElement olatPasswordAuthentication;
        private TextElement userListTextArea;
        private SelectionElement acceptDisclaimer;
        private SelectionElement langGerman;
        private FormSubmit submitButton;

        public ChangePasswordForm(final UserRequest ureq, final WindowControl wControl) {
            super(ureq, wControl);

            initForm(ureq);
        }

        @Override
        protected void formOK(final UserRequest ureq) {
            fireEvent(ureq, Event.DONE_EVENT);
        }

        @Override
        protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

            userListTextArea = FormUIFactory.getInstance().addTextAreaElement("bulk.psw.users", 10, 2, null, formLayout);
            olatPasswordAuthentication = FormUIFactory.getInstance().addTextElement("pswtextfield", "bulk.psw.newpsw", 255, "", formLayout);
            acceptDisclaimer = FormUIFactory.getInstance().addCheckboxesVertical("bulk.auto.disc", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
            langGerman = FormUIFactory.getInstance().addCheckboxesVertical("bulk.lang.german", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);

            final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
            formLayout.add(buttonLayout);
            submitButton = FormUIFactory.getInstance().addFormSubmitButton("bulk.psw.submit", buttonLayout);

            acceptDisclaimer.select("xx", true);
            langGerman.select("xx", true);
        }

        private String[] getUsernames() {
            final String[] retVal = userListTextArea.getValue().split("\r\n");
            return retVal;
        }

        private String getPassword() {
            return olatPasswordAuthentication.getValue();
        }

        private boolean getDisclaimerAccept() {
            return acceptDisclaimer.isSelected(0);
        }

        private boolean getLangGerman() {
            return langGerman.isSelected(0);
        }

        @Override
        protected void doDispose() {
            if (olatPasswordAuthentication != null) {
                olatPasswordAuthentication.setValue("");
            }
            if (userListTextArea != null) {
                userListTextArea.setValue("");
            }
        }

    }

}
